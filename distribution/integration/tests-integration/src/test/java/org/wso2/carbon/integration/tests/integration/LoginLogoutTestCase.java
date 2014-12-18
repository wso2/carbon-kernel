/*
*Copyright (c) 2014​, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.integration.tests.integration;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfoAdminStub;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.common.utils.LoginLogoutUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Map;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * A test case which tests logging in & logging out of a Carbon core server
 */
public class LoginLogoutTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(LoginLogoutTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
    }


    @Test(groups = {"carbon.core"}, description = "Tests the server login functionality")
    public void testLoginWithBasicAuth() throws Exception {

        boolean loginStats = util.loginWithBasicAuth(
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword().toCharArray(),
                contextUrls.getBackEndUrl());

        assertTrue(loginStats, "Authentication failed !!");
    }

    @Test(groups = {"carbon.core"}, description = "Tests the server login functionality",
          dependsOnMethods = {"testLoginWithBasicAuth"})
    public void testLogin() throws Exception {

        String sessionCookie = util.login(
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword().toCharArray(),
                contextUrls.getBackEndUrl());

        assertNotNull(sessionCookie, "session cookies cannot be null - login failed");
    }


    @Test(groups = {"carbon.core"}, dependsOnMethods = {"testLogin"}, description =
            "Tests the server logout functionality")
    public void testLogout() throws Exception {
        util.logout(contextUrls.getBackEndUrl());
    }

    @Test(groups = {"carbon.core"}, description = "Checks whether remember me data is correctly processed.",
          dependsOnMethods = "testLogout")
    public void loginWithRememberMe() throws Exception {


        LoggedUserInfoAdminStub stub = null;
        try {
            stub = new LoggedUserInfoAdminStub(contextUrls.getBackEndUrl() + "LoggedUserInfoAdmin");
        } catch (AxisFault axisFault) {
            log.error("Unable to create LoggedUserInfoAdmin stub", axisFault);
            fail("Unable to create LoggedUserInfoAdmin stub, with remember me login");
        }

        ServiceClient client = stub._getServiceClient();

        CarbonUtils.setBasicAccessSecurityHeaders(
                automationContext.getContextTenant().getContextUser().getUserName(),
                automationContext.getContextTenant().getContextUser().getPassword(),
                true, client);


        try {
            stub.getUserInfo();
        } catch (Exception e) {
            log.error("Unable to retrieve data from LoggedUserInfoAdmin", e);
            fail("Unable to create LoggedUserInfoAdmin stub, with remember me login");
        }

        checkRememberMeData(client);

        client.cleanup();
        util.logout(contextUrls.getBackEndUrl());
    }

    private void checkRememberMeData(ServiceClient serviceClient) {

        OperationContext operationContext = serviceClient.getLastOperationContext();
        MessageContext inMessageContext = null;
        try {
            inMessageContext = operationContext.getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
        } catch (AxisFault axisFault) {
            String msg = "Unable to retrieve IN message context from operation context of service client";
            log.error(msg, axisFault);
            fail(msg);
        }

        Map transportHeaders = (Map) inMessageContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        String cookieValue = (String) transportHeaders.get("RememberMeCookieValue");
        String cookieAge = (String) transportHeaders.get("RememberMeCookieAge");

        assertNotNull(cookieValue, "Cookie value cannot be null for a remember me request");
        assertNotNull(cookieAge, "Cookie age cannot be null for a remember me request");

    }
}

