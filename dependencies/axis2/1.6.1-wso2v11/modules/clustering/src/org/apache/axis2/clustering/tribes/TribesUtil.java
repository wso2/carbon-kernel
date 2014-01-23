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

package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.Utils;
import org.apache.catalina.tribes.Channel;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.util.Arrays;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Properties;

public class TribesUtil {

    private static Log log = LogFactory.getLog(TribesUtil.class);

    public static void printMembers(MembershipManager membershipManager) {
        Member[] members = membershipManager.getMembers();
        if (members != null) {
            int length = members.length;
            if (length > 0) {
                log.info("Members of current cluster");
                for (int i = 0; i < length; i++) {
                    log.info("Member" + (i + 1) + " " + getName(members[i]));
                }
            } else {
                log.info("No members in current cluster");
            }
        }
    }

    public static String getName(Member member) {
        return getHost(member) + ":" + member.getPort() + "(" + new String(member.getDomain()) + ")";
    }

    public static String getHost(Member member) {
        byte[] hostBytes = member.getHost();
        StringBuffer host = new StringBuffer();
        if (hostBytes != null) {
            for (int i = 0; i < hostBytes.length; i++) {
                int hostByte = hostBytes[i] >= 0 ? (int) hostBytes[i] : (int) hostBytes[i] + 256;
                host.append(hostByte);
                if (i < hostBytes.length - 1) {
                    host.append(".");
                }
            }
        }
        return host.toString();
    }

    public static String getLocalHost(Channel channel) {
        return getName(channel.getLocalMember(true));
    }

    public static String getLocalHost(Parameter tcpListenHost){
        String host = null;
        if (tcpListenHost != null) {
            host = ((String) tcpListenHost.getValue()).trim();
        } else {
            try {
                host = Utils.getIpAddress();
            } catch (SocketException e) {
                String msg = "Could not get local IP address";
                log.error(msg, e);
            }
        }
        if (System.getProperty(ClusteringConstants.LOCAL_IP_ADDRESS) != null) {
            host = System.getProperty(ClusteringConstants.LOCAL_IP_ADDRESS);
        }
        return host;
    }

    public static byte[] getRpcMembershipChannelId(byte[] domain) {
        return (new String(domain) + ":" + TribesConstants.RPC_MEMBERSHIP_CHANNEL).getBytes();
    }

    public static byte[] getRpcInitChannelId(byte[] domain) {
        return (new String(domain) + ":" + TribesConstants.RPC_INIT_CHANNEL).getBytes();
    }

    public static byte[] getRpcMessagingChannelId(byte[] domain) {
        return (new String(domain) + ":" + TribesConstants.RPC_MESSAGING_CHANNEL).getBytes();
    }

    public static boolean isInDomain(Member member, byte[] domain) {
        return Arrays.equals(domain, member.getDomain());
    }

    public static boolean areInSameDomain(Member member1, Member member2) {
        return Arrays.equals(member1.getDomain(), member2.getDomain());
    }

    public static org.apache.axis2.clustering.Member toAxis2Member(Member member) {
        org.apache.axis2.clustering.Member axis2Member =
                new org.apache.axis2.clustering.Member(TribesUtil.getHost(member),
                                                       member.getPort());
        Properties props = getProperties(member.getPayload());

        String httpPort = props.getProperty("httpPort");
        if (httpPort != null && httpPort.trim().length() != 0) {
            axis2Member.setHttpPort(Integer.parseInt(httpPort));
        }

        String httpsPort = props.getProperty("httpsPort");
        if (httpsPort != null && httpsPort.trim().length() != 0) {
            axis2Member.setHttpsPort(Integer.parseInt(httpsPort));
        }

        String isActive = props.getProperty(ClusteringConstants.Parameters.IS_ACTIVE);
        if (isActive != null && isActive.trim().length() != 0) {
            axis2Member.setActive(Boolean.valueOf(isActive));
        }
        
        String remoteHost = props.getProperty("remoteHost");
        if (remoteHost != null && remoteHost.trim().length() != 0) {
            axis2Member.setRemoteHost(remoteHost);
        }

        axis2Member.setDomain(new String(member.getDomain()));
        axis2Member.setProperties(props);
        return axis2Member;
    }

    private static Properties getProperties(byte[] payload) {
        Properties props = null;
        try {
            ByteArrayInputStream bin = new ByteArrayInputStream(payload);
            props = new Properties();
            props.load(bin);
        } catch (IOException ignored) {
            // This error will never occur
        }
        return props;
    }
}
