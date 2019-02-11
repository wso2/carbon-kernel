/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.integration.tests.integration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.extensions.servers.carbonserver.MultipleServersManager;
import org.wso2.carbon.automation.extensions.servers.carbonserver.TestServerManager;
import org.wso2.carbon.integration.tests.integration.test.servers.CarbonTestServerManager;

import java.util.HashMap;
import java.util.Map;

/**
 * This test starts & stops a couple of Carbon servers from a single test runtime
 */
public class MultipleCarbonInstancesTestCase {

    public MultipleServersManager manager = new MultipleServersManager();
    public Map<String, String> startupParameterMapOne = new HashMap<String, String>();
    public Map<String, String> startupParameterMapTwo = new HashMap<String, String>();

    private static final Log log = LogFactory.getLog(MultipleCarbonInstancesTestCase.class);

    @BeforeClass(groups = {"carbon.multi.server"})
    public void testStartServers() throws Exception {

        AutomationContext context = new AutomationContext();

        startupParameterMapOne.put("-DportOffset", "10");
        TestServerManager server1 = new TestServerManager(context, System.getProperty("carbon.zip"),
                startupParameterMapOne);
        startupParameterMapTwo.put("-DportOffset", "20");
        TestServerManager server2 = new TestServerManager(context, System.getProperty("carbon.zip"),
                startupParameterMapTwo);
        manager.startServers(server1, server2);
    }

    @AfterGroups(groups = {"carbon.multi.server"})
    public void testStopServers() throws Exception {
        manager.stopAllServers();
    }

    @Test(groups = {"carbon.multi.server"})
    public void test() {
        log.info("Test server startup with system properties");
    }


}
