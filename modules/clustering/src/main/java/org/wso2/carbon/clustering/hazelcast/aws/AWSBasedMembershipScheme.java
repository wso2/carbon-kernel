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
package org.wso2.carbon.clustering.hazelcast.aws;

import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.api.Cluster;
import org.wso2.carbon.clustering.hazelcast.HazelcastCarbonCluster;
import org.wso2.carbon.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.clustering.internal.DataHolder;

import java.util.List;

/**
 * TODO: class description
 */
public class AWSBasedMembershipScheme implements HazelcastMembershipScheme {
    private static Logger logger = LoggerFactory.getLogger(AWSBasedMembershipScheme.class);
//    private final Map<String, Parameter> parameters;
    private final String primaryDomain;
    private final NetworkConfig nwConfig;
    private HazelcastInstance primaryHazelcastInstance;
    private final List<ClusterMessage> messageBuffer;
    private HazelcastCarbonCluster carbonCluster;

    public AWSBasedMembershipScheme(/*Map<String, Parameter> parameters,*/
                                    String primaryDomain,
                                    Config config,
                                    HazelcastInstance primaryHazelcastInstance,
                                    List<ClusterMessage> messageBuffer) {
//        this.parameters = parameters;
        this.primaryDomain = primaryDomain;
        this.primaryHazelcastInstance = primaryHazelcastInstance;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
    }

    @Override
    public void setCarbonCluster(HazelcastCarbonCluster hazelcastCarbonCluster) {
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
    public void init(){
        nwConfig.getJoin().getMulticastConfig().setEnabled(false);
        nwConfig.getJoin().getTcpIpConfig().setEnabled(false);
        AwsConfig awsConfig = nwConfig.getJoin().getAwsConfig();
        awsConfig.setEnabled(true);

//        Parameter accessKey = getParameter(AWSConstants.ACCESS_KEY);
//        Parameter secretKey = getParameter(AWSConstants.SECRET_KEY);
//        Parameter securityGroup = getParameter(AWSConstants.SECURITY_GROUP);
//        Parameter connTimeout = getParameter(AWSConstants.CONNECTION_TIMEOUT);
//        Parameter hostHeader = getParameter(AWSConstants.HOST_HEADER);
//        Parameter region = getParameter(AWSConstants.REGION);
//        Parameter tagKey = getParameter(AWSConstants.TAG_KEY);
//        Parameter tagValue = getParameter(AWSConstants.TAG_VALUE);
//
//        if (accessKey != null) {
//            awsConfig.setAccessKey(((String)accessKey.getValue()).trim());
//        }
//        if (secretKey != null) {
//            awsConfig.setSecretKey(((String)secretKey.getValue()).trim());
//        }
//        if (securityGroup != null) {
//            awsConfig.setSecurityGroupName(((String)securityGroup.getValue()).trim());
//        }
//        if (connTimeout != null) {
//            awsConfig.setConnectionTimeoutSeconds(Integer.parseInt(((String) connTimeout.getValue()).trim()));
//        }
//        if (hostHeader != null) {
//            awsConfig.setHostHeader(((String)hostHeader.getValue()).trim());
//        }
//        if (region != null) {
//            awsConfig.setRegion(((String)region.getValue()).trim());
//        }
//        if (tagKey != null) {
//            awsConfig.setTagKey(((String)tagKey.getValue()).trim());
//        }
//        if (tagValue != null) {
//            awsConfig.setTagValue(((String)tagValue.getValue()).trim());
//        }

    }

    @Override
    public void joinGroup() {
        primaryHazelcastInstance.getCluster().addMembershipListener(new AWSMembershipListener());
    }

//    public Parameter getParameter(String name) {
//        return parameters.get(name);
//    }

    private class AWSMembershipListener implements MembershipListener {
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            // send all cluster messages
            carbonCluster.addMember(HazelcastUtil.toClusterMember(member));
            logger.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
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
            carbonCluster.removeMember(HazelcastUtil.toClusterMember(member));
            logger.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
        }
    }
}
