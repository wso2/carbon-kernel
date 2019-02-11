/*                                                                             
 * Copyright 2004,2005 The Apache Software Foundation.                         
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.cluster.mgt.core;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.TransportInDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.ServerManagement;

import java.util.Map;

/**
 * Cluster management command for ending maintenance of this node
 */
public class EndMaintenanceCommand extends GroupManagementCommand {

    private static final Log log = LogFactory.getLog(EndMaintenanceCommand.class);

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
       Map<String, TransportInDescription> inTransports =
               configurationContext.getAxisConfiguration().getTransportsIn();
        try {
            new ServerManagement(inTransports, configurationContext).endMaintenance();
        } catch (Exception e) {
            String msg = "Cannot end maintenance";
            log.error(msg, e);
        }
        try {
            org.wso2.carbon.core.ServerStatus.setServerRunning();
        } catch (AxisFault e) {
            String msg = "Cannot set server to running mode";
            log.error(msg, e);
        }
    }
}
