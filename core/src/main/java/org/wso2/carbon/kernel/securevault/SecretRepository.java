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

import org.wso2.carbon.kernel.securevault.config.model.SecretRepositoryConfiguration;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

/**
 * This interface is used to register SecretRepositories. An implementation of this interface should be registered
 * as an OSGi service using the SecretRepository interface.
 *
 * The implementation of this interface can be different from one SecretRepository to another depending on its
 * requirements and behaviour.
 *
 * @since 5.2.0
 */
public interface SecretRepository {

    /**
     * This method will be called with a {@link SecretRepositoryConfiguration}, a {@link MasterKeyReader}.
     * An implementation of this method should initialize the {@link SecretRepository}, which make the
     * SecretRepository ready for {@code loadSecrets} and {@code persistSecrets}
     *
     * @param secretRepositoryConfiguration {@link SecretRepositoryConfiguration}
     * @param masterKeyReader          an initialized {@link MasterKeyReader}
     * @throws SecureVaultException    on an error while trying to initialize the SecretRepository
     */
    void init(SecretRepositoryConfiguration secretRepositoryConfiguration, MasterKeyReader masterKeyReader)
            throws SecureVaultException;

    /**
     * An implementation of this method should load the secrets from underlying secret repository.
     *
     * @param secretRepositoryConfiguration {@link SecretRepositoryConfiguration}
     * @throws SecureVaultException    on an error while trying to load secrets
     */
    void loadSecrets(SecretRepositoryConfiguration secretRepositoryConfiguration) throws SecureVaultException;

    /**
     * An implementation of this method should persist the secrets to the underlying secret repository.
     *
     * @param secretRepositoryConfiguration {@link SecretRepositoryConfiguration}
     * @throws SecureVaultException    on an error while trying to persis secrets
     */
    void persistSecrets(SecretRepositoryConfiguration secretRepositoryConfiguration) throws SecureVaultException;

    /**
     * An implementation of this method should provide the plain text secret for a given alias.
     *
     * @param alias alias of the secret
     * @return      if the given alias is available, a char[] consisting the plain text secret else and empty char[]
     */
    char[] resolve(String alias);

    /**
     * An implementation of this method should provide the relevant encryption logic.
     *
     * @param plainText             plain text as a byte array
     * @return byte[]               cipher text as a byte array
     * @throws SecureVaultException on an error while trying to encrypt.
     */
    byte[] encrypt(byte[] plainText) throws SecureVaultException;

    /**
     * An implementation of this method should provide the relevant decryption logic.
     *
     * @param cipherText            cipher text as a byte array
     * @return byte[]               plain text as a byte array
     * @throws SecureVaultException on an error while trying to encrypt.
     */
    byte[] decrypt(byte[] cipherText) throws SecureVaultException;
}
