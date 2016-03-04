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
package org.wso2.carbon.multitenancy;

import org.wso2.carbon.multitenancy.api.Tenant;
import org.wso2.carbon.multitenancy.api.TenantEvent;
import org.wso2.carbon.multitenancy.api.TenantStore;
import org.wso2.carbon.multitenancy.exception.TenantException;
import org.wso2.carbon.multitenancy.exception.TenantStoreException;
import org.wso2.carbon.multitenancy.internal.OSGiServiceHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TenantRuntime represents the entity who manages the tenants. TenantRuntime allows you to add tenants, delete
 * tenants, get loaded tenant information, load tenant etc.
 *
 * @since 1.0.0
 */
public class TenantRuntime {

    private TenantStore tenantStore;

    private Map<String, Tenant> loadedTenants = new HashMap<>();

    public TenantRuntime(TenantStore tenantStore) {
        this.tenantStore = tenantStore;
    }

    public Tenant loadTenant(String tenantDomain) throws TenantException {
        if (loadedTenants.containsKey(tenantDomain)) {
            return loadedTenants.get(tenantDomain);
        }
        Tenant tenant;
        try {
            tenant = tenantStore.loadTenant(tenantDomain);
            loadedTenants.put(tenantDomain, tenant);
            OSGiServiceHolder.getInstance().getTenantListeners()
                    .forEach(tenantListener ->
                            tenantListener.notify(new TenantEvent(TenantEvent.LOADED, tenantDomain)));
        } catch (TenantStoreException e) {
            throw new TenantException("Error while loading tenant with the domain : " + tenantDomain, e);
        }
        return tenant;
    }

    public List<Tenant> getLoadedTenants() {
        return loadedTenants.values()
                .stream()
                .collect(Collectors.toList());
    }

    public Tenant unloadTenant(String tenantDomain) {
        Tenant tenant = loadedTenants.remove(tenantDomain);
        OSGiServiceHolder.getInstance().getTenantListeners()
                .forEach(tenantListener ->
                        tenantListener.notify(new TenantEvent(TenantEvent.UNLOADED, tenantDomain)));
        return tenant;
    }
}
