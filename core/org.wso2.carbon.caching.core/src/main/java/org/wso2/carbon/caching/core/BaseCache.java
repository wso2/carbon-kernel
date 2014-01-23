package org.wso2.carbon.caching.core;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheManager;

/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Date: Oct 1, 2010 Time: 2:47:14 PM
 */

/**
 * A base class for all cache implementations in user core module.
 */
public abstract class BaseCache {

	protected Cache cache = null;

	protected BaseCache(String cacheName) {
		this.cache = CacheManager.getInstance().getCache(cacheName);
	}

    

	/**
	 * Add a cache entry.
	 * 
	 * @param key
	 *            Key which cache entry is indexed.
	 * @param entry
	 *            Actual object where cache entry is placed.
	 */
	public void addToCache(CacheKey key, CacheEntry entry) {
		if (this.cache.containsKey(key)) {
			// Element already in the cache. Remove it first
			this.cache.remove(key);
		}

		this.cache.put(key, entry);
	}

	/**
	 * Retrieves a cache entry.
	 * 
	 * @param key
	 *            CacheKey
	 * @return Cached entry.
	 */
	public CacheEntry getValueFromCache(CacheKey key) {

		if (this.cache.containsKey(key)) {
			return (CacheEntry) this.cache.get(key);
		}

		return null;

	}

	/**
	 * Clears a cache entry.
	 * 
	 * @param key
	 *            Key to clear cache.
	 */
	public void clearCacheEntry(CacheKey key) {
		if (this.cache.containsKey(key)) {
			this.cache.remove(key);
		}
	}

	/**
	 * Remove everything in the cache.
	 */
	public void clear() {
		this.cache.clear();
	}

}
