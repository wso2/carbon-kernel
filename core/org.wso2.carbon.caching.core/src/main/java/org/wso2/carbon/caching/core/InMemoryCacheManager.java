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
package org.wso2.carbon.caching.core;

import net.sf.jsr107cache.*;
import net.sf.jsr107cache.CacheEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of an in-memory cache manager.
 */
@SuppressWarnings("unused")
public class InMemoryCacheManager implements CarbonCacheManager {

    private final Map<String, Cache> caches = Collections.synchronizedMap(
            new HashMap<String, Cache>());

    public void initialize(String carbonHome) {
        // we really have nothing to do in here.
    }

    public String getDefaultCacheName() {
        return "";
    }

    public Cache getCache(String cacheName) {
        Cache cache = caches.get(cacheName);
        if (cache == null) {
            // We wrap each In-memory Cache, so that it can be used via the standard JSR107
            // JCache API.
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(
                        InMemoryCacheManager.class.getClassLoader());
                cache = new InMemoryJCacheWrapper(new ConcurrentHashMap<Object, Object>());
                registerCache(cacheName, cache);
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
            }
        }
        return cache;
    }


    public Cache getDistributedCacheWithMode(String cacheName, String cacheModeString,
                                             boolean isSync, boolean fallBackToDefault) {
	    return getCache(cacheName);
    }

	public Cache getLocalCache(String cacheName) {
        return getCache(cacheName);
    }

    public void registerCache(String cacheName, Cache cache) {
        if (cache == null) {
            caches.remove(cacheName);
        } else {
            // We register a cache only if there is no cache already registered.
            if (caches.get(cacheName) == null) {
                caches.put(cacheName, cache);
            }
        }
    }

    public CacheFactory getCacheFactory() throws CacheException {
        throw new CacheException("The InMemoryCacheManager does not provide an " +
                "in-built nor look-up a CacheFactory.");
    }

    ////////////////////////////////////////////////////////
    // JSR107 JCache wrapper for In-memory cache
    ////////////////////////////////////////////////////////

    private static class InMemoryJCacheEntry implements net.sf.jsr107cache.CacheEntry {

        private Object key;
        private Object value;
        long createdTime;

        private InMemoryJCacheEntry(Object key, Object value) {
            this.key = key;
            this.value = value;
            this.createdTime = new Date().getTime();
        }

        public int getHits() {
            throw new UnsupportedOperationException("The In-memory cache manager does not allow " +
                    "to find the number of hits on a per-entry basis.");
        }

        public long getLastAccessTime() {
            throw new UnsupportedOperationException("The In-memory cache manager does not provide " +
                    "timing details.");
        }

        public long getLastUpdateTime() {
            throw new UnsupportedOperationException("The In-memory cache manager does not provide " +
                    "timing details.");
        }

        public long getCreationTime() {
            return createdTime;
        }

        public long getExpirationTime() {
            throw new UnsupportedOperationException("The In-memory cache manager does not provide " +
                    "timing details.");
        }

        public long getVersion() {
            // We use the time at which the cache entry was created as the version of the entry.
            return createdTime;
        }

        public boolean isValid() {
            return true;
        }

        public long getCost() {
            // This implementation does not have a notion of cost. Accordingly, 0 is always
            // returned.
            return 0;
        }

        public Object getKey() {
            return key != null ? key : null;
        }

        public Object getValue() {
            return value != null ? value : null;
        }

        public Object setValue(Object o) {
            if (key != null) {
                value = o;
                return o;
            }
            return null;
        }
    }

    private static class InMemoryJCacheWrapper implements Cache {

        private Map<Object, Object> cache;

        public InMemoryJCacheWrapper(Map<Object, Object> cache) {
            this.cache = cache;
        }

        public boolean containsKey(Object o) {
            return cache.containsKey(o);
        }

        public boolean containsValue(Object o) {
            return cache.containsValue(o);
        }

        public Set entrySet() {
            return cache.entrySet();
        }

        public boolean equals(Object o) {
            return (o instanceof InMemoryJCacheWrapper) &&
                    ((InMemoryJCacheWrapper)o).cache.equals(cache);
        }

        public int hashCode() {
            return (int) (((long) cache.hashCode()) +
                    this.getClass().getCanonicalName().hashCode());
        }

        public boolean isEmpty() {
            return cache.isEmpty();
        }

        public Set keySet() {
            return cache.keySet();
        }

        @SuppressWarnings("unchecked")
        public void putAll(Map map) {
            cache.putAll(map);
        }

        public int size() {
            return cache.size();
        }

        public Collection values() {
            return cache.values();
        }

        public Object get(Object o) {
            return cache.get(o);
        }

        public Map getAll(Collection collection) throws CacheException {
            Map<Object, Object> output = new ConcurrentHashMap<Object, Object>();
            for (Object o : collection) {
                output.put(o, get(o));
            }
            return output;
        }

        public void load(Object o) throws CacheException {
            //
        }

        public void loadAll(Collection collection) throws CacheException {
            for (Object o : collection) {
                load(o);
            }
        }

        public Object peek(Object o) {
            return cache.entrySet().isEmpty() ? null : cache.entrySet().iterator().next();
        }

        @SuppressWarnings("unchecked")
        public Object put(Object o, Object o1) {
            return cache.put(o, o1);
        }

        public CacheEntry getCacheEntry(Object o) {
            return new InMemoryJCacheEntry(o, cache.get(o));
        }

        public CacheStatistics getCacheStatistics() {
            throw new UnsupportedOperationException("The InMemoryCacheManager does not provide " +
                    "statistics.");
        }

        public Object remove(Object o) {
            return cache.remove(o);
        }

        public void clear() {
            cache.clear();
        }

        public void evict() {
            // we have nothing to do in here.
        }

        public void addListener(CacheListener cacheListener) {
            // we have nothing to do in here.
        }

        public void removeListener(CacheListener cacheListener) {
            // we have nothing to do in here.
        }
    }
}
