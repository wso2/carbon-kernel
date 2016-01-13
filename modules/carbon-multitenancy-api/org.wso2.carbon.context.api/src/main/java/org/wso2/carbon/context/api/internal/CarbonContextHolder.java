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

import javax.security.auth.Subject;
import java.util.Stack;

public class CarbonContextHolder {

    private String tenantDomain;
    private Subject subject;

    private static ThreadLocal<CarbonContextHolder> currentContextHolder = new ThreadLocal<CarbonContextHolder>() {
        protected CarbonContextHolder initialValue() {
            return new CarbonContextHolder();
        }
    };

    private CarbonContextHolder() {
        tenantDomain = null;
        subject = null;
    }

    private static ThreadLocal<Stack<CarbonContextHolder>> parentContextHolderStack = new ThreadLocal<>();

    public static CarbonContextHolder getThreadLocalCarbonContextHolder() {
        return currentContextHolder.get();
    }

    public void startTenantFlow() {
        Stack<CarbonContextHolder> carbonContextDataHolders = parentContextHolderStack.get();
        if (carbonContextDataHolders == null) {
            carbonContextDataHolders = new Stack<>();
            parentContextHolderStack.set(carbonContextDataHolders);
        }
        carbonContextDataHolders.push(currentContextHolder.get());
        currentContextHolder.remove();
    }

    public void endTenantFlow() {
        Stack<CarbonContextHolder> carbonContextDataHolders = parentContextHolderStack.get();
        if (carbonContextDataHolders != null) {
            currentContextHolder.set(carbonContextDataHolders.pop());
        }
    }

    public static void destroyCurrentCarbonContextHolder() {
        currentContextHolder.remove();
        parentContextHolderStack.remove();
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        if (this.tenantDomain != null && !this.tenantDomain.equals(tenantDomain)) {
            throw new IllegalStateException("Trying to set the domain from " + this.tenantDomain + " to "
                    + tenantDomain);
        }
        this.tenantDomain = tenantDomain;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }
}
