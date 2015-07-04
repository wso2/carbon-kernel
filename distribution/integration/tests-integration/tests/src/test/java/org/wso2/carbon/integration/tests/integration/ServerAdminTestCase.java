/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.tests.integration;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.LoginLogoutUtil;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;

import static org.testng.Assert.*;

/**
 * Test case which tests ServerAdmin functionality
 */
public class ServerAdminTestCase extends CarbonIntegrationBaseTest {

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String host;
    private String userName;
    private String password;
    private String backEndURL;

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
        host = automationContext.getDefaultInstance().getHosts().get("default");
        userName = automationContext.getContextTenant().getContextUser().getUserName();
        password = automationContext.getContextTenant().getContextUser().getPassword();
        backEndURL = contextUrls.getBackEndUrl();
    }

    @Test(groups = {"carbon.core"}, threadPoolSize = 10, invocationCount = 10,
            description = "Test server information retrieval from the ServerAdmin service")
    public void testRetrieveServerInfo() throws Exception {
        ServerAdminClient serverAdmin = new ServerAdminClient
                ("https://" + host + ":" + FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT + "/services/ServerAdmin/",
                        userName, password);
        assertNotNull(serverAdmin.getServerData(), "Carbon server data cannot be null");
    }

    @Test(groups = {"carbon.core"})
    public void testInvalidRemoteAddress() {

        // This should throw an exception
        try {
            util.login(userName, password.toCharArray(), backEndURL + "invalid");
            fail("Should not be able to login");
        } catch (Exception e) {
            assertTrue(true);
        }
    }
}
