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
package org.wso2.carbon.kernel.tenant;

import java.util.Map;

/**
 * TenantRuntime represents the entity who manages the tenants. TenantRuntime allows you to add tenants, delete
 * tenants, get tenant information, load tenant etc.
 *
 * @param <T> type of the tenants that this class manages
 * @see Tenant
 * @see org.wso2.carbon.kernel.tenant.store.TenantStore
 * @since 5.0.0
 */
public interface TenantRuntime<T extends Tenant> {

    /**
     * Initializes the TenantRuntime.
     * Usually TenantRuntime is associated with a TenantStore. This method initializes
     * TenantStore as well.
     *
     * @throws Exception if an error occurs during the initialization process of this class or the TenantStore class.
     * @see org.wso2.carbon.kernel.tenant.store.TenantStore
     */
    public void init() throws Exception;

    /**
     * <p>
     * Adds a new tenant to the system from the specified <code>Tenant</code> object.
     * Persists the given tenant data using the available <code>TenantStore</code> implementation.
     * </p>
     *
     * @param tenant the Tenant instance with data
     * @return an instance of the created tenant
     * @throws Exception in the event of a failure while storing the tenant
     */
    public T addTenant(T tenant) throws Exception;

    /**
     * Adds a new tenant to the system using the specified <code>domain</code>, <code>name</code>,
     * <code>description</code>, username and the email address of the tenant administrator.
     *
     * @param domain                the domain of the tenant
     * @param name                  the human readable name of the tenant
     * @param description           the description of the tenant
     * @param adminUsername         the username of the tenant administrator
     * @param adminUserEmailAddress the email address of the tenant administrator
     * @param props                 the map of the tenant attributes
     * @return an instance of the created tenant
     * @throws Exception in the event of a failure while storing the tenant
     */
    public T addTenant(String domain, String name, String description, String adminUsername,
                       String adminUserEmailAddress, Map<String, String> props) throws Exception;

    /**
     * Adds a new tenant to the system using the specified <code>domain</code>, <code>name</code>,
     * <code>description</code>, username, the email address of the tenant administrator,
     * <code>parentTenantDomain</code>, list of child tenant domains and the allowed depth of the tenant hierarchy.
     *
     * @param domain                the domain of the tenant
     * @param name                  the human readable name of the tenant
     * @param description           the description of the tenant
     * @param adminUsername         the username of the tenant administrator
     * @param adminUserEmailAddress the email address of the tenant administrator
     * @param props                 the map of the tenant attributes
     * @param parentTenantDomain    the tenant domain of the parent tenant
     * @param childTenantDomains    the array of child tenant domains
     * @param depthOfHierarchy      the allowed depth of the tenant hierarchy
     * @return an instance of the created tenant
     * @throws Exception in the event of a failure while storing the tenant
     */
    public T addTenant(String domain, String name, String description, String adminUsername,
                       String adminUserEmailAddress, Map<String, String> props, String parentTenantDomain,
                       String[] childTenantDomains, int depthOfHierarchy) throws Exception;

    /**
     * Deletes the tenant from the system.
     *
     * @param tenantDomain the domain of the tenant to be deleted.
     * @return an instance of the deleted tenant.
     * @throws Exception in the event of a failure while deleting the tenant
     */
    public T deleteTenant(String tenantDomain) throws Exception;

    /**
     * Loads the tenant from the TenantStore and returns a Tenant instance.
     *
     * @param tenantDomain the domain of the tenant to be loaded
     * @return the loaded tenant
     * @throws Exception in the event of a failure while loading the tenant details
     */
    public T getTenant(String tenantDomain) throws Exception;
}
