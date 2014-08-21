package org.wso2.carbon.caching.core.ehcache;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.manager.DefaultCacheManager;
import org.wso2.carbon.caching.core.CacheConfiguration;
import org.wso2.carbon.caching.core.CarbonCacheManager;

public class EhcacheManager extends net.sf.jsr107cache.CacheManager implements CarbonCacheManager {
    private static final Log log = LogFactory.getLog(EhcacheManager.class);
    private static Boolean lock = new Boolean(true);
    private long maxExpirationMillis = 0;
    private long maxIdleExpirationMillis = 0;
    private int maxEntries = 0;

    public void initialize(String carbonHome) {
        org.wso2.carbon.caching.core.CacheConfiguration cacheConfiguration =
                                                                             org.wso2.carbon.caching.core.CacheConfiguration.getInstance();

        maxExpirationMillis =
                                   cacheConfiguration.getProperty("maxExpirationMillis") == null
                                                                                                ? CacheConfiguration.DEFAULT_EXPIRATION
                                                                                                : Long.parseLong(cacheConfiguration.getProperty("maxExpirationMillis"));

        maxIdleExpirationMillis =
                                       cacheConfiguration.getProperty("maxIdleExpirationMillis") == null
                                                                                                        ? CacheConfiguration.DEFAULT_EXPIRATION
                                                                                                        : Long.parseLong(cacheConfiguration.getProperty("maxIdleExpirationMillis"));

        maxEntries =
                         cacheConfiguration.getProperty("maxEntries") == null
                                                                             ? CacheConfiguration.MAX_ELEMENTS
                                                                             : Integer.parseInt(cacheConfiguration.getProperty("maxEntries"));

        if (log.isDebugEnabled()) {
            log.debug("cache manager initialized");
        }

    }

    public String getDefaultCacheName() {
        return DefaultCacheManager.DEFAULT_CACHE_NAME;
    }

    public Cache getCache(String cacheName) {
		if (cacheName == null) {
			cacheName = DefaultCacheManager.DEFAULT_CACHE_NAME;
		}
	    net.sf.ehcache.CacheManager manager = net.sf.ehcache.CacheManager.getInstance();
	    Cache cache = manager.getJCache(cacheName);
	    if (cache == null) {
		    synchronized (lock) {
			    cache = manager.getJCache(cacheName);
			    if (cache == null) {
				    manager.addCache(cacheName);
				    cache = manager.getJCache(cacheName);
				    net.sf.ehcache.config.CacheConfiguration cacheConfig =
						    manager.getCache(cacheName).getCacheConfiguration();
				    cacheConfig.setDiskPersistent(false);
				    cacheConfig.setDiskSpoolBufferSizeMB(0);
				    cacheConfig.setOverflowToDisk(false);
				    cacheConfig.setTimeToIdleSeconds(maxIdleExpirationMillis / 1000L);
				    cacheConfig.setTimeToLiveSeconds(maxExpirationMillis / 1000L);
				    cacheConfig.setMaxElementsInMemory(maxEntries);
                }
            }
        }
        return cache;
    }

    public CacheFactory getCacheFactory() throws CacheException {
        throw new CacheException(
                                 "The EhcacheCacheManager does not provide an in-built nor look-up a CacheFactory.");
    }

    public void registerCache(String cacheName, Cache cache) {
        throw new RuntimeException("The EhcacheCacheManager does not provide register.");
    }
}
