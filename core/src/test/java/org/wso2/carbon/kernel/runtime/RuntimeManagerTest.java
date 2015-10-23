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
import org.wso2.carbon.kernel.runtime.spi.Runtime;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime Manager Test class.
 */
public class RuntimeManagerTest {
    RuntimeManager runtimeManager;
    CustomRuntime customRuntime;
    private List<Runtime> runtimeList = new ArrayList<Runtime>();

    @BeforeTest
    public void setup() throws RuntimeServiceException {
        customRuntime = new CustomRuntime();
        runtimeManager = new RuntimeManager();
    }

    @Test
    public void testRegisterRuntime() {
        runtimeManager.registerRuntime(customRuntime);
        Assert.assertEquals(1, runtimeManager.getRuntimeList().size());
    }

    @Test(dependsOnMethods = {"testRegisterRuntime"})
    public void testUnRegisterRuntime() {
        runtimeManager.unRegisterRuntime(customRuntime);
        Assert.assertEquals(0, runtimeManager.getRuntimeList().size());
    }

}
