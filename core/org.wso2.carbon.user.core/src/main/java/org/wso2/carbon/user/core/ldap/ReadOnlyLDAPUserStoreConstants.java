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

import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.BASIC;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.CONNECTION;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.GROUP;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataCategory.USER;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataImportance.FALSE;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataImportance.TRUE;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.BOOLEAN;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.PASSWORD;
import static org.wso2.carbon.user.core.constants.UserStoreUIConstants.DataTypes.STRING;
import static org.wso2.carbon.user.core.ldap.LDAPConstants.DEFAULT_LDAP_TIME_FORMATS_PATTERN;

public class ReadOnlyLDAPUserStoreConstants {

    //Properties for Read Write LDAP User Store Manager
    public static final ArrayList<Property> ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> UNIQUE_ID_ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTIONAL_ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();

    //For multiple attribute separation
    private static final String MULTI_ATTRIBUTE_SEPARATOR = "MultiAttributeSeparator";
    private static final String MULTI_ATTRIBUTE_SEPARATOR_DESCRIPTION = "This is the separator for multiple claim values";
    private static final String DisplayNameAttributeDescription = "Attribute name to display as the Display Name";
    private static final String DisplayNameAttribute = "DisplayNameAttribute";
    private static final String roleDNPattern = "RoleDNPattern";
    private static final String roleDNPatternDescription =
            "The patten for groups's DN. It can be defined to improve " + "the LDAP search";

    static {

        setMandatoryProperty(UserStoreConfigConstants.connectionURL, "Connection URL", "ldap://",
                UserStoreConfigConstants.connectionURLDescription, false,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(UserStoreConfigConstants.connectionName, "Connection Name", "uid=," + "ou=",
                UserStoreConfigConstants.connectionNameDescription, false,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword, "Connection Password", "",
                UserStoreConfigConstants.connectionPasswordDescription, true,
                new Property[] { CONNECTION.getProperty(), PASSWORD.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase, "User Search Base",
                "ou=Users,dc=wso2,dc=org", UserStoreConfigConstants.userSearchBaseDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "Username Attribute", "uid",
                UserStoreConfigConstants.userNameAttributeDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter, "User Search Filter",
                "(&(objectClass=person)(uid=?))", UserStoreConfigConstants.usernameSearchFilterDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "User List Filter", "(objectClass=person)",
                UserStoreConfigConstants.usernameListFilterDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryPropertyForUniqueIdStore(UserStoreConfigConstants.userIdAttribute,
                UserStoreConfigConstants.userIdAttributeName, "scimId",
                UserStoreConfigConstants.userIdAttributeDescription,false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryPropertyForUniqueIdStore(UserStoreConfigConstants.userIdSearchFilter,
                UserStoreConfigConstants.userIdSearchFilterAttributeName, "(&(objectClass=person)(scimId=?))",
                UserStoreConfigConstants.userIdSearchFilterDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        // Group Id Related Userstore Configurations - By default this will be disabled.
        setMandatoryPropertyForUniqueIdStore(UserStoreConfigConstants.GROUP_ID_ENABLED,
                UserStoreConfigConstants.GROUP_ID_ENABLED_DISPLAY_NAME, Boolean.toString(false),
                UserStoreConfigConstants.GROUP_ID_ENABLED_DESCRIPTION, false,
                new Property[]{GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty()});
        setMandatoryPropertyForUniqueIdStore(UserStoreConfigConstants.GROUP_ID_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_ID_ATTRIBUTE_DISPLAY_NAME, LDAPConstants.DEFAULT_GROUP_ID_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_ID_ATTRIBUTE_DESCRIPTION, false,
                new Property[]{GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty()});

        setProperty(UserStoreConfigConstants.userDNPattern, "User DN Pattern", "",
                UserStoreConfigConstants.userDNPatternDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty(DisplayNameAttribute, "Display name attribute", "", DisplayNameAttributeDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false",
                UserStoreConfigConstants.disabledDescription,
                new Property[] { BASIC.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.readGroups, "Read Groups", "true",
                UserStoreConfigConstants.readLDAPGroupsDescription,
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupSearchBase, "Group Search Base", "ou=Groups,dc=wso2,dc=org",
                UserStoreConfigConstants.groupSearchBaseDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupNameAttribute, "Group Name Attribute", "cn",
                UserStoreConfigConstants.groupNameAttributeDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupNameSearchFilter, "Group Search Filter",
                "(&(objectClass=groupOfNames)(cn=?))", UserStoreConfigConstants.groupNameSearchFilterDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupNameListFilter, "Group List Filter", "(objectClass=groupOfNames)",
                UserStoreConfigConstants.groupNameListFilterDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.GROUP_CREATED_DATE_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_CREATED_DATE_ATTRIBUTE_DISPLAY_NAME,
                LDAPConstants.DEFAULT_GROUP_CREATED_DATE_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_CREATED_DATE_ATTRIBUTE_DESCRIPTION,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.GROUP_LAST_MODIFIED_DATE_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_LAST_MODIFIED_DATE_ATTRIBUTE_DISPLAY_NAME,
                LDAPConstants.DEFAULT_GROUP_LAST_MODIFIED_DATE_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_LAST_MODIFIED_DATE_ATTRIBUTE_DESCRIPTION,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setProperty(roleDNPattern, "Group DN Pattern", "", roleDNPatternDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setProperty(UserStoreConfigConstants.membershipAttribute, "Membership Attribute", "member",
                UserStoreConfigConstants.membershipAttributeDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty(UserStoreConfigConstants.memberOfAttribute, "Member Of Attribute", "",
                UserStoreConfigConstants.memberOfAttribute,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty("BackLinksEnabled", "Enable Back Links", "false",
                " Whether to allow attributes to be result from references to the object from other objects",
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });

        setProperty("Referral", "Referral", "ignore",
                "Guides the requests to a domain controller in the correct domain",
                new Property[] { GROUP.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty("ReplaceEscapeCharactersAtUserLogin", "Enable Escape Characters at User Login", "true",
                "Whether replace escape character when user login",
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });
        setProperty("UniqueID", "", "", "",
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty(UserStoreConfigConstants.lDAPInitialContextFactory, "LDAP Initial Context Factory",
                "com.sun.jndi.ldap.LdapCtxFactory", UserStoreConfigConstants.lDAPInitialContextFactoryDescription,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty(UserStoreConfigConstants.dateAndTimePattern, UserStoreConfigConstants.dateAndTimePatternDisplayName,
                DEFAULT_LDAP_TIME_FORMATS_PATTERN, UserStoreConfigConstants.dateAndTimePatternDescription,
                new Property[]{CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty()});
        setProperty(UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME, "Case Insensitive Username", "true",
                UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME_DESCRIPTION,
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
    }

    private static void setMandatoryProperty(String name, String displayName, String value, String description,
            boolean encrypt, Property[] childProperties) {

        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, childProperties);
        ROLDAP_USERSTORE_PROPERTIES.add(property);
    }

    private static void setMandatoryPropertyForUniqueIdStore(String name, String displayName, String value,
            String description, boolean encrypt, Property[] childProperties) {

        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, childProperties);
        UNIQUE_ID_ROLDAP_USERSTORE_PROPERTIES.add(property);
    }

    private static void setProperty(String name, String displayName, String value, String description,
            Property[] childProperties) {

        Property property = new Property(name, value, displayName + "#" + description, childProperties);
        OPTIONAL_ROLDAP_USERSTORE_PROPERTIES.add(property);
    }
}
