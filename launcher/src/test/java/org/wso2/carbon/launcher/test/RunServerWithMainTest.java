package org.wso2.carbon.launcher.test;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.Main;
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static org.wso2.carbon.launcher.Constants.DEFAULT_PROFILE;
import static org.wso2.carbon.launcher.Constants.LOG_LEVEL_WARN;
import static org.wso2.carbon.launcher.Constants.PAX_DEFAULT_SERVICE_LOG_LEVEL;
import static org.wso2.carbon.launcher.Constants.PROFILE;

/**
 * Staring server with Main method.
 *
 * @since 5.0.0
 */
public class RunServerWithMainTest extends BaseTest {
    private Logger logger;
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

    @Test(dependsOnMethods = {"stopMainThreadTestCase"})
    public void startCarbonServerTestCase() {
        new Thread() {
            public void run() {
                Main.main(new String[]{});
            }
        }.start();
    }

    @Test
    public void stopMainThreadTestCase() {
        new Thread() {
            public void run() {
                try {
                    while (readPID() == null) {
                        sleep(100);
                    }
                    String pid = readPID();
                    Assert.assertNotNull(pid);
                    String cmd = "taskkill /F /PID " + pid;
                    Runtime.getRuntime().exec(cmd);
                } catch (InterruptedException e) {
                    logger.warning("Error while calling thread.sleep");
                } catch (IOException e) {
                    //Issue in terminating task with pid
                }
            }
        }.start();
    }

    private String readPID() {
        BufferedReader br;
        String pid = null;
        String pidFileName = Paths.get(System.getProperty(Constants.CARBON_HOME), "wso2carbon.pid").toString();
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
}
