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

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.wso2.carbon.utils.component.xml.Component;
import org.wso2.carbon.utils.component.xml.ComponentConfigFactory;
import org.wso2.carbon.utils.component.xml.ComponentConstants;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Axis2DeployerRegistry implements BundleListener {
    private static Log log = LogFactory.getLog(Axis2DeployerRegistry.class);

    private DeploymentEngine deploymentEngine;

    private final Lock lock = new ReentrantLock();

    private Map<Bundle, Deployer> deployerMap;

    public Axis2DeployerRegistry(AxisConfiguration axisConfiguration) {
        deployerMap = new HashMap<Bundle, Deployer>();
        this.deploymentEngine = (DeploymentEngine) axisConfiguration.getConfigurator();
    }

    public void register(Bundle[] bundles, List<DeployerConfig> deployerConfigs) {
        for (Bundle bundle : bundles) {
            register(bundle);
        }
        for (DeployerConfig deployerConfig : deployerConfigs) {
            Deployer deployer = getDeployer(deployerConfig.getClassStr());
            addDeployer(deployerConfig, deployer);
        }
    }

    public void register(Bundle bundle) {
        lock.lock();
        try {

            if (deployerMap.get(bundle) != null /* Bundle already processed */ ||
                    bundle.getState() != Bundle.ACTIVE /* Bundle has become inactive */) {

                return;
            }

            URL url = bundle.getEntry("META-INF/component.xml");
            if (url == null) {
                //component.xml not found
                log.warn("Axis2Deployer header found in the MANIFEST : But no component.xml found in the bundle");
                return;
            }

            InputStream inputStream = url.openStream();
            Component component = ComponentConfigFactory.build(inputStream);
            DeployerConfig[] deployerConfigs = null;
            if (component != null) {
                deployerConfigs = (DeployerConfig[]) component.getComponentConfig(ComponentConstants.DEPLOYER_CONFIG);
            }

            if (deployerConfigs != null) {
                for (DeployerConfig deployerConfig : deployerConfigs) {

                    Class deployerClass;
                    try {
                        deployerClass = bundle.loadClass(deployerConfig.getClassStr());
                    } catch (ClassNotFoundException e) {
                        deployerClass = Class.forName(deployerConfig.getClassStr());
                    }

                    Deployer deployer = (Deployer) deployerClass.newInstance();
                    addDeployer(deployerConfig, deployer);
                    deployerMap.put(bundle, deployer);
                }
            }
        } catch (Exception e) {
            String msg = "Error while deploying the Deployer from bunlde : " + bundle.getSymbolicName() + "-" +
                    bundle.getVersion();
            log.error(msg, e);
        } finally {
            lock.unlock();
        }
    }

    private void addDeployer(DeployerConfig deployerConfig, Deployer deployer) {

        String directory = deployerConfig.getDirectory();
        String extension = deployerConfig.getExtension();
        deployer.setDirectory(directory);
        deployer.setExtension(extension);

        //Add the deployer to deployment engine
        deploymentEngine.addDeployer(deployer, directory, extension);

    }

    private Deployer getDeployer(String className) {
        Deployer deployer = null;
        try {
            Class deployerClass = Class.forName(className);
            deployer = (Deployer) deployerClass.newInstance();

        } catch (ClassNotFoundException e) {
            log.error("Deployer class not found ", e);
        } catch (InstantiationException e) {
            log.error("Cannot create new deployer instance", e);
        } catch (IllegalAccessException e) {
            log.error("Error creating deployer", e);
        }
        return deployer;
    }

    public void unRegister(Bundle bundle) {
        lock.lock();
        try {
            //TODO implement this method
//            Deployer deployer = deployerMap.get(bundle);
//            if(deployer == null){
//                //No deployer registered from this bundle
//                return;
//            }

//            deploymentEngine.removeDeployer(deployer.getClass());

            // remove the deployer from the deployment engine
        } finally {
            lock.unlock();
        }
    }

    public void bundleChanged(BundleEvent event) {
        if (event.getType() == BundleEvent.RESOLVED) {
            unRegister(event.getBundle());
        }
    }
}
