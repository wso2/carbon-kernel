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
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.dao.*;
import org.wso2.carbon.registry.core.dataaccess.DAOManager;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.ConcurrentModificationException;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.DatabaseConstants;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDatabaseTransaction;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.jdbc.utils.VersionRetriever;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.utils.VersionedPath;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of the {@link ResourceVersionDAO} to store resources on a JDBC-based database
 * with versioning enabled.
 */
public class JDBCResourceVersionDAO implements ResourceVersionDAO {

    private static Log log = LogFactory.getLog(JDBCResourceVersionDAO.class);
    private ResourceDAO resourceDAO;
    private CommentsDAO commentsDAO;
    private RatingsDAO ratingsDAO;
    private AssociationDAO associationDAO;
    private TagsDAO tagsDAO;
    private static final Object ADD_SNAPSHOT_LOCK = new Object();

    /**
     * Default constructor
     *
     * @param daoManager instance of the data access object manager.
     */
    public JDBCResourceVersionDAO(DAOManager daoManager) {
        this.resourceDAO = daoManager.getResourceDAO();
        this.commentsDAO = daoManager.getCommentsDAO(StaticConfiguration.isVersioningComments());
        this.ratingsDAO = daoManager.getRatingsDAO(StaticConfiguration.isVersioningRatings());
        this.tagsDAO = daoManager.getTagsDAO(StaticConfiguration.isVersioningTags());
        this.associationDAO = daoManager.getAssociationDAO();
    }

    public Long[] getSnapshotIDs(String resourcePath) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        ResultSet results = null;
        PreparedStatement ps = null;
        try {
            ResourceIDImpl resourceIdImpl = resourceDAO.getResourceID(resourcePath);
            if (resourceIdImpl == null) {
                return new Long[0];
            }
            if (resourceIdImpl.isCollection()) {
                String sql = "SELECT REG_SNAPSHOT_ID FROM REG_SNAPSHOT WHERE " +
                        "REG_PATH_ID=? AND REG_RESOURCE_NAME IS NULL AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceIdImpl.getPathID());
                ps.setInt(2, CurrentSession.getTenantId());
            } else {
                String sql = "SELECT REG_SNAPSHOT_ID FROM REG_SNAPSHOT WHERE " +
                        "REG_PATH_ID=? AND REG_RESOURCE_NAME=? AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceIdImpl.getPathID());
                ps.setString(2, resourceIdImpl.getName());
                ps.setInt(3, CurrentSession.getTenantId());
            }

            results = ps.executeQuery();
            List<Long> snapshotIDs = new ArrayList<Long>();
            while (results.next()) {
                long snapshotNumber = results.getLong(DatabaseConstants.SNAPSHOT_ID_FIELD);
                snapshotIDs.add(snapshotNumber);
            }
            Collections.sort(snapshotIDs, Collections.reverseOrder());

            return snapshotIDs.toArray(new Long[snapshotIDs.size()]);

        } catch (SQLException e) {

            String msg = "Failed to get snapshot numbers of resource " +
                    resourcePath + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (results != null) {
                        results.close();
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

    public void fillResourceContentArchived(ResourceImpl resourceImpl) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ResultSet result1 = null;
        PreparedStatement ps1 = null;
        try {
            // we can't use the UNION sql operator as some databases (derby) doesn't support
            // UNION with BLOB data
            // first check the current resource table
            String resourceContentSQL = "SELECT REG_CONTENT_DATA FROM REG_CONTENT_HISTORY WHERE " +
                    "REG_CONTENT_ID = ? AND REG_TENANT_ID=?";
            ps1 = conn.prepareStatement(resourceContentSQL);
            int contentId = resourceImpl.getDbBasedContentID();
            ps1.setInt(1, contentId);
            ps1.setInt(2, CurrentSession.getTenantId());
            result1 = ps1.executeQuery();
            if (result1.next()) {
                resourceImpl.setContentStreamWithNoUpdate(
                        RegistryUtils.getMemoryStream(
                                result1.getBinaryStream(DatabaseConstants.CONTENT_DATA_FIELD)));
            }
        }
        catch (SQLException ex) {
            String msg =
                    "Failed in filling resource content for resource " + resourceImpl.getPath() +
                            ex.getMessage();
            log.error(msg, ex);
            throw new RegistryException(msg, ex);
        }
        finally {
            try {
                try {
                    if (result1 != null) {
                        result1.close();
                    }
                } finally {
                    if (ps1 != null) {
                        ps1.close();
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    public ResourceImpl get(ResourceIDImpl resourceID, long snapshotID) throws RegistryException {
        VersionRetriever versionRetriever = getVersionList(snapshotID);
        ResourceDO resourceDO = null;
        int versionIndex = 0;
        while (true) {
            long version = versionRetriever.getVersion(versionIndex);
            if (version == -1) {
                // stream is over..
                break;
            }
            resourceDO = getResourceDOArchived(version);
            if (resourceDO.getPathID() == resourceID.getPathID() &&
                    ((resourceID.isCollection() && resourceDO.getName() == null) ||
                            (resourceID.getName() != null &&
                                    resourceID.getName().equals(resourceDO.getName())))) {
                break;
            }
            resourceDO = null;
            versionIndex++;
        }
        if (resourceDO == null) {
            String msg = "The resource was not found at " +
                    resourceID.getPath() + " for the snapshot " + snapshotID + ".";
            log.debug(msg);
            return null;
        }
        ResourceImpl resourceImpl;
        if (resourceID.isCollection()) {
            CollectionVersionImpl collectionImpl =
                    new CollectionVersionImpl(resourceID.getPath(), resourceDO);
            collectionImpl.setVersionListIndex(versionIndex);
            collectionImpl.setVersionList(versionRetriever);
            resourceImpl = collectionImpl;
            
            int tempTenantId = CurrentSession.getTenantId();
            if (tempTenantId != MultitenantConstants.SUPER_TENANT_ID &&
            		tempTenantId != MultitenantConstants.INVALID_TENANT_ID) {
                String[] childPaths = getChildPaths(resourceID,
                        versionRetriever, versionIndex,
                        0, -1, snapshotID, JDBCDatabaseTransaction.getConnection());
                collectionImpl.setContent(childPaths);
            }
        } else {
            resourceImpl = new ResourceImpl(resourceID.getPath(), resourceDO);
        }
        return resourceImpl;
    }

    public boolean resourceExists(ResourceIDImpl resourceID, long snapshotID)
            throws RegistryException {
        VersionRetriever versionRetriever = getVersionList(snapshotID);
        if (versionRetriever == null) {
            return false;   
        }
        ResourceDO resourceDO = null;
        int versionIndex = 0;
        while (true) {
            long version = versionRetriever.getVersion(versionIndex);
            if (version == -1) {
                // stream is over..
                break;
            }
            resourceDO = getResourceDOArchived(version);
            if (resourceDO.getPathID() == resourceID.getPathID() &&
                    ((resourceID.isCollection() && resourceDO.getName() == null) ||
                            (resourceID.getName() != null &&
                                    resourceID.getName().equals(resourceDO.getName())))) {
                break;
            }
            resourceDO = null;
            versionIndex++;
        }
        return resourceDO != null;
    }

    public VersionRetriever getVersionList(ResourceIDImpl resourceID, long snapshotID)
            throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ResultSet result = null;
        PreparedStatement ps = null;
        VersionRetriever versionRetriever = null;
        try {
            if (resourceID.isCollection()) {
                String sql =
                        "SELECT REG_PATH_ID, REG_RESOURCE_VIDS FROM REG_SNAPSHOT " +
                                "WHERE REG_SNAPSHOT_ID=? AND REG_PATH_ID = ? " +
                                "AND REG_RESOURCE_NAME IS NULL AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setLong(1, snapshotID);
                ps.setInt(2, resourceID.getPathID());
                ps.setInt(3, CurrentSession.getTenantId());
            } else {
                String sql =
                        "SELECT REG_PATH_ID, REG_RESOURCE_VIDS FROM REG_SNAPSHOT " +
                                "WHERE REG_SNAPSHOT_ID=? AND REG_PATH_ID = ? " +
                                "AND REG_RESOURCE_NAME=? AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setLong(1, snapshotID);
                ps.setInt(2, resourceID.getPathID());
                ps.setString(3, resourceID.getName());
                ps.setInt(4, CurrentSession.getTenantId());
            }

            result = ps.executeQuery();

            if (result.next()) {
                InputStream resourceVIDStream = RegistryUtils.getMemoryStream(
                        result.getBinaryStream(DatabaseConstants.RESOURCE_VIDS_FIELD));
                versionRetriever = new VersionRetriever(resourceVIDStream);
            }
        } catch (Exception e) {

            String msg = "Failed to get version of resource " + resourceID.getPath() +
                    " of snapshot " + snapshotID + ". " + e.getMessage();
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
        return versionRetriever;
    }

    public VersionRetriever getVersionList(long snapshotID) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ResultSet result = null;
        PreparedStatement ps = null;
        VersionRetriever versionRetriever = null;
        try {
            String sql = "SELECT REG_PATH_ID, REG_RESOURCE_VIDS FROM REG_SNAPSHOT WHERE " +
                    "REG_SNAPSHOT_ID=? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, snapshotID);
            ps.setInt(2, CurrentSession.getTenantId());

            result = ps.executeQuery();

            if (result.next()) {
                InputStream resourceVIDStream = RegistryUtils.getMemoryStream(
                        result.getBinaryStream(DatabaseConstants.RESOURCE_VIDS_FIELD));
                versionRetriever = new VersionRetriever(resourceVIDStream);
            }
        } catch (Exception e) {

            String msg =
                    "Failed to get version of the snapshot " + snapshotID + ". " + e.getMessage();
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
        return versionRetriever;
    }

    public CollectionImpl get(ResourceIDImpl resourceID, long snapshotID,
                              int start, int pageLen) throws RegistryException {
        if (!resourceID.isCollection()) {
            String msg = "Child resource range can only be specified for collections. " +
                    resourceID.getPath() + " is not a collection.";
            log.error(msg);
            throw new RegistryException(msg);
        }

        VersionRetriever versionRetriever = getVersionList(snapshotID);
        int versionIndex = 0;
        ResourceDO resourceDO = null;
        while (true) {
            long version = versionRetriever.getVersion(versionIndex);
            if (version == -1) {
                // no more stream
                break;
            }
            resourceDO = getResourceDOArchived(version);
            if (resourceDO.getPathID() == resourceID.getPathID() &&
                    ((resourceID.isCollection() && resourceDO.getName() == null) ||
                            (resourceID.getName() != null &&
                                    resourceID.getName().equals(resourceDO.getName())))) {
                break;
            }
            resourceDO = null;
            versionIndex++;
        }
        if (resourceDO == null) {
            String msg = "The resource was not found for " +
                    resourceID.getPath() + " for the snapshot " + snapshotID + ".";
            log.debug(msg);
            return null;
        }
        CollectionVersionImpl collectionImpl =
                new CollectionVersionImpl(resourceID.getPath(), resourceDO);
        collectionImpl.setVersionListIndex(versionIndex);
        collectionImpl.setVersionList(versionRetriever);

        fillChildren(collectionImpl, versionRetriever, versionIndex, start, pageLen, snapshotID);
        resourceDAO.fillResourceProperties(collectionImpl);

        return collectionImpl;
    }

    public void fillChildren(CollectionImpl collectionImpl, VersionRetriever versionRetriever,
                             int parentVersionIndex, int start, int pageLen, long snapshotID)
            throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        String[] childPaths = getChildPaths(collectionImpl.getResourceIDImpl(),
                versionRetriever, parentVersionIndex, start, pageLen, snapshotID, conn);
        collectionImpl.setContent(childPaths);
    }

    /**
     * Method to return a child count of a collection (database connection should also be provided)
     *
     * @param resourceID    the resource id of the collection object which the children are
     *                      calculated.
     * @param versionNumber the version number.
     * @param conn          the database connection.
     *
     * @return the child count.
     * @throws RegistryException throws if the operation failed.
     */
    @SuppressWarnings("unused")
    public int getChildCount(String resourceID, long versionNumber, Connection conn)
            throws RegistryException {

        ResultSet results = null;
        PreparedStatement ps = null;
        try {

            int childCount = 0;

            String sql = "SELECT REG_CHILD_RID FROM REG_DEPENDENCY_VERSION " +
                    "WHERE REG_PARENT_RID=? AND REG_PARENT_VERSION=? AND REG_TENANT_ID=?";

            ps = conn.prepareStatement(sql);
            ps.setString(1, resourceID);
            ps.setLong(2, versionNumber);
            ps.setInt(3, CurrentSession.getTenantId());

            results = ps.executeQuery();
            while (results.next()) {

                String childID = results.getString(DatabaseConstants.CHILD_RID_FIELD);

                if (AuthorizationUtils.authorize(childID, ActionConstants.GET)) {
                    childCount++;
                }
            }

            return childCount;

        } catch (SQLException e) {

            String msg = "Failed to get child count of resource " +
                    resourceID + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (results != null) {
                        results.close();
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

    public String[] getChildPaths(ResourceIDImpl resourceID, VersionRetriever versionRetriever,
                                  int parentVersionIndex, int start, int pageLen,
                                  long snapshotID, DataAccessManager dataAccessManager)
            throws RegistryException {
        String[] childPaths = null;

        if (Transaction.isStarted()) {
            childPaths = getChildPaths(resourceID,
                    versionRetriever, parentVersionIndex,
                    start, pageLen, snapshotID, JDBCDatabaseTransaction.getConnection());

        } else {

            Connection conn = null;
            boolean transactionSucceeded = false;
            try {
                conn = ((JDBCDataAccessManager)
                        dataAccessManager).getDataSource().getConnection();

                // If a managed connection already exists, use that instead of a new connection.
                JDBCDatabaseTransaction.ManagedRegistryConnection temp =
                        JDBCDatabaseTransaction.getManagedRegistryConnection(conn);
                if (temp != null) {
                    conn.close();
                    conn = temp;
                }
                if (conn.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED) {
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                }
                conn.setAutoCommit(false);

                childPaths = getChildPaths(resourceID,
                    versionRetriever, parentVersionIndex,
                    start, pageLen, snapshotID, conn);
                transactionSucceeded = true;
            } catch (SQLException e) {

                String msg = "Failed to get the child paths " + pageLen + " child paths from " +
                        start + " of resource " + resourceID.getPath() + ". " + e.getMessage();
                log.error(msg, e);
                throw new RegistryException(msg, e);

            } finally {
                if (transactionSucceeded) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        log.error("Failed to commit the database connection used in " +
                                "getting child paths of the collection " + resourceID.getPath());
                    }
                } else if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        log.error("Failed to rollback the database connection used in " +
                                "getting child paths of the collection " + resourceID.getPath());
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        log.error("Failed to close the database connection used in " +
                                "getting child paths of collection " + resourceID.getPath());
                    }
                }
            }
        }
        return childPaths;
    }

    /**
     * Get the child paths of a resource, (should be a collection)
     *
     * @param resourceID         the resource id of the collection.
     * @param versionRetriever   the version retriever to be used.
     * @param snapshotID         the snapshot id.
     * @param start              start value of the range of children.
     * @param pageLen            the length of the children to retrieve.
     * @param parentVersionIndex the version index of the parent.
     * @param conn               the database connection.
     *
     * @return an array of child paths.
     * @throws RegistryException throws if the operation failed.
     */
    public String[] getChildPaths(ResourceIDImpl resourceID, VersionRetriever versionRetriever,
                                  int parentVersionIndex,
                                  int start, int pageLen,
                                  long snapshotID, Connection conn) throws RegistryException {

        List<String> childPathList = new ArrayList<String>();
        String parentPath = getCurrentPath(resourceID.getPath());

        // we have the versionRetriever of the descendants collection, need to figure out up to
        // which one the collections are immediate child of the collectionImpl
        // skipping the index 0 to skip the itself
        int current = 0;
        int end = start + pageLen;
        boolean isValidPath = false;
        int versionIndex = parentVersionIndex + 1;
        while (true) {
            PreparedStatement ps = null;
            PreparedStatement ps2 = null;
            ResultSet result = null;
            ResultSet result2 = null;
            try {
                // make sure we are within limit
                if ((pageLen != -1 && current > end)) {
                    break;
                }

                long version = versionRetriever.getVersion(versionIndex);
                if (version == -1) {
                    // stream is over..
                    break;
                }
                String sql = "(SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE WHERE " +
                        "REG_VERSION=? AND REG_TENANT_ID=?)" +
                        "UNION " +
                        "(SELECT REG_PATH_ID, REG_NAME FROM REG_RESOURCE_HISTORY WHERE " +
                        "REG_VERSION=? AND REG_TENANT_ID=?)";

                ps = conn.prepareStatement(sql);
                ps.setLong(1, version);
                ps.setInt(2, CurrentSession.getTenantId());
                ps.setLong(3, version);
                ps.setInt(4, CurrentSession.getTenantId());
                result = ps.executeQuery();

                if (result.next()) {
                    int pathId = result.getInt(DatabaseConstants.PATH_ID_FIELD);
                    String resourceName = result.getString(DatabaseConstants.NAME_FIELD);
                    if (pathId == resourceID.getPathID() && resourceName != null) {
                        // this should be a child resource
                        String childPath = parentPath +
                                (parentPath.equals(RegistryConstants.PATH_SEPARATOR) ? "" :
                                        RegistryConstants.PATH_SEPARATOR) +
                                resourceName +
                                RegistryConstants.VERSION_SEPARATOR + snapshotID;
                        if (current >= start) {
                            childPathList.add(childPath);
                        }
                        isValidPath = true;
                        current++;
                    } else if (resourceName == null) {
                        // Could be child resources, was replaced to this from
                        // SELECTED REG_PATH_VALUE FROM REG_PATH WHERE REG_PATH_ID=?
                        // AND REG_PARENT_PATH_ID=?" to avoid another combination of indexes
                        sql = "SELECT REG_PATH_PARENT_ID, REG_PATH_VALUE FROM REG_PATH WHERE " +
                                "REG_PATH_ID=? AND REG_TENANT_ID=?";

                        ps2 = conn.prepareStatement(sql);
                        ps2.setLong(1, pathId);
                        ps2.setInt(2, CurrentSession.getTenantId());
                        result2 = ps2.executeQuery();
                        if (result2.next()) {
                            int parentPathId =
                                    result2.getInt(DatabaseConstants.PATH_PARENT_ID_FIELD);
                            if (parentPathId == resourceID.getPathID()) {
                                // so we confirm that this is a child of our collection
                                String childPath =
                                        result2.getString(DatabaseConstants.PATH_VALUE_FIELD) +
                                                RegistryConstants.VERSION_SEPARATOR + snapshotID;
                                if (current >= start) {
                                    childPathList.add(childPath);
                                }
                                isValidPath = true;
                                current++;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                String msg = "Failed to get child paths of resource " +
                        resourceID.getPath() + "for version. " + e.getMessage();
                log.error(msg, e);
                throw new RegistryException(msg, e);
            } finally {
                // closing open prepared statements & result sets before moving on to next iteration
                try {
                    try {
                        if (result != null) {
                            result.close();
                        }
                    } finally {
                        try {
                            if (ps != null) {
                                ps.close();
                            }
                        } finally {
                            try {
                                if (result2 != null) {
                                    result2.close();
                                }
                            } finally {
                                if (ps2 != null) {
                                    ps2.close();
                                }
                            }
                        }
                    }
                } catch (SQLException ex) {
                    String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                    log.error(msg, ex);
                }
            }
            if (!isValidPath && current > 0) {
                break;
            }
            versionIndex++;
        }
        return childPathList.toArray(new String[childPathList.size()]);
    }

    // Utility method to get the current path.
    private String getCurrentPath(String resourcePath) {
        String currentPath = resourcePath;
        if (resourcePath.indexOf('?') > 0) {
            currentPath = resourcePath.split("\\?")[0];
        } else if (resourcePath.indexOf(RegistryConstants.URL_SEPARATOR) > 0) {
            currentPath = resourcePath.split("\\;")[0];
        }

        return currentPath;
    }

    public long createSnapshot(int pathId, String name, InputStream versionsStream)
            throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet result = null;
        try {
            String sql =
                    "INSERT INTO REG_SNAPSHOT (REG_PATH_ID, REG_RESOURCE_NAME, " +
                            "REG_RESOURCE_VIDS, REG_TENANT_ID) VALUES (?, ?, ?, ?)";
            String sql1 = "SELECT MAX(REG_SNAPSHOT_ID) FROM REG_SNAPSHOT";

            int size = versionsStream.available();
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            boolean returnsGeneratedKeys = DBUtils.canReturnGeneratedKeys(dbProductName);
            if (returnsGeneratedKeys) {
                ps = conn.prepareStatement(sql, new String[]{
                        DBUtils.getConvertedAutoGeneratedColumnName(dbProductName,
                                "REG_SNAPSHOT_ID")});
            } else {
                ps = conn.prepareStatement(sql);
            }
            ps.setInt(1, pathId);
            ps.setString(2, name);
            ps.setBinaryStream(3, versionsStream, size);
            ps.setInt(4, CurrentSession.getTenantId());
            if (returnsGeneratedKeys) {
                ps.executeUpdate();
                result = ps.getGeneratedKeys();
            } else {
                synchronized (ADD_SNAPSHOT_LOCK) {
                    ps.executeUpdate();
                    if (dbProductName.equals("OpenEdge RDBMS")) {
                        String sql2 = "UPDATE REG_SNAPSHOT SET REG_SNAPSHOT_ID = " +
                                "PUB.REG_SNAPSHOT_SEQUENCE.NEXTVAL WHERE REG_SNAPSHOT_ID = 0";
                        PreparedStatement ps2 = null;
                        try {
                            ps2 = conn.prepareStatement(sql2);
                            ps2.executeUpdate();
                        } finally {
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                    }
                    ps1 = conn.prepareStatement(sql1);
                    result = ps1.executeQuery();
                }
            }
            long snapshotID = -1;
            if (result.next()) {
                snapshotID = result.getLong(1);
            }
            return snapshotID;
        } catch (Exception e) {

            String msg = "Failed to write resource content to the database. " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (result != null) {
                        result.close();
                    }
                } finally {
                    try {
                        if (ps1 != null) {
                            ps1.close();
                        }
                    } finally {
                        if (ps != null) {
                            ps.close();
                        }
                    }
                }
            } catch (SQLException ex) {
                String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                log.error(msg, ex);
            }
        }
    }

    public boolean isResourceHistoryExist(long version) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            String sql =
                    "SELECT REG_PATH_ID FROM REG_RESOURCE_HISTORY WHERE REG_VERSION=? " +
                            "AND REG_TENANT_ID=?";

            ps = conn.prepareStatement(sql);
            ps.setLong(1, version);
            ps.setInt(2, CurrentSession.getTenantId());
            result = ps.executeQuery();
            if (result.next()) {
                return true;
            }

        } catch (SQLException e) {
            String msg = "Failed reading the history table for resource version  " +
                    version + " . " + e.getMessage();
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
        return false;
    }

    public boolean isResourceHistoryExist(String path) throws RegistryException {

        // first assuming it is a collection
        ResourceIDImpl resourceID = resourceDAO.getResourceID(path, true);
        if (resourceID != null) {
            return isResourceHistoryExist(resourceID);
        }

        // second assuming it is a resource
        resourceID = resourceDAO.getResourceID(path, false);
        return resourceID == null || isResourceHistoryExist(resourceID);

    }

    public boolean isResourceHistoryExist(ResourceIDImpl resourceID) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ResultSet result = null;
        PreparedStatement ps = null;
        try {
            if (resourceID.isCollection()) {
                String sql = "SELECT REG_PATH_ID FROM REG_RESOURCE_HISTORY WHERE " +
                        "REG_PATH_ID=? AND REG_NAME IS NULL AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceID.getPathID());
                ps.setInt(2, CurrentSession.getTenantId());
                result = ps.executeQuery();
                if (result.next()) {
                    return true;
                }
            } else {
                String sql = "SELECT REG_PATH_ID FROM REG_RESOURCE_HISTORY WHERE " +
                        "REG_PATH_ID=? AND REG_NAME = ? AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceID.getPathID());
                ps.setString(2, resourceID.getName());
                ps.setInt(3, CurrentSession.getTenantId());
                result = ps.executeQuery();
                if (result.next()) {
                    return true;
                }
            }

        } catch (SQLException e) {
            String msg = "Failed reading the history table for resource path " +
                    resourceID.getPath() + " . " + e.getMessage();
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
        return false;
    }

    public boolean isContentHistoryExist(int contentId) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ResultSet result = null;
        PreparedStatement ps = null;
        try {
            String sql = "SELECT REG_CONTENT_DATA FROM REG_CONTENT_HISTORY WHERE " +
                    "REG_CONTENT_ID = ? AND REG_TENANT_ID=?";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, contentId);
            ps.setInt(2, CurrentSession.getTenantId());
            result = ps.executeQuery();
            if (result.next()) {
                return true;
            }
        } catch (SQLException e) {
            String msg = "Failed reading the history table for content " +
                    contentId + " . " + e.getMessage();
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
        return false;
    }

    public String restoreResources(long version, long snapshotID) throws RegistryException {
        // get the archived resource
        ResourceDO resourceDO = getResourceDOArchived(version);
        String resourcePath =
                resourceDAO.getPath(resourceDO.getPathID(), resourceDO.getName(), false);

        // create the resourceId
        ResourceImpl oldResource;
        if (resourceDO.getName() == null) {
            // this is a collection
            oldResource = new CollectionImpl(resourcePath, resourceDO);
        } else {
            oldResource = new ResourceImpl(resourcePath, resourceDO);
        }

        int oldContentID = resourceDO.getContentID();
        if (oldContentID > 0) {
            // if the non-collection restore content
            // get the archived content
            InputStream contentData = null;
            try {
                contentData = getContentArchived(oldContentID);
                if (contentData != null) {
                    resourceDO.setContentID(resourceDAO.addContentBytes(contentData));
                }
            } finally {
                if (contentData != null) {
                    try {
                        contentData.close();
                    } catch (IOException e) {
                        log.error("Failed to close the stream", e);
                    }
                }
            }
        }
        resourceDAO.addResourceDO(resourceDO);
        // copy comments, taggings, ratings to new version
        ResourceImpl newResource;
        if (resourceDO.getName() == null) {
            newResource = new CollectionImpl(resourcePath, resourceDO);
        } else {
            newResource = new ResourceImpl(resourcePath, resourceDO);
        }

        if (StaticConfiguration.isVersioningProperties()) {
            resourceDAO.fillResourceProperties(oldResource);
            newResource.setProperties(oldResource.getProperties());
            resourceDAO.addProperties(newResource);
            String linkRestoration = newResource.getProperty(
                    RegistryConstants.REGISTRY_LINK_RESTORATION);
            if (linkRestoration != null) {
                String[] parts = linkRestoration.split(RegistryConstants.URL_SEPARATOR);
                if (parts.length == 4) {
                    if (parts[2] != null && parts[2].length() == 0) {
                        parts[2] = null;
                    }
                    RegistryUtils.registerHandlerForRemoteLinks(RegistryContext.getBaseInstance(),
                            parts[0], parts[1], parts[2], parts[3]);
                } else if (parts.length == 3) {
                    RegistryUtils.registerHandlerForSymbolicLinks(RegistryContext.getBaseInstance(),
                            parts[0], parts[1], parts[2]);
                }
            }
        }

        commentsDAO.copyComments(oldResource, newResource);
        tagsDAO.copyTags(oldResource, newResource);
        ratingsDAO.copyRatings(oldResource, newResource);
        VersionedPath versionedPath = new VersionedPath();
        versionedPath.setVersion(snapshotID);
        versionedPath.setPath(oldResource.getPath());
        associationDAO.removeAllAssociations(newResource.getPath());
        associationDAO.copyAssociations(versionedPath.toString(), newResource.getPath());

        // finally return the resource path
        return resourcePath;
    }

    // get the archived resource DO
    private ResourceDO getResourceDOArchived(long version) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ResultSet result = null;
        PreparedStatement ps = null;
        try {
            String sql =
                    "SELECT REG_PATH_ID, REG_NAME, REG_VERSION, REG_MEDIA_TYPE, REG_CREATOR, " +
                            "REG_CREATED_TIME, REG_LAST_UPDATOR, REG_LAST_UPDATED_TIME, " +
                            "REG_DESCRIPTION, REG_CONTENT_ID, REG_UUID " +
                            "FROM REG_RESOURCE_HISTORY WHERE REG_VERSION =? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(sql);
            ps.setLong(1, version);
            ps.setInt(2, CurrentSession.getTenantId());

            result = ps.executeQuery();

            if (result.next()) {
                ResourceDO resourceDO = new ResourceDO();

                // this is always the current version of the resource
                resourceDO.setPathID(result.getInt(DatabaseConstants.PATH_ID_FIELD));
                resourceDO.setName(result.getString(DatabaseConstants.NAME_FIELD));
                resourceDO.setVersion(result.getInt(DatabaseConstants.VERSION_FIELD));
                resourceDO.setMediaType(result.getString(DatabaseConstants.MEDIA_TYPE_FIELD));
                resourceDO.setAuthor(result.getString(DatabaseConstants.CREATOR_FIELD));
                resourceDO.setCreatedOn(
                        result.getTimestamp(DatabaseConstants.CREATED_TIME_FIELD).getTime());
                resourceDO.setLastUpdater(result.getString(DatabaseConstants.LAST_UPDATER_FIELD));
                resourceDO.setLastUpdatedOn(
                        result.getTimestamp(DatabaseConstants.LAST_UPDATED_TIME_FIELD).getTime());
                resourceDO.setDescription(result.getString(DatabaseConstants.DESCRIPTION_FIELD));
                resourceDO.setContentID(result.getInt(DatabaseConstants.CONTENT_ID_FIELD));
                resourceDO.setUUID(result.getString(DatabaseConstants.UUID_FIELD));

                return resourceDO;
            }
        } catch (SQLException e) {

            String msg = "Failed to get the resource version " +
                    version + ". " + e.getMessage();
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
        return null;
    }

    // Get the archived content.
    private InputStream getContentArchived(int contentID) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ResultSet result = null;
        PreparedStatement ps = null;
        try {

            String resourceContentSQL =
                    "SELECT REG_CONTENT_DATA  FROM  REG_CONTENT_HISTORY WHERE " +
                            "REG_CONTENT_ID = ? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(resourceContentSQL);
            ps.setLong(1, contentID);
            ps.setInt(2, CurrentSession.getTenantId());
            result = ps.executeQuery();
            if (result.next()) {
                InputStream rawStream =
                        result.getBinaryStream(DatabaseConstants.CONTENT_DATA_FIELD);
                if (rawStream != null) {
                    return RegistryUtils.getMemoryStream(rawStream);
                }
            }
        } catch (SQLException e) {

            String msg = "Failed to get the archived content " +
                    contentID + ". " + e.getMessage();
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
        return null;
    }

    public void versionResource(ResourceDO resourceDO, boolean keepProperties)
            throws RegistryException {
        if (resourceDO.getName() != null) {
            // this implies this is a resource
            int contentID = resourceDO.getContentID();
            if (contentID > 0) {
                // the content stream is deleted later within this function to avoid violation
                // of foreign key constrains
                versionContent(contentID);
            }
        }
        // now write the query to copy the old to the history table.
        if (!isResourceHistoryExist(resourceDO.getVersion())) {
            putResourceToHistory(resourceDO);
        }

        // remove the modified non-versioned resources
        if (!StaticConfiguration.isVersioningProperties() && !keepProperties) {
            resourceDAO.removeProperties(resourceDO);
        }

        // delete the old entry from the resource table
        resourceDAO.deleteResource(resourceDO);

        // version the content as well
        if (resourceDO.getName() != null) {

            int contentID = resourceDO.getContentID();
            if (contentID > 0) {
                // delete the old content stream from the latest table
                resourceDAO.deleteContentStream(contentID);
            }
        }
    }

    public void putResourceToHistory(ResourceDO resourceDO) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            String sql =
                    "INSERT INTO REG_RESOURCE_HISTORY (REG_PATH_ID, REG_NAME, REG_VERSION, " +
                            "REG_MEDIA_TYPE, REG_CREATOR, REG_CREATED_TIME, REG_LAST_UPDATOR, " +
                            "REG_LAST_UPDATED_TIME, REG_DESCRIPTION, " +
                            "REG_CONTENT_ID, REG_TENANT_ID, REG_UUID) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);

            ps.setInt(1, resourceDO.getPathID());
            ps.setString(2, resourceDO.getName());
            ps.setLong(3, resourceDO.getVersion());
            ps.setString(4, resourceDO.getMediaType());
            ps.setString(5, resourceDO.getAuthor());
            ps.setTimestamp(6, new Timestamp(resourceDO.getCreatedOn()));
            ps.setString(7, resourceDO.getLastUpdater());
            ps.setTimestamp(8, new Timestamp(resourceDO.getLastUpdatedOn()));
            ps.setString(9, resourceDO.getDescription());
            if (resourceDO.getContentID() > 0) {
                ps.setInt(10, resourceDO.getContentID());
            } else {
                ps.setNull(10, Types.INTEGER);
            }
            ps.setInt(11, CurrentSession.getTenantId());
            ps.setString(12,resourceDO.getUUID());
            ps.executeUpdate();

        }
        catch (SQLException ex) {
            String msg = "Failed to copy resource version" + resourceDO.getVersion() +
                    " to the history table " + ex.getMessage();
            log.error(msg, ex);
            throw new RegistryException(msg, ex);
        }
        finally {
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

    // Create a version of the resource content.
    private void versionContent(int contentID) throws RegistryException {

        if (!isContentHistoryExist(contentID)) {
            InputStream contentStream = resourceDAO.getContentStream(contentID);
            if (contentStream == null) {
                // create an empty input stream
                contentStream = new ByteArrayInputStream(RegistryUtils.encodeString(""));
            }
            // copy the content to the content_history table
            JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                    JDBCDatabaseTransaction.getConnection();
            PreparedStatement ps = null;
            try {

                String sql =
                        "INSERT INTO REG_CONTENT_HISTORY (REG_CONTENT_ID, REG_CONTENT_DATA, " +
                                "REG_TENANT_ID) VALUES (?, ?, ?)";

                int size = contentStream.available();
                ps = conn.prepareStatement(sql);
                ps.setInt(1, contentID);
                ps.setBinaryStream(2, contentStream, size);
                ps.setInt(3, CurrentSession.getTenantId());
                ps.executeUpdate();

            } catch (Exception ex) {
                String msg = "Failed to put the content into history with the content id " +
                        contentID + ". " + ex.getMessage();
                if (isContentHistoryExist(contentID)) {
                    log.error("Concurrent Modification: " + msg, ex);
                    throw new ConcurrentModificationException(msg, ex);
                }
                log.error(msg, ex);
                throw new RegistryException(msg, ex);
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
    }    
    
    public void removeSnapshot(long snapshotId) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
            PreparedStatement ps = null;
            try {

                String sql = "DELETE FROM REG_SNAPSHOT WHERE REG_SNAPSHOT_ID = ? ";
                
                ps = conn.prepareStatement(sql);
                ps.setLong(1, snapshotId);                
                ps.executeUpdate();

            } catch (Exception ex) {
                String msg = "Failed to remove the snapshot with the id: " +
                        snapshotId + ". " + ex.getMessage();                
                log.error(msg, ex);
                throw new RegistryException(msg, ex);
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
    
    
    public void removePropertyValues(long regVersionId) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
         PreparedStatement ps1 = null;
         PreparedStatement ps2 = null;
         
         try {       	 
        	        	 
        	 // Get the ids to be removed
        	 List<Long> idList = getPropertyIds(regVersionId);
        	 
        	 String sqlChild = "DELETE FROM REG_RESOURCE_PROPERTY WHERE REG_PROPERTY_ID = ? AND REG_TENANT_ID = ? ";
        	 String sqlParent = "DELETE FROM REG_PROPERTY WHERE REG_ID = ? AND REG_TENANT_ID = ?";
        	 
        	 ps1 = conn.prepareStatement(sqlChild);
        	 ps2 = conn.prepareStatement(sqlParent);
        	 
        	 for(long id : idList){
        		ps1.setLong(1, id);
        		ps1.setInt(2, CurrentSession.getTenantId());        		
        		ps2.setLong(1, id);
        		ps2.setInt(2, CurrentSession.getTenantId());
        		ps1.executeUpdate();
        		ps2.executeUpdate();        		 
        	 }
         } catch (Exception ex) {
             String msg = "Failed to remove the properties with the version id: " +
                     regVersionId + ". " + ex.getMessage();                
             log.error(msg, ex);
             throw new RegistryException(msg, ex);
         } finally {
             try {
                 try {
                     if (ps1 != null) {
                         ps1.close();
                     }
                 } finally {
                     if (ps2 != null) {
                         ps2.close();
                     }
                 }
             } catch (SQLException ex) {
                 String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                 log.error(msg, ex);
             }
         }   	
    }
    
    private List<Long> getPropertyIds(long regVersionId) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet results = null;
        List<Long> idList = new ArrayList<Long>();
    	
        try {       	
        		
        		String sql = "SELECT P.REG_ID FROM REG_PROPERTY P, REG_RESOURCE_PROPERTY RP WHERE " +
        			"RP.REG_VERSION = ? AND RP.REG_TENANT_ID = ? AND RP.REG_TENANT_ID=P.REG_TENANT_ID " +
        			"AND RP.REG_PROPERTY_ID=P.REG_ID";
        	
        		ps = conn.prepareStatement(sql);        		
        		ps.setLong(1, regVersionId);        		
        		ps.setInt(2, CurrentSession.getTenantId());
        		
        		results =  ps.executeQuery();        	
        	
        	while(results.next()){
        		idList.add(results.getLong(1));       		
        	}        	
        } catch (Exception ex) {
            String msg = "Failed to retrieve the properties with the REG_VERSION: " +
                    regVersionId + ". " + ex.getMessage();                
            log.error(msg, ex);
            throw new RegistryException(msg, ex);
        } finally {
            try {
                try {
                    if (results != null) {
                        results.close();
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
    	return idList;
    }   
        
}
