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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.DeploymentEngine;
import org.wso2.carbon.deployment.CarbonDeploymentService;
import org.wso2.carbon.deployment.api.DeploymentService;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;

/**
 * The bundle activator which activates the carbon deployment engine
 *
 */
public class DeploymentEngineActivator implements BundleActivator{
    private static Log log = LogFactory.getLog(DeploymentEngineActivator.class);


    public void start(BundleContext bundleContext) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Starting Carbon Deployment Engine");
        }
        try {
            // Initialize deployment engine and scan it
            ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();
            String carbonRepositoryLocation = serverConfiguration.
                    getFirstProperty("Deployment.RepositoryLocation");
            DeploymentEngine carbonDeploymentEngine =
                    new DeploymentEngine(carbonRepositoryLocation);
            carbonDeploymentEngine.start();

            // Add deployment engine to the data holder for later usages/references of this object
            DataHolder.getInstance().
                    setCarbonDeploymentEngine(carbonDeploymentEngine);

            // Register DeploymentService
            DeploymentService deploymentService =
                    new CarbonDeploymentService(carbonDeploymentEngine);
            bundleContext.registerService(DeploymentService.class.getName(), deploymentService,
                                          null);
            if (log.isDebugEnabled()) {
                log.debug("Started Carbon Deployment Engine");
            }
        } catch (CarbonDeploymentException e) {
            String msg = "Could not initialize carbon deployment engine";
            log.fatal(msg, e);
        }
    }

    public void stop(BundleContext bundleContext) throws Exception {

    }
}
