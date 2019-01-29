package org.wso2.ei.config;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for converting flat dotted keys into hierarchical key model expected by Jinja parser.
 */
public class JinjaParserTest {

    private Map<String, Object> inputMap = new HashMap<>();

    @BeforeMethod
    public void setUp() {
        for (int i = 0; i < flatKeyDataSet().length; i++) {
            inputMap.put((String) flatKeyDataSet()[i][0], flatKeyDataSet()[i][1]);
        }
    }

    @Test(dataProvider = "flatKeySetProvider")
    public void testDottedKeyProcessing(String key, Object value) {
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
    public Object[][] flatKeyDataSet() {
        return new Object[][]{
                {"a.b.c", "value1"},
                {"a.b.d", "value2"},
                {"e", "value3"},
                {"f.g", 1234}
        };
    }
}
