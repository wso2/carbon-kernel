/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.utils.deployment;

import org.apache.axis2.deployment.repository.util.DeploymentFileData;

/**
 * Wraps the DeploymentFileData object, and provides additional
 * detail on whether the current DeploymentFileData is ghost or not
 */
public class DeploymentFileDataWrapper {

    private DeploymentFileData deploymentFileData;

    private boolean isGhost;

    public DeploymentFileDataWrapper(DeploymentFileData deploymentFileData, boolean isGhost) {
        this.deploymentFileData = deploymentFileData;
        this.isGhost = isGhost;
    }

    public DeploymentFileData getDeploymentFileData() {
        return deploymentFileData;
    }

    public void setDeploymentFileData(DeploymentFileData deploymentFileData) {
        this.deploymentFileData = deploymentFileData;
    }

    public boolean isGhost() {
        return isGhost;
    }

    public void setGhost(boolean isGhost) {
        this.isGhost = isGhost;
    }
}
