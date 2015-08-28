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

import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.config.LocalMemberProperty;
import org.wso2.carbon.internal.clustering.ClusterUtil;

import java.net.SocketException;
import java.util.List;
import java.util.Properties;

/**
 * Utility methods for member manipulation
 */
public final class MemberUtils {
    private static Logger logger = LoggerFactory.getLogger(MemberUtils.class);

    private MemberUtils() {
    }

    /**
     * Adds a give cluster member to the hazelcast TCP config instance
     *
     * @param member the cluster member to add
     * @param config the tcp config instance to use when adding the member
     */
    public static void addMember(ClusterMember member,
                                 TcpIpConfig config) {
        String localMemberHost = member.getHostName();
        if (localMemberHost != null && !localMemberHost.equalsIgnoreCase("127.0.0.1") &&
                !localMemberHost.equalsIgnoreCase("localhost")) {
            localMemberHost = localMemberHost.trim();
        } else {
            try {
                localMemberHost = ClusterUtil.getIpAddress();
            } catch (SocketException e) {
                String msg = "Could not set local member host";
                logger.error(msg, e);
            }
        }

        String memberStr = localMemberHost + ":" + member.getPort();
        if (!config.getMembers().contains(memberStr)) {
            config.addMember(memberStr);
            logger.info("Added member: " + member);
        }
    }

    /**
     * This will return the local member with all the local member properties populated
     *
     * @param domain               the cluster domain
     * @param localMemberHost      local member host
     * @param localMemberPort      local member port
     * @param clusterConfiguration the cluster configuration
     * @return the populated local member object
     */
    public static ClusterMember getLocalMember(String domain,
                                               String localMemberHost,
                                               int localMemberPort,
                                               ClusterConfiguration clusterConfiguration) {
        ClusterMember member = new ClusterMember(localMemberHost, localMemberPort);
        Properties memberInfo = new Properties();

//      TODO : Since we are not depending on confContext, How to get http/s ports?

        if (localMemberHost != null) {
            memberInfo.setProperty("hostName", localMemberHost);
        }

        List<LocalMemberProperty> localMemberProperties = clusterConfiguration.
                getLocalMemberConfiguration().getProperties();

        for (LocalMemberProperty localMemberProperty : localMemberProperties) {
            String attributeName = localMemberProperty.getName();
            if (attributeName != null) {
                attributeName = replaceProperty(attributeName, memberInfo);
            }

            String attributeValue = localMemberProperty.getValue();
            if (attributeValue != null) {
                attributeValue = replaceProperty(attributeValue, memberInfo);
                memberInfo.setProperty(attributeName, attributeValue);
            }
        }

        memberInfo.remove("hostName"); // this was needed only to populate other properties. No need to send it.
        member.setProperties(memberInfo);
        member.setDomain(domain);
        return member;
    }

    private static String replaceProperty(String text, Properties props) {
        int indexOfStartingChars = -1;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        while (indexOfStartingChars < text.indexOf("${") &&
                (indexOfStartingChars = text.indexOf("${")) != -1 &&
                (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String sysProp = text.substring(indexOfStartingChars + 2,
                    indexOfClosingBrace);
            String propValue = props.getProperty(sysProp);
            if (propValue == null) {
                propValue = System.getProperty(sysProp);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                        text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }

    /**
     * Returns the the distributed map associated with the cluster domain
     *
     * @param hazelcastInstance the hazelcastInstance to get the map
     * @param domain            the cluster domain
     * @return the map associated with the cluster domain
     */
    public static IMap<String, ClusterMember> getMembersMap(HazelcastInstance hazelcastInstance,
                                                            String domain) {
        return hazelcastInstance.getMap("$" + domain + ".members");
    }
}
