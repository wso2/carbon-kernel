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
package org.wso2.carbon.utils.deployment;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DescriptionBuilder;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.deployment.ServiceGroupBuilder;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.FaultyServiceData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.utils.IOStreamUtils;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.wso2.carbon.utils.WSO2Constants.BUNDLE_ID;

/**
 *
 */
public class Axis2ServiceRegistry {
    private static Log log = LogFactory.getLog(Axis2ServiceRegistry.class);

    private ConfigurationContext configCtx;

    private AxisConfiguration axisConfig;

//    private final Lock lock = new ReentrantLock();

    private Map<Bundle, List<AxisServiceGroup>> serviceGroupMap;

    private static String componentsDirPath;

    static {
        componentsDirPath = System.getProperty(CarbonBaseConstants.CARBON_COMPONENTS_DIR_PATH);
        if (componentsDirPath == null) {
            String carbonRepo = System.getenv("CARBON_REPOSITORY");
            if (carbonRepo == null) {
                carbonRepo = System.getProperty("carbon.repository");
            }
            if (carbonRepo == null) {
                carbonRepo = Paths.get(System.getProperty("carbon.home"), "repository").toString();
            }
            componentsDirPath = Paths.get(carbonRepo, "components").toString();
        }
    }

    public Axis2ServiceRegistry(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
        this.axisConfig = configCtx.getAxisConfiguration();
        this.serviceGroupMap = new ConcurrentHashMap<Bundle, List<AxisServiceGroup>>();
    }

    public void register(Bundle[] bundles) {
        for (Bundle bundle : bundles) {
            if (bundle.getState() == Bundle.ACTIVE) {
                register(bundle);
            }
        }
        //At this point all the services in OSGi bundles are deployed.
        //Checking whether there are any faulty service due to modules and transports.
        logFaultyServiceInfo();
    }

    /**
     * @param bundle Carbon Compoent that needs to be registred
     */
    public void register(Bundle bundle) {
        try {
            addServices(bundle);
        } catch (Throwable e) {
            String msg =
                    "Error while adding services from bundle : " +
                            bundle.getSymbolicName() + "-" + bundle.getVersion();
            log.error(msg, e);
        }
    }

    public void unregister(Bundle bundle) {
        try {
            List<AxisServiceGroup> axisServiceGroupList = serviceGroupMap.get(bundle);
            if (axisServiceGroupList != null) {
                for (AxisServiceGroup axisServiceGroup : axisServiceGroupList) {
                    try {
                        axisConfig.removeServiceGroup(axisServiceGroup.getServiceGroupName());
                        if (log.isDebugEnabled()) {
                            log.debug("Stopping" +
                                    axisServiceGroup.getServiceGroupName() +
                                    " service group in Bundle - " +
                                    bundle.getSymbolicName());
                        }
                        for (Iterator<AxisService> iterator = axisServiceGroup.getServices();
                             iterator.hasNext();) {
                            AxisService service = iterator.next();
                            if (log.isDebugEnabled()) {
                                log.debug("Service - " + service.getName());
                            }
                        }
                    } catch (AxisFault e) {
                        String msg =
                                "Error while removing services from bundle : " +
                                        bundle.getBundleId();
                        log.error(msg, e);
                    }
                }
                serviceGroupMap.remove(bundle);
            }
        } catch (Exception e) {
            String msg =
                    "Error while unregistering  bundle : " +
                            bundle.getBundleId();
            log.error(msg, e);
        }
    }

    public void bundleChanged(BundleEvent event) {
        Bundle bundle = event.getBundle();
        switch (event.getType()) {
            case BundleEvent.STARTED:
                register(bundle);
                break;

            case BundleEvent.STOPPED:
                unregister(bundle);
                break;
        }
    }

    private void addServices(Bundle bundle) {
        if (!serviceGroupMap.containsKey(bundle)) {
            Enumeration enumeration = bundle.findEntries("META-INF", "*services.xml", true);
            List<AxisServiceGroup> axisServiceGroupList = null;
            if (enumeration != null) {
                axisServiceGroupList = new ArrayList<AxisServiceGroup>();
            }
            while (enumeration != null && enumeration.hasMoreElements()) {
                try {
                    ClassLoader loader =
                            new BundleClassLoader(bundle, Axis2ServiceRegistry.class.getClassLoader());
                    URL url = (URL) enumeration.nextElement();
                    AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);

                    // Here we are using Bundle-SymbolicName combined with Bundle-Version as the ServiceGroupName
                    Dictionary headers = bundle.getHeaders();
                    String bundleSymbolicName = (String) headers.get("Bundle-SymbolicName");
                    String bundleVersion = (String) headers.get("Bundle-Version");
                    serviceGroup.setServiceGroupName(bundleSymbolicName + "-" + bundleVersion);

                    serviceGroup.addParameter(BUNDLE_ID, bundle.getBundleId());
                    serviceGroup.addParameter("last.updated", bundle.getLastModified());
                    serviceGroup.setServiceGroupClassLoader(loader);
                    InputStream inputStream = url.openStream();
                    DescriptionBuilder builder = new DescriptionBuilder(inputStream, configCtx);
                    OMElement rootElement = builder.buildOM();
                    String elementName = rootElement.getLocalName();
                    HashMap<String,AxisService> wsdlServicesMap = processWSDL(bundle);
                    if (wsdlServicesMap != null && wsdlServicesMap.size() > 0) {
                        for (AxisService service : wsdlServicesMap.values()) {
                            Iterator<AxisOperation> operations = service.getOperations();
                            while (operations.hasNext()) {
                                AxisOperation axisOperation = operations.next();
                                axisConfig.getPhasesInfo().setOperationPhases(axisOperation);
                            }
                        }
                    }
                    if (DeploymentConstants.TAG_SERVICE.equals(elementName)) {
                        AxisService axisService = new AxisService(bundleSymbolicName);
                        axisService.setParent(serviceGroup);
                        axisService.setClassLoader(loader);
                        ServiceBuilder serviceBuilder = new ServiceBuilder(configCtx, axisService);
                        serviceBuilder.setWsdlServiceMap(wsdlServicesMap);
                        AxisService service = serviceBuilder.populateService(rootElement);
                        ArrayList<AxisService> serviceList = new ArrayList<AxisService>();
                        serviceList.add(service);
                        DeploymentEngine.addServiceGroup(serviceGroup,
                                serviceList,
                                null,
                                null,
                                axisConfig);
                        if (log.isDebugEnabled()) {
                            log.debug("Deployed axis2 service:" + service.getName() +
                                    " in Bundle: " +
                                    bundle.getSymbolicName());
                        }
                    } else if (DeploymentConstants.TAG_SERVICE_GROUP.equals(elementName)) {
                        ServiceGroupBuilder groupBuilder =
                                new ServiceGroupBuilder(rootElement, wsdlServicesMap,
                                        configCtx);
                        ArrayList<? extends AxisService> serviceList = groupBuilder.populateServiceGroup(serviceGroup);
                        DeploymentEngine.addServiceGroup(serviceGroup,
                                serviceList,
                                null,
                                null,
                                axisConfig);
                        if (log.isDebugEnabled()) {
                            log.debug("Deployed axis2 service group:" +
                                    serviceGroup.getServiceGroupName() + " in Bundle: " +
                                    bundle.getSymbolicName());
                        }
                    }
                    axisServiceGroupList.add(serviceGroup);
                } catch (Exception e) {
                    String msg = "Error building service from bundle : " +
                                 "Symbolic Name: " + bundle.getSymbolicName() +
                                 ",Bundle Version: " + bundle.getVersion() + 
                                 ", ID: " + bundle.getBundleId();
                    log.error(msg, e);
                }
            }
            if (axisServiceGroupList != null && axisServiceGroupList.size() > 0) {
                serviceGroupMap.put(bundle, axisServiceGroupList);
            }
        }
    }

    private HashMap processWSDL(Bundle bundle) throws IOException, XMLStreamException {
        Enumeration enumeration = bundle.findEntries("META-INF", "*.wsdl", true);
        if (enumeration == null) {
            return new HashMap();
        }

        String bundleLocation = bundle.getLocation();
        // Sometimes value of the bundleLocation can be a string such as the following.
        // reference:file:plugins/org.wso2.carbon.statistics-3.2.0.jar
        // In these situations we need to remove the "reference:" part from the bundleLocation.
        if (bundleLocation.startsWith("reference:")) {
            bundleLocation = bundleLocation.substring("reference:".length());
        }

        // Extracting bundle file name.
        String[] subStrings = bundleLocation.split("/");
        String bundleFileName = subStrings[subStrings.length - 1];

        File bundleFile;
        URL bundleURL = new URL(bundleLocation);

        if (bundleURL.getProtocol().equals("file")) {
            bundleFile = new File(bundleURL.getFile());
        } else {
            InputStream bundleStream = bundleURL.openStream();

            // Generate temp file path for the bundle.
            String tempBundleDirPath = System.getProperty("java.io.tmpdir") + File.separator + "bundles";

            //Creating a temp dir to store bundles.
            File tempBundleDir = new File(tempBundleDirPath);
            if (!tempBundleDir.exists() && !tempBundleDir.mkdir()) {
                log.warn("Could not create temp bundle directory " + tempBundleDir.getAbsolutePath());
                return new HashMap();
            }

            bundleFile = new File(tempBundleDirPath, bundleFileName);
            OutputStream bundleFileOutputSteam = new FileOutputStream(bundleFile);

            // Copying input stream to the file output stream
            IOStreamUtils.copyInputStream(bundleStream, bundleFileOutputSteam);
        }

        if (!bundleFile.exists()) {
            //If the bundle does not exits, then we check in the plugins dir.
            String file = bundleURL.getFile();
            if (file.startsWith("..")) {
                file = file.substring("..".length());
            }
            bundleFile = new File(componentsDirPath + File.separator + file);
        }

        if (!bundleFile.exists()) {
            return new HashMap();
        }

        DeploymentFileData deploymentFileData = new DeploymentFileData(bundleFile);
        ArchiveReader archiveReader = new ArchiveReader();
        return archiveReader.processWSDLs(deploymentFileData);
    }



    public void logFaultyServiceInfo() {
        Map<String, Map<String, FaultyServiceData>> faultyServices = axisConfig.getFaultyServicesDuetoModules();
        if (faultyServices != null && !faultyServices.isEmpty()) {
            for(Map.Entry<String, Map<String, FaultyServiceData>> moduleEntry : faultyServices.entrySet()){
                Map<String, FaultyServiceData> faultyServicesDueToModule = moduleEntry.getValue();
                for (Map.Entry<String, FaultyServiceData> entry : faultyServicesDueToModule.entrySet()) {
                    AxisServiceGroup serviceGroup = entry.getValue().getServiceGroup();
                    //Check whether this is service is deployed from an OSGi bundle, if not this faluty services info is
                    //already logged by axis2.
                    if(serviceGroup.getParameter(BUNDLE_ID) != null){
                        log.warn("Service :" + serviceGroup.getServiceGroupName() +
                                 " is faulty, due to unavailability of the module :" + moduleEntry.getKey());
                    }
                }
            }
        }
    }
}
