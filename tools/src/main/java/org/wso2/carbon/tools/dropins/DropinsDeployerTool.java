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
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines a tool which can deploy the OSGi bundles added to CARBON_HOME/osgi/dropins directory
 * in WSO2 Carbon Server.
 *
 * @since 5.1.0
 */
public class DropinsDeployerTool implements CarbonTool {
    private static final Logger logger = Logger.getLogger(DropinsDeployerTool.class.getName());

    /**
     * Executes the WSO2 Carbon dropins deployer tool based on the specified arguments.
     *
     * @param toolArgs the {@link String} argument specifying the CARBON_HOME
     */
    @Override
    public void execute(String... toolArgs) {
        if ((toolArgs != null) && (toolArgs.length == 2)) {
            String carbonProfile = toolArgs[0];
            String carbonHome = toolArgs[1];
            if (carbonProfile.isEmpty()) {
                logger.log(Level.INFO, DropinsDeployerToolUtils.getHelpMessage());
                return;
            }

            try {
                DropinsDeployerToolUtils.executeTool(carbonHome, carbonProfile);
            } catch (CarbonToolException | IOException e) {
                logger.log(Level.SEVERE, "Error when executing the dropins deployer tool", e);
            }
        } else {
            logger.log(Level.INFO, DropinsDeployerToolUtils.getHelpMessage());
        }
    }
}
