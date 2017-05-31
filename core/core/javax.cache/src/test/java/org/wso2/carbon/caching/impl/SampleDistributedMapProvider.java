/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.caching.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Sample DistributedMapProvider implementation to test MapEntryListener interface.
 */
public class SampleDistributedMapProvider implements DistributedMapProvider {

    private Map<String, DistMap> maps = new HashMap<>();

    public SampleDistributedMapProvider(String mapName) {
        MapEntryListener entryListener = new SampleMapEntryListenerTest();
        addSampleMap(mapName, entryListener);
    }

    private void addSampleMap (String mapName, final MapEntryListener entryListener) {
        DistMap<String, String> sampleMap = new DistMap<>(entryListener);
        maps.put(mapName, sampleMap);
    }

    @Override
    public <K, V> Map<K, V> getMap(String mapName, final MapEntryListener entryListener) {
        return maps.get(mapName);
    }

    @Override
    public void removeMap(String mapName) {
        maps.remove(mapName);
    }

    private class SampleMapEntryListenerTest implements MapEntryListener {

        @Override
        public <X> void entryAdded(X key) {
            throw new RuntimeException("entryAdded event got executed.");
        }

        @Override
        public <X> void entryRemoved(X key) {
            throw new RuntimeException("entryRemoved event got executed.");
        }

        @Override
        public <X> void entryUpdated(X key) {
            throw new RuntimeException("entryUpdated event got executed.");
        }

        @Override
        public void mapCleared() {
            throw new RuntimeException("mapCleared event got executed.");
        }
    }

    private class DistMap<K, V> implements Map<K, V> {
        private Map<K, V> map;
        private MapEntryListener entryListener;

        public DistMap(final MapEntryListener entryListener) {
            this.map = new HashMap<>();
            this.entryListener = entryListener;
        }

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return map.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return map.get(key);
        }

        @Override
        public V put(K key, V value) {
            map.put(key, value);
            entryListener.entryAdded(key);
            return value;
        }

        @Override
        public V remove(Object key) {
            map.remove(key);
            entryListener.entryRemoved(key);
            return null;
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            map.putAll(m);
        }

        @Override
        public void clear() {
            map.clear();
            entryListener.mapCleared();
        }

        @Override
        public Set<K> keySet() {
            return new LinkedHashSet<K>();
        }

        @Override
        public Collection<V> values() {
            return new ArrayList<>();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return new LinkedHashSet<>();
        }
    }
}
