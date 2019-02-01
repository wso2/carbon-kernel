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

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Message sending and receiving in asynchronous mode.
 *
 * @param <T> ClusteringMessage which is being wrapped.
 */
public class AsyncClusteringMessage<T extends ClusteringMessage> extends ClusteringMessage {

    private static final transient Log log = LogFactory.getLog(AsyncClusteringMessage.class);
    private static final long serialVersionUID = 95L;
    private T wrappedMessage;
    private String clusterNodeId;

    public AsyncClusteringMessage(T wrappedMessage) {

        this.wrappedMessage = wrappedMessage;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {

        wrappedMessage.execute(configurationContext);
    }

    @Override
    public ClusteringCommand getResponse() {

        return wrappedMessage.getResponse();
    }

    public void setClusterNodeId(String clusterNodeId) {

        this.clusterNodeId = clusterNodeId;
    }

    public String getClusterNodeId() {

        return clusterNodeId;
    }
}
