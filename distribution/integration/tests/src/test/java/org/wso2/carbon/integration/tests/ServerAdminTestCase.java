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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;

import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Test case which tests ServerAdmin functionality
 */
public class ServerAdminTestCase {
    private static final Log log = LogFactory.getLog(ServerAdminTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String sessionCookie;

     /**
     * @deprecated This is no longer required. Now we can talk to admin services with basic auth.
     * @throws java.lang.Exception If an error occurred.
     */
    @Deprecated
    public void login() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        sessionCookie = util.login();
    }

    @Test(groups = {"carbon.core"}, threadPoolSize = 10, invocationCount = 10,
            description = "Test server information retrieval from the ServerAdmin service")
    public void testRetrieveServerInfo() throws Exception {
        ServerAdminClient serverAdmin = LoginLogoutUtil.getServerAdminClient(0);
        assertNotNull(serverAdmin.getServerData(), "Carbon server data cannot be null");
    }

    @Test(groups = {"carbon.core"})
    public void testInvalidRemoteAddress() {
        ClientConnectionUtil.waitForPort(9443);

        // This should throw an exception
        try {
            sessionCookie = util.login("127.0.0.1\n[2012-03-13 00:56:13,923]  " +
                    "INFO {org.wso2.carbon.core.services.util.CarbonAuthenticationUtil} -  " +
                    "'admin' logged in at [2012-03-13 00:56:13,0923] from IP address 127.0.0.1");
            Assert.fail("Should not be able to login");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }


}
