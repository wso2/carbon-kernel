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
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.common.utils.CarbonCommandToolsUtil;
import org.wso2.carbon.integration.common.utils.CarbonIntegrationBaseTest;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.rmi.RemoteException;

/**
 * This class is to test java2wsdl command by providing a java class and get wsdl file
 */
public class Java2WsdlCommandTestCase extends CarbonIntegrationBaseTest {

    private static final Log log = LogFactory.getLog(CarbonServerBasicOperationTestCase.class);
    Process process;


    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        File sourceFile =
                new File(TestConfigurationProvider.getResourceLocation() + File.separator +
                         "artifacts" + File.separator + "CARBON" + File.separator + "carbontools" +
                         File.separator + "testjava2wsdl");
        File targetFile = new File(
                System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin/testjava2wsdl");
        super.copyFolder(sourceFile, targetFile);
    }

    @AfterClass(alwaysRun = true)
    public void cleanResources() throws RemoteException {
        process.destroy();
    }

    @Test(groups = "wso2.as", description = "Java to wsdl test")
    public void testJava2Wsdl() throws CarbonToolsIntegrationTestException {
        String[] cmdArrayToWsdl2Java;
        String commandDirectory;
        if (CarbonCommandToolsUtil.isCurrentOSWindows()) {
            throw new SkipException("Issue with wsdl2java.bat");
//            https://wso2.org/jira/browse/CARBON-15150
//            cmdArrayToWsdl2Java =
//                    new String[]{"cmd.exe", "/c", "start", "java2wsdl.bat", "-cn", "testjava2wsdl.Java2Wsdl"};
//            commandDirectory = System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin";
        } else {
            cmdArrayToWsdl2Java = new String[]{"sh", "java2wsdl.sh", "-cn", "testjava2wsdl.Java2Wsdl"};
            commandDirectory = System.getProperty(ServerConstants.CARBON_HOME) + "/bin";
        }
        process = CarbonCommandToolsUtil.runScript(commandDirectory, cmdArrayToWsdl2Java);
        boolean fileCreated = CarbonCommandToolsUtil.waitForFileCreation(
                System.getProperty(ServerConstants.CARBON_HOME) + File.separator + "bin" +
                File.separator + "Java2Wsdl.wsdl");
        Assert.assertTrue(fileCreated, "Java file not created successfully");

    }




}
