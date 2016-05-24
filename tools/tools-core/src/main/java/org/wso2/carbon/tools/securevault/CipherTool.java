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

package org.wso2.carbon.tools.securevault;

import org.wso2.carbon.tools.CarbonTool;
import org.wso2.carbon.tools.exception.CarbonToolException;
import org.wso2.carbon.tools.securevault.exception.CipherToolException;
import org.wso2.carbon.tools.securevault.model.CarbonKeyStore;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.xml.bind.DatatypeConverter;

/**
 * This class defines a tool which can decrypt the secrets defined in the
 * CARBON-HOME/conf/security/secrets.properties file using a given keystore.
 *
 * @since 5.1.0
 */
public class CipherTool implements CarbonTool {
    private static final Logger logger = Logger.getLogger(CipherTool.class.getName());

    private static Map<String, String> aliasPasswordMap = new HashMap<>();

    /**
     * Executes the WSO2 Carbon cipher-tool based on the specified arguments.
     *
     * @param toolArgs the {@link String} argument specifying the user options and CARBON_HOME
     */
    @Override
    public void execute(String... toolArgs) {
        if ((toolArgs != null) && (toolArgs.length > 1)) {
            try{
                String carbonHome = toolArgs[1];
                if (carbonHome == null || carbonHome.isEmpty()) {
                    throw new CarbonToolException("Invalid Carbon home specified: " + carbonHome);
                }
                executeTool(toolArgs, carbonHome);
            } catch (CarbonToolException exception) {
                logger.log(Level.SEVERE, exception.getMessage());
            }

        }

    }

    /**
     * init the mode of operation of cipher tool using command line argument
     *
     * @param args command line arguments
     */
    private void executeTool(String[] args, String carbonHome) throws CarbonToolException {
        String userArgument;
        for (String arg : args) {
            if (arg.equals("-help")) {
                Utils.printHelp();
                return;
            } else if (arg.substring(0, 2).equals("-D")) {
                userArgument = arg.substring(2);
                if (userArgument.equals(SecureVaultConstants.CONFIGURE)) {
                    performEncryption(userArgument, carbonHome);
                } else {
                    logger.log(Level.INFO, "This option is not define!");
                    return;
                }
            }
        }
    }

    private void performEncryption(String userArgument, String carbonHome) throws CarbonToolException {
        Optional<CarbonKeyStore> keystore = KeyStoreUtil.loadKeystoreConfiguration(carbonHome);
        Cipher cipher = KeyStoreUtil.getCipherInstance(keystore);
        if (userArgument != null &&
                userArgument.equals(SecureVaultConstants.CONFIGURE)) {
            loadSecretsAndAliasFromFile(carbonHome);
            encryptSecrets(cipher, carbonHome);
            Utils.writeSecretYamlConfiguration(keystore.get(), carbonHome);
        } else {
            encryptValues(cipher);
        }
    }

    /**
     * Encrypt plain text password defined in secrets.properties file.
     *
     * @param cipher cipher
     */
    private void encryptSecrets(Cipher cipher, String carbonHome) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : aliasPasswordMap.entrySet()) {
            String value = entry.getValue();
            if (value != null && !value.equals("")) {
                if (value.startsWith("plainText=")) {
                    value = value.substring(("plainText=").length(), value.length());
                    value = doEncryption(cipher, value);
                }
            } else {
                value = getPasswordFromConsole(entry.getKey(), cipher);
            }
            properties.setProperty(entry.getKey(), "cipherText\u003d" + value);
        }

        Utils.writeToPropertyFile(properties, Utils.getSecretsFileLocation(carbonHome).toString());
    }

    /**
     * loads the secret alias, and secrets from secrets.properties file.
     */
    private void loadSecretsAndAliasFromFile(String carbonHome) {

        Properties secretProperties =
                Utils.loadProperties(Utils.getSecretsFileLocation(carbonHome).toString());
        for (Object key : secretProperties.keySet()) {
            String passwordAlias = (String) key;
            aliasPasswordMap.put(passwordAlias, secretProperties.getProperty(passwordAlias));
        }
    }

    /**
     * encrypt the plain text password
     *
     * @param cipher       init cipher
     * @param plainTextPwd plain text password
     * @return encrypted password
     */
    private String doEncryption(Cipher cipher, String plainTextPwd) {
        String encodedValue;
        try {
            byte[] encryptedPassword = cipher.doFinal(
                    plainTextPwd.getBytes(Charset.forName(SecureVaultConstants.UTF8)));
            encodedValue = DatatypeConverter.printBase64Binary(encryptedPassword);
        } catch (BadPaddingException e) {
            //throwing a runtime exception as this exception may occur due to a invalid user inputs.
            throw new CipherToolException("Error encrypting password ", e);
        } catch (IllegalBlockSizeException e) {
            //throwing a runtime exception as this exception may occur due to a invalid user inputs.
            throw new CipherToolException("Error encrypting password ", e);
        }
        logger.log(Level.INFO, "\nEncryption is done Successfully\n");
        return encodedValue;
    }

    /**
     * returns the encrypted value entered via the Console for the given Secret Alias
     *
     * @param key    key
     * @param cipher cipher
     * @return encrypted value
     */
    private String getPasswordFromConsole(String key, Cipher cipher) {
        String firstPassword = Utils.getValueFromConsole("Enter Password of Secret Alias - '" + key + "' : ", true);
        String secondPassword = Utils.getValueFromConsole("Please Enter Password Again : ", true);
        if (!firstPassword.isEmpty() && firstPassword.equals(secondPassword)) {
            String encryptedValue = doEncryption(cipher, firstPassword);
            aliasPasswordMap.put(key, encryptedValue);
            return encryptedValue;
        } else {
            throw new CipherToolException("Error : Password does not match");
        }
    }

    /**
     * encrypt text retrieved from Console.
     *
     * @param cipher cipher
     */
    private void encryptValues(Cipher cipher) {
        String firstPassword = Utils.getValueFromConsole("Enter Plain Text Value : ", true);
        String secondPassword = Utils.getValueFromConsole("Please Enter Value Again : ", true);

        if (!firstPassword.isEmpty() && firstPassword.equals(secondPassword)) {
            String encryptedText = doEncryption(cipher, firstPassword);
            logger.log(Level.INFO, "\nEncrypted value is : \n" + encryptedText + "\n");
        } else {
            throw new CipherToolException("Error : Password does not match");
        }
    }
}
