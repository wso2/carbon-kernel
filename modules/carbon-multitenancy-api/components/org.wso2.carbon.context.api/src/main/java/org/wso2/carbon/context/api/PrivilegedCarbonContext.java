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
package org.wso2.carbon.context.api;

import org.wso2.carbon.context.api.internal.CarbonContextHolder;
import org.wso2.carbon.multitenancy.api.Tenant;

import javax.security.auth.Subject;

/**
 * This CarbonContext provides users the ability to carry out privileged actions such as switching tenant flows,
 * setting domain, setting subject etc.
 *
 * @since 5.0.0
 */

public final class PrivilegedCarbonContext extends CarbonContext {

    private PrivilegedCarbonContext(CarbonContextHolder carbonContextHolder) {
        super(carbonContextHolder);
    }

    public static CarbonContext getCurrentContext() {
        return new PrivilegedCarbonContext(CarbonContextHolder.getCurrentContextHolder());
    }

    public static void destroyCurrentContext() {
        CarbonContextUtils.checkSecurity();
        getCurrentContext().getCarbonContextHolder().destroyCurrentCarbonContextHolder();
    }

    public void setTenant(Tenant tenant) {
        CarbonContextUtils.checkSecurity();
        getCarbonContextHolder().setTenant(tenant);
    }

    public void setSubject(Subject subject) {
        CarbonContextUtils.checkSecurity();
        getCarbonContextHolder().setSubject(subject);
    }

    public void setProperty(String name, Object value) {
        CarbonContextUtils.checkSecurity();
        getCarbonContextHolder().setProperty(name, value);
    }
}
