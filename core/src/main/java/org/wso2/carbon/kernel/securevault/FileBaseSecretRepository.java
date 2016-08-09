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

import java.nio.charset.Charset;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Holds secrets on a file.
 *
 * @since 5.2.0
 */
public class FileBaseSecretRepository implements SecretRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileBaseSecretRepository.class);
    /*Map of encrypted values keyed by alias for property name */
    private final Map<String, Object> encryptDataMap = new HashMap<>();
    /*Map of decrypted values keyed by alias for property name */
    private final Map<String, String> secretsMap = new HashMap<String, String>();

    private String secretRepoLocation;

    @Override
    public void init(KeyStoreInformation keyStoreInformation) {

        final String utf8 = "UTF-8";

        Properties secrets = new Properties();
        if (!secretRepoLocation.isEmpty()) {
            secrets = Utils.loadProperties(secretRepoLocation);
            if (secrets.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Cipher texts cannot be loaded form : " + secretRepoLocation);
                }
                return;
            }
        }

        secrets.entrySet().stream()
                .map(entry -> new Secret(entry.getKey().toString(), entry.getValue().toString()))
                .forEach(secret -> encryptDataMap.put(secret.getAlias(), secret.getToken()));


        Key key = Utils.getKey(CipherOperationMode.DECRYPT, keyStoreInformation);
        CipherInformation cipherInformation = new CipherInformation();
        cipherInformation.setCipherOperationMode(CipherOperationMode.DECRYPT);
        BaseCipher cipher = CipherFactory.createCipher(cipherInformation, key);

        for (Object alias : secrets.keySet()) {
            //decrypt the encrypted text
            String secretAlias = String.valueOf(alias);
            String encryptedText = secrets.getProperty(secretAlias);
            if (encryptedText == null || "".equals(encryptedText.trim())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("There is no secret for the alias : " + alias);
                }
                continue;
            }

            //secrets.properties file has secrets as "plainText<space>encryptedValue. Hence need to split and read.
            encryptedText = encryptedText.split(" ")[1];
            String decryptedText = new String(
                    cipher.decrypt(encryptedText.trim().getBytes(Charset.forName(utf8))),
                    Charset.forName(utf8));
            secretsMap.put(secretAlias, decryptedText);
        }
    }

    @Override
    public String getSecret(Optional<String> alias) {
        if (!alias.isPresent()) {
            Utils.handleException("The alias can not be empty.");
        }

        if (secretsMap.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("There is no secret found for alias '" + alias + "' returning alias itself");
            }
            return alias.get();
        }

        String secret = secretsMap.get(alias.get());
        if (secret == null || "".equals(secret)) {
            if (logger.isDebugEnabled()) {
                logger.debug("There is no secret found for alias '" + alias + "' returning alias itself");
            }
            return alias.get();
        }
        return secret;
    }

    public boolean isTokenEncrypted(String alias) {
        if (encryptDataMap.containsKey(alias)) {
            Optional<String> encryptedText = Optional.of((String) encryptDataMap.get(alias));
            return encryptedText.isPresent() && encryptedText.get().startsWith("cipherText");
        }
        return false;
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
