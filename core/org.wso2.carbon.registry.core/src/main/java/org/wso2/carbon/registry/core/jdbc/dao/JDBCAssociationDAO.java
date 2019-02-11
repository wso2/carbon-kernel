/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core.jdbc.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.dao.AssociationDAO;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.DatabaseConstants;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDatabaseTransaction;
import org.wso2.carbon.registry.core.session.CurrentSession;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * An implementation of the {@link AssociationDAO} to store associations on a JDBC-based database.
 */
public class JDBCAssociationDAO implements AssociationDAO {

    private static final Log log = LogFactory.getLog(JDBCAssociationDAO.class);

    public void addAssociation(String sourcePath,
                               String targetPath,
                               String associationType) throws RegistryException {
        if (isAssociationExisting(sourcePath, targetPath, associationType)) {
            // not to duplicate the associations.
            return;
        }
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        PreparedStatement ps = null;
        try {
            String propSQL =
                    "INSERT INTO REG_ASSOCIATION (REG_SOURCEPATH, REG_TARGETPATH, " +
                            "REG_ASSOCIATION_TYPE, REG_TENANT_ID) VALUES (?,?,?,?)";
            ps = conn.prepareStatement(propSQL);
            ps.setString(1, sourcePath);
            ps.setString(2, targetPath);
            ps.setString(3, associationType);
            ps.setInt(4, CurrentSession.getTenantId());
            ps.executeUpdate();

        } catch (SQLException e) {

            String msg = "Failed to add association between resources " +
                    sourcePath + " and " + targetPath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    // Determines whether an association exists or not.
    private boolean isAssociationExisting(String sourcePath,
                                          String targetPath,
                                          String associationType) throws RegistryException {
        boolean isExisting = false;
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ResultSet result = null;
        PreparedStatement ps = null;
        try {
            String propSQL =
                    "SELECT REG_SOURCEPATH FROM REG_ASSOCIATION WHERE REG_SOURCEPATH=? AND " +
                            "REG_TARGETPATH=? AND REG_ASSOCIATION_TYPE=? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(propSQL);
            ps.setString(1, sourcePath);
            ps.setString(2, targetPath);
            ps.setString(3, associationType);
            ps.setInt(4, CurrentSession.getTenantId());
            result = ps.executeQuery();

            if (result.next()) {
                isExisting = true;
            }

        } catch (SQLException e) {
            String msg = "Failed to check the existence of the association between resources " +
                    sourcePath + " and " + targetPath + " for association type " +
                    associationType + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (result != null) {
                    result.close();
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
        return isExisting;
    }

    public void removeAssociation(String sourcePath,
                                  String targetPath,
                                  String associationType) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        PreparedStatement ps = null;
        try {
            String propSQL =
                    "DELETE FROM REG_ASSOCIATION WHERE REG_SOURCEPATH=? AND REG_TARGETPATH=? " +
                            "AND REG_ASSOCIATION_TYPE=? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(propSQL);
            ps.setString(1, sourcePath);
            ps.setString(2, targetPath);
            ps.setString(3, associationType);
            ps.setInt(4, CurrentSession.getTenantId());
            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to remove association between resources " +
                    sourcePath + " and " + targetPath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    public Association[] getAllAssociations(String resourcePath) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ArrayList<Association> allAssociations;
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            String propSQL = "SELECT REG_SOURCEPATH,REG_TARGETPATH,REG_ASSOCIATION_TYPE FROM " +
                    "REG_ASSOCIATION WHERE (REG_SOURCEPATH=? OR REG_TARGETPATH=?)" +
                    " AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(propSQL);
            ps.setString(1, resourcePath);
            ps.setString(2, resourcePath);
            ps.setInt(3, CurrentSession.getTenantId());
            result = ps.executeQuery();
            allAssociations = new ArrayList<Association>();

            while (result.next()) {
                Association association = new Association();
                association.setSourcePath(result.getString(DatabaseConstants.SOURCEPATH_FIELD));
                association
                        .setDestinationPath(result.getString(DatabaseConstants.TARGETPATH_FIELD));
                association.setAssociationType(
                        result.getString(DatabaseConstants.ASSOCIATION_TYPE_FIELD));
                allAssociations.add(association);
            }
        } catch (SQLException e) {
            String msg = "Failed to get all associations of resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (result != null) {
                        result.close();
                    }
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }

        return allAssociations.toArray(new Association[allAssociations.size()]);
    }

    public Association[] getAllAssociationsForType(String resourcePath, String associationType)
            throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet result = null;
        ArrayList<Association> associations;
        try {
            String propSQL = "SELECT REG_SOURCEPATH, REG_TARGETPATH FROM REG_ASSOCIATION " +
                    "WHERE (REG_SOURCEPATH=? OR REG_TARGETPATH=?) " +
                    "AND REG_ASSOCIATION_TYPE=? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(propSQL);
            ps.setString(1, resourcePath);
            ps.setString(2, resourcePath);
            ps.setString(3, associationType);
            ps.setInt(4, CurrentSession.getTenantId());

            result = ps.executeQuery();
            associations = new ArrayList<Association>();

            while (result.next()) {
                Association association = new Association();
                association.setSourcePath(result.getString(DatabaseConstants.SOURCEPATH_FIELD));
                association
                        .setDestinationPath(result.getString(DatabaseConstants.TARGETPATH_FIELD));
                association.setAssociationType(associationType);
                associations.add(association);
            }
        } catch (SQLException e) {
            String msg = "Failed to get associations of type " +
                    associationType + " for resource " + resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (result != null) {
                        result.close();
                    }
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }

        return associations.toArray(new Association[associations.size()]);
    }

    public void replaceAssociations(String oldPath, String newPath) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        PreparedStatement ps2 = null;
        try {
            String sql2 = "UPDATE REG_ASSOCIATION SET REG_TARGETPATH=? WHERE " +
                    "REG_TARGETPATH=? AND REG_TENANT_ID=?";

            ps2 = conn.prepareStatement(sql2);
            ps2.setString(1, newPath);
            ps2.setString(2, oldPath);
            ps2.setInt(3, CurrentSession.getTenantId());
            ps2.executeUpdate();

        } catch (SQLException e) {

            String msg = "Failed to replace the associations of " + oldPath +
                    " by re-associating them to " + newPath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                if (ps2 != null) {
                    ps2.close();
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    public void removeAllAssociations(String resourcePath) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        String sql = "DELETE FROM REG_ASSOCIATION WHERE (REG_SOURCEPATH=? " +
                "OR REG_TARGETPATH=?) AND REG_TENANT_ID=?";

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, resourcePath);
            ps.setString(2, resourcePath);
            ps.setInt(3, CurrentSession.getTenantId());
            ps.executeUpdate();

        } catch (SQLException e) {

            String msg = "Failed to remove associations of resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            }
            catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR +
                        ex.getMessage();
                log.error(msg, ex);
            }
        }
    }

    public void copyAssociations(String fromPath, String toPath) throws RegistryException {
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                    JDBCDatabaseTransaction.getConnection();
            String propSQL =
                    "SELECT REG_TARGETPATH, REG_ASSOCIATION_TYPE FROM REG_ASSOCIATION WHERE " +
                            "REG_SOURCEPATH=? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(propSQL);
            ps.setString(1, fromPath);
            ps.setInt(2, CurrentSession.getTenantId());
            result = ps.executeQuery();
            ArrayList<Association> arrayList = new ArrayList<Association>();

            // Read all results first
            while (result.next()) {
                Association association = new Association();
                association.setSourcePath(toPath);
                association
                        .setDestinationPath(result.getString(DatabaseConstants.TARGETPATH_FIELD));
                association.setAssociationType(
                        result.getString(DatabaseConstants.ASSOCIATION_TYPE_FIELD));
                arrayList.add(association);
            }

            // Then add associations
            for (Association association : arrayList) {
                addAssociation(toPath, association.getDestinationPath(),
                        association.getAssociationType());
            }
        } catch (SQLException e) {
            String msg = "SQLException occurred during copyAssociations";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (result != null) {
                        result.close();
                    }
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

}
