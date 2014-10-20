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

import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

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

    private Util() {
    }
}
