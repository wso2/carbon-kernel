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
package org.wso2.carbon.clustering.hazelcast.wka;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
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
import org.wso2.carbon.clustering.exception.MembershipInitializationException;
import org.wso2.carbon.clustering.hazelcast.HazelcastCarbonCluster;
import org.wso2.carbon.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.util.HazelcastUtil;
import org.wso2.carbon.clustering.hazelcast.util.MemberUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Well-known Address membership scheme based on Hazelcast
 */
public class WKABasedMembershipScheme implements HazelcastMembershipScheme {
    private static Logger logger = LoggerFactory.getLogger(WKABasedMembershipScheme.class);
    private ClusterConfiguration clusterConfiguration;
    private String primaryDomain;
    private List<ClusterMember> wkaMembers = new ArrayList<ClusterMember>();
    private final List<ClusterMessage> messageBuffer;
    private NetworkConfig nwConfig;

    private IMap<String, ClusterMember> allMembers;
    private volatile HazelcastInstance primaryHazelcastInstance;
    private com.hazelcast.core.Member localMember;
    private HazelcastCarbonCluster carbonCluster;

    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setCarbonCluster(HazelcastCarbonCluster hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }

    @Override
    public void setLocalMember(com.hazelcast.core.Member localMember) {
        this.localMember = localMember;
    }

    public WKABasedMembershipScheme(ClusterConfiguration clusterConfiguration,
                                    String primaryDomain,
                                    List<ClusterMember> wkaMembers,
                                    Config config,
                                    List<ClusterMessage> messageBuffer) {
        this.clusterConfiguration = clusterConfiguration;
        this.primaryDomain = primaryDomain;
        this.wkaMembers = wkaMembers;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
    }

    @Override
    public void init() throws MembershipInitializationException {
        try {
            nwConfig.getJoin().getMulticastConfig().setEnabled(false);
            TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
            tcpIpConfig.setEnabled(true);
            configureWKAParameters();

            // Add the WKA members
            for (ClusterMember wkaMember : wkaMembers) {
                MemberUtils.addMember(wkaMember, tcpIpConfig);
            }
        } catch (Exception e) {
            throw new MembershipInitializationException("Error while trying initialize " +
                                                        "WKA membership scheme", e);
        }
    }

    private boolean isLocalMember(ClusterMember member) {
        return member.getHostName().equals(nwConfig.getPublicAddress()) &&
               member.getPort() == nwConfig.getPort();
    }

    private void configureWKAParameters() {
        String connTimeout = clusterConfiguration.getFirstProperty(WKAConstants.CONNECTION_TIMEOUT);
        TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
        if (connTimeout != null) {
            tcpIpConfig.
                    setConnectionTimeoutSeconds(Integer.parseInt(connTimeout.trim()));
        }
    }


    @Override
    public void joinGroup() throws MembershipFailedException {
        try {
            primaryHazelcastInstance.getCluster().addMembershipListener(new WKAMembershipListener());
            allMembers = MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain);
            allMembers.addEntryListener(new MemberEntryListener(), true);

            // Add the rest of the members
            for (ClusterMember member : allMembers.values()) {
                InetSocketAddress inetSocketAddress = localMember.getInetSocketAddress();
                if (!member.getHostName().equals(inetSocketAddress.getHostName()) &&
                    member.getPort() != inetSocketAddress.getPort()) {  // Don't add the local member
                    MemberUtils.addMember(member, nwConfig.getJoin().getTcpIpConfig());
                }
            }
        } catch (Exception e) {
            throw new MembershipFailedException("Error while trying join wka " +
                                                "membership scheme", e);
        }
    }

    private class WKAMembershipListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();
            if (primaryHazelcastInstance.getCluster().getLocalMember().equals(member)) {
                return;
            }
            carbonCluster.addMember(HazelcastUtil.toClusterMember(member));
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
            logger.info("Member joined [" + member.getUuid() + "]: " +
                     member.getInetSocketAddress().toString());
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            com.hazelcast.core.Member hazelcastMember = membershipEvent.getMember();
            String uuid = hazelcastMember.getUuid();
            logger.info("Member left [" + uuid + "]: " +
                     hazelcastMember.getInetSocketAddress().toString());

            // If the member who left is a WKA member, try to keep reconnecting to it
            ClusterMember member = allMembers.get(membershipEvent.getMember().getUuid());
            if (member == null) {
                return;
            }
            boolean isWKAMember = false;
            for (ClusterMember wkaMember : wkaMembers) {
                if (wkaMember.getHostName().equals(member.getHostName()) &&
                    wkaMember.getPort() == member.getPort()) {
                    logger.info("WKA member " + member + " left cluster.");
                    isWKAMember = true;
                    break;
                }
            }
            carbonCluster.removeMember(HazelcastUtil.toClusterMember(hazelcastMember));
            if (!isWKAMember) {
                allMembers.remove(uuid);
            }
        }
    }

    /**
     * This class is maintained in order to obtain member properties
     */
    private class MemberEntryListener implements EntryListener<String, ClusterMember> {
        @Override
        public void entryAdded(EntryEvent<String, ClusterMember> entryEvent) {
            MemberUtils.addMember(entryEvent.getValue(), nwConfig.getJoin().getTcpIpConfig());
        }

        @Override
        public void entryRemoved(EntryEvent<String, ClusterMember> entryEvent) {
            // Nothing to do
        }

        @Override
        public void entryUpdated(EntryEvent<String, ClusterMember> stringMemberEntryEvent) {
            // Nothing to do
        }

        @Override
        public void entryEvicted(EntryEvent<String, ClusterMember> stringMemberEntryEvent) {
            // Nothing to do
        }
    }
}
