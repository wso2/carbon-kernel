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

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.configurations.UrlGenerationUtil;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.LoginLogoutUtil;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;
import org.wso2.carbon.utils.ServerConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.testng.Assert.*;

/**
 * Test case which tests ServerAdmin functionality
 */
public class ServerAdminTestCase extends CarbonIntegrationBaseTest {
    private static final Log log = LogFactory.getLog(ServerAdminTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private String host;
    private String userName;
    private String password;
    private String backEndURL;
    private static final long TIMEOUT = 5 * 60000;
    private static final long PORT_OPEN_TIMEOUT = 2 * 60000;

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

    @Test(groups = {"carbon.core"})
    public void testServerStateChangeErrorScenario() throws Exception {
        applyConfigChange();
        restartServer();
        log.debug("Current carbon home : " + System.getProperty(ServerConstants.CARBON_HOME));
        String sessionCookie = util.login(userName, password.toCharArray(), backEndURL);
        log.debug("Logged-in cookie : " + sessionCookie);
        String url = UrlGenerationUtil.getLoginURL(automationContext.getDefaultInstance()) +
                "server-admin/proxy_ajaxprocessor.jsp?action=shutdown";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Cookie", sessionCookie);
        int responseCode = con.getResponseCode();
        assertEquals(responseCode, HttpStatus.SC_FORBIDDEN);
    }

    private void applyConfigChange() throws IOException {
        Path sourcePath = Paths.get(TestConfigurationProvider.getResourceLocation(), "serveradmin");
        Path targetPath = Paths.get(System.getProperty(ServerConstants.CARBON_HOME), "repository", "conf");
        Files.copy(Paths.get(sourcePath.toString(), "carbon.xml"), Paths.get(targetPath.toString(), "carbon.xml"),
                StandardCopyOption.REPLACE_EXISTING);
        log.debug("Replaced carbon.xml at : " + Paths.get(targetPath.toString(), "carbon.xml").toString());
        Files.copy(Paths.get(sourcePath.toString(), "catalina-server.xml"), Paths.get(targetPath.toString(),
                "tomcat", "catalina-server.xml"), StandardCopyOption.REPLACE_EXISTING);
        log.debug("Replaced catalina-server.xml at : " + Paths.get(targetPath.toString(),
                "tomcat", "catalina-server.xml"));
    }

    private void restartServer() throws Exception {
        ServerAdminClient serverAdmin = new ServerAdminClient("https://" + host + ":" +
                FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT + "/services/ServerAdmin/", userName, password);
        assertTrue(serverAdmin.restart(), "Server restart failure");
        int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT);
        long startTime = System.currentTimeMillis();
        while (ClientConnectionUtil.isPortOpen(httpsPort) &&
                (System.currentTimeMillis() - startTime < PORT_OPEN_TIMEOUT)) {
            Thread.sleep(1000);
        }
        Thread.sleep(15000);
        ClientConnectionUtil.waitForPort(httpsPort, TIMEOUT, true, automationContext.getDefaultInstance().
                getHosts().get("default"));
        Thread.sleep(10000);
    }
}
