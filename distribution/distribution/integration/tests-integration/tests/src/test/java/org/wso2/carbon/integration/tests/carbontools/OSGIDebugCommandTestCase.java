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
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.ContextXpathConstants;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.frameworkutils.enums.OperatingSystems;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.carbon.integration.tests.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.tests.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
* Test -DosgiDebugOptions
* This test cases is not testing it's all features since: https://wso2.org/jira/browse/CARBON-15170
*/

public class OSGIDebugCommandTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(OSGIDebugCommandTestCase.class);
    private AutomationContext automationContextOfInstance002;
    private String commandDirectory;
    private int portOffset = 1;
    private Process process = null;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        automationContextOfInstance002 =
                new AutomationContext(CarbonIntegrationConstants.PRODUCT_GROUP,
                                      CarbonIntegrationConstants.INSTANCE,
                                      ContextXpathConstants.SUPER_TENANT,
                                      ContextXpathConstants.ADMIN);
    }

    @Test(groups = {"carbon.core"}, description = "OSGI debug command test")
    public void testOSGIDebugCommand()
            throws CarbonToolsIntegrationTestException, IOException {
        String[] cmdArray;
        String expectedString = "OSGi debugging has been enabled with options:";
        boolean isFoundTheMessage = false;
        InputStream is = null;

        BufferedReader br = null;
        if ((CarbonCommandToolsUtil.getCurrentOperatingSystem().
                contains(OperatingSystems.WINDOWS.name().toLowerCase()))) {
            cmdArray = new String[]
                    {"cmd.exe", "/c", "wso2server.bat", "-DosgiDebugOptions", "-DportOffset=1"};
        } else {
            cmdArray = new String[]
                    {"sh", "wso2server.sh", "-DosgiDebugOptions", "-DportOffset=1"};
        }
        commandDirectory = getCarbonHome(automationContextOfInstance002) +
                File.separator + "bin";

        try {
            process = CarbonCommandToolsUtil.runScript(commandDirectory, cmdArray);
            String line;
            long startTime = System.currentTimeMillis();
            while ((System.currentTimeMillis() - startTime) < CarbonIntegrationConstants.DEFAULT_WAIT_MS) {
                is = process.getInputStream();
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                if (br != null) {
                    line = br.readLine();
                    if (line != null && line.contains(expectedString)) {
                        log.info("found the string " + expectedString + " in line " + line);
                        isFoundTheMessage = true;
                        break;
                    }
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (br != null) {
                br.close();
            }
        }
        CarbonCommandToolsUtil.isServerStartedUp(automationContextOfInstance002, portOffset);
        Assert.assertTrue(isFoundTheMessage, "Java file not created successfully");
    }

    @AfterClass(alwaysRun = true)
    public void serverShutDown() {
        try {
            if (CarbonCommandToolsUtil.isServerStartedUp(automationContextOfInstance002, portOffset)) {
                CarbonCommandToolsUtil.serverShutdown(portOffset);
            }
        } catch (CarbonToolsIntegrationTestException e) {
            log.info("Server already Shutdown");
        }
        if (process != null) {
            process.destroy();
        }
    }


    private String getCarbonHome(AutomationContext context)
            throws CarbonToolsIntegrationTestException {
        try {


            String carbonZip = System.getProperty(FrameworkConstants.SYSTEM_PROPERTY_CARBON_ZIP_LOCATION);
            CarbonServerManager carbonServerManager = new CarbonServerManager(context);
            String carbonHomePath = carbonServerManager.setUpCarbonHome(carbonZip);
            return carbonHomePath;

        } catch (IOException ex) {
            log.error("Extracting the pack and getting the carbon home failed", ex);
            throw new CarbonToolsIntegrationTestException("Extracting the pack and getting the " +
                                                          "carbon home failed", ex);
        } catch (AutomationFrameworkException e) {
            log.error("Extracting the pack and getting the carbon home failed", e);
            throw new CarbonToolsIntegrationTestException("Extracting the pack and getting the " +
                                                          "carbon home failed", e);
        }
    }

}
