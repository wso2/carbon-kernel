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
package org.wso2.carbon.user.core.jdbc;

public final class JDBCRealmConstants {

    public static final String SELECT_USER = "SelectUserSQL";
    public static final String GET_ROLE_LIST = "GetRoleListSQL";
    public static final String GET_SHARED_ROLE_LIST = "GetSharedRoleListSQL";
    public static final String GET_USER_FILTER = "UserFilterSQL";
    public static final String GET_USER_ROLE = "UserRoleSQL";
    public static final String GET_SHARED_ROLES_FOR_USER = "UserSharedRoleSQL";
    public static final String GET_IS_ROLE_EXISTING = "IsRoleExistingSQL";
    public static final String GET_USERS_IN_ROLE = "GetUserListOfRoleSQL";
    public static final String GET_USERS_IN_SHARED_ROLE = "GetUserListOfSharedRoleSQL";
    public static final String GET_IS_USER_EXISTING = "IsUserExistingSQL";
    public static final String GET_PROPS_FOR_PROFILE = "GetUserPropertiesForProfileSQL";
    public static final String GET_PROP_FOR_PROFILE = "GetUserPropertyForProfileSQL";
    public static final String GET_USERS_FOR_PROP = "GetUserLisForPropertySQL";
    public static final String GET_PROFILE_NAMES = "GetProfileNamesSQL";
    public static final String GET_PROFILE_NAMES_FOR_USER = "GetUserProfileNamesSQL";
    public static final String GET_USERID_FROM_USERNAME = "GetUserIDFromUserNameSQL";
    public static final String GET_USERNAME_FROM_TENANT_ID = "GetUserNameFromTenantIDSQL";
    public static final String GET_TENANT_ID_FROM_USERNAME = "GetTenantIDFromUserNameSQL";
    public static final String ADD_SHARED_ROLE = "AddSharedRoleSQL";
    public static final String ADD_USER = "AddUserSQL";
    public static final String ADD_USER_TO_ROLE = "AddUserToRoleSQL";
    public static final String ADD_USER_PERMISSION = "AddUserPermissionSQL";
    public static final String ADD_ROLE = "AddRoleSQL";
    public static final String ADD_ROLE_TO_USER = "AddRoleToUserSQL";
    public static final String ADD_SHARED_ROLE_TO_USER = "AddSharedRoleToUserSQL";
    public static final String ADD_ROLE_PERMISSION = "AddRolePermissionSQL";
    public static final String REMOVE_USER_FROM_ROLE = "RemoveUserFromRoleSQL";
    public static final String REMOVE_USER_FROM_SHARED_ROLE = "RemoveUserFromSharedRoleSQL";
    public static final String REMOVE_ROLE_FROM_USER = "RemoveRoleFromUserSQL";
    public static final String DELETE_ROLE = "DeleteRoleSQL";
    public static final String ON_DELETE_ROLE_REMOVE_USER_ROLE = "OnDeleteRoleRemoveUserRoleMappingSQL";
    public static final String ON_DELETE_ROLE_DELETE_PERMISSION = "OnDeleteRoleRemovePermissionsSQL";
    public static final String DELETE_USER = "DeleteUserSQL";
    public static final String ON_DELETE_USER_REMOVE_USER_ROLE = "OnDeleteUserRemoveUserRoleMappingSQL";
    public static final String ON_DELETE_USER_REMOVE_ATTRIBUTE = "OnDeleteUserRemoveUserAttributeSQL";
    public static final String ON_DELETE_USER_DELETE_PERMISSION = "OnDeleteUserRemovePermissionsSQL";
    public static final String IS_DOMAIN_EXISTING = "IsDomainExistingSQL";
    public static final String ADD_DOMAIN = "AddDomainSQL";
    public static final String UPDATE_USER_PASSWORD = "UpdateUserPasswordSQL";
    public static final String UPDATE_ROLE_NAME = "UpdateRoleNameSQL";
    public static final String ADD_USER_PROPERTY = "AddUserPropertySQL";
    public static final String UPDATE_USER_PROPERTY = "UpdateUserPropertySQL";
    public static final String DELETE_USER_PROPERTY = "DeleteUserPropertySQL";
    public static final String USER_NAME_UNIQUE = "UserNameUniqueAcrossTenantsSQL";
    public static final String SELECT_USER_SQL = "SELECT * FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?";
    public static final String GET_ROLE_LIST_SQL = "SELECT UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE FROM UM_ROLE WHERE UM_ROLE_NAME LIKE ? AND UM_TENANT_ID=? AND UM_SHARED_ROLE ='0' ORDER BY UM_ROLE_NAME";
    public static final String GET_SHARED_ROLE_LIST_SQL = "SELECT UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE FROM UM_ROLE WHERE UM_ROLE_NAME LIKE ? AND UM_SHARED_ROLE ='1' ORDER BY UM_ROLE_NAME";
    public static final String GET_USER_FILTER_SQL = "SELECT UM_USER_NAME FROM UM_USER WHERE UM_USER_NAME LIKE ? AND UM_TENANT_ID=? ORDER BY UM_USER_NAME";
    public static final String GET_USER_ROLE_SQL = "SELECT UM_ROLE_NAME FROM UM_USER_ROLE, UM_ROLE, UM_USER WHERE UM_USER.UM_USER_NAME=? AND UM_USER.UM_ID=UM_USER_ROLE.UM_USER_ID AND UM_ROLE.UM_ID=UM_USER_ROLE.UM_ROLE_ID AND UM_USER_ROLE.UM_TENANT_ID=? AND UM_ROLE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?";
    public static final String GET_SHARED_ROLES_FOR_USER_SQL =
            "SELECT UM_ROLE_NAME, UM_ROLE.UM_TENANT_ID, UM_SHARED_ROLE FROM UM_SHARED_USER_ROLE INNER JOIN UM_USER ON "
                    + "UM_SHARED_USER_ROLE.UM_USER_ID = UM_USER.UM_ID INNER JOIN UM_ROLE ON "
                    + "UM_SHARED_USER_ROLE.UM_ROLE_ID = UM_ROLE.UM_ID WHERE UM_USER.UM_USER_NAME = ? "
                    + "AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = UM_USER.UM_TENANT_ID AND "
                    + "UM_SHARED_USER_ROLE.UM_ROLE_TENANT_ID = UM_ROLE.UM_TENANT_ID AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = ? ";
    public static final String GET_IS_ROLE_EXISTING_SQL = "SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?";
    public static final String GET_USERS_IN_ROLE_SQL = "SELECT UM_USER_NAME FROM UM_USER_ROLE, UM_ROLE, UM_USER WHERE UM_ROLE.UM_ROLE_NAME=? AND UM_USER.UM_ID=UM_USER_ROLE.UM_USER_ID AND UM_ROLE.UM_ID=UM_USER_ROLE.UM_ROLE_ID AND UM_USER_ROLE.UM_TENANT_ID=? AND UM_ROLE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?";
    public static final String GET_USERS_IN_SHARED_ROLE_SQL =
            "SELECT UM_USER_NAME FROM UM_SHARED_USER_ROLE INNER JOIN UM_USER ON "
                    + "UM_SHARED_USER_ROLE.UM_USER_ID = UM_USER.UM_ID INNER JOIN UM_ROLE ON "
                    + "UM_SHARED_USER_ROLE.UM_ROLE_ID = UM_ROLE.UM_ID WHERE UM_ROLE.UM_ROLE_NAME= ? "
                    + "AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = UM_USER.UM_TENANT_ID AND "
                    + "UM_SHARED_USER_ROLE.UM_ROLE_TENANT_ID = UM_ROLE.UM_TENANT_ID";
    public static final String GET_IS_USER_EXISTING_SQL = "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?";
    public static final String GET_PROPS_FOR_PROFILE_SQL = "SELECT UM_ATTR_NAME, UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE, UM_USER WHERE UM_USER.UM_ID = UM_USER_ATTRIBUTE.UM_USER_ID AND UM_USER.UM_USER_NAME=? AND UM_PROFILE_ID=? AND UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?";
    public static final String GET_PROP_FOR_PROFILE_SQL = "SELECT UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE, UM_USER WHERE UM_USER.UM_ID = UM_USER_ATTRIBUTE.UM_USER_ID AND UM_USER.UM_USER_NAME=? AND UM_ATTR_NAME=? AND UM_PROFILE_ID=? AND UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?";
    public static final String GET_USERS_FOR_PROP_SQL = "SELECT UM_USER_NAME FROM UM_USER, UM_USER_ATTRIBUTE WHERE UM_USER_ATTRIBUTE.UM_USER_ID = UM_USER.UM_ID AND UM_USER_ATTRIBUTE.UM_ATTR_NAME =? AND UM_USER_ATTRIBUTE.UM_ATTR_VALUE LIKE ? AND UM_USER_ATTRIBUTE.UM_PROFILE_ID=? AND UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?";
    public static final String GET_PROFILE_NAMES_SQL = "SELECT DISTINCT UM_PROFILE_ID FROM UM_USER_ATTRIBUTE WHERE UM_TENANT_ID=?";
    public static final String GET_PROFILE_NAMES_FOR_USER_SQL = "SELECT DISTINCT UM_PROFILE_ID FROM UM_USER_ATTRIBUTE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String GET_USERID_FROM_USERNAME_SQL = "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?";
    public static final String GET_USERNAME_FROM_TENANT_ID_SQL = "SELECT UM_USER_NAME FROM UM_USER WHERE UM_TENANT_ID=?";
    public static final String GET_TENANT_ID_FROM_USERNAME_SQL = "SELECT UM_TENANT_ID FROM UM_USER WHERE UM_USER_NAME=?";
    public static final String ADD_USER_SQL = "INSERT INTO UM_USER (UM_USER_NAME, UM_USER_PASSWORD, UM_SALT_VALUE, UM_REQUIRE_CHANGE, UM_CHANGED_TIME, UM_TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)";
    public static final String ADD_USER_TO_ROLE_SQL = "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) VALUES ((SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?), ?)";
    public static final String ADD_USER_PERMISSION_SQL = "AddUserPermission";
    public static final String ADD_ROLE_SQL = "INSERT INTO UM_ROLE (UM_ROLE_NAME, UM_TENANT_ID) VALUES (?, ?)";
    public static final String ADD_SHARED_ROLE_SQL = "UPDATE UM_ROLE SET UM_SHARED_ROLE = ? WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID = ?";
    public static final String ADD_ROLE_TO_USER_SQL = "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) VALUES ((SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), ?)";
    public static final String ADD_SHARED_ROLE_TO_USER_SQL =
            "INSERT INTO UM_SHARED_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_USER_TENANT_ID, UM_ROLE_TENANT_ID) "
                    + "VALUES ((SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),"
                    + "(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), ?, ?)";
    public static final String ADD_ROLE_PERMISSION_SQL = "AddRolePermissionSQL";
    public static final String REMOVE_USER_FROM_SHARED_ROLE_SQL =
            "DELETE FROM UM_SHARED_USER_ROLE WHERE   UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?) "
                    + "AND UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) "
                    + "AND UM_USER_TENANT_ID=? AND UM_ROLE_TENANT_ID = ?";
    public static final String REMOVE_USER_FROM_ROLE_SQL = "DELETE FROM UM_USER_ROLE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String REMOVE_ROLE_FROM_USER_SQL = "DELETE FROM UM_USER_ROLE WHERE UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?) AND UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String DELETE_ROLE_SQL = "DELETE FROM UM_ROLE WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID=?";
    public static final String ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL = "DELETE FROM UM_USER_ROLE WHERE UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String ON_DELETE_ROLE_DELETE_PERMISSION_SQL = "OnDeleteRoleRemovePermissionsSQL";
    public static final String DELETE_USER_SQL = "DELETE FROM UM_USER WHERE UM_USER_NAME = ? AND UM_TENANT_ID=?";
    public static final String ON_DELETE_USER_REMOVE_USER_ROLE_SQL = "DELETE FROM UM_USER_ROLE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL = "DELETE FROM UM_USER_ATTRIBUTE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?";
    public static final String ON_DELETE_USER_DELETE_PERMISSION_SQL = "OnDeleteUserRemovePermissionsSQL";
    public static final String UPDATE_USER_PASSWORD_SQL = "UPDATE UM_USER SET UM_USER_PASSWORD= ?, UM_SALT_VALUE=?, UM_REQUIRE_CHANGE=?, UM_CHANGED_TIME=? WHERE UM_USER_NAME= ? AND UM_TENANT_ID=?";
    public static final String UPDATE_ROLE_NAME_SQL = "UPDATE UM_ROLE set UM_ROLE_NAME=? WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID=?";
    public static final String ADD_USER_PROPERTY_SQL = "INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, UM_ATTR_VALUE, UM_PROFILE_ID, UM_TENANT_ID) VALUES ((SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), ?, ?, ?, ?)";
    public static final String UPDATE_USER_PROPERTY_SQL = "UPDATE UM_USER_ATTRIBUTE SET UM_ATTR_VALUE=? WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ATTR_NAME=? AND UM_PROFILE_ID=? AND UM_TENANT_ID=?";
    public static final String DELETE_USER_PROPERTY_SQL = "DELETE FROM UM_USER_ATTRIBUTE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ATTR_NAME=? AND UM_PROFILE_ID=? AND UM_TENANT_ID=?";
    public static final String USER_NAME_UNIQUE_SQL =
            "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=?";
    public static final String IS_DOMAIN_EXISTING_SQL = "SELECT UM_DOMAIN_ID FROM UM_DOMAIN WHERE UM_DOMAIN_NAME=? AND UM_TENANT_ID=?";
    public static final String ADD_DOMAIN_SQL = "INSERT INTO UM_DOMAIN (UM_DOMAIN_NAME, UM_TENANT_ID) VALUES (?, ?)";
    public static final String CHECK_DOMAIN_ID_EXISTING_SQL = "SELECT UM_DOMAIN_ID FROM UM_DOMAIN WHERE UM_DOMAIN_ID=? AND UM_TENANT_ID=?";
    public static final String UPDATE_DOMAIN_SQL = "UPDATE UM_DOMAIN SET UM_DOMAIN_NAME=? WHERE UM_DOMAIN_NAME=? AND UM_TENANT_ID=?";
    public static final String DELETE_DOMAIN_SQL = "DELETE FROM UM_DOMAIN WHERE UM_DOMAIN_NAME=? AND UM_TENANT_ID=?";
    public static final String DIGEST_FUNCTION = "PasswordDigest";
    public static final String STORE_SALTED_PASSWORDS = "StoreSaltedPassword";
    // properties
    public static final String DATASOURCE = "dataSource";
    public static final String URL = "url";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String DRIVER_NAME = "driverName";
    public static final String MAX_ACTIVE = "maxActive";
    public static final String MAX_WAIT = "maxWait";
    public static final String MIN_IDLE = "minIdle";
    public static final String MAX_IDLE = "maxIdle";
    public static final String VALIDATION_QUERY = "validationQuery";
    public static final String DIALECT_FLAG = "dialectFlag";
    public static final String TEST_WHILE_IDLE = "testWhileIdle";
    public static final String TIME_BETWEEN_EVICTION_RUNS_MILLIS = "timeBetweenEvictionRunsMillis";
    public static final String MIN_EVIC_TABLE_IDLE_TIME_MILLIS = "minEvictableIdleTimeMillis";
    public static final String NUM_TESTS_PEREVICTION_RUN = "numTestsPerEvictionRun";

    // mssql
    public static final String ADD_USER_TO_ROLE_MSSQL_SQL = "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) SELECT (SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),(?)";
    public static final String ADD_ROLE_TO_USER_MSSQL_SQL = "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) SELECT (SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), (?)";
    public static final String ADD_USER_PROPERTY_MSSQL_SQL = "INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, UM_ATTR_VALUE, UM_PROFILE_ID, UM_TENANT_ID) SELECT (SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), (?), (?), (?), (?)";
    public static final String ADD_USER_TO_ROLE_MSSQL = "AddUserToRoleSQL-mssql";
    public static final String ADD_ROLE_TO_USER_MSSQL = "AddRoleToUserSQL-mssql";
    public static final String ADD_USER_PROPERTY_MSSQL = "AddUserPropertySQL-mssql";
    //openedge
    public static final String ADD_USER_TO_ROLE_OPENEDGE_SQL = "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) SELECT UU.UM_ID, UR.UM_ID, ? FROM UM_USER UU, UM_ROLE UR WHERE UU.UM_USER_NAME=? AND UU.UM_TENANT_ID=? AND UR.UM_ROLE_NAME=? AND UR.UM_TENANT_ID=?";
    public static final String ADD_ROLE_TO_USER_OPENEDGE_SQL = "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) SELECT UR.UM_ID, UU.UM_ID, ? FROM UM_ROLE UR, UM_USER UU WHERE UR.UM_ROLE_NAME=? AND UR.UM_TENANT_ID=? AND UU.UM_USER_NAME=? AND UU.UM_TENANT_ID=?";
    public static final String ADD_USER_PROPERTY_OPENEDGE_SQL = "INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, UM_ATTR_VALUE, UM_PROFILE_ID, UM_TENANT_ID) SELECT UM_ID, ?, ?, ?, ? FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?";
    public static final String ADD_USER_TO_ROLE_OPENEDGE = "AddUserToRoleSQL-openedge";
    public static final String ADD_ROLE_TO_USER_OPENEDGE = "AddRoleToUserSQL-openedge";
    public static final String ADD_USER_PROPERTY_OPENEDGE = "AddUserPropertySQL-openedge";
    private JDBCRealmConstants() {
    }
}
