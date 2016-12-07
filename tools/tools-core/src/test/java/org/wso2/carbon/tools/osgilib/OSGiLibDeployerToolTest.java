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
package org.wso2.carbon.tools.osgilib;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.extensions.model.BundleInfo;
import org.wso2.carbon.tools.CarbonToolExecutor;
import org.wso2.carbon.tools.TestConstants;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * This class defines the unit test cases for Carbon OSGi-lib tool.
 *
 * @since 5.1.0
 */
public class OSGiLibDeployerToolTest {

    private static final Path carbonHome = Paths.get(TestConstants.TARGET_FOLDER, "carbon-home");
    private static final List<String> profileNames = new ArrayList<>();

    @BeforeClass
    public static void initTestClass() throws IOException {
        createDirectories(Paths.get(carbonHome.toString(), Constants.OSGI_LIB));
    }

    @Test(description = "Attempts to execute OSGi-lib tool when no profiles directory exists", expectedExceptions = {
            IOException.class})
    public void testExecutingToolWhenNoProfilesDirectory() throws CarbonToolException, IOException {
        OSGiLibDeployerToolUtils.executeTool(carbonHome.toString(), TestConstants.ALL_CARBON_PROFILES);
    }

    @Test(description = "Attempts to execute OSGi-lib tool with null Carbon home", expectedExceptions = {
            CarbonToolException.class})
    public void testExecutingToolWithInvalidCarbonHome() throws CarbonToolException, IOException {
        OSGiLibDeployerToolUtils.executeTool(null, TestConstants.ALL_CARBON_PROFILES);
    }

    @Test(description = "Attempts to execute OSGi-lib tool with empty Carbon home", expectedExceptions = {
            CarbonToolException.class})
    public void testExecutingToolWithEmptyCarbonHome() throws CarbonToolException, IOException {
        OSGiLibDeployerToolUtils.executeTool("", TestConstants.ALL_CARBON_PROFILES);
    }

    @Test(description = "Attempts to execute OSGi-lib capability with a single available Carbon Profile "
            + "with a non-OSGi bundle in OSGi-lib", priority = 1)
    public void testExecutingOSGiLibCapabilityWithANonOSGiJAR() throws CarbonToolException, IOException {
        Path profile = Paths.get(carbonHome.toString(), Constants.PROFILE_REPOSITORY,
                Constants.DEFAULT_PROFILE, "configuration", "org.eclipse.equinox.simpleconfigurator");
        createDirectories(profile);

        if (Files.exists(profile)) {
            Files.createFile(Paths.get(profile.toString(), Constants.BUNDLES_INFO));
        }
        profileNames.add(Constants.DEFAULT_PROFILE);
        prepareCarbonHomeForOSGiLibTests();

        System.setProperty(org.wso2.carbon.tools.Constants.CARBON_TOOL_SYSTEM_PROPERTY, "osgi-lib-deployer");
        String[] args = {Constants.DEFAULT_PROFILE, carbonHome.toString()};
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = getExpectedBundleInfo();
        Path defaultBundleInfo = Paths.get(carbonHome.toString(), Constants.PROFILE_REPOSITORY,
                Constants.DEFAULT_PROFILE, "configuration", "org.eclipse.equinox.simpleconfigurator",
                Constants.BUNDLES_INFO);

        List<BundleInfo> actual = getActualBundleInfo(defaultBundleInfo);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute OSGi-lib capability with a single available Carbon Profile", priority = 2)
    public void testExecutingOSGiLibCapabilityWithASingleProfile() throws CarbonToolException, IOException {
        Files.deleteIfExists(Paths.get(carbonHome.toString(), Constants.OSGI_LIB, TestConstants.ARTIFACT_FIVE));

        String[] args = {Constants.DEFAULT_PROFILE, carbonHome.toString()};
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = getExpectedBundleInfo();
        Path defaultBundleInfo = Paths.get(carbonHome.toString(), Constants.PROFILE_REPOSITORY,
                Constants.DEFAULT_PROFILE, "configuration", "org.eclipse.equinox.simpleconfigurator",
                Constants.BUNDLES_INFO);

        List<BundleInfo> actual = getActualBundleInfo(defaultBundleInfo);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute OSGi-lib capability with all available Carbon Profiles", priority = 3)
    public void testExecutingOSGiLibCapabilityWithAllProfiles() throws CarbonToolException, IOException {
        createOtherProfiles();

        List<BundleInfo> expected = getExpectedBundleInfo();
        List<BundleInfo> actual;

        String[] args = { TestConstants.ALL_CARBON_PROFILES, carbonHome.toString()};
        CarbonToolExecutor.main(args);

        for (String profileName : profileNames) {
            Path bundlesInfo = Paths.
                    get(carbonHome.toString(), Constants.PROFILE_REPOSITORY, profileName,
                            "configuration", "org.eclipse.equinox.simpleconfigurator", Constants.BUNDLES_INFO);
            actual = getActualBundleInfo(bundlesInfo);
            boolean matching = compareBundleInfo(expected, actual);
            if (!matching) {
                Assert.fail();
            }
        }
        Assert.assertTrue(true);
    }

    @Test(description = "Attempts to execute OSGi-lib capability with an empty Carbon Profile name", priority = 4)
    public void testExecutingOSGiLibCapabilityForEmptyProfile() throws IOException {
        Path profile = Paths.
                get(carbonHome.toString(), Constants.PROFILE_REPOSITORY,
                        TestConstants.APP_MANAGER_PROFILE, "configuration", "org.eclipse.simpleconfigurator");
        createDirectories(profile);

        if (Files.exists(profile)) {
            Files.createFile(Paths.get(profile.toString(), Constants.BUNDLES_INFO));
        }
        profileNames.add(TestConstants.APP_MANAGER_PROFILE);

        String[] args = {"", carbonHome.toString()};
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = new ArrayList<>();
        List<BundleInfo> actual = getActualBundleInfo(Paths.get(profile.toString(), Constants.BUNDLES_INFO));
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute OSGi-lib capability with null tool arguments", priority = 5)
    public void testExecutingOSGiLibCapabilityForInvalidToolArgs() throws IOException {
        Path profile = Paths.
                get(carbonHome.toString(), Constants.PROFILE_REPOSITORY,
                        TestConstants.APP_MANAGER_PROFILE, "configuration", "org.eclipse.simpleconfigurator");

        CarbonToolExecutor.main(null);
        List<BundleInfo> expected = new ArrayList<>();
        List<BundleInfo> actual = getActualBundleInfo(Paths.get(profile.toString(), Constants.BUNDLES_INFO));
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute OSGi-lib capability for an invalidly structured Profile bundles.info file "
            + "path. OSGi-lib capability executed on a single Profile.", priority = 6)
    public void testExecutingOSGiLibCapabilityForInvalidBundlesInfoPath() throws IOException {
        Path profile = Paths.get(carbonHome.toString(), Constants.PROFILE_REPOSITORY,
                TestConstants.APP_MANAGER_PROFILE, "configuration", "org.eclipse.simpleconfigurator");

        String[] args = { TestConstants.APP_MANAGER_PROFILE, carbonHome.toString()};
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = new ArrayList<>();
        List<BundleInfo> actual = getActualBundleInfo(Paths.get(profile.toString(), Constants.BUNDLES_INFO));
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    @Test(description = "Attempts to execute OSGi-lib capability for an invalidly structured Profile bundles.info file "
            + "path. OSGi-lib capability executed on multiple Profiles.", priority = 6)
    public void testExecutingOSGiLibCapabilityForMultipleProfilesWithInvalidBundlesInfoPath() throws IOException {
        Path profile = Paths.get(carbonHome.toString(), Constants.PROFILE_REPOSITORY,
                TestConstants.APP_MANAGER_PROFILE, "configuration", "org.eclipse.simpleconfigurator");

        String[] args = { TestConstants.ALL_CARBON_PROFILES, carbonHome.toString()};
        CarbonToolExecutor.main(args);

        List<BundleInfo> expected = new ArrayList<>();
        List<BundleInfo> actual = getActualBundleInfo(Paths.get(profile.toString(), Constants.BUNDLES_INFO));
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
            Path profile = Paths.
                    get(carbonHome.toString(), Constants.PROFILE_REPOSITORY, profileName,
                            "configuration", "org.eclipse.equinox.simpleconfigurator");
            createDirectories(profile);
            if (Files.exists(profile)) {
                Path bundlesInfo = Paths.get(profile.toString(), Constants.BUNDLES_INFO);
                if (!Files.exists(bundlesInfo)) {
                    Files.createFile(bundlesInfo);
                }
            }
        }
    }

    private static void prepareCarbonHomeForOSGiLibTests() throws IOException {
        Files.copy(Paths.get(TestConstants.TARGET_FOLDER, TestConstants.TEST_RESOURCES, Constants.OSGI_LIB,
                TestConstants.ARTIFACT_ONE),
                Paths.get(carbonHome.toString(), Constants.OSGI_LIB, TestConstants.ARTIFACT_ONE));
        Files.copy(Paths.get(TestConstants.TARGET_FOLDER, TestConstants.TEST_RESOURCES, Constants.OSGI_LIB,
                TestConstants.ARTIFACT_TWO),
                Paths.get(carbonHome.toString(), Constants.OSGI_LIB, TestConstants.ARTIFACT_TWO));
        Files.copy(Paths.get(TestConstants.TARGET_FOLDER, TestConstants.TEST_RESOURCES, Constants.OSGI_LIB,
                TestConstants.ARTIFACT_THREE),
                Paths.get(carbonHome.toString(), Constants.OSGI_LIB, TestConstants.ARTIFACT_THREE));
        Files.copy(Paths.get(TestConstants.TARGET_FOLDER, TestConstants.TEST_RESOURCES, Constants.OSGI_LIB,
                TestConstants.ARTIFACT_FOUR),
                Paths.get(carbonHome.toString(), Constants.OSGI_LIB, TestConstants.ARTIFACT_FOUR));
        Files.copy(Paths.get(TestConstants.TARGET_FOLDER, TestConstants.TEST_RESOURCES, Constants.OSGI_LIB,
                TestConstants.ARTIFACT_FIVE),
                Paths.get(carbonHome.toString(), Constants.OSGI_LIB, TestConstants.ARTIFACT_FIVE));
    }

    private static List<BundleInfo> getExpectedBundleInfo() {
        List<BundleInfo> bundleInfo = new ArrayList<>();
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.osgi," + TestConstants.EQUINOX_OSGI_VERSION + ",../../" +
                Constants.OSGI_LIB + "/" + TestConstants.ARTIFACT_ONE + ",4,true"));
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.equinox.simpleconfigurator," +
                TestConstants.EQUINOX_SMP_CONFIGURATOR_VERSION + ",../../" + Constants.OSGI_LIB +
                "/" + TestConstants.ARTIFACT_TWO + ",4,true"));
        bundleInfo.add(BundleInfo
                .getInstance("org.eclipse.equinox.util," + TestConstants.EQUINOX_UTIL_VERSION + ",../../" +
                        Constants.OSGI_LIB + "/" + TestConstants.ARTIFACT_THREE + ",4,true"));
        bundleInfo.add(BundleInfo.getInstance("org.eclipse.equinox.launcher," + 
                TestConstants.EQUINOX_LAUNCHER_VERSION + ",../../" + Constants.OSGI_LIB + "/" + 
                TestConstants.ARTIFACT_FOUR + ",4,true"));

        return bundleInfo;
    }

    private static List<BundleInfo> getActualBundleInfo(Path bundleInfoFile) throws IOException {
        if ((bundleInfoFile != null) && (Files.exists(bundleInfoFile))) {
            List<String> bundleInfoLines = Files.readAllLines(bundleInfoFile);
            List<BundleInfo> bundleInfo = new ArrayList<>();
            bundleInfoLines.stream().forEach(line -> bundleInfo.add(BundleInfo.getInstance(line)));

            return bundleInfo;
        } else {
            throw new IOException("Invalid " + Constants.BUNDLES_INFO + " file specified");
        }
    }

    private static boolean compareBundleInfo(List<BundleInfo> expected, List<BundleInfo> actual) {
        return (expected.size() == actual.size()) && ((expected.stream().filter(bundleInfo ->
                actual.stream().filter(actualBundleInfo -> actualBundleInfo.equals(bundleInfo)).count() == 1).count())
                == expected.size());
    }
}
