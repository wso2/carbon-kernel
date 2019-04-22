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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.config.StaticConfiguration;
import org.wso2.carbon.registry.core.dao.ResourceDAO;
import org.wso2.carbon.registry.core.dataaccess.DataAccessManager;
import org.wso2.carbon.registry.core.exceptions.*;
import org.wso2.carbon.registry.core.exceptions.ConcurrentModificationException;
import org.wso2.carbon.registry.core.jdbc.DatabaseConstants;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDataAccessManager;
import org.wso2.carbon.registry.core.jdbc.dataaccess.JDBCDatabaseTransaction;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.CurrentSession;
import org.wso2.carbon.registry.core.utils.AuthorizationUtils;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.DBUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * An implementation of the {@link ResourceDAO} to store resources on a JDBC-based database.
 */
public class JDBCResourceDAO implements ResourceDAO {

    private static final Log log = LogFactory.getLog(JDBCResourceDAO.class);

    private static final Object ADD_RESOURCE_LOCK = new Object();
    private static final Object ADD_CONTENT_LOCK = new Object();
    private static final Object ADD_PROPERTY_LOCK = new Object();

    private static final String SELECT_NAME_VALUE_PROP_P = "SELECT REG_NAME, REG_VALUE FROM REG_PROPERTY P, ";

    public ResourceIDImpl getResourceID(String path) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        try {
            // step1: need to check whether it is a resource or collection while retrieving path id
            JDBCPathCache pathCache = JDBCPathCache.getPathCache();
            int pathID = pathCache.getPathID(conn, path);
            boolean isCollection = true;
            if (pathID == -1) {
                isCollection = false;
            }

            // step2: get the resource name + path id for non-collection
            // (for collection this is already done from the step1
            String resourceName = null;
            if (!isCollection) {
                // we have to re-get the path id for the parent path..
                String parentPath = RegistryUtils.getParentPath(path);
                resourceName = RegistryUtils.getResourceName(path);

                pathID = pathCache.getPathID(conn, parentPath);
            }

            if (pathID != -1) {
                ResourceIDImpl resourceID = new ResourceIDImpl();
                resourceID.setCollection(isCollection);
                resourceID.setName(resourceName);
                resourceID.setPathID(pathID);
                resourceID.setPath(path);
                return resourceID;
            }


        } catch (SQLException e) {
            String msg = "Failed to get ID of the resource at path " + path + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        return null;
    }

    public ResourceIDImpl getResourceID(String path, boolean isCollection)
            throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        try {
            // step1: need to check whether it is a resource or collection while retrieving path id

            int pathID;
            String resourceName = null;
            if (isCollection) {
                pathID = JDBCPathCache.getPathCache().getPathID(conn, path);
            } else {
                // we have to re-get the path id for the parent path..
                String parentPath;
                if (path.equals(RegistryConstants.ROOT_PATH)) {
                    parentPath = null;
                } else {
                    parentPath = RegistryUtils.getParentPath(path);
                }
                resourceName = RegistryUtils.getResourceName(path);

                pathID = JDBCPathCache.getPathCache().getPathID(conn, parentPath);
            }

            if (pathID != -1) {
                ResourceIDImpl resourceID = new ResourceIDImpl();
                resourceID.setCollection(isCollection);
                resourceID.setName(resourceName);
                resourceID.setPathID(pathID);
                resourceID.setPath(path);
                return resourceID;
            }


        } catch (SQLException e) {
            String msg = "Failed to get ID of the resource at path " + path + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }

        return null;
    }

    public boolean resourceExists(ResourceIDImpl resourceID) throws RegistryException {
        return getVersion(resourceID) != -1;
    }

    public boolean resourceExists(String path) throws RegistryException {
        ResourceIDImpl resourceID = getResourceID(path);
        if (resourceID == null) {
            // resource definitely doesn't exists
            return false;
        }
        if (resourceExists(resourceID)) {
            // resource definitely exist
            return true;
        }
        // if resourceID is a collection we have to check resourceID with non-collection as well..
        return resourceID.isCollection() && resourceExists(path, false);
    }

    public boolean resourceExists(String path, boolean isCollection) throws RegistryException {
        ResourceIDImpl resourceID = getResourceID(path, isCollection);
        return resourceID != null && resourceExists(resourceID);
    }

    public long getVersion(ResourceIDImpl resourceID) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet result = null;
        long version = -1;
        try {
            if (resourceID.isCollection()) {
                String sql = "SELECT REG_VERSION FROM REG_RESOURCE WHERE " +
                        "REG_PATH_ID=? AND REG_NAME IS NULL AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceID.getPathID());
                ps.setInt(2, CurrentSession.getTenantId());

                result = ps.executeQuery();
                if (result.next()) {
                    version = result.getLong(DatabaseConstants.VERSION_FIELD);
                }
            } else {
                String sql = "SELECT REG_VERSION FROM REG_RESOURCE WHERE " +
                        "REG_PATH_ID=? AND REG_NAME=? AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceID.getPathID());
                ps.setString(2, resourceID.getName());
                ps.setInt(3, CurrentSession.getTenantId());

                result = ps.executeQuery();
                if (result.next()) {
                    version = result.getLong(DatabaseConstants.VERSION_FIELD);
                }
            }
        } catch (SQLException e) {
            String msg = "Failed to check the existence of the resource " +
                    resourceID.getPath() + ". " + e.getMessage();
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

        return version;
    }

    public ResourceImpl get(String path) throws RegistryException {
        ResourceIDImpl resourceID = getResourceID(path);
        if (resourceID == null) {
            return null;
        }
        return get(resourceID);
    }

    public ResourceImpl get(ResourceIDImpl resourceID) throws RegistryException {
        ResourceImpl resourceImpl = getResource(resourceID);
        if (resourceImpl == null) {
            // it is possible the resource doesn't exist
            return null;
        }
        fillResourceProperties(resourceImpl);
        return resourceImpl;
    }

    public CollectionImpl get(String path, int start, int pageLen) throws RegistryException {
        ResourceIDImpl resourceID = getResourceID(path);
        if (resourceID == null) {
            return null;
        }
        return get(resourceID, start, pageLen);
    }

    public CollectionImpl get(ResourceIDImpl resourceID, int start, int pageLen)
            throws RegistryException {
        CollectionImpl collection = (CollectionImpl) getResource(resourceID);
        if (collection == null) {
            // it is possible the resource doesn't exist
            return null;
        }
        fillChildren(collection, start, pageLen);
        fillResourceProperties(collection);
        return collection;
    }

    public void fillResource(ResourceImpl resourceImpl) throws RegistryException {
        if (resourceImpl == null) {
            throw new RegistryException("Unable to fill null resource");
        }
        if (!(resourceImpl instanceof CollectionImpl) &&
                resourceImpl.getDbBasedContentID() > 0) {
            fillResourceContentWithNoUpdate(resourceImpl);
        } else {
			if (resourceImpl instanceof CollectionImpl) {
				int tempTenantId = CurrentSession.getTenantId();

				if (tempTenantId != MultitenantConstants.INVALID_TENANT_ID &&
						tempTenantId != MultitenantConstants.SUPER_TENANT_ID) {
					JDBCDatabaseTransaction.ManagedRegistryConnection conn = JDBCDatabaseTransaction
							.getConnection();
					fillChildren((CollectionImpl) resourceImpl, 0, -1, conn);
				}
			}
        }
        
        fillResourcePropertiesWithNoUpdate(resourceImpl);
    }

    public void fillResource(CollectionImpl collection, int start, int pageLen)
            throws RegistryException {
        fillChildren(collection, start, pageLen);
        fillResourcePropertiesWithNoUpdate(collection);
    }

    public void fillResourcePropertiesWithNoUpdate(ResourceImpl resourceImpl)
            throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet results = null;
        try {
            if (StaticConfiguration.isVersioningProperties()) {
                String propSQL =
                        SELECT_NAME_VALUE_PROP_P +
                                "REG_RESOURCE_PROPERTY RP " +
                                "WHERE P.REG_ID=RP.REG_PROPERTY_ID AND RP.REG_VERSION=? AND " +
                                "P.REG_TENANT_ID=RP.REG_TENANT_ID AND RP.REG_TENANT_ID=? ORDER BY P.REG_ID";
                ps = conn.prepareStatement(propSQL);
                ps.setLong(1, resourceImpl.getVersionNumber());
                ps.setInt(2, CurrentSession.getTenantId());
                results = ps.executeQuery();
            } else if (resourceImpl instanceof CollectionImpl) {
                String propSQL =
                        SELECT_NAME_VALUE_PROP_P +
                                "REG_RESOURCE_PROPERTY RP WHERE P.REG_ID=RP.REG_PROPERTY_ID " +
                                "AND RP.REG_PATH_ID=? AND RP.REG_RESOURCE_NAME IS NULL " +
                                "AND P.REG_TENANT_ID=RP.REG_TENANT_ID AND RP.REG_TENANT_ID=? ORDER BY P.REG_ID";
                ps = conn.prepareStatement(propSQL);
                ResourceIDImpl resourceID = resourceImpl.getResourceIDImpl();
                ps.setLong(1, resourceID.getPathID());
                ps.setInt(2, CurrentSession.getTenantId());
                results = ps.executeQuery();
            } else {
                String propSQL =
                        SELECT_NAME_VALUE_PROP_P +
                                "REG_RESOURCE_PROPERTY RP WHERE P.REG_ID=RP.REG_PROPERTY_ID " +
                                "AND RP.REG_PATH_ID=? AND RP.REG_RESOURCE_NAME=? " +
                                "AND P.REG_TENANT_ID=RP.REG_TENANT_ID AND RP.REG_TENANT_ID=? ORDER BY P.REG_ID";
                ps = conn.prepareStatement(propSQL);
                ResourceIDImpl resourceID = resourceImpl.getResourceIDImpl();
                ps.setLong(1, resourceID.getPathID());
                ps.setString(2, resourceID.getName());
                ps.setInt(3, CurrentSession.getTenantId());
                results = ps.executeQuery();
            }

            while (results.next()) {
                String name = results.getString(DatabaseConstants.NAME_FIELD);
                String value = results.getString(DatabaseConstants.VALUE_FIELD);
                resourceImpl.addPropertyWithNoUpdate(name, value);
            }
        } catch (SQLException e) {
            String msg = "Failed to add properties to the resource " +
                    resourceImpl.getPath() + ". " + e.getMessage();
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

    public void fillResourceProperties(ResourceImpl resourceImpl) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet results = null;
        try {
            if (StaticConfiguration.isVersioningProperties()) {
                String propSQL =
                        SELECT_NAME_VALUE_PROP_P +
                                "REG_RESOURCE_PROPERTY RP " +
                                "WHERE P.REG_ID=RP.REG_PROPERTY_ID AND RP.REG_VERSION=? AND " +
                                "P.REG_TENANT_ID=RP.REG_TENANT_ID AND RP.REG_TENANT_ID=? ORDER BY P.REG_ID";
                ps = conn.prepareStatement(propSQL);
                ps.setLong(1, resourceImpl.getVersionNumber());
                ps.setInt(2, CurrentSession.getTenantId());
                results = ps.executeQuery();
            } else if (resourceImpl instanceof CollectionImpl) {
                String propSQL =
                        SELECT_NAME_VALUE_PROP_P +
                                "REG_RESOURCE_PROPERTY RP WHERE P.REG_ID=RP.REG_PROPERTY_ID " +
                                "AND RP.REG_PATH_ID=? AND RP.REG_RESOURCE_NAME IS NULL " +
                                "AND P.REG_TENANT_ID=RP.REG_TENANT_ID AND RP.REG_TENANT_ID=? ORDER BY P.REG_ID";
                ps = conn.prepareStatement(propSQL);
                ResourceIDImpl resourceID = resourceImpl.getResourceIDImpl();
                ps.setLong(1, resourceID.getPathID());
                ps.setInt(2, CurrentSession.getTenantId());
                results = ps.executeQuery();
            } else {
                String propSQL =
                        SELECT_NAME_VALUE_PROP_P +
                                "REG_RESOURCE_PROPERTY RP WHERE P.REG_ID=RP.REG_PROPERTY_ID " +
                                "AND RP.REG_PATH_ID=? AND RP.REG_RESOURCE_NAME=? " +
                                "AND P.REG_TENANT_ID=RP.REG_TENANT_ID AND RP.REG_TENANT_ID=? ORDER BY P.REG_ID";
                ps = conn.prepareStatement(propSQL);
                ResourceIDImpl resourceID = resourceImpl.getResourceIDImpl();
                ps.setLong(1, resourceID.getPathID());
                ps.setString(2, resourceID.getName());
                ps.setInt(3, CurrentSession.getTenantId());
                results = ps.executeQuery();
            }

            while (results.next()) {
                String name = results.getString(DatabaseConstants.NAME_FIELD);
                String value = results.getString(DatabaseConstants.VALUE_FIELD);
                resourceImpl.addProperty(name, value);
            }
        } catch (SQLException e) {
            String msg = "Failed to add properties to the resource " +
                    resourceImpl.getPath() + ". " + e.getMessage();
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
        resourceImpl.setPropertiesModified(false);
    }

    public void addRoot(ResourceImpl resourceImpl)
            throws RegistryException {
        String path = RegistryConstants.ROOT_PATH;
        createAndApplyResourceID(path, null, resourceImpl);

        addResourceWithoutContentId(resourceImpl, false);
        addProperties(resourceImpl);
    }

    public void add(String path, ResourceIDImpl parentID, ResourceImpl resourceImpl)
            throws RegistryException {
        // creating the resourceID
        createAndApplyResourceID(path, parentID, resourceImpl);

        addResourceWithoutContentId(resourceImpl, false);

        if (!(resourceImpl instanceof Collection)) {
            addContent(resourceImpl);
            if (resourceImpl.getDbBasedContentID() > 0) {
                updateContentId(resourceImpl);
            }
        }

        addProperties(resourceImpl);
    }

    public void createAndApplyResourceID(String path, ResourceIDImpl parentID,
                                         ResourceImpl resourceImpl)
            throws RegistryException {
        ResourceIDImpl resourceID =
                createResourceID(path, parentID, resourceImpl instanceof CollectionImpl);
        resourceImpl.setName(resourceID.getName());
        resourceImpl.setPath(resourceID.getPath());
        resourceImpl.setPathID(resourceID.getPathID());
    }

    public ResourceIDImpl createResourceID(String path, ResourceIDImpl parentID,
                                           boolean isCollection)
            throws RegistryException {
        ResourceIDImpl resourceID = new ResourceIDImpl();
        try {
            int parentPathID = -1;
            if (parentID != null) {
                parentPathID = parentID.getPathID();
            }
            if (isCollection) {
                resourceID.setName(null);
                resourceID.setPath(path);

                JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                        JDBCDatabaseTransaction.getConnection();
                JDBCPathCache pathCache = JDBCPathCache.getPathCache();
                int pathID = pathCache.getPathID(conn, path);
                if (pathID == -1) {
                    pathID = pathCache.addEntry(path, parentPathID);
                }
                resourceID.setPathID(pathID);
            } else {
                String resourceName = RegistryUtils.getResourceName(path);
                resourceID.setName(resourceName);
                resourceID.setPath(path);
                resourceID.setPathID(parentPathID);
            }
        } catch (SQLException e) {
            String msg = "Failed to create the resource id for the resource " +
                    path + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return resourceID;
    }

    public void deleteContentStream(int contentID) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            String resourceContentSQL = "DELETE FROM REG_CONTENT WHERE " +
                    "REG_CONTENT_ID = ? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(resourceContentSQL);
            ps.setLong(1, contentID);
            ps.setInt(2, CurrentSession.getTenantId());
            ps.executeUpdate();

        } catch (SQLException ex) {
            String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
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

    public InputStream getContentStream(int contentID) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            String resourceContentSQL = "SELECT REG_CONTENT_DATA  FROM  REG_CONTENT WHERE " +
                    "REG_CONTENT_ID = ? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(resourceContentSQL);
            ps.setLong(1, contentID);
            ps.setInt(2, CurrentSession.getTenantId());
            result = ps.executeQuery();
            if (result.next()) {
                InputStream rawInputStream =
                        result.getBinaryStream(DatabaseConstants.CONTENT_DATA_FIELD);
                if (rawInputStream != null) {
                    return RegistryUtils.getMemoryStream(rawInputStream);
                }
            }

        } catch (SQLException ex) {
            String msg = RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
            log.error(msg, ex);
            throw new RegistryException(msg, ex);
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

    public void update(ResourceImpl resourceImpl) throws RegistryException {

        // there is no difference of adding a resource and updating as it is always
        // a new entry to the resource table
        addResourceWithoutContentId(resourceImpl, true);
        if (!(resourceImpl instanceof CollectionImpl)) {
            // adding content new
            addContent(resourceImpl);
            if (resourceImpl.getDbBasedContentID() > 0) {
                updateContentId(resourceImpl);
            }
        }

        // adding properties..
        addProperties(resourceImpl);

    }

    public int getChildCount(CollectionImpl collection, DataAccessManager dataAccessManager)
            throws RegistryException {
        int childCount = -1;
        if (Transaction.isStarted()) {
            childCount = getChildCount(collection, JDBCDatabaseTransaction.getConnection());
        } else {
            Connection conn = null;
            boolean transactionSucceeded = false;
            try {
                if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
                    String msg = "Failed to get child count. Invalid data access manager.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                conn = ((JDBCDataAccessManager)
                        dataAccessManager).getDataSource().getConnection();

                // If a managed connection already exists, use that instead of a new
                // connection.
                JDBCDatabaseTransaction.ManagedRegistryConnection temp =
                        JDBCDatabaseTransaction.getManagedRegistryConnection(conn);
                if (temp != null) {
                    conn.close();
                    conn = temp;
                }
                if (conn.getTransactionIsolation() !=
                        Connection.TRANSACTION_READ_COMMITTED) {
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                }
                conn.setAutoCommit(false);

                childCount = getChildCount(collection, conn);
                transactionSucceeded = true;

            } catch (SQLException e) {

                String msg = "Failed to get the child count of resource " +
                        collection.getPath() + ". " + e.getMessage();
                log.error(msg, e);
                throw new RegistryException(msg, e);

            } finally {
                if (transactionSucceeded) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        log.error("Failed to commit the database connection used in " +
                                "getting child count of the collection " + collection.getPath());
                    }
                } else if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        log.error("Failed to rollback the database connection used in " +
                                "getting child count of the collection " + collection.getPath());
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        log.error("Failed to close the database connection used in " +
                                "getting child count of collection " + collection.getPath());
                    }
                }
            }
        }
        return childCount;
    }

    /**
     * Method to return a child count of a collection (database connection should also be provided)
     *
     * @param collection the collection object which the children are calculated.
     * @param conn       the database connection.
     *
     * @return the child count.
     * @throws RegistryException throws if the operation failed.
     */
    public int getChildCount(CollectionImpl collection, Connection conn)
            throws RegistryException {

        ResourceIDImpl resourceID = collection.getResourceIDImpl();
        ResultSet results1 = null;
        PreparedStatement ps1 = null;
        ResultSet results2 = null;
        PreparedStatement ps2 = null;


        int childCount = 0;
        try {
            // step1: get the child resources.
            String sql = "SELECT COUNT(R.REG_NAME) AS REG_RES_COUNT FROM REG_RESOURCE R WHERE " +
                    "R.REG_PATH_ID=? AND R.REG_TENANT_ID=?";

            ps1 = conn.prepareStatement(sql);
            ps1.setInt(1, resourceID.getPathID());
            ps1.setInt(2, CurrentSession.getTenantId());
            results1 = ps1.executeQuery();

            if (results1.next()) {
                // here -1 is to avoid the parent collection itself
                childCount += results1.getInt(DatabaseConstants.RES_COUNT_FIELD);
            }

            // step2: get the child collections
            sql = "SELECT COUNT(P.REG_PATH_ID) AS REG_RES_COUNT FROM REG_PATH P, REG_RESOURCE R " +
                    "WHERE P.REG_PATH_PARENT_ID=? AND P.REG_TENANT_ID=? AND " +
                    "R.REG_PATH_ID=P.REG_PATH_ID AND R.REG_TENANT_ID=?";
            ps2 = conn.prepareStatement(sql);
            ps2.setInt(1, resourceID.getPathID());
            ps2.setInt(2, CurrentSession.getTenantId());
            ps2.setInt(3, CurrentSession.getTenantId());

            results2 = ps2.executeQuery();

            if (results2.next()) {
                childCount += results2.getInt(DatabaseConstants.RES_COUNT_FIELD);
            }

        } catch (SQLException e) {

            String msg = "Failed to get child paths of resource " +
                    collection.getPath() + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (results1 != null) {
                        results1.close();
                    }
                } finally {
                    try {
                        if (ps1 != null) {
                            ps1.close();
                        }
                    } finally {
                        try {
                            if (results2 != null) {
                                results2.close();
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
        return childCount;
    }

    public void fillChildren(CollectionImpl collection, int start, int pageLen)
            throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        fillChildren(collection, start, pageLen, conn);
    }

    public void fillChildren(CollectionImpl collection, DataAccessManager dataAccessManager)
            throws RegistryException {
        if (Transaction.isStarted()) {
            fillChildren(collection, 0, -1, JDBCDatabaseTransaction.getConnection());
        } else {
            Connection conn = null;
            boolean transactionSucceeded = false;
            try {
                if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
                    String msg = "Failed to fill children. Invalid data access manager.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                conn = ((JDBCDataAccessManager)
                        dataAccessManager).getDataSource().getConnection();

                // If a managed connection already exists, use that instead of a new
                // connection.
                JDBCDatabaseTransaction.ManagedRegistryConnection temp =
                        JDBCDatabaseTransaction.getManagedRegistryConnection(conn);
                if (temp != null) {
                    conn.close();
                    conn = temp;
                }
                if (conn.getTransactionIsolation() !=
                        Connection.TRANSACTION_READ_COMMITTED) {
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                }
                conn.setAutoCommit(false);

                fillChildren(collection, 0, -1, conn);
                transactionSucceeded = true;
            } catch (SQLException e) {

                String msg = "Failed to get child paths of " +
                        collection.getPath() + ". " + e.getMessage();
                log.error(msg, e);
                throw new RegistryException(msg, e);

            } finally {
                if (transactionSucceeded) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        log.error("Failed to commit the database connection used in " +
                                "getting child paths of the collection " + collection.getPath());
                    }
                } else if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        log.error("Failed to rollback the database connection used in " +
                                "getting child paths of the collection " + collection.getPath());
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        log.error("Failed to close the database connection opened in " +
                                "getting the child paths of " + collection.getPath(), e);
                    }
                }
            }
        }
    }

    /**
     * Fill the children for a resource that already filled with meta data. Children are filled only
     * at the at of the intersect of the given range and resource existence range.
     *
     * @param collection collection to fill the children and properties.
     * @param start      start value of the range of children.
     * @param pageLen    the length of the children to retrieve
     * @param conn       the database connection
     *
     * @throws RegistryException if the operation failed.
     */
    public void fillChildren(CollectionImpl collection, int start, int pageLen, Connection conn)
            throws RegistryException {
        String[] childPaths = getChildren(collection, start, pageLen, conn);
        collection.setContent(childPaths);
        if (childPaths != null) {
            collection.setChildCount(childPaths.length);
        }
    }

    public String[] getChildren(CollectionImpl collection, int start, int pageLen,
                                DataAccessManager dataAccessManager)
            throws RegistryException {
        String[] childPaths = null;

        if (Transaction.isStarted()) {
            childPaths = getChildren(collection, start, pageLen,
                    JDBCDatabaseTransaction.getConnection());
        } else {
            Connection conn = null;
            boolean transactionSucceeded = false;
            try {
                if (!(dataAccessManager instanceof JDBCDataAccessManager)) {
                    String msg = "Failed to get children. Invalid data access manager.";
                    log.error(msg);
                    throw new RegistryException(msg);
                }
                conn = ((JDBCDataAccessManager)
                        dataAccessManager).getDataSource().getConnection();

                // If a managed connection already exists, use that instead of a new
                // connection.
                JDBCDatabaseTransaction.ManagedRegistryConnection temp =
                        JDBCDatabaseTransaction.getManagedRegistryConnection(conn);
                if (temp != null) {
                    conn.close();
                    conn = temp;
                }
                if (conn.getTransactionIsolation() !=
                        Connection.TRANSACTION_READ_COMMITTED) {
                    conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
                }
                conn.setAutoCommit(false);

                childPaths = getChildren(collection, start, pageLen, conn);
                transactionSucceeded = true;
            } catch (SQLException e) {

                String msg =
                        "Failed to get the child paths " + pageLen + " child paths from " +
                                start + " of resource " + collection.getPath() + ". " +
                                e.getMessage();
                log.error(msg, e);
                throw new RegistryException(msg, e);

            } finally {
                if (transactionSucceeded) {
                    try {
                        conn.commit();
                    } catch (SQLException e) {
                        log.error("Failed to commit the database connection used in " +
                                "getting child paths of the collection " + collection.getPath());
                    }
                } else if (conn != null) {
                    try {
                        conn.rollback();
                    } catch (SQLException e) {
                        log.error("Failed to rollback the database connection used in " +
                                "getting child paths of the collection " + collection.getPath());
                    }
                }
                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        log.error("Failed to close the database connection used in " +
                                "getting child paths of the collection " + collection.getPath());
                    }
                }
            }
        }
        return childPaths;
    }

    /**
     * Get the children of the collection. Children are filled only at the at of the intersect of
     * the given range and resource existence range.
     *
     * @param collection collection to fill the children and properties.
     * @param start      start value of the range of children.
     * @param pageLen    the length of the children to retrieve
     * @param conn       the database connection
     *
     * @return an array of children paths
     * @throws RegistryException throws if the operation failed.
     */
    public String[] getChildren(CollectionImpl collection, int start, int pageLen, Connection conn)
            throws RegistryException {

        ResourceIDImpl resourceID = collection.getResourceIDImpl();
        ResultSet results1 = null;
        PreparedStatement ps1 = null;
        ResultSet results2 = null;
        PreparedStatement ps2 = null;

        ResourcePath parentResourcePath = new ResourcePath(resourceID.getPath());
        String parentPath = parentResourcePath.getPath();
        try {
            // step1: get the child resources.
            String sql =
                    "SELECT R.REG_NAME FROM REG_RESOURCE R WHERE R.REG_PATH_ID=? " +
                            "AND R.REG_TENANT_ID=?";

            ps1 = conn.prepareStatement(sql);
            ps1.setInt(1, resourceID.getPathID());
            ps1.setInt(2, CurrentSession.getTenantId());
            results1 = ps1.executeQuery();

            List<String> childResourcesList = new ArrayList<String>();
            List<String> childCollectionsList = new ArrayList<String>();
            while (results1.next()) {
                String childName = results1.getString(DatabaseConstants.NAME_FIELD);
                if (childName == null) {
                    // skip the null named resource (which is the parent resource
                    continue;
                }
                String childPath = parentPath +
                        (parentPath.equals(RegistryConstants.PATH_SEPARATOR) ? "" :
                                RegistryConstants.PATH_SEPARATOR) +
                        childName;

                ResourceIDImpl childResourceID = new ResourceIDImpl();
                childResourceID.setPath(childPath);
                childResourceID.setCollection(false);
                childResourceID.setName(childName);
                childResourceID.setPathID(resourceID.getPathID());
                if (AuthorizationUtils.authorize(childPath, ActionConstants.GET)) {
                    childResourcesList.add(childPath);
                }
            }

            int end = start + pageLen;
            if (childResourcesList.size() < end || pageLen == -1) {
                // step2: get the child collections
                sql = "SELECT P.REG_PATH_ID, P.REG_PATH_VALUE FROM REG_PATH P, REG_RESOURCE R " +
                        "WHERE P.REG_PATH_PARENT_ID=? AND P.REG_TENANT_ID=? AND " +
                        "R.REG_PATH_ID=P.REG_PATH_ID AND " +
                        "R.REG_NAME IS NULL AND R.REG_TENANT_ID=?";
                ps2 = conn.prepareStatement(sql);
                ps2.setInt(1, resourceID.getPathID());
                ps2.setInt(2, CurrentSession.getTenantId());
                ps2.setInt(3, CurrentSession.getTenantId());

                results2 = ps2.executeQuery();

                while (results2.next()) {
                    String childPath = results2.getString(DatabaseConstants.PATH_VALUE_FIELD);
                    int childPathId = results2.getInt(DatabaseConstants.PATH_ID_FIELD);

                    ResourceIDImpl childResourceID = new ResourceIDImpl();
                    childResourceID.setPath(childPath);
                    childResourceID.setCollection(true);
                    childResourceID.setName(null);
                    childResourceID.setPathID(childPathId);
                    if (AuthorizationUtils.authorize(childPath, ActionConstants.GET)) {
                        childCollectionsList.add(childPath);
                    }
                }
            }

            int totalSize = childResourcesList.size() + childCollectionsList.size();
            if (totalSize < start) {
                throw new RegistryException("Didn't have enough results to start at #" + start);
            }
            Collections.sort(childResourcesList);
            Collections.sort(childCollectionsList);

            int currentPageLen = pageLen;
            if (currentPageLen == -1) {
                currentPageLen = totalSize - start;
                end = totalSize - 1;
            }

            int resultMaxSize = currentPageLen < (totalSize - start) ?
                    currentPageLen : (totalSize - start);
            String[] childPaths = new String[resultMaxSize];
            // copying child resources
            int current = 0;
            int i;
            for (i = 0; i < childResourcesList.size(); i++) {
                if (current >= resultMaxSize) {
                    break;
                }
                if (i >= start && i <= end) {
                    childPaths[current] = childResourcesList.get(i);
                    current++;
                }
            }
            // copying child collections
            for (int j = 0; j < childCollectionsList.size(); j++, i++) {
                if (current >= resultMaxSize) {
                    break;
                }
                if (i >= start && i <= end) {
                    childPaths[current] = childCollectionsList.get(j);
                    current++;
                }
            }
            return childPaths;

        } catch (SQLException e) {

            String msg = "Failed to get child paths of resource " +
                    collection.getPath() + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            try {
                try {
                    if (results1 != null) {
                        results1.close();
                    }
                } finally {
                    try {
                        if (ps1 != null) {
                            ps1.close();
                        }
                    } finally {
                        try {
                            if (results2 != null) {
                                results2.close();
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
    }

    public String[] getChildren(CollectionImpl collection, int start, int pageLen)
            throws RegistryException {
        return getChildren(collection, start, pageLen, JDBCDatabaseTransaction.getConnection());
    }

    public ResourceImpl getResourceMetaData(String path) throws RegistryException {
        ResourceIDImpl resourceIDImpl = getResourceID(path);
        ResourceImpl resourceImpl = null;
        if (resourceIDImpl != null) {
            resourceImpl = getResourceMetaData(resourceIDImpl);

            if (resourceImpl == null && resourceIDImpl.isCollection()) {
                // we should check the resourceID for a non-collection too.
                resourceIDImpl = getResourceID(path, false);
                if (resourceIDImpl != null) {
                    resourceImpl = getResourceMetaData(resourceIDImpl);
                }
            }
        }
        return resourceImpl;
    }

    public ResourceImpl getResourceMetaData(ResourceIDImpl resourceID) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        ResourceImpl resourceImpl = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            boolean isCollection = resourceID.isCollection();
            String sql;
            int pathID = resourceID.getPathID();
            String resourceName = resourceID.getName();
            if (isCollection) {
                // collection
                sql = "SELECT REG_MEDIA_TYPE, REG_CREATOR, REG_CREATED_TIME, " +
                        "REG_LAST_UPDATOR, REG_LAST_UPDATED_TIME, REG_VERSION, REG_DESCRIPTION, " +
                        "REG_CONTENT_ID, REG_UUID FROM REG_RESOURCE WHERE REG_PATH_ID=? AND REG_NAME " +
                        "IS NULL AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, pathID);
                ps.setInt(2, CurrentSession.getTenantId());
            } else {
                // non collection
                sql = "SELECT REG_MEDIA_TYPE, REG_CREATOR, REG_CREATED_TIME, " +
                        "REG_LAST_UPDATOR, REG_LAST_UPDATED_TIME, REG_VERSION, REG_DESCRIPTION, " +
                        "REG_CONTENT_ID, REG_UUID FROM REG_RESOURCE WHERE REG_PATH_ID=? AND REG_NAME = ? " +
                        "AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, pathID);
                ps.setString(2, resourceName);
                ps.setInt(3, CurrentSession.getTenantId());
            }

            result = ps.executeQuery();

            if (result.next()) {

                if (isCollection) {
                    resourceImpl = new CollectionImpl();
                } else {
                    resourceImpl = new ResourceImpl();
                }

                // this is always the current version of the resource
                resourceImpl.setPathID(resourceID.getPathID());
                resourceImpl.setPath(resourceID.getPath());
                resourceImpl.setName(resourceID.getName());
                resourceImpl.setMediaType(result.getString(DatabaseConstants.MEDIA_TYPE_FIELD));
                resourceImpl.setAuthorUserName(result.getString(DatabaseConstants.CREATOR_FIELD));
                resourceImpl
                        .setCreatedTime(result.getTimestamp(DatabaseConstants.CREATED_TIME_FIELD));
                resourceImpl.setLastUpdaterUserName(
                        result.getString(DatabaseConstants.LAST_UPDATER_FIELD));
                resourceImpl.setLastModified(
                        result.getTimestamp(DatabaseConstants.LAST_UPDATED_TIME_FIELD));
                resourceImpl.setVersionNumber(result.getInt(DatabaseConstants.VERSION_FIELD));
                resourceImpl.setDescription(result.getString(DatabaseConstants.DESCRIPTION_FIELD));
                resourceImpl.setDbBasedContentID(result.getInt(DatabaseConstants.CONTENT_ID_FIELD));
                resourceImpl.setUUID(result.getString(DatabaseConstants.UUID_FIELD));
            }
        } catch (SQLException e) {
            String msg = "Failed to get the resource at path " + resourceID.getPath() + ". " +
                    e.getMessage();
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

        return resourceImpl;
    }

    /**
     * Method to return a resource when the resourceID is provided.
     *
     * @param resourceID the resource id.
     *
     * @return the resource for the given id.
     * @throws RegistryException throws if the operation failed.
     */
    private ResourceImpl getResource(ResourceIDImpl resourceID) throws RegistryException {
        ResourceImpl resourceImpl = getResourceMetaData(resourceID);

        if (resourceImpl != null && !(resourceImpl instanceof CollectionImpl) &&
                resourceImpl.getDbBasedContentID() > 0) {
            fillResourceContent(resourceImpl);
        }
        return resourceImpl;
    }

    public ResourceImpl getResourceWithNoUpdate(ResourceIDImpl resourceID)
            throws RegistryException {
        ResourceImpl resourceImpl = getResourceMetaData(resourceID);

        if (resourceImpl != null && !(resourceImpl instanceof CollectionImpl) &&
                resourceImpl.getDbBasedContentID() > 0) {
            fillResourceContentWithNoUpdate(resourceImpl);
        }
        return resourceImpl;
    }

    public void fillResourceContent(ResourceImpl resourceImpl) throws RegistryException {
        int contentId = resourceImpl.getDbBasedContentID();
        InputStream contentStream = getContentStream(contentId);
        if (contentStream == null) {
            resourceImpl.setContent(null);
        } else {
            resourceImpl.setContentStream(contentStream);
        }
    }

    public void fillResourceContentWithNoUpdate(ResourceImpl resourceImpl)
            throws RegistryException {
        int contentId = resourceImpl.getDbBasedContentID();
        InputStream contentStream = getContentStream(contentId);
        if (contentStream == null) {
            resourceImpl.setContentWithNoUpdate(null);
        } else {
            resourceImpl.setContentStreamWithNoUpdate(contentStream);
        }
    }

    public void updateCollectionLastUpdatedTime(ResourceIDImpl resourceID)
            throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            String sql;
            // If SQL Server or SQL Server Express.
            if (dbProductName.contains("Microsoft")) {
                sql = "UPDATE REG_RESOURCE WITH (NOWAIT) SET REG_LAST_UPDATED_TIME=? WHERE " +
                        "REG_PATH_ID=? AND REG_NAME IS NULL AND REG_TENANT_ID=?";
            } else {
                sql = "UPDATE REG_RESOURCE SET REG_LAST_UPDATED_TIME=? WHERE " +
                        "REG_PATH_ID=? AND REG_NAME IS NULL AND REG_TENANT_ID=?";
            }
            ps = conn.prepareStatement(sql);

            long now = System.currentTimeMillis();
            ps.setTimestamp(1, new Timestamp(now));
            ps.setLong(2, resourceID.getPathID());
            ps.setInt(3, CurrentSession.getTenantId());

            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to update the last updated time for the collection. " +
                    "path: " + resourceID.getPath() + ". " +
                    e.getMessage();
            log.error(msg, e);
            if (msg.toLowerCase().contains("lock")) {
                // there can be deadlocks due to concurrent modifications, which we need to handle
                // separately.
                throw new ConcurrentModificationException(msg, e);
            } else {
                throw new RegistryException(msg, e);
            }
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

    public void updateContentId(ResourceImpl resourceImpl)
            throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            String sql = "UPDATE REG_RESOURCE SET REG_CONTENT_ID=? WHERE REG_VERSION=? " +
                    "AND REG_TENANT_ID=?";

            ps = conn.prepareStatement(sql);

            ps.setInt(1, resourceImpl.getDbBasedContentID());
            ps.setLong(2, resourceImpl.getVersionNumber());
            ps.setInt(3, CurrentSession.getTenantId());

            ps.executeUpdate();

        } catch (SQLException e) {
            String msg = "Failed to update the content id: " + resourceImpl.getPath() + ". " +
                    e.getMessage();
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

    public void addResourceWithoutContentId(ResourceImpl resourceImpl, boolean isUpdatingExisting)
            throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet result = null;
        ResourceIDImpl resourceID = resourceImpl.getResourceIDImpl();
        try {
            String sql =
                    "INSERT INTO REG_RESOURCE (REG_PATH_ID, REG_NAME, REG_MEDIA_TYPE, " +
                            "REG_CREATOR, REG_CREATED_TIME, REG_LAST_UPDATOR, " +
                            "REG_LAST_UPDATED_TIME, REG_DESCRIPTION, " +
                            "REG_TENANT_ID, REG_UUID) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sql1 = "SELECT MAX(REG_VERSION) FROM REG_RESOURCE";

            long now = System.currentTimeMillis();

            String dbProductName = conn.getMetaData().getDatabaseProductName();
            boolean returnsGeneratedKeys = DBUtils.canReturnGeneratedKeys(dbProductName);
            if (returnsGeneratedKeys) {
                ps = conn.prepareStatement(sql, new String[]{
                        DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "REG_VERSION")});
            } else {
                ps = conn.prepareStatement(sql);
            }
            ps.setInt(1, resourceID.getPathID());
            ps.setString(2, resourceID.getName());
            ps.setString(3, resourceImpl.getMediaType());
            if (isUpdatingExisting) {
                String authorName = resourceImpl.getAuthorUserName();
                if (authorName == null) {
                    authorName = CurrentSession.getUser();
                    resourceImpl.setAuthorUserName(authorName);
                }
                ps.setString(4, authorName);

                Date createdTime = resourceImpl.getCreatedTime();
                Timestamp createdTimestamp;
                if (createdTime == null) {
                    createdTimestamp = new Timestamp(now);
                } else {
                    createdTimestamp = new Timestamp(createdTime.getTime());
                }
                ps.setTimestamp(5, createdTimestamp);
            } else {
                ps.setString(4, CurrentSession.getUser());
                resourceImpl.setAuthorUserName(CurrentSession.getUser());
                ps.setTimestamp(5, new Timestamp(now));
            }
            ps.setString(6, CurrentSession.getUser());
            ps.setTimestamp(7, new Timestamp(now));
            ps.setString(8, resourceImpl.getDescription());
            ps.setInt(9, CurrentSession.getTenantId());
            ps.setString(10,resourceImpl.getUUID());

            if (returnsGeneratedKeys) {
                ps.executeUpdate();
                result = ps.getGeneratedKeys();
            } else {
                synchronized (ADD_RESOURCE_LOCK) {
                    ps.executeUpdate();
                    ps1 = conn.prepareStatement(sql1);
                    result = ps1.executeQuery();
                }
            }
            if (result.next()) {
                long version = result.getLong(1);
                resourceImpl.setVersionNumber(version);
            }


        } catch (SQLException e) {
            String msg = "Failed to add resource to path " + resourceImpl.getPath() + ". " +
                    e.getMessage();
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

    public void addResourceWithNoUpdate(ResourceImpl resourceImpl)
            throws RegistryException {

        ResourceDO resourceDO = resourceImpl.getResourceDO();
        addResourceDO(resourceDO);
        resourceImpl.setVersionNumber(resourceDO.getVersion());
    }

    public void addResourceDO(ResourceDO resourceDO)
            throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        ResultSet result = null;

        try {
            String sql =
                    "INSERT INTO REG_RESOURCE (REG_PATH_ID, REG_NAME, REG_MEDIA_TYPE, " +
                            "REG_CREATOR, REG_CREATED_TIME, REG_LAST_UPDATOR, " +
                            "REG_LAST_UPDATED_TIME, REG_DESCRIPTION, " +
                            "REG_CONTENT_ID, REG_TENANT_ID, REG_UUID) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sql1 = "SELECT MAX(REG_VERSION) FROM REG_RESOURCE";

            String dbProductName = conn.getMetaData().getDatabaseProductName();
            boolean returnsGeneratedKeys = DBUtils.canReturnGeneratedKeys(dbProductName);
            if (returnsGeneratedKeys) {
                ps = conn.prepareStatement(sql, new String[]{
                        DBUtils.getConvertedAutoGeneratedColumnName(dbProductName, "REG_VERSION")});
            } else {
                ps = conn.prepareStatement(sql);
            }
            ps.setInt(1, resourceDO.getPathID());
            ps.setString(2, resourceDO.getName());
            ps.setString(3, resourceDO.getMediaType());
            ps.setString(4, resourceDO.getAuthor());
            ps.setTimestamp(5, new Timestamp(resourceDO.getCreatedOn()));
            ps.setString(6, resourceDO.getLastUpdater());
            ps.setTimestamp(7, new Timestamp(resourceDO.getLastUpdatedOn()));
            ps.setString(8, resourceDO.getDescription());
            if (resourceDO.getContentID() > 0) {
                ps.setInt(9, resourceDO.getContentID());
            } else {
                ps.setNull(9, Types.INTEGER);
            }
            ps.setInt(10, CurrentSession.getTenantId());
            ps.setString(11,resourceDO.getUUID());

            if (returnsGeneratedKeys) {
                ps.executeUpdate();
                result = ps.getGeneratedKeys();
            } else {
                synchronized (ADD_RESOURCE_LOCK) {
                    ps.executeUpdate();
                    ps1 = conn.prepareStatement(sql1);
                    result = ps1.executeQuery();
                }
            }
            if (result.next()) {
                long version = result.getLong(1);
                resourceDO.setVersion(version);
            }

            ps.close();

        } catch (SQLException e) {
            String msg = "Failed to add resource to version " + resourceDO.getVersion() + ". " +
                    e.getMessage();
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

    public void deleteResource(ResourceDO resourceDO) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            if (resourceDO.getName() == null) {
                String sql = "DELETE FROM REG_RESOURCE WHERE REG_PATH_ID=? AND " +
                        "REG_NAME IS NULL AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceDO.getPathID());
                ps.setInt(2, CurrentSession.getTenantId());

                ps.executeUpdate();
            } else {
                String sql = "DELETE FROM REG_RESOURCE WHERE REG_PATH_ID=? AND " +
                        "REG_NAME=? AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceDO.getPathID());
                ps.setString(2, resourceDO.getName());
                ps.setInt(3, CurrentSession.getTenantId());

                ps.executeUpdate();
            }

        } catch (SQLException e) {

            String msg = "Failed to delete the resource with id " + resourceDO.getVersion() + ". " +
                    e.getMessage();
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

    public void addProperties(ResourceImpl resource) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        String sql1, sql2, sql3;
        boolean isVersioningProperties = StaticConfiguration.isVersioningProperties();

        sql1 = "INSERT INTO REG_PROPERTY (REG_NAME, REG_VALUE, REG_TENANT_ID) VALUES (?, ?, ?)";
        sql2 = "SELECT MAX(REG_ID) FROM REG_PROPERTY";
        if (isVersioningProperties) {
            sql3 = "INSERT INTO REG_RESOURCE_PROPERTY (REG_PROPERTY_ID, REG_VERSION, " +
                    "REG_TENANT_ID) VALUES (?, ?, ?)";
        } else {
            sql3 = "INSERT INTO REG_RESOURCE_PROPERTY (REG_PROPERTY_ID, REG_PATH_ID, " +
                    "REG_RESOURCE_NAME, REG_TENANT_ID) VALUES (?, ?, ?, ?)";
        }

        ResourceIDImpl resourceID = resource.getResourceIDImpl();
        Properties props = resource.getProperties();

        if (props != null) {
            for (Object nameObj : props.keySet()) {
                String name = (String) nameObj;
                List propValues = (List) props.get(name);
                if (propValues != null) {
                    for (Object valueObj : propValues) {
                        PreparedStatement ps1 = null;
                        PreparedStatement ps2 = null;
                        PreparedStatement ps3 = null;
                        ResultSet resultSet1 = null;
                        try {
                            String value = (String) valueObj;
                            String dbProductName = conn.getMetaData().getDatabaseProductName();
                            boolean returnsGeneratedKeys =
                                    DBUtils.canReturnGeneratedKeys(dbProductName);
                            if (returnsGeneratedKeys) {
                                ps1 = conn.prepareStatement(sql1, new String[]{
                                        DBUtils.getConvertedAutoGeneratedColumnName(dbProductName,
                                                DatabaseConstants.ID_FIELD)});
                            } else {
                                ps1 = conn.prepareStatement(sql1);
                            }
                            ps3 = conn.prepareStatement(sql3);

                            // prepare to execute query1 for the property
                            ps1.setString(1, name);
                            ps1.setString(2, value);
                            ps1.setInt(3, CurrentSession.getTenantId());
                            if (returnsGeneratedKeys) {
                                ps1.executeUpdate();
                                resultSet1 = ps1.getGeneratedKeys();
                            } else {
                                synchronized (ADD_PROPERTY_LOCK) {
                                    ps1.executeUpdate();
                                    ps2 = conn.prepareStatement(sql2);
                                    resultSet1 = ps2.executeQuery();
                                }
                            }
                            if (resultSet1.next()) {
                                // setting the property id
                                int propertyId = resultSet1.getInt(1);
                                ps3.setInt(1, propertyId);
                                if (isVersioningProperties) {
                                    ps3.setLong(2, resource.getVersionNumber());
                                    ps3.setInt(3, CurrentSession.getTenantId());
                                } else {
                                    ps3.setLong(2, resourceID.getPathID());
                                    if (resourceID.getName() == null) {
                                        ps3.setNull(3, Types.VARCHAR);
                                    } else {
                                        ps3.setString(3, resourceID.getName());
                                    }
                                    ps3.setInt(4, CurrentSession.getTenantId());
                                }
                                ps3.executeUpdate();
                            }

                        } catch (SQLException e) {

                            String msg = "Failed to add properties to the resource " +
                                    resource.getPath() + ". " + e.getMessage();
                            log.error(msg, e);
                            throw new RegistryException(msg, e);
                        } finally {
                            // closing prepared statements before moving on to next iteration
                            try {
                                try {
                                    try {
                                        if (resultSet1 != null) {
                                            resultSet1.close();
                                        }
                                    } finally {
                                        if (ps1 != null) {
                                            ps1.close();
                                        }
                                    }
                                } finally {
                                    try {
                                        if (ps2 != null) {
                                            ps2.close();
                                        }
                                    } finally {
                                        if (ps3 != null) {
                                            ps3.close();
                                        }
                                    }
                                }
                            } catch (SQLException ex) {
                                String msg =
                                        RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR;
                                log.error(msg, ex);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get id values of properties of a resource.
     *
     * @param resourceDO the resource which properties are to retrieve.
     *
     * @return the array of property ids.
     * @throws RegistryException throws if the operation failed.
     */
    private Integer[] getPropertyIds(ResourceDO resourceDO) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        List<Integer> propertyIds = new ArrayList<Integer>();
        PreparedStatement ps = null;
        ResultSet results = null;
        try {
            String sql;
            if (StaticConfiguration.isVersioningProperties()) {
                sql = "SELECT REG_PROPERTY_ID FROM REG_RESOURCE_PROPERTY WHERE " +
                        "REG_VERSION=? AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setLong(1, resourceDO.getVersion());
                ps.setInt(2, CurrentSession.getTenantId());

            } else if (resourceDO.getName() == null) {
                // a collection
                sql = "SELECT REG_PROPERTY_ID FROM REG_RESOURCE_PROPERTY WHERE " +
                        "REG_PATH_ID=? AND REG_RESOURCE_NAME IS NULL AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceDO.getPathID());
                ps.setInt(2, CurrentSession.getTenantId());
            } else {
                sql = "SELECT REG_PROPERTY_ID FROM REG_RESOURCE_PROPERTY WHERE " +
                        "REG_PATH_ID=? AND REG_RESOURCE_NAME=? AND REG_TENANT_ID=?";

                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceDO.getPathID());
                ps.setString(2, resourceDO.getName());
                ps.setInt(3, CurrentSession.getTenantId());
            }

            results = ps.executeQuery();
            while (results.next()) {
                int propertyId = results.getInt(DatabaseConstants.PROPERTY_ID_FIELD);
                propertyIds.add(propertyId);
            }

        } catch (SQLException e) {

            String msg = "Failed to get properties on resource version" +
                    resourceDO.getVersion() + ". " + e.getMessage();
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

        return propertyIds.toArray(new Integer[propertyIds.size()]);
    }

    public void removeProperties(ResourceDO resourceDO) throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();

        PreparedStatement ps1 = null, ps2 = null;

        Integer[] propertyIds = getPropertyIds(resourceDO);
        if (propertyIds == null) {
            return;
        }
        try {

            String sql =
                    "DELETE FROM REG_RESOURCE_PROPERTY WHERE REG_PROPERTY_ID= ? " +
                            "AND REG_TENANT_ID=?";
            ps1 = conn.prepareStatement(sql);

            sql = "DELETE FROM REG_PROPERTY WHERE REG_ID= ? AND REG_TENANT_ID=?";
            ps2 = conn.prepareStatement(sql);

            for (Integer propertyId : propertyIds) {
                ps1.setInt(1, propertyId);
                ps1.setInt(2, CurrentSession.getTenantId());
                ps2.setInt(1, propertyId);
                ps2.setInt(2, CurrentSession.getTenantId());
                ps1.addBatch();
                ps2.addBatch();
            }

            if (propertyIds.length > 0) {
                try {
                    ps1.executeBatch();
                    ps2.executeBatch();
                } catch (SQLException e) {
                    ps1.clearBatch();
                    ps2.clearBatch();
                    // the exception will be handled in the next catch block
                    throw e;
                }
            }
        } catch (SQLException e) {

            String msg = "Failed to remove properties from resource version " +
                    resourceDO.getVersion() + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
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

    public void addContent(ResourceImpl resourceImpl) throws RegistryException {

        if (!(resourceImpl.getContent() instanceof byte[])) {
            if (log.isDebugEnabled()) {
                String msg = "Content of the resource " + resourceImpl.getPath() + " is null or " +
                        "not a byte array. Content will not be persisted to the database.";
                log.debug(msg);
            }
            resourceImpl.setDbBasedContentID(0);
            return;
        }

        InputStream contentStream = resourceImpl.getContentStream();
        int contentID = addContentBytes(contentStream);
        resourceImpl.setDbBasedContentID(contentID);
    }

    public int addContentBytes(InputStream contentStream) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        int contentID = -1;

        try {
            String sql = "INSERT INTO REG_CONTENT (REG_CONTENT_DATA, REG_TENANT_ID) VALUES (?, ?)";
            String sql1 = "SELECT MAX(REG_CONTENT_ID) FROM REG_CONTENT";

            int size = contentStream.available();
            PreparedStatement ps, ps1 = null;
            String dbProductName = conn.getMetaData().getDatabaseProductName();
            boolean returnsGeneratedKeys = DBUtils.canReturnGeneratedKeys(dbProductName);
            if (returnsGeneratedKeys) {
                ps = conn.prepareStatement(sql,
                        new String[]{DBUtils.getConvertedAutoGeneratedColumnName(
                                dbProductName, "REG_CONTENT_ID")});
            } else {
                ps = conn.prepareStatement(sql);
            }
            try {
                ps.setBinaryStream(1, contentStream, size);
                ps.setInt(2, CurrentSession.getTenantId());
                ResultSet result;
                if (returnsGeneratedKeys) {
                    ps.executeUpdate();
                    result = ps.getGeneratedKeys();
                } else {
                    synchronized (ADD_CONTENT_LOCK) {
                        ps.executeUpdate();
                        if (dbProductName.equals("OpenEdge RDBMS")) {
                            String sql2 = "UPDATE REG_CONTENT SET REG_CONTENT_ID = " +
                                    "PUB.REG_CONTENT_SEQUENCE.NEXTVAL WHERE REG_CONTENT_ID = 0";
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
                try {
                    if (result.next()) {
                        contentID = result.getInt(1);
                    }
                } finally {
                    if (result != null) {
                        result.close();
                    }
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
        } catch (IOException e) {
            String msg = "An error occurred while processing content stream.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } catch (SQLException e) {
            String msg = "Failed to write resource content to the database.";
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
        return contentID;
    }

    public ResourceDO getResourceDO(long version) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet result = null;
        try {

            String sql =
                    "SELECT REG_PATH_ID, REG_NAME, REG_MEDIA_TYPE, REG_CREATOR, " +
                            "REG_CREATED_TIME, REG_LAST_UPDATOR, REG_LAST_UPDATED_TIME, " +
                            "REG_DESCRIPTION, REG_CONTENT_ID, REG_UUID " +
                            "FROM REG_RESOURCE WHERE REG_VERSION = ? AND REG_TENANT_ID=?";

            ps = conn.prepareStatement(sql);
            ps.setLong(1, version);
            ps.setInt(2, CurrentSession.getTenantId());

            ResourceDO resourceDO = null;
            result = ps.executeQuery();
            if (result.next()) {
                resourceDO = new ResourceDO();

                resourceDO.setPathID(result.getInt(DatabaseConstants.PATH_ID_FIELD));
                resourceDO.setName(result.getString(DatabaseConstants.NAME_FIELD));
                resourceDO.setVersion(version);
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
            }

            return resourceDO;

        } catch (SQLException e) {

            String msg = "Failed to read resource version data for resource version:" +
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
    }

    public ResourceDO getResourceDO(ResourceIDImpl resourceID) throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            String sql;
            if (resourceID.isCollection()) {
                sql = "SELECT  R.REG_PATH_ID, R.REG_NAME, R.REG_VERSION, R.REG_MEDIA_TYPE, " +
                        "R.REG_CREATOR, R.REG_CREATED_TIME, R.REG_LAST_UPDATOR, " +
                        "R.REG_LAST_UPDATED_TIME, R.REG_DESCRIPTION, R.REG_CONTENT_ID, R.REG_UUID " +
                        "FROM REG_RESOURCE R WHERE R.REG_PATH_ID=? AND R.REG_NAME IS NULL " +
                        "AND R.REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceID.getPathID());
                ps.setInt(2, CurrentSession.getTenantId());

            } else {
                sql = "SELECT  R.REG_PATH_ID, R.REG_NAME, R.REG_VERSION, R.REG_MEDIA_TYPE, " +
                        "R.REG_CREATOR, R.REG_CREATED_TIME, R.REG_LAST_UPDATOR, " +
                        "R.REG_LAST_UPDATED_TIME, R.REG_DESCRIPTION, R.REG_CONTENT_ID, R.REG_UUID " +
                        "FROM REG_RESOURCE R WHERE R.REG_PATH_ID=? AND R.REG_NAME=? " +
                        "AND R.REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, resourceID.getPathID());
                ps.setString(2, resourceID.getName());
                ps.setInt(3, CurrentSession.getTenantId());
            }

            ResourceDO resourceDO = null;
            result = ps.executeQuery();
            if (result.next()) {
                resourceDO = new ResourceDO();

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
            }

            return resourceDO;

        } catch (SQLException e) {

            String msg = "Failed to read resource version data for resource " +
                    resourceID + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            if (result != null) {
                try {
                    result.close();
                } catch (SQLException e) {
                    log.error(RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR, e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    log.error(RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR, e);
                }
            }
        }
    }

    public List<ResourceIDImpl> getChildPathIds(ResourceIDImpl resourceID)
            throws RegistryException {

        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        ArrayList<ResourceIDImpl> childPathList = new ArrayList<ResourceIDImpl>();
        PreparedStatement ps = null;
        ResultSet results = null;
        try {
            // step1: get the child resources.
            String sql =
                    "SELECT R.REG_NAME FROM REG_RESOURCE R WHERE R.REG_PATH_ID=? " +
                            "AND R.REG_TENANT_ID=?";

            ps = conn.prepareStatement(sql);
            ps.setInt(1, resourceID.getPathID());
            ps.setInt(2, CurrentSession.getTenantId());
            results = ps.executeQuery();

            while (results.next()) {
                String childName = results.getString(DatabaseConstants.NAME_FIELD);
                if (childName == null) {
                    // skip the parent resource..
                    continue;
                }
                String parentPath = resourceID.getPath();
                String childPath = parentPath +
                        (parentPath.equals(RegistryConstants.PATH_SEPARATOR) ? "" :
                                RegistryConstants.PATH_SEPARATOR) +
                        childName;
                ResourceIDImpl childResourceID = new ResourceIDImpl();
                childResourceID.setPath(childPath);
                childResourceID.setCollection(false);
                childResourceID.setName(childName);
                childResourceID.setPathID(resourceID.getPathID());
                childPathList.add(childResourceID);
            }
            results.close();
            results = null;
            ps.close();
            ps = null;

            sql = "SELECT P.REG_PATH_ID, P.REG_PATH_VALUE FROM REG_PATH P, REG_RESOURCE R " +
                    "WHERE P.REG_PATH_PARENT_ID=? AND P.REG_TENANT_ID=? AND " +
                    "R.REG_PATH_ID=P.REG_PATH_ID AND R.REG_NAME IS NULL AND R.REG_TENANT_ID=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, resourceID.getPathID());
            ps.setInt(2, CurrentSession.getTenantId());
            ps.setInt(3, CurrentSession.getTenantId());

            results = ps.executeQuery();

            while (results.next()) {
                String childPath = results.getString(DatabaseConstants.PATH_VALUE_FIELD);
                int childPathId = results.getInt(DatabaseConstants.PATH_ID_FIELD);

                ResourceIDImpl childResourceID = new ResourceIDImpl();
                childResourceID.setPath(childPath);
                childResourceID.setCollection(true);
                childResourceID.setName(null);
                childResourceID.setPathID(childPathId);
                childPathList.add(childResourceID);
            }
        } catch (SQLException e) {

            String msg = "Failed to get child paths of resource " +
                    resourceID.getPath() + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        } finally {
            if (results != null) {
                try {
                    results.close();
                } catch (SQLException e) {
                    log.error(RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR, e);
                }
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    log.error(RegistryConstants.RESULT_SET_PREPARED_STATEMENT_CLOSE_ERROR, e);
                }
            }
        }
        return childPathList;
    }

    public String getPathFromId(int pathId) throws RegistryException {
        try {
            return JDBCPathCache.getPathCache()
                    .getPath(JDBCDatabaseTransaction.getConnection(), pathId);
        } catch (SQLException e) {
            String msg = "Failed to get the path for the path id " + pathId + ". " + e.getMessage();
            log.error(msg, e);
            throw new RegistryException(msg, e);
        }
    }

    public String getPath(long version) throws RegistryException {
        ResourceDO resourceDO = getResourceDO(version);
        if (resourceDO == null) {
            return null;
        }
        return getPath(resourceDO.getPathID(), resourceDO.getName(), false);
    }

    public String getPath(int pathId, String resourceName, boolean checkExistence)
            throws RegistryException {
        String pathCollection = getPathFromId(pathId);
        if (pathCollection == null) {
            return null;
        }
        String correctPath;
        if (resourceName == null) {
            // this is the parent path..
            correctPath = pathCollection;
        } else if (pathCollection.equals(RegistryConstants.ROOT_PATH)) {
            correctPath = pathCollection + resourceName;
        } else {
            correctPath = pathCollection + RegistryConstants.PATH_SEPARATOR + resourceName;
        }

        // need to check the existence of the path, otherwise return null,
        if (checkExistence) {
            ResourceIDImpl resourceID = new ResourceIDImpl();
            resourceID.setCollection(resourceName == null);
            resourceID.setName(resourceName);
            resourceID.setPath(correctPath);
            resourceID.setPathID(pathId);
            ResourceDO resourceDO = getResourceDO(resourceID);
            if (resourceDO == null) {
                return null;
            }
        }
        return correctPath;
    }

    public void moveResources(ResourceIDImpl source, ResourceIDImpl target)
            throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            if (source.isCollection()) {
                String sql = "UPDATE REG_RESOURCE SET REG_PATH_ID=? WHERE " +
                        "REG_PATH_ID=? AND REG_NAME IS NULL AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, target.getPathID());
                ps.setInt(2, source.getPathID());
                ps.setInt(3, CurrentSession.getTenantId());
                ps.executeUpdate();
            } else {
                String sql = "UPDATE REG_RESOURCE SET REG_PATH_ID=?, REG_NAME =? WHERE " +
                        "REG_PATH_ID=? AND REG_NAME=? AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, target.getPathID());
                ps.setString(2, target.getName());
                ps.setInt(3, source.getPathID());
                ps.setString(4, source.getName());
                ps.setInt(5, CurrentSession.getTenantId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Failed to move resource from  " + source.getPath() +
                    " to " + target.getPath() + ". " + e.getMessage();
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

    public void moveResourcePaths(ResourceIDImpl source, ResourceIDImpl target)
            throws RegistryException {
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            String sql =
                    "UPDATE REG_RESOURCE SET REG_PATH_ID=? WHERE REG_PATH_ID=? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, target.getPathID());
            ps.setInt(2, source.getPathID());
            ps.setInt(3, CurrentSession.getTenantId());
            ps.executeUpdate();
        } catch (SQLException e) {
            String msg = "Failed to move resource paths from  " + source.getPath() +
                    " to " + target.getPath() + ". " + e.getMessage();
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

    public void moveProperties(ResourceIDImpl source, ResourceIDImpl target)
            throws RegistryException {
        if (StaticConfiguration.isVersioningProperties()) {
            return;
        }
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            if (source.isCollection()) {
                String sql = "UPDATE REG_RESOURCE_PROPERTY SET REG_PATH_ID=? WHERE " +
                        "REG_PATH_ID=? AND REG_RESOURCE_NAME IS NULL AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, target.getPathID());
                ps.setInt(2, source.getPathID());
                ps.setInt(3, CurrentSession.getTenantId());
                ps.executeUpdate();
            } else {
                String sql =
                        "UPDATE REG_RESOURCE_PROPERTY SET REG_PATH_ID=?, REG_RESOURCE_NAME=? " +
                                "WHERE REG_PATH_ID=? AND REG_RESOURCE_NAME=? AND REG_TENANT_ID=?";
                ps = conn.prepareStatement(sql);
                ps.setInt(1, target.getPathID());
                ps.setString(2, target.getName());
                ps.setInt(3, source.getPathID());
                ps.setString(4, source.getName());
                ps.setInt(5, CurrentSession.getTenantId());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            String msg = "Failed to move properties from  " + source.getPath() +
                    " to " + target.getPath() + ". " + e.getMessage();
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

    public void movePropertyPaths(ResourceIDImpl source, ResourceIDImpl target)
            throws RegistryException {
        if (StaticConfiguration.isVersioningProperties()) {
            return;
        }
        JDBCDatabaseTransaction.ManagedRegistryConnection conn =
                JDBCDatabaseTransaction.getConnection();
        PreparedStatement ps = null;
        try {
            String sql = "UPDATE REG_RESOURCE_PROPERTY SET REG_PATH_ID=? WHERE " +
                    "REG_PATH_ID=? AND REG_TENANT_ID=?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, target.getPathID());
            ps.setInt(2, source.getPathID());
            ps.setInt(3, CurrentSession.getTenantId());
            ps.executeUpdate();
        } catch (SQLException e) {
            String msg = "Failed to move property paths from  " + source.getPath() +
                    " to " + target.getPath() + ". " + e.getMessage();
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
}
