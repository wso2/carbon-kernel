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

import org.wso2.carbon.caching.impl.CacheEntry;

import javax.cache.Cache;
import javax.cache.configuration.Factory;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: class description
 */
class TestCacheLoader<K, V> implements CacheLoader<K, V>, Factory<CacheLoader<String, String>> {

    @Override
    public V load(K key) throws CacheLoaderException {
        return new CacheEntry<K, V>(key, (V) ("key" + System.currentTimeMillis())).getValue();
    }

    @Override
    public Map<K, V> loadAll(Iterable<? extends K> keys) throws CacheLoaderException {
        Map<K, V> map = new HashMap<K, V>();
        for (K key : keys) {
            map.put(key, (V) ("key" + System.currentTimeMillis()));
        }
        return map;
    }

    @Override public CacheLoader<String, String> create() {
        return new TestCacheLoader<>();
    }
}
