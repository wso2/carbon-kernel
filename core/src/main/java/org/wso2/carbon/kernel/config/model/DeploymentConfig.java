/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.config.model;


import org.wso2.carbon.configuration.annotations.Configuration;
import org.wso2.carbon.configuration.annotations.Element;

/**
 * Deployment Config bean.
 *
 * @since 5.0.0
 */
@Configuration(namespace = "deployment", description = "Deployment Engine related configurations")
public class DeploymentConfig {

    @Element(description = "Currently there can be two modes\n" +
            " 1. Scheduled Mode - (enable below property to \"scheduled\") - where the task runs periodically and " +
            "trigger deployment\n" +
            " 2. Triggered Mode - (enable below property to \"triggered\") - the deployment has to be triggered " +
            "externally,\n" +
            "  eg : in a worker node we don't need the task to run, but rather when we receive a cluster msg,\n" +
            "the deployment has to be triggered manually.", defaultValue = "scheduled")
    private DeploymentModeEnum mode;

    @Element(description = "Location of the artifact repository", defaultValue = "${carbon.home}/deployment/")
    private String repositoryLocation;

    @Element(description = "Deployment update interval in seconds. This is the interval between repository listener " +
            "executions.", defaultValue = "15")
    private int updateInterval;

    public DeploymentModeEnum getMode() {
        return mode;
    }

    public String getRepositoryLocation() {
        return repositoryLocation;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
}
