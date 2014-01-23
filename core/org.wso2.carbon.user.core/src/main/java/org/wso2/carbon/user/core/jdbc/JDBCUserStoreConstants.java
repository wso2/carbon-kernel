/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.user.core.jdbc;


import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserStoreConfigConstants;

import java.util.ArrayList;

public class JDBCUserStoreConstants {


    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> JDBC_UM_MANDATORY_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> JDBC_UM_OPTIONAL_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> JDBC_UM_ADVANCED_PROPERTIES = new ArrayList<Property>();


    static {

    	setMandatoryProperty(JDBCRealmConstants.DRIVER_NAME, "", "Full qualified driver name");
    	setMandatoryProperty(JDBCRealmConstants.URL, "", "URL of the user store database");
    	setMandatoryProperty(JDBCRealmConstants.USER_NAME, "", "Username for the database");
    	setMandatoryProperty(JDBCRealmConstants.PASSWORD, "", "Password for the database");
//        setMandatoryProperty(UserStoreConfigConstants.dataSource, "jdbc/WSO2CarbonDB", UserStoreConfigConstants.dataSourceDescription);

        setProperty(UserStoreConfigConstants.disabled, "false", UserStoreConfigConstants.disabledDescription);
        setProperty(UserStoreConfigConstants.maxUserNameListLength, "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);


//      LDAP Specific Properties
        setProperty("PasswordDigest", "SHA-256", UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty(UserStoreConfigConstants.readGroups, "true", UserStoreConfigConstants.readLDAPGroupsDescription);
        setProperty("ReadOnly", "false", "Indicates whether the user store of this realm operates in the user read only mode or not");
        setProperty("IsEmailUserName", "false", "Indicates whether Email is used as user name (apply when realm operates in read only mode).");
        setProperty("DomainCalculation", "default", "Can be either default or custom (apply when realm operates in read only mode)");
        setProperty("StoreSaltedPassword", "true", "Indicates whether to salt the password");
        setProperty(UserStoreConfigConstants.writeGroups, "true", UserStoreConfigConstants.writeGroupsDescription);
        setProperty("UserNameUniqueAcrossTenants", "false", "An attribute used for multi-tenancy");
        setProperty("PasswordJavaRegEx", "^[\\S]{5,30}$", "A regular expression to validate passwords");
        setProperty("PasswordJavaScriptRegEx", "^[\\S]{5,30}$", "The regular expression used by the font-end components for password validation");
        setProperty("UsernameJavaRegEx", "^[\\S]{5,30}$", "A regular expression to validate user names");
//        setProperty("UsernameJavaRegEx","^[^~!#$;%^*+={}\\\\|\\\\\\\\&lt;&gt;,\\\'\\\"]{3,30}$","A regular expression to validate user names");
        setProperty("UsernameJavaScriptRegEx", "^[\\S]{5,30}$", "The regular expression used by the font-end components for username validation");
        setProperty("RolenameJavaRegEx", "^[\\S]{5,30}$", "A regular expression to validate role names");
//        setProperty("RolenameJavaRegEx","^[^~!#$;%^*+={}\\\\|\\\\\\\\&lt;&gt;,\\\'\\\"]{3,30}$","A regular expression to validate role names");
        setProperty("RolenameJavaScriptRegEx", "^[\\S]{5,30}$", "The regular expression used by the font-end components for role name validation");
        setProperty(UserStoreConfigConstants.SCIMEnabled, "false", UserStoreConfigConstants.SCIMEnabledDescription);


        //Advanced Properties (No descriptions added for each property)
        setAdvancedProperty("SelectUserSQL", "SELECT * FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?", "");
        setAdvancedProperty("GetRoleListSQL", "SELECT UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME LIKE ? AND UM_TENANT_ID=? AND UM_SHARED_ROLE ='0' ORDER BY UM_ROLE_NAME", "");
        setAdvancedProperty("GetSharedRoleListSQL", "SELECT UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME LIKE ? AND UM_SHARED_ROLE ='1' ORDER BY UM_ROLE_NAME", "");
        setAdvancedProperty("UserFilterSQL", "SELECT UM_USER_NAME FROM UM_USER WHERE UM_USER_NAME LIKE ? " +
                "AND UM_TENANT_ID=? ORDER BY UM_USER_NAME", "");
        setAdvancedProperty("UserRoleSQL ", "SELECT UM_ROLE_NAME FROM UM_USER_ROLE, UM_ROLE, UM_USER WHERE " +
                "UM_USER.UM_USER_NAME=? AND UM_USER.UM_ID=UM_USER_ROLE.UM_USER_ID AND UM_ROLE.UM_ID=UM_USER_ROLE.UM_ROLE_ID " +
                "AND UM_USER_ROLE.UM_TENANT_ID=? AND UM_ROLE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?", "");
        setAdvancedProperty("UserSharedRoleSQL",
                "SELECT UM_ROLE_NAME, UM_ROLE.UM_TENANT_ID, UM_SHARED_ROLE FROM UM_SHARED_USER_ROLE INNER JOIN UM_USER ON "
                        + "UM_SHARED_USER_ROLE.UM_USER_ID = UM_USER.UM_ID INNER JOIN UM_ROLE ON "
                        + "UM_SHARED_USER_ROLE.UM_ROLE_ID = UM_ROLE.UM_ID WHERE UM_USER.UM_USER_NAME = ? "
                        + "AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = UM_USER.UM_TENANT_ID AND "
                        + "UM_SHARED_USER_ROLE.UM_ROLE_TENANT_ID = UM_ROLE.UM_TENANT_ID AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = ? ","");


        setAdvancedProperty("IsRoleExistingSQL", "SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserListOfRoleSQL", "SELECT UM_USER_NAME FROM UM_USER_ROLE, UM_ROLE, UM_USER WHERE " +
                "UM_ROLE.UM_ROLE_NAME=? AND UM_USER.UM_ID=UM_USER_ROLE.UM_USER_ID AND UM_ROLE.UM_ID=UM_USER_ROLE.UM_ROLE_ID " +
                "AND UM_USER_ROLE.UM_TENANT_ID=? AND UM_ROLE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserListOfSharedRoleSQL",
                "SELECT UM_USER_NAME FROM UM_SHARED_USER_ROLE INNER JOIN UM_USER ON "
                        + "UM_SHARED_USER_ROLE.UM_USER_ID = UM_USER.UM_ID INNER JOIN UM_ROLE ON "
                        + "UM_SHARED_USER_ROLE.UM_ROLE_ID = UM_ROLE.UM_ID WHERE UM_ROLE.UM_ROLE_NAME= ? "
                        + "AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = UM_USER.UM_TENANT_ID AND "
                        + "UM_SHARED_USER_ROLE.UM_ROLE_TENANT_ID = UM_ROLE.UM_TENANT_ID","");

        setAdvancedProperty("IsUserExistingSQL", "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserPropertiesForProfileSQL", "SELECT UM_ATTR_NAME, UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE, UM_USER WHERE " +
                "UM_USER.UM_ID = UM_USER_ATTRIBUTE.UM_USER_ID AND UM_USER.UM_USER_NAME=? AND UM_PROFILE_ID=? " +
                "AND UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserPropertyForProfileSQL", "SELECT UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE, UM_USER WHERE " +
                "UM_USER.UM_ID = UM_USER_ATTRIBUTE.UM_USER_ID AND UM_USER.UM_USER_NAME=? AND UM_ATTR_NAME=? " +
                "AND UM_PROFILE_ID=? AND UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserLisForPropertySQL", "SELECT UM_USER_NAME FROM UM_USER, UM_USER_ATTRIBUTE WHERE " +
                "UM_USER_ATTRIBUTE.UM_USER_ID = UM_USER.UM_ID AND UM_USER_ATTRIBUTE.UM_ATTR_NAME =? AND " +
                "UM_USER_ATTRIBUTE.UM_ATTR_VALUE =? AND UM_USER_ATTRIBUTE.UM_PROFILE_ID=? AND " +
                "UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?","");
        setAdvancedProperty("GetProfileNamesSQL ", "SELECT DISTINCT UM_PROFILE_ID FROM UM_USER_ATTRIBUTE WHERE UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserProfileNamesSQL", "SELECT DISTINCT UM_PROFILE_ID FROM UM_USER_ATTRIBUTE WHERE " +
                "UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserIDFromUserNameSQL", "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserNameFromTenantIDSQL", "SELECT UM_USER_NAME FROM UM_USER WHERE UM_TENANT_ID=?","");
        setAdvancedProperty("GetTenantIDFromUserNameSQL", "SELECT UM_TENANT_ID FROM UM_USER WHERE UM_USER_NAME=?","");

        setAdvancedProperty("AddUserSQL", "INSERT INTO UM_USER (UM_USER_NAME, UM_USER_PASSWORD, UM_SALT_VALUE, UM_REQUIRE_CHANGE, " +
                "UM_CHANGED_TIME, UM_TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)","");
        setAdvancedProperty("AddUserToRoleSQL", "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) VALUES " +
                "((SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME=? AND UM_TENANT_ID=?), ?)","");
        setAdvancedProperty("AddRoleSQL", "INSERT INTO UM_ROLE (UM_ROLE_NAME, UM_TENANT_ID) VALUES (?, ?)","");
        setAdvancedProperty("AddSharedRoleSQL", "UPDATE UM_ROLE SET UM_SHARED_ROLE = ? WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID = ?","");
        setAdvancedProperty("AddRoleToUserSQL", "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) VALUES " +
                "((SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_USER WHERE " +
                "UM_USER_NAME=? AND UM_TENANT_ID=?), ?)","");
        setAdvancedProperty("AddSharedRoleToUserSQL",
                "INSERT INTO UM_SHARED_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_USER_TENANT_ID, UM_ROLE_TENANT_ID) "
                        + "VALUES ((SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),"
                        + "(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), ?, ?)","");

        setAdvancedProperty("RemoveUserFromSharedRoleSQL",
                "DELETE FROM UM_SHARED_USER_ROLE WHERE   UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?) "
                        + "AND UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) "
                        + "AND UM_USER_TENANT_ID=? AND UM_ROLE_TENANT_ID = ?","");

        setAdvancedProperty("RemoveUserFromRoleSQL", "DELETE FROM UM_USER_ROLE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE " +
                "UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND " +
                "UM_TENANT_ID=?) AND UM_TENANT_ID=?","");

        setAdvancedProperty("RemoveRoleFromUserSQL", "DELETE FROM UM_USER_ROLE WHERE UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME=? AND UM_TENANT_ID=?) AND UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? " +
                "AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");

        setAdvancedProperty("DeleteRoleSQL ", "DELETE FROM UM_ROLE WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID=?","");
        setAdvancedProperty("OnDeleteRoleRemoveUserRoleMappingSQL ", "DELETE FROM UM_USER_ROLE WHERE UM_ROLE_ID=(SELECT UM_ID FROM " +
                "UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");
        setAdvancedProperty("DeleteUserSQL", "DELETE FROM UM_USER WHERE UM_USER_NAME = ? AND UM_TENANT_ID=?","");
        setAdvancedProperty("OnDeleteUserRemoveUserRoleMappingSQL", "DELETE FROM UM_USER_ROLE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE " +
                "UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");
        setAdvancedProperty("OnDeleteUserRemoveUserAttributeSQL ", "DELETE FROM UM_USER_ATTRIBUTE WHERE " +
                "UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");

        setAdvancedProperty("UpdateUserPasswordSQL", "UPDATE UM_USER SET UM_USER_PASSWORD= ?, UM_SALT_VALUE=?, " +
                "UM_REQUIRE_CHANGE=?, UM_CHANGED_TIME=? WHERE UM_USER_NAME= ? AND UM_TENANT_ID=?","");
        setAdvancedProperty("UpdateRoleNameSQL", "UPDATE UM_ROLE set UM_ROLE_NAME=? WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID=?","");

        setAdvancedProperty("AddUserPropertySQL ", "INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, UM_ATTR_VALUE, " +
                "UM_PROFILE_ID, UM_TENANT_ID) VALUES ((SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), ?, ?, ?, ?)","");
        setAdvancedProperty("UpdateUserPropertySQL ", "UPDATE UM_USER_ATTRIBUTE SET UM_ATTR_VALUE=? WHERE " +
                "UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ATTR_NAME=? AND" +
                " UM_PROFILE_ID=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("DeleteUserPropertySQL ", "DELETE FROM UM_USER_ATTRIBUTE WHERE UM_USER_ID=(SELECT UM_ID FROM " +
                "UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ATTR_NAME=? AND UM_PROFILE_ID=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("UserNameUniqueAcrossTenantsSQL", "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=?","");

        setAdvancedProperty("IsDomainExistingSQL", "SELECT UM_DOMAIN_ID FROM UM_DOMAIN WHERE UM_DOMAIN_NAME=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("AddDomainSQL", "INSERT INTO UM_DOMAIN (UM_DOMAIN_NAME, UM_TENANT_ID) VALUES (?, ?)","");

        // mssql
        setAdvancedProperty("AddUserToRoleSQL-mssql", "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) SELECT " +
                "(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME=? AND UM_TENANT_ID=?),(?)","");
        setAdvancedProperty("AddRoleToUserSQL-mssql", "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) SELECT " +
                "(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_USER WHERE " +
                "UM_USER_NAME=? AND UM_TENANT_ID=?), (?)","");
        setAdvancedProperty("AddUserPropertySQL-mssql", "INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, UM_ATTR_VALUE, " +
                "UM_PROFILE_ID, UM_TENANT_ID) SELECT (SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), (?), (?), (?), (?)","");

        //openedge
        setAdvancedProperty("AddUserToRoleSQL-openedge", "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) SELECT " +
                "UU.UM_ID, UR.UM_ID, ? FROM UM_USER UU, UM_ROLE UR WHERE UU.UM_USER_NAME=? AND UU.UM_TENANT_ID=? AND " +
                "UR.UM_ROLE_NAME=? AND UR.UM_TENANT_ID=?","");
        setAdvancedProperty("AddRoleToUserSQL-openedge", "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) SELECT " +
                "UR.UM_ID, UU.UM_ID, ? FROM UM_ROLE UR, UM_USER UU WHERE UR.UM_ROLE_NAME=? AND UR.UM_TENANT_ID=? AND " +
                "UU.UM_USER_NAME=? AND UU.UM_TENANT_ID=?","");
        setAdvancedProperty("AddUserPropertySQL-openedge", "INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, " +
                "UM_ATTR_VALUE, UM_PROFILE_ID, UM_TENANT_ID) SELECT UM_ID, ?, ?, ?, ? FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?","");

    }


    private static void setProperty(String name, String value, String description) {
        Property property = new Property(name, value, description, null);
        JDBC_UM_OPTIONAL_PROPERTIES.add(property);

    }

    private static void setMandatoryProperty(String name, String value, String description) {
        Property property = new Property(name, value, description, null);
        JDBC_UM_MANDATORY_PROPERTIES.add(property);

    }

    private static void setAdvancedProperty(String name, String value, String description) {
        Property property = new Property(name, value, description, null);
        JDBC_UM_ADVANCED_PROPERTIES.add(property);

    }


}
