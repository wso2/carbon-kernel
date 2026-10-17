/*
 * Copyright (c) 2005-2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.user.core.tenant;

public class TenantConstants {

    public static final String TENANT_TABLE_ALIAS = "T";

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
    public static final String UPDATE_TENANT_WITH_ASSOCIATE_UUID_SQL = "UPDATE UM_TENANT SET UM_DOMAIN_NAME=?, " +
            "UM_EMAIL=?, UM_CREATED_DATE=?, UM_ORG_UUID=? WHERE UM_ID=?";
    public static final String GET_TENANT_SQL = "SELECT * FROM UM_TENANT WHERE UM_ID=?";
    public static final String GET_TENANT_BY_UUID_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_CREATED_DATE, UM_USER_CONFIG, UM_TENANT_UUID FROM UM_TENANT WHERE " +
            "UM_TENANT_UUID=?";

    /**
     * @deprecated Use {@link #GET_TENANT_BY_UUID_INCLUDING_ORG_UUID_AND_ORG_NAME_SQL} instead.
     */
    @Deprecated
    public static final String GET_TENANT_BY_UUID_INCLUDING_UM_ORG_UUID_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_CREATED_DATE, UM_USER_CONFIG, UM_TENANT_UUID, UM_ORG_UUID FROM UM_TENANT WHERE " +
            "UM_TENANT_UUID=?";
    public static final String GET_TENANT_BY_UUID_INCLUDING_ORG_UUID_AND_ORG_NAME_SQL = "SELECT T.UM_ID, " +
            "T.UM_DOMAIN_NAME, T.UM_EMAIL, T.UM_CREATED_DATE, T.UM_ACTIVE, T.UM_CREATED_DATE, T.UM_USER_CONFIG, " +
            "T.UM_TENANT_UUID, T.UM_ORG_UUID, O.UM_ORG_NAME FROM UM_TENANT T " +
            "LEFT OUTER JOIN UM_ORG O ON T.UM_ORG_UUID = O.UM_ID WHERE UM_TENANT_UUID=?";
    public static final String GET_ALL_TENANTS_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE FROM UM_TENANT ORDER BY UM_ID";
    public static final String GET_ALL_TENANTS_EXCEPT_ORGANIZATIONS_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE FROM UM_TENANT WHERE UM_ORG_UUID IS NULL OR UM_ORG_UUID IN " +
            "(SELECT UM_ID FROM UM_ORG WHERE UM_PARENT_ID IS NULL) ORDER BY UM_ID";
    public static final String LIST_TENANTS_COUNT_SQL = "SELECT COUNT(*) FROM UM_TENANT";
    public static final String COUNT_TENANTS_EXCLUDING_ORGANIZATIONS_SQL = " UM_ORG_UUID " +
            "IN (SELECT UM_ID FROM UM_ORG WHERE UM_PARENT_ID IS NULL)";

    /**
     * @deprecated Use {@link #LIST_TENANTS_INCLUDING_ORG_NAME_PAGINATED_SQL} instead.
     */
    @Deprecated
    public static final String LIST_TENANTS_PAGINATED_SQL = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID FROM UM_TENANT WHERE UM_ORG_UUID IN " +
            "(SELECT UM_ID FROM UM_ORG WHERE UM_PARENT_ID IS NULL) ";

    /**
     * @deprecated Use {@link #LIST_TENANTS_INCLUDING_ORG_NAME_PAGINATED_SQL} instead.
     */
    @Deprecated
    public static final String LIST_TENANTS_PAGINATED_ORACLE = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID FROM (SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID, rownum AS rnum FROM (SELECT UM_ID, " +
            "UM_DOMAIN_NAME, UM_EMAIL, UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID FROM UM_TENANT WHERE " +
            "UM_ORG_UUID IN (SELECT UM_ID FROM UM_ORG WHERE UM_PARENT_ID IS NULL) ";
    /**
     * @deprecated Use {@link #LIST_TENANTS_INCLUDING_ORG_NAME_PAGINATED_SQL} instead.
     */
    @Deprecated
    public static final String LIST_TENANTS_PAGINATED_DB2 = "SELECT UM_ID, UM_DOMAIN_NAME, UM_EMAIL, " +
            "UM_CREATED_DATE, UM_ACTIVE, UM_USER_CONFIG, UM_TENANT_UUID FROM (SELECT ROW_NUMBER() OVER (ORDER BY %s) " +
            "AS rn,UM_TENANT.* FROM UM_TENANT WHERE UM_ORG_UUID IN (SELECT UM_ID FROM UM_ORG WHERE UM_PARENT_ID IS NULL)";
    public static final String LIST_TENANTS_INCLUDING_ORG_NAME_PAGINATED_SQL = "SELECT T.UM_ID, T.UM_DOMAIN_NAME, " +
            "T.UM_EMAIL, T.UM_CREATED_DATE, T.UM_ACTIVE, T.UM_USER_CONFIG, T.UM_TENANT_UUID, O.UM_ORG_NAME " +
            "FROM UM_TENANT T INNER JOIN UM_ORG O ON T.UM_ORG_UUID = O.UM_ID WHERE O.UM_PARENT_ID IS NULL ";
    public static final String LIST_TENANTS_MYSQL_TAIL = "ORDER BY %s LIMIT ?, ?";

    /**
     * @deprecated Use {@link #LIST_TENANTS_TAIL} instead.
     * Oracle 12c+, SQL Server, DB2 11.1+, and PostgreSQL 13+ all support the OFFSET FETCH NEXT syntax for pagination.
     */
    public static final String LIST_TENANTS_POSTGRESQL_TAIL = "ORDER BY %s LIMIT ? OFFSET ?";
    public static final String LIST_TENANTS_DB2_TAIL = " ) WHERE rn BETWEEN ? AND ?";
    public static final String LIST_TENANTS_MSSQL_TAIL = "ORDER BY %s OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String LIST_TENANTS_ORACLE_TAIL = "ORDER BY %s) WHERE rownum <= ?) WHERE rnum > ?";

    public static final String LIST_TENANTS_TAIL = "ORDER BY %s.%s OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
    public static final String LIST_TENANTS_DOMAIN_FILTER_LIKE = "%s UM_DOMAIN_NAME LIKE ? ";
    public static final String LIST_TENANTS_DOMAIN_FILTER_EQUAL = "%s UM_DOMAIN_NAME=? ";
    public static final String GET_DOMAIN_SQL = "SELECT UM_DOMAIN_NAME FROM UM_TENANT WHERE UM_ID=?";
    public static final String GET_TENANT_NAME_SQL = "SELECT O.UM_ORG_NAME FROM UM_TENANT T " +
            "INNER JOIN UM_ORG O ON T.UM_ORG_UUID = O.UM_ID WHERE T.UM_ID = ?";
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

    public static final String GET_SECONDARY_USER_STORES_SQL =
            "SELECT UM_DOMAIN_NAME, UM_USER_STORE_CONFIG FROM UM_SECONDARY_USER_STORE WHERE UM_TENANT_ID = ?";
}
