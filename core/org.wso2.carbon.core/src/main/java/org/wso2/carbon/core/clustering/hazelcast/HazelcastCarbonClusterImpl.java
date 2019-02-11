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

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.clustering.api.CarbonCluster;
import org.wso2.carbon.core.clustering.api.ClusterMember;
import org.wso2.carbon.core.clustering.api.ClusterMembershipListener;
import org.wso2.carbon.core.clustering.api.ClusterMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: class description
 */
public class HazelcastCarbonClusterImpl implements CarbonCluster {
    private static final Log log = LogFactory.getLog(HazelcastCarbonClusterImpl.class);

    private HazelcastInstance hazelcastInstance;
    private List<ClusterMembershipListener> membershipListeners = new ArrayList<ClusterMembershipListener>();
    private List<ClusterMember> primaryClusterMembers = new ArrayList<ClusterMember>();

    public HazelcastCarbonClusterImpl(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public List<ClusterMember> getClusterMembers() {
        return Collections.unmodifiableList(primaryClusterMembers);
    }

    @Override
    public void addMembershipListener(ClusterMembershipListener membershipListener) {
        membershipListeners.add(membershipListener);
    }

    @Override
    public void removeMembershipListener(ClusterMembershipListener membershipListener) {
        membershipListeners.remove(membershipListener);
    }

    @Override
    public void sendMessage(ClusterMessage clusterMessage) {
        //TODO
    }

    @Override
    public void sendMessage(ClusterMessage clusterMessage, List<ClusterMember> members) {
        for (ClusterMember member : members) {
            ITopic<ClusterMessage> msgTopic = hazelcastInstance.getTopic(HazelcastConstants.REPLAY_MESSAGE_QUEUE + member.getId());
            msgTopic.publish(clusterMessage);
        }
    }

    public void memberAdded(Member member) {
        ClusterMember clusterMember = HazelcastUtil.toClusterMember(member);
        for (ClusterMembershipListener membershipListener : membershipListeners) {
            membershipListener.memberAdded(clusterMember);
        }
        primaryClusterMembers.add(clusterMember);
    }

    public void memberRemoved(Member member) {
        ClusterMember clusterMember = HazelcastUtil.toClusterMember(member);
        for (ClusterMembershipListener membershipListener : membershipListeners) {
            membershipListener.memberRemoved(clusterMember);
        }
        primaryClusterMembers.remove(clusterMember);
    }


}
