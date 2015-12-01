/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.utils;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class tests the functionality of org.wso2.carbon.kernel.utils.Utils class.
 *
 * @since 5.0.0
 */
public class UtilsTest {

    private static void setEnvironmentalVariables(Map<String, String> newenv) throws Exception {
        Class[] classes = Collections.class.getDeclaredClasses();
        Map<String, String> env = System.getenv();
        for (Class cl : classes) {
            if ("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                Field field = cl.getDeclaredField("m");
                field.setAccessible(true);
                Object obj = field.get(env);
                Map<String, String> map = (Map<String, String>) obj;
                map.clear();
                map.putAll(newenv);
            }
        }
    }

    @Test
    public void testGetCarbonConfigHomePathNonNullSystemProperty() throws Exception {
        String carbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
        boolean isCarbonHomeChanged = false;

        if (carbonRepoDirPath == null) {
            carbonRepoDirPath = "test-carbon-repo-dir-path";
            System.setProperty(Constants.CARBON_REPOSITORY, carbonRepoDirPath);
            isCarbonHomeChanged = true;
        }
        Assert.assertEquals(Utils.getCarbonConfigHome(), Paths.get(carbonRepoDirPath + "/conf"));

        if (isCarbonHomeChanged) {
            System.clearProperty(Constants.CARBON_REPOSITORY);
        }
    }


    @Test
    public void testGetCarbonHome() throws Exception {
        if (!isWindows()) {
            String carbonHome = System.getProperty(Constants.CARBON_HOME);
            boolean isCarbonHomeChanged = false;

            if (carbonHome == null) {
                carbonHome = "test-carbon-home";
                System.setProperty(Constants.CARBON_HOME, carbonHome);
                isCarbonHomeChanged = true;
            }
            Assert.assertEquals(Utils.getCarbonHome(), Paths.get(carbonHome));

            Map<String, String> envMap = new HashMap<>();
            envMap.put(Constants.CARBON_HOME_ENV, "test-env");

            Map<String, String> backup = System.getenv();

            setEnvironmentalVariables(envMap);

            System.clearProperty(Constants.CARBON_HOME);
            Assert.assertEquals(Utils.getCarbonHome(), Paths.get("test-env"));

            if (isCarbonHomeChanged) {
                System.clearProperty(Constants.CARBON_HOME);
            } else {
                System.setProperty(Constants.CARBON_HOME, carbonHome);
            }
            setEnvironmentalVariables(backup);
        }
    }

    @Test(dependsOnMethods = "testGetCarbonHome")
    public void testGetCarbonConfigHomePathNullSystemPropertyScenarioOne() throws Exception {
        if (!isWindows()) {
            String carbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
            boolean isCarbonRepoPathChanged = false;

            if (carbonRepoDirPath != null) {
                System.clearProperty(Constants.CARBON_REPOSITORY);
            } else {
                isCarbonRepoPathChanged = true;
            }

            Map<String, String> envMap = new HashMap<>();
            envMap.put(Constants.CARBON_REPOSITORY_PATH_ENV, "test-env");

            Map<String, String> backup = System.getenv();

            setEnvironmentalVariables(envMap);

            Assert.assertEquals(Utils.getCarbonConfigHome(), Paths.get("test-env/conf"));

            if (isCarbonRepoPathChanged && carbonRepoDirPath != null) {
                System.setProperty(Constants.CARBON_REPOSITORY, carbonRepoDirPath);
            }

            setEnvironmentalVariables(backup);
        }
    }

    @Test(dependsOnMethods = {"testGetCarbonHome", "testGetCarbonConfigHomePathNullSystemPropertyScenarioOne"})
    public void testGetCarbonConfigHomePathNullSystemPropertyScenarioTwo() throws Exception {
        if (!isWindows()) {
            String backupCarbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
            Map<String, String> backupCarbonRepoPathEnv = System.getenv();

            if (backupCarbonRepoDirPath != null) {
                System.clearProperty(backupCarbonRepoDirPath);
            }

            if (System.getenv(Constants.CARBON_REPOSITORY_PATH_ENV) != null) {
                setEnvironmentalVariables(new HashMap<>());
            }

            String backupCarbonHome = System.getProperty(Constants.CARBON_HOME);

            if (backupCarbonHome == null) {
                System.setProperty(Constants.CARBON_HOME, "test-carbon-home");
            }

            Assert.assertEquals(Utils.getCarbonConfigHome(),
                    Paths.get(Utils.getCarbonHome().toString(), "repository", "conf"));

            if (backupCarbonRepoDirPath != null) {
                System.setProperty(Constants.CARBON_REPOSITORY, backupCarbonRepoDirPath);
            }

            setEnvironmentalVariables(backupCarbonRepoPathEnv);

            if (backupCarbonHome == null) {
                System.clearProperty(Constants.CARBON_HOME);
            }
        }
    }


    @Test
    public void testSubstituteVarsSystemPropertyNotNull() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        boolean isCarbonHomeChanged = false;

        if (carbonHome == null) {
            carbonHome = "test-carbon-home";
            System.setProperty(Constants.CARBON_HOME, carbonHome);
            isCarbonHomeChanged = true;
        }

        Assert.assertEquals(Utils.substituteVars("${carbon.home}"), carbonHome);

        if (isCarbonHomeChanged) {
            System.clearProperty(Constants.CARBON_HOME);
        }
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testSubstituteVarsSystemPropertyIsNull() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        boolean isCarbonHomeChanged = false;

        if (carbonHome != null) {
            System.clearProperty(Constants.CARBON_HOME);
            isCarbonHomeChanged = true;
        }

        try {
            Utils.substituteVars("${carbon.home}");
        } finally {
            if (isCarbonHomeChanged) {
                System.setProperty(Constants.CARBON_HOME, carbonHome);
            }
        }
    }

    private boolean isWindows() {
        boolean isWindows = false;
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            isWindows = true;
        }
        return isWindows;
    }
}
