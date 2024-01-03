/*
 * Copyright (c) 2022, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.hazelcast;

import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 */
public class GroupManagementCommandListener implements MessageListener<GroupManagementCommand> {
    private static final Log log = LogFactory.getLog(GroupManagementCommandListener.class);
    private ConfigurationContext configurationContext;

    public GroupManagementCommandListener(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    @Override
    public void onMessage(Message<GroupManagementCommand> clusteringMessage) {
        try {
            GroupManagementCommand msg = clusteringMessage.getMessageObject();
                log.info("Received GroupManagementCommand: " + msg);
                msg.execute(configurationContext);
        } catch (ClusteringFault e) {
            log.error("Cannot process ClusteringMessage", e);
        }
    }
}
