/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.user.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.sql.DataSource;

public class UnitOfWorkTransactionContext {

    private static Log log = LogFactory.getLog(UnitOfWorkTransactionContext.class);
    private HashMap<DataSource, Connection> activeConnection = new HashMap();
    private int transactionDepth = 0;

    /**
     * Commit all the transaction, if something happened at the middle then it print all the commit and uncommitted
     * transaction.
     */
    public void commitAllConnection() {

        List<DataSource> listOfDataSource = new ArrayList<>();
        for (DataSource dataSource : activeConnection.keySet()) {
            try {
                Connection connection = activeConnection.get(dataSource);
                if (connection != null) {
                    listOfDataSource.add(dataSource);
                    connection.commit();
                }
            } catch (SQLException e) {

                log.error("Error occurred while committing the connection. Connection: " + activeConnection
                        .get(dataSource) + ". We have committed few transaction before error occurs. Committed "
                        + "dataSource are: " + listOfDataSource.toString(), e);
            }
        }
    }

    /**
     * Rollback all the transaction, if something happened at the middle then it will continue and rollback all the
     * connection as much as possible.
     */
    public void rollbackAllConnection() {
        for (DataSource dataSource : activeConnection.keySet()) {
            try {
                Connection connection = activeConnection.get(dataSource);
                if (connection != null) {
                    connection.rollback();
                }
            } catch (SQLException e) {
                log.error("Error occurred while rollback the connection for dataSource: " + dataSource, e);
                continue;
            }
        }
    }

    /**
     * Set and return the connection.
     *
     * @param dataSource dataSource of the connection
     * @return the current connection
     * @throws SQLException
     */
    public Connection getDBConnection(DataSource dataSource) throws SQLException {

        Connection currentConnectionForDataSource = activeConnection.get(dataSource);
        if (currentConnectionForDataSource == null) {
            currentConnectionForDataSource = dataSource.getConnection();
            activeConnection.put(dataSource, currentConnectionForDataSource);
        }
        return currentConnectionForDataSource;
    }

    /**
     * Get the transaction level.
     *
     * @return transaction level
     */
    public int getTransactionDepth() {

        return transactionDepth;
    }

    /**
     * Increment the transaction depth by one to store the level of a transaction.
     */
    public void incrementTransactionDepth() {

        transactionDepth++;
    }

    /**
     * Decrement the transaction depth by one to notice the remaining levels of a transaction.
     */
    public void decrementTransactionDepth() {

        transactionDepth--;
    }

    /**
     * Close all the db connection.
     */
    public void closeConnection() {

        for (DataSource dataSource : activeConnection.keySet()) {
            Connection connection = activeConnection.get(dataSource);
            try {
                connection.close();
            } catch (SQLException e) {
                log.error("Error occurred while close the connection:  " + connection, e);
                continue;
            }
        }
    }
}
