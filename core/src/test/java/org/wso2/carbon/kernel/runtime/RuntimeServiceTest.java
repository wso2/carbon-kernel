/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.internal.runtime.RuntimeManager;
import org.wso2.carbon.kernel.runtime.exception.RuntimeServiceException;
import org.wso2.carbon.kernel.runtime.runtime.CustomRuntime;
import org.wso2.carbon.kernel.runtime.service.CustomRuntimeService;
import org.wso2.carbon.kernel.runtime.spi.Runtime;

/**
 * Runtime Service Test class.
 */
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
    public void testInitRuntime() throws RuntimeServiceException {
        for (Runtime runtime : runtimeManager.getRuntimeList()) {
            runtime.init();
            Assert.assertEquals(RuntimeState.INACTIVE, runtime.getState());
        }
    }

    @Test(dependsOnMethods = {"testInitRuntime"})
    public void testStartRuntime() throws RuntimeServiceException {
        customRuntimeService.startRuntimes();
        for (Runtime runtime : runtimeManager.getRuntimeList()) {
            Assert.assertEquals(RuntimeState.ACTIVE, runtime.getState());
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
    public void testBeginMaintenance() throws RuntimeServiceException {
        customRuntimeService.beginMaintenance();
        for (Runtime runtime : runtimeManager.getRuntimeList()) {
            Assert.assertEquals(RuntimeState.MAINTENANCE, runtime.getState());
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
    public void testEndMaintenance() throws RuntimeServiceException {
        customRuntimeService.endMaintenance();
        for (Runtime runtime : runtimeManager.getRuntimeList()) {
            Assert.assertEquals(RuntimeState.INACTIVE, runtime.getState());
        }
    }

    @Test(dependsOnMethods = {"testEndMaintenance"})
    public void testEndRuntime() throws RuntimeServiceException {
        customRuntimeService.stopRuntimes();
        for (Runtime runtime : runtimeManager.getRuntimeList()) {
            Assert.assertEquals(RuntimeState.INACTIVE, runtime.getState());
        }
    }

}
