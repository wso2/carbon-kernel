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

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

/**
 * OSGi Command provider for testing Carbon caching
 */
public class CachingCommandProvider implements CommandProvider {

    @Override
    public String getHelp() {
        return "---Carbon Caching (JSR107)---\n" +
                "\tcachePut <cache-name> <key> <value> - Put a string into the cache.\n" +
                "\tcacheGet <cache-name> <key> - Get the value of <key>\n" +
                "\tcacheDelete <cache-name> <key> - Delete the cache entry corresponding to <key>\n";
    }

    public void _cachePut(CommandInterpreter ci) {
        String cacheName = ci.nextArgument();
        String key = ci.nextArgument();
        String value = ci.nextArgument();
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        cacheManager.getCache(cacheName).put(key, value);
    }

    public void _cacheGet(CommandInterpreter ci) {
        String cacheName = ci.nextArgument();
        String key = ci.nextArgument();
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        System.out.println(cacheManager.getCache(cacheName).get(key));
    }

    public void _cacheDelete(CommandInterpreter ci) {
        String cacheName = ci.nextArgument();
        String key = ci.nextArgument();
        CachingProvider provider = Caching.getCachingProvider();
        CacheManager cacheManager = provider.getCacheManager();
        System.out.println(cacheManager.getCache(cacheName).remove(key));
    }
}
