/*
 * Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.wso2.carbon.core.session;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.ha.ClusterMessage;
import org.apache.catalina.ha.session.SessionMessage;
import org.wso2.carbon.CarbonConstants;

import java.util.Map;
import java.util.Set;

/**
 * This is wrapper class which wraps tomcat's ClusterMessage in axis2's ClusteringMessage.
 * Since we are using axis2's tribes clustering communication to send cluster messages,
 * we have to wrap the tomcat cluster message, So that at receiving, side we can unwrap
 * the massage and call the tomcat session manager to handle that received message.
 */

public class CarbonSessionReplicationMessage extends ClusteringMessage {

    ClusterMessage clusterMessage;

    public CarbonSessionReplicationMessage() {
    }

    public void setSessionClusterMessage(ClusterMessage clusterMessage) {
        this.clusterMessage = clusterMessage;
    }

    @Override
    public ClusteringCommand getResponse() {
        return new CarbonSessionReplicationMessage();
    }

    @Override
    public void execute(ConfigurationContext configContext) throws ClusteringFault {
        if (clusterMessage != null) {
            //Process the received replication message
            Map<String, CarbonTomcatClusterableSessionManager> sessionManagerMap =
                    (Map<String, CarbonTomcatClusterableSessionManager>) configContext.
                            getProperty(CarbonConstants.TOMCAT_SESSION_MANAGER_MAP);
            if (sessionManagerMap != null && !sessionManagerMap.isEmpty() &&
                ((SessionMessage) clusterMessage).getContextName() != null) {
                String context = getWebappContext(((SessionMessage) clusterMessage).
                        getContextName(), sessionManagerMap.keySet());
                if (context != null) {
                    CarbonTomcatClusterableSessionManager manager = sessionManagerMap.get(context);
                    if (manager != null) {
                        manager.clusterMessageReceived(clusterMessage);
                    }
                }
            }
        }
    }

    private String getWebappContext(String path, Set<String> contextSet) {
        for (String key : contextSet) {
            if (path.contains(key)) {
                return key;
            }
        }
        return null;
    }
}
