/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.deployment.config;

/**
 * DeploymentConfiguration class holds static configuration parameters specified in the deployment.yml file.
 *
 * @since 5.1.0
 */
public class DeploymentConfiguration {

    private DeploymentModeEnum mode = DeploymentModeEnum.scheduled;

    private String repositoryLocation = "${carbon.home}/deployment/";

    private int updateInterval = 15;

    private DeploymentNotifierConfig deploymentNotifier = new DeploymentNotifierConfig();

    public DeploymentModeEnum getMode() {
        return mode;
    }

    public String getRepositoryLocation() {
        return repositoryLocation;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public DeploymentNotifierConfig getDeploymentNotifier() {
        return deploymentNotifier;
    }

}
