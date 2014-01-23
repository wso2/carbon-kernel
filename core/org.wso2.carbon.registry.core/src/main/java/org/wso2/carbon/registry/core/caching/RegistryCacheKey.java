/*
 *  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.core.caching;


import java.io.Serializable;

/**
 * The container class for the cache key used in the registry kernel
 */
@SuppressWarnings("unused")
public class RegistryCacheKey implements Serializable{
    private static final long serialVersionUID = -5590538019841708811L;

    private int tenantId;
    private String path;
    private String connectionURL;

    /**
     * Creates a new key for an entry to be cached.
     * @param path          the resource path.
     * @param tenantId      the tenant identifier.
     * @param connectionURL the JDBC connection URL.
     */
    public RegistryCacheKey(String path, int tenantId, String connectionURL) {
        this.tenantId = tenantId;
        this.path = path;
        this.connectionURL = connectionURL;
    }

    /**
     * Method to obtain resource path on cache key.
     *
     * @return resource path on cache key.
     */
    public String getPath() {
        return path;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object key) {
        if (!(key instanceof RegistryCacheKey)) {
            return false;
        }
        RegistryCacheKey cacheKey = (RegistryCacheKey) key;

        return cacheKey.tenantId == tenantId &&
                (cacheKey.path == null && path == null ||
                        cacheKey.path != null && cacheKey.path.equals(path)) &&
                (cacheKey.connectionURL == null && connectionURL == null ||
                        cacheKey.connectionURL != null &&
                                cacheKey.connectionURL.equals(connectionURL));
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        int output = Integer.valueOf(tenantId).hashCode();
        if (path != null) {
            output += path.hashCode();
        }
        if (connectionURL != null) {
            output += connectionURL.hashCode();
        }
        return output;
    }
}

