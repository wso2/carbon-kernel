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
package org.wso2.carbon.feature.mgt.core.operations;

public class OperationFactory {
    public static final String INSTALL_ACTION = "org.wso2.carbon.prov.action.install";
    public static final String UNINSTALL_ACTION = "org.wso2.carbon.prov.action.uninstall";
    public static final String REVERT_ACTION = "org.wso2.carbon.prov.action.revert";

    public static ProfileChangeOperation getProfileChangeOperation(String actionType) {
        if (INSTALL_ACTION.equals(actionType)) {
            return new InstallOperation(INSTALL_ACTION);
        } else if (UNINSTALL_ACTION.equals(actionType)) {
            return new UninstallOperation(UNINSTALL_ACTION);
        } else if (REVERT_ACTION.equals(actionType)) {
            return new RevertOperation(REVERT_ACTION);
        }
        return null;
    }
}
