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
import org.wso2.carbon.context.CarbonCoreInitializedEvent;
import org.wso2.carbon.context.CarbonCoreInitializedEventImpl;
import org.wso2.carbon.registry.api.RegistryService;
import org.wso2.carbon.user.api.UserRealmService;

/**
 * @scr.component name="org.wso2.carbon.context.internal.CarbonContextServiceComponent" immediate="true"
 * @scr.reference name="registry.service" interface="org.wso2.carbon.registry.api.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="userRealmService" interface="org.wso2.carbon.user.api.UserRealmService"
 * cardinality="1..1" policy="dynamic"  bind="setUserRealmService" unbind="unsetUserRealmService"
 */
public class CarbonContextServiceComponent {
    private OSGiDataHolder dataHolder = OSGiDataHolder.getInstance();


    protected void activate(ComponentContext componentContext) {
        //register a CarbonCoreInitializedEvent (an empty service) to guarantee the activation order
        componentContext.getBundleContext().registerService(CarbonCoreInitializedEvent.class.getName(), new CarbonCoreInitializedEventImpl(), null);
    }

    protected void deactivate(ComponentContext componentContext) {
    }

    protected void setRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        dataHolder.setRegistryService(null);
    }

    protected void setUserRealmService(UserRealmService userRealmService){
        dataHolder.setUserRealmService(userRealmService);
    }

    protected void unsetUserRealmService(UserRealmService userRealmService){
        dataHolder.setUserRealmService(null);
    }
}
