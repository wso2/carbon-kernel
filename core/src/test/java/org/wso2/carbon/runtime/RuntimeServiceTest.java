/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.internal.runtime.RuntimeManager;
import org.wso2.carbon.runtime.exception.RuntimeServiceException;
import org.wso2.carbon.runtime.runtime.CustomRuntime;
import org.wso2.carbon.runtime.service.CustomRuntimeService;
import org.wso2.carbon.runtime.spi.Runtime;

public class RuntimeServiceTest {
    CustomRuntime customRuntime;
    RuntimeManager runtimeManager;
    CustomRuntimeService customRuntimeService;

    @BeforeTest
    public void setup() {
        customRuntime = new CustomRuntime();
        runtimeManager = new RuntimeManager();
        runtimeManager.registerRuntime(customRuntime);
        customRuntimeService = new CustomRuntimeService(runtimeManager);
    }

    @Test
    public void testBeforeInitRuntime() {
        for (Runtime runtime : runtimeManager.getRuntimeList()) {
            Assert.assertEquals(RuntimeState.PENDING, runtime.getState());
        }
    }

    @Test(dependsOnMethods = {"testBeforeInitRuntime"})
    public void testStartBeforeInitRuntime() {
        boolean serverStarted = false;
        try {
            customRuntimeService.startRuntimes();
            serverStarted = true;
        } catch (RuntimeServiceException e) {
            Assert.assertFalse(serverStarted);
        }
    }

    @Test(dependsOnMethods = {"testStartBeforeInitRuntime"})
    public void testStopBeforeInitRuntime() {
        boolean serverStopped = false;
        try {
            customRuntimeService.stopRuntimes();
            serverStopped = true;
        } catch (RuntimeServiceException e) {
            Assert.assertFalse(serverStopped);
        }
    }

    @Test(dependsOnMethods = {"testStopBeforeInitRuntime"})
    public void testBeginMaintenanceBeforeInitRuntime() {
        boolean startMaintenance = false;
        try {
            customRuntimeService.beginMaintenance();
            startMaintenance = true;
        } catch (RuntimeServiceException e) {
            Assert.assertFalse(startMaintenance);
        }
    }

    @Test(dependsOnMethods = {"testBeginMaintenanceBeforeInitRuntime"})
    public void testEndMaintenanceBeforeInitRuntime() {
        boolean endMaintenance = false;
        try {
            customRuntimeService.endMaintenance();
            endMaintenance = true;
        } catch (RuntimeServiceException e) {
            Assert.assertFalse(endMaintenance);
        }
    }

    @Test(dependsOnMethods = {"testStartBeforeInitRuntime"})
    public void testInitRuntime() {
        try {
            for (Runtime runtime : runtimeManager.getRuntimeList()) {
                runtime.init();
                Assert.assertEquals(RuntimeState.INACTIVE, runtime.getState());
            }
        } catch (RuntimeServiceException e) {
            e.printStackTrace();
        }
    }

    @Test(dependsOnMethods = {"testInitRuntime"})
    public void testStartRuntime() {
        try {
            customRuntimeService.startRuntimes();
            for (Runtime runtime : runtimeManager.getRuntimeList()) {
                Assert.assertEquals(RuntimeState.ACTIVE, runtime.getState());
            }
        } catch (RuntimeServiceException e) {
            e.printStackTrace();
        }
    }

    @Test(dependsOnMethods = {"testStartRuntime"})
    public void testStartOnAlreadyStartedRuntime() {
        boolean serverStarted = false;
        try {
            customRuntimeService.startRuntimes();
            serverStarted = true;
        } catch (RuntimeServiceException e) {
            Assert.assertFalse(serverStarted);
        }
    }

    @Test(dependsOnMethods = {"testStartRuntime"})
    public void testBeginMaintenance() {
        try {
            customRuntimeService.beginMaintenance();
            for (Runtime runtime : runtimeManager.getRuntimeList()) {
                Assert.assertEquals(RuntimeState.MAINTENANCE, runtime.getState());
            }
        } catch (RuntimeServiceException e) {
            e.printStackTrace();
        }
    }

    @Test(dependsOnMethods = {"testBeginMaintenance"})
    public void testStartOnMaintenanceRuntime() {
        boolean serverStarted = false;
        try {
            customRuntimeService.startRuntimes();
            serverStarted = true;
        } catch (RuntimeServiceException e) {
            Assert.assertFalse(serverStarted);
        }
    }

    @Test(dependsOnMethods = {"testStartOnMaintenanceRuntime"})
    public void testEndMaintenance() {
        try {
            customRuntimeService.endMaintenance();
            for (Runtime runtime : runtimeManager.getRuntimeList()) {
                Assert.assertEquals(RuntimeState.INACTIVE, runtime.getState());
            }
        } catch (RuntimeServiceException e) {
            e.printStackTrace();
        }
    }

    @Test(dependsOnMethods = {"testEndMaintenance"})
    public void testEndRuntime() {
        try {
            customRuntimeService.stopRuntimes();
            for (Runtime runtime : runtimeManager.getRuntimeList()) {
                Assert.assertEquals(RuntimeState.INACTIVE, runtime.getState());
            }
        } catch (RuntimeServiceException e) {
            e.printStackTrace();
        }
    }

}
