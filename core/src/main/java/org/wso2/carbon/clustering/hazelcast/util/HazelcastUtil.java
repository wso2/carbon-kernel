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
package org.wso2.carbon.clustering.hazelcast.util;

import com.hazelcast.core.Member;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.config.LocalMemberProperty;
import org.wso2.carbon.clustering.exception.MessageFailedException;
import org.wso2.carbon.internal.DataHolder;
import org.wso2.carbon.internal.clustering.CarbonCluster;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Utility class for hazezcast based clustering implementation
 */
public class HazelcastUtil {
    public static ClusterMember toClusterMember(Member hazelcastMember) {
        InetSocketAddress inetSocketAddress = hazelcastMember.getInetSocketAddress();
        ClusterMember clusterMember = new ClusterMember(inetSocketAddress.getHostName(),
                                                        inetSocketAddress.getPort());
        clusterMember.setId(hazelcastMember.getUuid());
        return clusterMember;
    }

    /**
     * Replay messages to a newly joining member
     */
    public static void sendMessagesToMember(List<ClusterMessage> messageBuffer,
                                            Member member) throws MessageFailedException {
        CarbonCluster carbonCluster = DataHolder.getInstance().getCarbonCluster();
        for (ClusterMessage clusterMessage : messageBuffer) {
            ArrayList<ClusterMember> members = new ArrayList<>();
            members.add(HazelcastUtil.toClusterMember(member));
            carbonCluster.sendMessage(clusterMessage, members);
        }
    }

    /**
     * This method will read properties specified in cluster.xml and load it to the given
     * properties instance
     *
     * @param clusterConfiguration the cluster configuration to read the properties
     * @param properties           the properties instance to load the read properties
     */
    public static void loadPropertiesFromConfig(ClusterConfiguration clusterConfiguration,
                                                Properties properties) {

        List<LocalMemberProperty> localMemberProperties = clusterConfiguration.
                getLocalMemberConfiguration().getProperties();

        for (LocalMemberProperty localMemberProperty : localMemberProperties) {
            String attributeName = localMemberProperty.getName();
            String attributeValue = localMemberProperty.getValue();
            properties.setProperty(attributeName, attributeValue);
        }
    }

    /**
     * This will lookup specific property from cluster.xml by using the given name
     *
     * @param clusterConfiguration cluster configuration to lookup property
     * @param propertyName         the property name to lookup
     * @return the value of the looked up property
     */
    public static String lookupHazelcastProperty(ClusterConfiguration clusterConfiguration,
                                                 String propertyName) {
        String propertyValue = null;
        List<LocalMemberProperty> localMemberProperties = clusterConfiguration.
                getLocalMemberConfiguration().getProperties();

        for (LocalMemberProperty localMemberProperty : localMemberProperties) {
            if (propertyName.equals(localMemberProperty.getName())) {
                propertyValue = localMemberProperty.getValue();
                break;
            }
        }
        return propertyValue;
    }
}
