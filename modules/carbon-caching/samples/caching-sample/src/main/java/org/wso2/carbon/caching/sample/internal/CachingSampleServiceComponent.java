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
package org.wso2.carbon.caching.sample.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.caching.CarbonCachingService;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

/**
 * CarbonCaching sample service component.
 */
@Component(
        name = "org.wso2.carbon.caching.sample.internal.CachingSampleServiceComponent",
        immediate = true
)
public class CachingSampleServiceComponent {
    private static final Logger log = LoggerFactory.getLogger(CachingSampleServiceComponent.class);
    private Duration cacheExpiry = new Duration(TimeUnit.MINUTES, 15);
    private CarbonCachingService cachingService;

    @Activate
    protected void activate(BundleContext bundleContext) {
        String cacheName = "CachingSample.cache";
        String key = "k";
        String value = "v";

        try {
            getCache(cacheName).put(key, value);
            log.info("Cache put succeeded");
            log.info("Cache get: key=" + key + ", value=" + getCache(cacheName).get(key));
            getCache(cacheName).remove(key);
            log.info("Cache delete succeeded");
            log.info("Cache get: key=" + key + ", value=" + getCache(cacheName).get(key));

            log.info("CachingSample is activated");
        } catch (Throwable e) {
            log.error("Could not activate CachingSample bundle", e);
        }
    }

    @Reference(
            name = "carbon-caching.service",
            service = CarbonCachingService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCachingService"
    )
    protected void addCachingService(CarbonCachingService cachingService, Map<String, ?> properties) {
        this.cachingService = cachingService;
    }

    protected void removeCachingService(CarbonCachingService cachingService, Map<String, ?> properties) {
        this.cachingService = null;
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
