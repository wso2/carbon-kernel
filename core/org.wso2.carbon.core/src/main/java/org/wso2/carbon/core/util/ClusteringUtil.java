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

import java.util.Map;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringConstants;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.session.CarbonTomcatClusterableSessionManager;
import org.wso2.carbon.core.session.CarbonTomcatSessionMessage;

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
            //TODO Check for if session replication is enabled and then call sendQueuedMessages
//            if (configContext.getProperty(SessionConstants.SESSION_REPLICATION_INITIALIZED) != null) {
//                if ((Boolean) configContext.
//                        getProperty(SessionConstants.SESSION_REPLICATION_INITIALIZED)) {
//                    Utils.sendQueuedMessages(configContext);
//                }
//            }
            sendQueuedMessages(configContext);
            // Configuration Manager
            /*if (clusteringAgent.getConfigurationManager() != null) {
                Map msgReceiverMap = new HashMap();
                msgReceiverMap.put(WSDL2Constants.MEP_URI_IN_OUT,
                                   new RPCMessageReceiver());
                msgReceiverMap.put(WSDL2Constants.MEP_URI_IN_ONLY,
                                   new RPCInOnlyMessageReceiver());
                msgReceiverMap.put(WSDL2Constants.MEP_URI_ROBUST_IN_ONLY,
                                   new RPCInOnlyMessageReceiver());
                AxisService nodeManagerService =
                        AxisService.createService("org.wso2.carbon.clustering.NodeManager",
                                                  axisConfig,
                                                  msgReceiverMap,
                                                  null,
                                                  "http://org.apache.axis2/xsd",
                                                  clusteringAgent.getClass().getClassLoader());
                List transports = new ArrayList();
                transports.add(ServerConstants.HTTPS_TRANSPORT);
                nodeManagerService.setExposedTransports(transports);
                nodeManagerService.setName(ClusteringConstants.NODE_MANAGER_SERVICE);
                nodeManagerService.setScope(Constants.SCOPE_TRANSPORT_SESSION);
                nodeManagerService.setElementFormDefault(false);
                AxisServiceGroup adminSG =
                        axisConfig.getServiceGroup(ServerConstants.ADMIN_SERVICE_GROUP);
                if (adminSG != null) {
                    try {
                        adminSG.addService(nodeManagerService);
                    } catch (Exception e) {
                        throw AxisFault.makeFault(e);
                    }
                } else {
                    axisConfig.addService(nodeManagerService);
                }
            }*/

            configContext.
                    setNonReplicableProperty(ClusteringConstants.CLUSTER_INITIALIZED, "true");
        }
    }
    
    private static void sendQueuedMessages(ConfigurationContext configContext)
            throws ClusteringFault {
        Map<String, CarbonTomcatClusterableSessionManager> sessionManagerMap =
                (Map<String, CarbonTomcatClusterableSessionManager>) configContext.
                        getProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP);
        if (sessionManagerMap != null && !sessionManagerMap.isEmpty()) {

            for (CarbonTomcatClusterableSessionManager sessionManager : sessionManagerMap.values()) {
                Map<String, CarbonTomcatSessionMessage> messageMap = sessionManager.
                        getQueuedSessionMsgMap();

                ClusteringAgent clusteringAgent = configContext.
                        getAxisConfiguration().getClusteringAgent();
                if (clusteringAgent != null) {
                    for (String key : messageMap.keySet()) {
                        clusteringAgent.sendMessage(messageMap.get(key), true);
                        messageMap.remove(key);
                    }
                }
            }
        }
    }
}
