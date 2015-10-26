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
package org.wso2.carbon.kernel.internal.startupresolver;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * This class tests the functionality of org.wso2.carbon.kernel.internal.startupresolver.MultiCounter.
 *
 * @since 5.0.0
 */
public class MultiCounterTest {

    private MultiCounter<String> multiCounter;
    private int randomInt;
    private Set<String> keySet;

    @BeforeClass
    public void init() {
        multiCounter = new MultiCounter<>();
        Double randomNumber = Math.random() % 100;
        randomInt = randomNumber.intValue() + 100;
        keySet = new HashSet<>();
        String key;

        for (int i = 0; i < randomInt; i++) {
            key = "key-" + i;
            multiCounter.incrementAndGet(key);
            keySet.add(key);
        }
    }

    @Test
    public void testIncrementAndGet() throws Exception {
        keySet.add("test-key");
        for (int i = 0; i < randomInt; i++) {
            Assert.assertEquals(multiCounter.incrementAndGet("test-key"), i + 1);
        }
    }

    @Test(dependsOnMethods = "testIncrementAndGet")
    public void testDecrementAndGet() throws Exception {
        Assert.assertEquals(multiCounter.decrementAndGet("test-key"), randomInt - 1);
    }

    @Test(dependsOnMethods = "testDecrementAndGet")
    public void testGet() throws Exception {
        Assert.assertEquals(multiCounter.get("test-key"), randomInt - 1);
    }

    @Test(dependsOnMethods = "testIncrementAndGet")
    public void testGetAllKeys() throws Exception {
        keySet.equals(multiCounter.getAllKeys());
    }
}
