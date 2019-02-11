package org.wso2.carbon.nextgen.config;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
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

    @BeforeClass
    public void setUp() {

        String deploymentConfiguration =
                FileUtils.getFile("src", "test", "resources", TOML_FILE_NAME).getAbsolutePath();

        parsedValueMap = TomlParser.parse(deploymentConfiguration);
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
        return new Object[][]{
                {"header_test.b.c", "value1"},
                {"header_test.b.d", "value2"},
                {"key", "value3"},
                };
    }

}
