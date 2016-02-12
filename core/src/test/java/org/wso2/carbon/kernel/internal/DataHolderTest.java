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
package org.wso2.carbon.kernel.internal;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.internal.runtime.RuntimeManager;

/**
 * Unit tests for org.wso2.carbon.kernel.internal.DataHolder class.
 *
 * @since 5.0.0
 */
public class DataHolderTest {
    private DataHolder dataHolder = null;
    private RuntimeManager runtimeManager = null;

    @BeforeClass
    public void setup() {
        dataHolder = DataHolder.getInstance();
        runtimeManager = new RuntimeManager();
    }

    @Test
    public void testDataHolderGetInstance() {
        Assert.assertNotNull(dataHolder);
    }

    @Test
    public void testRuntimeManager() {
        dataHolder.setRuntimeManager(runtimeManager);
        Assert.assertEquals(runtimeManager, dataHolder.getRuntimeManager(), "RuntimeManager set in the setter is" +
                "different from the RumtimeManager returned by the getter.");
    }
}
