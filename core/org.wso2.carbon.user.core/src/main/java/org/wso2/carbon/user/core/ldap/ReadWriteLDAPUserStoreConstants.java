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

public class ReadWriteLDAPUserStoreConstants{


        //Properties for Read Write LDAP User Store Manager
    public static final ArrayList<Property> RWLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTINAL_RWLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    static {
        setMandatoryProperty(UserStoreConfigConstants.connectionName,"uid=,ou=",UserStoreConfigConstants.connectionNameDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionURL,"ldap://",UserStoreConfigConstants.connectionURLDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword,"",UserStoreConfigConstants.connectionPasswordDescription);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase,"ou=Users,dc=wso2,dc=org",UserStoreConfigConstants.userSearchBaseDescription);
        setMandatoryProperty(UserStoreConfigConstants.disabled,"false",UserStoreConfigConstants.disabledDescription);
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription);
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "uid", UserStoreConfigConstants.userNameAttributeDescription);
        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter,"(&amp;(objectClass=person)(uid=?))",UserStoreConfigConstants.usernameSearchFilterDescription);
        setMandatoryProperty(UserStoreConfigConstants.userEntryObjectClass,"wso2Person",UserStoreConfigConstants.userEntryObjectClassDescription);
        setMandatoryProperty(UserStoreConfigConstants.groupEntryObjectClass, "groupOfNames", UserStoreConfigConstants.groupEntryObjectClassDescription);

        setProperty(UserStoreConfigConstants.maxUserNameListLength, "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);
        setProperty(UserStoreConfigConstants.SCIMEnabled, "false", UserStoreConfigConstants.SCIMEnabledDescription);
        
        Property readLDAPGroups = new Property(UserStoreConfigConstants.readGroups,"true",UserStoreConfigConstants.readLDAPGroupsDescription,null);
        //Mandatory only if readGroups is enabled
        Property groupSearchBase = new Property(UserStoreConfigConstants.groupSearchBase,"ou=Groups,dc=wso2,dc=org",UserStoreConfigConstants.groupSearchBaseDescription,null);
        Property groupNameListFilter = new Property(UserStoreConfigConstants.groupNameListFilter,"(objectClass=groupOfNames)",UserStoreConfigConstants.groupNameListFilterDescription,null);
        Property groupNameAttribute = new Property(UserStoreConfigConstants.groupNameAttribute,"cn",UserStoreConfigConstants.groupNameAttributeDescription,null);
        Property membershipAttribute = new Property(UserStoreConfigConstants.membershipAttribute,"member",UserStoreConfigConstants.membershipAttributeDescription,null);
        Property groupNameSearchFilter = new Property(UserStoreConfigConstants.groupNameSearchFilter,"(&amp;(objectClass=groupOfNames)(cn=?))"
                ,UserStoreConfigConstants.groupNameSearchFilterDescription,null);
//        readLDAPGroups.setChildProperties(new Property[]{groupSearchBase,groupNameListFilter,groupNameAttribute,membershipAttribute,groupNameSearchFilter});

        RWLDAP_USERSTORE_PROPERTIES.add(readLDAPGroups);
        RWLDAP_USERSTORE_PROPERTIES.add(groupSearchBase);
        RWLDAP_USERSTORE_PROPERTIES.add(groupNameAttribute);
        RWLDAP_USERSTORE_PROPERTIES.add(groupNameListFilter);
        RWLDAP_USERSTORE_PROPERTIES.add(membershipAttribute);
        RWLDAP_USERSTORE_PROPERTIES.add(groupNameSearchFilter);


//      LDAP Specific Properties
        setProperty(UserStoreConfigConstants.passwordHashMethod,"SHA",UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty(UserStoreConfigConstants.userDNPattern,"uid={0},ou=Users,dc=wso2,dc=org",UserStoreConfigConstants.userDNPatternDescription);
        setProperty(UserStoreConfigConstants.passwordJavaScriptRegEx,"^[\\S]{5,30}$",UserStoreConfigConstants.passwordJavaScriptRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaScriptRegEx,"^[\\S]{3,30}$",UserStoreConfigConstants.usernameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.usernameJavaRegEx,"[a-zA-Z0-9._-|//]{3,30}$",UserStoreConfigConstants.usernameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.roleNameJavaScriptRegEx,"^[\\S]{3,30}$",UserStoreConfigConstants.roleNameJavaScriptRegExDescription);
        setProperty(UserStoreConfigConstants.roleNameJavaRegEx,"[a-zA-Z0-9._-|//]{3,30}$",UserStoreConfigConstants.roleNameJavaRegExDescription);
        setProperty(UserStoreConfigConstants.writeGroups,"true",UserStoreConfigConstants.writeGroupsDescription);
        setProperty(UserStoreConfigConstants.emptyRolesAllowed,"true",UserStoreConfigConstants.emptyRolesAllowedDescription);
        setProperty(UserStoreConfigConstants.memberOfAttribute,"",UserStoreConfigConstants.memberOfAttribute);
    }

    private static void setMandatoryProperty(String name,String value,String description){
        Property property = new Property(name,value,description,null);
        RWLDAP_USERSTORE_PROPERTIES.add(property);

    }

    private static void setProperty(String name,String value,String description){
        Property property = new Property(name,value,description,null);
        OPTINAL_RWLDAP_USERSTORE_PROPERTIES.add(property);

    }

}
