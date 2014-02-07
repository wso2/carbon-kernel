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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.clustering.ClusterConfiguration;
import org.wso2.carbon.clustering.ClusterContext;
import org.wso2.carbon.clustering.ClusterMember;
import org.wso2.carbon.clustering.hazelcast.wka.WKAConstants;
import org.wso2.carbon.clustering.internal.DataHolder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.List;
import java.util.Properties;

/**
 * Utility methods for member manipulation
 */
public final class MemberUtils {
    private static Logger logger = LoggerFactory.getLogger(MemberUtils.class);


    public static boolean canConnect(ClusterMember wkaMember) {
        logger.debug("Trying to connect to WKA member {} ...", wkaMember.getInetSocketAddress());
        try {
            InetAddress addr = InetAddress.getByName(wkaMember.getHostName());
            SocketAddress sockAddr = new InetSocketAddress(addr, wkaMember.getPort());
            new Socket().connect(sockAddr, 10000);
            return true;
        } catch (IOException e) {
            logger.error("Error occurred while trying connect", e);
            String msg = e.getMessage();
            if (!msg.contains("Connection refused") && !msg.contains("connect timed out")) {
                logger.error("Cannot connect to WKA member " + wkaMember, e);
            }
        }
        return false;
    }

    public static void addMember(ClusterMember member,
                                 TcpIpConfig config) {
        String memberStr = member.getHostName() + ":" + member.getPort();
        if (!config.getMembers().contains(memberStr)) {
            config.addMember(memberStr);
            logger.info("Added member: " + member);
        }
    }

    public static ClusterMember getLocalMember(String domain,
                                               String localMemberHost,
                                               int localMemberPort,
                                               ClusterContext clusterContext) {
        ClusterMember member =  new ClusterMember(localMemberHost, localMemberPort);
        Properties memberInfo = new Properties();

//      TODO : Since we are not depending on confContext, How to get http/s ports?
//        int portOffset = 0;
//            if (System.getElement("portOffset") != null) {
//                portOffset = Integer.parseInt(System.getElement("portOffset"));
//        }
//        if (httpTransport != null) {
//            Parameter port = httpTransport.getParameter("port");
//            if (port != null) {
//                int httpPort = Integer.valueOf((String) port.getValue()) + portOffset;
//                member.setHttpPort(httpPort);
//
//            }
//        }
//        TransportInDescription httpsTransport = axisConfig.getTransportIn("https");
//        if (httpsTransport != null) {
//            Parameter port = httpsTransport.getParameter("port");
//            if (port != null) {
//                int httpsPort = Integer.valueOf((String) port.getValue()) + portOffset;
//                member.setHttpsPort(httpsPort);
//            }
//        }
//        Parameter isActiveParam = getParameter(ClusteringConstants.Parameters.IS_ACTIVE);
//        if (isActiveParam != null) {
//            memberInfo.setProperty(ClusteringConstants.Parameters.IS_ACTIVE,
//                                   (String) isActiveParam.getValue());
//        }
//
        if (localMemberHost != null) {
            memberInfo.setProperty("hostName", localMemberHost);
        }
        ClusterConfiguration clusterConfiguration = clusterContext.getClusterConfiguration();
        List<Object> propsParam = clusterConfiguration.getElement("properties");
        if (propsParam != null) {
            for (Object property : propsParam) {
                if (property instanceof Node) {
                    NodeList nodeList = ((Node) property).getChildNodes();
                    for (int count = 0; count < nodeList.getLength(); count++) {
                        Node node = nodeList.item(count);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            String attributeName = node.getNodeName();
                            if (attributeName != null) {
                                attributeName = replaceProperty(attributeName, memberInfo);
                            }
                            String attributeValue = node.getTextContent();
                            if (attributeValue != null) {
                                attributeValue = replaceProperty(attributeValue, memberInfo);
                                memberInfo.setProperty(attributeName, attributeValue);
                            }
                        }
                    }
                }
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

    public static IMap<String, ClusterMember> getMembersMap(HazelcastInstance hazelcastInstance,
                                                            String domain) {
        return hazelcastInstance.getMap("$" + domain + ".members");
    }

    private MemberUtils() {
    }
}
