/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.tenant;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * This is the tenant id cache.
 */
class TenantIdCache {

    private static final String TENANT_ID_CACHE_MANAGER = "TENANT_ID_CACHE_MANAGER";
    private static final String TENANT_ID_CACHE = "TENANT_ID_CACHE";
    private static Log log = LogFactory.getLog(TenantIdCache.class);
    private static TenantIdCache tenantIdCache = new TenantIdCache();

    private TenantIdCache() {
    }

    /**
     * Gets a new instance of TenantCache.
     *
     * @return A new instance of TenantCache.
     */
    public synchronized static TenantIdCache getInstance() {
        return tenantIdCache;
    }

    private Cache<TenantDomainKey, TenantIdEntry> getTenantIdCache() {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
            CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(TENANT_ID_CACHE_MANAGER);
            return cacheManager.getCache(TENANT_ID_CACHE);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(TenantDomainKey key, TenantIdEntry entry) {

        try {
            startSuperTenantFlow();
            Cache<TenantDomainKey, TenantIdEntry> cache = getTenantIdCache();
            if (cache != null) {
                cache.put(key, entry);
                log.debug(TENANT_ID_CACHE + " which is under " + TENANT_ID_CACHE_MANAGER + ", added the entry : " +
                        entry + " for the key : " + key + " successfully");
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while getting the cache : " + TENANT_ID_CACHE + " which is under " +
                            TENANT_ID_CACHE_MANAGER);
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
    public TenantIdEntry getValueFromCache(TenantDomainKey key) {
        try {
            startSuperTenantFlow();
            Cache<TenantDomainKey, TenantIdEntry> cache = getTenantIdCache();
            if (cache != null) {
                if (cache.containsKey(key)) {
                    TenantIdEntry entry = cache.get(key);
                    if (log.isDebugEnabled()) {
                        log.debug(TENANT_ID_CACHE + " which is under " + TENANT_ID_CACHE_MANAGER + ", found the " +
                                "entry : " + entry + " for the key : " + key + " successfully");
                    }
                    return entry;
                }
                if (log.isDebugEnabled()) {
                    log.debug(TENANT_ID_CACHE + " which is under " + TENANT_ID_CACHE_MANAGER + ", doesn't contain the key : " + key);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while getting the cache : " + TENANT_ID_CACHE + " which is under " + TENANT_ID_CACHE_MANAGER);
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
    public void clearCacheEntry(TenantDomainKey key) {
        try {
            startSuperTenantFlow();
            Cache<TenantDomainKey, TenantIdEntry> cache = getTenantIdCache();
            if (cache != null) {
                if (cache.containsKey(key)) {
                    cache.remove(key);
                    if (log.isDebugEnabled()) {
                        log.debug(TENANT_ID_CACHE + " which is under " + TENANT_ID_CACHE_MANAGER + ",vis removed " +
                                "entry for the key : " + key + " successfully");
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug(TENANT_ID_CACHE + " which is under " + TENANT_ID_CACHE_MANAGER + ", doen't contain " +
                            "the key : " + key);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while getting the cache : " + TENANT_ID_CACHE + " which is under " +
                            TENANT_ID_CACHE_MANAGER);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {
        try {
            startSuperTenantFlow();
            Cache<TenantDomainKey, TenantIdEntry> cache = getTenantIdCache();
            if (cache != null) {
                cache.removeAll();
                if (log.isDebugEnabled()) {
                    log.debug(TENANT_ID_CACHE + " which is under " + TENANT_ID_CACHE_MANAGER + ", is cleared " +
                            "successfully");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Error while getting the cache : " + TENANT_ID_CACHE + " which is under " +
                            TENANT_ID_CACHE_MANAGER);
                }
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void startSuperTenantFlow() {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(MultitenantConstants.SUPER_TENANT_ID);
    }
}
