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
package org.wso2.carbon.caching.core;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;

/**
 * A cache manager implementation for Carbon based on JSR 107 JCache.
 */
public interface CarbonCacheManager {

    /**
     * Method to initialize the cache manager. This should only be called once for a given JVM
     * instance.
     *
     * @param carbonHome the location of the home directory of the Carbon Server.
     */
    void initialize(String carbonHome);

    /**
     * Method to obtain the name of the default cache.
     *
     * @return the name of the default cache.
     */
    String getDefaultCacheName();

    /**
     * Method to obtain a named cache instance.
     *
     * @param cacheName the name of the cache.
     *
     * @return the cache instance.
     */
    Cache getCache(String cacheName);
    
    /**
     * Registers the given cache instance by the given name.
     *
     * @param cacheName the name of the cache.
     * @param cache     the cache instance.
     */
    void registerCache(String cacheName, Cache cache);

    /**
     * Method to obtain a Cache Factory instance.
     *
     * @return the cache factory instance.
     *
     * @throws CacheException if the operation is not allowed or if it fails.
     */
    CacheFactory getCacheFactory() throws CacheException;
}
