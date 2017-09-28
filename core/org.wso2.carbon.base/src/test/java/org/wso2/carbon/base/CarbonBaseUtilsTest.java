/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.base;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by kasun on 9/26/17.
 */

public class CarbonBaseUtilsTest {
    private static final Logger logger = Logger.getLogger(CarbonBaseUtilsTest.class);

    @BeforeClass

    public static void setSecurityManager() {
        URL resourceURL = CarbonBaseUtilsTest.class.getClassLoader().getResource("");
        if (resourceURL != null) {
            String resourcePath = resourceURL.getPath();
            resourcePath = resourcePath + "policy-test.policy";
            System.setProperty("java.security.policy", resourcePath);
            System.setSecurityManager(new SecurityManager());
           /* System.setProperty("java.security.policy", Paths.get("src/test/resources").toString() + "/testPolicy.policy");
            System.setSecurityManager(new SecurityManager());*/
        }
    }


    @AfterClass
    public static void clearSecurityManager() {
        System.clearProperty("java.security.policy");

    }

    @Test
    public void testGetCarbonConfigDirPath() throws ClassNotFoundException, IllegalAccessException {
        logger.debug("setting env variables for testing getCarbonConfigDirPath() in CarbonBaseUtils");
        Map<String, String> newEnv = new HashMap<>();
        newEnv.put(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH_ENV, "../wso2am-2.1.0/repository/conf");
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newEnv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newEnv);
        } catch (NoSuchFieldException e) {
            Class[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for (Class cl : classes) {
                if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = null;
                    try {
                        field = cl.getDeclaredField("m");
                    } catch (NoSuchFieldException e1) {
                        logger.error(e.getMessage());
                    }
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    Map<String, String> map = (Map<String, String>) obj;
                    map.clear();
                    map.putAll(newEnv);
                }
            }
        }
        logger.debug("setting env variables done for testing getCarbonConfigDirPath() in CarbonBaseUtils");
        assertEquals("Must provide the location where carbon.xml resides", "../wso2am-2.1.0/repository/conf"
                , CarbonBaseUtils.getCarbonConfigDirPath());


    }

    @Test
    public void testGetServerXML() {
        System.setProperty(CarbonBaseConstants.CARBON_CONFIG_DIR_PATH, "../wso2am-2.1.0/repository/conf");
        String absoluteLocation = CarbonBaseUtils.getServerXml();
        assertEquals("Must match the formed absoluteLocation of carbon.xml",
                "../wso2am-2.1.0/repository/conf/carbon" + ".xml", absoluteLocation);
    }

    @Test
    public void testGetCarbonHome() {
        System.setProperty(CarbonBaseConstants.CARBON_HOME, "../wso2am-2.1.0");
        assertEquals("Must provide the carbon home ", "../wso2am-2.1.0", CarbonBaseUtils.getCarbonHome());
    }

    @Test
    public void testCheckSecurityWithClasses() {

        List<String> allowedClasses = Arrays.asList("org.junit.runners.model.FrameworkMethod$1.runReflectiveCall", "org.wso2.carbon.base" +
                        ".CarbonBaseUtils.checkSecurity", "org.junit.internal.runners.statements.InvokeMethod.evaluate",
                "org.junit.runner.JUnitCore.run", "org.junit.runners.ParentRunner.run");
        CarbonBaseUtils.checkSecurity(allowedClasses);
    }

    @Test
    public void testCheckSecurityWithMethods() {
        Map<String, String> allowedMethods = new HashMap<>();
        allowedMethods.put("sun.reflect.NativeMethodAccessorImpl", "invoke");
        allowedMethods.put("sun.reflect.DelegatingMethodAccessorImpl", "invoke");
        allowedMethods.put("java.lang.reflect.Method", "invoke");
        allowedMethods.put("org.junit.internal.runners.statements.InvokeMethod", "evaluate");

        CarbonBaseUtils.checkSecurity(allowedMethods);
    }


}


