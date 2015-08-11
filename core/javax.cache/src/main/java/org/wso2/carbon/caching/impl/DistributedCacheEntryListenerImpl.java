/**
 *  Copyright 2011-2013 Terracotta, Inc.
 *  Copyright 2011-2013 Oracle America Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.caching.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.cache.Cache;
import javax.cache.event.*;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

/**
 * Distributed cache listener implementation
 */
public class DistributedCacheEntryListenerImpl<K, V> implements CacheEntryCreatedListener<K, V>,
        CacheEntryUpdatedListener<K, V>, CacheEntryRemovedListener<K, V>, Serializable {
    private static final Log log = LogFactory.getLog(DistributedCacheEntryListenerImpl.class);
    Map<K, V> localCache;
    Cache<K, V> distributedCache;
    private CacheImpl cacheImpl;

    public DistributedCacheEntryListenerImpl(CacheImpl cacheImpl) {
        localCache = cacheImpl.getLocalCache();
        distributedCache = cacheImpl.getDistributedCache();
        this.cacheImpl = cacheImpl;
    }


    @Override
    public void onCreated(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents)
            throws CacheEntryListenerException {
        Iterator<CacheEntryEvent<? extends K, ? extends V>> iterator = cacheEntryEvents.iterator();
        while (iterator.hasNext()) {
            CacheEntryEvent<? extends K, ? extends V> next = iterator.next();
            CacheEntry<K, V> value = (CacheEntry<K, V>) distributedCache.get(next.getKey());
            if (value != null) {
                cacheImpl.notifyCacheEntryCreated(value.getKey(), value.getValue());
            }

            if (!localCache.containsKey(next.getKey())) return;

            if (value != null) {
                localCache.put(next.getKey(), (V) value);
            }
        }
    }

    @Override
    public void onRemoved(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents)
            throws CacheEntryListenerException {
        Iterator<CacheEntryEvent<? extends K, ? extends V>> iterator = cacheEntryEvents.iterator();
        while (iterator.hasNext()) {
            CacheEntryEvent<? extends K, ? extends V> next = iterator.next();
            //Trigger registered listeners when a distributed cache entry is getting removed.
            CacheEntry<K, V> value = (CacheEntry<K, V>) distributedCache.get(next.getKey());
            if (value != null) {
                cacheImpl.notifyCacheEntryRemoved(value.getKey(), value.getValue());
            }


            localCache.remove(next.getKey());
        }


    }

    @Override
    public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> cacheEntryEvents)
            throws CacheEntryListenerException {
        Iterator<CacheEntryEvent<? extends K, ? extends V>> iterator = cacheEntryEvents.iterator();
        while (iterator.hasNext()) {
            CacheEntryEvent<? extends K, ? extends V> next = iterator.next();
            //Trigger registered listeners when a distributed cache entry is getting updated.
            CacheEntry<K, V> value = (CacheEntry<K, V>) distributedCache.get(next.getKey());
            if (value != null) {
                cacheImpl.notifyCacheEntryUpdated(value.getKey(), value.getValue());
            }

            if (!localCache.containsKey(next.getKey())) return;

            if (value != null) {
                localCache.put(next.getKey(), (V) value);
            }
        }


    }
}