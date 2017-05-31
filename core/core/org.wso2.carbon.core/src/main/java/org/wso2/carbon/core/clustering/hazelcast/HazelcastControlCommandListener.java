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
package org.wso2.carbon.core.clustering.hazelcast;

import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * TODO: class description
 */
public class HazelcastControlCommandListener implements MessageListener<ControlCommand> {
    private static final Log log = LogFactory.getLog(HazelcastControlCommandListener.class);
    private ConfigurationContext configurationContext;

    public HazelcastControlCommandListener(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    @Override
    public void onMessage(Message<ControlCommand> controlCommand) {
        try {
            log.info("Received ControlCommand: " + controlCommand.getMessageObject());
            controlCommand.getMessageObject().execute(configurationContext);
        } catch (ClusteringFault e) {
            log.error("Cannot process ControlCommand", e);
        }
    }
}
