package org.wso2.carbon.launcher.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.CarbonServer;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


/**
 * Test Java.util.Logging log records.
 */
public class JavaUtilLoggingTest extends BaseTest {

    private Logger logger;
    private CarbonLaunchConfig launchConfig;
    private CarbonServer carbonServer;
    private File logFile;
    private String defaultConfigFile;

    @BeforeClass
    public void init() throws IOException {
        setupCarbonHome();
        String loggingPropertiesConfigFilePath = Paths
                .get("src", "test", "resources", "java.util.logging.properties")
                .toString();
        defaultConfigFile = readDefaultConfigurationFile();
        logger = BootstrapLogger.getCarbonLogger(JavaUtilLoggingTest.class.getName());
        reConfigureLogManager(loggingPropertiesConfigFilePath);
        //reset log manager and read configuration from a new file
        logger = BootstrapLogger.getCarbonLogger(JavaUtilLoggingTest.class.getName());
        logFile = Paths.get(Utils.getCarbonHomeDirectory().toString(), "logs", Constants.CARBON_LOG_FILE_NAME).toFile();
    }

    private void reConfigureLogManager(String loggingPropertiesConfigFilePath) throws IOException {
        //LogManager initially loads the default configuration from $JAVA-HOME/lib/logging.properties file.
        //We need to reload the configuration from a custom file "loggingPropertiesConfigFilePath".
        System.setProperty("java.util.logging.config.file", loggingPropertiesConfigFilePath);
        LogManager manager = LogManager.getLogManager();
        manager.readConfiguration();
    }

    private String readDefaultConfigurationFile() {
        String name = System.getProperty("java.util.logging.config.file");
        if (name == null) {
            name = System.getProperty("java.home");
            name = name + File.separator + "lib" + File.separator + "logging.properties";
        }
        return name;
    }

    @Test
    public void logWithUtilLogging() {
        logger.log(Level.FINE, "Logging Java Util Logging with level - FINE");
    }

    @Test(dependsOnMethods = "logWithUtilLogging")
    public void readCarbonLogsTestCase() throws FileNotFoundException {
        ArrayList<String> logRecords =
                getLogsFromTestResource(new FileInputStream(logFile));
        //test if log records are added to carbon.log
        boolean isContainsInLogs = containsLogRecord(logRecords,
                "{org.wso2.carbon.launcher.test.JavaUtilLoggingTest} - " +
                        "Logging Java Util Logging with level - FINE");
        Assert.assertTrue(isContainsInLogs);
    }


    @Test (dependsOnMethods = "readCarbonLogsTestCase")
    public void resetDefaultUtilLoggingConfiguration() throws IOException {
        //reset the LogManager configurations to the default values.
        reConfigureLogManager(defaultConfigFile);
    }
}
