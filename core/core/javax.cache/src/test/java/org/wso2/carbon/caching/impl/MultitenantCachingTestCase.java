/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.caching.impl;

import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheManager;
import javax.cache.Caching;
import java.io.File;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test case for tenant based caching scenarios
 */
public class MultitenantCachingTestCase {
    private Cache<String, Integer> cache;

    public MultitenantCachingTestCase() {
        System.setProperty("carbon.home", new File(".").getAbsolutePath());

        String cacheName = "sampleCache";
        // CacheManager cacheManager = Caching.getCacheManager(); // same as Caching.getCacheManagerFactory().getCacheManager("__default__");

        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        cc.setTenantDomain("foo.com");
        cc.setTenantId(1);

        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager("test");
        cache = cacheManager.getCache(cacheName);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107.mt"},
          expectedExceptions = {SecurityException.class},
          description = "")
    public void testIllegalAccess() {
        Integer sampleValue = 1245;
        String key1 = "testIllegalAccess-123";
        cache.put(key1, sampleValue);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("bar.com");
            cc.setTenantId(2);

            cache.get(key1); // Should throw SecurityException
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107.mt"},
          description = "")
    public void testLegalAccess() {
        Integer sampleValue = 1245;
        String key1 = "testLegalAccess-123";
        cache.put(key1, sampleValue);

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("foo.com");
            cc.setTenantId(1);

            assertEquals(cache.get(key1), sampleValue);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107.mt"},
          description = "")
    public void testCreateCacheWithSameNameByTwoTenants() {
        Integer sampleValue = 1245;
        String key1 = "testCreateCacheWithSameNameByTwoTenants-123";
        String key2 = "testCreateCacheWithSameNameByTwoTenants-1234";
        String cacheManagerName = "testCacheManager";
        String cacheName = "sampleCache";

        // Tenant wso2.com
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("wso2.com");
            cc.setTenantId(1);

            CacheManager cacheManager =
                    Caching.getCacheManagerFactory().getCacheManager(cacheManagerName);
            Cache<String, Integer> cache1 = cacheManager.getCache(cacheName);
            cache1.put(key1, sampleValue);
            cache1.put(key2, sampleValue);
            cache1 = cacheManager.getCache(cacheName);
            assertEquals(sampleValue, cache1.get(key1));
            checkCacheSize(cache1, 2);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        // Tenant ibm.com
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("ibm.com");
            cc.setTenantId(2);

            CacheManager cacheManager =
                    Caching.getCacheManagerFactory().getCacheManager(cacheManagerName);
            Cache<String, Integer> cache1 = cacheManager.getCache(cacheName);
            cache1.put(key1, sampleValue);
            cache1 = cacheManager.getCache(cacheName);
            assertEquals(sampleValue, cache1.get(key1));

            checkCacheSize(cache1, 1);
            cache1 = cacheManager.getCache(cacheName);
            cache1.remove(key1);
            cache1 = cacheManager.getCache(cacheName);
            checkCacheSize(cache1, 0);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107.mt"},
          description = "")
    public void testCreateCacheWithSameNameByTwoTenantsWithDefaultCacheManager() {
        Integer sampleValue = 1245;
        String key1 = "testCreateCacheWithSameNameByTwoTenants-123";
        String key2 = "testCreateCacheWithSameNameByTwoTenants-1234";
        String cacheName = "sampleCache";

        // Tenant apple.com
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("apple.com");
            cc.setTenantId(1);

            CacheManager cacheManager = Caching.getCacheManager(); // Default CacheManager
            Cache<String, Integer> cache1 = cacheManager.getCache(cacheName);
            cache1.put(key1, sampleValue);
            cache1.put(key2, sampleValue);
            cache1 = cacheManager.getCache(cacheName);
            assertEquals(sampleValue, cache1.get(key1));
            checkCacheSize(cache1, 2);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        // Tenant orange.com
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("orange.com");
            cc.setTenantId(2);

            CacheManager cacheManager = Caching.getCacheManager(); // Default CacheManager
            Cache<String, Integer> cache1 = cacheManager.getCache(cacheName);
            cache1.put(key1, sampleValue);
            cache1 = cacheManager.getCache(cacheName);
            assertEquals(sampleValue, cache1.get(key1));

            checkCacheSize(cache1, 1);
            cache1 = cacheManager.getCache(cacheName);
            cache1.remove(key1);
            cache1 = cacheManager.getCache(cacheName);
            checkCacheSize(cache1, 0);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107.mt"},
          description = "")
    public void testCacheBuilderForTenants() {
        String cacheName = "testCacheBuilderForTenants";
        String key = "kxkx";
        int value = 9876;

        // Tenant wso2.org
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("wso2.org");
            cc.setTenantId(4);

            CacheManager cacheManager = Caching.getCacheManager(); // Default CacheManager
            Cache<String, Integer> cache = cacheManager.<String, Integer>createCacheBuilder(cacheName).
                    setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, 10)).
                    setStoreByValue(false).build();

            cache.put(key, value);
            assertEquals(cache.get(key).intValue(), value);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

        // Tenant afkham.org
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("afkham.org");
            cc.setTenantId(5);

            CacheManager cacheManager = Caching.getCacheManager(); // Default CacheManager
            Cache<String, Integer> cache = cacheManager.<String, Integer>createCacheBuilder(cacheName).
                    setExpiry(CacheConfiguration.ExpiryType.MODIFIED, new CacheConfiguration.Duration(TimeUnit.SECONDS, 10)).
                    setStoreByValue(false).build();
            cache.put(key, value);
            assertEquals(cache.get(key).intValue(), value);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107.mt"},
          expectedExceptions = {javax.cache.CacheException.class},
          description = "")
    public void testCreateExistingCache() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("apple.com");
            cc.setTenantId(1);
            CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager("test");
            String cacheName = "testCreateExistingCache";
            cacheManager.<String, Integer>createCacheBuilder(cacheName).
                    setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                              new CacheConfiguration.Duration(TimeUnit.SECONDS, 10)).
                    setStoreByValue(false).build();
            cacheManager.<String, Integer>createCacheBuilder(cacheName).
                    setExpiry(CacheConfiguration.ExpiryType.MODIFIED,
                              new CacheConfiguration.Duration(TimeUnit.SECONDS, 10)).
                    setStoreByValue(false).build();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107.mt"},
          description = "")
    public void testCacheLoaderForTenants() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            cc.setTenantDomain("bikes.com");
            cc.setTenantId(1);
            CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager("test");
            String cacheName = "testCacheLoaderForTenants";
            Cache<String, String> cache =
                    cacheManager.<String, String>createCacheBuilder(cacheName).
                            setCacheLoader(new TestCacheLoader<String, String>()).build();
            Future<String> future = cache.load("key1");
            while (!future.isDone()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
            assertNotNull(cache.get("key1"));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @SuppressWarnings("unused")
    private void checkCacheSize(Cache<String, Integer> cache1, int expectedCacheSize) {
        int cacheSize = 0;
        for (Cache.Entry<String, Integer> entry : cache1) {
            cacheSize++;
        }
        assertEquals(cacheSize, expectedCacheSize);
    }
}
