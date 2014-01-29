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
package org.wso2.carbon.clustering.hazelcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.MembershipEvent;
import org.wso2.carbon.clustering.api.Cluster;
import org.wso2.carbon.clustering.api.MembershipListener;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.spi.ClusteringAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TODO: class description
 */
public class HazelcastCarbonCluster implements Cluster {
    private static Logger logger = LoggerFactory.getLogger(HazelcastCarbonCluster.class);

    private ClusteringAgent clusteringAgent;
    private List<MembershipListener> membershipListeners = new ArrayList<MembershipListener>();
    private List<ClusterMember> primaryClusterMembers = new ArrayList<ClusterMember>();

    public HazelcastCarbonCluster(ClusteringAgent clusteringAgent) {
        this.clusteringAgent = clusteringAgent;
    }

    @Override
    public void sendMessage(ClusterMessage clusterMessage) {
        try {
            clusteringAgent.sendMessage(clusterMessage);
        } catch (MessageFailedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMessage(ClusterMessage clusterMessage, List<ClusterMember> members) {
        try {
            clusteringAgent.sendMessage(clusterMessage, members);
        } catch (MessageFailedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ClusterMember> getMembers() {
        return Collections.unmodifiableList(primaryClusterMembers);
    }

    public void addMembershipListener(MembershipListener membershipListener) {
        logger.debug("Adding new membership listener {} " ,membershipListener);
        membershipListeners.add(membershipListener);
    }

    public void removeMembershipListener(MembershipListener membershipListener) {
        logger.debug("Removing membership listener {} " ,membershipListener);
        membershipListeners.remove(membershipListener);
    }

    public void addMember(ClusterMember clusterMember) {
        logger.debug("Adding new member {} ", clusterMember.getId());
        for (MembershipListener membershipListener : membershipListeners) {
            membershipListener.memberAdded(new MembershipEvent(clusterMember, 1));
        }
        primaryClusterMembers.add(clusterMember);
    }

    public void removeMember(ClusterMember clusterMember) {
        logger.debug("Removing member {} ", clusterMember.getId());
        for (MembershipListener membershipListener : membershipListeners) {
            membershipListener.memberRemoved(new MembershipEvent(clusterMember, 2));
        }
        primaryClusterMembers.remove(clusterMember);
    }
}
