package org.wso2.carbon.user.core.tenant;



/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Date: Oct 1, 2010 Time: 2:36:37 PM
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

/**
 * This is the tenant cache.
 */
class TenantDomainCache {

    public static final String TENANT_DOMAIN_CACHE_MANAGER = "TENANT_DOMAIN_CACHE_MANAGER";
    public static final String TENANT_DOMAIN_CACHE = "TENANT_DOMAIN_CACHE";
    private static Log log = LogFactory.getLog(TenantDomainCache.class);
    private static TenantDomainCache tenantDomainCache = new TenantDomainCache();

    private TenantDomainCache() {
    }

    /**
     * Gets a new instance of TenantCache.
     *
     * @return A new instance of TenantCache.
     */
    public synchronized static TenantDomainCache getInstance() {
        return tenantDomainCache;
    }

    private Cache<TenantIdKey, TenantDomainEntry> getTenantDomainCache() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        Cache<TenantIdKey, TenantDomainEntry> cache = cacheManager.getCache(TENANT_DOMAIN_CACHE);
        return cache;
    }

    /**
     * Add a cache entry.
     *
     * @param key   Key which cache entry is indexed.
     * @param entry Actual object where cache entry is placed.
     */
    public void addToCache(TenantIdKey key, TenantDomainEntry entry) {
        // Element already in the cache. Remove it first
        clearCacheEntry(key);

        Cache<TenantIdKey, TenantDomainEntry> cache = getTenantDomainCache();
        if (cache != null) {
            cache.put(key, entry);
            log.debug(TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER + ", added the entry : " + entry
                    + " for the key : " + key + " successfully");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting the cache : " + TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER);
            }
        }
    }

    /**
     * Retrieves a cache entry.
     *
     * @param key CacheKey
     * @return Cached entry if the key presents, else returns null.
     */
    public TenantDomainEntry getValueFromCache(TenantIdKey key) {
        Cache<TenantIdKey, TenantDomainEntry> cache = getTenantDomainCache();
        if (cache != null) {
            if (cache.containsKey(key)) {
                TenantDomainEntry entry = cache.get(key);
                if (log.isDebugEnabled()) {
                    log.debug(TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER + ", found the entry : " + entry
                            + " for the key : " + key + " successfully");
                }
                return entry;
            }
            if (log.isDebugEnabled()) {
                log.debug(TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER + ", doesn't contain the key : " + key);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting the cache : " + TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER);
            }
        }
        return null;
    }


    /**
     * Clears a cache entry.
     *
     * @param key Key to clear cache.
     */
    public void clearCacheEntry(TenantIdKey key) {
        Cache<TenantIdKey, TenantDomainEntry> cache = getTenantDomainCache();
        if (cache != null) {
            if (cache.containsKey(key)) {
                cache.remove(key);
                if (log.isDebugEnabled()) {
                    log.debug(TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER + ", is removed entry for the key : " + key + " successfully");
                }
            }

            if (log.isDebugEnabled()) {
                log.debug(TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER + ", doen't contain the key : " + key);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting the cache : " + TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER);
            }
        }
    }

    /**
     * Remove everything in the cache.
     */
    public void clear() {
        Cache<TenantIdKey, TenantDomainEntry> cache = getTenantDomainCache();
        if (cache != null) {
            cache.removeAll();
            if (log.isDebugEnabled()) {
                log.debug(TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER + ", is cleared successfully");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Error while getting the cache : " + TENANT_DOMAIN_CACHE + " which is under " + TENANT_DOMAIN_CACHE_MANAGER);
            }
        }
    }

}
