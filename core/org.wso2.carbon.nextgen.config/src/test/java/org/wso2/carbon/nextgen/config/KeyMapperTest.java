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
import org.wso2.carbon.nextgen.config.model.Context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test key mapping functionality.
 */
public class KeyMapperTest {

    private Context inputContext = new Context();
    private Map<String, Object> keyMappings = new HashMap<>();
    private Context outputContext;

    @BeforeClass
    public void setUp() throws ConfigParserException {

        Object[][] flatKeyConfigs = keyMappingDataSet();
        for (Object[] flatKeyConfig : flatKeyConfigs) {
            inputContext.getTemplateData().put((String) flatKeyConfig[0], flatKeyConfig[2]);

            Object newKey = flatKeyConfig[1];
            if (newKey != null) { // no mapping
                keyMappings.put((String) flatKeyConfig[0], flatKeyConfig[1]);
            }
        }

        Object[][] unmappedConfigs = unmappedKeySet();
        for (Object[] config : unmappedConfigs) {
            inputContext.getTemplateData().put((String) config[0], config[1]);
        }
        outputContext = KeyMapper.map(inputContext, keyMappings);
    }

    @Test(dataProvider = "mappedKeyValues")
    public void testMappedKeyParsing(String oldKey, Object newKey, String value) {

        if (newKey instanceof String) {
            Assert.assertEquals(outputContext.getTemplateData().get(newKey), value, "Value was not mapped to new key");
        } else if (newKey instanceof List) {
            ((List) newKey).forEach(key -> {
                Assert.assertEquals(outputContext.getTemplateData().get(key), value, "Value was not mapped to new key");
            });
        }
        Assert.assertNull(outputContext.getTemplateData().get(oldKey), "Old key [ " + oldKey + " ] should not be" +
                " present in the mapped values");
    }

    @Test(dataProvider = "unmappedKeyValues")
    public void testUnMappedKeyParsing(String key, String value) {

        Assert.assertEquals(outputContext.getTemplateData().get(key), value, "Value was not mapped to key");
    }

    @DataProvider(name = "mappedKeyValues")
    public Object[][] keyMappingDataSet() {

        List list = Arrays.asList("a.b.c.d.e", "x.y.z");
        return new Object[][]{
                {"old_key_1", "newKey1", "value1"},
                {"old_key_2", "new-key-2", "value2"},
                {"org.wso2.old_key", "org.wso2.oldKey", "value3"},
                {"a_b_c_d", list, "value4"},
                {"a:b.c.d", "x:y.z", "value5"}
        };
    }

    @DataProvider(name = "unmappedKeyValues")
    private Object[][] unmappedKeySet() {

        return new Object[][]{
                {"no_mapping_key", "testValue"}
        };
    }
}
