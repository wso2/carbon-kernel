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
package org.wso2.carbon.caching.impl;



import com.hazelcast.cache.impl.HazelcastServerCachingProvider;
import com.hazelcast.core.HazelcastInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;

import javax.cache.CacheManager;

/**
 * TODO: class description
 */
public class DataHolder {
	private static final Log log = LogFactory.getLog(DataHolder.class);
	private static DataHolder instance = new DataHolder();

	private ServerConfigurationService serverConfigurationService;
	private HazelcastInstance hazelcastInstance;
	private CacheManager hazelcastCacheManager;

	private DataHolder() {
	}

	public static DataHolder getInstance() {
		return instance;
	}


	public ServerConfigurationService getServerConfigurationService() {
		if (this.serverConfigurationService == null) {
			String msg = "Before activating javax caching  bundle, an instance of "
			             + "ServerConfigurationService should be in existence";
			log.error(msg);
		}
		return this.serverConfigurationService;
	}

	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}

	public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	public CacheManager getHazelcastCacheManager() {
		if (hazelcastInstance != null) {
			hazelcastCacheManager =
					HazelcastServerCachingProvider.createCachingProvider(hazelcastInstance).getCacheManager();
		}
		return hazelcastCacheManager;
	}
}
