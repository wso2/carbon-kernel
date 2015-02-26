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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

// This class is to test build.xml by running ant check repository lib folder for jars and
// run ant localize and check repository/components/dropins for language bundle

public class RunBuildXMLTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(RunBuildXMLTestCase.class);
    AutomationContext context;
    String carbonHome;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        context = new AutomationContext("CARBON", "carbon002",
                                        ContextXpathConstants.SUPER_TENANT,
                                        ContextXpathConstants.ADMIN);
        carbonHome = CarbonCommandToolsUtil.getCarbonHome(context);
    }

    @Test(groups = {"wso2.as"}, description = "Server restart test")
    public void testBuildXMLGenerateRemoteRegistryClients()
            throws CarbonToolsIntegrationTestException {
        boolean isJarCreated = false;
        Process process = null;
        try {

            File folder = new File(carbonHome + File.separator + "repository" + File.separator + "lib");
            File[] listOfFilesBeforeRunAntCommand = folder.listFiles();
            String[] cmdArray;
            if (CarbonCommandToolsUtil.isCurrentOSWindows()) {
                cmdArray = new String[]{"cmd.exe", "/c", "start", "ant" };
            } else {
                cmdArray = new String[]{"ant"};
            }
            process = CarbonCommandToolsUtil.runScript(carbonHome + File.separator + "bin", cmdArray);
            long startTime = System.currentTimeMillis();
            long timeout = 20000;
            while ((System.currentTimeMillis() - startTime) < timeout) {
                File[] listOfFilesAfterRunAntCommand = folder.listFiles();
                if (listOfFilesBeforeRunAntCommand != null && listOfFilesAfterRunAntCommand != null) {
                    if (listOfFilesAfterRunAntCommand.length > listOfFilesBeforeRunAntCommand.length) {
                        log.info("Jars created successfully");
                        isJarCreated = true;
                        break;
                    }
                } else {
                    if (listOfFilesAfterRunAntCommand != null && listOfFilesAfterRunAntCommand.length > 0) {
                        log.info("Jars created successfully");
                        isJarCreated = true;
                        break;
                    }
                }
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        assertTrue(isJarCreated, "Jar not created successfully");
    }

    @Test(groups = {"wso2.as"}, description = "Server restart test")
    public void testBuildXMLGenerateLanguageBundle()
            throws CarbonToolsIntegrationTestException, IOException {
        boolean isJarCreated = false;
        Process process = null;
        try {
            File sourceFile =
                    new File(TestConfigurationProvider.getResourceLocation() + File.separator +
                             "artifacts" + File.separator + "CARBON" + File.separator + "carbontools" +
                             File.separator + "resources");
            File targetFile = new File(carbonHome + File.separator + "resources");
            super.copyFolder(sourceFile, targetFile);
            String cmdArray[];
            if (CarbonCommandToolsUtil.isCurrentOSWindows()) {
                cmdArray = new String[]{"cmd.exe", "/c", "start", "ant", "localize"};
            } else {
                cmdArray = new String[]{"ant", "localize"};
            }
            process = CarbonCommandToolsUtil.runScript(carbonHome + File.separator + "bin", cmdArray);
            File folder = new File(carbonHome + File.separator + "repository" + File.separator +
                                   "components" + File.separator + "dropins");
            long startTime = System.currentTimeMillis();
            long timeout = 20000;
            while ((System.currentTimeMillis() - startTime) < timeout) {
                if (folder.exists() && folder.isDirectory()) {
                    File[] listOfFiles = folder.listFiles();
                    for (File file : listOfFiles) {//Check rep lib as well
                        if (file.getName().
                                contains("org.wso2.carbon.identity.oauth.ui.languageBundle_4.0.7.jar")) {
                            log.info("LanguageBundle jar created successfully");
                            isJarCreated = true;
                            break;
                        }
                    }
                    if (isJarCreated) {
                        break;
                    }
                }
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        assertTrue(isJarCreated, "Jar not created successfully");
    }

}
