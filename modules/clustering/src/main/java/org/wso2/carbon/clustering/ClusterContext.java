package org.wso2.carbon.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.clustering.api.MembershipListener;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClusterContext {

    private static Logger logger = LoggerFactory.getLogger(CarbonCluster.class);

    private List<MembershipListener> membershipListeners = new ArrayList<>();
    private List<ClusterMember> primaryClusterMembers = new ArrayList<>();
    private ClusterConfiguration clusterConfiguration;

    public ClusterContext(ClusterConfiguration clusterConfiguration) {
        this.clusterConfiguration = clusterConfiguration;
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

    public List<ClusterMember> getPrimaryClusterMembers() {
        return primaryClusterMembers;
    }

    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

}
