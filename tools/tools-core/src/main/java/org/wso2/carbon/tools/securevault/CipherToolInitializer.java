/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.tools.securevault;

import org.wso2.carbon.tools.CarbonTool;
import org.wso2.carbon.tools.exception.CarbonToolException;
import org.wso2.carbon.tools.securevault.utils.CommandLineParser;
import org.wso2.carbon.tools.securevault.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Java class which defines the CipherToolInitializer as a CarbonTool.
 *
 * @since 5.2.0
 */
public class CipherToolInitializer implements CarbonTool {
    private static final Logger logger = Logger.getLogger(CipherToolInitializer.class.getName());


    @Override
    public void execute(String... toolArgs) {
        CommandLineParser commandLineParser;
        try {
            commandLineParser = Utils.createCommandLineParser(toolArgs);
        } catch (CarbonToolException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            printHelpMessage();
            throw new RuntimeException("Unable to run CipherTool", e);
        }

        URLClassLoader urlClassLoader = Utils.getCustomClassLoader(commandLineParser.getCustomLibPath());

        try {
            Object objCipherTool = Utils.createCipherTool(urlClassLoader);
            processCommand(commandLineParser.getCommandName().orElse(""),
                    commandLineParser.getCommandParam().orElse(""), objCipherTool);
        } catch (CarbonToolException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException("Unable to run CipherTool", e);
        }
    }

    private void processCommand(String command, String parameter, Object objCipherTool) throws CarbonToolException {
        Method method;
        try {
            switch (command) {
                case Constants.ENCRYPT_TEXT_COMMAND:
                    method = objCipherTool.getClass().getMethod(Constants.ENCRYPT_TEXT_METHOD, String.class);
                    method.invoke(objCipherTool, parameter);
                    break;
                case Constants.DECRYPT_TEXT_COMMAND:
                    method = objCipherTool.getClass().getMethod(Constants.DECRYPT_TEXT_METHOD, String.class);
                    method.invoke(objCipherTool, parameter);
                    break;
                default:
                    method = objCipherTool.getClass().getMethod(Constants.ENCRYPT_SECRETS_METHOD);
                    method.invoke(objCipherTool);
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new CarbonToolException("Failed to execute Cipher Tool command", e);
        }
    }

    /**
     * Prints a help message for the secure vault tool usage.
     */
    private void printHelpMessage() {
        logger.info("\nIncorrect usage of the cipher tool.\n\n"
                + "Instructions: sh ciphertool.sh [<command> <parameter>]\n\n"
                + "If no commandline options are provided, CipherTool will encrypt the secrets given in the\n"
                + "[CARBON_HOME]/conf/security/secrets.properties file. This is the default behaviour.\n"
                + "CipherTool will read the configurations from secure-vault.yaml file. Hence it is mandatory\n"
                + "to update the [CARBON_HOME]/conf/secure-vault.yaml file before running CipherTool\n\n"
                + "Usages:\n\n"
                + "1. With no option specified, cipher tool will encrypt the secrets given in the\n"
                + "   [CARBON_HOME]conf/security/secrets.properties file.\n\n"
                + "2. -encryptText : this option will first encrypt a given text and then prints the base64 encoded\n"
                + "   string of the encoded cipher text in the console.\n"
                + "     Eg: ciphertool.sh -encryptText Abc@123\n\n"
                + "3. -decryptText : this option accepts base64 encoded cipher text and prints the decoded plain text\n"
                + "   in the console.\n"
                + "     Eg: ciphertool.sh -decryptText XxXxXx\n"
        );
    }
}
