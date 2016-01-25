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
package org.wso2.carbon.caching.internal;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;

import java.util.concurrent.TimeUnit;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.AccessedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

/**
 * OSGi Command provider for testing Carbon caching
 */
public class CachingCommandProvider implements CommandProvider {
    private Duration cacheExpiry = new Duration(TimeUnit.MINUTES, 15);

    @Override
    public String getHelp() {
        return "---Carbon Caching (JSR107)---\n" +
                "\tcachePut <cache-name> <key> <value> - Put a string into the cache.\n" +
                "\tcacheGet <cache-name> <key> - Get the value of <key>\n" +
                "\tcacheDelete <cache-name> <key> - Delete the cache entry corresponding to <key>\n" +
                "\tcacheFlush <cache-name> - Flush the cache\n" +
                "\tcachePrint <cache-name> - Print all the key-value pairs in the cache";
    }

    public void _cacheSetExpiry(CommandInterpreter ci) {

    }

    public void _cachePut(CommandInterpreter ci) {
        String cacheName = ci.nextArgument();
        String key = ci.nextArgument();
        String value = ci.nextArgument();
        getCache(cacheName).put(key, value);
        System.out.println("OK");
    }

    public void _cacheGet(CommandInterpreter ci) {
        String cacheName = ci.nextArgument();
        String key = ci.nextArgument();
        System.out.println(getCache(cacheName).get(key));
    }

    public void _cacheDelete(CommandInterpreter ci) {
        String cacheName = ci.nextArgument();
        String key = ci.nextArgument();
        getCache(cacheName).remove(key);
        System.out.println("OK");
    }

    public void _cacheFlush(CommandInterpreter ci) {
        String cacheName = ci.nextArgument();
        getCache(cacheName).removeAll();
        System.out.println("OK");
    }

    public void _cachePrint(CommandInterpreter ci) {
        String cacheName = ci.nextArgument();
        Cache<String, String> cache = getCache(cacheName);
        for (Cache.Entry<String, String> entry : cache) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }

    private Cache<String, String> getCache(String cacheName) {
        CachingProvider provider = Caching.getCachingProvider();
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
     * @param name
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
