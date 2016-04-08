package org.wso2.carbon.launcher.test;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.CarbonServer;
import org.wso2.carbon.launcher.ServerStatus;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.nio.file.Paths;
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
    private CarbonServer carbonServer;

    @BeforeSuite
    public void init() {
        setupCarbonHome();
        logger = Logger.getLogger(CarbonServerStartTest.class.getName());

        String profileName = System.getProperty(PROFILE);
        if (profileName == null || profileName.length() == 0) {
            System.setProperty(PROFILE, DEFAULT_PROFILE);
        }

        // Set log level for Pax logger to WARN.
        System.setProperty(PAX_DEFAULT_SERVICE_LOG_LEVEL, LOG_LEVEL_WARN);
        launchConfigs();
        carbonServer = new CarbonServer(launchConfig);
    }

    @Test(dependsOnMethods = {"stopCarbonServerTestCase"})
    public void startCarbonServerTestCase() throws Exception {
        carbonServer.start();
    }

    @Test
    public void stopCarbonServerTestCase() {
        new Thread() {
            public void run() {
                try {
                    while (carbonServer.getServerCurrentStatus() != ServerStatus.STARTED) {
                        sleep(100);
                    }
                    Assert.assertEquals(carbonServer.getServerCurrentStatus(), ServerStatus.STARTED);
                    carbonServer.stop();
                } catch (InterruptedException e) {
                    logger.warning("Error while calling thread.sleep");
                }
            }
        }.start();
    }

    private void launchConfigs() {
        String launchPropFilePath = Paths.get(Utils.getLaunchConfigDirectory().toString(),
                LAUNCH_PROPERTIES_FILE).toString();
        File launchPropFile = new File(launchPropFilePath);

        if (launchPropFile.exists()) {
            logger.log(Level.FINE, "Loading the Carbon launch configuration from the file " +
                    launchPropFile.getAbsolutePath());

            //loading launch.properties file
            launchConfig = new CarbonLaunchConfig(launchPropFile);
        }
    }

    @AfterTest
    public void stopServer() {
        // We need to invoke the stop method of the CarbonServer to allow the server to cleanup itself.
        carbonServer.stop();
    }

}
