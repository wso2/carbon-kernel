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


import java.util.Optional;

/**
 * Represents the abstraction 'Repository of secret'
 * Implementation can be multiple types, eg: file.
 *
 * @since 5.2.0
 */
public interface SecretRepository {

    /**
     * Initializes the repository based on provided configuration
     *
     * @param keyStoreInformation Configuration for keystore.
     */
    void init(KeyStoreInformation keyStoreInformation);

    /**
     * Returns the secret of provided alias name . An alias represents the logical name
     * for a look up secret
     *
     * @param alias Alias name for look up a secret
     * @return Secret if there is any , otherwise ,alias itself
     */
    String getSecret(Optional<String> alias);

    boolean isTokenEncrypted(String alias);
    //todo
    String getLocation();

    //todo
    void setLocation(String location);

    //todo
    void setProvider(String provider);
}
