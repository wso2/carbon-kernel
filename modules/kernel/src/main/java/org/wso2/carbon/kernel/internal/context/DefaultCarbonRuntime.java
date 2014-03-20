/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.kernel.internal.context;

import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.PrivilegedCarbonRuntime;
import org.wso2.carbon.kernel.internal.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.tenant.TenantRuntime;

public class DefaultCarbonRuntime implements PrivilegedCarbonRuntime {
    private CarbonConfiguration carbonConfiguration;
    private TenantRuntime tenantRuntime;

    public CarbonConfiguration getConfiguration() {
        return carbonConfiguration;
    }

    @Override
    public TenantRuntime getTenantRuntime() {
        return tenantRuntime;
    }

    @Override
    public void setCarbonConfiguration(CarbonConfiguration carbonConfiguration) {
        this.carbonConfiguration = carbonConfiguration;
    }

    @Override
    public void setTenantRuntime(TenantRuntime tenantRuntime) {
        this.tenantRuntime = tenantRuntime;
    }
}
