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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * This class defines the unit test cases for DropinsDeployerUtils.java.
 *
 * @since 5.1.0
 */
public class DropinsDeployerUtilsTest {
    private static final String testArtifactOne = "org.wso2.carbon.sample.datasource.mgt.jar";
    private static final String testArtifactTwo = "org.wso2.carbon.sample.dbs.deployer.jar";
    private static final String testArtifactThree = "org.wso2.carbon.sample.deployer.mgt.jar";
    private static final Path carbonHome = Paths.get("target", "carbon-home");
    private static final List<String> profileNames = new ArrayList<>();

    @BeforeClass
    public static void initTestClass() throws IOException {
        createDirectories(carbonHome);

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
    }

    @Test(description = "Attempts to retrieve a string notifying the Carbon profiles available when no profiles "
            + "are available", priority = 0)
    public void testRetrievingProfileStringWhenNoProfileExists() throws IOException {
        StringBuilder actual = DropinsDeployerUtils.getProfileString(carbonHome.toString());
        Assert.assertTrue("WSO2 CARBON PROFILES\nNo profiles available".equals(actual.toString()));
    }

    @Test(description = "Attempts to retrieve a string notifying the Carbon profiles available", priority = 1)
    public void testRetrievingProfileStringWhenProfilesExist() throws IOException {
        createProfiles();
        StringBuilder expected = new StringBuilder("WSO2 CARBON PROFILES\n");
        IntStream.range(0, profileNames.size()).forEach(
                (index) -> expected.append(index + 1).append(". ").append(profileNames.get(index)).append("\n"));
        expected.append("Choose the appropriate profile number: \n");

        StringBuilder actual = DropinsDeployerUtils.getProfileString(carbonHome.toString());
        Assert.assertTrue(expected.toString().equals(actual.toString()));
    }

    @Test(description = "Attempts to retrieve an existing, valid Carbon Profile", priority = 2)
    public void testChoosingExistingCarbonProfile() throws IOException {
        Optional<String> profileName = DropinsDeployerUtils.getUserChoice(carbonHome.toString(), 3);
        Assert.assertTrue(profileName.get().equals("AS"));
    }

    @Test(description = "Attempts to retrieve an non-existent Carbon Profile", priority = 3)
    public void testChoosingNonExistingCarbonProfile() throws IOException {
        Optional<String> profileName = DropinsDeployerUtils.getUserChoice(carbonHome.toString(), 4);
        Assert.assertTrue(!profileName.isPresent());
    }

    @Test(description = "Attempts to test the validity of the OSGi bundle information written to profile "
            + "bundles.info files", priority = 4)
    public void testExecutingDropinsCapability() throws IOException {
        List<BundleInfo> expected = getExpectedBundleInfo();
        List<BundleInfo> actual;

        for (String profileName : profileNames) {
            Path bundlesInfo = Paths.get(carbonHome.toString(), "osgi", "profiles", profileName, "configuration",
                    "org.eclipse.equinox.simpleconfigurator", "bundles.info");
            DropinsDeployerUtils.executeDropinsCapability(carbonHome.toString(), profileName);
            actual = getActualBundleInfo(bundlesInfo);
            boolean matching = compareBundleInfo(expected, actual);
            if (!matching) {
                Assert.fail();
            }
        }

        Assert.assertTrue(true);
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
