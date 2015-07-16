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

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.clustering.ClusterMessage;
import org.wso2.carbon.clustering.exception.MessageFailedException;

import java.util.List;
import java.util.Map;

/**
 * The message listener using with hazelcast based distributed topics
 * for sending/receiving messages from/to cluster
 */
public class HazelcastClusterMessageListener implements MessageListener<ClusterMessage> {
    private static Logger logger = LoggerFactory.getLogger(HazelcastClusterMessageListener.class);
    private final Map<String, Long> recdMsgsBuffer;
    private final List<ClusterMessage> sentMsgsBuffer;

    public HazelcastClusterMessageListener(final Map<String, Long> recdMsgsBuffer,
                                           final List<ClusterMessage> sentMsgsBuffer) {
        this.recdMsgsBuffer = recdMsgsBuffer;
        this.sentMsgsBuffer = sentMsgsBuffer;
    }

    @Override
    public void onMessage(Message<ClusterMessage> clusteringMessage) {
        try {
            ClusterMessage msg = clusteringMessage.getMessageObject();
            if (!sentMsgsBuffer.contains(msg)) { // Ignore own messages
                logger.info("Received ClusteringMessage: " + msg);
                msg.execute();
                recdMsgsBuffer.put(msg.getUuid(), System.currentTimeMillis());
            }
        } catch (MessageFailedException e) {
            logger.error("Cannot process ClusteringMessage", e);
        }
    }
}
