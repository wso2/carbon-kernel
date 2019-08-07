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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Support class to implement Unit of work patter.
 */
public class UnitOfWork {

    private static Log log = LogFactory.getLog(UnitOfWork.class);
    private boolean errorOccurred = false;
    private static ThreadLocal<UnitOfWorkTransactionContext> transactionContextThreadLocal = new ThreadLocal<>();

    public UnitOfWork() {

        super();
    }

    /**
     * Begin the transaction process.
     */
    public static UnitOfWork beginTransaction() {

        UnitOfWorkTransactionContext UnitOfWorkTransactionContext = transactionContextThreadLocal.get();
        if (UnitOfWorkTransactionContext == null) {
            UnitOfWorkTransactionContext = new UnitOfWorkTransactionContext();
            transactionContextThreadLocal.set(UnitOfWorkTransactionContext);
        }
        UnitOfWorkTransactionContext.incrementTransactionDepth();
        return new UnitOfWork();
    }

    /**
     * Returns an database connection.
     *
     * @param dataSource dataSource of the connection.
     * @return current connection
     * @throws SQLException
     * @Deprecated The getDBConnection should handle both transaction and non-transaction connection. Earlier it
     * handle only the transactionConnection. Therefore this method was deprecated and changed as handle both
     * transaction and non-transaction connection. getDBConnection(DataSource dataSource, boolean autoCommit) method
     * used as alternative of this method.
     */
    @Deprecated
    public Connection getDBConnection(DataSource dataSource) throws SQLException {

        return getDBConnection(dataSource, true);
    }

    /**
     * Returns an database connection.
     *
     * @param dataSource dataSource of the connection.
     * @param autoCommit autocommit state of the connection.
     * @return current connection
     * @throws SQLException
     */
    public Connection getDBConnection(DataSource dataSource, boolean autoCommit) throws SQLException {

        UnitOfWorkTransactionContext unitOfWorkTransactionContext = transactionContextThreadLocal.get();
        if (unitOfWorkTransactionContext == null) {
            throw new UnitOfWorkException("There is no transaction getting started");
        }
        Connection connection = unitOfWorkTransactionContext.getDBConnection(dataSource);

        if (!autoCommit) {
            //We need only set "autocommit==false", which indicate start of database transaction.
            connection.setAutoCommit(autoCommit);
        }
        return connection;
    }

    /**
     * End the transaction by committing to the transaction.
     */
    public void commitTransaction() {

        try {
            UnitOfWorkTransactionContext unitOfWorkTransactionContext = transactionContextThreadLocal.get();
            if (unitOfWorkTransactionContext == null) {
                throw new UnitOfWorkException("There is no transaction getting started");

            }
            unitOfWorkTransactionContext.decrementTransactionDepth();
            if (unitOfWorkTransactionContext.getTransactionDepth() == 0 && !errorOccurred) {
                unitOfWorkTransactionContext.commitAllConnection();
            }
        } catch (SQLException e) {
            log.error("Error occurred while commit connection", e);
        }
    }

    /**
     * Revoke the transaction when catch then sql transaction errors.
     */
    public void rollbackTransaction() {

        try {
            errorOccurred = true;
            UnitOfWorkTransactionContext unitOfWorkTransactionContext = transactionContextThreadLocal.get();
            if (unitOfWorkTransactionContext == null) {
                throw new UnitOfWorkException("There is no transaction getting started");

            }
            unitOfWorkTransactionContext.decrementTransactionDepth();
            if (unitOfWorkTransactionContext.getTransactionDepth() == 0 && errorOccurred) {
                unitOfWorkTransactionContext.rollbackAllConnection();
            }
        } catch (SQLException e) {
            log.error("Error occurred while rollback connection", e);
        }

    }

    /**
     * close all the remaining transaction and the connections
     */
    public void closeTransaction() {

        try {
            UnitOfWorkTransactionContext unitOfWorkTransactionContext = transactionContextThreadLocal.get();
            if (unitOfWorkTransactionContext == null) {
                throw new UnitOfWorkException("There is no transaction getting started");
            }
            unitOfWorkTransactionContext.closeConnection();
            transactionContextThreadLocal.remove();

        } catch (SQLException e) {
            log.error("Error occurred while close all the transaction and connection", e);
        }
    }

}
