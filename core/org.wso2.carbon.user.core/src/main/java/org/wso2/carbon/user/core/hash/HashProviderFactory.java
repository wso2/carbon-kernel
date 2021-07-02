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
import java.util.Set;

/**
 * This is the service interface for the HashProviderFactory which can be used to integrate any hashing algorithm.
 */
public interface HashProviderFactory {

    /**
     * Get an instance HashProvider with the default hash provider configurations.
     *
     * @return HashProvider instance.
     */
    HashProvider getHashProvider();

    /**
     * Get an instance of HashProvider with the given initProperty configurations.
     *
     * @param initProperties The properties that needs to be initialized.
     * @return HashProvider instance.
     * @throws HashProviderException If an error occurred while getting the instance of the hashProvider.
     */
    HashProvider getHashProvider(Map<String, Object> initProperties) throws HashProviderException;

    /**
     * Get Hash Provider meta properties names.
     *
     * @return Hash Provider meta properties names.
     */
    Set<String> getHashProviderConfigProperties();

    /**
     * Get the hashing algorithm supported by the HashProvider instance which is returned from the factory.
     *
     * @return The factory algorithm type.
     */
    String getAlgorithm();
}
