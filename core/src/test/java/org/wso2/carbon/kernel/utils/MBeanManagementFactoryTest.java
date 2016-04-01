/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.kernel.utils;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.management.MBeanServer;

/**
 * Unit test class for org.wso2.carbon.kernel.utils.MBeanManagementFactory.
 *
 * @since 5.1.0
 */
public class MBeanManagementFactoryTest {

    private MBeanServer mBeanServer;

    @BeforeTest
    public void setup() throws Exception {
        mBeanServer = MBeanManagementFactory.getMBeanServer();
    }

    @Test
    public void testMBeanManagementFactory() {
        Assert.assertNotNull(mBeanServer);
    }
}
