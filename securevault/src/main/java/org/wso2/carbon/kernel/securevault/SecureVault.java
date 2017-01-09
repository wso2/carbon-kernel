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

import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

/**
 * This interface is used to register SecureVault.
 * Any component that needs SecureVault capabilities should depend on this interface.
 *
 * @since 5.2.0
 */
public interface SecureVault {

    /**
     * Resolves the given alias in to a plain text password.
     *
     * @param alias alias of the secret
     * @return      if the alias exist return the plain text password else return an empty char[]
     * @throws SecureVaultException on an error while trying to resolve.
     */
    char[] resolve(String alias) throws SecureVaultException;

    /**
     * An implementation of this method should delegate the encryption to the underlying {@link SecretRepository}
     * and get the {@code plainText} encrypted.
     *
     * @param plainText             plain text as a byte array
     * @return byte[]               cipher text as a byte array
     * @throws SecureVaultException on an error while trying to encrypt.
     */
    byte[] encrypt(byte[] plainText) throws SecureVaultException;

    /**
     * An implementation of this method should delegate the decryption to the underlying {@link SecretRepository}
     * and get the {@code cipherText} decrypted.
     *
     * @param cipherText            cipher text as a byte array
     * @return byte[]               plain text as a byte array
     * @throws SecureVaultException on an error while trying to decrypt.
     */
    byte[] decrypt(byte[] cipherText) throws SecureVaultException;
}
