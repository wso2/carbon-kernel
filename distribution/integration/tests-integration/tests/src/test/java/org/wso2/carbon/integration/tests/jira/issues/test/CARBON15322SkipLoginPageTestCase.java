/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.annotations.ExecutionEnvironment;
import org.wso2.carbon.automation.engine.annotations.SetEnvironment;
import org.wso2.carbon.automation.test.utils.http.client.HttpRequestUtil;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.integration.tests.common.utils.CarbonIntegrationBaseTest;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class CARBON15322SkipLoginPageTestCase extends CarbonIntegrationBaseTest {

    private ServerConfigurationManager serverConfigurationManager;
    private static String sampleServletJar = "SampleServletTest.jar";

    @SetEnvironment(executionEnvironments = ExecutionEnvironment.STANDALONE)
    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        super.init();
        serverConfigurationManager = new ServerConfigurationManager(automationContext);
        File sampleServletTest = new File(
                System.getProperty("basedir", ".") + File.separator + "target" + File.separator + "resources" +
                File.separator + "artifacts" + File.separator + "CARBON" + File.separator + "SampleSerlvet" +
                File.separator + sampleServletJar);
        serverConfigurationManager.copyToComponentDropins(sampleServletTest);
        serverConfigurationManager.restartGracefully();
        super.init();
    }

    @SetEnvironment(executionEnvironments = ExecutionEnvironment.STANDALONE)
    @AfterClass(alwaysRun = true)
    public void RestoreServer() throws Exception {
        serverConfigurationManager.removeFromComponentLib(sampleServletJar);
        serverConfigurationManager.restartGracefully();
        super.init();
    }

    @SetEnvironment(executionEnvironments = ExecutionEnvironment.STANDALONE)
    @Test(groups = {"carbon.core"}, description = "Skip Login Page for 2 levels of Endpoint")
    public void testEndpointFoo() throws Exception {
        String expectedResponse = "/sampleservlet/foo";
        String endpoint = "https://" + automationContext.getDefaultInstance().getHosts().get("default") + ":" +
                          automationContext.getDefaultInstance().getPorts().get("https") + expectedResponse;
        HttpResponse response = HttpRequestUtil.sendGetRequest(endpoint, null);
        assertTrue(expectedResponse.equalsIgnoreCase(response.getData()),
                   "Expected message did not match actual message");
    }

    @SetEnvironment(executionEnvironments = ExecutionEnvironment.STANDALONE)
    @Test(groups = "carbon.core", description = "Skip Login Page for 3 levels of Endpoint")
    public void testEndpointFooBar() throws Exception {
        String expectedResponse = "/sampleservlet/foo/bar";
        String endpoint = "https://" + automationContext.getDefaultInstance().getHosts().get("default") + ":" +
                          automationContext.getDefaultInstance().getPorts().get("https") + expectedResponse;
        HttpResponse response = HttpRequestUtil.sendGetRequest(endpoint, null);
        assertTrue(expectedResponse.equalsIgnoreCase(response.getData()),
                   "Expected message did not match actual message");
    }
}
