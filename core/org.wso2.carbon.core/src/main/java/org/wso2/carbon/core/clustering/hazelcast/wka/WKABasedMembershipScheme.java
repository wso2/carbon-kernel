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
package org.wso2.carbon.core.clustering.hazelcast.wka;

import com.hazelcast.config.Config;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.core.clustering.hazelcast.util.MemberUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Well-known Address membership scheme based on Hazelcast
 */
public class WKABasedMembershipScheme implements HazelcastMembershipScheme {
    private static final Log log = LogFactory.getLog(WKABasedMembershipScheme.class);
    private Map<String, Parameter> parameters;
    private String primaryDomain;
    private List<Member> wkaMembers = new ArrayList<Member>();
    private final List<ClusteringMessage> messageBuffer;
    private NetworkConfig nwConfig;

    private IMap<String, Member> allMembers;
    private volatile HazelcastInstance primaryHazelcastInstance;
    private com.hazelcast.core.Member localMember;
    private HazelcastCarbonClusterImpl carbonCluster;

    public void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance) {
        this.primaryHazelcastInstance = primaryHazelcastInstance;
    }

    @Override
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }

    @Override
    public void setLocalMember(com.hazelcast.core.Member localMember) {
        this.localMember = localMember;
    }

    public WKABasedMembershipScheme(Map<String, Parameter> parameters,
                                    String primaryDomain,
                                    List<Member> wkaMembers,
                                    Config config,
                                    List<ClusteringMessage> messageBuffer) {
        this.parameters = parameters;
        this.primaryDomain = primaryDomain;
        this.wkaMembers = wkaMembers;
        this.messageBuffer = messageBuffer;
        this.nwConfig = config.getNetworkConfig();
    }

    @Override
    public void init() throws ClusteringFault {
        nwConfig.getJoin().getMulticastConfig().setEnabled(false);
        TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        configureWKAParameters();

        // Add the WKA members
        for (org.apache.axis2.clustering.Member wkaMember : wkaMembers) {
            MemberUtils.addMember(wkaMember, tcpIpConfig);
        }
    }

    private boolean isLocalMember(Member member) {
        return member.getHostName().equals(nwConfig.getPublicAddress()) &&
               member.getPort() == nwConfig.getPort();
    }

    private void configureWKAParameters() throws ClusteringFault {
        Parameter connTimeout = getParameter(WKAConstants.CONNECTION_TIMEOUT);
        TcpIpConfig tcpIpConfig = nwConfig.getJoin().getTcpIpConfig();
        if (connTimeout != null) {
            tcpIpConfig.
                    setConnectionTimeoutSeconds(Integer.parseInt(((String) (connTimeout.getValue())).trim()));
        }
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public void joinGroup() throws ClusteringFault {
        primaryHazelcastInstance.getCluster().addMembershipListener(new WKAMembershipListener());
        allMembers = MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain);
        allMembers.addEntryListener(new MemberEntryListener(), true);

        // Add the rest of the members
        for (Member member : allMembers.values()) {
            InetSocketAddress inetSocketAddress = localMember.getInetSocketAddress();
            if (!member.getHostName().equals(inetSocketAddress.getHostName()) &&
                member.getPort() != inetSocketAddress.getPort()) {  // Don't add the local member
                MemberUtils.addMember(member, nwConfig.getJoin().getTcpIpConfig());
            }
        }
    }

    private class WKAMembershipListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            com.hazelcast.core.Member member = membershipEvent.getMember();
            if (primaryHazelcastInstance.getCluster().getLocalMember().equals(member)) {
                return;
            }
            carbonCluster.memberAdded(member);
            HazelcastUtil.sendMessagesToMember(messageBuffer, member, carbonCluster);
            log.info("Member joined [" + member.getUuid() + "]: " +
                     member.getInetSocketAddress().toString());
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            com.hazelcast.core.Member hazelcastMember = membershipEvent.getMember();
            String uuid = hazelcastMember.getUuid();
            log.info("Member left [" + uuid + "]: " +
                     hazelcastMember.getInetSocketAddress().toString());

            // If the member who left is a WKA member, try to keep reconnecting to it
            Member member = allMembers.get(membershipEvent.getMember().getUuid());
            if (member == null) {
                return;
            }
            boolean isWKAMember = false;
            for (Member wkaMember : wkaMembers) {
                if (wkaMember.getHostName().equals(member.getHostName()) &&
                    wkaMember.getPort() == member.getPort()) {
                    log.info("WKA member " + member + " left cluster.");
                    isWKAMember = true;
                    break;
                }
            }
            carbonCluster.memberRemoved(hazelcastMember);
            if (!isWKAMember) {
                allMembers.remove(uuid);
            }
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }

    }

    /**
     * This class is maintained in order to obtain member properties
     */
    private class MemberEntryListener implements EntryListener<String, Member> {
        @Override
        public void entryAdded(EntryEvent<String, Member> entryEvent) {
            MemberUtils.addMember(entryEvent.getValue(), nwConfig.getJoin().getTcpIpConfig());
        }

        @Override
        public void entryRemoved(EntryEvent<String, Member> entryEvent) {
            // Nothing to do
        }

        @Override
        public void entryUpdated(EntryEvent<String, Member> stringMemberEntryEvent) {
            // Nothing to do
        }

        @Override
        public void entryEvicted(EntryEvent<String, Member> stringMemberEntryEvent) {
            // Nothing to do
        }
    }
}
