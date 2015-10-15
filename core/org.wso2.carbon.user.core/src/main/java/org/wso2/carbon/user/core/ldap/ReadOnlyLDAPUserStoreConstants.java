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
package org.wso2.carbon.user.core.ldap;


import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreConfigConstants;

import java.util.ArrayList;

public class ReadOnlyLDAPUserStoreConstants {


    //Properties for Read Write LDAP User Store Manager
    public static final ArrayList<Property> ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTIONAL_ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> RO_LDAP_UM_ADVANCED_PROPERTIES = new ArrayList<Property>();


    //For multiple attribute separation
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION = "This is the separator for multiple claim values";

    static {
        setMandatoryProperty(UserStoreConfigConstants.connectionURL, "Connection URL", "ldap://",
                UserStoreConfigConstants.connectionURLDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.connectionName, "Connection Name", "uid=," +
                "ou=", UserStoreConfigConstants.connectionNameDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword, "Connection Password",
                "", UserStoreConfigConstants.connectionPasswordDescription, true);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase, "User Search Based",
                "ou=Users,dc=wso2,dc=org", UserStoreConfigConstants.userSearchBaseDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.userEntryObjectClass,
                "User Entry Object Class", "wso2Person", UserStoreConfigConstants
                        .userEntryObjectClassDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "Username Attribute",
                "uid", UserStoreConfigConstants.userNameAttributeDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter, "User Search Filter",
                "(&amp;(objectClass=person)(uid=?))", UserStoreConfigConstants
                        .usernameSearchFilterDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "User List Filter",
                "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription, false);

        setProperty(UserStoreConfigConstants.userDNPattern, "User DN Pattern", "", UserStoreConfigConstants.userDNPatternDescription);
        setProperty(UserStoreConfigConstants.displayNameAttribute, "Display name attribute", "",
                UserStoreConfigConstants.displayNameAttributeDescription);
        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false", UserStoreConfigConstants.disabledDescription);

        setProperty(UserStoreConfigConstants.readGroups, "Read Groups", "true", UserStoreConfigConstants
                .readLDAPGroupsDescription);
        setProperty(UserStoreConfigConstants.writeGroups, "Write Groups", "true", UserStoreConfigConstants.writeGroupsDescription);
        setProperty(UserStoreConfigConstants.groupSearchBase, "Group Search Base", "ou=Groups,dc=wso2,dc=org",
                UserStoreConfigConstants.groupSearchBaseDescription);
        setProperty(UserStoreConfigConstants.groupEntryObjectClass, "Group Entry Object Class", "groupOfNames",
                UserStoreConfigConstants.groupEntryObjectClassDescription);
        setProperty(UserStoreConfigConstants.groupNameAttribute, "Group Name Attribute", "cn",
                UserStoreConfigConstants.groupNameAttributeDescription);
        setProperty(UserStoreConfigConstants.groupNameSearchFilter, "Group Search Filter", "(&amp;(objectClass=groupOfNames)(cn=?))",
                UserStoreConfigConstants.groupNameSearchFilterDescription);
        setProperty(UserStoreConfigConstants.groupNameListFilter, "Group List Filter", "(objectClass=groupOfNames))",
                UserStoreConfigConstants.groupNameListFilterDescription);
        setProperty(UserStoreConfigConstants.roleDNPattern, "Role DN Pattern", "", UserStoreConfigConstants
                .roleDNPatternDescription);

        setProperty(UserStoreConfigConstants.membershipAttribute, "Membership Attribute", "member", UserStoreConfigConstants.membershipAttributeDescription);
        setProperty(UserStoreConfigConstants.memberOfAttribute, "Member Of Attribute", "", UserStoreConfigConstants.memberOfAttribute);
        setProperty("BackLinksEnabled", "Enable Back Links", "false", " Whether to allow attributes to be result from" +
                "references to the object from other objects");

        setProperty(UserStoreConfigConstants.usernameJavaRegEx, "Username RegEx (Java)", "[a-zA-Z0-9._-|//]{3,30}$", UserStoreConfigConstants.usernameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaScriptRegEx, "Username RegEx (Javascript)", "^[\\S]{3,30}$", UserStoreConfigConstants.usernameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaRegExViolationErrorMsg, "Username RegEx Violation Error " +
                        "Message","Username pattern policy violated.", UserStoreConfigConstants.usernameJavaRegExViolationErrorMsgDescription);
        setProperty(UserStoreConfigConstants.passwordJavaRegEx, "Password RegEx (Java)", "^[\\S]{5,30}$",
                UserStoreConfigConstants.passwordJavaRegExDescription);
        setProperty(UserStoreConfigConstants.passwordJavaScriptRegEx, "Password RegEx (Javascript)", "^[\\S]{5,30}$",
                UserStoreConfigConstants.passwordJavaScriptRegExDescription);
        setProperty(UserStoreConfigConstants.passwordJavaRegExViolationErrorMsg, "Password RegEx Violation Error " +
                "Message", "Password pattern policy violated.", UserStoreConfigConstants.passwordJavaRegExViolationErrorMsgDescription);
        setProperty(UserStoreConfigConstants.roleNameJavaRegEx, "Role Name RegEx (Java)", "[a-zA-Z0-9._-|//]{3,30}$", UserStoreConfigConstants.roleNameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.roleNameJavaScriptRegEx, "Role Name RegEx (Javascript)", "^[\\S]{3,30}$", UserStoreConfigConstants.roleNameJavaScriptRegExDescription);
        setProperty("UniqueID", "", "", "");

        setAdvancedProperty(UserStoreConfigConstants.SCIMEnabled, "Enable SCIM", "false", UserStoreConfigConstants
                .SCIMEnabledDescription);

        setAdvancedProperty(UserStoreConfigConstants.BULK_IMPORT_SUPPORT, "Bulk Import Support", "true", "Bulk Import" +
                " Supported");
        setAdvancedProperty(UserStoreConfigConstants.emptyRolesAllowed, "Allow Empty Roles", "true", UserStoreConfigConstants
                .emptyRolesAllowedDescription);


        setAdvancedProperty(UserStoreConfigConstants.passwordHashMethod, "Password Hashing Algorithm", "PLAIN_TEXT",
                UserStoreConfigConstants.passwordHashMethodDescription);
        setAdvancedProperty(MULTI_ATTRIBUTE_SEPARATOR, "Multiple Attribute Separator", ",", MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION);


        setAdvancedProperty(UserStoreConfigConstants.maxUserNameListLength, "Maximum User List Length", "100", UserStoreConfigConstants
                .maxUserNameListLengthDescription);
        setAdvancedProperty(UserStoreConfigConstants.maxRoleNameListLength, "Maximum Role List Length", "100", UserStoreConfigConstants
                .maxRoleNameListLengthDescription);

        setAdvancedProperty(UserStoreConfigConstants.userRolesCacheEnabled, "Enable User Role Cache", "true", UserStoreConfigConstants
                .userRolesCacheEnabledDescription);

        setAdvancedProperty(UserStoreConfigConstants.connectionPoolingEnabled, "Enable LDAP Connection Pooling", "false",
                UserStoreConfigConstants.connectionPoolingEnabledDescription);

        setAdvancedProperty(UserStoreConfigConstants.LDAPConnectionTimeout, "LDAP Connection Timeout", "5000",
                UserStoreConfigConstants.LDAPConnectionTimeoutDescription);
        setAdvancedProperty(UserStoreConfigConstants.readTimeout, "LDAP Read Timeout", "5000", UserStoreConfigConstants
                .readTimeoutDescription);
        setAdvancedProperty(UserCoreConstants.RealmConfig.RETRY_ATTEMPTS, "Retry Attempts", "0", "Number of retries for" +
                " authentication in case ldap read timed out.");    }

    private static void setMandatoryProperty(String name, String displayName, String value,
                                             String description, boolean encrypt) {
        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, null);
        ROLDAP_USERSTORE_PROPERTIES.add(property);

    }

    private static void setProperty(String name, String displayName, String value,
                                    String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        OPTIONAL_ROLDAP_USERSTORE_PROPERTIES.add(property);

    }

    private static void setAdvancedProperty(String name, String displayName, String value,
                                            String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        RO_LDAP_UM_ADVANCED_PROPERTIES.add(property);

    }


}
