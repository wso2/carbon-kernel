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
package org.wso2.carbon.kernel.tenant;


import org.wso2.carbon.kernel.internal.DataHolder;
import org.wso2.carbon.kernel.tenant.exception.TenantException;
import org.wso2.carbon.kernel.tenant.exception.TenantStoreException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TenantRuntime represents the entity who manages the tenant. TenantRuntime allows you to add tenant, delete
 * tenant, get loaded tenant information, load tenant etc.
 *
 * @since 5.1.0
 */
public class TenantRuntime {

    private TenantStore<Tenant> tenantStore;

    private Map<String, Tenant> loadedTenants = new HashMap<>();

    /**
     * Constructs the TenantRuntime instance with the given implementation of TenantStore.
     *
     * @param tenantStore tenant store instance to use with this runtime.
     */
    public TenantRuntime(TenantStore<Tenant> tenantStore) {
        this.tenantStore = tenantStore;
    }

    /**
     * Loads and returns the tenant object by given tenant domain value and invokes the tenant listeners for tenant
     * loaded event.
     *
     * @param tenantDomain name of the tenant domain to be loaded.
     * @return the loaded tenant object.
     * @throws TenantException on error while trying to load the tenant.
     */
    public Tenant loadTenant(String tenantDomain) throws TenantException {
        if (loadedTenants.containsKey(tenantDomain)) {
            return loadedTenants.get(tenantDomain);
        }
        Tenant tenant;
        try {
            tenant = tenantStore.loadTenant(tenantDomain);
            loadedTenants.put(tenantDomain, tenant);
            DataHolder.getInstance().getTenantListeners()
                    .forEach(tenantListener ->
                            tenantListener.notify(new TenantEvent(TenantEvent.LOADED, tenantDomain)));
        } catch (TenantStoreException e) {
            throw new TenantException("Error while loading tenant with the domain : " + tenantDomain, e);
        }
        return tenant;
    }

    /**
     * Method to return the loaded tenant object list.
     *
     * @return list of loaded tenants.
     */
    public List<Tenant> getLoadedTenants() {
        return loadedTenants.values()
                .stream()
                .collect(Collectors.toList());
    }

    /**
     * Removes the tenant from loaded list and invokes the listeners for tenant unloaded event.
     *
     * @param tenantDomain the value of the tenant domain to unload.
     * @return the unloaded tenant instance.
     */
    public Tenant unloadTenant(String tenantDomain) {
        Tenant tenant = loadedTenants.remove(tenantDomain);
        DataHolder.getInstance().getTenantListeners()
                .forEach(tenantListener ->
                        tenantListener.notify(new TenantEvent(TenantEvent.UNLOADED, tenantDomain)));
        return tenant;
    }
}
