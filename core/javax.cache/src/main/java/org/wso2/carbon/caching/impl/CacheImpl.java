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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.eviction.EvictionAlgorithm;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Cache;
import javax.cache.CacheConfiguration;
import javax.cache.CacheLoader;
import javax.cache.CacheManager;
import javax.cache.CacheStatistics;
import javax.cache.Status;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryExpiredListener;
import javax.cache.event.CacheEntryListener;
import javax.cache.event.CacheEntryReadListener;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;
import javax.cache.mbeans.CacheMXBean;
import javax.cache.transaction.IsolationLevel;
import javax.cache.transaction.Mode;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * TODO: class description
 * <p/>
 * TODO: Cache statistics
 */
@SuppressWarnings("unchecked")
public class CacheImpl<K, V> implements Cache<K, V> {
    private static final Log log = LogFactory.getLog(CacheImpl.class);
    private static final int TIME_STAMP_REPLICATOR_THREADS = 2;
    private static final int TIME_STAMP_REPLICATING_INTERVAL = 50;
    private static final long MAX_CLEANUP_TIME = 60000;
    private int timeStampReplicatorCount;
    private static final int CACHE_LOADER_THREADS = 2;
    private static final long DEFAULT_CACHE_EXPIRY_MINS = 15;
    private static final long DEFAULT_CACHE_EXPIRY_MILLIS = DEFAULT_CACHE_EXPIRY_MINS * 60 * 1000;
    private static final float CACHE_OVERCAPACITY_FACTOR = 0.75f;
    private static final float CACHE_EVICTION_FACTOR = 0.25f;

    private String cacheName;
    private CacheManager cacheManager;
    private boolean isLocalCache;
    private Map<K, CacheEntry<K, V>> distributedCache;
    private Map<K, Long> distributedTimestampMap;
    private Map<K, Long> localTimestampMap;
    private long capacity = CachingConstants.DEFAULT_CACHE_CAPACITY;
    private final Map<K, CacheEntry<K, V>> localCache = new ConcurrentHashMap<K, CacheEntry<K, V>>((int)capacity, 0.75f, 50);
    private CacheConfiguration<K, V> cacheConfiguration;

    private List<CacheEntryListener> cacheEntryListeners = new ArrayList<CacheEntryListener>();
    private Status status;
    private CacheStatisticsImpl cacheStatistics;
    private ObjectName cacheMXBeanObjName;
    private final ExecutorService cacheLoadExecService = Executors.newFixedThreadPool(CACHE_LOADER_THREADS);

    private String ownerTenantDomain;
    private int ownerTenantId;
    private long lastAccessed = System.currentTimeMillis();

    private EvictionAlgorithm evictionAlgorithm = CachingConstants.DEFAULT_EVICTION_ALGORITHM;

    public CacheImpl(String cacheName, CacheManager cacheManager) {
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
        this.cacheName = cacheName;
        this.cacheManager = cacheManager;
        DistributedMapProvider distributedMapProvider =
                DataHolder.getInstance().getDistributedMapProvider();
        if (isLocalCache(cacheName, distributedMapProvider)) {
            if (log.isDebugEnabled()) {
                log.debug("Using local cache");
            }
            isLocalCache = true;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Using Hazelcast based distributed cache");
            }
            distributedCache = distributedMapProvider.getMap(getMapName(cacheName, cacheManager),
                    new MapEntryListenerImpl());
            distributedTimestampMap = distributedMapProvider.getMap(getMapName(CachingConstants.TIMESTAMP_CACHE_PREFIX +
                    cacheName, cacheManager), new TimestampMapEntryListenerImpl());
            initTimestampReplicator();
        }
        cacheStatistics = new CacheStatisticsImpl();
        registerMBean();
        CacheManagerFactoryImpl.addCacheForMonitoring(this);
        status = Status.STARTED;
    }

    private boolean isLocalCache(String cacheName, DistributedMapProvider distributedMapProvider) {
        return cacheName.startsWith(CachingConstants.LOCAL_CACHE_PREFIX) || distributedMapProvider == null;
    }

    void switchToDistributedMode() {
        DistributedMapProvider distributedMapProvider =
                DataHolder.getInstance().getDistributedMapProvider();
        if (isLocalCache(cacheName, distributedMapProvider)) {
            return;
        }
        distributedCache = distributedMapProvider.getMap(getMapName(cacheName, cacheManager),
                new MapEntryListenerImpl());
        distributedTimestampMap = distributedMapProvider.getMap(getMapName(CachingConstants.TIMESTAMP_CACHE_PREFIX
                + cacheName, cacheManager),new TimestampMapEntryListenerImpl());

        isLocalCache = false;

        initTimestampReplicator();
        // copy cache entries from localCache to distributed cache
        for (Map.Entry<K, CacheEntry<K, V>> entry : localCache.entrySet()) {
            distributedCache.put(entry.getKey(), entry.getValue());
        }
    }

    private String getMapName(String cacheName, CacheManager cacheManager) {
        return "$cache.$domain[" + ownerTenantDomain + "]" +
                cacheManager.getName() + "#" + cacheName;
    }

    private void initTimestampReplicator(){

        ScheduledExecutorService timeStampReplicator = Executors.newScheduledThreadPool(TIME_STAMP_REPLICATOR_THREADS, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                Thread th = new Thread(runnable);
                th.setName("TimeStampReplicator (" + ownerTenantDomain + ")-" + cacheName + "-" + timeStampReplicatorCount++);
                return th;
            }
        });
        localTimestampMap = new ConcurrentHashMap<K, Long>();
        String replicateFrequency = System.getProperty("timestamp.replication.frequency");
        int frequency = TIME_STAMP_REPLICATING_INTERVAL;
        if (replicateFrequency != null) {
            frequency = Integer.parseInt(replicateFrequency);
        }
        log.debug("Timestamp Replication Frequency set to " + frequency);
        timeStampReplicator.scheduleAtFixedRate(new TimestampReplicateTask(), frequency, frequency,
                TimeUnit.MILLISECONDS);
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

    private void registerMBean() {
        String serverPackage = "org.wso2.carbon";
        try {
            String objectName = serverPackage + ":type=Cache,tenant=" + ownerTenantDomain +
                    ",manager=" + cacheManager.getName() + ",name=" + cacheName;
            MBeanServer mserver = getMBeanServer();
            cacheMXBeanObjName = new ObjectName(objectName);
            Set set = mserver.queryNames(new ObjectName(objectName), null);
            if (set.isEmpty()) {
                CacheMXBeanImpl cacheMXBean =
                        new CacheMXBeanImpl(this, ownerTenantDomain, ownerTenantId);
                mserver.registerMBean(cacheMXBean, cacheMXBeanObjName);
            }
        } catch (Exception e) {
            String msg = "Could not register CacheMXBeanImpl MBean";
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        CacheEntry entry = localCache.get(key);
        V value = null;
        if (entry != null) {
            value = (V) entry.getValue();
            if (!isLocalCache) {
               // distributedTimestampMap.put(key, lastAccessed);
                synchronized (key){
                    localTimestampMap.put(key,lastAccessed);
                }
            }
            notifyCacheEntryRead(key, value);
        } else if(!isLocalCache) {    // Try reading it from the distributed cache
            entry = distributedCache.get(key);
            if(entry != null){
                entry.setLastAccessed(lastAccessed);
                localCache.put(key, entry);
                value = (V) entry.getValue();
                //distributedTimestampMap.put(key, lastAccessed);
                synchronized (key){
                    localTimestampMap.put(key,lastAccessed);
                }
                notifyCacheEntryRead(key, value);
            }
        }
        return value;
    }

    @Override
    public Map<K, V> getAll(Set<? extends K> keys) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> source = localCache;
        Map<K, V> destination = new HashMap<K, V>(keys.size());
        for (K key : keys) {
            destination.put(key, source.get(key).getValue());
        }
        return destination;
    }

    /**
     * @deprecated This method is highly inefficient. Do not use.
     */
    public void syncCaches() {
        if(!isLocalCache){
            for(Map.Entry<K, CacheEntry<K, V>> entry : distributedCache.entrySet()){
                K key = entry.getKey();
                CacheEntry<K, V> value = entry.getValue();
                if(!localCache.containsKey(key) ||
                        value.getLastModified() > localCache.get(key).getLastModified()){
                    localCache.put(key, value);
                    distributedTimestampMap.put(key,value.getLastAccessed());
                }
            }
        }
    }

    public Collection<CacheEntry<K, V>> getAll() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        return localCache.values();
    }

    @Override
    public boolean containsKey(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        boolean containsKey = localCache.containsKey(key);
        if(!containsKey && !isLocalCache){
            containsKey = distributedCache.containsKey(key);
            if(containsKey){
                CacheEntry<K, V> value = distributedCache.get(key);
                if (value != null) {
                    if (distributedTimestampMap.containsKey(key)) {
                        Long distributedLastAccessed = distributedTimestampMap.get(key);
                        setLastAccessed(value, distributedLastAccessed);
                    }
                    localCache.put(key, value);
                } else {
                    if (distributedCache.containsKey(key)) {
//                        log.warn("Cache value is null but key [" + key + "] is available!");
                    }
                    containsKey = false;
                }
            }
        }
        return containsKey;
    }

    private void setLastAccessed(CacheEntry<K, V> value, Long distributedLastAccessed) {
        if (distributedLastAccessed != null && distributedLastAccessed > value.getLastAccessed()) {
            value.setLastAccessed(distributedLastAccessed);
        } else {
            value.setLastAccessed(System.currentTimeMillis());
        }
    }

    @Override
    public Future<V> load(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        CacheLoader<K, ? extends V> cacheLoader = cacheConfiguration.getCacheLoader();
        if (cacheLoader == null) {
            return null;
        }
        if (containsKey(key)) {
            return null;
        }
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        FutureTask<V> task = new FutureTask<V>(new CacheLoaderLoadCallable<K, V>(this, cacheLoader, key,
                carbonContext.getTenantDomain(),
                carbonContext.getTenantId()));
        cacheLoadExecService.submit(task);
        return task;
    }

    private void checkStatusStarted() {
        if (!status.equals(Status.STARTED)) {
            throw new IllegalStateException("The cache status is not STARTED");
        }
    }

    @Override
    public Future<Map<K, ? extends V>> loadAll(final Set<? extends K> keys) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        if (keys == null) {
            throw new NullPointerException("keys");
        }
        CacheLoader<K, ? extends V> cacheLoader = cacheConfiguration.getCacheLoader();
        if (cacheLoader == null) {
            return null;
        }
        if (keys.contains(null)) {
            throw new NullPointerException("key");
        }
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        Callable<Map<K, ? extends V>> callable =
                new CacheLoaderLoadAllCallable<K, V>(this, cacheLoader, keys,
                        carbonContext.getTenantDomain(),
                        carbonContext.getTenantId());
        FutureTask<Map<K, ? extends V>> task = new FutureTask<Map<K, ? extends V>>(callable);
        cacheLoadExecService.submit(task);
        return task;
    }

    @Override
    public CacheStatistics getStatistics() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        return cacheStatistics;
    }

    private void internalPut(K key, V value) {
        // If the cache capacity has been exceeded by more than CACHE_OVERCAPACITY_FACTOR, do not put anymore until cache gets cleared
        if(localCache.size() >= capacity * (1 + CACHE_OVERCAPACITY_FACTOR)){
            return;
        }
        this.localCache.put(key, new CacheEntry(key, value));
        if (!isLocalCache) {
            this.distributedCache.put(key, new CacheEntry(key, value));
        }
    }

    @Override
    public void put(K key, V value) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        CacheEntry entry = localCache.get(key);
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

    private void notifyCacheEntryCreated(K key, V value) {
        CacheEntryEvent event = createCacheEntryEvent(key, value);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryCreatedListener) {
                ((CacheEntryCreatedListener) cacheEntryListener).entryCreated(event);
            }
        }
    }

    private void notifyCacheEntryUpdated(K key, V value) {
        CacheEntryEvent event = createCacheEntryEvent(key, value);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryUpdatedListener) {
                ((CacheEntryUpdatedListener) cacheEntryListener).entryUpdated(event);
            }
        }
    }

    private void notifyCacheEntryRead(K key, V value) {
        CacheEntryEvent event = createCacheEntryEvent(key, value);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryReadListener) {
                ((CacheEntryReadListener) cacheEntryListener).entryRead(event);
            }
        }
    }

    private void notifyCacheEntryRemoved(K key, V value) {
        CacheEntryEvent event = createCacheEntryEvent(key, value);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryRemovedListener) {
                ((CacheEntryRemovedListener) cacheEntryListener).entryRemoved(event);
            }
        }
    }

    private void notifyCacheEntryExpired(K key, V value) {
        CacheEntryEvent event = createCacheEntryEvent(key, value);
        for (CacheEntryListener cacheEntryListener : cacheEntryListeners) {
            if (cacheEntryListener instanceof CacheEntryExpiredListener) {
                ((CacheEntryExpiredListener) cacheEntryListener).entryExpired(event);
            }
        }
    }

    private CacheEntryEvent createCacheEntryEvent(K key, V value) {
        CacheEntryEventImpl event = new CacheEntryEventImpl(this);
        event.setKey(key);
        event.setValue(value);
        return event;
    }

    @Override
    public V getAndPut(K key, V value) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        V oldValue = localCache.get(key).getValue();
        put(key, value);
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
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
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        if (!localCache.containsKey(key)) {
            internalPut(key, value);
            notifyCacheEntryCreated(key, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        CacheEntry entry = localCache.remove((K) key);
        if (!isLocalCache) {
            distributedCache.remove(key);
            distributedTimestampMap.remove(key);
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
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        CacheEntry<K, V> cacheEntry = localCache.remove(key);
        if (!isLocalCache) {
            distributedCache.remove(key);
            distributedTimestampMap.remove(key);
            localTimestampMap.remove(key);
        }
        notifyCacheEntryRemoved(key, oldValue);
        return localCache.get(key) == null;
    }

    @Override
    public V getAndRemove(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        CacheEntry entry = localCache.remove(key);
        if (!isLocalCache) {
            distributedCache.remove(key);
            distributedTimestampMap.remove(key);
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
        checkStatusStarted();
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
        checkStatusStarted();
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
        checkStatusStarted();
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
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> map = localCache;
        for (K key : keys) {
            CacheEntry entry = map.remove(key);
            if(!isLocalCache){
                distributedCache.remove(key);
                distributedTimestampMap.remove(key);
            }
            notifyCacheEntryRemoved(key, (V) entry.getValue());
        }
    }

    @Override
    public void removeAll() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        Map<K, CacheEntry<K, V>> map = localCache;
        for (Map.Entry<K, CacheEntry<K, V>> entry : map.entrySet()) {
            notifyCacheEntryRemoved(entry.getKey(), entry.getValue().getValue());
        }
        map.clear();
        if(!isLocalCache){
            distributedCache.clear();
            distributedTimestampMap.clear();
        }
        //TODO: Notify value removed
    }

    @Override
    public CacheConfiguration<K, V> getConfiguration() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        if (cacheConfiguration == null) {
            cacheConfiguration = getDefaultCacheConfiguration();
        }
        return cacheConfiguration;
    }

    private CacheConfiguration<K, V> getDefaultCacheConfiguration() {
        return new CacheConfigurationImpl(true, true, true, true, IsolationLevel.NONE, Mode.NONE,
                new CacheConfiguration.Duration[]{new CacheConfiguration.Duration(TimeUnit.MINUTES,
                        DEFAULT_CACHE_EXPIRY_MINS),
                        new CacheConfiguration.Duration(TimeUnit.MINUTES,
                                DEFAULT_CACHE_EXPIRY_MINS)});
    }

    @Override
    public boolean registerCacheEntryListener(CacheEntryListener<? super K, ? super V> cacheEntryListener) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        return cacheEntryListeners.add(cacheEntryListener);
    }

    @Override
    public boolean unregisterCacheEntryListener(CacheEntryListener<?, ?> cacheEntryListener) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        return cacheEntryListeners.remove(cacheEntryListener);
    }

    @Override
    public Object invokeEntryProcessor(K key, EntryProcessor<K, V> entryProcessor) {
//        V v = getMap().get(key);
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
        });  //TODO change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getName() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        return this.cacheName;
    }

    @Override
    public CacheManager getCacheManager() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        return cacheManager;
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
    public Iterator<K> keys() {
        return localCache.keySet().iterator();
    }

    @Override
    public Iterator<Entry<K, V>> iterator() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        return new CacheEntryIterator<K, V>(localCache.values().iterator());
    }

    @Override
    public CacheMXBean getMBean() {
        throw new UnsupportedOperationException("getMBean is an ambiguous method which is not supported");
    }

    @Override
    public void start() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        lastAccessed = System.currentTimeMillis();
        if (status == Status.STARTED) {
            throw new IllegalStateException();
        }
        status = Status.STARTED;
    }

    @Override
    public void stop() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        lastAccessed = System.currentTimeMillis();
        localCache.clear();

        if (!isLocalCache) {
            distributedCache.clear();
            distributedTimestampMap.clear();
        }

        // Unregister the cacheMXBean MBean
        MBeanServer mserver = getMBeanServer();
        try {
            mserver.unregisterMBean(cacheMXBeanObjName);
        } catch (InstanceNotFoundException ignored) {
        } catch (MBeanRegistrationException e) {
            log.error("Cannot unregister CacheMXBean", e);
        }
        status = Status.STOPPED;
        cacheManager.removeCache(cacheName);
    }

    @Override
    public Status getStatus() {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        return status;
    }

    public void expire(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        CacheEntry entry = localCache.remove(key);
        if(!isLocalCache){
            try {
                distributedCache.remove(key);
                distributedTimestampMap.remove(key);
                localTimestampMap.remove(key);
            } catch (Exception e) {
                log.warn("Exception occurred while expiring item from distributed cache. " + e.getMessage());
            }
        }
        if (isIdle()) {
            cacheManager.removeCache(cacheName);
        }
        if (entry != null) {
            notifyCacheEntryExpired(key, (V) entry.getValue());
        }
    }

    public void evict(K key) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        checkStatusStarted();
        localCache.remove(key);
        /*if (log.isDebugEnabled()) {
            log.debug("Evicted entry:" + key + ", from local cache:" + cacheName);
        }*/
        if(!isLocalCache){
            try {
                distributedCache.remove(key);
                distributedTimestampMap.remove(key);
                localTimestampMap.remove(key);

                /*if (log.isDebugEnabled()) {
                    log.debug("Evicted entry:" + key + ", from distributed cache:" + cacheName);
                }*/
            } catch (Exception e) {
                log.warn("Exception occurred while evicting item from distributed cache. " + e.getMessage());
            }
        }
    }

    public void setCacheConfiguration(CacheConfigurationImpl cacheConfiguration) {
        Util.checkAccess(ownerTenantDomain, ownerTenantId);
        this.cacheConfiguration = cacheConfiguration;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public void setEvictionAlgorithm(EvictionAlgorithm evictionAlgorithm) {
        this.evictionAlgorithm = evictionAlgorithm;
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

    private boolean isIdle() {
        long timeDiff = System.currentTimeMillis() - lastAccessed;
        return localCache.isEmpty() && (timeDiff >= CachingConstants.MAX_CACHE_IDLE_TIME_MILLIS);
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

    @Override
    public int hashCode() {
        int result = cacheName != null ? cacheName.hashCode() : 0;
        result = 31 * result + (cacheManager != null ? cacheManager.hashCode() : 0);
        result = 31 * result + (ownerTenantDomain != null ? ownerTenantDomain.hashCode() : 0);
        result = 31 * result + ownerTenantId;
        return result;
    }

    @SuppressWarnings("unchecked")
    void runCacheExpiry() {
        CacheConfiguration cacheConfiguration = getConfiguration();

        CacheConfiguration.Duration modifiedExpiry =
                cacheConfiguration.getExpiry(CacheConfiguration.ExpiryType.MODIFIED);
        long modifiedExpiryDuration =
                modifiedExpiry == null ?
                        DEFAULT_CACHE_EXPIRY_MILLIS :
                        modifiedExpiry.getTimeUnit().toMillis(modifiedExpiry.getDurationAmount());

        CacheConfiguration.Duration accessedExpiry =
                cacheConfiguration.getExpiry(CacheConfiguration.ExpiryType.ACCESSED);
        long accessedExpiryDuration =
                accessedExpiry == null ?
                        DEFAULT_CACHE_EXPIRY_MILLIS :
                        accessedExpiry.getTimeUnit().toMillis(accessedExpiry.getDurationAmount());

        Collection<CacheEntry<K, V>> cacheEntries = getAll();

        long evictionListSize = 0;
        if (localCache.size() > capacity) {
            evictionListSize = localCache.size() - capacity; // Evict all extra entries
            evictionListSize += (long) (capacity * CachingConstants.CACHE_EVICTION_FACTOR); // Evict 25% of cache
        }

        TreeSet<CacheEntry> evictionList = new TreeSet<CacheEntry>(new Comparator<CacheEntry>() {

            @Override
            /**
             * Compares its two arguments for order.  Returns a negative integer,
             * zero, or a positive integer as the first argument is less than, equal
             * to, or greater than the second.
             */
            public int compare(CacheEntry o1, CacheEntry o2) {
                if(o1.getLastAccessed() == o2.getLastAccessed()) {
                    if(o1.getKey().equals(o2.getKey())){
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

            if (now - lastAccessed >= accessedExpiryDuration ||
                    now - lastModified >= modifiedExpiryDuration) {
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
    }

    /**
     * Callable used for cache loader.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
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
                entry = cacheLoader.load(key);
                cache.put(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.error("Could not load cache item with key " + key + " into cache " +
                        cache.getName() + " owned by tenant ", e);
                throw e;
            }
            return entry.getValue();
        }
    }

    private class TimestampReplicateTask implements Runnable{

        @Override
        public void run() {
           if(!isLocalCache){
               if(localTimestampMap != null && localTimestampMap.size() > 0){
                   Iterator<Map.Entry<K,Long>> iterator = localTimestampMap.entrySet().iterator();
                   while (iterator.hasNext()){
                       Map.Entry<K, Long> entry = iterator.next();
                       synchronized (entry.getKey()){
                       distributedTimestampMap.put(entry.getKey(),entry.getValue());
                       iterator.remove();
                       }
                   }
               }
           }
        }
    }

    /**
     * Callable used for cache loader.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
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

    private class MapEntryListenerImpl implements MapEntryListener{

        @Override
        public <X> void entryAdded(X key) {
            if (!localCache.containsKey(key)) return;
            CacheEntry<K, V> value = distributedCache.get(key);
            if (value != null) {
                if (distributedTimestampMap.containsKey(key)) {
                    Long distributedLastAccessed = distributedTimestampMap.get(key);
                    setLastAccessed(value, distributedLastAccessed);
                } else {
                    distributedTimestampMap.put((K) key, value.getLastAccessed());
                }
                localCache.put((K) key, value);
            }
        }

        @Override
        public <X> void entryRemoved(X key) {
            localCache.remove((K)key);
        }

        @Override
        public <X> void entryUpdated(X key) {
            if (!localCache.containsKey(key)) return;
            CacheEntry<K, V> value = distributedCache.get(key);
            if (value != null) {
                if (distributedTimestampMap.containsKey(key)) {
                    Long distributedLastAccessed = distributedTimestampMap.get(key);
                    setLastAccessed(value, distributedLastAccessed);
                }else{
                    distributedTimestampMap.put((K) key,value.getLastAccessed());
                }
                localCache.put((K)key, value);
            }
        }
    }

    private class TimestampMapEntryListenerImpl implements MapEntryListener{

        @Override
        public <X> void entryAdded(X key) {
            if (!localCache.containsKey(key)) return;
            CacheEntry<K, V> value = localCache.get(key);
            if (value != null) {
            	Long timeStamp = distributedTimestampMap.get(key);
            	if(timeStamp != null){
            		value.setLastAccessed(timeStamp);
            	} else {
            		value.setLastAccessed(new Date().getTime());
            	}
            }
        }

        @Override
        public <X> void entryRemoved(X key) {
        }

        @Override
        public <X> void entryUpdated(X key) {
            if (!localCache.containsKey(key)) return;
            CacheEntry<K, V> value = localCache.get(key);
            if (value != null) {
            	Long timeStamp = distributedTimestampMap.get(key);
            	if(timeStamp != null){
            		value.setLastAccessed(timeStamp);            		
            	} else {
            		value.setLastAccessed(new Date().getTime());
            	}
            }
        }
    }
}
