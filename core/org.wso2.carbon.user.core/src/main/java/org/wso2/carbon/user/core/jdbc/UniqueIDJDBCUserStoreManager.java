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
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.common.AuthenticationResult;
import org.wso2.carbon.user.core.common.FailureReason;
import org.wso2.carbon.user.core.common.LoginIdentifier;
import org.wso2.carbon.user.core.common.PaginatedSearchResult;
import org.wso2.carbon.user.core.common.RoleBreakdown;
import org.wso2.carbon.user.core.common.RoleContext;
import org.wso2.carbon.user.core.common.UniqueIDPaginatedSearchResult;
import org.wso2.carbon.user.core.common.User;
import org.wso2.carbon.user.core.jdbc.caseinsensitive.JDBCCaseInsensitiveConstants;
import org.wso2.carbon.user.core.model.Condition;
import org.wso2.carbon.user.core.model.ExpressionAttribute;
import org.wso2.carbon.user.core.model.ExpressionCondition;
import org.wso2.carbon.user.core.model.ExpressionOperation;
import org.wso2.carbon.user.core.model.OperationalCondition;
import org.wso2.carbon.user.core.model.SqlBuilder;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.Secret;
import org.wso2.carbon.utils.UnsupportedSecretTypeException;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;

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
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_A_USER;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_ADDING_ROLE;
import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE;
import static org.wso2.carbon.user.core.util.DatabaseUtil.getLoggableSqlString;

public class UniqueIDJDBCUserStoreManager extends JDBCUserStoreManager {

    private static Log log = LogFactory.getLog(UniqueIDJDBCUserStoreManager.class);

    private static final String QUERY_FILTER_STRING_ANY = "*";
    private static final String SQL_FILTER_STRING_ANY = "%";
    private static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";
    private static final String SHA_1_PRNG = "SHA1PRNG";
    private static final String DB2 = "db2";
    private static final String MSSQL = "mssql";
    private static final String ORACLE = "oracle";
    private static final String MYSQL = "mysql";
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION =
            "This is the separator for multiple claim " + "values";
    private static final String VALIDATION_INTERVAL = "validationInterval";
    private static final List<Property> UNIQUE_ID_JDBC_UM_ADVANCED_PROPERTIES = new ArrayList<>();

    public UniqueIDJDBCUserStoreManager() {

    }

    public UniqueIDJDBCUserStoreManager(RealmConfiguration realmConfig, int tenantId) throws UserStoreException {

        super(realmConfig, tenantId);
    }

    public UniqueIDJDBCUserStoreManager(DataSource ds, RealmConfiguration realmConfig, int tenantId,
            boolean addInitData) throws UserStoreException {

        super(ds, realmConfig, tenantId, addInitData);
    }

    public UniqueIDJDBCUserStoreManager(DataSource ds, RealmConfiguration realmConfig) throws UserStoreException {

        super(ds, realmConfig);
    }

    public UniqueIDJDBCUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);
    }

    public UniqueIDJDBCUserStoreManager(RealmConfiguration realmConfig, Map<String, Object> properties,
            ClaimManager claimManager, ProfileConfigurationManager profileManager, UserRealm realm, Integer tenantId,
            boolean skipInitData) throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, skipInitData);
    }

    @Override
    public List<User> doListUsersWithID(String filter, int maxItemLimit) throws UserStoreException {

        List<User> users = new ArrayList<>();
        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        if (maxItemLimit == 0) {
            return Collections.emptyList();
        }

        int givenMax;
        int searchTime;
        try {
            givenMax = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_USER_LIST));
        } catch (NumberFormatException e) {
            givenMax = UserCoreConstants.MAX_USER_ROLE_LIST;
        }

        try {
            searchTime = Integer
                    .parseInt(realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (NumberFormatException e) {
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
                throw new UserStoreException("Attempts to establish a connection with the data source has failed.");
            }

            if (isCaseSensitiveUsername()) {
                sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USER_FILTER_WITH_ID);
            } else {
                sqlStmt = realmConfig
                        .getUserStoreProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE);
            }

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, filter);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }

            prepStmt.setMaxRows(maxItemLimit);
            try {
                prepStmt.setQueryTimeout(searchTime);
            } catch (Exception e) {
                // this can be ignored since timeout method is not implemented
                if (log.isDebugEnabled()) {
                    log.debug(e);
                }
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

                User user = getUser(userID, userName);
                userList.add(user);
            }
            rs.close();

            if (userList.size() > 0) {
                users = userList;
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
    public String[] doGetUserListOfRole(String roleName, String filter) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public String[] doGetUserListOfRole(String roleName, String filter, int maxItemLimit) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public List<User> doGetUserListOfRoleWithID(String roleName, String filter) throws UserStoreException {

        RoleContext roleContext = createRoleContext(roleName);
        return getUserListOfJDBCRoleWithID(roleContext, filter);
    }

    public List<User> getUserListOfJDBCRoleWithID(RoleContext ctx, String filter) throws UserStoreException {

        return getUserListOfJDBCRoleWithID(ctx, filter, QUERY_MAX_ITEM_LIMIT_ANY);
    }

    @Override
    public List<User> doGetUserListOfRoleWithID(String roleName, String filter, int maxItemLimit)
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
    public List<User> getUserListOfJDBCRoleWithID(RoleContext ctx, String filter, int maxItemLimit)
            throws UserStoreException {

        String roleName = ctx.getRoleName();
        List<User> users;
        String sqlStmt;

        if (maxItemLimit == 0) {
            return Collections.emptyList();
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
                users = getUsersFromDatabaseWithConstraints(this, sqlStmt, maxItemLimit, queryTimeout, filter, roleName,
                        tenantId, tenantId, tenantId);
            } else {
                users = getUsersFromDatabaseWithConstraints(this, sqlStmt, maxItemLimit, queryTimeout, filter,
                        roleName);
            }
        } else {
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_FILTER_WITH_ID);
            users = getUsersFromDatabaseWithConstraints(this, sqlStmt, maxItemLimit, queryTimeout, filter, roleName);
        }
        return users;
    }

    @Override
    public String[] getProfileNames(String userName) throws UserStoreException {

        String userID = getUserIDFromUserName(userName);
        return getProfileNamesWithID(userID);
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
    public boolean doCheckIsUserInRole(String userName, String roleName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public Map<String, String> getUserPropertyValues(String userName, String[] propertyNames, String profileName)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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

    @Override
    public boolean doCheckExistingUser(String userName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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
    private List<User> getUsersFromDatabaseWithConstraints(UserStoreManager userStoreManager, String sqlStmt,
            int maxRows, int queryTimeout, Object... params) throws UserStoreException {

        if (log.isDebugEnabled()) {
            String loggableSqlString = getLoggableSqlString(sqlStmt, params);
            String msg = "Using SQL : " + loggableSqlString + ", and maxRows: " + maxRows + ", and queryTimeout: "
                    + queryTimeout;
            log.debug(msg);
        }

        List<User> values;
        try (Connection dbConnection = getDBConnection()) {
            values = DatabaseUtil
                    .getUsersFromDatabaseWithConstraints(userStoreManager, dbConnection, sqlStmt, maxRows, queryTimeout,
                            params);
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
    public String[] getUserListFromProperties(String property, String value, String profileName)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public boolean doCheckExistingUserNameWithIDImpl(String userName) throws UserStoreException {

        String sqlStmt;
        if (isCaseSensitiveUsername()) {
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_IS_USER_NAME_EXISTING);
        } else {
            sqlStmt = realmConfig
                    .getUserStoreProperty(JDBCCaseInsensitiveConstants.GET_IS_USER_NAME_EXISTING_CASE_INSENSITIVE);
        }
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for is user existing null");
        }
        boolean isExisting;

        String isUnique = realmConfig.getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USERNAME_UNIQUE);
        if (Boolean.parseBoolean(isUnique) && !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
            String uniquenesSql;
            if (isCaseSensitiveUsername()) {
                uniquenesSql = realmConfig.getUserStoreProperty(JDBCRealmConstants.USER_NAME_UNIQUE);
            } else {
                uniquenesSql = realmConfig
                        .getUserStoreProperty(JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_CASE_INSENSITIVE);
            }
            isExisting = isValueExisting(uniquenesSql, null, userName);
            if (log.isDebugEnabled()) {
                log.debug("The username should be unique across tenants.");
            }
        } else {
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                isExisting = isValueExisting(sqlStmt, null, userName, tenantId);
            } else {
                isExisting = isValueExisting(sqlStmt, null, userName);
            }
        }

        return isExisting;
    }

    @Override
    protected AuthenticationResult doAuthenticateWithID(List<LoginIdentifier> loginIdentifiers, Object credential)
            throws UserStoreException {

        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);
        User user;

        if (!isValidCredentials(credential)) {
            String reason = "Password validation failed";
            if (log.isDebugEnabled()) {
                log.debug(reason);
            }
            return getAuthenticationResult(reason);
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
            sqlstmt = getSqlQuery(loginIdentifiers.size()).toString();
            if (log.isDebugEnabled()) {
                log.debug(sqlstmt);
            }

            prepStmt = dbConnection.prepareStatement(sqlstmt);

            // The sql parameters count will be calculated by considering the sql query built by getSqlQuery() method.
            int count = 1;
            prepStmt.setInt(count++, tenantId);
            for (LoginIdentifier loginIdentifier : loginIdentifiers) {
                prepStmt.setString(count++, loginIdentifier.getLoginKey());
                prepStmt.setString(count++, loginIdentifier.getLoginValue());
                prepStmt.setString(count++, loginIdentifier.getProfileName());
                prepStmt.setInt(count++, tenantId);
            }
            rs = prepStmt.executeQuery();

            int resultsCount = 0;
            while (rs.next()) {
                // Handle multiple matching users.
                resultsCount++;
                if (resultsCount > 1) {
                    String reason = "Invalid scenario. Multiple users found for the given attribute values: ";
                    if (log.isDebugEnabled()) {
                        log.debug(reason);
                    }
                    return getAuthenticationResult(reason);
                }

                String userID = rs.getString(1);
                String userName = rs.getString(2);
                String storedPassword = rs.getString(3);
                String saltValue = null;
                if ("true".equalsIgnoreCase(
                        realmConfig.getUserStoreProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                    saltValue = rs.getString(4);
                }

                boolean requireChange = rs.getBoolean(5);
                Timestamp changedTime = rs.getTimestamp(6);

                GregorianCalendar gc = new GregorianCalendar();
                gc.add(GregorianCalendar.HOUR, -24);
                Date date = gc.getTime();

                if (requireChange && changedTime.before(date)) {
                    isAuthed = false;
                    authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(new FailureReason("Password change required."));
                } else {
                    password = preparePassword(credential, saltValue);
                    if ((storedPassword != null) && (storedPassword.equals(password))) {
                        isAuthed = true;
                        user = new User(userID, userName, userName, null, null, null, null);
                        try {
                            user.setTenantDomain(getTenantDomain(tenantId));
                            user.setUserStoreDomain(UserCoreUtil.getDomainName(realmConfig));
                        } catch (org.wso2.carbon.user.api.UserStoreException e) {
                            throw new UserStoreException(e);
                        }
                        authenticationResult = new AuthenticationResult(
                                AuthenticationResult.AuthenticationStatus.SUCCESS);
                        authenticationResult.setAuthenticatedUser(user);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving user authentication info for user : ";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException("Authentication Failure", e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        if (log.isDebugEnabled()) {
            log.debug("User login attempt. Login status: " + isAuthed);
        }

        return authenticationResult;
    }

    private StringBuilder getSqlQuery(int limit) {

        StringBuilder sqlStatement = new StringBuilder("SELECT UM_USER.UM_USER_ID, UM_USER.UM_USER_NAME, UM_USER"
                + ".UM_USER_PASSWORD, UM_USER.UM_SALT_VALUE, UM_USER.UM_REQUIRE_CHANGE, UM_USER.UM_CHANGED_TIME FROM "
                + "UM_USER WHERE UM_USER.UM_TENANT_ID=? AND UM_USER.UM_ID IN (");

        for (int i = 1; i <= limit; i++) {
            sqlStatement.append("SELECT UM_USER_ATTRIBUTE.UM_USER_ID FROM UM_USER_ATTRIBUTE WHERE UM_ATTR_NAME = ? "
                    + "AND UM_ATTR_VALUE = ? AND UM_PROFILE_ID = ? AND UM_USER_ATTRIBUTE.UM_TENANT_ID=?");
            if (i < limit) {
                sqlStatement.append(" AND ");
            }
        }

        sqlStatement.append(")");
        return sqlStatement;
    }

    @Override
    public AuthenticationResult doAuthenticateWithID(String preferredUserNameProperty, String preferredUserNameValue,
            Object credential, String profileName) throws UserStoreException {

        // If the user is trying to authenticate with username.
        if (preferredUserNameProperty.equals(getUserNameMappedAttribute())) {
            return doAuthenticateWithUserName(preferredUserNameValue, credential);
        }

        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);
        User user;

        if (!isValidCredentials(credential)) {
            String reason = "Password validation failed";
            if (log.isDebugEnabled()) {
                log.debug(reason);
            }
            return getAuthenticationResult(reason);
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
                    String reason = "Invalid scenario. Multiple users found for the given username property: "
                            + preferredUserNameProperty + " and value: " + preferredUserNameValue;
                    if (log.isDebugEnabled()) {
                        log.debug(reason);
                    }
                    return getAuthenticationResult(reason);
                }

                String userID = rs.getString(1);
                String userName = rs.getString(2);
                String storedPassword = rs.getString(3);
                String saltValue = null;
                if ("true".equalsIgnoreCase(
                        realmConfig.getUserStoreProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                    saltValue = rs.getString(4);
                }

                boolean requireChange = rs.getBoolean(5);
                Timestamp changedTime = rs.getTimestamp(6);

                GregorianCalendar gc = new GregorianCalendar();
                gc.add(GregorianCalendar.HOUR, -24);
                Date date = gc.getTime();

                if (requireChange && changedTime.before(date)) {
                    isAuthed = false;
                    authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(new FailureReason("Password change required."));
                } else {
                    password = preparePassword(credential, saltValue);
                    if ((storedPassword != null) && (storedPassword.equals(password))) {
                        isAuthed = true;
                        user = getUser(userID, userName);
                        user.setPreferredUsername(preferredUserNameProperty);
                        authenticationResult = new AuthenticationResult(
                                AuthenticationResult.AuthenticationStatus.SUCCESS);
                        authenticationResult.setAuthenticatedUser(user);
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
            log.debug("User " + preferredUserNameValue + " login attempt. Login success: " + isAuthed);
        }

        return authenticationResult;
    }

    @Override
    public AuthenticationResult doAuthenticateWithID(String userID, Object credential) throws UserStoreException {

        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);
        User user;

        if (!isValidCredentials(credential)) {
            String reason = "Password validation failed";
            if (log.isDebugEnabled()) {
                log.debug(reason);
            }
            return getAuthenticationResult(reason);
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

            sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER_ID);

            if (log.isDebugEnabled()) {
                log.debug(sqlstmt);
            }

            prepStmt = dbConnection.prepareStatement(sqlstmt);
            prepStmt.setString(1, userID);
            if (sqlstmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String userName = rs.getString(2);
                String storedPassword = rs.getString(3);
                String saltValue = null;
                if ("true".equalsIgnoreCase(
                        realmConfig.getUserStoreProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                    saltValue = rs.getString(4);
                }
                boolean requireChange = rs.getBoolean(5);
                Timestamp changedTime = rs.getTimestamp(6);

                GregorianCalendar gc = new GregorianCalendar();
                gc.add(GregorianCalendar.HOUR, -24);
                Date date = gc.getTime();

                if (requireChange && changedTime.before(date)) {
                    isAuthed = false;
                    authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(new FailureReason("Password change required."));
                } else {
                    password = preparePassword(credential, saltValue);
                    if ((storedPassword != null) && (storedPassword.equals(password))) {
                        isAuthed = true;
                        user = getUser(userID, userName);
                        authenticationResult = new AuthenticationResult(
                                AuthenticationResult.AuthenticationStatus.SUCCESS);
                        authenticationResult.setAuthenticatedUser(user);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving user authentication info for userID : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException("Authentication Failure", e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        if (log.isDebugEnabled()) {
            log.debug("UserID " + userID + " login attempt. Login success: " + isAuthed);
        }

        return authenticationResult;
    }

    protected AuthenticationResult doAuthenticateWithUserName(String userName, Object credential)
            throws UserStoreException {

        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);
        User user;

        // In order to avoid unnecessary db queries.
        if (!isValidUserName(userName)) {
            String reason = "Username validation failed.";
            if (log.isDebugEnabled()) {
                log.debug(reason);
            }
            return getAuthenticationResult(reason);
        }

        if (UserCoreUtil.isRegistryAnnonymousUser(userName)) {
            String reason = "Anonymous user trying to login.";
            log.error(reason);
            return getAuthenticationResult(reason);
        }

        if (!isValidCredentials(credential)) {
            String reason = "Password validation failed.";
            if (log.isDebugEnabled()) {
                log.debug(reason);
            }
            return getAuthenticationResult(reason);
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
                sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER_NAME);
            } else {
                sqlstmt = realmConfig
                        .getUserStoreProperty(JDBCCaseInsensitiveConstants.SELECT_USER_NAME_CASE_INSENSITIVE);
            }

            if (log.isDebugEnabled()) {
                log.debug(sqlstmt);
            }

            prepStmt = dbConnection.prepareStatement(sqlstmt);
            prepStmt.setString(1, userName);
            if (sqlstmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }

            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String userID = rs.getString(1);
                String storedPassword = rs.getString(3);
                String saltValue = null;
                if ("true".equalsIgnoreCase(
                        realmConfig.getUserStoreProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS))) {
                    saltValue = rs.getString(4);
                }
                boolean requireChange = rs.getBoolean(5);
                Timestamp changedTime = rs.getTimestamp(6);

                GregorianCalendar gc = new GregorianCalendar();
                gc.add(GregorianCalendar.HOUR, -24);
                Date date = gc.getTime();

                if (requireChange && changedTime.before(date)) {
                    isAuthed = false;
                    authenticationResult = new AuthenticationResult(AuthenticationResult.AuthenticationStatus.FAIL);
                    authenticationResult.setFailureReason(new FailureReason("Password change required."));
                } else {
                    password = preparePassword(credential, saltValue);
                    if ((storedPassword != null) && (storedPassword.equals(password))) {
                        isAuthed = true;
                        user = getUser(userID, userName);
                        authenticationResult = new AuthenticationResult(
                                AuthenticationResult.AuthenticationStatus.SUCCESS);
                        authenticationResult.setAuthenticatedUser(user);
                    }
                }
            }
        } catch (SQLException e) {
            String msg = "Error occurred while retrieving user authentication info for userName : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException("Authentication Failure", e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        if (log.isDebugEnabled()) {
            log.debug("UserName " + userName + " login attempt. Login success: " + isAuthed);
        }

        return authenticationResult;
    }

    private AuthenticationResult getAuthenticationResult(String reason) {

        AuthenticationResult authenticationResult = new AuthenticationResult(
                AuthenticationResult.AuthenticationStatus.FAIL);
        authenticationResult.setFailureReason(new FailureReason(reason));
        return authenticationResult;
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public User doAddUserWithID(String userName, Object credential, String[] roleList, Map<String, String> claims,
            String profileName, boolean requirePasswordChange) throws UserStoreException {

        // Assigning unique user ID of the user as the username in the system.
        String userID = getUniqueUserID();
        // Assign preferredUsername to the username claim.
        claims = addUserNameAttribute(userName, claims);
        // Assign userID to the userid claim.
        claims = addUserIDAttribute(userID, claims);
        persistUser(userID, userName, credential, roleList, claims, profileName, requirePasswordChange);

        return getUser(userID, userName);

    }

    /*
     * This method persists the user information in the database.
     */
    protected void persistUser(String userID, String userName, Object credential, String[] roleList,
            Map<String, String> claims, String profileName, boolean requirePasswordChange) throws UserStoreException {

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
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, userName, password, "",
                        requirePasswordChange, new Date(), tenantId);
            } else if (sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue != null)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, userName, password, saltValue,
                        requirePasswordChange, new Date(), tenantId);
            } else if (!sqlStmt1.contains(UserCoreConstants.UM_TENANT_COLUMN) && (saltValue == null)) {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, userName, password, "",
                        requirePasswordChange, new Date());
            } else {
                this.updateStringValuesToDatabase(dbConnection, sqlStmt1, userID, userName, password, saltValue,
                        requirePasswordChange, new Date());
            }

            if (roleList != null && roleList.length > 0) {

                RoleBreakdown breakdown = getSharedRoleBreakdown(roleList);
                String[] roles = breakdown.getRoles();

                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantIDs();

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
                // Add user properties.
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
                String errorMessage = "Error while rollback add user operation for user : " + userName;
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e1);
                }
                throw new UserStoreException(errorMessage, e1);
            }
            String errorMessage = "Error while persisting user : " + userName;
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
    public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected String doGetUserIDFromUserNameWithID(String userName) throws UserStoreException {

        if (userName == null) {
            throw new IllegalArgumentException("userName cannot be null.");
        }

        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String userID = null;
        try {
            dbConnection = getDBConnection();

            if (isCaseSensitiveUsername()) {
                sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER_ID_FROM_USER_NAME);
            } else {
                sqlStmt = realmConfig.getUserStoreProperty(
                        JDBCCaseInsensitiveConstants.SELECT_USER_ID_FROM_USER_NAME_CASE_INSENSITIVE);
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, userName);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                userID = rs.getString(1);
            }
        } catch (SQLException e) {
            String msg = "Database error occurred while retrieving userID for a UserName : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        return userID;
    }

    @Override
    public String getUserIDFromProperties(String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        try {
            String property = claimManager.getAttributeName(getMyDomainName(), claimURI);
            if (property == null) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "Could not find the matching property for claim URI: " + claimURI + " in user " + "domain: "
                                    + getMyDomainName());
                }
                return null;
            }
            List<String> userIds = this.doGetUserListFromPropertiesWithID(property, claimValue, profileName);
            if (userIds.isEmpty()) {
                if (log.isDebugEnabled()) {
                    log.debug("No UserID found for the claim: " + claimURI + ", value: " + claimValue + ", in domain:"
                            + " " + getMyDomainName());
                }
                return null;
            } else if (userIds.size() > 1) {
                throw new UserStoreException(
                        "Invalid scenario. Multiple users cannot be found for the given value: " + claimValue
                                + "of the " + "claim: " + claimURI);
            } else {
                // username can have only one userId. Take the first element.
                return userIds.get(0);
            }
        } catch (org.wso2.carbon.user.api.UserStoreException e) {
            throw new UserStoreException(
                    "Error occurred while retrieving the userId of domain : " + getMyDomainName() + " and " + "claim"
                            + claimURI + " value: " + claimValue, e);
        }
    }

    @Override
    public String doGetUserNameFromUserIDWithID(String userID) throws UserStoreException {

        if (userID == null) {
            throw new IllegalArgumentException("userID cannot be null.");
        }

        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String userName = null;
        try {
            dbConnection = getDBConnection();
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER_NAME_FROM_USER_ID);

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, userID);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                userName = rs.getString(1);
            }
        } catch (SQLException e) {
            String msg = "Database error occurred while retrieving userName for a userID : " + userID;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        return userName;
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
    public String[] doListUsers(String filter, int maxItemLimit) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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
    public void doSetUserClaimValue(String userName, String claimURI, String claimValue, String profileName)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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

    @Override
    public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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
        breakdown.setRoles(roles.toArray(new String[0]));
        breakdown.setTenantIds(tenantIds.toArray(new Integer[0]));

        // Shared roles and tenant ids
        breakdown.setSharedRoles(sharedRoles.toArray(new String[0]));
        breakdown.setSharedTenantIDs(sharedTenantIds.toArray(new Integer[0]));

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
                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantIDs();

                String sqlStmt1;
                if (roles.length > 0) {
                    sqlStmt1 = realmConfig.getUserStoreProperty(JDBCRealmConstants.REMOVE_ROLE_FROM_USER_WITH_ID);
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
                    if (!doCheckIsUserInRoleWithID(userID, role)) {
                        newRoleList.add(role);
                    }
                }

                String[] rolesToAdd = newRoleList.toArray(new String[0]);
                // If user name and role names are prefixed with domain name, remove the domain name
                RoleBreakdown breakdown = getSharedRoleBreakdown(rolesToAdd);
                String[] roles = breakdown.getRoles();
                String[] sharedRoles = breakdown.getSharedRoles();
                Integer[] sharedTenantIds = breakdown.getSharedTenantIDs();

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
    public String[] doGetExternalRoleListOfUser(String userName, String filter) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected void doSetUserAttribute(String userName, String attributeName, String value, String profileName)
            throws UserStoreException {

        // This method is not needed for JDBC userstores as the users are already added with the IDs.
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
    public void doSetUserClaimValues(String userName, Map<String, String> claims, String profileName)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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
            Map<String, String> availableProperties = getUserPropertyValuesWithID(userID, propertyArr, profileName);
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
    public void doDeleteUserClaimValue(String userName, String claimURI, String profileName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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
    public void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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
    public void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public void doUpdateCredentialWithID(String userID, Object newCredential, Object oldCredential)
            throws UserStoreException {

        // No need to check old password here because we already authenticate in super class
        this.doUpdateCredentialByAdminWithID(userID, newCredential);
    }

    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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
    public void doDeleteUser(String userName) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public Date doGetPasswordExpirationTimeWithID(String userID) throws UserStoreException {

        if (userID != null && userID.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
            return super.getPasswordExpirationTimeWithID(userID);
        }

        Connection dbConnection = null;
        ResultSet rs = null;
        PreparedStatement prepStmt = null;
        String sqlstmt;
        Date date = null;

        try {
            dbConnection = getDBConnection();
            dbConnection.setAutoCommit(false);

            sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER_ID_WITH_ID);
            if (log.isDebugEnabled()) {
                log.debug(sqlstmt);
            }

            prepStmt = dbConnection.prepareStatement(sqlstmt);
            prepStmt.setString(1, userID);
            if (sqlstmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                boolean requireChange = rs.getBoolean(1);
                Timestamp changedTime = rs.getTimestamp(2);
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
     * @return saltValue.
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
     * @param userID
     * @param propertyName
     * @param profileName
     * @return
     * @throws UserStoreException
     */
    protected String getProperty(Connection dbConnection, String userID, String propertyName, String profileName)
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
            prepStmt.setString(1, userID);
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
            String msg =
                    "Error occurred while retrieving user profile property for user : " + userID + " & property name : "
                            + propertyName + " & profile name : " + profileName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(null, rs, prepStmt);
        }
    }

    @Override
    public List<String> doGetUserListFromPropertiesWithID(String property, String value, String profileName)
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

        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        List<String> userList = new ArrayList<>();
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
                String userID = rs.getString(1);
                userList.add(userID);
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

        return userList;
    }

    @Override
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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
        return roles.toArray(new String[0]);
    }

    @Override
    protected String[] doGetSharedRoleListOfUser(String userName, String tenantDomain, String filter)
            throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected Map<String, Map<String, String>> getUsersPropertyValuesWithID(List<String> users, String[] propertyNames,
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
                String userID = rs.getString(1);
                String value = rs.getString(3);

                if (usersPropertyValuesMap.get(userID) != null) {
                    usersPropertyValuesMap.get(userID).put(name, value);
                } else {
                    Map<String, String> attributes = new HashMap<>();
                    attributes.put(name, value);
                    usersPropertyValuesMap.put(userID, attributes);
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

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected Map<String, List<String>> doGetExternalRoleListOfUsersWithID(List<String> userIDs)
            throws UserStoreException {

        if (log.isDebugEnabled()) {
            log.debug("Getting roles of users: " + userIDs);
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
                throw new UserStoreException("The sql statement for retrieving users roles is null.");
            }
            for (int i = 0; i < userIDs.size(); i++) {

                usernameParameter.append("'").append(userIDs.get(i)).append("'");

                if (i != userIDs.size() - 1) {
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
                String userID = UserCoreUtil.addDomainToName(rs.getString(1), domainName);
                String roleName = UserCoreUtil.addDomainToName(rs.getString(2), domainName);
                if (rolesListOfUsersMap.get(userID) != null) {
                    rolesListOfUsersMap.get(userID).add(roleName);
                } else {
                    List<String> roleNames = new ArrayList<>();
                    roleNames.add(roleName);
                    rolesListOfUsersMap.put(userID, roleNames);
                }
            }
            return rolesListOfUsersMap;
        } catch (SQLException e) {
            String errorMessage = "Error Occurred while getting role lists of users.";
            if (log.isDebugEnabled()) {
                errorMessage = errorMessage + ": " + userIDs;
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

    @Override
    public void doAddRole(String roleName, String[] userList, boolean shared) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
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

    @Override
    protected PaginatedSearchResult doListUsers(String filter, int limit, int offset) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected UniqueIDPaginatedSearchResult doListUsersWithID(String filter, int limit, int offset)
            throws UserStoreException {

        List<User> users = new ArrayList<>();
        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int givenMax;
        int searchTime;
        String profileName = UserCoreConstants.DEFAULT_PROFILE;

        UniqueIDPaginatedSearchResult result = new UniqueIDPaginatedSearchResult();

        if (limit == 0) {
            return result;
        }

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

        if (limit < 0 || limit > givenMax) {
            limit = givenMax;
        }

        try {

            if (filter != null && filter.trim().length() != 0) {
                filter = filter.trim();
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
            } else {
                filter = "%";
            }

            List<User> list = new LinkedList<>();

            dbConnection = getDBConnection();

            if (dbConnection == null) {
                throw new UserStoreException("Attempts to establish a connection with the data source has failed.");
            }

            String type = DatabaseCreator.getDatabaseType(dbConnection);

            if (offset <= 0) {
                offset = 0;
            } else {
                offset = offset - 1;
            }

            if (DB2.equalsIgnoreCase(type)) {
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            } else if (MSSQL.equalsIgnoreCase(type)) {
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
            } else if (ORACLE.equalsIgnoreCase(type)) {
                limit = offset + limit;
            }

            if (isCaseSensitiveUsername()) {
                sqlStmt = realmConfig
                        .getUserStoreProperty(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_WITH_ID + "-" + type);
                if (sqlStmt == null) {
                    sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_WITH_ID);
                }
            } else {
                sqlStmt = realmConfig.getUserStoreProperty(
                        JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE_PAGINATED + "-" + type);
                if (sqlStmt == null) {
                    sqlStmt = realmConfig.getUserStoreProperty(
                            JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE_PAGINATED);
                }
            }

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, filter);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
                prepStmt.setLong(3, limit);
                prepStmt.setLong(4, offset);
            } else {
                prepStmt.setLong(2, limit);
                prepStmt.setLong(3, offset);
            }

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
                    return result;
                }
                String errorMessage =
                        "Error while fetching users according to filter : " + filter + " & limit " + ": " + limit;
                if (log.isDebugEnabled()) {
                    log.debug(errorMessage, e);
                }
                throw new UserStoreException(errorMessage, e);
            }

            while (rs.next()) {
                String userID = rs.getString(1);
                String userName = rs.getString(2);
                if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
                    continue;
                }
                User user = getUser(userID, userName);
                list.add(user);
            }
            rs.close();

            if (list.size() > 0) {
                users = list;
            }
            users.sort(Comparator.comparing(User::getUsername));
        } catch (Exception e) {
            String msg = "Error occurred while retrieving users for filter : " + filter + " & limit : " + limit;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        result.setUsers(users);

        if (users.size() == 0) {
            result.setSkippedUserCount(doGetListUsersCountWithID(filter));
        }
        return result;
    }

    protected int doGetListUsersCountWithID(String filter) throws UserStoreException {

        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int count = 0;

        try {

            if (filter != null && StringUtils.isNotEmpty(filter.trim())) {
                filter = filter.trim().replace("*", "%");
                filter = filter.replace("?", "_");
            } else {
                filter = "%";
            }

            dbConnection = getDBConnection();

            if (dbConnection == null) {
                throw new UserStoreException("null connection");
            }

            if (isCaseSensitiveUsername()) {
                sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_USER_FILTER_PAGINATED_COUNT_WITH_ID);
            } else {
                sqlStmt = realmConfig.getUserStoreProperty(
                        JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_PAGINATED_COUNT_WITH_ID);
            }

            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, filter);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(2, tenantId);
            }

            rs = prepStmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            String msg = "Error occurred while retrieving users count for filter : " + filter;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
        return count;
    }

    @Override
    public PaginatedSearchResult getUserListFromProperties(String property, String value, String profileName, int limit,
            int offset) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public UniqueIDPaginatedSearchResult doGetUserListFromPropertiesWithID(String property, String value,
            String profileName, int limit, int offset) throws UserStoreException {

        UniqueIDPaginatedSearchResult result = new UniqueIDPaginatedSearchResult();

        if (profileName == null) {
            profileName = UserCoreConstants.DEFAULT_PROFILE;
        }

        if (limit == 0) {
            return result;
        }

        if (value == null) {
            throw new IllegalArgumentException("Filter value cannot be null.");
        }
        if (value.contains(QUERY_FILTER_STRING_ANY)) {
            // This is to support LDAP like queries. Value having only * is restricted except one *.
            if (!value.matches("(\\*)\\1+")) {
                // Convert all the * to % except \*.
                value = value.replaceAll("(?<!\\\\)\\*", SQL_FILTER_STRING_ANY);
            }
        }

        List<User> users = new ArrayList<>();
        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        List<User> list = new ArrayList<>();
        try {
            dbConnection = getDBConnection();
            String type = DatabaseCreator.getDatabaseType(dbConnection);

            if (offset <= 0) {
                offset = 0;
            } else {
                offset = offset - 1;
            }

            if (ORACLE.equalsIgnoreCase(type)) {
                limit = offset + limit;
            } else if (MSSQL.equalsIgnoreCase(type)) {
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
            } else if (DB2.equalsIgnoreCase(type)) {
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            }

            sqlStmt = realmConfig
                    .getUserStoreProperty(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_WITH_ID + "-" + type);
            if (sqlStmt == null) {
                sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_PAGINATED_USERS_FOR_PROP_WITH_ID);
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, property);
            prepStmt.setString(2, value);
            prepStmt.setString(3, profileName);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(4, tenantId);
                prepStmt.setInt(5, tenantId);
                prepStmt.setInt(6, limit);
                prepStmt.setInt(7, offset);
            } else {
                prepStmt.setInt(4, limit);
                prepStmt.setInt(5, offset);
            }
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                String userID = rs.getString(1);
                String userName = rs.getString(2);
                User user = getUser(userID, userName);
                list.add(user);
            }

            if (list.size() > 0) {
                users = list;
            }
            result.setUsers(users);
        } catch (Exception e) {
            String msg = "Database error occurred while paginating users for a property : " + property + " & value : "
                    + value + "& profile name : " + profileName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        if (users.size() == 0) {
            result.setSkippedUserCount(getUserListFromPropertiesCountWithID(property, value, profileName));
        }
        return result;
    }

    protected int getUserListFromPropertiesCountWithID(String property, String value, String profileName)
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

        int count = 0;
        Connection dbConnection = null;
        String sqlStmt;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        try {
            dbConnection = getDBConnection();
            sqlStmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.GET_PAGINATED_USERS_COUNT_FOR_PROP_WITH_ID);
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, property);
            prepStmt.setString(2, value);
            prepStmt.setString(3, profileName);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(4, tenantId);
                prepStmt.setInt(5, tenantId);
            }
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }

        } catch (SQLException e) {
            String msg = "Database error occurred while paginating users count for a property : " + property + " & "
                    + "value :" + " " + value + "& profile name : " + profileName;
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        return count;
    }

    @Override
    protected PaginatedSearchResult doGetUserList(Condition condition, String profileName, int limit, int offset,
            String sortBy, String sortOrder) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    protected UniqueIDPaginatedSearchResult doGetUserListWithID(Condition condition, String profileName, int limit,
            int offset, String sortBy, String sortOrder) throws UserStoreException {

        boolean isGroupFiltering = false;
        boolean isUsernameFiltering = false;
        boolean isClaimFiltering = false;
        // To identify Mysql multi group filter and multi claim filter.
        int totalMultiGroupFilters = 0;
        int totalMultiClaimFilters = 0;

        UniqueIDPaginatedSearchResult result = new UniqueIDPaginatedSearchResult();

        if (limit == 0) {
            return result;
        }

        // Since we support only AND operation get expressions as a list.
        List<ExpressionCondition> expressionConditions = new ArrayList<>();
        getExpressionConditions(condition, expressionConditions);

        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName())) {
                isGroupFiltering = true;
                totalMultiGroupFilters++;
            } else if (ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                isUsernameFiltering = true;
            } else {
                isClaimFiltering = true;
                totalMultiClaimFilters++;
            }
        }

        List<User> users = new ArrayList<>();
        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        List<User> list = new ArrayList<>();
        try {
            dbConnection = getDBConnection();
            String type = DatabaseCreator.getDatabaseType(dbConnection);

            if (offset <= 0) {
                offset = 0;
            } else {
                offset = offset - 1;
            }

            if (DB2.equalsIgnoreCase(type)) {
                int initialOffset = offset;
                offset = offset + limit;
                limit = initialOffset + 1;
            } else if (ORACLE.equalsIgnoreCase(type)) {
                limit = offset + limit;
            } else if (MSSQL.equalsIgnoreCase(type)) {
                int initialOffset = offset;
                offset = limit + offset;
                limit = initialOffset + 1;
            }

            SqlBuilder sqlBuilder = getQueryString(isGroupFiltering, isUsernameFiltering, isClaimFiltering,
                    expressionConditions, limit, offset, sortBy, sortOrder, profileName, type, totalMultiGroupFilters,
                    totalMultiClaimFilters);

            if (MYSQL.equals(type) && totalMultiGroupFilters > 1 && totalMultiClaimFilters > 1) {
                String fullQuery = sqlBuilder.getQuery();
                String[] splits = fullQuery.split("INTERSECT ");
                int startIndex = 0;
                int endIndex = 0;
                for (String query : splits) {
                    List<User> tempUserList = new ArrayList<>();
                    int occurrence = StringUtils.countMatches(query, QUERY_BINDING_SYMBOL);
                    endIndex = endIndex + occurrence;
                    prepStmt = dbConnection.prepareStatement(query);
                    populatePrepareStatement(sqlBuilder, prepStmt, startIndex, endIndex);
                    rs = prepStmt.executeQuery();
                    while (rs.next()) {
                        String userID = rs.getString(1);
                        String userName = rs.getString(2);
                        User user = getUser(userID, userName);
                        tempUserList.add(user);
                    }

                    if (startIndex == 0) {
                        list = tempUserList;
                    } else {
                        list.retainAll(tempUserList);
                    }
                    startIndex += occurrence;
                }
            } else {
                prepStmt = dbConnection.prepareStatement(sqlBuilder.getQuery());
                int occurrence = StringUtils.countMatches(sqlBuilder.getQuery(), "?");
                populatePrepareStatement(sqlBuilder, prepStmt, 0, occurrence);
                rs = prepStmt.executeQuery();
                while (rs.next()) {
                    String userID = rs.getString(1);
                    String userName = rs.getString(2);
                    User user = getUser(userID, userName);
                    list.add(user);
                }
            }

            if (list.size() > 0) {
                users = list;
            }
            result.setUsers(users);

        } catch (Exception e) {
            String msg = "Error occur while doGetUserList for multi attribute searching";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        return result;
    }

    private void populatePrepareStatement(SqlBuilder sqlBuilder, PreparedStatement prepStmt, int startIndex,
            int endIndex) throws SQLException {

        Map<Integer, Integer> integerParameters = sqlBuilder.getIntegerParameters();
        Map<Integer, String> stringParameters = sqlBuilder.getStringParameters();
        Map<Integer, Long> longParameters = sqlBuilder.getLongParameters();

        for (Map.Entry<Integer, Integer> entry : integerParameters.entrySet()) {
            if (entry.getKey() > startIndex && entry.getKey() <= endIndex) {
                prepStmt.setInt(entry.getKey() - startIndex, entry.getValue());
            }
        }

        for (Map.Entry<Integer, String> entry : stringParameters.entrySet()) {
            if (entry.getKey() > startIndex && entry.getKey() <= endIndex) {
                prepStmt.setString(entry.getKey() - startIndex, entry.getValue());
            }
        }

        for (Map.Entry<Integer, Long> entry : longParameters.entrySet()) {
            if (entry.getKey() > startIndex && entry.getKey() <= endIndex) {
                prepStmt.setLong(entry.getKey() - startIndex, entry.getValue());
            }
        }
    }

    protected SqlBuilder getQueryString(boolean isGroupFiltering, boolean isUsernameFiltering, boolean isClaimFiltering,
            List<ExpressionCondition> expressionConditions, int limit, int offset, String sortBy, String sortOrder,
            String profileName, String dbType, int totalMultiGroupFilters, int totalMultiClaimFilters)
            throws UserStoreException {

        StringBuilder sqlStatement;
        SqlBuilder sqlBuilder;
        boolean hitGroupFilter = false;
        boolean hitClaimFilter = false;
        int groupFilterCount = 0;
        int claimFilterCount = 0;

        if (isGroupFiltering && isUsernameFiltering && isClaimFiltering || isGroupFiltering && isClaimFiltering) {

            if (DB2.equals(dbType)) {
                sqlStatement = new StringBuilder("SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT "
                        + "ROW_NUMBER() OVER (ORDER BY UM_USER_NAME) AS rn, p.*  FROM (SELECT DISTINCT UM_USER_NAME  "
                        + "FROM UM_ROLE R INNER JOIN UM_USER_ROLE UR ON R.UM_ID = UR.UM_ROLE_ID INNER JOIN UM_USER U "
                        + "ON UR.UM_USER_ID =U.UM_ID INNER JOIN UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID");
            } else if (MSSQL.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT UM_USER_NAME, ROW_NUMBER() OVER "
                                + "(ORDER BY UM_USER_NAME) AS RowNum FROM (SELECT DISTINCT UM_USER_NAME FROM UM_ROLE "
                                + "R INNER JOIN UM_USER_ROLE UR ON R.UM_ID = UR.UM_ROLE_ID INNER JOIN UM_USER U ON UR"
                                + ".UM_USER_ID =U.UM_ID INNER JOIN UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID");
            } else if (ORACLE.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT UM_USER_NAME, rownum AS rnum "
                                + "FROM (SELECT  UM_USER_NAME FROM UM_ROLE R INNER JOIN UM_USER_ROLE UR ON R.UM_ID = UR"
                                + ".UM_ROLE_ID INNER JOIN UM_USER U ON UR.UM_USER_ID =U.UM_ID INNER JOIN "
                                + "UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID");
            } else {
                sqlStatement = new StringBuilder(
                        "SELECT DISTINCT U.UM_USER_ID, U.UM_USER_NAME FROM UM_ROLE R INNER JOIN "
                                + "UM_USER_ROLE UR INNER JOIN UM_USER U INNER JOIN UM_USER_ATTRIBUTE UA ON R.UM_ID = "
                                + "UR.UM_ROLE_ID AND UR.UM_USER_ID =" + " U.UM_ID AND U.UM_ID = UA.UM_USER_ID");
            }
            sqlBuilder = new SqlBuilder(sqlStatement).where("R.UM_TENANT_ID = ?", tenantId)
                    .where("U.UM_TENANT_ID = ?", tenantId).where("UR.UM_TENANT_ID = ?", tenantId)
                    .where("UA.UM_TENANT_ID = ?", tenantId).where("UA.UM_PROFILE_ID = ?", profileName);
        } else if (isGroupFiltering && isUsernameFiltering || isGroupFiltering) {
            if (DB2.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT ROW_NUMBER() OVER (ORDER BY "
                                + "UM_USER_NAME) AS rn, p.*  FROM (SELECT DISTINCT UM_USER_NAME  FROM UM_ROLE R INNER"
                                + " JOIN UM_USER_ROLE UR ON R.UM_ID = UR.UM_ROLE_ID INNER JOIN UM_USER U ON UR"
                                + ".UM_USER_ID "
                                + "=U.UM_ID ");
            } else if (MSSQL.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT UM_USER_NAME, ROW_NUMBER() OVER "
                                + "(ORDER BY UM_USER_NAME) AS RowNum FROM (SELECT DISTINCT UM_USER_NAME FROM UM_ROLE "
                                + "R INNER JOIN UM_USER_ROLE UR ON R.UM_ID = UR.UM_ROLE_ID INNER JOIN UM_USER U ON UR"
                                + ".UM_USER_ID =U.UM_ID");
            } else if (ORACLE.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT UM_USER_NAME, rownum AS rnum "
                                + "FROM (SELECT  UM_USER_NAME FROM UM_ROLE R INNER JOIN UM_USER_ROLE UR ON R.UM_ID = UR"
                                + ".UM_ROLE_ID INNER JOIN UM_USER U ON UR.UM_USER_ID =U.UM_ID");
            } else {
                sqlStatement = new StringBuilder(
                        "SELECT DISTINCT U.UM_USER_ID, U.UM_USER_NAME FROM UM_ROLE R INNER JOIN "
                                + "UM_USER_ROLE UR INNER JOIN UM_USER U ON R.UM_ID = UR.UM_ROLE_ID AND UR.UM_USER_ID "
                                + "=U.UM_ID");
            }

            sqlBuilder = new SqlBuilder(sqlStatement).where("R.UM_TENANT_ID = ?", tenantId)
                    .where("U.UM_TENANT_ID = ?", tenantId).where("UR.UM_TENANT_ID = ?", tenantId);
        } else if (isUsernameFiltering && isClaimFiltering || isClaimFiltering) {
            if (DB2.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT ROW_NUMBER() OVER (ORDER BY "
                                + "UM_USER_NAME) AS rn, p.*  FROM (SELECT DISTINCT UM_USER_NAME  FROM  UM_USER U "
                                + "INNER JOIN UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID");
            } else if (MSSQL.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT UM_USER_NAME, ROW_NUMBER() OVER "
                                + "(ORDER BY UM_USER_NAME) AS RowNum FROM (SELECT DISTINCT UM_USER_NAME FROM UM_USER "
                                + "U INNER JOIN UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID");
            } else if (ORACLE.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT UM_USER_NAME, rownum AS rnum "
                                + "FROM (SELECT UM_USER_NAME FROM UM_USER U INNER JOIN UM_USER_ATTRIBUTE UA ON U"
                                + ".UM_ID = "
                                + "UA.UM_USER_ID");
            } else {
                sqlStatement = new StringBuilder(
                        "SELECT DISTINCT U.UM_USER_ID, U.UM_USER_NAME FROM UM_USER U INNER JOIN "
                                + "UM_USER_ATTRIBUTE UA ON U.UM_ID = UA.UM_USER_ID");
            }
            sqlBuilder = new SqlBuilder(sqlStatement).where("U.UM_TENANT_ID = ?", tenantId)
                    .where("UA.UM_TENANT_ID = ?", tenantId).where("UA.UM_PROFILE_ID = ?", profileName);
        } else if (isUsernameFiltering) {
            if (DB2.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT ROW_NUMBER() OVER (ORDER BY "
                                + "UM_USER_NAME) AS rn, p.*  FROM (SELECT DISTINCT UM_USER_NAME  FROM UM_USER U");
            } else if (MSSQL.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT UM_USER_NAME, ROW_NUMBER() OVER "
                                + "(ORDER BY UM_USER_NAME) AS RowNum FROM (SELECT DISTINCT UM_USER_NAME FROM UM_USER "
                                + "U");
            } else if (ORACLE.equals(dbType)) {
                sqlStatement = new StringBuilder(
                        "SELECT U.UM_USER_ID, U.UM_USER_NAME FROM (SELECT UM_USER_NAME, rownum AS rnum "
                                + "FROM (SELECT UM_USER_NAME FROM UM_USER U");
            } else {
                sqlStatement = new StringBuilder("SELECT U.UM_USER_ID, U.UM_USER_NAME FROM UM_USER U");
            }

            sqlBuilder = new SqlBuilder(sqlStatement).where("U.UM_TENANT_ID = ?", tenantId);
        } else {
            throw new UserStoreException("Condition is not valid.");
        }

        SqlBuilder header = new SqlBuilder(new StringBuilder(sqlBuilder.getSql()));
        addingWheres(sqlBuilder, header);

        for (ExpressionCondition expressionCondition : expressionConditions) {
            if (ExpressionAttribute.ROLE.toString().equals(expressionCondition.getAttributeName())) {
                if (!MYSQL.equals(dbType) || (MYSQL.equals(dbType) && totalMultiGroupFilters > 1
                        && totalMultiClaimFilters > 1)) {
                    multiGroupQueryBuilder(sqlBuilder, header, hitGroupFilter, expressionCondition);
                    hitGroupFilter = true;
                } else {
                    multiGroupMySqlQueryBuilder(sqlBuilder, groupFilterCount, expressionCondition);
                    groupFilterCount++;
                }
            } else if (ExpressionOperation.EQ.toString().equals(expressionCondition.getOperation())
                    && ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isCaseSensitiveUsername()) {
                    sqlBuilder.where("U.UM_USER_NAME = ?", expressionCondition.getAttributeValue());
                } else {
                    sqlBuilder.where("U.UM_USER_NAME = LOWER(?)", expressionCondition.getAttributeValue());
                }
            } else if (ExpressionOperation.CO.toString().equals(expressionCondition.getOperation())
                    && ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isCaseSensitiveUsername()) {
                    sqlBuilder.where("U.UM_USER_NAME LIKE ?", "%" + expressionCondition.getAttributeValue() + "%");
                } else {
                    sqlBuilder
                            .where("U.UM_USER_NAME LIKE LOWER(?)", "%" + expressionCondition.getAttributeValue() + "%");
                }
            } else if (ExpressionOperation.EW.toString().equals(expressionCondition.getOperation())
                    && ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isCaseSensitiveUsername()) {
                    sqlBuilder.where("U.UM_USER_NAME LIKE ?", "%" + expressionCondition.getAttributeValue());
                } else {
                    sqlBuilder.where("U.UM_USER_NAME LIKE LOWER(?)", "%" + expressionCondition.getAttributeValue());
                }
            } else if (ExpressionOperation.SW.toString().equals(expressionCondition.getOperation())
                    && ExpressionAttribute.USERNAME.toString().equals(expressionCondition.getAttributeName())) {
                if (isCaseSensitiveUsername()) {
                    sqlBuilder.where("U.UM_USER_NAME LIKE ?", expressionCondition.getAttributeValue() + "%");
                } else {
                    sqlBuilder.where("U.UM_USER_NAME LIKE LOWER(?)", expressionCondition.getAttributeValue() + "%");
                }
            } else {
                // Claim filtering
                if (!MYSQL.equals(dbType) || (MYSQL.equals(dbType) && totalMultiGroupFilters > 1
                        && totalMultiClaimFilters > 1)) {
                    multiClaimQueryBuilder(sqlBuilder, header, hitClaimFilter, expressionCondition);
                    hitClaimFilter = true;
                } else {
                    multiClaimMySqlQueryBuilder(sqlBuilder, claimFilterCount, expressionCondition);
                    claimFilterCount++;
                }
            }
        }

        if (MYSQL.equals(dbType)) {
            sqlBuilder.updateSql(" GROUP BY U.UM_USER_NAME ");
            if (groupFilterCount > 0) {
                sqlBuilder.updateSql(" HAVING COUNT(DISTINCT R.UM_ROLE_NAME) = " + groupFilterCount);
            }
            if (claimFilterCount > 0) {
                sqlBuilder.updateSql(" HAVING COUNT(DISTINCT UA.UM_ATTR_VALUE) = " + claimFilterCount);
            }
        }

        if (!(MYSQL.equals(dbType) && totalMultiGroupFilters > 1 && totalMultiClaimFilters > 1)) {
            if (DB2.equals(dbType)) {
                sqlBuilder.setTail(") AS p) WHERE rn BETWEEN ? AND ?", limit, offset);
            } else if (MSSQL.equals(dbType)) {
                sqlBuilder.setTail(") AS R) AS P WHERE P.RowNum BETWEEN ? AND ?", limit, offset);
            } else if (ORACLE.equals(dbType)) {
                sqlBuilder.setTail(" ORDER BY UM_USER_NAME) where rownum <= ?) WHERE  rnum > ?", limit, offset);
            } else {
                sqlBuilder.setTail(" ORDER BY UM_USER_NAME ASC LIMIT ? OFFSET ?", limit, offset);
            }
        }
        return sqlBuilder;
    }

    private void multiGroupQueryBuilder(SqlBuilder sqlBuilder, SqlBuilder header, boolean hitFirstRound,
            ExpressionCondition expressionCondition) {

        if (hitFirstRound) {
            sqlBuilder.updateSql(" INTERSECT " + header.getSql());
            addingWheres(header, sqlBuilder);
            buildGroupWhereConditions(sqlBuilder, expressionCondition.getOperation(),
                    expressionCondition.getAttributeValue());
        } else {
            buildGroupWhereConditions(sqlBuilder, expressionCondition.getOperation(),
                    expressionCondition.getAttributeValue());
        }
    }

    private void buildGroupWhereConditions(SqlBuilder sqlBuilder, String operation, String value) {

        if (ExpressionOperation.EQ.toString().equals(operation)) {
            sqlBuilder.where("R.UM_ROLE_NAME = ?", value);
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            sqlBuilder.where("R.UM_ROLE_NAME LIKE ?", "%" + value);
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            sqlBuilder.where("R.UM_ROLE_NAME LIKE ?", "%" + value + "%");
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            sqlBuilder.where("R.UM_ROLE_NAME LIKE ?", value + "%");
        }
    }

    private void multiGroupMySqlQueryBuilder(SqlBuilder sqlBuilder, int groupFilterCount,
            ExpressionCondition expressionCondition) {

        if (groupFilterCount == 0) {
            buildGroupWhereConditions(sqlBuilder, expressionCondition.getOperation(),
                    expressionCondition.getAttributeValue());
        } else {
            buildGroupConditionWithOROperator(sqlBuilder, expressionCondition.getOperation(),
                    expressionCondition.getAttributeValue());
        }
    }

    private void buildGroupConditionWithOROperator(SqlBuilder sqlBuilder, String operation, String value) {

        if (ExpressionOperation.EQ.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("R.UM_ROLE_NAME = ?", value);
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("R.UM_ROLE_NAME LIKE ?", "%" + value);
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("R.UM_ROLE_NAME LIKE ?", "%" + value + "%");
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("R.UM_ROLE_NAME LIKE ?", value + "%");
        }
    }

    private void multiClaimQueryBuilder(SqlBuilder sqlBuilder, SqlBuilder header, boolean hitFirstRound,
            ExpressionCondition expressionCondition) {

        if (hitFirstRound) {
            sqlBuilder.updateSql(" INTERSECT " + header.getSql());
            addingWheres(header, sqlBuilder);
            buildClaimWhereConditions(sqlBuilder, expressionCondition.getAttributeName(),
                    expressionCondition.getOperation(), expressionCondition.getAttributeValue());
        } else {
            buildClaimWhereConditions(sqlBuilder, expressionCondition.getAttributeName(),
                    expressionCondition.getOperation(), expressionCondition.getAttributeValue());
        }
    }

    private void buildClaimWhereConditions(SqlBuilder sqlBuilder, String attributeName, String operation,
            String attributeValue) {

        sqlBuilder.where("UA.UM_ATTR_NAME = ?", attributeName);

        if (ExpressionOperation.EQ.toString().equals(operation)) {
            sqlBuilder.where("UA.UM_ATTR_VALUE = ?", attributeValue);
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            sqlBuilder.where("UA.UM_ATTR_VALUE LIKE ?", "%" + attributeValue);
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            sqlBuilder.where("UA.UM_ATTR_VALUE LIKE ?", "%" + attributeValue + "%");
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            sqlBuilder.where("UA.UM_ATTR_VALUE LIKE ?", attributeValue + "%");
        }
    }

    private void multiClaimMySqlQueryBuilder(SqlBuilder sqlBuilder, int claimFilterCount,
            ExpressionCondition expressionCondition) {

        if (claimFilterCount == 0) {
            buildClaimWhereConditions(sqlBuilder, expressionCondition.getAttributeName(),
                    expressionCondition.getOperation(), expressionCondition.getAttributeValue());
        } else {
            buildClaimConditionWithOROperator(sqlBuilder, expressionCondition.getAttributeName(),
                    expressionCondition.getOperation(), expressionCondition.getAttributeValue());
        }
    }

    private void buildClaimConditionWithOROperator(SqlBuilder sqlBuilder, String attributeName, String operation,
            String attributeValue) {

        sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_NAME = ?", attributeName);

        if (ExpressionOperation.EQ.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_VALUE = ?", attributeValue);
        } else if (ExpressionOperation.EW.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_VALUE LIKE ?", "%" + attributeValue);
        } else if (ExpressionOperation.CO.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_VALUE LIKE ?", "%" + attributeValue + "%");
        } else if (ExpressionOperation.SW.toString().equals(operation)) {
            sqlBuilder.updateSqlWithOROperation("UA.UM_ATTR_VALUE LIKE ?", attributeValue + "%");
        }
    }

    private void addingWheres(SqlBuilder baseSqlBuilder, SqlBuilder newSqlBuilder) {

        for (int i = 0; i < baseSqlBuilder.getWheres().size(); i++) {

            if (baseSqlBuilder.getIntegerParameters().containsKey(i + 1)) {
                newSqlBuilder
                        .where(baseSqlBuilder.getWheres().get(i), baseSqlBuilder.getIntegerParameters().get(i + 1));

            } else if (baseSqlBuilder.getStringParameters().containsKey(i + 1)) {
                newSqlBuilder.where(baseSqlBuilder.getWheres().get(i), baseSqlBuilder.getStringParameters().get(i + 1));

            } else if (baseSqlBuilder.getIntegerParameters().containsKey(i + 1)) {
                newSqlBuilder.where(baseSqlBuilder.getWheres().get(i), baseSqlBuilder.getLongParameters().get(i + 1));
            }
        }
    }

    private void getExpressionConditions(Condition condition, List<ExpressionCondition> expressionConditions) {

        if (condition instanceof ExpressionCondition) {
            expressionConditions.add((ExpressionCondition) condition);
        } else if (condition instanceof OperationalCondition) {
            Condition leftCondition = ((OperationalCondition) condition).getLeftCondition();
            getExpressionConditions(leftCondition, expressionConditions);
            Condition rightCondition = ((OperationalCondition) condition).getRightCondition();
            getExpressionConditions(rightCondition, expressionConditions);
        }
    }

    @Override
    public int getUserId(String username) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public int getTenantId(String username) throws UserStoreException {

        throw new UserStoreException("Operation is not supported.");
    }

    @Override
    public boolean isUniqueUserIdEnabled() {

        return true;
    }
}
