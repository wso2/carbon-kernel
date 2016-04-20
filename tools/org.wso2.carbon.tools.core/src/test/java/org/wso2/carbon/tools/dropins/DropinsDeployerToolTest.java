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
package org.wso2.carbon.tools.dropins;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.extensions.model.BundleInfo;
import org.wso2.carbon.tools.CarbonToolExecutor;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the unit test cases for DropinsDeployerToolUtils.java.
 *
 * @since 5.1.0
 */
public class DropinsDeployerToolTest {
    private static final String equinoxOSGiVersion = System.getProperty("equinox.osgi.version");
    private static final String equinoxSimpleConfiguratorVersion = System.
            getProperty("equinox.simpleconfigurator.version");
    private static final String equinoxUtilVersion = System.getProperty("equinox.util.version");
    private static final String equinoxLauncherVersion = System.getProperty("equinox.launcher.version");
    private static final String carbonKernelVersion = System.getProperty("carbon.kernel.version");

    private static final String artifactOne = "org.eclipse.osgi_" + equinoxOSGiVersion + ".jar";
    private static final String artifactTwo =
            "org.eclipse.equinox.simpleconfigurator_" + equinoxSimpleConfiguratorVersion + ".jar";
    private static final String artifactThree = "org.eclipse.equinox.util_" + equinoxUtilVersion + ".jar";
    private static final String artifactFour = "org.eclipse.equinox.launcher_" + equinoxLauncherVersion + ".jar";
    private static final String artifactFive = "tool-test-artifact-" + carbonKernelVersion + ".jar";

    private static final String dropinsDirectory = "dropins";
    private static final String allProfilesIndicator = "ALL";
    private static final Path carbonHome = Paths.get("target", "carbon-home");
    private static final List<String> profileNames = new ArrayList<>();

    @BeforeClass
    public static void initTestClass() throws IOException {
        createDirectories(Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, dropinsDirectory));
    }

    @Test(description = "Attempts to execute dropins tool when no profiles directory exists", expectedExceptions = {
            IOException.class }, priority = 0)
    public void testExecutingToolWhenNoProfilesDirectory() throws CarbonToolException, IOException {
        DropinsDeployerToolUtils.executeTool(carbonHome.toString(), allProfilesIndicator);
    }

    @Test(description = "Attempts to execute dropins tool with null Carbon home", expectedExceptions = {
            CarbonToolException.class }, priority = 0)
    public void testExecutingToolWithInvalidCarbonHome() throws CarbonToolException, IOException {
        DropinsDeployerToolUtils.executeTool(null, allProfilesIndicator);
    }

    @Test(description = "Attempts to execute dropins tool with empty Carbon home", expectedExceptions = {
            CarbonToolException.class }, priority = 0)
    public void testExecutingToolWithEmptyCarbonHome() throws CarbonToolException, IOException {
        DropinsDeployerToolUtils.executeTool("", allProfilesIndicator);
    }

    @Test(description = "Attempts to execute dropins capability with a single available Carbon Profile "
            + "with a non-OSGi bundle in dropins", priority = 1, expectedExceptions = { RuntimeException.class })
    public void testExecutingDropinsCapabilityWithANonOSGiJAR() throws CarbonToolException, IOException {
        Path profile = Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH,
                Constants.DEFAULT_PROFILE, "configuration", "org.eclipse.equinox.simpleconfigurator");
        createDirectories(profile);

        if (Files.exists(profile)) {
            Files.createFile(Paths.get(profile.toString(), "bundles.info"));
        }
        profileNames.add(Constants.DEFAULT_PROFILE);
        prepareCarbonHomeForDropinsTests();

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        String[] args = { Constants.DEFAULT_PROFILE, carbonHome.toString() };
        CarbonToolExecutor.main(args);
    }

    @Test(description = "Attempts to execute dropins capability with a single available Carbon Profile", priority = 2)
    public void testExecutingDropinsCapabilityWithASingleProfile() throws CarbonToolException, IOException {
        Files.deleteIfExists(
                Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, dropinsDirectory, artifactFive));

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        String[] args = { Constants.DEFAULT_PROFILE, carbonHome.toString() };
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = getExpectedBundleInfo();
        Path defaultBundleInfo = Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH,
                Constants.DEFAULT_PROFILE, "configuration", "org.eclipse.equinox.simpleconfigurator", "bundles.info");

        List<BundleInfo> actual = getActualBundleInfo(defaultBundleInfo);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute dropins capability with all available Carbon Profiles", priority = 3)
    public void testExecutingDropinsCapabilityWithAllProfiles() throws CarbonToolException, IOException {
        createOtherProfiles();

        List<BundleInfo> expected = getExpectedBundleInfo();
        List<BundleInfo> actual;

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        String[] args = { allProfilesIndicator, carbonHome.toString() };
        CarbonToolExecutor.main(args);

        for (String profileName : profileNames) {
            Path bundlesInfo = Paths.get(carbonHome.toString(), "osgi", "profiles", profileName, "configuration",
                    "org.eclipse.equinox.simpleconfigurator", "bundles.info");
            actual = getActualBundleInfo(bundlesInfo);
            boolean matching = compareBundleInfo(expected, actual);
            if (!matching) {
                Assert.fail();
            }
        }
        Assert.assertTrue(true);
    }

    @Test(description = "Attempts to execute dropins capability with an empty Carbon Profile name", priority = 4)
    public void testExecutingDropinsCapabilityForEmptyProfile() throws IOException {
        String profileName = "App-Manager";
        Path profile = Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH, profileName,
                "configuration", "org.eclipse.simpleconfigurator");
        createDirectories(profile);

        if (Files.exists(profile)) {
            Files.createFile(Paths.get(profile.toString(), "bundles.info"));
        }
        profileNames.add(profileName);

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        String[] args = { "", carbonHome.toString() };
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = new ArrayList<>();
        List<BundleInfo> actual = getActualBundleInfo(Paths.get(profile.toString(), "bundles.info"));
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute dropins capability with null tool arguments", priority = 5)
    public void testExecutingDropinsCapabilityForInvalidToolArgs() throws IOException {
        String profileName = "App-Manager";
        Path profile = Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH, profileName,
                "configuration", "org.eclipse.simpleconfigurator");

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        CarbonToolExecutor.main(null);

        List<BundleInfo> expected = new ArrayList<>();
        List<BundleInfo> actual = getActualBundleInfo(Paths.get(profile.toString(), "bundles.info"));
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute dropins capability for an invalidly structured Profile bundles.info file "
            + "path. Dropins capability executed on a single Profile.", priority = 6)
    public void testExecutingDropinsCapabilityForInvalidBundlesInfoPath() throws IOException {
        String profileName = "App-Manager";
        Path profile = Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH, profileName,
                "configuration", "org.eclipse.simpleconfigurator");

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        String[] args = { profileName, carbonHome.toString() };
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = new ArrayList<>();
        List<BundleInfo> actual = getActualBundleInfo(Paths.get(profile.toString(), "bundles.info"));
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute dropins capability for an invalidly structured Profile bundles.info file "
            + "path. Dropins capability executed on multiple Profiles.", priority = 6)
    public void testExecutingDropinsCapabilityForMultipleProfilesWithInvalidBundlesInfoPath() throws IOException {
        String profileName = "App-Manager";
        Path profile = Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, Constants.PROFILE_PATH, profileName,
                "configuration", "org.eclipse.simpleconfigurator");

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        String[] args = { allProfilesIndicator, carbonHome.toString() };
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = new ArrayList<>();
        List<BundleInfo> actual = getActualBundleInfo(Paths.get(profile.toString(), "bundles.info"));
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    private static void createDirectories(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    private static void createOtherProfiles() throws IOException {
        profileNames.add("MSS");
        profileNames.add("AS");

        for (String profileName : profileNames) {
            Path profile = Paths.get(carbonHome.toString(), "osgi", "profiles", profileName, "configuration",
                    "org.eclipse.equinox.simpleconfigurator");
            createDirectories(profile);
            if (Files.exists(profile)) {
                Path bundlesInfo = Paths.get(profile.toString(), "bundles.info");
                if (!Files.exists(bundlesInfo)) {
                    Files.createFile(bundlesInfo);
                }
            }
        }
    }

    private static void prepareCarbonHomeForDropinsTests() throws IOException {
        Files.copy(Paths.get("target", "test-resources", "dropins", artifactOne),
                Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, dropinsDirectory, artifactOne));
        Files.copy(Paths.get("target", "test-resources", "dropins", artifactTwo),
                Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, dropinsDirectory, artifactTwo));
        Files.copy(Paths.get("target", "test-resources", "dropins", artifactThree),
                Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, dropinsDirectory, artifactThree));
        Files.copy(Paths.get("target", "test-resources", "dropins", artifactFour),
                Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, dropinsDirectory, artifactFour));
        Files.copy(Paths.get("target", "test-resources", "dropins", artifactFive),
                Paths.get(carbonHome.toString(), Constants.OSGI_REPOSITORY, dropinsDirectory, artifactFive));
    }

    private static List<BundleInfo> getExpectedBundleInfo() {
        List<BundleInfo> bundleInfo = new ArrayList<>();
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.osgi," + equinoxOSGiVersion + ",../../" +
                dropinsDirectory + "/org.eclipse.osgi_" + equinoxOSGiVersion + ".jar,4,true"));
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.equinox.simpleconfigurator," +
                equinoxSimpleConfiguratorVersion + ",../../" + dropinsDirectory +
                "/org.eclipse.equinox.simpleconfigurator_" + equinoxSimpleConfiguratorVersion + ".jar,4,true"));
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.equinox.util," + equinoxUtilVersion + ",../../" +
                dropinsDirectory + "/org.eclipse.equinox.util_" + equinoxUtilVersion + ".jar,4,true"));
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
}
