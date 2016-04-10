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

import org.wso2.carbon.launcher.extensions.DropinsBundleDeployerUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * A Java class which defines utility functions used within the OSGi dropins bundle deployer tool.
 *
 * @since 5.1.0
 */
public class DropinsDeployerUtils {
    /**
     * Executes the dropins capability on the specified WSO2 Carbon Profile specified.
     *
     * @param carbonHome  the {@link String} value of carbon.home
     * @param profileName the name of the Carbon Profile
     * @throws IOException if an I/O error occurs
     */
    public static void executeDropinsCapability(String carbonHome, String profileName) throws IOException {
        Path bundlesInfoFile = Paths.get(carbonHome, "osgi", "profiles", profileName, "configuration",
                "org.eclipse.equinox.simpleconfigurator", "bundles.info");
        DropinsBundleDeployerUtils.executeDropinsCapability(carbonHome, bundlesInfoFile);
    }

    /**
     * Generates a user interface message specifying the available Carbon Profiles.
     *
     * @param carbonHome the {@link String} value of carbon.home
     * @return a user interface message specifying the available Carbon Profiles
     * @throws IOException if an I/O error occurs
     */
    public static StringBuilder getProfileString(String carbonHome) throws IOException {
        final StringBuilder userProfiles = new StringBuilder("WSO2 CARBON PROFILES\n");
        List<String> profiles = DropinsBundleDeployerUtils.getCarbonProfiles(carbonHome);
        if (profiles.size() > 0) {
            IntStream.range(0, profiles.size()).forEach(
                    (index) -> userProfiles.append(index + 1).append(". ").append(profiles.get(index)).append("\n"));
            userProfiles.append("Choose the appropriate profile number: \n");
        } else {
            userProfiles.append("No profiles available");
        }

        return userProfiles;
    }

    /**
     * Returns the user's choice of WSO2 Carbon Profile based on the index provided.
     *
     * @param carbonHome the {@link String} value of carbon.home
     * @param userChoice the user's choice in integer form based on the profile numbering specified on the
     *                   user interface
     * @return the user's choice of WSO2 Carbon Profile based on the index provided
     * @throws IOException if an I/O error occurs
     */
    public static Optional<String> getUserChoice(String carbonHome, int userChoice) throws IOException {
        List<String> profiles = DropinsBundleDeployerUtils.getCarbonProfiles(carbonHome);

        if ((userChoice > 0) && (userChoice <= profiles.size())) {
            return Optional.ofNullable(profiles.get(userChoice - 1));
        } else {
            return Optional.empty();
        }
    }
}
