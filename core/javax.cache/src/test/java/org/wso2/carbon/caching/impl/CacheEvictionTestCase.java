/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.caching.impl;

import org.testng.annotations.Test;
import org.wso2.carbon.caching.impl.eviction.LeastRecentlyUsedEvictionAlgorithm;
import org.wso2.carbon.caching.impl.eviction.MostRecentlyUsedEvictionAlgorithm;
import org.wso2.carbon.caching.impl.eviction.RandomEvictionAlgorithm;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.File;

import static org.testng.Assert.*;


/**
 * Tests for Cache Eviction
 */
public class CacheEvictionTestCase {

    public CacheEvictionTestCase() {
        System.setProperty("carbon.home", new File(".").getAbsolutePath());
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testEviction() {
        String cacheName = "sampleCache";

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);

        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        Cache<String, Integer> cache = cacheManager.getCache(cacheName);

        String oldestKey = "x" + System.currentTimeMillis();
        cache.put(oldestKey, Integer.MAX_VALUE);
        for (int i = 0; i < 10500; i++) {
            cache.put("a" + System.currentTimeMillis(), i);
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
        String newestKey = "y" + System.currentTimeMillis();
        cache.put(newestKey, Integer.MAX_VALUE);

        if(cache instanceof CacheImpl) {
            CacheImpl cacheImpl = (CacheImpl) cache;
            cacheImpl.setEvictionAlgorithm(new LeastRecentlyUsedEvictionAlgorithm());
            cacheImpl.runCacheExpiry();
            assertEquals(((CacheImpl) cache).getAll().size(), 7500);
            assertNull(cache.get(oldestKey));
            assertNotNull(cache.get(newestKey));
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testMRUCacheEviction() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        String cacheName = "testMRUCacheEviction";
        Cache<String, Integer> cache = cacheManager.getCache(cacheName);

        if(cache instanceof CacheImpl) {
            ((CacheImpl) cache).setCapacity(2);
        }

        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        int value1 = 9877;
        int value2 = 1234;
        int value3 = 5678;
        cache.put(key1, value1);
        assertEquals(cache.get(key1).intValue(), value1);
        try {
            Thread.sleep(2);
        } catch (InterruptedException ignored) {
        }
        cache.put(key2, value2);
        assertEquals(cache.get(key2).intValue(), value2);
        try {
            Thread.sleep(2);
        } catch (InterruptedException ignored) {
        }
        cache.put(key3, value3);
        assertEquals(cache.get(key3).intValue(), value3);

        if(cache instanceof CacheImpl) {
            CacheImpl cacheImpl = (CacheImpl) cache;
            cacheImpl.setEvictionAlgorithm(new MostRecentlyUsedEvictionAlgorithm());
            cacheImpl.runCacheExpiry();
            assertNull(cache.get(key3));
            assertEquals(cache.get(key1).intValue(), value1);
            assertEquals(cache.get(key2).intValue(), value2);
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testLRUCacheEviction() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        String cacheName = "testLRUCacheEvictionXX";
        Cache<String, Integer> cache = cacheManager.getCache(cacheName);

        if(cache instanceof  CacheImpl) {
            ((CacheImpl) cache).setCapacity(2);
        }

        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        int value1 = 9879;
        int value2 = 1234;
        int value3 = 5678;
        cache.put(key1, value1);
        assertEquals(cache.get(key1).intValue(), value1);
        try {
            Thread.sleep(2);
        } catch (InterruptedException ignored) {
        }
        cache.put(key2, value2);
        assertEquals(cache.get(key2).intValue(), value2);
        try {
            Thread.sleep(2);
        } catch (InterruptedException ignored) {
        }
        cache.put(key3, value3);
        assertEquals(cache.get(key3).intValue(), value3);
        if(cache instanceof CacheImpl) {
            CacheImpl cacheImpl = (CacheImpl) cache;
            cacheImpl.setEvictionAlgorithm(new LeastRecentlyUsedEvictionAlgorithm());
            cacheImpl.runCacheExpiry();
            assertNull(cache.get(key1));
            assertEquals(cache.get(key2).intValue(), value2);
            assertEquals(cache.get(key3).intValue(), value3);
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testRandomCacheEviction() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        String cacheName = "testRandomCacheEviction";
        Cache<String, Integer> cache = cacheManager.getCache(cacheName);

        if(cache instanceof CacheImpl) {
            ((CacheImpl) cache).setCapacity(2);
        }

        String key1 = "key1";
        String key2 = "key2";
        String key3 = "key3";
        int value1 = 9876;
        int value2 = 1234;
        int value3 = 5678;
        cache.put(key1, value1);
        assertEquals(cache.get(key1).intValue(), value1);
        cache.put(key2, value2);
        assertEquals(cache.get(key2).intValue(), value2);
        cache.put(key3, value3);
        if(cache instanceof CacheImpl) {
            CacheImpl cacheImpl = (CacheImpl) cache;
            cacheImpl.setEvictionAlgorithm(new RandomEvictionAlgorithm());
            cacheImpl.runCacheExpiry();
            assertEquals(((CacheImpl) cache).getAll().size(), 2);
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testDefaultCacheEviction() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        String cacheName = "testDefaultCacheEviction";
        Cache<String, Integer> cache = cacheManager.getCache(cacheName);
        for (int i = 0; i < 20000; i++) {
            cache.put("key" + i, i);
        }
        if(cache instanceof CacheImpl) {
            CacheImpl cacheImpl = (CacheImpl) cache;
            cacheImpl.setEvictionAlgorithm(new LeastRecentlyUsedEvictionAlgorithm());
            cacheImpl.runCacheExpiry();
            assertEquals(cacheImpl.getAll().size(),
                    (int) (CachingConstants.DEFAULT_CACHE_CAPACITY * (1 - CachingConstants.CACHE_EVICTION_FACTOR)));
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testDefaultMRUCacheEviction() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        String cacheName = "testDefaultMRUCacheEviction";
        Cache<String, Integer> cache = cacheManager.getCache(cacheName);
        for (int i = 0; i < 20000; i++) {
            cache.put("key" + i, i);
        }
        if(cache instanceof CacheImpl) {
            CacheImpl cacheImpl = (CacheImpl) cache;
            cacheImpl.setEvictionAlgorithm(new MostRecentlyUsedEvictionAlgorithm());
            cacheImpl.runCacheExpiry();
            assertEquals(((CacheImpl) cache).getAll().size(),
                    (int) (CachingConstants.DEFAULT_CACHE_CAPACITY * (1 - CachingConstants.CACHE_EVICTION_FACTOR)));
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testDefaultRandomCacheEviction() {
        CacheManager cacheManager = Caching.getCachingProvider().getCacheManager();
        String cacheName = "testDefaultRandomCacheEviction";
        Cache<String, Integer> cache = cacheManager.getCache(cacheName);
        for (int i = 0; i < 20000; i++) {
            cache.put("key" + i, i);
        }
        if(cache instanceof CacheImpl) {
            CacheImpl cacheImpl = (CacheImpl) cache;
            cacheImpl.setEvictionAlgorithm(new RandomEvictionAlgorithm());
            cacheImpl.runCacheExpiry();
            assertEquals(((CacheImpl) cache).getAll().size(),
                    (int) (CachingConstants.DEFAULT_CACHE_CAPACITY * (1 - CachingConstants.CACHE_EVICTION_FACTOR)));
        }
    }
}
