/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.core.clustering.hazelcast;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Listener which should be used for non-guaranteed delivery mode.
 * e.g. Cache Invalidation Messages.
 */
public class HazelcastIdempotentClusterMessageListener implements MessageListener<IdempotentWrappedClusteringMessage> {

    private static final Log log = LogFactory.getLog(HazelcastIdempotentClusterMessageListener.class);
    private ConfigurationContext configurationContext;
    private String nodeId;

    public HazelcastIdempotentClusterMessageListener(ConfigurationContext configurationContext,
                                                     String nodeId) {

        this.configurationContext = configurationContext;
        this.nodeId = nodeId;
    }

    @Override
    public void onMessage(Message<IdempotentWrappedClusteringMessage> clusteringMessage) {

        try {
            ClusteringMessage msg = clusteringMessage.getMessageObject();
            if (msg instanceof IdempotentWrappedClusteringMessage) {
                IdempotentWrappedClusteringMessage idempotentWrappedClusteringMessage = (IdempotentWrappedClusteringMessage) msg;
                if (nodeId != null && !nodeId.equals(
                        idempotentWrappedClusteringMessage.getClusterNodeId())) { // Ignore own messages
                    if (log.isDebugEnabled()) {
                        log.debug("Received Cache invalidation message: " + msg);
                    }
                    msg.execute(configurationContext);
                }
            }
        } catch (ClusteringFault e) {
            log.error("Cannot process ClusteringMessage", e);
        }
    }
}
