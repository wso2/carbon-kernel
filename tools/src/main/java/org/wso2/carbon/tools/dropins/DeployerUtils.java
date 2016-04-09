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
import org.wso2.carbon.launcher.utils.Utils;

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
public class DeployerUtils {
    protected static void executeDropinsCapability(String carbonHome, String profileName) throws IOException {
        String homeDirectory = Optional.ofNullable(carbonHome).orElse(Utils.getCarbonHomeDirectory().toString());
        Optional.ofNullable(homeDirectory).orElseThrow(() -> new RuntimeException("No CARBON_HOME specified"));

        Path bundlesInfoFile = Paths.get(carbonHome, "osgi", "profiles", profileName, "configuration",
                "org.eclipse.equinox.simpleconfigurator", "bundles.info");
        DropinsBundleDeployerUtils.executeDropinsCapability(bundlesInfoFile);
    }

    protected static StringBuilder getProfileString(String carbonHome) throws IOException {
        String homeDirectory = Optional.ofNullable(carbonHome).orElse(Utils.getCarbonHomeDirectory().toString());
        Optional.ofNullable(homeDirectory).orElseThrow(() -> new RuntimeException("No CARBON_HOME specified"));

        final StringBuilder userProfiles = new StringBuilder("WSO2 CARBON PROFILES\n");
        List<String> profiles = DropinsBundleDeployerUtils.getCarbonProfiles(carbonHome);
        IntStream.range(0, profiles.size()).forEach(
                (index) -> userProfiles.append(index + 1).append(". ").append(profiles.get(index)).append("\n"));
        userProfiles.append("Choose the appropriate profile number: \n");

        return userProfiles;
    }

    protected static Optional<String> getUserChoice(String carbonHome, int userChoice) throws IOException {
        List<String> profiles = DropinsBundleDeployerUtils.getCarbonProfiles(carbonHome);

        if ((userChoice > 0) && (userChoice < profiles.size())) {
            return Optional.ofNullable(profiles.get(userChoice));
        } else {
            return Optional.empty();
        }
    }
}
