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

import org.wso2.carbon.tools.CarbonTool;
import org.wso2.carbon.tools.exceptions.CarbonToolException;

import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines a tool which can deploy the OSGi bundles added to CARBON_HOME/osgi/dropins directory
 * in WSO2 Carbon Server.
 *
 * @since 5.1.0
 */
public class DropinsDeployer implements CarbonTool {
    private static final Logger logger = Logger.getLogger(DropinsDeployer.class.getName());

    /**
     * Executes the WSO2 Carbon dropins deployer tool based on the specified arguments.
     *
     * @param toolArgs the {@link String} argument specifying the CARBON_HOME
     * @throws CarbonToolException if an error occurs when executing the tool
     */
    @Override
    public void execute(String... toolArgs) throws CarbonToolException {
        if ((toolArgs != null) && (toolArgs.length == 1)) {
            String carbonHome = toolArgs[0];
            if (carbonHome != null) {
                executeTool(carbonHome);
            } else {
                throw new CarbonToolException("The carbon.home cannot be null");
            }
        }
    }

    /**
     * Executes the WSO2 Carbon dropins deployer tool.
     *
     * @param carbonHome the {@link String} value of carbon.home
     * @throws CarbonToolException if an error occurred when executing the tool
     */
    private static void executeTool(String carbonHome) throws CarbonToolException {
        try {
            StringBuilder message = DropinsDeployerUtils.getProfileString(carbonHome);
            logger.log(Level.INFO, message.toString());

            String userChoice = new Scanner(System.in).nextLine();
            Optional<String> profileName = DropinsDeployerUtils.getUserChoice(carbonHome, Integer.parseInt(userChoice));
            if (profileName.isPresent()) {
                DropinsDeployerUtils.executeDropinsCapability(carbonHome, profileName.get());
            } else {
                throw new CarbonToolException("Invalid WSO2 Carbon Profile name specified");
            }
        } catch (IOException e) {
            throw new CarbonToolException("An I/O error occurred when executing the dropins deployer tool", e);
        }
    }
}
