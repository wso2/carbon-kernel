/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.caching.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ModifiedExpiryPolicy;
import javax.cache.spi.CachingProvider;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * carbon cache manager implementation
 */
public class CarbonCacheManager implements CacheManager {
    private static final Log log = LogFactory.getLog(CarbonCacheManager.class);
    private static CacheCleanupTask cacheCleanupTask = new CacheCleanupTask();
    private static Random randomGenerator = new Random();

    static {
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread th = new Thread(runnable);
                th.setName("CacheExpirySchedulerThread-" + randomGenerator.nextInt(100));
                return th;
            }
        };
        ScheduledExecutorService cacheExpiryScheduler =
                Executors.newScheduledThreadPool(10, threadFactory);
        cacheExpiryScheduler.scheduleWithFixedDelay(cacheCleanupTask, 30, 30, TimeUnit.SECONDS);
    }

    private final HashMap<String, CacheImpl<?, ?>> caches = new HashMap<String, CacheImpl<?, ?>>();
    private final CachingProviderImpl cachingProvider;
    private final URI uri;
    private final WeakReference<ClassLoader> classLoaderReference;
    private final Properties properties;
    private String ownerTenantDomain;
    private int ownerTenantId;
    private String name;
    private long lastAccessed = -1;
    private boolean isClosed;

    public CarbonCacheManager(CachingProviderImpl cachingProvider, URI uri,
                              ClassLoader classLoader, Properties properties) {

        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        if (carbonContext == null) {
            throw new IllegalStateException("CarbonContext cannot be null");
        }
        ownerTenantDomain = carbonContext.getTenantDomain();
        if (ownerTenantDomain == null) {
            throw new IllegalStateException("Tenant domain cannot be " + ownerTenantDomain);
        }
        ownerTenantId = carbonContext.getTenantId();
        if (ownerTenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new IllegalStateException("Tenant ID cannot be " + ownerTenantId);
        }

        touch();

        // init
        this.cachingProvider = cachingProvider;

        if (uri == null) {
            throw new NullPointerException("No CacheManager URI specified");
        }
        this.uri = uri;
        name = getURI().toString();

        if (classLoader == null) {
            throw new NullPointerException("No ClassLoader specified");
        }
        this.classLoaderReference = new WeakReference<ClassLoader>(classLoader);

        this.properties = properties == null ? new Properties() : new Properties(properties);

        isClosed = false;
    }

    public int getOwnerTenantId() {
        return ownerTenantId;
    }

    @Override
    public CachingProvider getCachingProvider() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();
        return cachingProvider;
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public ClassLoader getClassLoader() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();
        return classLoaderReference.get();
    }

    @Override
    public Properties getProperties() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();
        return properties;
    }

    @Override
    public <K, V, C extends Configuration<K, V>> Cache<K, V> createCache(String cacheName,
                                                                         C configuration)
            throws IllegalArgumentException {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();

        if (isClosed()) {
            throw new IllegalStateException();
        }

        if (cacheName == null) {
            throw new NullPointerException("cacheName must not be null");
        }

        if (configuration == null) {
            throw new NullPointerException("configuration must not be null");
        }

//		synchronized (caches) {
        CacheImpl<?, ?> cache = caches.get(cacheName);

        if (cache == null) {
            cache = new CacheImpl(cacheName, this, configuration);
            caches.put(cache.getName(), cache);

            return (Cache<K, V>) cache;
        } else {
            throw new CacheException("A cache named " + cacheName + " already exists.");
        }
//		}
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName, Class<K> keyType,
                                       Class<V> valueType) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();

        if (isClosed()) {
            throw new IllegalStateException();
        }

        if (keyType == null) {
            throw new NullPointerException("keyType can not be null");
        }

        if (valueType == null) {
            throw new NullPointerException("valueType can not be null");
        }

//		synchronized (caches) {
        CacheImpl<?, ?> cache = caches.get(cacheName);

        if (cache == null) {
            return null;
        } else {
            Configuration<?, ?> configuration = cache.getConfiguration(CompleteConfiguration.class);

            if (configuration.getKeyType() != null &&
                    configuration.getKeyType().equals(keyType)) {

                if (configuration.getValueType() != null &&
                        configuration.getValueType().equals(valueType)) {

                    return (Cache<K, V>) cache;
                } else {
                    throw new ClassCastException("Incompatible cache value types specified, expected " +
                            configuration.getValueType() + " but " + valueType + " was specified");
                }
            } else {
                throw new ClassCastException("Incompatible cache key types specified, expected " +
                        configuration.getKeyType() + " but " + keyType + " was specified");
            }
        }
//		}
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();

        if (isClosed()) {
            throw new IllegalStateException();
        }
//		synchronized (caches) {
        CacheImpl cache = caches.get(cacheName);

        if (cache == null) {
            cache = (CacheImpl) createCache(cacheName, new MutableConfiguration<Object, Object>().setExpiryPolicyFactory(
                    AccessedExpiryPolicy.factoryOf(Duration.ONE_MINUTE)).setExpiryPolicyFactory(
                    CreatedExpiryPolicy.factoryOf(Duration.ONE_MINUTE)).setExpiryPolicyFactory(
                    ModifiedExpiryPolicy.factoryOf(Duration.ONE_MINUTE)));
            return cache;
        } else {
            Configuration configuration = cache.getConfiguration(CompleteConfiguration.class);

            if (configuration.getKeyType().equals(Object.class) &&
                    configuration.getValueType().equals(Object.class)) {
                return cache;
            } else {
                throw new IllegalArgumentException("Cache " + cacheName + " was " +
                        "defined with specific types Cache<" +
                        configuration.getKeyType() + ", " + configuration.getValueType() + "> " +
                        "in which case CacheManager.getCache(String, Class, Class) must be used");
            }

        }
//		}
    }

    @Override
    public Iterable<String> getCacheNames() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();
        HashSet<String> set = new HashSet<>();
        for (Cache<?, ?> cache : caches.values()) {
            set.add(cache.getName());
        }
        return Collections.unmodifiableSet(set);
    }

    @Override
    public void destroyCache(String cacheName) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();

        if (isClosed()) {
            throw new IllegalStateException();
        }
        if (cacheName == null) {
            throw new NullPointerException();
        }

        Cache<?, ?> cache;
        synchronized (caches) {
            cache = caches.get(cacheName);
        }

        if (cache != null) {
            cache.close();
        }
    }

    @Override
    public void enableManagement(String cacheName, boolean enabled) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();
        if (isClosed()) {
            throw new IllegalStateException();
        }
        if (cacheName == null) {
            throw new NullPointerException();
        }
        ((CacheImpl) caches.get(cacheName)).setManagementEnabled(enabled);
    }

    @Override
    public void enableStatistics(String cacheName, boolean enabled) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        touch();
        if (isClosed()) {
            throw new IllegalStateException();
        }
        if (cacheName == null) {
            throw new NullPointerException();
        }
        ((CacheImpl) caches.get(cacheName)).setStatisticsEnabled(enabled);
    }

    @Override
    public void close() {
        cachingProvider.releaseCacheManager(getURI(), getClassLoader());

        isClosed = true;

        ArrayList<Cache<?, ?>> cacheList;
        cacheList = new ArrayList<Cache<?, ?>>(caches.values());
        caches.clear();
        for (Cache<?, ?> cache : cacheList) {
            try {
                cache.close();
            } catch (Exception e) {
                log.info("Error stopping cache: " + cache, e);
            }
        }
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CarbonCacheManager that = (CarbonCacheManager) o;

        if (ownerTenantId != that.ownerTenantId) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (ownerTenantDomain != null ? !ownerTenantDomain.equals(that.ownerTenantDomain) : that.ownerTenantDomain != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (ownerTenantDomain != null ? ownerTenantDomain.hashCode() : 0);
        result = 31 * result + ownerTenantId;
        return result;
    }

    private void touch() {
        lastAccessed = System.currentTimeMillis();
    }

    void switchToDistributedMode() {
        for (Cache<?, ?> cache : caches.values()) {
            ((CacheImpl) cache).switchToDistributedMode();
        }
    }
}
