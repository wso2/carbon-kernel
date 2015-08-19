/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.core.clustering.hazelcast.aws;

import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.core.clustering.MembershipScheme;

import java.util.List;
import java.util.Map;

/**
 * AWS based membership scheme service.
 */
public class AWSBasedMembershipScheme implements HazelcastMembershipScheme {
    private static final Log log = LogFactory.getLog(AWSBasedMembershipScheme.class);
    private Map<String, Parameter> parameters;
    private NetworkConfig nwConfig;
    private HazelcastInstance primaryHazelcastInstance;
    private List<ClusteringMessage> messageBuffer;
    private HazelcastCarbonClusterImpl carbonCluster;

    public AWSBasedMembershipScheme() {
    }

    @Override
    public void init(Map<String, Parameter> parameters,
                     String primaryDomain,
                     List<org.apache.axis2.clustering.Member> wkaMembers,
                     Config primaryHazelcastConfig,
                     HazelcastInstance primaryHazelcastInstance,
                     List<ClusteringMessage> messageBuffer) {
        this.parameters = parameters;
        this.primaryHazelcastInstance = primaryHazelcastInstance;
        this.messageBuffer = messageBuffer;
        this.nwConfig = primaryHazelcastConfig.getNetworkConfig();
    }

    @Override
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }

    @Override
    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void init() throws ClusteringFault {
        nwConfig.getJoin().getMulticastConfig().setEnabled(false);
        nwConfig.getJoin().getTcpIpConfig().setEnabled(false);
        AwsConfig awsConfig = nwConfig.getJoin().getAwsConfig();
        awsConfig.setEnabled(true);

        Parameter accessKey = getParameter(AWSConstants.ACCESS_KEY);
        Parameter secretKey = getParameter(AWSConstants.SECRET_KEY);
        Parameter securityGroup = getParameter(AWSConstants.SECURITY_GROUP);
        Parameter connTimeout = getParameter(AWSConstants.CONNECTION_TIMEOUT);
        Parameter hostHeader = getParameter(AWSConstants.HOST_HEADER);
        Parameter region = getParameter(AWSConstants.REGION);
        Parameter tagKey = getParameter(AWSConstants.TAG_KEY);
        Parameter tagValue = getParameter(AWSConstants.TAG_VALUE);

        if (accessKey != null) {
            awsConfig.setAccessKey(((String)accessKey.getValue()).trim());
        }
        if (secretKey != null) {
            awsConfig.setSecretKey(((String)secretKey.getValue()).trim());
        }
        if (securityGroup != null) {
            awsConfig.setSecurityGroupName(((String)securityGroup.getValue()).trim());
        }
        if (connTimeout != null) {
            awsConfig.setConnectionTimeoutSeconds(Integer.parseInt(((String) connTimeout.getValue()).trim()));
        }
        if (hostHeader != null) {
            awsConfig.setHostHeader(((String)hostHeader.getValue()).trim());
        }
        if (region != null) {
            awsConfig.setRegion(((String)region.getValue()).trim());
        }
        if (tagKey != null) {
            awsConfig.setTagKey(((String)tagKey.getValue()).trim());
        }
        if (tagValue != null) {
            awsConfig.setTagValue(((String)tagValue.getValue()).trim());
        }

    }

    @Override
    public void joinGroup() throws ClusteringFault {
        primaryHazelcastInstance.getCluster().addMembershipListener(new AWSMembershipListener());
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    private class AWSMembershipListener implements MembershipListener {
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            carbonCluster.memberAdded(member);
            log.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            // Wait for sometime for the member to completely join before replaying messages
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            carbonCluster.memberRemoved(member);
            log.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
        }

        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }

    }
}
