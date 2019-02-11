/*
* Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package org.wso2.carbon.registry.app;


import org.wso2.carbon.registry.core.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation to cache resources along with an Entity Tag.
 */
public class CachedResources {

    private Map<String, Resource> resources;
    private Map<String, String> eTags;

    /**
     * Default constructor which initializes cache.
     */
    public CachedResources() {
        resources = new HashMap<String, Resource>();
        eTags = new HashMap<String, String>();
    }

    /**
     * Method to determine whether the resource at the given path is cached.
     *
     * @param path the resource path.
     *
     * @return whether cached or not.
     */
    public boolean isResourceCached(String path) {
        return !resources.isEmpty() && resources.containsKey(path);
    }

    /**
     * Method to cache a given resource along with an Entity Tag.
     *
     * @param path     the resource path.
     * @param resource the resource to cache.
     * @param eTag     the entity tag.
     * @param size     the maximum cache size.
     *
     * @return whether the operation succeeded or not.
     */
    public boolean cacheResource(String path, Resource resource, String eTag, long size) {
        if (resources.get(path) != null && getSizeOfCache() <= size) {
            resources.put(path, resource);
            eTags.put(path, eTag);
            return true;
        }
        return false;
    }

    /**
     * Method to fetch a cached resource.
     *
     * @param path the resource path.
     *
     * @return the cached resource if it is cached or null if not.
     */
    public Resource getCachedResource(String path) {
        return resources.get(path);
    }

    /**
     * Returns the Entity Tag corresponding to the given path.
     *
     * @param path the resource path.
     *
     * @return the Entity Tag.
     */
    public String getETag(String path) {
        return eTags.get(path);
    }

    /**
     * Method to obtain the size of the cache.
     *
     * @return the size of the cache.
     */
    public int getSizeOfCache() {
        return resources.size();
    }
}
