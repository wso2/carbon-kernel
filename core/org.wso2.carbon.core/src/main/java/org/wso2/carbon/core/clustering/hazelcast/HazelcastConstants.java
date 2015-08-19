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

/**
 * Constants used in Hazelcast based clustering implementation
 */
public final class HazelcastConstants {

    public static final String LOCAL_MEMBER_PORT = "localMemberPort";
    public static final String LOCAL_MEMBER_HOST = "localMemberHost";
    public static final String MGT_CENTER_URL = "mgtCenterURL";
    public static final String LICENSE_KEY = "licenseKey";
    public static final String CLUSTERING_MESSAGE_TOPIC = "$clustering.message.topic";
    public static final String GROUP_MGT_CMD_TOPIC = ".group.mgt.cmd.topic";
    public static final String CONTROL_COMMAND_TOPIC = "$control.$command.$topic";
    public static final String GROUP_PASSWORD = "groupPassword";

    public static final String REPLAY_MESSAGE_QUEUE = "$ReplayMessageQueue:";

    public static final String AWS_MEMBERSHIP_SCHEME = "aws";
    public static final String MULTICAST_MEMBERSHIP_SCHEME = "multicast";
    public static final String WKA_MEMBERSHIP_SCHEME = "wka";

    public static final String CLUSTER_COORDINATOR_LOCK = "$coordinator#@lock";
    public static final String MEMBERSHIP_SCHEME_NAME = "membership.scheme.name";

    private HazelcastConstants() {
    }
}
