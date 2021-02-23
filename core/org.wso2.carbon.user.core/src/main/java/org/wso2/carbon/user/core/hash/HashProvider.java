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
 * This is the service interface for the HashProvider which can be used to integrate any hashing algorithm.
 */
public interface HashProvider {

    /**
     * Calculate the hash value according to the given properties and salt.
     *
     * @param value          The value which needs to be hashed.
     * @param salt           The salt value.
     * @param metaProperties The attributes which are needed by the HashProvider to calculate hash of a given value.
     * @return The calculated hash value for the respective value.
     * @throws HashProviderException Exception which will be thrown when there is an issue with HashProvider service.
     */
    byte[] getHash(String value, String salt, Map<String, Object> metaProperties)
            throws HashProviderException;

    /**
     * Get hash algorithm supported by the HashProvider.
     *
     * @return Hashing algorithm which is being used for hashing.
     */
    String getAlgorithm();
}
