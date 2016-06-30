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

package org.wso2.carbon.kernel.securevault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Properties;

/**
 * Secure vault utils.
 *
 * @since 5.2.0
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    /**
     * Loads the properties from a given property file path
     *
     * @param filePath Path of the property file
     * @return Properties loaded from given file
     */
    public static Properties loadProperties(String filePath) {

        Properties properties = new Properties();
        String carbonHome = System.getProperty("carbon.home");
        filePath = carbonHome + File.separator + filePath;
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            return properties;
        }

        InputStream in = null;
        try {
            in = new FileInputStream(configFile);
            properties.load(in);
        } catch (IOException e) {
            String msg = "Error loading properties from a file at :" + filePath;
            handleException(msg);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {
                    logger.error("Error while closing input stream");
                }
            }
        }
        return properties;
    }

    public static Key getKey(CipherOperationMode operationMode, KeyStoreInformation keyStoreInformation) {
        KeyStore keystore = keyStoreInformation.getKeyStore();
        if (operationMode == CipherOperationMode.ENCRYPT) {
            return getPublicKey(keyStoreInformation, keystore);
        } else {
            return getPrivateKey(keyStoreInformation, keystore);
        }
    }

    /**
     * Returns the public key based on initialization data
     *
     * @return PublicKey if there is a one , otherwise null
     */
    private static PublicKey getPublicKey(KeyStoreInformation keyStoreInformation, KeyStore keystore) {
        Key key = getPublicKeyFromCertificate(keyStoreInformation.getAlias(), keystore);
        if (key instanceof PublicKey) {
            return (PublicKey) key;
        }
        return null;
    }

    /**
     * Returns the key based on certificate of the owner to who given alias belong
     *
     * @param alias The alias of the certificate in the specified keyStore
     * @return Key , if there is a one , otherwise null
     */
    private static Key getPublicKeyFromCertificate(String alias, KeyStore keyStore) {
        try {
            Certificate certificate = keyStore.getCertificate(alias);
            if (certificate != null) {
                return certificate.getPublicKey();
            }
        } catch (KeyStoreException e) {
            logger.error("Error loading key for alias : " + alias, e);
        }
        return null;
    }

    /**
     * Returns the private key based on initialization data
     *
     * @return PrivateKey if there is a one , otherwise null
     */
    private static PrivateKey getPrivateKey(KeyStoreInformation keyStoreInformation, KeyStore keyStore) {
        String keyPassword = "wso2carbon";   //todo call secret callbackhandler
        Key key = getKey(keyStoreInformation.getAlias(), keyPassword, keyStore);
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        return null;
    }

    /**
     * Returns the key based on provided alias and key password
     *
     * @param alias       The alias of the certificate in the specified keyStore
     * @param keyPassword Password for key within the KeyStrore
     * @return Key if there is a one , otherwise null
     */
    private static Key getKey(String alias, String keyPassword, KeyStore keyStore) {

        if (alias == null || "".equals(alias)) {
            logger.error("The alias need to provided to get certificate");
        }
        if (keyPassword != null) {
            try {
                return keyStore.getKey(alias, keyPassword.toCharArray());
            } catch (KeyStoreException e) {
                handleException("Error loading key for alias : " + alias, e);
            } catch (NoSuchAlgorithmException e) {
                handleException("Error loading key for alias : " + alias, e);
            } catch (UnrecoverableKeyException e) {
                handleException("Error loading key for alias : " + alias, e);
            }
        }
        return null;
    }

    public static void handleException(String msg) {
        logger.error(msg);
        throw new SecureVaultException(msg);
    }

    public static void handleException(String msg, Exception e) {
        logger.error(msg, e);
        throw new SecureVaultException(msg, e);
    }
}
