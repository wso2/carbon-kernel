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
package org.wso2.carbon.kernel.internal.context;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.CarbonRuntime;

/**
 * Unit test class for org.wso2.carbon.kernel.internal.context.CarbonRuntimeFactory.
 *
 * @since 5.0.0
 */
public class CarbonRuntimeFactoryTest {
    private CarbonRuntime carbonRuntime;

    @BeforeTest
    public void setup() throws Exception {
        ConfigProvider configProvider = new CarbonConfigProviderImpl();
        carbonRuntime = CarbonRuntimeFactory.createCarbonRuntime(configProvider);
    }

    @Test
    public void testDefaultCarbonRuntime() {
        Assert.assertNotNull(carbonRuntime.getConfiguration());
    }
}
