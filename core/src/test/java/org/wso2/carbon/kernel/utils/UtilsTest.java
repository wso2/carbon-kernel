package org.wso2.carbon.kernel.utils;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UtilsTest {

    protected static void set(Map<String, String> newenv) {
        try {
            Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            theEnvironmentField.setAccessible(true);
            Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
            env.putAll(newenv);
            Field theCaseInsensitiveEnvironmentField = processEnvironmentClass
                    .getDeclaredField("theCaseInsensitiveEnvironment");
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            cienv.putAll(newenv);
        } catch (NoSuchFieldException e) {
            try {
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
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    @Test public void testGetCarbonConfigHomePathNonNullSystemProperty() throws Exception {
        String carbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
        Boolean needToClearCarbonRepoDirPathAtTheEnd = false;

        if (carbonRepoDirPath == null) {
            carbonRepoDirPath = "test-carbon-repo-dir-path";
            System.setProperty(Constants.CARBON_REPOSITORY, carbonRepoDirPath);
            needToClearCarbonRepoDirPathAtTheEnd = true;
        }
        Assert.assertEquals(Utils.getCarbonConfigHome(), Paths.get(carbonRepoDirPath + "/conf"));

        if (needToClearCarbonRepoDirPathAtTheEnd) {
            System.clearProperty(Constants.CARBON_REPOSITORY);
        }
    }

    @Test public void testGetCarbonConfigHomePathNullSystemPropertyScenarioOne() throws Exception {

        String carbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
        Boolean needToSetCarbonRepoDirPathAtTheEnd = false;

        if (carbonRepoDirPath != null) {
            System.clearProperty(Constants.CARBON_REPOSITORY);
        } else {
            needToSetCarbonRepoDirPathAtTheEnd = true;
        }

        Map<String, String> envMap = new HashMap<>();
        envMap.put(Constants.CARBON_REPOSITORY_PATH_ENV, "test-env");

        Map<String, String> backup = System.getenv();

        set(envMap);

        Assert.assertEquals(Utils.getCarbonConfigHome(), Paths.get("test-env/conf"));

        if (needToSetCarbonRepoDirPathAtTheEnd && carbonRepoDirPath != null) {
            System.setProperty(Constants.CARBON_REPOSITORY, carbonRepoDirPath);
        }

        set(backup);

    }

    @Test public void testGetCarbonConfigHomePathNullSystemPropertyScenarioTwo() throws Exception {
        String backupCarbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
        Map<String, String> backupCarbonRepoPathEnv = System.getenv();

        if (backupCarbonRepoDirPath != null) {
            System.clearProperty(backupCarbonRepoDirPath);
        }

        if (System.getenv(Constants.CARBON_REPOSITORY_PATH_ENV) != null) {
            set(new HashMap<>());
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

        set(backupCarbonRepoPathEnv);

        if (backupCarbonHome == null) {
            System.clearProperty(Constants.CARBON_HOME);
        }
    }

    @Test public void testGetCarbonHome() throws Exception {

        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        Boolean needToClearCarbonHomeAtTheEnd = false;

        if (carbonHome == null) {
            carbonHome = "test-carbon-home";
            System.setProperty(Constants.CARBON_HOME, carbonHome);
            needToClearCarbonHomeAtTheEnd = true;
        }
        Assert.assertEquals(Utils.getCarbonHome(), Paths.get(carbonHome));

        Map<String, String> envMap = new HashMap<>();
        envMap.put(Constants.CARBON_HOME_ENV, "test-env");

        Map<String, String> backup = System.getenv();

        set(envMap);

        System.clearProperty(Constants.CARBON_HOME);
        Assert.assertEquals(Utils.getCarbonHome(), Paths.get("test-env"));

        if (needToClearCarbonHomeAtTheEnd) {
            System.clearProperty(Constants.CARBON_HOME);
        } else {
            System.setProperty(Constants.CARBON_HOME, carbonHome);
        }
        set(backup);
    }
}
