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

package org.wso2.carbon.keystore.persistence.impl;

import org.wso2.carbon.keystore.persistence.KeyStorePersistenceManager;
import org.wso2.carbon.keystore.persistence.model.KeyStoreModel;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * HybridKeyStorePersistenceManager is a hybrid implementation of KeyStorePersistenceManager. It uses both JDBC and
 * Registry implementations. All new KeyStores will be added to the database, while existing keystores will be
 * maintained in the registry.
 */
public class HybridKeyStorePersistenceManager implements KeyStorePersistenceManager {

    private final JDBCKeyStorePersistenceManager jdbcKeyStorePersistenceManager =
            new JDBCKeyStorePersistenceManager();
    private final RegistryKeyStorePersistenceManager registryKeyStorePersistenceManager =
            new RegistryKeyStorePersistenceManager();

    @Override
    public void addKeystore(KeyStoreModel keyStore, int tenantId) throws SecurityException {

        jdbcKeyStorePersistenceManager.addKeystore(keyStore, tenantId);
    }

    @Override
    public Optional<KeyStoreModel> getKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        Optional<KeyStoreModel> keyStoreModel = jdbcKeyStorePersistenceManager.getKeyStore(keyStoreName, tenantId);
        if (!keyStoreModel.isPresent()) {
            keyStoreModel = registryKeyStorePersistenceManager.getKeyStore(keyStoreName, tenantId);
        }
        return keyStoreModel;
    }

    @Override
    public boolean isKeyStoreExists(String keyStoreName, int tenantId) throws SecurityException {

        if (jdbcKeyStorePersistenceManager.isKeyStoreExists(keyStoreName, tenantId)) {
            return true;
        } else {
            return registryKeyStorePersistenceManager.isKeyStoreExists(keyStoreName, tenantId);
        }
    }

    @Override
    public List<KeyStoreModel> listKeyStores(int tenantId) throws SecurityException {

        List<KeyStoreModel> keyStoreModels = jdbcKeyStorePersistenceManager.listKeyStores(tenantId);
        keyStoreModels.addAll(registryKeyStorePersistenceManager.listKeyStores(tenantId));
        return keyStoreModels;
    }

    @Override
    public void updateKeyStore(KeyStoreModel keyStoreModel, int tenantId) throws SecurityException {

        if (jdbcKeyStorePersistenceManager.isKeyStoreExists(keyStoreModel.getName(), tenantId)) {
            jdbcKeyStorePersistenceManager.updateKeyStore(keyStoreModel, tenantId);
        } else {
            registryKeyStorePersistenceManager.updateKeyStore(keyStoreModel, tenantId);
        }
    }

    @Override
    public void deleteKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        if (jdbcKeyStorePersistenceManager.isKeyStoreExists(keyStoreName, tenantId)) {
            jdbcKeyStorePersistenceManager.deleteKeyStore(keyStoreName, tenantId);
        } else {
            registryKeyStorePersistenceManager.deleteKeyStore(keyStoreName, tenantId);
        }
    }

    @Override
    public Date getKeyStoreLastModifiedDate(String keyStoreName, int tenantId) {

        if (jdbcKeyStorePersistenceManager.isKeyStoreExists(keyStoreName, tenantId)) {
            return jdbcKeyStorePersistenceManager.getKeyStoreLastModifiedDate(keyStoreName, tenantId);
        } else {
            return registryKeyStorePersistenceManager.getKeyStoreLastModifiedDate(keyStoreName, tenantId);
        }
    }

    @Override
    public String getEncryptedKeyStorePassword(String keyStoreName, int tenantId) throws SecurityException {

        if (jdbcKeyStorePersistenceManager.isKeyStoreExists(keyStoreName, tenantId)) {
            return jdbcKeyStorePersistenceManager.getEncryptedKeyStorePassword(keyStoreName, tenantId);
        } else {
            return registryKeyStorePersistenceManager.getEncryptedKeyStorePassword(keyStoreName, tenantId);
        }
    }

    @Override
    public String getEncryptedPrivateKeyPassword(String keyStoreName, int tenantId) throws SecurityException {

        if (jdbcKeyStorePersistenceManager.isKeyStoreExists(keyStoreName, tenantId)) {
            return jdbcKeyStorePersistenceManager.getEncryptedPrivateKeyPassword(keyStoreName, tenantId);
        } else {
            return registryKeyStorePersistenceManager.getEncryptedPrivateKeyPassword(keyStoreName, tenantId);
        }
    }
}
