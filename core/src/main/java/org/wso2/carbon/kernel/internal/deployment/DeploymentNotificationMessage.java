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
package org.wso2.carbon.kernel.internal.deployment;

import org.wso2.carbon.kernel.deployment.Artifact;
import org.wso2.carbon.kernel.deployment.Lifecycle;

import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The JAXB POJO that is used to send the artifact deployment
 * status, usually to a JMS topic.
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DeploymentNotificationMessage {

    @XmlElement
    private Object artifactKey;

    @XmlElement
    private String artifactType;

    @XmlElement
    private String host; //todo set host

    @XmlElement
    private Date timestamp;

    @XmlElement
    private Lifecycle.STATE deploymentState;

    @XmlElement
    private String message;

    @XmlElement
    public Properties properties;

    /**
     * Extracts info from the Artifact object.
     *
     * @param artifact The artifact that we need to extract info from
     */
    public DeploymentNotificationMessage(Artifact artifact) {
        this(artifact, null);
    }

    /**
     * The default constructor is required by JAXB
     * to do serialization.
     */
    public DeploymentNotificationMessage() {
    }

    public DeploymentNotificationMessage(Artifact artifact, Date timestamp) {
        this.artifactKey = artifact.getKey();
        this.artifactType = artifact.getType().get().toString();
        this.timestamp = Optional.ofNullable(timestamp).map(tstamp -> new Date(timestamp.getTime())).orElse(new Date());
    }

    public Object getArtifactKey() {
        return artifactKey;
    }

    public void setArtifactKey(Object artifactKey) {
        this.artifactKey = artifactKey;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public void setArtifactType(String artifactType) {
        this.artifactType = artifactType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = new Date(timestamp.getTime());
    }

    public Lifecycle.STATE getDeploymentState() {
        return deploymentState;
    }

    public void setDeploymentState(Lifecycle.STATE deploymentState) {
        this.deploymentState = deploymentState;
    }

    public String getMessage() {
        return message;
    }

    public void setTraceContent(String message) {
        this.message = message;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return String.format("%1$s - %2$s - %3$s ", artifactKey, artifactType, deploymentState);
    }

}
