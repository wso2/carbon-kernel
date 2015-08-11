/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.core.caching;

import javax.cache.Cache;
import javax.cache.management.CacheStatisticsMXBean;

import org.wso2.carbon.caching.impl.CacheImpl;
import org.wso2.carbon.caching.impl.CacheStatisticsMXBeanImpl;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

/**
 * Implementation of caching for resource paths. This is based on {@link Cache}. The path cache
 * instance will maintain a cache for registry paths. However, it is required to create an extension
 * of this class to actually access the cache.
 */
public class PathCache {

    /**
     * The path cache.
     */
    protected static Cache<RegistryCacheKey, RegistryCacheEntry> getCache() {
        return RegistryUtils.getResourcePathCache(RegistryConstants.PATH_CACHE_ID);
    }

    /**
     * Method to obtain the created path cache instance. If the path cache instance has not been
     * created as yet, an instance will be created using the default settings.
     *
     * @return the created path cache instance
     */
    public static PathCache getPathCache() {
        return new PathCache();
    }

    /**
     * Method to get the cache hit rate.
     *
     * @return the cache hit rate.
     */
    public double hitRate() {
        CacheStatisticsMXBean stats = ((CacheImpl)getCache()).getStatisticsMXBean();
        if((stats.getCacheHits() + stats.getCacheMisses())==0){
        	return 0;
        }else{
        return (double) stats.getCacheHits() /
                ((double) (stats.getCacheHits() + stats.getCacheMisses()));
        }
    }
}