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
import org.wso2.carbon.user.core.UserStoreConfigConstants;

import java.util.ArrayList;

public class ReadWriteLDAPUserStoreConstants {


    //Properties for Read Write LDAP User Store Manager
    public static final ArrayList<Property> RWLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTINAL_RWLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();

    //For multiple attribute separation
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION = "This is the separator for multiple claim values";
    private static final String DisplayNameAttributeDescription = "Attribute name to display as the Display Name";
    private static final String DisplayNameAttribute = "DisplayNameAttribute";

    static {
        setMandatoryProperty(UserStoreConfigConstants.connectionName, "Connection Name", "uid=," +
                "ou=", UserStoreConfigConstants.connectionNameDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.connectionURL, "Connection URL", "ldap://",
                UserStoreConfigConstants.connectionURLDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword, "Connection Password",
                "", UserStoreConfigConstants.connectionPasswordDescription, true);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase, "User Search Based",
                "ou=Users,dc=wso2,dc=org", UserStoreConfigConstants.userSearchBaseDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "User Object Class",
                "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "Username Attribute",
                "uid", UserStoreConfigConstants.userNameAttributeDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter, "User Search Filter",
                "(&amp;(objectClass=person)(uid=?))", UserStoreConfigConstants
                        .usernameSearchFilterDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.userEntryObjectClass,
                "User Entry Object Class", "wso2Person", UserStoreConfigConstants
                        .userEntryObjectClassDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.groupEntryObjectClass,
                "Group Entry Object Class", "groupOfNames", UserStoreConfigConstants
                        .groupEntryObjectClassDescription, false);

        setProperty(UserStoreConfigConstants.maxUserNameListLength, "Maximum User List Length", "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "Maximum Role List Length", "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "Enable User Role Cache", "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);
        setProperty(UserStoreConfigConstants.SCIMEnabled, "SCIM Enabled", "false", UserStoreConfigConstants.SCIMEnabledDescription);
        setProperty(DisplayNameAttribute, "Display name attribute", "uid", DisplayNameAttributeDescription);
        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false", UserStoreConfigConstants.disabledDescription);
        setProperty(MULTI_ATTRIBUTE_SEPARATOR, "Multiple Attribute Separator", ",", MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION);
        setProperty(UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME, "Case Insensitive Username", "false",
                UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME_DESCRIPTION);

        Property readLDAPGroups = new Property(UserStoreConfigConstants.readGroups, "true", "Enable Read Groups#" + UserStoreConfigConstants.readLDAPGroupsDescription, null);
        //Mandatory only if readGroups is enabled
        Property groupSearchBase = new Property(UserStoreConfigConstants.groupSearchBase, "ou=Groups,dc=wso2,dc=org", "Group Search Base#" + UserStoreConfigConstants.groupSearchBaseDescription, null);
        Property groupNameListFilter = new Property(UserStoreConfigConstants.groupNameListFilter, "(objectClass=groupOfNames)", "Group Object Class#" + UserStoreConfigConstants.groupNameListFilterDescription, null);
        Property groupNameAttribute = new Property(UserStoreConfigConstants.groupNameAttribute, "cn", "Group Name Attribute#" + UserStoreConfigConstants.groupNameAttributeDescription, null);
        Property membershipAttribute = new Property(UserStoreConfigConstants.membershipAttribute, "member", "Membership Attribute#" + UserStoreConfigConstants.membershipAttributeDescription, null);
        Property groupNameSearchFilter = new Property(UserStoreConfigConstants.groupNameSearchFilter, "(&amp;(objectClass=groupOfNames)(cn=?))"
                , "Group Search Filter#" + UserStoreConfigConstants.groupNameSearchFilterDescription, null);
//        readLDAPGroups.setChildProperties(new Property[]{groupSearchBase,groupNameListFilter,groupNameAttribute,membershipAttribute,groupNameSearchFilter});

        RWLDAP_USERSTORE_PROPERTIES.add(readLDAPGroups);
        RWLDAP_USERSTORE_PROPERTIES.add(groupSearchBase);
        RWLDAP_USERSTORE_PROPERTIES.add(groupNameAttribute);
        RWLDAP_USERSTORE_PROPERTIES.add(groupNameListFilter);
        RWLDAP_USERSTORE_PROPERTIES.add(membershipAttribute);
        RWLDAP_USERSTORE_PROPERTIES.add(groupNameSearchFilter);


//      LDAP Specific Properties
        setProperty(UserStoreConfigConstants.passwordHashMethod, "Password Hashing Algorithm", "SHA", UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty(UserStoreConfigConstants.userDNPattern, "User DN Pattern", "", UserStoreConfigConstants.userDNPatternDescription);
        setProperty(UserStoreConfigConstants.passwordJavaScriptRegEx, "Password RegEx (Javascript)", "^[\\S]{5,30}$", UserStoreConfigConstants.passwordJavaScriptRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaScriptRegEx, "Username RegEx (Javascript)", "^[\\S]{3,30}$", UserStoreConfigConstants.usernameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaRegEx, "Username RegEx (Java)", "[a-zA-Z0-9._-|//]{3,30}$", UserStoreConfigConstants.usernameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.roleNameJavaScriptRegEx, "Role Name RegEx (Javascript)", "^[\\S]{3,30}$", UserStoreConfigConstants.roleNameJavaScriptRegExDescription);
        setProperty(UserStoreConfigConstants.roleNameJavaRegEx, "Role Name RegEx (Java)", "[a-zA-Z0-9._-|//]{3,30}$", UserStoreConfigConstants.roleNameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.writeGroups, "Enable Write Groups", "true", UserStoreConfigConstants.writeGroupsDescription);
        setProperty(UserStoreConfigConstants.emptyRolesAllowed, "Allow Empty Roles", "true", UserStoreConfigConstants.emptyRolesAllowedDescription);
        setProperty(UserStoreConfigConstants.memberOfAttribute, "Member Of Attribute", "", UserStoreConfigConstants.memberOfAttribute);
        setProperty(UserStoreConfigConstants.connectionPoolingEnabled, "Enable LDAP Connection Pooling", "false",
                UserStoreConfigConstants.connectionPoolingEnabledDescription);
        setProperty("UniqueID", "", "", "");
        setProperty(UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME, "Case Insensitive Username", "false", UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME_DESCRIPTION);
    }

    private static void setMandatoryProperty(String name, String displayName, String value,
                                             String description, boolean encrypt) {
        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, null);
        RWLDAP_USERSTORE_PROPERTIES.add(property);

    }

    private static void setProperty(String name, String displayName, String value,
                                    String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        OPTINAL_RWLDAP_USERSTORE_PROPERTIES.add(property);

    }

}
