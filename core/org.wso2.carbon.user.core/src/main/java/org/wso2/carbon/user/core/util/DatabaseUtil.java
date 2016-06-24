/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.user.core.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DatabaseUtil {

    private static final int DEFAULT_MAX_ACTIVE = 40;
    private static final int DEFAULT_MAX_WAIT = 1000 * 60;
    private static final int DEFAULT_MIN_IDLE = 5;
    private static final int DEFAULT_MAX_IDLE = 6;
    private static Log log = LogFactory.getLog(DatabaseUtil.class);
    private static DataSource dataSource = null;
    private static final String VALIDATION_INTERVAL = "validationInterval";
    private static final long DEFAULT_VALIDATION_INTERVAL = 30000;

    /**
     * Gets a database pooling connection. If a pool is not created this will create a connection pool.
     *
     * @param realmConfig The realm configuration. This includes necessary configuration parameters needed to
     *                    create a database pool.
     *                    <p/>
     *                    NOTE : If we use this there will be a single connection for all tenants. But there might be a requirement
     *                    where different tenants want to connect to multiple data sources. In that case we need to create
     *                    a dataSource for each tenant.
     * @return A database pool.
     */
    public static synchronized DataSource getRealmDataSource(RealmConfiguration realmConfig) {

        if (dataSource == null) {
            return createRealmDataSource(realmConfig);
        } else {
            return dataSource;
        }
    }

    /**
     * Close all database connections in the pool.
     */
    public static synchronized void closeDatabasePoolConnection() {
        if (dataSource != null && dataSource instanceof org.apache.tomcat.jdbc.pool.DataSource) {
            ((org.apache.tomcat.jdbc.pool.DataSource) dataSource).close();
            dataSource = null;
        }
    }

    private static DataSource lookupDataSource(String dataSourceName) {
        try {
            return (DataSource) InitialContext.doLookup(dataSourceName);
        } catch (Exception e) {
            throw new RuntimeException("Error in looking up data source: " + e.getMessage(), e);
        }
    }

    public static DataSource createUserStoreDataSource(RealmConfiguration realmConfig) {
        String dataSourceName = realmConfig.getUserStoreProperty(JDBCRealmConstants.DATASOURCE);
        if (dataSourceName != null) {
            return lookupDataSource(dataSourceName);
        }

        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setDriverClassName(realmConfig.getUserStoreProperty(JDBCRealmConstants.DRIVER_NAME));
        if (poolProperties.getDriverClassName() == null) {
            return null;
        }

        poolProperties.setUrl(realmConfig.getUserStoreProperty(JDBCRealmConstants.URL));
        poolProperties.setUsername(realmConfig.getUserStoreProperty(JDBCRealmConstants.USER_NAME));
        poolProperties.setPassword(realmConfig.getUserStoreProperty(JDBCRealmConstants.PASSWORD));

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_ACTIVE))) {
            poolProperties.setMaxActive(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_ACTIVE)));
        } else {
            poolProperties.setMaxActive(DEFAULT_MAX_ACTIVE);
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.MIN_IDLE))) {
            poolProperties.setMinIdle(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MIN_IDLE)));
        } else {
            poolProperties.setMinIdle(DEFAULT_MIN_IDLE);
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_IDLE))) {
            poolProperties.setMinIdle(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_IDLE)));
        } else {
            poolProperties.setMinIdle(DEFAULT_MAX_IDLE);
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_WAIT))) {
            poolProperties.setMaxWait(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_WAIT)));
        } else {
            poolProperties.setMaxWait(DEFAULT_MAX_WAIT);
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_WHILE_IDLE))) {
            poolProperties.setTestWhileIdle(Boolean.parseBoolean(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.TEST_WHILE_IDLE)));
        }

        if (StringUtils.isNotEmpty( realmConfig.getUserStoreProperty(JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS))) {
            poolProperties.setTimeBetweenEvictionRunsMillis(Integer.parseInt(
                    realmConfig.getUserStoreProperty(
                            JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS)));
        }

        if (StringUtils.isNotEmpty( realmConfig.getUserStoreProperty(JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS))) {
            poolProperties.setMinEvictableIdleTimeMillis(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS)));
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.VALIDATION_QUERY) != null) {
            poolProperties.setValidationQuery(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.VALIDATION_QUERY));
            poolProperties.setTestOnBorrow(true);
        }
        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(VALIDATION_INTERVAL)) &&
            StringUtils.isNumeric(realmConfig.getUserStoreProperty(VALIDATION_INTERVAL))) {
            poolProperties.setValidationInterval(Long.parseLong(realmConfig.getUserStoreProperty(
                    VALIDATION_INTERVAL)));
        } else {
            poolProperties.setValidationInterval(DEFAULT_VALIDATION_INTERVAL);
        }
		
        if (StringUtils.isNotEmpty( realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ABANDONED))){
        	poolProperties.setRemoveAbandoned(Boolean.parseBoolean(realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ABANDONED)));
        }
        
        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.LOG_ABANDONED))){
        	poolProperties.setLogAbandoned(Boolean.parseBoolean(realmConfig.getUserStoreProperty(JDBCRealmConstants.LOG_ABANDONED)));
        }

        if (StringUtils.isNotEmpty(realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ABANDNONED_TIMEOUT))){
        	poolProperties.setRemoveAbandonedTimeout(Integer.parseInt(realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ABANDNONED_TIMEOUT)));
        }
    
        return new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
    }

    private static DataSource createRealmDataSource(RealmConfiguration realmConfig) {
        String dataSourceName = realmConfig.getRealmProperty(JDBCRealmConstants.DATASOURCE);
        if (dataSourceName != null) {
            return lookupDataSource(dataSourceName);
        }
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setDriverClassName(realmConfig.getRealmProperty(JDBCRealmConstants.DRIVER_NAME));
        poolProperties.setUrl(realmConfig.getRealmProperty(JDBCRealmConstants.URL));
        poolProperties.setUsername(realmConfig.getRealmProperty(JDBCRealmConstants.USER_NAME));
        poolProperties.setPassword(realmConfig.getRealmProperty(JDBCRealmConstants.PASSWORD));

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MAX_ACTIVE) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.MAX_ACTIVE).trim().equals("")) {
            poolProperties.setMaxActive(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MAX_ACTIVE)));
        } else {
            poolProperties.setMaxActive(DEFAULT_MAX_ACTIVE);
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MIN_IDLE) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.MIN_IDLE).trim().equals("")) {
            poolProperties.setMinIdle(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MIN_IDLE)));
        } else {
            poolProperties.setMinIdle(DEFAULT_MIN_IDLE);
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MAX_IDLE) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.MAX_IDLE).trim().equals("")) {
            poolProperties.setMinIdle(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MAX_IDLE)));
        } else {
            poolProperties.setMinIdle(DEFAULT_MAX_IDLE);
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MAX_WAIT) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.MAX_WAIT).trim().equals("")) {
            poolProperties.setMaxWait(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MAX_WAIT)));
        } else {
            poolProperties.setMaxWait(DEFAULT_MAX_WAIT);
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.TEST_WHILE_IDLE) != null &&
                !realmConfig.getRealmProperty(JDBCRealmConstants.TEST_WHILE_IDLE).trim().equals("")) {
            poolProperties.setTestWhileIdle(Boolean.parseBoolean(realmConfig.getRealmProperty(
                    JDBCRealmConstants.TEST_WHILE_IDLE)));
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS) != null &&
                !realmConfig.getRealmProperty(
                        JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS).trim().equals("")) {
            poolProperties.setTimeBetweenEvictionRunsMillis(Integer.parseInt(
                    realmConfig.getRealmProperty(
                            JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS)));
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS) != null &&
                !realmConfig.getRealmProperty(
                        JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS).trim().equals("")) {
            poolProperties.setMinEvictableIdleTimeMillis(Integer.parseInt(realmConfig.getRealmProperty(
                    JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS)));
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.VALIDATION_QUERY) != null) {
            poolProperties.setValidationQuery(realmConfig.getRealmProperty(
                    JDBCRealmConstants.VALIDATION_QUERY));
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.REMOVE_ABANDONED) != null) {
            poolProperties.setRemoveAbandoned(Boolean.parseBoolean(realmConfig.getRealmProperty(
            		JDBCRealmConstants.REMOVE_ABANDONED)));
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.LOG_ABANDONED) != null) {
            poolProperties.setLogAbandoned(Boolean.parseBoolean(realmConfig.getRealmProperty(
            		JDBCRealmConstants.LOG_ABANDONED)));
        }

        if (realmConfig.getRealmProperty(JDBCRealmConstants.REMOVE_ABANDNONED_TIMEOUT) != null) {
            poolProperties.setRemoveAbandonedTimeout(Integer.parseInt(realmConfig.getRealmProperty(
            		JDBCRealmConstants.REMOVE_ABANDNONED_TIMEOUT)));
        }

        dataSource = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
        return dataSource;
    }

    public static String[] getStringValuesFromDatabase(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        String[] values = new String[0];

        try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        //allow to send null data since null allowed values can be in the table. eg: domain name
                        prepStmt.setString(i + 1, null);
                        //throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            try(ResultSet rs = prepStmt.executeQuery() ) {
                List<String> lst = new ArrayList<String>();
                while (rs.next()) {
                    String name = rs.getString(1);
                    lst.add(name);
                }
                if (lst.size() > 0) {
                    values = lst.toArray(new String[lst.size()]);
                }
                return values;
            }
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);

        } finally {
            close(dbConnection);
        }
    }

    /*This retrieves two parameters, combines them and send back*/
    public static String[] getStringValuesFromDatabaseForInternalRoles(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        String[] values = new String[0];
        //PreparedStatement prepStmt = null;
        try(PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt) ) {
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            try (ResultSet rs = prepStmt.executeQuery()) {
                List<String> lst = new ArrayList<String>();
                while (rs.next()) {
                    String name = rs.getString(1);
                    String domain = rs.getString(2);
                    if (domain != null) {
                        name = UserCoreUtil.addDomainToName(name, domain);
                    }
                    lst.add(name);
                }
                if (lst.size() > 0) {
                    values = lst.toArray(new String[lst.size()]);
                }
                return values;
            }
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }finally {
            close(dbConnection);
        }
    }

    public static int getIntegerValueFromDatabase(Connection dbConnection, String sqlStmt,
                                                  Object... params) throws UserStoreException {

        int value = -1;
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)){
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            try(ResultSet rs = prepStmt.executeQuery()) {
                if (rs.next()) {
                    value = rs.getInt(1);
                }
                return value;
            }
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }finally {
            close(dbConnection);
        }
    }

    public static void udpateUserRoleMappingInBatchModeForInternalRoles(Connection dbConnection,
                                                                        String sqlStmt, String primaryDomain, Object... params) throws UserStoreException {
        boolean localConnection = false;
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            int batchParamIndex = -1;
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String[]) {
                        batchParamIndex = i;
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            if (batchParamIndex != -1) {
                String[] values = (String[]) params[batchParamIndex];
                for (String value : values) {
                    String strParam = (String) value;
                    //add domain if not set
                    strParam = UserCoreUtil.addDomainToName(strParam, primaryDomain);
                    //get domain from name
                    String domainParam = UserCoreUtil.extractDomainFromName(strParam);
                    if (domainParam != null) {
                        domainParam = domainParam.toUpperCase();
                    }
                    //set domain to sql
                    prepStmt.setString(params.length + 1, domainParam);
                    //remove domain before persisting
                    String nameWithoutDomain = UserCoreUtil.removeDomainFromName(strParam);
                    //set name in sql
                    prepStmt.setString(batchParamIndex + 1, nameWithoutDomain);
                    prepStmt.addBatch();
                }
            }

            int[] count = prepStmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Executed a batch update. Query is : " + sqlStmt + ": and result is"
                        + Arrays.toString(count));
            }
            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {  // can remove since this is not needed with try-with-resources -
            close(dbConnection);
        }
    }

    public static void udpateUserRoleMappingWithExactParams(Connection dbConnection, String sqlStmt,
                                                            String[] roles, String userName,
                                                            Integer[] tenantIds, int currentTenantId)
            throws UserStoreException {
        boolean localConnection = false;
        try (PreparedStatement ps = dbConnection.prepareStatement(sqlStmt)){
            byte count = 0;
            byte index = 0;

            for (String role : roles) {
                count = 0;
                ps.setString(++count, role);
                ps.setInt(++count, tenantIds[index]);
                ps.setString(++count, userName);
                ps.setInt(++count, currentTenantId);
                ps.setInt(++count, currentTenantId);
                ps.setInt(++count, tenantIds[index]);

                ps.addBatch();
                ++index;
            }

            int[] cnt = ps.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Executed a batch update. Query is : " + sqlStmt + ": and result is" +
                        Arrays.toString(cnt));
            }
            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            close(dbConnection);
        }
    }

    public static void udpateUserRoleMappingInBatchMode(Connection dbConnection, String sqlStmt,
                                                        Object... params) throws UserStoreException {
        boolean localConnection = false;
        try (PreparedStatement prepStmt = dbConnection.prepareStatement(sqlStmt)) {
            int batchParamIndex = -1;
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Null data provided.");
                    } else if (param instanceof String[]) {
                        batchParamIndex = i;
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    }
                }
            }
            if (batchParamIndex != -1) {
                String[] values = (String[]) params[batchParamIndex];
                for (String value : values) {
                    prepStmt.setString(batchParamIndex + 1, value);
                    prepStmt.addBatch();
                }
            }

            int[] count = prepStmt.executeBatch();
            if (log.isDebugEnabled()) {
                log.debug("Executed a batch update. Query is : " + sqlStmt + ": and result is"
                        + Arrays.toString(count));
            }
            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            if (localConnection) {
				close(dbConnection);
            }
        }
    }

    private static PreparedStatement getPreparedStatement(Connection dbConnection, String sqlStmt, Object... params) throws SQLException {

        PreparedStatement preparedStatement = dbConnection.prepareStatement(sqlStmt);
        if (params != null) {

            int index = 1;
            for (Object param : params) {
                if (param == null || param instanceof String){
                    //allow to send null data since null allowed values can be in the table. eg: domain name
                    preparedStatement.setString(index++, (String) param);
                }else if (param instanceof Integer){
                    preparedStatement.setInt(index++, (Integer) param);
                }else if (param instanceof Short) {
                    preparedStatement.setShort(index++, (Short) param);
                }else if (param instanceof Date) {
                    Timestamp time = new Timestamp(((Date) param).getTime());
                    preparedStatement.setTimestamp(index++, time);
                }
            }
        }
        return preparedStatement;
    }

    public static void updateDatabase(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        try ( PreparedStatement prepStmt = getPreparedStatement(dbConnection, sqlStmt,params)) {
            prepStmt.executeUpdate();
            DatabaseUtil.closeAllConnections(null, prepStmt);
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            close(dbConnection);
        }
    }

    public static Connection getDBConnection(DataSource dataSource) throws SQLException {
        Connection dbConnection = dataSource.getConnection();
        dbConnection.setAutoCommit(false);
        if (dbConnection.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
        return dbConnection;
    }

    public static void closeConnection(Connection dbConnection) {
        close(dbConnection);
    }

    /**
     *
     * @param dbObject -  java.sql object types that implement both Wrapper and AutoCloseable interfaces
     */
    private static <AutoClosableWrapper extends AutoCloseable & Wrapper> void close(AutoClosableWrapper dbObject) {
        if (dbObject != null) {
            try {
                dbObject.close();
            } catch (SQLRecoverableException ex) {
                handleSQLRecoverableException(dbObject, ex);
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("An unknown error occurred during close operation" + e.getMessage(), e);
            } finally {
                dbObject = null;
            }
        }
    }

    private static <AutoClosableWrapper extends AutoCloseable & Wrapper>  void handleSQLRecoverableException(AutoClosableWrapper dbObject, SQLRecoverableException recException){
        try {
            log.error("SQLRecoverable exception encountered.  Attempting recovery.", recException);
            dbObject.close();
            try (Connection connection = getDBConnection(dataSource)) {
                connection.close();
            } catch (SQLException | NullPointerException e) {
                if (dataSource == null)
                    log.error("A null datasource was encountered during SQLRecoverableException handling recovery operation - exiting recovery. " + e.getMessage(), e);
                else
                    log.error("An error occurred during SQLRecoverableException handling recovery operation - exiting recovery." + e.getMessage() , e);
            }
        } catch (SQLException sqlEx){
            log.error("An error occurred during SQLRecoverableException handling close operation  - continuing with errors. " + sqlEx.getMessage(), sqlEx);
        } catch (Exception e) {
            log.error("An unknown error occurred during SQLRecoverableException handling close operation. " + e.getMessage(), e);
        }
    }

    private static void closeStatements(PreparedStatement... prepStmts) {
        if (prepStmts != null && prepStmts.length > 0) {
            for (PreparedStatement stmt : prepStmts) {
                close(stmt);
            }
        }
    }

    public static void close(Connection dbConnection, PreparedStatement... prepStmts) {
        closeStatements(prepStmts);
        close(dbConnection);
    }

    public static void close(Connection dbConnection, ResultSet rs, PreparedStatement... prepStmts) {
        close(rs);
        closeStatements(prepStmts);
        close(dbConnection);
    }
    public static void close(Connection dbConnection, ResultSet rs1, ResultSet rs2,
                                           PreparedStatement... prepStmts) {
        close(rs1);
        close(rs2);
        closeStatements(prepStmts);
        close(dbConnection);
    }

    /**
     * Recommend:
     * @deprecated Should discontinue use in favor of the parametrized close method.
     */
    public static void closeAllConnections(Connection dbConnection, PreparedStatement... prepStmts) {
        close(dbConnection, prepStmts);
    }

    /**
     * Recommend:
     * @deprecated Should discontinue use in favor of the parametrized close method.
     */
    public static void closeAllConnections(Connection dbConnection, ResultSet rs, PreparedStatement... prepStmts) {
        close(dbConnection,rs,prepStmts);
    }

    /**
     * Recommend:
     * @deprecated Should discontinue use in favor of the parametrized close method.
     */
    public static void closeAllConnections(Connection dbConnection, ResultSet rs1, ResultSet rs2,
                                           PreparedStatement... prepStmts) {
        close(dbConnection, rs1, rs2, prepStmts);
    }


    public static void rollBack(Connection dbConnection) {
        try {
            if (dbConnection != null) {
                dbConnection.rollback();
            }
        } catch (SQLException e1) {
            log.error("An error occurred while rolling back transactions. ", e1);
        }
    }
}
