package org.wso2.carbon.nextgen.config;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
    public void testResolve(Map<String, Object> context, String key, Object expected) throws ConfigParserException {

        Properties secrets = new Properties();
        ReferenceResolver.resolve(context, secrets);
        Object actual = context.get(key);
        Assert.assertEquals(actual, expected, "Incorrect resolved value for " + key);
    }

    @Test(dataProvider = "invalidReferencesProvider", expectedExceptions = ConfigParserException.class)
    public void testResolve(Map<String, Object> context, String key) throws ConfigParserException {
        Properties secrets = new Properties();
        ReferenceResolver.resolve(context, secrets);
        Assert.fail("Placeholder reference resolution should have been failed.");
    }

    @Test(dataProvider = "secretProvider")
    public void testResolve(Map<String, Object> context) throws ConfigParserException {
        Properties secrets = new Properties();
        secrets.put("b.c.d","sssssss");
        ReferenceResolver.resolve(context, secrets);
    }

    @Test(dataProvider = "secretProviderNegative")
    public void testResolveNegative(Map<String, Object> context) {
        Properties secrets = new Properties();

        try {
            ReferenceResolver.resolve(context, secrets);
            Assert.fail();
        } catch (ConfigParserException e) {
            Assert.assertTrue(e.getMessage().contains("Secret References can't be resolved for b.d.d"));
        }
    }

    @DataProvider(name = "contextProvider")
    public Object[][] resolverDataSet() {

        Map<String, Object> fileContextPlaceholders = new HashMap<>();
        Map<String, Object> systemContextPlaceholders = new HashMap<>();
        Map<String, Object> environmentContextPlaceholders = new HashMap<>();
        Map<String, Object> complexPlaceholders = new HashMap<>();
        fileContextPlaceholders.put("fa", "AAA");
        fileContextPlaceholders.put("fa1", "$conf{fa}");
        fileContextPlaceholders.put("fa2", "$conf{fa1}");
        fileContextPlaceholders.put("fb", "BBB");
        fileContextPlaceholders.put("fb1", "$conf{fa}-$conf{fb}");
        fileContextPlaceholders.put("fb2", "$conf{fa1}-$conf{fb}");
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
        invalidReferenceContext1.put("fc", "$conf{fd}");
        invalidReferenceContext1.put("fd", "$conf{fc}");
        invalidReferenceContext2.put("fe", "$conf{fz}");
        return new Object[][]{
                {invalidReferenceContext1, "fc"},
                {invalidReferenceContext2, "fe"},
        };
    }

    @DataProvider(name = "secretProvider")
    public Object[][] secretDataSet() {

        Map<String, Object> context = new HashMap<>();
        context.put("secrets.a.b.c", "aaaaa");
        context.put("secrets.b.c.d", "aaaaaaaa");
        context.put("aaa.bbb.ccc", "$conf{deployment_admin_password}");
        context.put("deployment_admin_password", "$secret{b.c.d}");
        return new Object[][]{
                {context}
        };
    }

    @DataProvider(name = "secretProviderNegative")
    public Object[][] secretDataSet1() {

        Map<String, Object> context = new HashMap<>();
        context.put("secrets.a.b.c", "aaaaa");
        context.put("secrets.b.c.d", "aaaaaaaa");
        context.put("aaa.bbb.ccc", "$conf{deployment.admin.password}");
        context.put("deployment.admin.password", "$secret{b.d.d}");
        return new Object[][]{
                {context}
        };
    }

}
