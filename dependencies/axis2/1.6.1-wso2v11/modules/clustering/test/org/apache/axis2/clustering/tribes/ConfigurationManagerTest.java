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

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.management.DefaultNodeManager;
import org.apache.axis2.clustering.state.DefaultStateManager;
import org.apache.axis2.context.ConfigurationContext;

public class ConfigurationManagerTest extends
                                      org.apache.axis2.clustering.management.ConfigurationManagerTestCase {

    protected ClusteringAgent getClusterManager(ConfigurationContext configCtx) {
        TribesClusteringAgent tribesClusterManager = new TribesClusteringAgent();
        tribesClusterManager.setConfigurationContext(configCtx);
        DefaultNodeManager configurationManager = new DefaultNodeManager();
        tribesClusterManager.setNodeManager(configurationManager);
        DefaultStateManager contextManager = new DefaultStateManager();
        tribesClusterManager.setStateManager(contextManager);
        return tribesClusterManager;
    }

}
