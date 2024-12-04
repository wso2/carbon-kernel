/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.core.keystore.persistence;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Factory class to get the KeyStorePersistenceManager based on the configuration.
 */
public class KeyStorePersistenceManagerFactory {

    private static final String KEYSTORE_STORAGE_TYPE =
            CarbonUtils.getServerConfiguration().getFirstProperty("Security.KeyStores.DataStorageType");
    private static final String HYBRID = "hybrid";
    private static final String REGISTRY = "registry";
    private static final String DATABASE = "database";

    private KeyStorePersistenceManagerFactory() {

    }

    public static KeyStorePersistenceManager getKeyStorePersistenceManager() {

        KeyStorePersistenceManager defaultPolicyPersistenceManager = new RegistryKeyStorePersistenceManager();
        if (StringUtils.isNotBlank(KEYSTORE_STORAGE_TYPE)) {
            switch (KEYSTORE_STORAGE_TYPE) {
                case REGISTRY:
                    return new RegistryKeyStorePersistenceManager();
                case DATABASE:
                    return new JDBCKeyStorePersistenceManager();
                default:
                    return defaultPolicyPersistenceManager;
            }
        }
        return defaultPolicyPersistenceManager;
    }
}
