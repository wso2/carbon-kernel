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

package org.wso2.carbon.keystore.persistence;

import org.wso2.carbon.keystore.persistence.model.KeyStoreMetadata;
import org.wso2.carbon.keystore.persistence.model.KeyStoreModel;

import java.util.Date;
import java.util.List;

/**
 * This interface supports the persistence/storage related logics of key stores.
 */
public interface KeyStorePersistenceManager {

    /**
     * Add the key store to the data storage.
     *
     * @param keyStoreModel Key store model.
     * @throws SecurityException If an error occurs while adding the key store.
     */
    void addKeystore(KeyStoreModel keyStoreModel) throws SecurityException;

    /**
     * Get the key store from the data storage.
     *
     * @param keyStoreName Name of the key store.
     * @param tenantId     Tenant Id.
     * @return Key store model.
     * @throws SecurityException If an error occurs while getting the key store.
     */
    KeyStoreModel getKeyStore(String keyStoreName, int tenantId) throws SecurityException;

    /**
     * Method to retrieve list of keystore metadata of all the keystores in a tenant.
     *
     * @param tenantId tenantId.
     * @return List of KeyStoreMetaData objects.
     * @throws SecurityException If an error occurs while retrieving the keystore data.
     */
    List<KeyStoreMetadata> listKeyStores(int tenantId) throws SecurityException;

    /**
     * Update the key store in the data storage.
     *
     * @param keyStoreModel Key store model.
     */
    void updateKeyStore(KeyStoreModel keyStoreModel);

    /**
     * Delete the key store from the data storage.
     *
     * @param keyStoreName Name of the key store.
     * @param tenantId     Tenant Id.
     * @throws SecurityException If an error occurs while deleting the key store.
     */
    void deleteKeyStore(String keyStoreName, int tenantId) throws SecurityException;

    /**
     * Get the last modified date of the key store.
     *
     * @param keyStoreName Key store name.
     * @return Last modified date of the key store.
     */
    Date getKeyStoreLastModifiedDate(String keyStoreName, int tenantId);

    /**
     * Get the password as a character array for the given key store name.
     *
     * @param keyStoreName key store name.
     * @param tenantId     Tenant Id.
     * @return KeyStore Password as a character array.
     * @throws SecurityException If there is an error while getting the key store password.
     */
    String getEncryptedKeyStorePassword(String keyStoreName, int tenantId) throws SecurityException;

    /**
     * Get the private key password as a character array for the given key store name.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant Id.
     * @return Private key password as a character array.
     * @throws SecurityException If an error occurs while getting the private key password.
     */
    String getEncryptedPrivateKeyPassword(String keyStoreName, int tenantId) throws SecurityException;
}
