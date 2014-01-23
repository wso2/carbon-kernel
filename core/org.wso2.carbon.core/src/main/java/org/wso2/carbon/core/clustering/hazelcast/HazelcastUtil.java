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

import com.hazelcast.core.Member;
import org.apache.axis2.clustering.ClusteringMessage;
import org.wso2.carbon.core.clustering.api.CarbonCluster;
import org.wso2.carbon.core.clustering.api.ClusterMember;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: class description
 */
public class HazelcastUtil {
    public static ClusterMember toClusterMember(Member hazelcastMember) {
        return new ClusterMember(hazelcastMember.getUuid(), hazelcastMember.getInetSocketAddress());
    }

    /**
     * Replay messages to a newly joining member
     */
    public static void sendMessagesToMember(List<ClusteringMessage> messageBuffer,
                                            Member member,
                                            CarbonCluster carbonCluster){
        for (ClusteringMessage clusteringMessage : messageBuffer) {
            ArrayList<ClusterMember> members = new ArrayList<ClusterMember>();
            members.add(HazelcastUtil.toClusterMember(member));
            carbonCluster.sendMessage(new WrapperClusterMessage(clusteringMessage), members);
        }
    }
}
