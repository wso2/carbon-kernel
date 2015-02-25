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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.integration.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationConstants;
import org.wso2.carbon.integration.common.utils.LoginLogoutUtil;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.io.File;
import java.util.HashMap;

import static org.testng.Assert.assertTrue;

/**
 * This class is to test change MySQL user password using chpasswd.sh/chpasswd.bat
 */
public class ChangeUserPasswordMySQLDBTestCase extends CarbonIntegrationBaseTest {


    private static final Log log = LogFactory.getLog(ChangeUserPasswordMySQLDBTestCase.class);
    private LoginLogoutUtil loginLogoutUtil;
    private boolean scriptRunStatus;
    private AutomationContext context;
    private int portOffset = 1;
    private HashMap<String, String> serverPropertyMap = new HashMap<String, String>();
    private MultipleServersManager manager = new MultipleServersManager();
    private String CARBON_HOME = null;
    private static String MYSQL_DB_URL ;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        context = new AutomationContext("CARBON", "carbon002",
                                        ContextXpathConstants.SUPER_TENANT,
                                        ContextXpathConstants.SUPER_ADMIN);
        loginLogoutUtil = new LoginLogoutUtil(context);

        MYSQL_DB_URL = context.getConfigurationValue(String.format(CarbonIntegrationConstants.DB_URL, "MySQL"));
    }


    @Test(groups = "wso2.as", description = "H2DB Password changing script run test",enabled = false)
    public void testScriptRun() throws Exception {

        serverPropertyMap.put("-DportOffset", Integer.toString(portOffset));
        AutomationContext autoCtx = new AutomationContext();
        CarbonTestServerManager server =
                new CarbonTestServerManager(autoCtx, System.getProperty("carbon.zip"), serverPropertyMap);
        manager.startServers(server);
        CARBON_HOME = server.getCarbonHome();
        manager.stopAllServers();

        String[] cmdArray;
        String commandDirectory;
        if (CarbonCommandToolsUtil.isCurrentOSWindows()) {
            cmdArray = new String[]{
                    "cmd.exe", "/c", "chpasswd.sh", "--db-url",MYSQL_DB_URL,
                    "--db-driver", "com.mysql.jdbc.Driver", "--db-username", "root", "--db-password",
                    "root123", "--username", "admin", "--new-password", "admin123"};
            commandDirectory = CARBON_HOME + File.separator + "bin";
        } else {
            cmdArray = new String[]{
                    "sh", "chpasswd.sh", "--db-url", MYSQL_DB_URL,
                    "--db-driver", "com.mysql.jdbc.Driver", "--db-username", "root", "--db-password",
                    "root123", "--username", "admin", "--new-password", "admin123"};
            commandDirectory = CARBON_HOME + "/bin";
        }

        scriptRunStatus =
                CarbonCommandToolsUtil.isScriptRunSuccessfully(commandDirectory, cmdArray,
                                                               "Password updated successfully");
        log.info("Script running status : " + scriptRunStatus);
        assertTrue(scriptRunStatus, "Script executed successfully");


        manager.startServers(server);

    }

    @Test(groups = "wso2.as", description = "H2DB password change test", dependsOnMethods = {"testScriptRun"} ,
    enabled = false)
    public void testChangeUserPasswordMySQL() throws Exception {
        int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset;
        String url = context.getContextUrls().getBackEndUrl();
        String backendURL = url.replaceAll("(:\\d+)", ":" + httpsPort);
        String loginStatusString = loginLogoutUtil.login("admin", "admin123".toCharArray(), backendURL);
        assertTrue(loginStatusString.contains("JSESSIONID"), "Unsuccessful login");

    }

    @AfterClass(alwaysRun = true)
    public void serverShutDown() throws Exception {
        int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset;
        String url = context.getContextUrls().getBackEndUrl();
        String backendURL = url.replaceAll("(:\\d+)", ":" + httpsPort);
        CarbonCommandToolsUtil.serverShutdown(backendURL, "admin", "admin123", context, portOffset);
    }



}
