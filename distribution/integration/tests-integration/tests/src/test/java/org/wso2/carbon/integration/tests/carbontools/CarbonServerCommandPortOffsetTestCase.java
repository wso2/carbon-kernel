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
import org.wso2.carbon.integration.tests.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationConstants;
import org.wso2.carbon.integration.tests.common.utils.LoginLogoutUtil;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.util.HashMap;

import static org.testng.Assert.assertTrue;

/**
 * Provides test commands -DportOffset, -DhttpsPort and -DhttpPort when starting the carbon server
 */
public class CarbonServerCommandPortOffsetTestCase extends CarbonIntegrationBaseTest {

    private HashMap<String, String> serverPropertyMap;
    private MultipleServersManager manager = new MultipleServersManager();
    private int portOffset = 1;
    private AutomationContext automationContextOfInstance002;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        automationContextOfInstance002 =
                new AutomationContext(CarbonIntegrationConstants.PRODUCT_GROUP,
                                      CarbonIntegrationConstants.INSTANCE,
                                      ContextXpathConstants.SUPER_TENANT,
                                      ContextXpathConstants.ADMIN);
    }

    @Test(groups = {"carbon.core"}, description = "Server portOffset test")
    public void testCommandPortOffset() throws Exception {
        serverPropertyMap = new HashMap<String, String>();
        serverPropertyMap.put("-DportOffset", Integer.toString(portOffset));

        try {
            CarbonTestServerManager.start(serverPropertyMap);
            assertTrue(CarbonCommandToolsUtil.isServerStartedUp(automationContextOfInstance002,
                                                                portOffset), "Unsuccessful login");
        } finally {
            CarbonTestServerManager.stop();
        }
    }

    // Disabled this test because of bug jira:https://wso2.org/jira/browse/CARBON-15118
    @Test(groups = {"carbon.core"}, description = "Test Command line server startup parameters " +
                                              " -DhttpPort and -DhttpsPort test", enabled = false)
    public void testCommandDhttpDhttpsPort() throws Exception {

        serverPropertyMap = new HashMap<String, String>();
        boolean isPortsOccupied = false;

        String httpsPort = automationContextOfInstance002.getInstance().getPorts().get("https");
        String httpPort = automationContextOfInstance002.getInstance().getPorts().get("http");

        serverPropertyMap.put("-DhttpsPort", httpsPort);
        serverPropertyMap.put("-DhttpPort", httpPort);

        try {
            CarbonTestServerManager.start(serverPropertyMap);

            LoginLogoutUtil loginLogoutUtil = new LoginLogoutUtil();
            loginLogoutUtil.login(automationContext.getSuperTenant().getTenantAdmin().getUserName(),
                                  automationContext.getSuperTenant().getTenantAdmin().getPassword().toCharArray(),
                                  automationContext.getContextUrls().getBackEndUrl());

            boolean isFoundHttpPort = CarbonCommandToolsUtil.
                    findMultipleStringsInLog(new String[]{"HTTP port", httpPort});

            boolean isFoundHttpsPort = CarbonCommandToolsUtil.
                    findMultipleStringsInLog(new String[]{"HTTPS port", httpsPort});

            if (isFoundHttpPort && isFoundHttpsPort) {
                isPortsOccupied = true;
            }
            assertTrue(isPortsOccupied, "Couldn't start the server on specified http & https ports");
        } finally {
            CarbonTestServerManager.stop();
        }
    }
}
