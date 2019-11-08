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

package org.wso2.carbon.user.core.jdbc;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.constants.UserCoreClaimConstants;
import org.wso2.carbon.user.core.jdbc.caseinsensitive.JDBCCaseInsensitiveConstants;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.sql.DataSource;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLTimeoutException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_USER;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_ROLE;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE;
import static org.wso2.carbon.user.core.util.DatabaseUtil.getLoggableSqlString;

public class UniqueIDJDBCUserStoreManager extends JDBCUserStoreManager {

    // private boolean useOnlyInternalRoles;
    private static Log log = LogFactory.getLog(UniqueIDJDBCUserStoreManager.class);

    private static final String QUERY_FILTER_STRING_ANY = "*";
    private static final String SQL_FILTER_STRING_ANY = "%";
    private static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";
    private static final String SHA_1_PRNG = "SHA1PRNG";

    protected DataSource jdbcds = null;
    protected Random random = new Random();
    protected int maximumUserNameListLength = -1;
    protected int queryTimeout = -1;

    public UniqueIDJDBCUserStoreManager() {

    }

    /**
     * @param realmConfig
     * @param tenantId
     * @throws UserStoreException
     */
    public UniqueIDJDBCUserStoreManager(RealmConfiguration realmConfig, int tenantId) throws UserStoreException {
        super(realmConfig, tenantId);
    }

    /**
     * This constructor is used by the support IS
     *
     * @param ds
     * @param realmConfig
     * @param tenantId
     * @param addInitData
     * @param tenantId
     */
    public UniqueIDJDBCUserStoreManager(DataSource ds, RealmConfiguration realmConfig, int tenantId,
            boolean addInitData) throws UserStoreException {

        super(ds, realmConfig, tenantId, addInitData);
    }

    /**
     * This constructor to accommodate PasswordUpdater called from chpasswd script
     *
     * @param ds
     * @param realmConfig
     * @throws UserStoreException
     */
    public UniqueIDJDBCUserStoreManager(DataSource ds, RealmConfiguration realmConfig) throws UserStoreException {

        super(ds, realmConfig);
    }

    /**
     * @param realmConfig
     * @param properties
     * @param claimManager
     * @param profileManager
     * @param realm
     * @param tenantId
     * @throws UserStoreException
     */
    public UniqueIDJDBCUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
    }

    /**
     * @param realmConfig
     * @param properties
     * @param claimManager
     * @param profileManager
     * @param realm
     * @param tenantId
     * @param skipInitData
     * @throws UserStoreException
     */
    public UniqueIDJDBCUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId,
            boolean skipInitData) throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, skipInitData);
    }

    // Loading JDBC data store on demand.
    private DataSource getJDBCDataSource() throws UserStoreException {
        if (jdbcds == null) {
            jdbcds = loadUserStoreSpacificDataSoruce();
        }
        return jdbcds;
    }

    @Override
    public User[] doListUsersWithID(String filter, int maxItemLimit) throws UserStoreException {

        User[] users = null;
        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String userNameAttribute = this.getUserNameMappedAttribute();

        if (maxItemLimit == 0) {
            return new User[0];
        }

        int givenMax;
        int searchTime;
        try {
            givenMax = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (Exception e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }

        try {
            searchTime = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (Exception e) {
            searchTime = UserCoreConstants.MAX_SEARCH_TIME;
        }

        if (maxItemLimit < 0 || maxItemLimit > givenMax) {
            maxItemLimit = givenMax;
        }

        try {

            if (filter != null && filter.trim().length() != 0) {
                filter = filter.trim();
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
            } else {
                filter = "%";
            }

            List<User> userList = new ArrayList<>();

            dbConnection = getDBConnection();

            if (dbConnection == null) {
                throw new UserStoreException("null connection");
            }

            if (isCaseSensitiveUsername()) {
                sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USER_FILTER_WITH_ID);
            } else {
                sqlStmt = realmConfig
                        .getUserStoreProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE);
            }

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, userNameAttribute);
            prepStmt.setString(2, filter);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(4, tenantId);
                prepStmt.setInt(5, tenantId);
            }

            prepStmt.setMaxRows(maxItemLimit);
            try {
                prepStmt.setQueryTimeout(searchTime);
            } catch (Exception e) {
                // this can be ignored since timeout method is not implemented
                log.debug(e);
            }

            try {
                rs = prepStmt.executeQuery();
            } catch (SQLException e) {
                if (e instanceof SQLTimeoutException) {
                    log.error("The cause might be a time out. Hence ignored", e);
                    return users;
                }
                String errorMessage =
                        "Error while fetching users according to filter : " + filter + " & max Item limit " + ": "
                                + maxItemLimit;
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
                throw new UserStoreException(errorMessage, e);
            }

            while (rs.next()) {

                String userID = rs.getString(1);
                String userName = rs.getString(2);
                if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userID)) {
                    continue;
                }

                RealmService realmService = UserCoreUtil.getRealmService();
                User user = new User(userID, userName, userName);
                try {
                    user.setTenantDomain(realmService.getTenantManager().getDomain(tenantId));
                    user.setUserStoreDomain(UserCoreUtil.getDomainName(realmConfig));
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    throw new UserStoreException(e);
                }
                userList.add(user);
            }
            rs.close();

            if (userList.size() > 0) {
                users = userList.stream().toArray(User[]::new);
            }

        } catch (SQLException e) {
            String msg = "Error occurred while retrieving users for filter : " + filter + " & max Item limit : "
                    + maxItemLimit;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return users;

    }

    private String getUserFilterQuery(String caseSensitiveQueryPropertyName, String caseInsensitiveQueryPropertyName) {

        String sqlStmt;
        if (isCaseSensitiveUsername()) {
            sqlStmt = realmConfig.getUserStoreProperty(caseSensitiveQueryPropertyName);
        } else {
            sqlStmt = realmConfig.getUserStoreProperty(caseInsensitiveQueryPropertyName);
        }
        return sqlStmt;
    }

    @Override
    public boolean doCheckIsUserInRoleWithID(String userID, String roleName) throws UserStoreException {

        String[] roles = doGetExternalRoleListOfUserWithID(userID, roleName);
        if (roles != null) {
            for (String role : roles) {
                if (role.equalsIgnoreCase(roleName)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public User[] doGetUserListOfRoleWithID(String roleName, String filter) throws UserStoreException {

        RoleContext roleContext = createRoleContext(roleName);
        return getUserListOfJDBCRoleWithID(roleContext, filter);
    }

    public User[] getUserListOfJDBCRoleWithID(RoleContext ctx, String filter) throws UserStoreException {

        return getUserListOfJDBCRoleWithID(ctx, filter, QUERY_MAX_ITEM_LIMIT_ANY);
    }

    @Override
    public User[] doGetUserListOfRoleWithID(String roleName, String filter, int maxItemLimit)
            throws UserStoreException {

        RoleContext roleContext = createRoleContext(roleName);
        return getUserListOfJDBCRoleWithID(roleContext, filter, maxItemLimit);
    }

    /**
     * Return the list of users belong to the given JDBC role for the given {@link RoleContext}, filter and max item
     * limit.
     *
     * @param ctx          {@link RoleContext} corresponding to the JDBC role.
     * @param filter       String filter for the users.
     * @param maxItemLimit Maximum number of items returned.
     * @return The list of users matching the provided constraints.
     * @throws UserStoreException
     */
    public User[] getUserListOfJDBCRoleWithID(RoleContext ctx, String filter, int maxItemLimit)
            throws UserStoreException {

        String roleName = ctx.getRoleName();
        User[] users = null;
        String sqlStmt;
        String mappedAttribute = this.getUserNameMappedAttribute();

        if (maxItemLimit == 0) {
            return new User[0];
        }

        if (maxItemLimit < 0 || maxItemLimit > maximumUserNameListLength) {
            maxItemLimit = maximumUserNameListLength;
        }

        if (StringUtils.isNotEmpty(filter)) {
            filter = filter.trim();
            filter = filter.replace("*", "%");
            filter = filter.replace("?", "_");
        } else {
            filter = "%";
        }

        if (!ctx.isShared()) {
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_IN_ROLE_FILTER_WITH_ID);
            if (sqlStmt == null) {
                throw new UserStoreException("The sql statement for retrieving user roles is null");
            }

            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                users = getUsersFromDatabaseWithConstraints(sqlStmt, maxItemLimit, queryTimeout, mappedAttribute,
                        filter, roleName, tenantId, tenantId, tenantId, tenantId);
            } else {
                users = getUsersFromDatabaseWithConstraints(sqlStmt, maxItemLimit, queryTimeout, mappedAttribute,
                        filter, roleName);
            }

        } else if (ctx.isShared()) {
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_FILTER_WITH_ID);
            users = getUsersFromDatabaseWithConstraints(sqlStmt, maxItemLimit, queryTimeout, mappedAttribute, filter,
                    roleName);
        }

        if (users != null) {

            for (User user : users) {
                RealmService realmService = UserCoreUtil.getRealmService();
                try {
                    user.setTenantDomain(realmService.getTenantManager().getDomain(tenantId));
                    user.setUserStoreDomain(UserCoreUtil.getDomainName(realmConfig));
                } catch (org.wso2.carbon.user.api.UserStoreException e) {
                    throw new UserStoreException(e);
                }
            }
        }
        log.debug("Roles are not defined for the role name " + roleName);

        return users;

    }

    @Override
    public String[] getProfileNamesWithID(String userID) throws UserStoreException {

        userID = UserCoreUtil.removeDomainFromName(userID);
        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_WITH_ID);

        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for retrieving is null.");
        }
        String[] names;
        if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
            names = getStringValuesFromDatabase(sqlStmt, userID, tenantId, tenantId);
        } else {
            names = getStringValuesFromDatabase(sqlStmt, userID);
        }
        if (names.length == 0) {
            names = new String[] { UserCoreConstants.DEFAULT_PROFILE };
        } else {
            Arrays.sort(names);
            if (Arrays.binarySearch(names, UserCoreConstants.DEFAULT_PROFILE) < 0) {
                // we have to add the default profile
                String[] newNames = new String[names.length + 1];
                int i = 0;
                for (i = 0; i < names.length; i++) {
                    newNames[i] = names[i];
                }
                newNames[i] = UserCoreConstants.DEFAULT_PROFILE;
                names = newNames;
            }
        }

        return names;
    }

    @Override
    public int getUserIdWithID(String userID) throws UserStoreException {

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERID_FROM_USERNAME_WITH_ID);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for retrieving ID is null");
        }
        int id;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, userID, tenantId);
            } else {
                id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, userID);
            }
        } catch (SQLException e) {
            String errorMessage = "Error occurred while getting user id from user ID : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        return id;
    }

    @Override
    public int getTenantIdWithID(String userID) throws UserStoreException {

        if (this.tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            throw new UserStoreException("Not allowed to perform this operation");
        }
        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME_WITH_ID);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for retrieving ID is null");
        }
        int id;
        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            id = DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, userID);
        } catch (SQLException e) {
            String errorMessage = "Error occurred while getting tenant ID from username : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        return id;
    }

    @Override
    public Map<String, String> getUserPropertyValuesWithID(String userID, String[] propertyNames, String profileName)
            throws UserStoreException {

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String[] propertyNamesSorted = propertyNames.clone();
        Arrays.sort(propertyNamesSorted);
        Map<String, String> map = new HashMap<>();
        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_PROPS_FOR_PROFILE_WITH_ID);
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, userID);
            prepStmt.setString(2, profileName);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(3, tenantId);
                prepStmt.setInt(4, tenantId);
            }
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                String value = rs.getString(2);
                if (Arrays.binarySearch(propertyNamesSorted, name) < 0) {
                    continue;
                }
                map.put(name, value);
            }

            return map;
        } catch (SQLException e) {
            String errorMessage =
                    "Error Occurred while getting property values for user : " + userID + " & profile name : "
                            + profileName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    private String[] getStringValuesFromDatabase(String sqlStmt, Object... params) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Query: " + sqlStmt);
            for (int i = 0; i < params.length; i++) {
                Object param = params[i];
                log.debug("Input value: " + param);
            }
        }

        String[] values;
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        try {
            dbConnection = getDBConnection();
            values = DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt, params);
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving string values.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return values;
    }

    /**
     * Get {@link User}[] of users from the database for the given SQL query and the constraints.
     *
     * @param sqlStmt      {@link String} SQL query.
     * @param maxRows      Upper limit to the number of rows returned from the database.
     * @param queryTimeout SQL query timeout limit in seconds. Zero means there is no limit.
     * @param params       Values passed for the SQL query placeholders.
     * @return {@link User}[] of results.
     * @throws UserStoreException
     */
    private User[] getUsersFromDatabaseWithConstraints(String sqlStmt, int maxRows, int queryTimeout, Object... params)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            String loggableSqlString = getLoggableSqlString(sqlStmt, params);
            String msg = "Using SQL : " + loggableSqlString + ", and maxRows: " + maxRows + ", and queryTimeout: "
                    + queryTimeout;
            log.debug(msg);
        }

        User[] values;
        try (Connection dbConnection = getDBConnection()) {
            values = DatabaseUtil
                    .getUsersFromDatabaseWithConstraints(dbConnection, sqlStmt, maxRows, queryTimeout, params);
        } catch (SQLException e) {
            String msg = "Error occurred while accessing the database connection.";
            throw new UserStoreException(msg, e);
        }
        return values;
    }

    private String[] getRoleNamesWithDomainWithID(String sqlStmt, String userID, int tenantId, boolean appendDn)
            throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> roles = new ArrayList<>();
        try {
            dbConnection = getDBConnection();
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            byte count = 0;
            prepStmt.setString(++count, userID);
            prepStmt.setInt(++count, tenantId);

            rs = prepStmt.executeQuery();
            // append the domain if exist
            String domain = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

            while (rs.next()) {
                String name = rs.getString(1);
                int tenant = rs.getInt(2);
                // boolean shared = rs.getBoolean(3);

                String role = name;
                if (appendDn) {
                    name = UserCoreUtil.addTenantDomainToEntry(name, String.valueOf(tenant));
                }
                roles.add(role);
            }

        } catch (SQLException e) {
            String msg =
                    "Error occurred while retrieving role name with tenant id : " + tenantId + " & user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return roles.toArray(new String[roles.size()]);
    }

    /**
     * @return
     * @throws SQLException
     * @throws UserStoreException
     */
    protected Connection getDBConnection() throws SQLException, UserStoreException {
        Connection dbConnection = getJDBCDataSource().getConnection();
        dbConnection.setAutoCommit(false);
        if (dbConnection.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        }
        return dbConnection;
    }

    /**
     * @param sqlStmt
     * @param dbConnection
     * @param params
     * @return
     * @throws UserStoreException
     */
    protected boolean isValueExisting(String sqlStmt, Connection dbConnection, Object... params)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isExisting = false;
        boolean doClose = false;
        try {
            if (dbConnection == null) {
                dbConnection = getDBConnection();
                doClose = true; // because we created it
            }
            if (DatabaseUtil.getIntegerValueFromDatabase(dbConnection, sqlStmt, params) > -1) {
                isExisting = true;
            }
            return isExisting;
        } catch (SQLException e) {
            String msg = "Error occurred while checking existence of values.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            if (doClose) {
                DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            }
        }
    }

    @Override
    public boolean doCheckExistingUserWithID(String userID) throws UserStoreException {

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_IS_USER_EXISTING_WITH_ID);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for is user existing null.");
        }
        boolean isExisting;

        String isUnique = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USERNAME_UNIQUE);
        if (Boolean.parseBoolean(isUnique) && !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userID)) {
            String uniquenesSql = realmConfig.getUserStoreProperty(JDBCRealmConstants.USER_ID_UNIQUE_SQL_WITH_ID);
            isExisting = isValueExisting(uniquenesSql, null, userID);
            if (log.isDebugEnabled()) {
                log.debug("The user ID should be unique across tenants.");
            }
        } else {
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                isExisting = isValueExisting(sqlStmt, null, userID, tenantId);
            } else {
                isExisting = isValueExisting(sqlStmt, null, userID);
            }
        }

        return isExisting;
    }

    @Override
    public boolean doCheckExistingUserName(String userName) throws UserStoreException {

        String sqlStmt;
        String mappedAttribute = this.getUserNameMappedAttribute();

        if (isCaseSensitiveUsername()) {
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_IS_USER_NAME_EXISTING);
        } else {
            sqlStmt = realmConfig
                    .getUserStoreProperty(JDBCCaseInsensitiveConstants.GET_IS_USER_NAME_EXISTING_CASE_INSENSITIVE);
        }
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for is user existing null.");
        }
        boolean isExisting;

        String isUnique = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USERNAME_UNIQUE);
        if (Boolean.parseBoolean(isUnique) && !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
            String uniquenesSql;
            if (isCaseSensitiveUsername()) {
                uniquenesSql = realmConfig.getUserStoreProperty(JDBCRealmConstants.USER_NAME_UNIQUE_WITH_ID);
            } else {
                uniquenesSql = realmConfig
                        .getUserStoreProperty(JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_CASE_INSENSITIVE_WITH_ID);
            }
            isExisting = isValueExisting(uniquenesSql, null, mappedAttribute, userName);
            if (log.isDebugEnabled()) {
                log.debug("The username should be unique across tenants.");
            }
        } else {
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                isExisting = isValueExisting(sqlStmt, null, mappedAttribute, userName, tenantId);
            } else {
                isExisting = isValueExisting(sqlStmt, null, mappedAttribute, userName);
            }
        }

        return isExisting;
    }

    @Override
    public User doAuthenticateWithID(String preferredUserNameProperty, String preferredUserNameValue, Object credential,
            String profileName) throws UserStoreException {

        User user = null;
        if (!checkUserNameValid(preferredUserNameValue)) {
            if (log.isDebugEnabled()) {
                log.debug("Username validation failed");
            }
            return null;
        }

        if (!checkUserPasswordValid(credential)) {
            if (log.isDebugEnabled()) {
                log.debug("Password validation failed");
            }
            return null;
        }

        if (UserCoreUtil.isRegistryAnnonymousUser(preferredUserNameValue)) {
            log.error("Anonymous user trying to login");
            return null;
        }

        // add the properties
        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        Connection dbConnection = null;
        ResultSet rs = null;
        PreparedStatement prepStmt = null;
        String sqlstmt;
        String password;
        boolean isAuthed = false;

        try {

            dbConnection = getDBConnection();
            dbConnection.setAutoCommit(false);

            if (isCaseSensitiveUsername()) {
                sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER_WITH_ID);
            } else {
                sqlstmt = realmConfig
                        .getUserStoreProperty(JDBCCaseInsensitiveConstants.SELECT_USER_WITH_ID_CASE_INSENSITIVE);
            }

            if (log.isDebugEnabled()) {
                log.debug(sqlstmt);
            }

            prepStmt = dbConnection.prepareStatement(sqlstmt);
            prepStmt.setString(1, preferredUserNameProperty);
            prepStmt.setString(2, preferredUserNameValue);
            prepStmt.setString(3, profileName);
            if (sqlstmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(4, tenantId);
                prepStmt.setInt(5, tenantId);
            }

            rs = prepStmt.executeQuery();

            int count = 0;
            while (rs.next()) {

                // Handle multiple matching users.
                count++;
                if (count > 1) {
                    if (log.isDebugEnabled()) {
                        log.debug("Invalid scenario. Multiple users found for the given username property: "
                                + preferredUserNameProperty + " and value: " + preferredUserNameValue);
                    }
                    isAuthed = false;
                    user = null;
                    break;
                }

                String userID = rs.getString(1);
                String storedPassword = rs.getString(2);
                String saltValue = null;
                if ("true".equalsIgnoreCase(
                        realmConfig.getUserStoreProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                    saltValue = rs.getString(3);
                }

                boolean requireChange = rs.getBoolean(4);
                Timestamp changedTime = rs.getTimestamp(5);

                GregorianCalendar gc = new GregorianCalendar();
                gc.add(GregorianCalendar.HOUR, -24);
                Date date = gc.getTime();

                if (requireChange && changedTime.before(date)) {
                    isAuthed = false;
                } else {
                    password = preparePassword(credential, saltValue);
                    if ((storedPassword != null) && (storedPassword.equals(password))) {
                        isAuthed = true;
                        RealmService realmService = UserCoreUtil.getRealmService();
                        user = new User(userID,
                                getUserClaimValueWithID(userID, UserCoreClaimConstants.USERNAME_CLAIM_URI, profileName),
                                preferredUserNameValue, null, null, null);
                        try {
                            user.setTenantDomain(realmService.getTenantManager().getDomain(tenantId));
                            user.setUserStoreDomain(UserCoreUtil.getDomainName(
                                    CarbonContext.getThreadLocalCarbonContext().getUserRealm()
                                            .getRealmConfiguration()));
                        } catch (org.wso2.carbon.user.api.UserStoreException e) {
                            throw new UserStoreException(e);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            String msg =
                    "Error occurred while retrieving user authentication info for user : " + preferredUserNameValue;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException("Authentication Failure", e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        if (log.isDebugEnabled()) {
            log.debug("User " + preferredUserNameValue + " login attempt. Login success :: " + isAuthed);
        }

        return user;

    }

    @Override
    public User doAddUserWithID(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {

        // Assigning unique user ID of the user as the username in the system.
        String userID = getUniqueUserID();
        // Assign preferredUsername as the username claim.
        claims = addUserNameAttribute(userName, claims);
        persistUser(userID, credential, roleList, claims, profileName, requirePasswordChange);

        RealmService realmService = UserCoreUtil.getRealmService();
        User user = new User(userID, userName, userName);
        try {
            user.setTenantDomain(realmService.getTenantManager().getDomain(tenantId));
            user.setUserStoreDomain(UserCoreUtil.getDomainName(realmConfig));
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(e);
        }
        return user;

    }

    /*
     * This method persists the user information in the database.
     */
    protected void persistUser(String userID, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {

        Connection dbConnection;
        try {
            dbConnection = getDBConnection();
        } catch (SQLException e) {
            String errorMessage = "Error occurred while getting DB connection.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }

        Secret credentialObj;
        try {
            credentialObj = Secret.getSecret(credential);
        } catch (UnsupportedSecretTypeException e) {
            throw new UserStoreException("Unsupported credential type.", e);
        }

        try {
            String sqlStmt1 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_WITH_ID);
            String saltValue = null;

            if ("true".equalsIgnoreCase(
                    realmConfig.getUserStoreProperties().get(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                saltValue = generateSaltValue();
            }

            String password = this.preparePassword(credentialObj, saltValue);

            // do all 4 possibilities
            if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue == null)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, password, "", requirePasswordChange,
                        new Date(), tenantId);
            } else if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue != null)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, password, saltValue,
                        requirePasswordChange, new Date(), tenantId);
            } else if (!sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue == null)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, password, "", requirePasswordChange,
                        new Date());
            } else {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, password, saltValue,
                        requirePasswordChange, new Date());
            }

            if (roleList != null && roleList.length > 0) {

                RoleBreakdown breakdown = getSharedRoleBreakdown(roleList);
                String[] roles = breakdown.getRoles();

                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantids();

                String sqlStmt2;
                String type = DatabaseCreator.getDatabaseType(dbConnection);
                if (roles.length > 0) {
                    // Adding user to the non shared roles
                    sqlStmt2 = realmConfig
                            .getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_WITH_ID + "-" + type);
                    if (sqlStmt2 == null) {
                        sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_WITH_ID);
                    }

                    if (sqlStmt2.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                        if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                            DatabaseUtil
                                    .udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, tenantId, roles, tenantId,
                                            userID, tenantId);
                        } else {
                            DatabaseUtil
                                    .udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, roles, tenantId, userID,
                                            tenantId, tenantId);
                        }
                    } else {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, roleList, userID);
                    }

                }
                if (sharedRoles.length > 0) {
                    // Adding user to the shared roles
                    sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_WITH_ID);
                    DatabaseUtil.udpateUserRoleMappingWithExactParams(dbConnection, sqlStmt2, sharedRoles, userID,
                            sharedTenantIds, tenantId);
                }

            }

            if (claims != null) {
                // add the properties
                if (profileName == null) {
                    profileName = UserCoreConstants.DEFAULT_PROFILE;
                }

                addPropertiesWithID(dbConnection, userID, claims, profileName);
            }
            dbConnection.commit();

        } catch (Exception e) {
            try {
                dbConnection.rollback();
            } catch (SQLException e1) {
                String errorMessage = "Error while rollback add user operation for user : " + userID;
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e1);
                }
                throw new UserStoreException(errorMessage, e1);
            }
            String errorMessage = "Error while persisting user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            if (e instanceof UserStoreException && ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE.getCode()
                    .equals(((UserStoreException) e).getErrorCode())) {
                // Duplicate entry
                throw new UserStoreException(errorMessage, ERROR_CODE_DUPLICATE_WHILE_ADDING_A_USER.getCode(), e);
            } else {
                // Other SQL Exception
                throw new UserStoreException(errorMessage, e);
            }
        } finally {
            credentialObj.clear();
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public void doAddRoleWithID(String roleName, String[] userIDList, boolean shared) throws UserStoreException {

        if (shared && isSharedGroupEnabled()) {
            doAddSharedRoleWithID(roleName, userIDList);
        }

        Connection dbConnection = null;

        try {
            dbConnection = getDBConnection();
            String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt, roleName, tenantId);
            } else {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt, roleName);
            }
            if (userIDList != null) {
                // add role to user
                String type = DatabaseCreator.getDatabaseType(dbConnection);
                String sqlStmt2 = realmConfig
                        .getUserStoreProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_WITH_ID + "-" + type);

                if (sqlStmt2 == null) {
                    sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_WITH_ID);
                }
                if (sqlStmt2.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                    if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, tenantId, userIDList,
                                tenantId, roleName, tenantId);
                    } else {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, userIDList, tenantId,
                                roleName, tenantId, tenantId);
                    }
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, userIDList, roleName);
                }

            }
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while adding role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } catch (Exception e) {
            String errorMessage = "Error occurred while getting database type from DB connection";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            if (e instanceof UserStoreException && ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE.getCode()
                    .equals(((UserStoreException) e).getErrorCode())) {
                // Duplicate entry
                throw new UserStoreException(errorMessage, ERROR_CODE_DUPLICATE_WHILE_ADDING_ROLE.getCode(), e);
            } else {
                // Other SQL Exception
                throw new UserStoreException(errorMessage, e);
            }
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public void doDeleteUserWithID(String userID) throws UserStoreException {

        String sqlStmt1 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_WITH_ID);
        if (sqlStmt1 == null) {
            throw new UserStoreException("The sql statement for delete user-role mapping is null.");
        }

        String sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_WITH_ID);
        if (sqlStmt2 == null) {
            throw new UserStoreException("The sql statement for delete user attribute is null.");
        }

        String sqlStmt3 = realmConfig.getUserStoreProperty(JDBCRealmConstants.DELETE_USER_WITH_ID);
        if (sqlStmt3 == null) {
            throw new UserStoreException("The sql statement for delete user is null.");
        }

        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, tenantId, tenantId);
                this.updateStringValuesToDatabase(dbConnection, sqlStmt2, userID, tenantId, tenantId);
                this.updateStringValuesToDatabase(dbConnection, sqlStmt3, userID, tenantId);
            } else {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID);
                this.updateStringValuesToDatabase(dbConnection, sqlStmt2, userID);
                this.updateStringValuesToDatabase(dbConnection, sqlStmt3, userID);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Error occurred while deleting user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public void doUpdateUserListOfRoleWithID(String roleName, String[] deletedUserIDs, String[] newUserIDs)
            throws UserStoreException {

        JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(roleName);
        roleName = ctx.getRoleName();
        int roleTenantId = ctx.getTenantId();
        boolean isShared = ctx.isShared();

        String sqlStmt1 = realmConfig.getUserStoreProperty(isShared ?
                JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_WITH_ID :
                JDBCRealmConstants.REMOVE_USER_FROM_ROLE_WITH_ID);
        if (sqlStmt1 == null) {
            throw new UserStoreException("The sql statement for remove user from role is null.");
        }

        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            String type = DatabaseCreator.getDatabaseType(dbConnection);
            String sqlStmt2;
            if (!isShared) {
                sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_WITH_ID + "-" + type);
                if (sqlStmt2 == null) {
                    sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_WITH_ID);
                }
            } else {
                sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_WITH_ID);
            }
            if (sqlStmt2 == null) {
                throw new UserStoreException("The sql statement for add user to role is null.");
            }
            if (deletedUserIDs != null) {
                if (isShared) {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, roleName, tenantId,
                            deletedUserIDs, tenantId, tenantId, roleTenantId);
                } else {
                    if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, deletedUserIDs, tenantId,
                                roleName, tenantId, tenantId);
                    } else {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, deletedUserIDs, roleName);
                    }
                }
            }
            if (newUserIDs != null) {
                if (isShared) {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, roleName, roleTenantId,
                            newUserIDs, tenantId, tenantId, roleTenantId);

                } else {
                    if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                        if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                            DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, tenantId, newUserIDs,
                                    tenantId, roleName, tenantId);
                        } else {
                            DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, newUserIDs, tenantId,
                                    roleName, tenantId, tenantId);
                        }
                    } else {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, newUserIDs, roleName);
                    }
                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Database error occurred while updating user list of role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } catch (Exception e) {
            String errorMessage = "Error occurred while getting database type from DB connection";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }

    }

    /**
     * Break the provided role list based on whether roles are shared or not
     *
     * @param rolesList
     * @return
     */
    private RoleBreakdown getSharedRoleBreakdown(String[] rolesList) {
        List<String> roles = new ArrayList<>();
        List<Integer> tenantIds = new ArrayList<>();

        List<String> sharedRoles = new ArrayList<>();
        List<Integer> sharedTenantIds = new ArrayList<>();

        for (String role : rolesList) {

            if (StringUtils.isNotEmpty(role)) {
                String[] deletedRoleNames = role.split(CarbonConstants.DOMAIN_SEPARATOR);
                if (deletedRoleNames.length > 1) {
                    role = deletedRoleNames[1];
                }

                JDBCRoleContext ctx = (JDBCRoleContext) createRoleContext(role);
                role = ctx.getRoleName();
                int roleTenantId = ctx.getTenantId();
                boolean isShared = ctx.isShared();

                if (isShared) {
                    sharedRoles.add(role);
                    sharedTenantIds.add(roleTenantId);
                } else {
                    roles.add(role);
                    tenantIds.add(roleTenantId);
                }
            }

        }

        RoleBreakdown breakdown = new RoleBreakdown();

        // Non shared roles and tenant ids
        breakdown.setRoles(roles.stream().toArray(String[]::new));
        breakdown.setTenantIds(tenantIds.stream().toArray(Integer[]::new));

        // Shared roles and tenant ids
        breakdown.setSharedRoles(sharedRoles.stream().toArray(String[]::new));
        breakdown.setSharedTenantids(sharedTenantIds.stream().toArray(Integer[]::new));

        return breakdown;

    }

    @Override
    public void doUpdateRoleListOfUserWithID(String userID, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            String type = DatabaseCreator.getDatabaseType(dbConnection);
            String sqlStmt2 = null;
            // if user name and role names are prefixed with domain name, remove the domain name
            String[] userNames = userID.split(CarbonConstants.DOMAIN_SEPARATOR);
            if (userNames.length > 1) {
                userID = userNames[1];
            }
            if (deletedRoles != null && deletedRoles.length > 0) {
                // Break the provided role list based on whether roles are shared or not
                RoleBreakdown breakdown = getSharedRoleBreakdown(deletedRoles);
                String[] roles = breakdown.getRoles();
                // Integer[] tenantIds = breakdown.getTenantIds();

                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantids();

                String sqlStmt1;

                if (roles.length > 0) {
                    sqlStmt1 = realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ROLE_FROM_USER);
                    if (sqlStmt1 == null) {
                        throw new UserStoreException("The sql statement for remove user from role is null.");
                    }
                    if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, roles, tenantId, userID,
                                tenantId, tenantId);
                    } else {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, roles, userID);
                    }
                }
                if (sharedRoles.length > 0) {
                    sqlStmt1 = realmConfig
                            .getUserStoreProperty(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_WITH_ID);
                    if (sqlStmt1 == null) {
                        throw new UserStoreException("The sql statement for remove user from role is null");
                    }
                    DatabaseUtil.udpateUserRoleMappingWithExactParams(dbConnection, sqlStmt1, sharedRoles, userID,
                            sharedTenantIds, tenantId);
                }
            }

            if (newRoles != null && newRoles.length > 0) {

                ArrayList<String> newRoleList = new ArrayList<>();
                for (String role : newRoles) {
                    if (!isExistingRole(role)) {
                        String errorMessage = "The role: " + role + " does not exist.";
                        throw new UserStoreException(errorMessage);
                    }
                    if (!isUserInRole(userID, role)) {
                        newRoleList.add(role);
                    }
                }

                String[] rolesToAdd = newRoleList.stream().toArray(String[]::new);
                // if user name and role names are prefixed with domain name,
                // remove the domain name
                RoleBreakdown breakdown = getSharedRoleBreakdown(rolesToAdd);
                String[] roles = breakdown.getRoles();
                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantids();

                if (roles.length > 0) {
                    sqlStmt2 = realmConfig
                            .getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_WITH_ID + "-" + type);
                    if (sqlStmt2 == null) {
                        sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_WITH_ID);
                    }
                    if (sqlStmt2 == null) {
                        throw new UserStoreException("The sql statement for add user to role is null.");
                    }
                    if (sqlStmt2.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                        if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                            DatabaseUtil
                                    .udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, tenantId, roles, tenantId,
                                            userID, tenantId);
                        } else {
                            DatabaseUtil
                                    .udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, roles, tenantId, userID,
                                            tenantId, tenantId);
                        }
                    } else {
                        DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, newRoles, userID);
                    }
                }
                if (sharedRoles.length > 0) {
                    sqlStmt2 = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_WITH_ID);
                    if (sqlStmt2 == null) {
                        throw new UserStoreException("The sql statement for remove user from role is null.");
                    }
                    DatabaseUtil.udpateUserRoleMappingWithExactParams(dbConnection, sqlStmt2, sharedRoles, userID,
                            sharedTenantIds, tenantId);

                }
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Database error occurred while updating role list of user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } catch (UserStoreException e) {
            String errorMessage = "Error occurred while updating role list of user:" + userID;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(e.getMessage(), e);
        } catch (Exception e) {
            String errorMessage = "Error occurred while getting database type from DB connection";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }

    }

    @Override
    public void doSetUserClaimValueWithID(String userID, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        if (claimValue == null) {
            throw new UserStoreException("Cannot set null values.");
        }
        Connection dbConnection = null;
        String property = null;
        try {
            dbConnection = getDBConnection();
            property = getClaimAtrribute(claimURI, userID, null);
            String value = getProperty(dbConnection, userID, property, profileName);
            if (value == null) {
                addPropertyWithID(dbConnection, userID, property, claimValue, profileName);
            } else {
                updatePropertyWithID(dbConnection, userID, property, claimValue, profileName);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String msg =
                    "Database error occurred while saving user claim value for user : " + userID + " & claim URI : "
                            + claimURI + " claim value : " + claimValue;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } catch (UserStoreException e) {
            String errorMessage =
                    "Error occurred while adding or updating claim value for user : " + userID + " & claim URI : "
                            + claimURI + " attribute : " + property + " profile : " + profileName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage =
                    "Error occurred while getting claim attribute for user : " + userID + " & claim URI : " + claimURI;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public void doSetUserClaimValuesWithID(String userID, Map<String, String> claims, String profileName)
            throws UserStoreException {

        Connection dbConnection = null;
        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        if (claims.get(UserCoreConstants.PROFILE_CONFIGURATION) == null) {
            claims.put(UserCoreConstants.PROFILE_CONFIGURATION, UserCoreConstants.DEFAULT_PROFILE_CONFIGURATION);
        }

        try {

            ArrayList<String> propertyListToUpdate = new ArrayList<>();
            Map<String, String> claimPropertyMap = new HashMap<>();
            Iterator<Map.Entry<String, String>> ite = claims.entrySet().iterator();

            // Get the property names fo the claims
            while (ite.hasNext()) {
                Map.Entry<String, String> entry = ite.next();
                String claimURI = entry.getKey();

                String property = getClaimAtrribute(claimURI, userID, null);
                propertyListToUpdate.add(property);
                claimPropertyMap.put(claimURI, property);
            }

            String[] propertyArr = new String[propertyListToUpdate.size()];
            propertyArr = propertyListToUpdate.toArray(propertyArr);

            // Get available properties
            Map<String, String> availableProperties = getUserPropertyValues(userID, propertyArr, profileName);
            Map<String, String> newClaims = new HashMap<>();
            Map<String, String> availableClaims = new HashMap<>();

            // Divide claim list to already available claims (need to update those) and new claims (need to add those)
            Iterator<Map.Entry<String, String>> ite2 = claims.entrySet().iterator();
            while (ite2.hasNext()) {
                Map.Entry<String, String> entry = ite2.next();
                String claimURI = entry.getKey();
                String claimValue = claimPropertyMap.get(claimURI);
                if (claimValue != null && availableProperties.containsKey(claimValue)) {
                    String availableValue = availableProperties.get(claimValue);
                    if (availableValue != null && availableValue.equals(entry.getValue())) {
                        continue;
                    } else {
                        availableClaims.put(claimURI, entry.getValue());
                    }
                } else {
                    newClaims.put(claimURI, entry.getValue());
                }
            }

            dbConnection = getDBConnection();
            addPropertiesWithID(dbConnection, userID, newClaims, profileName);
            updateProperties(dbConnection, userID, availableClaims, profileName);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Database error occurred while setting user claim values for user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage = "Error occurred while getting claim attribute for user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public void doDeleteUserClaimValueWithID(String userID, String claimURI, String profileName)
            throws UserStoreException {

        Connection dbConnection = null;
        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        try {
            String property;
            if (UserCoreConstants.PROFILE_CONFIGURATION.equals(claimURI)) {
                property = UserCoreConstants.PROFILE_CONFIGURATION;
            } else {
                property = getClaimAtrribute(claimURI, userID, null);
            }

            dbConnection = getDBConnection();
            this.deletePropertyWithID(dbConnection, userID, property, profileName);
            dbConnection.commit();
        } catch (SQLException e) {
            String msg =
                    "Database error occurred while deleting user claim value for user : " + userID + " & claim URI : "
                            + claimURI;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage =
                    "Error occurred while getting claim attribute for user : " + userID + " & claim URI : " + claimURI;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public void doDeleteUserClaimValuesWithID(String userID, String[] claims, String profileName)
            throws UserStoreException {

        Connection dbConnection = null;
        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }
        try {
            dbConnection = getDBConnection();
            for (String claimURI : claims) {
                String property = getClaimAtrribute(claimURI, userID, null);
                this.deletePropertyWithID(dbConnection, userID, property, profileName);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Database error occurred while deleting user claim values for user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            String errorMessage = "Error occurred while getting claim attribute for user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public void doUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential)
            throws UserStoreException {

        // no need to check old password here because we already authenticate in super class
        this.doUpdateCredentialByAdminWithID(userID, newCredential);
    }

    @Override
    public void doUpdateCredentialByAdminWithID(String userID, Object newCredential) throws UserStoreException {

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.UPDATE_USER_PASSWORD_WITH_ID);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for delete user claim value is null");
        }
        String saltValue = null;
        if ("true".equalsIgnoreCase(
                realmConfig.getUserStoreProperties().get(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
            saltValue = generateSaltValue();
        }

        String password = this.preparePassword(newCredential, saltValue);

        if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN) && saltValue == null) {
            updateStringValuesToDatabase(null, sqlStmt, password, "", false, new Date(), userID, tenantId);
        } else if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN) && saltValue != null) {
            updateStringValuesToDatabase(null, sqlStmt, password, saltValue, false, new Date(), userID, tenantId);
        } else if (!sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN) && saltValue == null) {
            updateStringValuesToDatabase(null, sqlStmt, password, "", false, new Date(), userID);
        } else {
            updateStringValuesToDatabase(null, sqlStmt, password, saltValue, false, new Date(), userID);
        }
    }

    @Override
    public Date getPasswordExpirationTimeWithID(String userID) throws UserStoreException {

        if (userID != null && userID.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
            return super.getPasswordExpirationTime(userID);
        }

        Connection dbConnection = null;
        ResultSet rs = null;
        PreparedStatement prepStmt = null;
        String sqlstmt;
        Date date = null;

        try {
            dbConnection = getDBConnection();
            dbConnection.setAutoCommit(false);

            sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER_WITH_ID);
            if (log.isDebugEnabled()) {
                log.debug(sqlstmt);
            }

            prepStmt = dbConnection.prepareStatement(sqlstmt);
            prepStmt.setString(1, userID);
            if (sqlstmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }

            rs = prepStmt.executeQuery();

            if (rs.next() == true) {
                boolean requireChange = rs.getBoolean(5);
                Timestamp changedTime = rs.getTimestamp(6);
                if (requireChange) {
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(changedTime);
                    gc.add(GregorianCalendar.HOUR, 24);
                    date = gc.getTime();
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving password expiration time for user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return date;
    }

    /**
     * This private method returns a saltValue using SecureRandom.
     *
     * @return saltValue
     */
    private String generateSaltValue() {

        String saltValue;
        try {
            SecureRandom secureRandom = SecureRandom.getInstance(SHA_1_PRNG);
            byte[] bytes = new byte[16];
            //secureRandom is automatically seeded by calling nextBytes
            secureRandom.nextBytes(bytes);
            saltValue = Base64.encode(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA1PRNG algorithm could not be found.");
        }
        return saltValue;
    }

    /**
     * @param dbConnection
     * @param sqlStmt
     * @param params
     * @throws UserStoreException
     */
    private void updateStringValuesToDatabase(Connection dbConnection, String sqlStmt, Object... params)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        boolean localConnection = false;
        try {
            if (dbConnection == null) {
                localConnection = true;
                dbConnection = getDBConnection();
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Invalid data provided");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Date) {
                        // Timestamp timestamp = new Timestamp(((Date) param).getTime());
                        // prepStmt.setTimestamp(i + 1, timestamp);
                        prepStmt.setTimestamp(i + 1, new Timestamp(System.currentTimeMillis()));
                    } else if (param instanceof Boolean) {
                        prepStmt.setBoolean(i + 1, (Boolean) param);
                    }
                }
            }
            int count = prepStmt.executeUpdate();

            if (log.isDebugEnabled()) {
                if (count == 0) {
                    log.debug("No rows were updated");
                }
                log.debug("Executed query is " + sqlStmt + " and number of updated rows :: " + count);
            }

            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating string values to database.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            if (e instanceof SQLIntegrityConstraintViolationException) {
                // Duplicate entry
                throw new UserStoreException(msg, ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE.getCode(), e);
            } else {
                // Other SQL Exception
                throw new UserStoreException(msg, e);
            }
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    public void addPropertyWithID(Connection dbConnection, String userID, String propertyName, String value,
            String profileName) throws UserStoreException {

        try {
            String type = DatabaseCreator.getDatabaseType(dbConnection);
            String sqlStmt = realmConfig
                    .getUserStoreProperty(JDBCRealmConstants.ADD_USER_PROPERTY_WITH_ID + "-" + type);
            if (sqlStmt == null) {
                sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_PROPERTY_WITH_ID);
            }
            if (sqlStmt == null) {
                throw new UserStoreException("The sql statement for add user property sql is null");
            }

            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    updateStringValuesToDatabase(dbConnection, sqlStmt, propertyName, value, profileName, tenantId,
                            userID, tenantId);
                } else {
                    updateStringValuesToDatabase(dbConnection, sqlStmt, userID, tenantId, propertyName, value,
                            profileName, tenantId);
                }
            } else {
                updateStringValuesToDatabase(dbConnection, sqlStmt, userID, propertyName, value, profileName);
            }
        } catch (Exception e) {
            String msg = "Error occurred while adding user property for user : " + userID + " & property name : "
                    + propertyName + " & value : " + value;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        }
    }

    protected void updatePropertyWithID(Connection dbConnection, String userID, String propertyName, String value,
            String profileName) throws UserStoreException {

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for add user property sql is null.");
        }

        if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
            updateStringValuesToDatabase(dbConnection, sqlStmt, value, userID, tenantId, propertyName, profileName,
                    tenantId);
        } else {
            updateStringValuesToDatabase(dbConnection, sqlStmt, value, userID, propertyName, profileName);
        }

    }

    /**
     * Update user properties.
     *
     * @param dbConnection DB connection.
     * @param userID       user ID.
     * @param propertyName properties need to be added.
     * @param profileName  profile name.
     * @throws UserStoreException
     */
    protected void deletePropertyWithID(Connection dbConnection, String userID, String propertyName, String profileName)
            throws UserStoreException {

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.DELETE_USER_PROPERTY_WITH_ID);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for add user property sql is null.");
        }

        if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
            updateStringValuesToDatabase(dbConnection, sqlStmt, userID, tenantId, propertyName, profileName, tenantId);
        } else {
            updateStringValuesToDatabase(dbConnection, sqlStmt, userID, propertyName, profileName);
        }
    }

    /**
     * @param dbConnection
     * @param userName
     * @param propertyName
     * @param profileName
     * @return
     * @throws UserStoreException
     */
    protected String getProperty(Connection dbConnection, String userName, String propertyName, String profileName)
            throws UserStoreException {

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_PROP_FOR_PROFILE_WITH_ID);
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for add user property sql is null");
        }
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String value = null;
        try {
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, userName);
            prepStmt.setString(2, propertyName);
            prepStmt.setString(3, profileName);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(4, tenantId);
                prepStmt.setInt(5, tenantId);
            }

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                value = rs.getString(1);
            }
            return value;
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving user profile property for user : " + userName
                    + " & property name : " + propertyName + " & profile name : " + profileName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    private DataSource loadUserStoreSpacificDataSoruce() throws UserStoreException {
        return DatabaseUtil.createUserStoreDataSource(realmConfig);
    }

    @Override
    public String[] doGetUserListFromProperties(String property, String value, String profileName)
            throws UserStoreException {

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        if (value == null) {
            throw new IllegalArgumentException("Filter value cannot be null");
        }
        if (value.contains(QUERY_FILTER_STRING_ANY)) {
            // This is to support LDAP like queries. Value having only * is restricted except one *.
            if (!value.matches("(\\*)\\1+")) {
                // Convert all the * to % except \*.
                value = value.replaceAll("(?<!\\\\)\\*", SQL_FILTER_STRING_ANY);
            }
        }

        String[] users = new String[0];
        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        List<String> list = new ArrayList<>();
        try {
            dbConnection = getDBConnection();
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_FOR_PROP_WITH_ID);
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, property);
            prepStmt.setString(2, value);
            prepStmt.setString(3, profileName);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(4, tenantId);
                prepStmt.setInt(5, tenantId);
            }
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(1);
                list.add(name);
            }

            if (list.size() > 0) {
                users = list.stream().toArray(String[]::new);
            }

        } catch (SQLException e) {
            String msg =
                    "Database error occurred while listing users for a property : " + property + " & value : " + value
                            + " & profile name : " + profileName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        return users;
    }

    @Override
    public String[] doGetExternalRoleListOfUserWithID(String userID, String filter) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Getting roles of user: " + userID + " with filter: " + filter);
        }

        String sqlStmt;
        String[] names;
        if (filter.equals("*") || StringUtils.isEmpty(filter)) {
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USER_ROLE_WITH_ID);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                names = getStringValuesFromDatabase(sqlStmt, userID, tenantId, tenantId, tenantId);
            } else {
                names = getStringValuesFromDatabase(sqlStmt, userID);
            }
        } else {
            filter = filter.trim();
            filter = filter.replace("*", "%");
            filter = filter.replace("?", "_");
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_IS_USER_ROLE_EXIST_WITH_ID);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                names = getStringValuesFromDatabase(sqlStmt, userID, tenantId, tenantId, tenantId, filter);
            } else {
                names = getStringValuesFromDatabase(sqlStmt, userID, filter);
            }
        }
        List<String> roles = new ArrayList<>();
        if (log.isDebugEnabled()) {
            if (names != null) {
                for (String name : names) {
                    log.debug("Found role: " + name);
                }
            } else {
                log.debug("No external role found for the user: " + userID);
            }
        }

        Collections.addAll(roles, names);
        return roles.stream().toArray(String[]::new);
    }

    @Override
    protected Map<String, Map<String, String>> getUsersPropertyValues(List<String> users, String[] propertyNames,
            String profileName) throws UserStoreException {

        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String[] propertyNamesSorted = propertyNames.clone();
        Arrays.sort(propertyNamesSorted);

        Map<String, Map<String, String>> usersPropertyValuesMap = new HashMap<>();
        try {
            dbConnection = getDBConnection();
            StringBuilder usernameParameter = new StringBuilder();

            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_PROPS_FOR_PROFILE_WITH_ID);
            for (int i = 0; i < users.size(); i++) {

                usernameParameter.append("'").append(users.get(i)).append("'");

                if (i != users.size() - 1) {
                    usernameParameter.append(",");
                }
            }

            sqlStmt = sqlStmt.replaceFirst("\\?", usernameParameter.toString());
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, profileName);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
                prepStmt.setInt(3, tenantId);
            }

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString(2);
                if (Arrays.binarySearch(propertyNamesSorted, name) < 0) {
                    continue;
                }
                String username = rs.getString(1);
                String value = rs.getString(3);

                if (usersPropertyValuesMap.get(username) != null) {
                    usersPropertyValuesMap.get(username).put(name, value);
                } else {
                    Map<String, String> attributes = new HashMap<>();
                    attributes.put(name, value);
                    usersPropertyValuesMap.put(username, attributes);
                }
            }
            return usersPropertyValuesMap;
        } catch (SQLException e) {
            String errorMessage = "Error Occurred while getting property values";
            if (log.isDebugEnabled()) {
                errorMessage = errorMessage + ": " + users;
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    @Override
    protected Map<String, List<String>> doGetExternalRoleListOfUsers(List<String> userNames) throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Getting roles of users: " + userNames);
        }

        // Get the relevant userID for the given username.
        if (UserCoreUtil.isUniqueUserIDFeatureEnabled()) {
            userNames = getUserIDsByUserNames(userNames, null);
        }

        String sqlStmt;
        Map<String, List<String>> rolesListOfUsersMap = new HashMap<>();
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            dbConnection = getDBConnection();
            StringBuilder usernameParameter = new StringBuilder();
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_ROLE_WITH_ID);
            if (sqlStmt == null) {
                throw new UserStoreException("The sql statement for retrieving users roles is null");
            }
            for (int i = 0; i < userNames.size(); i++) {

                usernameParameter.append("'").append(userNames.get(i)).append("'");

                if (i != userNames.size() - 1) {
                    usernameParameter.append(",");
                }
            }
            sqlStmt = sqlStmt.replaceFirst("\\?", usernameParameter.toString());
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(1, tenantId);
                prepStmt.setInt(2, tenantId);
                prepStmt.setInt(3, tenantId);
            }
            rs = prepStmt.executeQuery();
            String domainName = getMyDomainName();
            while (rs.next()) {
                String username = UserCoreUtil.addDomainToName(rs.getString(1), domainName);
                String roleName = UserCoreUtil.addDomainToName(rs.getString(2), domainName);
                if (rolesListOfUsersMap.get(username) != null) {
                    rolesListOfUsersMap.get(username).add(roleName);
                } else {
                    List<String> roleNames = new ArrayList<>();
                    roleNames.add(roleName);
                    rolesListOfUsersMap.put(username, roleNames);
                }
            }
            return rolesListOfUsersMap;
        } catch (SQLException e) {
            String errorMessage = "Error Occurred while getting role lists of users";
            if (log.isDebugEnabled()) {
                errorMessage = errorMessage + ": " + userNames;
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    protected void doAddSharedRoleWithID(String roleName, String[] userList) throws UserStoreException {

        Connection dbConnection = null;
        try {
            dbConnection = getDBConnection();
            String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt, true, roleName, tenantId);
            } else {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt, true, roleName);
            }
            if (userList != null) {
                // add role to user
                int roleTenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
                sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_WITH_ID);
                DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt, roleName, roleTenantId, userList,
                        tenantId, tenantId, roleTenantId);
            }
            dbConnection.commit();
        } catch (SQLException e) {
            String msg = "Database error occurred while adding shared role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } catch (Exception e) {
            String msg = "Error occurred while adding shared role.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    @Override
    public String[] doGetSharedRoleListOfUserWithID(String userID, String tenantDomain, String filter)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Looking for shared roles for user: " + userID + " for tenant: " + tenantDomain);
        }

        if (isSharedGroupEnabled()) {
            // shared roles
            String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_WITH_ID);
            String[] sharedNames = getRoleNamesWithDomainWithID(sqlStmt, userID, tenantId, true);

            return sharedNames;
        }
        return new String[0];
    }

    /**
     * Add properties as a batch.
     *
     * @param dbConnection DB connection.
     * @param userID       user ID.
     * @param properties   properties need to be added.
     * @param profileName  profile name.
     * @throws org.wso2.carbon.user.api.UserStoreException
     */
    private void addPropertiesWithID(Connection dbConnection, String userID, Map<String, String> properties,
            String profileName) throws org.wso2.carbon.user.api.UserStoreException {

        String type;
        try {
            type = DatabaseCreator.getDatabaseType(dbConnection);
        } catch (Exception e) {
            String msg = "Error occurred while adding user properties for user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        }

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_PROPERTY_WITH_ID + "-" + type);
        if (sqlStmt == null) {
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.ADD_USER_PROPERTY_WITH_ID);
        }
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for add user property sql is null");
        }

        PreparedStatement prepStmt = null;
        boolean localConnection = false;

        try {
            if (dbConnection == null) {
                localConnection = true;
                dbConnection = getDBConnection();
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            Map<String, String> userAttributes = new HashMap<>();
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String attributeName = getClaimAtrribute(entry.getKey(), userID, null);
                String attributeValue = entry.getValue();
                userAttributes.put(attributeName, attributeValue);
            }

            for (Map.Entry<String, String> entry : userAttributes.entrySet()) {
                String propertyName = entry.getKey();
                String propertyValue = entry.getValue();
                if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                    if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                        batchUpdateStringValuesToDatabase(prepStmt, propertyName, propertyValue, profileName, tenantId,
                                userID, tenantId);
                    } else {
                        batchUpdateStringValuesToDatabase(prepStmt, userID, tenantId, propertyName, propertyValue,
                                profileName, tenantId);
                    }
                } else {
                    batchUpdateStringValuesToDatabase(prepStmt, userID, propertyName, propertyValue, profileName);
                }
            }

            int[] counts = prepStmt.executeBatch();
            if (log.isDebugEnabled()) {
                int totalUpdated = 0;
                if (counts != null) {
                    for (int i : counts) {
                        totalUpdated += i;
                    }
                }

                if (totalUpdated == 0) {
                    log.debug("No rows were updated");
                }
                log.debug("Executed query is " + sqlStmt + " and number of updated rows :: " + totalUpdated);
            }

            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating string values to database.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    /**
     * Update properties as a batch.
     *
     * @param dbConnection DB connection.
     * @param userID       user ID.
     * @param properties   properties need to be added.
     * @param profileName  profile name.
     * @throws org.wso2.carbon.user.api.UserStoreException
     */
    private void updateProperties(Connection dbConnection, String userID, Map<String, String> properties,
            String profileName) throws org.wso2.carbon.user.api.UserStoreException {

        String type;
        try {
            type = DatabaseCreator.getDatabaseType(dbConnection);
        } catch (Exception e) {
            String msg = "Error occurred while updating user properties for user : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        }

        String sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID + "-" + type);
        if (sqlStmt == null) {
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID);
        }
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for update user property sql is null.");
        }

        PreparedStatement prepStmt = null;
        boolean localConnection = false;

        try {
            if (dbConnection == null) {
                localConnection = true;
                dbConnection = getDBConnection();
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);

            for (Map.Entry<String, String> entry : properties.entrySet()) {
                String propertyName = getClaimAtrribute(entry.getKey(), userID, null);
                String propertyValue = entry.getValue();
                if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                    if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                        batchUpdateStringValuesToDatabase(prepStmt, propertyName, propertyValue, profileName, tenantId,
                                userID, tenantId);
                    } else {
                        batchUpdateStringValuesToDatabase(prepStmt, propertyValue, userID, tenantId, propertyName,
                                profileName, tenantId);
                    }
                } else {
                    batchUpdateStringValuesToDatabase(prepStmt, propertyValue, userID, propertyName, profileName);
                }
            }

            int[] counts = prepStmt.executeBatch();
            if (log.isDebugEnabled()) {
                int totalUpdated = 0;
                if (counts != null) {
                    for (int i : counts) {
                        totalUpdated += i;
                    }
                }

                if (totalUpdated == 0) {
                    log.debug("No rows were updated");
                }
                log.debug("Executed query is " + sqlStmt + " and number of updated rows :: " + totalUpdated);
            }

            if (localConnection) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            String msg = "Error occurred while updating string values to database.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            if (localConnection) {
                DatabaseUtil.closeAllConnections(dbConnection);
            }
            DatabaseUtil.closeAllConnections(null, prepStmt);
        }
    }

    /**
     * Prepare the batch
     *
     * @param prepStmt
     * @param params
     * @throws UserStoreException
     */
    private void batchUpdateStringValuesToDatabase(PreparedStatement prepStmt, Object... params)
            throws UserStoreException {
        try {
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param == null) {
                        throw new UserStoreException("Invalid data provided");
                    } else if (param instanceof String) {
                        prepStmt.setString(i + 1, (String) param);
                    } else if (param instanceof Integer) {
                        prepStmt.setInt(i + 1, (Integer) param);
                    } else if (param instanceof Date) {
                        prepStmt.setTimestamp(i + 1, new Timestamp(System.currentTimeMillis()));
                    } else if (param instanceof Boolean) {
                        prepStmt.setBoolean(i + 1, (Boolean) param);
                    }
                }
            }
            prepStmt.addBatch();
        } catch (SQLException e) {
            String msg = "Error occurred while updating property values to database.";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        }
    }

    private boolean isCaseSensitiveUsername() {
        String isUsernameCaseInsensitiveString = realmConfig.getUserStoreProperty(CASE_INSENSITIVE_USERNAME);
        return !Boolean.parseBoolean(isUsernameCaseInsensitiveString);
    }

    private int getMaxUserNameListLength() {

        int maxUserList;
        try {
            maxUserList = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (Exception e) {
            // The user store property might not be configured. Therefore logging as debug.
            if (log.isDebugEnabled()) {
                log.debug("Unable to get the " + UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST
                        + " from the realm configuration. The default value: " + UserCoreConstants.MAX_USER_ROLE_LIST
                        + " is used instead.", e);
            }
            maxUserList = UserCoreConstants.MAX_USER_ROLE_LIST;
        }
        return maxUserList;
    }

    private int getSQLQueryTimeoutLimit() {

        int searchTime;
        try {
            searchTime = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (Exception e) {
            // The user store property might not be configured. Therefore logging as debug.
            if (log.isDebugEnabled()) {
                log.debug("Unable to get the " + UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME
                        + " from the realm configuration. The default value: " + UserCoreConstants.MAX_SEARCH_TIME
                        + " is used instead.", e);
            }
            searchTime = UserCoreConstants.MAX_SEARCH_TIME;
        }
        return searchTime;
    }

}
