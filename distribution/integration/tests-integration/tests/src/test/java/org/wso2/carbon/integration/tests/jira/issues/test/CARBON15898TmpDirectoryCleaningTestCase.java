/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.integration.tests.jira.issues.test;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.servers.carbonserver.CarbonServerManager;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;

import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

/**
 * This test case checks whether <WSO2_CARBON_HOME>/tmp directory in not deleted at server startup.
 * Rather the content of <WSO2_CARBON_HOME>/tmp should be removed.
 */
public class CARBON15898TmpDirectoryCleaningTestCase extends CarbonIntegrationBaseTest {

    private static CarbonServerManager carbonServerManager;
    private static int portOffset = 11;
    private static String carbonHome;
    private static Map<String, String> startupOptions;

    @BeforeClass
    public void initialize() throws XPathExpressionException, IOException, AutomationFrameworkException {
        super.init();

        carbonServerManager = new CarbonServerManager(new AutomationContext());
        String carbonZipLocation = System.getProperty("carbon.zip");
        carbonHome = carbonServerManager.setUpCarbonHome(carbonZipLocation);

        startupOptions = new HashMap<>();
        startupOptions.put("-DportOffset", String.valueOf(portOffset));

        carbonServerManager.startServerUsingCarbonHome(carbonHome, startupOptions);

    }

    @Test(groups = "carbon.integration.jira", description = "checks the creation timestamp of <WSO2_CARBON_HOME>/tmp " +
            "to ensure that the tmp directory is not deleted at server startup.")
    public void test() throws IOException, AutomationFrameworkException {
        Path tmpDirectory = Paths.get(carbonHome, "tmp");
        FileTime fileCreatedTime = Files.readAttributes(tmpDirectory, BasicFileAttributes.class).creationTime();

        carbonServerManager.serverShutdown(portOffset);
        carbonServerManager.startServerUsingCarbonHome(carbonHome, startupOptions);

        FileTime newFileCreatedTime = Files.readAttributes(tmpDirectory, BasicFileAttributes.class).creationTime();

        Assert.assertEquals(fileCreatedTime.toMillis() == newFileCreatedTime.toMillis(),
                "<WSO2_CARBON_HOME>/tmp directory has been recreated in server restart.");
    }

    @AfterClass
    public void destroy() throws AutomationFrameworkException {
        carbonServerManager.serverShutdown(portOffset);
    }

}
