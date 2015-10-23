package org.wso2.carbon.base;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UtilsTest {

    private static void set(Map<String, String> newenv) throws Exception {
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
    public void testGetCarbonConfigDirPathNonNullSystemProperty() throws Exception {
        String carbonRepoDirPath = System.getProperty(Constants.CARBON_REPOSITORY);
        Boolean needToClearCarbonRepoDirPathAtTheEnd = false;

        if (carbonRepoDirPath == null) {
            carbonRepoDirPath = "test-carbon-repo-dir-path";
            System.setProperty(Constants.CARBON_REPOSITORY, carbonRepoDirPath);
            needToClearCarbonRepoDirPathAtTheEnd = true;
        }
        Assert.assertEquals(Utils.getCarbonConfigDirPath(), carbonRepoDirPath + "/conf");

        if (needToClearCarbonRepoDirPathAtTheEnd) {
            System.clearProperty(Constants.CARBON_REPOSITORY);
        }
    }

    @Test
    public void testGetCarbonConfigDirPathNullSystemPropertyScenarioOne() throws Exception {

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

        Assert.assertEquals(Utils.getCarbonConfigDirPath(), "test-env/conf");

        if (needToSetCarbonRepoDirPathAtTheEnd && carbonRepoDirPath != null) {
            System.setProperty(Constants.CARBON_REPOSITORY, carbonRepoDirPath);
        }

        set(backup);

    }

    @Test
    public void testGetCarbonConfigDirPathNullSystemPropertyScenarioTwo() throws Exception {
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

        Assert.assertEquals(Utils.getCarbonConfigDirPath(),
                Paths.get(Utils.getCarbonHome(), "repository", "conf").toString());

        if (backupCarbonRepoDirPath != null) {
            System.setProperty(Constants.CARBON_REPOSITORY, backupCarbonRepoDirPath);
        }

        set(backupCarbonRepoPathEnv);

        if (backupCarbonHome == null) {
            System.clearProperty(Constants.CARBON_HOME);
        }
    }

    @Test
    public void testGetCarbonHome() throws Exception {

        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        Boolean needToClearCarbonHomeAtTheEnd = false;

        if (carbonHome == null) {
            carbonHome = "test-carbon-home";
            System.setProperty(Constants.CARBON_HOME, carbonHome);
            needToClearCarbonHomeAtTheEnd = true;
        }
        Assert.assertEquals(Utils.getCarbonHome(), carbonHome);

        Map<String, String> envMap = new HashMap<>();
        envMap.put(Constants.CARBON_HOME_ENV, "test-env");

        Map<String, String> backup = System.getenv();

        set(envMap);

        System.clearProperty(Constants.CARBON_HOME);
        Assert.assertEquals(Utils.getCarbonHome(), "test-env");

        if (needToClearCarbonHomeAtTheEnd) {
            System.clearProperty(Constants.CARBON_HOME);
        } else {
            System.setProperty(Constants.CARBON_HOME, carbonHome);
        }
        set(backup);
    }
}
