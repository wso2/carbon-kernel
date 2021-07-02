/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.hash;

import org.wso2.carbon.user.core.exceptions.HashProviderException;

import java.util.Map;

/**
 * This is the service interface for the HashProvider.
 */
public interface HashProvider {

    /**
     * Initialize the HashProvider with default configuration values.
     */
    void init();

    /**
     * Initialize the HashProvider with the given configuration values.
     *
     * @param initProperties Map with HashProvider initializing properties.
     * @throws HashProviderException If an error occurred while initializing the hashProvider.
     */
    void init(Map<String, Object> initProperties) throws HashProviderException;

    /**
     * Calculate the hash value according to initialized HashProvider configurations.
     *
     * @param plainText The plain text value which needs to be hashed.
     * @param salt      The salt value.
     * @return The calculated hash value for the respective value.
     * @throws HashProviderException If an error occurred while getting the hash value.
     */
    byte[] calculateHash(char[] plainText, String salt) throws HashProviderException;

    /**
     * Get HashProvider parameters.
     *
     * @return Map of HashProvider parameters.
     */
    Map<String, Object> getParameters();

    /**
     * Get hash algorithm supported by the HashProvider.
     *
     * @return Hashing algorithm which is supported by the HashProvider.
     */
    String getAlgorithm();
}
