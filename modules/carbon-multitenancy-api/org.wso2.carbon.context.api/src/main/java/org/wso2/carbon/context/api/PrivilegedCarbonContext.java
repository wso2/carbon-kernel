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

import javax.security.auth.Subject;

public class PrivilegedCarbonContext extends CarbonContext {

    private PrivilegedCarbonContext(CarbonContextHolder carbonContextHolder) {
        super(carbonContextHolder);
    }

    public static PrivilegedCarbonContext getThreadLocalCarbonContext() {
        Utils.checkSecurity();
        return new PrivilegedCarbonContext(CarbonContextHolder.getThreadLocalCarbonContextHolder());
    }

    public static void startTenantFlow() {
        Utils.checkSecurity();
        getThreadLocalCarbonContext().getCarbonContextHolder().startTenantFlow();
    }


    public static void endTenantFlow() {
        Utils.checkSecurity();
        getThreadLocalCarbonContext().getCarbonContextHolder().endTenantFlow();
    }


    public static void destroyCurrentContext() {
        Utils.checkSecurity();
        CarbonContextHolder.destroyCurrentCarbonContextHolder();
    }

    public void setTenantDomain(String tenantDomain) {
        getCarbonContextHolder().setTenantDomain(tenantDomain);
    }

    public void setSubject(Subject subject) {
        getCarbonContextHolder().setSubject(subject);
    }
}
