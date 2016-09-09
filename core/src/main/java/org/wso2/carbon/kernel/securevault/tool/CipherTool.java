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

package org.wso2.carbon.kernel.securevault.tool;

import org.wso2.carbon.kernel.internal.securevault.SecureVaultConfigurationProvider;
import org.wso2.carbon.kernel.securevault.MasterKeyReader;
import org.wso2.carbon.kernel.securevault.SecretRepository;
import org.wso2.carbon.kernel.securevault.SecureVaultUtils;
import org.wso2.carbon.kernel.securevault.config.model.SecureVaultConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

import java.net.URLClassLoader;
import java.util.logging.Logger;

/**
 * The Java class which defines the CipherTool.
 *
 * @since 5.2.0
 */
public class CipherTool {
    private static final Logger logger = Logger.getLogger(CipherTool.class.getName());
    private SecureVaultConfiguration secureVaultConfiguration;
    private SecretRepository secretRepository;

    public void init(URLClassLoader urlClassLoader) throws SecureVaultException {
        secureVaultConfiguration = SecureVaultConfigurationProvider.getConfiguration();

        String secretRepositoryType = secureVaultConfiguration.getSecretRepositoryConfig().getType()
                .orElseThrow(() -> new SecureVaultException("Secret repository type is mandatory"));
        String masterKeyReaderType = secureVaultConfiguration.getMasterKeyReaderConfig().getType()
                .orElseThrow(() -> new SecureVaultException("Master key reader type is mandatory"));

        MasterKeyReader masterKeyReader;
        try {
            masterKeyReader = (MasterKeyReader) urlClassLoader.loadClass(masterKeyReaderType).newInstance();
            secretRepository = (SecretRepository) urlClassLoader.loadClass(secretRepositoryType).newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new SecureVaultException("Failed to instantiate implementation classes.", e);
        }

        masterKeyReader.init(secureVaultConfiguration.getMasterKeyReaderConfig());
        secretRepository.init(secureVaultConfiguration.getSecretRepositoryConfig(), masterKeyReader);
    }

    public void encryptSecrets() throws SecureVaultException {
        secretRepository.persistSecrets(secureVaultConfiguration.getSecretRepositoryConfig());
    }

    public String encryptText(String plainText) throws SecureVaultException {
        byte[] encryptedPassword = secretRepository.encrypt(SecureVaultUtils.toBytes(plainText.trim()));
        String base64Encoded = new String(SecureVaultUtils.toChars(SecureVaultUtils.base64Encode(encryptedPassword)));
        logger.info(base64Encoded);
        return base64Encoded;
    }

    public String decryptText(String cipherText) throws SecureVaultException {
        byte[] decryptedPassword = secretRepository.decrypt(SecureVaultUtils
                .base64Decode(SecureVaultUtils.toBytes(cipherText)));
        String plainText = new String(SecureVaultUtils.toChars(decryptedPassword));
        logger.info(plainText);
        return plainText;
    }
}
