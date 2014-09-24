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
package org.wso2.carbon.user.core.claim;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClaimInvalidationCache {
	private static Log log = LogFactory.getLog(ClaimInvalidationCache.class);
	
    private static final String CLAIM_CACHE_MANAGER = "Claim.Cache.Manager";
    private String CLAIM_CACHE_NAME = "Claim.Cache";
    
    private static ClaimInvalidationCache claimCache;
    private static final Object lock = new Object();

    private String INVALIDATE_CACHE_KEY = "Invalidate.Cache.Key";

	private int myHashCode;

	private ClaimInvalidationCache() {
	}
	
	public static ClaimInvalidationCache getInstance() {
		if(claimCache == null){
			synchronized (lock){
				if(claimCache == null){
					claimCache = new ClaimInvalidationCache();
	            }
	        }
	    }
	    return claimCache;
	}

	private Cache<String,Integer> getClaimCache() {
    	CacheManager manager = Caching.getCacheManagerFactory().getCacheManager(CLAIM_CACHE_MANAGER);
        Cache<String,Integer> cache = manager.getCache(CLAIM_CACHE_NAME);
        return cache;
	}
	
	public boolean isInvalid(){
	    Integer hashCode = getValueFromCache(INVALIDATE_CACHE_KEY);
	    if(hashCode != null){
	        if(log.isDebugEnabled()){
	            log.debug("My Hash code of Claim cache is : " + myHashCode);
	            log.debug("Shared Hash code of Claim cache is : " + hashCode);
	        }
	        if(hashCode > myHashCode){
	            myHashCode = hashCode;
	            return true;
	        }
	    }
	    return false;
	}

	public void invalidateCache(){
       myHashCode ++;
       addToCache(INVALIDATE_CACHE_KEY, myHashCode);
       if(log.isDebugEnabled()){
           log.debug("My Hash code of Claim cache is : " + myHashCode);
       }
   }

	/**
	 * Add a cache entry.
	 * 
	 * @param key
	 *            Key which cache entry is indexed.
	 * @param entry
	 *            Actual object where cache entry is placed.
	 */
	private void addToCache(String key, Integer entry) {
		// Element already in the cache. Remove it first
		clearCacheEntry(key);

		Cache<String,Integer> cache = getClaimCache();
		if (cache != null) {
			cache.put(key, entry);
		}
	}

	/**
	 * Retrieves a cache entry.
	 * 
	 * @param key
	 *            CacheKey
	 * @return Cached entry.
	 */
	private Integer getValueFromCache(String key) {
		Cache<String,Integer> cache = getClaimCache();
		if (cache != null) {
            return cache.get(key);
		}
		return null;
	}

	/**
	 * Clears a cache entry.
	 * 
	 * @param key
	 *            Key to clear cache.
	 */
	private void clearCacheEntry(String key) {
		Cache<String,Integer> cache = getClaimCache();
		if (cache != null) {
            cache.remove(key);
		}
	}
}






