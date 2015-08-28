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

import org.wso2.carbon.clustering.exception.MembershipFailedException;
import org.wso2.carbon.clustering.exception.MembershipInitializationException;
import org.wso2.carbon.internal.clustering.ClusterContext;

/**
 * A representation of a membership scheme such as "multicast based" or "well-known address (WKA)
 * based" schemes. This is directly related to the membership discovery mechanism.
 *
 * @since 5.0.0
 */
public interface MembershipScheme {

    /**
     * Initialize this membership scheme using the given cluster context instance
     *
     * @param clusterContext the cluster context to be used within the membership scheme
     * @throws MembershipInitializationException on error while initializing membership scheme
     * @see ClusterContext
     */
    void init(ClusterContext clusterContext) throws MembershipInitializationException;


    /**
     * JOIN the group
     *
     * @throws MembershipFailedException If an error occurs while joining the group
     */
    void joinGroup() throws MembershipFailedException;

}
