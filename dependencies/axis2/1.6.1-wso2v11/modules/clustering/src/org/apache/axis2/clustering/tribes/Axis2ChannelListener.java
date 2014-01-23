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

package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.management.DefaultNodeManager;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.clustering.management.NodeManagementCommand;
import org.apache.axis2.clustering.state.DefaultStateManager;
import org.apache.axis2.clustering.state.StateClusteringCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.ByteMessage;
import org.apache.catalina.tribes.ChannelListener;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.group.RpcMessage;
import org.apache.catalina.tribes.io.XByteBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * This is the Tribes channel listener which is used for listening on the channels, receiving
 * messages & accepting messages.
 */
public class Axis2ChannelListener implements ChannelListener {
    private static final Log log = LogFactory.getLog(Axis2ChannelListener.class);

    private DefaultStateManager stateManager;
    private DefaultNodeManager nodeManager;

    private ConfigurationContext configurationContext;

    public Axis2ChannelListener(ConfigurationContext configurationContext,
                                DefaultNodeManager nodeManager,
                                DefaultStateManager stateManager) {
        this.nodeManager = nodeManager;
        this.stateManager = stateManager;
        this.configurationContext = configurationContext;
    }

    public void setStateManager(DefaultStateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void setNodeManager(DefaultNodeManager nodeManager) {
        this.nodeManager = nodeManager;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Invoked by the channel to determine if the listener will process this message or not.
     * @param msg Serializable
     * @param sender Member
     * @return boolean
     */
    public boolean accept(Serializable msg, Member sender) {
        return !(msg instanceof RpcMessage);  // RpcMessages  will not be handled by this listener
    }

    /**
     * Receive a message from the channel
     * @param msg Serializable
     * @param sender - the source of the message
     */
    public void messageReceived(Serializable msg, Member sender) {
        try {
            byte[] message = ((ByteMessage) msg).getMessage();
            msg = XByteBuffer.deserialize(message,
                                          0,
                                          message.length,
                                          ClassLoaderUtil.getClassLoaders());
        } catch (Exception e) {
            String errMsg = "Cannot deserialize received message";
            log.error(errMsg, e);
            throw new RemoteProcessException(errMsg, e);
        }

        // If the system has not still been intialized, reject all incoming messages, except the
        // GetStateResponseCommand message
        if (configurationContext.
                getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null) {
            log.warn("Received message " + msg +
                     " before cluster initialization has been completed from " +
                     TribesUtil.getName(sender));
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Received message " + msg + " from " + TribesUtil.getName(sender));
        }

        try {
            processMessage(msg);
        } catch (Exception e) {
            String errMsg = "Cannot process received message";
            log.error(errMsg, e);
            throw new RemoteProcessException(errMsg, e);
        }
    }

    private void processMessage(Serializable msg) throws ClusteringFault {
        if (msg instanceof StateClusteringCommand && stateManager != null) {
            StateClusteringCommand ctxCmd = (StateClusteringCommand) msg;
            ctxCmd.execute(configurationContext);
        } else if (msg instanceof NodeManagementCommand && nodeManager != null) {
            ((NodeManagementCommand) msg).execute(configurationContext);
        } else if (msg instanceof GroupManagementCommand){
            ((GroupManagementCommand) msg).execute(configurationContext);
        }
    }
}
