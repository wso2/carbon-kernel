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
import org.wso2.carbon.clustering.ClusterContext;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.exception.MembershipFailedException;
import org.wso2.carbon.clustering.exception.MembershipInitializationException;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.util.HazelcastUtil;

import java.util.List;

/**
 * AWS - Amazon Web Service, based membership scheme supported with hazelcast based clustering
 */
public class AWSBasedMembershipScheme implements HazelcastMembershipScheme {
    private static Logger logger = LoggerFactory.getLogger(AWSBasedMembershipScheme.class);
    private ClusterConfiguration clusterConfiguration;
    private final NetworkConfig nwConfig;
    private HazelcastInstance primaryHazelcastInstance;
    private final List<ClusterMessage> messageBuffer;
    private ClusterContext clusterContext;

    public AWSBasedMembershipScheme(Config config,
                                    HazelcastInstance primaryHazelcastInstance,
                                    List<ClusterMessage> messageBuffer) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
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
    public void init(ClusterContext clusterContext) throws MembershipInitializationException {
        this.clusterContext = clusterContext;
        this.clusterConfiguration = clusterContext.getClusterConfiguration();
        try {
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
        } catch (Exception e) {
            throw new MembershipInitializationException("Error while trying initialize " +
                                                        "AWS membership scheme", e);
        }

    }

    @Override
    public void joinGroup() throws MembershipFailedException {
        try {
            primaryHazelcastInstance.getCluster().addMembershipListener(new AWSMembershipListener());
        } catch (Exception e) {
            throw new MembershipFailedException("Error while trying join aws " +
                                                "membership scheme", e);
        }
    }

    public String getProperty(String name) {
        return clusterConfiguration.getFirstProperty(name);
    }

    private class AWSMembershipListener implements MembershipListener {
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            // send all cluster messages
            clusterContext.addMember(HazelcastUtil.toClusterMember(member));
            logger.info("Member joined [" + member.getUuid() + "]: " +
                        member.getInetSocketAddress().toString());
            // Wait for sometime for the member to completely join before replaying messages
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            try {
                HazelcastUtil.sendMessagesToMember(messageBuffer, member);
            } catch (MessageFailedException e) {
                logger.error("Error while sending member joined message", e);
            }
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            clusterContext.removeMember(HazelcastUtil.toClusterMember(member));
            logger.info("Member left [" + member.getUuid() + "]: " +
                        member.getInetSocketAddress().toString());
        }
    }
}
