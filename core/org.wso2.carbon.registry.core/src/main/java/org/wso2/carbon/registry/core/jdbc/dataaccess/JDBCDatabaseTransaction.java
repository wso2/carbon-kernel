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
import org.wso2.carbon.registry.core.dataaccess.DatabaseTransaction;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.statistics.StatisticsLog;
import org.wso2.carbon.registry.core.statistics.query.DBQueryStatisticsLog;
import org.wso2.carbon.registry.core.statistics.query.StatisticsRecord;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class represents a database transaction, which is used for databases that support JDBC.
 */
public class JDBCDatabaseTransaction implements DatabaseTransaction {

    private static final Log log = LogFactory.getLog(Transaction.class);

    // The instance of the logger to be used to log statistics.
    private static final Log statsLog = StatisticsLog.getLog();

    // The instance of the logger to be used to log database query statistics.
    private static final Log dbQueryLog = DBQueryStatisticsLog.getLog();

    private static ThreadLocal<TransactionEntry> tCurrent =
            new ThreadLocal<TransactionEntry>() {
                protected TransactionEntry initialValue() {
                    return new TransactionEntry();
                }
            };

    private static ThreadLocal<Stack<TransactionEntry>> tStackedTransactionalEntryStack =
            new ThreadLocal<Stack<TransactionEntry>>() {
                protected Stack<TransactionEntry> initialValue() {
                    Stack<TransactionEntry> StackedTransactionalEntryStack =
                            new Stack<TransactionEntry>();
                    StackedTransactionalEntryStack.push(null);
                    return StackedTransactionalEntryStack;
                }
            };

    // Method to obtain stacked transactional connection.
    private static TransactionEntry getStackedTransactionalEntry() {
        Stack<TransactionEntry> transactionalEntryStack =
                tStackedTransactionalEntryStack.get();
        if (transactionalEntryStack == null) {
            tStackedTransactionalEntryStack.remove();
            transactionalEntryStack = tStackedTransactionalEntryStack.get();
        }
        return transactionalEntryStack.peek();
    }

    // Method to set stacked transactional connection.
    private static void setStackedTransactionalEntry(
            TransactionEntry StackedTransactionalEntry) {
        Stack<TransactionEntry> transactionalEntryStack =
                tStackedTransactionalEntryStack.get();
        if (transactionalEntryStack == null) {
            tStackedTransactionalEntryStack.remove();
            transactionalEntryStack = tStackedTransactionalEntryStack.get();
        }
        transactionalEntryStack.push(StackedTransactionalEntry);
    }

    // Method to remove stacked transactional connection.
    private static void removeStackedTransactionalEntry() {
        Stack<TransactionEntry> transactionalEntryStack =
                tStackedTransactionalEntryStack.get();
        if (transactionalEntryStack != null) {
            transactionalEntryStack.pop();
        }
    }

    public void pushTransaction() {
        // This method will stack the current transaction by creating a stacked transactional
        // connection object and then begin the new transaction.
        log.trace("pushing current transaction to stack");
        setStackedTransactionalEntry(tCurrent.get());
        tCurrent.set(new TransactionEntry());
    }

    public void popTransaction() {
        log.trace("popping current transaction from stack");
        TransactionEntry transactionEntry =
                getStackedTransactionalEntry();
        if (transactionEntry != null) {
            removeStackedTransactionalEntry();
            tCurrent.set(transactionEntry);
        }
    }

    public boolean isStarted() {
        if (tCurrent.get() != null) {
            return tCurrent.get().isStarted();
        } else {
            log.error("The current transaction entry has not been created.");
            return false;
        }
    }

    public void setStarted(boolean started) {
        if (tCurrent.get() != null) {
            tCurrent.get().setStarted(started);
        } else {
            log.error("The current transaction entry has not been created.");
        }
    }

    /**
     * Method to get connection.
     *
     * @return the connection.
     */
    public static ManagedRegistryConnection getConnection() {
        if (tCurrent.get() != null) {
            return tCurrent.get().getConnection();
        } else {
            log.error("The current transaction entry has not been created.");
            return null;
        }
    }

    /**
     * Method to set connection.
     *
     * @param connection the connection.
     */
    public static void setConnection(Connection connection) {
        if (tCurrent.get() != null) {
            if (connection != null) {
                tCurrent.get().setStarted(true);
                tCurrent.get().setConnection(new ManagedRegistryConnection(connection));
            } else {
                tCurrent.get().setConnection(null);
            }
        } else {
            log.error("The current transaction entry has not been created.");
        }
    }

    /**
     * Method to remove the connection from the transaction.
     */
    @SuppressWarnings("unused")
    public static void removeConnection() {
        if (tCurrent.get() != null) {
            tCurrent.get().setStarted(false);
            tCurrent.get().setConnection(null);
        } else {
            log.error("The current transaction entry has not been created.");
        }
    }

    public void incNestedDepth() {
        if (tCurrent.get() != null) {
            int transactionDepth = tCurrent.get().getNestedDepth();
            if (transactionDepth == 0) {
                tCurrent.get().setStarted(true);
                tCurrent.get().setRollbacked(false);
            }
            transactionDepth++;
            tCurrent.get().setNestedDepth(transactionDepth);
        } else {
            log.error("The current transaction entry has not been created.");
        }
    }

    public void decNestedDepth() {
        if (tCurrent.get() != null) {
            int transactionDepth = tCurrent.get().getNestedDepth();
            transactionDepth--;
            tCurrent.get().setNestedDepth(transactionDepth);
            if (transactionDepth == 0) {
                tCurrent.get().setStarted(false);
            }
        } else {
            log.error("The current transaction entry has not been created.");
        }
    }

    public int getNestedDepth() {
        if (tCurrent.get() != null) {
            return tCurrent.get().getNestedDepth();
        } else {
            log.error("The current transaction entry has not been created.");
            return 0;
        }
    }

    public boolean isRollbacked() {
        if (tCurrent.get() != null) {
            return tCurrent.get().isRollbacked();
        } else {
            log.error("The current transaction entry has not been created.");
            return false;
        }
    }

    public void setRollbacked(boolean rollbacked) {
        if (tCurrent.get() != null) {
            tCurrent.get().setRollbacked(rollbacked);
        } else {
            log.error("The current transaction entry has not been created.");
        }
    }

    /**
     * Method to obtain a connection that is managed by the registry transactions implementation.
     * If the managed transaction is already closed, committed or rollbacked, this method will
     * reinstate it.
     *
     * @param conn un-managed connection.
     *
     * @return managed connection.
     */
    public static ManagedRegistryConnection getManagedRegistryConnection(Connection conn) {
        // Please be careful with the places in which this method is used. There are around 5 places
        // which creates a connection, then tries to check whether such a connection exists, and if
        // it does it would use the managed connection instead of the newly created one. Making the
        // smallest modification to that could lead into something like mounting getting busted for
        // example.
        return JDBCDatabaseTransaction.ManagedRegistryConnection.getManagedRegistryConnection(conn,
                true);
    }

    /**
     * A structure that can stack a transactional connection, which is handy when switching
     * connections.
     */
    private static class TransactionEntry {

        private boolean started;
        private ManagedRegistryConnection connection;
        private int nestedDepth;
        private boolean rollbacked;


        public boolean isStarted() {
            return started;
        }

        public void setStarted(boolean started) {
            this.started = started;
        }

        public ManagedRegistryConnection getConnection() {
            return connection;
        }

        public void setConnection(ManagedRegistryConnection connection) {
            this.connection = connection;
        }

        public int getNestedDepth() {
            return nestedDepth;
        }

        public void setNestedDepth(int nestedDepth) {
            this.nestedDepth = nestedDepth;
        }

        public boolean isRollbacked() {
            return rollbacked;
        }

        public void setRollbacked(boolean rollbacked) {
            this.rollbacked = rollbacked;
        }
    }

    // This represents a stack trace containing the details of who created a connection, along with
    // the created time.
    private static class ConnectionCreatorStack {

        private List<StackTraceElement> stackTraceElements;
        private java.util.Date createdTime;

        public ConnectionCreatorStack(StackTraceElement[] stackTraceElements,
                                      java.util.Date createdTime) {
            if (stackTraceElements != null) {
                this.stackTraceElements = Arrays.asList(stackTraceElements);
            }
            this.createdTime = createdTime;
        }

        public StackTraceElement[] getStackTraceElements() {
            if (stackTraceElements == null) {
                return new StackTraceElement[0];
            }
            return stackTraceElements.toArray(new StackTraceElement[stackTraceElements.size()]);
        }

        public java.util.Date getCreatedTime() {
            return createdTime;
        }
    }

    /**
     * Class to store connection statistics to monitor numbers of connections created and closed,
     * etc.
     */
    private static class ConnectionStatistics {
        private long connectionsCreated = 0;
        private long connectionsClosed = 0;
        private long connectionsCommitted = 0;
        private long connectionsRollbacked = 0;
        private long statementsPrepared = 0;
        private long statementsClosed = 0;

        private Map<String, ConnectionCreatorStack> connectionCreatorStacks =
                new LinkedHashMap<String, ConnectionCreatorStack>();

        public synchronized void
        setConnectionCreatorStack(String uuid,
                              ConnectionCreatorStack connectionCreatorStack) {
            connectionCreatorStacks.put(uuid, connectionCreatorStack);
        }

        public synchronized boolean isConnectionCreatorStackFound(String uuid) {
            return connectionCreatorStacks.get(uuid) != null;
        }

        public synchronized void removeConnectionCreatorStack(String uuid) {
            connectionCreatorStacks.remove(uuid);
        }

        public Collection<ConnectionCreatorStack> getConnectionCreatorStacks() {
            return connectionCreatorStacks.values();
        }

        public long getConnectionsCreated() {
            return connectionsCreated;
        }

        public long getConnectionsClosed() {
            return connectionsClosed;
        }

        public long getConnectionsCommitted() {
            return connectionsCommitted;
        }

        public long getConnectionsRollbacked() {
            return connectionsRollbacked;
        }

        public long getStatementsPrepared() {
            return statementsPrepared;
        }

        public long getStatementsClosed() {
            return statementsClosed;
        }

        public synchronized void incrementConnectionsCreated() {
            if (connectionsCreated != Long.MAX_VALUE) {
                connectionsCreated++;
            }
        }

        public synchronized void incrementConnectionsClosed() {
            if (connectionsClosed != Long.MAX_VALUE) {
                connectionsClosed++;
            }
        }

        public synchronized void incrementConnectionsCommitted() {
            if (connectionsCommitted != Long.MAX_VALUE) {
                connectionsCommitted++;
            }
        }

        public synchronized void incrementConnectionsRollbacked() {
            if (connectionsRollbacked != Long.MAX_VALUE) {
                connectionsRollbacked++;
            }
        }

        public synchronized void incrementStatementsPrepared() {
            if (statementsPrepared != Long.MAX_VALUE) {
                statementsPrepared++;
            }
        }

        public synchronized void incrementStatementsClosed() {
            if (statementsClosed != Long.MAX_VALUE) {
                statementsClosed++;
            }
        }
    }

    /**
     * Class that will wrap a prepared statement in debug mode, to obtain statistic records related
     * to prepared statements.
     */
    @SuppressWarnings("unused")
    private static class MonitoredPreparedStatement implements PreparedStatement {

        // This holds the un-managed connection.
        private PreparedStatement preparedStatement;

        // This holds an instance of the connection statistics object.
        private ConnectionStatistics connectionStatistics = null;

        // The executor service used to create threads to record connection statistics.
        private ExecutorService executor = null;

        public MonitoredPreparedStatement(PreparedStatement preparedStatement,
                                          ConnectionStatistics connectionStatistics,
                                          ExecutorService executor) {
            this.preparedStatement = preparedStatement;
            this.connectionStatistics = connectionStatistics;
            this.executor = executor;
            recordStatementPrepared();
        }

        // Increments the number of statements prepared by all connections.
        private void recordStatementPrepared() {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (connectionStatistics == null) {
                        log.error("Unable to store connection statistics.");
                    } else {
                        connectionStatistics.incrementStatementsPrepared();
                    }
                }
            };
            if (executor != null) {
                executor.execute(runnable);
            }
        }

        // Increments the number of statements prepared by all connections.
        private void recordStatementClosed() {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (connectionStatistics == null) {
                        log.error("Unable to store connection statistics.");
                    } else {
                        connectionStatistics.incrementStatementsClosed();
                    }
                }
            };
            if (executor != null) {
                executor.execute(runnable);
            }
        }

        public ResultSet executeQuery() throws SQLException {
            if (dbQueryLog.isDebugEnabled()) {
                recordStatistics(preparedStatement.toString());
            }
            return preparedStatement.executeQuery();
        }

        public int executeUpdate() throws SQLException {
            if (dbQueryLog.isDebugEnabled()) {
                recordStatistics(preparedStatement.toString());
            }
            return preparedStatement.executeUpdate();
        }

        private void recordStatistics(String statement) {
            String type = getOperationType(statement);
            StatisticsRecord statisticsRecord = DBQueryStatisticsLog.getStatisticsRecord();
            for (String record : getTableNames(statement)) {
                statisticsRecord.addRecord(record + " (" + type + ")");
                if (Boolean.toString(true).equals(
                        System.getProperty("carbon.registry.statistics.output.queries.executed"))) {
                    statisticsRecord.addQuery(statement);
                }
            }
        }

        private String[] getTableNames(final String statement) {
            List<String> names = Arrays.asList("REG_CLUSTER_LOCK", "REG_LOG", "REG_PATH",
                    "REG_CONTENT", "REG_CONTENT_HISTORY", "REG_RESOURCE", "REG_RESOURCE_HISTORY",
                    "REG_COMMENT", "REG_RESOURCE_COMMENT", "REG_RATING", "REG_RESOURCE_RATING",
                    "REG_TAG", "REG_RESOURCE_TAG", "REG_PROPERTY", "REG_RESOURCE_PROPERTY",
                    "REG_ASSOCIATION", "REG_SNAPSHOT");
            List<String> namesOnStatement = new LinkedList<String>();
            for (String name : names) {
                if (statement.contains(name)) {
                    namesOnStatement.add(name);
                }
            }
            Collections.sort(namesOnStatement, new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return Integer.valueOf(statement.indexOf(o1)).compareTo(statement.indexOf(o2));
                }
            });
            return namesOnStatement.toArray(new String[namesOnStatement.size()]);
        }

        private String getOperationType(String statement) {
            if (statement.contains("SELECT")) {
                return "R";
            } else if (statement.contains("INSERT") || statement.contains("UPDATE")  ||
                    statement.contains("DELETE") ) {
                return "W";
            }
            return "";
        }

        public void setNull(int i, int i1) throws SQLException {
            preparedStatement.setNull(i, i1);
        }

        public void setBoolean(int i, boolean b) throws SQLException {
            preparedStatement.setBoolean(i, b);
        }

        public void setByte(int i, byte b) throws SQLException {
            preparedStatement.setByte(i, b);
        }

        public void setShort(int i, short s) throws SQLException {
            preparedStatement.setShort(i, s);
        }

        public void setInt(int i, int i1) throws SQLException {
            preparedStatement.setInt(i, i1);
        }

        public void setLong(int i, long l) throws SQLException {
            preparedStatement.setLong(i, l);
        }

        public void setFloat(int i, float v) throws SQLException {
            preparedStatement.setFloat(i, v);
        }

        public void setDouble(int i, double v) throws SQLException {
            preparedStatement.setDouble(i, v);
        }

        public void setBigDecimal(int i, BigDecimal bigDecimal) throws SQLException {
            preparedStatement.setBigDecimal(i, bigDecimal);
        }

        public void setString(int i, String s) throws SQLException {
            preparedStatement.setString(i, s);
        }

        public void setBytes(int i, byte[] bytes) throws SQLException {
            preparedStatement.setBytes(i, bytes);
        }

        public void setDate(int i, Date date) throws SQLException {
            preparedStatement.setDate(i, date);
        }

        public void setTime(int i, Time time) throws SQLException {
            preparedStatement.setTime(i, time);
        }

        public void setTimestamp(int i, Timestamp timestamp) throws SQLException {
            preparedStatement.setTimestamp(i, timestamp);
        }

        public void setAsciiStream(int i, InputStream inputStream, int i1) throws SQLException {
            preparedStatement.setAsciiStream(i, inputStream, i1);
        }

        @SuppressWarnings("deprecation")
        public void setUnicodeStream(int i, InputStream inputStream, int i1) throws SQLException {
            preparedStatement.setUnicodeStream(i, inputStream, i1);
        }

        public void setBinaryStream(int i, InputStream inputStream, int i1) throws SQLException {
            preparedStatement.setBinaryStream(i, inputStream, i1);
        }

        public void clearParameters() throws SQLException {
            preparedStatement.clearParameters();
        }

        public void setObject(int i, Object o, int i1, int i2) throws SQLException {
            preparedStatement.setObject(i, o, i1, i2);
        }

        public void setObject(int i, Object o, int i1) throws SQLException {
            preparedStatement.setObject(i, o, i1);
        }

        public void setObject(int i, Object o) throws SQLException {
            preparedStatement.setObject(i, o);
        }

        public boolean execute() throws SQLException {
            return preparedStatement.execute();
        }

        public void addBatch() throws SQLException {
            preparedStatement.addBatch();
        }

        public void setCharacterStream(int i, Reader reader, int i1) throws SQLException {
            preparedStatement.setCharacterStream(i, reader, i1);
        }

        public void setRef(int i, Ref ref) throws SQLException {
            preparedStatement.setRef(i, ref);
        }

        public void setBlob(int i, Blob blob) throws SQLException {
            preparedStatement.setBlob(i, blob);
        }

        public void setClob(int i, Clob clob) throws SQLException {
            preparedStatement.setClob(i, clob);
        }

        public void setArray(int i, Array array) throws SQLException {
            preparedStatement.setArray(i, array);
        }

        public ResultSetMetaData getMetaData() throws SQLException {
            return preparedStatement.getMetaData();
        }

        public void setDate(int i, Date date, Calendar calendar) throws SQLException {
            preparedStatement.setDate(i, date, calendar);
        }

        public void setTime(int i, Time time, Calendar calendar) throws SQLException {
            preparedStatement.setTime(i, time, calendar);
        }

        public void setTimestamp(int i, Timestamp timestamp, Calendar calendar) throws SQLException {
            preparedStatement.setTimestamp(i, timestamp, calendar);
        }

        public void setNull(int i, int i1, String s) throws SQLException {
            preparedStatement.setNull(i, i1, s);
        }

        public void setURL(int i, URL url) throws SQLException {
            preparedStatement.setURL(i, url);
        }

        public ParameterMetaData getParameterMetaData() throws SQLException {
            return preparedStatement.getParameterMetaData();
        }

        public ResultSet executeQuery(String s) throws SQLException {
            return preparedStatement.executeQuery(s);
        }

        public int executeUpdate(String s) throws SQLException {
            return preparedStatement.executeUpdate(s);
        }

        public void close() throws SQLException {
            recordStatementClosed();
            preparedStatement.close();
        }

        public int getMaxFieldSize() throws SQLException {
            return preparedStatement.getMaxFieldSize();
        }

        public void setMaxFieldSize(int i) throws SQLException {
            preparedStatement.setMaxFieldSize(i);
        }

        public int getMaxRows() throws SQLException {
            return preparedStatement.getMaxRows();
        }

        public void setMaxRows(int i) throws SQLException {
            preparedStatement.setMaxRows(i);
        }

        public void setEscapeProcessing(boolean b) throws SQLException {
            preparedStatement.setEscapeProcessing(b);
        }

        public int getQueryTimeout() throws SQLException {
            return preparedStatement.getQueryTimeout();
        }

        public void setQueryTimeout(int i) throws SQLException {
            preparedStatement.setQueryTimeout(i);
        }

        public void cancel() throws SQLException {
            preparedStatement.cancel();
        }

        public SQLWarning getWarnings() throws SQLException {
            return preparedStatement.getWarnings();
        }

        public void clearWarnings() throws SQLException {
            preparedStatement.clearWarnings();
        }

        public void setCursorName(String s) throws SQLException {
            preparedStatement.setCursorName(s);
        }

        public boolean execute(String s) throws SQLException {
            return preparedStatement.execute(s);
        }

        public ResultSet getResultSet() throws SQLException {
            return preparedStatement.getResultSet();
        }

        public int getUpdateCount() throws SQLException {
            return preparedStatement.getUpdateCount();
        }

        public boolean getMoreResults() throws SQLException {
            return preparedStatement.getMoreResults();
        }

        public void setFetchDirection(int i) throws SQLException {
            preparedStatement.setFetchDirection(i);
        }

        public int getFetchDirection() throws SQLException {
            return preparedStatement.getFetchDirection();
        }

        public void setFetchSize(int i) throws SQLException {
            preparedStatement.setFetchSize(i);
        }

        public int getFetchSize() throws SQLException {
            return preparedStatement.getFetchSize();
        }

        public int getResultSetConcurrency() throws SQLException {
            return preparedStatement.getResultSetConcurrency();
        }

        public int getResultSetType() throws SQLException {
            return preparedStatement.getResultSetType();
        }

        public void addBatch(String s) throws SQLException {
            preparedStatement.addBatch(s);
        }

        public void clearBatch() throws SQLException {
            preparedStatement.clearBatch();
        }

        public int[] executeBatch() throws SQLException {
            return preparedStatement.executeBatch();
        }

        public Connection getConnection() throws SQLException {
            return preparedStatement.getConnection();
        }

        public boolean getMoreResults(int i) throws SQLException {
            return preparedStatement.getMoreResults(i);
        }

        public ResultSet getGeneratedKeys() throws SQLException {
            return preparedStatement.getGeneratedKeys();
        }

        public int executeUpdate(String s, int i) throws SQLException {
            return preparedStatement.executeUpdate(s, i);
        }

        public int executeUpdate(String s, int[] integers) throws SQLException {
            return preparedStatement.executeUpdate(s, integers);
        }

        public int executeUpdate(String s, String[] strings) throws SQLException {
            return preparedStatement.executeUpdate(s, strings);
        }

        public boolean execute(String s, int i) throws SQLException {
            return preparedStatement.execute(s, i);
        }

        public boolean execute(String s, int[] integers) throws SQLException {
            return preparedStatement.execute(s, integers);
        }

        public boolean execute(String s, String[] strings) throws SQLException {
            return preparedStatement.execute(s, strings);
        }

        public int getResultSetHoldability() throws SQLException {
            return preparedStatement.getResultSetHoldability();
        }

        ////////////////////////////////////////////////////////
        // JDK 6 only methods.
        ////////////////////////////////////////////////////////

        public void setNClob(int i, Reader reader) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setClob(int i, Reader reader) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setBlob(int i, InputStream x) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setNClob(int i, Reader reader, long l) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setClob(int i, Reader reader, long l) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setBlob(int i, InputStream x, long l) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setNCharacterStream(int i, Reader reader) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setCharacterStream(int i, Reader reader) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setAsciiStream(int i, InputStream x) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setBinaryStream(int i, InputStream x) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setNCharacterStream(int i, Reader reader, long l) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setCharacterStream(int i, Reader reader, long l) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setAsciiStream(int i, InputStream x, long l) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setBinaryStream(int i, InputStream x, long l) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setNClob(int i, NClob nClob) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setNString(int i, String string) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setSQLXML(int i, SQLXML sqlxml) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setRowId(int i, RowId rowId) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public boolean isPoolable() throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setPoolable(boolean b) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public boolean isClosed() throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public boolean isWrapperFor(Class<?> c) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public <T> T unwrap(Class<T> c) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        ////////////////////////////////////////////////////////
        // JDK 7 only methods.
        ////////////////////////////////////////////////////////
        
		public void closeOnCompletion() throws SQLException {
			// This is a JDK7 specific operation that we don't support for JDK5 and 6 compatibility
			throw new UnsupportedOperationException("This method is not supported");
		}

		public boolean isCloseOnCompletion() throws SQLException {
			// This is a JDK7 specific operation that we don't support for JDK5 and 6 compatibility
			throw new UnsupportedOperationException("This method is not supported");
		}
    }

    /**
     * Wrapped connection that manages connections making it possible to have connections to
     * multiple databases.
     */
    @SuppressWarnings("unused")
    public static final class ManagedRegistryConnection implements Connection {

        private static final int DEFAULT_CONNECTION_CREATION_WAIT_TIME = 100;
        // This holds the un-managed connection.
        private Connection connection;

        // This holds an instance of the connection statistics object.
        private static ConnectionStatistics connectionStatistics = null;

        // The executor service used to create threads to record connection statistics.
        private static ExecutorService executor = null;

        private String uuid = UUIDGenerator.generateUUID();

        static {
            if (statsLog.isDebugEnabled()) {
                initializeStatisticsLogging();
            }
        }

        private static synchronized void initializeStatisticsLogging() {
            if (executor != null) {
                return;
            }
            connectionStatistics = new ConnectionStatistics();
            executor = Executors.newCachedThreadPool();
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    executor.shutdownNow();
                }
            });
            final ScheduledExecutorService scheduler =
                    Executors.newScheduledThreadPool(10);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    scheduler.shutdownNow();
                }
            });
            Runnable runnable = new Runnable() {
                public void run() {
                    if (connectionStatistics == null) {
                        log.error("Unable to store connection statistics.");
                    } else {
                        statsLog.debug("Total Number of Connections Created      : " +
                                connectionStatistics.getConnectionsCreated());
                        statsLog.debug("Total Number of Connections Closed       : " +
                                connectionStatistics.getConnectionsClosed());
                        statsLog.debug("Total Number of Connections Committed    : " +
                                connectionStatistics.getConnectionsCommitted());
                        statsLog.debug("Total Number of Connections Rollbacked   : " +
                                connectionStatistics.getConnectionsRollbacked());
                        statsLog.debug("Total Number of Statements Prepared      : " +
                                connectionStatistics.getStatementsPrepared());
                        statsLog.debug("Total Number of Statements Closed        : " +
                                connectionStatistics.getStatementsClosed());
                        StringBuffer sb = new StringBuffer("");
                        int count = 0;
                        try {
                            for (ConnectionCreatorStack connectionCreatorStack :
                                    connectionStatistics.getConnectionCreatorStacks()) {
                                sb.append("\nTransaction ").append(++count);
                                java.util.Date currentTime = new java.util.Date();
                                long activeFor = currentTime.getTime() -
                                        connectionCreatorStack.getCreatedTime().getTime();
                                sb.append(" (Active For ").append(activeFor).append("ms) : ");
                                int temp = 0;
                                for (StackTraceElement stackTraceElement :
                                        connectionCreatorStack.getStackTraceElements()) {
                                    if (temp++ < 3) {
                                        continue;
                                    }
                                    sb.append("\n\t").append(stackTraceElement.getClassName());
                                    sb.append(".").append(stackTraceElement.getMethodName());
                                    sb.append("(").append(stackTraceElement.getFileName());
                                    sb.append(":").append(stackTraceElement.getLineNumber());
                                    sb.append(")");
                                }
                            }
                        } catch (Exception e) {
                            statsLog.debug("An error occurred while determining active " +
                                    "transactions.", e);
                        }
                        statsLog.debug("Total Number of Active Transactions      : " + count +
                                sb.toString());
                    }
                }
            };
            scheduler.scheduleAtFixedRate(runnable, 60, 60, TimeUnit.SECONDS);
        }

        // This contains a list of all the managed connections.
        private static ThreadLocal<Map<String, ManagedRegistryConnection>>
                tManagedConnectionMap =
                new ThreadLocal<Map<String, ManagedRegistryConnection>>() {
                    protected Map<String, ManagedRegistryConnection> initialValue() {
                        return new LinkedHashMap<String, ManagedRegistryConnection>();
                    }
                };

        // This contains a list of committed or rollbacked connections.
        private static ThreadLocal<Map<String, ManagedRegistryConnection>>
                tCommittedAndRollbackedConnectionMap =
                new ThreadLocal<Map<String, ManagedRegistryConnection>>() {
                    protected Map<String, ManagedRegistryConnection> initialValue() {
                        return new LinkedHashMap<String, ManagedRegistryConnection>();
                    }
                };

        // This contains a list of connections that have been closed.
        private static ThreadLocal<Map<String, ManagedRegistryConnection>>
                tClosedConnectionMap =
                new ThreadLocal<Map<String, ManagedRegistryConnection>>() {
                    protected Map<String, ManagedRegistryConnection> initialValue() {
                        return new LinkedHashMap<String, ManagedRegistryConnection>();
                    }
                };

        // This identifies whether at least one nested transactional connection was rollbacked.
        private static ThreadLocal<Boolean> tRollbackedConnection =
                new ThreadLocal<Boolean>() {
                    protected Boolean initialValue() {
                        return false;
                    }
                };

        public ManagedRegistryConnection(Connection connection) {
            // If the connection is already managed, obtain the original connection.
            if (connection instanceof ManagedRegistryConnection) {
                this.connection = ((ManagedRegistryConnection) connection).getConnection();
            } else {
                this.connection = connection;
            }
            log.trace("Saving managed registry connection to map.");
            tManagedConnectionMap.get().put(getConnectionId(), this);

            // We start from scratch here, so remove any closed or committed connections.
            if (tClosedConnectionMap.get().get(getConnectionId()) != null) {
                tClosedConnectionMap.get().put(getConnectionId(), null);
            }
            if (tCommittedAndRollbackedConnectionMap.get().get(getConnectionId()) != null) {
                tCommittedAndRollbackedConnectionMap.get().put(getConnectionId(), null);
            }
            if (statsLog.isDebugEnabled()) {
                final StackTraceElement[] stackTraceElements =
                        Thread.currentThread().getStackTrace();
                Runnable runnable = new Runnable() {
                    public void run() {
                        if (connectionStatistics == null) {
                            log.error("Unable to store connection statistics.");
                        } else {
                            connectionStatistics.incrementConnectionsCreated();
                            connectionStatistics.setConnectionCreatorStack(uuid,
                                    new ConnectionCreatorStack(stackTraceElements,
                                            new java.util.Date()));
                        }
                    }
                };
                if (executor != null) {
                    executor.execute(runnable);
                } else {
                    initializeStatisticsLogging();
                    executor.execute(runnable);
                }
            }
        }

        private String getConnectionId() {
            return RegistryUtils.getConnectionId(this.connection);
        }

        /**
         * If a managed connection for the corresponding connection is available, get that.
         *
         * @param connection the original connection
         * @param reinstate whether to instate the connection or not.
         *
         * @return the managed connection or null
         */
        public static ManagedRegistryConnection getManagedRegistryConnection(Connection connection,
                                                              boolean reinstate) {
            if (tManagedConnectionMap != null && tManagedConnectionMap.get() != null) {
                ManagedRegistryConnection mrc = tManagedConnectionMap.get().get(
                        RegistryUtils.getConnectionId(connection));
                if (mrc != null && reinstate) {
                    if (tClosedConnectionMap != null && tClosedConnectionMap.get() != null &&
                            tClosedConnectionMap.get().get(
                                    RegistryUtils.getConnectionId(connection)) != null) {
                        tClosedConnectionMap.get().put(
                                RegistryUtils.getConnectionId(connection), null);
                    }
                    if (tCommittedAndRollbackedConnectionMap != null &&
                            tCommittedAndRollbackedConnectionMap.get() != null &&
                            tCommittedAndRollbackedConnectionMap.get().get(
                                    RegistryUtils.getConnectionId(connection)) != null) {
                        tCommittedAndRollbackedConnectionMap.get().put(
                                RegistryUtils.getConnectionId(connection), null);
                    }
                }
                return mrc;
            }
            return null;
        }

        // This utility method returns the number of connections stored in a given map.
        private int getConnectionCount(Map<String, ManagedRegistryConnection> connections) {
            int count = 0;
            for (Map.Entry<String, ManagedRegistryConnection> e : connections.entrySet()) {
                if (e.getValue() != null) {
                    count++;
                }
            }
            return count;
        }

        public void commit() throws SQLException {
            if (statsLog.isDebugEnabled()) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        if (connectionStatistics == null) {
                            log.error("Unable to store connection statistics.");
                        } else {
                            connectionStatistics.incrementConnectionsCommitted();
                        }
                    }
                };
                if (executor != null) {
                    executor.execute(runnable);
                } else {
                    initializeStatisticsLogging();
                    executor.execute(runnable);
                }
            }
            if (tRollbackedConnection.get()) {
                // If at least one connection was rollbacked, do the same for all.
                log.trace("Rolling back the transaction(s).");
                rollback();
            } else {
                if (tManagedConnectionMap.get().size() == 1) {
                    // If there is only one connection, simply commit that.
                    connection.commit();
                    log.trace("Committed all transactions.");
                } else if ((tCommittedAndRollbackedConnectionMap.get().get(getConnectionId())
                        == null) && (tManagedConnectionMap.get().size() ==
                        getConnectionCount(tCommittedAndRollbackedConnectionMap.get()) + 1)) {
                    // If this is the outer connection, then commit all inner connections and then
                    // the outer connection.
                    Map<String, ManagedRegistryConnection> connections =
                            tCommittedAndRollbackedConnectionMap.get();
                    for (Map.Entry<String, ManagedRegistryConnection> e : connections.entrySet()) {
                        if (e.getValue() != null) {
                            e.getValue().getConnection().commit();
                            connections.put(e.getKey(), null);
                        }
                    }
                    // Clean up list of committed and rollbacked connections.
                    tCommittedAndRollbackedConnectionMap.set(new LinkedHashMap<String,
                            ManagedRegistryConnection>());
                    connection.commit();
                    log.trace("Committed all transactions.");
                } else if (tManagedConnectionMap.get().size() <
                        getConnectionCount(tCommittedAndRollbackedConnectionMap.get())) {
                    throw new SQLException("Total number of available connections are less than " +
                            "the total number of committed connections");
                } else {
                    // If this is an inner connection, keep this for later.
                    tCommittedAndRollbackedConnectionMap.get().put(getConnectionId(), this);
                }
            }
        }

        public void rollback() throws SQLException {
            if (statsLog.isDebugEnabled()) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        if (connectionStatistics == null) {
                            log.error("Unable to store connection statistics.");
                        } else {
                            connectionStatistics.incrementConnectionsRollbacked();
                        }
                    }
                };
                if (executor != null) {
                    executor.execute(runnable);
                } else {
                    initializeStatisticsLogging();
                    executor.execute(runnable);
                }
            }
            // Calling this method once will set the flag indicating at least one connection was
            // rollbacked.
            tRollbackedConnection.set(true);
            if (tManagedConnectionMap.get().size() == 1) {
                // If there is only one connection, simply rollback that.
                connection.rollback();
                // Clear the flag after rollback
                tRollbackedConnection.set(false);
                log.trace("Rolled back all transactions.");
            } else if ((tCommittedAndRollbackedConnectionMap.get().get(getConnectionId())
                        == null) && (tManagedConnectionMap.get().size() ==
                        getConnectionCount(tCommittedAndRollbackedConnectionMap.get()) + 1)) {
                // If this is the outer connection, then rollback all inner connections and then
                // the outer connection.
                Map<String, ManagedRegistryConnection> connections =
                        tCommittedAndRollbackedConnectionMap.get();
                for (Map.Entry<String, ManagedRegistryConnection> e : connections.entrySet()) {
                    if (e.getValue() != null) {
                        e.getValue().getConnection().rollback();
                    }
                }
                // Clean up list of committed and rollbacked connections.
                tCommittedAndRollbackedConnectionMap.set(new LinkedHashMap<String,
                        ManagedRegistryConnection>());
                connection.rollback();
                // Clear the flag after rollback
                tRollbackedConnection.set(false);
                log.trace("Rolled back all transactions.");
            } else if (tManagedConnectionMap.get().size() <
                    getConnectionCount(tCommittedAndRollbackedConnectionMap.get())) {
                throw new SQLException("Total number of available connections are less than the " +
                        "total number of rollbacked or committed connections");
            } else {
                // If this is an inner connection, keep this for later.
                tCommittedAndRollbackedConnectionMap.get().put(getConnectionId(), this);
            }
        }

        public void close() throws SQLException {
            if (statsLog.isDebugEnabled()) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        if (connectionStatistics == null) {
                            log.error("Unable to store connection statistics.");
                        } else {
                            connectionStatistics.incrementConnectionsClosed();
                            while (!connectionStatistics.isConnectionCreatorStackFound(uuid)) {
                                // Due to the way in which threads are scheduled in Java, the thread
                                // responsible for handling the reporting of statistics for the
                                // connection close event might run before the thread handling the
                                // reporting of statistics for the the connection creation event.
                                // Therefore, we wait until we are certain that the connection has
                                // been created.
                                try {
                                    Thread.sleep(DEFAULT_CONNECTION_CREATION_WAIT_TIME);
                                } catch (InterruptedException ignored) {}
                            }
                            connectionStatistics.removeConnectionCreatorStack(uuid);
                        }
                    }
                };
                if (executor != null) {
                    executor.execute(runnable);
                } else {
                    initializeStatisticsLogging();
                    executor.execute(runnable);
                }
            }
            if (tManagedConnectionMap.get().size() == 1) {
                // If there is only one connection, simply close that.
                connection.close();
                // Clean up list of managed connections, since we have closed the outer connection.
                tManagedConnectionMap.set(new LinkedHashMap<String, ManagedRegistryConnection>());
                log.trace("Closed all transactions.");
            } else if ((tClosedConnectionMap.get().get(getConnectionId())
                        == null) && (tManagedConnectionMap.get().size() ==
                        getConnectionCount(tClosedConnectionMap.get()) + 1)) {
                // If this is the outer connection, then close all inner connections and then
                // the outer connection.
                Map<String, ManagedRegistryConnection> connections = tClosedConnectionMap.get();
                for (Map.Entry<String, ManagedRegistryConnection> e : connections.entrySet()) {
                    if (e.getValue() != null) {
                        e.getValue().getConnection().close();
                    }
                }
                // Clean up list of closed connections.
                tClosedConnectionMap.set(new LinkedHashMap<String, ManagedRegistryConnection>());
                connection.close();
                // Clean up list of managed connections. Closing a connection and all the nested
                // transactional connections would result in closing all of the managed connections.
                // This would leave us with nothing to manage.
                tManagedConnectionMap.set(new LinkedHashMap<String, ManagedRegistryConnection>());
                log.trace("Closed all transactions.");
            } else if (tManagedConnectionMap.get().size() <
                    getConnectionCount(tClosedConnectionMap.get())) {
                throw new SQLException("Total number of available connections are less than the " +
                        "total number of closed connections");
            } else {
                // If this is an inner connection, keep this for later.
                tClosedConnectionMap.get().put(getConnectionId(), this);
            }
        }

        public boolean isClosed() throws SQLException {
            // If there is only one connection, we don't add it to the closed connection map.
            if (tManagedConnectionMap.get().size() == 1) {
                return connection.isClosed();
            }
            // This connection is closed, if it is in the closed connections map, or if the
            // underlying connection is closed.
            return tClosedConnectionMap.get().get(getConnectionId()) != null ||
                    connection.isClosed();
        }

        public void rollback(Savepoint savepoint) throws SQLException {
            if (tManagedConnectionMap.get().size() == 1) {
                connection.rollback(savepoint);
            } else {
                throw new UnsupportedOperationException("Rollback to Save Point is not supported " +
                        "for operations involving multiple connections.");
            }
        }

        ////////////////////////////////////////////////////////
        // JDK 6 only methods.
        ////////////////////////////////////////////////////////

        public Blob createBlob() throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public Clob createClob() throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public SQLXML createSQLXML() throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public NClob createNClob() throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public Properties getClientInfo() {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public String getClientInfo(String name) {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public boolean isValid(int timeout) {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setClientInfo(Properties properties) {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public void setClientInfo(String name, String value) {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public boolean isWrapperFor(Class<?> i) {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        public <T> T unwrap(Class<T> i) {
            // This is a JDK6 specific operation that we don't support for JDK5 compatibility
            throw new UnsupportedOperationException("This method is not supported");
        }

        ////////////////////////////////////////////////////////
        // Other un-managed methods
        ////////////////////////////////////////////////////////

        public Statement createStatement() throws SQLException {
            return connection.createStatement();
        }

        public PreparedStatement prepareStatement(String s) throws SQLException {
            if (statsLog.isDebugEnabled() || dbQueryLog.isDebugEnabled()) {
                if (executor == null) {
                    initializeStatisticsLogging();
                }
                return new MonitoredPreparedStatement(connection.prepareStatement(s),
                        connectionStatistics, executor);
            }
            return connection.prepareStatement(s);
        }

        public CallableStatement prepareCall(String s) throws SQLException {
            return connection.prepareCall(s);
        }

        public String nativeSQL(String s) throws SQLException {
            return connection.nativeSQL(s);
        }

        public void setAutoCommit(boolean b) throws SQLException {
            connection.setAutoCommit(b);
        }

        public boolean getAutoCommit() throws SQLException {
            return connection.getAutoCommit();
        }

        public DatabaseMetaData getMetaData() throws SQLException {
            return connection.getMetaData();
        }

        public void setReadOnly(boolean b) throws SQLException {
            connection.setReadOnly(b);
        }

        public boolean isReadOnly() throws SQLException {
            return connection.isReadOnly();
        }

        public void setCatalog(String s) throws SQLException {
            connection.setCatalog(s);
        }

        public String getCatalog() throws SQLException {
            return connection.getCatalog();
        }

        public void setTransactionIsolation(int i) throws SQLException {
            connection.setTransactionIsolation(i);
        }

        public int getTransactionIsolation() throws SQLException {
            return connection.getTransactionIsolation();
        }

        public SQLWarning getWarnings() throws SQLException {
            return connection.getWarnings();
        }

        public void clearWarnings() throws SQLException {
            connection.clearWarnings();
        }

        public Statement createStatement(int i, int i1) throws SQLException {
            return connection.createStatement(i, i1);
        }

        public PreparedStatement prepareStatement(String s, int i, int i1) throws SQLException {
            if (statsLog.isDebugEnabled() || dbQueryLog.isDebugEnabled()) {
                if (executor == null) {
                    initializeStatisticsLogging();
                }
                return new MonitoredPreparedStatement(connection.prepareStatement(s, i, i1),
                        connectionStatistics, executor);
            }
            return connection.prepareStatement(s, i, i1);
        }

        public CallableStatement prepareCall(String s, int i, int i1) throws SQLException {
            return connection.prepareCall(s, i, i1);
        }

        public Map<String, Class<?>> getTypeMap() throws SQLException {
            return connection.getTypeMap();
        }

        public void setTypeMap(Map<String, Class<?>> stringClassMap) throws SQLException {
            connection.setTypeMap(stringClassMap);
        }

        public void setHoldability(int i) throws SQLException {
            connection.setHoldability(i);
        }

        public int getHoldability() throws SQLException {
            return connection.getHoldability();
        }

        public Savepoint setSavepoint() throws SQLException {
            return connection.setSavepoint();
        }

        public Savepoint setSavepoint(String s) throws SQLException {
            return connection.setSavepoint(s);
        }

        public void releaseSavepoint(Savepoint savepoint) throws SQLException {
            connection.releaseSavepoint(savepoint);
        }

        public Statement createStatement(int i, int i1, int i2) throws SQLException {
            return connection.createStatement(i, i1, i2);
        }

        public PreparedStatement prepareStatement(String s, int i, int i1, int i2)
                throws SQLException {
            if (statsLog.isDebugEnabled() || dbQueryLog.isDebugEnabled()) {
                if (executor == null) {
                    initializeStatisticsLogging();
                }
                return new MonitoredPreparedStatement(connection.prepareStatement(s, i, i1, i2),
                        connectionStatistics, executor);
            }
            return connection.prepareStatement(s, i, i1, i2);
        }

        public CallableStatement prepareCall(String s, int i, int i1, int i2) throws SQLException {
            return connection.prepareCall(s, i, i1, i2);
        }

        public PreparedStatement prepareStatement(String s, int i) throws SQLException {
            if (statsLog.isDebugEnabled() || dbQueryLog.isDebugEnabled()) {
                if (executor == null) {
                    initializeStatisticsLogging();
                }
                return new MonitoredPreparedStatement(connection.prepareStatement(s, i),
                        connectionStatistics, executor);
            }
            return connection.prepareStatement(s, i);
        }

        public PreparedStatement prepareStatement(String s, int[] integers) throws SQLException {
            if (statsLog.isDebugEnabled() || dbQueryLog.isDebugEnabled()) {
                if (executor == null) {
                    initializeStatisticsLogging();
                }
                return new MonitoredPreparedStatement(connection.prepareStatement(s, integers),
                        connectionStatistics, executor);
            }
            return connection.prepareStatement(s, integers);
        }

        public PreparedStatement prepareStatement(String s, String[] strings) throws SQLException {
            if (statsLog.isDebugEnabled() || dbQueryLog.isDebugEnabled()) {
                if (executor == null) {
                    initializeStatisticsLogging();
                }
                return new MonitoredPreparedStatement(connection.prepareStatement(s, strings),
                        connectionStatistics, executor);
            }
            return connection.prepareStatement(s, strings);
        }

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }

		public void setSchema(String schema) throws SQLException {
			// This is a JDK7 specific operation that we don't support for JDK5 and 6 compatibility
			throw new UnsupportedOperationException("This method is not supported");
			
		}
		
        ////////////////////////////////////////////////////////
        // JDK 7 only methods.
        ////////////////////////////////////////////////////////

		public String getSchema() throws SQLException {
			// This is a JDK7 specific operation that we don't support for JDK5 and 6 compatibility
			throw new UnsupportedOperationException("This method is not supported");
		}

		public void abort(Executor executor) throws SQLException {
			// This is a JDK7 specific operation that we don't support for JDK5 and 6 compatibility
			throw new UnsupportedOperationException("This method is not supported");
			
		}

		public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
			// This is a JDK7 specific operation that we don't support for JDK5 and 6 compatibility
			throw new UnsupportedOperationException("This method is not supported");
			
		}

		public int getNetworkTimeout() throws SQLException {
			// This is a JDK7 specific operation that we don't support for JDK5 and 6 compatibility
			throw new UnsupportedOperationException("This method is not supported");
		}
    }

}
