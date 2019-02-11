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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.event.*;
import java.io.File;

import static org.testng.Assert.*;

/**
 * Testing CacheListeners
 */
public class CacheListenerTestCase {

    public static final String TEST_CACHE_ENTRY_UPDATED = "testCacheEntryUpdated";
    public static final String TEST_CACHE_ENTRY_READ = "testCacheEntryRead";
    public static final String TEST_CACHE_ENTRY_REMOVED = "testCacheEntryRemoved";
    public static final String TEST_CACHE_ENTRY_EXPIRED = "testCacheEntryExpired";
    public static final String TEST_CACHE_ENTRY_CREATED = "testCacheEntryCreated";
    private static final String TEST_FULL_SCENARIO_CACHE_REMOVED = "testFullScenarioCacheRemoved";
    private static final String TEST_FULL_SCENARIO_CACHE_EXPIRED = "testFullScenarioCacheExpired";
    public static final String CACHE_NAME = "CacheListenerTestCase-cache";
    private Cache<String, Long> cache;
    private CacheEntryCreatedListenerImpl<String, Long> cacheEntryCreatedListener;
    private CacheEntryExpiredListenerImpl<String, Long> cacheEntryExpiredListener;
    private CacheEntryReadListenerImpl<String, Long> cacheEntryReadListener;
    private CacheEntryRemovedListenerImpl<String, Long> cacheEntryRemovedListener;
    private CacheEntryUpdatedListenerImpl<String, Long> cacheEntryUpdatedListener;
    private String key;

    @BeforeMethod
    public void setup() {
        System.setProperty("carbon.home", new File(".").getAbsolutePath());

        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("foo.com");
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(1);

        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager("test");
        cache = cacheManager.getCache(CACHE_NAME);

        cacheEntryCreatedListener = new CacheEntryCreatedListenerImpl<String, Long>();
        cache.registerCacheEntryListener(cacheEntryCreatedListener);
        cacheEntryExpiredListener = new CacheEntryExpiredListenerImpl<String, Long>();
        cache.registerCacheEntryListener(cacheEntryExpiredListener);
        cacheEntryReadListener = new CacheEntryReadListenerImpl<String, Long>();
        cache.registerCacheEntryListener(cacheEntryReadListener);
        cacheEntryRemovedListener = new CacheEntryRemovedListenerImpl<String, Long>();
        cache.registerCacheEntryListener(cacheEntryRemovedListener);
        cacheEntryUpdatedListener = new CacheEntryUpdatedListenerImpl<String, Long>();
        cache.registerCacheEntryListener(cacheEntryUpdatedListener);
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheEntryCreated() {
        String key = TEST_CACHE_ENTRY_CREATED;
        setKey(key);
        cache.put(key, System.currentTimeMillis());
        assertTrue(cacheEntryCreatedListener.isInvoked());
        assertFalse(cacheEntryExpiredListener.isInvoked());
        assertFalse(cacheEntryReadListener.isInvoked());
        assertFalse(cacheEntryRemovedListener.isInvoked());
        assertFalse(cacheEntryUpdatedListener.isInvoked());
    }
    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheEntryUpdated() {
        String key = TEST_CACHE_ENTRY_UPDATED;
        setKey(key);
        cache.put(key, System.currentTimeMillis());
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        cache.put(key, System.currentTimeMillis());
        assertTrue(cacheEntryCreatedListener.isInvoked());
        assertTrue(cacheEntryUpdatedListener.isInvoked());
        assertFalse(cacheEntryExpiredListener.isInvoked());
        assertFalse(cacheEntryReadListener.isInvoked());
        assertFalse(cacheEntryRemovedListener.isInvoked());
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheEntryRead() {
        String key = TEST_CACHE_ENTRY_READ;
        setKey(key);
        cache.put(key, System.currentTimeMillis());
        cache.get(key);

        assertTrue(cacheEntryCreatedListener.isInvoked());
        assertTrue(cacheEntryReadListener.isInvoked());
        assertFalse(cacheEntryExpiredListener.isInvoked());
        assertFalse(cacheEntryRemovedListener.isInvoked());
        assertFalse(cacheEntryUpdatedListener.isInvoked());
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheEntryRemoved() {
        String key = TEST_CACHE_ENTRY_REMOVED;
        setKey(key);
        cache.put(key, System.currentTimeMillis());
        cache.remove(key);

        assertTrue(cacheEntryCreatedListener.isInvoked());
        assertTrue(cacheEntryRemovedListener.isInvoked());
        assertFalse(cacheEntryReadListener.isInvoked());
        assertFalse(cacheEntryExpiredListener.isInvoked());
        assertFalse(cacheEntryUpdatedListener.isInvoked());
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testCacheEntryExpired() {
        String key = TEST_CACHE_ENTRY_EXPIRED;
        setKey(key);
        cache.put(key, System.currentTimeMillis());
        ((CacheImpl) cache).expire(key);

        assertTrue(cacheEntryCreatedListener.isInvoked());
        assertTrue(cacheEntryExpiredListener.isInvoked());
        assertFalse(cacheEntryRemovedListener.isInvoked());
        assertFalse(cacheEntryReadListener.isInvoked());
        assertFalse(cacheEntryUpdatedListener.isInvoked());
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testFullScenarioCacheEntryRemoved(){
        String key = TEST_FULL_SCENARIO_CACHE_REMOVED;
        setKey(key);
        cache.put(key, System.currentTimeMillis());
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        cache.put(key, System.currentTimeMillis());
        cache.get(key);
        cache.remove(key);

        assertTrue(cacheEntryCreatedListener.isInvoked());
        assertTrue(cacheEntryReadListener.isInvoked());
        assertTrue(cacheEntryUpdatedListener.isInvoked());
        assertTrue(cacheEntryRemovedListener.isInvoked());
        assertFalse(cacheEntryExpiredListener.isInvoked());
    }

    @Test(groups = {"org.wso2.carbon.clustering.hazelcast.jsr107"},
            description = "")
    public void testFullScenarioCacheEntryExpired(){
        String key = TEST_FULL_SCENARIO_CACHE_EXPIRED;
        setKey(key);
        cache.put(key, System.currentTimeMillis());
        try {
            Thread.sleep(100);
        } catch (InterruptedException ignored) {
        }
        cache.put(key, System.currentTimeMillis());
        cache.get(key);
        ((CacheImpl)cache).expire(key);

        assertTrue(cacheEntryCreatedListener.isInvoked());
        assertTrue(cacheEntryReadListener.isInvoked());
        assertTrue(cacheEntryUpdatedListener.isInvoked());
        assertTrue(cacheEntryExpiredListener.isInvoked());
        assertFalse(cacheEntryRemovedListener.isInvoked());
    }

    private void setKey(String key){
        this.key = key;
    }

    private abstract class CacheEntryListener {
        protected boolean isInvoked;

        public boolean isInvoked() {
            return isInvoked;
        }
    }

    private class CacheEntryCreatedListenerImpl<K, V> extends CacheEntryListener implements CacheEntryCreatedListener<K, V> {

        @Override
        public void entryCreated(CacheEntryEvent event) throws CacheEntryListenerException {
            isInvoked = true;
            assertEquals(event.getSource().getName(), CACHE_NAME);
            assertEquals(event.getKey(), key);
            assertTrue(event.getValue() instanceof Long);
        }
    }

    private class CacheEntryUpdatedListenerImpl<K, V> extends CacheEntryListener implements CacheEntryUpdatedListener<K, V> {

        @Override
        public void entryUpdated(CacheEntryEvent event) throws CacheEntryListenerException {
            isInvoked = true;
            assertEquals(event.getSource().getName(), CACHE_NAME);
            assertEquals(event.getKey(), key);
            assertTrue(event.getValue() instanceof Long);
        }
    }

    private class CacheEntryReadListenerImpl<K, V> extends CacheEntryListener implements CacheEntryReadListener<K, V> {

        @Override
        public void entryRead(CacheEntryEvent event) throws CacheEntryListenerException {
            isInvoked = true;
            assertEquals(event.getSource().getName(), CACHE_NAME);
            assertEquals(event.getKey(), key);
            assertTrue(event.getValue() instanceof Long);
        }
    }

    private class CacheEntryRemovedListenerImpl<K, V> extends CacheEntryListener implements CacheEntryRemovedListener<K, V> {

        @Override
        public void entryRemoved(CacheEntryEvent event) throws CacheEntryListenerException {
            isInvoked = true;
            assertEquals(event.getSource().getName(), CACHE_NAME);
            assertEquals(event.getKey(), key);
            assertTrue(event.getValue() instanceof Long);
        }
    }

    private class CacheEntryExpiredListenerImpl<K, V> extends CacheEntryListener implements CacheEntryExpiredListener {

        @Override
        public void entryExpired(CacheEntryEvent event) throws CacheEntryListenerException {
            isInvoked = true;
            assertEquals(event.getSource().getName(), CACHE_NAME);
            assertEquals(event.getKey(), key);
            assertTrue(event.getValue() instanceof Long);
        }
    }
}
