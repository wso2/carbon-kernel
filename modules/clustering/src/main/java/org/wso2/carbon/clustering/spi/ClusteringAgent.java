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

package org.wso2.carbon.clustering.spi;

import org.wso2.carbon.clustering.ClusterContext;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.exception.MessageFailedException;

import java.util.List;

/**
 * The ClusteringAgent which manages the cluster node in a cluster. This will basically do the
 * starting, joining and shutdown the node with cluster. It also provide the functionality to
 * send cluster messages to the cluster, or w set of cluster members in the cluster.
 * <p>
 * Any new clustering implementation that need to be plugged into carbon, should implement this and
 * register it as an OSGi service with the service level property (agentType) to uniquely identify
 * it at runtime.
 *
 * @since 5.0.0
 */
public interface ClusteringAgent {

    /**
     * Initialize the agent which will initialize this node, and join the cluster
     *
     * @param clusterContext the cluster context to be used for initializing the cluster agent
     * @throws ClusterInitializationException on error while initializing the cluster
     * @see ClusterContext
     */
    void init(ClusterContext clusterContext) throws ClusterInitializationException;

    /**
     * Shutdown the agent which will remove this node from cluster
     */
    void shutdown();

    /**
     * Send a message to all members in the cluster
     *
     * @param msg the cluster message to send
     * @throws MessageFailedException on error while sending the message
     * @see ClusterMessage
     */
    void sendMessage(ClusterMessage msg) throws MessageFailedException;

    /**
     * Send a message to a set of specific members in the cluster
     *
     * @param msg the cluster message to send
     * @param members the set of members to whom the cluster message should be sent
     * @throws MessageFailedException on error while sending the message
     * @see ClusterMessage
     * @see ClusterMember
     */
    void sendMessage(ClusterMessage msg, List<ClusterMember> members) throws MessageFailedException;
}