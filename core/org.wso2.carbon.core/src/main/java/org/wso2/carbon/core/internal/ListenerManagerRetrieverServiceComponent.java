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
package org.wso2.carbon.core.internal;

import org.apache.axis2.engine.ListenerManager;

/**
 * This service  component is responsible for retrieving the ListenerManager OSGi service
 * that is used  by the Carbon server. This ListenerManager will be required by Carbon core when the 
 * ListenerManager needs to be stopped.
 *
 * @scr.component name="org.wso2.carbon.core.internal.ListenerManagerRetrieverServiceComponent"
 * immediate="true"
 * @scr.reference name="listener.manager.service" interface="org.apache.axis2.engine.ListenerManager"
 * cardinality="1..1" policy="dynamic"  bind="setListenerManager" unbind="unsetListenerManager"
 */
public class ListenerManagerRetrieverServiceComponent {

    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    protected void setListenerManager(ListenerManager listenerManager) {
        dataHolder.setListenerManager(listenerManager);
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        dataHolder.setListenerManager(null);
    }
}
