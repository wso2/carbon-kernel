/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.kernel;

import org.wso2.carbon.kernel.internal.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.tenant.Tenant;
import org.wso2.carbon.kernel.tenant.TenantRuntime;

/**
 * PrivilegedCarbonRuntime represents the server runtime. This class contains setter methods to set
 * CarbonConfiguration and TenantRuntime. Only privileged code can access these setter method. CarbonRuntime class
 * simply gives a read-only access the the server runtime.
 *
 * @see Tenant
 * @see TenantRuntime
 * @since 5.0.0
 */
public interface PrivilegedCarbonRuntime extends CarbonRuntime {

    /**
     * Accepts an instance of the CarbonConfiguration class.
     *
     * @param carbonConfiguration the CarbonConfiguration instance
     */
    public void setCarbonConfiguration(CarbonConfiguration carbonConfiguration);

    /**
     * Accepts an instance of the TenantRuntime class.
     *
     * @param tenantRuntime the TenantRuntime instance
     */
    public void setTenantRuntime(TenantRuntime<Tenant> tenantRuntime);
}
