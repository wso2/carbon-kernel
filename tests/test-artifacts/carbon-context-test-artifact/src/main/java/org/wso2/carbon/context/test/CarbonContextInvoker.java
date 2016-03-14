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
package org.wso2.carbon.context.test;

import org.wso2.carbon.kernel.context.PrivilegedCarbonContext;
import org.wso2.carbon.kernel.tenant.Tenant;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * An example class to test the carbon context API invocation
 *
 * @since 5.1.0
 */
public class CarbonContextInvoker {
    private String tenantPropertyKey;
    private String tenantPropertyValue;
    private String carbonContextPropertyKey;
    private Object carbonContextPropertyValue;
    private String userPrincipalName;

    public CarbonContextInvoker(String tenantPropertyKey, String tenantPropertyValue,
                         String carbonContextPropertyKey, Object carbonContextPropertyValue, String userPrincipalName) {
        this.tenantPropertyKey = tenantPropertyKey;
        this.tenantPropertyValue = tenantPropertyValue;
        this.carbonContextPropertyKey = carbonContextPropertyKey;
        this.carbonContextPropertyValue = carbonContextPropertyValue;
        this.userPrincipalName = userPrincipalName;
    }

    public void invoke() {
        PrivilegedCarbonContext privilegedCarbonContext =
                (PrivilegedCarbonContext) PrivilegedCarbonContext.getCurrentContext();
        Principal userPrincipal = () -> userPrincipalName;
        Map<String, Object> properties = new HashMap<>();
        properties.put(tenantPropertyKey, tenantPropertyValue);
        privilegedCarbonContext.setUserPrincipal(userPrincipal);
        privilegedCarbonContext.setProperty(carbonContextPropertyKey, carbonContextPropertyValue);
        Tenant tenant = PrivilegedCarbonContext.getCurrentContext().getServerTenant();
        tenant.setProperties(properties);
    }
}
