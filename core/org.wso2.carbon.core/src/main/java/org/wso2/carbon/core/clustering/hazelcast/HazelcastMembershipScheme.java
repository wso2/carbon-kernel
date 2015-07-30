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
package org.wso2.carbon.core.clustering.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.clustering.MembershipScheme;
import org.apache.axis2.description.Parameter;

import java.util.List;
import java.util.Map;

/**
 * MembershipScheme which adds some methods for the Hazelcast based clustering implementation
 */
public interface HazelcastMembershipScheme extends MembershipScheme {

    void init(Map<String, Parameter> parameters,
              String primaryDomain,
              List<org.apache.axis2.clustering.Member> wkaMembers,
              Config primaryHazelcastConfig,
              HazelcastInstance primaryHazelcastInstance,
              List<ClusteringMessage> messageBuffer) throws ClusteringFault;

    void setPrimaryHazelcastInstance(HazelcastInstance primaryHazelcastInstance);

    void setLocalMember(Member localMember);

    void setCarbonCluster(HazelcastCarbonClusterImpl hazelcastCarbonCluster);
}
