package org.wso2.ei.config;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class ReferenceResolverTest {

    @BeforeClass
    public void setupSystemProperties() {

        System.setProperty("syskey1", "sysval1");
        System.setProperty("syskey2", "sysval2");
    }

    @AfterClass
    public void clearSystemProperties() {

        System.clearProperty("syskey1");
        System.clearProperty("syskey2");
    }

    @Test(dataProvider = "contextProvider")
    public void testResolve(Map<String, Object> context, String key, Object expected) throws ValidationException {

        ReferenceResolver.resolve(context);
        Object actual = context.get(key);
        Assert.assertEquals(actual, expected, "Incorrect resolved value for " + key);
    }

    @Test(dataProvider = "invalidReferencesProvider", expectedExceptions = ValidationException.class)
    public void testResolve(Map<String, Object> context, String key) throws ValidationException {

        ReferenceResolver.resolve(context);
        Assert.fail("Placeholder reference resolution should have been failed.");
    }

    @DataProvider(name = "contextProvider")
    public Object[][] resolverDataSet() {

        Map<String, Object> fileContextPlaceholders = new HashMap<>();
        Map<String, Object> systemContextPlaceholders = new HashMap<>();
        Map<String, Object> environmentContextPlaceholders = new HashMap<>();
        Map<String, Object> complexPlaceholders = new HashMap<>();
        fileContextPlaceholders.put("fa", "AAA");
        fileContextPlaceholders.put("fa1", "${fa}");
        fileContextPlaceholders.put("fa2", "${fa1}");
        fileContextPlaceholders.put("fb", "BBB");
        fileContextPlaceholders.put("fb1", "${fa}-${fb}");
        fileContextPlaceholders.put("fb2", "${fa1}-${fb}");
        systemContextPlaceholders.put("sa", "$sys{syskey1}");
        systemContextPlaceholders.put("sb", "$sys{syskey1}-AAA");
        systemContextPlaceholders.put("sc", "$sys{syskey1}-$sys{syskey2}");
        return new Object[][]{
                {fileContextPlaceholders, "fa", "AAA"},
                {fileContextPlaceholders, "fa1", "AAA"},
                {fileContextPlaceholders, "fa2", "AAA"},
                {fileContextPlaceholders, "fb1", "AAA-BBB"},
                {fileContextPlaceholders, "fb2", "AAA-BBB"},
                {systemContextPlaceholders, "sa", "sysval1"},
                {systemContextPlaceholders, "sb", "sysval1-AAA"},
                {systemContextPlaceholders, "sc", "sysval1-sysval2"},
        };
    }

    @DataProvider(name = "invalidReferencesProvider")
    public Object[][] invalidResolverDataSet() {

        Map<String, Object> invalidReferenceContext1 = new HashMap<>();
        Map<String, Object> invalidReferenceContext2 = new HashMap<>();
        invalidReferenceContext1.put("fc", "${fd}");
        invalidReferenceContext1.put("fd", "${fc}");
        invalidReferenceContext2.put("fe", "${fz}");
        return new Object[][]{
                {invalidReferenceContext1, "fc"},
                {invalidReferenceContext2, "fe"},
        };
    }
}
