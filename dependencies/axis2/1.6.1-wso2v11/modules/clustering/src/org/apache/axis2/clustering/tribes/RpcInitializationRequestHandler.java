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
import org.apache.axis2.clustering.control.GetConfigurationCommand;
import org.apache.axis2.clustering.control.GetConfigurationResponseCommand;
import org.apache.axis2.clustering.control.GetStateCommand;
import org.apache.axis2.clustering.control.GetStateResponseCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.group.RpcCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Handles RPC initialization requests from members
 */
public class RpcInitializationRequestHandler implements RpcCallback {

    private static Log log = LogFactory.getLog(RpcInitializationRequestHandler.class);
    private ConfigurationContext configurationContext;

    public RpcInitializationRequestHandler(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public Serializable replyRequest(Serializable msg, Member invoker) {
        if (log.isDebugEnabled()) {
            log.debug("Initialization request received by RpcInitializationRequestHandler");
        }
        if (msg instanceof GetStateCommand) {
            // If a GetStateRequest is received by a node which has not yet initialized
            // this node cannot send a response to the state requester. So we simply return.
            if (configurationContext.
                    getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null) {
                return null;
            }
            try {
                log.info("Received " + msg + " initialization request message from " +
                         TribesUtil.getName(invoker));
                GetStateCommand command = (GetStateCommand) msg;
                command.execute(configurationContext);
                GetStateResponseCommand getStateRespCmd = new GetStateResponseCommand();
                getStateRespCmd.setCommands(command.getCommands());
                return getStateRespCmd;
            } catch (ClusteringFault e) {
                String errMsg = "Cannot handle initialization request";
                log.error(errMsg, e);
                throw new RemoteProcessException(errMsg, e);
            }
        } else if (msg instanceof GetConfigurationCommand) {
            // If a GetConfigurationCommand is received by a node which has not yet initialized
            // this node cannot send a response to the state requester. So we simply return.
            if (configurationContext.
                    getPropertyNonReplicable(ClusteringConstants.CLUSTER_INITIALIZED) == null) {
                return null;
            }
            try {
                log.info("Received " + msg + " initialization request message from " +
                         TribesUtil.getName(invoker));
                GetConfigurationCommand command = (GetConfigurationCommand) msg;
                command.execute(configurationContext);
                GetConfigurationResponseCommand
                        getConfigRespCmd = new GetConfigurationResponseCommand();
                getConfigRespCmd.setServiceGroups(command.getServiceGroupNames());
                return getConfigRespCmd;
            } catch (ClusteringFault e) {
                String errMsg = "Cannot handle initialization request";
                log.error(errMsg, e);
                throw new RemoteProcessException(errMsg, e);
            }
        } 
        return null;
    }

    public void leftOver(Serializable msg, Member member) {
        //TODO: Method implementation

    }
}