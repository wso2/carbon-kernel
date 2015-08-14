/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;
import org.wso2.carbon.server.admin.ui.ServerAdminClient;

import java.util.HashMap;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for gracefully & forcefully shutting down Carbon servers
 */
public class ServerShutdownTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(ServerShutdownTestCase.class);

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    @Test(groups = {"carbon.core.graceful.shutdown.test"})
    public void testGracefulServerShutdown() throws Exception {

        log.info("Testing graceful server shutdown...");

        int portOffset = 35;
        HashMap<String, String> startUpParameterMap = new HashMap<String, String>();
        startUpParameterMap.put("-DportOffset", String.valueOf(portOffset));
        try {
            if (!CarbonTestServerManager.isServerRunning()) {
                CarbonTestServerManager.start(startUpParameterMap);
            } else {
                CarbonTestServerManager.stop();
                CarbonTestServerManager.start(startUpParameterMap);
            }

            int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset;
            ClientConnectionUtil.waitForPort(httpsPort, automationContext.getInstance().getHosts().get("default"));

            ServerAdminClient serverAdmin = new ServerAdminClient("https://" +
                                                                  automationContext.getInstance().getHosts().get("default") + ":" +
                                                                  (httpsPort) + "/services/ServerAdmin/",
                                                                  automationContext.getContextTenant().getContextUser().getUserName(),
                                                                  automationContext.getContextTenant().getContextUser().getPassword());

            serverAdmin.shutdownGracefully();

            Thread.sleep(20000);

            assertFalse(ClientConnectionUtil.isPortOpen(httpsPort),
                        "Port " + httpsPort + " shouldn't be open when the server is gracefully shutting down");

            int httpPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTP_PORT) + portOffset;

            assertFalse(ClientConnectionUtil.isPortOpen(httpPort),
                        "Port " + httpPort + " shouldn't be open when the server is gracefully shutting down");
        }finally {
            CarbonTestServerManager.stop();
        }
    }

    @Test(groups = {"carbon.core.shutdown.test"})
    public void testServerShutdown() throws Exception {

        log.info("Testing server shutdown...");

        int portOffset = 37;
        HashMap<String, String> startUpParameterMap = new HashMap<String, String>();
        startUpParameterMap.put("-DportOffset", String.valueOf(portOffset));

        try {
            CarbonTestServerManager.start(startUpParameterMap);

            int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset;
            ClientConnectionUtil.waitForPort(httpsPort, automationContext.getInstance().getHosts().get("default"));
            ServerAdminClient serverAdmin = new ServerAdminClient("https://"
                                                                  + automationContext.getInstance().getHosts().get("default") + ":" +
                                                                  (httpsPort) + "/services/ServerAdmin/",
                                                                  automationContext.getContextTenant().getContextUser().getUserName(),
                                                                  automationContext.getContextTenant().getContextUser().getPassword());

            assertTrue(serverAdmin.shutdown(), "Server shout down failure");
            Thread.sleep(20000);
            assertFalse(ClientConnectionUtil.isPortOpen(httpsPort),
                        "Port " + httpsPort + " shouldn't be open when the server is shutting down");
            int httpPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTP_PORT) + portOffset;
            assertFalse(ClientConnectionUtil.isPortOpen(httpPort),
                        "Port " + httpPort + " shouldn't be open when the server is shutting down");
        }finally {
            CarbonTestServerManager.stop();
        }
    }

}
