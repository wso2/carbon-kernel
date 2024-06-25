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
package org.wso2.carbon.user.core;

import org.wso2.carbon.user.core.ldap.LDAPConstants;

/**
 * This class contains the constants related to User Store Configurations.
 */
public class UserStoreConfigConstants {
    public static final String PRIMARY = "PRIMARY";
    public static final String DOMAIN_NAME = "DomainName";
    public static final String USER_STORES = "userstores";
    public static final String TENANTS = "tenants";
    public static final String RESOLVE_USER_ID_FROM_USER_NAME_CACHE_NAME = "user_id_from_user_name_cache";
    public static final String RESOLVE_USER_NAME_FROM_USER_ID_CACHE_NAME = "user_name_from_user_id_cache";
    public static final String RESOLVE_USER_UNIQUE_ID_FROM_USER_NAME_CACHE_NAME = "user_unique_id_from_user_name_cache";
    public static final String RESOLVE_USER_NAME_FROM_UNIQUE_USER_ID_CACHE_NAME = "user_name_from_unique_user_id_cache";

    public static final String RESOLVE_GROUP_NAME_FROM_USER_ID_CACHE_NAME = "group_name_from_user_id_cache";

    //Define datasource property for JDBC
    public static final String dataSource = "dataSource";
    public static final String dataSourceDescription = "Connection name to user store";
    //Common Properties
    public static final String maxRoleNameListLength = "MaxRoleNameListLength";
    public static final String maxRoleNameListLengthDescription = "Maximum number of groups retrieved at once";
    public static final String maxUserNameListLength = "MaxUserNameListLength";
    public static final String maxUserNameListLengthDescription = "Maximum number of users retrieved at once";
    public static final String userRolesCacheEnabled = "UserRolesCacheEnabled";
    public static final String userRolesCacheEnabledDescription = "This is to indicate whether to cache the group "
            + "list of a user";
    public static final String SCIMEnabledDescription = "Whether SCIM is enabled for the user store";
    public static final String claimOperationsSupported = "ClaimOperationsSupported";
    public static final String claimOperationsSupportedDescription = "Whether the userstore supports claim read and write";
    public static final String getClaimOperationsSupportedDisplayName = "Claim Operations Supported";
    @Deprecated
    public static final String enableMaxUserLimitForSCIM = "EnableMaxUserLimitForSCIM";
    @Deprecated
    public static final String enableMaxUserLimitForSCIMDescription = "Whether to include max user limit during SCIM " +
            "user list operation";
    @Deprecated
    public static final String enableMaxUserLimitDisplayName = "Enable MaxUserLimit For SCIM";
    public static final String immutableAttributes = "ImmutableAttributes";
    public static final String immutableAttributesDescription = "Comma-separated list of user store maintained " +
            "immutable attributes";
    public static final String immutableAttributesDisplayName = "Immutable Attributes";
    public static final String timestampAttributes = "TimestampAttributes";
    public static final String timestampAttributesDescription = "Comma-separated list of user store attributes " +
            "having the data type of Timestamp and may require a conversion when reading from/writing to user store";
    public static final String dateAndTimePattern = "DateAndTimePattern";
    public static final String dateAndTimePatternDescription = "Patterns of Date and Time values are stored in the " +
            "userstore separated by brackets";
    public static final String dateAndTimePatternDisplayName = "Date And Time Pattern";
    public static final String timestampAttributesDisplayName = "Timestamp Attributes";

    //Mandatory to LDAP user stores
    public static final String connectionURL = "ConnectionURL";
    public static final String connectionURLDescription = "Connection URL for the user store";
    public static final String connectionName = "ConnectionName";
    public static final String connectionNameDescription = "This should be a DN (Distinguish Name) of a user with " +
                                                           "sufficient permissions to perform operations on users " +
                                                           "and groups in LDAP";
    public static final String connectionPassword = "ConnectionPassword";
    public static final String connectionPasswordDescription = "Password of the admin user";
    public static final String userSearchBase = "UserSearchBase";
    public static final String userSearchBaseDescription = "DN of the context under which user entries are stored in LDAP";
    public static final String disabled = "Disabled";
    public static final String disabledDescription = "Whether user store is disabled";
    public static final String referral = "Referral";
    public static final String referralDisplayName = "Referral";
    public static final String referralDescription = "Guides the requests to a domain controller in the correct domain";

    //Write Group Privilege Properties
    public static final String writeGroups = "WriteGroups";
    public static final String writeGroupsDescription = "Indicate whether write groups enabled";
    public static final String userEntryObjectClass = "UserEntryObjectClass";
    public static final String userEntryObjectClassDescription = "Object Class used to construct user entries";
    public static final String passwordJavaScriptRegEx = "PasswordJavaScriptRegEx";
    public static final String passwordJavaScriptRegExDescription = "Policy that defines the password format";
    public static final String usernameJavaScriptRegEx = "UserNameJavaScriptRegEx";
    public static final String usernameJavaScriptRegExDescription = "The regular expression used by the front-end components for username validation";
    public static final String usernameJavaRegEx = "UserNameJavaRegEx";
    public static final String usernameJavaRegExDescription = "A regular expression to validate user names";
    public static final String roleNameJavaScriptRegEx = "RoleNameJavaScriptRegEx";
    public static final String roleNameJavaScriptRegExDescription = "The regular expression used by the front-end "
            + "components for group name validation";
    public static final String roleNameJavaRegEx = "RoleNameJavaRegEx";
    public static final String roleNameJavaRegExDescription = "A regular expression to validate group names";
    public static final String groupEntryObjectClass = "GroupEntryObjectClass";
    public static final String groupEntryObjectClassDescription = "Object Class used to construct group entries";
    public static final String emptyRolesAllowed = "EmptyRolesAllowed";
    public static final String emptyRolesAllowedDescription = "Specifies whether the underlying user store allows "
            + "empty groups to be added";

    //LDAP Specific Properties
    public static final String passwordHashMethod = "PasswordHashMethod";
    public static final String passwordHashMethodDescription = "Password Hash method to use when storing user entries";
    public static final String usernameListFilter = "UserNameListFilter";
    public static final String usernameListFilterDescription = "Filtering criteria for listing all the user entries in LDAP";
    public static final String usernameSearchFilter = "UserNameSearchFilter";
    public static final String usernameSearchFilterDescription = "Filtering criteria for searching a particular user entry";
    public static final String userIdSearchFilter = "UserIdSearchFilter";
    public static final String userIdSearchFilterAttributeName = "User ID Search Filter";
    public static final String userIdSearchFilterDescription = "Filtering criteria for searching a particular user " +
            "entry";

    // Group id related userstore config property values.
    public static final String GROUP_ID_ENABLED = "GroupIDEnabled";
    public static final String GROUP_ID_ENABLED_DISPLAY_NAME = "Enable Group Unique Id";
    public static final String GROUP_ID_ENABLED_DESCRIPTION = "Enable userstore managed groups";
    public static final String GROUP_ID_ATTRIBUTE = "GroupIdAttribute";
    public static final String GROUP_ID_ATTRIBUTE_DISPLAY_NAME = "Group Id Attribute";
    public static final String GROUP_ID_ATTRIBUTE_DESCRIPTION = "Attribute used to uniquely identify group";
    public static final String GROUP_CREATED_DATE_ATTRIBUTE = "GroupCreatedDateAttribute";
    public static final String GROUP_CREATED_DATE_ATTRIBUTE_DISPLAY_NAME = "Group Created Date Attribute";
    public static final String GROUP_CREATED_DATE_ATTRIBUTE_DESCRIPTION = "Attribute used to store created date of " +
            "the group";
    public static final String GROUP_LAST_MODIFIED_DATE_ATTRIBUTE = "GroupLastModifiedDateAttribute";
    public static final String GROUP_LAST_MODIFIED_DATE_ATTRIBUTE_DISPLAY_NAME = "Group Last Modified Date Attribute";
    public static final String GROUP_LAST_MODIFIED_DATE_ATTRIBUTE_DESCRIPTION = "Attribute used to store the last " +
            "modified date of the group";
    public static final String GROUP_LOCATION_URI_ATTRIBUTE = "GroupLocation";

    public static final String userNameAttribute = "UserNameAttribute";
    public static final String userNameAttributeDescription = "Attribute used for uniquely identifying a user entry. Users can be authenticated using their email address, uid and etc";
    public static final String userIdAttribute = "UserIDAttribute";
    public static final String userIdAttributeName = "User ID Attribute";
    public static final String userIdAttributeDescription = "Attribute used for uniquely identifying a user entry.";
    public static final String readGroups = "ReadGroups";
    public static final String readLDAPGroupsDescription = "Specifies whether groups should be read from LDAP";
    public static final String groupSearchBase = "GroupSearchBase";
    public static final String groupSearchBaseDescription = "DN of the context under which user entries are stored in LDAP";
    public static final String groupNameListFilter = "GroupNameListFilter";
    public static final String groupNameListFilterDescription = "Filtering criteria for listing all the group entries in LDAP";
    public static final String groupNameAttribute = "GroupNameAttribute";
    public static final String groupNameAttributeDescription = "Attribute used for uniquely identifying a user entry";
    public static final String groupNameSearchFilter = "GroupNameSearchFilter";
    public static final String groupNameSearchFilterDescription = "Filtering criteria for searching a particular group entry";
    public static final String membershipAttribute = "MembershipAttribute";
    public static final String membershipAttributeDescription = "Attribute used to define members of LDAP groups";
    public static final String memberOfAttribute = LDAPConstants.MEMBEROF_ATTRIBUTE;
    public static final String memberOfAttributeDescription = "Attribute used to define groups of a LDAP User";
    public static final String userDNPattern = "UserDNPattern";
    public static final String userDNPatternDescription = "The patten for user's DN. It can be defined to improve the LDAP search";
    public static final String connectionPoolingEnabled = "ConnectionPoolingEnabled";
    public static final String connectionPoolingEnabledDescription = "Set this property to enable LDAP connection " +
            "pooling.";
    public static final String lDAPInitialContextFactory = "LDAPInitialContextFactory";
    public static final String lDAPInitialContextFactoryDescription = "The property to set LDAP Initial Context Factory";
    // Property to enable TLS connection with LDAP server using StartTLS extended operation.
    public static final String STARTTLS_ENABLED = "StartTLSEnabled";
    public static final String STARTTLS_ENABLED_DISPLAY_NAME = "Enable StartTLS";
    public static final String STARTTLS_ENABLED_DESCRIPTION = "Enable secure connection by using " +
            "StartTLS extended operation in LDAP";
    public static final String SSLCertificateValidationEnabled = "SSLCertificateValidationEnabled";
    public static final String SSLCertificateValidationEnabledDescription = "Set/Unset this property to enable/disable " +
            "certificate validation for LDAPS connections";

    // Property to specify waiting time to re-establish LDAP connection after couple of failure attempts.
    public static final String CONNECTION_RETRY_DELAY = "ConnectionRetryDelay";
    public static final String CONNECTION_RETRY_DELAY_DISPLAY_NAME = "Connection Retry Delay";
    public static final String CONNECTION_RETRY_DELAY_DESCRIPTION = "Specifies waiting time in milliseconds"
            + " inorder to establish the connection after couple of failure attempts.";
    public static final int DEFAULT_CONNECTION_RETRY_DELAY_IN_MILLISECONDS = 120000;
    public static final String MAX_CONNECTION_RETRY_DELAY_IN_MILLISECONDS =
            "UserStore.maxConnectionRetryDelayInMilliSeconds";

    public static final String OBJECT_GUID = "objectGuid";

    public static final String CONNECTION_RETRY_COUNT = "ConnectionRetryCount";
    public static final int DEFAULT_CONNECTION_RETRY_COUNT = 2;
    public static final String MAX_CONNECTION_RETRY_COUNT = "UserStore.maxConnectionRetryCount";
    public static final String CONNECTION_RETRY_COUNT_DISPLAY_NAME = "Connection Retry Count";
    public static final String CONNECTION_RETRY_COUNT_DESCRIPTION = "Specifies connection retry times"
            + " inorder to re-establish the connection on failure";

    // Property for specify case insensitivity for User stores.
    public static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";
    public static final String CASE_INSENSITIVE_USERNAME_DESCRIPTION = "Whether the username is case sensitive or not";
    public static final String CASE_INSENSITIVE_ATTRIBUTES = "CaseInsensitiveAttributes";
    public static final String CASE_INSENSITIVE_ATTRIBUTES_DESCRIPTION = "Comma-separated list of user store maintained " +
            "case insensitive attributes";

    // Property for specify whether case-sensitive username can be used as the cache key.
    public static final String USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS = "UseCaseSensitiveUsernameForCacheKeys";
    public static final String USE_CASE_SENSITIVE_USERNAME_FOR_CACHE_KEYS_DESCRIPTION =
            "Whether case-sensitive username can be used as the cache key or not.";

    // Property to bypass account locking for the userstore.
    public static final String BYPASS_ACCOUNT_LOCK = "BypassAccountLock";
    public static final String BYPASS_ACCOUNT_LOCK_DESCRIPTION = "Enable bypass account locking for the userstore";

    // Property to specify attributes that need to skip multi-valued attribute separation.
    public static final String singleValuedAttributes = "SingleValuedAttributes";
    public static final String singleValuedAttributesDescription = "Comma-separated list of attributes that need to " +
            "skip multi-valued attribute separation";
    public static final String singleValuedAttributesDisplayName = "Single Valued Attributes";

}
