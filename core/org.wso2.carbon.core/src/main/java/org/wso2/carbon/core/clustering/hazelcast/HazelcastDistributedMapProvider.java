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
package org.wso2.carbon.core.clustering.hazelcast;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.DistributedMapProvider;
import org.wso2.carbon.caching.impl.MapEntryListener;

import java.util.*;

public class HazelcastDistributedMapProvider implements DistributedMapProvider {

    private static final Log log = LogFactory.getLog(HazelcastDistributedMapProvider.class);

    private HazelcastInstance hazelcastInstance;
    private Map<String, DistMap> maps = new HashMap<String, DistMap>();

    public HazelcastDistributedMapProvider(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public <K, V> Map<K, V> getMap(String mapName, final MapEntryListener entryListener) {
        DistMap<K,V> map = maps.get(mapName);
        if(map == null){
            map = new DistMap<K, V>(mapName, entryListener);
            maps.put(mapName, map);
        }
        return map;
    }

    @Override
    public void removeMap(String mapName) {
        DistMap map = maps.get(mapName);
        if(map != null) {
            hazelcastInstance.getMap(mapName).removeEntryListener(map.getListenerId());
        }
        maps.remove(mapName);
        hazelcastInstance.getMap(mapName).flush();
    }

    private class DistMap<K, V> implements Map<K, V> {
        private IMap<K, V> map;
        private String listenerId;

        public DistMap(String mapName, final MapEntryListener entryListener) {
            this.map = hazelcastInstance.getMap(mapName);
            if (entryListener != null) {
                listenerId = map.addEntryListener(new EntryListener<K, V>() {
                    @Override
                    public void entryAdded(EntryEvent<K, V> kvEntryEvent) {
                        if (!kvEntryEvent.getMember().equals(hazelcastInstance.getCluster().getLocalMember())) {
                            entryListener.entryAdded(kvEntryEvent.getKey());
                        }
                    }

                    @Override
                    public void entryRemoved(EntryEvent<K, V> kvEntryEvent) {
                        if (!kvEntryEvent.getMember().equals(hazelcastInstance.getCluster().getLocalMember())) {
                            entryListener.entryRemoved(kvEntryEvent.getKey());
                        }
                    }

                    @Override
                    public void entryUpdated(EntryEvent<K, V> kvEntryEvent) {
                        if (!kvEntryEvent.getMember().equals(hazelcastInstance.getCluster().getLocalMember())) {
                            entryListener.entryUpdated(kvEntryEvent.getKey());
                        }
                    }

                    @Override
                    public void entryEvicted(EntryEvent<K, V> kvEntryEvent) {
                        if (!kvEntryEvent.getMember().equals(hazelcastInstance.getCluster().getLocalMember())) {
                            entryListener.entryRemoved(kvEntryEvent.getKey());
                        }
                    }

                    @Override
                    public void mapEvicted(MapEvent mapEvent) {
                        map.evictAll();
                    }

                    @Override
                    public void mapCleared(MapEvent mapEvent) {
                        map.clear();
                    }
                }, false);
            }
        }

        @Override
        public int size() {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                return map.size();
            }
            return 0;
        }

        @Override
        public boolean isEmpty() {
            if(hazelcastInstance.getLifecycleService().isRunning()){
                return map.isEmpty();
            }
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            return hazelcastInstance.getLifecycleService().isRunning() && map.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return hazelcastInstance.getLifecycleService().isRunning() && map.containsValue(value);
        }

        @Override
        public V get(Object key) {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                return map.get(key);
            }
            return null;
        }

        @Override
        public V put(K key, V value) {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                map.set(key, value);
            }
            return value;
        }

        @Override
        public V remove(Object key) {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                map.remove((K)key);
            }
            return null;
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                map.putAll(m);
            }
        }

        @Override
        public void clear() {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                map.clear();
            }
        }

        @Override
        public Set<K> keySet() {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                return map.keySet();
            }
            return new LinkedHashSet<K>();
        }

        @Override
        public Collection<V> values() {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                return map.values();
            }
            return new ArrayList<V>();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            if (hazelcastInstance.getLifecycleService().isRunning()) {
                return map.entrySet();
            }
            return new LinkedHashSet<Entry<K, V>>();
        }

        public String getListenerId() {
            return listenerId;
        }
    }
}
