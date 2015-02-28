/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
package org.wso2.carbon.integration.tests.carbontools;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.integration.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.common.utils.LoginLogoutUtil;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.util.HashMap;

import static org.testng.Assert.assertTrue;

/**
 * This class is for test commands -DportOffset, -DhttpsPort and -DhttpPort when starting the carbon server
 */
public class CarbonServerCommandPortOffsetTestCase extends CarbonIntegrationBaseTest {

    private HashMap<String, String> serverPropertyMap;
    private AutomationContext autoCtx;
    private MultipleServersManager manager = new MultipleServersManager();
    private int portOffset = 1;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
    }

    @Test(groups = {"wso2.as"}, description = "Server portOffset test")
    public void testCommandPortOffset()
            throws Exception {
        serverPropertyMap = new HashMap<String, String>();
        AutomationContext context =
                new AutomationContext("CARBON", "carbon002",
                                      ContextXpathConstants.SUPER_TENANT,
                                      ContextXpathConstants.ADMIN);
        serverPropertyMap.put("-DportOffset", Integer.toString(portOffset));
        autoCtx = new AutomationContext();
        CarbonTestServerManager server =
                new CarbonTestServerManager(autoCtx, System.getProperty("carbon.zip"), serverPropertyMap);
        try {
            manager.startServers(server);
            assertTrue(CarbonCommandToolsUtil.isServerStartedUp(context, portOffset), "Unsuccessful login");
        } finally {
            manager.stopAllServers();
        }


    }

    // Disabled this test because of bug jira:https://wso2.org/jira/browse/CARBON-15118
    @Test(groups = {"wso2.as"}, description = "Server DhttpPort and DhttpsPort test", enabled = false)
    public void testCommandDhttpDhttpsPort() throws Exception {
        serverPropertyMap = new HashMap<String, String>();
        boolean isPortsOccupied = false;
        AutomationContext context =
                new AutomationContext("CARBON", "carbon002",
                                      ContextXpathConstants.SUPER_TENANT,
                                      ContextXpathConstants.ADMIN);
        serverPropertyMap.put("-DhttpsPort", "9444");
        serverPropertyMap.put("-DhttpPort", "9764");
        autoCtx = new AutomationContext();

        CarbonTestServerManager server = new CarbonTestServerManager
                (autoCtx, System.getProperty("carbon.zip"), serverPropertyMap);

        try {
            manager.startServers(server);
            LoginLogoutUtil loginLogoutUtil = new LoginLogoutUtil(context);
            loginLogoutUtil.login();

            boolean isFoundHttpPort = CarbonCommandToolsUtil.
                    findMultipleStringsInLog(new String[]{"HTTP port", "9764"});

            boolean isFoundHttpsPort = CarbonCommandToolsUtil.
                    findMultipleStringsInLog(new String[]{"HTTPS port", "9444"});

            if (isFoundHttpPort && isFoundHttpsPort) {
                isPortsOccupied = true;
            }
            assertTrue(isPortsOccupied, "Couldn't start the server on specified http & https ports");
        } finally {
            manager.stopAllServers();
        }
    }
}
