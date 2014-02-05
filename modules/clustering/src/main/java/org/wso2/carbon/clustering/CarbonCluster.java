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
package org.wso2.carbon.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.api.Cluster;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.clustering.internal.DataHolder;
import org.wso2.carbon.clustering.spi.ClusteringAgent;

import java.util.Collections;
import java.util.List;

/**
 * The cluster api implementation
 */
public class CarbonCluster implements Cluster {
    private static Logger logger = LoggerFactory.getLogger(CarbonCluster.class);

    private ClusteringAgent clusteringAgent;

    public CarbonCluster(ClusteringAgent clusteringAgent) {
        this.clusteringAgent = clusteringAgent;
    }

    @Override
    public void sendMessage(ClusterMessage clusterMessage) {
        try {
            clusteringAgent.sendMessage(clusterMessage);
        } catch (MessageFailedException e) {
            logger.error("Error while sending message to cluster", e);
        }
    }

    @Override
    public void sendMessage(ClusterMessage clusterMessage, List<ClusterMember> members) {
        try {
            clusteringAgent.sendMessage(clusterMessage, members);
        } catch (MessageFailedException e) {
            logger.error("Error while sending message to cluster members", e);
        }
    }

    @Override
    public List<ClusterMember> getMembers() {
        ClusterContext clusterContext = DataHolder.getInstance().getClusterContext();
        return Collections.unmodifiableList(clusterContext.getPrimaryClusterMembers());
    }
}
