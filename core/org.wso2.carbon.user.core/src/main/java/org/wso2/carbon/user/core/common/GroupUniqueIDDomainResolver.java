/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.com).
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.user.core.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.xml.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.sql.DataSource;

/**
 * Purpose of this class is to keep a mapping between group unique id and the domain of that group to reduce the
 * userstore lookups by resolving the correct domain when the group id is provided. This is will act as persisted
 * cache.
 */
public class GroupUniqueIDDomainResolver {

    private static final Log log = LogFactory.getLog(GroupUniqueIDDomainResolver.class);

    // Generic constants.
    private static final String GROUP_UNIQUE_ID_DOMAIN_RESOLVER_CACHE_MANGER_NAME =
            "group_unique_id_domain_cache_manager";
    private static final String GROUP_UNIQUE_ID_DOMAIN_RESOLVER_CACHE_NAME = "group_unique_id_domain_cache";
    private static final String DOMAIN_COLUMN_NAME = "UM_DOMAIN_NAME";

    // DB related.
    private final DataSource dataSource;
    private static final String IS_DOMAIN_EXISTS_SQL = "SELECT UM_DOMAIN_ID FROM UM_GROUP_UUID_DOMAIN_MAPPER WHERE " +
            "UM_GROUP_ID = ?";
    private static final String UPDATE_DOMAIN_NAME = "UPDATE UM_GROUP_UUID_DOMAIN_MAPPER SET UM_DOMAIN_ID = (SELECT " +
            "UM_DOMAIN_ID FROM UM_DOMAIN WHERE UM_DOMAIN_NAME = ? AND UM_TENANT_ID = ?), UM_TENANT_ID = ? " +
            "WHERE UM_GROUP_ID = ?";
    private static final String ADD_DOMAIN_NAME = "INSERT INTO UM_GROUP_UUID_DOMAIN_MAPPER (UM_GROUP_ID, " +
            "UM_DOMAIN_ID, UM_TENANT_ID) VALUES (?, (SELECT UM_DOMAIN_ID FROM UM_DOMAIN WHERE UM_DOMAIN_NAME = ? AND " +
            "UM_TENANT_ID = ?), ?)";
    private static final String GET_DOMAIN = "SELECT UM_DOMAIN_NAME FROM UM_DOMAIN WHERE UM_DOMAIN_ID=(SELECT " +
            "UM_DOMAIN_ID FROM UM_GROUP_UUID_DOMAIN_MAPPER WHERE UM_GROUP_ID = ? AND UM_TENANT_ID = ?)";

    public GroupUniqueIDDomainResolver(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    /**
     * Get the domain for the given group id.
     *
     * @param groupId  Group unique id.
     * @param tenantId Tenant id.
     * @return Domain of the group.
     * @throws UserStoreException If an error occurred while getting the mapped domain.
     */
    public String getDomainForGroupId(String groupId, int tenantId) throws UserStoreException {

        try {
            if (StringUtils.isEmpty(groupId)) {
                throw new UserStoreException("Group Id cannot be empty or null");
            }

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId, true);
            CacheManager cacheManager = Caching.getCacheManagerFactory()
                    .getCacheManager(GROUP_UNIQUE_ID_DOMAIN_RESOLVER_CACHE_MANGER_NAME);
            Cache<String, String> uniqueIdDomainCache =
                    cacheManager.getCache(GROUP_UNIQUE_ID_DOMAIN_RESOLVER_CACHE_NAME);

            // Read the cache first.
            String domainName = uniqueIdDomainCache.get(groupId);
            if (domainName != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cache hit for group id: " + groupId);
                }
                return domainName;
            }
            // Domain name is not in the cache.
            if (log.isDebugEnabled()) {
                log.debug("Cache miss for group id: " + groupId + " searching from the database");
            }
            // Read the domain name from the Database;
            domainName = getDomainFromDB(groupId, tenantId);
            // Update the cache.
            if (domainName != null) {
                uniqueIdDomainCache.put(groupId, domainName);
                if (log.isDebugEnabled()) {
                    log.debug("Domain with name: " + domainName + " retrieved from the database.");
                }
            }
            return domainName;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Set the domain for the group id. This will update the domain name if there is already a record available.
     *
     * @param groupId            Group unique id.
     * @param userstoreDomain    Userstore domain name.
     * @param tenantId           Tenant id.
     * @param persistToCacheOnly Whether to persist the mapping only in the cache. This needs to be set to true if
     *                           the userstore manager does not support group uuid.
     * @throws UserStoreException If an error occurred while setting the domain name for the group id.
     */
    public void setDomainForGroupId(String groupId, String userstoreDomain, int tenantId, boolean persistToCacheOnly) throws UserStoreException {

        try {
            if (StringUtils.isEmpty(groupId) || StringUtils.isEmpty(userstoreDomain)) {
                throw new UserStoreException("group id or userstore domain cannot be empty or null");
            }
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId, true);
            CacheManager cacheManager = Caching.getCacheManagerFactory()
                    .getCacheManager(GROUP_UNIQUE_ID_DOMAIN_RESOLVER_CACHE_MANGER_NAME);
            Cache<String, String> uniqueIdDomainCache =
                    cacheManager.getCache(GROUP_UNIQUE_ID_DOMAIN_RESOLVER_CACHE_NAME);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Persisting group id: %s against domain: %s", groupId, userstoreDomain));
            }
            // Persist the domain in the DB domain mapper only if specified.
            if (!persistToCacheOnly) {
                persistDomainAgainstGroupId(groupId, userstoreDomain, tenantId);
            }
            // Add to the cache.
            uniqueIdDomainCache.put(groupId, userstoreDomain);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Successfully persisted group id: %s against domain: %s and added to " +
                        "the cache", groupId, userstoreDomain));
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private String getDomainFromDB(String groupId, int tenantId) throws UserStoreException {

        String domainName = null;
        try (Connection dbConnection = getDBConnection();
             PreparedStatement preparedStatement = dbConnection.prepareStatement(GET_DOMAIN)) {
            preparedStatement.setString(1, groupId);
            preparedStatement.setInt(2, tenantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    domainName = resultSet.getString(DOMAIN_COLUMN_NAME);
                }
            }
        } catch (SQLException ex) {
            throw new UserStoreException(String.format("Error occurred while reading the domain name for " +
                    "group id: %s in tenant: %s from database.", groupId, tenantId), ex);
        }
        return domainName;
    }

    private void persistDomainAgainstGroupId(String groupId, String userstoreDomain, int tenantId)
            throws UserStoreException {

        try (Connection dbConnection = getDBConnection()) {
            /*
             * Check whether the domain already exists in the DB against this group id. If so, we have to update the
             * record. Do it in the same connection.
             */
            try {
                if (isDomainExistsForGroupId(groupId, dbConnection)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Domain name available for the provided group id: " + groupId +
                                ". Hence updating it");
                    }
                    try (PreparedStatement preparedStatement = dbConnection.prepareStatement(UPDATE_DOMAIN_NAME)) {
                        preparedStatement.setString(1, userstoreDomain);
                        preparedStatement.setInt(2, tenantId);
                        preparedStatement.setInt(3, tenantId);
                        preparedStatement.setString(4, groupId);
                        preparedStatement.execute();
                        commitTransaction(dbConnection);
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("No domain name found for the give group id: " + groupId
                                + ". Hence adding it as a new value");
                    }
                    try (PreparedStatement preparedStatement = dbConnection.prepareStatement(ADD_DOMAIN_NAME)) {
                        preparedStatement.setString(1, groupId);
                        preparedStatement.setString(2, userstoreDomain);
                        preparedStatement.setInt(3, tenantId);
                        preparedStatement.setInt(4, tenantId);
                        preparedStatement.execute();
                        commitTransaction(dbConnection);
                    }
                }
            } catch (SQLException e) {
                rollbackTransaction(dbConnection);
                throw new UserStoreException(String.format("Error occurred while persisting domain: %s against the " +
                        "group id: %s in tenant: %s", userstoreDomain, groupId, tenantId), e);
            }
        } catch (SQLException ex) {
            throw new UserStoreException(String.format("Error occurred while persisting domain: %s against the " +
                    "group id: %s for tenant: %s", userstoreDomain, groupId, tenantId), ex);
        }
    }

    private boolean isDomainExistsForGroupId(String groupId, Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(IS_DOMAIN_EXISTS_SQL)) {
            preparedStatement.setString(1, groupId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Connection getDBConnection() throws SQLException, UserStoreException {

        if (dataSource == null) {
            throw new UserStoreException("Datasource is null. Cannot create connection");
        }
        return dataSource.getConnection();
    }

    /**
     * Commit a transaction.
     *
     * @param dbConnection database connection.
     */
    private void commitTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null && !dbConnection.getAutoCommit()) {
                dbConnection.commit();
            }
        } catch (SQLException e) {
            log.error("An error occurred while transaction commit", e);
            rollbackTransaction(dbConnection);
        }
    }

    /**
     * Rollback a transaction.
     *
     * @param dbConnection Database connection.
     */
    private void rollbackTransaction(Connection dbConnection) {

        try {
            if (dbConnection != null && !dbConnection.getAutoCommit()) {
                dbConnection.rollback();
            }
        } catch (SQLException e) {
            log.error("An error occurred while transaction rollback", e);
        }
    }
}
