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
package org.wso2.carbon.caching.impl.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.caching.impl.CachingAxisConfigurationObserver;
import org.wso2.carbon.caching.impl.DataHolder;
import org.wso2.carbon.caching.impl.DistributedMapProvider;
import org.wso2.carbon.caching.impl.clustering.ClusterCacheInvalidationRequestSender;
import org.wso2.carbon.utils.Axis2ConfigurationContextObserver;
import org.wso2.carbon.utils.ConfigurationContextService;

import javax.cache.CacheInvalidationRequestSender;
import javax.cache.event.CacheEntryListener;

@Component(name = "org.wso2.carbon.caching.impl.internal.CachingServiceComponent", immediate = true)
public class CachingServiceComponent {
    private static final Log log = LogFactory.getLog(CachingServiceComponent.class);
    private DataHolder dataHolder = DataHolder.getInstance();

    @Activate
    protected void activate(ComponentContext ctx) {
       if(log.isDebugEnabled()){
           log.debug("CachingServiceComponent activated");
       }
        //register service for CachingAxisConfigurationObserver
        BundleContext bundleContext = ctx.getBundleContext();
        CachingAxisConfigurationObserver cachingAxisConfigurationObserver =
                new CachingAxisConfigurationObserver();
        bundleContext.registerService(Axis2ConfigurationContextObserver.class.getName(),
                cachingAxisConfigurationObserver, null);
        ClusterCacheInvalidationRequestSender clusterCacheInvalidationRequestSender =
                new ClusterCacheInvalidationRequestSender();
        bundleContext.registerService(CacheEntryListener.class.getName(),clusterCacheInvalidationRequestSender,null);
        bundleContext.registerService(CacheInvalidationRequestSender.class.getName(),
                clusterCacheInvalidationRequestSender,null);
    }

    @Deactivate
    protected void deactivate(ComponentContext ctx) {
    }

    @Reference(name = "distributedMapProvider", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetDistributedMapProvider")
    protected void setDistributedMapProvider(DistributedMapProvider mapProvider) {
        dataHolder.setDistributedMapProvider(mapProvider);
    }

    protected void unsetDistributedMapProvider(DistributedMapProvider mapProvider) {
        dataHolder.setDistributedMapProvider(null);
    }

    @Reference(name = "server.configuration.service", cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetServerConfigurationService")
    protected void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        dataHolder.setServerConfigurationService(serverConfigurationService);
    }

    protected void unsetServerConfigurationService(ServerConfigurationService serverConfigurationService) {
        dataHolder.setServerConfigurationService(null);
    }

    @Reference(name = "config.context.service", cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, 
            unbind = "unsetClusteringAgent") 
    protected void setClusteringAgent(ConfigurationContextService configurationContextService) {
        dataHolder.setClusteringAgent(configurationContextService.getServerConfigContext().getAxisConfiguration().
                getClusteringAgent());
    }

    protected void unsetClusteringAgent(ConfigurationContextService configurationContextService) {
        dataHolder.setClusteringAgent(null);
    }

    @Reference(name = "cache.entry.listener.service", cardinality = ReferenceCardinality.MULTIPLE, policy =
            ReferencePolicy.DYNAMIC, unbind = "removeCacheEntryListener")
    protected void addCacheEntryListener(CacheEntryListener cacheEntryListener) {
        dataHolder.getCacheEntryListeners().add(cacheEntryListener);
    }

    protected void removeCacheEntryListener(CacheEntryListener cacheEntryListener) {
        dataHolder.getCacheEntryListeners().remove(cacheEntryListener);
    }

    @Reference(name = "cache.invalidation.request.sender.service", cardinality = ReferenceCardinality.MULTIPLE, policy =
            ReferencePolicy.DYNAMIC, unbind = "removeCacheInvalidationRequestSender")
    protected void addCacheInvalidationRequestSender(CacheInvalidationRequestSender cacheInvalidationRequestSender) {

        dataHolder.addCacheInvalidationRequestSender(cacheInvalidationRequestSender.getClass().getName(),
                cacheInvalidationRequestSender);
    }

    protected void removeCacheInvalidationRequestSender(CacheInvalidationRequestSender cacheInvalidationRequestSender) {

        dataHolder.removeCacheInvalidationRequestSender(cacheInvalidationRequestSender.getClass().getName());
    }
}
