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
package org.wso2.carbon.core.clustering.hazelcast;

import com.hazelcast.config.AwsConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.MapEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.util.MemberUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * GroupManagementAgent based on Hazelcast
 */
public class HazelcastGroupManagementAgent implements GroupManagementAgent {
    private static final Log log = LogFactory.getLog(HazelcastGroupManagementAgent.class);
    private static final String PORT_MAPPING_PREFIX = "port.mapping.";
    private String description;
    private IMap<String, Member> members;
    private List<Member> connectedMembers = new CopyOnWriteArrayList<Member>();
    private String localMemberUUID;
    private String domain;
    private String subDomain;
    private String securityGroup;
    private ITopic<GroupManagementCommand> groupManagementTopic;
    private int groupMgtPort;
    private TcpIpConfig tcpIpConfig;
    private List<Member> wkaMembers = new ArrayList<Member>();

    public void init(Config primaryHazelcastConfig,
                     ConfigurationContext configurationContext) {
        NetworkConfig primaryNwConfig = primaryHazelcastConfig.getNetworkConfig();
        Config config = new Config();
        config.setInstanceName(domain);
        config.setMemberAttributeConfig(primaryHazelcastConfig.getMemberAttributeConfig());
        NetworkConfig groupNwConfig = config.getNetworkConfig();
        groupNwConfig.setPublicAddress(primaryNwConfig.getPublicAddress());
        if (primaryNwConfig.getPort() == groupMgtPort) {
            throw new IllegalArgumentException("group_mgt_port for the " + domain + " is the same as the primary localMemberPort. Please set a different value");
        }
        groupNwConfig.setPort(groupMgtPort);
        MulticastConfig primaryMulticastConfig = primaryNwConfig.getJoin().getMulticastConfig();
        AwsConfig primaryAwsConfig = primaryNwConfig.getJoin().getAwsConfig();
        MulticastConfig groupMulticastConfig = groupNwConfig.getJoin().getMulticastConfig();
        AwsConfig groupAwsConfig = groupNwConfig.getJoin().getAwsConfig();
        groupMulticastConfig.setEnabled(primaryMulticastConfig.isEnabled());
        groupNwConfig.getJoin().getTcpIpConfig().setEnabled(primaryNwConfig.getJoin().getTcpIpConfig().isEnabled());
        groupNwConfig.getJoin().getAwsConfig().setEnabled(primaryNwConfig.getJoin().getAwsConfig().isEnabled());
        config.setLicenseKey(primaryHazelcastConfig.getLicenseKey());
        config.setManagementCenterConfig(primaryHazelcastConfig.getManagementCenterConfig());

        tcpIpConfig = groupNwConfig.getJoin().getTcpIpConfig();
        if (primaryMulticastConfig.isEnabled()) {
            groupMulticastConfig.setMulticastPort(primaryMulticastConfig.getMulticastPort());
            groupMulticastConfig.setMulticastGroup(primaryMulticastConfig.getMulticastGroup());
            groupMulticastConfig.setMulticastTimeoutSeconds(primaryMulticastConfig.getMulticastTimeoutSeconds());
            groupMulticastConfig.setMulticastTimeToLive(primaryMulticastConfig.getMulticastTimeToLive());
        } else if (groupNwConfig.getJoin().getAwsConfig().isEnabled()) {
            groupAwsConfig.setAccessKey(primaryAwsConfig.getAccessKey());
            groupAwsConfig.setSecretKey(primaryAwsConfig.getSecretKey());
            groupAwsConfig.setTagKey(primaryAwsConfig.getTagKey());
            groupAwsConfig.setTagValue(primaryAwsConfig.getTagValue());
            groupAwsConfig.setRegion(primaryAwsConfig.getRegion());
            groupAwsConfig.setHostHeader(primaryAwsConfig.getHostHeader());
//            groupAwsConfig.setSecurityGroupName(primaryAwsConfig.getSecurityGroupName()); //TODO: Find a way to set security group
            groupAwsConfig.setConnectionTimeoutSeconds(primaryAwsConfig.getConnectionTimeoutSeconds());
        } else if (groupNwConfig.getJoin().getTcpIpConfig().isEnabled()) {
            tcpIpConfig.setConnectionTimeoutSeconds(primaryNwConfig.getJoin().getTcpIpConfig().getConnectionTimeoutSeconds());
            for (Member wkaMember : wkaMembers) {
                tcpIpConfig.addMember(wkaMember.getHostName() + ":" + wkaMember.getPort());
            }
        }

        GroupConfig groupConfig = config.getGroupConfig();
        groupConfig.setName(domain);
        config.setProperties(primaryHazelcastConfig.getProperties());
        HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(domain);
        if (hazelcastInstance == null) {
            hazelcastInstance = Hazelcast.newHazelcastInstance(config);
        }
        hazelcastInstance.getCluster().addMembershipListener(new GroupMembershipListener());
        localMemberUUID = hazelcastInstance.getCluster().getLocalMember().getUuid();
        Member localMember =
                MemberUtils.getLocalMember(domain, groupNwConfig.getPublicAddress(),
                                           groupMgtPort);
        log.info("Group management local member for domain [" + domain + "],sub-domain [" +
                 subDomain + "] UUID: " + localMemberUUID + ". " + localMember);
        MemberUtils.getMembersMap(hazelcastInstance, domain).put(localMemberUUID, localMember);
        members = MemberUtils.getMembersMap(hazelcastInstance, domain);
        members.addEntryListener(new MemberEntryListener(), true);
        for (Member member : members.values()) {
            connectMember(member);
        }
        groupManagementTopic = hazelcastInstance.getTopic(HazelcastConstants.GROUP_MGT_CMD_TOPIC);
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setSubDomain(String subDomain) {
        this.subDomain = subDomain;
    }

    @Override
    public void setGroupMgtPort(int groupMgtPort) {
        this.groupMgtPort = groupMgtPort;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void applicationMemberAdded(Member member) {
    	// Nothing to implement
    }

    @Override
    public void applicationMemberRemoved(Member member) {
    	// Nothing to implement
    }

    @Override
    public List<Member> getMembers() {
        return connectedMembers;
    }

    @Override
    public void send(GroupManagementCommand groupManagementCommand) throws ClusteringFault {
        groupManagementTopic.publish(groupManagementCommand);
    }

    public void addMember(Member member) {
        wkaMembers.add(member);
    }

    private class GroupMembershipListener implements MembershipListener {

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            com.hazelcast.core.Member member = membershipEvent.getMember();
            log.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
        }

        @Override
        public void memberRemoved(MembershipEvent membershipEvent) {
            com.hazelcast.core.Member member = membershipEvent.getMember();
            log.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            Member removed = members.remove(membershipEvent.getMember().getUuid());
            connectedMembers.remove(removed);
        }

        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }
    }

    private class MemberEntryListener implements EntryListener<String, Member> {

        @Override
        public void entryAdded(EntryEvent<String, Member> entryEvent) {
            if (entryEvent.getKey().equals(localMemberUUID)) { // Ignore local member
                return;
            }
            Member member = entryEvent.getValue();
            connectMember(member);
        }

        @Override
        public void entryRemoved(EntryEvent<String, Member> entryEvent) {
            // With the hazelcast 3.2.6 upgrade there has been an implementation change in the
            // EntryEvent which is received to the EntryListener when an entry is removed from
            // the IMap.
            // Now a null value is returned from the EntryEvent#getValue method where the removed
            // member is available in the EntryEvent#getOldValue method.
            // More info : https://wso2.org/jira/browse/CARBON-15057
            Member memberToRemove = entryEvent.getOldValue();
            connectedMembers.remove(memberToRemove);
            applicationMemberRemoved(memberToRemove);
        }

        @Override
        public void entryUpdated(EntryEvent<String, Member> entryEvent) {
            // Nothing to do
        }

        @Override
        public void entryEvicted(EntryEvent<String, Member> stringObjectEntryEvent) {
            // Nothing to do
        }

        @Override
        public void mapEvicted(MapEvent mapEvent) {
            // Nothing to do
        }

        @Override
        public void mapCleared(MapEvent mapEvent) {
            // Nothing to do
        }
    }

    private void connectMember(Member member) {
        if (!member.getDomain().equals(domain) || !subDomain.equals(member.getProperties().get("subDomain"))) {
            return;
        }
        if (!connectedMembers.contains(member)) {
            Thread th = new Thread(new MemberAdder(member));
            th.setPriority(Thread.MAX_PRIORITY);
            th.start();
        }
    }

    private class MemberAdder implements Runnable {

        private final Member member;

        private MemberAdder(Member member) {
            this.member = member;
        }

        public void run() {
            if (connectedMembers.contains(member)) {
                return;
            }
            if (canConnect(member)) {
                if (!connectedMembers.contains(member)) {
                    connectedMembers.add(member);
                    if (tcpIpConfig.isEnabled()) {
                        MemberUtils.addMember(member, tcpIpConfig);
                    }
                }
                log.info("Application member " + member + " joined application cluster");
            } else {
                log.error("Could not add application member " + member);
            }
        }

        /**
         * Before adding a member, we will try to verify whether we can connect to it
         *
         * @param member The member whose connectvity needs to be verified
         * @return true, if the member can be contacted; false, otherwise.
         */
        private boolean canConnect(Member member) {
            if (log.isDebugEnabled()) {
                log.debug("Trying to connect to member " + member + "...");
            }
            for (int retries = 30; retries > 0; retries--) {
                try {
                    InetAddress addr = InetAddress.getByName(member.getHostName());

                    // keep mapped ports
                    List<String> mappedPorts = new ArrayList<String>();
                    // extract mapped ports
                    Properties memberProperties = member.getProperties();
                    for (String property : memberProperties.stringPropertyNames()) {
                        if (property.contains(PORT_MAPPING_PREFIX)) {
                            mappedPorts.add(memberProperties.getProperty(property));
                        }
                    }
                    // checking the connection to the mapped ports
                    for (String portStr : mappedPorts) {
                        if (log.isDebugEnabled()) {
                            log.debug("Mapped Port=" + portStr);
                        }
                        int port = Integer.parseInt(portStr);
                        SocketAddress httpSockaddr = new InetSocketAddress(addr, port);
                        new Socket().connect(httpSockaddr, 10000);
                    }
                    int httpPort = member.getHttpPort();
                    if (log.isDebugEnabled()) {
                        log.debug("HTTP Port=" + httpPort);
                    }
                    if (httpPort != -1) {
                        SocketAddress httpSockaddr = new InetSocketAddress(addr, httpPort);
                        new Socket().connect(httpSockaddr, 10000);
                    }
                    int httpsPort = member.getHttpsPort();
                    if (log.isDebugEnabled()) {
                        log.debug("HTTPS Port=" + httpsPort);
                    }
                    if (httpsPort != -1) {
                        SocketAddress httpsSockaddr = new InetSocketAddress(addr, httpsPort);
                        new Socket().connect(httpsSockaddr, 10000);
                    }
                    if (httpPort == -1 && httpsPort == -1) {
                        return false;
                    }
                    return true;
                } catch (IOException e) {
                    if (log.isDebugEnabled()) {
                        log.debug("", e);
                    }
                    String msg = e.getMessage();
                    if (!msg.contains("Connection refused") && !msg.contains("connect timed out")) {
                        log.error("Cannot connect to member " + member, e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            return false;
        }
    }
}
