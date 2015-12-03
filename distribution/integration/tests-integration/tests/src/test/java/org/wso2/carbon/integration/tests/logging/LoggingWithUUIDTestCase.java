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

package org.wso2.carbon.integration.tests.logging;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.TestUserMode;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.extensions.servers.utils.FileManipulator;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.UUID;
import javax.xml.xpath.XPathExpressionException;

/**
 * This test case tests the logging in wso2carbon.log with log UUID enabled (via the log4j.properties file).
 * This tests whether the UUID is logged in the pattern layout along with the log message.
 */
public class LoggingWithUUIDTestCase extends CarbonIntegrationBaseTest {

    private TestServerManager serverManager;
    private String carbonHome;
    private int portOffset = 29;
    private AutomationContext context;

    @BeforeClass
    public void setup() throws XPathExpressionException, IOException, AutomationFrameworkException {
        context = new AutomationContext(CarbonIntegrationConstants.PRODUCT_GROUP, TestUserMode.SUPER_TENANT_ADMIN);

        HashMap<String, String> startUpParameterMap = new HashMap<>();
        startUpParameterMap.put("-DportOffset", String.valueOf(portOffset));

        serverManager = new TestServerManager(context, System.getProperty("carbon.zip"), startUpParameterMap);

        File log4JPropertiesSrcFile = Paths.get(
                System.getProperty(FrameworkConstants.SYSTEM_ARTIFACT_RESOURCE_LOCATION),
                "log4j", "loggingWithUUID", "log4j.properties").toFile();
        serverManager.startServer();
        carbonHome = serverManager.getCarbonHome();
        File log4JPropertiesTargetFile = Paths.get(carbonHome, "repository", "conf", "log4j.properties").toFile();

        FileManipulator.copyFile(log4JPropertiesSrcFile, log4JPropertiesTargetFile);
        restartServer();
    }

    @Test(groups = "org.wso2.carbon.logging", description = "read the last line of wso2carbon.log file and " +
                                                            "make sure UUID is there.")
    public void logUUIDTestCase() {
        String lastLog = getLastLineOfCarbonLog().trim();
        if (lastLog.isEmpty()) {
            Assert.fail("Last line of wso2carbon.log is empty");
        }
        String uuid = lastLog.split(" ")[1];
        uuid = uuid.substring(1, uuid.length() - 1);

        try {
            UUID.fromString(uuid);
        } catch (Exception e) {
            Assert.fail("log UUID is not logged with the log message.");
        }
    }

    @AfterClass
    public void destroy() throws AutomationFrameworkException {
        if (serverManager != null) {
            serverManager.stopServer();
        }
    }


    private void restartServer() throws AutomationFrameworkException, XPathExpressionException {
        serverManager.restartGracefully();
        ClientConnectionUtil
                .waitForPort(Integer.parseInt(FrameworkConstants.SERVER_DEFAULT_HTTPS_PORT) + portOffset, 5 * 60000,
                             true, context.getInstance().getHosts().get("default"));

    }

    private String getLastLineOfCarbonLog() {
        String lastLine = "";
        File wso2carbonLog = Paths.get(carbonHome, "repository", "logs", "wso2carbon.log").toFile();

        try (FileInputStream fis = new FileInputStream(wso2carbonLog)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(fis));

            String temp;
            while ((temp = br.readLine()) != null) {
                lastLine = temp;
            }
        } catch (FileNotFoundException e) {
            Assert.fail("wso2carbon.log file not found.");
        } catch (IOException e) {
            Assert.fail("Error in reading the last line from wso2carbon.log");
        }

        return lastLine;
    }
}