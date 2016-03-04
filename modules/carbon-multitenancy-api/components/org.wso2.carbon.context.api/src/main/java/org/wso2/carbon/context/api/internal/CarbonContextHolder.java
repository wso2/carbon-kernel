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

import org.wso2.carbon.context.api.CarbonContextUtils;
import org.wso2.carbon.context.api.TenantDomainSupplier;
import org.wso2.carbon.multitenancy.api.Tenant;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class will preserve an instance the current CarbonContext as a thread local variable. If a CarbonContext is
 * available on a thread-local-scope this class will do the required lookup and obtain the corresponding instance.
 *
 * @since 5.0.0
 */

public final class CarbonContextHolder {

    private Tenant tenant;
    private Principal userPrincipal;
    private Map<String, Object> properties = new HashMap<>();

    private static ThreadLocal<CarbonContextHolder> currentContextHolder = new ThreadLocal<CarbonContextHolder>() {
        protected CarbonContextHolder initialValue() {
            return new CarbonContextHolder();
        }
    };

    private CarbonContextHolder() {
        Optional<String> optionalTenantDomain = CarbonContextUtils.getSystemTenantDomain();
        optionalTenantDomain.ifPresent(tenantDomain -> this.tenant = new Tenant(tenantDomain));
    }

    public static CarbonContextHolder getCurrentContextHolder() {
        return currentContextHolder.get();
    }

    public void destroyCurrentCarbonContextHolder() {
        currentContextHolder.remove();
    }


    public void setTenant(TenantDomainSupplier tenantDomainSupplier) {
        if (!CarbonContextUtils.getSystemTenantDomain().isPresent()) {
            String tenantDomain = tenantDomainSupplier.get();
            if (tenant == null) {
                tenant = new Tenant(tenantDomain);
                return;
            }
            Optional.of(tenant.getDomain())
                    .filter(currentTenantDomain -> currentTenantDomain.equals(tenantDomain))
                    .orElseThrow(() -> new IllegalStateException("Trying to override the current tenant " +
                            tenant.getDomain() + " to " + tenantDomain));
        }
    }


    public Tenant getTenant() {
        return tenant;
    }

    /**
     * Method to obtain a property on this CarbonContext instance.
     *
     * @param name the property name.
     * @return the value of the property by the given name.
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }

    /**
     * Method to set a property on this CarbonContext instance.
     *
     * @param name  the property name.
     * @param value the value to be set to the property by the given name.
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(Principal userPrincipal) {
        if (this.userPrincipal == null) {
            this.userPrincipal = userPrincipal;
            return;
        }
        Optional.ofNullable(this.userPrincipal.getName())
                .filter(name -> userPrincipal.getName().equals(name))
                .orElseThrow(() -> new IllegalStateException("Trying to override the already available user " +
                        "principal from " + this.userPrincipal.toString() + " to " + userPrincipal.toString()));
    }
}
