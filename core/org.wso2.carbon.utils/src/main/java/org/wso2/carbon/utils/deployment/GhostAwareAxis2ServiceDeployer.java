/*
*  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.utils.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.ServiceDeployer;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Ghost aware aar services deployer.
 */
public class GhostAwareAxis2ServiceDeployer extends ServiceDeployer {

    private static Log log = LogFactory.getLog(GhostAwareAxis2ServiceDeployer.class);

    private boolean isGhostOn = GhostDeployerUtils.isGhostOn();
    private AxisConfiguration axisConfig;

    @Override
    public void init(ConfigurationContext configCtx) {
        super.init(configCtx);
        this.axisConfig = configCtx.getAxisConfiguration();

    }

    @Override
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        //how to know whether this service should be deployed as the actual service or as a ghost service
        String filePath = deploymentFileData.getAbsolutePath();
        GhostArtifactRegistry ghostRegistry = GhostDeployerUtils.getGhostArtifactRegistry(axisConfig);
        DeploymentFileDataWrapper existingDfd = ghostRegistry.getDeploymentFileData(filePath);

        boolean doGhostDeployment = true;
        if (existingDfd != null && existingDfd.isGhost()) {
            //if the service is already deployed, then treat this as an actual service deployment.
            // So, remove the current @DeploymentFileData from ghost service registry.
            doGhostDeployment = false;
            ghostRegistry.removeDeploymentFileData(filePath);
            if (log.isDebugEnabled()) {
                log.debug("Doing actual service deployment: " + deploymentFileData.getAbsolutePath());
            }
        }

        if (isGhostOn && doGhostDeployment) { //if ghost on and this dfd should deployed as a ghost service
            String absoluteFilePath = deploymentFileData.getAbsolutePath();
            File ghostFile = GhostDeployerUtils.getGhostFile(absoluteFilePath, axisConfig);

            if (ghostFile == null || !ghostFile.exists()) {
                // ghost file is not found. so this is a new service and we have to deploy it
                Set<AxisService> axisServicesBefore = new HashSet<AxisService>(
                        axisConfig.getServices().values());

                super.deploy(deploymentFileData);
                ghostRegistry.addDeploymentFileData(deploymentFileData, false);

                Set<AxisService> axisServicesAfter = new HashSet<AxisService>(
                        axisConfig.getServices().values());
                axisServicesAfter.removeAll(axisServicesBefore); //find out the services that got deployed
                GhostDeployerUtils.serializeServiceGroup(axisServicesAfter, deploymentFileData, axisConfig);
            } else {
                // load the ghost service group
                GhostDeployerUtils.deployGhostServiceGroup(ghostFile, deploymentFileData, axisConfig);
            }

        } else {
            super.deploy(deploymentFileData);
            if(isGhostOn) {
                //we need a reference to the actual file data object at @ServiceUnloader
                ghostRegistry.addDeploymentFileData(deploymentFileData, false);
            }
        }

    }


    @Override
    public void undeploy(String fileName) throws DeploymentException {
        if(isGhostOn) {
            GhostDeployerUtils.removeGhostFile(fileName, axisConfig);
        }
        super.undeploy(fileName);
    }

}
