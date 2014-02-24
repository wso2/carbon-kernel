/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.deployment.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.DeploymentEngine;
import org.wso2.carbon.deployment.CarbonDeploymentService;
import org.wso2.carbon.deployment.api.DeploymentService;
import org.wso2.carbon.deployment.exception.DeploymentEngineException;

/**
 * The bundle activator which activates the carbon deployment engine
 */
public class DeploymentEngineActivator implements BundleActivator {
    private static Logger logger = LoggerFactory.getLogger(DeploymentEngineActivator.class);
    private ServiceRegistration serviceRegistration;


    public void start(BundleContext bundleContext) throws Exception {
        try {
            // Initialize deployment engine and scan it
            ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
            String carbonRepositoryLocation = serverConfiguration.
                    getFirstProperty("Deployment.RepositoryLocation");
            DeploymentEngine carbonDeploymentEngine =
                    new DeploymentEngine(carbonRepositoryLocation);
            logger.debug("Starting Carbon Deployment Engine {}", carbonDeploymentEngine);
            carbonDeploymentEngine.start();

            // Add deployment engine to the data holder for later usages/references of this object
            DataHolder.getInstance().
                    setCarbonDeploymentEngine(carbonDeploymentEngine);

            // Register DeploymentService
            DeploymentService deploymentService =
                    new CarbonDeploymentService(carbonDeploymentEngine);
            serviceRegistration = bundleContext.registerService(DeploymentService.class.getName(),
                                                                deploymentService, null);

            logger.debug("Started Carbon Deployment Engine");
        } catch (DeploymentEngineException e) {
            String msg = "Could not initialize carbon deployment engine";
            logger.error(msg, e);
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {
        serviceRegistration.unregister();
    }
}
