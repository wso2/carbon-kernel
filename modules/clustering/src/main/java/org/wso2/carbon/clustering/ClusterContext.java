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

package org.wso2.carbon.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.api.MembershipListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The cluster context class which holds the runtime information of the cluster such as members,
 * membership listeners
 */
public class ClusterContext {

    private static Logger logger = LoggerFactory.getLogger(CarbonCluster.class);

    private List<MembershipListener> membershipListeners = new ArrayList<>();
    private List<ClusterMember> primaryClusterMembers = new ArrayList<>();
    private ClusterConfiguration clusterConfiguration;

    public ClusterContext(ClusterConfiguration clusterConfiguration) {
        this.clusterConfiguration = clusterConfiguration;
    }

    public void addMembershipListener(MembershipListener membershipListener) {
        logger.debug("Adding new membership listener {} ", membershipListener);
        membershipListeners.add(membershipListener);
    }

    public void removeMembershipListener(MembershipListener membershipListener) {
        logger.debug("Removing membership listener {} ", membershipListener);
        membershipListeners.remove(membershipListener);
    }

    public void addMember(ClusterMember clusterMember) {
        logger.debug("Adding new member {} ", clusterMember.getId());
        for (MembershipListener membershipListener : membershipListeners) {
            membershipListener.memberAdded(new MembershipEvent(clusterMember,
                                                               MembershipEvent.MEMBER_ADDED));
        }
        primaryClusterMembers.add(clusterMember);
    }

    public void removeMember(ClusterMember clusterMember) {
        logger.debug("Removing member {} ", clusterMember.getId());
        for (MembershipListener membershipListener : membershipListeners) {
            membershipListener.memberRemoved(new MembershipEvent(clusterMember,
                                                                 MembershipEvent.MEMBER_REMOVED));
        }
        primaryClusterMembers.remove(clusterMember);
    }

    public List<ClusterMember> getPrimaryClusterMembers() {
        return primaryClusterMembers;
    }

    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    public List<MembershipListener> getMembershipListeners() {
        return membershipListeners;
    }

}
