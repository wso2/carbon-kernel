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

import org.wso2.carbon.tools.exception.CarbonToolException;
import org.wso2.carbon.tools.securevault.exception.CipherToolException;
import org.wso2.carbon.tools.securevault.model.KeyStoreInformation;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

/**
 * A Java class which defines utility functions used within the cipher-tool.
 *
 * @since 5.1.0
 */
public class KeyStoreUtil {

    private static final Logger logger = Logger.getLogger(KeyStoreUtil.class.getName());

    /**
     * Initializes the Cipher.
     *
     * @return cipher cipher
     */
    public static Cipher getCipherInstance(Optional<KeyStoreInformation> keystoreOptional) throws CarbonToolException {
        Cipher cipher = null;
        if (keystoreOptional.isPresent()) {
            KeyStoreInformation keyStoreInformation = keystoreOptional.get();
            String keyStoreFile = keyStoreInformation.getLocation();
            String keyType = keyStoreInformation.getType();
            String keyAlias = keyStoreInformation.getKeyAlias();
            String password = keyStoreInformation.getPassword();
            if (password == null || password.isEmpty()) {
                password = Utils.getValueFromConsole(
                        "Please Enter Primary KeyStore Password of Carbon Server : ", true);
            }
            if (password == null) {
                throw new CipherToolException("KeyStore password can not be null");
            }
            KeyStore primaryKeyStore = getKeyStore(keyStoreFile, password, keyType);
            try {
                Certificate certs = primaryKeyStore.getCertificate(keyAlias);
                cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, certs);
            } catch (KeyStoreException e) {
                throw new CarbonToolException("Error retrieving certificate from keystore ");
            } catch (NoSuchAlgorithmException e) {
                throw new CarbonToolException("requested cryptographic algorithm is not available ");
            } catch (NoSuchPaddingException e) {
                throw new CarbonToolException("Error initializing Cipher ");
            } catch (InvalidKeyException e) {
                throw new CarbonToolException("Error due to invalid key ");
            }

            logger.log(Level.INFO, "\nPrimary KeyStore of Carbon Server is initialized Successfully\n");
        }
        if (cipher == null) {
            throw new CarbonToolException("Could not initialize cipher");
        }
        return cipher;
    }

    /**
     * create a CarbonKeystore from keystore configuration.
     */
    @SuppressWarnings("unchecked")
    public static Optional<KeyStoreInformation> loadKeystoreConfiguration(String carbonHome)
            throws CarbonToolException {

        KeyStoreInformation keyStoreInformation = null;

        Path serverConfigurationFile =
                Paths.get(carbonHome, SecureVaultConstants.CONF_DIR, SecureVaultConstants.CARBON_CONFIG_FILE);

        if (Files.exists(serverConfigurationFile)) {
            //WSO2 Environment

            Yaml yaml = new Yaml();
            InputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(serverConfigurationFile.toString());
                Map<String, Object> config = (Map<String, Object>) yaml.load(fileInputStream);
                Map<String, Object> keyStoreConfig = (Map<String, Object>) config.get("KeyStore");
                if (keyStoreConfig.isEmpty()) {
                    throw new CipherToolException("No KeyStore configuration was added to the server configuration");
                }
                String keyStoreLocation = (String) keyStoreConfig.get("Location");
                keyStoreLocation = carbonHome + keyStoreLocation.substring((keyStoreLocation.indexOf('}')) + 1);
                String keyType = (String) keyStoreConfig.get("Type");
                String keyAlias = (String) keyStoreConfig.get("KeyAlias");

                keyStoreInformation = new KeyStoreInformation(keyStoreLocation, keyType,
                        (String) keyStoreConfig.get("Password"), keyAlias);

            } catch (FileNotFoundException e) {
                throw new CipherToolException("No configuration file found at : " +
                        serverConfigurationFile.toString(), e);
            } finally {
                try {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                } catch (IOException e) {
                    throw new CarbonToolException("Error while closing inputstream for : " +
                            serverConfigurationFile.toString());
                }
            }

        } else {
            //todo handling non-wso2 environments
        }

        if (keyStoreInformation == null || keyStoreInformation.getLocation() == null ||
                keyStoreInformation.getLocation().trim().isEmpty()) {
            throw new CipherToolException("KeyStore file path cannot be empty");
        }
        if (keyStoreInformation.getKeyAlias() == null ||
                keyStoreInformation.getKeyAlias().trim().isEmpty()) {
            throw new CipherToolException("Key alias cannot be empty");
        }

        return Optional.of(keyStoreInformation);
    }

    private static KeyStore getKeyStore(String location, String storePassword,
                                        String storeType) throws CarbonToolException {
        BufferedInputStream bufferedInputStream = null;
        try {
            bufferedInputStream = new BufferedInputStream(new FileInputStream(location));
            KeyStore keyStore = KeyStore.getInstance(storeType);
            keyStore.load(bufferedInputStream, storePassword.toCharArray());
            return keyStore;
        } catch (KeyStoreException e) {
            throw new CarbonToolException("Error loading keyStore from ' " + location + " ' ");
        } catch (IOException e) {
            throw new CarbonToolException("Error loading keyStore from ' " + location + " ' ");
        } catch (NoSuchAlgorithmException e) {
            throw new CarbonToolException("The requested cryptographic algorithm is not initialized " +
                    "with the keystore from : ' " + location + " ' ");
        } catch (CertificateException e) {
            throw new CarbonToolException("Error creating certificate with keystore from ' " + location + " ' ");
        } finally {
            if (bufferedInputStream != null) {
                try {
                    bufferedInputStream.close();
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Error while closing input stream");
                }
            }
        }
    }
}
