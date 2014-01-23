package org.wso2.carbon.caching.core;

import java.io.Serializable;

import net.sf.jsr107cache.CacheException;

public interface CacheInvalidator {
	
	public void invalidateCache(String cacheName, Serializable key) throws CacheException;

}
