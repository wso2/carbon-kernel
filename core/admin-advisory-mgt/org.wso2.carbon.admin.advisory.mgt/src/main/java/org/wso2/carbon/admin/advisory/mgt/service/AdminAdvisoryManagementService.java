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

package org.wso2.carbon.admin.advisory.mgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.advisory.mgt.constants.AdminAdvisoryManagementConstants;
import org.wso2.carbon.admin.advisory.mgt.dto.AdminAdvisoryBannerDTO;
import org.wso2.carbon.admin.advisory.mgt.exception.AdminAdvisoryMgtException;
import org.wso2.carbon.admin.advisory.mgt.util.RegistryResourceConfig;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;

/**
 * This service is to configure the Admin Advisory Management functionality.
 */
public class AdminAdvisoryManagementService {

    protected static final Log LOG = LogFactory.getLog(AdminAdvisoryManagementService.class);
    private static final String ADMIN_ADVISORY_BANNER_PATH = "identity/config/adminAdvisoryBanner";
    private final RegistryResourceConfig registryResourceConfig = new RegistryResourceConfig();

    /**
     * This method is used to save the Admin advisory banner configurations which is specific to tenant.
     *
     * @param adminAdvisoryBanner Admin advisory banner to be saved.
     */
    public void saveAdminAdvisoryConfig(AdminAdvisoryBannerDTO adminAdvisoryBanner) throws AdminAdvisoryMgtException {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        Resource bannerResource = createAdminBannerRegistryResource(adminAdvisoryBanner);

        registryResourceConfig.putRegistryResource(bannerResource, ADMIN_ADVISORY_BANNER_PATH, tenantDomain);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Admin advisory banner configuration saved successfully for tenant: " + tenantDomain);
        }
    }

    /**
     * This method is used to load the tenant specific Admin advisory banner configurations.
     *
     * @return AdminAdvisoryBannerDTO object.
     */
    public AdminAdvisoryBannerDTO getAdminAdvisoryConfig() throws AdminAdvisoryMgtException {

        AdminAdvisoryBannerDTO adminAdvisoryBanner;
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        Resource registryResource = registryResourceConfig.getRegistryResource(ADMIN_ADVISORY_BANNER_PATH,
                tenantDomain);
        if (registryResource != null) {
            adminAdvisoryBanner = createAdminAdvisoryBannerDTO(registryResource);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Admin advisory banner configuration loaded successfully for tenant: " + tenantDomain);
            }
        } else {
            adminAdvisoryBanner = new AdminAdvisoryBannerDTO();
            adminAdvisoryBanner.setEnableBanner(AdminAdvisoryManagementConstants.ENABLE_BANNER_BY_DEFAULT);
            adminAdvisoryBanner.setBannerContent(AdminAdvisoryManagementConstants.DEFAULT_BANNER_CONTENT);
        }

        return adminAdvisoryBanner;
    }

    /**
     * This method is used to convert AdminAdvisoryBannerDTO to Resource object to be saved in registry.
     *
     * @return Resource object.
     */
    private Resource createAdminBannerRegistryResource(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO) {

        // Set resource properties.
        Resource bannerResource = new ResourceImpl();
        bannerResource.setProperty(AdminAdvisoryManagementConstants.ENABLE_BANNER,
                String.valueOf(adminAdvisoryBannerDTO.getEnableBanner()));
        bannerResource.setProperty(AdminAdvisoryManagementConstants.BANNER_CONTENT,
                String.valueOf(adminAdvisoryBannerDTO.getBannerContent()));

        return bannerResource;
    }

    /**
     * This method is used to convert Resource object to AdminAdvisoryBannerDTO to be saved in registry.
     *
     * @return AdminAdvisoryBannerDTO object.
     */
    private AdminAdvisoryBannerDTO createAdminAdvisoryBannerDTO(Resource bannerResource) {

        AdminAdvisoryBannerDTO adminAdvisoryBannerDTO = new AdminAdvisoryBannerDTO();
        String enableBanner = bannerResource.getProperty(AdminAdvisoryManagementConstants.ENABLE_BANNER);
        String content = bannerResource.getProperty(AdminAdvisoryManagementConstants.BANNER_CONTENT);
        adminAdvisoryBannerDTO.setEnableBanner(Boolean.parseBoolean(enableBanner));
        adminAdvisoryBannerDTO.setBannerContent(content);

        return adminAdvisoryBannerDTO;
    }
}
