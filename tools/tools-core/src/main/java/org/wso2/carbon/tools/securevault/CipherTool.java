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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.xml.bind.DatatypeConverter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * todo.
 *
 * @since 5.1.0
 */
public class CipherTool implements CarbonTool {   //todo rename

    private static Map<String, String> aliasPasswordMap = new HashMap<String, String>();
    ;

    /**
     * init the mode of operation of cipher tool using command line argument
     *
     * @param args command line arguments
     */
    private static void initialize(String[] args) throws IOException {
        String property;
        for (String arg : args) {
            if (arg.equals("-help")) {
                printHelp();
                System.exit(0);
            } else if (arg.substring(0, 2).equals("-D")) {
                property = arg.substring(2);
                if (property.equals(SecureVaultConstants.CONFIGURE)) {
                    Utils.setSystemProperties();
                    Cipher cipher = KeyStoreUtil.initializeCipher();
                    loadXpathValuesAndPasswordDetails();
                    System.setProperty(property, SecureVaultConstants.TRUE);  //todo usage of this property?
                    encryptCipherTextFile(cipher);
                    Utils.writeSecretYamlConfiguration();
                } else if (property.equals(SecureVaultConstants.CHANGE)) {
                    System.setProperty(property, SecureVaultConstants.TRUE);
                } else if (property.substring(0, 8).equals(SecureVaultConstants.CONSOLE_PASSWORD_PARAM)) {
                    System.setProperty(SecureVaultConstants.KEYSTORE_PASSWORD, property.substring(9));
                } else {
                    System.out.println("This option is not define!");
                    System.exit(-1);
                }
            }
        }
        //Utils.setSystemProperties();
    }

    /**
     * print the help on command line
     */
    private static void printHelp() {

        System.out.println("\n---------Cipher Tool Help---------\n");
        System.out.println("By default, CipherTool can be used for creating encrypted value for given plaint text\n");
        System.out.println("Options :\n");

        System.out.println("\t-Dconfigure\t\t This option would allow user to secure plain text passwords in carbon " +
                "configuration files. CipherTool will replace all the passwords listed in " +
                "cipher-text.properties file with encrypted values and modify related password elements " +
                "in the configuration files with secret alias names. Also secret-conf.properties file is " +
                "modified with the default configuration data");

        System.out.println("\t-Dchange\t\t This option would allow user to change the specific password which has " +
                "been secured\n");
        System.out.println("\t-Dpassword=<password>\t This option would allow user to provide the password as a " +
                "command line argument. NOTE: Providing the password in command line arguments list is " +
                "not recommended.\n");
    }


    /**
     * Encrypt plain text password defined in cipher-text.properties file. If not read password from command-line and
     * save to cipher-text.properties
     *
     * @param cipher cipher
     */
    private static void encryptCipherTextFile(Cipher cipher) {
        Properties properties = new Properties();
        for (Map.Entry<String, String> entry : aliasPasswordMap.entrySet()) {
            String value = entry.getValue();
            if (value != null && !value.equals("")) {
                if (value.startsWith("plainText=")) {
                    value = value.substring(("plainText=").length(), value.length());
                    System.out.println("value is " + value);
                    value = doEncryption(cipher, value);
                }
            } else {
                //value = getPasswordFromConsole(entry.getKey(), cipher);
            }
            properties.setProperty(entry.getKey(), "cipherText\u003d" + value);
        }

        writeToPropertyFile(properties, System.getProperty(SecureVaultConstants.CIPHER_TEXT_PROPERTY_FILE_PROPERTY));
    }

    /**
     * loads the secret alias, config filename and xpath
     */
    private static void loadXpathValuesAndPasswordDetails() {

        Properties cipherTextProperties =
                Utils.loadProperties(System.getProperty(SecureVaultConstants.CIPHER_TEXT_PROPERTY_FILE_PROPERTY));
        for (Object key : cipherTextProperties.keySet()) {
            String passwordAlias = (String) key;
            aliasPasswordMap.put(passwordAlias, cipherTextProperties.getProperty(passwordAlias));
        }
        System.out.println("alias map size : " + aliasPasswordMap.size());
        System.out.println("properties file : " + System.getProperty(SecureVaultConstants.CIPHER_TEXT_PROPERTY_FILE_PROPERTY));
    }

    /**
     * writes the properties into a file
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
            //throw new CipherToolException(msg + " Error : " + e.getMessage());
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                System.err.println("Error while closing output stream");
            }
        }
    }

    /**
     * encrypt the plain text password
     *
     * @param cipher       init cipher
     * @param plainTextPwd plain text password
     * @return encrypted password
     */
    private static String doEncryption(Cipher cipher, String plainTextPwd) {
        String encodedValue = null;
        try {
            byte[] encryptedPassword = cipher.doFinal(plainTextPwd.getBytes(Charset.forName(SecureVaultConstants.UTF8)));
            encodedValue = DatatypeConverter.printBase64Binary(encryptedPassword);
        } catch (BadPaddingException e) {
            //throw new CipherToolException("Error encrypting password ", e);
        } catch (IllegalBlockSizeException e) {
            //throw new CipherToolException("Error encrypting password ", e);
        }
        System.out.println("\nEncryption is done Successfully\n");
        return encodedValue;
    }

    @Override
    public void execute(String... toolArgs) {
        if ((toolArgs != null) && (toolArgs.length == 2)) {
            String carbonHome = toolArgs[1];
            System.setProperty(SecureVaultConstants.CARBON_HOME, carbonHome);
        }

        try {
            initialize(toolArgs);
        } catch (IOException e) {
            e.printStackTrace();     //todo
        }
    }
}
