/*
 *  Copyright (c) 2005-2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.user.core.tenant;

public class TenantConstants {
    public static final String ADD_TENANT_WITH_ID_SQL = "INSERT INTO UM_TENANT (UM_ID,UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG) VALUES(?,?,?,?,?)";

    public static final String ADD_TENANT_WITH_ID_AND_WITH_ORG_UUID_SQL = "INSERT INTO UM_TENANT (UM_ID,UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG, UM_ORG_UUID) VALUES(?,?,?,?,?,?)";
    public static final String ADD_TENANT_WITH_ID_AND_UUID_SQL = "INSERT INTO UM_TENANT (UM_ID,UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG, UM_TENANT_UUID) VALUES(?,?,?,?,?,?)";

    public static final String ADD_TENANT_WITH_ID_AND_UUID_AND_ORG_UUID_SQL = "INSERT INTO UM_TENANT (UM_ID,UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG, UM_ORG_UUID, UM_TENANT_UUID) VALUES(?,?,?,?,?,?,?)";
    public static final String ADD_TENANT_SQL = "INSERT INTO UM_TENANT (UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG) VALUES(?,?,?,?)";

    public static final String ADD_TENANT_SQL_WITH_ORG_UUID = "INSERT INTO UM_TENANT (UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG, UM_ORG_UUID) VALUES(?,?,?,?,?)";
    public static final String ADD_TENANT_SQL_WITH_UUID = "INSERT INTO UM_TENANT (UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG, UM_TENANT_UUID) VALUES(?,?,?,?,?)";

    public static final String ADD_TENANT_SQL_WITH_UUID_AND_WITH_ORG_UUID = "INSERT INTO UM_TENANT (UM_DOMAIN_NAME," +
            "UM_EMAIL, UM_CREATED_DATE, UM_USER_CONFIG, UM_TENANT_UUID, UM_ORG_UUID) VALUES(?,?,?,?,?,?)";
    public static final String UPDATE_TENANT_CONFIG_SQL = "UPDATE UM_TENANT SET UM_USER_CONFIG=? WHERE UM_ID=?";
    public static final String UPDATE_TENANT_SQL = "UPDATE UM_TENANT SET UM_DOMAIN_NAME=?, UM_EMAIL=?," +
            " UM_CREATED_DATE=? WHERE UM_ID=?";
    public static final String GET_TENANT_SQL = "SELECT * FROM UM_TENANT WHERE UM_ID=?";
    public static final String GET_TENANT_BY_UUID_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_CREATED_DATE, UM_USER_CONFIG, UM_TENANT_UUID FROM UM_TENANT WHERE " +
            "UM_TENANT_UUID=?";

    public static final String GET_TENANT_BY_UUID_INCLUDING_UM_ORG_UUID_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_CREATED_DATE, UM_USER_CONFIG, UM_TENANT_UUID, UM_ORG_UUID FROM UM_TENANT WHERE " +
            "UM_TENANT_UUID=?";
    public static final String GET_ALL_TENANTS_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE FROM UM_TENANT ORDER BY UM_ID";
    public static final String LIST_TENANTS_COUNT_SQL = "SELECT COUNT(*) FROM UM_TENANT";
    public static final String LIST_TENANTS_PAGINATED_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID FROM UM_TENANT ";
    public static final String LIST_TENANTS_PAGINATED_ORACLE = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID FROM (SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID, rownum AS rnum FROM (SELECT UM_ID, " +
            "UM_DOMAIN_NAME, UM_EMAIL, UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID FROM UM_TENANT ";
    public static final String LIST_TENANTS_PAGINATED_DB2 = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID FROM (SELECT ROW_NUMBER() OVER";
    public static final String LIST_TENANTS_MYSQL_TAIL = "ORDER BY %s LIMIT ?, ?";
    public static final String LIST_TENANTS_POSTGRESQL_TAIL = "ORDER BY %s LIMIT ? OFFSET ?";
    public static final String LIST_TENANTS_DB2_TAIL = "(ORDER BY %s) AS rn,UM_TENANT.* FROM UM_TENANT)" +
            "WHERE rn BETWEEN ? AND ?";
    public static final String LIST_TENANTS_MSSQL_TAIL = "ORDER BY %s OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String LIST_TENANTS_ORACLE_TAIL = "ORDER BY %s) WHERE rownum <= ?) WHERE rnum > ?";
    public static final String GET_DOMAIN_SQL = "SELECT UM_DOMAIN_NAME FROM UM_TENANT WHERE UM_ID=?";
    public static final String GET_TENANT_ID_SQL = "SELECT UM_ID FROM UM_TENANT WHERE UM_DOMAIN_NAME=?";
    public static final String ACTIVATE_SQL = "UPDATE UM_TENANT SET UM_ACTIVE='1' WHERE UM_ID=?";
    public static final String DEACTIVATE_SQL = "UPDATE UM_TENANT SET UM_ACTIVE='0' WHERE UM_ID=?";
    public static final String ACTIVATE_BY_UUID_SQL = "UPDATE UM_TENANT SET UM_ACTIVE='1' WHERE UM_TENANT_UUID=?";
    public static final String DEACTIVATE_BY_UUID_SQL = "UPDATE UM_TENANT SET UM_ACTIVE='0' WHERE UM_TENANT_UUID=?";
    public static final String IS_TENANT_ACTIVE_SQL = "SELECT UM_ACTIVE FROM UM_TENANT WHERE UM_ID=?";
    public static final String DELETE_TENANT_SQL = "DELETE FROM UM_TENANT WHERE UM_ID=?";
    public static final String DELETE_TENANT_BY_UUID_SQL = "DELETE FROM UM_TENANT WHERE UM_TENANT_UUID=?";
    public static final String DELETE_UM_DOMAIN_BY_TENANT_ID_SQL = "DELETE FROM UM_DOMAIN WHERE UM_TENANT_ID=?";
    public static final String GET_MATCHING_TENANT_IDS_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL," +
            " UM_CREATED_DATE, UM_ACTIVE FROM UM_TENANT WHERE UM_DOMAIN_NAME like ?";

    public static final String IS_TENANT_UUID_COLUMN_EXISTS_MYSQL = "SELECT UM_TENANT_UUID FROM UM_TENANT LIMIT 1";
    public static final String IS_TENANT_UUID_COLUMN_EXISTS_DB2 = "SELECT UM_TENANT_UUID FROM UM_TENANT FETCH" +
            " FIRST 1 ROWS ONLY";
    public static final String IS_TENANT_UUID_COLUMN_EXISTS_MSSQL = "SELECT TOP 1 UM_TENANT_UUID FROM " +
            "UM_TENANT";
    public static final String IS_TENANT_UUID_COLUMN_EXISTS_INFORMIX = "SELECT FIRST 1 UM_TENANT_UUID FROM " +
            "UM_TENANT";
    public static final String IS_TENANT_UUID_COLUMN_EXISTS_ORACLE = "SELECT UM_TENANT_UUID FROM UM_TENANT " +
            "WHERE ROWNUM < 2";

    // Check UM_ORG_UUID column existence.
    public static final String IS_UM_ORG_UUID_COLUMN_EXISTS_MYSQL = "SELECT UM_ORG_UUID FROM UM_TENANT LIMIT 1";
    public static final String IS_UM_ORG_UUID_COLUMN_EXISTS_DB2 = "SELECT UM_ORG_UUID FROM UM_TENANT FETCH" +
            " FIRST 1 ROWS ONLY";
    public static final String IS_UM_ORG_UUID_COLUMN_EXISTS_MSSQL = "SELECT TOP 1 UM_ORG_UUID FROM " +
            "UM_TENANT";
    public static final String IS_UM_ORG_UUID_COLUMN_EXISTS_INFORMIX = "SELECT FIRST 1 UM_ORG_UUID FROM " +
            "UM_TENANT";
    public static final String IS_UM_ORG_UUID_COLUMN_EXISTS_ORACLE = "SELECT UM_ORG_UUID FROM UM_TENANT " +
            "WHERE ROWNUM < 2";
}
