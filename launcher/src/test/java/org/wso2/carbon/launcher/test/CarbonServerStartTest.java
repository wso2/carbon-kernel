package org.wso2.carbon.launcher.test;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.Main;
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.wso2.carbon.launcher.Constants.DEFAULT_PROFILE;
import static org.wso2.carbon.launcher.Constants.LAUNCH_PROPERTIES_FILE;
import static org.wso2.carbon.launcher.Constants.LOG_LEVEL_WARN;
import static org.wso2.carbon.launcher.Constants.PAX_DEFAULT_SERVICE_LOG_LEVEL;
import static org.wso2.carbon.launcher.Constants.PROFILE;

/**
 * Test server start and stop events.
 *
 * @since 5.0.0
 */
public class CarbonServerStartTest extends BaseTest {
    private Logger logger;
    private CarbonLaunchConfig launchConfig;
    private File logFile;

    @BeforeClass
    public void init() {
        setupCarbonHome();
        logFile = new File(Utils.getRepositoryDirectory() + File.separator + "logs" +
                File.separator + "wso2carbon.log");
        logger = BootstrapLogger.getCarbonLogger(CarbonServerStartTest.class.getName());

        String profileName = System.getProperty(PROFILE);
        if (profileName == null || profileName.length() == 0) {
            System.setProperty(PROFILE, DEFAULT_PROFILE);
        }

        // Set log level for Pax logger to WARN.
        System.setProperty(PAX_DEFAULT_SERVICE_LOG_LEVEL, LOG_LEVEL_WARN);
    }

    @Test
    public void startCarbonServerTestCase() {
        String launchPropFilePath = Paths.get(Utils.getLaunchConfigDirectory().toString(),
                LAUNCH_PROPERTIES_FILE).toString();
        File launchPropFile = new File(launchPropFilePath);

        if (launchPropFile.exists()) {
            logger.log(Level.FINE, "Loading the Carbon launch configuration from the file " +
                    launchPropFile.getAbsolutePath());

            //loading launch.properties file
            launchConfig = new CarbonLaunchConfig(launchPropFile);
        }
        System.setProperty("carbon.server.restart", "true");
        new Thread() {
            public void run() {
                Main.main(new String[]{});
            }
        }.start();

        new Thread() {
            public void run() {
                try {
                    while (readPID() == null) {
                        sleep(100);
                    }
                    String cmd = "taskkill /F /PID " + readPID();
                    Runtime.getRuntime().exec(cmd);
                } catch (InterruptedException e) {
                    logger.warning("Error while calling thread.sleep");
                } catch (IOException e) {
                    //Issue in terminating task with pid
                }
            }
        }.start();
    }

//    @Test(dependsOnMethods = {"startCarbonServerTestCase"})
//    public void processIDTestCase() throws IOException {
//        Assert.assertNotNull(readPID());
//    }

    @Test(dependsOnMethods = {"startCarbonServerTestCase"})
    public void serverTerminationTestCase() throws IOException {
        String sampleMessage = "Sample message-Terminating server with PID " + readPID();
        String resultLog = "INFO {org.wso2.carbon.launcher.test.CarbonServerStartTest} - " +
                "Sample message-Terminating server";

        logger.info(sampleMessage);
        ArrayList<String> logRecords =
                getLogsFromTestResource(new FileInputStream(logFile));
        //test if log records are added to wso2carbon.log
        boolean containsResultInLog = containsLogRecord(logRecords, resultLog);
        Assert.assertTrue(containsResultInLog);
    }

    private String readPID() {
        BufferedReader br = null;
        String pid = null;
        String pidFileName = Paths.get(testResourceDir, "wso2carbon.pid").toString();
        try {
            br = new BufferedReader(new FileReader(pidFileName));
            pid = br.readLine();
            if (pid == null) {
                return null;
            }
        } catch (FileNotFoundException e) {
            logger.severe("File not found with name " + pidFileName);
        } catch (IOException e) {
            logger.severe("Error reading file " + pidFileName);
        }

        return pid;
    }

    @AfterTest
    public void cleanupLogfile() throws IOException {
        FileOutputStream writer = new FileOutputStream(logFile);
        writer.write((new String()).getBytes());
        writer.close();
    }

}
