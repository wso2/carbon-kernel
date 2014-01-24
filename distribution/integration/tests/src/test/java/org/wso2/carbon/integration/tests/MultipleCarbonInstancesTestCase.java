/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.tests;

import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.MultipleServersManager;

import java.io.IOException;

/**
 * This test starts & stops a couple of Carbon servers from a single test runtime
 */
public class MultipleCarbonInstancesTestCase {

    public MultipleServersManager manager = new MultipleServersManager();

    @BeforeGroups(groups = {"carbon.multi.server"})
    public void testStartServers() throws IOException {
        CarbonTestServerManager server1 = new CarbonTestServerManager(System.getProperty("carbon.zip"),
                                                                      10);
        CarbonTestServerManager server2 = new CarbonTestServerManager(System.getProperty("carbon.zip"),
                                                                      12);

        manager.startServers(server1, server2);
    }

    @Test(groups = {"carbon.multi.server"})
    public void test(){

    }

    @AfterGroups(groups = {"carbon.multi.server"})
    public void testStopServers() throws Exception {
        manager.stopAllServers();
    }
}
