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
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.config.membership.scheme.AWSSchemeConfig;
import org.wso2.carbon.clustering.exception.MembershipFailedException;
import org.wso2.carbon.clustering.exception.MembershipInitializationException;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.util.HazelcastUtil;
import org.wso2.carbon.internal.clustering.ClusterContext;

import java.util.List;

/**
 * AWS based membership scheme based on Hazelcast
 */
public class AWSMembershipScheme implements HazelcastMembershipScheme {
    private static final Logger log = LoggerFactory.getLogger(AWSMembershipScheme.class);

    private final AwsConfig awsConfig;
    private HazelcastInstance hazelcastInstance;
    private ClusterContext clusterContext;
    private final List<ClusterMessage> messageBuffer;

    public AWSMembershipScheme(AwsConfig awsConfig, List<ClusterMessage> messageBuffer) {
        this.awsConfig = awsConfig;
        this.messageBuffer = messageBuffer;
    }

    @Override
    public void init(ClusterContext clusterContext) throws MembershipInitializationException {
        this.clusterContext = clusterContext;
        AWSSchemeConfig awsSchemeConfig = (AWSSchemeConfig) clusterContext.getClusterConfiguration().
                getMembershipSchemeConfiguration().getMembershipScheme();

        awsConfig.setAccessKey(awsSchemeConfig.getAccessKey());
        awsConfig.setSecretKey(awsSchemeConfig.getSecretKey());
        awsConfig.setSecurityGroupName(awsSchemeConfig.getSecurityGroup());
        int connTimeout = awsSchemeConfig.getConnTimeout();
        if (connTimeout != -1) {
            awsConfig.setConnectionTimeoutSeconds(connTimeout);
        }
        String hostHeader = awsSchemeConfig.getHostHeader();
        if (hostHeader != null) {
            awsConfig.setHostHeader(hostHeader);
        }
        awsConfig.setRegion(awsSchemeConfig.getRegion());
        String tagKey = awsSchemeConfig.getTagKey();
        String tagValue = awsSchemeConfig.getTagValue();
        if (tagKey != null && tagValue != null) {
            awsConfig.setTagKey(tagKey);
            awsConfig.setTagValue(tagValue);
        }
    }

    @Override
    public void joinGroup() throws MembershipFailedException {
        hazelcastInstance.getCluster().addMembershipListener(new AWSMembershipListener());
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {

        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {
    }

    private class AWSMembershipListener implements MembershipListener {
        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            log.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            // Wait for sometime for the member to completely join before replaying messages
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            try {
                HazelcastUtil.sendMessagesToMember(messageBuffer, member);
            } catch (MessageFailedException e) {
                log.error("Error while sending member joined message", e);
            }
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            clusterContext.removeMember(HazelcastUtil.toClusterMember(member));
            log.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {

        }
    }
}
