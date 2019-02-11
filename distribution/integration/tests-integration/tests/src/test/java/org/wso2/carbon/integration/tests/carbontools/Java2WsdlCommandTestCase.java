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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.frameworkutils.enums.OperatingSystems;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.tests.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.tests.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
* Test java2wsdl command by providing a java class and get wsdl file
*/
public class Java2WsdlCommandTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(Java2WsdlCommandTestCase.class);
    private Process process;

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        File sourceFile =
                new File(TestConfigurationProvider.getResourceLocation() + File.separator +
                         "artifacts" + File.separator + "CARBON" + File.separator + "carbontools" +
                         File.separator + "testjava2wsdl");

        File targetFile =
                new File(System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin" +
                         File.separator + "testjava2wsdl");

        super.copyFolder(sourceFile, targetFile); //copy testjava2wsdl directory into bin
    }

    @Test(groups = "carbon.core", description = "Verify Java to wsdl")
    public void testJava2Wsdl() throws CarbonToolsIntegrationTestException {
        String[] cmdArrayToWsdl2Java;
        String commandDirectory;
        if ((CarbonCommandToolsUtil.getCurrentOperatingSystem().
                contains(OperatingSystems.WINDOWS.name().toLowerCase()))) {
            throw new SkipException("Bug with wsdl2java.bat when running on Windows");
            //TODO - uncomment the following when CARBON-1515O is fixed
            //https://wso2.org/jira/browse/CARBON-15150
            //cmdArrayToWsdl2Java =
            //new String[]{"cmd.exe", "/c", "start", "java2wsdl.bat", "-cn", "testjava2wsdl.Java2Wsdl"};
            //commandDirectory = System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin";
        } else {
            cmdArrayToWsdl2Java = new String[]{"sh", "java2wsdl.sh", "-cn", "testjava2wsdl.Java2Wsdl"};
            commandDirectory = System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin";
        }
        process = CarbonCommandToolsUtil.runScript(commandDirectory, cmdArrayToWsdl2Java);
        boolean fileCreated =
                CarbonCommandToolsUtil.waitForFileCreation(
                        System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin" +
                        File.separator + "Java2Wsdl.wsdl");

        assertTrue(fileCreated, "Java file not created successfully");

    }

    @AfterClass(alwaysRun = true)
    public void cleanResources() throws RemoteException {
        if (process != null) {
            process.destroy();
        }
    }
}
