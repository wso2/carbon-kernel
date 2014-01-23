/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.caching.infinispan;

import net.sf.jsr107cache.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.AdvancedCache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.global.TransportConfigurationBuilder;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.interceptors.CacheMgmtInterceptor;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.stats.Stats;
import org.wso2.carbon.base.CarbonBaseUtils;
import org.wso2.carbon.caching.core.CacheConfiguration;
import org.wso2.carbon.caching.core.CarbonCacheManager;

import java.io.IOException;
import java.net.CacheResponse;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache manager based on <a href="http://jboss.org/infinispan">Infinispan</a> from JBoss.
 */
@SuppressWarnings("unused")
public class InfinispanCacheManager extends CacheManager implements CarbonCacheManager {
    private EmbeddedCacheManager cacheManager;
    private EmbeddedCacheManager localCacheManager;
    private static final String DEFAULT_CLUSTER_NAME = "wso2carbon-cache";
    private static final String CARBON_DEFAULT_CACHE = "carbon_cache";
    
	private ConfigurationBuilder configBuilder = null;
	private ConfigurationBuilder localConfigBuilder = null;
    private  CacheMode cacheMode;
    
    private final Map<String, Cache> caches = Collections.synchronizedMap(new HashMap<String, Cache>());

    private static final Log log = LogFactory.getLog(InfinispanCacheManager.class);

    /**
     * {@inheritDoc}
     */
    public void initialize(String carbonHome) {
        CarbonBaseUtils.checkSecurity();
        log.debug("Starting Cache Manager initialization");

        String clusterName = DEFAULT_CLUSTER_NAME;
        cacheMode = CacheMode.LOCAL;
        boolean isSync = true;
        long maxExpirationMillis = CacheConfiguration.DEFAULT_EXPIRATION;
        long maxIdleExpirationMillis = CacheConfiguration.DEFAULT_EXPIRATION;
        long l1LifeSpan = CacheConfiguration.DEFAULT_EXPIRATION;
        boolean l1Enabled = true;
        String configurationFile = null;
        boolean isClusteringEnabled = false;
        int maxEntries = -1;

        CacheConfiguration cacheConfiguration = CacheConfiguration.getInstance();
        String cacheModeString = cacheConfiguration.getProperty("configuration.cacheMode") == null ? CacheConfiguration.CACHE_MODE_LOCAL :
                cacheConfiguration.getProperty("configuration.cacheMode");
        maxExpirationMillis = cacheConfiguration.getProperty("maxExpirationMillis") == null ? CacheConfiguration.DEFAULT_EXPIRATION :
                 Long.parseLong(cacheConfiguration.
                        getProperty("maxExpirationMillis"));
        maxIdleExpirationMillis = cacheConfiguration.
                 getProperty("maxIdleExpirationMillis") == null ? CacheConfiguration.DEFAULT_EXPIRATION :
                 Long.parseLong(cacheConfiguration.
                        getProperty("maxIdleExpirationMillis"));
        
        maxEntries = cacheConfiguration.getProperty("maxEntries") == null ? -1 :
                 Integer.parseInt(cacheConfiguration.getProperty("maxEntries"));

        GlobalConfigurationBuilder builder = new GlobalConfigurationBuilder();
        localCacheManager = new DefaultCacheManager(builder.nonClusteredDefault().build());
        Configuration dcc = localCacheManager.getDefaultCacheConfiguration();
        localConfigBuilder = new ConfigurationBuilder().read(dcc);
        localConfigBuilder.expiration().lifespan(maxExpirationMillis);
        localConfigBuilder.expiration().maxIdle(maxIdleExpirationMillis);
        if(localConfigBuilder.locking() != null){
            localConfigBuilder.locking().useLockStriping(false);
        }
        if (maxEntries != -1) {
            localConfigBuilder.eviction().maxEntries(maxEntries);
        }
        log.debug("Successfully Initialized Infinispan Cache Manager");
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultCacheName() {
        return DefaultCacheManager.DEFAULT_CACHE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public Cache getCache(String cacheName) {
		if (cacheName == null) {
			cacheName = DefaultCacheManager.DEFAULT_CACHE_NAME;
		}
		
        Cache cache = caches.get(cacheName);
        if (cache == null) {
            // We wrap each Infinispan Cache, so that it can be used via the
            // standard JSR107
            // JCache API.
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread()
                      .setContextClassLoader(InfinispanCacheManager.class.getClassLoader());
                localCacheManager.defineConfiguration(cacheName, localConfigBuilder.build());
                org.infinispan.Cache infinispanCache = localCacheManager.getCache(cacheName);
                cache = new InfinispanJCacheWrapper(infinispanCache);
                registerCache(cacheName, cache);
            } finally {
                Thread.currentThread().setContextClassLoader(tccl);
            }
        }
        return cache;
    }
    
    /**
     * {@inheritDoc}
     */
    public void registerCache(String cacheName, Cache cache) {
        if (cache == null) {
            Cache removedCache = caches.remove(cacheName);
            // Removing the cache will also result in the stopping of the cache
            if (removedCache != null && removedCache instanceof InfinispanJCacheWrapper) {
                org.infinispan.Cache original =
                        ((InfinispanJCacheWrapper) removedCache).getOriginal();
                if (original.getStatus() == ComponentStatus.RUNNING) {
                    original.stop();
                }
            }
        } else {
            // We register a cache only if there is no cache already registered.
            if (caches.get(cacheName) == null) {
                caches.put(cacheName, cache);
            }
        }
    }
    
	
    /**
     * {@inheritDoc}
     */
    public CacheFactory getCacheFactory() throws CacheException {
        throw new CacheException("The InfinispanCacheManager does not provide an " +
                "in-built nor look-up a CacheFactory.");
    }
    
    ////////////////////////////////////////////////////////
    // JSR107 JCache wrapper for Infinispan
    ////////////////////////////////////////////////////////

    private static class InfinispanJCacheEntry implements CacheEntry {

        private InternalCacheEntry cacheEntry;

        private InfinispanJCacheEntry(InternalCacheEntry cacheEntry) {
            this.cacheEntry = cacheEntry;
        }

        public int getHits() {
            throw new UnsupportedOperationException("Infinispan does not allow to find the" +
                    " number of hits on a per-entry basis.");
        }

        public long getLastAccessTime() {
            if (cacheEntry == null) {
                return 0;
            }
            return cacheEntry.getLastUsed();
        }

        public long getLastUpdateTime() {
            if (cacheEntry == null) {
                return 0;
            }
            return cacheEntry.getLastUsed();
        }

        public long getCreationTime() {
            if (cacheEntry == null) {
                return 0;
            }
            return cacheEntry.getCreated();
        }

        public long getExpirationTime() {
            if (cacheEntry == null) {
                return 0;
            }
            return cacheEntry.getExpiryTime();
        }

        public long getVersion() {
            // We use the time at which the cache entry was created as the version of the entry.
            return cacheEntry.getCreated();
        }

        public boolean isValid() {
            return cacheEntry.isValid();
        }

        public long getCost() {
            // This implementation does not have a notion of cost. Accordingly, 0 is always
            // returned.
            return 0;
        }

        public Object getKey() {
            return cacheEntry != null ? cacheEntry.getKey() : null;
        }

        public Object getValue() {
            return cacheEntry != null ? cacheEntry.getValue() : null;
        }

        public Object setValue(Object o) {
            return cacheEntry != null ? cacheEntry.setValue(o) : null;
        }
    }

    private static class InfinispanJCacheStatistics implements CacheStatistics {

        private Stats stats;
        private CacheMgmtInterceptor interceptor;

        private InfinispanJCacheStatistics(AdvancedCache cache) {
            this.stats = cache.getStats();
            this.interceptor = null;
            try {
                @SuppressWarnings("unchecked")
                List<CommandInterceptor> chain = (List<CommandInterceptor>) cache.getInterceptorChain();
                for (CommandInterceptor i : chain) {
                    if (i instanceof CacheMgmtInterceptor) {
                        interceptor = (CacheMgmtInterceptor)i;
                        break;
                    }
                }
            } catch (Exception ignored) {
                // We are not bothered if this fails.
            }
        }

        public int getStatisticsAccuracy() {
            throw new UnsupportedOperationException("This version of Infinispan does not provide " +
                    "details on accuracy of statistics.");
        }

        public int getObjectCount() {
            return stats.getCurrentNumberOfEntries();
        }

        public int getCacheHits() {
            return (int) stats.getHits();
        }

        public int getCacheMisses() {
            return (int) stats.getMisses();
        }

        public void clearStatistics() {
            if (interceptor != null) {
                interceptor.resetStatistics();
            }
        }
    }

    private static class InfinispanJCacheWrapper implements Cache {

        private org.infinispan.Cache cache;

        public InfinispanJCacheWrapper(org.infinispan.Cache cache) {
            if (cache.getStatus() == ComponentStatus.TERMINATED ||
                    cache.getStatus() ==  ComponentStatus.INSTANTIATED) {
                cache.start();
            } else if (cache.getStatus() == ComponentStatus.FAILED) {
                throw new RuntimeException("Failed to start the cache: " + cache.getName());
            }
            this.cache = cache;
        }
        
        private void logInvalidCacheStatus(String method) {
        	log.info("InfinispanJCacheWrapper Error: Cache is terminated: " + method); 
        }

        public org.infinispan.Cache getOriginal() {
            return cache;
        }

        public boolean containsKey(Object o) {
            boolean result = false;
            try {
            	result = cache.containsKey(o);            	
            } catch (IllegalStateException e) {
            	logInvalidCacheStatus("containsKey");
            	return false;
            }
            return result;
        }

        public boolean containsValue(Object o) {
            boolean result = false;
            try {
            	result = cache.containsValue(o);
            } catch (IllegalStateException e) {
            	logInvalidCacheStatus("containsValue");
            	return false;
            }
            return result;
        }

        public Set entrySet() {
            Set result = null;
            try {
            	result = cache.entrySet();
            } catch (IllegalStateException e) {
            	logInvalidCacheStatus("entrySet");
            	return null;
            }
            return result;
        }

        public boolean equals(Object o) {
            return (o instanceof InfinispanJCacheWrapper) &&
                    ((InfinispanJCacheWrapper)o).cache.equals(cache);
        }

        public int hashCode() {
            return (int) (((long) cache.hashCode()) +
                    this.getClass().getCanonicalName().hashCode());
        }

        public boolean isEmpty() {
            boolean result = false;
            try {
            	result = cache.isEmpty();
            } catch (IllegalStateException e) { 
            	logInvalidCacheStatus("isEmpty");
            	return true;
            }
            return result;
        }

        public Set keySet() {
            Set result = null;
            try {
            	result = cache.keySet();
            } catch (IllegalStateException e) {
            	logInvalidCacheStatus("keySet");
            	return null; //TODO //return new HashSet<Object>();
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        public void putAll(Map map) {        	
        	try {
        		cache.putAll(map);
        	} catch (IllegalStateException e) {   
        		logInvalidCacheStatus("putAll");
        	}
        }

        public int size() {
        	int result = 0;
        	try {
        		result = cache.size();
        	} catch (IllegalStateException e) {
        		logInvalidCacheStatus("size");
        		return 0;
        	}
        	return result;
        }

        public Collection values() {
        	Collection result = null;
        	try {
            	result = cache.values();
        	} catch (IllegalStateException e) {
        		logInvalidCacheStatus("values");
        		return null;
        	}
        	return result;
        }

        public Object get(Object o) {
        	Object result = null;
        	try {
        		result = cache.get(o);
        	} catch (IllegalStateException e) {
        		logInvalidCacheStatus("get");
        		return null;
        	}
        	return result;
        }

        public Map getAll(Collection collection) throws CacheException {
            Map<Object, Object> output = new ConcurrentHashMap<Object, Object>();

        	try { 
	            for (Object o : collection) {
	                output.put(o, get(o));
	            }
        	} catch (IllegalStateException e) {
        		logInvalidCacheStatus("getAll");
        		return null; // TODO return empry output 
        	}
            return output;
        }

        public void load(Object o) throws CacheException {
            for (Object listener : cache.getListeners()) {
                if (listener instanceof CacheListener) {
                    ((CacheListener)listener).onLoad(o);
                }
            }
        }

        public void loadAll(Collection collection) throws CacheException {
            for (Object o : collection) {
                load(o);
            }
        }

        public Object peek(Object o) {       	
        	Object result = null;
        	try {
            	result = cache.getAdvancedCache().get(o);
        	} catch (IllegalStateException e) {
        		logInvalidCacheStatus("peek");
        		return null;
        	}
        	return result;
        }

        @SuppressWarnings("unchecked")
        public Object put(Object o, Object o1) {
        	Object result = null;
        	try {
            	result = cache.put(o, o1);
        	} catch (IllegalStateException e) {
        		logInvalidCacheStatus("put");
        		return null;
        	}
        	return result;
        }

        public CacheEntry getCacheEntry(Object o) {
        	CacheEntry result = null;
        	try {
            	result = new InfinispanJCacheEntry(
                    cache.getAdvancedCache().getDataContainer().get(o));
        	} catch (IllegalStateException e) {
        		logInvalidCacheStatus("getCacheEntry");
        		return null;
        	}
        	return result;
        }

        public CacheStatistics getCacheStatistics() {
            return new InfinispanJCacheStatistics(cache.getAdvancedCache());
        }

        public Object remove(Object o) {
        	try {
        		return cache.remove(o);
        	} catch (IllegalStateException e){ 
        		logInvalidCacheStatus("remove");
        		return null;
        	}
        }

        public void clear() {       	
        	try {
        		cache.clear();
        	} catch (IllegalStateException e) { 
        		logInvalidCacheStatus("clear");
        	}
        }

        public void evict() {
            cache.getAdvancedCache().getEvictionManager().processEviction();
        }

        public void addListener(CacheListener cacheListener) {
            cache.addListener(cacheListener);
        }

        public void removeListener(CacheListener cacheListener) {
            cache.removeListener(cacheListener);
        }
    }
}
