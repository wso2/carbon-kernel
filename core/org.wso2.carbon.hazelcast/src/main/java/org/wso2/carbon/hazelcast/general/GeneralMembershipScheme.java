/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.hazelcast.general;

import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.*;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.hazelcast.HazelcastCarbonClusterImpl;
import org.wso2.carbon.hazelcast.HazelcastMembershipScheme;
import org.wso2.carbon.hazelcast.HazelcastUtil;
import org.wso2.carbon.hazelcast.multicast.MulticastBasedMembershipScheme;
import org.wso2.carbon.hazelcast.util.MemberUtils;

import java.util.List;
import java.util.Map;

/**
 * This membership scheme is used to update carbon cluster when
 * hazelcast.xml is used for hazelcast configuration.
 */
public class GeneralMembershipScheme implements HazelcastMembershipScheme {
    private static final Log log = LogFactory.getLog(MulticastBasedMembershipScheme.class);
    private String primaryDomain;
    private final List<ClusteringMessage> messageBuffer;
    private HazelcastCarbonClusterImpl carbonCluster;
    private HazelcastInstance primaryHazelcastInstance;

    public GeneralMembershipScheme(String primaryDomain,
                                   List<ClusteringMessage> messageBuffer) {
        this.primaryDomain = primaryDomain;
        this.messageBuffer = messageBuffer;
    }

    @Override
    public void init() throws ClusteringFault {
        // Nothing to do
    }

    @Override
    public void joinGroup() throws ClusteringFault {
        primaryHazelcastInstance.getCluster().addMembershipListener(new GeneralMembershipListener());
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

    private class GeneralMembershipListener implements MembershipListener {
        private Map<String, org.apache.axis2.clustering.Member> members;

        public GeneralMembershipListener() {
            members = MemberUtils.getMembersMap(primaryHazelcastInstance, primaryDomain);
        }

        @Override
        public void memberAdded(MembershipEvent membershipEvent) {
            Member member = membershipEvent.getMember();

            // send all cluster messages
            carbonCluster.memberAdded(member);
            log.info("Member joined [" + member.getUuid() + "]: " + member.getSocketAddress().toString());
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
            log.info("Member left [" + member.getUuid() + "]: " + member.getSocketAddress().toString());
            members.remove(member.getUuid());
        }

    }
}
