/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.CarbonServerEvent;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.extensions.OSGiLibBundleDeployer;
import org.wso2.carbon.launcher.extensions.OSGiLibBundleDeployerUtils;
import org.wso2.carbon.launcher.extensions.model.BundleInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This Java class defines the unit tests for OSGi-lib bundle deployment.
 *
 * @since 5.1.0
 */
public class OSGiLibBundleDeployerTest extends BaseTest {
    //  OSGi-lib deployer unit-test constants
    private static final List<String> profileNames = new ArrayList<>();
    private static final String profileMSS = "mss";
    private static final String bundlesInfoFile = "bundles.info";

    private static String carbonHome;

    @BeforeClass
    public void initTestClass() throws IOException {
        setupCarbonHome();
        carbonHome = System.getProperty(Constants.CARBON_HOME);
        //  deletes the profile directories, if present
        delete(Paths.get(carbonHome, Constants.PROFILE_REPOSITORY));
    }

    @Test(description = "Attempts to get Carbon runtimes when there is no runtimes", expectedExceptions = {
        IOException.class })
    public void testGettingCarbonProfilesFromNonExistingProfilesFolder() throws IOException {
        OSGiLibBundleDeployerUtils.getCarbonProfiles(carbonHome);
    }

    @Test(description = "Attempts to execute OSGi-lib capability with profile system property not explicitly set",
            priority = 1)
    public void testExecutingOSGiLibCapabilityForDefaultProfile() throws IOException {
        createProfiles();

        OSGiLibBundleDeployer deployer = new OSGiLibBundleDeployer();
        deployer.notify(new CarbonServerEvent(CarbonServerEvent.STARTING, null));

        List<BundleInfo> expected = getExpectedBundleInfo();
        Path bundlesInfo = Paths.
                get(carbonHome, Constants.PROFILE_REPOSITORY, Constants.DEFAULT_PROFILE, "configuration",
                        "org.eclipse.equinox.simpleconfigurator", bundlesInfoFile);
        List<BundleInfo> actual = getActualBundleInfo(bundlesInfo);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute OSGi-lib capability for a chosen Carbon Runtime", priority = 2)
    public void testExecutingOSGiLibCapabilityForSelectedProfile() throws IOException {
        System.setProperty(Constants.PROFILE, profileMSS);
        OSGiLibBundleDeployer deployer = new OSGiLibBundleDeployer();
        deployer.notify(new CarbonServerEvent(CarbonServerEvent.STARTING, null));

        List<BundleInfo> expected = getExpectedBundleInfo();
        Path bundlesInfo = Paths.get(carbonHome, Constants.PROFILE_REPOSITORY, profileMSS, "configuration",
                "org.eclipse.equinox.simpleconfigurator", bundlesInfoFile);
        List<BundleInfo> actual = getActualBundleInfo(bundlesInfo);
        System.clearProperty(Constants.PROFILE);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to load OSGi bundle information from a source directory with files of multiple "
            + "formats", priority = 3)
    public void testGettingNewBundlesInfoFromMultipleFileFormats() throws IOException {
        Path lib = Paths.get(carbonHome, Constants.OSGI_LIB);
        Files.createFile(Paths.get(lib.toString(), "sample.txt"));

        List<BundleInfo> expected = getExpectedBundleInfo();
        List<BundleInfo> actual = OSGiLibBundleDeployerUtils.getBundlesInfo(lib);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts loading the Carbon runtime names", priority = 3)
    public void testLoadingCarbonProfiles() throws IOException {
        List<String> actual = OSGiLibBundleDeployerUtils.getCarbonProfiles(carbonHome);
        boolean matching = ((profileNames
                .stream()
                .filter(expectedName -> actual
                        .stream()
                        .filter(expectedName::equals)
                        .count() == 1))
                .count() == profileNames.size());
        Assert.assertTrue(matching);
    }

    @Test(description = "Attempts to load OSGi bundle information from a source directory with an existing "
            + "bundle removed", priority = 4)
    public void testRemovingExistingBundle() throws IOException {
        String equinoxLauncherVersion = System.getProperty("equinox.launcher.version");
        BundleInfo bundleInfoRemoved = BundleInfo.getInstance(
                "org.eclipse.equinox.launcher," + equinoxLauncherVersion + ",../../" + Constants.OSGI_LIB
                        + "/org.eclipse.equinox.launcher_" + equinoxLauncherVersion + ".jar,4,true");

        Path lib = Paths.get(carbonHome, Constants.OSGI_LIB);
        Files.deleteIfExists(
                Paths.get(lib.toString(), "org.eclipse.equinox.launcher_" + equinoxLauncherVersion + ".jar"));

        List<BundleInfo> expected = getExpectedBundleInfo();
        expected.remove(bundleInfoRemoved);

        OSGiLibBundleDeployer deployer = new OSGiLibBundleDeployer();
        deployer.notify(new CarbonServerEvent(CarbonServerEvent.STARTING, null));

        Path bundlesInfo = Paths.
                get(carbonHome, Constants.PROFILE_REPOSITORY, Constants.DEFAULT_PROFILE, "configuration",
                        "org.eclipse.equinox.simpleconfigurator", bundlesInfoFile);
        List<BundleInfo> actual = getActualBundleInfo(bundlesInfo);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to install new OSGi bundles with invalid Carbon Runtime name",
            priority = 5,
            expectedExceptions = { IllegalArgumentException.class })
    public void testInstallingNewBundlesWithInvalidCarbonProfile() throws IOException {
        OSGiLibBundleDeployerUtils.updateOSGiLib(carbonHome, null, new ArrayList<>());
    }

    @Test(description = "Attempts to install new OSGi bundles with invalid list of new OSGi bundles",
            priority = 5,
            expectedExceptions = { IllegalArgumentException.class })
    public void testInstallingNewBundlesWithInvalidBundlesInfo() throws IOException {
        OSGiLibBundleDeployerUtils.updateOSGiLib(carbonHome, Constants.DEFAULT_PROFILE, null);
    }

    @Test(description = "Attempts to load the new OSGi bundle information from a non existing folder",
            priority = 5,
            expectedExceptions = { IOException.class })
    public void testLoadingNewBundleInfoFromNonExistingFolder() throws IOException {
        OSGiLibBundleDeployerUtils.getBundlesInfo(Paths.get(carbonHome, Constants.OSGI_REPOSITORY, Constants.OSGI_LIB));
    }

    /**
     * Utility functions for OSGi-lib unit-tests.
     */

    private static void createProfiles() throws IOException {
        profileNames.add(Constants.DEFAULT_PROFILE);
        profileNames.add(profileMSS);

        List<String> defaultBundlesInfoContent = new ArrayList<>();
        defaultBundlesInfoContent.add("#version=1");

        for (String profileName : profileNames) {
            Path profile = Paths.
                    get(carbonHome, Constants.PROFILE_REPOSITORY, profileName, "configuration",
                            "org.eclipse.equinox.simpleconfigurator");
            createDirectories(profile);
            if (Files.exists(profile)) {
                Files.write(Paths.get(profile.toString(), bundlesInfoFile), defaultBundlesInfoContent);
            }
        }
    }

    private static void createDirectories(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    private static List<BundleInfo> getExpectedBundleInfo() {
        List<BundleInfo> bundleInfo = new ArrayList<>();
        String equinoxOSGiVersion = System.getProperty("equinox.osgi.version");
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.osgi," + equinoxOSGiVersion + ",../../" +
                Constants.OSGI_LIB + "/org.eclipse.osgi_" + equinoxOSGiVersion + ".jar,4,true"));

        String equinoxSimpleConfiguratorVersion = System.getProperty("equinox.simpleconfigurator.version");
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.equinox.simpleconfigurator," +
                equinoxSimpleConfiguratorVersion + ",../../" + Constants.OSGI_LIB +
                "/org.eclipse.equinox.simpleconfigurator_" + equinoxSimpleConfiguratorVersion + ".jar,4,true"));

        String equinoxUtilVersion = System.getProperty("equinox.util.version");
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.equinox.util," + equinoxUtilVersion + ",../../" +
                Constants.OSGI_LIB + "/org.eclipse.equinox.util_" + equinoxUtilVersion + ".jar,4,true"));

        String equinoxLauncherVersion = System.getProperty("equinox.launcher.version");
        bundleInfo.add(BundleInfo.getInstance(
                "org.eclipse.equinox.launcher," + equinoxLauncherVersion + ",../../" + Constants.OSGI_LIB
                        + "/org.eclipse.equinox.launcher_" + equinoxLauncherVersion + ".jar,4,true"));

        return bundleInfo;
    }

    private static List<BundleInfo> getActualBundleInfo(Path bundleInfoFile) throws IOException {
        if ((bundleInfoFile != null) && (Files.exists(bundleInfoFile))) {
            List<String> bundleInfoLines = Files.readAllLines(bundleInfoFile);
            List<BundleInfo> bundleInfo = new ArrayList<>();
            bundleInfoLines
                    .stream()
                    .forEach(line -> bundleInfo.add(BundleInfo.getInstance(line)));

            return bundleInfo;
        } else {
            throw new IOException("Invalid bundles.info file specified");
        }
    }

    private static boolean compareBundleInfo(List<BundleInfo> expected, List<BundleInfo> actual) {
        return (expected.size() == actual.size()) &&
                ((expected
                        .stream()
                        .filter(bundleInfo -> actual
                                .stream()
                                .filter(actualBundleInfo -> actualBundleInfo.equals(bundleInfo)).count() == 1)
                        .count()) == expected.size());
    }

    private static void delete(Path path) throws IOException {
        Path osgiRepoPath = Paths.get(carbonHome, Constants.OSGI_REPOSITORY);
        Files.list(path)
                .filter(child -> !osgiRepoPath.equals(child) && Files.isDirectory(child))
                .forEach(child -> {
                    try {
                        FileUtils.deleteDirectory(child.toFile());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
    }

}
