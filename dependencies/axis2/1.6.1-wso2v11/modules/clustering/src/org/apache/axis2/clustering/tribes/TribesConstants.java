/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.apache.axis2.clustering.tribes;

/**
 * This class holds the configuration parameters which are specific to Tribes
 */
public final class TribesConstants {

    /**
     * The ID of the RPC initialization message channel
     */
    public static final String RPC_INIT_CHANNEL = "rpc.init.channel";

    /**
     * The ID of the RPC messaging channel
     */
    public static final String RPC_MESSAGING_CHANNEL = "rpc.msg.channel";

    /**
     * The ID of the RPC membership message channel. This channel is only used when WKA
     * membership discovery mechanism is used
     */
    public static final String RPC_MEMBERSHIP_CHANNEL = "rpc.membership.channel";

    // Message sending and receiving options
    public static final int MSG_ORDER_OPTION = 512;

    // Option that indicates that a message is related to membership
    public static final int MEMBERSHIP_MSG_OPTION = 1024;

    // Option that indicates that a message should be processed at-most once
    public static final int AT_MOST_ONCE_OPTION = 2048;

    public static final byte[] RPC_CHANNEL_ID = "axis2.rpc.channel".getBytes();

    public static final String LOCAL_MEMBER_HOST = "localMemberHost";
    public static final String LOCAL_MEMBER_BIND_ADDRESS = "localMemberBindAddress";
    public static final String LOCAL_MEMBER_PORT = "localMemberPort";
    public static final String LOCAL_MEMBER_BIND_PORT = "localMemberBindPort";

    public static final String MCAST_ADDRESS = "mcastAddress";
    public static final String MCAST_BIND_ADDRESS = "multicastBindAddress";
    public static final String MCAST_PORT = "mcastPort";
    public static final String MCAST_FREQUENCY = "mcastFrequency";
    public static final String MEMBER_DROP_TIME = "memberDropTime";
    public static final String MCAST_CLUSTER_DOMAIN = "mcastClusterDomain";
    public static final String TCP_LISTEN_HOST = "tcpListenHost";
    public static final String BIND_ADDRESS = "bindAddress";
    public static final String TCP_LISTEN_PORT = "tcpListenPort";
    public static final String MAX_RETRIES = "maxRetries";
}
