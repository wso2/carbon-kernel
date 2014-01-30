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
package org.wso2.carbon.clustering.hazelcast.util;

import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterMember;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Properties;

/**
 * Utility methods for member manipulation
 */
public final class MemberUtils {
    private static Logger logger = LoggerFactory.getLogger(MemberUtils.class);
    //    private static Map<String, Parameter> parameters;
//    private static ConfigurationContext configurationContext;
    private static boolean isInitialized;

    public static void init(/*Map<String, Parameter> parameters, ConfigurationContext configurationContext*/) {
//        MemberUtils.parameters = parameters;
//        MemberUtils.configurationContext = configurationContext;
        isInitialized = true;
    }

    public static boolean canConnect(ClusterMember wkaMember) {
        logger.debug("Trying to connect to WKA member {} ...", wkaMember);
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
                                               int localMemberPort) {
        if (!isInitialized) {
            throw new IllegalStateException("MemberUtils not initialized. Call MemberUtils.init() first");
        }
        ClusterMember member =
                new ClusterMember(localMemberHost,
                                  localMemberPort);
        Properties memberInfo = new Properties();
//        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
//        TransportInDescription httpTransport = axisConfig.getTransportIn("http");
//        int portOffset = 0;
//        Parameter param = getParameter(ClusteringConstants.Parameters.AVOID_INITIATION);
//        if (param != null && !JavaUtils.isTrueExplicitly(param.getValue())) {
//            //AvoidInitialization = false, Hence we set the portOffset
//            if (System.getProperty("portOffset") != null) {
//                portOffset = Integer.parseInt(System.getProperty("portOffset"));
//            }
//        }
//
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
//        if (localMemberHost != null) {
//            memberInfo.setProperty("hostName", localMemberHost);
//        }
//
//        Parameter propsParam = getParameter("properties");
//        if (propsParam != null) {
//            OMElement paramEle = propsParam.getParameterElement();
//            for (Iterator iter = paramEle.getChildrenWithLocalName("property"); iter.hasNext(); ) {
//                OMElement propEle = (OMElement) iter.next();
//                OMAttribute nameAttrib = propEle.getAttribute(new QName("name"));
//                if (nameAttrib != null) {
//                    String attribName = nameAttrib.getAttributeValue();
//                    attribName = replaceProperty(attribName, memberInfo);
//
//                    OMAttribute valueAttrib = propEle.getAttribute(new QName("value"));
//                    if (valueAttrib != null) {
//                        String attribVal = valueAttrib.getAttributeValue();
//                        attribVal = replaceProperty(attribVal, memberInfo);
//                        memberInfo.setProperty(attribName, attribVal);
//                    }
//                }
//            }
//        }

        memberInfo.remove("hostName"); // this was needed only to populate other properties. No need to send it.
        member.setProperties(memberInfo);
        member.setDomain(domain);
        return member;
    }

//    private static Parameter getParameter(String name) {
//        return parameters.get(name);
//    }

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
