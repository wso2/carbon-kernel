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
import org.wso2.carbon.launcher.extensions.model.BundleInfo;
import org.wso2.carbon.tools.CarbonToolManager;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class defines the unit test cases for DropinsDeployerToolUtils.java.
 *
 * @since 5.1.0
 */
public class DropinsDeployerToolTest {
    private static final String testArtifactOne = "org.wso2.carbon.sample.datasource.mgt.jar";
    private static final String testArtifactTwo = "org.wso2.carbon.sample.dbs.deployer.jar";
    private static final String testArtifactThree = "org.wso2.carbon.sample.deployer.mgt.jar";
    private static final String testArtifactFour = "tool-test-artifact.jar";
    private static final Path carbonHome = Paths.get("target", "carbon-home");
    private static final List<String> profileNames = new ArrayList<>();

    @BeforeClass
    public static void initTestClass() throws IOException {
        createDirectories(carbonHome);
    }

    @Test(description = "Attempts to get Carbon profiles when no profiles directory exists", expectedExceptions = {
            CarbonToolException.class }, priority = 0)
    public void testGettingProfilesWhenNoProfilesDirectory() throws CarbonToolException {
        DropinsDeployerToolUtils.executeTool(carbonHome.toString());
    }

    @Test(description = "Attempts to retrieve a string notifying the Carbon profiles available when no profiles "
            + "are available", priority = 1)
    public void testRetrievingProfileStringWhenNoProfileExists() throws IOException {
        prepareCarbonHomeForDropinsTests();
        StringBuilder actual = DropinsDeployerToolUtils.getProfileString(carbonHome.toString());
        Assert.assertTrue("WSO2 CARBON PROFILES\nNo profiles available".equals(actual.toString()));
    }

    @Test(description = "Attempts to retrieve an non-existent Carbon Profile", priority = 2)
    public void testChoosingNonExistingCarbonProfile() throws IOException {
        Optional<String> profileName = DropinsDeployerToolUtils.getUserChoice(carbonHome.toString(), 7);
        Assert.assertTrue(!profileName.isPresent());
    }

    @Test(description = "Attempts to execute dropins capability with all available Carbon Profiles", priority = 3)
    public void testExecutingDropinsCapabilityWithAllProfiles() throws CarbonToolException, IOException {
        createProfiles();
        ByteArrayInputStream stream = new ByteArrayInputStream(("" + (profileNames.size() + 1)).getBytes());
        System.setIn(stream);

        List<BundleInfo> expected = getExpectedBundleInfo();
        List<BundleInfo> actual;

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        String[] args = { carbonHome.toString() };
        CarbonToolManager.main(args);

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

    @Test(description = "Attempts to execute dropins capability with a single available Carbon Profiles", priority = 4)
    public void testExecutingDropinsCapabilityWithASingleProfile() throws CarbonToolException, IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(("" + 3).getBytes());
        System.setIn(stream);

        List<BundleInfo> expected = getExpectedBundleInfo();
        List<BundleInfo> actual;

        System.setProperty("wso2.carbon.tool", "dropins-deployer");
        String[] args = { carbonHome.toString() };
        CarbonToolManager.main(args);

        Path bundlesInfo = Paths.get(carbonHome.toString(), "osgi", "profiles", "AS", "configuration",
                "org.eclipse.equinox.simpleconfigurator", "bundles.info");
        actual = getActualBundleInfo(bundlesInfo);
        Assert.assertTrue(compareBundleInfo(expected, actual));
    }

    private static void createDirectories(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    private static void createProfiles() throws IOException {
        profileNames.add("default");
        profileNames.add("MSS");
        profileNames.add("AS");

        for (String profileName : profileNames) {
            Path profile = Paths.get(carbonHome.toString(), "osgi", "profiles", profileName, "configuration",
                    "org.eclipse.equinox.simpleconfigurator");
            createDirectories(profile);
            if (Files.exists(profile)) {
                Files.createFile(Paths.get(profile.toString(), "bundles.info"));
            }
        }
    }

    private static void prepareCarbonHomeForDropinsTests() throws IOException {
        Path dropins = Paths.get(carbonHome.toString(), "osgi", "dropins");
        createDirectories(dropins);
        Path profiles = Paths.get(carbonHome.toString(), "osgi", "profiles");
        createDirectories(profiles);

        Files.copy(Paths.get("target", "test-resources", "dropins", testArtifactOne),
                Paths.get(dropins.toString(), testArtifactOne));
        Files.copy(Paths.get("target", "test-resources", "dropins", testArtifactTwo),
                Paths.get(dropins.toString(), testArtifactTwo));
        Files.copy(Paths.get("target", "test-resources", "dropins", testArtifactThree),
                Paths.get(dropins.toString(), testArtifactThree));
        Files.copy(Paths.get("target", "test-resources", "dropins", testArtifactFour),
                Paths.get(dropins.toString(), testArtifactFour));
    }

    private static List<BundleInfo> getExpectedBundleInfo() {
        List<BundleInfo> bundleInfo = new ArrayList<>();

        String kernelVersion = System.getProperty("carbon.kernel.version").replace("-", ".");

        bundleInfo.add(BundleInfo.getInstance("org.wso2.carbon.sample.datasource.mgt," + kernelVersion
                + ",../../dropins/org.wso2.carbon.sample.datasource.mgt.jar,4,true"));
        bundleInfo.add(BundleInfo.getInstance("org.wso2.carbon.sample.dbs.deployer," + kernelVersion
                + ",../../dropins/org.wso2.carbon.sample.dbs.deployer.jar,4,true"));
        bundleInfo.add(BundleInfo.getInstance("org.wso2.carbon.sample.deployer.mgt," + kernelVersion
                + ",../../dropins/org.wso2.carbon.sample.deployer.mgt.jar,4,true"));

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
