/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.context.api.internal;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.multitenancy.TenantRuntime;

/**
 * TODO
 */
@Component(
        name = "org.wso2.carbon.context.api.internal.CarbonContextServiceComponent",
        immediate = true
)
public class CarbonContextServiceComponent {
    @Reference(
            service = TenantRuntime.class,
            policy = ReferencePolicy.DYNAMIC,
            cardinality = ReferenceCardinality.MANDATORY,
            unbind = "unsetTenantRuntime"
    )
    protected void setTenantRuntime(TenantRuntime tenantRuntime) {
        OSGiServiceHolder.getInstance().setTenantRuntime(tenantRuntime);
    }

    protected void unsetTenantRuntime(TenantRuntime tenantRuntime) {
        OSGiServiceHolder.getInstance().setTenantRuntime(null);
    }
}
