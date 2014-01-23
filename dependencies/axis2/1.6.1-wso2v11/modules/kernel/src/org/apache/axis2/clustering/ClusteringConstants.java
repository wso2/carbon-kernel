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


package org.apache.axis2.clustering;

/**
 * All constants used by the Axis2 clustering implementation
 */
public final class ClusteringConstants {

    private ClusteringConstants() {
    }

    /**
     * The default domain to which this member belongs to. This node may be running in application
     * or loadBalance mode
     */
    public static final String DEFAULT_DOMAIN = "apache.axis2.domain";

    public static final String NODE_MANAGER_SERVICE = "Axis2NodeManager";
    public static final String REQUEST_BLOCKING_HANDLER = "RequestBlockingHandler";
    public static final String CLUSTER_INITIALIZED = "local_cluster.initialized";
    public static final String RECD_CONFIG_INIT_MSG = "local_recd.config.init.method";
    public static final String RECD_STATE_INIT_MSG = "local_recd.state.init.method";
    public static final String BLOCK_ALL_REQUESTS = "local_wso2wsas.block.requests";
    public static final String LOCAL_IP_ADDRESS = "axis2.local.ip.address";

    /**
     * The main cluster configuration parameters
     */
    public static final class Parameters {

        /**
         * The membership scheme used in this setup. The only values supported at the moment are
         * "multicast" and "wka"
         */
        public static final String MEMBERSHIP_SCHEME = "membershipScheme";

        /**
         * The clustering domain/group. Nodes in the same group will belong to the same multicast
         * domain. There will not be interference between nodes in different groups.
         */
        public static final String DOMAIN = "domain";

        /**
         * Indicates the mode in which this member is running. Valid values are "application" and
         * "loadBalance"
         * <p/>
         * application - This member hosts end user applications
         * loadBalance - This member is a part of the load balancer cluster
         */
        public static final String MODE = "mode";

        /**
         * This is the even handler which will be notified in the case of load balancing events occurring.
         * This class has to be an implementation of org.apache.axis2.clustering.LoadBalanceEventHandler
         * <p/>
         * This entry is only valid if the "mode" parameter is set to loadBalance
         */
        public static final String LOAD_BALANCE_EVENT_HANDLER = "loadBalanceEventHandler";

        /**
         * This parameter is only valid when the "mode" parameter is set to "application"
         * <p/>
         * This indicates the domain in which the the applications being load balanced are deployed.
         */
        public static final String APPLICATION_DOMAIN = "applicationDomain";

        /**
         * When a Web service request is received, and processed, before the response is sent to the
         * client, should we update the states of all members in the cluster? If the value of
         * this parameter is set to "true", the response to the client will be sent only after
         * all the members have been updated. Obviously, this can be time consuming. In some cases,
         * such this overhead may not be acceptable, in which case the value of this parameter
         * should be set to "false"
         */
        public static final String SYNCHRONIZE_ALL_MEMBERS = "synchronizeAll";

        /**
         * Do not automatically initialize the cluster. The programmer has to explicitly initialize
         * the cluster.
         */
        public static final String AVOID_INITIATION = "AvoidInitiation";

        /**
         * Preserve message ordering. This will be done according to sender order
         */
        public static final String PRESERVE_MSG_ORDER = "preserveMessageOrder";

        /**
         * Maintain atmost-once message processing semantics
         */
        public static final String ATMOST_ONCE_MSG_SEMANTICS = "atmostOnceMessageSemantics";

        /**
         * Indicates whether this member is ACTIVE or PASSIVE
         */
        public static final String IS_ACTIVE = "isActive";

        /**
         * The implementaion of
         */
        public static final String MEMBERSHIP_LISTENER = "membershipListener";
    }

    public static final class MembershipScheme {
        /**
         * Multicast based membership discovery scheme
         */
        public static final String MULTICAST_BASED = "multicast";

        /**
         * Well-Known Address based membership management scheme
         */
        public static final String WKA_BASED = "wka";
    }
}
