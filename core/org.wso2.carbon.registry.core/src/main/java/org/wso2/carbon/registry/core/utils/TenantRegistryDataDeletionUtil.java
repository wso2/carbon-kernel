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

package org.wso2.carbon.registry.core.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.TenantRegistryDataDeletionConstants;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDatabaseTransaction;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TenantRegistryDataDeletionUtil {

    private static final Log log = LogFactory.getLog(TenantRegistryDataDeletionUtil.class);
    private int tenantId;

    public TenantRegistryDataDeletionUtil(int tenantId) {
        this.tenantId = tenantId;
    }

    protected void deleteAllDataFromRegistryDB(JDBCDataAccessManager jdbcDataAccessManager) throws RegistryException {

        try {

            jdbcDataAccessManager.getTransactionManager().beginTransaction();
            JDBCDatabaseTransaction.ManagedRegistryConnection conn = JDBCDatabaseTransaction.getConnection();
            // Every execution of a query added a info log, if failed to delete data from a specific table its easy to
            // find from which table data deletion corrupted.
            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_CLUSTER_LOCK_SQL, tenantId);
            log.debug("Data deleted From REG_CLUSTER_LOCK table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_LOG_SQL, tenantId);
            log.debug("Data deleted From REG_LOG table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_ASSOCIATION_SQL, tenantId);
            log.debug("Data deleted From REG_ASSOCIATION table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_SNAPSHOT_SQL, tenantId);
            log.debug("Data deleted From REG_SNAPSHOT table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_RESOURCE_COMMENT_SQL, tenantId);
            log.debug("Data deleted From REG_RESOURCE_COMMENT table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_COMMENT_SQL, tenantId);
            log.debug("Data deleted From REG_COMMENT table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_RESOURCE_RATING_SQL, tenantId);
            log.debug("Data deleted From REG_RESOURCE_RATING table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_RATING_SQL, tenantId);
            log.debug("Data deleted From REG_RATING table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_RESOURCE_TAG_SQL, tenantId);
            log.debug("Data deleted From REG_RESOURCE_TAG table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_TAG_SQL, tenantId);
            log.debug("Data deleted From REG_TAG table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_RESOURCE_PROPERTY_SQL, tenantId);
            log.debug("Data deleted From REG_RESOURCE_PROPERTY table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_PROPERTY_SQL, tenantId);
            log.debug("Data deleted From REG_PROPERTY table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_RESOURCE_HISTORY_SQL, tenantId);
            log.debug("Data deleted From REG_RESOURCE_HISTORY table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_CONTENT_HISTORY_SQL, tenantId);
            log.debug("Data deleted From REG_CONTENT_HISTORY table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_RESOURCE_SQL, tenantId);
            log.debug("Data deleted From REG_RESOURCE table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_RESOURCE_PROPERTY_SQL, tenantId);
            log.debug("Data deleted From REG_RESOURCE_PROPERTY table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_CONTENT_SQL, tenantId);
            log.debug("Data deleted From REG_CONTENT table for tenant id : " + tenantId);

            this.executeDeleteQuery(conn, TenantRegistryDataDeletionConstants.DELETE_PATH_SQL, tenantId);
            log.debug("Data deleted From REG_TENANT_ID table for tenant id : " + tenantId);

            jdbcDataAccessManager.getTransactionManager().commitTransaction();


        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            try {
                log.info("Rollback transaction for registry data deletion for tenant id : " + tenantId);
                jdbcDataAccessManager.getTransactionManager().rollbackTransaction();
                throw new RegistryException(e.getMessage(), e);
            } catch (RegistryException e1) {
                String errMsgForRollback = "Error while transaction rollback for tenant id : " + tenantId;
                log.error(errMsgForRollback, e1);
                throw new RegistryException(errMsgForRollback, e1);
            }
        }
    }

    private void executeDeleteQuery(Connection conn, String query, int tenantId) throws RegistryException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(query);
            ps.setInt(1, tenantId);
            ps.executeUpdate();

        } catch (SQLException e) {
            String errMsg = "Error executing query " + query + " for tenant: " + tenantId;
            throw new RegistryException(errMsg, e);
        } finally {
            if (ps != null) {
                DatabaseUtil.closeAllConnections(null, ps);
            }
        }
    }
}
