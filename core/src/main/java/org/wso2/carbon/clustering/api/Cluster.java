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

package org.wso2.carbon.clustering.api;

import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.exception.MessageFailedException;

import java.util.List;

/**
 * The Cluster API which is given an OSGi service to carbon platform. Users can lookup this and
 * consume those services such as sending cluster message, getting the list of cluster members,
 * etc.
 *
 * @since 5.0.0
 */

public interface Cluster {
    /**
     * Send the given cluster message to the whole cluster
     *
     * @param clusterMessage the cluster message to be sent
     * @throws MessageFailedException on error
     * @see ClusterMessage
     */
    void sendMessage(ClusterMessage clusterMessage) throws MessageFailedException;

    /**
     * Send the given cluster message to a set of members in the cluster
     *
     * @param clusterMessage the cluster message to be sent
     * @param members        the list of members to send the cluster message
     * @throws MessageFailedException on error
     * @see ClusterMessage
     * @see ClusterMember
     */
    void sendMessage(ClusterMessage clusterMessage, List<ClusterMember> members)
            throws MessageFailedException;

    /**
     * Return the list of currently available members in the cluster
     *
     * @return the member list
     * @see ClusterMember
     */
    List<ClusterMember> getMembers();
}
