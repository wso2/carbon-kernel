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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_ACTIVE) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_ACTIVE).trim().equals("")) {
            poolProperties.setMaxActive(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_ACTIVE)));
        } else {
            poolProperties.setMaxActive(DEFAULT_MAX_ACTIVE);
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MIN_IDLE) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.MIN_IDLE).trim().equals("")) {
            poolProperties.setMinIdle(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MIN_IDLE)));
        } else {
            poolProperties.setMinIdle(DEFAULT_MIN_IDLE);
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_IDLE) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_IDLE).trim().equals("")) {
            poolProperties.setMinIdle(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_IDLE)));
        } else {
            poolProperties.setMinIdle(DEFAULT_MAX_IDLE);
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_WAIT) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.MAX_WAIT).trim().equals("")) {
            poolProperties.setMaxWait(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MAX_WAIT)));
        } else {
            poolProperties.setMaxWait(DEFAULT_MAX_WAIT);
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_WHILE_IDLE) != null &&
                !realmConfig.getUserStoreProperty(JDBCRealmConstants.TEST_WHILE_IDLE).trim().equals("")) {
            poolProperties.setTestWhileIdle(Boolean.parseBoolean(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.TEST_WHILE_IDLE)));
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS) != null &&
                !realmConfig.getUserStoreProperty(
                        JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS).trim().equals("")) {
            poolProperties.setTimeBetweenEvictionRunsMillis(Integer.parseInt(
                    realmConfig.getUserStoreProperty(
                            JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS)));
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS) != null &&
                !realmConfig.getUserStoreProperty(
                        JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS).trim().equals("")) {
            poolProperties.setMinEvictableIdleTimeMillis(Integer.parseInt(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS)));
        }

        if (realmConfig.getUserStoreProperty(JDBCRealmConstants.VALIDATION_QUERY) != null) {
            poolProperties.setValidationQuery(realmConfig.getUserStoreProperty(
                    JDBCRealmConstants.VALIDATION_QUERY));
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

        dataSource = new org.apache.tomcat.jdbc.pool.DataSource(poolProperties);
        return dataSource;
    }

    public static String[] getStringValuesFromDatabase(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        String[] values = new String[0];
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
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
            rs = prepStmt.executeQuery();
            List<String> lst = new ArrayList<String>();
            while (rs.next()) {
                String name = rs.getString(1);
                lst.add(name);
            }
            if (lst.size() > 0) {
                values = lst.toArray(new String[lst.size()]);
            }
            return values;
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    /*This retrieves two parameters, combines them and send back*/
    public static String[] getStringValuesFromDatabaseForInternalRoles(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        String[] values = new String[0];
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
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
            rs = prepStmt.executeQuery();
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
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    public static int getIntegerValueFromDatabase(Connection dbConnection, String sqlStmt,
                                                  Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int value = -1;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
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
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                value = rs.getInt(1);
            }
            return value;
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    public static void udpateUserRoleMappingInBatchModeForInternalRoles(Connection dbConnection,
                                                                        String sqlStmt, String primaryDomain, Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
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
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    public static void udpateUserRoleMappingWithExactParams(Connection dbConnection, String sqlStmt,
                                                            String[] roles, String userName,
                                                            Integer[] tenantIds, int currentTenantId)
            throws UserStoreException {
        PreparedStatement ps = null;
        boolean localConnection = false;
        try {
            ps = dbConnection.prepareStatement(sqlStmt);
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
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, ps);
        }
    }

    public static void udpateUserRoleMappingInBatchMode(Connection dbConnection, String sqlStmt,
                                                        Object... params) throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
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
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    public static void updateDatabase(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
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
                    } else if (param instanceof Short) {
                        prepStmt.setShort(i + 1, (Short) param);
                    } else if (param instanceof Date) {
                        Date date = (Date) param;
                        Timestamp time = new Timestamp(date.getTime());
                        prepStmt.setTimestamp(i + 1, time);
                    }
                }
            }
            int count = prepStmt.executeUpdate();
            /*if (log.isDebugEnabled()) {
                log.debug("Executed Query is " + sqlStmt + " and number of updated rows :: "
                        + count);
            }*/
        } catch (SQLException e) {
            String errorMessage = "Using sql : " + sqlStmt + " " + e.getMessage();
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    public static Connection getDBConnection(DataSource dataSource) throws SQLException {
        Connection dbConnection = dataSource.getConnection();
        return dbConnection;
    }

    public static void closeConnection(Connection dbConnection) {

        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage(), e);
            }
        }
    }

    private static void closeResultSet(ResultSet rs) {

        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close result set  - " + e.getMessage(), e);
            }
        }

    }

    private static void closeStatement(PreparedStatement preparedStatement) {

        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " + e.getMessage(), e);
            }
        }

    }

    private static void closeStatements(PreparedStatement... prepStmts) {

        if (prepStmts != null && prepStmts.length > 0) {
            for (PreparedStatement stmt : prepStmts) {
                closeStatement(stmt);
            }
        }

    }

    public static void closeAllConnections(Connection dbConnection, PreparedStatement... prepStmts) {

        closeStatements(prepStmts);
        closeConnection(dbConnection);
    }

    public static void closeAllConnections(Connection dbConnection, ResultSet rs, PreparedStatement... prepStmts) {

        closeResultSet(rs);
        closeStatements(prepStmts);
        closeConnection(dbConnection);
    }

    public static void closeAllConnections(Connection dbConnection, ResultSet rs1, ResultSet rs2,
                                           PreparedStatement... prepStmts) {
        closeResultSet(rs1);
        closeResultSet(rs1);
        closeStatements(prepStmts);
        closeConnection(dbConnection);
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
