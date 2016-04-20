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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.CarbonServerEvent;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.extensions.DropinsBundleDeployer;
import org.wso2.carbon.launcher.extensions.DropinsBundleDeployerUtils;
import org.wso2.carbon.launcher.extensions.model.BundleInfo;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This Java class defines the unit tests for dropins OSGi bundle deployment.
 *
 * @since 5.1.0
 */
public class DropinsBundleDeployerTest extends BaseTest {
    //  dropins deployer unit-test constants
    private static final List<String> profileNames = new ArrayList<>();
    private static final String dropinsDirectory = "dropins";
    private static final String profileMSS = "mss";
    private static final String bundlesInfoFile = "bundles.info";

    private static String carbonHome;

    @BeforeClass
    public void initTestClass() throws IOException {
        setupCarbonHome();
        carbonHome = System.getProperty(Constants.CARBON_HOME);
        //  deletes the profiles directory, if present
        delete(Paths.get(carbonHome, Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH));
    }

    @Test(description = "Attempts to get Carbon Profiles when profiles directory is absent",
            expectedExceptions = { IOException.class })
    public void testGettingCarbonProfilesFromNonExistingProfilesFolder() throws IOException {
        DropinsBundleDeployerUtils.getCarbonProfiles(carbonHome);
    }

    @Test(description = "Attempts to execute dropins capability with profile system property not explicitly set",
            priority = 1)
    public void testExecutingDropinsCapabilityForDefaultProfile() throws IOException {
        createProfiles();

        DropinsBundleDeployer deployer = new DropinsBundleDeployer();
        deployer.notify(new CarbonServerEvent(CarbonServerEvent.STARTING, null));

        List<BundleInfo> expected = getExpectedBundleInfo();
        Path bundlesInfo = Paths
                .get(carbonHome, Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH, Constants.DEFAULT_PROFILE,
                        "configuration", "org.eclipse.equinox.simpleconfigurator", bundlesInfoFile);
        List<BundleInfo> actual = getActualBundleInfo(bundlesInfo);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute dropins capability for a chosen Carbon Profile", priority = 2)
    public void testExecutingDropinsCapabilityForSelectedProfile() throws IOException {
        System.setProperty(Constants.PROFILE, profileMSS);
        DropinsBundleDeployer deployer = new DropinsBundleDeployer();
        deployer.notify(new CarbonServerEvent(CarbonServerEvent.STARTING, null));

        List<BundleInfo> expected = getExpectedBundleInfo();
        Path bundlesInfo = Paths.get(carbonHome, Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH,
                profileMSS, "configuration", "org.eclipse.equinox.simpleconfigurator", bundlesInfoFile);
        List<BundleInfo> actual = getActualBundleInfo(bundlesInfo);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to load OSGi bundle information from a source directory with files of multiple "
            + "formats", priority = 3)
    public void testGettingNewBundlesInfoFromMultipleFileFormats() throws IOException {
        Path dropins = Paths.get(carbonHome, Constants.OSGI_REPOSITORY, dropinsDirectory);
        Files.createFile(Paths.get(dropins.toString(), "sample.txt"));

        List<BundleInfo> expected = getExpectedBundleInfo();
        List<BundleInfo> actual = DropinsBundleDeployerUtils.getNewBundlesInfo(dropins);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempt loading the Carbon profile names", priority = 3)
    public void testLoadingCarbonProfiles() throws IOException {
        List<String> actual = DropinsBundleDeployerUtils.getCarbonProfiles(carbonHome);
        boolean matching = ((profileNames.stream().
                filter(expectedName -> actual.stream().
                        filter(expectedName::equals).count() == 1)).count() == profileNames.size());
        Assert.assertTrue(matching);
    }

    @Test(description = "Attempts to load OSGi bundle information from a non existing source directory", priority = 3,
            expectedExceptions = { IOException.class })
    public void testGettingNewBundlesInfoFromNonExistingFolder() throws IOException {
        Path dropins = Paths.get(carbonHome, dropinsDirectory);
        DropinsBundleDeployerUtils.getNewBundlesInfo(dropins);
    }

    @Test(description = "Attempts to load OSGi bundle information from a null folder path", priority = 3,
            expectedExceptions = { IOException.class })
    public void testGettingNewBundlesInfoFromInvalidFolder() throws IOException {
        DropinsBundleDeployerUtils.getNewBundlesInfo(null);
    }

    @Test(description = "Attempts to check whether to update a non-existing bundles.info file", priority = 3,
            expectedExceptions = { IOException.class })
    public void testUpdatingBundlesInfoCheckForNonExistingFile() throws IOException {
        Path bundlesInfo = Paths.get(carbonHome, dropinsDirectory, bundlesInfoFile);
        DropinsBundleDeployerUtils.hasToUpdateBundlesInfoFile(null, bundlesInfo);
    }

    @Test(description = "Attempts to check whether to update a null file", priority = 3,
            expectedExceptions = { IOException.class })
    public void testUpdatingBundlesInfoCheckForInvalidFile() throws IOException {
        DropinsBundleDeployerUtils.hasToUpdateBundlesInfoFile(null, null);
    }

    @Test(description = "Attempts to merge dropins bundle info of a non-existing bundles.info file", priority = 3,
            expectedExceptions = { IOException.class })
    public void testMergingDropinsBundlesInfoWithNonExistingFile() throws IOException {
        Path bundlesInfo = Paths.get(carbonHome, dropinsDirectory, bundlesInfoFile);
        DropinsBundleDeployerUtils.mergeDropinsBundleInfo(null, bundlesInfo);
    }

    @Test(description = "Attempts to dropins bundle info merger with a null file", priority = 3,
            expectedExceptions = { IOException.class })
    public void testMergingDropinsBundlesInfoWithInvalidFile() throws IOException {
        DropinsBundleDeployerUtils.mergeDropinsBundleInfo(null, null);
    }

    /**
     * Utility functions for dropins unit-tests.
     */

    private static void createProfiles() throws IOException {
        profileNames.add(Constants.DEFAULT_PROFILE);
        profileNames.add(profileMSS);

        for (String profileName : profileNames) {
            Path profile = Paths.get(carbonHome, Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH,
                    profileName, "configuration", "org.eclipse.equinox.simpleconfigurator");
            createDirectories(profile);
            if (Files.exists(profile)) {
                Files.createFile(Paths.get(profile.toString(), bundlesInfoFile));
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
                dropinsDirectory + "/org.eclipse.osgi_" + equinoxOSGiVersion + ".jar,4,true"));

        String equinoxSimpleConfiguratorVersion = System.getProperty("equinox.simpleconfigurator.version");
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.equinox.simpleconfigurator," +
                equinoxSimpleConfiguratorVersion + ",../../" + dropinsDirectory +
                "/org.eclipse.equinox.simpleconfigurator_" + equinoxSimpleConfiguratorVersion + ".jar,4,true"));

        String equinoxUtilVersion = System.getProperty("equinox.util.version");
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.equinox.util," + equinoxUtilVersion + ",../../" +
                dropinsDirectory + "/org.eclipse.equinox.util_" + equinoxUtilVersion + ".jar,4,true"));

        String equinoxLauncherVersion = System.getProperty("equinox.launcher.version");
        bundleInfo.add(BundleInfo.getInstance(
                "org.eclipse.equinox.launcher," + equinoxLauncherVersion + ",../../" + dropinsDirectory
                        + "/org.eclipse.equinox.launcher_" + equinoxLauncherVersion + ".jar,4,true"));

        return bundleInfo;
    }

    private static List<BundleInfo> getActualBundleInfo(Path bundleInfoFile) throws IOException {
        if ((bundleInfoFile != null) && (Files.exists(bundleInfoFile))) {
            List<String> bundleInfoLines = Files.readAllLines(bundleInfoFile);
            List<BundleInfo> bundleInfo = new ArrayList<>();
            bundleInfoLines.stream().forEach(line -> bundleInfo.add(BundleInfo.getInstance(line)));

            return bundleInfo;
        } else {
            throw new IOException("Invalid bundles.info file specified");
        }
    }

    private static boolean compareBundleInfo(List<BundleInfo> expected, List<BundleInfo> actual) {
        return (expected.size() == actual.size()) && ((expected.stream().filter(bundleInfo ->
                actual.stream().filter(actualBundleInfo -> actualBundleInfo.equals(bundleInfo)).count() == 1).count())
                == expected.size());
    }

    private static boolean delete(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            List<Path> children = listFiles(path);
            if (children.size() > 0) {
                for (Path aChild : children) {
                    delete(aChild);
                }
            }
        }
        return Files.deleteIfExists(path);
    }

    private static List<Path> listFiles(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            directoryStream.forEach(files::add);
        }
        return files;
    }
}
