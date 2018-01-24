/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.user.core.common;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.privacy.IdManager;
import org.wso2.carbon.privacy.Identifiable;
import org.wso2.carbon.privacy.exception.IdManagerException;
import org.wso2.carbon.user.core.model.UserImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;

/**
 * Default implementation of the User ID manager.
 */
public class JDBCUserIdManager implements IdManager {

    private DataSource dataSource;

    public JDBCUserIdManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String getNameFromId(String id) throws IdManagerException {

        if (StringUtils.isEmpty(id)) {
            throw new IdManagerException("Id cannot be empty.");
        }

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLQueries.GET_NAME_FROM_ID);
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(TableColumns.ID);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new IdManagerException(e);
        }
    }

    @Override
    public String getIdFromName(String name) throws IdManagerException {

        if (StringUtils.isEmpty(name)) {
            throw new IdManagerException("Name cannot be empty.");
        }

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLQueries.GET_ID_FROM_NAME);
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString(TableColumns.NAME);
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new IdManagerException(e);
        }
    }

    @Override
    public Identifiable getIdentifiableFromName(String s) throws IdManagerException {

        return null;
    }

    @Override
    public Identifiable getIdentifiableFromId(String s) throws IdManagerException {

        return null;
    }

    @Override
    public Identifiable addIdForName(Identifiable identifiable) throws IdManagerException {

        if (StringUtils.isEmpty(identifiable.getName())) {
            throw new IdManagerException("Identifiable name cannot be empty.");
        }

        String name = identifiable.getName();
        String id;

        if (StringUtils.isEmpty(identifiable.getId())) {
            id = UUID.randomUUID().toString();
        } else {
            id = identifiable.getId();
        }

        try {
            if (checkIdExist(id)) {
                throw new IdManagerException("Given Id: " + id + " already exist.");
            }
            if (checkNameExist(name)) {
                throw new IdManagerException("Provided user name already exist with an Id.");
            }
        } catch (SQLException e) {
            throw new IdManagerException("Error occurred while performing pre checks.", e);
        }

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLQueries.ADD_ID_FOR_NAME);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.execute();

            return new UserImpl(id, name);
        } catch (SQLException e) {
            throw new IdManagerException(e);
        }
    }

    @Override
    public void removeIdForName(Identifiable identifiable) throws IdManagerException {

        if (StringUtils.isEmpty(identifiable.getName()) && StringUtils.isEmpty(identifiable.getId())) {
            throw new IdManagerException("Identifiable name or id should present.");
        }

        String name = identifiable.getName();
        String id = identifiable.getId();

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLQueries.DELETE_RECORD_FROM_NAME_OR_ID);
            preparedStatement.setString(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new IdManagerException(e);
        }
    }

    private boolean checkNameExist(String name) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLQueries.CHECK_NAME_EXIST);
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean checkIdExist(String id) throws SQLException {

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(SQLQueries.CHECK_ID_EXIST);
            preparedStatement.setString(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static final class SQLQueries {

        private static final String ADD_ID_FOR_NAME = "";
        private static final String CHECK_NAME_EXIST = "";
        private static final String CHECK_ID_EXIST = "";
        private static final String DELETE_RECORD_FROM_NAME_OR_ID = "";
        private static final String GET_NAME_FROM_ID = "";
        private static final String GET_ID_FROM_NAME = "";
    }

    private static final class TableColumns {

        private static final String ID = "";
        public static final String NAME = "";
    }
}
