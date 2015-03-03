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
import org.wso2.carbon.automation.engine.frameworkutils.enums.OperatingSystems;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;

import java.io.File;

import static org.testng.Assert.assertTrue;

/**
 * Test build.xml by running ant check repository lib folder for jars and
 * run ant localize and check repository/components/dropins for language bundle
 */
public class RunBuildXMLTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(RunBuildXMLTestCase.class);
    private String carbonHome;
    private long FILE_CREATION_TIMEOUT_MS = 1000 * 20;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        carbonHome = CarbonCommandToolsUtil.getCarbonHome(automationContext);
    }

    @Test(groups = {"carbon.core"}, description = "Running the ant command and verifying the jar copying")
    public void testBuildXMLGenerateRemoteRegistryClients()
            throws CarbonToolsIntegrationTestException {
        boolean isJarCreated = false;
        Process process = null;
        try {

            File folder = new File(carbonHome + File.separator + "repository" + File.separator + "lib");
            File[] listOfFilesBeforeRunAntCommand = folder.listFiles();
            String[] cmdArray;

            if ((CarbonCommandToolsUtil.getCurrentOperatingSystem().contains(
                    OperatingSystems.WINDOWS.name().toLowerCase()))) {
                cmdArray = new String[]{"cmd.exe", "/c", "start", "ant"};//run ant comment in bin directory
            } else {
                cmdArray = new String[]{"ant"};
            }

            process = CarbonCommandToolsUtil.runScript(carbonHome + File.separator + "bin", cmdArray);
            long startTime = System.currentTimeMillis();

            while ((System.currentTimeMillis() - startTime) < FILE_CREATION_TIMEOUT_MS) {
                File[] listOfFilesAfterRunAntCommand = folder.listFiles();
                if (listOfFilesBeforeRunAntCommand != null && listOfFilesAfterRunAntCommand != null) {
                    if (listOfFilesAfterRunAntCommand.length > listOfFilesBeforeRunAntCommand.length) {
                        log.info("Jars copied successfully");
                        isJarCreated = true;
                        break;
                    }
                } else {
                    if (listOfFilesAfterRunAntCommand != null && listOfFilesAfterRunAntCommand.length > 0) {
                        log.info("Jars copied successfully");
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
        assertTrue(isJarCreated, "Jar not copied successfully");
    }

    @Test(groups = {"carbon.core"}, description = "Run the ant localize command and verifying the languageBundle")
    public void testBuildXMLGenerateLanguageBundle()
            throws CarbonToolsIntegrationTestException {
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

            if ((CarbonCommandToolsUtil.getCurrentOperatingSystem().contains(
                    OperatingSystems.WINDOWS.name().toLowerCase()))) {
                cmdArray = new String[]{"cmd.exe", "/c", "start", "ant", "localize"};
            } else {
                cmdArray = new String[]{"ant", "localize"};
            }

            process = CarbonCommandToolsUtil.runScript(carbonHome + File.separator + "bin", cmdArray);

            File folder = new File(carbonHome + File.separator + "repository" + File.separator +
                                   "components" + File.separator + "dropins");

            long startTime = System.currentTimeMillis();
            while (!isJarCreated && (System.currentTimeMillis() - startTime) < FILE_CREATION_TIMEOUT_MS) {
                if (folder.exists() && folder.isDirectory()) {
                    File[] listOfFiles = folder.listFiles();
                    if (listOfFiles != null) {
                        for (File file : listOfFiles) {//Check repository lib as well
                            if (file.getName().contains("org.wso2.carbon.identity.oauth.ui.languageBundle_4.0.7.jar")) {
                                log.info("LanguageBundle jar copied successfully");
                                isJarCreated = true;
                                break;
                            }
                        }
                    }
                }
            }
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        assertTrue(isJarCreated, "Jar not copied successfully");
    }

}
