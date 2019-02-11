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

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.management.GroupManagementCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.Controllable;
import org.wso2.carbon.utils.ServerConstants;

/**
 *
 */
public class RestartMemberGracefullyCommand extends GroupManagementCommand {
    private static final Log log = LogFactory.getLog(RestartMemberGracefullyCommand.class);

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        final Controllable controllable =
                (Controllable) configurationContext.
                        getProperty(ServerConstants.CARBON_INSTANCE);
        Thread th = new Thread() {
            public void run() {
                try {
                    Thread.sleep(1000);
                    controllable.restartGracefully();
                } catch (Exception e) {
                    String msg = "Cannot restart server";
                    log.error(msg, e);
                    throw new RuntimeException(msg, e);
                }
            }
        };
        th.start();
    }
}
