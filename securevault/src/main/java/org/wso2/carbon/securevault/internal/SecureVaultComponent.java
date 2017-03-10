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

package org.wso2.carbon.securevault.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.securevault.MasterKeyReader;
import org.wso2.carbon.securevault.SecretRepository;
import org.wso2.carbon.securevault.SecureVault;
import org.wso2.carbon.securevault.SecureVaultInitializer;

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
        name = "org.wso2.carbon.securevault.internal.SecureVaultComponent",
        immediate = true
)
public class SecureVaultComponent {

    private static final Logger logger = LoggerFactory.getLogger(SecureVaultComponent.class);

    public SecureVaultComponent() {
        SecureVaultInitializer.getInstance().initFromSecureVaultYAML();
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        logger.debug("Activating SecureVaultComponent");
    }

    @Deactivate
    public void deactivate(BundleContext bundleContext) {
        SecureVaultDataHolder.getInstance().setBundleContext(null);
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
        if (secretRepository.getClass().getName().equals(
                SecureVaultInitializer.getInstance().getSecretRepositoryType())) {
            logger.debug("Registering secret repository : {}", SecureVaultInitializer.getInstance()
                    .getSecretRepositoryType());
            SecureVaultDataHolder.getInstance().setSecretRepository(secretRepository);
            initializeSecureVault();
        }
    }

    protected void unRegisterSecretRepository(SecretRepository secretRepository) {
        if (secretRepository.getClass().getName().equals(
                SecureVaultInitializer.getInstance().getSecretRepositoryType())) {
            logger.debug("Un-registering secret repository : {}", SecureVaultInitializer.getInstance()
                    .getSecretRepositoryType());
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
        if (masterKeyReader.getClass().getName().equals(
                SecureVaultInitializer.getInstance().getMasterKeyReaderType())) {
            logger.debug("Registering secret repository : ", SecureVaultInitializer.getInstance()
                    .getMasterKeyReaderType());
            SecureVaultDataHolder.getInstance().setMasterKeyReader(masterKeyReader);
            initializeSecureVault();
        }
    }

    protected void unregisterMasterKeyReader(MasterKeyReader masterKeyReader) {
        if (masterKeyReader.getClass().getName().equals(
                SecureVaultInitializer.getInstance().getMasterKeyReaderType())) {
            logger.debug("Un-registering secret repository : ", SecureVaultInitializer.getInstance()
                    .getMasterKeyReaderType());
            SecureVaultDataHolder.getInstance().setMasterKeyReader(null);
        }
    }

    /**
     * Initialise the Secure Vault. This method wait until master key reader service and secret repository service are
     * resolved and call SecureVaultInitializer.initializeSecureVault to initialise master key reader and secret
     * repository and loading secrets to secret repository and will register SecureVault service finally if all
     * the previous tasks successful.
     */
    private void initializeSecureVault() {

        if (!SecureVaultDataHolder.getInstance().getSecretRepository().isPresent() ||
                !SecureVaultDataHolder.getInstance().getMasterKeyReader().isPresent() ||
                !SecureVaultDataHolder.getInstance().getBundleContext().isPresent()) {
            logger.debug("Waiting for Secure Vault dependencies");
            return;
        }

        SecureVaultInitializer.getInstance().initializeSecureVault();

        if (SecureVaultInitializer.getInstance().initialized) {
            SecureVaultDataHolder.getInstance().getBundleContext()
                    .ifPresent(bundleContext -> bundleContext
                            .registerService(SecureVault.class, new SecureVaultImpl(), null));
        }

    }
}
