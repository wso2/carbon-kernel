/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.kernel.deployment;

import java.util.Date;
import java.util.Optional;
import java.util.Properties;

/**
 * The deployment lifecycle of artifacts. An instance of this is passed
 * as part of the lifecycle event into the lifecycle listeners.
 *
 * @since 5.1.0
 */
public class Lifecycle {

    /**
     * The artifact deployment/undeployment state.
     * If the artifact deployed/undeployed without any errors,
     * then the state will be successful denoting a successful artifact
     * deployment.
     *
     */
    public enum STATE {
        SUCCESSFUL,
        FAILED
    }

    private Artifact artifact;

    private Date timestamp;
    private STATE deploymentState;
    private String message;

    public Properties properties;

    public Lifecycle(Artifact artifact, Date date) {
        this.artifact = artifact;
        //assume a successful deployment initially.
        this.deploymentState = STATE.SUCCESSFUL;

        this.timestamp = Optional.ofNullable(date).map(tstamp -> new Date(date.getTime())).orElse(new Date());
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public Date getTimestamp() {
        return new Date(Optional.of(timestamp).get().getTime());
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = new Date(timestamp.getTime());
    }

    public STATE getDeploymentState() {
        return deploymentState;
    }

    public void setDeploymentState(STATE deploymentState) {
        this.deploymentState = deploymentState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
