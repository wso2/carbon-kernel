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
package org.wso2.carbon.kernel.internal.startupresolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Counter implementation which maintains multiple key occurrences. This implementation is thread-safe.
 *
 * @param <K> the type of keys maintained by this map
 */
public class MultiCounter<K> {
    private Map<K, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    /**
     * Increment the count of the specified key by one and returns new value.
     *
     * @param key with which the associated value is incremented by one
     * @return count after incrementing by one.
     */
    public synchronized int incrementAndGet(K key) {
        if (!counterMap.containsKey(key)) {
            counterMap.put(key, new AtomicInteger(1));
            return 1;
        }
        return counterMap.get(key).incrementAndGet();
    }

    /**
     * Decrements the count of the specified key by one and returns the new value.
     *
     * @param key with which the associated value is decremented by one.
     * @return count after decrementing by one.
     */
    public synchronized int decrementAndGet(K key) {
        if (counterMap.containsKey(key)) {
            int tally = counterMap.get(key).decrementAndGet();
            if (tally == 0) {
                counterMap.remove(key);
                return tally;
            }
            return tally;
        } else {
            counterMap.put(key, new AtomicInteger(-1));
            return -1;
        }
    }

    /**
     * Returns count of the specified key.
     *
     * @param key to be used to retrieve the count.
     * @return count of the specified key.
     */
    public int get(K key) {
        if (counterMap.get(key) == null) {
            return 0;
        }
        return counterMap.get(key).get();
    }

    /**
     * Returns all the keys managed by this counter.
     *
     * @return a list of Keys.
     */
    public List<K> getAllKeys() {
        return new ArrayList<>(counterMap.keySet());
    }
}
