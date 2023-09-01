/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
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

package org.wso2.carbon.core.keystore.dao;

import org.wso2.carbon.core.keystore.KeyStoreManagementException;
import org.wso2.carbon.core.keystore.model.KeyStoreModel;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for KeyStore.
 */
public interface KeyStoreDAO {

    /**
     * Add a new KeyStore.
     *
     * @param tenantUUID    Tenant UUID.
     * @param keyStoreModel KeyStore model where the keystore data is maintained.
     * @throws KeyStoreManagementException If an error occurs while adding the KeyStore.
     */
    void addKeyStore(String tenantUUID, KeyStoreModel keyStoreModel) throws
            KeyStoreManagementException;

    /**
     * Get all KeyStores.
     *
     * @param tenantUUID tenant UUID
     * @return List of KeyStoreModels.
     * @throws KeyStoreManagementException If an error occurs while retrieving the KeyStores.
     */
    List<KeyStoreModel> getKeyStores(String tenantUUID) throws KeyStoreManagementException;

    /**
     * Get a KeyStore.
     *
     * @param tenantUUID Tenant UUID.
     * @param fileName   Name of the KeyStore file.
     * @return KeyStoreModel.
     * @throws KeyStoreManagementException If an error occurs while retrieving the KeyStore.
     */
    Optional<KeyStoreModel> getKeyStore(String tenantUUID, String fileName) throws KeyStoreManagementException;

    /**
     * Delete a KeyStore.
     *
     * @param tenantUUID Tenant UUID.
     * @param fileName   Name of the KeyStore file.
     * @throws KeyStoreManagementException If an error occurs while deleting the KeyStore.
     */
    void deleteKeyStore(String tenantUUID, String fileName) throws KeyStoreManagementException;

    /**
     * Update a KeyStore.
     *
     * @param tenantUUID    Tenant UUID.
     * @param keyStoreModel KeyStore model where the keystore data is maintained.
     * @throws KeyStoreManagementException If an error occurs while updating the KeyStore.
     */
    void updateKeyStore(String tenantUUID, KeyStoreModel keyStoreModel) throws KeyStoreManagementException;

    /**
     * Add a public certificate to a KeyStore.
     *
     * @param tenantUUID Tenant UUID.
     * @param fileName   Name of the KeyStore file.
     * @param pubCertId  Public certificate ID.
     * @throws KeyStoreManagementException If an error occurs while adding the public certificate.
     */
    void addPubCertIdToKeyStore(String tenantUUID, String fileName, String pubCertId)
            throws KeyStoreManagementException;

    /**
     * Get the public certificate ID of a KeyStore.
     *
     * @param tenantUUID Tenant UUID.
     * @param fileName   Name of the KeyStore file.
     * @return Public certificate ID.
     * @throws KeyStoreManagementException If an error occurs while retrieving the public certificate ID.
     */
    Optional<String> getPubCertIdFromKeyStore(String tenantUUID, String fileName) throws KeyStoreManagementException;
}
