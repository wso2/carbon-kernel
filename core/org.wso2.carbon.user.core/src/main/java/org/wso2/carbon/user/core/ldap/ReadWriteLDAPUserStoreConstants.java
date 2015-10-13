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
    public static final ArrayList<Property> RW_LDAP_UM_ADVANCED_PROPERTIES = new ArrayList<Property>();


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
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase, "User Search Base",
                "ou=system", UserStoreConfigConstants.userSearchBaseDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "Username Attribute",
                "uid", UserStoreConfigConstants.userNameAttributeDescription, false);

        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter, "User Search Filter",
                "(&amp;(objectClass=person)(uid=?))", UserStoreConfigConstants
                        .usernameSearchFilterDescription, false);
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "User List Filter",
                "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription, false);


        setProperty(UserStoreConfigConstants.userDNPattern, "User DN Pattern", "", UserStoreConfigConstants.userDNPatternDescription);
        setProperty(UserStoreConfigConstants.displayNameAttribute, "Display name attribute", "uid",
                UserStoreConfigConstants.displayNameAttributeDescription);
        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false", UserStoreConfigConstants.disabledDescription);
        setProperty(UserStoreConfigConstants.readGroups, "Read Groups", "false", UserStoreConfigConstants
                .readLDAPGroupsDescription);
        setProperty(UserStoreConfigConstants.groupSearchBase, "Group Search Base", "ou=Groups,dc=wso2,dc=org",
                UserStoreConfigConstants.groupSearchBaseDescription);
        setProperty(UserStoreConfigConstants.groupNameAttribute, "Group Name Attribute", "cn", UserStoreConfigConstants.groupNameAttributeDescription);
        setProperty(UserStoreConfigConstants.groupNameSearchFilter, "Group Search Filter",
                "(&amp;(objectClass=groupOfNames)(cn=?))", UserStoreConfigConstants.groupNameSearchFilterDescription);
        setProperty(UserStoreConfigConstants.groupNameListFilter, "Group List Filter", "(objectClass=groupOfNames)",
                UserStoreConfigConstants.groupNameListFilterDescription);

        setProperty(UserStoreConfigConstants.roleDNPattern, "Role DN Pattern", "", UserStoreConfigConstants
                .roleDNPatternDescription);

        setProperty(UserStoreConfigConstants.membershipAttribute, "Membership Attribute", "member", UserStoreConfigConstants.membershipAttributeDescription);
        setProperty(UserStoreConfigConstants.memberOfAttribute, "Member Of Attribute", "", UserStoreConfigConstants.memberOfAttribute);
        setProperty("BackLinksEnabled", "Enable Back Links", "false", " Whether to allow attributes to be result from" +
                "references to the object from other objects");

        setProperty("ReplaceEscapeCharactersAtUserLogin", "Enable Escape Characters at User Login", "true", "Whether replace escape character when user login");
        setProperty("UniqueID", "", "", "");

        setAdvancedProperty(UserStoreConfigConstants.SCIMEnabled, "Enable SCIM", "false", UserStoreConfigConstants
                .SCIMEnabledDescription);

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


    private static void setAdvancedProperty(String name, String displayName, String value,
                                            String description) {
        Property property = new Property(name, value, displayName + "#" + description, null);
        RW_LDAP_UM_ADVANCED_PROPERTIES.add(property);

    }

}
