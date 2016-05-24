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

import org.wso2.carbon.tools.securevault.exception.CipherToolException;
import org.wso2.carbon.tools.securevault.model.CarbonKeyStore;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Java class which defines utility functions used within the cipher-tool.
 *
 * @since 5.1.0
 */
public class Utils {

    private static final Logger logger = Logger.getLogger(Utils.class.getName());

    /**
     * read values from a given property file.
     *
     * @param filePath file path
     * @return Properties properties
     */
    public static Properties loadProperties(String filePath) {
        Properties properties = new Properties();
        File file = new File(filePath);
        if (!file.exists()) {
            return properties;
        }

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            properties.load(inputStream);
        } catch (IOException e) {
            String msg = "Error loading properties from a file at :" + filePath;
            throw new RuntimeException(msg + " Error : " + e.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error while closing input stream");
                }
            }
        }
        return properties;
    }

    /**
     * Write to the Secret-vault.yml file.
     */
    public static void writeSecretYamlConfiguration(CarbonKeyStore keyStore, String carbonHome) {

        String secretsYamlFile = carbonHome + File.separator + SecureVaultConstants.CONF_DIR +
                File.separator + SecureVaultConstants.SECURITY_DIR +
                File.separator + SecureVaultConstants.SECRET_YAML_FILE;

        Map<String, Object> data = new HashMap<>();

        String keyStoreFile = keyStore.getLocation();
        String keyType = keyStore.getType();
        String aliasName = keyStore.getKeyAlias();

        data.put(SecureVaultConstants.SecureVault.SECRET_REPOSITORIES, "file");
        data.put(SecureVaultConstants.SecureVault.SECRET_FILE_PROVIDER,
                SecureVaultConstants.SecureVault.SECRET_FILE_BASE_PROVIDER_CLASS);
        data.put(SecureVaultConstants.SecureVault.SECRET_FILE_LOCATION,
                getSecretsFileLocation(carbonHome).toString());
        data.put(SecureVaultConstants.SecureVault.KEYSTORE_LOCATION, keyStoreFile);
        data.put(SecureVaultConstants.SecureVault.KEYSTORE_TYPE, keyType);
        data.put(SecureVaultConstants.SecureVault.KEYSTORE_ALIAS, aliasName);
        data.put(SecureVaultConstants.SecureVault.KEYSTORE_STORE_PASSWORD, keyStore.getPassword());

        Yaml yaml = new Yaml();
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(secretsYamlFile)), "UTF-8"));
            yaml.dump(data, writer);
        } catch (IOException e) {
            throw new CipherToolException("Error writing to file " + SecureVaultConstants.SECRET_PROPERTY_FILE, e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                throw new CipherToolException("Error closing the writer  ", e);
            }
        }
        logger.log(Level.INFO, "\nSecret Configurations are written to the property file successfully\n");
    }

    /**
     * Retrieve value from command-line
     */
    public static String getValueFromConsole(String msg, boolean isPassword) {
        Console console = System.console();
        if (console != null) {
            if (isPassword) {
                char[] password;
                if ((password = console.readPassword("[%s]", msg)) != null) {
                    return String.valueOf(password);
                }
            } else {
                String value;
                if ((value = console.readLine("[%s]", msg)) != null) {
                    return value;
                }
            }
        }
        throw new RuntimeException("String cannot be null");  //todo
    }

    public static Path getSecretsFileLocation(String carbonHome) {
        return Paths.get(carbonHome, SecureVaultConstants.CONF_DIR,
                SecureVaultConstants.SECURITY_DIR, SecureVaultConstants.SECRETS_FILE);
    }

    /**
     * print the help on command line
     */
    public static void printHelp() {

        logger.log(Level.INFO, "\n---------Cipher Tool Help---------\n");
        logger.log(Level.INFO, "By default, " +
                "CipherTool can be used for creating encrypted value for given plaint text\n");
        logger.log(Level.INFO, "Options :\n");

        logger.log(Level.INFO, "\t-Dconfigure\t\t " +
                "This option would allow user to secure plain text passwords in carbon " +
                "configuration files. CipherTool will replace all the passwords listed in " +
                "secrets.properties file with encrypted values. Also secret-vault.yml file is " +
                "modified with the default configuration data");

        logger.log(Level.INFO, "\t-Dchange\t\t " +
                "This option would allow user to change the specific password which has " +
                "been secured\n");
        logger.log(Level.INFO, "\t-Dpassword=<password>\t " +
                "This option would allow user to provide the password as a " +
                "command line argument. NOTE: Providing the password in command line arguments list is " +
                "not recommended.\n");
    }

    /**
     * writes the secrets into a file.
     *
     * @param properties properties
     * @param filePath   filepath
     */
    public static void writeToPropertyFile(Properties properties, String filePath) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(filePath);
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            String msg = "Error loading properties from a file at : " + filePath;
            throw new CipherToolException(msg + " Error : " + e.getMessage());
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while closing output stream");
            }
        }
    }
}
