package org.wso2.carbon.nextgen.config;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test key mapping with the mappings file.
 */
public class KeyMapperWithMappingsFileTest {

    private Map<String, Object> mappedKeysFromConfig;

    public KeyMapperWithMappingsFileTest() throws ConfigParserException {

        Object[][] flatKeyConfigs = keyMappingDataSet();
        Map<String, Object> inputMap = new HashMap<>();
        for (Object[] flatKeyConfig : flatKeyConfigs) {
            inputMap.put((String) flatKeyConfig[0], flatKeyConfig[2]);
        }

        Object[][] unmappedConfigs = unmappedKeySet();
        for (Object[] config : unmappedConfigs) {
            inputMap.put((String) config[0], config[1]);
        }

        String mappingConfiguration =
                FileUtils.getFile("src", "test", "resources", "test-mapping.json").getAbsolutePath();
        mappedKeysFromConfig = KeyMapper.mapWithConfig(inputMap, mappingConfiguration);
    }

    @Test(dataProvider = "mappedKeyValues")
    public void testKeyMappingWithConfigFile(String oldKey, String newKey, String value) {

        Assert.assertEquals(mappedKeysFromConfig.get(newKey), value, "Value was not mapped to new key");
        Assert.assertNull(mappedKeysFromConfig.get(oldKey), "Old key [ " + oldKey + " ] should not be present in the "
                                                            + "mapped "
                                                            + "values");
    }

    @DataProvider(name = "mappedKeyValues")
    public Object[][] keyMappingDataSet() {

        return new Object[][]{
                {"old_key_1", "newKey1", "value1"},
                {"old_key_2", "new-key-2", "value2"},
                {"org.wso2.old_key", "org.wso2.oldKey", "value3"},
                };
    }

    @DataProvider(name = "unmappedKeyValues")
    private Object[][] unmappedKeySet() {

        return new Object[][]{
                {"no_mapping_key", "testValue"}
        };
    }

}


