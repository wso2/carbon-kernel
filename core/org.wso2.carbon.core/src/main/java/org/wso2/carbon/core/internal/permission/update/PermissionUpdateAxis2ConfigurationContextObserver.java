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
package org.wso2.carbon.core.internal.permission.update;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

/**
 * This OSGi service will take care of updating the permission cache when tenants are lazily loaded
 * & unloaded.
 */
public class PermissionUpdateAxis2ConfigurationContextObserver extends
        AbstractAxis2ConfigurationContextObserver {

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        PermissionUpdater.update(tenantId);
    }

    public void terminatingConfigurationContext(ConfigurationContext configCtx) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        PermissionUpdater.remove(tenantId);
    }

    public void terminatedConfigurationContext(ConfigurationContext configCtx) {
        // Nothing to do
    }
}