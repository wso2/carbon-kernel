/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.nextgen.config;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test key mapping functionality.
 */
public class KeyMapperTest {

    private Map<String, Object> inputMap = new HashMap<>();
    private Map<String, String> keyMappings = new HashMap<>();
    private Map<String, Object> outputMap = new HashMap<>();

    @BeforeClass
    public void setUp() throws ConfigParserException {

        Object[][] flatKeyConfigs = keyMappingDataSet();
        for (Object[] flatKeyConfig : flatKeyConfigs) {
            inputMap.put((String) flatKeyConfig[0], flatKeyConfig[2]);

            String newKey = (String) flatKeyConfig[1];
            if (!newKey.isEmpty()) { // no mapping
                keyMappings.put((String) flatKeyConfig[0], (String) flatKeyConfig[1]);
            }
        }

        Object[][] unmappedConfigs = unmappedKeySet();
        for (Object[] config : unmappedConfigs) {
            inputMap.put((String) config[0], config[1]);
        }
        outputMap = KeyMapper.map(inputMap, keyMappings);
    }

    @Test(dataProvider = "mappedKeyValues")
    public void testMappedKeyParsing(String oldKey, String newKey, String value) {

        Assert.assertEquals(outputMap.get(newKey), value, "Value was not mapped to new key");
        Assert.assertNull(outputMap.get(oldKey), "Old key [ " + oldKey + " ] should not be present in the mapped "
                                                 + "values");
    }

    @Test(dataProvider = "unmappedKeyValues")
    public void testUnMappedKeyParsing(String key, String value) {

        Assert.assertEquals(outputMap.get(key), value, "Value was not mapped to key");
    }

    @DataProvider(name = "mappedKeyValues")
    public Object[][] keyMappingDataSet() {

        return new Object[][]{
                {"old_key_1", "newKey1", "value1"},
                {"old_key_2", "new-key-2", "value2"},
                {"org.wso2.old_key", "org.wso2.oldKey", "value3"}
        };
    }

    @DataProvider(name = "unmappedKeyValues")
    private Object[][] unmappedKeySet() {

        return new Object[][]{
                {"no_mapping_key", "testValue"}
        };
    }
}
