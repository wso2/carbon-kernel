/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.nextgen.config;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
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
    public void testResolve(Map<String, Object> context, String key, Object expected) throws ConfigParserException {

        Map secrets = Collections.emptyMap();
        Map resolvedSystemProperties = new HashMap();
        Map resolvedEnvironmentVariables = new HashMap();
        ReferenceResolver.resolve(context, secrets, resolvedSystemProperties, resolvedEnvironmentVariables);
        Object actual = context.get(key);
        if (expected instanceof ArrayList) {
            ArrayList<String> expectedArrayList = (ArrayList<String>) expected;
            ArrayList<String> actualArrayList = (ArrayList<String>) actual;
            for (int i = 0; i < expectedArrayList.size(); i++) {
                Assert.assertEquals(actualArrayList.get(i), expectedArrayList.get(i),
                                    "Incorrect resolved value for " + key);
            }
        } else {
            Assert.assertEquals(actual, expected, "Incorrect resolved value for " + key);
        }
    }

    @Test(dataProvider = "invalidReferencesProvider", expectedExceptions = ConfigParserException.class)
    public void testResolve(Map<String, Object> context, String key) throws ConfigParserException {

        Map secrets = new HashMap();
        Map resolvedSystemProperties = new HashMap();
        Map resolvedEnvironmentVariables = new HashMap();
        ReferenceResolver.resolve(context, secrets, resolvedSystemProperties, resolvedEnvironmentVariables);
        Assert.fail("Placeholder reference resolution should have been failed.");
    }

    @Test(dataProvider = "secretProvider")
    public void testResolve(Map<String, Object> context) throws ConfigParserException {

        Map secrets = new HashMap();
        secrets.put("b.c.d", "sssssss");
        Map resolvedSystemProperties = new HashMap();
        Map resolvedEnvironmentVariables = new HashMap();
        ReferenceResolver.resolve(context, secrets, resolvedSystemProperties, resolvedEnvironmentVariables);
    }

    @Test(dataProvider = "secretProvider")
    public void testResolveNegative1(Map<String, Object> context) {
        Map secrets = new HashMap();
        Map resolvedSystemProperties = new HashMap();
        Map resolvedEnvironmentVariables = new HashMap();
        secrets.put("b.c.d", "[sssssss]");
        try {
            ReferenceResolver.resolve(context, secrets, resolvedSystemProperties, resolvedEnvironmentVariables);
            Assert.fail();
        } catch (ConfigParserException e) {
            Assert.assertTrue(e.getMessage().contains("Secret references can't be plain text for "));
        }
    }

    @Test(dataProvider = "secretProviderNegative")
    public void testResolveNegative2(Map<String, Object> context) {
        Map secrets = new HashMap();
        Map resolvedSystemProperties = new HashMap();
        Map resolvedEnvironmentVariables = new HashMap();

        try {
            ReferenceResolver.resolve(context, secrets, resolvedSystemProperties, resolvedEnvironmentVariables);
            Assert.fail();
        } catch (ConfigParserException e) {
            Assert.assertTrue(e.getMessage().contains("Secret references can't be resolved for b.d.d"));
        }
    }

    @DataProvider(name = "contextProvider")
    public Object[][] resolverDataSet() {

        ArrayList<String> referenceArray = new ArrayList<>();
        referenceArray.add("$ref{fa}:$ref{fb}");
        referenceArray.add("$ref{fb}");

        ArrayList<String> expectedArray = new ArrayList<>();
        expectedArray.add("AAA:BBB");
        expectedArray.add("BBB");

        Map<String, Object> fileContextPlaceholders = new HashMap<>();
        Map<String, Object> systemContextPlaceholders = new HashMap<>();
        fileContextPlaceholders.put("fa", "AAA");
        fileContextPlaceholders.put("fa1", "$ref{fa}");
        fileContextPlaceholders.put("fa2", "$ref{fa1}");
        fileContextPlaceholders.put("fb", "BBB");
        fileContextPlaceholders.put("fb1", "$ref{fa}-$ref{fb}");
        fileContextPlaceholders.put("fb2", "$ref{fa1}-$ref{fb}");
        fileContextPlaceholders.put("fc", true);
        fileContextPlaceholders.put("fc1", "$ref{fc}");
        fileContextPlaceholders.put("fc2", "$ref{fc1}");
        fileContextPlaceholders.put("fc3", referenceArray);
        systemContextPlaceholders.put("sa", "$sys{syskey1}");
        systemContextPlaceholders.put("sb", "$sys{syskey1}-AAA");
        systemContextPlaceholders.put("sc", "$sys{syskey1}-$sys{syskey2}");
        return new Object[][]{
                {fileContextPlaceholders, "fa", "AAA"},
                {fileContextPlaceholders, "fa1", "AAA"},
                {fileContextPlaceholders, "fa2", "AAA"},
                {fileContextPlaceholders, "fb1", "AAA-BBB"},
                {fileContextPlaceholders, "fb2", "AAA-BBB"},
                {fileContextPlaceholders, "fc1", true},
                {fileContextPlaceholders, "fc2", true},
                {fileContextPlaceholders, "fc3", expectedArray},
                {systemContextPlaceholders, "sa", "sysval1"},
                {systemContextPlaceholders, "sb", "sysval1-AAA"},
                {systemContextPlaceholders, "sc", "sysval1-sysval2"},
        };
    }

    @DataProvider(name = "invalidReferencesProvider")
    public Object[][] invalidResolverDataSet() {

        Map<String, Object> invalidReferenceContext1 = new HashMap<>();
        Map<String, Object> invalidReferenceContext2 = new HashMap<>();
        invalidReferenceContext1.put("fc", "$ref{fd}");
        invalidReferenceContext1.put("fd", "$ref{fc}");
        invalidReferenceContext2.put("fe", "$ref{fz}");
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
        context.put("aaa.bbb.ccc", "$ref{deployment_admin_password}");
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
        context.put("aaa.bbb.ccc", "$ref{server.admin.password}");
        context.put("deployment.admin.password", "$secret{b.d.d}");
        return new Object[][]{
                {context}
        };
    }

}
