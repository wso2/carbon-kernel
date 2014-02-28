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
package org.wso2.carbon.clustering.hazelcast;

/**
 * Constants used in Hazelcast based clustering implementation
 */
public final class HazelcastConstants {

    public static final String CLUSTERING_MESSAGE_TOPIC = "$clustering.message.topic";
    public static final String CONTROL_COMMAND_TOPIC = "$control.$command.$topic";

    public static final String INSTANCE_NAME = "hazelcast.instance.name";
    public static final String MAX_NO_HEARTBEAT_SECONDS = "hazelcast.max.no.heartbeat.seconds";
    public static final String MAX_NO_MASTER_CONFIRMATION_SECONDS =
            "hazelcast.max.no.master.confirmation.seconds";
    public static final String MERGE_FIRST_RUN_DELAY_SECONDS =
            "hazelcast.merge.first.run.delay.seconds";
    public static final String MERGE_NEXT_RUN_DELAY_SECONDS =
            "hazelcast.merge.next.run.delay.secondss";

    public static final String REPLAY_MESSAGE_QUEUE = "$ReplayMessageQueue:";

    private HazelcastConstants() {
    }

}
