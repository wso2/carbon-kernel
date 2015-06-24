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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.frameworkutils.enums.OperatingSystems;
import org.wso2.carbon.integration.tests.common.bean.DataSourceBean;
import org.wso2.carbon.integration.tests.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.tests.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationConstants;
import org.wso2.carbon.integration.tests.common.utils.LoginLogoutUtil;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;

import static org.testng.Assert.assertTrue;

/**
 * Provides test cases for executing chpasswd.sh/chpasswd.bat with Mysql DB
 */
public class ChangeUserPasswordMySQLDBTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(ChangeUserPasswordMySQLDBTestCase.class);
    private LoginLogoutUtil loginLogoutUtil;
    private AutomationContext automationContextOfInstance002;
    private int portOffset = 1;
    private HashMap<String, String> serverPropertyMap = new HashMap<String, String>();
    private DataSourceBean dataSourceBean;
    private char[] userNewPassword = {'a', 'd', 'm', 'i', 'n', '1', '2', '3'};
    private AutomationContext automationContext;
    private String userName;

    @SetEnvironment(executionEnvironments = ExecutionEnvironment.PLATFORM)
    @BeforeClass(alwaysRun = true)
    public void initialize() throws XPathExpressionException, MalformedURLException, AxisFault {

        automationContextOfInstance002 =
                new AutomationContext(CarbonIntegrationConstants.PRODUCT_GROUP,
                                      CarbonIntegrationConstants.INSTANCE,
                                      ContextXpathConstants.SUPER_TENANT,
                                      ContextXpathConstants.SUPER_ADMIN);

        loginLogoutUtil = new LoginLogoutUtil();
        automationContext = new AutomationContext();
        dataSourceBean = CarbonCommandToolsUtil.getDataSourceInformation("MySQL");
        userName = automationContext.getContextTenant().getContextUser().getUserName();
    }

    @SetEnvironment(executionEnvironments = ExecutionEnvironment.PLATFORM)
    @Test(groups = "carbon.core", description = "H2DB Password changing script run test")
    public void testMysqlPasswordChangeScript() throws Exception {

        serverPropertyMap.put("-DportOffset", Integer.toString(portOffset));

        CarbonTestServerManager carbonServer =
                new CarbonTestServerManager(automationContext, System.getProperty("carbon.zip"),
                                            serverPropertyMap);

        String carbonHome = carbonServer.startServer();
        carbonServer.stopServer();

        String[] cmdArray;
        String commandDirectory = carbonHome + File.separator + "bin";


        if ((CarbonCommandToolsUtil.getCurrentOperatingSystem().
                contains(OperatingSystems.WINDOWS.name().toLowerCase()))) {

            cmdArray = new String[]{
                    "cmd.exe", "/c", "chpasswd.sh", "--db-url", dataSourceBean.getUrl(),
                    "--db-driver", dataSourceBean.getDriverClassName(), "--db-username",
                    dataSourceBean.getUserName(), "--db-password",
                    String.valueOf(dataSourceBean.getPassWord()), "--username", userName,
                    "--new-password", String.valueOf(userNewPassword)};
        } else {
            cmdArray = new String[]{
                    "sh", "chpasswd.sh", "--db-url", dataSourceBean.getUrl(),
                    "--db-driver", dataSourceBean.getDriverClassName(), "--db-username",
                    dataSourceBean.getUserName(), "--db-password",
                    String.valueOf(dataSourceBean.getPassWord()), "--username", userName,
                    "--new-password", String.valueOf(userNewPassword)};
        }
        boolean scriptRunStatus =
                CarbonCommandToolsUtil.isScriptRunSuccessfully(commandDirectory, cmdArray,
                                                               "Password updated successfully");
        log.info("Script running status : " + scriptRunStatus);
        assertTrue(scriptRunStatus, "Script executed successfully");
    }

    @SetEnvironment(executionEnvironments = ExecutionEnvironment.PLATFORM)
    @Test(groups = "carbon.core", description = "H2DB password change test",
            dependsOnMethods = {"testMysqlPasswordChangeScript"})
    public void testChangeUserPasswordMySQL() throws Exception {
        int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset;
        String url = automationContextOfInstance002.getContextUrls().getBackEndUrl();
        String backendURL = url.replaceAll("(:\\d+)", ":" + httpsPort);

        String loginStatusString =
                loginLogoutUtil.login(userName, String.valueOf(userNewPassword).toCharArray(), backendURL);
        assertTrue(loginStatusString.contains("JSESSIONID"), "Unsuccessful login");

    }

    @SetEnvironment(executionEnvironments = ExecutionEnvironment.PLATFORM)
    @AfterClass(alwaysRun = true)
    public void serverShutDown()
            throws XPathExpressionException, CarbonToolsIntegrationTestException {
        int httpsPort = Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset;
        String url = automationContextOfInstance002.getContextUrls().getBackEndUrl();
        String backendURL = url.replaceAll("(:\\d+)", ":" + httpsPort);
        CarbonCommandToolsUtil.serverShutdown(backendURL, userName, String.valueOf(userNewPassword), portOffset);
    }


}
