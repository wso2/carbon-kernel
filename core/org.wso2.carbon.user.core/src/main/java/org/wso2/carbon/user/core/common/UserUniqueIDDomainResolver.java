/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * Purpose of this class is to keep a mapping between user unique id and the domain of that user to reduce the
 * user store lookups by resolving the correct domain when the user id is provided. This is will act as persisted cache.
 * @since 4.6.0
 */
public class UserUniqueIDDomainResolver {

    private static final Log log = LogFactory.getLog(UserUniqueIDDomainResolver.class);

    // Generic constants.
    private static final String UNIQUE_ID_DOMAIN_RESOLVER_CACHE_MANGER_NAME = "unique_id_domain_cache_manager";
    private static final String UNIQUE_ID_DOMAIN_RESOLVER_CACHE_NAME = "unique_id_domain_cache";
    private static final String DOMAIN_COLUMN_NAME = "UM_DOMAIN_NAME";

    // DB related.
    private DataSource dataSource;
    private static final String IS_DOMAIN_EXISTS_SQL =
            "SELECT UM_DOMAIN_ID " +
            "FROM UM_UUID_DOMAIN_MAPPER " +
            "WHERE UM_USER_ID = ?";
    private static final String UPDATE_DOMAIN_NAME =
            "UPDATE UM_UUID_DOMAIN_MAPPER " +
            "SET UM_DOMAIN_ID = (SELECT UM_DOMAIN_ID FROM UM_DOMAIN WHERE UM_DOMAIN_NAME = ? " +
                    "AND UM_TENANT_ID = ?), UM_TENANT_ID = ? " +
            "WHERE UM_USER_ID = ?";
    private static final String ADD_DOMAIN_NAME =
            "INSERT INTO UM_UUID_DOMAIN_MAPPER (UM_USER_ID, UM_DOMAIN_ID, UM_TENANT_ID) " +
            "VALUES (?, (SELECT UM_DOMAIN_ID FROM UM_DOMAIN WHERE UM_DOMAIN_NAME = ? AND UM_TENANT_ID = ?), ?)";
    private static final String GET_DOMAIN =
            "SELECT UM_DOMAIN_NAME " +
            "FROM UM_DOMAIN " +
            "WHERE UM_DOMAIN_ID = (SELECT UM_DOMAIN_ID " +
            "FROM UM_UUID_DOMAIN_MAPPER " +
            "WHERE UM_USER_ID = ? AND UM_TENANT_ID = ?)";

    public UserUniqueIDDomainResolver(DataSource dataSource) {

        this.dataSource = dataSource;
    }

    /**
     * Get the domain for the given user id.
     *
     * @param userId Unique user id of the user.
     * @return Domain related to this user. Null if no domain name recorded.
     * @throws UserStoreException If error occurred.
     */
    public String getDomainForUserId(String userId, int tenantId) throws UserStoreException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId, true);
            CacheManager cacheManager = Caching.getCacheManagerFactory()
                    .getCacheManager(UNIQUE_ID_DOMAIN_RESOLVER_CACHE_MANGER_NAME);
            Cache<String, String> uniqueIdDomainCache = cacheManager.getCache(UNIQUE_ID_DOMAIN_RESOLVER_CACHE_NAME);

            if (StringUtils.isEmpty(userId)) {
                throw new UserStoreException("User Id cannot be empty or null.");
            }

            // Read the cache first.
            String domainName = uniqueIdDomainCache.get(userId);
            if (domainName != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Cache hit for user id: " + userId);
                }
                return domainName;
            }

            if (log.isDebugEnabled()) {
                log.debug("Cache miss for user id: " + userId + " searching from the database.");
            }

            // Read the domain name from the Database;
            domainName = getDomainFromDB(userId, tenantId);

            // Update the cache.
            if (domainName != null) {
                uniqueIdDomainCache.put(userId, domainName);
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
     * Set the domain for the user id. This will update the domain name if there is already a record available.
     *
     * @param userId     Unique user id of the user.
     * @param userDomain Domain
     * @throws UserStoreException
     */
    public void setDomainForUserId(String userId, String userDomain, int tenantId) throws UserStoreException {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId, true);
            CacheManager cacheManager = Caching.getCacheManagerFactory()
                    .getCacheManager(UNIQUE_ID_DOMAIN_RESOLVER_CACHE_MANGER_NAME);
            Cache<String, String> uniqueIdDomainCache = cacheManager.getCache(UNIQUE_ID_DOMAIN_RESOLVER_CACHE_NAME);

            if (StringUtils.isEmpty(userId) || StringUtils.isEmpty(userDomain)) {
                throw new UserStoreException("User id or user domain cannot be empty or null.");
            }

            if (log.isDebugEnabled()) {
                log.debug("Persisting user id: " + userId + " against domain: " + userDomain);
            }

            // Persist the domain in the DB.
            persistDomainAgainstUserId(userId, userDomain, tenantId);

            // Add to the cache.
            uniqueIdDomainCache.put(userId, userDomain);

            if (log.isDebugEnabled()) {
                log.debug("Successfully persisted user id: " + userId + " against domain: " + userDomain
                        + " and added to the cache.");
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private String getDomainFromDB(String userId, int tenantId) throws UserStoreException {

        String domainName = null;
        try (Connection dbConnection = getDBConnection();
             PreparedStatement preparedStatement = dbConnection.prepareStatement(GET_DOMAIN)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setInt(2, tenantId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    domainName = resultSet.getString(DOMAIN_COLUMN_NAME);
                }
            }
        } catch (SQLException ex) {
            throw new UserStoreException("Error occurred while reading the domain name for user id from database.", ex);
        }
        return domainName;
    }

    private void persistDomainAgainstUserId(String userId, String userDomain, int tenantId) throws UserStoreException {

        try (Connection dbConnection = getDBConnection()) {

            // Check whether the domain already exists in the DB against this user id. If so, we have to
            // update the record.
            // Do it in the same connection.
            if (isDomainExistsForUserId(userId, dbConnection)) {
                if (log.isDebugEnabled()) {
                    log.debug("Domain name available for the provided user id: " + userId + " Hence updating it.");
                }
                try (PreparedStatement preparedStatement = dbConnection.prepareStatement(UPDATE_DOMAIN_NAME)) {
                    preparedStatement.setString(1, userDomain);
                    preparedStatement.setInt(2, tenantId);
                    preparedStatement.setInt(3, tenantId);
                    preparedStatement.setString(4, userId);
                    preparedStatement.execute();
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("No domain name found for the give user id: " + userId
                            + " Hence adding it as a new value.");
                }
                try (PreparedStatement preparedStatement = dbConnection.prepareStatement(ADD_DOMAIN_NAME)) {
                    preparedStatement.setString(1, userId);
                    preparedStatement.setString(2, userDomain);
                    preparedStatement.setInt(3, tenantId);
                    preparedStatement.setInt(4, tenantId);
                    preparedStatement.execute();
                }
            }
        } catch (SQLException ex) {
            throw new UserStoreException("Error occurred while persisting domain against the user id.", ex);
        }
    }

    private boolean isDomainExistsForUserId(String userId, Connection connection) throws SQLException {

        try (PreparedStatement preparedStatement = connection.prepareStatement(IS_DOMAIN_EXISTS_SQL)) {
            preparedStatement.setString(1, userId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private Connection getDBConnection() throws SQLException, UserStoreException {

        if (dataSource == null) {
            throw new UserStoreException("Datasource is null. Cannot create connection.");
        }
        return dataSource.getConnection();
    }
}
