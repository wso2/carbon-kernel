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

package org.wso2.carbon.kernel.securevault.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.securevault.SecretRepository;
import org.wso2.carbon.kernel.securevault.SecureVaultConstants;
import org.wso2.carbon.kernel.securevault.SecureVaultUtils;
import org.wso2.carbon.kernel.securevault.config.model.SecretRepositoryConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This abstract class implements {@link SecretRepository} and it provides basic implementations for
 * {@link SecretRepository#loadSecrets}, {@link SecretRepository#persistSecrets} and {@link SecretRepository#resolve}
 * methods. An extended class of this should provide concrete implementations for other abstract methods and register
 * that class as an OSGi service of interface {@link SecretRepository}.
 *
 * @since 5.2.0
 */
public abstract class AbstractSecretRepository implements SecretRepository {
    private static Logger logger = LoggerFactory.getLogger(AbstractSecretRepository.class);
    private final Map<String, char[]> secrets = new HashMap<>();

    @Override
    public void loadSecrets(SecretRepositoryConfiguration secretRepositoryConfiguration)
            throws SecureVaultException {
        logger.debug("Loading secrets to SecretRepository");
        Path secretPropertiesFilePath = Paths.get(SecureVaultUtils
                .getSecretPropertiesFileLocation(secretRepositoryConfiguration));

        String resolvedFileContent = SecureVaultUtils.resolveFileToString(secretPropertiesFilePath.toFile());
        Properties secretsProperties = new Properties();
        try {
            secretsProperties.load(new StringReader(resolvedFileContent));
        } catch (IOException e) {
            throw new SecureVaultException("Failed to load secrets.properties file");
        }

        for (Map.Entry<Object, Object> entry: secretsProperties.entrySet()) {
            String key = entry.getKey().toString().trim();
            String value = entry.getValue().toString().trim();

            char[] decryptedPassword;
            String[] tokens = value.split(SecureVaultConstants.SPACE);

            if (tokens.length != 2) {
                logger.error("Secret properties file contains an invalid entry at key : {}", key);
                continue;
            }

            String updatedTokenValue = SecureVaultUtils.substituteVariables(tokens[1]);
            if (SecureVaultConstants.CIPHER_TEXT.equals(tokens[0])) {
                byte[] base64Decoded = SecureVaultUtils.base64Decode(SecureVaultUtils.toBytes(updatedTokenValue));
                decryptedPassword = SecureVaultUtils.toChars(decrypt(base64Decoded));
            } else if (SecureVaultConstants.PLAIN_TEXT.equals(tokens[0])) {
                decryptedPassword = updatedTokenValue.toCharArray();
            } else {
                logger.error("Unknown prefix in secrets file");
                continue;
            }
            secrets.put(key, decryptedPassword);
        }
        logger.debug("Secret repository loaded with '{}' secrets", secrets.size());
    }

    @Override
    public void persistSecrets(SecretRepositoryConfiguration secretRepositoryConfiguration)
            throws SecureVaultException {
        logger.debug("Persisting secrets to SecretRepository");
        Path secretPropertiesFilePath = Paths.get(SecureVaultUtils
                .getSecretPropertiesFileLocation(secretRepositoryConfiguration));
        Properties secretsProperties = SecureVaultUtils.loadSecretFile(secretPropertiesFilePath);

        int count = 0;
        for (Map.Entry<Object, Object> entry: secretsProperties.entrySet()) {
            String key = entry.getKey().toString().trim();
            String value = entry.getValue().toString().trim();

            byte[] encryptedPassword;
            String[] tokens = value.split(SecureVaultConstants.SPACE);
            if (tokens.length != 2) {
                logger.error("Secret properties file contains an invalid entry at key : {}", key);
                continue;
            }

            if (SecureVaultConstants.PLAIN_TEXT.equals(tokens[0])) {
                encryptedPassword = SecureVaultUtils.base64Encode(encrypt(SecureVaultUtils.toBytes(tokens[1].trim())));
                secretsProperties.setProperty(key, SecureVaultConstants.CIPHER_TEXT + " "
                        + new String(SecureVaultUtils.toChars(encryptedPassword)));
                count++;
            }
        }

        SecureVaultUtils.updateSecretFile(secretPropertiesFilePath, secretsProperties);

        logger.debug("Secrets file updated with '{}' new encrypted secrets", count);
    }

    @Override
    public char[] resolve(String alias) {
        char[] secret = secrets.get(alias);
        if (secret != null && secret.length != 0) {
            return secret;
        }
        return new char[0];
    }
}
