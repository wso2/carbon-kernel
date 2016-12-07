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

import org.wso2.carbon.launcher.Constants;
import org.wso2.carbon.launcher.extensions.OSGiLibBundleDeployerUtils;
import org.wso2.carbon.launcher.extensions.model.BundleInfo;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Java class which defines utility functions used within the OSGi-lib bundle deployer tool.
 *
 * @since 5.1.0
 */
class OSGiLibDeployerToolUtils {
    private static final Logger logger = Logger.getLogger(OSGiLibDeployerToolUtils.class.getName());

    /**
     * Executes the WSO2 Carbon OSGi-lib deployer tool.
     *
     * @param carbonHome the {@link String} value of carbon.home
     * @param profile    the Carbon Profile identifier
     * @throws CarbonToolException if the {@code carbonHome} is invalid
     * @throws IOException         if an I/O error occurs when extracting the Carbon Profile names
     */
    static void executeTool(String carbonHome, String profile) throws CarbonToolException, IOException {
        if ((carbonHome == null) || (carbonHome.isEmpty())) {
            throw new CarbonToolException("Invalid Carbon home specified: " + carbonHome);
        }

        if (profile != null) {
            Path osgiLibDirectoryPath = Paths.get(carbonHome, Constants.OSGI_LIB);
            logger.log(Level.FINE,
                    "Loading the new OSGi bundle information from " + Constants.OSGI_LIB + " folder...");
            List<BundleInfo> newBundlesInfo = OSGiLibBundleDeployerUtils.getBundlesInfo(osgiLibDirectoryPath);
            logger.log(Level.FINE, "Successfully loaded the new OSGi bundle information from " + Constants.OSGI_LIB +
                    " folder");

            if (profile.equals("ALL")) {
                OSGiLibBundleDeployerUtils.getCarbonProfiles(carbonHome)
                        .forEach(carbonProfile -> {
                            try {
                                OSGiLibBundleDeployerUtils.updateOSGiLib(carbonHome, carbonProfile, newBundlesInfo);
                            } catch (IOException e) {
                                logger.log(Level.SEVERE,
                                        "Failed to update the OSGi bundle information of Carbon Profile: "
                                                + carbonProfile, e);
                            }
                        });
            } else {
                try {
                    OSGiLibBundleDeployerUtils.updateOSGiLib(carbonHome, profile, newBundlesInfo);
                } catch (IOException e) {
                    logger.log(Level.SEVERE,
                            "Failed to update the OSGi bundle information of Carbon Profile: " + profile, e);
                }
            }
        }
    }

    /**
     * Returns a help message for the OSGi-lib tool usage.
     *
     * @return a help message for the OSGi-lib tool usage
     */
    static String getHelpMessage() {
        return "Incorrect usage of the OSGi-lib deployer tool.\n\n" +
                "Instructions: sh osgi-lib.sh [profile]\n" + "profile - name of the Carbon Profile to be updated\n\n" +
                "Keyword option for profile:\n" +
                "ALL\tUpdate OSGi-lib bundle information of all Carbon Profiles " +
                "(ex: sh osgi-lib.sh ALL/osgi-lib.bat ALL)\n";
    }
}
