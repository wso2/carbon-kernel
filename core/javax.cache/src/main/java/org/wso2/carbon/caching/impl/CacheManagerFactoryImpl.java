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

import javax.cache.CacheManager;
import javax.cache.CacheManagerFactory;
import javax.cache.CachingShutdownException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * TODO: class description
 */
public class CacheManagerFactoryImpl implements CacheManagerFactory {

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

    static void addCacheForMonitoring(CacheImpl cache) {
        cacheCleanupTask.addCacheForMonitoring(cache);
    }

    void removeCacheFromMonitoring(CacheImpl cache) {
        cacheCleanupTask.removeCacheFromMonitoring(cache);
    }

    /**
     * Map<tenantDomain, Map<cacheManagerName,CacheManager> >
     */
    private Map<String, Map<String, CacheManager>> globalCacheManagerMap =
            new ConcurrentHashMap<String, Map<String, CacheManager>>();

    void switchToDistributedMode(){
        for (Map<String, CacheManager> cacheManagerMap : globalCacheManagerMap.values()) {
            for (CacheManager cacheManager : cacheManagerMap.values()) {
                ((CarbonCacheManager) cacheManager).switchToDistributedMode();
            }
        }
    }

    @Override
    public CacheManager getCacheManager(String cacheManagerName) {
        String tenantDomain = Util.getTenantDomain();
        if(tenantDomain == null){
            throw new NullPointerException("Tenant domain has not been set in CarbonContext");
        }
        Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
        if (cacheManagers == null) {
            synchronized (tenantDomain.intern()) {
                if ((cacheManagers = globalCacheManagerMap.get(tenantDomain)) == null) {
                    cacheManagers = new ConcurrentHashMap<String, CacheManager>();
                    globalCacheManagerMap.put(tenantDomain, cacheManagers);
                }
            }
        }
        CacheManager cacheManager = cacheManagers.get(cacheManagerName);
        if (cacheManager == null) {
            synchronized ((tenantDomain + "*.*" + cacheManagerName).intern()) {
                if ((cacheManager = cacheManagers.get(cacheManagerName)) == null) {
                    cacheManager = new CarbonCacheManager(cacheManagerName, this);
                    cacheManagers.put(cacheManagerName, cacheManager);
                }
            }
        }
        return cacheManager;
    }

    @Override
    public CacheManager getCacheManager(ClassLoader classLoader, String name) {
        // Since we have a single CacheManager, we don't have to take the ClassLoader into consideration
        return getCacheManager(name);
    }

    @Override
    public void close() throws CachingShutdownException {
        String tenantDomain = Util.getTenantDomain();
        synchronized (tenantDomain.intern()) {
            Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
            if (cacheManagers != null) {
                for (CacheManager cacheManager : cacheManagers.values()) {
                    cacheManager.shutdown();
                }
                cacheManagers.clear();
            }
        }
    }

    @Override
    public boolean close(ClassLoader classLoader) throws CachingShutdownException {
        close();
        return true;
    }

    @Override
    public boolean close(ClassLoader classLoader, String name) throws CachingShutdownException {
        String tenantDomain = Util.getTenantDomain();
        Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
        CacheManager cacheManager;
        if (cacheManagers != null) {
            cacheManager = cacheManagers.get(name);
            cacheManager.shutdown();
            return true;
        }
        return false;
    }

    public void removeCacheManager(CarbonCacheManager cacheManager, String tenantDomain) {
        Map<String, CacheManager> cacheManagers = globalCacheManagerMap.get(tenantDomain);
        if (cacheManagers != null) {
            cacheManagers.remove(cacheManager.getName());
        }
    }

    /**removing tenant data from global cache manager map
     *
     * @param tenantDomain
     */
    public void removeTenant(String tenantDomain) {
        globalCacheManagerMap.remove(tenantDomain);
    }
}
