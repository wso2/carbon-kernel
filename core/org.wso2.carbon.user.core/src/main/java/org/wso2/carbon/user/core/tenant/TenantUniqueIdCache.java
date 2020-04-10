/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.user.core.tenant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * Tenant unique id cache which holds tenant unique id as the key and tenant as the entry.
 */
public class TenantUniqueIdCache {

    private static final String TENANT_UNIQUE_ID_CACHE_MANAGER = "TENANT_UNIQUE_ID_CACHE_MANAGER";
    private static final String TENANT_UNIQUE_ID_CACHE = "TENANT_UNIQUE_ID_CACHE";
    private static Log log = LogFactory.getLog(TenantUniqueIdCache.class);
    private static TenantUniqueIdCache tenantUniqueIdCache = new TenantUniqueIdCache();

    private TenantUniqueIdCache() {

    }

    /**
     * Gets a new instance of TenantUniqueIdCache.
     *
     * @return A new instance of TenantUniqueIdCache.
     */
    public synchronized static TenantUniqueIdCache getInstance() {

        return tenantUniqueIdCache;
    }

    /**
     * Getting existing cache if the cache available, else returns a newly created cache.
     * This logic handles by javax.cache implementation
     */
    private <T> Cache<TenantUniqueIDKey, T> getTenantUUIDCache() {

        Cache<TenantUniqueIDKey, T> cache;
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(TENANT_UNIQUE_ID_CACHE_MANAGER);
        cache = cacheManager.getCache(TENANT_UNIQUE_ID_CACHE);
        return cache;
    }

    /**
     * Add a cache entry.
     * Tenant
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public <T> void addToCache(TenantUniqueIDKey key, T entry) {

        PrivilegedCarbonContext.startTenantFlow();

        try {
            startSuperTenantFlow();
            // Element already in the cache. Remove it first.
            clearCacheEntry(key);
            Cache<TenantUniqueIDKey, T> cache = getTenantUUIDCache();
            if (cache != null) {
                cache.put(key, entry);
                if (log.isDebugEnabled()) {
                    log.debug(TENANT_UNIQUE_ID_CACHE + " which is under " + TENANT_UNIQUE_ID_CACHE_MANAGER + ", " +
                            "added the entry : " + entry + " for the key : " + key + " successfully");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while getting the cache : " + TENANT_UNIQUE_ID_CACHE + " which is under " +
                            TENANT_UNIQUE_ID_CACHE_MANAGER);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry if the key presents, else returns null.
     */
    public <T> T getValueFromCache(TenantUniqueIDKey key) {

        PrivilegedCarbonContext.startTenantFlow();

        try {
            startSuperTenantFlow();

            Cache<TenantUniqueIDKey, T> cache = getTenantUUIDCache();
            if (cache != null) {
                if (cache.containsKey(key)) {
                    T entry = cache.get(key);
                    if (log.isDebugEnabled()) {
                        log.debug(TENANT_UNIQUE_ID_CACHE + " which is under " + TENANT_UNIQUE_ID_CACHE_MANAGER +
                                ", found the entry : " + entry + " for the key : " + key + " successfully");
                    }
                    return entry;
                }
                if (log.isDebugEnabled()) {
                    log.debug(TENANT_UNIQUE_ID_CACHE + " which is under " + TENANT_UNIQUE_ID_CACHE_MANAGER + ", " +
                            "doesn't contain the key : " + key);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while getting the cache : " + TENANT_UNIQUE_ID_CACHE + " which is under " +
                            TENANT_UNIQUE_ID_CACHE_MANAGER);
                }
            }
            return null;
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(TenantUniqueIDKey key) {

        PrivilegedCarbonContext.startTenantFlow();

        try {
            startSuperTenantFlow();

            Cache<TenantUniqueIDKey, Object> cache = getTenantUUIDCache();
            if (cache != null) {
                if (cache.containsKey(key)) {
                    cache.remove(key);
                    if (log.isDebugEnabled()) {
                        log.debug(TENANT_UNIQUE_ID_CACHE + " which is under " + TENANT_UNIQUE_ID_CACHE_MANAGER + ", is "
                                + "removed entry for the key : " + key + " successfully");
                    }
                }
                if (log.isDebugEnabled()) {
                    log.debug(TENANT_UNIQUE_ID_CACHE + " which is under " + TENANT_UNIQUE_ID_CACHE_MANAGER + ", " +
                            "doesn't contain the key : " + key);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while getting the cache : " + TENANT_UNIQUE_ID_CACHE + " which is under " +
                            TENANT_UNIQUE_ID_CACHE_MANAGER);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void startSuperTenantFlow() {

        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
        carbonContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {

        PrivilegedCarbonContext.startTenantFlow();

        try {
            startSuperTenantFlow();
            Cache<TenantUniqueIDKey, Object> tenantUniqueIDCache = getTenantUUIDCache();
            if (tenantUniqueIDCache != null) {
                tenantUniqueIDCache.removeAll();
                if (log.isDebugEnabled()) {
                    log.debug(TENANT_UNIQUE_ID_CACHE + " which is under " + TENANT_UNIQUE_ID_CACHE_MANAGER + ", " +
                            "is cleared successfully");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while getting the cache : " + TENANT_UNIQUE_ID_CACHE + " which is under " +
                            TENANT_UNIQUE_ID_CACHE_MANAGER);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
