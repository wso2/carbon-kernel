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

package org.wso2.carbon.clustering.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB representation for a Cluster
 */
@XmlRootElement(name = "Cluster")
@XmlAccessorType(XmlAccessType.FIELD)
public class ClusterConfiguration {
    @XmlElement(name = "Enable", required = true, type = Boolean.class)
    private boolean isEnabled;

    @XmlElement(name = "Agent", required = true)
    private String agent;

    @XmlElement(name = "Domain", required = true)
    private String domain;

    @XmlElement(name = "LocalMember", required = true)
    private LocalMemberConfiguration localMemberConfiguration;

    @XmlElement(name = "MembershipScheme", required = true)
    private MembershipSchemeConfiguration membershipSchemeConfiguration;


    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public LocalMemberConfiguration getLocalMemberConfiguration() {
        return localMemberConfiguration;
    }

    public void setLocalMemberConfiguration(LocalMemberConfiguration localMemberConfiguration) {
        this.localMemberConfiguration = localMemberConfiguration;
    }

    public MembershipSchemeConfiguration getMembershipSchemeConfiguration() {
        return membershipSchemeConfiguration;
    }

    public void setMembershipSchemeConfiguration(
            MembershipSchemeConfiguration membershipSchemeConfiguration) {
        this.membershipSchemeConfiguration = membershipSchemeConfiguration;
    }
}
