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
import org.wso2.carbon.integration.tests.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationConstants;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.io.File;
import java.io.FilenameFilter;

import static org.testng.Assert.assertTrue;

/**
* Test build.xml by running ant check repository lib folder for jars and
* run ant localize and check repository/components/dropins for language bundle
*/
public class RunBuildXMLTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(RunBuildXMLTestCase.class);
    private String carbonHome;
    private int portOffset = 1;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        if (CarbonTestServerManager.getCarbonHome() == null) {
            CarbonTestServerManager.start(portOffset);
            carbonHome = CarbonTestServerManager.getCarbonHome();
            CarbonTestServerManager.stop();
        } else {
            carbonHome = CarbonTestServerManager.getCarbonHome();
        }
    }

    @Test(groups = {"carbon.core"}, description = "Running the ant command and verifying the jar copying")
    public void testBuildXMLGenerateRemoteRegistryClients() throws Exception {
        boolean isJarCreated = false;
        Process process = null;
        try {

            File folder = new File(carbonHome + File.separator + "repository" + File.separator + "lib");
            File[] listOfFilesBeforeRunAntCommand = folder.listFiles(new FilenameFilter() {
                public boolean accept(File directory, String fileName) {
                    return fileName.toLowerCase().endsWith(".jar");
                }
            });
            String[] cmdArray;

            if ((CarbonCommandToolsUtil.getCurrentOperatingSystem().contains(
                    OperatingSystems.WINDOWS.name().toLowerCase()))) {
                cmdArray = new String[]{"cmd.exe", "/c", "start", "ant"};//run ant comment in bin directory
            } else {
                cmdArray = new String[]{"ant"};
            }

            process = CarbonCommandToolsUtil.runScript(carbonHome + File.separator + "bin", cmdArray);
            long startTime = System.currentTimeMillis();

            while ((System.currentTimeMillis() - startTime) < CarbonIntegrationConstants.DEFAULT_WAIT_MS) {
                File[] listOfFilesAfterRunAntCommand = folder.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.toLowerCase().endsWith(".jar");
                    }
                });

                if (listOfFilesBeforeRunAntCommand != null && listOfFilesAfterRunAntCommand != null) {
                    if (listOfFilesAfterRunAntCommand.length > listOfFilesBeforeRunAntCommand.length) {
                        log.info("Jars copied successfully");
                        isJarCreated = true;
                        break;
                    }
                } else if (listOfFilesAfterRunAntCommand != null && listOfFilesAfterRunAntCommand.length > 0) {
                        log.info("Jars copied successfully");
                        isJarCreated = true;
                        break;
                } else{
                    Thread.sleep(1000); // Sleeping 1 second
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
    public void testBuildXMLGenerateLanguageBundle() throws Exception {
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
            while (!isJarCreated && (System.currentTimeMillis() - startTime) < CarbonIntegrationConstants.DEFAULT_WAIT_MS) {
                if (folder.exists() && folder.isDirectory()) {
                    File[] listOfFiles = folder.listFiles();
                    if (listOfFiles != null) {
                        for (File file : listOfFiles) {//Check repository lib as well
                            if (file.getName().contains("org.wso2.carbon.identity.oauth.ui.languageBundle_1.0.jar")) {
                                log.info("LanguageBundle jar copied successfully");
                                isJarCreated = true;
                                break;
                            }
                        }
                    }
                }else{
                    log.info("LanguageBundle not created yet time " + (System.currentTimeMillis() - startTime) + " milliseconds");
                    Thread.sleep(1000);// Sleeping 1 second
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
