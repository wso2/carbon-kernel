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

import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines a tool which can deploy the OSGi bundles added to CARBON_HOME/osgi/dropins directory.
 *
 * @since 5.1.0
 */
public class OSGiBundleDeployer {
    private static final Logger logger = BootstrapLogger.getCarbonLogger(OSGiBundleDeployer.class.getName());

    public static void executeDropinsBundleDeployment(String[] args) throws IOException {
        if ((args != null) && (args.length == 1)) {
            String carbonHome = args[0];
            if (carbonHome != null) {
                execute(carbonHome);
            } else {
                throw new RuntimeException("Invalid CARBON_HOME specified");
            }
        }
    }

    private static void execute(String carbonHome) throws IOException {
        StringBuilder message = DeployerUtils.getProfileString(carbonHome);
        logger.log(Level.INFO, message.toString());

        String userChoice = new Scanner(System.in).nextLine();
        Optional<String> profileName = DeployerUtils.getUserChoice(carbonHome, Integer.parseInt(userChoice));
        if (profileName.isPresent()) {
            DeployerUtils.executeDropinsCapability(carbonHome, profileName.get());
        } else {
            logger.log(Level.INFO, "Invalid WSO2 Carbon profile name");
        }
    }
}
