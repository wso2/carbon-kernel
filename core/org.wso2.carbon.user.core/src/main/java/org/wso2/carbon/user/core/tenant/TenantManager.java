/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.user.core.tenant;

import org.osgi.framework.BundleContext;
import org.wso2.carbon.user.core.NotImplementedException;
import org.wso2.carbon.user.core.UserStoreException;

public interface TenantManager extends org.wso2.carbon.user.api.TenantManager {

    String getSuperTenantDomain() throws UserStoreException;

    void setBundleContext(BundleContext bundleContext);

    /**
     * In some tenant management scenarios, for example: TM with embedded-ldap, we need to
     * initialize existing tenant partitions at the new start of the server.
     * This method will handle that.
     */
    void initializeExistingPartitions();

    /**
     * List tenant information of tenants.
     *
     * @param limit     limit per page.
     * @param offset    offset value.
     * @param filter    filter value.
     * @param sortOrder order of tenant ASC/DESC.
     * @param sortBy    the attribute need to sort.
     * @return TenantSearchResult tenant's basic information list.
     * @throws UserStoreException if there is an error when listing the tenants.
     */
    default TenantSearchResult listTenants(Integer limit, Integer offset, String sortOrder, String sortBy,
                                           String filter) throws UserStoreException {

        throw new NotImplementedException(
                "listTenants operation is not implemented in: " + this.getClass());
    }

    /**
     * Gets a Tenant object.
     *
     * @param tenantUniqueID tenant unique identifier.
     * @return Tenant.
     * @throws UserStoreException if there is an error in tenant retrieval.
     */
    default Tenant getTenant(String tenantUniqueID) throws UserStoreException {

        throw new NotImplementedException(
                "getTenant operation is not implemented in: " + this.getClass());
    }

    /**
     * Activates a tenant.
     *
     * @param tenantUniqueID tenant unique identifier.
     * @throws UserStoreException if there is an error in tenant activation.
     */
    default void activateTenant(String tenantUniqueID) throws UserStoreException {

        throw new NotImplementedException(
                "activateTenant operation is not implemented in: " + this.getClass());
    }

    /**
     * Deactivates a tenant.
     *
     * @param tenantUniqueID tenant unique identifier.
     * @throws UserStoreException if there is an error in tenant de activation.
     */
    default void deactivateTenant(String tenantUniqueID) throws UserStoreException {

        throw new NotImplementedException(
                "deactivateTenant operation is not implemented in: " + this.getClass());
    }

    /**
     * Delete all tenant information related to tenant stored in UM tables.
     *
     * @param tenantId Id of the tenant
     * @throws UserStoreException
     */
    default void deleteTenantUMData(int tenantId) throws UserStoreException {

    };

    /**
     * Deletes a tenant from the system.
     *
     * @param tenantUniqueID tenant unique identifier.
     * @throws UserStoreException if there is an error in tenant deletion.
     */
    default void deleteTenant(String tenantUniqueID) throws UserStoreException {

        throw new NotImplementedException(
                "deleteTenant operation is not implemented in: " + this.getClass());
    }
}
