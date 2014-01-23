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

package org.wso2.carbon.caching.core.realm;

import org.wso2.carbon.caching.core.BaseCache;
import org.wso2.carbon.user.api.UserRealm;

public class RealmCache extends BaseCache {

    public static final String CUSTOM_TENANT_CACHE = "CUSTOM_TENANT_CACHE";

    private static RealmCache tenantCache = null;

    private RealmCache() {
        super(CUSTOM_TENANT_CACHE);
    }

    /**
     * Gets a new instance of TenantCache.
     * 
     * @return A new instance of TenantCache.
     */
    public synchronized static RealmCache getInstance() {
        if (tenantCache == null) {
            tenantCache = new RealmCache();
        }
        return tenantCache;
    }

    public UserRealm getUserRealm(int tenantId, String realmName) {
        RealmCacheKey key = new RealmCacheKey(tenantId, realmName);
        RealmCacheEntry entry = (RealmCacheEntry) tenantCache.getValueFromCache(key);
        if (entry != null) {
            return entry.getUserRealm();
        } else {
            return null;
        }
    }

    public void addToCache(int tenantId, String realmName, UserRealm userRealm) {
        tenantCache.addToCache(new RealmCacheKey(tenantId, realmName),
                               new RealmCacheEntry(userRealm));
    }

    /**
     * Clear the cache entry
     *
     * @param tenantId
     * @param realmName
     */
    public void clearFromCache(int tenantId, String realmName) {
        RealmCacheKey key = new RealmCacheKey(tenantId, realmName);
        RealmCacheEntry entry = (RealmCacheEntry) tenantCache.getValueFromCache(key);
        if (entry != null) {
            tenantCache.clearCacheEntry(key);
        }
    }
}
