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


import org.wso2.carbon.multitenancy.api.Tenant;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.security.auth.Subject;

/**
 * This class will preserve an instance the current CarbonContext as a thread local variable. If a CarbonContext is
 * available on a thread-local-scope this class will do the required lookup and obtain the corresponding instance.
 *
 * @since 5.0.0
 */

public class CarbonContextHolder {

    private Tenant tenant;
    private Subject subject;
    private Map<String, Object> properties;

    private static ThreadLocal<CarbonContextHolder> currentContextHolder = new ThreadLocal<CarbonContextHolder>() {
        protected CarbonContextHolder initialValue() {
            return new CarbonContextHolder();
        }
    };

    private CarbonContextHolder() {
    }

    private static ThreadLocal<Stack<CarbonContextHolder>> carbonContextHolderStack = new ThreadLocal<>();

    public static CarbonContextHolder getCurrentContextHolder() {
        return currentContextHolder.get();
    }


    public void destroyCurrentCarbonContextHolder() {
        currentContextHolder.remove();
        carbonContextHolderStack.remove();
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        if (this.tenant != null && this.tenant.getDomain() != null
                && !this.tenant.getDomain().equals(tenant.getDomain())) {
            throw new IllegalStateException("Trying to override the current tenant " + this.tenant.getDomain() +
                    " to " + tenant.getDomain());
        }
        this.tenant = tenant;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        if (this.subject != null && !this.subject.equals(subject)) {
            throw new IllegalStateException("Trying to override the already available subject " +
                    this.subject.toString() + " to  " + subject.toString());
        }
        this.subject = subject;
    }

    /**
     * Method to obtain a property on this CarbonContext instance.
     *
     * @param name the property name.
     * @return the value of the property by the given name.
     */
    public Object getProperty(String name) {
        if (properties == null) {
            return null;
        }
        return properties.get(name);
    }

    /**
     * Method to set a property on this CarbonContext instance.
     *
     * @param name  the property name.
     * @param value the value to be set to the property by the given name.
     */
    public void setProperty(String name, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(name, value);
    }
}
