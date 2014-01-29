package org.wso2.carbon.clustering.api;

import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;

import java.util.List;


public interface Cluster {
    void sendMessage(ClusterMessage clusterMessage);

    void sendMessage(ClusterMessage clusterMessage, List<ClusterMember> members);

    List<ClusterMember> getMembers();
}
