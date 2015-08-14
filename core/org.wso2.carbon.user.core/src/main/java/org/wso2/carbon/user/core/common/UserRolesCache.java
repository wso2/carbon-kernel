/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.internal.UserStoreMgtDSComponent;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

public class UserRolesCache {

    private static final String USER_ROLES_CACHE_MANAGER = "USER_ROLES_CACHE_MANAGER";
    private static final String USER_ROLES_CACHE = "USER_ROLES_CACHE";
    private static Log log = LogFactory.getLog(UserRolesCache.class);
    private static UserRolesCache userRolesCache = new UserRolesCache();

    private int timeOut = UserCoreConstants.USER_ROLE_CACHE_DEFAULT_TIME_OUT;

    private UserRolesCache() {

    }

    /**
     * Gets a new instance of UserRolesCache.
     *
     * @return A new instance of UserRolesCache.
     */
    public static UserRolesCache getInstance() {
        return userRolesCache;
    }


    /**
     * Getting existing cache if the cache available, else returns a newly created cache.
     * This logic handles by javax.cache implementation
     */
    private Cache<UserRolesCacheKey, UserRolesCacheEntry> getUserRolesCache() {
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(USER_ROLES_CACHE_MANAGER);
//        cacheManager.<UserRolesCacheKey, UserRolesCacheEntry>createCacheBuilder(USER_ROLES_CACHE).  //  TODO time out not working
//                setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.MINUTES, timeOut)).
//                setStoreByValue(false);
        return cacheManager.getCache(USER_ROLES_CACHE);
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
                log.debug("USER_ROLES_CACHE doesn't exist in CacheManager:\n" + traceString);
            }
            return true;
        }
        return false;
    }

    //add to cache
    public void addToCache(String serverId, int tenantId, String userName, String[] userRoleList) {

        Cache<UserRolesCacheKey, UserRolesCacheEntry> cache = this.getUserRolesCache();
        //check for null
        if (isCacheNull(cache)) {
            return;
        }

        if (!isUsernameCaseSensitive(userName, tenantId)){
            userName = userName.toLowerCase();
        }
        //create cache key
        UserRolesCacheKey userRolesCacheKey = new UserRolesCacheKey(serverId, tenantId, userName);
        //create cache entry
        UserRolesCacheEntry userRolesCacheEntry = new UserRolesCacheEntry(userRoleList);
        //add to cache
        cache.put(userRolesCacheKey, userRolesCacheEntry);

    }

    //get roles list of user
    public String[] getRolesListOfUser(String serverId, int tenantId, String userName) {

        Cache<UserRolesCacheKey, UserRolesCacheEntry> cache = this.getUserRolesCache();
        //check for null
        if (isCacheNull(cache)) {
            return new String[0];
        }

        if (!isUsernameCaseSensitive(userName, tenantId)){
            userName = userName.toLowerCase();
        }
        //create cache key
        UserRolesCacheKey userRolesCacheKey = new UserRolesCacheKey(serverId, tenantId, userName);
        //search cache and get cache entry
        UserRolesCacheEntry userRolesCacheEntry = cache.get(userRolesCacheKey);
        return userRolesCacheEntry.getUserRolesList();
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    // lear userRolesCache by tenantId
    public void clearCacheByTenant(int tenantId) {

        Cache<UserRolesCacheKey, UserRolesCacheEntry> cache = this.getUserRolesCache();
        cache.removeAll();
    }

    // Clear userRolesCache by serverId, tenant and user name
    public void clearCacheEntry(String serverId, int tenantId, String userName) {

        Cache<UserRolesCacheKey, UserRolesCacheEntry> cache = getUserRolesCache();
        // Check for null
        if (isCacheNull(cache)) {
            return;
        }

        if (!isUsernameCaseSensitive(userName, tenantId)){
            userName = userName.toLowerCase();
        }
        UserRolesCacheKey userRolesCacheKey = new UserRolesCacheKey(serverId, tenantId, userName);
        if (cache.containsKey(userRolesCacheKey)) {
            cache.remove(userRolesCacheKey);
        }
        // creating new key for isUserHasRole cache.
        userRolesCacheKey = new UserRolesCacheKey(serverId, tenantId,
                UserCoreConstants.IS_USER_IN_ROLE_CACHE_IDENTIFIER + userName);
        if (cache.containsKey(userRolesCacheKey)) {
            cache.remove(userRolesCacheKey);
        }
    }

    private boolean isUsernameCaseSensitive(String username, int tenantId){
        if (UserStoreMgtDSComponent.getRealmService()!= null) {
            //this check is added to avoid NullPointerExceptions if the osgi is not started yet.
            //as an example when running the unit tests.
            try {
                UserStoreManager userStoreManager = (UserStoreManager) UserStoreMgtDSComponent.getRealmService()
                        .getTenantUserRealm(tenantId).getUserStoreManager();
                UserStoreManager userAvailableUserStoreManager = userStoreManager.getSecondaryUserStoreManager
                        (getDomainFromName(username));
                if (userAvailableUserStoreManager instanceof AbstractUserStoreManager) {
                    return ((AbstractUserStoreManager) userAvailableUserStoreManager).isCaseSensitiveUsername();
                } else {
                    return false;
                }
            } catch (UserStoreException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Error while reading user store property CaseSensitiveUsername. Considering as false.");
                }
            }
        }
        return false;
    }

    private String getDomainFromName(String name) {
        int index;
        if ((index = name.indexOf("/")) > 0) {
            String domain = name.substring(0, index);
            return domain;
        }
        return UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME;
    }
}
