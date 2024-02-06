/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.admin.advisory.mgt.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.advisory.mgt.exception.AdminAdvisoryMgtException;
import org.wso2.carbon.admin.advisory.mgt.internal.AdminAdvisoryManagementDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * This class is used to persist and retrieve registry resources.
 */
public class RegistryResourceConfig {

    protected static final Log LOG = LogFactory.getLog(RegistryResourceConfig.class);
    private static final RegistryService registryService =
            AdminAdvisoryManagementDataHolder.getInstance().getRegistryService();
    private static final RealmService realmService =
            AdminAdvisoryManagementDataHolder.getInstance().getRealmService();
    private static final String MSG_RESOURCE_PERSIST = "Resource persisted at %s in %s tenant registry.";
    private static final String ERROR_PERSIST_RESOURCE = "Error persisting registry resource of %s tenant at %s";
    private static final String ERROR_GET_RESOURCE = "Error retrieving registry resource from %s for tenant %s.";

    /**
     * This method is used to save the tenant specific registry resource.
     *
     * @param identityResource Resource to be saved.
     * @param path             Path of the resource.
     * @param tenantDomain     Tenant domain.
     * @throws AdminAdvisoryMgtException Error while saving the registry resource.
     */
    public void putRegistryResource(Resource identityResource, String path, String tenantDomain)
            throws AdminAdvisoryMgtException {

        startTenantFlow(tenantDomain);
        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            Registry registry = registryService.getConfigSystemRegistry(tenantId);
            registry.put(path, identityResource);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format(MSG_RESOURCE_PERSIST, path, tenantDomain));
            }
        } catch (RegistryException | UserStoreException e) {
            String errorMsg = String.format(ERROR_PERSIST_RESOURCE, tenantDomain, path);
            throw new AdminAdvisoryMgtException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * This method is used to get the tenant specific registry resource.
     *
     * @param path         Path of the resource.
     * @param tenantDomain Tenant domain.
     * @return Resource from the registry.
     * @throws AdminAdvisoryMgtException Error while saving the registry resource.
     */
    public Resource getRegistryResource(String path, String tenantDomain) throws AdminAdvisoryMgtException {

        startTenantFlow(tenantDomain);
        Resource resource = null;
        try {
            int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            Registry registry = registryService.getConfigSystemRegistry(tenantId);

            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            }
        } catch (RegistryException | UserStoreException e) {
            String errorMsg = String.format(ERROR_GET_RESOURCE, path, tenantDomain);
            throw new AdminAdvisoryMgtException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return resource;
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }

}
