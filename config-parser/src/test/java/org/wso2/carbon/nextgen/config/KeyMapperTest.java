package org.wso2.carbon.nextgen.config;

import org.apache.commons.io.FileUtils;
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
    private Map<String, Object> outputFromToml = new HashMap<>();

    @BeforeClass
    public void setUp() {

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
        String mappingConfiguration =
                FileUtils.getFile("src", "test", "resources", "test-mapping.toml").getAbsolutePath();
        outputFromToml = KeyMapper.mapWithTomlConfig(inputMap, mappingConfiguration);
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

    @Test(dataProvider = "mappedKeyValues")
    public void testKeyMappingWithTomlFile(String oldKey, String newKey, String value) {

        Assert.assertEquals(outputFromToml.get(newKey), value, "Value was not mapped to new key");
        Assert.assertNull(outputFromToml.get(oldKey), "Old key [ " + oldKey + " ] should not be present in the mapped "
                + "values");
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
