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

package org.wso2.carbon.launcher.test;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.CarbonServerListener;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.config.CarbonInitialBundle;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;
import org.wso2.carbon.launcher.extensions.DropinsBundleDeployer;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.wso2.carbon.launcher.Constants.DEFAULT_PROFILE;
import static org.wso2.carbon.launcher.Constants.LAUNCH_PROPERTIES_FILE;
import static org.wso2.carbon.launcher.Constants.LOG_LEVEL_WARN;
import static org.wso2.carbon.launcher.Constants.PAX_DEFAULT_SERVICE_LOG_LEVEL;
import static org.wso2.carbon.launcher.Constants.PROFILE;

/**
 * Test loading launch configurations from launch.properties file.
 *
 * @since 5.0.0
 */
public class LoadLaunchConfigTest extends BaseTest {
    private Logger logger;
    private CarbonLaunchConfig launchConfig;
    private File logFile;

    public LoadLaunchConfigTest() {
        super();
    }

    @BeforeClass
    public void init() throws IOException {
        setupCarbonHome();
        logFile = Paths.get(Utils.getCarbonHomeDirectory().toString(), "logs", Constants.CARBON_LOG_FILE_NAME).toFile();
        logger = Logger.getLogger(CarbonLaunchConfig.class.getName());
        logger.addHandler(new CarbonLoggerTest.CarbonLogHandler(logFile));
        String profileName = System.getProperty(PROFILE);
        if (profileName == null || profileName.length() == 0) {
            System.setProperty(PROFILE, DEFAULT_PROFILE);
        }

        // Set log level for Pax logger to WARN.
        System.setProperty(PAX_DEFAULT_SERVICE_LOG_LEVEL, LOG_LEVEL_WARN);
    }

    @Test
    public void loadCarbonLaunchConfigFromFileTestCase() {
        String launchPropFilePath = Paths.get(Utils.getLaunchConfigDirectory().toString(),
                LAUNCH_PROPERTIES_FILE).toString();
        File launchPropFile = new File(launchPropFilePath);

        if (launchPropFile.exists()) {
            logger.log(Level.FINE, "Loading the Carbon launch configuration from the file " +
                    launchPropFile.getAbsolutePath());

            //loading launch.properties file
            launchConfig = new CarbonLaunchConfig(launchPropFile);
        }
        Assert.assertTrue(launchPropFile.exists(), "launch.properties file does not exists");
    }

    @Test
    public void loadCarbonLaunchConfigTestCase() {
        launchConfig = new CarbonLaunchConfig();

    }

    @Test
    public void loadCarbonLaunchConfigFromURLTestCase() throws MalformedURLException {
        String launchPropFilePath = Paths.get(Utils.getLaunchConfigDirectory().toString(),
                LAUNCH_PROPERTIES_FILE).toString();
        URL launchPropFileURL = new File(launchPropFilePath).toURI().toURL();

        launchConfig = new CarbonLaunchConfig(launchPropFileURL);
    }

    @Test(dependsOnMethods = {"loadCarbonLaunchConfigFromFileTestCase"})
    public void loadLaunchConfigOSGiFrameworkTestCase() {
        //test if property "carbon.osgi.framework" has set according to sample launch.properties file
        URL url = launchConfig.getCarbonOSGiFramework();
        Assert.assertEquals(url.getFile().split("plugins")[1],
                "/org.eclipse.osgi_3.10.2.v20150203-1939.jar");
    }

    @Test(dependsOnMethods = {"loadCarbonLaunchConfigFromFileTestCase"})
    public void loadLaunchConfigInitialBundlesTestCase() {
        //test if property "carbon.initial.osgi.bundles" has set according to sample launch.properties file
        List<CarbonInitialBundle> initialBundleList = launchConfig.getInitialBundles();
        Assert.assertEquals(initialBundleList.get(0).getLocation().getFile().split("plugins")[1],
                "/org.eclipse.equinox.simpleconfigurator_1.1.0.v20131217-1203.jar");
    }

    @Test(dependsOnMethods = {"loadCarbonLaunchConfigFromFileTestCase"})
    public void loadLaunchConfigOsgiRepoTestCase() throws MalformedURLException {
        //test if property "carbon.osgi.repository" has set according to sample launch.properties file
        URL osgiRepoURL = launchConfig.getCarbonOSGiRepository();
        Path expectedPath = Paths.get(System.getProperty(Constants.CARBON_HOME), "osgi");
        Assert.assertEquals(osgiRepoURL.toString().concat(File.separator), expectedPath.toUri().toURL().toString());
    }

    @Test(dependsOnMethods = {"loadCarbonLaunchConfigFromFileTestCase"})
    public void loadLaunchConfigServerListenersTestCase() throws MalformedURLException {
        //test if property "carbon.server.listeners" has set according to sample launch.properties file
        CarbonServerListener carbonServerListener = launchConfig.getCarbonServerListeners().get(0);
        Assert.assertTrue(carbonServerListener instanceof DropinsBundleDeployer);
    }

    @Test(dependsOnMethods = {"loadCarbonLaunchConfigFromFileTestCase"})
    public void carbonLogAppendTestCase() throws FileNotFoundException {
        String sampleMessage = "Sample message-test logging with class CarbonLaunchConfig";
        String resultLog = "INFO {org.wso2.carbon.launcher.test.LoadLaunchConfigTest carbonLogAppendTestCase} - " +
                "Sample message-test logging with class CarbonLaunchConfig";
        logger.info(sampleMessage);
        ArrayList<String> logRecords =
                getLogsFromTestResource(new FileInputStream(logFile));
        //test if log records are added to carbon.log
        boolean isContainsInLogs = containsLogRecord(logRecords, resultLog);
        Assert.assertTrue(isContainsInLogs);
    }

    @AfterTest
    public void cleanupLogfile() throws IOException {
        FileOutputStream writer = new FileOutputStream(logFile);
        writer.close();
    }
}
