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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(name = "org.wso2.carbon.core.internal.ListenerManagerRetrieverServiceComponent", immediate = true)
public class ListenerManagerRetrieverServiceComponent {

    private CarbonCoreDataHolder dataHolder = CarbonCoreDataHolder.getInstance();

    @Reference(name = "listener.manager.service", cardinality = ReferenceCardinality.MANDATORY, 
            policy = ReferencePolicy.DYNAMIC, unbind = "unsetListenerManager")
    protected void setListenerManager(ListenerManager listenerManager) {
        dataHolder.setListenerManager(listenerManager);
    }

    protected void unsetListenerManager(ListenerManager listenerManager) {
        dataHolder.setListenerManager(null);
    }
}
