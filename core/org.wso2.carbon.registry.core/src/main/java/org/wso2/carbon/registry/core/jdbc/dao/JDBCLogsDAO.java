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
package org.wso2.carbon.registry.core.jdbc.dao;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.LogEntry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.DataBaseConfiguration;
import org.wso2.carbon.registry.core.config.Mount;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.RemoteConfiguration;
import org.wso2.carbon.registry.core.dao.LogsDAO;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.DatabaseConstants;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDatabaseTransaction;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.pagination.PaginationConstants;
import org.wso2.carbon.registry.core.pagination.PaginationContext;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.LogRecord;
import org.wso2.carbon.registry.core.pagination.PaginationUtils;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * An implementation of the {@link LogsDAO} to store logs on a JDBC-based database.
 */
public class JDBCLogsDAO implements LogsDAO {

    private static final Log log = LogFactory.getLog(JDBCLogsDAO.class);
    private String enableApiPagination = PaginationConstants.ENABLE_API_PAGINATE;
    private Map<String, DataBaseConfiguration> dbConfigs = new HashMap<String, DataBaseConfiguration>();
    private Map<String, String> pathMap = new HashMap<String, String>();

    public JDBCLogsDAO() {
        RegistryContext registryContext = RegistryContext.getBaseInstance();
        for (Mount mount : registryContext.getMounts()) {
            for(RemoteConfiguration configuration : registryContext.getRemoteInstances()) {
                if (configuration.getDbConfig() != null &&
                        mount.getInstanceId().equals(configuration.getId())) {
                    dbConfigs.put(mount.getPath(),
                            registryContext.getDBConfig(configuration.getDbConfig()));
                    pathMap.put(mount.getPath(), mount.getTargetPath());
                }
            }
        }
    }

    public void saveLogBatch(LogRecord[] logRecords) throws RegistryException {
        DataAccessManager dataAccessManager;
        if (CurrentSession.getUserRegistry() != null
                && CurrentSession.getUserRegistry().getRegistryContext() != null) {
            dataAccessManager = CurrentSession.getUserRegistry().getRegistryContext()
                    .getDataAccessManager();
        } else {
            dataAccessManager = RegistryContext.getBaseInstance().getDataAccessManager();
        }
        if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
            String msg = "Failed to get logs. Invalid data access manager.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        if (dbConfigs.size() > 0) {
            Map<String, List<LogRecord>> entryMap = new HashMap<String, List<LogRecord>>();
            List<LogRecord> localEntries = new LinkedList<LogRecord>();
            for (LogRecord entry : logRecords) {
                String resourcePath = entry.getResourcePath();
                boolean pathAdded = false;
                for (String sourcePath : dbConfigs.keySet()) {
                    if (resourcePath.startsWith(sourcePath)) {
                        // Add logs before mount translation.
                        if (!entryMap.containsKey(sourcePath)) {
                            entryMap.put(sourcePath, new LinkedList<LogRecord>());
                        }
                        entry.setResourcePath(pathMap.get(sourcePath) + resourcePath.substring(sourcePath.length()));
                        entryMap.get(sourcePath).add(entry);
                        pathAdded = true;
                        break;
                    } else if (resourcePath.startsWith(pathMap.get(sourcePath))) {
                        // Add logs after mount translation.
                        if (!entryMap.containsKey(sourcePath)) {
                            entryMap.put(sourcePath, new LinkedList<LogRecord>());
                        }
                        entryMap.get(sourcePath).add(entry);
                        pathAdded = true;
                        break;
                    }
                }
                if (!pathAdded) {
                    localEntries.add(entry);
                }
            }
            for (String sourcePath : dbConfigs.keySet()) {
                List<LogRecord> temp = entryMap.get(sourcePath);
                if (temp != null) {
                    addLogRecords(temp.toArray(new LogRecord[temp.size()]),
                            new JDBCDataAccessManager(dbConfigs.get(sourcePath)));
                }
            }
            addLogRecords(localEntries.toArray(new LogRecord[localEntries.size()]),
                    (JDBCDataAccessManager) dataAccessManager);
        } else {
            addLogRecords(logRecords, (JDBCDataAccessManager) dataAccessManager);
        }
    }

    private void addLogRecords(LogRecord[] logRecords, JDBCDataAccessManager dataAccessManager)
            throws RegistryException {
        PreparedStatement s = null;
        Connection conn = null;
        try {
            conn = dataAccessManager.getDataSource().getConnection();
            if (conn.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
                conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }
            conn.setAutoCommit(false);
            String sql = "INSERT INTO REG_LOG (REG_PATH, REG_USER_ID, REG_LOGGED_TIME, "
                    + "REG_ACTION, REG_ACTION_DATA, REG_TENANT_ID) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

            s = conn.prepareStatement(sql);
            for (LogRecord logRecord : logRecords) {
                s.clearParameters();
                s.setString(1, logRecord.getResourcePath());
                s.setString(2, logRecord.getUserName());
                s.setTimestamp(3, new Timestamp(logRecord.getTimestamp().getTime()));
                s.setInt(4, logRecord.getAction());
                s.setString(5, logRecord.getActionData());
                s.setInt(6, logRecord.getTenantId());
                s.addBatch();
            }
            int[] status = s.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Successfully added " + status.length + " log records.");
            }
            conn.commit();

        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                log.error("Failed to rollback log insertion.", e);
            }
            String msg = "Failed to update log batch records " + ". "
                    + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                if (s != null) {
                    s.close();
                }
                if (conn != null && !(conn.isClosed())) {
                    conn.close();
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    public List getLogList(String path, int action,
                        String userName, Date from, Date to, boolean descending, DataAccessManager dataAccessManager)
            throws RegistryException {

        // This is for fixing REGISTRY-1911
        try {
            if (userName == null && CurrentSession.getUser() != null &&
                    !CurrentSession.getUser().equals(CarbonConstants.REGISTRY_SYSTEM_USERNAME)) {
                UserRealm realm = CurrentSession.getUserRealm();
                UserStoreManager reader = realm.getUserStoreManager();
                String[] roles = reader.getRoleListOfUser(CurrentSession.getUser());
                List list = Arrays.asList(roles);

                if (!list.contains(CurrentSession.getUserRealm().getRealmConfiguration().getAdminRoleName())) {
                    userName = CurrentSession.getUser();
                }

            }
        } catch (UserStoreException e) {
            String msg = "Failed to get list of roles of user " + userName;
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        String resourcePath = path;
        if (resourcePath != null) {
            // if you have a path, see whether the path is inside the mount or not.
            JDBCDataAccessManager jdbcDataAccessManager = null;
            if (dbConfigs.size() > 0) {
                for (String sourcePath : dbConfigs.keySet()) {
                    if (resourcePath.startsWith(sourcePath)) {
                        resourcePath = pathMap.get(sourcePath) + resourcePath.substring(sourcePath.length());
                        jdbcDataAccessManager = new JDBCDataAccessManager(dbConfigs.get(sourcePath));
                    }
                }
            }
            if (jdbcDataAccessManager != null) {
                // if it is inside the mount then return results only from the mount.
                boolean transactionSucceeded = false;
                try {
                    beginTransaction(jdbcDataAccessManager);
                    List<LogEntry> logEntries = internalGetLogs(true, resourcePath, action, userName, from, to, descending,
                            JDBCDatabaseTransaction.getConnection());
                    transactionSucceeded = true;
                    return logEntries;
                } finally {
                    if (transactionSucceeded) {
                        commitTransaction(jdbcDataAccessManager);
                    } else {
                        rollbackTransaction(jdbcDataAccessManager);
                    }
                }
            }
            // if it is not inside the mount, then it could be partially inside a mount and the rest from the local
            // repository.
            boolean foundMounts = false;
            List<LogEntry> logEntries = new LinkedList<LogEntry>();
            for (String sourcePath : dbConfigs.keySet()) {
                if (sourcePath.startsWith(resourcePath)) {
                    // first search everything inside mounts if required.
                    boolean transactionSucceeded = false;
                    JDBCDataAccessManager manager = new JDBCDataAccessManager(dbConfigs.get(sourcePath));
                    List<LogEntry> temp = null;
                    try {
                        beginTransaction(manager);
                        foundMounts = true;
                        temp = internalGetLogs(false, resourcePath, action, userName, from, to, descending,
                                JDBCDatabaseTransaction.getConnection());
                        transactionSucceeded = true;
                    } finally {
                        if (transactionSucceeded) {
                            commitTransaction(manager);
                        } else {
                            rollbackTransaction(manager);
                        }
                    }
                    if (temp == null) {
                        continue;
                    }
                    for (LogEntry entry : temp) {
                        fixLogEntries(logEntries, sourcePath, entry);
                    }
                }
            }
            // finally search inside the local repository.
            logEntries.addAll(internalGetLogs(!foundMounts, resourcePath, action, userName, from, to, descending,
                    JDBCDatabaseTransaction.getConnection()));
            return (foundMounts && logEntries.size() > 0) ?
                    Arrays.asList(getPaginatedLogs(logEntries.toArray(new LogEntry[logEntries.size()]))) : logEntries;
        } else {
            if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
                String msg = "Failed to get logs. Invalid data access manager.";
                log.error(msg);
                throw new RegistryException(msg);
            }
            JDBCDataAccessManager jdbcDataAccessManager = (JDBCDataAccessManager) dataAccessManager;
            boolean foundMounts = false;
            // if you don't have a path, then search all over.
            List<LogEntry> logEntries = new LinkedList<LogEntry>();
            if (dbConfigs.size() > 0) {
                for (String sourcePath : dbConfigs.keySet()) {
                    boolean transactionSucceeded = false;
                    JDBCDataAccessManager manager = new JDBCDataAccessManager(dbConfigs.get(sourcePath));
                    List<LogEntry> temp = null;
                    try {
                        beginTransaction(manager);
                        foundMounts = true;
                        temp = internalGetLogs(false, resourcePath, action, userName, from, to, descending,
                                JDBCDatabaseTransaction.getConnection());
                        transactionSucceeded = true;
                    } finally {
                        if (transactionSucceeded) {
                            commitTransaction(manager);
                        } else {
                            rollbackTransaction(manager);
                        }
                    }
                    if (temp == null) {
                        continue;
                    }
                    for (LogEntry entry : temp) {
                        fixLogEntries(logEntries, sourcePath, entry);
                    }
                }
            }
            logEntries.addAll(internalGetLogs(!foundMounts, resourcePath, action, userName, from, to, descending,
                    JDBCDatabaseTransaction.getConnection()));
            return (foundMounts && logEntries.size() > 0) ?
                    Arrays.asList(getPaginatedLogs(logEntries.toArray(new LogEntry[logEntries.size()]))) : logEntries;
        }
    }

    private void beginTransaction(JDBCDataAccessManager dataAccessManager) throws RegistryException {
        Transaction.pushTransaction();
        dataAccessManager.getTransactionManager().beginTransaction();
    }

    private void commitTransaction(JDBCDataAccessManager dataAccessManager) throws RegistryException {
        dataAccessManager.getTransactionManager().commitTransaction();
        Transaction.popTransaction();
    }

    private void rollbackTransaction(JDBCDataAccessManager dataAccessManager) throws RegistryException {
        dataAccessManager.getTransactionManager().rollbackTransaction();
        Transaction.popTransaction();
    }

    private List<LogEntry> internalGetLogs(boolean paginate, String resourcePath, int action, String userName,
                                           Date from, Date to, boolean descending, Connection conn)
            throws RegistryException {
        try {
            String dbName = conn.getMetaData().getDatabaseProductName();
            if (dbName.contains("Microsoft") || dbName.equals("Oracle")) {
                enableApiPagination = "false";
            }
        } catch (SQLException e) {
            throw new RegistryException("Failed to get Database product name ", e);
        }

        if(conn == null) {
            log.fatal("Failed to get Logs. Communications link failure. The connection to the database could not be acquired.");
            throw new RegistryException("Failed to get Logs. Communications link failure. The connection to the database could not be acquired.");
        }

        PreparedStatement s = null;
        ResultSet results = null;


        boolean paginated = false;
        int start = 0;
        int count = 0;
        String sortOrder ="";
        String sortBy  ="";
        MessageContext messageContext = null;
        //   enableApiPagination is the value of system property - enable.registry.api.paginating
        if (enableApiPagination == null || enableApiPagination.equals("true")) {
            messageContext = MessageContext.getCurrentMessageContext();
            if (messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext)) {

                PaginationContext paginationContext = PaginationUtils.initPaginationContext(messageContext);
                start = paginationContext.getStart();
                count = paginationContext.getCount();
                if(start == 0){
                    start =1;
                }
                sortBy = paginationContext.getSortBy();
                sortOrder = paginationContext.getSortOrder();
                paginated = paginate;
            }
        }
        String sql =
                "SELECT REG_PATH, REG_USER_ID, REG_LOGGED_TIME, REG_ACTION, REG_ACTION_DATA FROM " +
                        "REG_LOG";

        boolean queryStarted = false;
        sql = addWherePart(resourcePath, queryStarted, sql, userName, from, to, action);

        if ("ASC".equals(sortOrder)) {
            descending = false;
        }
        if (descending) {
            sql = sql + " ORDER BY REG_LOGGED_TIME DESC";
        } else {
            sql = sql + " ORDER BY REG_LOGGED_TIME ASC";
        }
        try {
            if (enableApiPagination == null || enableApiPagination.equals("true")) {
                // TYPE_SCROLL_INSENSITIVE and CONCUR_UPDATABLE should be set to move the cursor through the resultSet
                s = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            } else {
                s = conn.prepareStatement(sql);
            }
            int paramNumber = 1;

            if (resourcePath != null) {
                s.setString(paramNumber, resourcePath);
                paramNumber++;
            }

            if (userName != null) {
                s.setString(paramNumber, userName);
                paramNumber++;
            }

            if (from != null) {
                s.setTimestamp(paramNumber, new Timestamp(from.getTime()));
                paramNumber++;
            }

            if (to != null) {
                s.setTimestamp(paramNumber, new Timestamp(to.getTime()));
                paramNumber++;
            }

            if (action != -1) {
                s.setInt(paramNumber, action);
                paramNumber++;
            }
            s.setInt(paramNumber, CurrentSession.getTenantId());

            results = s.executeQuery();

            List<LogEntry> resultList = new ArrayList<LogEntry>();
            if (paginated) {
                //Check start index is a valid one
                if (results.relative(start)) {
                    //This is to get cursor to correct position to execute results.next().
                    results.previous();
                    int i = 0;
                    while (results.next() && i < count) {
                        i++;
                        resultList.add(getLogEntry(results));
                    }
                } else {
                    log.debug("start index doesn't exist in the result set");
                }
                //move the cursor to the last index
                if (results.last()) {
                    log.debug("cursor move to the last index of result set");
                } else {
                    log.debug("cursor doesn't move to the last index of result set");
                }
                //set row count to the message context.
                PaginationUtils.setRowCount(messageContext, Integer.toString(results.getRow()));

            } else {
                while (results.next()) {
                    resultList.add(getLogEntry(results));
                }
                LogEntry[] logEntries = resultList.toArray(new LogEntry[resultList.size()]);
                resultList = Arrays.asList(logEntries);
            }
            return resultList;

        } catch (SQLException e) {

            String msg = "Failed to get logs. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (results != null) {
                        results.close();
                    }
                } finally {
                    if (s != null) {
                        s.close();
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    private static LogEntry[] getPaginatedLogs(LogEntry[] logEntries) {
        LogEntry[] paginatedResult;
        MessageContext messageContext = MessageContext.getCurrentMessageContext();
        if (messageContext != null && PaginationUtils.isPaginationHeadersExist(messageContext)) {

            int rowCount = logEntries.length;
            try {
                PaginationUtils.setRowCount(messageContext, Integer.toString(rowCount));
                PaginationContext paginationContext = PaginationUtils.initPaginationContext(messageContext);

                int start = paginationContext.getStart();
                int count = paginationContext.getCount();

                int startIndex;
                if (start == 1) {
                    startIndex = 0;
                } else {
                    startIndex = start;
                }
                if (rowCount < start + count) {
                    paginatedResult = new LogEntry[rowCount - startIndex];
                    System.arraycopy(logEntries, startIndex, paginatedResult, 0, (rowCount - startIndex));
                } else {
                    paginatedResult = new LogEntry[count];
                    System.arraycopy(logEntries, startIndex, paginatedResult, 0, count);
                }

                return paginatedResult;

            } finally {
                PaginationContext.destroy();
            }
        } else {
            return logEntries;
        }
    }

    private LogEntry getLogEntry(ResultSet results) throws SQLException {

        LogEntry logEntry = new LogEntry();
        logEntry.setResourcePath(results.getString(DatabaseConstants.PATH_FIELD));
        logEntry.setUserName(results.getString(DatabaseConstants.USER_ID_FIELD));
        logEntry.setDate(
                new Date(results.getTimestamp(
                        DatabaseConstants.LOGGED_TIME_FIELD).getTime()));
        logEntry.setAction(results.getInt(DatabaseConstants.ACTION_FIELD));
        logEntry.setActionData(results.getString(DatabaseConstants.ACTION_DATA_FIELD));

        return logEntry;
    }

    // Utility method to add 'WHERE' part to an SQL query.
    private String addWherePart(String resourcePath,
                                boolean queryStarted,
                                String sql,
                                String userName,
                                Date from,
                                Date to,
                                int action) {
        if (resourcePath != null) {
            if (queryStarted) {
                sql = sql + " AND REG_PATH=?";
            } else {
                sql = sql + " WHERE REG_PATH=?";
                queryStarted = true;
            }
        }

        if (userName != null) {
            if (queryStarted) {
                sql = sql + "  AND REG_USER_ID=?";
            } else {
                sql = sql + "  WHERE REG_USER_ID=?";
                queryStarted = true;
            }
        }

        if (from != null) {
            if (queryStarted) {
                sql = sql + " AND REG_LOGGED_TIME>?";
            } else {
                sql = sql + " WHERE REG_LOGGED_TIME>?";
                queryStarted = true;
            }
        }

        if (to != null) {
            if (queryStarted) {
                sql = sql + " AND REG_LOGGED_TIME<?";
            } else {
                sql = sql + " WHERE REG_LOGGED_TIME<?";
                queryStarted = true;
            }
        }

        if (action != -1) {
            if (queryStarted) {
                sql = sql + " AND REG_ACTION=?";
            } else {
                sql = sql + " WHERE REG_ACTION=?";
                queryStarted = true;
            }
        }

        if (queryStarted) {
            sql = sql + " AND REG_TENANT_ID=?";
        } else {
            sql = sql + " WHERE REG_TENANT_ID=?";
        }
        return sql;
    }

    public LogEntry[] getLogs(String path,
                              int action,
                              String userName,
                              Date from,
                              Date to,
                              boolean descending,
                              int start,
                              int pageLen,
                              DataAccessManager dataAccessManager)
            throws RegistryException {
        if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
            String msg = "Failed to get logs. Invalid data access manager.";
            log.error(msg);
            throw new RegistryException(msg);
        }
        String resourcePath = path;
        if (resourcePath != null) {
            // if you have a path, see whether the path is inside the mount or not.
            DataSource dataSource = null;
            if (dbConfigs.size() > 0) {
                for (String sourcePath : dbConfigs.keySet()) {
                    if (resourcePath.startsWith(sourcePath)) {
                        resourcePath = pathMap.get(sourcePath) + resourcePath.substring(sourcePath.length());
                        dataSource = new JDBCDataAccessManager(dbConfigs.get(sourcePath)).getDataSource();
                    }
                }
            }
            if (dataSource != null) {
                // if it is inside the mount then return results only from the mount.
                return internalGetLogs(action, userName, from, to, descending, start, pageLen, resourcePath,
                        dataSource);
            }
            // if it is not inside the mount, then it could be partially inside a mount and the rest from the local
            // repository.
            List<LogEntry> logEntries = new LinkedList<LogEntry>();
            for (String sourcePath : dbConfigs.keySet()) {
                if (sourcePath.startsWith(resourcePath)) {
                    // first search everything inside mounts if required.
                    LogEntry[] temp = internalGetLogs(action, userName, from, to, descending, start, pageLen,
                            resourcePath, new JDBCDataAccessManager(dbConfigs.get(sourcePath)).getDataSource());
                    if (temp == null) {
                        continue;
                    }
                    for (LogEntry entry : temp) {
                        fixLogEntries(logEntries, sourcePath, entry);
                    }
                }
            }
            // finally search inside the local repository.
            logEntries.addAll(Arrays.asList(internalGetLogs(action, userName, from, to, descending, start, pageLen,
                    resourcePath, ((JDBCDataAccessManager) dataAccessManager).getDataSource())));
            return logEntries.toArray(new LogEntry[logEntries.size()]);
        } else {
            // if you don't have a path, then search all over.
            List<LogEntry> logEntries = new LinkedList<LogEntry>();
            if (dbConfigs.size() > 0) {
                for (String sourcePath : dbConfigs.keySet()) {
                    LogEntry[] temp = internalGetLogs(action, userName, from, to, descending, start, pageLen,
                            resourcePath, new JDBCDataAccessManager(dbConfigs.get(sourcePath)).getDataSource());
                    if (temp == null) {
                        continue;
                    }
                    for (LogEntry entry : temp) {
                        fixLogEntries(logEntries, sourcePath, entry);
                    }
                }
            }
            logEntries.addAll(Arrays.asList(internalGetLogs(action, userName, from, to, descending, start, pageLen,
                    resourcePath, ((JDBCDataAccessManager) dataAccessManager).getDataSource())));
            return logEntries.toArray(new LogEntry[logEntries.size()]);
        }
    }

    private void fixLogEntries(List<LogEntry> logEntries, String sourcePath, LogEntry entry) {
        String entryPath = entry.getResourcePath();
        String targetPath = pathMap.get(sourcePath);
        if (entryPath.startsWith(targetPath)) {
            entry.setResourcePath(sourcePath + entryPath.substring(targetPath.length()));
            logEntries.add(entry);
        }
    }

    private LogEntry[] internalGetLogs(int action, String userName, Date from, Date to, boolean descending, int start,
                                       int pageLen, String resourcePath, DataSource dataSource)
            throws RegistryException {
        String sql =
                "SELECT REG_PATH, REG_USER_ID, REG_LOGGED_TIME, REG_ACTION, REG_ACTION_DATA FROM " +
                        "REG_LOG";
        boolean queryStarted = false;
        Connection conn = null;
        PreparedStatement s = null;
        ResultSet results = null;
        sql = addWherePart(resourcePath, queryStarted, sql, userName, from, to, action);

        if (descending) {
            sql = sql + " ORDER BY REG_LOGGED_TIME DESC";
        }
        try {
            conn = dataSource.getConnection();
            s = conn.prepareStatement(sql);

            int paramNumber = 1;

            if (resourcePath != null) {
                s.setString(paramNumber, resourcePath);
                paramNumber++;
            }

            if (userName != null) {
                s.setString(paramNumber, userName);
                paramNumber++;
            }

            if (from != null) {
                s.setTimestamp(paramNumber, new Timestamp(from.getTime()));
                paramNumber++;
            }

            if (to != null) {
                s.setTimestamp(paramNumber, new Timestamp(to.getTime()));
                paramNumber++;
            }

            if (action != -1) {
                s.setInt(paramNumber, action);
                paramNumber++;
            }
            s.setInt(paramNumber, CurrentSession.getTenantId());

            results = s.executeQuery();

            List<LogEntry> resultList = new ArrayList<LogEntry>();
            int current = 0;
            while (results.next()) {
                if (current >= start && (pageLen == -1 || current < start + pageLen)) {
                    LogEntry logEntry = new LogEntry();
                    logEntry.setResourcePath(results.getString(DatabaseConstants.PATH_FIELD));
                    logEntry.setUserName(results.getString(DatabaseConstants.USER_ID_FIELD));
                    logEntry.setDate(
                            new Date(results.getTimestamp(
                                    DatabaseConstants.LOGGED_TIME_FIELD).getTime()));
                    logEntry.setAction(results.getInt(DatabaseConstants.ACTION_FIELD));
                    logEntry.setActionData(results.getString(DatabaseConstants.ACTION_DATA_FIELD));

                    resultList.add(logEntry);
                }
                current++;
            }
            return resultList.toArray(new LogEntry[resultList.size()]);

        } catch (SQLException e) {

            String msg = "Failed to get logs. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (results != null) {
                        results.close();
                    }
                } finally {
                    try {
                        if (s != null) {
                            s.close();
                        }
                    } finally {
                        if (conn != null) {
                            conn.close();
                        }
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    public LogEntry[] getLogs(String path,
                              int action,
                              String userName,
                              Date from,
                              Date to,
                              boolean descending,
                              DataAccessManager dataAccessManager)
            throws RegistryException {

        if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
            String msg = "Failed to get logs. Invalid data access manager.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        String resourcePath = path;
        if (resourcePath != null) {
            // if you have a path, see whether the path is inside the mount or not.
            DataSource dataSource = null;
            if (dbConfigs.size() > 0) {
                for (String sourcePath : dbConfigs.keySet()) {
                    if (resourcePath.startsWith(sourcePath)) {
                        resourcePath = pathMap.get(sourcePath) + resourcePath.substring(sourcePath.length());
                        dataSource = new JDBCDataAccessManager(dbConfigs.get(sourcePath)).getDataSource();
                    }
                }
            }
            if (dataSource != null) {
                // if it is inside the mount then return results only from the mount.
                return internalGetLogs(action, userName, from, to, descending, resourcePath,
                        dataSource);
            }
            // if it is not inside the mount, then it could be partially inside a mount and the rest from the local
            // repository.
            List<LogEntry> logEntries = new LinkedList<LogEntry>();
            for (String sourcePath : dbConfigs.keySet()) {
                if (sourcePath.startsWith(resourcePath)) {
                    // first search everything inside mounts if required.
                    LogEntry[] temp = internalGetLogs(action, userName, from, to, descending,
                            resourcePath, new JDBCDataAccessManager(dbConfigs.get(sourcePath)).getDataSource());
                    if (temp == null) {
                        continue;
                    }
                    for (LogEntry entry : temp) {
                        fixLogEntries(logEntries, sourcePath, entry);
                    }
                }
            }
            // finally search inside the local repository.
            logEntries.addAll(Arrays.asList(internalGetLogs(action, userName, from, to, descending,
                    resourcePath, ((JDBCDataAccessManager) dataAccessManager).getDataSource())));
            return logEntries.toArray(new LogEntry[logEntries.size()]);
        } else {
            // if you don't have a path, then search all over.
            List<LogEntry> logEntries = new LinkedList<LogEntry>();
            if (dbConfigs.size() > 0) {
                for (String sourcePath : dbConfigs.keySet()) {
                    LogEntry[] temp = internalGetLogs(action, userName, from, to, descending,
                            resourcePath, new JDBCDataAccessManager(dbConfigs.get(sourcePath)).getDataSource());
                    if (temp == null) {
                        continue;
                    }
                    for (LogEntry entry : temp) {
                        fixLogEntries(logEntries, sourcePath, entry);
                    }
                }
            }
            logEntries.addAll(Arrays.asList(internalGetLogs(action, userName, from, to, descending,
                    resourcePath, ((JDBCDataAccessManager) dataAccessManager).getDataSource())));
            return logEntries.toArray(new LogEntry[logEntries.size()]);
        }
    }

    private LogEntry[] internalGetLogs(int action, String userName, Date from, Date to, boolean descending,
                                       String resourcePath, DataSource dataSource)
            throws RegistryException {
        boolean queryStarted = false;
        PreparedStatement s = null;
        ResultSet results = null;
        Connection conn = null;
        String sql =
                "SELECT REG_PATH, REG_USER_ID, REG_LOGGED_TIME, REG_ACTION, REG_ACTION_DATA FROM " +
                        "REG_LOG";

        sql = addWherePart(resourcePath, queryStarted, sql, userName, from, to, action);

        if (descending) {
            sql = sql + " ORDER BY REG_LOGGED_TIME DESC";
        }

        try {
            conn = dataSource.getConnection();
            s = conn.prepareStatement(sql);

            int paramNumber = 1;

            if (resourcePath != null) {
                s.setString(paramNumber, resourcePath);
                paramNumber++;
            }

            if (userName != null) {
                s.setString(paramNumber, userName);
                paramNumber++;
            }

            if (from != null) {
                s.setTimestamp(paramNumber, new Timestamp(from.getTime()));
                paramNumber++;
            }

            if (to != null) {
                s.setTimestamp(paramNumber, new Timestamp(to.getTime()));
                paramNumber++;
            }

            if (action != -1) {
                s.setInt(paramNumber, action);
                paramNumber++;
            }

            s.setInt(paramNumber, CurrentSession.getTenantId());

            results = s.executeQuery();

            List<LogEntry> resultList = new ArrayList<LogEntry>();
            while (results.next()) {
                LogEntry logEntry = new LogEntry();
                logEntry.setResourcePath(results.getString(DatabaseConstants.PATH_FIELD));
                logEntry.setUserName(results.getString(DatabaseConstants.USER_ID_FIELD));
                logEntry.setDate(
                        new Date(results.getTimestamp(
                                DatabaseConstants.LOGGED_TIME_FIELD).getTime()));
                logEntry.setAction(results.getInt(DatabaseConstants.ACTION_FIELD));
                logEntry.setActionData(results.getString(DatabaseConstants.ACTION_DATA_FIELD));

                resultList.add(logEntry);
            }
            return resultList.toArray(new LogEntry[resultList.size()]);

        } catch (SQLException e) {

            String msg = "Failed to get logs. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (results != null) {
                        results.close();
                    }
                } finally {
                    try {
                        if (s != null) {
                            s.close();
                        }
                    } finally {
                        if (conn != null) {
                            conn.close();
                        }
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    public int getLogsCount(String resourcePath,
                            int action,
                            String userName,
                            Date from,
                            Date to,
                            boolean descending)
            throws RegistryException {
        int count = 0;

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        String sql = "SELECT COUNT(*) AS REG_LOG_COUNT FROM REG_LOG";

        boolean queryStarted = false;

        sql = addWherePart(resourcePath, queryStarted, sql, userName, from, to, action);

        PreparedStatement s = null;
        ResultSet results = null;
        try {
            s = conn.prepareStatement(sql);

            int paramNumber = 1;

            if (resourcePath != null) {
                s.setString(paramNumber, resourcePath);
                paramNumber++;
            }

            if (userName != null) {
                s.setString(paramNumber, userName);
                paramNumber++;
            }

            if (from != null) {
                s.setTimestamp(paramNumber, new Timestamp(from.getTime()));
                paramNumber++;
            }

            if (to != null) {
                s.setTimestamp(paramNumber, new Timestamp(to.getTime()));
                paramNumber++;
            }

            if (action != -1) {
                s.setInt(paramNumber, action);
                paramNumber++;
            }

            s.setInt(paramNumber, CurrentSession.getTenantId());

            results = s.executeQuery();

            if (results.next()) {
                count = results.getInt(DatabaseConstants.LOG_COUNT_FIELD);
            }


        } catch (SQLException e) {

            String msg = "Failed to get logs. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (results != null) {
                        results.close();
                    }
                } finally {
                    if (s != null) {
                        s.close();
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
        return count;
    }
}
