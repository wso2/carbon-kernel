package org.wso2.ei.config;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Jinja template parsing test class.
 */
public class JinjaParserTest {

    @Test(dataProvider = "flatKeySetProvider")
    public void testDottedKeyProcessing(String key, Object value) {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(key, value);
        Map<String, Object> outputMap = JinjaParser.getHierarchicalDottedKeyMap(inputMap);
        String[] dottedKeyArray = key.split("\\.");

        Map<String, Object> lastKeyMap = outputMap;
        for (int i = 0; i < dottedKeyArray.length - 1; i++) {
            Object obj = lastKeyMap.get(dottedKeyArray[i]);
            Assert.assertTrue(obj instanceof Map, "Value should be a map");
            lastKeyMap = (Map) obj;
        }
        Assert.assertEquals(lastKeyMap.get(dottedKeyArray[dottedKeyArray.length - 1]), value);
    }


    @DataProvider(name = "flatKeySetProvider")
    public Object[][] nonExistingQueues() {
        return new Object[][]{
                {"a.b.c", "value"},
                {"a", "testValue"},
                };
    }


}
