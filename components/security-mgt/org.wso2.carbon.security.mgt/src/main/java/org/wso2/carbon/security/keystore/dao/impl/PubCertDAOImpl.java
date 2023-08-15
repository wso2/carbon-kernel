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

package org.wso2.carbon.security.keystore.dao.impl;

import org.wso2.carbon.database.utils.jdbc.NamedPreparedStatement;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.security.keystore.KeyStoreManagementException;
import org.wso2.carbon.security.keystore.dao.PubCertDAO;
import org.wso2.carbon.security.keystore.dao.constants.PubCertDAOConstants;
import org.wso2.carbon.security.keystore.dao.constants.PubCertDAOConstants.PubCertTableColumns;
import org.wso2.carbon.security.keystore.model.PubCertModel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * This class provides the implementation of the PubCertDAO interface.
 */
public class PubCertDAOImpl implements PubCertDAO {

    private static final String DB_CONN_RETRIEVAL_ERROR_MSG = "Error while getting the DB connection.";

    public PubCertDAOImpl() {
        // Default constructor.
    }

    @Override
    public String addPubCert(String tenantUUID, PubCertModel pubCertModel) throws KeyStoreManagementException {

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(true)) {
            try {
                String uuid = processAddPubCert(connection, pubCertModel, tenantUUID);
                IdentityDatabaseUtil.commitTransaction(connection);
                return uuid;
            } catch (SQLException e) {
                IdentityDatabaseUtil.rollbackTransaction(connection);
                throw new KeyStoreManagementException("Error while adding public certificate.", e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(DB_CONN_RETRIEVAL_ERROR_MSG, e);
        }
    }

    @Override
    public Optional<PubCertModel> getPubCert(String tenantUUID, String uuid) throws KeyStoreManagementException {

        PubCertModel pubCertModel = null;

        try (Connection connection = IdentityDatabaseUtil.getDBConnection(false)) {
            try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                    PubCertDAOConstants.SQLQueries.GET_PUB_CERT)) {
                statement.setString(PubCertTableColumns.ID, uuid);
                statement.setString(PubCertTableColumns.TENANT_UUID, tenantUUID);
                // T
                statement.setMaxRows(1);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        pubCertModel = new PubCertModel();
                        pubCertModel.setFileNameAppender(resultSet.getString(PubCertTableColumns.FILE_NAME_APPENDER));
                        pubCertModel.setContent(resultSet.getBytes(PubCertTableColumns.CONTENT));
                    }
                }
            } catch (SQLException e) {
                throw new KeyStoreManagementException("Error while retrieving notification template types.", e);
            }
        } catch (SQLException e) {
            throw new KeyStoreManagementException(DB_CONN_RETRIEVAL_ERROR_MSG, e);
        }
        return Optional.ofNullable(pubCertModel);
    }

    private String processAddPubCert(Connection connection, PubCertModel pubCertModel, String tenantUUID)
            throws SQLException {

        String id = UUID.randomUUID().toString();

        try (NamedPreparedStatement statement = new NamedPreparedStatement(connection,
                PubCertDAOConstants.SQLQueries.ADD_PUB_CERT)) {
            statement.setString(PubCertTableColumns.ID, id);
            statement.setString(PubCertTableColumns.FILE_NAME_APPENDER, pubCertModel.getFileNameAppender());
            statement.setString(PubCertTableColumns.TENANT_UUID, tenantUUID);
            statement.setBytes(4, pubCertModel.getContent());
            statement.executeUpdate();
        }
        return id;
    }
}
