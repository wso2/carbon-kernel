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

import org.wso2.carbon.kernel.utils.Utils;

import javax.xml.bind.annotation.XmlElement;

/**
 * JAXB mapping for Deployment Config.
 */
public class DeploymentConfig {

    @XmlElement(name = "Mode", required = true)
    private DeploymentModeEnum mode;

    @XmlElement(name = "RepositoryLocation", required = true)
    private String repositoryLocation;

    @XmlElement(name = "UpdateInterval", required = true)
    private int updateInterval;

    public DeploymentModeEnum getMode() {
        return mode;
    }

    public String getRepositoryLocation() {
        //TODO Find a better solution to filtering of system properties.[CARBON-14705]
        return Utils.substituteVars(repositoryLocation);
    }

    public int getUpdateInterval() {
        return updateInterval;
    }
}
