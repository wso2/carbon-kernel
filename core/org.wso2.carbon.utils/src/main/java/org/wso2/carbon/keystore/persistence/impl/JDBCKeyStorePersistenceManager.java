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

import org.wso2.carbon.database.utils.jdbc.NamedJdbcTemplate;
import org.wso2.carbon.database.utils.jdbc.exceptions.DataAccessException;
import org.wso2.carbon.keystore.persistence.KeyStorePersistenceManager;
import org.wso2.carbon.keystore.persistence.model.KeyStoreModel;
import org.wso2.carbon.utils.internal.CarbonUtilsDataHolder;

import java.io.ByteArrayInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KEYSTORE_SCHEMA_VERSION;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.CONTENT;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.CREATED_AT;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.ID;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.NAME;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.PASSWORD;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.PRIVATE_KEY_ALIAS;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.PRIVATE_KEY_PASS;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.PROVIDER;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.PUB_CERT_ID;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.TENANT_ID;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.TYPE;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.UPDATED_AT;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.KeyStoreTableColumns.VERSION;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.ADD_KEY_STORE;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.DELETE_KEY_STORE;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.GET_ENCRYPTED_KEY_STORE_PASSWORD;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.GET_ENCRYPTED_PRIVATE_KEY_PASSWORD;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.GET_KEY_STORE;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.GET_KEY_STORE_LAST_UPDATED_TIME;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.IS_KEYSTORE_EXISTS;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.LIST_KEY_STORES;
import static org.wso2.carbon.keystore.persistence.constant.PersistenceManagerConstants.SqlQueries.UPDATE_KEY_STORE;

import static java.time.ZoneOffset.UTC;

/**
 * This implementation handles the keystore storage/persistence related logics in the Database.
 */
public class JDBCKeyStorePersistenceManager implements KeyStorePersistenceManager {

    private static final Calendar CALENDAR = Calendar.getInstance(TimeZone.getTimeZone(UTC));

    @Override
    public void addKeystore(KeyStoreModel keyStore, int tenantId) throws SecurityException {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        try {
            Timestamp currentTime = new Timestamp(new Date().getTime());
            namedJdbcTemplate.executeInsert(ADD_KEY_STORE, (preparedStatement -> {
                preparedStatement.setString(NAME, keyStore.getName());
                preparedStatement.setString(TYPE, keyStore.getType());
                preparedStatement.setString(PROVIDER, keyStore.getProvider());
                preparedStatement.setString(PASSWORD, keyStore.getEncryptedPassword());
                preparedStatement.setString(PRIVATE_KEY_ALIAS, keyStore.getPrivateKeyAlias());
                preparedStatement.setString(PRIVATE_KEY_PASS, keyStore.getEncryptedPrivateKeyPass());
                preparedStatement.setBinaryStream(CONTENT, new ByteArrayInputStream(keyStore.getContent()),
                        keyStore.getContent().length);
                preparedStatement.setString(PUB_CERT_ID, keyStore.getPublicCertId());
                preparedStatement.setInt(TENANT_ID, tenantId);
                preparedStatement.setString(VERSION, KEYSTORE_SCHEMA_VERSION);
                preparedStatement.setTimeStamp(CREATED_AT, currentTime, CALENDAR);
                preparedStatement.setTimeStamp(UPDATED_AT, currentTime, CALENDAR);
            }), keyStore, false);
        } catch (DataAccessException e) {
            throw new SecurityException("Error while adding the key store: " + keyStore.getName(), e);
        }
    }

    @Override
    public Optional<KeyStoreModel> getKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        KeyStoreModel keyStoreModel;
        try {
            keyStoreModel = namedJdbcTemplate.fetchSingleRecord(GET_KEY_STORE, (resultSet, rowNumber) ->
                    mapResultToKeyStoreModel(resultSet), (preparedStatement -> {
                preparedStatement.setString(NAME, keyStoreName);
                preparedStatement.setInt(TENANT_ID, tenantId);
            }));
        } catch (DataAccessException e) {
            throw new SecurityException("Error while retrieving key store: " + keyStoreName, e);
        }
        return Optional.ofNullable(keyStoreModel);
    }

    @Override
    public boolean isKeyStoreExists(String keyStoreName, int tenantId) throws SecurityException {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        try {
            Integer keystoreID = namedJdbcTemplate.fetchSingleRecord(IS_KEYSTORE_EXISTS,
                    (resultSet, rowNumber) -> resultSet.getInt(ID), (preparedStatement -> {
                        preparedStatement.setString(NAME, keyStoreName);
                        preparedStatement.setInt(TENANT_ID, tenantId);
                    }));
            return keystoreID != null;
        } catch (DataAccessException e) {
            throw new SecurityException("Error while checking the KeyStore existence for keystore name: "
                    + keyStoreName, e);
        }
    }

    @Override
    public List<KeyStoreModel> listKeyStores(int tenantId) throws SecurityException {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        List<KeyStoreModel> keyStores;
        try {
            keyStores = namedJdbcTemplate.executeQuery(LIST_KEY_STORES, (resultSet, rowNumber) ->
                    mapResultToKeyStoreModel(resultSet), (preparedStatement ->
                    preparedStatement.setInt(TENANT_ID, tenantId)
            ));
        } catch (DataAccessException e) {
            throw new SecurityException("Error while retrieving key store list of tenant: " + tenantId, e);
        }
        return keyStores;
    }

    @Override
    public void updateKeyStore(KeyStoreModel keyStoreModel, int tenantId) throws SecurityException {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        try {
            namedJdbcTemplate.executeUpdate(UPDATE_KEY_STORE, (preparedStatement -> {
                preparedStatement.setBinaryStream(CONTENT, new ByteArrayInputStream(keyStoreModel.getContent()),
                        keyStoreModel.getContent().length);
                preparedStatement.setTimeStamp(UPDATED_AT, new Timestamp(new Date().getTime()), CALENDAR);
                preparedStatement.setString(NAME, keyStoreModel.getName());
                preparedStatement.setInt(TENANT_ID, tenantId);
            }));
        } catch (DataAccessException e) {
            throw new SecurityException("Error while updating key store: " + keyStoreModel.getName(), e);
        }
    }

    @Override
    public void deleteKeyStore(String keyStoreName, int tenantId) throws SecurityException {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        try {
            namedJdbcTemplate.executeUpdate(DELETE_KEY_STORE, (preparedStatement -> {
                preparedStatement.setString(NAME, keyStoreName);
                preparedStatement.setInt(TENANT_ID, tenantId);
            }));
        } catch (DataAccessException e) {
            throw new SecurityException("Error while deleting key store: " + keyStoreName, e);
        }

    }

    @Override
    public Date getKeyStoreLastModifiedDate(String keyStoreName, int tenantId) {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_KEY_STORE_LAST_UPDATED_TIME, (resultSet, rowNumber) ->
                    resultSet.getTimestamp(UPDATED_AT), (preparedStatement -> {
                preparedStatement.setString(NAME, keyStoreName);
                preparedStatement.setInt(TENANT_ID, tenantId);
            }));
        } catch (DataAccessException e) {
            throw new SecurityException("Error while retrieving key store last modified date for keystore: " +
                    keyStoreName, e);
        }
    }

    @Override
    public String getEncryptedKeyStorePassword(String keyStoreName, int tenantId) throws SecurityException {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_ENCRYPTED_KEY_STORE_PASSWORD, (resultSet, rowNumber) ->
                    resultSet.getString(PASSWORD), (preparedStatement -> {
                preparedStatement.setString(NAME, keyStoreName);
                preparedStatement.setInt(TENANT_ID, tenantId);
            }));
        } catch (DataAccessException e) {
            throw new SecurityException("Error while retrieving key store password.", e);
        }
    }

    @Override
    public String getEncryptedPrivateKeyPassword(String keyStoreName, int tenantId) throws SecurityException {

        NamedJdbcTemplate namedJdbcTemplate = getNewNamedJdbcTemplate();
        try {
            return namedJdbcTemplate.fetchSingleRecord(GET_ENCRYPTED_PRIVATE_KEY_PASSWORD, (resultSet, rowNumber) ->
                    resultSet.getString(PRIVATE_KEY_PASS), (preparedStatement -> {
                preparedStatement.setString(NAME, keyStoreName);
                preparedStatement.setInt(TENANT_ID, tenantId);
            }));
        } catch (DataAccessException e) {
            throw new SecurityException("Error while retrieving private key password :" + keyStoreName, e);
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
        keyStoreModel.setPublicCertId(resultSet.getString(PUB_CERT_ID));
        return keyStoreModel;
    }

    /**
     * Get a new Named Jdbc Template. KeyStore datasource (Shared Database) is used.
     *
     * @return a new Named Jdbc Template.
     */
    private static NamedJdbcTemplate getNewNamedJdbcTemplate() {

        return new NamedJdbcTemplate(CarbonUtilsDataHolder.getInstance().getDataSource());
    }
}
