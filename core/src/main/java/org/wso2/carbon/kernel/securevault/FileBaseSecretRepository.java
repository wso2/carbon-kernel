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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by nipuni on 6/6/16.   //todo
 */
public class FileBaseSecretRepository implements SecretRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileBaseSecretRepository.class);
    private static final String ALGORITHM = "RSA";
    private String secretRepoLocation;

    /*Map of encrypted values keyed by alias for property name */
    private final Map<String, Object> secretsMap = new HashMap<>();

    /**
     * Loads the properties from a given property file path
     *
     * @param filePath Path of the property file
     * @return Properties loaded from given file        //todo move to a utils class
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
            logger.error(msg, e);
//            throw new SecureVaultException(msg, e);     //todo
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

    @Override
    public void init(Map<String, Object> keystoreConfiguration) {

        Properties secrets = new Properties();
        if (!secretRepoLocation.isEmpty()) {
            secrets = loadProperties(secretRepoLocation);
            if (secrets.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cipher texts cannot be loaded form : " + secretRepoLocation);
                }
                return;
            }
        }

        secrets.entrySet().stream()
                .map(entry -> new Secret(entry.getKey().toString(), entry.getValue().toString()))
                .forEach(secret -> secretsMap.put(secret.getAlias(), secret));
//        StringBuffer buffer = new StringBuffer();
//        buffer.append(DOT);
//        buffer.append(KEY_STORE);

        //Load keyStore
//        String keyStore = MiscellaneousUtil.getProperty(properties,
//                buffer.toString(), null);
//        KeyStoreWrapper keyStoreWrapper;
//        if (TRUSTED.equals(keyStore)) {
//            keyStoreWrapper = trust;
//
//        } else {
//            keyStoreWrapper = identity;
//        }
//
//        //Creates a cipherInformation
//
//        CipherInformation cipherInformation = new CipherInformation();
//        cipherInformation.setAlgorithm(algorithm);
//        cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
//        cipherInformation.setInType(EncodingType.BASE64); //TODO
//        DecryptionProvider baseCipher =
//                CipherFactory.createCipher(cipherInformation, keyStoreWrapper);
//
//        for (Object alias : cipherProperties.keySet()) {
//            //decrypt the encrypted text
//            String key = String.valueOf(alias);
//            String encryptedText = cipherProperties.getProperty(key);
//            encryptedData.put(key, encryptedText);
//            if (encryptedText == null || "".equals(encryptedText.trim())) {
//                if (log.isDebugEnabled()) {
//                    log.debug("There is no secret for the alias : " + alias);
//                }
//                continue;
//            }
//
//            String decryptedText = new String(baseCipher.decrypt(encryptedText.trim().getBytes()));
//            secrets.put(key, decryptedText);
//        }
//        initialize = true;
    }

    @Override
    public String getSecret(String alias) {
        return null;
    }

    @Override
    public String getLocation() {
        return this.secretRepoLocation;
    }

    @Override
    public void setLocation(String location) {
        this.secretRepoLocation = location;
    }

    @Override
    public void setProvider(String provider) {

    }
}
