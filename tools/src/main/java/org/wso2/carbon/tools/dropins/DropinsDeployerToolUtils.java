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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Java class which defines utility functions used within the OSGi dropins bundle deployer tool.
 *
 * @since 5.1.0
 */
public class DropinsDeployerToolUtils {
    private static final Logger logger = Logger.getLogger(DropinsDeployerToolUtils.class.getName());

    /**
     * Executes the WSO2 Carbon dropins deployer tool.
     *
     * @param carbonHome the {@link String} value of carbon.home
     * @param profile    the Carbon Profile identifier
     * @throws CarbonToolException if the {@code carbonHome} is invalid
     * @throws IOException         if an I/O error occurs when extracting the Carbon Profile names
     */
    public static void executeTool(String carbonHome, String profile) throws CarbonToolException, IOException {
        if ((carbonHome == null) || (carbonHome.isEmpty())) {
            throw new CarbonToolException("Invalid Carbon home specified: " + carbonHome);
        }

        if (profile == null) {
            logger.log(Level.INFO, getHelpMessage());
        } else {
            if (profile.equals("ALL")) {
                DropinsBundleDeployerUtils.getCarbonProfiles(carbonHome).forEach(carbonProfile -> {
                    try {
                        DropinsBundleDeployerUtils.executeDropinsCapability(carbonHome, carbonProfile);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE,
                                "Failed to update the OSGi bundle information of Carbon Profile: " + carbonProfile, e);
                    }
                });
            } else {
                try {
                    DropinsBundleDeployerUtils.executeDropinsCapability(carbonHome, profile);
                } catch (IOException e) {
                    logger.log(Level.SEVERE,
                            "Failed to update the OSGi bundle information of Carbon Profile: " + profile, e);
                }
            }
        }
    }

    /**
     * Returns a help message for the dropins tool usage.
     *
     * @return a help message for the dropins tool usage
     */
    private static String getHelpMessage() {
        return "Incorrect usage of the dropins deployer tool.\n" + "Instructions: sh dropins.sh $profile\n"
                + "$profile\tname of the Carbon Profile to be updated\n" + "Keyword options for $profile:\n"
                + "ALL\tUpdate dropins OSGi bundle information of all Carbon Profiles (ex: sh dropins.sh ALL)\n";
    }
}
