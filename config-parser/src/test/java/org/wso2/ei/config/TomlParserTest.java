package org.wso2.ei.config;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test class to check the parsed output map values of the {@link TomlParser} class.
 */
public class TomlParserTest {

    private static final String TOML_FILE_NAME = "test.toml";

    private Map<String, Object> parsedValueMap = new HashMap<>();

    @BeforeMethod
    public void setUp() {
        parsedValueMap = TomlParser.parse(TOML_FILE_NAME);
    }

    @Test(dataProvider = "flatKeySetProvider")
    public void testParse(String key, Object value) {

        Object objValue = parsedValueMap.get(key);
        Assert.assertEquals(objValue.getClass(), value.getClass(), "Value type mismatch");
        Assert.assertEquals(parsedValueMap.get(key), value, "Value didn't match. Toml parsing error");
    }

    @DataProvider(name = "flatKeySetProvider")
    public Object[][] flatKeyDataSet() {
        return new Object[][]{
                {"a.b.c", "value1"},
                {"a.b.d", "value2"},
                {"e", "value3"},
                };
    }

}
