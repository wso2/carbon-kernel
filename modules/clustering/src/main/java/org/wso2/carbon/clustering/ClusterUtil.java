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
package org.wso2.carbon.clustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.config.ClusterConfiguration;
import org.wso2.carbon.clustering.config.MembershipSchemeConfiguration;
import org.wso2.carbon.clustering.config.membership.scheme.WKAMember;
import org.wso2.carbon.clustering.config.membership.scheme.WKASchemeConfig;
import org.wso2.carbon.clustering.exception.ClusterConfigurationException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Utility class for cluster module
 */
public class ClusterUtil {

    private static Logger logger = LoggerFactory.getLogger(ClusterUtil.class);


    /**
     * Returns the well known members defined in the cluster.xml
     *
     * @param clusterConfiguration the clusterConfiguration instance
     * @return List of well known members
     */
    public static List<ClusterMember> getWellKnownMembers(
            ClusterConfiguration clusterConfiguration) {
        List<ClusterMember> members = new ArrayList<>();

        List<WKAMember> wkaMembers = ((WKASchemeConfig)clusterConfiguration.
                getMembershipSchemeConfiguration().getMembershipScheme()).getWkaMembers();

        for (WKAMember wkaMember : wkaMembers) {
            String hostName = wkaMember.getHost();
            int port = wkaMember.getPort();

            if (hostName != null && port != 0) {
                members.add(new ClusterMember(replaceVariables(hostName),
                                              port));
            }
        }
        return members;

    }

    private static String replaceVariables(String text) {
        int indexOfStartingChars;
        int indexOfClosingBrace;

        // The following condition deals with properties.
        // Properties are specified as ${system.property},
        // and are assumed to be System properties
        if ((indexOfStartingChars = text.indexOf("${")) != -1 &&
            (indexOfClosingBrace = text.indexOf("}")) != -1) { // Is a property used?
            String var = text.substring(indexOfStartingChars + 2,
                                        indexOfClosingBrace);

            String propValue = System.getProperty(var);
            if (propValue == null) {
                propValue = System.getenv(var);
            }
            if (propValue != null) {
                text = text.substring(0, indexOfStartingChars) + propValue +
                       text.substring(indexOfClosingBrace + 1);
            }
        }
        return text;
    }

    public static String getIpAddress() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String address = "127.0.0.1";

        while (e.hasMoreElements()) {
            NetworkInterface netface = (NetworkInterface) e.nextElement();
            Enumeration addresses = netface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress ip = (InetAddress) addresses.nextElement();
                if (!ip.isLoopbackAddress() && isIP(ip.getHostAddress())) {
                    return ip.getHostAddress();
                }
            }
        }

        return address;
    }

    private static boolean isIP(String hostAddress) {
        return hostAddress.split("[.]").length == 4;
    }
    /**
     * Get the membership scheme applicable to this cluster
     *
     * @return The membership scheme. Only "wka" & "multicast" are valid return values.
     * @throws org.wso2.carbon.clustering.exception.ClusterConfigurationException
     *          If the membershipScheme specified in the cluster.xml file is invalid
     */
    public static String getMembershipScheme(ClusterConfiguration clusterConfiguration)
            throws ClusterConfigurationException {
        String mbrScheme = null;

        Object membershipScheme = clusterConfiguration.getMembershipSchemeConfiguration().
                getMembershipScheme();
        if (membershipScheme != null) {
            String membershipSchemeParam = membershipScheme.toString();
            mbrScheme = ClusteringConstants.MembershipScheme.MULTICAST_BASED;
            if (membershipSchemeParam != null) {
                mbrScheme = membershipSchemeParam.trim();
            }
            if (!mbrScheme.equals(ClusteringConstants.MembershipScheme.MULTICAST_BASED)
                && !mbrScheme.equals(ClusteringConstants.MembershipScheme.WKA_BASED)) {
                String msg = "Invalid membership scheme '" + mbrScheme +
                             "'. Supported schemes are " +
                             ClusteringConstants.MembershipScheme.MULTICAST_BASED +
                             ", " + ClusteringConstants.MembershipScheme.WKA_BASED;
                logger.error(msg);
                throw new ClusterConfigurationException(msg);
            }
        }

        return mbrScheme;
    }
}
