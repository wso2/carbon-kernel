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
package org.wso2.carbon.tools;

import org.wso2.carbon.tools.converter.BundleGeneratorTool;
import org.wso2.carbon.tools.exception.CarbonToolException;
import org.wso2.carbon.tools.osgilib.OSGiLibDeployerTool;
import org.wso2.carbon.tools.spi.ICFRegisterTool;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This Java class defines the WSO2 Carbon-kernel tool executor, which manages the execution of numerous
 * optional tools available along with the Carbon-kernel based on the user's choice of execution.
 * <p>
 * The user will be able to execute the desired tool using the relevant .sh/.bat file available in the
 * distribution. The ability to identify the appropriate tool to execute and performing the execution is
 * done by the {@code CarbonToolExecutor}.
 *
 * @since 5.1.0
 */
public class CarbonToolExecutor {
    private static final Logger logger = Logger.getLogger(CarbonToolExecutor.class.getName());

    /**
     * Application executor for the WSO2 Carbon Tool executor.
     *
     * @param args the arguments to be used within the tool
     */
    public static void main(String[] args) {
        String toolIdentifier = System.getProperty(Constants.CARBON_TOOL_SYSTEM_PROPERTY);
        Optional.ofNullable(toolIdentifier)
                .ifPresent(identifier -> {
                    try {
                        executeTool(identifier, args);
                    } catch (CarbonToolException e) {
                        logger.log(Level.SEVERE, "An error has occurred when executing the Carbon tool", e);
                    }
                });
    }

    /**
     * Executes the appropriate Carbon tool along with the arguments specified.
     *
     * @param toolIdentifier the identifier of the tool to be executed
     * @param toolArgs       the arguments needed for the functioning of the tool
     * @throws CarbonToolException if the tool cannot be identified for execution
     */
    private static void executeTool(String toolIdentifier, String... toolArgs) throws CarbonToolException {
        if (toolIdentifier == null) {
            throw new CarbonToolException("The Carbon tool identifier cannot be null");
        }

        CarbonTool carbonTool;
        switch (toolIdentifier) {
        case "jar-to-bundle-converter":
            carbonTool = new BundleGeneratorTool();
            break;
        case "osgi-lib-deployer":
            carbonTool = new OSGiLibDeployerTool();
            break;
        case "icf-register":
            carbonTool = new ICFRegisterTool();
            break;
        default:
            carbonTool = null;
        }

        if (carbonTool != null) {
            carbonTool.execute(toolArgs);
        }
    }
}
