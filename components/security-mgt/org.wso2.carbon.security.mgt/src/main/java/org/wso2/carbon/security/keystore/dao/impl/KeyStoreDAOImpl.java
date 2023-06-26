package org.wso2.carbon.security.keystore.dao.impl;

import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.security.SecurityConfigException;
import org.wso2.carbon.security.keystore.dao.KeyStoreDAO;
import org.wso2.carbon.security.keystore.dao.constants.KeyStoreDAOConstants;
import org.wso2.carbon.security.keystore.dao.constants.KeyStoreDAOConstants.KeyStoreTableColumns;
import org.wso2.carbon.security.keystore.model.KeyStoreModel;

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

import static org.wso2.carbon.security.keystore.dao.DAOUtils.getTenantUUID;

import static java.time.ZoneOffset.UTC;

public class KeyStoreDAOImpl extends KeyStoreDAO {

    private final String tenantUUID;
    private static final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(UTC));

    public KeyStoreDAOImpl(int tenantId) throws SecurityConfigException {
        super(tenantId);
        this.tenantUUID = getTenantUUID(tenantId);
    }

    @Override
    public void addKeyStore(KeyStoreModel keyStoreModel) throws SecurityConfigException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                processAddKeyStore(connection, keyStoreModel);
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                // TODO: Check whether this exception type is okay. Also see if we need to use a server exception type. i.e something like SecurityConfigServerException
                throw new SecurityConfigException("Error while adding key store.", e);
            }
        } catch (SQLException e) {
            // TODO: Check whether this exception type is okay. Also see if we need to use a server exception type. i.e something like SecurityConfigServerException
            throw new SecurityConfigException("Error while adding key store.", e);
        }
    }

    @Override
    public List<KeyStoreModel> getKeyStores() throws SecurityConfigException {

        List<KeyStoreModel> keyStores = new ArrayList<>();

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    KeyStoreDAOConstants.SqlQueries.GET_KEY_STORES)) {
                statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        keyStores.add(mapResultToKeyStoreModel(resultSet));
                    }
                }
            } catch (SQLException e) {
                throw new SecurityConfigException("Error while retrieving key stores.", e);
            }
        } catch (SQLException e) {
            throw new SecurityConfigException("Error while retrieving key stores.", e);
        }

        return keyStores;
    }

    @Override
    public Optional<KeyStoreModel> getKeyStore(String fileName) throws SecurityConfigException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
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
                throw new SecurityConfigException("Error while retrieving key stores.", e);
            }
        } catch (SQLException e) {
            throw new SecurityConfigException("Error while retrieving key stores.", e);
        }
        return Optional.empty();
    }

    @Override
    public void deleteKeyStore(String fileName) throws SecurityConfigException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    KeyStoreDAOConstants.SqlQueries.DELETE_KEY_STORE_BY_FILE_NAME)) {
                statement.setString(KeyStoreTableColumns.FILE_NAME, fileName);
                statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
                statement.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new SecurityConfigException("Error while deleting key store.", e);
            }
        } catch (SQLException e) {
            throw new SecurityConfigException("Error while deleting key store.", e);
        }
    }

    @Override
    public void updateKeyStore(KeyStoreModel keyStoreModel) throws SecurityConfigException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                processUpdateKeyStore(connection, keyStoreModel);
                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                // TODO: Check whether this exception type is okay. Also see if we need to use a server exception type. i.e something like SecurityConfigServerException
                throw new SecurityConfigException("Error while updating key store.", e);
            }
        } catch (SQLException e) {
            // TODO: Check whether this exception type is okay. Also see if we need to use a server exception type. i.e something like SecurityConfigServerException
            throw new SecurityConfigException("Error while updating key store.", e);
        }
    }

    @Override
    public void addPubCertIdToKeyStore(String fileName, String pubCertId) throws SecurityConfigException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    KeyStoreDAOConstants.SqlQueries.ADD_PUB_CERT_ID_TO_KEY_STORE)) {
                statement.setString(KeyStoreTableColumns.PUB_CERT_ID, pubCertId);
                statement.setTimeStamp(KeyStoreTableColumns.LAST_UPDATED, new Timestamp(new Date().getTime()), calendar);
                statement.setString(KeyStoreTableColumns.FILE_NAME, fileName);
                statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
                statement.executeUpdate();

                IdentityDatabaseUtil.commitTransaction(connection);
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new SecurityConfigException("Error while linking public certificate to key store.", e);
            }
        } catch (SQLException e) {
            throw new SecurityConfigException("Error while linking public certificate to key store.", e);
        }
    }

    @Override
    public Optional<String> getPubCertIdFromKeyStore(String fileName) throws SecurityConfigException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
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
                throw new SecurityConfigException("Error while retrieving public certificate of key store.", e);
            }
        } catch (SQLException e) {
            throw new SecurityConfigException("Error while retrieving public certificate of key store.", e);
        }
        return Optional.empty();
    }

    private KeyStoreModel mapResultToKeyStoreModel(ResultSet resultSet) throws SQLException {

        KeyStoreModel keyStoreModel = new KeyStoreModel();
        keyStoreModel.setType(resultSet.getString(KeyStoreTableColumns.TYPE));
        keyStoreModel.setProvider(resultSet.getString(KeyStoreTableColumns.PROVIDER));
        keyStoreModel.setFileName(resultSet.getString(KeyStoreTableColumns.FILE_NAME));
        keyStoreModel.setPassword(resultSet.getString(KeyStoreTableColumns.PASSWORD));
        keyStoreModel.setPrivateKeyAlias(resultSet.getString(KeyStoreTableColumns.PRIVATE_KEY_ALIAS));
        keyStoreModel.setPrivateKeyPass(resultSet.getString(KeyStoreTableColumns.PRIVATE_KEY_PASS));
        keyStoreModel.setContent(resultSet.getBytes(KeyStoreTableColumns.CONTENT));
        keyStoreModel.setLastUpdated(resultSet.getTimestamp(KeyStoreTableColumns.LAST_UPDATED));
        return keyStoreModel;
    }

    private String processAddKeyStore(Connection connection, KeyStoreModel keyStoreModel)
            throws SQLException {

        String id = UUID.randomUUID().toString();

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                KeyStoreDAOConstants.SqlQueries.ADD_KEY_STORE)) {
            statement.setString(KeyStoreTableColumns.ID, id);
            statement.setString(KeyStoreTableColumns.FILE_NAME, keyStoreModel.getFileName());
            statement.setString(KeyStoreTableColumns.TYPE, keyStoreModel.getType());
            statement.setString(KeyStoreTableColumns.PROVIDER, keyStoreModel.getProvider());
            statement.setString(KeyStoreTableColumns.PASSWORD, keyStoreModel.getPassword());
            statement.setString(KeyStoreTableColumns.PRIVATE_KEY_ALIAS, keyStoreModel.getPrivateKeyAlias());
            statement.setString(KeyStoreTableColumns.PRIVATE_KEY_PASS, keyStoreModel.getPrivateKeyPass());
            statement.setString(KeyStoreTableColumns.TENANT_UUID, tenantUUID);
            statement.setTimeStamp(KeyStoreTableColumns.LAST_UPDATED, new Timestamp(new Date().getTime()), calendar);
            statement.setBytes(10, keyStoreModel.getContent());
            statement.executeUpdate();
        }
        return id;
    }

    private void processUpdateKeyStore(Connection connection, KeyStoreModel keyStoreModel)
            throws SQLException {

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                KeyStoreDAOConstants.SqlQueries.UPDATE_KEY_STORE_BY_FILE_NAME)) {
            statement.setString(1, keyStoreModel.getType());
            statement.setString(2, keyStoreModel.getProvider());
            statement.setString(3, keyStoreModel.getPassword());
            statement.setString(4, keyStoreModel.getPrivateKeyAlias());
            statement.setString(5, keyStoreModel.getPrivateKeyPass());
            statement.setTimestamp(6, new Timestamp(new Date().getTime()), calendar);
            statement.setBytes(7, keyStoreModel.getContent());
            statement.setString(8, keyStoreModel.getFileName());
            statement.setString(9, tenantUUID);
            statement.executeUpdate();
        }
    }
}
