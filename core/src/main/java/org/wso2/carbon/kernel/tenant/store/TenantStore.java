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
package org.wso2.carbon.kernel.tenant.store;

import org.wso2.carbon.kernel.tenant.Tenant;

/**
 * TenantStore represents the storage of tenants. This class gives an abstract API to TenantRuntime to persist
 * tenant information.
 *
 * @param <T> type of the tenants that this class persists
 * @see Tenant
 * @since 5.0.0
 */
public interface TenantStore<T extends Tenant> {

    /**
     * Initializes the storage.
     * Some implementations prefer to load all the tenant information at once. But some implementations loads tenant
     * in a lazy loading manner.
     *
     * @throws Exception if the tenant store fails to initialization
     */
    public void init() throws Exception;

    /**
     * Loads the tenant from the underlying storage.
     *
     * @param tenantDomain the domain of the tenant
     * @return the loaded Tenant instance
     * @throws Exception if the tenant store fails to load the tenant
     */
    public T loadTenant(String tenantDomain) throws Exception;

    /**
     * Persist the tenant in the underlying storage
     *
     * @param tenant the Tenant instance to be persisted
     * @throws Exception if the tenant store fails to persists the tenant
     */
    public void persistTenant(T tenant) throws Exception;

    /**
     * Deletes the tenant from the underlying storage
     *
     * @param tenantDomain the domain of the tenant
     * @return the deleted Tenant instance
     * @throws Exception if the tenant store fails to delete the tenant.
     */
    public T deleteTenant(String tenantDomain) throws Exception;
}
