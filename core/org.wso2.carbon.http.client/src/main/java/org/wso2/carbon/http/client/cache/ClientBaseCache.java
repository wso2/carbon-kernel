/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.http.client.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * A base class for all cache implementations in Identity modules. This maintains  caches in the tenanted space.
 * A copy of this class is maintained at org.wso2.carbon.identity.organization.management.service.cache component.
 *
 * @param <K> cache key type.
 * @param <V> cache value type.
 */
public abstract class ClientBaseCache<K extends Serializable, V> {

    private final Cache<K, V> cache;

    protected ClientBaseCache(int cacheSize, int expireAfterAccess, RemovalListener<K, V> removalListener) {

        cache = CacheBuilder.newBuilder()
                .maximumSize(cacheSize)
                .expireAfterAccess(expireAfterAccess, TimeUnit.MILLISECONDS)
                .removalListener(removalListener)
                .build();
    }

    public void put(K key, V value) {

        cache.put(key, value);
    }

    public V get(K key, Callable<V> loader) {

        try {
            return cache.get(key, loader);
        } catch (ExecutionException e) {
            // TODO: handle exception
            return null;
        }
    }

    public V get(K key) {

        return cache.getIfPresent(key);
    }

    public void cleanUp() {

        cache.cleanUp();
    }
}
