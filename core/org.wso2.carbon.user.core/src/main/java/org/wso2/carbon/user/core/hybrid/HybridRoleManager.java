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
package org.wso2.carbon.user.core.hybrid;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.authorization.AuthorizationCache;
import org.wso2.carbon.user.core.common.UserRolesCache;
import org.wso2.carbon.user.core.constants.UserCoreDBConstants;
import org.wso2.carbon.user.core.constants.UserCoreErrorConstants;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.jdbc.caseinsensitive.JDBCCaseInsensitiveConstants;
import org.wso2.carbon.user.core.util.DatabaseUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.dbcreator.DatabaseCreator;
import org.wso2.carbon.utils.xml.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static org.wso2.carbon.user.core.constants.UserCoreErrorConstants.ErrorMessages.ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE;
import static org.wso2.carbon.user.core.hybrid.HybridJDBCConstants.COUNT_INTERNAL_ONLY_ROLES_SQL;
import static org.wso2.carbon.user.core.hybrid.HybridJDBCConstants.COUNT_INTERNAL_ROLES_SQL;

public class HybridRoleManager {

    private static Log log = LogFactory.getLog(JDBCUserStoreManager.class);
    private final int DEFAULT_MAX_ROLE_LIST_SIZE = 1000;
    private final int DEFAULT_MAX_SEARCH_TIME = 1000;
    protected UserRealm userRealm = null;
    protected UserRolesCache userRolesCache = null;
    int tenantId;
    private DataSource dataSource;
    private RealmConfiguration realmConfig;
    private String isCascadeDeleteEnabled;
    private boolean userRolesCacheEnabled = true;
    private static final String APPLICATION_DOMAIN = "Application";
    private static final String WORKFLOW_DOMAIN = "Workflow";

    private static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";

    private static final String DB2 = "db2";

    public HybridRoleManager(DataSource dataSource, int tenantId, RealmConfiguration realmConfig,
                             UserRealm realm) throws UserStoreException {
        super();
        this.dataSource = dataSource;
        this.tenantId = tenantId;
        this.realmConfig = realmConfig;
        this.isCascadeDeleteEnabled = realmConfig.getRealmProperty(UserCoreDBConstants.CASCADE_DELETE_ENABLED);
        this.userRealm = realm;
        //persist internal domain
        UserCoreUtil.persistDomain(UserCoreConstants.INTERNAL_DOMAIN, tenantId, dataSource);
        UserCoreUtil.persistDomain(APPLICATION_DOMAIN, tenantId, dataSource);
        UserCoreUtil.persistDomain(WORKFLOW_DOMAIN, tenantId, dataSource);

    }

    /**
     * @param roleName Domain-less role
     * @param userList Domain-aware user list
     * @throws UserStoreException
     */
    public void addHybridRole(String roleName, String[] userList) throws UserStoreException {
        Connection dbConnection = null;
        try {

            // ########### Domain-less Roles and Domain-aware Users from here onwards #############

            // This method is always invoked by the primary user store manager.
            String primaryDomainName = getMyDomainName();

            if (primaryDomainName != null) {
                primaryDomainName = primaryDomainName.toUpperCase();
            }

            dbConnection = DatabaseUtil.getDBConnection(dataSource);

            if (!this.isExistingRole(roleName)) {
                DatabaseUtil.updateDatabase(dbConnection, HybridJDBCConstants.ADD_ROLE_SQL,
                        roleName, tenantId);
                dbConnection.commit();
            } else {
                throwRoleAlreadyExistsError(roleName);
            }
            if (userList != null) {
                String sql = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL;
                String type = DatabaseCreator.getDatabaseType(dbConnection);
                if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
                    sql = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL_MSSQL;
                }
                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    sql = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL_OPENEDGE;
                    DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection,
                            sql, primaryDomainName, userList, tenantId, roleName, tenantId);
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection,
                            sql, primaryDomainName, userList, roleName, tenantId, tenantId, tenantId);
                }
            }
            dbConnection.commit();
        } catch (UserStoreException e) {
            String errorMessage = "Error occurred while adding hybrid role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            // handle duplicate entry.
            if (ERROR_CODE_DUPLICATE_WHILE_WRITING_TO_DATABASE.getCode().equals(e.getErrorCode())) {
                throwRoleAlreadyExistsError(roleName);
            }

            // Propagate any other.
            throw e;
        } catch (SQLException e) {
            String errorMessage = "Error occurred while adding hybrid role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            // Other SQL Exception
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

    /**
     * @param tenantID
     */
    protected void clearUserRolesCacheByTenant(int tenantID) {
        if (userRolesCache != null) {
            userRolesCache.clearCacheByTenant(tenantID);
            AuthorizationCache authorizationCache = AuthorizationCache.getInstance();
            authorizationCache.clearCacheByTenant(tenantID);
        }
    }

    /**
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isExistingRole(String roleName) throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isExisting = false;

        try {

            // ########### Domain-less Roles and Domain-aware Users from here onwards #############

            dbConnection = DatabaseUtil.getDBConnection(dataSource);
            prepStmt = dbConnection.prepareStatement(HybridJDBCConstants.GET_ROLE_ID);
            prepStmt.setString(1, roleName);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                int value = rs.getInt(1);
                if (value > -1) {
                    isExisting = true;
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Is roleName: " + roleName + " Exist: " + isExisting + " TenantId: " + tenantId);
            }
            return isExisting;
        } catch (SQLException e) {
            String errorMessage = "Error occurred while checking is existing role for role name : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    /**
     * @param filter
     * @return
     * @throws UserStoreException
     */
    public String[] getHybridRoles(String filter) throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;

        String sqlStmt = HybridJDBCConstants.GET_ROLES;
        int maxItemLimit = UserCoreConstants.MAX_USER_ROLE_LIST;
        int searchTime = UserCoreConstants.MAX_SEARCH_TIME;

        try {
            maxItemLimit = Integer.parseInt(realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_ROLE_LIST));
        } catch (Exception e) {
            maxItemLimit = DEFAULT_MAX_ROLE_LIST_SIZE;
        }

        try {
            searchTime = Integer.parseInt(realmConfig
                    .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_MAX_SEARCH_TIME));
        } catch (Exception e) {
            searchTime = DEFAULT_MAX_SEARCH_TIME;
        }

        // Convert 'Application' domain from uppercase to PascalCase to accurately perform the DB search.
        if (filter.toLowerCase().startsWith(UserCoreConstants.APPLICATION_DOMAIN.toLowerCase())) {
            int index;
            if ((index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR)) >= 0) {
                filter = UserCoreConstants.APPLICATION_DOMAIN + filter.substring(index);
            }
        }
        try {
            if (filter != null && filter.trim().length() != 0) {
                filter = filter.trim();
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
            } else {
                filter = "%";
            }

            dbConnection = DatabaseUtil.getDBConnection(dataSource);

            if (dbConnection == null) {
                throw new UserStoreException("null connection");
            }

            dbConnection.setAutoCommit(false);
            if (dbConnection.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
                dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            }

            String dbType = DatabaseCreator.getDatabaseType(dbConnection);

            if (filter.startsWith(UserCoreConstants.INTERNAL_DOMAIN)) {
                if (DB2.equalsIgnoreCase(dbType)) {
                    sqlStmt = HybridJDBCConstants.GET_INTERNAL_ROLES_DB2;
                } else {
                    sqlStmt = HybridJDBCConstants.GET_INTERNAL_ROLES;
                }
            }

            prepStmt = dbConnection.prepareStatement(sqlStmt);

            byte increment = 0;
            if (filter.startsWith(UserCoreConstants.INTERNAL_DOMAIN)) {
                prepStmt.setString(++increment, UserCoreUtil.removeDomainFromName(filter));
            } else {
                prepStmt.setString(++increment, filter);
            }

            if (filter.startsWith(UserCoreConstants.INTERNAL_DOMAIN)) {
                prepStmt.setString(++increment, "%/%");
            }

            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                prepStmt.setInt(++increment, tenantId);
            }
            prepStmt.setMaxRows(maxItemLimit);
            try {
                prepStmt.setQueryTimeout(searchTime);
            } catch (Exception e) {
                // this can be ignored since timeout method is not implemented
                log.debug(e);
            }
            List<String> filteredRoles = new ArrayList<String>();

            try {
                rs = prepStmt.executeQuery();
            } catch (SQLException e) {
                log.error("Error while retrieving roles from Internal JDBC role store", e);
                // May be due time out, therefore ignore this exception
            }

            if (rs != null) {
                while (rs.next()) {
                    String name = rs.getString(1);
                    // Append the domain
                    if (!name.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                        name = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR
                               + name;
                    }
                    filteredRoles.add(name);
                }
            }
            return filteredRoles.toArray(new String[filteredRoles.size()]);
        } catch (SQLException e) {
            String errorMessage = "Error occurred while getting hybrid roles from filter : " + filter;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } catch (Exception e) {
            String msg = "Error occur while getting database type";
            if (log.isDebugEnabled()) {
                log.debug(msg, e);
            }
            throw new UserStoreException(msg, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }
    }

    /**
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public String[] getUserListOfHybridRole(String roleName) throws UserStoreException {

        if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
            return userRealm.getUserStoreManager().listUsers("*", -1);
        }

        // ########### Domain-less Roles and Domain-aware Users from here onwards #############

        String sqlStmt = HybridJDBCConstants.GET_USER_LIST_OF_ROLE_SQL;
        Connection dbConnection = null;
        try {
            dbConnection = DatabaseUtil.getDBConnection(dataSource);
            String[] names = DatabaseUtil.getStringValuesFromDatabaseForInternalRoles(dbConnection, sqlStmt,
                    roleName, tenantId, tenantId);
            return names;
        } catch (SQLException e) {
            String errorMessage = "Error occurred while getting user list from hybrid role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    /**
     * @param roleName
     * @param deletedUsers
     * @param newUsers
     * @throws UserStoreException
     */
    public void updateUserListOfHybridRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {

        String sqlStmt1 = HybridJDBCConstants.REMOVE_USER_FROM_ROLE_SQL;
        String sqlStmt2 = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL;
        if (!isCaseSensitiveUsername()) {
            sqlStmt1 = HybridJDBCConstants.REMOVE_USER_FROM_ROLE_SQL_CASE_INSENSITIVE;
        }
        Connection dbConnection = null;

        try {

            // ########### Domain-less Roles and Domain-aware Users from here onwards #############
            String primaryDomainName = getMyDomainName();

            if (primaryDomainName != null) {
                primaryDomainName = primaryDomainName.toUpperCase();
            }

            dbConnection = DatabaseUtil.getDBConnection(dataSource);
            String type = DatabaseCreator.getDatabaseType(dbConnection);

            if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
                sqlStmt2 = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL_MSSQL;
            }

            if (deletedUsers != null && deletedUsers.length > 0) {
                DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(
                        dbConnection, sqlStmt1, primaryDomainName, deletedUsers,
                        roleName, tenantId, tenantId, tenantId);
                // authz cache of deleted users from role, needs to be updated
                for (String deletedUser : deletedUsers) {
                    userRealm.getAuthorizationManager().clearUserAuthorization(deletedUser);
                }
            }

            if (newUsers != null && newUsers.length > 0) {
                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    sqlStmt2 = HybridJDBCConstants.ADD_USER_TO_ROLE_SQL_OPENEDGE;
                    DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection,
                            sqlStmt2, primaryDomainName, newUsers, tenantId, roleName, tenantId);
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection,
                            sqlStmt2, primaryDomainName, newUsers, roleName, tenantId, tenantId, tenantId);
                }
            }

            dbConnection.commit();
        } catch (SQLException | UserStoreException e) {
            String errorMessage = "Error occurred while updating user list of hybrid role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
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
     * Update group list of role.
     *
     * @param roleName      Role name.
     * @param deletedGroups Deleted groups.
     * @param newGroups     New groups.
     * @throws UserStoreException UserStoreException.
     */
    public void updateGroupListOfHybridRole(String roleName, String[] deletedGroups, String[] newGroups)
            throws UserStoreException {

        String sqlStmt1 = HybridJDBCConstants.REMOVE_GROUP_FROM_ROLE_SQL;
        String sqlStmt2 = HybridJDBCConstants.ADD_GROUP_TO_ROLE_SQL;

        try (Connection dbConnection = DatabaseUtil.getDBConnection(dataSource)) {

            String domainName = getMyDomainName();
            if (domainName != null) {
                domainName = domainName.toUpperCase();
            }

            String type = DatabaseCreator.getDatabaseType(dbConnection);
            if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
                sqlStmt2 = HybridJDBCConstants.ADD_GROUP_TO_ROLE_SQL_MSSQL;
            }

            if (ArrayUtils.isNotEmpty(deletedGroups)) {
                DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection, sqlStmt1, domainName,
                        deletedGroups, roleName, tenantId, tenantId, tenantId);
            }

            if (ArrayUtils.isNotEmpty(newGroups)) {
                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    sqlStmt2 = HybridJDBCConstants.ADD_GROUP_TO_ROLE_SQL_OPENEDGE;
                    DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection, sqlStmt2, domainName,
                            newGroups, tenantId, roleName, tenantId);
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchModeForInternalRoles(dbConnection, sqlStmt2, domainName,
                            newGroups, roleName, tenantId, tenantId, tenantId);
                }
            }
            dbConnection.commit();
        } catch (SQLException | UserStoreException e) {
            String errorMessage = "Error occurred while updating user list of hybrid role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Error occurred while getting database type from DB connection";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
    }

    /**
     * Get group list of the given hybrid role.
     *
     * @param roleName Role name.
     * @return List og groups.
     * @throws UserStoreException UserStoreException.
     */
    public String[] getGroupListOfHybridRole(String roleName) throws UserStoreException {

        String sqlStmt = HybridJDBCConstants.GET_GROUP_LIST_OF_ROLE_SQL;
        try (Connection dbConnection = DatabaseUtil.getDBConnection(dataSource)) {
            return DatabaseUtil
                    .getStringValuesFromDatabaseForInternalRoles(dbConnection, sqlStmt, roleName, tenantId, tenantId);
        } catch (SQLException e) {
            String errorMessage = "Error occurred while getting user list from hybrid role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
    }

    /**
     * @param userName
     * @return
     * @throws UserStoreException
     */
    public String[] getHybridRoleListOfUser(String userName, String filter) throws UserStoreException {

        String sqlStmt;
        Connection dbConnection = null;
        String[] roles;
        userName = UserCoreUtil.addDomainToName(userName, getMyDomainName());
        String domain = UserCoreUtil.extractDomainFromName(userName);
        // ########### Domain-less Roles and Domain-aware Users from here onwards #############
        try {
            dbConnection = DatabaseUtil.getDBConnection(dataSource);

            if (domain != null) {
                domain = domain.toUpperCase();
            }
            if (filter.equals("*") || StringUtils.isEmpty(filter)) {
                sqlStmt = getHybridRoleListSqlStatement(
                        realmConfig.getRealmProperty(HybridJDBCConstants.GET_ROLE_LIST_OF_USER),
                        HybridJDBCConstants.GET_ROLE_LIST_OF_USER_SQL,
                        JDBCCaseInsensitiveConstants.GET_ROLE_LIST_OF_USER_SQL_CASE_INSENSITIVE);
                roles = DatabaseUtil
                        .getStringValuesFromDatabase(dbConnection, sqlStmt, UserCoreUtil.removeDomainFromName(userName),
                                tenantId, tenantId, tenantId, domain);
            } else if (filter.contains("*") || filter.contains("?")) {
                filter = filter.trim();
                filter = filter.replace("*", "%");
                filter = filter.replace("?", "_");
                sqlStmt = getHybridRoleListSqlStatement(
                        realmConfig.getRealmProperty(HybridJDBCConstants.GET_IS_ROLE_EXIST_LIST_OF_USER),
                        HybridJDBCConstants.GET_ROLE_OF_USER_SQL,
                        JDBCCaseInsensitiveConstants.GET_IS_USER_ROLE_SQL_CASE_INSENSITIVE);

                // If the filter contains the internal domain, then here we remove the internal domain from the filter
                // as the database only has the role name without the internal domain.
                filter = truncateInternalDomainFromFilter(filter);
                roles = DatabaseUtil
                        .getStringValuesFromDatabase(dbConnection, sqlStmt, UserCoreUtil.removeDomainFromName(userName),
                                tenantId, tenantId, tenantId, domain, filter);
            } else {
                sqlStmt = getHybridRoleListSqlStatement(
                        realmConfig.getRealmProperty(HybridJDBCConstants.GET_IS_ROLE_EXIST_LIST_OF_USER),
                        HybridJDBCConstants.GET_USER_ROLE_NAME_SQL,
                        JDBCCaseInsensitiveConstants.GET_IS_USER_ROLE_SQL_CASE_INSENSITIVE);

                filter = truncateInternalDomainFromFilter(filter);
                roles = DatabaseUtil
                        .getStringValuesFromDatabase(dbConnection, sqlStmt, UserCoreUtil.removeDomainFromName(userName),
                                tenantId, tenantId, tenantId, domain, filter);
            }

            if (!CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
                // Adding everyone role
                if (roles == null || roles.length == 0) {
                    return new String[]{realmConfig.getEveryOneRoleName()};
                }
                List<String> allRoles = new ArrayList<String>();
                boolean isEveryone = false;
                for (String role : roles) {
                    if (!role.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                        role = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR + role;
                    }
                    if (role.equals(realmConfig.getEveryOneRoleName())) {
                        isEveryone = true;
                    }
                    allRoles.add(role);
                }
                if (!isEveryone) {
                    allRoles.add(realmConfig.getEveryOneRoleName());
                }
                return allRoles.toArray(new String[allRoles.size()]);
            } else {
                return roles;
            }
        } catch (SQLException e) {
            String errorMessage = "Error occurred while getting hybrid role list of user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    /**
     * If the filter contains the internal domain, then here we remove the internal domain from the filter
     * as the database only has the role name without the internal domain.
     *
     * @param filter raw filter
     * @return truncated filter without the internal domain
     */
    private String truncateInternalDomainFromFilter(String filter) {

        String filterLowerCased = filter.toLowerCase();

        if (filterLowerCased.contains(UserCoreConstants.INTERNAL_DOMAIN_LOWER_CASED)
                && filterLowerCased.indexOf(UserCoreConstants.INTERNAL_DOMAIN_LOWER_CASED) == 0) {
            int index;
            if ((index = filter.indexOf(CarbonConstants.DOMAIN_SEPARATOR)) >= 0) {
                filter = filter.substring(index + 1);
            }
        }
        return filter;
    }

    /**
     * Get hybrid role list of users
     *
     * @param userNames user name list
     * @return map of hybrid role list of users
     * @throws UserStoreException userStoreException
     */
    public Map<String, List<String>> getHybridRoleListOfUsers(List<String> userNames, String domainName) throws
            UserStoreException {

        if (CollectionUtils.isEmpty(userNames)) {
            return new HashMap<>();
        }
        Map<String, List<String>> hybridRoleListOfUsers = new HashMap<>();
        String sqlStmt = realmConfig.getRealmProperty(HybridJDBCConstants.GET_ROLE_LIST_OF_USERS);
        StringBuilder usernameParameter = new StringBuilder();
        if (isCaseSensitiveUsername()) {
            if (StringUtils.isEmpty(sqlStmt)) {
                sqlStmt = HybridJDBCConstants.GET_INTERNAL_ROLE_LIST_OF_USERS_SQL;
            }
            for (int i = 0; i < userNames.size(); i++) {

                userNames.set(i, userNames.get(i).replaceAll("'", "''"));
                usernameParameter.append("'").append(userNames.get(i)).append("'");

                if (i != userNames.size() - 1) {
                    usernameParameter.append(",");
                }
            }
        } else {
            if (sqlStmt == null) {
                sqlStmt = JDBCCaseInsensitiveConstants.GET_INTERNAL_ROLE_LIST_OF_USERS_SQL_CASE_INSENSITIVE;
            }
            for (int i = 0; i < userNames.size(); i++) {

                userNames.set(i, userNames.get(i).replaceAll("'", "''"));
                usernameParameter.append("LOWER('").append(userNames.get(i)).append("')");

                if (i != userNames.size() - 1) {
                    usernameParameter.append(",");
                }
            }
        }

        sqlStmt = sqlStmt.replaceFirst("\\?", Matcher.quoteReplacement(usernameParameter.toString()));
        try (Connection connection = DatabaseUtil.getDBConnection(dataSource);
                PreparedStatement prepStmt = connection.prepareStatement(sqlStmt)) {
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, tenantId);
            prepStmt.setString(4, domainName);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    String userName = resultSet.getString(1);
                    if (!userNames.contains(userName)) {
                        continue;
                    }

                    String roleName = resultSet.getString(2);
                    List<String> userRoles = hybridRoleListOfUsers.get(userName);
                    if (userRoles == null) {
                        userRoles = new ArrayList<>();
                        hybridRoleListOfUsers.put(userName, userRoles);
                    }

                    if (!roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                        roleName = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR + roleName;
                    }
                    userRoles.add(roleName);
                }
            }

            for (String userName : userNames) {
                List<String> hybridRoles = hybridRoleListOfUsers.get(userName);
                if (hybridRoles == null) {
                    hybridRoles = new ArrayList<>();
                    hybridRoleListOfUsers.put(userName, hybridRoles);
                }
                if (!hybridRoles.contains(realmConfig.getEveryOneRoleName())
                        && !CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
                    hybridRoles.add(realmConfig.getEveryOneRoleName());
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while getting hybrid role list of users : " + Arrays.toString(userNames.toArray())
                            + " in domain: " + domainName;
            throw new UserStoreException(errorMessage, e);
        }

        return hybridRoleListOfUsers;
    }

    /**
     * Get hybrid role list of groups.
     *
     * @param groupNames group name list.
     * @return map of hybrid role list of groups.
     * @throws UserStoreException userStoreException.
     */
    public Map<String, List<String>> getHybridRoleListOfGroups(List<String> groupNames, String domainName)
            throws UserStoreException {

        if (CollectionUtils.isEmpty(groupNames)) {
            return new HashMap<>();
        }
        Map<String, List<String>> hybridRoleListOfGroups = new HashMap<>();
        String sqlStmt = realmConfig.getRealmProperty(HybridJDBCConstants.GET_ROLE_LIST_OF_GROUPS);
        StringBuilder groupNameParameter = new StringBuilder();

        if (StringUtils.isEmpty(sqlStmt)) {
            sqlStmt = HybridJDBCConstants.GET_INTERNAL_ROLE_LIST_OF_GROUPS_SQL;
        }
        for (int i = 0; i < groupNames.size(); i++) {
            groupNames.set(i, groupNames.get(i).replaceAll("'", "''"));
            groupNameParameter.append("'").append(groupNames.get(i)).append("'");

            if (i != groupNames.size() - 1) {
                groupNameParameter.append(",");
            }
        }

        sqlStmt = sqlStmt.replaceFirst("\\?", Matcher.quoteReplacement(groupNameParameter.toString()));
        try (Connection connection = DatabaseUtil.getDBConnection(dataSource);
                PreparedStatement prepStmt = connection.prepareStatement(sqlStmt)) {
            prepStmt.setInt(1, tenantId);
            prepStmt.setInt(2, tenantId);
            prepStmt.setInt(3, tenantId);
            prepStmt.setString(4, domainName);
            try (ResultSet resultSet = prepStmt.executeQuery()) {
                while (resultSet.next()) {
                    String groupName = resultSet.getString(1);
                    if (!groupNames.contains(groupName)) {
                        continue;
                    }

                    String roleName = resultSet.getString(2);
                    List<String> groupRoles = hybridRoleListOfGroups.computeIfAbsent(groupName, k -> new ArrayList<>());

                    if (!roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                        roleName = UserCoreConstants.INTERNAL_DOMAIN + CarbonConstants.DOMAIN_SEPARATOR + roleName;
                    }
                    groupRoles.add(roleName);
                }
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while getting hybrid role list of groups : " + Arrays.toString(groupNames.toArray())
                            + " in domain: " + domainName;
            throw new UserStoreException(errorMessage, e);
        }

        return hybridRoleListOfGroups;
    }

    /**
     * @param user
     * @param deletedRoles
     * @param addRoles
     * @throws UserStoreException
     */
    public void updateHybridRoleListOfUser(String user, String[] deletedRoles, String[] addRoles)
            throws UserStoreException {

        String sqlStmt1 = HybridJDBCConstants.REMOVE_ROLE_FROM_USER_SQL;
        String sqlStmt2 = HybridJDBCConstants.ADD_ROLE_TO_USER_SQL;
        if(!isCaseSensitiveUsername()){
            sqlStmt1 = HybridJDBCConstants.REMOVE_ROLE_FROM_USER_SQL_CASE_INSENSITIVE;
        }
        Connection dbConnection = null;

        try {

            user = UserCoreUtil.addDomainToName(user, getMyDomainName());
            String domain = UserCoreUtil.extractDomainFromName(user);
            // ########### Domain-less Roles and Domain-aware Users from here onwards #############

            dbConnection = DatabaseUtil.getDBConnection(dataSource);
            String type = DatabaseCreator.getDatabaseType(dbConnection);
            if (UserCoreConstants.MSSQL_TYPE.equals(type)) {
                sqlStmt2 = HybridJDBCConstants.ADD_ROLE_TO_USER_SQL_MSSQL;
            }

            if (domain != null) {
                domain = domain.toUpperCase();
            }

            if (deletedRoles != null && deletedRoles.length > 0) {
                DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt1, deletedRoles,
                        tenantId, UserCoreUtil.removeDomainFromName(user), tenantId, tenantId, domain);
            }
            if (addRoles != null && addRoles.length > 0) {
                ArrayList<String> newRoleList = new ArrayList<>();
                for (String role : addRoles) {
                    if(!isExistingRole(role)){
                        String errorMessage = "The role: " + role + " does not exist.";
                        throw new UserStoreException(errorMessage);
                    }
                    if (!isUserInRole(user, role)) {
                        newRoleList.add(role);
                    }
                }

                String[] rolesToAdd = newRoleList.toArray(new String[newRoleList.size()]);

                if (UserCoreConstants.OPENEDGE_TYPE.equals(type)) {
                    sqlStmt2 = HybridJDBCConstants.ADD_ROLE_TO_USER_SQL_OPENEDGE;
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, user,
                            tenantId, rolesToAdd, tenantId);
                } else {
                    DatabaseUtil.udpateUserRoleMappingInBatchMode(dbConnection, sqlStmt2, rolesToAdd,
                            tenantId, UserCoreUtil.removeDomainFromName(user), tenantId, tenantId, domain);
                }
            }
            dbConnection.commit();
        } catch (SQLException | UserStoreException e) {
            String errorMessage = "Error occurred while updating hybrid role list of user : " + user;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Error occurred while getting database type from DB connection";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
        // Authorization cache of user should also be updated if deleted roles are involved
        if (deletedRoles != null && deletedRoles.length > 0) {
            userRealm.getAuthorizationManager().clearUserAuthorization(user);
        }
    }

    /**
     * @param roleName
     * @throws UserStoreException
     */
    public void deleteHybridRole(String roleName) throws UserStoreException {

        // ########### Domain-less Roles and Domain-aware Users from here onwards #############

        if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
            throw new UserStoreException("Invalid operation");
        }

        Connection dbConnection = null;
        try {
            dbConnection = DatabaseUtil.getDBConnection(dataSource);
            if(isCascadeDeleteEnabled == null || !Boolean.parseBoolean(isCascadeDeleteEnabled)) {
                DatabaseUtil.updateDatabase(dbConnection,
                        HybridJDBCConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL, roleName, tenantId, tenantId);
            }
            DatabaseUtil.updateDatabase(dbConnection, HybridJDBCConstants.DELETE_ROLE_SQL,
                    roleName, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Error occurred while deleting hybrid role : " + roleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }

        // UM_ROLE_PERMISSION Table, roles are associated with Domain ID.
        // At this moment Role name doesn't contain the Domain prefix.
        // clearRoleAuthorization() expects domain qualified name.
        // Hence we add the "Internal" Domain name explicitly here.
        if (!roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
            roleName = UserCoreUtil.addDomainToName(roleName, UserCoreConstants.INTERNAL_DOMAIN);
        }
        // also need to clear role authorization
        userRealm.getAuthorizationManager().clearRoleAuthorization(roleName);
    }

    /**
     * @param roleName
     * @param newRoleName
     * @throws UserStoreException
     */
    public void updateHybridRoleName(String roleName, String newRoleName) throws UserStoreException {

        // ########### Domain-less Roles and Domain-aware Users from here onwards #############

        if (!org.apache.commons.lang.StringUtils.equalsIgnoreCase(roleName, newRoleName)
                && this.isExistingRole(newRoleName)) {
            throw new UserStoreException("Role name: " + newRoleName
                    + " in the system. Please pick another role name.");
        }

        String sqlStmt = HybridJDBCConstants.UPDATE_ROLE_NAME_SQL;
        if (sqlStmt == null) {
            throw new UserStoreException("The sql statement for update hybrid role name is null");
        }

        Connection dbConnection = null;
        try {

            dbConnection = DatabaseUtil.getDBConnection(dataSource);
            if (sqlStmt.contains(UserCoreConstants.UM_TENANT_COLUMN)) {
                DatabaseUtil.updateDatabase(dbConnection, sqlStmt, newRoleName, roleName, tenantId);
            } else {
                DatabaseUtil.updateDatabase(dbConnection, sqlStmt, newRoleName, roleName);
            }
            dbConnection.commit();
            if (roleName.contains(UserCoreConstants.DOMAIN_SEPARATOR)) {
                this.userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(roleName,
                        newRoleName);
            } else {
                String domainNamePrefix = UserCoreConstants.INTERNAL_DOMAIN + UserCoreConstants.DOMAIN_SEPARATOR;
                this.userRealm.getAuthorizationManager().resetPermissionOnUpdateRole(domainNamePrefix
                        + roleName, domainNamePrefix + newRoleName);
            }
        } catch (SQLException e) {
            String errorMessage =
                    "Error occurred while updating hybrid role : " + roleName + " to new role : " + newRoleName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection);
        }
    }

    /**
     * Get hybrid role count for the given filter.
     *
     * @param filter The domain qualified filter. If the domain is 'Internal', all the 'Application' roles are skipped.
     * @throws UserStoreException If an error occur while getting the hybrid role count using the filter.
     */
    public Long countHybridRoles(String filter) throws UserStoreException {

        Connection dbConnection = null;
        String sqlStmt = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet = null;

        try {
            dbConnection = DatabaseUtil.getDBConnection(dataSource);
            if (filter.startsWith(UserCoreConstants.INTERNAL_DOMAIN)) {
                sqlStmt = COUNT_INTERNAL_ONLY_ROLES_SQL;
                filter = filter.replace(UserCoreConstants.INTERNAL_DOMAIN, "");
            } else {
                sqlStmt = COUNT_INTERNAL_ROLES_SQL;
            }
            prepStmt = dbConnection.prepareStatement(sqlStmt);
            prepStmt.setString(1, filter);
            prepStmt.setInt(2, tenantId);
            prepStmt.setQueryTimeout(UserCoreConstants.MAX_SEARCH_TIME);

            resultSet = prepStmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("RESULT");
            } else {
                log.error("No role count is retrieved for Internal domain filter: " + filter);
                return (long) -1;
            }
        } catch (SQLException e) {
            if (log.isDebugEnabled()) {
                log.debug("An error occurred while getting hybrid roles count Using sql : " + sqlStmt + ", with " +
                        "the filter: " + filter);
            }
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, resultSet, prepStmt);
        }
    }

    /**
     * ##### This method is not used anywhere
     *
     * @param userName
     * @param roleName
     * @return
     * @throws UserStoreException
     */
    public boolean isUserInRole(String userName, String roleName) throws UserStoreException {
        // TODO
        String[] roles = getHybridRoleListOfUser(userName, "*");
        if (roles != null && roleName != null) {
            for (String role : roles) {
                if (roleName.contains(CarbonConstants.DOMAIN_SEPARATOR)) {
                    if (role.equalsIgnoreCase(roleName)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Role: " + roleName + " is already assigned to the user: " + userName);
                        }
                        return true;
                    }
                } else {
                    if (UserCoreUtil.removeDomainFromName(role).equalsIgnoreCase(roleName)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Role: " + roleName + " is already assigned to the user: " + userName);
                        }
                        return true;
                    }
                }
            }
        }

        return false;
//		if (UserCoreUtil.isEveryoneRole(roleName, realmConfig)) {
//			return true;
//		}
//
//		userName = UserCoreUtil.addDomainToName(userName, getMyDomainName());
//        String domain = UserCoreUtil.extractDomainFromName(userName);
//		// ########### Domain-less Roles and Domain-aware Users from here onwards #############
//
//		Connection dbConnection = null;
//		PreparedStatement prepStmt = null;
//		ResultSet rs = null;
//		boolean isUserInRole = false;
//		try {
//			dbConnection = DatabaseUtil.getDBConnection(dataSource);
//			prepStmt = dbConnection.prepareStatement(HybridJDBCConstants.IS_USER_IN_ROLE_SQL);
//			prepStmt.setString(1, userName);
//			prepStmt.setString(2, roleName);
//			prepStmt.setInt(3, tenantId);
//            prepStmt.setInt(4, tenantId);
//            prepStmt.setInt(5, tenantId);
//			prepStmt.setString(6, domain);
//			rs = prepStmt.executeQuery();
//			if (rs.next()) {
//				int value = rs.getInt(1);
//				if (value != -1) {
//					isUserInRole = true;
//				}
//			}
//			dbConnection.commit();
//		} catch (SQLException e) {
//			throw new UserStoreException(e.getMessage(), e);
//		} finally {
//			DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
//		}
//		return isUserInRole;
    }

    /**
     * If a user is added to a hybrid role, that entry should be deleted upon deletion of the user.
     *
     * @param userName
     * @throws UserStoreException
     */
    public void deleteUser(String userName) throws UserStoreException {

        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;

        userName = UserCoreUtil.addDomainToName(userName, getMyDomainName());
        String domain = UserCoreUtil.extractDomainFromName(userName);
        // ########### Domain-less Roles and Domain-aware Users from here onwards #############

        if (domain != null) {
            domain = domain.toUpperCase();
        }

        String sqlStmt = HybridJDBCConstants.REMOVE_USER_SQL;
        if (!isCaseSensitiveUsername()) {
            sqlStmt = HybridJDBCConstants.REMOVE_USER_SQL_CASE_INSENSITIVE;
        }

        try {
            dbConnection = DatabaseUtil.getDBConnection(dataSource);
            preparedStatement = dbConnection.prepareStatement(sqlStmt);
            preparedStatement.setString(1, UserCoreUtil.removeDomainFromName(userName));
            preparedStatement.setInt(2, tenantId);
            preparedStatement.setInt(3, tenantId);
            preparedStatement.setString(4, domain);
            preparedStatement.execute();
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Error occurred while deleting user : " + userName;
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, preparedStatement);
        }
    }

    /**
     *
     */
    protected void initUserRolesCache() {

        String userRolesCacheEnabledString = (realmConfig
                .getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_ROLES_CACHE_ENABLED));

        if (userRolesCacheEnabledString != null && !userRolesCacheEnabledString.equals("")) {
            userRolesCacheEnabled = Boolean.parseBoolean(userRolesCacheEnabledString);
            if (log.isDebugEnabled()) {
                log.debug("User Roles Cache is configured to:" + userRolesCacheEnabledString);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.info("User Roles Cache is not configured. Default value: "
                        + userRolesCacheEnabled + " is taken.");
            }
        }

        if (userRolesCacheEnabled) {
            int timeOut = UserCoreConstants.USER_ROLE_CACHE_DEFAULT_TIME_OUT;
            String timeOutString = realmConfig.
                    getUserStoreProperty(UserCoreConstants.RealmConfig.PROPERTY_USER_ROLE_CACHE_TIME_OUT);
            if (timeOutString != null) {
                timeOut = Integer.parseInt(timeOutString);
            }
            userRolesCache = UserRolesCache.getInstance();
            userRolesCache.setTimeOut(timeOut);
        }
    }

    /**
     * @return
     */
    protected String getMyDomainName() {
        return UserCoreUtil.getDomainName(realmConfig);
    }

    private boolean isCaseSensitiveUsername() throws UserStoreException{

        String isUsernameCaseInsensitiveString = realmConfig.getUserStoreProperty(CASE_INSENSITIVE_USERNAME);
        return !Boolean.parseBoolean(isUsernameCaseInsensitiveString);
    }

    /**
     * Get the SQL statement for HybridRole.
     *
     * @param getRoleListOfUserSQLConfig    query for getting role set from resource property.
     * @param caseSensitiveUsernameQuery    query for getting role with case sensitive username.
     * @param nonCaseSensitiveUsernameQuery query for getting role with non-case sensitive username.
     * @return sql statement.
     * @throws UserStoreException
     */
    private String getHybridRoleListSqlStatement(String getRoleListOfUserSQLConfig, String caseSensitiveUsernameQuery,
            String nonCaseSensitiveUsernameQuery) throws UserStoreException {

        String sqlStmt;
        if (isCaseSensitiveUsername()) {
            sqlStmt = caseSensitiveUsernameQuery;
        } else {
            sqlStmt = nonCaseSensitiveUsernameQuery;
        }
        if (!StringUtils.isEmpty(getRoleListOfUserSQLConfig)) {
            sqlStmt = getRoleListOfUserSQLConfig;
        }
        return sqlStmt;
    }

    private void throwRoleAlreadyExistsError(String roleName) throws UserStoreException{

        String errorCode = UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS.getCode();
        String errorMessage = String.format(UserCoreErrorConstants.ErrorMessages.ERROR_CODE_ROLE_ALREADY_EXISTS
                .getMessage(), roleName);
        throw new UserStoreException(errorCode + " - " + errorMessage, errorCode, null);
    }

    /**
     * Check whether the group exists in the UM_HYBRID_GROUP_ROLE table.
     *
     * @param groupName        The group name.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    public boolean isGroupAssignedToHybridRoles(String groupName) throws UserStoreException {

        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isGroupAssignedToHybridRoles = false;

        try (Connection dbConnection = DatabaseUtil.getDBConnection(dataSource)) {
            prepStmt = dbConnection.prepareStatement(HybridJDBCConstants.GET_GROUP_ROLE_MAPPING_ID);
            prepStmt.setString(1, groupName);
            prepStmt.setInt(2, tenantId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                int value = rs.getInt(1);
                if (value > -1) {
                    isGroupAssignedToHybridRoles = true;
                }
            }
            return isGroupAssignedToHybridRoles;
        } catch (SQLException e) {
            String errorMessage = "Error occurred while checking the group : " + groupName +
                    "has assigned hybrid roles.";
            if (log.isDebugEnabled()) {
                log.debug(errorMessage, e);
            }
            throw new UserStoreException(errorMessage, e);
        }
    }

    /**
     * Update group name in the UM_HYBRID_GROUP_ROLE table.
     *
     * @param groupName        The current group name.
     * @param newGroupName     The new group name.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    public void updateGroupName(String groupName, String newGroupName) throws UserStoreException {

        if (!this.isGroupAssignedToHybridRoles(groupName)) {
            return;
        }

        try (Connection dbConnection = DatabaseUtil.getDBConnection(dataSource)) {
            DatabaseUtil.updateDatabase(dbConnection, HybridJDBCConstants.UPDATE_GROUP_NAME_SQL,
                    newGroupName, groupName, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Error occurred while updating group name : " + groupName +
                    " to new group name: " + newGroupName + " in assigned hybrid roles.";
            throw new UserStoreException(errorMessage, e);
        }
    }

    /**
     * Delete group from the UM_HYBRID_GROUP_ROLE table.
     *
     * @param groupName        The group name.
     * @throws UserStoreException An unexpected exception has occurred.
     */
    public void removeGroupRoleMappingByGroupName(String groupName) throws UserStoreException {

        if (!this.isGroupAssignedToHybridRoles(groupName)) {
            return;
        }

        try (Connection dbConnection = DatabaseUtil.getDBConnection(dataSource)) {
            DatabaseUtil.updateDatabase(dbConnection, HybridJDBCConstants.DELETE_GROUP_SQL, groupName, tenantId);
            dbConnection.commit();
        } catch (SQLException e) {
            String errorMessage = "Error occurred while deleting the group : " + groupName;
            throw new UserStoreException(errorMessage, e);
        }
    }
}
