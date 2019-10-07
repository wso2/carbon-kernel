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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Message sending and receiving in idempotent mode. Idempotent mode turns off replay and retry handling.
 *
 * @param <T> ClusteringMessage which is being wrapped.
 */
public class IdempotentWrappedClusteringMessage<T extends ClusteringMessage> extends ClusteringMessage implements Externalizable {

    private static final transient Log log = LogFactory.getLog(IdempotentWrappedClusteringMessage.class);
    private static final long serialVersionUID = 95L;
    private T wrappedMessage;
    private String clusterNodeId;

    private IdempotentWrappedClusteringMessage() {
    }

    public IdempotentWrappedClusteringMessage(T wrappedMessage) {

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

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(wrappedMessage);
        byte[] clusterNodeIdBytes = clusterNodeId.getBytes(StandardCharsets.ISO_8859_1);
        out.writeInt(clusterNodeIdBytes.length);
        out.write(clusterNodeIdBytes);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        wrappedMessage = (T) in.readObject();
        int clusterNodeIdSize = in.readInt();
        byte[] clusterNodeIdBytes = new byte[clusterNodeIdSize];
        in.read(clusterNodeIdBytes);
        clusterNodeId = new String(clusterNodeIdBytes, StandardCharsets.ISO_8859_1);
    }
}
