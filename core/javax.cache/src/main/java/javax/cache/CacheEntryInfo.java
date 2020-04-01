/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package javax.cache;

import java.io.Serializable;
import java.util.UUID;

/**
 * Cache Information to Send to invalidate Cache
 */
public class CacheEntryInfo implements Serializable {

    private static final long serialVersionUID = 90L;
    private final String uuid = UUID.randomUUID().toString();
    private final long timestamp = System.currentTimeMillis();
    private String cacheManagerName;
    private String cacheName;
    private Object cacheKey;
    private String tenantDomain;
    private int tenantId;

    public CacheEntryInfo(String cacheManagerName, String cacheName, Object cacheKey, String tenantDomain, int tenantId) {

        this.cacheManagerName = cacheManagerName;
        this.cacheName = cacheName;
        this.cacheKey = cacheKey;
        this.tenantDomain = tenantDomain;
        this.tenantId = tenantId;
    }

    public String getCacheManagerName() {

        return cacheManagerName;
    }

    public void setCacheManagerName(String cacheManagerName) {

        this.cacheManagerName = cacheManagerName;
    }

    public String getCacheName() {

        return cacheName;
    }

    public void setCacheName(String cacheName) {

        this.cacheName = cacheName;
    }

    public Object getCacheKey() {

        return cacheKey;
    }

    public void setCacheKey(Object cacheKey) {

        this.cacheKey = cacheKey;
    }

    public String getTenantDomain() {

        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {

        this.tenantDomain = tenantDomain;
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public String getUuid() {

        return uuid;
    }

    public long getTimestamp() {

        return timestamp;
    }

    @Override
    public String toString() {

        return "CacheInfo{" +
                "uuid='" + uuid + '\'' +
                ", timestamp=" + timestamp +
                ", cacheManagerName='" + cacheManagerName + '\'' +
                ", cacheName='" + cacheName + '\'' +
                ", cacheKey=" + cacheKey +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", tenantId=" + tenantId +
                '}';
    }
}


