/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.user.core.jdbc;

public class JDBCTenantUMDataDeletionConstants {

    public static final String DELETE_USER_PERMISSION_SQL = "DELETE FROM UM_USER_PERMISSION WHERE UM_TENANT_ID = ?";

    public static final String DELETE_ROLE_PERMISSION_SQL = "DELETE FROM UM_ROLE_PERMISSION WHERE UM_TENANT_ID = ?";

    public static final String DELETE_PERMISSION_SQL = "DELETE FROM UM_PERMISSION WHERE UM_TENANT_ID = ?";

    public static final String DELETE_PROFILE_CONFIG_SQL = "DELETE FROM UM_PROFILE_CONFIG WHERE UM_TENANT_ID = ?";

    public static final String DELETE_CLAIM_SQL = "DELETE FROM UM_CLAIM WHERE UM_TENANT_ID = ?";

    public static final String DELETE_DIALECT_SQL = "DELETE FROM UM_DIALECT WHERE UM_TENANT_ID = ?";

    public static final String DELETE_USER_ATTRIBUTE_SQL = "DELETE FROM UM_USER_ATTRIBUTE WHERE UM_TENANT_ID = ?";

    public static final String DELETE_HYBRID_USER_ROLE_SQL = "DELETE FROM UM_HYBRID_USER_ROLE WHERE UM_TENANT_ID = ?";

    public static final String DELETE_HYBRID_ROLE_SQL = "DELETE FROM UM_HYBRID_ROLE WHERE UM_TENANT_ID = ?";

    public static final String DELETE_HYBRID_REMEMBER_ME_SQL = "DELETE FROM UM_HYBRID_REMEMBER_ME WHERE UM_TENANT_ID = ?";

    public static final String DELETE_USER_ROLE_SQL = "DELETE FROM UM_USER_ROLE WHERE UM_TENANT_ID = ?";

    public static final String DELETE_ROLE_SQL = "DELETE FROM UM_ROLE WHERE UM_TENANT_ID = ?";

    public static final String DELETE_USER_SQL = "DELETE FROM UM_USER WHERE UM_TENANT_ID = ?";

    public static final String DELETE_DOMAIN_SQL = "DELETE FROM UM_DOMAIN WHERE UM_TENANT_ID = ?";

    public static final String DELETE_SYSTEM_ROLE_SQL = "DELETE FROM UM_SYSTEM_ROLE WHERE UM_TENANT_ID = ?";

    public static final String DELETE_SYSTEM_USER_ROLE_SQL = "DELETE FROM UM_SYSTEM_USER_ROLE WHERE UM_TENANT_ID = ?";

    public static final String DELETE_SYSTEM_USER_SQL = "DELETE FROM UM_SYSTEM_USER WHERE UM_TENANT_ID = ?";

}
