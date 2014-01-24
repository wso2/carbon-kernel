/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.tests;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.core.commons.stub.loggeduserinfo.LoggedUserInfoAdminStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.Map;

/**
 * A test case which tests logging in & logging out of a Carbon core server
 */
public class LoginLogoutTestCase {

    private static final Log log = LogFactory.getLog(LoginLogoutTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @Deprecated
    @Test(groups = {"carbon.core"},
          description = "Tests the server login functionality", dependsOnMethods = {"loginWithBasicAuth"})
    public void login() throws Exception {
        util.login();
    }

    @Test(groups = {"carbon.core"},
          description = "Tests the server login functionality")
    public void loginWithBasicAuth() throws Exception {
        boolean b = util.loginWithBasicAuth();
        Assert.assertTrue(b, "Authentication failed !!");
    }

    @Test(groups = {"carbon.core"}, dependsOnMethods = {"loginWithBasicAuth"},
          description = "Tests the server logout functionality")
    public void logout() throws Exception {
        util.logout();
    }

    @Test(groups = {"carbon.core"},
            description = "Checks whether remember me data is correctly processed.")
    public void loginWithRememberMe() throws Exception {

        int portOffset = 0;

        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset);

        String authenticationServiceURL =
                "https://localhost:" + (Integer.parseInt(FrameworkSettings.HTTPS_PORT) + portOffset) +
                        "/services/";


        LoggedUserInfoAdminStub stub = null;
        try {
            stub = new LoggedUserInfoAdminStub(authenticationServiceURL + "LoggedUserInfoAdmin");
        } catch (AxisFault axisFault) {
            log.error("Unable to create LoggedUserInfoAdmin stub", axisFault);
            Assert.fail("Unable to create LoggedUserInfoAdmin stub, with remember me login");
        }

        ServiceClient client = stub._getServiceClient();

        CarbonUtils.setBasicAccessSecurityHeaders(FrameworkSettings.USER_NAME, FrameworkSettings.PASSWORD, true,
                client);


        try {
            stub.getUserInfo();
        } catch (Exception e) {
            log.error("Unable to retrieve data from LoggedUserInfoAdmin", e);
            Assert.fail("Unable to create LoggedUserInfoAdmin stub, with remember me login");
        }

        checkRememberMeData(client);

        client.cleanup();
        util.logout();
    }

    private void checkRememberMeData(ServiceClient serviceClient) {

        OperationContext operationContext = serviceClient.getLastOperationContext();
        MessageContext inMessageContext = null;
        try {
            inMessageContext = operationContext.getMessageContext(WSDL2Constants.MESSAGE_LABEL_IN);
        } catch (AxisFault axisFault) {
            String msg = "Unable to retrieve IN message context from operation context of service client";
            log.error(msg, axisFault);
            Assert.fail(msg);
        }

        Map transportHeaders = (Map) inMessageContext.getProperty(MessageContext.TRANSPORT_HEADERS);

        String cookieValue = (String) transportHeaders.get("RememberMeCookieValue");
        String cookieAge = (String) transportHeaders.get("RememberMeCookieAge");

        Assert.assertNotNull(cookieValue, "Cookie value cannot be null for a remember me request");
        Assert.assertNotNull(cookieAge, "Cookie age cannot be null for a remember me request");

    }
}
