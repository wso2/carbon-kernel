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

package org.wso2.carbon.security.keystore.dao;

import org.wso2.carbon.security.keystore.KeyStoreManagementException;
import org.wso2.carbon.security.keystore.model.KeyStoreModel;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for KeyStore.
 */
public interface KeyStoreDAO {

    void addKeyStore(String tenantUUID, KeyStoreModel keyStoreModel) throws
            KeyStoreManagementException;

    List<KeyStoreModel> getKeyStores(String tenantUUID) throws KeyStoreManagementException;

    Optional<KeyStoreModel> getKeyStore(String tenantUUID, String fileName) throws KeyStoreManagementException;

    void deleteKeyStore(String tenantUUID, String fileName) throws KeyStoreManagementException;

    void updateKeyStore(String tenantUUID, KeyStoreModel keyStoreModel) throws KeyStoreManagementException;

    void addPubCertIdToKeyStore(String tenantUUID, String fileName, String pubCertId) throws KeyStoreManagementException;

    Optional<String> getPubCertIdFromKeyStore(String tenantUUID, String fileName) throws KeyStoreManagementException;
}
