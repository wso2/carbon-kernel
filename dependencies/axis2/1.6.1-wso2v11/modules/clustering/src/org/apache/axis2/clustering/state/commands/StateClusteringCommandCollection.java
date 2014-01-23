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

package org.apache.axis2.clustering.state.commands;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.state.StateClusteringCommand;
import org.apache.axis2.context.ConfigurationContext;

import java.util.ArrayList;
import java.util.List;

/**
 *  A StateClusteringCommand consisting of a collection of other StateClusteringCommands
 */
public class StateClusteringCommandCollection extends StateClusteringCommand {

    private final List<StateClusteringCommand> commands;

    public StateClusteringCommandCollection(List<StateClusteringCommand> commands) {
        this.commands = commands;
    }

    public void execute(ConfigurationContext configContext) throws ClusteringFault {
        for (StateClusteringCommand command : commands) {
            command.execute(configContext);
        }
    }

    public boolean isEmpty(){
        return commands != null && commands.isEmpty();
    }

    public String toString() {
        return "StateClusteringCommandCollection";
    }
}
