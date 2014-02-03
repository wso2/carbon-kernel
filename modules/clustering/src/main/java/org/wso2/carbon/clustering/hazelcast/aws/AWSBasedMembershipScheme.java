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
import org.wso2.carbon.clustering.ClusterConfiguration;
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
    private ClusterConfiguration clusterConfiguration;
    private final String primaryDomain;
    private final NetworkConfig nwConfig;
    private HazelcastInstance primaryHazelcastInstance;
    private final List<ClusterMessage> messageBuffer;
    private HazelcastCarbonCluster carbonCluster;

    public AWSBasedMembershipScheme(ClusterConfiguration clusterConfiguration,
                                    String primaryDomain,
                                    Config config,
                                    HazelcastInstance primaryHazelcastInstance,
                                    List<ClusterMessage> messageBuffer) {
        this.clusterConfiguration = clusterConfiguration;
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
        // Nothing to do
    }

    @Override
    public void init(){
        nwConfig.getJoin().getMulticastConfig().setEnabled(false);
        nwConfig.getJoin().getTcpIpConfig().setEnabled(false);
        AwsConfig awsConfig = nwConfig.getJoin().getAwsConfig();
        awsConfig.setEnabled(true);

        String accessKey = getProperty(AWSConstants.ACCESS_KEY);
        String secretKey = getProperty(AWSConstants.SECRET_KEY);
        String securityGroup = getProperty(AWSConstants.SECURITY_GROUP);
        String connTimeout = getProperty(AWSConstants.CONNECTION_TIMEOUT);
        String hostHeader = getProperty(AWSConstants.HOST_HEADER);
        String region = getProperty(AWSConstants.REGION);
        String tagKey = getProperty(AWSConstants.TAG_KEY);
        String tagValue = getProperty(AWSConstants.TAG_VALUE);

        if (accessKey != null) {
            awsConfig.setAccessKey(accessKey.trim());
        }
        if (secretKey != null) {
            awsConfig.setSecretKey(secretKey.trim());
        }
        if (securityGroup != null) {
            awsConfig.setSecurityGroupName(securityGroup.trim());
        }
        if (connTimeout != null) {
            awsConfig.setConnectionTimeoutSeconds(Integer.parseInt(connTimeout.trim()));
        }
        if (hostHeader != null) {
            awsConfig.setHostHeader(hostHeader.trim());
        }
        if (region != null) {
            awsConfig.setRegion(region.trim());
        }
        if (tagKey != null) {
            awsConfig.setTagKey(tagKey.trim());
        }
        if (tagValue != null) {
            awsConfig.setTagValue(tagValue.trim());
        }

    }

    @Override
    public void joinGroup() {
        primaryHazelcastInstance.getCluster().addMembershipListener(new AWSMembershipListener());
    }

    public String getProperty(String name) {
        return clusterConfiguration.getFirstProperty(name);
    }

    private class AWSMembershipListener implements MembershipListener {
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            // send all cluster messages
            carbonCluster.addMember(HazelcastUtil.toClusterMember(member));
            logger.info("Member joined [" + member.getUuid() + "]: " +
                        member.getInetSocketAddress().toString());
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
            logger.info("Member left [" + member.getUuid() + "]: " +
                        member.getInetSocketAddress().toString());
        }
    }
}
