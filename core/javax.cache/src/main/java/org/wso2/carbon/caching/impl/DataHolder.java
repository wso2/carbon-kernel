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

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.api.ServerConfigurationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.CacheInvalidationRequestPropagator;
import javax.cache.CacheInvalidationRequestSender;
import javax.cache.event.CacheEntryListener;
import javax.cache.spi.AnnotationProvider;

/**
 * TODO: class description
 */
public class DataHolder {

    private static final Log log = LogFactory.getLog(DataHolder.class);
    private static DataHolder instance = new DataHolder();

    private DistributedMapProvider distributedMapProvider;
    private ServerConfigurationService serverConfigurationService;
    private ClusteringAgent clusteringAgent;
    private CachingProviderImpl cachingProvider = new CachingProviderImpl();
    private AnnotationProvider annotationProvider = new AnnotationProviderImpl();
    private List<CacheEntryListener> cacheEntryListeners = new ArrayList<>();
    private Map<String, CacheInvalidationRequestSender> cacheInvalidationRequestSenders = new HashMap<>();
    private CacheInvalidationRequestSender configuredCacheInvalidationSender;
    private List<CacheInvalidationRequestPropagator> cacheInvalidationRequestPropagators = new ArrayList<>();

    public static DataHolder getInstance() {

        return instance;
    }

    private DataHolder() {

    }

    public DistributedMapProvider getDistributedMapProvider() {

        return distributedMapProvider;
    }

    public ServerConfigurationService getServerConfigurationService() {

        if (this.serverConfigurationService == null) {
            String msg = "Before activating javax caching  bundle, an instance of "
                    + "ServerConfigurationService should be in existence";
            log.error(msg);
        }
        return this.serverConfigurationService;
    }

    public void setDistributedMapProvider(DistributedMapProvider distributedMapProvider) {

        this.distributedMapProvider = distributedMapProvider;
        try {
            if (distributedMapProvider != null) {
                cachingProvider.switchToDistributedMode();
            }
        } catch (Exception e) {
            log.error("Cannot setDistributedMapProvider", e);
        }
    }

    public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {

        this.serverConfigurationService = serverConfigurationService;
    }

    public void setClusteringAgent(ClusteringAgent clusteringAgent) {

        this.clusteringAgent = clusteringAgent;
    }

    public CachingProviderImpl getCachingProvider() {

        return cachingProvider;
    }

    public AnnotationProvider getAnnotationProvider() {

        return annotationProvider;
    }

    public ClusteringAgent getClusteringAgent() {

        return clusteringAgent;
    }

    public List<CacheEntryListener> getCacheEntryListeners() {

        return cacheEntryListeners;
    }

    public Map<String, CacheInvalidationRequestSender> getCacheInvalidationRequestSenders() {

        return cacheInvalidationRequestSenders;
    }

    public void addCacheInvalidationRequestSender(String name,
                                                  CacheInvalidationRequestSender cacheInvalidationRequestSender) {

        this.cacheInvalidationRequestSenders.put(name, cacheInvalidationRequestSender);
        this.configuredCacheInvalidationSender = null;
    }

    public void removeCacheInvalidationRequestSender(String name) {

        this.cacheInvalidationRequestSenders.remove(name);
        this.configuredCacheInvalidationSender = null;
    }

    /*
    Method mark as package private in order to not to call from outside and this class need to be moved into the
    internal package.
     */
    CacheInvalidationRequestSender getConfiguredCacheInvalidationSender() {

        if (configuredCacheInvalidationSender == null) {
            synchronized (this) {
                if (configuredCacheInvalidationSender == null) {
                    configuredCacheInvalidationSender = Util.getCacheInvalidationRequestSender();
                }
                return configuredCacheInvalidationSender;
            }
        }
        return configuredCacheInvalidationSender;
    }

    public List<CacheInvalidationRequestPropagator> getCacheInvalidationRequestPropagators() {

        return cacheInvalidationRequestPropagators;
    }

    public void addCacheInvalidationRequestPropagator(
            CacheInvalidationRequestPropagator cacheInvalidationRequestPropagator) {

        this.cacheInvalidationRequestPropagators.add(cacheInvalidationRequestPropagator);
    }

    public void removeCacheInvalidationRequestPropagator(
            CacheInvalidationRequestPropagator cacheInvalidationRequestPropagator) {

        this.cacheInvalidationRequestPropagators.remove(cacheInvalidationRequestPropagator);
    }
}
