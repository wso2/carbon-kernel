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
package org.wso2.carbon.clustering.hazelcast.multicast;

import com.hazelcast.config.MulticastConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterConfiguration;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;
import org.wso2.carbon.clustering.exception.MembershipFailedException;
import org.wso2.carbon.clustering.hazelcast.HazelcastCarbonCluster;
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
    private String primaryDomain;
    private MulticastConfig config;
    private final List<ClusterMessage> messageBuffer;
    private HazelcastCarbonCluster carbonCluster;
    private HazelcastInstance primaryHazelcastInstance;

    public MulticastBasedMembershipScheme(ClusterConfiguration clusterConfiguration,
                                          String primaryDomain,
                                          MulticastConfig config,
                                          List<ClusterMessage> messageBuffer) {
        this.clusterConfiguration = clusterConfiguration;
        this.primaryDomain = primaryDomain;
        this.config = config;
        this.messageBuffer = messageBuffer;
    }

    @Override
    public void init() {
        config.setEnabled(true);
        configureMulticastParameters();
    }

    private void configureMulticastParameters() {
        String mcastAddress = getProperty(MulticastConstants.MULTICAST_ADDRESS);
        if (mcastAddress != null) {
            config.setMulticastGroup(mcastAddress);
        }
        String mcastPort = getProperty(MulticastConstants.MULTICAST_PORT);
        if (mcastPort != null) {
            config.setMulticastPort(Integer.parseInt(mcastPort.trim()));
        }
        String mcastTimeout = getProperty(MulticastConstants.MULTICAST_TIMEOUT);
        if (mcastTimeout != null) {
            config.setMulticastTimeoutSeconds(Integer.parseInt(mcastTimeout.trim()));
        }
        String mcastTTL = getProperty(MulticastConstants.MULTICAST_TTL);
        if (mcastTTL != null) {
            config.setMulticastTimeToLive(Integer.parseInt(mcastTTL.trim()));
        }
    }

    private String getProperty(String name) {
        return clusterConfiguration.getFirstProperty(name);
    }

    @Override
    public void joinGroup() throws MembershipFailedException {
        try {
            primaryHazelcastInstance.getCluster().
                    addMembershipListener(new MulticastMembershipListener());
        } catch (Exception e) {
            throw new MembershipFailedException("Error while trying join multicast " +
                                                "membership scheme", e);
        }
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
    public void setCarbonCluster(HazelcastCarbonCluster hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }

    private class MulticastMembershipListener implements MembershipListener {
        private Map<String, ClusterMember> members;

        public MulticastMembershipListener() {
            members = MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain);
        }

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            carbonCluster.addMember(HazelcastUtil.toClusterMember(member));
            logger.info("Member joined [" + member.getUuid() + "]: " + member.
                    getInetSocketAddress().toString());
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
            logger.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().
                    toString());
            members.remove(member.getUuid());
        }
    }
}
