/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.core.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;

/**
 *
 */
public class ClusteringUtil {
    private static boolean isClusteringAgentInitialized;

    public static void enableClustering(ConfigurationContext configContext) throws AxisFault {
        AxisConfiguration axisConfig = configContext.getAxisConfiguration();
        ClusteringAgent clusteringAgent = axisConfig.getClusteringAgent();
        if (clusteringAgent != null) {
            clusteringAgent.setConfigurationContext(configContext);

            if (!isClusteringAgentInitialized) {
                clusteringAgent.init();
                isClusteringAgentInitialized = true;
            }

            configContext.
                    setNonReplicableProperty(ClusteringConstants.CLUSTER_INITIALIZED, "true");
        }
    }
}
