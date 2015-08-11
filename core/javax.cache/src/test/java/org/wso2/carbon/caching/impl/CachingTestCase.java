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
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

/**
 * TODO: class description
 */
public class CachingTestCase {

    private Cache<String, Integer> cache;
    private String key = "testKey";

    public CachingTestCase() throws URISyntaxException {
        System.setProperty("carbon.home", new File(".").getAbsolutePath());

        String cacheName = "sampleCache";
        // CacheManager cacheManager = Caching.getCacheManager(); // same as Caching.getCacheManagerFactory().getCacheManager("__default__");

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);

        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test"), null);
        cache = cacheManager.getCache(cacheName);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void checkNonExistentItem() throws Exception {
        assertNull(cache.get(key));
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            dependsOnMethods = "checkNonExistentItem",
            description = "")
    public void checkPut() throws Exception {
        Integer sampleValue = 1245;
        cache.put(key, sampleValue);
        assertEquals(cache.get(key), sampleValue);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void checkMultipleCacheManagers() throws URISyntaxException {
        String cacheName = "sampleCache";
        CacheManager cacheManager1 = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test-1"), null);
        Cache<String, Integer> cache1 = cacheManager1.getCache(cacheName);
        int value1 = 9876;
        cache1.put(key, value1);

        CacheManager cacheManager2 = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test-2"), null);
        Cache<String, String> cache2 = cacheManager2.getCache(cacheName);
        String value2 = "Afkham Azeez";
        cache2.put(key, value2);

        assertEquals(cache1.get(key).intValue(), value1);
        assertEquals(cache2.get(key), value2);

        assertNotEquals(cache1.get(key), value2);
        assertNotEquals(cache2.get(key), value1);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void checkMultipleCaches() throws URISyntaxException {
        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test-1"), null);
        Cache<String, Integer> cache1 = cacheManager.getCache("sampleCache1");
        Cache<String, String> cache2 = cacheManager.getCache("sampleCache2");

        int value1 = 9876;
        String value2 = "Afkham Azeez";
        cache1.put(key, value1);
        cache2.put(key, value2);

        assertEquals(cache1.get(key).intValue(), value1);
        assertEquals(cache2.get(key), value2);

        assertNotEquals(cache1.get(key), value2);
        assertNotEquals(cache2.get(key), value1);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void checkWithCustomCacheConfiguration() throws URISyntaxException {
        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test"), null);
        String cacheName = "cacheXXX";
        cache = cacheManager.createCache(cacheName, new MutableConfiguration<String, Integer>()
                .setExpiryPolicyFactory(
                        ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 10)))
                .setStoreByValue(false));
        int value = 9876;
        cache.put(key, value);
        assertEquals(cache.get(key).intValue(), value);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            expectedExceptions = {javax.cache.CacheException.class},
            dependsOnMethods = "checkWithCustomCacheConfiguration")
    public void testCreateExistingCache() throws URISyntaxException {
        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test"), null);
        String cacheName = "cacheXXX";
        cache = cacheManager.createCache(cacheName, new MutableConfiguration<String, Integer>()
                .setExpiryPolicyFactory(
                        ModifiedExpiryPolicy.factoryOf(new Duration(TimeUnit.SECONDS, 10)))
                .setStoreByValue(false));
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testSerializableObject() throws URISyntaxException {

        String name = "Afkham Azeez";
        String address = "301/2A, Dehiwela Road";
        Long id = (long) 789;
        SerializableTestObject obj = new SerializableTestObject(name, address, id);

        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test"), null);
        String cacheName = "sampleCacheX";
        Cache<Long, SerializableTestObject> cache = cacheManager.getCache(cacheName);
        cache.put(id, obj);

        Cache<Long, SerializableTestObject> cache2 = cacheManager.getCache(cacheName);

        assertEquals(cache2.get(id).getId(), id);
        assertEquals(cache2.get(id).getAddress(), address);
        assertEquals(cache2.get(id).getName(), name);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            dependsOnMethods = "testSerializableObject",
            description = "")
    public void testRemoveObjectFromCache() throws URISyntaxException {
        Long id = (long) 789;
        String cacheName = "sampleCacheX";
        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test"), null);
        Cache<Long, SerializableTestObject> cache = cacheManager.getCache(cacheName);
        assertNotNull(cache.get(id));

        Cache<Long, SerializableTestObject> cache2 = cacheManager.getCache(cacheName);
        cache2.remove(id);
        assertNull(cache.get(id));
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheIterator() throws URISyntaxException {
        Long id = (long) 789;
        String cacheName = "sampleCacheABC";
        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test"), null);
        Cache<Long, Long> cache = cacheManager.getCache(cacheName);
        cache.put((long) 123, id);
        cache.put((long) 456, id);
        cache.put((long) 789, id);
        cache.put((long) 12, id);

        int entries = 0;
        for (Cache.Entry<Long, Long> entry : cache) {
            assertNotNull(entry);
            entries++;
        }
        assertEquals(entries, 4);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheLoaderLoadAll() throws URISyntaxException {
        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test"), null);
        String cacheName = "cacheYYY";
        Cache<String, String> cache =
                cacheManager.createCache(cacheName, new MutableConfiguration<String, String>()
                        .setReadThrough(true)
                        .setCacheLoaderFactory(new TestCacheLoader<String, String>()));
        HashSet<String> hashSet = new HashSet<>();

        for (int i = 1; i < 6; i++) {
            hashSet.add("key" + i);
        }

        cache.loadAll(hashSet, false, null);

        for (int i = 1; i < 6; i++) {
            assertNull(cache.get("key" + i));
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        for (int i = 1; i < 6; i++) {
            assertNotNull(cache.get("key" + i));
        }
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheLoaderLoad() throws URISyntaxException {
        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("test"), null);
        String cacheName = "testCacheLoaderLoad-ZZZ";
        Cache<String, String> cache =
                cacheManager.createCache(cacheName, new MutableConfiguration<String, String>()
                        .setCacheLoaderFactory(new TestCacheLoader<>()));
//        Future<String> future = cache.load("key1");
        cache.put("key1", "ss");
        assertNotNull(cache.get("key1"));
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheExpiry() throws URISyntaxException {
        CacheManager cacheManager = Caching.getCachingProvider("org.wso2.carbon.caching.impl.CachingProviderImpl").getCacheManager(new URI("testCacheExpiry-manager"), null);
        String cacheName = "testCacheExpiry";
        Cache<String, Integer> cache = cacheManager.createCache(cacheName, new MutableConfiguration<String, Integer>()
                .setExpiryPolicyFactory(ModifiedExpiryPolicy.factoryOf(
                        new Duration(TimeUnit.MILLISECONDS, 100)))
                .setStoreByValue(false));
        int value = 9876;
        cache.put(key, value);
        assertEquals(cache.get(key).intValue(), value);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }
        ((CacheImpl) cache).runCacheExpiry();
        assertNull(cache.get(key));
    }
}
