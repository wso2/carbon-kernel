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
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.exception.ClusteringException;
import org.wso2.carbon.clustering.hazelcast.HazelcastCarbonCluster;
import org.wso2.carbon.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.clustering.hazelcast.util.MemberUtils;

import java.util.List;
import java.util.Map;

/**
 * Multicast based membership scheme based on Hazelcast
 */
public class MulticastBasedMembershipScheme implements HazelcastMembershipScheme {
    private static Logger logger = LoggerFactory.getLogger(MulticastBasedMembershipScheme.class);
//    private final Map<String, Parameter> parameters;
    private String primaryDomain;
    private MulticastConfig config;
    private final List<ClusterMessage> messageBuffer;
    private HazelcastCarbonCluster carbonCluster;
    private HazelcastInstance primaryHazelcastInstance;

    public MulticastBasedMembershipScheme(/*Map<String, Parameter> parameters,*/
                                          String primaryDomain,
                                          MulticastConfig config,
                                          List<ClusterMessage> messageBuffer) {
//        this.parameters = parameters;
        this.primaryDomain = primaryDomain;
        this.config = config;
        this.messageBuffer = messageBuffer;
    }

    @Override
    public void init() throws ClusteringException {
        config.setEnabled(true);
        configureMulticastParameters();
    }

    private void configureMulticastParameters() throws ClusteringException {
//        Parameter mcastAddress = getParameter(MulticastConstants.MULTICAST_ADDRESS);
//        if (mcastAddress != null) {
//            config.setMulticastGroup((String) mcastAddress.getValue());
//        }
//        Parameter mcastPort = getParameter(MulticastConstants.MULTICAST_PORT);
//        if (mcastPort != null) {
//            config.setMulticastPort(Integer.parseInt(((String) (mcastPort.getValue())).trim()));
//        }
//        Parameter mcastTimeout = getParameter(MulticastConstants.MULTICAST_TIMEOUT);
//        if (mcastTimeout != null) {
//            config.setMulticastTimeoutSeconds(Integer.parseInt(((String) (mcastTimeout.getValue())).trim()));
//        }
//        Parameter mcastTTL = getParameter(MulticastConstants.MULTICAST_TTL);
//        if (mcastTTL != null) {
//            config.setMulticastTimeToLive(Integer.parseInt(((String) (mcastTTL.getValue())).trim()));
//        }
    }
//
//    public Parameter getParameter(String name) {
//        return parameters.get(name);
//    }

    @Override
    public void joinGroup(){
        primaryHazelcastInstance.getCluster().addMembershipListener(new MulticastMembershipListener());
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
            members.remove(member.getUuid());
        }
    }
}
