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

import javax.cache.Cache;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.management.CacheMXBean;

/**
 * Cache mx bean implementation
 */
public class CacheMXBeanImpl<K, V> implements CacheMXBean {

	private final Cache<K, V> cache;

	/**
	 * Constructor
	 *
	 * @param cache             the cache
	 */
	public CacheMXBeanImpl(Cache cache) {
		this.cache = cache;
	}

	@Override
	public String getKeyType() {
		return cache.getConfiguration(CompleteConfiguration.class).getKeyType().getName();
	}

	@Override
	public String getValueType() {
		return cache.getConfiguration(CompleteConfiguration.class).getValueType().getName();
	}

	@Override
	public boolean isReadThrough() {
		return cache.getConfiguration(CompleteConfiguration.class).isReadThrough();
	}

	@Override
	public boolean isWriteThrough() {
		return cache.getConfiguration(CompleteConfiguration.class).isWriteThrough();
	}

	@Override
	public boolean isStoreByValue() {
		return cache.getConfiguration(Configuration.class).isStoreByValue();
	}

	@Override
	public boolean isStatisticsEnabled() {
		return cache.getConfiguration(CompleteConfiguration.class).isStatisticsEnabled();
	}

	@Override
	public boolean isManagementEnabled() {
		return cache.getConfiguration(CompleteConfiguration.class).isManagementEnabled();
	}
}
