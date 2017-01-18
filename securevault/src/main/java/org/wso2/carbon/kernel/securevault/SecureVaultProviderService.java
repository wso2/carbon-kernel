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
import org.wso2.carbon.kernel.securevault.internal.SecureVaultDataHolder;

/**
 * Service provider class for secure vault functionality.
 */
public class SecureVaultProviderService implements SecureVault {

    private static SecureVaultProviderService providerService = null;

    private SecureVaultProviderService() {
    }

    public static synchronized SecureVaultProviderService getInstance() {
        if (providerService == null) {
            providerService = new SecureVaultProviderService();
        }
        return providerService;
    }


    @Override
    public char[] resolve(String alias) throws SecureVaultException {
        return SecureVaultDataHolder.getInstance().getSecretRepository()
                .orElseThrow(() -> new SecureVaultException("No secret repository found."))
                .resolve(alias);
    }

    @Override
    public byte[] encrypt(byte[] plainText) throws SecureVaultException {
        return SecureVaultDataHolder.getInstance().getSecretRepository()
                .orElseThrow(() -> new SecureVaultException("No secret repository found."))
                .encrypt(plainText);
    }

    @Override
    public byte[] decrypt(byte[] cipherText) throws SecureVaultException {
        return SecureVaultDataHolder.getInstance().getSecretRepository()
                .orElseThrow(() -> new SecureVaultException("No secret repository found."))
                .decrypt(cipherText);
    }
}
