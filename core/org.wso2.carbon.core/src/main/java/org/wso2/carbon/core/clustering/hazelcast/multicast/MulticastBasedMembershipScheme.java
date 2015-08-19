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
package org.wso2.carbon.core.clustering.hazelcast.multicast;

import com.hazelcast.config.Config;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.description.Parameter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.core.clustering.hazelcast.HazelcastUtil;
import org.wso2.carbon.core.clustering.hazelcast.util.MemberUtils;
import org.wso2.carbon.core.clustering.MembershipScheme;

import java.util.List;
import java.util.Map;

/**
 * Multicast based membership scheme based on Hazelcast
 */
public class MulticastBasedMembershipScheme implements HazelcastMembershipScheme {
    private static final Log log = LogFactory.getLog(MulticastBasedMembershipScheme.class);
    private Map<String, Parameter> parameters;
    private String primaryDomain;
    private MulticastConfig config;
    private List<ClusteringMessage> messageBuffer;
    private HazelcastCarbonClusterImpl carbonCluster;
    private HazelcastInstance primaryHazelcastInstance;

    public MulticastBasedMembershipScheme() {
    }

    @Override
    public void init(Map<String, Parameter> parameters,
                     String primaryDomain,
                     List<org.apache.axis2.clustering.Member> wkaMembers,
                     Config primaryHazelcastConfig,
                     HazelcastInstance primaryHazelcastInstance,
                     List<ClusteringMessage> messageBuffer) throws ClusteringFault {

        this.parameters = parameters;
        this.primaryDomain = primaryDomain;
        this.config = primaryHazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig();
        this.messageBuffer = messageBuffer;

        init();
    }

    @Override
    public void init() throws ClusteringFault {
        config.setEnabled(true);
        configureMulticastParameters();
    }

    private void configureMulticastParameters() throws ClusteringFault {
        Parameter mcastAddress = getParameter(MulticastConstants.MULTICAST_ADDRESS);
        if (mcastAddress != null) {
            config.setMulticastGroup((String) mcastAddress.getValue());
        }
        Parameter mcastPort = getParameter(MulticastConstants.MULTICAST_PORT);
        if (mcastPort != null) {
            config.setMulticastPort(Integer.parseInt(((String) (mcastPort.getValue())).trim()));
        }
        Parameter mcastTimeout = getParameter(MulticastConstants.MULTICAST_TIMEOUT);
        if (mcastTimeout != null) {
            config.setMulticastTimeoutSeconds(Integer.parseInt(((String) (mcastTimeout.getValue())).trim()));
        }
        Parameter mcastTTL = getParameter(MulticastConstants.MULTICAST_TTL);
        if (mcastTTL != null) {
            config.setMulticastTimeToLive(Integer.parseInt(((String) (mcastTTL.getValue())).trim()));
        }
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public void joinGroup() throws ClusteringFault {
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
    public void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster) {
        this.carbonCluster = hazelcastCarbonCluster;
    }

    private class MulticastMembershipListener implements MembershipListener {
        private Map<String, org.apache.axis2.clustering.Member> members;

        public MulticastMembershipListener() {
            members = MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain);
        }

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            carbonCluster.memberAdded(member);
            log.info("Member joined [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
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
            carbonCluster.memberRemoved(member);
            log.info("Member left [" + member.getUuid() + "]: " + member.getInetSocketAddress().toString());
            members.remove(member.getUuid());
        }

        @Override
        public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        }

    }
}
