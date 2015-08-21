/*
 * Copyright 2009-2010 WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.user.core.util;

import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;

import java.util.Map;

/**
 * Map default JDBC User store properties if they are not configured in user-mgt.xml
 */

public class JDBCRealmUtil {

    public static Map<String, String> getSQL(Map<String, String> properties) {

        if (!properties.containsKey(JDBCRealmConstants.SELECT_USER)) {
            properties.put(JDBCRealmConstants.SELECT_USER, JDBCRealmConstants.SELECT_USER_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_ROLE_LIST)) {
            properties.put(JDBCRealmConstants.GET_ROLE_LIST, JDBCRealmConstants.GET_ROLE_LIST_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_SHARED_ROLE_LIST)) {
            properties.put(JDBCRealmConstants.GET_SHARED_ROLE_LIST, JDBCRealmConstants.GET_SHARED_ROLE_LIST_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.GET_USER_ROLE)) {
            properties.put(JDBCRealmConstants.GET_USER_ROLE, JDBCRealmConstants.GET_USER_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER)) {
            properties.put(JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER,
                    JDBCRealmConstants.GET_SHARED_ROLES_FOR_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USER_FILTER)) {
            properties.put(JDBCRealmConstants.GET_USER_FILTER,
                    JDBCRealmConstants.GET_USER_FILTER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_IS_ROLE_EXISTING)) {
            properties.put(JDBCRealmConstants.GET_IS_ROLE_EXISTING,
                    JDBCRealmConstants.GET_IS_ROLE_EXISTING_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_IN_ROLE)) {
            properties.put(JDBCRealmConstants.GET_USERS_IN_ROLE,
                    JDBCRealmConstants.GET_USERS_IN_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_IS_USER_EXISTING)) {
            properties.put(JDBCRealmConstants.GET_IS_USER_EXISTING,
                    JDBCRealmConstants.GET_IS_USER_EXISTING_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PROPS_FOR_PROFILE)) {
            properties.put(JDBCRealmConstants.GET_PROPS_FOR_PROFILE,
                    JDBCRealmConstants.GET_PROPS_FOR_PROFILE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PROP_FOR_PROFILE)) {
            properties.put(JDBCRealmConstants.GET_PROP_FOR_PROFILE,
                    JDBCRealmConstants.GET_PROP_FOR_PROFILE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_FOR_PROP)) {
            properties.put(JDBCRealmConstants.GET_USERS_FOR_PROP,
                    JDBCRealmConstants.GET_USERS_FOR_PROP_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PROFILE_NAMES)) {
            properties.put(JDBCRealmConstants.GET_PROFILE_NAMES,
                    JDBCRealmConstants.GET_PROFILE_NAMES_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER)) {
            properties.put(JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER,
                    JDBCRealmConstants.GET_PROFILE_NAMES_FOR_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERID_FROM_USERNAME)) {
            properties.put(JDBCRealmConstants.GET_USERID_FROM_USERNAME,
                    JDBCRealmConstants.GET_USERID_FROM_USERNAME_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME)) {
            properties.put(JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME,
                    JDBCRealmConstants.GET_TENANT_ID_FROM_USERNAME_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER)) {
            properties.put(JDBCRealmConstants.ADD_USER, JDBCRealmConstants.ADD_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_TO_ROLE)) {
            properties.put(JDBCRealmConstants.ADD_USER_TO_ROLE,
                    JDBCRealmConstants.ADD_USER_TO_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_PERMISSION)) {
            properties.put(JDBCRealmConstants.ADD_USER_PERMISSION,
                    JDBCRealmConstants.ADD_USER_PERMISSION_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE)) {
            properties.put(JDBCRealmConstants.ADD_ROLE, JDBCRealmConstants.ADD_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_SHARED_ROLE)) {
            properties.put(JDBCRealmConstants.ADD_SHARED_ROLE,
                    JDBCRealmConstants.ADD_SHARED_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE_TO_USER)) {
            properties.put(JDBCRealmConstants.ADD_ROLE_TO_USER,
                    JDBCRealmConstants.ADD_ROLE_TO_USER_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER)) {
            properties.put(JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER,
                    JDBCRealmConstants.ADD_SHARED_ROLE_TO_USER_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE_PERMISSION)) {
            properties.put(JDBCRealmConstants.ADD_ROLE_PERMISSION,
                    JDBCRealmConstants.ADD_ROLE_PERMISSION_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.REMOVE_USER_FROM_ROLE)) {
            properties.put(JDBCRealmConstants.REMOVE_USER_FROM_ROLE,
                    JDBCRealmConstants.REMOVE_USER_FROM_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE)) {
            properties.put(JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE,
                    JDBCRealmConstants.REMOVE_USER_FROM_SHARED_ROLE_SQL);
        }

        if (!properties.containsKey(JDBCRealmConstants.REMOVE_ROLE_FROM_USER)) {
            properties.put(JDBCRealmConstants.REMOVE_ROLE_FROM_USER,
                    JDBCRealmConstants.REMOVE_ROLE_FROM_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.DELETE_ROLE)) {
            properties.put(JDBCRealmConstants.DELETE_ROLE, JDBCRealmConstants.DELETE_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE)) {
            properties.put(JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE,
                    JDBCRealmConstants.ON_DELETE_ROLE_REMOVE_USER_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION)) {
            properties.put(JDBCRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION,
                    JDBCRealmConstants.ON_DELETE_ROLE_DELETE_PERMISSION_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.DELETE_USER)) {
            properties.put(JDBCRealmConstants.DELETE_USER, JDBCRealmConstants.DELETE_USER_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE)) {
            properties.put(JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE,
                    JDBCRealmConstants.ON_DELETE_USER_REMOVE_USER_ROLE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE)) {
            properties.put(JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE,
                    JDBCRealmConstants.ON_DELETE_USER_REMOVE_ATTRIBUTE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ON_DELETE_USER_DELETE_PERMISSION)) {
            properties.put(JDBCRealmConstants.ON_DELETE_USER_DELETE_PERMISSION,
                    JDBCRealmConstants.ON_DELETE_USER_DELETE_PERMISSION_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.UPDATE_USER_PASSWORD)) {
            properties.put(JDBCRealmConstants.UPDATE_USER_PASSWORD,
                    JDBCRealmConstants.UPDATE_USER_PASSWORD_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_PROPERTY)) {
            properties.put(JDBCRealmConstants.ADD_USER_PROPERTY,
                    JDBCRealmConstants.ADD_USER_PROPERTY_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.USER_NAME_UNIQUE)) {
            properties.put(JDBCRealmConstants.USER_NAME_UNIQUE,
                    JDBCRealmConstants.USER_NAME_UNIQUE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.UPDATE_USER_PROPERTY)) {
            properties.put(JDBCRealmConstants.UPDATE_USER_PROPERTY,
                    JDBCRealmConstants.UPDATE_USER_PROPERTY_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.DELETE_USER_PROPERTY)) {
            properties.put(JDBCRealmConstants.DELETE_USER_PROPERTY,
                    JDBCRealmConstants.DELETE_USER_PROPERTY_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.UPDATE_ROLE_NAME)) {
            properties.put(JDBCRealmConstants.UPDATE_ROLE_NAME,
                    JDBCRealmConstants.UPDATE_ROLE_NAME_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL)) {
            properties.put(JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL,
                    JDBCRealmConstants.ADD_USER_TO_ROLE_MSSQL_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL)) {
            properties.put(JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL,
                    JDBCRealmConstants.ADD_ROLE_TO_USER_MSSQL_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL)) {
            properties.put(JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL,
                    JDBCRealmConstants.ADD_USER_PROPERTY_MSSQL_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE)) {
            properties.put(JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE,
                    JDBCRealmConstants.GET_USERS_IN_SHARED_ROLE_SQL);
        }

        //openedge
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE)) {
            properties.put(JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE,
                    JDBCRealmConstants.ADD_USER_TO_ROLE_OPENEDGE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE)) {
            properties.put(JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE,
                    JDBCRealmConstants.ADD_ROLE_TO_USER_OPENEDGE_SQL);
        }
        if (!properties.containsKey(JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE)) {
            properties.put(JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE,
                    JDBCRealmConstants.ADD_USER_PROPERTY_OPENEDGE_SQL);
        }
        return properties;
    }
}
