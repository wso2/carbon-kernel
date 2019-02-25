package org.wso2.carbon.nextgen.config;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
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
    public void testDottedKeyProcessing(String key, Object value) throws ConfigParserException {
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

    @Test (dataProvider = "splitDataProvider")
    public void testSplitMethod(String input, String[] expectedOutput) {
        List<String> stringList = JinjaParser.splitWithoutEmptyStrings(input);

        Assert.assertEquals(stringList.size(), expectedOutput.length, "Split item count mismatch.");
        for (int i = 0; i < stringList.size(); i++) {
            Assert.assertEquals(stringList.get(i), expectedOutput[i], "Split value mismatch.");
        }
    }

    @Test (dataProvider = "dottedKeyArraySplitDataProvider")
    public void testDottedKeyArraySplit(String input, String[] expectedOutput) throws ConfigParserException {
        List<String> stringList = JinjaParser.getDottedKeyArray(input);

        Assert.assertEquals(stringList.size(), expectedOutput.length, "Split item count mismatch.");
        for (int i = 0; i < stringList.size(); i++) {
            Assert.assertEquals(stringList.get(i), expectedOutput[i], "Split value mismatch.");
        }
    }

    @DataProvider(name = "dottedKeyArraySplitDataProvider")
    public Object[][] dottedKeyArraySplitData() {
        return new Object[][]{
                {"one.two.three.four", new String[]{"one", "two", "three", "four"}},
                {".one.'two.three'", new String[]{"one", "two.three"}},
                {".one.'two.three'.four", new String[]{"one", "two.three", "four"}},
                {".one.two.'three.four'.five.six", new String[]{"one", "two", "three.four", "five", "six"}},
                {".one.two.'three.four.five'.six.seven", new String[]{"one", "two", "three.four.five", "six", "seven"}},
                };
    }

    @DataProvider(name = "splitDataProvider")
    public Object[][] splitTestData() {
        return new Object[][]{
                {"one.two.three", new String[]{"one", "two", "three"}},
                {".one.two.three.", new String[]{"one", "two", "three"}},
                {".one.two.four.", new String[]{"one", "two", "four"}},
                {".one.two.three..four.", new String[]{"one", "two", "three", "four"}},
                };
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
