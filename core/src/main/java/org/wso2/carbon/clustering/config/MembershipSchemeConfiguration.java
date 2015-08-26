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

import org.wso2.carbon.clustering.ClusteringConstants;
import org.wso2.carbon.clustering.config.membership.scheme.AWSSchemeConfig;
import org.wso2.carbon.clustering.config.membership.scheme.GenericSchemeConfig;
import org.wso2.carbon.clustering.config.membership.scheme.MulticastSchemeConfig;
import org.wso2.carbon.clustering.config.membership.scheme.WKASchemeConfig;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

/**
 * JAXB repesentaiton of cluster MembershipScheme configuration
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MembershipSchemeConfiguration {

    @XmlElements(value = {
            @XmlElement(name = ClusteringConstants.MembershipScheme.MULTICAST_BASED,
                    type = MulticastSchemeConfig.class),
            @XmlElement(name = ClusteringConstants.MembershipScheme.WKA_BASED, type = WKASchemeConfig.class),
            @XmlElement(name = ClusteringConstants.MembershipScheme.AWS_BASED, type = AWSSchemeConfig.class),
            @XmlElement(name = ClusteringConstants.MembershipScheme.GENERIC, type = GenericSchemeConfig.class)
    })
    private Object membershipScheme;

    public Object getMembershipScheme() {
        return membershipScheme;
    }

    public void setMembershipScheme(Object membershipScheme) {
        this.membershipScheme = membershipScheme;
    }
}
