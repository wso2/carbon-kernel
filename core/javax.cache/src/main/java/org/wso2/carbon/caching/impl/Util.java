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

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.caching.impl.clustering.ClusterCacheInvalidationRequestSender;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.Map;

import javax.cache.CacheEntryInfo;
import javax.cache.CacheInvalidationRequestSender;
import javax.cache.event.CacheEntryEvent;

/**
 * TODO: class description
 */
public final class Util {

    public static void checkAccess(String ownerTenantDomain, int ownerTenantId) {
        CarbonContext carbonContext = getCarbonContext();
        String callerTenantDomain = carbonContext.getTenantDomain();
        if (callerTenantDomain == null) {
            throw new IllegalStateException("Caller tenant domain cannot be null");
        }
        int callerTenantId = carbonContext.getTenantId();
        if (callerTenantId == MultitenantConstants.INVALID_TENANT_ID) {
            throw new IllegalStateException("Caller Tenant ID cannot be " + MultitenantConstants.INVALID_TENANT_ID);
        }

        if (callerTenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME) &&
            callerTenantId == MultitenantConstants.SUPER_TENANT_ID) {
            return;
        }

        if (!callerTenantDomain.equals(ownerTenantDomain) || callerTenantId != ownerTenantId) {
            throw new SecurityException("Illegal access attempt to cache " +
                                        "] owned by tenant {[" + ownerTenantDomain + "],[" +
                                        ownerTenantId + "]} by tenant {[" + callerTenantDomain +
                                        "],[" + callerTenantId + "]}");
        }
    }

    private static CarbonContext getCarbonContext() {
        CarbonContext carbonContext = CarbonContext.getThreadLocalCarbonContext();
        if (carbonContext == null) {
            throw new IllegalStateException("CarbonContext cannot be null");
        }
        return carbonContext;
    }

    /**
     * Get map name of a cache in the distributed map provider
     * @param cacheName name of the cache
     * @param ownerTenantDomain owner tenant domain of the cache manager
     * @param cacheManagerName name of the cache manager
     * @return the distributed map name
     */
    public static String getDistributedMapNameOfCache(String cacheName, String ownerTenantDomain,
                                                      String cacheManagerName) {
        return "$cache.$domain[" + ownerTenantDomain + "]" +
                cacheManagerName + "#" + cacheName;
    }

    public static String getTenantDomain() {
        return getCarbonContext().getTenantDomain();
    }

    /**
     * Return the default cache timeout value (Mins) specified in Carbon.xml
     *
     * @return long
     */
    public static long getDefaultCacheTimeout() {
        ServerConfigurationService serverConfigService = DataHolder.getInstance().getServerConfigurationService();
        if (serverConfigService != null) {
            String defaultCacheTimeoutValue = serverConfigService.getFirstProperty("Cache.DefaultCacheTimeout");
            return defaultCacheTimeoutValue == null ? CachingConstants.DEFAULT_CACHE_EXPIRY_MINS :
                    Long.parseLong(defaultCacheTimeoutValue);
        }
        return CachingConstants.DEFAULT_CACHE_EXPIRY_MINS;
    }

    /**
     * Return the default realm cache timeout value (Mins) specified in Carbon.xml.
     *
     * @return default realm cache timeout in mins.
     */
    public static long getDefaultRealmCacheTimeout() {

        ServerConfigurationService serverConfigService = DataHolder.getInstance().getServerConfigurationService();
        if (serverConfigService != null) {
            String defaultCacheTimeoutValue = serverConfigService.getFirstProperty("Cache.DefaultRealmCacheTimeout");
            return StringUtils.isEmpty(defaultCacheTimeoutValue) ? CachingConstants.DEFAULT_REALM_CACHE_EXPIRY_MINS :
                    Long.parseLong(defaultCacheTimeoutValue);
        }
        return CachingConstants.DEFAULT_REALM_CACHE_EXPIRY_MINS;
    }

    /**
     * Returnn the CacheInfo Object from CacheEntry
     * @param cacheEntryEvent CacheEntryEvent
     * @return CacheInfo
     */
    public static CacheEntryInfo createCacheInfo(CacheEntryEvent cacheEntryEvent) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        return new CacheEntryInfo(cacheEntryEvent.getSource().getCacheManager().getName(),
                cacheEntryEvent.getSource().getName(), cacheEntryEvent.getKey(), tenantDomain, tenantId);
    }

    /**
     * Return CacheInvalidationRequestSender Configured
     * @return CacheInvalidationRequestSender implementation
     */
    public static CacheInvalidationRequestSender getCacheInvalidationRequestSender() {

        ServerConfigurationService serverConfigService = DataHolder.getInstance().getServerConfigurationService();
        Map<String, CacheInvalidationRequestSender> cacheInvalidationRequestSenders =
                DataHolder.getInstance().getCacheInvalidationRequestSenders();
        if (serverConfigService != null) {
            String cacheInvalidation = serverConfigService.getFirstProperty(CachingConstants.CACHE_INVALIDATION_IMPL);
            if (StringUtils.isNotEmpty(cacheInvalidation) &&
                    cacheInvalidationRequestSenders.containsKey(cacheInvalidation)) {
                return DataHolder.getInstance().getCacheInvalidationRequestSenders().get(cacheInvalidation);
            }
        }
        CacheInvalidationRequestSender cacheInvalidationRequestSender =
                cacheInvalidationRequestSenders.get(CachingConstants.DEFAULT_CACHE_INVALIDATION_CLASS);
        if (cacheInvalidationRequestSender != null) {
            return cacheInvalidationRequestSender;
        }
        return new ClusterCacheInvalidationRequestSender();
    }
    private Util() {
    }
}
