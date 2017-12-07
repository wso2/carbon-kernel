/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.integration.tests.jira.issues.test;

import org.junit.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.carbon.automation.extensions.servers.utils.InputStreamHandler;
import org.wso2.carbon.automation.test.utils.common.TestConfigurationProvider;
import org.wso2.carbon.integration.tests.common.exception.CarbonToolsIntegrationTestException;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CARBON16062PrimaryHazelcastConfigNullTestCase extends CarbonIntegrationBaseTest {

    public static final String ARTIFACTS_DIRECTORY = "artifacts";
    public static final String CUSTOM_JAR_NAME = "org.wso2.membershipScheme.dummy-1.0.jar";
    public static final String REPOSITORY_DIRECTORY = "repository";
    public static final String CLUSTER_INITIALIZATION_COMPLETED_MESSAGE = "Cluster initialization completed";
    private final int PORT_OFFSET = 10;
    private String carbonHome;
    private AutomationContext context;
    private Process process;

    @BeforeClass(alwaysRun = true)
    public void initTests() throws Exception {
        super.init();
        context = new AutomationContext();

        //Create CarbonServerManager instance to start new Carbon instance
        CarbonServerManager carbonServerManager = new CarbonServerManager(context);

        //Get Carbon zip file location
        String carbonZipLocation = System.getProperty("carbon.zip");
        //Extract Carbon pack in to temp directory
        carbonHome = carbonServerManager.setUpCarbonHome(carbonZipLocation);

        copyCustomMemberShipSchemeBundleToDropins();
        replaceAxis2XML();

        process = startServer(PORT_OFFSET);

    }

    private void replaceAxis2XML() throws IOException {
        Path sourcePath = Paths.get(TestConfigurationProvider.getResourceLocation(), ARTIFACTS_DIRECTORY, "CARBON",
                "CARBON16062", "axis2.xml");
        Path targetPath = Paths.get(carbonHome, REPOSITORY_DIRECTORY, "conf", "axis2", "axis2.xml");
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private void copyCustomMemberShipSchemeBundleToDropins() throws IOException, CarbonToolsIntegrationTestException {
        Path sourcePath = Paths.get(TestConfigurationProvider.getResourceLocation(), ARTIFACTS_DIRECTORY, "CARBON",
                "CARBON16062", CUSTOM_JAR_NAME);
        Path targetPath = Paths.get(carbonHome, REPOSITORY_DIRECTORY, "components", "lib", CUSTOM_JAR_NAME);
        Files.copy(sourcePath, targetPath);
    }

    @Test(groups = "carbon.core", description = "Test custom membership scheme where Primary hazelcast config "
            + "becoming null")
    public void verifyCustomMembershipScheme() throws Exception {
        InputStreamHandler errorStreamHandler = new InputStreamHandler("errorStream", process.getErrorStream());
        InputStreamHandler inputStreamHandler = new InputStreamHandler("inputStream", process.getInputStream());

        // start the stream readers
        inputStreamHandler.start();
        errorStreamHandler.start();

        long time = System.currentTimeMillis() + 60 * 1000;
        String output = inputStreamHandler.getOutput();

        while (!output.contains(CLUSTER_INITIALIZATION_COMPLETED_MESSAGE) && System.currentTimeMillis() < time) {
            output = inputStreamHandler.getOutput();
        }

        Assert.assertTrue("Error while initializing custom membership scheme",output.contains
                (CLUSTER_INITIALIZATION_COMPLETED_MESSAGE));
    }

    private Process startServer(int portOffset) throws IOException {
        Process process;
        File commandDir = new File(carbonHome + File.separator + "bin");
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            String[] cmdArray;
            cmdArray = new String[] { "cmd.exe", "wso2server.bat", "-DportOffset=" + portOffset };
            process = Runtime.getRuntime().exec(cmdArray, null, commandDir);
        } else {
            String[] cmdArray;
            cmdArray = new String[] { "sh", "wso2server.sh", "-DportOffset=" + portOffset };
            process = Runtime.getRuntime().exec(cmdArray, null, commandDir);
        }
        return process;
    }
}

