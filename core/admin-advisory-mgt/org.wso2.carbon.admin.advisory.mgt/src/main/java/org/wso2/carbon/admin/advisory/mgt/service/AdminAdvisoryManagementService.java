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
import org.wso2.carbon.admin.advisory.mgt.dao.AdminAdvisoryBannerDAO;
import org.wso2.carbon.admin.advisory.mgt.dto.AdminAdvisoryBannerDTO;
import org.wso2.carbon.admin.advisory.mgt.exception.AdminAdvisoryMgtException;
import org.wso2.carbon.admin.advisory.mgt.internal.AdminAdvisoryManagementDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.util.Optional;

/**
 * This service is to configure the Admin Advisory Management functionality.
 */
public class AdminAdvisoryManagementService {

    private static final Log LOG = LogFactory.getLog(AdminAdvisoryManagementService.class);

    private static final Boolean ENABLE_BANNER_BY_DEFAULT = false;
    private static final String DEFAULT_BANNER_CONTENT = "Warning - unauthorized use of this tool is strictly " +
            "prohibited. All activities performed using this tool are logged and monitored.";

    /**
     * This method is used to save the Admin advisory banner configurations which is specific to tenant.
     *
     * @param adminAdvisoryBanner Admin advisory banner to be saved.
     */
    public void saveAdminAdvisoryConfig(AdminAdvisoryBannerDTO adminAdvisoryBanner) throws AdminAdvisoryMgtException {

        AdminAdvisoryBannerDAO adminAdvisoryBannerDAO =
                AdminAdvisoryManagementDataHolder.getInstance().getAdminAdvisoryBannerDAOService();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        adminAdvisoryBannerDAO.saveAdminAdvisoryConfig(adminAdvisoryBanner, tenantDomain);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Admin advisory banner configurations successfully stored in storage: " +
                    adminAdvisoryBannerDAO.getClass() + " for tenant: " + tenantDomain + ".");
        }
    }

    /**
     * This method is used to load the tenant specific Admin advisory banner configurations.
     *
     * @return AdminAdvisoryBannerDTO object.
     */
    public AdminAdvisoryBannerDTO getAdminAdvisoryConfig() throws AdminAdvisoryMgtException {

        AdminAdvisoryBannerDAO adminAdvisoryBannerDAO =
                AdminAdvisoryManagementDataHolder.getInstance().getAdminAdvisoryBannerDAOService();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        Optional<AdminAdvisoryBannerDTO> optionalAdminAdvisoryBanner =
                adminAdvisoryBannerDAO.loadAdminAdvisoryConfig(tenantDomain);

        if (optionalAdminAdvisoryBanner.isPresent()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Admin advisory banner configurations successfully loaded from storage: " +
                        adminAdvisoryBannerDAO.getClass() + " for tenant: " + tenantDomain + ".");
            }
            return optionalAdminAdvisoryBanner.get();
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Admin advisory banner configurations are not available in storage: " +
                        adminAdvisoryBannerDAO.getClass() +
                        ". Hence, default configurations will be used for tenant: " + tenantDomain + ".");
            }
            AdminAdvisoryBannerDTO adminAdvisoryBanner = new AdminAdvisoryBannerDTO();
            adminAdvisoryBanner.setEnableBanner(ENABLE_BANNER_BY_DEFAULT);
            adminAdvisoryBanner.setBannerContent(DEFAULT_BANNER_CONTENT);
            return adminAdvisoryBanner;
        }
    }
}
