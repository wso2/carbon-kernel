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

import static org.testng.Assert.assertTrue;


/**
 * Tests for gracefully & forcefully restarting Carbon servers
 */
public class ServerRestartTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(ServerRestartTestCase.class);
    private static final long TIMEOUT = 5 * 60000;
    private static final long TIMEOUT_ISPORTOPEN = 30 * 1000;

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {
        super.init(TestUserMode.SUPER_TENANT_ADMIN);
    }

    @Test(groups = {"carbon.core.graceful.restart.test"})
    public void testGracefulServerRestart() throws Exception {

        log.info("Testing server graceful restart...");

        int portOffset = 121;

        HashMap<String, String> startUpParameterMap = new HashMap<String, String>();
        startUpParameterMap.put("-DportOffset", String.valueOf(portOffset));


        try {
            if (!CarbonTestServerManager.isServerRunning()) {
                CarbonTestServerManager.start(startUpParameterMap);
            } else {
                CarbonTestServerManager.stop();
                CarbonTestServerManager.start(startUpParameterMap);
            }
            int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT)
                            + portOffset;
            ClientConnectionUtil.waitForPort(httpsPort, automationContext.getDefaultInstance().
                    getHosts().get("default"));
            ServerAdminClient serverAdmin = new ServerAdminClient("https://" +
                                                                  automationContext.getInstance().getHosts().get("default") + ":" +
                                                                  httpsPort + "/services/ServerAdmin/",
                                                                  automationContext.getSuperTenant().getTenantAdmin().getUserName(), automationContext.
                    getSuperTenant().getTenantAdmin().getPassword());
            assertTrue(serverAdmin.restartGracefully(), "Server gracefully restart failure");
            long startTime = System.currentTimeMillis();
            while (ClientConnectionUtil.isPortOpen(httpsPort) &&
                   System.currentTimeMillis() - startTime < TIMEOUT_ISPORTOPEN) {
                Thread.sleep(1000);
            }
            Thread.sleep(15000); //This sleep should be there, since we have to give some time for
            //the server to initiate restart. Otherwise, "waitForPort" call
            //might happen before server initiate restart.
            ClientConnectionUtil.waitForPort(httpsPort, TIMEOUT, true, automationContext.getInstance().
                    getHosts().get("default"));
            Thread.sleep(10000);
        } finally {
            CarbonTestServerManager.stop();


        }
    }

    @Test(groups = {"carbon.core.restart.test"})
    public void testServerRestart() throws Exception {

        log.info("Testing server restart...");

        int portOffset = 122;

        HashMap<String, String> startUpParameterMap = new HashMap<String, String>();
        startUpParameterMap.put("-DportOffset", String.valueOf(portOffset));

        try {
            if (!CarbonTestServerManager.isServerRunning()) {
                CarbonTestServerManager.start(startUpParameterMap);
            } else {
                CarbonTestServerManager.stop();
                CarbonTestServerManager.start(startUpParameterMap);
            }


            int httpsPort = 9443 + portOffset;
            ClientConnectionUtil.waitForPort(httpsPort, automationContext.getDefaultInstance().
                    getHosts().get("default"));
            ServerAdminClient serverAdmin = new ServerAdminClient("https://" + automationContext.getDefaultInstance().
                    getHosts().get("default") + ":" + httpsPort + "/services/ServerAdmin/",
                                                                  automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                                                  automationContext.getSuperTenant().getTenantAdmin().getPassword());
            assertTrue(serverAdmin.restart(), "Server restart failure");
            long startTime = System.currentTimeMillis();
            while (ClientConnectionUtil.isPortOpen(httpsPort) &&
                   System.currentTimeMillis() - startTime < TIMEOUT_ISPORTOPEN) {
                Thread.sleep(1000);
            }
            Thread.sleep(15000);
            ClientConnectionUtil.waitForPort(httpsPort, TIMEOUT, true, automationContext.getInstance().
                    getHosts().get("default"));
            Thread.sleep(10000);
        } finally {

            CarbonTestServerManager.stop();

        }
    }

}
