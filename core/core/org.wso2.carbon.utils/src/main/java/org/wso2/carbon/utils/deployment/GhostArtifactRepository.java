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

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Keeps track of deployment file data objects of ghost artifacts.
 * This is needed to load to actual service when needed.
 *
 * We also need a reference to the actual deployment file data
 * to unload a webapp and load its ghost after a period of inactivity.
 *
 */
public class GhostArtifactRepository {

    /**
     * Map of deployment file data wrappers
     * key is the absolute file path of DeploymentFileData
     * value is the DeploymentFileDataWrapper which is a wrapper around DeploymentFileData.
     */
    private Map<String, DeploymentFileDataWrapper> deploymentFileDataList;

    public GhostArtifactRepository(AxisConfiguration axisConfiguration) {
        deploymentFileDataList = new HashMap<String, DeploymentFileDataWrapper>();

        /**
         * Since axis2 @ServiceDeployer is hard-coded in Axis2, we over-ride it by adding our own
         * axis2 aar service deployer that is ghost aware.
         */
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfiguration.getConfigurator();
        deploymentEngine.addDeployer(
                new GhostAwareAxis2ServiceDeployer(),
                CarbonUtils.getAxis2ServicesDir(axisConfiguration),
                ".aar");
    }

    public DeploymentFileDataWrapper getDeploymentFileData(String fileName) {
        // avoid the leading "/" in filename in windows environment
        File deploymentFile = new File(fileName);
        fileName = deploymentFile.getPath();
        return deploymentFileDataList.get(fileName);

    }

    /**
     * Add a DeploymentFileData
     * @param deploymentFileData
     * @param isGhost true if this dfd was deployed as a ghost. false otherwise.
     */
    public void addDeploymentFileData(DeploymentFileData deploymentFileData,
                                      boolean isGhost) {
        // avoid the leading "/" in filename in windows environment
        File deploymentFile = new File(deploymentFileData.getAbsolutePath());
        String fileName = deploymentFile.getPath();
        DeploymentFileDataWrapper dfdWrapper = new DeploymentFileDataWrapper(deploymentFileData, isGhost);
        this.deploymentFileDataList.put(fileName, dfdWrapper);
    }

    public DeploymentFileDataWrapper removeDeploymentFileData(String filePath) {
        return deploymentFileDataList.remove(filePath);
    }

}