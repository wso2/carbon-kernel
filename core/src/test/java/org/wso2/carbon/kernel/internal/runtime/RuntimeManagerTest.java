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
package org.wso2.carbon.kernel.internal.runtime;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.deployment.exception.DeployerRegistrationException;
import org.wso2.carbon.kernel.deployment.exception.DeploymentEngineException;
import org.wso2.carbon.kernel.runtime.Runtime;

import java.util.List;

/**
 * Test class to test org.wso2.carbon.kernel.internal.runtime.RuntimeManager.
 *
 * @since 5.0.0
 */
public class RuntimeManagerTest {

    private RuntimeManager runtimeManager;
    private Runtime runtime;

    @BeforeTest
    public void setup() throws DeploymentEngineException, DeployerRegistrationException {
        runtimeManager = new RuntimeManager();
        runtime = new CustomRuntime();
        runtimeManager.registerRuntime(runtime);
    }

    @Test
    public void testGetRuntimesList() {
        List<Runtime> runtimeList = runtimeManager.getRuntimeList();
        Assert.assertEquals(runtimeList.size(), 1, "Should contain one runtime");
        Assert.assertEquals(runtimeList.get(0), runtime, "Runtime fetched from the runtimeManager does not match" +
                "with the runtime set in the runtimeManager.");
    }

    @Test(dependsOnMethods = {"testGetRuntimesList"})
    public void testUnRegisterRuntime() {
        runtimeManager.unRegisterRuntime(runtime);
        Assert.assertEquals(runtimeManager.getRuntimeList().size(), 0, "After unregistration there should be no" +
                "runtimes in the runtime manager");
    }
}
