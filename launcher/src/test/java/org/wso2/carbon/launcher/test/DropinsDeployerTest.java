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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.CarbonServerEvent;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.extensions.DropinsBundleDeployer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A test class for the dropins capability tests.
 *
 * @since 5.0.0
 */
public class DropinsDeployerTest extends BaseTest {

    private static final Logger logger = Logger.getLogger(DropinsDeployerTest.class.getName());

    @BeforeClass
    public void init() {
        setupCarbonHome();
        setupDropinsFolderStructure();
        setupBundlesInfoFile();
    }

    @Test
    public void readBundlesInfoToEmptyFile() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        Path bundlesInfoParent = Paths
                .get(carbonHome, "osgi", Constants.PROFILES, Constants.DEFAULT_PROFILE, "configuration",
                        "org.eclipse.equinox.simpleconfigurator");
        boolean matching = false;
        try {
            Path bundlesInfoFile = Paths.get(bundlesInfoParent.toString(), "bundles.info");
            if (!Files.exists(bundlesInfoFile)) {
                Files.createFile(bundlesInfoFile);
            }

            CarbonServerEvent carbonServerEvent = new CarbonServerEvent(CarbonServerEvent.STARTING, null);
            DropinsBundleDeployer deployer = new DropinsBundleDeployer();
            deployer.notify(carbonServerEvent);

            List<String> expected = getExpectedBundlesInfoList();
            matching = false;
            List<String> actual = Files.readAllLines(Paths.get(bundlesInfoParent.toString(), "bundles.info"));
            matching = compareBundlesInfoContent(expected, actual);
            Files.deleteIfExists(Paths.get(bundlesInfoParent.toString(), "bundles.info"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read the bundles.info file.");
        }
        Assert.assertTrue(matching);
    }

    @Test
    public void readBundlesInfoToNonEmptyFile() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        Path bundlesInfoParent = Paths
                .get(carbonHome, "osgi", Constants.PROFILES, Constants.DEFAULT_PROFILE, "configuration",
                        "org.eclipse.equinox.simpleconfigurator");
        boolean matching = false;
        try {
            Path bundlesInfoFile = Paths.get(bundlesInfoParent.toString(), "bundles.info");
            if (!Files.exists(bundlesInfoFile)) {
                Files.createFile(bundlesInfoFile);
            }
            List<String> fileContent = new ArrayList<>();
            fileContent.add("org.eclipse.equinox.launcher,1.3.0.v20140415-2008,"
                    + "../../dropins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar,4,true");
            fileContent.add("org.eclipse.equinox.simpleconfigurator,1.1.0.v20131217-1203,"
                    + "../../dropins/org.eclipse.equinox.simpleconfigurator_1.1.0.v20131217-1203.jar,4,true");
            Files.write(bundlesInfoFile, fileContent);

            CarbonServerEvent carbonServerEvent = new CarbonServerEvent(CarbonServerEvent.STARTING, null);
            DropinsBundleDeployer deployer = new DropinsBundleDeployer();
            deployer.notify(carbonServerEvent);

            List<String> expected = getExpectedBundlesInfoList();
            matching = false;

            List<String> actual = Files.readAllLines(Paths.get(bundlesInfoParent.toString(), "bundles.info"));
            matching = compareBundlesInfoContent(expected, actual);
            Files.deleteIfExists(Paths.get(bundlesInfoParent.toString(), "bundles.info"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not read the bundles.info file.");
        }

        Assert.assertTrue(matching);
    }

    private void setupDropinsFolderStructure() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        try {
            Path dropinsFolderPath = Paths.get(carbonHome, "osgi", "dropins");
            Files.createDirectories(dropinsFolderPath);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not create dropins folder structure.");
        }
    }

    private void setupBundlesInfoFile() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        Path bundlesInfoParent = Paths
                .get(carbonHome, "osgi", Constants.PROFILES, Constants.DEFAULT_PROFILE, "configuration",
                        "org.eclipse.equinox.simpleconfigurator");
        try {
            Files.createDirectories(bundlesInfoParent);
            Path bundlesInfoFilePath = Paths.get(bundlesInfoParent.toString(), "bundles.info");
            Files.createFile(bundlesInfoFilePath);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Could not create bundles.info file.");
        }
    }

    private static boolean compareBundlesInfoContent(List<String> expected, List<String> actual) {
        if ((expected != null && actual != null) && (expected.size() == actual.size())) {
            for (String entry : expected) {
                if (!actual.contains(entry)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<String> getExpectedBundlesInfoList() {
        List<String> expected = new ArrayList<>();
        expected.add("org.eclipse.equinox.launcher,1.3.0.v20140415-2008,"
                + "../../dropins/org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar,4,true");
        expected.add("org.eclipse.equinox.simpleconfigurator,1.1.0.v20131217-1203,"
                + "../../dropins/org.eclipse.equinox.simpleconfigurator_1.1.0.v20131217-1203.jar,4,true");
        expected.add("org.eclipse.equinox.util,1.0.500.v20130404-1337,"
                + "../../dropins/org.eclipse.equinox.util_1.0.500.v20130404-1337.jar,4,true");
        expected.add("org.eclipse.osgi,3.10.2.v20150203-1939,"
                + "../../dropins/org.eclipse.osgi_3.10.2.v20150203-1939.jar,4,true");

        return expected;
    }

    @AfterClass
    public void cleanUp() {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        List<Path> deletePaths = new ArrayList<>();
        Path bundlesInfoParent = Paths.get(carbonHome, "osgi", Constants.PROFILES, Constants.DEFAULT_PROFILE,
                "org.eclipse.equinox.simpleconfigurator");
        deletePaths.add(bundlesInfoParent);
        deletePaths.add(Paths.get(carbonHome, "osgi", Constants.PROFILES, Constants.DEFAULT_PROFILE, "configuration"));
        deletePaths.add(Paths.get(carbonHome, "osgi", Constants.PROFILES, Constants.DEFAULT_PROFILE));

        deletePaths.forEach(deletePath -> {
            try {
                Files.deleteIfExists(deletePath);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not delete " + deletePath.toString() + ".");
            }
        });
    }
}
