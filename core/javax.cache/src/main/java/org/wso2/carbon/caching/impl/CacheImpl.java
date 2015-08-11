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

import com.hazelcast.config.CacheConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.eviction.EvictionAlgorithm;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.*;
import javax.cache.event.*;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CompletionListener;
import javax.cache.management.CacheStatisticsMXBean;
import javax.cache.processor.EntryProcessor;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.EntryProcessorResult;
import javax.cache.processor.MutableEntry;
import javax.management.*;
import java.util.*;
import java.util.concurrent.*;

import static javax.cache.event.EventType.*;

/**
 * Cache implementation
 * ToDO need to implement Eviction algorythem
 */
public class CacheImpl<K, V> implements Cache<K, V> {
    private static final Log log = LogFactory.getLog(CacheImpl.class);
    private static final int CACHE_LOADER_THREADS = 2;
    private final ExecutorService cacheLoadExecService = Executors.newFixedThreadPool(CACHE_LOADER_THREADS);
    private static final long MAX_CLEANUP_TIME = 60000;
    private static final float CACHE_OVERCAPACITY_FACTOR = 0.75f;
    private final CacheMXBeanImpl cacheMXBean;
    private final CacheStatisticsMXBean statisticsMXBean;
    private final MutableConfiguration<K, V> configuration;
    private String cacheName;
    private CacheManager cacheManager;
    private CacheManager hazelcastCacheManager;
    private CacheLoader<K, V> cacheLoader;
    private Cache<K, CacheEntry<K, V>> distributedCache;
    private String ownerTenantDomain;
    private int ownerTenantId;
    private long lastAccessed = System.currentTimeMillis();
    private long capacity = CachingConstants.DEFAULT_CACHE_CAPACITY;
    private final Map<K, CacheEntry<K, V>> localCache = new ConcurrentHashMap<K, CacheEntry<K, V>>((int) capacity, 0.75f, 50);
    private boolean isLocalCache;
    private ObjectName cacheMXBeanObjName;
    private Map<K, Long> localTimestampMap = new ConcurrentHashMap<K, Long>();
    private volatile boolean isClosed;

    private List<CacheEntryListener> cacheEntryListeners = new ArrayList<CacheEntryListener>();
    private ConcurrentHashMap<Class<? extends CacheEntryListener>,
            ArrayList<CacheEntryEvent<K, V>>> eventMap = new ConcurrentHashMap<>();
    private EvictionAlgorithm evictionAlgorithm;


    public CacheImpl(String cacheName,
                     CacheManager cacheManager,
                     Configuration<K, V> configuration) {

        hazelcastCacheManager = DataHolder.getInstance().getHazelcastCacheManager();

        // setting up tenant id and domain
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        if (carbonContext == null) {
            throw new IllegalStateException("CarbonContext cannot be null");
        }
        ownerTenantDomain = carbonContext.getTenantDomain();
        if (ownerTenantDomain == null) {
            throw new IllegalStateException("Tenant domain cannot be null");
        }
        ownerTenantId = carbonContext.getTenantId();
        if (ownerTenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new IllegalStateException("Tenant ID cannot be " + ownerTenantId);
        }

        //setting cache manager, name and configuration
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;

        if (configuration instanceof CompleteConfiguration) {
            //support use of CompleteConfiguration
            this.configuration = new MutableConfiguration<K, V>((MutableConfiguration) configuration);
        } else {
            //support use of Basic Configuration
            MutableConfiguration<K, V> mutableConfiguration = new MutableConfiguration<K, V>();
            mutableConfiguration.setStoreByValue(configuration.isStoreByValue());
            mutableConfiguration.setTypes(configuration.getKeyType(), configuration.getValueType());
            mutableConfiguration.setExpiryPolicyFactory(AccessedExpiryPolicy.factoryOf(new Duration(
                    TimeUnit.MILLISECONDS, MAX_CLEANUP_TIME)));
            this.configuration = mutableConfiguration;
        }

        if (this.configuration.getCacheLoaderFactory() != null) {
            cacheLoader = (CacheLoader<K, V>) this.configuration.getCacheLoaderFactory().create();
        }

        // setting up local cache or distro cache attr
        if (isLocalCache(cacheName, hazelcastCacheManager)) {
            if (log.isDebugEnabled()) {
                log.debug("Using local cache");
            }
            isLocalCache = true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Using Hazelcast based distributed cache");
            }
            distributedCache = hazelcastCacheManager.getCache(Util.getDistributedCacheName(cacheName, ownerTenantDomain, cacheManager.getURI().toString()));
            if (distributedCache == null) {
                distributedCache = hazelcastCacheManager.createCache(
                        Util.getDistributedCacheName(cacheName, ownerTenantDomain,
                                cacheManager.getURI().toString()),
                        new CacheConfig<K, CacheEntry<K, V>>());

                DistributedCacheEntryListenerImpl<K, CacheEntry<K, V>> cacheEntryListener = new DistributedCacheEntryListenerImpl<K, CacheEntry<K, V>>(this);

                CacheEntryListenerConfiguration<K, CacheEntry<K, V>> listenerConfiguration =
                        new MutableCacheEntryListenerConfiguration(FactoryBuilder.factoryOf(cacheEntryListener), null, true, false);

                distributedCache.registerCacheEntryListener(listenerConfiguration);
            }
        }

        // mx bean setup
        cacheMXBean = new CacheMXBeanImpl<K, V>(this);
        statisticsMXBean = new CacheStatisticsMXBeanImpl();
        registerMBean();
        //TODO cache manager > cache factory monitoring

    }

    private void registerMBean() {
        String serverPackage = "org.wso2.carbon";
        try {
            String objectName = serverPackage + ":type=Cache,tenant=" + ownerTenantDomain +
                    ",manager=" + cacheManager.getURI().toString() + ",name=" + cacheName;
            MBeanServer mserver = getMBeanServer();
            cacheMXBeanObjName = new ObjectName(objectName);
            Set set = mserver.queryNames(new ObjectName(objectName), null);
            if (set.isEmpty()) {
                CacheMXBeanImpl cacheMXBean =
                        new CacheMXBeanImpl(this);
                mserver.registerMBean(cacheMXBean, cacheMXBeanObjName);
            }
        } catch (Exception e) {
            String msg = "Could not register CacheMXBeanImpl MBean";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    private MBeanServer getMBeanServer() {
        MBeanServer mserver;
        if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
            mserver = MBeanServerFactory.findMBeanServer(null).get(0);
        } else {
            mserver = MBeanServerFactory.createMBeanServer();
        }
        return mserver;
    }

    void switchToDistributedMode() {
        if (isLocalCache(cacheName, hazelcastCacheManager)) {
            return;
        }

        if (hazelcastCacheManager != null) {
            distributedCache = hazelcastCacheManager.getCache(Util.getDistributedCacheName(cacheName, ownerTenantDomain, cacheManager.getURI().toString()));
        }

        isLocalCache = false;

        // copy cache entries from localCache to distributed cache
        for (Map.Entry<K, CacheEntry<K, V>> entry : localCache.entrySet()) {
            distributedCache.put((K) entry.getKey(), (CacheEntry<K, V>) entry.getValue());
        }
    }

    //Distributed / hazelcast cache last access time
    private void setLastAccessed(CacheEntry<K, V> value, Long distributedLastAccessed) {
        if (distributedLastAccessed != null && distributedLastAccessed > value.getLastAccessed()) {
            value.setLastAccessed(distributedLastAccessed);
        } else {
            value.setLastAccessed(System.currentTimeMillis());
        }
    }


    private boolean isLocalCache(String cacheName, CacheManager hazelcastCacheManager) {
        return cacheName.startsWith(CachingConstants.LOCAL_CACHE_PREFIX) || hazelcastCacheManager == null;
    }


    private void internalPut(K key, V value) {
        // If the cache capacity has been exceeded by more than CACHE_OVERCAPACITY_FACTOR, do not put anymore until cache gets cleared
        if (localCache.size() >= capacity * (1 + CACHE_OVERCAPACITY_FACTOR)) {
            return;
        }
        this.localCache.put(key, new CacheEntry(key, value));
        if (!isLocalCache) {
            this.distributedCache.put(key, new CacheEntry(key, value));
        }
    }


    void notifyCacheEntryCreated(K key, V value) {
        List<CacheEntryEvent<K, V>> eventList = new ArrayList<>();
        eventList.add(new CacheEntryEventImpl<>(this, key, value, CREATED));
        eventMap.put(CacheEntryCreatedListener.class, (ArrayList<CacheEntryEvent<K, V>>) eventList);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryCreatedListener) {
                if (log.isDebugEnabled()) {
                    log.debug("Notification event trigger for cache entry create : " + cacheEntryListener.getClass());
                }
                ((CacheEntryCreatedListener) cacheEntryListener).onCreated(eventMap.get(CacheEntryCreatedListener.class));
            }
        }
    }

    void notifyCacheEntryUpdated(K key, V value) {
        List<CacheEntryEvent<K, V>> eventList = new ArrayList<>();
        eventList.add(new CacheEntryEventImpl<>(this, key, value, UPDATED));
        eventMap.put(CacheEntryUpdatedListener.class, (ArrayList<CacheEntryEvent<K, V>>) eventList);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryUpdatedListener) {
                if (log.isDebugEnabled()) {
                    log.debug("Notification event trigger for cache entry update : " + cacheEntryListener.getClass());
                }
                ((CacheEntryUpdatedListener) cacheEntryListener).onUpdated(eventMap.get(CacheEntryUpdatedListener.class));
            }
        }
    }

    void notifyCacheEntryRemoved(K key, V value) {
        List<CacheEntryEvent<K, V>> eventList = new ArrayList<>();
        eventList.add(new CacheEntryEventImpl<>(this, key, value, REMOVED));
        eventMap.put(CacheEntryRemovedListener.class, (ArrayList<CacheEntryEvent<K, V>>) eventList);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryRemovedListener) {
                if (log.isDebugEnabled()) {
                    log.debug("Notification event trigger for cache entry remove : " + cacheEntryListener.getClass());
                }
                ((CacheEntryRemovedListener) cacheEntryListener).onRemoved(eventMap.get(CacheEntryRemovedListener.class));
            }
        }
    }

    private void notifyCacheEntryExpired(K key, V value) {
        List<CacheEntryEvent<K, V>> eventList = new ArrayList<>();
        eventList.add(new CacheEntryEventImpl<>(this, key, value, EXPIRED));
        eventMap.put(CacheEntryExpiredListener.class, (ArrayList<CacheEntryEvent<K, V>>) eventList);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryExpiredListener) {
                if (log.isDebugEnabled()) {
                    log.debug("Notification event trigger for cache entry expire : " + cacheEntryListener.getClass());
                }
                ((CacheEntryExpiredListener) cacheEntryListener).onExpired(eventMap.get(cacheEntryListener.getClass()));
            }
        }
    }

    public void setManagementEnabled(boolean enabled) {
        configuration.setManagementEnabled(enabled);
    }

    public void setStatisticsEnabled(boolean enabled) {
        configuration.setStatisticsEnabled(enabled);
    }

    public CacheStatisticsMXBean getStatisticsMXBean() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        return statisticsMXBean;
    }

    @Override
    public V get(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        CacheEntry entry = (CacheEntry) localCache.get(key);
        V value = null;
        if (entry != null) {
            value = (V) entry.getValue();
            if (!isLocalCache) {
                localTimestampMap.put(key, lastAccessed);
            }
        } else if (!isLocalCache) {    // Try reading it from the distributed cache
            entry = distributedCache.get(key);
            if (entry != null) {
                entry.setLastAccessed(lastAccessed);
                localCache.put(key, entry);
                value = (V) entry.getValue();
                localTimestampMap.put(key, lastAccessed);
            }
        }
        return value;
    }


    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> source = localCache;
        Map<K, V> destination = new HashMap<K, V>(keys.size());
        for (K key : keys) {
            destination.put(key, ((CacheEntry<K, V>) source.get(key)).getValue());
        }
        return destination;
    }

    public Collection<CacheEntry<K, V>> getAll() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        return localCache.values();
    }

    @Override
    public boolean containsKey(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        boolean containsKey = localCache.containsKey(key);
        if (!containsKey && !isLocalCache) {
            containsKey = distributedCache.containsKey(key);
            if (containsKey) {
                CacheEntry<K, V> value = distributedCache.get(key);
                if (value != null) {
                    localCache.put(key, value);
                } else {
                    if (distributedCache.containsKey(key)) {
                        //log.warn("Cache value is null but key [" + key + "] is available!");
                    }
                    containsKey = false;
                }
            }
        }
        return containsKey;
    }

    @Override
    public void loadAll(final Set<? extends K> keys, final boolean replaceExistingValues,
                        final CompletionListener completionListener) {

        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        if (keys == null) {
            throw new NullPointerException("keys");
        }

        if (cacheLoader == null) {
            if (completionListener != null) {
                completionListener.onCompletion();
            }
        } else {
            for (K key : keys) {
                if (key == null) {
                    throw new NullPointerException("keys contains a null");
                }
            }

            cacheLoadExecService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<K> keysToLoad = new ArrayList<K>();
                        for (K key : keys) {
                            if (replaceExistingValues || !containsKey(key)) {
                                keysToLoad.add(key);
                            }
                        }

                        Map<? extends K, ? extends V> loaded;
                        try {
                            loaded = cacheLoader.loadAll(keysToLoad);
                        } catch (Exception e) {
                            if (!(e instanceof CacheLoaderException)) {
                                throw new CacheLoaderException("Exception in CacheLoader", e);
                            } else {
                                throw e;
                            }
                        }

                        for (K key : keysToLoad) {
                            if (loaded.get(key) == null) {
                                loaded.remove(key);
                            }
                        }

                        // TODO add new putAll overload method
                        //putAll(loaded, replaceExistingValues, false);

                        if (completionListener != null) {
                            completionListener.onCompletion();
                        }
                    } catch (Exception e) {
                        if (completionListener != null) {
                            completionListener.onException(e);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void put(K key, V value) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        CacheEntry entry = (CacheEntry) localCache.get(key);
        V oldValue = entry != null ? (V) entry.getValue() : null;
        if (oldValue == null) {
            internalPut(key, value);
            notifyCacheEntryCreated(key, value);
        } else {
            entry.setValue(value);
            internalPut(key, value);
            notifyCacheEntryUpdated(key, value);
        }
    }

    @Override
    public V getAndPut(K key, V value) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        V oldValue = localCache.get(key).getValue();
        put(key, value);
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            K key = entry.getKey();
            boolean entryExists = false;
            if (localCache.containsKey(key)) {
                entryExists = true;
            }
            V value = entry.getValue();
            internalPut(key, value);
            if (entryExists) {
                notifyCacheEntryUpdated(key, value);
            } else {
                notifyCacheEntryCreated(key, value);
            }
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        if (!localCache.containsKey(key)) {
            internalPut(key, value);
            notifyCacheEntryCreated(key, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        CacheEntry entry = localCache.remove((K) key);
        if (!isLocalCache) {
            distributedCache.remove(key);
            localTimestampMap.remove(key);
        }
        boolean removed = entry != null;
        if (removed) {
            notifyCacheEntryRemoved((K) key, (V) entry.getValue());
        }
        return localCache.get(key) == null;
    }

    @Override
    public boolean remove(K key, V oldValue) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        CacheEntry<K, V> cacheEntry = localCache.remove(key);
        if (!isLocalCache) {
            distributedCache.remove(key);
            localTimestampMap.remove(key);
        }
        notifyCacheEntryRemoved(key, oldValue);
        return localCache.get(key) == null;
    }

    @Override
    public V getAndRemove(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        CacheEntry entry = localCache.remove(key);
        if (!isLocalCache) {
            distributedCache.remove(key);
            localTimestampMap.remove(key);
        }
        if (entry != null) {
            V value = (V) entry.getValue();
            notifyCacheEntryRemoved(key, value);
            return value;
        }
        return null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> map = localCache;
        if (map.containsKey(key) && map.get(key).equals(new CacheEntry(key, oldValue))) {
            internalPut(key, newValue);
            notifyCacheEntryUpdated(key, newValue);
            return true;
        }
        return false;
    }

    @Override
    public boolean replace(K key, V value) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> map = localCache;
        if (map.containsKey(key)) {
            internalPut(key, value);
            notifyCacheEntryUpdated(key, value);
            return true;
        }
        return false;
    }

    @Override
    public V getAndReplace(K key, V value) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> map = localCache;
        CacheEntry<K, V> oldValue = map.get(key);
        if (oldValue != null) {
            internalPut(key, value);
            notifyCacheEntryUpdated(key, value);
            return oldValue.getValue();
        }
        return null;
    }

    @Override
    public void removeAll(Set<? extends K> keys) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> map = localCache;
        for (K key : keys) {
            CacheEntry entry = map.remove(key);
            if (!isLocalCache) {
                distributedCache.remove(key);
            }
            notifyCacheEntryRemoved(key, (V) entry.getValue());
        }
    }

    @Override
    public void removeAll() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> map = localCache;
        for (Map.Entry<K, CacheEntry<K, V>> entry : map.entrySet()) {
            notifyCacheEntryRemoved(entry.getKey(), entry.getValue().getValue());
        }
        map.clear();
        if (!isLocalCache) {
            distributedCache.clear();
        }
    }

    @Override
    public void clear() {
        //TODO need implement clear new clear method
    }

    @Override
    public <C extends Configuration<K, V>> C getConfiguration(Class<C> clazz) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        if (clazz.isInstance(configuration)) {
            return clazz.cast(configuration);
        }
        throw new IllegalArgumentException("The configuration class " + clazz +
                " is not supported by this implementation");
    }

    @Override
    public <T> T invoke(K key, EntryProcessor<K, V, T> entryProcessor,
                        Object... arguments) throws EntryProcessorException {
        lastAccessed = System.currentTimeMillis();
        return entryProcessor.process(new MutableEntry<K, V>() {
            @Override
            public boolean exists() {
                return false;  //TODO
            }

            @Override
            public void remove() {
                //TODO
            }

            @Override
            public void setValue(V value) {
                //TODO
            }

            @Override
            public K getKey() {
                return null;  //TODO
            }

            @Override
            public V getValue() {
                return null;  //TODO
            }

            @Override
            public <T> T unwrap(Class<T> clazz) {
                return null;
            }
        });  //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public <T> Map<K, EntryProcessorResult<T>> invokeAll(Set<? extends K> keys,
                                                         EntryProcessor<K, V, T> entryProcessor,
                                                         Object... arguments) {
        //TODO implement invoke all method
        return null;
    }

    @Override
    public String getName() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        return cacheName;
    }

    @Override
    public CacheManager getCacheManager() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        return cacheManager;
    }

    @Override
    public void close() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        localCache.clear();

        if (!isLocalCache) {
            distributedCache.clear();
        }

        // Unregister the cacheMXBean MBean
        MBeanServer mserver = getMBeanServer();
        try {
            mserver.unregisterMBean(cacheMXBeanObjName);
        } catch (InstanceNotFoundException ignored) {
        } catch (MBeanRegistrationException e) {
            log.error("Cannot unregister CacheMXBean", e);
        }
        cacheManager.destroyCache(cacheName);
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        if (cls.isAssignableFrom(this.getClass())) {
            return cls.cast(this);
        }

        throw new IllegalArgumentException("Unwrapping to " + cls +
                " is not a supported by this implementation");
    }

    @Override
    public void registerCacheEntryListener(
            CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        cacheEntryListeners.add(cacheEntryListenerConfiguration.getCacheEntryListenerFactory().create());
    }

    @Override
    public void deregisterCacheEntryListener(
            CacheEntryListenerConfiguration<K, V> cacheEntryListenerConfiguration) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        cacheEntryListeners.remove(cacheEntryListenerConfiguration.getCacheEntryListenerFactory().create());
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        return new CacheEntryIterator<K, V>(localCache.values().iterator());
    }


    @SuppressWarnings("unchecked")
    void runCacheExpiry() {

        Duration modifiedExpiry =
                configuration.getExpiryPolicyFactory().create().getExpiryForUpdate();
        long modifiedExpiryDuration =
                modifiedExpiry == null ?
                        Util.getDefaultCacheTimeout() * 60 * 1000 :
                        modifiedExpiry.getTimeUnit().toMillis(modifiedExpiry.getDurationAmount());

        Duration accessedExpiry =
                configuration.getExpiryPolicyFactory().create().getExpiryForAccess();
        long accessedExpiryDuration =
                accessedExpiry == null ?
                        Util.getDefaultCacheTimeout() * 60 * 1000 :
                        accessedExpiry.getTimeUnit().toMillis(accessedExpiry.getDurationAmount());

        Collection<CacheEntry<K, V>> cacheEntries = getAll();

        long evictionListSize = 0;
        if (localCache.size() > capacity) {
            evictionListSize = localCache.size() - capacity; // Evict all extra entries
            evictionListSize += (long) (capacity * CachingConstants.CACHE_EVICTION_FACTOR); // Evict 25% of cache
        }

        TreeSet<CacheEntry> evictionList = new TreeSet<>(new Comparator<CacheEntry>() {

            @Override
            /**
             * Compares its two arguments for order.  Returns a negative integer,
             * zero, or a positive integer as the first argument is less than, equal
             * to, or greater than the second.
             */
            public int compare(CacheEntry o1, CacheEntry o2) {
                if (o1.getLastAccessed() == o2.getLastAccessed()) {
                    if (o1.getKey().equals(o2.getKey())) {
                        return 0;
                    }
                    return -1;
                } else {
                    return (int) (o1.getLastAccessed() - o2.getLastAccessed());
                }
            }
        });
        long start = System.currentTimeMillis();
        for (CacheEntry<K, V> localCacheEntry : cacheEntries) { // All Cache entries in a Cache
            K key = localCacheEntry.getKey();
            if (localCache.size() >= capacity) {
                evictionList.add(localCacheEntry);
            }

            long lastAccessed = localCacheEntry.getLastAccessed();
            long lastModified = localCacheEntry.getLastModified();
            long now = System.currentTimeMillis();

            if (now - lastAccessed >= accessedExpiryDuration || now - lastModified >= modifiedExpiryDuration) {
                expire(key);
                if (log.isDebugEnabled()) {
                    log.debug("Expired: Cache:" + cacheName + ", entry:" + key);
                }
                if (System.currentTimeMillis() - start > MAX_CLEANUP_TIME) {
                    break;
                }
            }
        }

        if (localCache.size() >= capacity) {
            start = System.currentTimeMillis();
            for (int i = 0; i < evictionListSize; i++) {
                CacheEntry entry = evictionAlgorithm.getEntryForEviction(evictionList);
                if (entry != null) {
                    this.evict((K) entry.getKey());
                }
                if (System.currentTimeMillis() - start > MAX_CLEANUP_TIME) {
                    break;
                }
            }
            log.info("Evicted " + evictionListSize + " entries from cache " + cacheName);
        }

        // Replicate timestamps
        if (!isLocalCache) {
            for (Map.Entry<K, Long> entry : localTimestampMap.entrySet()) {
                Long oldValue = entry.getValue();
                Long newValue = entry.getValue();
                if (newValue.equals(oldValue)) { // Remove only if the value has not changed
                    localTimestampMap.remove(entry.getKey());
                }
            }
        }
    }

    public void expire(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        CacheEntry entry = localCache.remove(key);
        if (!isLocalCache) {
            try {
                distributedCache.remove(key);
                localTimestampMap.remove(key);
            } catch (Exception e) {
                log.warn("Exception occurred while expiring item from distributed cache. " + e.getMessage());
            }
        }
        if (isIdle()) {
            cacheManager.destroyCache(cacheName);
        }
        if (entry != null) {
            notifyCacheEntryExpired(key, (V) entry.getValue());
        }
    }

    public void evict(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        localCache.remove(key);
        /*if (log.isDebugEnabled()) {
            log.debug("Evicted entry:" + key + ", from local cache:" + cacheName);
        }*/
        if (!isLocalCache) {
            try {
                distributedCache.remove(key);
                localTimestampMap.remove(key);

                /*if (log.isDebugEnabled()) {
                    log.debug("Evicted entry:" + key + ", from distributed cache:" + cacheName);
                }*/
            } catch (Exception e) {
                log.warn("Exception occurred while evicting item from distributed cache. " + e.getMessage());
            }
        }
    }

    private boolean isIdle() {
        long timeDiff = System.currentTimeMillis() - lastAccessed;
        return localCache.isEmpty() && (timeDiff >= CachingConstants.MAX_CACHE_IDLE_TIME_MILLIS);
    }

    public void setEvictionAlgorithm(EvictionAlgorithm evictionAlgorithm) {
        this.evictionAlgorithm = evictionAlgorithm;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    @Override
    public int hashCode() {
        int result = cacheName != null ? cacheName.hashCode() : 0;
        result = 31 * result + (cacheManager != null ? cacheManager.hashCode() : 0);
        result = 31 * result + (ownerTenantDomain != null ? ownerTenantDomain.hashCode() : 0);
        result = 31 * result + ownerTenantId;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheImpl cache = (CacheImpl) o;

        if (ownerTenantId != cache.ownerTenantId) return false;
        if (cacheManager != null ? !cacheManager.equals(cache.cacheManager) : cache.cacheManager != null)
            return false;
        if (cacheName != null ? !cacheName.equals(cache.cacheName) : cache.cacheName != null)
            return false;
        if (ownerTenantDomain != null ? !ownerTenantDomain.equals(cache.ownerTenantDomain) : cache.ownerTenantDomain != null)
            return false;
        return true;
    }

    // all custom listeners

    public Cache<K, CacheEntry<K, V>> getDistributedCache() {
        return distributedCache;
    }

    public Map<K, CacheEntry<K, V>> getLocalCache() {
        return localCache;
    }

    private static class CacheLoaderLoadCallable<K, V> implements Callable<V> {
        private final CacheImpl<K, V> cache;
        private final CacheLoader<K, ? extends V> cacheLoader;
        private final K key;
        private final String tenantDomain;
        private final int tenantId;

        CacheLoaderLoadCallable(CacheImpl<K, V> cache, CacheLoader<K, ? extends V> cacheLoader, K key,
                                String tenantDomain, int tenantId) {
            this.cache = cache;
            this.cacheLoader = cacheLoader;
            this.key = key;
            this.tenantDomain = tenantDomain;
            this.tenantId = tenantId;
        }

        @Override
        public V call() throws Exception {
            Entry<K, ? extends V> entry;
            try {
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                entry = (Entry<K, ? extends V>) cacheLoader.load(key);
                cache.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("Could not load cache item with key " + key + " into cache " +
                        cache.getName() + " owned by tenant ", e);
                throw e;
            }
            return entry.getValue();
        }
    }

    private static class CacheLoaderLoadAllCallable<K, V> implements Callable<Map<K, ? extends V>> {
        private final CacheImpl<K, V> cache;
        private final CacheLoader<K, ? extends V> cacheLoader;
        private final Collection<? extends K> keys;
        private final String tenantDomain;
        private final int tenantId;

        CacheLoaderLoadAllCallable(CacheImpl<K, V> cache,
                                   CacheLoader<K, ? extends V> cacheLoader,
                                   Collection<? extends K> keys,
                                   String tenantDomain, int tenantId) {
            this.cache = cache;
            this.cacheLoader = cacheLoader;
            this.keys = keys;
            this.tenantDomain = tenantDomain;
            this.tenantId = tenantId;
        }

        @Override
        public Map<K, ? extends V> call() throws Exception {
            Map<K, ? extends V> value;
            try {
                PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                ArrayList<K> keysNotInStore = new ArrayList<K>();
                for (K key : keys) {
                    if (!cache.containsKey(key)) {
                        keysNotInStore.add(key);
                    }
                }
                value = cacheLoader.loadAll(keysNotInStore);
                cache.putAll(value);
            } catch (Exception e) {
                log.error("Could not load all cache items into cache " + cache.getName() + " owned by tenant ", e);
                throw e;
            }
            return value;
        }
    }

    private static final class CacheEntryIterator<K, V> implements Iterator<Entry<K, V>> {
        private Iterator<CacheEntry<K, V>> iterator;

        public CacheEntryIterator(Iterator<CacheEntry<K, V>> iterator) {
            this.iterator = iterator;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Entry<K, V> next() {
            return iterator.next();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void remove() {
            iterator.remove();
        }
    }
}

