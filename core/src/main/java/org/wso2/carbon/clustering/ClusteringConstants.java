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

import sun.net.www.content.text.Generic;

/**
 * All constants used by the carbon clustering implementation
 */
public final class ClusteringConstants {

    private ClusteringConstants() {
    }

    public static final String CLUSTER_AGENT = "Agent";

    /**
     * The default domain to which this member belongs to. This node may be running in application
     * or loadBalance mode
     */
    public static final String DEFAULT_DOMAIN = "wso2.carbon.domain";

    public static final String LOCAL_MEMBER_HOST = "LocalMember.Host";

    public static final String LOCAL_MEMBER_PORT = "LocalMember.Port";

    /**
     * The membership scheme used in this setup. The only values supported at the moment are
     * "multicast" and "wka"
     */
    public static final String MEMBERSHIP_SCHEME = "MembershipScheme";

    /**
     * The clustering domain/group. Nodes in the same group will belong to the same multicast
     * domain. There will not be interference between nodes in different groups.
     */
    public static final String DOMAIN = "Domain";


    public static final class MembershipScheme {
        /**
         * Multicast based membership discovery scheme
         */
        public static final String MULTICAST_BASED = "Multicast";

        /**
         * Well-Known Address based membership management scheme
         */
        public static final String WKA_BASED = "WKA";

        /**
         * Amazon AWS based membership scheme
         */
        public static final String AWS_BASED = "AWS";

        /**
         * Generic membership scheme
         */
        public static final String GENERIC = "Generic";
    }
}
