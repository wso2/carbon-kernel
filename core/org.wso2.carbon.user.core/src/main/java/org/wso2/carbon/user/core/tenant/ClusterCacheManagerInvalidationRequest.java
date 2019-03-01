/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.user.core.tenant;

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.caching.impl.CacheManagerFactoryImpl;

import javax.cache.Caching;

/**
 * This is the cluster-wide cache manager invalidation message that is sent
 * to all the other nodes in a cluster. This invalidates its own cache manager after deleting a tenant.
 *
 * This is based on Axis2 clustering.
 *
 */
public class ClusterCacheManagerInvalidationRequest extends ClusteringMessage {

    private static final transient Log log = LogFactory.getLog(ClusterCacheManagerInvalidationRequest.class);
    private static final long serialVersionUID = -8099468509378589084L;

    private String tenantDomain;
    private int tenantId;

    public ClusterCacheManagerInvalidationRequest( String tenantDomain, int tenantId) {

        this.tenantDomain = tenantDomain;
        this.tenantId = tenantId;
    }

    @Override
    public void execute(ConfigurationContext configurationContext) throws ClusteringFault {

        if (log.isDebugEnabled()) {
            log.debug("Received [" + this + "] ");
        }
        CacheManagerFactoryImpl cacheManagerFactory = (CacheManagerFactoryImpl) Caching.getCacheManagerFactory();
        cacheManagerFactory.removeCacheManagerMap(tenantDomain);
    }

    @Override
    public String toString() {

        return "ClusterCacheManagerInvalidationRequest{" +
                "tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", messageId=" + getUuid() + +
                '}';
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }


}
