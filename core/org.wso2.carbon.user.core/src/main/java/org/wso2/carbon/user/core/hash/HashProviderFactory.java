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

import java.util.List;
import java.util.Map;

/**
 * This is the service interface for the HashProviderFactory which can be used to integrate any hashing algorithm.
 */
public interface HashProviderFactory {

    /**
     * Initializes the default params and get HashProvider.
     *
     * @return HashProvider instance.
     */
    HashProvider getHashProvider();

    /**
     * Initializes the params and get HashProvider.
     *
     * @param initProperties The properties that needs to be initialized.
     * @return HashProvider instance.
     */
    HashProvider getHashProvider(Map<String, Object> initProperties);

    /**
     * Get meta property keys.
     *
     * @return List of metaProperty keys.
     */
    List<String> getMetaPropertyKeys();

    /**
     * Get the type of the factory algorithm being used.
     *
     * @return The factory algorithm type.
     */
    String getType();
}
