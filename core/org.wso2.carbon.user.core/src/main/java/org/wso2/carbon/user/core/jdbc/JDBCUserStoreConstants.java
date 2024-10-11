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
import org.wso2.carbon.user.core.jdbc.caseinsensitive.JDBCCaseInsensitiveConstants;

import java.util.ArrayList;

import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.BASIC;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.CONNECTION;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.GROUP;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.USER;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataImportance.FALSE;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataImportance.TRUE;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.BOOLEAN;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.NUMBER;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.PASSWORD;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.SQL;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.STRING;

/**
 * This class contains the constants related to the JDBC User Store Manager.
 */
public class JDBCUserStoreConstants {

    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> JDBC_UM_MANDATORY_PROPERTIES = new ArrayList<>();
    public static final ArrayList<Property> JDBC_UM_OPTIONAL_PROPERTIES = new ArrayList<>();
    public static final ArrayList<Property> JDBC_UM_ADVANCED_PROPERTIES = new ArrayList<>();
    private static final String usernameJavaRegExViolationErrorMsg = "UsernameJavaRegExViolationErrorMsg";
    private static final String usernameJavaRegExViolationErrorMsgDescription = "Error message when the Username is "
            + "not matched with UsernameJavaRegEx";
    private static final String passwordJavaRegExViolationErrorMsg = "PasswordJavaRegExViolationErrorMsg";
    private static final String passwordJavaRegExViolationErrorMsgDescription = "Error message when the Password is "
            + "not matched with passwordJavaRegEx";
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION = "This is the separator for multiple claim "
            + "values";
    private static final String VALIDATION_INTERVAL = "validationInterval";
    private static final String DISPLAY_NAME_ATTRIBUTE_DESCRIPTION = "This is the attribute name to display as the " +
            "display name of the user";
    public static final String DISPLAY_NAME_ATTRIBUTE = "DisplayNameAttribute";
    public static final String STORE_USER_ATTRIBUTE_VALUE_AS_UNICODE = "StoreUserAttributeValueAsUnicode";

    static {

        //setMandatoryProperty
        setMandatoryProperty(JDBCRealmConstants.URL, "Connection URL", "", "URL of the user store database", false,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(JDBCRealmConstants.USER_NAME, "Connection Name", "", "Username for the database", false,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(JDBCRealmConstants.PASSWORD, "Connection Password", "", "Password for the database", true,
                new Property[] { CONNECTION.getProperty(), PASSWORD.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(JDBCRealmConstants.DRIVER_NAME, "Driver Name", "", "Full qualified driver name", false,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        //set optional properties
        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false",
                UserStoreConfigConstants.disabledDescription,
                new Property[] { BASIC.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty("ReadOnly", "Read-only", "false",
                "Indicates whether the user store of this realm operates in the user read only mode or not",
                new Property[] { BASIC.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty(DISPLAY_NAME_ATTRIBUTE, "Display Name", "", DISPLAY_NAME_ATTRIBUTE_DESCRIPTION,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.readGroups, "Read Groups", "true",
                UserStoreConfigConstants.readLDAPGroupsDescription,
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.writeGroups, "Write Groups", "true",
                UserStoreConfigConstants.writeGroupsDescription,
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty("UsernameJavaRegEx", "Username RegEx (Java)", "[a-zA-Z0-9._\\-|//]{3,30}$",
                "A regular expression to validate user names",
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty("UsernameJavaScriptRegEx", "Username RegEx (Javascript)", "[a-zA-Z0-9._\\-|//]{3,30}$",
                "The regular expression used by the font-end components for username validation",
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(usernameJavaRegExViolationErrorMsg, "Username RegEx Violation Error Message",
                "Username pattern policy violated.", usernameJavaRegExViolationErrorMsgDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty("PasswordJavaRegEx", "Password RegEx (Java)", "^[\\S]{5,30}$",
                "A regular expression to validate passwords",
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty("PasswordJavaScriptRegEx", "Password RegEx (Javascript)", "^[\\S]{5,30}$",
                "The regular expression used by the font-end components for password validation",
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(passwordJavaRegExViolationErrorMsg, "Password RegEx Violation Error Message",
                "Password pattern policy violated", passwordJavaRegExViolationErrorMsgDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty("RolenameJavaRegEx", "Group Name RegEx (Java)", "[a-zA-Z0-9._\\-|//]{3,30}$",
                "A regular expression to validate group names",
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty("RolenameJavaScriptRegEx", "Group Name RegEx (Javascript)", "^[\\S]{5,30}$",
                "The regular expression used by the font-end components for group name validation",
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME, "Case Insensitive Username", "false",
                UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME_DESCRIPTION,
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });
        setProperty(UserStoreConfigConstants.CASE_INSENSITIVE_ATTRIBUTES, "Case Insensitive Attributes", "false",
                UserStoreConfigConstants.CASE_INSENSITIVE_ATTRIBUTES_DESCRIPTION,
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });
        setProperty(UserStoreConfigConstants.USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS,
                "Use Case Sensitive Username for Cache Keys", "true",
                UserStoreConfigConstants.USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS_DESCRIPTION,
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });

        //set Advanced properties
        setAdvancedProperty("IsBulkImportSupported", "Is Bulk Import Supported", "false",
                "Support Bulk User Import Operation for this user store",
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.DIGEST_FUNCTION, "Password Hashing Algorithm", "SHA-256",
                UserStoreConfigConstants.passwordHashMethodDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.HASHING_ALGORITHM_PROPERTIES,
                "UserStore Hashing Configurations", "{}",
                "Configurations for UserStore Hashing in JSON format",
                new Property[]{USER.getProperty(), STRING.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(MULTI_ATTRIBUTE_SEPARATOR, "Multiple Attribute Separator", ",",
                MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.STORE_SALTED_PASSWORDS, "Enable Salted Passwords", "true",
                "Indicates whether to salt the password",
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.MAX_ACTIVE, "Maximum Active Connections", "40", "The maximum number " +
                        "of active connections that can be allocated from the connection pool at the same time",
                new Property[]{USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.MIN_IDLE, "Minimum Idle Connections", "5", "The minimum number of " +
                        "connections that can remain idle in the pool, without extra ones being created",
                new Property[]{USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.MAX_IDLE, "Maximum Idle Connections", "6", "The maximum number of " +
                        "connections that can remain idle in the pool",
                new Property[]{USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.MAX_WAIT, "Maximum Wait Time for Requests", "60000", "The maximum time" +
                        " that requests are expected to wait in the queue for a connection to be released",
                new Property[]{USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.TEST_WHILE_IDLE, "Enable Test While Idle", "false", "The indication of" +
                        " whether connections will be validated when they remain idle",
                new Property[]{USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.TIME_BETWEEN_EVICTION_RUNS_MILLIS, "Time Between Eviction Runs", "5000",
                "The number of milliseconds to sleep between runs of the idle connection validation/cleaner thread",
                new Property[]{USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.MIN_EVIC_TABLE_IDLE_TIME_MILLIS, "Minimum Idle Time Before Eviction",
                "60000", "The minimum amount of time an object may sit idle in the pool before it is eligible for " +
                        "eviction", new Property[]{USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(UserStoreConfigConstants.maxUserNameListLength, "Maximum User List Length", "100",
                UserStoreConfigConstants.maxUserNameListLengthDescription,
                new Property[] { USER.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(UserStoreConfigConstants.maxRoleNameListLength, "Maximum Group List Length", "100",
                UserStoreConfigConstants.maxRoleNameListLengthDescription,
                new Property[] { GROUP.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(UserStoreConfigConstants.GROUP_ID_ENABLED, UserStoreConfigConstants.GROUP_ID_ENABLED_DISPLAY_NAME, Boolean.toString(true),
                UserStoreConfigConstants.GROUP_ID_ENABLED_DESCRIPTION,
                new Property[]{GROUP.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty()});

        setAdvancedProperty(UserStoreConfigConstants.userRolesCacheEnabled, "Enable User Group Cache", "true",
                UserStoreConfigConstants.userRolesCacheEnabledDescription,
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty("UserNameUniqueAcrossTenants", "Make Username Unique Across Tenants", "false",
                "An attribute used for multi-tenancy",
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.VALIDATION_QUERY, "Validation Query for the Database", "",
                "validationQuery is the SQL query that will be used to validate connections. This query MUST be an "
                        + "SQL SELECT statement that returns at least one row",
                new Property[] { CONNECTION.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(VALIDATION_INTERVAL, "Validation Interval(milliseconds)", "",
                "Used to avoid excess validation, only run validation at most at this frequency",
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.DEFAULT_AUTO_COMMIT, "Default Auto-Commit", "",
                "The default auto-commit state of connections created by this pool",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        /* Added for circuit breaker implementation. */
        setAdvancedProperty(UserStoreConfigConstants.CONNECTION_RETRY_COUNT, UserStoreConfigConstants
                        .CONNECTION_RETRY_COUNT_DISPLAY_NAME, String.valueOf(UserStoreConfigConstants
                        .DEFAULT_CONNECTION_RETRY_COUNT), UserStoreConfigConstants.CONNECTION_RETRY_COUNT_DESCRIPTION,
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(UserStoreConfigConstants.CONNECTION_RETRY_DELAY, UserStoreConfigConstants.
                CONNECTION_RETRY_DELAY_DISPLAY_NAME, String.valueOf(UserStoreConfigConstants
                .DEFAULT_CONNECTION_RETRY_DELAY_IN_MILLISECONDS), UserStoreConfigConstants
                .CONNECTION_RETRY_DELAY_DESCRIPTION, new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(),
                FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.DEFAULT_READ_ONLY, "Default Read Only", "",
                "The default read-only state of connections created by this pool",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.DEFAULT_TRANSACTION_ISOLATION, "Default Transaction Isolation", "",
                "The default TransactionIsolation state of connections created by this pool",
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.DEFAULT_CATALOG, "Default Catalog", "",
                "The default catalog of connections created by this pool",
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.INITIAL_SIZE, "Initial Size", "",
                "The initial number of connections that are created when the pool is started",
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.TEST_ON_RETURN, "Test On Return", "false",
                "The indication of whether objects will be validated before being returned to the pool",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.TEST_ON_BORROW, "Test On Borrow", "false",
                "The indication of whether objects will be validated before being borrowed from the pool",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.VALIDATOR_CLASS_NAME, "Validator Class Name", "",
                "The name of a class which implements the org.apache.tomcat.jdbc.pool.Validator interface and "
                        + "provides a no-arg constructor (may be implicit)",
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.NUM_TESTS_PER_EVICTION_RUN, "Num Tests Per Eviction Run", "",
                " Property not used in tomcat-jdbc-pool",
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.ACCESS_TO_UNDERLYING_CONNECTION_ALLOWED,
                "Access To Underlying " + "Connection Allowed", "",
                "Property not used. Access can be achieved by calling unwrap on the pooled connection",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.REMOVE_ABANDONED, "Remove Abandoned", "false",
                "Flag to remove abandoned connections if they exceed the removeAbandonedTimeout",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.REMOVE_ABANDONED_TIMEOUT, "Remove Abandoned Timeout", "",
                "Timeout in seconds before an abandoned(in use) connection can be removed",
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.LOG_ABANDONED, "Log Abandoned", "false",
                "Flag to log stack traces for application code which abandoned a Connection",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.CONNECTION_PROPERTIES, "Connection Properties", "",
                "The connection properties that will be sent to our JDBC driver when establishing new connections",
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.JDBC_INTERCEPTORS, "JDBC Interceptors", "", "JDBC Interceptors",
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.JMX_ENABLED, "JMX Enabled", "true", "Register the pool with JMX or not",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.FAIR_QUEUE, "Fair Queue", "true",
                "Set to true if you wish that calls to getConnection should be treated fairly in a true FIFO fashion",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.ABANDON_WHEN_PERCENTAGE_FULL, "Abandon When Percentage Full", "",
                "Connections that have been abandoned (timed out) wont get closed and reported up unless the number"
                        + " of connections in use are above the percentage defined by abandonWhenPercentageFull",
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.MAX_AGE, "Max Age", "", "Time in milliseconds to keep the connection",
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.USE_EQUALS, "Use Equals", "true",
                "Set to true if you wish the ProxyConnection class to use String.equals and set to false when you "
                        + "wish to use == when comparing method names",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.SUSPECT_TIMEOUT, "Suspect Timeout", "",
                "Similar to to the removeAbandonedTimeout value but instead of treating the connection as "
                        + "abandoned, and potentially closing the connection, this simply logs the warning if "
                        + "logAbandoned is set to true",
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.VALIDATION_QUERY_TIMEOUT, "Validation Query Timeout", "",
                "The timeout in seconds before a connection validation queries fail",
                new Property[] { CONNECTION.getProperty(), NUMBER.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.ALTERNATE_USERNAME_ALLOWED, "Alternate Username Allowed", "false",
                "If enabled, the pool size is still managed on a global level, and not on a per schema level",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.COMMIT_ON_RETURN, "Commit On Return", "false",
                "If autoCommit==false then the pool can complete the transaction by calling commit on the "
                        + "connection as it is returned to the pool If rollbackOnReturn==true then this attribute is "
                        + "ignored",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.ROLLBACK_ON_RETURN, "Rollback On Return", "false",
                "If autoCommit==false then the pool can terminate the transaction by calling rollback on the "
                        + "connection as it is returned to the pool",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setAdvancedProperty("CountRetrieverClass", "Count Implementation",
                "org.wso2.carbon.identity.user.store.count.jdbc.JDBCUserStoreCountRetriever",
                "Name of the class that implements the count functionality",
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(STORE_USER_ATTRIBUTE_VALUE_AS_UNICODE, "Store User Attribute Value As Unicode", "false",
                "Store user attribute value as unicode",
                new Property[] { CONNECTION.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        //Advanced Properties (No descriptions added for each property)
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER, "Select User SQL", JDBCRealmConstants.SELECT_USER_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_NAME, "Select Username SQL",
                JDBCRealmConstants.SELECT_USER_NAME_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_ID, "Select User ID SQL",
                JDBCRealmConstants.SELECT_USER_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_WITH_ID, "Select User ID SQL",
                JDBCRealmConstants.SELECT_USER_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.SELECT_USER_CASE_INSENSITIVE,
                "Select User SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.SELECT_USER_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.SELECT_USER_NAME_CASE_INSENSITIVE,
                "Select User Name SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.SELECT_USER_NAME_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.SELECT_USER_WITH_ID_CASE_INSENSITIVE,
                "Select User With ID SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.SELECT_USER_WITH_ID_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_ID_FROM_USER_NAME, "Select User ID From Username SQL",
                JDBCRealmConstants.SELECT_USER_ID_FROM_USER_NAME_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_NAME_FROM_USER_ID, "Select Username From User ID SQL",
                JDBCRealmConstants.SELECT_USER_NAME_FROM_USER_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.SELECT_USER_ID_FROM_USER_NAME_CASE_INSENSITIVE,
                "Select User ID From Username SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.SELECT_USER_ID_FROM_USER_NAME_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty("GetRoleListSQL", "Get Group List SQL",
                "SELECT UM_ROLE_NAME, UM_TENANT_ID, UM_SHARED_ROLE FROM UM_ROLE WHERE "
                        + "UM_ROLE_NAME LIKE ? AND UM_TENANT_ID=? AND UM_SHARED_ROLE ='0' ORDER BY UM_ROLE_NAME", "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLE_LIST, "Get Shared Group List SQL",
                JDBCRealmConstants.GET_SHARED_ROLE_LIST_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLE_LIST_H2, "Get Shared Group List SQL from H2",
                JDBCRealmConstants.GET_SHARED_ROLE_LIST_SQL_H2, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER, "User Filter SQL",
                JDBCRealmConstants.GET_USER_FILTER_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER_WITH_ID, "User ID Filter SQL",
                JDBCRealmConstants.GET_USER_FILTER_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER_WITH_ID_WITH_ESCAPE, "User ID Filter SQL With Escape",
                JDBCRealmConstants.GET_USER_FILTER_WITH_ID_WITH_ESCAPE_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USER_FILTER_WITH_ESCAPE, "User Filter SQL With Escape",
                JDBCRealmConstants.GET_USER_FILTER_SQL_WITH_ESCAPE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE,
                "User Filter SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_USER_FILTER_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE,
                "User Filter With ID SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_CASE_INSENSITIVE_WITH_ESCAPE,
                "User Filter With ID SQL Case Insensitive Username With Escape",
                JDBCCaseInsensitiveConstants.GET_USER_FILTER_WITH_ID_SQL_CASE_INSENSITIVE_WITH_ESCAPE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_FILTER_CASE_INSENSITIVE_WITH_ESCAPE,
                "User Filter SQL With Case Insensitive Username With Escape",
                JDBCCaseInsensitiveConstants.GET_USER_FILTER_SQL_CASE_INSENSITIVE_WITH_ESCAPE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USER_ROLE, "User Group SQL", JDBCRealmConstants.GET_USER_ROLE_SQL,
                "", new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USER_ROLE_WITH_ID, "User Group With ID SQL",
                JDBCRealmConstants.GET_USER_ROLE_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_ROLE_EXIST, "User Group Exist SQL",
                JDBCRealmConstants.GET_IS_USER_ROLE_EXIST_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_ROLE_EXIST_WITH_ID, "User Group Exist With ID SQL",
                JDBCRealmConstants.GET_IS_USER_ROLE_EXIST_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USER_ROLE_CASE_INSENSITIVE,
                "User Group SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_USER_ROLE_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_IS_USER_ROLE_EXIST_CASE_INSENSITIVE,
                "User Group Exist SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_USER_ROLE_EXIST_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER, "User Shared Role SQL",
                JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_WITH_ID, "User Shared Role With ID SQL",
                JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_SHARED_ROLES_FOR_USER_CASE_INSENSITIVE,
                "User Shared Role SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_SHARED_ROLES_FOR_USER_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.GET_IS_ROLE_EXISTING, "Is Group Existing SQL",
                JDBCRealmConstants.GET_IS_ROLE_EXISTING_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_ROLE, "Get User List Of Group SQL",
                JDBCRealmConstants.GET_USERS_IN_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_ROLE_FILTER, "Get User List Of Group Filter SQL",
                JDBCRealmConstants.GET_USERS_IN_ROLE_FILTER_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_ROLE_FILTER_WITH_ID,
                "Get User List Of Group Filter SQL With ID", JDBCRealmConstants.GET_USERS_IN_ROLE_FILTER_WITH_ID_SQL,
                "", new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE, "Get User List Of Shared Role SQL",
                JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_FILTER,
                "Get User List Of Shared Role Filter SQL", JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_FILTER_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_FILTER_WITH_ID,
                "Get User List Of Shared Role Filter With ID SQL",
                JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_FILTER_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_EXISTING, "Is User Existing SQL",
                JDBCRealmConstants.GET_IS_USER_EXISTING_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_EXISTING_WITH_ID, "Is User Existing SQL With ID",
                JDBCRealmConstants.GET_IS_USER_EXISTING_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_IS_USER_NAME_EXISTING, "Is Username Existing SQL",
                JDBCRealmConstants.GET_IS_USER_NAME_EXISTING_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE,
                "Is User Existing SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_IS_USER_EXISTING_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_IS_USER_EXISTING_CASE_INSENSITIVE,
                "Is Username Existing SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_IS_USER_NAME_EXISTING_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_PROPS_FOR_PROFILE, "Get User Properties for Profile SQL",
                JDBCRealmConstants.GET_PROPS_FOR_PROFILE_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_PROPS_FOR_PROFILE_WITH_ID,
                "Get User Properties for Profile SQL With ID", JDBCRealmConstants.GET_PROPS_FOR_PROFILE_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_PROPS_FOR_PROFILE_CASE_INSENSITIVE,
                "Get User Properties for Profile SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_PROPS_FOR_PROFILE_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_PROP_FOR_PROFILE, "Get User Property for Profile SQL",
                JDBCRealmConstants.GET_PROP_FOR_PROFILE_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_PROP_FOR_PROFILE_WITH_ID,
                "Get User Property for Profile With ID SQL", JDBCRealmConstants.GET_PROP_FOR_PROFILE_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_PROP_FOR_PROFILE_CASE_INSENSITIVE,
                "Get User Property for Profile SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_PROP_FOR_PROFILE_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_FOR_PROP, "Get User List for Property SQL",
                JDBCRealmConstants.GET_USERS_FOR_PROP_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_FOR_PROP_WITH_ESCAPE, "Get User List for Property SQL With " +
                "Escape", JDBCRealmConstants.GET_USERS_FOR_PROP_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_FOR_CLAIM_VALUE, "Get User List for Claim Value SQL",
                JDBCRealmConstants.GET_USERS_FOR_CLAIM_VALUE_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_FOR_PROP_WITH_ID, "Get User List for Property With ID SQL",
                JDBCRealmConstants.GET_USERS_FOR_PROP_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERS_FOR_CLAIM_VALUE_WITH_ID,
                "Get User List for Claim Value With ID SQL",
                JDBCRealmConstants.GET_USERS_FOR_CLAIM_VALUE_WITH_ID_SQL, "",
                new Property[]{USER.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USERS_FOR_PROP_WITH_ID_CASE_INSENSITIVE,
                "Get User List For Property With ID SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_USERS_FOR_PROP_WITH_ID_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USERS_FOR_CLAIM_VALUE_WITH_ID_CASE_INSENSITIVE,
                "Get User List For Claim Value With ID SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_USERS_FOR_CLAIM_VALUE_WITH_ID_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_PROFILE_NAMES, "Get Profile Names SQL",
                JDBCRealmConstants.GET_PROFILE_NAMES_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER, "Get User Profile Names SQL",
                JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_WITH_ID, "Get User Profile Names SQL With ID",
                JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_CASE_INSENSITIVE,
                "Get User Profile Names SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_PROFILE_NAMES_FOR_USER_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERID_FROM_USERNAME, "Get User ID From Username SQL",
                JDBCRealmConstants.GET_USERID_FROM_USERNAME_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERID_FROM_USERNAME_WITH_ID,
                "Get User ID From Username SQL With ID", JDBCRealmConstants.GET_USERID_FROM_USERNAME_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_USERID_FROM_USERNAME_CASE_INSENSITIVE,
                "Get User ID From Username SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_USERID_FROM_USERNAME_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_USERNAME_FROM_TENANT_ID, "Get Username From Tenant ID SQL",
                JDBCRealmConstants.GET_USERNAME_FROM_TENANT_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME, "Get Tenant ID From Username SQL",
                JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_CASE_INSENSITIVE,
                "Get Tenant ID From Username SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.GET_TENANT_ID_FROM_USERNAME_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.ADD_USER, "Add User SQL", JDBCRealmConstants.ADD_USER_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_WITH_ID, "Add User With ID SQL",
                JDBCRealmConstants.ADD_USER_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE, "Add User To Group SQL",
                JDBCRealmConstants.ADD_USER_TO_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_WITH_ID, "Add User To Group With ID SQL",
                JDBCRealmConstants.ADD_USER_TO_ROLE_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE,
                "Add User To Group SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE, "Add Group SQL", JDBCRealmConstants.ADD_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_SHARED_ROLE, "Add Shared Role SQL",
                JDBCRealmConstants.ADD_SHARED_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER, "Add Group To User SQL",
                JDBCRealmConstants.ADD_ROLE_TO_USER_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_WITH_ID, "Add Group To User With ID SQL",
                JDBCRealmConstants.ADD_ROLE_TO_USER_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER, "Add Shared Role To User SQL",
                JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_WITH_ID, "Add Shared Role To User With ID SQL",
                JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_CASE_INSENSITIVE,
                "Add Shared Role To User SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.ADD_SHARED_ROLE_TO_USER_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE, "Remove User From Shared Roles SQL",
                JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_WITH_ID,
                "Remove User From Shared Roles With ID SQL",
                JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE,
                "Remove User From Role SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_ROLE, "Remove User From Group SQL",
                JDBCRealmConstants.REMOVE_USER_FROM_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.REMOVE_USER_FROM_ROLE_WITH_ID, "Remove User From Group With ID SQL",
                JDBCRealmConstants.REMOVE_USER_FROM_ROLE_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_CASE_INSENSITIVE,
                "Remove Use From Group SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.REMOVE_USER_FROM_ROLE_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.REMOVE_ROLE_FROM_USER, "Remove Group From User SQL",
                JDBCRealmConstants.REMOVE_ROLE_FROM_USER_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.REMOVE_ROLE_FROM_USER_WITH_ID, "Remove Group From User With ID SQL",
                JDBCRealmConstants.REMOVE_ROLE_FROM_USER_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_CASE_INSENSITIVE,
                "Remove Group From User SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.REMOVE_ROLE_FROM_USER_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.DELETE_ROLE, "Delete Group SQL", JDBCRealmConstants.DELETE_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE,
                "On Delete Group, Remove User Group Mapping SQL",
                JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty("DeleteUserSQL", "Delete User SQL",
                "DELETE FROM UM_USER WHERE UM_USER_NAME = ? AND UM_TENANT_ID=?", "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.DELETE_USER_CASE_INSENSITIVE,
                "Delete User SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.DELETE_USER_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE,
                "On Delete User, Remove User Group Mapping SQL", JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_SQL,
                "", new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_WITH_ID,
                "On Delete User, Remove User Group Mapping SQL With ID",
                JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_WITH_ID_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.COUNT_USERS_WITH_CLAIM, "Count Users With Claim SQL",
                JDBCRealmConstants.COUNT_USERS_WITH_CLAIM_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.COUNT_INTERNAL_ROLES, "Count Internal Roles SQL",
                JDBCRealmConstants.COUNT_INTERNAL_ROLES_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.COUNT_APPLICATION_ROLES, "Count Application Roles SQL",
                JDBCRealmConstants.COUNT_APPLICATION_ROLES_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.COUNT_ROLES, "Count Groups SQL", JDBCRealmConstants.COUNT_ROLES_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.COUNT_USERS, "Count Users SQL", JDBCRealmConstants.COUNT_USERS_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE,
                "On Delete User, Remove User Attribute SQL", JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.COUNT_USERS_WITH_FILTER, "Count Users SQL With Filter",
                JDBCRealmConstants.COUNT_USERS_WITH_FILTER_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_WITH_ID,
                "On Delete User, Remove User Attribute SQL With ID",
                JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.DELETE_USER_WITH_ID, "Delete User With ID",
                JDBCRealmConstants.DELETE_USER_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_CASE_INSENSITIVE,
                "On Delete User, Remove User Attribute SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PASSWORD, "Update User Password SQL",
                JDBCRealmConstants.UPDATE_USER_PASSWORD_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PASSWORD_WITH_ID, "Update User Password With ID SQL",
                JDBCRealmConstants.UPDATE_USER_PASSWORD_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.UPDATE_USER_PASSWORD_CASE_INSENSITIVE,
                "Update User Password SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.UPDATE_USER_PASSWORD_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.UPDATE_ROLE_NAME, "Update Group Name SQL",
                JDBCRealmConstants.UPDATE_ROLE_NAME_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.GET_GROUP_ID_FROM_GROUP_NAME, "Get Group ID From Group Name SQL",
                JDBCRealmConstants.GET_GROUP_ID_FROM_GROUP_NAME_SQL, "",
                new Property[]{GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.GET_GROUP_NAME_FROM_GROUP_ID, "Get Group Name From Group ID SQL",
                JDBCRealmConstants.GET_GROUP_NAME_FROM_GROUP_ID_SQL, "",
                new Property[]{GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.GET_GROUP_FROM_GROUP_NAME, "Get Group From Group Name SQL",
                JDBCRealmConstants.GET_GROUP_FROM_GROUP_NAME_SQL, "",
                new Property[]{GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.GET_GROUP_FROM_GROUP_ID, "Get Group From Group ID SQL",
                JDBCRealmConstants.GET_GROUP_FROM_GROUP_ID_SQL, "",
                new Property[]{GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.ADD_GROUP, "Add Group SQL",
                JDBCRealmConstants.ADD_GROUP_SQL, "",
                new Property[]{GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty()});
        setAdvancedProperty(JDBCRealmConstants.UPDATE_GROUP_NAME, "Update Group Name SQL",
                JDBCRealmConstants.UPDATE_GROUP_NAME_SQL, "",
                new Property[]{GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty()});

        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY, "Add User Property SQL",
                JDBCRealmConstants.ADD_USER_PROPERTY_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_WITH_ID, "Add User Property With ID SQL",
                JDBCRealmConstants.ADD_USER_PROPERTY_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY, "Update User Property SQL",
                JDBCRealmConstants.UPDATE_USER_PROPERTY_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID, "Update User Property With ID SQL",
                JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED, "Update User Property" +
                        " With ID Optimized SQL",
                JDBCRealmConstants.UPDATE_USER_PROPERTY_WITH_ID_OPTIMIZED_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_PROPERTIES_WITH_ID, "Select User Properties With ID SQL",
                JDBCRealmConstants.SELECT_USER_PROPERTIES_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.SELECT_USER_PROPERTIES_WITH_ID_OPTIMIZED, "Select User " +
                        "Properties With ID  Optimized SQL",
                JDBCRealmConstants.SELECT_USER_PROPERTIES_WITH_ID_OPTIMIZED_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.UPDATE_USER_PROPERTY_CASE_INSENSITIVE,
                "Update User Property SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.UPDATE_USER_PROPERTY_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.DELETE_USER_PROPERTY, "Delete User Property SQL",
                JDBCRealmConstants.DELETE_USER_PROPERTY_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.DELETE_USER_PROPERTY_WITH_ID, "Delete User Property With ID SQL",
                JDBCRealmConstants.DELETE_USER_PROPERTY_WITH_ID_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.DELETE_USER_PROPERTY_CASE_INSENSITIVE,
                "Delete User Property SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.DELETE_USER_PROPERTY_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.USER_NAME_UNIQUE, "User Name Unique Across Tenant SQL",
                JDBCRealmConstants.USER_NAME_UNIQUE_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.USER_ID_UNIQUE_WITH_ID, "User ID Unique Across Tenant SQL With ID",
                JDBCRealmConstants.USER_ID_UNIQUE_SQL_WITH_ID, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.USER_NAME_UNIQUE_WITH_ID, "User Name Unique Across Tenant SQL With ID",
                JDBCRealmConstants.USER_NAME_UNIQUE_SQL_WITH_ID, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_CASE_INSENSITIVE,
                "User Name Unique Across Tenant SQL With Case Insensitive Username",
                JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_CASE_INSENSITIVE_WITH_ID,
                "User Name Unique Across Tenant SQL With Case Insensitive Username With ID",
                JDBCCaseInsensitiveConstants.USER_NAME_UNIQUE_SQL_CASE_INSENSITIVE_WITH_ID, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        setAdvancedProperty(JDBCRealmConstants.IS_DOMAIN_EXISTING, "Is Domain Existing SQL",
                JDBCRealmConstants.IS_DOMAIN_EXISTING_SQL, "",
                new Property[] { CONNECTION.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_DOMAIN, "Add Domain SQL", JDBCRealmConstants.ADD_DOMAIN_SQL, "",
                new Property[] { CONNECTION.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        // mssql
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL, "Add User To Group SQL (MSSQL)",
                JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL, "Add Group To User SQL (MSSQL)",
                JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL, "Add User Property (MSSQL)",
                JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_MSSQL,
                "Add User To Group SQL With Case Insensitive Username (MSSQL)",
                JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_MSSQL_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_MSSQL,
                "Add Group To User SQL With Case Insensitive Username (MSSQL)",
                JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_MSSQL_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_MSSQL,
                "Add User Property With Case Insensitive Username (MSSQL)",
                JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_MSSQL_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });

        //openedge
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE, "Add User To Group SQL (OpenEdge)",
                JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE, "Add Group To User SQL (OpenEdge)",
                JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE_SQL, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE, "Add User Property (OpenEdge)",
                JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE_SQL, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_CASE_INSENSITIVE_OPENEDGE,
                "Add User To Group SQL With Case Insensitive Username (OpenEdge)",
                JDBCCaseInsensitiveConstants.ADD_USER_TO_ROLE_OPENEDGE_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_CASE_INSENSITIVE_OPENEDGE,
                "Add Group To User SQL With Case Insensitive Username (OpenEdge)",
                JDBCCaseInsensitiveConstants.ADD_ROLE_TO_USER_OPENEDGE_SQL_CASE_INSENSITIVE, "",
                new Property[] { GROUP.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_CASE_INSENSITIVE_OPENEDGE,
                "Add User Property With Case Insensitive Username (OpenEdge)",
                JDBCCaseInsensitiveConstants.ADD_USER_PROPERTY_OPENEDGE_SQL_CASE_INSENSITIVE, "",
                new Property[] { USER.getProperty(), SQL.getProperty(), FALSE.getProperty() });
        setAdvancedProperty(UserStoreConfigConstants.claimOperationsSupported,
                UserStoreConfigConstants.getClaimOperationsSupportedDisplayName, "true",
                UserStoreConfigConstants.claimOperationsSupportedDescription,
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });
        setProperty("UniqueID", "", "", "",
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
    }

    private static void setProperty(String name, String displayName, String value, String description,
            Property[] childProperties) {

        Property property = new Property(name, value, displayName + "#" + description, childProperties);
        JDBC_UM_OPTIONAL_PROPERTIES.add(property);
    }

    private static void setMandatoryProperty(String name, String displayName, String value, String description,
            boolean encrypt, Property[] childProperties) {

        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, childProperties);
        JDBC_UM_MANDATORY_PROPERTIES.add(property);
    }

    private static void setAdvancedProperty(String name, String displayName, String value, String description,
            Property[] childProperties) {

        Property property = new Property(name, value, displayName + "#" + description, childProperties);
        JDBC_UM_ADVANCED_PROPERTIES.add(property);
    }

}
