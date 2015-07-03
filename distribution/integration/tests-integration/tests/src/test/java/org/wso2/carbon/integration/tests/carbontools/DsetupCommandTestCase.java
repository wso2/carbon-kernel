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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.tests.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationConstants;
import org.wso2.carbon.integration.tests.common.utils.LoginLogoutUtil;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.io.File;
import java.util.HashMap;

import static org.testng.Assert.assertTrue;


/**
* Check -Dsetup command by populating some users to DB and delete them using this command
*/
public class DsetupCommandTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(DsetupCommandTestCase.class);
    private LoginLogoutUtil loginLogoutUtil;
    private String carbonHome;
    private AutomationContext automationContextOfInstance002;
    private int portOffset = 1;
    private HashMap<String, String> serverPropertyMap = new HashMap<String, String>();


    @BeforeClass(alwaysRun = true)
    public void copyMasterDataSourceFile() throws Exception {
        super.init();
        automationContextOfInstance002 =
                new AutomationContext(CarbonIntegrationConstants.PRODUCT_GROUP,
                                      CarbonIntegrationConstants.INSTANCE,
                                      ContextXpathConstants.SUPER_TENANT,
                                      ContextXpathConstants.ADMIN);

        loginLogoutUtil = new LoginLogoutUtil();
        log.info("replacing the master-datasources.xml file");

        if (CarbonTestServerManager.isServerRunning()) {
            carbonHome = CarbonTestServerManager.getCarbonHome();
            CarbonTestServerManager.stop();
        } else {
            CarbonTestServerManager.start(portOffset);
            carbonHome = CarbonTestServerManager.getCarbonHome();
            CarbonTestServerManager.stop();
        }
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

    @Test(groups = "carbon.core", description = "Test -Dsetup recreate the database")
    public void testDsetupCommand() throws Exception {


        serverPropertyMap.put("-DportOffset", Integer.toString(portOffset));
        // start with -Dsetup command
        serverPropertyMap.put("-Dsetup", "");

        CarbonTestServerManager.start(serverPropertyMap);

        boolean startupStatus =
                CarbonCommandToolsUtil.isServerStartedUp(automationContextOfInstance002, portOffset);
        log.info("Server startup status : " + startupStatus);

        boolean fileCreated =
                CarbonCommandToolsUtil.waitForFileCreation(
                        carbonHome + File.separator + "repository" + File.separator +
                        "database" + File.separator + "DsetupCommandTEST_DB.h2.db");

        Assert.assertTrue(fileCreated, "DB file not created successfully");

        String loginStatusString =
                loginLogoutUtil.login(
                        automationContextOfInstance002.getSuperTenant().getTenantAdmin().getUserName(),
                        automationContextOfInstance002.getSuperTenant().getTenantAdmin().getPassword().toCharArray(),
                        automationContextOfInstance002.getContextUrls().getBackEndUrl());
        assertTrue(loginStatusString.contains("JSESSIONID"), "Unsuccessful login");
    }

    @AfterClass(alwaysRun = true)
    public void cleanResources() throws AutomationFrameworkException {
        CarbonTestServerManager.stop();
    }
}
