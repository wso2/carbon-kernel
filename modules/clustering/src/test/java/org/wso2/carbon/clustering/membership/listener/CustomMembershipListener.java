package org.wso2.carbon.clustering.membership.listener;

import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.MembershipEvent;
import org.wso2.carbon.clustering.api.MembershipListener;


public class CustomMembershipListener implements MembershipListener {
    private static String addedMember;
    private static String removedMember;
    @Override
    public void memberAdded(MembershipEvent event) {
        ClusterMember clusterMember = event.getMember();
        addedMember = clusterMember.getHostName() + ":" + clusterMember.getPort();
    }

    @Override
    public void memberRemoved(MembershipEvent event) {
        ClusterMember clusterMember = event.getMember();
        removedMember = clusterMember.getHostName() + ":" + clusterMember.getPort();
    }

    public String getAddedMember() {
        return addedMember;
    }

    public String getRemovedMember() {
        return removedMember;
    }
}
