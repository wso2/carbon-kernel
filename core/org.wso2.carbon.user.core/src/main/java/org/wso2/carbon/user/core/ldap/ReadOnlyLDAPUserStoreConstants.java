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

public class ReadOnlyLDAPUserStoreConstants {


    //Properties for Read Write LDAP User Store Manager
    public static final ArrayList<Property> ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> OPTIONAL_ROLDAP_USERSTORE_PROPERTIES = new ArrayList<Property>();
    static {
        setMandatoryProperty(UserStoreConfigConstants.connectionName,"uid=,ou=",UserStoreConfigConstants.connectionNameDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionURL,"ldap://",UserStoreConfigConstants.connectionURLDescription);
        setMandatoryProperty(UserStoreConfigConstants.connectionPassword,"",UserStoreConfigConstants.connectionPasswordDescription);
        setMandatoryProperty(UserStoreConfigConstants.userSearchBase,"ou=system",UserStoreConfigConstants.userSearchBaseDescription);
        setMandatoryProperty(UserStoreConfigConstants.disabled,"false",UserStoreConfigConstants.disabledDescription);
        setMandatoryProperty(UserStoreConfigConstants.usernameListFilter, "(objectClass=person)", UserStoreConfigConstants.usernameListFilterDescription);
        setMandatoryProperty(UserStoreConfigConstants.userNameAttribute, "uid", UserStoreConfigConstants.userNameAttributeDescription);
        setMandatoryProperty(UserStoreConfigConstants.usernameSearchFilter,"(&amp;(objectClass=person)(uid=?))",UserStoreConfigConstants.usernameSearchFilterDescription);
        setMandatoryProperty("ReadOnly","true","Indicates whether the user store is in read only mode or not");

        setProperty(UserStoreConfigConstants.maxUserNameListLength, "100", UserStoreConfigConstants.maxUserNameListLengthDescription);
        setProperty(UserStoreConfigConstants.maxRoleNameListLength, "100", UserStoreConfigConstants.maxRoleNameListLengthDescription);
        setProperty(UserStoreConfigConstants.userRolesCacheEnabled, "true", UserStoreConfigConstants.userRolesCacheEnabledDescription);
        setProperty(UserStoreConfigConstants.SCIMEnabled, "false", UserStoreConfigConstants.SCIMEnabledDescription);

        Property readLDAPGroups = new Property(UserStoreConfigConstants.readGroups,"false",UserStoreConfigConstants.readLDAPGroupsDescription,null);
        //Mandatory only if readGroups is enabled
        Property groupSearchBase = new Property(UserStoreConfigConstants.groupSearchBase,"ou=system",UserStoreConfigConstants.groupSearchBaseDescription,null);
        Property groupNameListFilter = new Property(UserStoreConfigConstants.groupNameListFilter,"(objectClass=groupOfNames)",UserStoreConfigConstants.groupNameListFilterDescription,null);
        Property groupNameAttribute = new Property(UserStoreConfigConstants.groupNameAttribute,"cn",UserStoreConfigConstants.groupNameAttributeDescription,null);
        Property membershipAttribute = new Property(UserStoreConfigConstants.membershipAttribute,"member",UserStoreConfigConstants.membershipAttributeDescription,null);
        readLDAPGroups.setChildProperties(new Property[]{groupSearchBase,groupNameListFilter,groupNameAttribute,membershipAttribute});
        OPTIONAL_ROLDAP_USERSTORE_PROPERTIES.add(readLDAPGroups);
        
        setProperty(UserStoreConfigConstants.groupSearchBase,"ou=system",UserStoreConfigConstants.groupSearchBaseDescription);
        setProperty(UserStoreConfigConstants.groupNameListFilter,"(objectClass=groupOfNames)",UserStoreConfigConstants.groupNameListFilterDescription);
        setProperty(UserStoreConfigConstants.groupNameSearchFilter,"(&amp;(objectClass=groupOfNames)(cn=?))",UserStoreConfigConstants.groupNameSearchFilterDescription);
        setProperty(UserStoreConfigConstants.groupNameAttribute,"cn",UserStoreConfigConstants.groupNameAttributeDescription);
        setProperty(UserStoreConfigConstants.membershipAttribute,"member",UserStoreConfigConstants.membershipAttributeDescription);
        

//      LDAP Specific Properties
        setProperty(UserStoreConfigConstants.passwordHashMethod,"PLAIN_TEXT",UserStoreConfigConstants.passwordHashMethodDescription);
        setProperty("ReplaceEscapeCharactersAtUserLogin","true","Whether replace escape character when user login");

    }

    private static void setMandatoryProperty(String name,String value,String description){
        Property property = new Property(name,value,description,null);
        ROLDAP_USERSTORE_PROPERTIES.add(property);

    }

    private static void setProperty(String name,String value,String description){
        Property property = new Property(name,value,description,null);
        OPTIONAL_ROLDAP_USERSTORE_PROPERTIES.add(property);

    }


}
