/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.nextgen.config;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.nextgen.config.model.Context;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class UnitResolverTest {

    private static final String UNIT_TOML = "test-unit-resolve.json";

    @Test(dataProvider = "validDataProvider")
    public void testUpdateUnits(String key, Object value, String expected) throws ConfigParserException {

        Context context = new Context();
        String unitConfiguration =
                FileUtils.getFile("src", "test", "resources", UNIT_TOML).getAbsolutePath();
        context.getTemplateData().put(key, value);
        UnitResolver.updateUnits(context, unitConfiguration);
        assertEquals(context.getTemplateData().get(key), expected);
    }

    @Test(dataProvider = "invalidDataProvider")
    public void testInvalidValues(String key, Object value, String expectedErrorRegex) {

        Context context = new Context();
        String unitConfiguration =
                FileUtils.getFile("src", "test", "resources", UNIT_TOML).getAbsolutePath();
        context.getTemplateData().put(key, value);
        try {
            UnitResolver.updateUnits(context, unitConfiguration);
            fail(String.format("Expected error message matching regex '%s' for %s = %s", expectedErrorRegex, key,
                    value));
        } catch (ConfigParserException e) {
            assertTrue(e.getMessage() != null && e.getMessage().matches(expectedErrorRegex),
                    String.format("Expected error message matching regex '%s' for %s = %s, but was %s",
                            expectedErrorRegex, key, value, e.getMessage()));
        }
    }

    @DataProvider(name = "validDataProvider")
    public Object[][] unitResolverDataSet() {

        return new Object[][]{
                {"a.b.m", 10, "10"},  // hr -> min
                {"a.b.m", "10h", "600"},  // hr -> min
                {"a.b.m", "10 h", "600"}, // hr -> min with space
                {"a.b.m", "10     h", "600"}, // hr -> min with multiple spaces
                {"a.b.s", "1m", "60"},    // min -> s
                {"a.b.s", "1", "1"},      // with no unit
                {"a.b.a", "1h", "1h"},    // key not defined as time value
                {"a.b.ms", "1m", "60000"},    // min -> ms
                {"a.b.m", "1d", "1440"},    // day -> min
        };
    }

    @DataProvider(name = "invalidDataProvider")
    public Object[][] invalidDataSet() {

        return new Object[][]{
                {"a.b.m", "adw", "Invalid configuration value.*"},
                {"a.b.m", "10x", "Invalid unit.*"},
                {"a.b.m", null, ".*Value is null.*"},
                {"a.b.m", new Object(), ".*Value type is.*"},
                {"a.b.m", "10ms", ".*Converted value result in 0 for non zero source value.*"},
        };
    }
}
