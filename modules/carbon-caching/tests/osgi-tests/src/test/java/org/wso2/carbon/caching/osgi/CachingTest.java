/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.caching.osgi;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.caching.CarbonCachingService;

import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import javax.inject.Inject;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CachingTest {
    private Duration cacheExpiry = new Duration(TimeUnit.MINUTES, 15);

    @Inject
    private CarbonCachingService cachingService;

    private static final String CACHE_NAME = "foo";
    private static final String KEY = "k";
    private static final String VALUE = "v";

    @Test
    public void testCachePut() throws Exception {

        getCache(CACHE_NAME).put(KEY, VALUE);
        assertEquals(getCache(CACHE_NAME).get(KEY), VALUE, "Value not found in cache");
    }

    @Test(dependsOnMethods = "testCachePut")
    public void testCacheDelete() throws Exception {
        assertEquals(getCache(CACHE_NAME).get(KEY), VALUE, "Value not found in cache");
        getCache(CACHE_NAME).remove(KEY);
        assertNull(getCache(CACHE_NAME).get(KEY), "Value did not get removed from cache");
    }

    private Cache<String, String> getCache(String cacheName) {
        CachingProvider provider = cachingService.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        Cache<String, String> cache = cacheManager.getCache(cacheName, String.class, String.class);
        if (cache == null) {
            cache = initCache(cacheName, cacheManager);
        }
        return cache;
    }

    /**
     * we initialize a cache with name
     *
     * @param name Name of the cache
     */
    private Cache<String, String> initCache(String name, CacheManager cacheManager) {

        //configure the cache
        MutableConfiguration<String, String> config = new MutableConfiguration<>();
        config.setStoreByValue(true)
                .setTypes(String.class, String.class)
                .setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(cacheExpiry))
                .setStatisticsEnabled(false);

        //create the cache
        return cacheManager.createCache(name, config);
    }
}
