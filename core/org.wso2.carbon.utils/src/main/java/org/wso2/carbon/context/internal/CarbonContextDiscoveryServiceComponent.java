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
package org.wso2.carbon.context.internal;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.DiscoveryService;

@Component(name = "org.wso2.carbon.context.internal.CarbonContextDiscoveryServiceComponent", immediate = true)
public class CarbonContextDiscoveryServiceComponent {
    private OSGiDataHolder dataHolder = OSGiDataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext componentContext) {
    }
    
    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
    }

    @Reference(name = "discoveryService", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetDiscoveryService")
    protected void setDiscoveryService(DiscoveryService discoveryService) {
        CarbonContextDataHolder.setDiscoveryServiceProvider(discoveryService);
    }

    protected void unsetDiscoveryService(DiscoveryService discoveryService) {
        CarbonContextDataHolder.setDiscoveryServiceProvider(null);
    }
}
