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


    //For multiple attribute separation
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION = "This is the separator for multiple claim values";

    static {

        setMandatoryProperty(JDBCRealmConstants.DRIVER_NAME, "Driver Name", "",
                "Full qualified driver name", false);
        setMandatoryProperty(JDBCRealmConstants.URL, "Connection URL", "",
                "URL of the user store database", false);
        setMandatoryProperty(JDBCRealmConstants.USER_NAME, "Connection Name", "",
                "Username for the database", false);
        setMandatoryProperty(JDBCRealmConstants.PASSWORD, "Connection Password", "",
                "Password for the database", true);
//        setMandatoryProperty(UserStoreConfigConstants.dataSource, "jdbc/WSO2CarbonDB", UserStoreConfigConstants.dataSourceDescription);

        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false", UserStoreConfigConstants.disabledDescription);
        setProperty(UserStoreConfigConstants.maxUserNameListLength, "Maximum User List Length", "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "Maximum Role List Length", "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "Enable User Role Cache", "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);


//      LDAP Specific Properties
        setProperty(JDBCRealmConstants.DIGEST_FUNCTION, "Password Hashing Algorithm", "SHA-256", UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty(UserStoreConfigConstants.readGroups, "Read Groups", "true", UserStoreConfigConstants.readLDAPGroupsDescription);
        setProperty("ReadOnly", "Read-only", "false", "Indicates whether the user store of this realm operates in the user read only mode or not");
        setProperty("IsEmailUserName", "Is Email Username", "false", "Indicates whether Email is used as user name (apply when realm operates in read only mode).");
        setProperty("DomainCalculation", "Domain Calculation", "default", "Can be either default or custom (apply when realm operates in read only mode)");
        setProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS, "Enable Salted Passwords", "true", "Indicates whether to salt the password");
        setProperty(UserStoreConfigConstants.writeGroups, "Enable Write Groups", "true", UserStoreConfigConstants.writeGroupsDescription);
        setProperty("UserNameUniqueAcrossTenants", "Make Username Unique Across Tenants", "false", "An attribute used for multi-tenancy");
        setProperty("PasswordJavaRegEx", "Password RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate passwords");
        setProperty("PasswordJavaRegExViolationErrorMsg", "Password RegEx Violation(Java) Error Message", "Password length should be within 5 to 30 characters",
                "Error message when the password is not matched with PasswordJavaRegEx ");
        setProperty("PasswordJavaScriptRegEx", "Password RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression used by the font-end components for password validation");
        setProperty("UsernameJavaRegEx", "Username RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate user names");
//        setProperty("UsernameJavaRegEx","^[^~!#$;%^*+={}\\\\|\\\\\\\\&lt;&gt;,\\\'\\\"]{3,30}$","A regular expression to validate user names");
        setProperty("UsernameJavaScriptRegEx", "Username RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression used by the font-end components for username validation");
        setProperty("RolenameJavaRegEx", "Role Name RegEx (Java)", "^[\\S]{5,30}$", "A regular expression to validate role names");
//        setProperty("RolenameJavaRegEx","^[^~!#$;%^*+={}\\\\|\\\\\\\\&lt;&gt;,\\\'\\\"]{3,30}$","A regular expression to validate role names");
        setProperty("RolenameJavaScriptRegEx", "Role Name RegEx (Javascript)", "^[\\S]{5,30}$", "The regular expression used by the font-end components for role name validation");
        setProperty(UserStoreConfigConstants.SCIMEnabled, "", "false", UserStoreConfigConstants.SCIMEnabledDescription);
        setProperty(MULTI_ATTRIBUTE_SEPARATOR, "Multiple Attribute Separator", ",", MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION);

        //Advanced Properties (No descriptions added for each property)
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_SQL, "Select User SQL",JDBCRealmConstants.SELECT_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_CASE_INSENSITIVE, "Select User SQL With Case Insensitivie" +
                        " Username",
                JDBCRealmConstants.SELECT_USER_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_ROLE_LIST, "Get Role List SQL", JDBCRealmConstants.GET_ROLE_LIST_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLE_LIST, "Get Shared Role List SQP", JDBCRealmConstants.GET_SHARED_ROLE_LIST_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER, "User Filter SQL", JDBCRealmConstants.GET_USER_FILTER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER_CASE_INSENSITIVE, "User Filter SQL With Case " +
                "Insensitive Username", JDBCRealmConstants.GET_USER_FILTER_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USER_ROLE, "User Role SQL", JDBCRealmConstants.GET_USER_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USER_ROLE_CASE_INSENSITIVE, "User Role SQL With Case " +
                "Insensitive Username", JDBCRealmConstants.GET_USER_ROLE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER, "User Shared Role SQL",
                JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_CASE_INSENSITIVE, "User Shared Role SQL With" +
                " Case Insensitive Username", JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_SQL_CASE_INSENSITIVE,
                "");


        setAdvancedProperty(JDBCRealmConstants.GET_IS_ROLE_EXISTING, "Is Role Existing SQL", JDBCRealmConstants.GET_IS_ROLE_EXISTING_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_ROLE, "Get User List Of Role SQL", JDBCRealmConstants.GET_USERS_IN_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE, "Get User List Of Shared Role SQL",
                JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_SQL, "");

        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_EXISTING, "Is User Existing SQL", JDBCRealmConstants.GET_IS_USER_EXISTING_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE, "Is User Existing SQL With Case" +
                " Insensitive Username", JDBCRealmConstants.GET_IS_USER_EXISTING_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROPS_FOR_PROFILE, "Get User Properties for Profile SQL", JDBCRealmConstants.GET_PROPS_FOR_PROFILE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROPS_FOR_PROFILE_CASE_INSENSITIVE, "Get User Properties for " +
                "Profile SQL With Case Insensitive Username", JDBCRealmConstants
                .GET_PROPS_FOR_PROFILE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROP_FOR_PROFILE, "Get User Property for Profile SQL", JDBCRealmConstants.GET_PROP_FOR_PROFILE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROP_FOR_PROFILE_CASE_INSENSITIVE, "Get User Property for Profile SQL With Case Insensitive Username", JDBCRealmConstants.GET_PROP_FOR_PROFILE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_FOR_PROP, "Get User List for Property SQL", JDBCRealmConstants.GET_USERS_FOR_PROP_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROFILE_NAMES, "Get Profile Names SQL", JDBCRealmConstants.GET_PROFILE_NAMES_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER, "Get User Profile Names SQL", JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE, "Get User Profile Names " +
                "SQL With Case Insensitive Username", JDBCRealmConstants
                .GET_PROFILE_NAMES_FOR_USER_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERID_FROM_USERNAME, "Get User ID From Username SQL", JDBCRealmConstants.GET_USERID_FROM_USERNAME_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERID_FROM_USERNAME_CASE_INSENSITIVE, "Get User ID From Username " +
                "SQL With Case Insensitive Username", JDBCRealmConstants
                .GET_USERID_FROM_USERNAME_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.GET_USERNAME_FROM_TENANT_ID, "Get Username From Tenant ID SQL", JDBCRealmConstants.GET_USERNAME_FROM_TENANT_ID_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME, "Get Tenant ID From Username SQL", JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE, "Get Tenant ID From " +
                "Username SQL With Case Insensitive Username", JDBCRealmConstants
                .GET_TENANT_ID_FROM_USERNAME_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.ADD_USER, "Add User SQL", JDBCRealmConstants.ADD_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE, "Add User To Role SQL", JDBCRealmConstants.ADD_USER_TO_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE, "Add User To Role SQL With Case " +
                "Insensitive Username", JDBCRealmConstants.ADD_USER_TO_ROLE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE, "Add Role SQL", JDBCRealmConstants.ADD_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_SHARED_ROLE, "Add Shared Role SQL",JDBCRealmConstants.ADD_SHARED_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER, "Add Shared Role To User SQL",
                JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE, "Add Shared Role To User SQL" +
                " With Case Insensitive Username",JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE, "Remove User From Shared Roles SQL",
                JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_CASE_INSENSITIVE, "Remove User From Shared Roles SQL",
                JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_ROLE, "Remove User From Role SQL", JDBCRealmConstants.REMOVE_USER_FROM_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE, "Remove User From Role SQL " +
                "With Case Insensitive Username", JDBCRealmConstants.REMOVE_USER_FROM_ROLE_SQL_CASE_INSENSITIVE,
                "");

        setAdvancedProperty(JDBCRealmConstants.REMOVE_ROLE_FROM_USER, "Remove Role From User SQL",JDBCRealmConstants.REMOVE_ROLE_FROM_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE, "Remove Role From User SQL " +
                "With Case Insensitive Username", JDBCRealmConstants.REMOVE_ROLE_FROM_USER_SQL_CASE_INSENSITIVE,
                "");

        setAdvancedProperty(JDBCRealmConstants.DELETE_ROLE, "Delete Roles SQL", JDBCRealmConstants.DELETE_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE, "On Delete Role Remove User Role Mapping SQL",JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.DELETE_USER, "Delete User SQL", JDBCRealmConstants.DELETE_USER_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.DELETE_USER_CASE_INSENSITIVE, "Delete User SQL With Case Insensitive" +
                " Username", JDBCRealmConstants.DELETE_USER_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE, "On Delete User Remove User Role Mapping SQL", JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE, "On Delete User Remove User Attribute SQL", JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_CASE_INSENSITIVE, "On Delete User " +
                "Remove User Attribute SQL With Case Insensitive Username", JDBCRealmConstants
                .ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PASSWORD, "Update User Password SQL", JDBCRealmConstants.UPDATE_USER_PASSWORD_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PASSWORD_CASE_INSENSITIVE, "Update User Password SQL With " +
                "Case Insensitive Username", JDBCRealmConstants.UPDATE_USER_PASSWORD_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.UPDATE_ROLE_NAME, "Update Role Name SQL", JDBCRealmConstants.UPDATE_ROLE_NAME_SQL, "");

        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY, "Add User Property SQL", JDBCRealmConstants.ADD_USER_PROPERTY_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY, "Update User Property SQL", JDBCRealmConstants.UPDATE_USER_PROPERTY_SQL , "");
        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY_CASE_INSENSITIVE, "Update User Property SQL With " +
                "Case Insensitive Username", JDBCRealmConstants.UPDATE_USER_PROPERTY_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.DELETE_USER_PROPERTY, "Delete User Property SQL", JDBCRealmConstants.DELETE_USER_PROPERTY_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.DELETE_USER_PROPERTY_CASE_INSENSITIVE, "Delete User Property SQL With " +
                "Case Insensitive Username", JDBCRealmConstants.DELETE_USER_PROPERTY_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.USER_NAME_UNIQUE, "User Name Unique Across Tenant SQL", JDBCRealmConstants.USER_NAME_UNIQUE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.USER_NAME_UNIQUE_CASE_INSENSITIVE, "User Name Unique Across Tenant SQL" +
                " With Case Insensitive Username", JDBCRealmConstants.USER_NAME_UNIQUE_SQL_CASE_INSENSITIVE, "");

        setAdvancedProperty(JDBCRealmConstants.IS_DOMAIN_EXISTING, "Is Domain Existing SQL", JDBCRealmConstants.IS_DOMAIN_EXISTING_SQL, "");

        // mssql
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL, "Add User To Role SQL (MSSQL)", JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL, "Add Role To User SQL (MSSQL)",JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL, "Add User Property (MSSQL)", JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_MSSQL, "Add User To Role SQL With " +
                "Case Insensitive Username (MSSQL)", JDBCRealmConstants
                .ADD_USER_TO_ROLE_MSSQL_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_MSSQL, "Add Role To User SQL With " +
                "Case Insensitive Username (MSSQL)", JDBCRealmConstants
                .ADD_ROLE_TO_USER_MSSQL_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_MSSQL, "Add User Property With Case" +
                " Insensitive Username (MSSQL)", JDBCRealmConstants
                .ADD_USER_PROPERTY_MSSQL_SQL_CASE_INSENSITIVE, "");

        //openedge
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE, "Add User To Role SQL (OpenEdge)", JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE, "Add Role To User SQL (OpenEdge)", JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE, "Add User Property (OpenEdge)", JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE_SQL, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_OPENEDGE, "Add User To Role SQL With" +
                " Case Insensitive Username (OpenEdge)", JDBCRealmConstants
                .ADD_USER_TO_ROLE_OPENEDGE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_OPENEDGE, "Add Role To User SQL With" +
                " Case Insensitive Username (OpenEdge)", JDBCRealmConstants
                .ADD_ROLE_TO_USER_OPENEDGE_SQL_CASE_INSENSITIVE, "");
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_OPENEDGE, "Add User Property With " +
                "Case Insensitive Username (OpenEdge)", JDBCRealmConstants
                .ADD_USER_PROPERTY_OPENEDGE_SQL_CASE_INSENSITIVE, "");
        setProperty("UniqueID", "", "", "");
        setProperty(UserStoreConfigConstants.CASE_SENSITIVE_USERNAME, "Case Sensitive Username", "true",
                UserStoreConfigConstants.CASE_SENSITIVE_USERNAME_DESCRIPTION);
    }


    private static void setProperty(String name, String displayName, String value,
                                    String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        JDBC_UM_OPTIONAL_PROPERTIES.add(property);

    }

    private static void setMandatoryProperty(String name, String displayName, String value,
                                             String description, boolean encrypt) {
        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
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
