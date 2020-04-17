/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.caching.impl.clustering;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CachingConstants;
import org.wso2.carbon.caching.impl.DataHolder;
import org.wso2.carbon.caching.impl.Util;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.CacheEntryInfo;
import javax.cache.CacheInvalidationRequestSender;
import javax.cache.event.CacheEntryCreatedListener;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryRemovedListener;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * Listens for cache entry removals and updates, and sends a cache invalidation request
 * to the other members in the cluster.
 * <p>
 * This is feature intended only when separate local caches are maintained by each node
 * in the cluster.
 */
public class
ClusterCacheInvalidationRequestSender implements CacheEntryRemovedListener, CacheEntryUpdatedListener,
        CacheEntryCreatedListener, CacheInvalidationRequestSender {

    private static final Log log = LogFactory.getLog(ClusterCacheInvalidationRequestSender.class);

    @Override
    public void entryRemoved(CacheEntryEvent event) throws CacheEntryListenerException {

        send(Util.createCacheInfo(event));
    }

    @Override
    public void entryUpdated(CacheEntryEvent event) throws CacheEntryListenerException {

        send(Util.createCacheInfo(event));
    }

    @Override
    public void entryCreated(CacheEntryEvent event) throws CacheEntryListenerException {

        // We don't need this as only a delete or an update should invalidate the caches.
    }

    private ClusteringAgent getClusteringAgent() {

        return DataHolder.getInstance().getClusteringAgent();
    }

    @Override
    public void send(CacheEntryInfo cacheEntryInfo) {

        String tenantDomain = cacheEntryInfo.getTenantDomain();
        int tenantId = cacheEntryInfo.getTenantId();

        if (MultitenantConstants.INVALID_TENANT_ID == tenantId) {
            if (log.isDebugEnabled()) {
                String stackTrace = ExceptionUtils.getStackTrace(new Throwable());
                log.debug("Tenant information cannot be found in the request. This originated from: \n" + stackTrace);
            }
            return;
        }

        if (!cacheEntryInfo.getCacheName().startsWith(CachingConstants.LOCAL_CACHE_PREFIX) ||
                getClusteringAgent() == null) {
            return;
        }
        int numberOfRetries = 0;
        if (log.isDebugEnabled()) {
            log.debug("Sending cache invalidation message to other cluster nodes for '" + cacheEntryInfo.getCacheKey() +
                    "' of the cache '" + cacheEntryInfo.getCacheName() + "' of the cache manager '" +
                    cacheEntryInfo.getCacheManagerName() + "'");
        }

        //Send the cluster message
        ClusterCacheInvalidationRequest.CacheInfo cacheInfo =
                new ClusterCacheInvalidationRequest.CacheInfo(cacheEntryInfo.getCacheManagerName(), cacheEntryInfo.getCacheName(),
                        cacheEntryInfo.getCacheKey());

        ClusterCacheInvalidationRequest clusterCacheInvalidationRequest = new ClusterCacheInvalidationRequest(
                cacheInfo, tenantDomain, tenantId);

        while (numberOfRetries < 60) {
            try {
                getClusteringAgent().sendMessage(clusterCacheInvalidationRequest, true);
                log.debug("Sent [" + clusterCacheInvalidationRequest + "]");
                break;
            } catch (ClusteringFault e) {
                numberOfRetries++;
                if (numberOfRetries < 60) {
                    log.warn("Could not send CacheInvalidationMessage for tenant " +
                            tenantId + ". Retry will be attempted in 2s. Request: " +
                            clusterCacheInvalidationRequest, e);
                } else {
                    log.error("Could not send CacheInvalidationMessage for tenant " +
                            tenantId + ". Several retries failed. Request:" + clusterCacheInvalidationRequest, e);
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
