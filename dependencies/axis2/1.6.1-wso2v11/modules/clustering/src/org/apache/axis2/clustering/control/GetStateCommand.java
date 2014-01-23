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

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.StateClusteringCommand;
import org.apache.axis2.clustering.state.StateClusteringCommandFactory;
import org.apache.axis2.clustering.state.StateManager;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class GetStateCommand extends ControlCommand {

    private StateClusteringCommand[] commands;

    public void execute(ConfigurationContext configCtx) throws ClusteringFault {
        ClusteringAgent clusteringAgent = configCtx.getAxisConfiguration().getClusteringAgent();
        if(clusteringAgent == null){
            return;
        }
        StateManager stateManager = clusteringAgent.getStateManager();
        if (stateManager != null) {
            Map excludedPropPatterns = stateManager.getReplicationExcludePatterns();
            List<StateClusteringCommand> cmdList = new ArrayList<StateClusteringCommand>();

            // Add the service group contexts, service contexts & their respective properties
            String[] sgCtxIDs = configCtx.getServiceGroupContextIDs();
            for (String sgCtxID : sgCtxIDs) {
                ServiceGroupContext sgCtx = configCtx.getServiceGroupContext(sgCtxID);
                StateClusteringCommand updateServiceGroupCtxCmd =
                        StateClusteringCommandFactory.getUpdateCommand(sgCtx,
                                                                         excludedPropPatterns,
                                                                         true);
                if (updateServiceGroupCtxCmd != null) {
                    cmdList.add(updateServiceGroupCtxCmd);
                }
                if (sgCtx.getServiceContexts() != null) {
                    for (Iterator iter2 = sgCtx.getServiceContexts(); iter2.hasNext();) {
                        ServiceContext serviceCtx = (ServiceContext) iter2.next();
                        StateClusteringCommand updateServiceCtxCmd =
                                StateClusteringCommandFactory.getUpdateCommand(serviceCtx,
                                                                                 excludedPropPatterns,
                                                                                 true);
                        if (updateServiceCtxCmd != null) {
                            cmdList.add(updateServiceCtxCmd);
                        }
                    }
                }
            }

            StateClusteringCommand updateCmd =
                    StateClusteringCommandFactory.getUpdateCommand(configCtx,
                                                                     excludedPropPatterns,
                                                                     true);
            if (updateCmd != null) {
                cmdList.add(updateCmd);
            }
            if (!cmdList.isEmpty()) {
                commands = cmdList.toArray(new StateClusteringCommand[cmdList.size()]);
            }
        }
    }

    public StateClusteringCommand[] getCommands() {
        return commands;
    }

    public String toString() {
        return "GetStateCommand";
    }
}
