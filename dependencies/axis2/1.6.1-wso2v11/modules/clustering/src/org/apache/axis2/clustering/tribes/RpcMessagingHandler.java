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
package org.apache.axis2.clustering.tribes;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.tribes.Member;
import org.apache.catalina.tribes.RemoteProcessException;
import org.apache.catalina.tribes.group.RpcCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;

/**
 * Handles RPC messages from members
 */
public class RpcMessagingHandler implements RpcCallback {

    private static Log log = LogFactory.getLog(RpcMessagingHandler.class);

    private ConfigurationContext configurationContext;

    public RpcMessagingHandler(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    public Serializable replyRequest(Serializable msg, Member invoker) {
        if (log.isDebugEnabled()) {
            log.debug("RPC request received by RpcMessagingHandler");
        }
        if (msg instanceof ClusteringMessage) {
            ClusteringMessage clusteringMsg = (ClusteringMessage) msg;
            try {
                clusteringMsg.execute(configurationContext);
            } catch (ClusteringFault e) {
                String errMsg = "Cannot handle RPC message";
                log.error(errMsg, e);
                throw new RemoteProcessException(errMsg, e);
            }
            return clusteringMsg.getResponse();
        } else {
            throw new IllegalArgumentException("Invalid RPC message of type " + msg.getClass() +
                                               " received");
        }
    }

    public void leftOver(Serializable msg, Member member) {
        //TODO: Method implementation
    }
}
