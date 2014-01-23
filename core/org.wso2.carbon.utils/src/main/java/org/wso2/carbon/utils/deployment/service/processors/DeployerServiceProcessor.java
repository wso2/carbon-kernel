/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.utils.deployment.service.processors;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.wso2.carbon.utils.deployment.GhostDeployer;
import org.wso2.carbon.utils.deployment.GhostDeployerUtils;

public class DeployerServiceProcessor extends ConfigurationServiceProcessor {

    public DeployerServiceProcessor(AxisConfiguration axisConfig, BundleContext bundleContext) {
        super(axisConfig, bundleContext);
    }

    public void processConfigurationService(ServiceReference sr, int action) throws AxisFault {
        Deployer deployer = (Deployer) bundleContext.getService(sr);
        lock.lock();
        try {
            String directory = (String) sr.getProperty(DeploymentConstants.DIRECTORY);
            String extension = (String) sr.getProperty(DeploymentConstants.EXTENSION);

            // Get GhostDeployer
            GhostDeployer ghostDeployer = GhostDeployerUtils.getGhostDeployer(axisConfig);

            if (action == ServiceEvent.REGISTERED || action == ServiceEvent.MODIFIED) {
                if (ghostDeployer == null) {
                    deploymentEngine.addDeployer(deployer, directory, extension);
                } else {
                    deploymentEngine.addDeployer(ghostDeployer, directory, extension);
                    ghostDeployer.addDeployer(deployer, directory, extension);
                }
            } else if (action == ServiceEvent.UNREGISTERING) {
                if (ghostDeployer != null) {
                    ghostDeployer.removeDeployer(directory, extension);
                }
                deploymentEngine.removeDeployer(directory, extension);
            }
        } finally {
            lock.unlock();
        }
    }
}