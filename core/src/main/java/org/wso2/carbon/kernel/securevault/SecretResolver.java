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

/**
 * Responsible for resolving secrets/encrypted data.
 *
 * @since 5.2.0
 */
public class SecretResolver {

    private static final Logger logger = LoggerFactory.getLogger(SecretResolver.class);

    private final SecretManager secretManager;

    public SecretResolver() {
        secretManager = SecretManager.getInstance();
    }
    /**
     * Check if a value for a given alias is encrypted.
     *
     * @param alias alias of the secret.
     * @return true if the value is encrypted or false otherwise
     */
    public boolean isTokenEncrypted(String alias) {
        return secretManager.isTokenEncrypted(alias);
    }

    /**
     * Resolve a given secret
     *
     * @param alias alias of the secret to be resolved
     * @return secret if alias is protected or return alias itself otherwise.
     */
    public String resolveSecret(String alias) {
        if (!secretManager.isInitialized()) {
            if (logger.isWarnEnabled()) {
                logger.warn("SecretManager has not been initialized.Cannot collect secrets. Returning alias itself.");
            }
            return alias;
        }

        return secretManager.getSecret(alias);
    }
}
