/**
 *  Copyright 2011-2013 Terracotta, Inc.
 *  Copyright 2011-2013 Oracle America Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.caching.impl;

import javax.cache.Cache;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.EventType;

/**
 *  Cache entry event implementation
 */
public class CacheEntryEventImpl<K, V> extends CacheEntryEvent<K, V> {

	private K key;
	private V value;
	private V oldValue;
	private boolean oldValueAvailable;

	public CacheEntryEventImpl(Cache<K, V> source, EventType eventType) {
		super(source, eventType);
	}


	/**
	 * Constructs a cache entry event from a given cache as source
	 *
	 * @param source    the cache that originated the event
	 * @param eventType
	 */
	public CacheEntryEventImpl(Cache<K, V> source, K key, V value, EventType eventType) {
		super(source, eventType);
		this.key = key;
		this.value = value;
		this.oldValue = null;
		this.oldValueAvailable = false;
	}

	/**
	 * Constructs a cache entry event from a given cache as source
	 * (with an old value)
	 *
	 * @param source   the cache that originated the event
	 * @param key      the key
	 * @param value    the value
	 * @param oldValue the oldValue
	 */
	public CacheEntryEventImpl(Cache<K, V> source, K key, V value, V oldValue, EventType eventType) {
		super(source, eventType);
		this.key = key;
		this.value = value;
		this.oldValue = oldValue;
		this.oldValueAvailable = true;
	}


	public void setKey(K key) {
		this.key = key;
	}

	public void setValue(V value) {
		this.value = value;
	}

	@Override
	public V getOldValue() {
		if (isOldValueAvailable()) {
			return oldValue;
		} else {
			throw new UnsupportedOperationException("Old value is not available for key");
		}
	}

	@Override
	public boolean isOldValueAvailable() {
		return oldValueAvailable;
	}

	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public <T> T unwrap(Class<T> clazz) {
		if (clazz != null && clazz.isInstance(this)) {
			return (T) this;
		} else {
			throw new IllegalArgumentException("The class " + clazz + " is unknown to this implementation");
		}
	}
}
