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
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.engine.frameworkutils.enums.OperatingSystems;
import org.wso2.carbon.integration.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;

import static org.testng.Assert.assertTrue;

/**
 * Test wsdl2java command by deploying a aar file and using the wsdl url of that
 */
public class Wsdl2JavaCommandTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(Wsdl2JavaCommandTestCase.class);

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @BeforeClass(alwaysRun = true)
    public void testDeployService() throws Exception {
        super.init();
    }

    @SetEnvironment(executionEnvironments = {ExecutionEnvironment.STANDALONE})
    @Test(groups = "wso2.as", description = "generate client code HelloWorld service")
    public void testGenerateClass()
            throws CarbonToolsIntegrationTestException, XPathExpressionException {
        boolean fileCreated = false;
        Process process = null;
        String commandDirectory;

        try {
            String serviceUrl = automationContext.getContextUrls().getServiceUrl() + "/Version?wsdl";
            log.info("Service URL -" + serviceUrl);
            String[] cmdArrayToWsdl2Java;
            if ((CarbonCommandToolsUtil.getCurrentOperatingSystem().contains(
                    OperatingSystems.WINDOWS.name().toLowerCase()))) {
                throw new SkipException("Bug when running wsdl2java.bat on windows");
                //TODO - uncomment the following code when carbon 15151 is fixed
                //https://wso2.org/jira/browse/CARBON-15151
                //cmdArrayToWsdl2Java =
                //new String[]{"cmd.exe", "/c", "wsdl2java.bat", "-uri", serviceUrl};
                //commandDirectory = System.getProperty(ServerConstants.CARBON_HOME) +
                //File.separator + "bin";
            } else {
                cmdArrayToWsdl2Java =
                        new String[]{"sh", "wsdl2java.sh", "-uri", serviceUrl};
                commandDirectory = System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin";
            }
            process = CarbonCommandToolsUtil.runScript(commandDirectory, cmdArrayToWsdl2Java);

            fileCreated = CarbonCommandToolsUtil.waitForFileCreation(
                    System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin" +
                    File.separator + "src" + File.separator + "org" + File.separator + "wso2" +
                    File.separator + "carbon" + File.separator + "core" + File.separator + "services" +
                    File.separator + "version" + File.separator + "VersionStub.java");

        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        assertTrue(fileCreated, "Java file not created successfully");
    }

}