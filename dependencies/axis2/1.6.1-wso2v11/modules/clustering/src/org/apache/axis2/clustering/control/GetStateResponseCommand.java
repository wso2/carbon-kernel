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

package org.apache.axis2.clustering.control;

import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.StateClusteringCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 */
public class GetStateResponseCommand extends ControlCommand {

    private static final Log log = LogFactory.getLog(GetStateResponseCommand.class);

    private StateClusteringCommand[] commands;

    public void execute(ConfigurationContext configContext) throws ClusteringFault {
        log.info("Received state initialization message");
        
        // Run this code only if this node is not already initialized
        if (configContext.
                getPropertyNonReplicable(ClusteringConstants.RECD_STATE_INIT_MSG) == null) {
            configContext.
                setNonReplicableProperty(ClusteringConstants.RECD_STATE_INIT_MSG, "true");
//            log.info("Received state initialization message");
            if (commands != null) {
                for (int i = 0; i < commands.length; i++) {
                    commands[i].execute(configContext);
                }
            }
        }
    }

    public void setCommands(StateClusteringCommand[] commands) {
        this.commands = commands;
    }

    public String toString() {
        return "GetStateResponseCommand";
    }
}
