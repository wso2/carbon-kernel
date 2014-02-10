/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import org.wso2.carbon.runtime.exception.RuntimeServiceException;
import org.wso2.carbon.runtime.runtime.CustomRuntime;

public class CustomRuntimeTest {
    CustomRuntime customRuntime;

    public CustomRuntimeTest() {

    }

    @BeforeTest
    public void setup() throws RuntimeServiceException {
        customRuntime = new CustomRuntime();
    }

    @Test
    public void testInitRuntime() {
        customRuntime.init();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.INACTIVE);
    }

    @Test(dependsOnMethods = {"testInitRuntime"})
    public void testRuntimeStart() throws RuntimeServiceException {
        customRuntime.start();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.ACTIVE);
    }

    @Test(dependsOnMethods = {"testRuntimeStart"})
    public void testRuntimeStartMaintenance() throws RuntimeServiceException {
        customRuntime.beginMaintenance();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.MAINTENANCE);
    }

    @Test(dependsOnMethods = {"testRuntimeStartMaintenance"})
    public void testRuntimeStopMaintenance() throws RuntimeServiceException {
        customRuntime.endMaintenance();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.INACTIVE);
    }

    @Test(dependsOnMethods = {"testRuntimeStopMaintenance"})
    public void testRuntimeStop() throws RuntimeServiceException {
        customRuntime.stop();
        Assert.assertEquals(customRuntime.getState(), RuntimeState.INACTIVE);
    }

}
