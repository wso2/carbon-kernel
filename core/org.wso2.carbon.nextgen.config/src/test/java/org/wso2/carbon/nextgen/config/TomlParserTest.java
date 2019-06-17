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

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.nextgen.config.model.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Test class to check the parsed output map values of the {@link TomlParser} class.
 */
public class TomlParserTest {

    private static final String TOML_FILE_NAME = "test.toml";

    private Map<String, Object> parsedValueMap = new HashMap<>();

    @BeforeClass
    public void setUp() throws ConfigParserException {

        Context context = new Context();
        String deploymentConfiguration =
                FileUtils.getFile("src", "test", "resources", TOML_FILE_NAME).getAbsolutePath();

        ConfigParser.ConfigPaths.setConfigFilePath(deploymentConfiguration);
        context = TomlParser.parse(context);
        parsedValueMap.putAll(context.getTemplateData());
    }

    @Test(dataProvider = "flatKeySetProvider")
    public void testParse(String key, Object value) {
        Object objValue = parsedValueMap.get(key);
        Assert.assertNotNull(objValue, "Invalid value for key " + key);
        Assert.assertEquals(objValue.getClass(), value.getClass(), "Value type mismatch");
        Assert.assertEquals(parsedValueMap.get(key), value, "Value didn't match. Toml parsing error");
    }

    @DataProvider(name = "flatKeySetProvider")
    public Object[][] flatKeyDataSet() {
        LinkedHashMap<String, String> data = new LinkedHashMap<String, String>();
        ArrayList<Object> result = new ArrayList<Object>();
        data.put("'a.b'", "value6");
        result.add(data);
        return new Object[][]{
                {"header_test.b.c", "value1"},
                {"header_test.b.d", "value2"},
                {"key", "value3"},
                {"a.'b.c'", "value4"},
                {"a.'d.e'", "value5"},
                {"single_quote_test", result},
        };
    }

}
