/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT     WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package org.wso2.carbon.user.core.common;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;
import org.wso2.carbon.user.core.util.UserCoreUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;

public class UserGroupCache {

    private static final String USER_GROUP_CACHE_MANAGER = "USER_GROUP_CACHE_MANAGER";
    private static final String USER_GROUP_CACHE = "USER_GROUP_CACHE";
    private static final String CASE_INSENSITIVE_USERNAME = "CaseInsensitiveUsername";
    private static Log log = LogFactory.getLog(UserRolesCache.class);
    private static UserGroupCache userGroupCache = new UserGroupCache();


    private int timeOut = UserCoreConstants.USER_ROLE_CACHE_DEFAULT_TIME_OUT;

    private UserGroupCache() {

    }

    /**
     * Gets a new instance of UserGroupCache.
     *
     * @return A new instance of UserGroupCache.
     */
    public static UserGroupCache getInstance() {
        return userGroupCache;
    }

    /**
     * Get the existing UserGroupCache or a newly created cache.
     * This logic handled by javax.cache implementation.
     *
     * @return the {@link Cache} UserGroupCache
     */
    private Cache<UserRolesCacheKey, UserRolesCacheEntry> getUserGroupCache() {

        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(USER_GROUP_CACHE_MANAGER);
        Cache userGroupCache = null;
        for (Cache cache : cacheManager.getCaches()) {
            if (StringUtils.equals(cache.getName(), USER_GROUP_CACHE) ||
                    StringUtils.equals(cache.getName(), CachingConstants.LOCAL_CACHE_PREFIX + USER_GROUP_CACHE)) {
                userGroupCache = cache;
            }
        }

        if (userGroupCache == null) {
            cacheManager.createCacheBuilder(USER_GROUP_CACHE).setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                    new CacheConfiguration.Duration(TimeUnit.MINUTES, timeOut)).build();
        }
        return cacheManager.getCache(USER_GROUP_CACHE);
    }

    /**
     * Avoiding NullPointerException when the cache is null
     *
     * @return boolean whether given cache is null
     */
    private boolean isCacheNull(Cache<UserRolesCacheKey, UserRolesCacheEntry> cache) {
        if (cache == null) {
            if (log.isDebugEnabled()) {
                StackTraceElement[] elemets = Thread.currentThread().getStackTrace();
                String traceString = "";
                for (int i = 1; i < elemets.length; ++i) {
                    traceString += elemets[i] + System.getProperty("line.separator");
                }
                log.debug("USER_GROUP_CACHE doesn't exist in CacheManager:\n" + traceString);
            }
            return true;
        }
        return false;
    }

    //add to cache
    public void addToCache(String serverId, int tenantId, String userName, List<Group> groups) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId, true);

            Cache<UserRolesCacheKey, UserRolesCacheEntry> cache = this.getUserGroupCache();
            //check for null
            if (isCacheNull(cache)) {
                return;
            }
            if (!isCaseSensitiveUsername(userName, tenantId)) {
                userName = userName.toLowerCase();
            }
            //create cache key
            UserRolesCacheKey userRolesCacheKey = new UserRolesCacheKey(serverId, tenantId, userName);
            //create cache entry
            UserRolesCacheEntry userRolesCacheEntry = new UserRolesCacheEntry(groups);
            //add to cache
            cache.put(userRolesCacheKey, userRolesCacheEntry);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Get list of groups from userGroupCache.
     *
     * @param serverId Server ID.
     * @param tenantId Tenant ID.
     * @param userName Username.
     * @return List of groups.
     */
    public List<Group> getGroupListOfUser(String serverId, int tenantId, String userName) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId, true);

            Cache<UserRolesCacheKey, UserRolesCacheEntry> cache = this.getUserGroupCache();
            //check for null
            if (isCacheNull(cache)) {
                return new ArrayList<>();
            }
            if (!isCaseSensitiveUsername(userName, tenantId)) {
                userName = userName.toLowerCase();
            }
            //create cache key
            UserRolesCacheKey userRolesCacheKey = new UserRolesCacheKey(serverId, tenantId, userName);
            //search cache and get cache entry
            UserRolesCacheEntry userRolesCacheEntry = cache.get(userRolesCacheKey);

            if (userRolesCacheEntry == null) {
                return new ArrayList<>();
            }

            return userRolesCacheEntry.getUserGroupList();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    // Clear userGroupCache by tenantId.
    public void clearCacheByTenant(int tenantId) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId, true);

            Cache<UserRolesCacheKey, UserRolesCacheEntry> cache = this.getUserGroupCache();
            cache.removeAll();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    // Clear userRolesCache by serverId, tenant and user name
    public void clearCacheEntry(String serverId, int tenantId, String userName) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            carbonContext.setTenantId(tenantId, true);
            Cache<UserRolesCacheKey, UserRolesCacheEntry> cache = this.getUserGroupCache();
            // Check for null
            if (isCacheNull(cache)) {
                return;
            }

            boolean caseSensitiveUsername = isCaseSensitiveUsername(userName, tenantId);
            if (!caseSensitiveUsername) {
                userName = userName.toLowerCase();
            }
            UserRolesCacheKey userRolesCacheKey = new UserRolesCacheKey(serverId, tenantId, userName);
            cache.remove(userRolesCacheKey);

            String userNameWithCacheIdentifier = UserCoreConstants.IS_USER_IN_ROLE_CACHE_IDENTIFIER + userName;

            // creating new key for isUserHasRole cache.
            if (!caseSensitiveUsername) {
                userNameWithCacheIdentifier = userNameWithCacheIdentifier.toLowerCase();
            }

            userRolesCacheKey = new UserRolesCacheKey(serverId, tenantId, userNameWithCacheIdentifier);
            cache.remove(userRolesCacheKey);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }


    private boolean isCaseSensitiveUsername(String username, int tenantId) {

        if (UserStoreMgtDSComponent.getRealmService() != null) {
            //this check is added to avoid NullPointerExceptions if the osgi is not started yet.
            //as an example when running the unit tests.
            try {
                if (UserStoreMgtDSComponent.getRealmService().getTenantUserRealm(tenantId) != null) {
                    UserStoreManager userStoreManager = (UserStoreManager) UserStoreMgtDSComponent.getRealmService()
                            .getTenantUserRealm(tenantId).getUserStoreManager();
                    UserStoreManager userAvailableUserStoreManager = userStoreManager.getSecondaryUserStoreManager
                            (removeUserInRoleIdentifier(UserCoreUtil.extractDomainFromName(username)));
                    String isUsernameCaseInsensitiveString = userAvailableUserStoreManager.getRealmConfiguration()
                            .getUserStoreProperty(CASE_INSENSITIVE_USERNAME);
                    return !Boolean.parseBoolean(isUsernameCaseInsensitiveString);

                }

            } catch (UserStoreException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error while reading user store property CaseInsensitiveUsername. Considering as false.");
                }
            }
        }
        return true;
    }

    private String removeUserInRoleIdentifier(String modifiedName) {
        String originalName = modifiedName;
        if (originalName.contains(UserCoreConstants.IS_USER_IN_ROLE_CACHE_IDENTIFIER)) {
            originalName = modifiedName.replace(UserCoreConstants.IS_USER_IN_ROLE_CACHE_IDENTIFIER, "");
        }
        return originalName;
    }

}
