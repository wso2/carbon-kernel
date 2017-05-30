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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.Version;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.wso2.carbon.CarbonConstants;

import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Axis2ModuleRegistry {
    private static Log log = LogFactory.getLog(Axis2ModuleRegistry.class);

    private AxisConfiguration axisConfig;

    private final Lock lock = new ReentrantLock();

    private Map<Bundle, AxisModule> moduleMap;

    public Axis2ModuleRegistry(AxisConfiguration axisConfiguration){
        this.axisConfig = axisConfiguration;
        this.moduleMap = new ConcurrentHashMap<Bundle, AxisModule>();
    }

    public void register(Bundle[] bundles) {
        for (Bundle bundle : bundles) {
            register(bundle);
        }
    }

    /**
     * Deploy the module inside the bundle and gloabally engaged them, if specified
     * @param bundle         from which the module should be looked for.
     */
    public void register(Bundle bundle) {
        lock.lock();
        try {
            if (moduleMap.containsKey(bundle)) {
                return;
            }

            Enumeration enumeration = bundle.findEntries("META-INF", "module.xml", true);
            while (enumeration != null && enumeration.hasMoreElements()) {
                URL xmlURL = (URL) enumeration.nextElement();               
                //Populate the AxisModule
                AxisModule axisModule = populateModule(bundle, xmlURL);

                //Deploy AxisModule in AxisConfiguration
                deployModule(axisModule);
                moduleMap.put(bundle, axisModule);
                if(log.isDebugEnabled()){
                    log.debug("Deploying Module: " + axisModule.getName() + "-" + axisModule.getVersion() +
                              " in bundle " + bundle.getSymbolicName());
                }
            }
        } catch (Throwable e) {
            String msg = "Error while deploying the module from bunlde : " + bundle.getSymbolicName() + "-" +
                    bundle.getVersion();
            log.error(msg, e);
        } finally {
            lock.unlock();
        }
    }

    public void unRegister(Bundle bundle) {
        lock.lock();
        try {
            AxisModule axisModule = moduleMap.get(bundle);
            if (axisModule != null) {
                axisConfig.removeModule(axisModule.getName(), axisModule.getVersion());
                if (log.isDebugEnabled()) {
                    log.debug("Stopping" + axisModule.getName() + ":" +
                            axisModule.getVersion() + " moduel in Bundle - " +
                            bundle.getSymbolicName());
                }
                moduleMap.remove(bundle);
            }
        } finally {
            lock.unlock();
        }
    }

    public void bundleChanged(BundleEvent event) {
        switch (event.getType()) {
            case BundleEvent.STOPPED:
                //TODO implement this
                break;
        }
    }

    private void deployModule(AxisModule axisModule) throws AxisFault {
        // set in default map if necessary
        HashMap moduleMap = new HashMap(1);
        moduleMap.put(axisModule.getName(), axisModule);
        Utils.calculateDefaultModuleVersion(moduleMap, axisConfig);

        DeploymentEngine.addNewModule(axisModule, axisConfig);
    }

    private AxisModule populateModule(Bundle bundle, URL xmlURL) throws Exception {
        ClassLoader loader =
                new BundleClassLoader(bundle, Axis2ModuleRegistry.class.getClassLoader());
        AxisModule axisModule = new AxisModule();
        axisModule.setModuleClassLoader(loader);
        ModuleBuilder builder = new ModuleBuilder(xmlURL.openStream(), axisModule, axisConfig);

        //Setting module name and version as the bundle symbolic name and version
        setModuleNameAndVersion(bundle, axisModule);

        builder.populateModule();
        axisModule.setParent(axisConfig);
        AxisModule module = axisConfig.getModule(axisModule.getName());

        if (module != null) {
            log.warn("Module : " + axisModule.getName() + "-" + axisModule.getVersion() +
                    " is already available.");
        }
        return axisModule;
    }

    private void setModuleNameAndVersion(Bundle bundle, AxisModule axismodule) throws AxisFault {
        Dictionary headers = bundle.getHeaders();
        String bundleSymbolicName = (String) headers.get("Bundle-SymbolicName");
        String bundleVersion = (String) headers.get("Bundle-Version");

        if (bundleSymbolicName != null && bundleSymbolicName.length() != 0) {
            axismodule.setName(bundleSymbolicName);
        }

        if (bundleVersion != null && bundleVersion.length() != 0) {
            String moduleVersion = "SNAPSHOT";
            /*
               Bundle version is defined as
               version ::=
                   major( '.' minor ( '.' micro ( '.' qualifier )? )? )?
                   major ::= number
                   minor ::= number
                   micro ::= number
                   qualifier ::= ( alphanum | ’_’ | '-' )+

               Hence, in order to sync up with Axis2 module versioning, which is a floating
               point number, following logic is used to create the version
               version := major(.minormircor)
            */
            String result;
            String[] versionSplit = bundleVersion.split("\\.");
            if (versionSplit.length == 4) {
               moduleVersion = versionSplit[0] + "." + versionSplit[1] + versionSplit[2] + "-" + versionSplit[3];
            } else if (versionSplit.length == 3) {
            	result = versionSplit[2];

			if (result.matches(".*\\d.*")) {
				moduleVersion = versionSplit[0] + "." + versionSplit[1]
						+ "." + result;
			} else {
				moduleVersion = versionSplit[0] + "." + versionSplit[1]
						+ "-" + result;
			}
			  } else if (versionSplit.length == 2) {
				  result = versionSplit[1];

					if (result.matches(".*\\d.*")) {
						moduleVersion = versionSplit[0] + "." + result;
					} else {
						moduleVersion = versionSplit[0] + "-" + result;
					}
					moduleVersion = versionSplit[0] + "." + versionSplit[1];
            } else if (versionSplit.length == 1) {
                moduleVersion = versionSplit[0];
            }

            try {
                axismodule.setVersion(new Version(moduleVersion));
            } catch (Exception e) {
                String msg = "Error while setting the version " + moduleVersion + " for the " +
                        "module : " + axismodule.getName();
                log.error(msg);
                throw new AxisFault(msg, e);
            }

            //Set a parameter to identiry this module was deployed through a bundle
            axismodule.addParameter(createManagedModuleParam());
        }
    }

    /**
     * Parameter to identify modules deployed by bundles
     *
     * @return
     */
    private Parameter createManagedModuleParam() {
        Parameter parameter = new Parameter(CarbonConstants.MANAGED_MODULE_PARAM_NAME, Boolean.TRUE.toString());
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("", "");

        OMElement paramEle = fac.createOMElement("parameter", ns);
        paramEle.addAttribute("name", CarbonConstants.MANAGED_MODULE_PARAM_NAME, ns);
        paramEle.setText(Boolean.TRUE.toString());
        parameter.setParameterElement(paramEle);
        return parameter;
    }
}
