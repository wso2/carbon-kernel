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

public class ActiveDirectoryUserStoreConstants {

    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> ACTIVE_DIRECTORY_UM_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> UNIQUE_ID_ACTIVE_DIRECTORY_UM_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES = new ArrayList<Property>();
    public static final String TRANSFORM_OBJECTGUID_TO_UUID = "transformObjectGUIDToUUID";
    public static final String TRANSFORM_OBJECTGUID_TO_UUID_DESC = "Return objectGUID in UUID Canonical Format";
    //For multiple attribute separation
    private static final String DisplayNameAttributeDescription = "Attribute name to display as the Display Name";
    private static final String DisplayNameAttribute = "DisplayNameAttribute";
    private static final String usernameJavaRegExViolationErrorMsg = "UsernameJavaRegExViolationErrorMsg";
    private static final String usernameJavaRegExViolationErrorMsgDescription =
            "Error message when the Username is not " + "matched with UsernameJavaRegEx";
    private static final String passwordJavaRegEx = "PasswordJavaRegEx";
    private static final String passwordJavaRegExViolationErrorMsg = "PasswordJavaRegExViolationErrorMsg";
    private static final String passwordJavaRegExViolationErrorMsgDescription =
            "Error message when the Password is " + "not matched with passwordJavaRegEx";
    private static final String passwordJavaRegExDescription = "Policy that defines the password format in backend";
    private static final String roleDNPattern = "RoleDNPattern";
    private static final String roleDNPatternDescription =
            "The patten for group's DN. It can be defined to improve the LDAP search";

    static {
        //Set mandatory properties
        setMandatoryProperty(UserStoreConfigConstants.connectionURL, "Connection URL", "ldaps://",
                UserStoreConfigConstants.connectionURLDescription, false,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setMandatoryProperty(UserStoreConfigConstants.connectionName, "Connection Name", "CN=,DC=",
                UserStoreConfigConstants.connectionNameDescription, false,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setMandatoryProperty(UserStoreConfigConstants.connectionPassword, "Connection Password", "",
                UserStoreConfigConstants.connectionPasswordDescription, true,
                new Property[] { CONNECTION.getProperty(), PASSWORD.getProperty(), TRUE.getProperty() });

        setMandatoryProperty(UserStoreConfigConstants.userSearchBase, "User Search Base", "CN=Users,DC=WSO2,DC=Com",
                UserStoreConfigConstants.userSearchBaseDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setMandatoryProperty(UserStoreConfigConstants.userEntryObjectClass, "User Entry Object Class", "user",
                UserStoreConfigConstants.userEntryObjectClassDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "Username Attribute", "cn",
                UserStoreConfigConstants.userNameAttributeDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter, "User Search Filter",
                "(&(objectClass=user)(cn=?))", UserStoreConfigConstants.usernameSearchFilterDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "User List Filter", "(objectClass=person)",
                UserStoreConfigConstants.usernameListFilterDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        //Set optional properties

        setProperty(UserStoreConfigConstants.userDNPattern, "User DN Pattern", "",
                UserStoreConfigConstants.userDNPatternDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty(DisplayNameAttribute, "Display name attribute", "", DisplayNameAttributeDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setProperty(UserStoreConfigConstants.disabled, "Disabled", "false",
                UserStoreConfigConstants.disabledDescription,
                new Property[] { BASIC.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME, "Case Insensitive Username", "true",
                UserStoreConfigConstants.CASE_INSENSITIVE_USERNAME_DESCRIPTION,
                new Property[] { USER.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });

        Property readLDAPGroups = new Property(UserStoreConfigConstants.readGroups, "true",
                "Read Groups#" + UserStoreConfigConstants.readLDAPGroupsDescription,
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        //Mandatory only if readGroups is enabled
        Property groupSearchBase = new Property(UserStoreConfigConstants.groupSearchBase, "CN=Users,DC=WSO2,DC=Com",
                "Group Search Base#" + UserStoreConfigConstants.groupSearchBaseDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        Property groupNameListFilter = new Property(UserStoreConfigConstants.groupNameListFilter,
                "(objectcategory=group)", "Group Filter#" + UserStoreConfigConstants.groupNameListFilterDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        Property groupNameAttribute = new Property(UserStoreConfigConstants.groupNameAttribute, "cn",
                "Group Name Attribute#" + UserStoreConfigConstants.groupNameAttributeDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        Property membershipAttribute = new Property(UserStoreConfigConstants.membershipAttribute, "member",
                "Membership Attribute#" + UserStoreConfigConstants.membershipAttributeDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        Property groupNameSearchFilter = new Property(UserStoreConfigConstants.groupNameSearchFilter,
                "(&(objectClass=group)(cn=?))",
                "Group Search Filter#" + UserStoreConfigConstants.groupNameSearchFilterDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        readLDAPGroups.setChildProperties(new Property[] {
                groupSearchBase, groupNameAttribute, groupNameListFilter, membershipAttribute, groupNameSearchFilter
        });
        OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES.add(readLDAPGroups);

        setProperty(UserStoreConfigConstants.writeGroups, "Write Groups", "true",
                UserStoreConfigConstants.writeGroupsDescription,
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupSearchBase, "Group Search Base", "CN=Users,DC=WSO2,DC=Com",
                UserStoreConfigConstants.groupSearchBaseDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.GROUP_CREATED_DATE_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_CREATED_DATE_ATTRIBUTE_DISPLAY_NAME, "whenCreated",
                UserStoreConfigConstants.GROUP_CREATED_DATE_ATTRIBUTE_DESCRIPTION,
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.GROUP_LAST_MODIFIED_DATE_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_LAST_MODIFIED_DATE_ATTRIBUTE_DISPLAY_NAME, "whenChanged",
                UserStoreConfigConstants.GROUP_LAST_MODIFIED_DATE_ATTRIBUTE_DESCRIPTION,
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupEntryObjectClass, "Group Entry Object Class", "group",
                UserStoreConfigConstants.groupEntryObjectClassDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupNameAttribute, "Group Name Attribute", "cn",
                UserStoreConfigConstants.groupNameAttributeDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupNameSearchFilter, "Group Search Filter",
                "(&(objectClass=group)(cn=?))", UserStoreConfigConstants.groupNameSearchFilterDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.groupNameListFilter, "Group List Filter", "(objectcategory=group)",
                UserStoreConfigConstants.groupNameListFilterDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setProperty(roleDNPattern, "Group DN Pattern", "", roleDNPatternDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setProperty(UserStoreConfigConstants.membershipAttribute, "Membership Attribute", "member",
                UserStoreConfigConstants.membershipAttributeDescription, new Property[] {
                        GROUP.getProperty(), STRING.getProperty(), FALSE.getProperty()
                });
        setProperty(UserStoreConfigConstants.memberOfAttribute, "Member Of Attribute", "memberOf",
                UserStoreConfigConstants.memberOfAttribute,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty("BackLinksEnabled", "Enable Back Links", "true",
                "Whether to allow attributes to be result from references to the object from other objects",
                new Property[] { GROUP.getProperty(), BOOLEAN.getProperty(), FALSE.getProperty() });
        setProperty("Referral", "Referral", "ignore",
                "Guides the requests to a domain controller in the correct domain",
                new Property[] { GROUP.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setProperty(UserStoreConfigConstants.usernameJavaRegEx, "Username RegEx (Java)", "[a-zA-Z0-9._-|//]{3,30}$",
                UserStoreConfigConstants.usernameJavaRegExDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.usernameJavaScriptRegEx, "Username RegEx (Javascript)", "^[\\S]{3,30}$",
                UserStoreConfigConstants.usernameJavaScriptRegExDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setProperty(usernameJavaRegExViolationErrorMsg, "Username RegEx Violation Error Message",
                "Username pattern policy violated.", usernameJavaRegExViolationErrorMsgDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setProperty(passwordJavaRegEx, "Password RegEx (Java)", "^[\\S]{5,30}$", passwordJavaRegExDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.passwordJavaScriptRegEx, "Password RegEx (Javascript)", "^[\\S]{5,30}$",
                UserStoreConfigConstants.passwordJavaScriptRegExDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setProperty(passwordJavaRegExViolationErrorMsg, "Password RegEx Violation Error Message",
                "Password pattern policy violated.", passwordJavaRegExViolationErrorMsgDescription,
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        setProperty(UserStoreConfigConstants.roleNameJavaRegEx, "Group Name RegEx (Java)", "[a-zA-Z0-9._-|//]{3,30}$",
                UserStoreConfigConstants.roleNameJavaRegExDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });

        setProperty(UserStoreConfigConstants.roleNameJavaScriptRegEx, "Group Name RegEx (Javascript)", "^[\\S]{3,30}$",
                UserStoreConfigConstants.roleNameJavaScriptRegExDescription,
                new Property[] { GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty("UniqueID", "", "", "",
                new Property[] { USER.getProperty(), STRING.getProperty(), FALSE.getProperty() });
        setProperty(UserStoreConfigConstants.lDAPInitialContextFactory, "LDAP Initial Context Factory",
                "com.sun.jndi.ldap.LdapCtxFactory", UserStoreConfigConstants.lDAPInitialContextFactoryDescription,
                new Property[] { CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty() });

        // Group Id Related Userstore Configurations - By default this will be disabled.
        setMandatoryPropertyForUniqueIdStore(UserStoreConfigConstants.GROUP_ID_ENABLED,
                UserStoreConfigConstants.GROUP_ID_ENABLED_DISPLAY_NAME, Boolean.toString(false),
                UserStoreConfigConstants.GROUP_ID_ENABLED_DESCRIPTION, false,
                new Property[]{GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty()});
        setMandatoryPropertyForUniqueIdStore(UserStoreConfigConstants.GROUP_ID_ATTRIBUTE,
                UserStoreConfigConstants.GROUP_ID_ATTRIBUTE_DISPLAY_NAME, "objectGuid",
                UserStoreConfigConstants.GROUP_ID_ATTRIBUTE_DESCRIPTION, false,
                new Property[]{GROUP.getProperty(), STRING.getProperty(), TRUE.getProperty()});
        setMandatoryPropertyForUniqueIdStore(UserStoreConfigConstants.userIdAttribute,
                UserStoreConfigConstants.userIdAttributeName, UserStoreConfigConstants.OBJECT_GUID,
                UserStoreConfigConstants.userIdAttributeDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setMandatoryPropertyForUniqueIdStore(UserStoreConfigConstants.userIdSearchFilter,
                UserStoreConfigConstants.userIdSearchFilterAttributeName, "(&(objectClass=person)(objectGuid=?))",
                UserStoreConfigConstants.userIdSearchFilterDescription, false,
                new Property[] { USER.getProperty(), STRING.getProperty(), TRUE.getProperty() });
        setProperty(UserStoreConfigConstants.dateAndTimePattern, UserStoreConfigConstants.dateAndTimePatternDisplayName,
                DEFAULT_LDAP_TIME_FORMATS_PATTERN, UserStoreConfigConstants.dateAndTimePatternDescription,
                new Property[]{CONNECTION.getProperty(), STRING.getProperty(), FALSE.getProperty()});
    }

    private static void setMandatoryProperty(String name, String displayName, String value, String description,
            boolean encrypt, Property[] childProperties) {

        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, childProperties);
        ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);
    }

    private static void setMandatoryPropertyForUniqueIdStore(String name, String displayName, String value,
            String description, boolean encrypt, Property[] childProperties) {

        String propertyDescription = displayName + "#" + description;
        if (encrypt) {
            propertyDescription += "#encrypt";
        }
        Property property = new Property(name, value, propertyDescription, childProperties);
        UNIQUE_ID_ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);
    }

    private static void setProperty(String name, String displayName, String value, String description,
            Property[] childProperties) {

        Property property = new Property(name, value, displayName + "#" + description, childProperties);
        OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);
    }
}
