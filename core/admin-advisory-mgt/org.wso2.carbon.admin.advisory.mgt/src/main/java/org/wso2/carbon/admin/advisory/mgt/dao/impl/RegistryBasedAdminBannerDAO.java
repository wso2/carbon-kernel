/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
package org.wso2.carbon.admin.advisory.mgt.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.advisory.mgt.dao.AdminAdvisoryBannerDAO;
import org.wso2.carbon.admin.advisory.mgt.dto.AdminAdvisoryBannerDTO;
import org.wso2.carbon.admin.advisory.mgt.exception.AdminAdvisoryMgtException;
import org.wso2.carbon.admin.advisory.mgt.internal.AdminAdvisoryManagementDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Optional;

/**
 * This class is used to manage storage of the Admin Advisory Banner configurations in the registry.
 */
public class RegistryBasedAdminBannerDAO implements AdminAdvisoryBannerDAO {

    private static final Log LOG = LogFactory.getLog(RegistryBasedAdminBannerDAO.class);

    private static final String ADMIN_ADVISORY_BANNER_PATH = "identity/config/adminAdvisoryBanner";
    private static final String ENABLE_BANNER = "enableBanner";
    private static final String BANNER_CONTENT = "bannerContent";

    @Override
    public void saveAdminAdvisoryConfig(AdminAdvisoryBannerDTO adminAdvisoryBanner, String tenantDomain)
            throws AdminAdvisoryMgtException {

        Resource bannerResource = createAdminBannerRegistryResource(adminAdvisoryBanner);
        putRegistryResource(bannerResource, ADMIN_ADVISORY_BANNER_PATH, tenantDomain);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Admin advisory banner configuration saved successfully in registry for tenant: " + tenantDomain);
        }
    }

    @Override
    public Optional<AdminAdvisoryBannerDTO> loadAdminAdvisoryConfig(String tenantDomain) throws AdminAdvisoryMgtException {

        Resource resource = getRegistryResource(ADMIN_ADVISORY_BANNER_PATH, tenantDomain);
        if (resource == null) {
            return Optional.empty();
        }

        AdminAdvisoryBannerDTO adminAdvisoryBanner = createAdminAdvisoryBannerDTO(resource);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Admin advisory banner configuration loaded successfully from registry for tenant: " +
                    tenantDomain);
        }
        return Optional.of(adminAdvisoryBanner);
    }

    /**
     * This method is used to convert AdminAdvisoryBannerDTO to Resource object to be saved in registry.
     *
     * @return Resource object.
     */
    private Resource createAdminBannerRegistryResource(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO) {

        Resource bannerResource = new ResourceImpl();
        bannerResource.setProperty(ENABLE_BANNER, String.valueOf(adminAdvisoryBannerDTO.getEnableBanner()));
        bannerResource.setProperty(BANNER_CONTENT, String.valueOf(adminAdvisoryBannerDTO.getBannerContent()));
        return bannerResource;
    }

    /**
     * This method is used to convert Resource object to AdminAdvisoryBannerDTO to be saved in registry.
     *
     * @return AdminAdvisoryBannerDTO object.
     */
    private AdminAdvisoryBannerDTO createAdminAdvisoryBannerDTO(Resource bannerResource) {

        String enableBanner = bannerResource.getProperty(ENABLE_BANNER);
        String content = bannerResource.getProperty(BANNER_CONTENT);

        AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = new AdminAdvisoryBannerDTO();
        adminAdvisoryBannerDTO.setEnableBanner(Boolean.parseBoolean(enableBanner));
        adminAdvisoryBannerDTO.setBannerContent(content);
        return adminAdvisoryBannerDTO;
    }

    /**
     * This method is used to save the tenant specific registry resource.
     *
     * @param identityResource Resource to be saved.
     * @param path             Path of the resource.
     * @param tenantDomain     Tenant domain.
     * @throws AdminAdvisoryMgtException Error while saving the registry resource.
     */
    private void putRegistryResource(Resource identityResource, String path, String tenantDomain)
            throws AdminAdvisoryMgtException {

        try {
            startTenantFlow(tenantDomain);
            Registry registry = getConfigSystemRegistry(tenantDomain);
            registry.put(path, identityResource);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("Resource persisted at %s in %s tenant registry.", path, tenantDomain));
            }
        } catch (RegistryException | UserStoreException e) {
            String errorMsg = String.format("Error persisting registry resource of %s tenant at %s", tenantDomain, path);
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
    private Resource getRegistryResource(String path, String tenantDomain) throws AdminAdvisoryMgtException {

        Resource resource = null;
        try {
            startTenantFlow(tenantDomain);
            Registry registry = getConfigSystemRegistry(tenantDomain);
            if (registry.resourceExists(path)) {
                resource = registry.get(path);
            }
        } catch (RegistryException | UserStoreException e) {
            String errorMsg =
                    String.format("Error retrieving registry resource from %s for tenant %s.", path, tenantDomain);
            throw new AdminAdvisoryMgtException(errorMsg, e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
        return resource;
    }

    private static Registry getConfigSystemRegistry(String tenantDomain)
            throws UserStoreException, RegistryException {

        int tenantId = AdminAdvisoryManagementDataHolder.getInstance().getRealmService().getTenantManager()
                .getTenantId(tenantDomain);
        return AdminAdvisoryManagementDataHolder.getInstance().getRegistryService().getConfigSystemRegistry(tenantId);
    }

    private void startTenantFlow(String tenantDomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
    }
}
