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
package org.wso2.carbon.caching;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple implementation of a {@link InternalMap} based on a {@link ConcurrentHashMap}.
 *
 * @param <K> the type of keys stored
 * @param <V> the type of values stored
 */
class SimpleInternalMap<K, V> implements InternalMap<K, V> {

  /**
   * The map containing the entries.
   */
  private final ConcurrentHashMap<K, V> internalMap = new ConcurrentHashMap<K, V>();

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsKey(Object key) {
    //noinspection SuspiciousMethodCalls
    return internalMap.containsKey(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void put(K key, V value) {
    internalMap.put(key, value);
  }

  @Override
  public V getAndPut(K key, V value) {
    return internalMap.put(key, value);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V remove(Object key) {
    //noinspection SuspiciousMethodCalls
    return internalMap.remove(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return internalMap.size();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Iterator<Map.Entry<K, V>> iterator() {
    return internalMap.entrySet().iterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public V get(Object key) {
    //noinspection SuspiciousMethodCalls
    return internalMap.get(key);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    internalMap.clear();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();

    builder.append(getClass().getName());
    builder.append("{");

    boolean isFirst = true;
    for (K key : internalMap.keySet()) {
      if (isFirst) {
        isFirst = false;
      } else {
        builder.append(", ");
      }

      builder.append("<");
      builder.append(key);
      builder.append(", ");
      builder.append(internalMap.get(key));
      builder.append(">");
    }

    builder.append("}");
    return builder.toString();
  }
}
