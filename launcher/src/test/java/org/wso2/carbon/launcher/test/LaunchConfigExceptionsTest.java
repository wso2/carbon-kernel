package org.wso2.carbon.launcher.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.LogManager;

import static org.wso2.carbon.launcher.Constants.DEFAULT_PROFILE;
import static org.wso2.carbon.launcher.Constants.LAUNCH_PROPERTIES_FILE;
import static org.wso2.carbon.launcher.Constants.LOG_LEVEL_WARN;
import static org.wso2.carbon.launcher.Constants.PAX_DEFAULT_SERVICE_LOG_LEVEL;
import static org.wso2.carbon.launcher.Constants.PROFILE;

/**
 * Test Exceptions for CarbonLaunchConfig class.
 *
 * @since 5.0.0
 */
public class LaunchConfigExceptionsTest extends BaseTest {

    private File logFile;

    public LaunchConfigExceptionsTest() {
        super();
    }

    @BeforeClass
    public void init() throws IOException {
        setupCarbonHome();
        logFile = Paths.get(Utils.getCarbonHomeDirectory().toString(), "logs", Constants.CARBON_LOG_FILE_NAME).toFile();
        LogManager.getLogManager().getLogger(CarbonLaunchConfig.class.getName()).
                addHandler(new CarbonLoggerTest.CarbonLogHandler(logFile));
        String profileName = System.getProperty(PROFILE);
        if (profileName == null || profileName.length() == 0) {
            System.setProperty(PROFILE, DEFAULT_PROFILE);
        }

        // Set log level for Pax logger to WARN.
        System.setProperty(PAX_DEFAULT_SERVICE_LOG_LEVEL, LOG_LEVEL_WARN);
    }

    /**
     * Test the RuntimeException thrown when launcher.properties file does not exist.
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void loadCarbonLaunchConfigFromFileTestCase() throws FileNotFoundException {
        String launchPropFilePath = Paths.get("test", LAUNCH_PROPERTIES_FILE).toString();
        File launchPropFile = new File(launchPropFilePath);

        new CarbonLaunchConfig(launchPropFile);
    }

    /**
     * Test the RuntimeException thrown when launcher.properties file does not exist.
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void loadCarbonLaunchConfigFromURLTestCase() throws MalformedURLException {
        String launchPropFilePath = Paths.get("test", LAUNCH_PROPERTIES_FILE).toString();
        URL launchPropFileURL = new File(launchPropFilePath).toURI().toURL();

        new CarbonLaunchConfig(launchPropFileURL);
    }

    /**
     * Test if carbon.log has error messages logged for exception in loadCarbonLaunchConfigFromURLTestCase.
     *
     * @throws FileNotFoundException if carbon.log file not exists.
     */
    @Test(dependsOnMethods = {"loadCarbonLaunchConfigFromURLTestCase"})
    public void verifyRuntimeExceptionErrorLogsTestCase() throws FileNotFoundException {

        String resultLog = "SEVERE {org.wso2.carbon.launcher.config.CarbonLaunchConfig loadConfigurationFromUrl} - "
                + "Error loading the launch.properties";
        ArrayList<String> logRecords = getLogsFromTestResource(new FileInputStream(logFile));
        //test if log records are added to carbon.log
        boolean isContainsInLogs = containsLogRecord(logRecords, resultLog);
        Assert.assertTrue(isContainsInLogs);
    }

    /**
     * Test the RuntimeException thrown when resolving properties from launcher.properties file.
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void loadCarbonLaunchConfigFromFaultFileTestCase() {
        String launchPropFilePath = Paths
                .get("src", "test", "resources", "InvalidLauncherFiles", "launcherWithEmptyValues.properties")
                .toString();
        File launchPropFile = new File(launchPropFilePath);

        new CarbonLaunchConfig(launchPropFile);
    }

    /**
     * Test if carbon.log has error messages logged for exception in loadCarbonLaunchConfigFromFaultFileTestCase.
     *
     * @throws FileNotFoundException if carbon.log file not exists.
     */
    @Test(dependsOnMethods = {"loadCarbonLaunchConfigFromFaultFileTestCase"})
    public void verifyErrorLogsLoadingPropertiesTestCase() throws FileNotFoundException {

        String resultLog = "SEVERE {org.wso2.carbon.launcher.config.CarbonLaunchConfig resolvePath} - "
                + "The property osgi.install.area must not be null or empty";
        ArrayList<String> logRecords = getLogsFromTestResource(new FileInputStream(logFile));
        //test if log records are added to carbon.log
        boolean isContainsInLogs = containsLogRecord(logRecords, resultLog);
        Assert.assertTrue(isContainsInLogs);
    }

    /**
     * Test the RuntimeException thrown when loading initial bundles from launch.properties file
     * due to invalid initial bundle entry.
     */
    @Test(expectedExceptions = {RuntimeException.class})
    public void launchConfigWithInvalidInitBundlesTestCase() {
        String launchPropFilePath = Paths
                .get("src", "test", "resources", "InvalidLauncherFiles", "launcherWithinvalidBundle.properties")
                .toString();
        File launchPropFile = new File(launchPropFilePath);

        new CarbonLaunchConfig(launchPropFile);
    }
}
