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
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * A Java class which defines utility functions used within the OSGi dropins bundle deployer tool.
 *
 * @since 5.1.0
 */
public class DropinsDeployerToolUtils {
    private static final Logger logger = Logger.getLogger(DropinsDeployerTool.class.getName());

    /**
     * Executes the WSO2 Carbon dropins deployer tool.
     *
     * @param carbonHome the {@link String} value of carbon.home
     * @throws CarbonToolException if an error occurred when executing the tool
     */
    public static void executeTool(String carbonHome) throws CarbonToolException {
        try {
            List<String> carbonProfiles = DropinsBundleDeployerUtils.getCarbonProfiles(carbonHome);
            if (carbonProfiles.size() > 0) {
                StringBuilder message = getProfileString(carbonHome);
                logger.log(Level.INFO, message.toString());

                String userChoice = new Scanner(System.in, "UTF-8").nextLine();

                Optional<String> profileName = getUserChoice(carbonHome, Integer.parseInt(userChoice));
                if (profileName.isPresent()) {
                    String name = profileName.get();
                    if (name.equals("All")) {
                        for (String profile : carbonProfiles) {
                            DropinsBundleDeployerUtils.executeDropinsCapability(carbonHome, profile);
                        }
                    } else {
                        DropinsBundleDeployerUtils.executeDropinsCapability(carbonHome, name);
                    }
                } else {
                    throw new CarbonToolException("Invalid WSO2 Carbon Profile name specified");
                }
            } else {
                StringBuilder message = getProfileString(carbonHome);
                logger.log(Level.INFO, message.toString());
            }
        } catch (IOException e) {
            throw new CarbonToolException("An I/O error occurred when executing the dropins deployer tool", e);
        }
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
            userProfiles.append(profiles.size() + 1).append(". All\n");
            userProfiles.append("Choose the appropriate profile number: ");
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
        profiles.add("All");

        if ((userChoice > 0) && (userChoice <= (profiles.size() + 1))) {
            return Optional.ofNullable(profiles.get(userChoice - 1));
        } else {
            return Optional.empty();
        }
    }
}
