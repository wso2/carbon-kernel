/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.tomcat.ext.internal;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;

import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

/**
 * CarbonTomcatServiceHolderTest includes test scenarios for
 * [1] properties, serverConfigurationService, carbonTomcatService and tccl of CarbonTomcatServiceHolder.
 * @since 4.4.19
 */
public class CarbonTomcatServiceHolderTest {

    private static final Logger log = Logger.getLogger("CarbonTomcatServiceHolderTest");

    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"},
            description = "Testing getters and setters for Server Configuration Service.")
    public void testServerConfigurationService () {
        // mocking inputs
        ServerConfigurationService serverConfigurationService = mock(ServerConfigurationService.class);
        // calling set method
        CarbonTomcatServiceHolder.setServerConfigurationService(serverConfigurationService);
        // checking retrieved values
        log.info("Testing getters and setters for serverConfigurationService");
        Assert.assertEquals(CarbonTomcatServiceHolder.getServerConfigurationService(), serverConfigurationService,
                "retrieved value did not match with set value for serverConfigurationService");
    }

    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"},
            description = "Testing getters and setters for Carbon Tomcat Service.")
    public void testCarbonTomcatService () {
        // mocking inputs
        CarbonTomcatService carbonTomcatService = mock(CarbonTomcatService.class);
        // calling set method
        CarbonTomcatServiceHolder.setCarbonTomcatService(carbonTomcatService);
        // checking retrieved values
        log.info("Testing getters and setters for carbonTomcatService");
        Assert.assertEquals(CarbonTomcatServiceHolder.getCarbonTomcatService(), carbonTomcatService,
                "retrieved value did not match with set value for carbonTomcatService");
    }

    @Test(groups = {"org.wso2.carbon.tomcat.ext.internal"},
            description = "Testing getters and setters for Class Loader.")
    public void testClassLoader () {
        // mocking inputs
        ClassLoader tccl = mock(ClassLoader.class);
        // calling set method
        CarbonTomcatServiceHolder.setTccl(tccl);
        // checking retrieved values
        log.info("Testing getters and setters for tccl");
        Assert.assertEquals(CarbonTomcatServiceHolder.getTccl(), tccl,
                "retrieved value did not match with set value for tccl");
    }
}
