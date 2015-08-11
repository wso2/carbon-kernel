/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.caching.impl;

import javax.cache.management.CacheStatisticsMXBean;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cache stat mx bean implementation
 */
public class CacheStatisticsMXBeanImpl implements CacheStatisticsMXBean, Serializable{

	private static final long serialVersionUID = -5589437411679003894L;
	private static final long NANOSECONDS_IN_A_MICROSECOND = 1000L;

	private final AtomicLong cacheRemovals = new AtomicLong();
	private final AtomicLong cacheExpiries = new AtomicLong();
	private final AtomicLong cachePuts = new AtomicLong();
	private final AtomicLong cacheHits = new AtomicLong();
	private final AtomicLong cacheMisses = new AtomicLong();
	private final AtomicLong cacheEvictions = new AtomicLong();
	private final AtomicLong cachePutTimeTakenNanos = new AtomicLong();
	private final AtomicLong cacheGetTimeTakenNanos = new AtomicLong();
	private final AtomicLong cacheRemoveTimeTakenNanos = new AtomicLong();

	public CacheStatisticsMXBeanImpl() {
	}

	@Override
	public void clear() {
		cachePuts.set(0);
		cacheMisses.set(0);
		cacheRemovals.set(0);
		cacheExpiries.set(0);
		cacheHits.set(0);
		cacheEvictions.set(0);
		cacheGetTimeTakenNanos.set(0);
		cachePutTimeTakenNanos.set(0);
		cacheRemoveTimeTakenNanos.set(0);
	}

	/**
	 * @return the number of hits
	 */
	@Override
	public long getCacheHits() {
		return cacheHits.longValue();
	}

	/**
	 * Returns cache hits as a percentage of total gets.
	 *
	 * @return the percentage of successful hits, as a decimal
	 */
	@Override
	public float getCacheHitPercentage() {
		Long hits = getCacheHits();
		if (hits == 0) {
			return 0;
		}
		return (float) hits / getCacheGets() * 100.0f;
	}

	/**
	 * @return the number of misses
	 */
	@Override
	public long getCacheMisses() {
		return cacheMisses.longValue();
	}

	/**
	 * Returns cache misses as a percentage of total gets.
	 *
	 * @return the percentage of accesses that failed to find anything
	 */
	@Override
	public float getCacheMissPercentage() {
		Long misses = getCacheMisses();
		if (misses == 0) {
			return 0;
		}
		return (float) misses / getCacheGets() * 100.0f;
	}

	/**
	 * The total number of requests to the cache. This will be equal to the sum of the hits and misses.
	 * <p/>
	 * A "get" is an operation that returns the current or previous value.
	 *
	 * @return the number of hits
	 */
	@Override
	public long getCacheGets() {
		return getCacheHits() + getCacheMisses();
	}

	/**
	 * The total number of puts to the cache.
	 * <p/>
	 * A put is counted even if it is immediately evicted. A replace includes a put and remove.
	 *
	 * @return the number of hits
	 */
	@Override
	public long getCachePuts() {
		return cachePuts.longValue();
	}

	/**
	 * The total number of removals from the cache. This does not include evictions, where the cache itself
	 * initiates the removal to make space.
	 * <p/>
	 * A replace invcludes a put and remove.
	 *
	 * @return the number of hits
	 */
	@Override
	public long getCacheRemovals() {
		return cacheRemovals.longValue();
	}

	/**
	 * @return the number of evictions from the cache
	 */
	@Override
	public long getCacheEvictions() {
		return cacheEvictions.longValue();
	}

	/**
	 * The mean time to execute gets.
	 *
	 * @return the time in milliseconds
	 */
	@Override
	public float getAverageGetTime() {
		if (cacheGetTimeTakenNanos.longValue() == 0 || getCacheGets() == 0) {
			return 0;
		}
		return (cacheGetTimeTakenNanos.longValue() / getCacheGets()) / NANOSECONDS_IN_A_MICROSECOND;
	}

	/**
	 * The mean time to execute puts.
	 *
	 * @return the time in milliseconds
	 */
	@Override
	public float getAveragePutTime() {
		if (cachePutTimeTakenNanos.longValue() == 0 || getCacheGets() == 0) {
			return 0;
		}
		return (cachePutTimeTakenNanos.longValue() / getCacheGets()) / NANOSECONDS_IN_A_MICROSECOND;
	}

	/**
	 * The mean time to execute removes.
	 *
	 * @return the time in milliseconds
	 */
	@Override
	public float getAverageRemoveTime() {
		if (cacheRemoveTimeTakenNanos.longValue() == 0 || getCacheGets() == 0) {
			return 0;
		}
		return (cacheRemoveTimeTakenNanos.longValue() / getCacheGets()) / NANOSECONDS_IN_A_MICROSECOND;
	}

	//package local incrementers

	/**
	 * Increases the counter by the number specified.
	 *
	 * @param number the number to increase the counter by
	 */
	public void increaseCacheRemovals(long number) {
		cacheRemovals.getAndAdd(number);
	}

	/**
	 * Increases the counter by the number specified.
	 *
	 * @param number the number to increase the counter by
	 */
	public void increaseCacheExpiries(long number) {
		cacheExpiries.getAndAdd(number);
	}

	/**
	 * Increases the counter by the number specified.
	 *
	 * @param number the number to increase the counter by
	 */
	public void increaseCachePuts(long number) {
		cachePuts.getAndAdd(number);
	}

	/**
	 * Increases the counter by the number specified.
	 *
	 * @param number the number to increase the counter by
	 */
	public void increaseCacheHits(long number) {
		cacheHits.getAndAdd(number);
	}

	/**
	 * Increases the counter by the number specified.
	 *
	 * @param number the number to increase the counter by
	 */
	public void increaseCacheMisses(long number) {
		cacheMisses.getAndAdd(number);
	}

	/**
	 * Increases the counter by the number specified.
	 *
	 * @param number the number to increase the counter by
	 */
	public void increaseCacheEvictions(long number) {
		cacheEvictions.getAndAdd(number);
	}

}
