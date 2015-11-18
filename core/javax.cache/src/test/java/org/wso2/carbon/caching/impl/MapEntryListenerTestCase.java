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
package org.wso2.carbon.caching.impl;

import org.testng.annotations.Test;

import java.util.Map;


/**
 * Tests for MapEntryListener interface.
 */
public class MapEntryListenerTestCase {
    private Map<String, String> map;

    public MapEntryListenerTestCase() {
        SampleDistributedMapProvider distributedMapProvider = new SampleDistributedMapProvider("sampleDistMap1");
        this.map = distributedMapProvider.getMap("sampleDistMap1", null);
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "entryAdded event got executed.")
    public void mapEntryListenerEntryAddedTest() throws Exception {
        this.map.put("sampleKey1", "sampleValue1");
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "entryRemoved event got executed.")
    public void mapEntryListenerEntryRemovedTest() throws Exception {
        this.map.remove("sampleKey1");
    }


    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "mapCleared event got executed.")
    public void mapEntryListenerMapClearedTest() throws Exception {
        this.map.clear();
    }
}
