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

public class ActiveDirectoryUserStoreConstants {


    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> ACTIVE_DIRECTORY_UM_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES = new ArrayList<Property>();

    static {
        setMandatoryProperty(UserStoreConfigConstants.connectionName,"CN=,DC=",UserStoreConfigConstants.connectionNameDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionURL,"ldaps://",UserStoreConfigConstants.connectionURLDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword,"",UserStoreConfigConstants.connectionPasswordDescription);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase,"CN=Users,DC=WSO2,DC=Com",UserStoreConfigConstants.userSearchBaseDescription);
        setMandatoryProperty(UserStoreConfigConstants.disabled,"false",UserStoreConfigConstants.disabledDescription);
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription);
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "cn", UserStoreConfigConstants.userNameAttributeDescription);
        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter,"(&amp;(objectClass=user)(cn=?))",UserStoreConfigConstants.usernameSearchFilterDescription);
        setMandatoryProperty(UserStoreConfigConstants.userEntryObjectClass,"user",UserStoreConfigConstants.userEntryObjectClassDescription);
        setProperty(UserStoreConfigConstants.groupEntryObjectClass,"group",UserStoreConfigConstants.groupEntryObjectClassDescription);

        setProperty(UserStoreConfigConstants.maxUserNameListLength, "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);
        setProperty(UserStoreConfigConstants.SCIMEnabled, "false", UserStoreConfigConstants.SCIMEnabledDescription);

        Property readLDAPGroups = new Property(UserStoreConfigConstants.readGroups,"true",UserStoreConfigConstants.readLDAPGroupsDescription,null);
        //Mandatory only if readGroups is enabled
        Property groupSearchBase = new Property(UserStoreConfigConstants.groupSearchBase,"CN=Users,DC=WSO2,DC=Com",UserStoreConfigConstants.groupSearchBaseDescription,null);
        Property groupNameListFilter = new Property(UserStoreConfigConstants.groupNameListFilter,"(objectcategory=group)",UserStoreConfigConstants.groupNameListFilterDescription,null);
        Property groupNameAttribute = new Property(UserStoreConfigConstants.groupNameAttribute,"cn",UserStoreConfigConstants.groupNameAttributeDescription,null);
        Property membershipAttribute = new Property(UserStoreConfigConstants.membershipAttribute,"member",UserStoreConfigConstants.membershipAttributeDescription,null);
        Property groupNameSearchFilter = new Property(UserStoreConfigConstants.groupNameSearchFilter,"(&amp;(objectClass=group)(cn=?))",UserStoreConfigConstants.groupNameSearchFilterDescription,null);
        readLDAPGroups.setChildProperties(new Property[]{groupSearchBase,groupNameListFilter,groupNameAttribute,membershipAttribute,groupNameSearchFilter});
        OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES.add(readLDAPGroups);
        
        setProperty(UserStoreConfigConstants.groupSearchBase,"CN=Users,DC=WSO2,DC=Com",UserStoreConfigConstants.groupSearchBaseDescription);
        setProperty(UserStoreConfigConstants.groupNameListFilter,"(objectcategory=group)",UserStoreConfigConstants.groupNameListFilterDescription);
        setProperty(UserStoreConfigConstants.groupNameAttribute,"cn",UserStoreConfigConstants.groupNameAttributeDescription);
        setProperty(UserStoreConfigConstants.membershipAttribute,"member",UserStoreConfigConstants.membershipAttributeDescription);
        setProperty(UserStoreConfigConstants.groupNameSearchFilter,"(&amp;(objectClass=group)(cn=?))",UserStoreConfigConstants.groupNameSearchFilterDescription);
        
        
        
//      AD Specific Properties
        setProperty(UserStoreConfigConstants.passwordHashMethod,"PLAIN_TEXT",UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty(UserStoreConfigConstants.passwordJavaScriptRegEx,"^[\\S]{5,30}$",UserStoreConfigConstants.passwordJavaScriptRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaScriptRegEx,"^[\\S]{3,30}$",UserStoreConfigConstants.usernameJavaScriptRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaRegEx,"[a-zA-Z0-9._-|//]{3,30}$",UserStoreConfigConstants.usernameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.roleNameJavaScriptRegEx,"^[\\S]{3,30}$",UserStoreConfigConstants.roleNameJavaScriptRegExDescription);
        setProperty(UserStoreConfigConstants.roleNameJavaRegEx,"[a-zA-Z0-9._-|//]{3,30}$",UserStoreConfigConstants.roleNameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.writeGroups,"true",UserStoreConfigConstants.writeGroupsDescription);
        setProperty(UserStoreConfigConstants.userDNPattern,"uid={0},ou=Users,dc=wso2,dc=org",UserStoreConfigConstants.userDNPatternDescription);
        setProperty(UserStoreConfigConstants.emptyRolesAllowed,"true",UserStoreConfigConstants.emptyRolesAllowedDescription);
        setProperty("defaultRealmName","WSO2.ORG","Default name for the realm");
        setProperty("kdcEnabled","false","Whether key distribution center enabled");
        setProperty("DisplayNameAttribute","cn","The display name which usually is the combination of the users first name, middle initial, and last name");
        setProperty("isADLDSRole","false","Whether an Active Directory Lightweight Directory Services role");
        setProperty("userAccountControl","512","Flags that control the behavior of the user account");
        setProperty("Referral","follow","Guides the requests to a domain controller in the correct domain");
        setProperty("BackLinksEnabled","true"," Whether to allow attributes to be result from references to the object from other objects");
    }

    private static void setMandatoryProperty(String name,String value,String description){
        Property property = new Property(name,value,description,null);
        ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);

    }

    private static void setProperty(String name,String value,String description){
        Property property = new Property(name,value,description,null);
        OPTIONAL_ACTIVE_DIRECTORY_UM_PROPERTIES.add(property);

    }


}
