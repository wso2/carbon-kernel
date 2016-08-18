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

import org.wso2.carbon.kernel.securevault.SecureVault;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;

/**
 * The default implementation of the SecureVault.
 *
 * @since 5.2.0
 */
public class SecureVaultImpl implements SecureVault {

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
