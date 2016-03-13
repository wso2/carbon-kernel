package org.wso2.carbon.kernel.tenant;
/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.kernel.tenant.exception.TenantStoreException;

/**
 * TenantStore represents the storage of tenant. This class gives an abstract API to TenantRuntime to persist
 * tenant information.
 *
 * @param <T> type of the tenants stored by this implementation
 * @see Tenant
 * @since 1.0.0
 */

public interface TenantStore<T extends Tenant> {

    /**
     * Initializes the storage.
     * Some implementations prefer to load all the tenant information at once. But some implementations loads tenant
     * in a lazy loading manner.
     *
     * @throws TenantStoreException if the tenant store fails to initialization
     */
    void init() throws TenantStoreException;

    /**
     * Loads the tenant from the underlying storage.
     *
     * @param tenantDomain the domain of the tenant
     * @return the loaded Tenant instance
     * @throws TenantStoreException if the tenant store fails to load the tenant
     */
    T loadTenant(String tenantDomain) throws TenantStoreException;

    /**
     * Add and persist the tenant in the underlying storage
     *
     * @param tenant the Tenant instance to be added to the store
     * @throws TenantStoreException if the tenant store fails to persists the tenant
     */
    void addTenant(T tenant) throws TenantStoreException;

    /**
     * Deletes the tenant from the underlying storage
     *
     * @param tenantDomain the domain of the tenant
     * @return the deleted Tenant instance
     * @throws TenantStoreException if the tenant store fails to delete the tenant.
     */
    T deleteTenant(String tenantDomain) throws TenantStoreException;
}
