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
package org.apache.axis2.clustering.control.wka;

import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.control.ControlCommand;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is the message a member will send to another member when it intends to join a group.
 * This is used with WKA based membership
 */
public class JoinGroupCommand extends ControlCommand {

    private Log log = LogFactory.getLog(JoinGroupCommand.class);

    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {
        log.info("JOIN request received");
    }
}
