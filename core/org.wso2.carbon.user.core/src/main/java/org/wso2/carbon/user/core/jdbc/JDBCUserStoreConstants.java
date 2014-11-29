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

    	setMandatoryProperty(JDBCRealmConstants.DRIVER_NAME,"Driver Name", "",
                "Full qualified driver name", false);
        setMandatoryProperty(JDBCRealmConstants.URL, "Connection URL", "",
                "URL of the user store database",false);
        setMandatoryProperty(JDBCRealmConstants.USER_NAME, "Connection Name", "",
                "Username for the database", false);
        setMandatoryProperty(JDBCRealmConstants.PASSWORD,"Connection Password", "",
                "Password for the database",true);
//        setMandatoryProperty(UserStoreConfigConstants.dataSource, "jdbc/WSO2CarbonDB", UserStoreConfigConstants.dataSourceDescription);

        setProperty(UserStoreConfigConstants.disabled,"Disabled", "false", UserStoreConfigConstants.disabledDescription);
        setProperty(UserStoreConfigConstants.maxUserNameListLength,"Maximum User List Length", "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength,"Maximum Role List Length", "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled,"Enable User Role Cache", "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);


//      LDAP Specific Properties
        setProperty("PasswordDigest","Password Hashing Algorithm", "SHA-256", UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty(UserStoreConfigConstants.readGroups,"Read Groups", "true", UserStoreConfigConstants.readLDAPGroupsDescription);
        setProperty("ReadOnly","Read-only", "false", "Indicates whether the user store of this realm operates in the user read only mode or not");
        setProperty("IsEmailUserName","Is Email Username", "false", "Indicates whether Email is used as user name (apply when realm operates in read only mode).");
        setProperty("DomainCalculation","Domain Calculation", "default", "Can be either default or custom (apply when realm operates in read only mode)");
        setProperty("StoreSaltedPassword","Enable Salted Passwords", "true", "Indicates whether to salt the password");
        setProperty(UserStoreConfigConstants.writeGroups,"Enable Write Groups", "true", UserStoreConfigConstants.writeGroupsDescription);
        setProperty("UserNameUniqueAcrossTenants","Make Username Unique Across Tenants", "false", "An attribute used for multi-tenancy");
        setProperty("PasswordJavaRegEx","Password RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate passwords");
        setProperty("PasswordJavaScriptRegEx","Password RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression used by the font-end components for password validation");
        setProperty("UsernameJavaRegEx","Username RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate user names");
//        setProperty("UsernameJavaRegEx","^[^~!#$;%^*+={}\\\\|\\\\\\\\&lt;&gt;,\\\'\\\"]{3,30}$","A regular expression to validate user names");
        setProperty("UsernameJavaScriptRegEx","Username RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression used by the font-end components for username validation");
        setProperty("RolenameJavaRegEx","Role Name RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate role names");
//        setProperty("RolenameJavaRegEx","^[^~!#$;%^*+={}\\\\|\\\\\\\\&lt;&gt;,\\\'\\\"]{3,30}$","A regular expression to validate role names");
        setProperty("RolenameJavaScriptRegEx","Role Name RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression used by the font-end components for role name validation");
        setProperty(UserStoreConfigConstants.SCIMEnabled,"", "false", UserStoreConfigConstants.SCIMEnabledDescription);


        //Advanced Properties (No descriptions added for each property)
        setAdvancedProperty("SelectUserSQL","Select User SQL", "SELECT * FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?", "");
        setAdvancedProperty("GetRoleListSQL","Get Role List SQL", "SELECT UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME LIKE ? AND UM_TENANT_ID=? AND UM_SHARED_ROLE ='0' ORDER BY UM_ROLE_NAME", "");
        setAdvancedProperty("GetSharedRoleListSQL","Get Shared Role List SQP", "SELECT UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME LIKE ? AND UM_SHARED_ROLE ='1' ORDER BY UM_ROLE_NAME", "");
        setAdvancedProperty("UserFilterSQL","User Filter SQL", "SELECT UM_USER_NAME FROM UM_USER WHERE UM_USER_NAME LIKE ? " +
                "AND UM_TENANT_ID=? ORDER BY UM_USER_NAME", "");
        setAdvancedProperty("UserRoleSQL ","User Role SQL", "SELECT UM_ROLE_NAME FROM UM_USER_ROLE, UM_ROLE, UM_USER WHERE " +
                "UM_USER.UM_USER_NAME=? AND UM_USER.UM_ID=UM_USER_ROLE.UM_USER_ID AND UM_ROLE.UM_ID=UM_USER_ROLE.UM_ROLE_ID " +
                "AND UM_USER_ROLE.UM_TENANT_ID=? AND UM_ROLE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?", "");
        setAdvancedProperty("UserSharedRoleSQL","User Shared Role SQL",
                "SELECT UM_ROLE_NAME, UM_ROLE.UM_TENANT_ID, UM_SHARED_ROLE FROM UM_SHARED_USER_ROLE INNER JOIN UM_USER ON "
                        + "UM_SHARED_USER_ROLE.UM_USER_ID = UM_USER.UM_ID INNER JOIN UM_ROLE ON "
                        + "UM_SHARED_USER_ROLE.UM_ROLE_ID = UM_ROLE.UM_ID WHERE UM_USER.UM_USER_NAME = ? "
                        + "AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = UM_USER.UM_TENANT_ID AND "
                        + "UM_SHARED_USER_ROLE.UM_ROLE_TENANT_ID = UM_ROLE.UM_TENANT_ID AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = ? ","");


        setAdvancedProperty("IsRoleExistingSQL","Is Role Existing SQL", "SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserListOfRoleSQL","Get User List Of Role SQL", "SELECT UM_USER_NAME FROM UM_USER_ROLE, UM_ROLE, UM_USER WHERE " +
                "UM_ROLE.UM_ROLE_NAME=? AND UM_USER.UM_ID=UM_USER_ROLE.UM_USER_ID AND UM_ROLE.UM_ID=UM_USER_ROLE.UM_ROLE_ID " +
                "AND UM_USER_ROLE.UM_TENANT_ID=? AND UM_ROLE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserListOfSharedRoleSQL","Get User List Of Shared Role SQL",
                "SELECT UM_USER_NAME FROM UM_SHARED_USER_ROLE INNER JOIN UM_USER ON "
                        + "UM_SHARED_USER_ROLE.UM_USER_ID = UM_USER.UM_ID INNER JOIN UM_ROLE ON "
                        + "UM_SHARED_USER_ROLE.UM_ROLE_ID = UM_ROLE.UM_ID WHERE UM_ROLE.UM_ROLE_NAME= ? "
                        + "AND UM_SHARED_USER_ROLE.UM_USER_TENANT_ID = UM_USER.UM_TENANT_ID AND "
                        + "UM_SHARED_USER_ROLE.UM_ROLE_TENANT_ID = UM_ROLE.UM_TENANT_ID","");

        setAdvancedProperty("IsUserExistingSQL","Is User Existing SQL", "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserPropertiesForProfileSQL","Get User Properties for Profile SQL", "SELECT UM_ATTR_NAME, UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE, UM_USER WHERE " +
                "UM_USER.UM_ID = UM_USER_ATTRIBUTE.UM_USER_ID AND UM_USER.UM_USER_NAME=? AND UM_PROFILE_ID=? " +
                "AND UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserPropertyForProfileSQL","Get User Property for Profile SQL", "SELECT UM_ATTR_VALUE FROM UM_USER_ATTRIBUTE, UM_USER WHERE " +
                "UM_USER.UM_ID = UM_USER_ATTRIBUTE.UM_USER_ID AND UM_USER.UM_USER_NAME=? AND UM_ATTR_NAME=? " +
                "AND UM_PROFILE_ID=? AND UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserLisForPropertySQL","Get User List for Property SQL", "SELECT UM_USER_NAME FROM UM_USER, UM_USER_ATTRIBUTE WHERE " +
                "UM_USER_ATTRIBUTE.UM_USER_ID = UM_USER.UM_ID AND UM_USER_ATTRIBUTE.UM_ATTR_NAME =? AND " +
                "UM_USER_ATTRIBUTE.UM_ATTR_VALUE =? AND UM_USER_ATTRIBUTE.UM_PROFILE_ID=? AND " +
                "UM_USER_ATTRIBUTE.UM_TENANT_ID=? AND UM_USER.UM_TENANT_ID=?","");
        setAdvancedProperty("GetProfileNamesSQL ","Get Profile Names SQL", "SELECT DISTINCT UM_PROFILE_ID FROM UM_USER_ATTRIBUTE WHERE UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserProfileNamesSQL","Get User Profile Names SQL", "SELECT DISTINCT UM_PROFILE_ID FROM UM_USER_ATTRIBUTE WHERE " +
                "UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserIDFromUserNameSQL","Get User ID From Username SQL", "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("GetUserNameFromTenantIDSQL","Get Username From Tenant ID SQL", "SELECT UM_USER_NAME FROM UM_USER WHERE UM_TENANT_ID=?","");
        setAdvancedProperty("GetTenantIDFromUserNameSQL","Get Tenant ID From Username SQL", "SELECT UM_TENANT_ID FROM UM_USER WHERE UM_USER_NAME=?","");

        setAdvancedProperty("AddUserSQL","Add User SQL", "INSERT INTO UM_USER (UM_USER_NAME, UM_USER_PASSWORD, UM_SALT_VALUE, UM_REQUIRE_CHANGE, " +
                "UM_CHANGED_TIME, UM_TENANT_ID) VALUES (?, ?, ?, ?, ?, ?)","");
        setAdvancedProperty("AddUserToRoleSQL","Add User To Role SQL", "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) VALUES " +
                "((SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME=? AND UM_TENANT_ID=?), ?)","");
        setAdvancedProperty("AddRoleSQL","Add Role SQL", "INSERT INTO UM_ROLE (UM_ROLE_NAME, UM_TENANT_ID) VALUES (?, ?)","");
        setAdvancedProperty("AddSharedRoleSQL","Add Shared Role SQL", "UPDATE UM_ROLE SET UM_SHARED_ROLE = ? WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID = ?","");
        setAdvancedProperty("AddRoleToUserSQL","Add Role To User SQL", "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) VALUES " +
                "((SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_USER WHERE " +
                "UM_USER_NAME=? AND UM_TENANT_ID=?), ?)","");
        setAdvancedProperty("AddSharedRoleToUserSQL","Add Shared Role To User SQL",
                "INSERT INTO UM_SHARED_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_USER_TENANT_ID, UM_ROLE_TENANT_ID) "
                        + "VALUES ((SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),"
                        + "(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), ?, ?)","");

        setAdvancedProperty("RemoveUserFromSharedRoleSQL","Remove User From Shared Roles SQL",
                "DELETE FROM UM_SHARED_USER_ROLE WHERE   UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?) "
                        + "AND UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) "
                        + "AND UM_USER_TENANT_ID=? AND UM_ROLE_TENANT_ID = ?","");

        setAdvancedProperty("RemoveUserFromRoleSQL","Remove User From Role SQL", "DELETE FROM UM_USER_ROLE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE " +
                "UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND " +
                "UM_TENANT_ID=?) AND UM_TENANT_ID=?","");

        setAdvancedProperty("RemoveRoleFromUserSQL","Remove Role From User SQL", "DELETE FROM UM_USER_ROLE WHERE UM_ROLE_ID=(SELECT UM_ID FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME=? AND UM_TENANT_ID=?) AND UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? " +
                "AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");

        setAdvancedProperty("DeleteRoleSQL ","Delete Roles SQL", "DELETE FROM UM_ROLE WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID=?","");
        setAdvancedProperty("OnDeleteRoleRemoveUserRoleMappingSQL ","On Delete Role Remove User Role Mapping SQL", "DELETE FROM UM_USER_ROLE WHERE UM_ROLE_ID=(SELECT UM_ID FROM " +
                "UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");
        setAdvancedProperty("DeleteUserSQL", "Delete User SQL","DELETE FROM UM_USER WHERE UM_USER_NAME = ? AND UM_TENANT_ID=?","");
        setAdvancedProperty("OnDeleteUserRemoveUserRoleMappingSQL","On Delete User Remove User Role Mapping SQL", "DELETE FROM UM_USER_ROLE WHERE UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE " +
                "UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");
        setAdvancedProperty("OnDeleteUserRemoveUserAttributeSQL ","On Delete User Remove User Attribute SQL", "DELETE FROM UM_USER_ATTRIBUTE WHERE " +
                "UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_TENANT_ID=?","");

        setAdvancedProperty("UpdateUserPasswordSQL","Update User Password SQL", "UPDATE UM_USER SET UM_USER_PASSWORD= ?, UM_SALT_VALUE=?, " +
                "UM_REQUIRE_CHANGE=?, UM_CHANGED_TIME=? WHERE UM_USER_NAME= ? AND UM_TENANT_ID=?","");
        setAdvancedProperty("UpdateRoleNameSQL","Update Role Name SQL", "UPDATE UM_ROLE set UM_ROLE_NAME=? WHERE UM_ROLE_NAME = ? AND UM_TENANT_ID=?","");

        setAdvancedProperty("AddUserPropertySQL ","Add User Property SQL", "INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, UM_ATTR_VALUE, " +
                "UM_PROFILE_ID, UM_TENANT_ID) VALUES ((SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), ?, ?, ?, ?)","");
        setAdvancedProperty("UpdateUserPropertySQL ","Update User Property SQL", "UPDATE UM_USER_ATTRIBUTE SET UM_ATTR_VALUE=? WHERE " +
                "UM_USER_ID=(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ATTR_NAME=? AND" +
                " UM_PROFILE_ID=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("DeleteUserPropertySQL ","Delete User Property SQL", "DELETE FROM UM_USER_ATTRIBUTE WHERE UM_USER_ID=(SELECT UM_ID FROM " +
                "UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?) AND UM_ATTR_NAME=? AND UM_PROFILE_ID=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("UserNameUniqueAcrossTenantsSQL","User Name Unique Across Tenant SQL", "SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=?","");

        setAdvancedProperty("IsDomainExistingSQL","Is Domain Existing SQL", "SELECT UM_DOMAIN_ID FROM UM_DOMAIN WHERE UM_DOMAIN_NAME=? AND UM_TENANT_ID=?","");
        setAdvancedProperty("AddDomainSQL","Add Domain SQL", "INSERT INTO UM_DOMAIN (UM_DOMAIN_NAME, UM_TENANT_ID) VALUES (?, ?)","");

        // mssql
        setAdvancedProperty("AddUserToRoleSQL-mssql","Add User To Role SQL (MSSQL)", "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) SELECT " +
                "(SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_ROLE WHERE " +
                "UM_ROLE_NAME=? AND UM_TENANT_ID=?),(?)","");
        setAdvancedProperty("AddRoleToUserSQL-mssql","Add Role To User SQL (MSSQL)", "INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) SELECT " +
                "(SELECT UM_ID FROM UM_ROLE WHERE UM_ROLE_NAME=? AND UM_TENANT_ID=?),(SELECT UM_ID FROM UM_USER WHERE " +
                "UM_USER_NAME=? AND UM_TENANT_ID=?), (?)","");
        setAdvancedProperty("AddUserPropertySQL-mssql","Add User Property (MSSQL)", "INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, UM_ATTR_VALUE, " +
                "UM_PROFILE_ID, UM_TENANT_ID) SELECT (SELECT UM_ID FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?), (?), (?), (?), (?)","");

        //openedge
        setAdvancedProperty("AddUserToRoleSQL-openedge","Add User To Role SQL (OpenEdge)", "INSERT INTO UM_USER_ROLE (UM_USER_ID, UM_ROLE_ID, UM_TENANT_ID) SELECT " +
                "UU.UM_ID, UR.UM_ID, ? FROM UM_USER UU, UM_ROLE UR WHERE UU.UM_USER_NAME=? AND UU.UM_TENANT_ID=? AND " +
                "UR.UM_ROLE_NAME=? AND UR.UM_TENANT_ID=?","");
        setAdvancedProperty("AddRoleToUserSQL-openedge", "Add Role To User SQL (OpenEdge)","INSERT INTO UM_USER_ROLE (UM_ROLE_ID, UM_USER_ID, UM_TENANT_ID) SELECT " +
                "UR.UM_ID, UU.UM_ID, ? FROM UM_ROLE UR, UM_USER UU WHERE UR.UM_ROLE_NAME=? AND UR.UM_TENANT_ID=? AND " +
                "UU.UM_USER_NAME=? AND UU.UM_TENANT_ID=?","");
        setAdvancedProperty("AddUserPropertySQL-openedge", "Add User Property (OpenEdge)","INSERT INTO UM_USER_ATTRIBUTE (UM_USER_ID, UM_ATTR_NAME, " +
                "UM_ATTR_VALUE, UM_PROFILE_ID, UM_TENANT_ID) SELECT UM_ID, ?, ?, ?, ? FROM UM_USER WHERE UM_USER_NAME=? AND UM_TENANT_ID=?","");
        setProperty("UniqueID", "","","");
    }


    private static void setProperty(String name, String displayName, String value,
            String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        JDBC_UM_OPTIONAL_PROPERTIES.add(property);

    }

    private static void setMandatoryProperty(String name, String displayName, String value,
            String description, boolean encrypt) {
        String propertyDescription = displayName + "#" + description;
        if(encrypt){
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, null);
        JDBC_UM_MANDATORY_PROPERTIES.add(property);

    }

    private static void setAdvancedProperty(String name, String displayName, String value,
            String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        JDBC_UM_ADVANCED_PROPERTIES.add(property);

    }


}
