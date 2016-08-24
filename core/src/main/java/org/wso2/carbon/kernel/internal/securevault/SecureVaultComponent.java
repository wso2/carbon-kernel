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

package org.wso2.carbon.kernel.internal.securevault;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.internal.DataHolder;
import org.wso2.carbon.kernel.securevault.MasterKeyReader;
import org.wso2.carbon.kernel.securevault.SecretRepository;
import org.wso2.carbon.kernel.securevault.SecureVault;
import org.wso2.carbon.kernel.securevault.config.model.SecureVaultConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

import java.util.Optional;

/**
 * This service component acts as a RequiredCapabilityListener for all the ${@link SecretRepository}s and
 * ${@link MasterKeyReader}s. This component will receive all the ${@link SecretRepository} and
 * ${@link MasterKeyReader} services registrations, but it will only keep references for the services that are
 * configured in the secure-vault.yaml. Once all the services are available, this component will initialize the
 * corresponding ${@link SecretRepository} and ${@link MasterKeyReader} and call the ${@link SecretRepository}
 * to load the secrets. Once the ${@link SecretRepository} is ready, this component will  register the
 * SecureVault OSGi service, which can then be used by other components for encryption and decryption.
 *
 * @since 5.2.0
 */
@Component(
        name = "org.wso2.carbon.kernel.internal.securevault.SecureVaultComponent",
        immediate = true
)
public class SecureVaultComponent {
    private static final Logger logger = LoggerFactory.getLogger(SecureVaultComponent.class);
    private Optional<SecureVaultConfiguration> optSecureVaultConfiguration;
    private boolean initialized = false;

    private String secretRepositoryType;
    private String masterKeyReaderType;

    public SecureVaultComponent() {
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

    @Activate
    public void activate() {
        logger.debug("Activating SecureVaultComponent");
    }

    @Deactivate
    public void deactivate() {
        logger.debug("Deactivating SecureVaultComponent");
    }

    @Reference(
            name = "secure.vault.secret.repository",
            service = SecretRepository.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unRegisterSecretRepository"
    )
    protected void registerSecretRepository(SecretRepository secretRepository) {
        if (secretRepository.getClass().getName().equals(secretRepositoryType)) {
            logger.debug("Registering secret repository : {}", secretRepositoryType);
            SecureVaultDataHolder.getInstance().setSecretRepository(secretRepository);
            initializeSecureVault();
        }
    }

    protected void unRegisterSecretRepository(SecretRepository secretRepository) {
        if (secretRepository.getClass().getName().equals(secretRepositoryType)) {
            logger.debug("Un-registering secret repository : {}", secretRepositoryType);
            SecureVaultDataHolder.getInstance().setSecretRepository(null);
        }
    }

    @Reference(
            name = "secure.vault.master.key.reader",
            service = MasterKeyReader.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterMasterKeyReader"
    )
    protected void registerMasterKeyReader(MasterKeyReader masterKeyReader) {
        if (masterKeyReader.getClass().getName().equals(masterKeyReaderType)) {
            logger.debug("Registering secret repository : ", masterKeyReaderType);
            SecureVaultDataHolder.getInstance().setMasterKeyReader(masterKeyReader);
            initializeSecureVault();
        }
    }

    protected void unregisterMasterKeyReader(MasterKeyReader masterKeyReader) {
        if (masterKeyReader.getClass().getName().equals(masterKeyReaderType)) {
            logger.debug("Un-registering secret repository : ", masterKeyReaderType);
            SecureVaultDataHolder.getInstance().setMasterKeyReader(null);
        }
    }

    private void initializeSecureVault() {
        synchronized (this) {
            if (initialized) {
                logger.debug("Secure Vault Component is already initialized");
                return;
            }

            if (!SecureVaultDataHolder.getInstance().getSecretRepository().isPresent() ||
                    !SecureVaultDataHolder.getInstance().getMasterKeyReader().isPresent()) {
                logger.debug("Waiting for Secure Vault dependencies");
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

                Optional.ofNullable(DataHolder.getInstance().getBundleContext())
                        .ifPresent(bundleContext -> bundleContext
                                .registerService(SecureVault.class, new SecureVaultImpl(), null));

                initialized = true;
            } catch (SecureVaultException e) {
                logger.error("Failed to initialize Secure Vault.", e);
            }
        }

        logger.debug("Secure Vault initialized successfully");
    }
}
