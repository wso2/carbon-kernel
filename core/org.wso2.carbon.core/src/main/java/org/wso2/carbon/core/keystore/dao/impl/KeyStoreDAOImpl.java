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

package org.wso2.carbon.core.keystore.dao.impl;

import org.wso2.carbon.core.internal.KeyStoreManagerDataHolder;
import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.core.keystore.KeyStoreManagementException;
import org.wso2.carbon.core.keystore.dao.KeyStoreDAO;
import org.wso2.carbon.core.keystore.dao.constants.KeyStoreDAOConstants;
import org.wso2.carbon.core.keystore.dao.constants.KeyStoreDAOConstants.KeyStoreTableColumns;
import org.wso2.carbon.core.keystore.model.KeyStoreModel;

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
import java.util.UUID;

import javax.sql.DataSource;

import static java.time.ZoneOffset.UTC;

/**
 * This class provides the implementation of the KeyStoreDAO interface.
 */
public class KeyStoreDAOImpl implements KeyStoreDAO {

    private final Calendar CALENDAR = Calendar.getInstance(TimeZone.getTimeZone(UTC));
    private final DataSource dataSource;

    public KeyStoreDAOImpl() {

        this.dataSource = KeyStoreManagerDataHolder.getDataSource();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addKeyStore(String tenantUUID, KeyStoreModel keyStoreModel) throws KeyStoreManagementException {

        try (Connection connection = DatabaseUtil.getDBConnection(this.dataSource)) {
            try {
                processAddKeyStore(connection, keyStoreModel, tenantUUID);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_ADD_KEY_STORE, e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_CANNOT_RETRIEVE_DB_CONN, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<KeyStoreModel> getKeyStores(String tenantUUID) throws KeyStoreManagementException {

        List<KeyStoreModel> keyStores = new ArrayList<>();

        try (Connection connection = DatabaseUtil.getDBConnection(this.dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    KeyStoreDAOConstants.SqlQueries.GET_KEY_STORES)) {
                statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        keyStores.add(mapResultToKeyStoreModel(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_GET_KEY_STORES, e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_CANNOT_RETRIEVE_DB_CONN, e);
        }

        return keyStores;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<KeyStoreModel> getKeyStore(String tenantUUID, String fileName) throws KeyStoreManagementException {

        try (Connection connection = DatabaseUtil.getDBConnection(this.dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    KeyStoreDAOConstants.SqlQueries.GET_KEY_STORE_BY_FILE_NAME)) {
                statement.setString(KeyStoreTableColumns.FILE_NAME, fileName);
                statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapResultToKeyStoreModel(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_GET_KEY_STORE, e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_CANNOT_RETRIEVE_DB_CONN, e);
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteKeyStore(String tenantUUID, String fileName) throws KeyStoreManagementException {

        try (Connection connection = DatabaseUtil.getDBConnection(this.dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    KeyStoreDAOConstants.SqlQueries.DELETE_KEY_STORE_BY_FILE_NAME)) {
                statement.setString(KeyStoreTableColumns.FILE_NAME, fileName);
                statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
                statement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new KeyStoreManagementException(
                        KeyStoreDAOConstants.ErrorMessages.ERROR_DELETE_KEY_STORE_BY_FILE_NAME, e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_CANNOT_RETRIEVE_DB_CONN, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateKeyStore(String tenantUUID, KeyStoreModel keyStoreModel) throws KeyStoreManagementException {

        try (Connection connection = DatabaseUtil.getDBConnection(this.dataSource)) {
            try {
                processUpdateKeyStore(connection, keyStoreModel, tenantUUID);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_UPDATE_KEY_STORE, e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_CANNOT_RETRIEVE_DB_CONN, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPubCertIdToKeyStore(String tenantUUID, String fileName, String pubCertId)
            throws KeyStoreManagementException {

        try (Connection connection = DatabaseUtil.getDBConnection(this.dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    KeyStoreDAOConstants.SqlQueries.ADD_PUB_CERT_ID_TO_KEY_STORE)) {
                statement.setString(KeyStoreTableColumns.PUB_CERT_ID, pubCertId);
                statement.setTimeStamp(KeyStoreTableColumns.LAST_UPDATED, new Timestamp(new Date().getTime()),
                        CALENDAR);
                statement.setString(KeyStoreTableColumns.FILE_NAME, fileName);
                statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
                statement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages
                        .ERROR_LINK_PUB_CERT_TO_KEY_STORE, e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_CANNOT_RETRIEVE_DB_CONN, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getPubCertIdFromKeyStore(String tenantUUID, String fileName)
            throws KeyStoreManagementException {

        try (Connection connection = DatabaseUtil.getDBConnection(this.dataSource)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    KeyStoreDAOConstants.SqlQueries.GET_PUB_CERT_ID_OF_KEY_STORE)) {
                statement.setString(KeyStoreTableColumns.FILE_NAME, fileName);
                statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.ofNullable(resultSet.getString(KeyStoreTableColumns.PUB_CERT_ID));
                    }
                }
            } catch (SQLException e) {
                throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages
                        .ERROR_GET_PUB_CERT_OF_KEY_STORE, e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(KeyStoreDAOConstants.ErrorMessages.ERROR_CANNOT_RETRIEVE_DB_CONN, e);
        }
        return Optional.empty();
    }

    private KeyStoreModel mapResultToKeyStoreModel(ResultSet resultSet) throws SQLException {

        return new KeyStoreModel.KeyStoreModelBuilder()
                .type(resultSet.getString(KeyStoreTableColumns.TYPE))
                .provider(resultSet.getString(KeyStoreTableColumns.PROVIDER))
                .fileName(resultSet.getString(KeyStoreTableColumns.FILE_NAME))
                .password(resultSet.getString(KeyStoreTableColumns.PASSWORD).toCharArray())
                .privateKeyAlias(resultSet.getString(KeyStoreTableColumns.PRIVATE_KEY_ALIAS))
                .privateKeyPass(resultSet.getString(KeyStoreTableColumns.PRIVATE_KEY_PASS).toCharArray())
                .content(resultSet.getBytes(KeyStoreTableColumns.CONTENT))
                .lastUpdated(resultSet.getTimestamp(KeyStoreTableColumns.LAST_UPDATED))
                .build();
    }

    private void processAddKeyStore(Connection connection, KeyStoreModel keyStoreModel, String tenantUUID)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                KeyStoreDAOConstants.SqlQueries.ADD_KEY_STORE)) {
            statement.setString(KeyStoreTableColumns.ID, UUID.randomUUID().toString());
            statement.setString(KeyStoreTableColumns.FILE_NAME, keyStoreModel.getFileName());
            statement.setString(KeyStoreTableColumns.TYPE, keyStoreModel.getType());
            statement.setString(KeyStoreTableColumns.PROVIDER, keyStoreModel.getProvider());
            statement.setString(KeyStoreTableColumns.PASSWORD, String.valueOf(keyStoreModel.getPassword()));
            // todo: check whether are we storing a null or an empty string when the field is not set?
            statement.setString(KeyStoreTableColumns.PRIVATE_KEY_ALIAS, keyStoreModel.getPrivateKeyAlias());
            statement.setString(KeyStoreTableColumns.PRIVATE_KEY_PASS,
                    String.valueOf(keyStoreModel.getPrivateKeyPass()));
            statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
            statement.setTimeStamp(KeyStoreTableColumns.LAST_UPDATED, new Timestamp(new Date().getTime()), CALENDAR);
            statement.setBytes(10, keyStoreModel.getContent());
            statement.executeUpdate();
        }
    }

    private void processUpdateKeyStore(Connection connection, KeyStoreModel keyStoreModel, String tenantUUID)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                KeyStoreDAOConstants.SqlQueries.UPDATE_KEY_STORE_BY_FILE_NAME)) {
            statement.setString(KeyStoreTableColumns.TYPE, keyStoreModel.getType());
            statement.setString(KeyStoreTableColumns.PROVIDER, keyStoreModel.getProvider());
            statement.setString(KeyStoreTableColumns.PASSWORD, String.valueOf(keyStoreModel.getPassword()));
            statement.setString(KeyStoreTableColumns.PRIVATE_KEY_ALIAS, keyStoreModel.getPrivateKeyAlias());
            statement.setString(KeyStoreTableColumns.PRIVATE_KEY_PASS,
                    String.valueOf(keyStoreModel.getPrivateKeyPass()));
            statement.setTimeStamp(KeyStoreTableColumns.LAST_UPDATED, new Timestamp(new Date().getTime()), CALENDAR);
            statement.setBytes(7, keyStoreModel.getContent());
            statement.setString(KeyStoreTableColumns.FILE_NAME, keyStoreModel.getFileName());
            statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
            statement.executeUpdate();
        }
    }
}
