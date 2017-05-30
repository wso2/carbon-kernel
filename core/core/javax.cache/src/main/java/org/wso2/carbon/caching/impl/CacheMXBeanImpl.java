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

import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.cache.Cache;
import javax.cache.CacheStatistics;
import javax.cache.Status;
import javax.cache.mbeans.CacheMXBean;
import java.util.Date;

/**
 * TODO: class description
 */
public class CacheMXBeanImpl implements CacheMXBean {

    private final Cache cache;
    private String ownerTenantDomain;
    private int ownerTenantId;

    /**
     * Constructor
     *
     * @param cache             the cache
     * @param ownerTenantDomain ownerTenantDomain
     * @param ownerTenantId     ownerTenantId
     */
    public CacheMXBeanImpl(Cache cache, String ownerTenantDomain, int ownerTenantId) {
        this.cache = cache;
        this.ownerTenantDomain = ownerTenantDomain;
        this.ownerTenantId = ownerTenantId;
    }

    @Override
    public void clear() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            setTenantCredentialsInCarbonContext();
            getCacheStatistics().clear();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public Date getStartAccumulationDate() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            setTenantCredentialsInCarbonContext();
            return getCacheStatistics().getStartAccumulationDate();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public long getCacheHits() {
        return getCacheStatistics().getCacheHits();
    }

    @Override
    public float getCacheHitPercentage() {
        return getCacheStatistics().getCacheHitPercentage();
    }

    @Override
    public long getCacheMisses() {
        return getCacheStatistics().getCacheMisses();
    }

    @Override
    public float getCacheMissPercentage() {
        return getCacheStatistics().getCacheMissPercentage();
    }

    @Override
    public long getCacheGets() {
        return getCacheStatistics().getCacheGets();
    }

    @Override
    public long getCachePuts() {
        return getCacheStatistics().getCachePuts();
    }

    @Override
    public long getCacheRemovals() {
        return getCacheStatistics().getCacheRemovals();
    }

    @Override
    public long getCacheEvictions() {
        return getCacheStatistics().getCacheEvictions();
    }

    @Override
    public float getAverageGetMillis() {
        return getCacheStatistics().getAverageGetMillis();
    }

    @Override
    public float getAveragePutMillis() {
        return getCacheStatistics().getAveragePutMillis();
    }

    @Override
    public float getAverageRemoveMillis() {
        return getCacheStatistics().getAverageRemoveMillis();
    }

    private CacheStatistics getCacheStatistics() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            setTenantCredentialsInCarbonContext();
            return cache.getStatistics();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public String getName() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            setTenantCredentialsInCarbonContext();
            return cache.getName();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public Status getStatus() {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            setTenantCredentialsInCarbonContext();
            return cache.getStatus();
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private void setTenantCredentialsInCarbonContext() {
        PrivilegedCarbonContext cc = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        cc.setTenantId(ownerTenantId);
        cc.setTenantDomain(ownerTenantDomain);
    }
}
