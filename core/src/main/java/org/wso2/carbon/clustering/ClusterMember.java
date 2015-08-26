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
package org.wso2.carbon.clustering;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Properties;

/**
 * A member in a Carbon cluster
 */
public class ClusterMember implements Serializable {

    private static final long serialVersionUID = 8903688708839996113L;

    /**
     * The host name of this member. Can be the name or the IP address
     */
    private String hostName;

    /**
     * The TCP port used by this member for communicating clustering messages
     */
    private int port = -1;

    /**
     * Indicates whether this member is ACTIVE or PASSIVE
     */
    private boolean isActive = true;

    /**
     * The domain of this member
     */
    private String domain;

    /**
     * Other member specific properties
     */
    private Properties properties = new Properties();

    /**
     * A unique id of this member
     */
    private String id;

    /**
     * InetAddres of this member
     */
    private InetSocketAddress inetSocketAddress;


    public ClusterMember(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        this.inetSocketAddress = new InetSocketAddress(hostName, port);
    }

    /**
     * Return the host name associated with this member
     *
     * @return hostname
     */
    public String getHostName() {
        String remoteHost = properties.getProperty("remoteHost");
        if (remoteHost != null) {
            return remoteHost;
        }
        return hostName;
    }

    /**
     * Return TCP port of this member
     *
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Indicates whether this member is active
     *
     * @return true if active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Sets the state of this member true-active/false-inactive
     *
     * @param active state true-active/false-inactive
     */
    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Returns the domain associated with this member
     *
     * @return domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the domain value for this member
     *
     * @param domain domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Returns the properties that are associated with this member
     *
     * @return the properties instance
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Custom properties associated with this member
     *
     * @param properties the properties instance
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * The InetSocketAddress of this member, which is populated using the host and port
     *
     * @return InetSocketAddress
     */
    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    /**
     * Returns the ID of this member
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets a unique ID for this member
     *
     * @param id the ID to set
     */
    public void setId(String id) {
        this.id = id;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClusterMember member = (ClusterMember) o;

        boolean isIdenticalPorts = port == member.getPort();

        return (isIdenticalPorts && (hostName != null ?
                hostName.equals(member.getHostName()) :
                member.getHostName() == null));
    }

    public int hashCode() {
        int result;
        result = (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + port;
        return result;
    }

    public String toString() {
        return "Host:" + hostName + ", Port: " + port + ", Domain: " + domain +
                ", Sub-domain:" + properties.getProperty("subDomain") + ", Active:" + isActive;
    }
}
