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

package org.wso2.carbon.admin.advisory.mgt.ui;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.admin.advisory.mgt.stub.AdminAdvisoryManagementServiceStub;
import org.wso2.carbon.admin.advisory.mgt.stub.dto.AdminAdvisoryBannerDTO;

/**
 * This class is used to call the AdminAdvisoryManagementService.
 */
public class AdminAdvisoryBannerClient {

    protected AdminAdvisoryManagementServiceStub stub;
    private static final String ADMIN_ADVISORY_MANAGEMENT_SERVICE = "AdminAdvisoryManagementService";

    /**
     * AdminAdvisoryBannerClient constructor.
     *
     * @param url           URL.
     * @param configContext Configuration context.
     * @throws AxisFault Error while creating AdminAdvisoryManagementServiceStub instance.
     */
    public AdminAdvisoryBannerClient(String url, ConfigurationContext configContext) throws AxisFault {

        try {
            stub = new AdminAdvisoryManagementServiceStub(configContext, url
                    + ADMIN_ADVISORY_MANAGEMENT_SERVICE);
        } catch (AxisFault e) {
            handleException(e.getMessage(), e);
        }
    }

    /**
     * AdminAdvisoryBannerClient constructor.
     *
     * @param cookie        Cookie.
     * @param url           URL.
     * @param configContext Configuration context.
     * @throws AxisFault Error while creating AdminAdvisoryManagementServiceStub instance.
     */
    public AdminAdvisoryBannerClient(String cookie, String url, ConfigurationContext configContext) throws AxisFault {

        try {
            stub = new AdminAdvisoryManagementServiceStub(configContext, url
                    + ADMIN_ADVISORY_MANAGEMENT_SERVICE);
            ServiceClient client = stub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault e) {
            handleException(e.getMessage(), e);
        }
    }

    /**
     * Saves the banner configuration.
     *
     * @param adminAdvisoryBannerDTO AdminAdvisoryBannerDTO.
     * @throws AxisFault Error while saving the banner configuration.
     */
    public void saveBannerConfig(AdminAdvisoryBannerDTO adminAdvisoryBannerDTO) throws AxisFault {

        try {
            stub.saveAdminAdvisoryConfig(adminAdvisoryBannerDTO);
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
    }

    /**
     * Loads the banner configuration.
     *
     * @return adminAdvisoryBannerDTO AdminAdvisoryBannerDTO.
     * @throws AxisFault Error while loading the banner configuration.
     */
    public AdminAdvisoryBannerDTO loadBannerConfig() throws AxisFault {

        try {
            return stub.getAdminAdvisoryConfig();
        } catch (Exception e) {
            handleException(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Handle exception.
     *
     * @throws AxisFault To handle the exception.
     */
    private void handleException(String msg, Exception e) throws AxisFault {

        throw new AxisFault(msg, e);
    }
}
