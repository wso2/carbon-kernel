/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.clustering.spi;

import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.exception.ClusterInitializationException;
import org.wso2.carbon.clustering.exception.MessageFailedException;

import java.util.List;

public interface ClusteringAgent {

    /**
     * Initialize the agent which will initialize this node, and join the cluster
     */
    void init() throws ClusterInitializationException;

    /**
     * Shutdown the agent which will remove this node from cluster
     */
    void shutdown();

    /**
     * Send a message to all members in the cluster
     */
    void sendMessage(ClusterMessage msg) throws MessageFailedException;

    /**
     * Send a message to a set of specific members in the cluster
     */
    void sendMessage(ClusterMessage msg, List<ClusterMember> members) throws MessageFailedException;
}