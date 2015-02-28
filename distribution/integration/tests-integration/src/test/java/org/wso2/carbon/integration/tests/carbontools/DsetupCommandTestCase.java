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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.frameworkutils.enums.OperatingSystems;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.common.utils.LoginLogoutUtil;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.net.MalformedURLException;

import static org.testng.Assert.assertTrue;


/**
 * This class is to check -Dsetup command by populating some users to DB and delete them using this command
 */
public class DsetupCommandTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(DsetupCommandTestCase.class);
    private LoginLogoutUtil authenticatorClient;

    private String carbonHome;
    private AutomationContext context;
    private int portOffset = 1;
    private Process process;

    @BeforeClass(alwaysRun = true)
    public void init()
            throws CarbonToolsIntegrationTestException, XPathExpressionException,
                   MalformedURLException, AxisFault {

        context =
                new AutomationContext("CARBON", "carbon002",
                                      ContextXpathConstants.SUPER_TENANT,
                                      ContextXpathConstants.ADMIN);

        authenticatorClient = new LoginLogoutUtil(context);
        carbonHome = CarbonCommandToolsUtil.getCarbonHome(context);
        log.info("replacing the master-datasources.xml file");
        File sourceFile =
                new File(TestConfigurationProvider.getResourceLocation() + File.separator +
                         "artifacts" + File.separator + "CARBON" + File.separator + "carbontools" +
                         File.separator + "master-datasources.xml");

        File targetFile =
                new File(carbonHome + File.separator + "repository" +
                         File.separator + "conf" + File.separator + "datasources" + File.separator +
                         "master-datasources.xml");
        super.copyFolder(sourceFile, targetFile);
    }

    @Test(groups = "wso2.greg", description = "Add resource")
    public void testCleanResource() throws Exception {

        String[] cmdArrayToRecreateDB;
        if ((CarbonCommandToolsUtil.getCurrentOperatingSystem().
                contains(OperatingSystems.WINDOWS.name().toLowerCase()))) {
            cmdArrayToRecreateDB = new String[]{"-Dsetup"};
            process = CarbonCommandToolsUtil.
                    startServerUsingCarbonHome(carbonHome, 1, context, cmdArrayToRecreateDB);
        } else {
            cmdArrayToRecreateDB =
                    new String[]{"-Dsetup"};
            process = CarbonCommandToolsUtil.
                    startServerUsingCarbonHome(carbonHome, 1, context, cmdArrayToRecreateDB);
        }
        boolean startupStatus = CarbonCommandToolsUtil.isServerStartedUp(context, portOffset);
        log.info("Server startup status : " + startupStatus);

        boolean fileCreated = CarbonCommandToolsUtil.
                waitForFileCreation(carbonHome + File.separator + "repository" + File.separator +
                                    "database" + File.separator + "DsetupCommandTEST_DB.h2.db");

        Assert.assertTrue(fileCreated, "Java file not created successfully");
        String loginStatusString = authenticatorClient.login();
        assertTrue(loginStatusString.contains("JSESSIONID"), "Unsuccessful login");

    }


    @AfterClass(alwaysRun = true)
    public void cleanResources() throws Exception {
        CarbonCommandToolsUtil.serverShutdown(1, context);
    }
}
