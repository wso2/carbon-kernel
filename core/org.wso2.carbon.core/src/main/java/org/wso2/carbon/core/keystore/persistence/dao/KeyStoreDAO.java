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

package org.wso2.carbon.core.keystore.persistence.dao;

import org.wso2.carbon.core.internal.CarbonCoreDataHolder;
import org.wso2.carbon.core.keystore.persistence.model.KeyStoreModel;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.sql.DataSource;

import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.DEFAULT_VERSION;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.CONTENT;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.CREATED_AT;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.NAME;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.PASSWORD;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.PRIVATE_KEY_ALIAS;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.PRIVATE_KEY_PASS;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.PROVIDER;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.PUB_CERT_ID;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.TENANT_ID;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.TYPE;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.UPDATED_AT;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.KeyStoreTableColumns.VERSION;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.ADD_KEY_STORE;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.DELETE_KEY_STORE_BY_NAME;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.GET_ENCRYPTED_KEY_STORE_PASSWORD;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.GET_ENCRYPTED_PRIVATE_KEY_PASSWORD;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.GET_KEY_STORES;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.GET_KEY_STORES_EXISTENCE;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.GET_KEY_STORE_BY_NAME;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.GET_KEY_STORE_LAST_UPDATED_TIME;
import static org.wso2.carbon.core.keystore.persistence.PersistenceManagerConstants.SqlQueries.UPDATE_KEY_STORE_BY_NAME;

import static java.time.ZoneOffset.UTC;

public class KeyStoreDAO {

    private final DataSource dataSource = CarbonCoreDataHolder.getInstance().getDataSource();
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));

    /**
     * Add a new key store.
     *
     * @param keyStoreModel Key store model.
     */
    public void addKeystore(KeyStoreModel keyStoreModel) {

        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            byte[] contentByteArray = keyStoreModel.getContent();
            int contentLength = contentByteArray.length;
            try (InputStream contentStream = new ByteArrayInputStream(contentByteArray)) {
                NamedPreparedStatement statement = new NamedPreparedStatement(connection, ADD_KEY_STORE);
                statement.setString(NAME, keyStoreModel.getName());
                statement.setString(TYPE, keyStoreModel.getType());
                statement.setString(PROVIDER, keyStoreModel.getProvider());
                statement.setString(PASSWORD, keyStoreModel.getEncryptedPassword());
                statement.setString(PRIVATE_KEY_ALIAS, keyStoreModel.getPrivateKeyAlias());
                statement.setString(PRIVATE_KEY_PASS, keyStoreModel.getEncryptedPrivateKeyPass());
                statement.setInt(TENANT_ID, keyStoreModel.getTenantId());
                statement.setString(PUB_CERT_ID, keyStoreModel.getPublicCertId());
                statement.setTimeStamp(CREATED_AT, new Timestamp(new Date().getTime()), calendar);
                statement.setTimeStamp(UPDATED_AT, new Timestamp(new Date().getTime()), calendar);
                statement.setBinaryStream(CONTENT, contentStream, contentLength);
                statement.setString(VERSION, DEFAULT_VERSION);
                statement.executeUpdate();
                connection.commit();
            } catch (IOException e) {
                throw new SecurityException("Error while adding key store.", e);
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while adding key store.", e);
        }
    }

    /**
     * Retrieve a key store by name.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant ID.
     * @return Key store model.
     */
    public Optional<KeyStoreModel> getKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_KEY_STORE_BY_NAME)) {
                statement.setString(NAME, keyStoreName);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapResultToKeyStoreModel(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while retrieving key store.", e);
        }
        return Optional.empty();
    }

    /**
     * Retrieve all key stores of a tenant.
     *
     * @param tenantId Tenant ID.
     * @return List of key store models.
     */
    public List<KeyStoreModel> listKeyStores(int tenantId) throws SecurityException {

        List<KeyStoreModel> keyStores = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_KEY_STORES)) {
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        keyStores.add(mapResultToKeyStoreModel(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while retrieving key store list of tenant.", e);
        }
        return keyStores;
    }

    /**
     * Update a key store.
     *
     * @param keyStoreModel Key store model.
     */
    public void updateKeyStore(KeyStoreModel keyStoreModel) {

        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            byte[] contentByteArray = keyStoreModel.getContent();
            int contentLength = contentByteArray.length;
            try (InputStream contentStream = new ByteArrayInputStream(contentByteArray)) {
                NamedPreparedStatement statement = new NamedPreparedStatement(connection, UPDATE_KEY_STORE_BY_NAME);
                statement.setBinaryStream(CONTENT, contentStream, contentLength);
                statement.setTimeStamp(UPDATED_AT, new Timestamp(new Date().getTime()), calendar);
                statement.setString(NAME, keyStoreModel.getName());
                statement.setInt(TENANT_ID, keyStoreModel.getTenantId());
                statement.executeUpdate();
                connection.commit();
            } catch (IOException e) {
                throw new SecurityException("Error while updating key store.", e);
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while updating key store.", e);
        }
    }

    /**
     * Delete a key store by name.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant ID.
     */
    public void deleteKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, DELETE_KEY_STORE_BY_NAME)) {
                statement.setString(NAME, keyStoreName);
                statement.setInt(TENANT_ID, tenantId);
                statement.executeUpdate();
                connection.commit();
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while deleting key store.", e);
        }
    }

    /**
     * Retrieve the last modified date of a key store.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant ID.
     * @return Last modified date.
     */
    public Date getKeyStoreLastModifiedDate(String keyStoreName, int tenantId) {

        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    GET_KEY_STORE_LAST_UPDATED_TIME)) {
                statement.setString(NAME, keyStoreName);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getDate(UPDATED_AT);
                    }
                }
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while retrieving key stores.", e);
        }
        return null;
    }

    /**
     * Retrieve the encrypted key store password.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant ID.
     * @return Encrypted key store password.
     */
    public String getEncryptedKeyStorePassword(String keyStoreName, int tenantId) throws SecurityException {

        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    GET_ENCRYPTED_KEY_STORE_PASSWORD)) {
                statement.setString(NAME, keyStoreName);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(PASSWORD);
                    }
                }
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while retrieving key store password.", e);
        }
        return null;
    }

    /**
     * Retrieve the encrypted private key password.
     *
     * @param keyStoreName Key store name.
     * @param tenantId     Tenant ID.
     * @return Encrypted private key password.
     */
    public String getEncryptedPrivateKeyPassword(String keyStoreName, int tenantId) throws SecurityException {

        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    GET_ENCRYPTED_PRIVATE_KEY_PASSWORD)) {
                statement.setString(NAME, keyStoreName);
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString(PRIVATE_KEY_PASS);
                    }
                }
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while retrieving key store password.", e);
        }
        return null;
    }

    /**
     * Check whether the tenant primary keystore exists or not.
     *
     * @param tenantId Tenant ID.
     * @return whether the tenant primary keystore exists or not.
     * @throws SecurityException If an error occurs.
     */
    public boolean isTenantPrimaryKeyStoreExists(int tenantId) throws SecurityException {

        try (Connection connection = DatabaseUtil.getDBConnection(dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection, GET_KEY_STORES_EXISTENCE)) {
                statement.setInt(TENANT_ID, tenantId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            throw new SecurityException("Error while checking Tenant Primary KeyStore existence", e);
        }
    }

    private KeyStoreModel mapResultToKeyStoreModel(ResultSet resultSet) throws SQLException {

        KeyStoreModel keyStoreModel = new KeyStoreModel();
        keyStoreModel.setName(resultSet.getString(NAME));
        keyStoreModel.setType(resultSet.getString(TYPE));
        keyStoreModel.setProvider(resultSet.getString(PROVIDER));
        keyStoreModel.setEncryptedPassword(resultSet.getString(PASSWORD));
        keyStoreModel.setPrivateKeyAlias(resultSet.getString(PRIVATE_KEY_ALIAS));
        keyStoreModel.setEncryptedPrivateKeyPass(resultSet.getString(PRIVATE_KEY_PASS));
        keyStoreModel.setContent(resultSet.getBytes(CONTENT));
        keyStoreModel.setTenantId(resultSet.getInt(TENANT_ID));
        keyStoreModel.setPublicCertId(resultSet.getString(PUB_CERT_ID));
        return keyStoreModel;
    }

}
