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

package org.wso2.carbon.kernel.securevault;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.securevault.config.model.SecureVaultConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.wso2.carbon.kernel.securevault.internal.SecureVaultConfigurationProvider;
import org.wso2.carbon.kernel.securevault.internal.SecureVaultDataHolder;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Responsible for initializing secure vault.
 *
 * @since 5.2.0
 */
public class SecureVaultInitializer {

    public boolean initialized = false;
    private static final Logger logger = LoggerFactory.getLogger(SecureVaultInitializer.class);
    private static SecureVaultInitializer secureVaultInitializer = SecureVaultInitializer.getInstance();

    private Optional<SecureVaultConfiguration> optSecureVaultConfiguration;

    private static String secretRepositoryType;
    private static String masterKeyReaderType;

    private ServiceLoader<SecretRepository> secretRepositoryLoader;
    private ServiceLoader<MasterKeyReader> masterKeyReaderLoader;

    private SecureVaultInitializer() {
        if (SecureVaultUtils.getCarbonHome().isPresent()) {
            initFromSecureVaultYAML();
        } else {
            initSPI();
        }
    }

    /**
     * Initialise secret repository and master key reader types from values which is read from secure-vault yaml.
     */
    private void initFromSecureVaultYAML() {
        try {
            optSecureVaultConfiguration = Optional.of(SecureVaultConfigurationProvider.getConfiguration());
            optSecureVaultConfiguration.ifPresent(secureVaultConfiguration -> {
                secretRepositoryType = secureVaultConfiguration.getSecretRepositoryConfig().getType().orElse("");
                masterKeyReaderType = secureVaultConfiguration.getMasterKeyReaderConfig().getType().orElse("");
            });

        } catch (SecureVaultException | RuntimeException e) {
            optSecureVaultConfiguration = Optional.empty();
            logger.error("Error while acquiring secure vault configuration", e);
        }
    }

    /**
     * Initialize the secret repository and master key reader service provider loaders.
     */
    private void initSPI() {
        secretRepositoryLoader = ServiceLoader.load(SecretRepository.class);
        masterKeyReaderLoader = ServiceLoader.load(MasterKeyReader.class);
    }

    public static synchronized SecureVaultInitializer getInstance() {
        if (secureVaultInitializer == null) {
            return new SecureVaultInitializer();
        }
        return secureVaultInitializer;
    }

    /**
     * Initialise secret repository for service provider implementation as the same as specified in secure-vault yaml.
     */
    private void initializeSecretRepository() {
        Iterator<SecretRepository> secretRepositoryTypes = secretRepositoryLoader.iterator();
        while (secretRepositoryTypes.hasNext()) {
            SecretRepository secretRepository = secretRepositoryTypes.next();
            if (secretRepositoryType.equals(secretRepository.getClass().getName())) {
                SecureVaultDataHolder.getInstance().setSecretRepository(secretRepository);
            }
        }
    }

    /**
     * Initialise master key reader for the service provider implementation as the same as specified in secure vault
     * yaml.
     */
    private void initializeMasterKeyReader() {
        Iterator<MasterKeyReader> masterKeyReaderTypes = masterKeyReaderLoader.iterator();
        while (masterKeyReaderTypes.hasNext()) {
            MasterKeyReader masterKeyReader = masterKeyReaderTypes.next();
            if (masterKeyReaderType.equals(masterKeyReader.getClass().getName())) {
                SecureVaultDataHolder.getInstance().setMasterKeyReader(masterKeyReader);
            }
        }

    }

    /**
     * Initialise the secure vault for SPI case.
     *
     * @param masterKeysFilePath Master key file path which is used to unlock secret repository.
     * @param secretPropertiesFilePath Secret properties file path which is used to store keys and aliases.
     * @param secureVaultYAMLPath Secure vault yaml path which is used to store all configuration related to secure
     *                            vault.
     */
    public void initializeSecureVault(String masterKeysFilePath, String secretPropertiesFilePath,
                                      String secureVaultYAMLPath) {
        System.setProperty("MasterKeys_File_Path", masterKeysFilePath);
        System.setProperty("secret.properties.path", secretPropertiesFilePath);
        System.setProperty("secure-vault.yaml.path", secureVaultYAMLPath);
        initFromSecureVaultYAML();
        initializeMasterKeyReader();
        initializeSecretRepository();
        initializeSecureVault();

    }

    /**
     * Initialize the secret repository.
     */
    public void initializeSecureVault() {
        synchronized (this) {
            if (initialized) {
                logger.debug("Secure Vault Component is already initialized");
                return;
            }

            try {
                logger.debug("Initializing the secure vault with, SecretRepositoryType={}, MasterKeyReaderType={}",
                        secretRepositoryType, masterKeyReaderType);

                SecureVaultConfiguration secureVaultConfiguration = optSecureVaultConfiguration
                        .orElseThrow(() -> new SecureVaultException("Cannot initialize secure vault without " +
                                "secure vault configurations"));

                MasterKeyReader masterKeyReader = SecureVaultDataHolder.getInstance().getMasterKeyReader()
                        .orElseThrow(() ->
                                new SecureVaultException("Cannot initialise secure vault without master key reader"));
                SecretRepository secretRepository = SecureVaultDataHolder.getInstance().getSecretRepository()
                        .orElseThrow(() ->
                                new SecureVaultException("Cannot initialise secure vault without secret repository"));

                masterKeyReader.init(secureVaultConfiguration.getMasterKeyReaderConfig());
                secretRepository.init(secureVaultConfiguration.getSecretRepositoryConfig(), masterKeyReader);

                secretRepository.loadSecrets(secureVaultConfiguration.getSecretRepositoryConfig());

                initialized = true;
            } catch (SecureVaultException e) {
                logger.error("Failed to initialize Secure Vault.", e);
            }
        }

        logger.debug("Secure Vault initialized successfully");
    }
}
