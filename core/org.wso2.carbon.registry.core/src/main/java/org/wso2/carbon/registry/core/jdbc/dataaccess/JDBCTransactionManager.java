/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.jdbc.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.dataaccess.TransactionManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * An implementation of {@link TransactionManager} to manage a back-end JDBC-based transactions.
 */
public class JDBCTransactionManager implements TransactionManager {

    private DataAccessManager dataAccessManager;
    private static final Log log = LogFactory.getLog(TransactionManager.class);

    /**
     * Creates a JDBC Transaction manager using the given data access manager.
     * @param dataAccessManager the data access manager to use.
     */
    public JDBCTransactionManager(DataAccessManager dataAccessManager) {
        this.dataAccessManager = dataAccessManager;
    }

    public void beginTransaction() throws RegistryException {
        if (dataAccessManager.getDatabaseTransaction().getNestedDepth() != 0) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "The transaction was not started, because it is called within a " +
                                "transaction, nested depth: " +
                                dataAccessManager.getDatabaseTransaction().getNestedDepth() + ".");
            }
            dataAccessManager.getDatabaseTransaction().incNestedDepth();
            if (JDBCDatabaseTransaction.getConnection() != null) {
                return;
            } else {
                // If we get here, there has been some issue related to connection failure.
                // There is no point in using the connection that is already existing on this
                // thread. We need to start using a new one.
                while (dataAccessManager.getDatabaseTransaction().getNestedDepth() > 0) {
                    dataAccessManager.getDatabaseTransaction().decNestedDepth();
                }
                while (dataAccessManager.getDatabaseTransaction().getNestedDepth() < 0) {
                    dataAccessManager.getDatabaseTransaction().incNestedDepth();
                }
            }
        }

        Connection conn = null;
        try {
            if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
                String msg = "Failed to begin transaction. Invalid data access manager.";
                log.error(msg);
                throw new RegistryException(msg);
            }
            DataSource dataSource = ((JDBCDataAccessManager)dataAccessManager).getDataSource();
            conn = dataSource.getConnection();

            // If a managed connection already exists, use that instead of a new connection.
            JDBCDatabaseTransaction.ManagedRegistryConnection temp =
                    JDBCDatabaseTransaction.getManagedRegistryConnection(conn);
            if (temp != null) {
                conn.close();
                conn = temp;
            }
            if (conn.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }
            conn.setAutoCommit(false);
            dataAccessManager.getDatabaseTransaction().incNestedDepth();
        } catch (SQLException e) {
            String msg = "Failed to start new registry transaction.";
            log.error(msg, e);
            if(conn!=null){
                try {
                    conn.close();
                } catch (SQLException e1) {
                    String msgFailed = "Failed to close connection.";
                    log.error(msgFailed, e);
                }
            }
            throw new RegistryException(msg, e);
        }

        JDBCDatabaseTransaction.setConnection(conn);
    }

    public void rollbackTransaction() throws RegistryException {
        dataAccessManager.getDatabaseTransaction().setRollbacked(true);
        if (dataAccessManager.getDatabaseTransaction().getNestedDepth() != 1) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "The transaction was not rollbacked, because it is called within a " +
                                "transaction, nested depth: " +
                                dataAccessManager.getDatabaseTransaction().getNestedDepth() + ".");
            }

            dataAccessManager.getDatabaseTransaction().decNestedDepth();
            return;
        }

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        try {
            conn.rollback();

        } catch (SQLException e) {
            String msg = "Failed to rollback transaction.";
            log.error(msg, e);
            throw new RegistryException(msg, e);

        } finally {
            endTransaction();
            dataAccessManager.getDatabaseTransaction().decNestedDepth();
        }
    }

    public void commitTransaction() throws RegistryException {
        if (dataAccessManager.getDatabaseTransaction().getNestedDepth() != 1) {
            if (log.isTraceEnabled()) {
                log.trace(
                        "The transaction was not committed, because it is called within a " +
                                "transaction, nested depth: " +
                                dataAccessManager.getDatabaseTransaction().getNestedDepth() + ".");
            }
            dataAccessManager.getDatabaseTransaction().decNestedDepth();
            return;
        }

        if (dataAccessManager.getDatabaseTransaction().isRollbacked()) {
            String msg =
                    "The transaction is already rollbacked, you can not commit a transaction " +
                            "already rollbacked, nested depth: " +
                            dataAccessManager.getDatabaseTransaction().getNestedDepth() +
                            ".";
            log.debug(msg);
            dataAccessManager.getDatabaseTransaction().decNestedDepth();
            throw new RegistryException(msg);
        }

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        try {
            conn.commit();

        } catch (SQLException e) {
            String msg = "Failed to commit transaction.";
            log.error(msg, e);
            throw new RegistryException(msg, e);

        } finally {
            endTransaction();
            dataAccessManager.getDatabaseTransaction().decNestedDepth();
        }
    }

    private void endTransaction() throws RegistryException {

        if (Transaction.isStarted()) {
            JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                    JDBCDatabaseTransaction.getConnection();
            try {
                conn.close();

            } catch (SQLException e) {
                String msg = "Failed to close transaction.";
                log.error(msg, e);
                throw new RegistryException(msg, e);

            } finally {
                dataAccessManager.getDatabaseTransaction().setStarted(false);
                JDBCDatabaseTransaction.setConnection(null);
            }
        }
    }

}
