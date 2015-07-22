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
package org.wso2.carbon.clustering.hazelcast.multicast;

import com.hazelcast.config.MulticastConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.internal.clustering.ClusterContext;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.config.membership.scheme.MulticastSchemeConfig;
import org.wso2.carbon.clustering.exception.MembershipFailedException;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.util.HazelcastUtil;
import org.wso2.carbon.clustering.hazelcast.util.MemberUtils;

import java.util.List;
import java.util.Map;

/**
 * Multicast based membership scheme based on Hazelcast
 */
public class MulticastBasedMembershipScheme implements HazelcastMembershipScheme {
    private static Logger logger = LoggerFactory.getLogger(MulticastBasedMembershipScheme.class);
    private ClusterConfiguration clusterConfiguration;
    private MulticastSchemeConfig multicastSchemeConfig;
    private String primaryDomain;
    private MulticastConfig config;
    private final List<ClusterMessage> messageBuffer;
    private ClusterContext clusterContext;
    private HazelcastInstance hazelcastInstance;

    public MulticastBasedMembershipScheme(String primaryDomain,
                                          MulticastConfig config,
                                          List<ClusterMessage> messageBuffer) {
        this.primaryDomain = primaryDomain;
        this.config = config;
        this.messageBuffer = messageBuffer;
    }

    @Override
    public void init(ClusterContext clusterContext) {
        config.setEnabled(true);
        this.clusterContext = clusterContext;
        this.clusterConfiguration = clusterContext.getClusterConfiguration();
        this.multicastSchemeConfig = (MulticastSchemeConfig) clusterConfiguration.
                getMembershipSchemeConfiguration().getMembershipScheme();
        configureMulticastParameters();
    }

    private void configureMulticastParameters() {
        String mcastAddress = multicastSchemeConfig.getGroup();
        if (mcastAddress != null) {
            config.setMulticastGroup(mcastAddress);
        }
        int mcastPort = multicastSchemeConfig.getPort();
        if (mcastPort != 0) {
            config.setMulticastPort(mcastPort);
        }
        int mcastTimeout = multicastSchemeConfig.getTimeout();
        if (mcastTimeout != 0) {
            config.setMulticastTimeoutSeconds(mcastTimeout);
        }
        int mcastTTL = multicastSchemeConfig.getTtl();
        if (mcastTTL != 0) {
            config.setMulticastTimeToLive(mcastTTL);
        }
    }

    @Override
    public void joinGroup() throws MembershipFailedException {
        try {
            hazelcastInstance.getCluster().
                    addMembershipListener(new MulticastMembershipListener());
        } catch (Exception e) {
            throw new MembershipFailedException("Error while trying join multicast " +
                                                "membership scheme", e);
        }
    }

    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public void setLocalMember(Member localMember) {
        // Nothing to do
    }

    /**
     * The hazelcast membership lister to handle multicast membership scheme related activities
     */
    private class MulticastMembershipListener implements MembershipListener {
        private Map<String, ClusterMember> members;

        public MulticastMembershipListener() {
            members = MemberUtils.getMembersMap(hazelcastInstance, primaryDomain);
        }

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            clusterContext.addMember(HazelcastUtil.toClusterMember(member));
            logger.info("Member joined [" + member.getUuid() + "]: " + member.
                    getInetSocketAddress().toString());
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
            logger.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().
                    toString());
            members.remove(member.getUuid());
        }
    }
}
