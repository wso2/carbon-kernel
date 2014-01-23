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
package org.wso2.carbon.caching.core.registry;

import java.io.Serializable;

/**
 * This class contains a reference to a resource that will be dynamically populated into the cache.
 */
@SuppressWarnings("unused")
public class GhostResource<T> implements Serializable {
    private static final long serialVersionUID = -2953483852512559586L;

    private transient T resource;

    /**
     * Creates a new entry to be cached.
     *
     * @param resource the encapsulated resource.
     */
    public GhostResource(T resource) {
        this.resource = resource;
    }

    /**
     * Method to obtain the encapsulated resource.
     *
     * @return the encapsulated resource.
     */
    public T getResource() {
        return resource;
    }

    /**
     * Method to change the encapsulated resource.
     *
     * @param resource the encapsulated resource.
     */
    public void setResource(T resource) {
        this.resource = resource;
    }
}
