/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.osgi.deployment;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.*;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import static org.apache.axis2.osgi.deployment.OSGiAxis2Constants.MODULE_NOT_FOUND_ERROR;
import static org.apache.axis2.osgi.deployment.OSGiAxis2Constants.OSGi_BUNDLE_ID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Creates proper AxisServiceGroup/AxisService looking into bundles
 */
public class ServiceRegistry extends AbstractRegistry<AxisServiceGroup> {

    private static Log log = LogFactory.getLog(ServiceRegistry.class);

    public ServiceRegistry(BundleContext context, ConfigurationContext configCtx) {
        super(context, configCtx);
    }

    public void register(Bundle bundle) {
        lock.lock();
        try {
            addServices(bundle);
        } finally {
            lock.unlock();
        }
    }

    /**
     * When a bundle is started this method will look for xml files that suffix with "services.xml".
     * Thus, a given bundle can have n number of *services.xml.
     * Ex: my1services.xml and my2_services.xml.
     * <p/>
     * Due to security consideration, if one *services.xml fail, all the services will treated as fail.
     *
     * @param bundle registered bundle
     */
    private void addServices(Bundle bundle) {
        if (!resolvedBundles.containsKey(bundle)) {
            Enumeration enumeration = bundle.findEntries("META-INF", "*services.xml", false);
            int i = 0;
            List<AxisServiceGroup> axisServiceGroupList = null;
            if (enumeration != null) {
                axisServiceGroupList = new ArrayList<AxisServiceGroup>();
            }
            while (enumeration != null && enumeration.hasMoreElements()) {
                try {
                    URL url = (URL) enumeration.nextElement();
                    AxisServiceGroup serviceGroup =
                            new AxisServiceGroup(configCtx.getAxisConfiguration());
                    serviceGroup.addParameter("last.updated", bundle.getLastModified());
                    ClassLoader loader =
                            new BundleClassLoader(bundle, Registry.class.getClassLoader());
                    serviceGroup.setServiceGroupClassLoader(loader);
                    InputStream inputStream = url.openStream();
                    DescriptionBuilder builder = new DescriptionBuilder(inputStream, configCtx);
                    OMElement rootElement = builder.buildOM();
                    String elementName = rootElement.getLocalName();
                    Dictionary headers = bundle.getHeaders();
                    String bundleSymbolicName = (String) headers.get("Bundle-SymbolicName");
                    bundleSymbolicName = bundleSymbolicName + "_" + i;
                    serviceGroup.setServiceGroupName(bundleSymbolicName);
                    HashMap wsdlServicesMap = new HashMap();
                    if (DeploymentConstants.TAG_SERVICE.equals(elementName)) {
                        AxisService axisService = new AxisService(bundleSymbolicName);
                        axisService.setParent(serviceGroup);
                        axisService.setClassLoader(loader);
                        ServiceBuilder serviceBuilder =
                                new OSGiServiceBuilder(configCtx, axisService);
                        serviceBuilder.setWsdlServiceMap(wsdlServicesMap);
                        AxisService service = serviceBuilder.populateService(rootElement);
                        ArrayList<AxisService> serviceList = new ArrayList<AxisService>();
                        serviceList.add(service);
                        DeploymentEngine.addServiceGroup(serviceGroup,
                                                         serviceList,
                                                         null,
                                                         null,
                                                         configCtx.getAxisConfiguration());
                        log.info("[Axis2/OSGi] Deployed axis2 service:" + service.getName() +
                                 " in Bundle: " +
                                 bundle.getSymbolicName());
                    } else if (DeploymentConstants.TAG_SERVICE_GROUP.equals(elementName)) {
                        ServiceGroupBuilder groupBuilder =
                                new OSGiServiceGroupBuilder(rootElement, wsdlServicesMap,
                                                            configCtx);
                        ArrayList serviceList = groupBuilder.populateServiceGroup(serviceGroup);
                        DeploymentEngine.addServiceGroup(serviceGroup,
                                                         serviceList,
                                                         null,
                                                         null,
                                                         configCtx.getAxisConfiguration());
                        log.info("[Axis2/OSGi] Deployed axis2 service group:" +
                                 serviceGroup.getServiceGroupName() + " in Bundle: " +
                                 bundle.getSymbolicName());
                    }
                    //bundle Id keeps the association between bundle and axisService group for later use
                    serviceGroup.addParameter(OSGi_BUNDLE_ID, bundle.getBundleId());
                    axisServiceGroupList.add(serviceGroup);
                    //marked as resolved.
                    if (unreslovedBundles.contains(bundle)) {
                        unreslovedBundles.remove(bundle);
                    }
                    i++;
                } catch (Throwable e) {
                    String msg = "Error while reading from the bundle";
                    if (e instanceof DeploymentException) {
                        String message = e.getMessage();
                        if (message != null && message.length() != 0) {
                            if (message.indexOf(MODULE_NOT_FOUND_ERROR) > -1) {
                                if (!unreslovedBundles.contains(bundle)) {
                                    log.info("A service being found with unmeant module " +
                                             "dependency. Hence, moved it to UNRESOLVED state.");
                                    unreslovedBundles.add(bundle);
                                } else {
                                    log.info("A service being found in UNRESOLVED state.");
                                }
                            } else {
                                log.error(msg, e);
                                break;
                            }
                        } else {
                            log.error(msg, e);
                            break;
                        }
                    } else {
                        log.error(msg, e);
                        break;
                    }
                }
            }
            if (axisServiceGroupList != null && axisServiceGroupList.size() > 0) {
                resolvedBundles.put(bundle, axisServiceGroupList);
            }
        }

    }

    public void unRegister(Bundle bundle, boolean uninstall) {
        lock.lock();
        try {
            List<AxisServiceGroup> axisServiceGroupList = resolvedBundles.get(bundle);
            if (axisServiceGroupList != null) {
                for (AxisServiceGroup axisServiceGroup : axisServiceGroupList) {
                    if (resolvedBundles.containsKey(bundle)) {
                        resolvedBundles.remove(bundle);
                    }
                    if (!unreslovedBundles.contains(bundle) && !uninstall) {
                        unreslovedBundles.add(bundle);
                    }
                    try {
                        for (Iterator iterator = axisServiceGroup.getServices();
                             iterator.hasNext();) {
                            AxisService service = (AxisService) iterator.next();
                            log.info("[Axis2/OSGi] Service - " + service.getName());
                        }
                        configCtx.getAxisConfiguration()
                                .removeServiceGroup(axisServiceGroup.getServiceGroupName());
                        log.info("[Axis2/OSGi] Stopping " +
                                 axisServiceGroup.getServiceGroupName() +
                                 " service group in Bundle - " +
                                 bundle.getSymbolicName());
                    } catch (AxisFault e) {
                        String msg = "Error while removing the service group";
                        log.error(msg, e);
                    }

                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void remove(Bundle bundle) {
        unRegister(bundle, true);
        lock.lock();
        try {
            if (resolvedBundles.containsKey(bundle)) {
                resolvedBundles.remove(bundle);
            }
            if (unreslovedBundles.contains(bundle)) {
                unreslovedBundles.remove(bundle);
            }
        } finally {
            lock.unlock();
        }
    }
}
