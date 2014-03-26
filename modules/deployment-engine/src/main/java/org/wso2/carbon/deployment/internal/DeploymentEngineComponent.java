/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.felix.scr.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.DeploymentEngine;
import org.wso2.carbon.deployment.CarbonDeploymentService;
import org.wso2.carbon.deployment.api.DeploymentService;
import org.wso2.carbon.deployment.exception.DeploymentEngineException;
import org.wso2.carbon.deployment.spi.Deployer;
import org.wso2.carbon.kernel.CarbonRuntime;

import java.util.ArrayList;
import java.util.List;

/**
 * The bundle activator which activates the carbon deployment engine
 */
@Component(
        name = "org.wso2.carbon.deployment.internal.DeploymentEngineComponent",
        description = "This service component is responsible for initializing the DeploymentEngine",
        immediate = true
)
@Reference(
        name = "carbon.deployer.service",
        referenceInterface = Deployer.class,
        cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        bind = "registerDeployer",
        unbind = "unregisterDeployer"
)
public class DeploymentEngineComponent {
    private static Logger logger = LoggerFactory.getLogger(DeploymentEngineComponent.class);
    @Reference(
            name = "carbon.runtime.service",
            referenceInterface = CarbonRuntime.class,
            cardinality = ReferenceCardinality.MANDATORY_UNARY,
            policy = ReferencePolicy.DYNAMIC,
            bind = "setCarbonRuntime",
            unbind = "unsetCarbonRuntime"
    )
    private CarbonRuntime carbonRuntime;
    private DeploymentEngine deploymentEngine;
    private ServiceRegistration serviceRegistration;
    private List<Deployer> deployerList = new ArrayList<>();


    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        try {
            // Initialize deployment engine and scan it
            String carbonRepositoryLocation = carbonRuntime.getConfiguration().
                    getDeploymentConfig().getRepositoryLocation();
            deploymentEngine = new DeploymentEngine(carbonRepositoryLocation);

            logger.debug("Starting Carbon Deployment Engine {}", deploymentEngine);
            deploymentEngine.start();

            // Add deployment engine to the data holder for later usages/references of this object
            OSGiServiceHolder.getInstance().
                    setCarbonDeploymentEngine(deploymentEngine);

            // Register DeploymentService
            DeploymentService deploymentService =
                    new CarbonDeploymentService(deploymentEngine);
            serviceRegistration = bundleContext.registerService(DeploymentService.class.getName(),
                                                                deploymentService, null);

            logger.debug("Started Carbon Deployment Engine");

            // register pending deployers in the list
            for (Deployer deployer : deployerList) {
                try {
                    deploymentEngine.registerDeployer(deployer);
                } catch (Exception e) {
                    logger.error("Error while adding deployer to the deployment engine", e);
                }
            }

        } catch (DeploymentEngineException e) {
            String msg = "Could not initialize carbon deployment engine";
            logger.error(msg, e);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Deactivate
    public void stop() throws Exception {
        serviceRegistration.unregister();
    }

    protected void registerDeployer(Deployer deployer) {
        if (deploymentEngine != null) {
            try {
                deploymentEngine.registerDeployer(deployer);
            } catch (Exception e) {
                logger.error("Error while adding deployer to the deployment engine", e);
            }
        } else {//carbon deployment engine is not initialized yet, so we keep them in a pending list
            deployerList.add(deployer);
        }
    }

    protected void unregisterDeployer(Deployer deployer) {
        try {
            deploymentEngine.unregisterDeployer(deployer);
        } catch (Exception e) {
            logger.error("Error while removing deployer from deployment engine", e);
        }
    }


    public void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = carbonRuntime;
        OSGiServiceHolder.getInstance().setCarbonRuntime(carbonRuntime);
    }

    public void unsetCarbonRuntime(CarbonRuntime carbonRuntime) {
        this.carbonRuntime = null;
        OSGiServiceHolder.getInstance().setCarbonRuntime(null);
    }
}
