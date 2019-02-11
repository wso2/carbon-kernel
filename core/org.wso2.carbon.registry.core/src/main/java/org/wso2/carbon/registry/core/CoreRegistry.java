/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.registry.core;

import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * CoreRegistry is the central get/put API for the Registry.  This is typically what you want if
 * you're a Java programmer wanting to simply store and manage Resources.  Since it's an interface,
 * you don't care if the results come from (say) an embedded file-based Registry or a remote one -
 * that decision (and the resulting cache dynamics, exception throwing, etc) can be made by a
 * factory or dependency injection.
 * <p/>
 * If you want programmatic access to features like tags/comments/ratings/versions, please have a
 * look at the full Registry interface which extends this one.
 */
public interface CoreRegistry extends org.wso2.carbon.registry.api.CoreRegistry {
    /**
     * Creates a new resource.
     *
     * @return the created resource.
     * @throws RegistryException if the operation failed.
     */
    Resource newResource() throws RegistryException;

    /**
     * Creates a new collection.
     *
     * @return the created collection.
     * @throws RegistryException if the operation failed.
     */
    Collection newCollection() throws RegistryException;

    /**
     * Returns the resource at the given path.
     *
     * @param path Path of the resource. e.g. /project1/server/deployment.xml
     *
     * @return Resource instance
     * @throws RegistryException is thrown if the resource is not in the registry
     */
    Resource get(String path) throws RegistryException;

    /**
     * Returns the Collection at the given path, with the content paginated according to the
     * arguments.
     *
     * @param path     the path of the collection.  MUST point to a collection!
     * @param start    the initial index of the child to return.  If there are fewer children than
     *                 the specified value, a RegistryException will be thrown.
     * @param pageSize the maximum number of results to return
     *
     * @return a Collection containing the specified results in the content
     * @throws RegistryException if the resource is not found, or if the path does not reference a
     *                           Collection, or if the start index is greater than the number of
     *                           children.
     */
    Collection get(String path, int start, int pageSize) throws RegistryException;

    /**
     * Check whether a resource exists at the given path
     *
     * @param path Path of the resource to be checked
     *
     * @return true if a resource exists at the given path, false otherwise.
     * @throws RegistryException if an error occurs
     */
    boolean resourceExists(String path) throws RegistryException;

    /**
     * Adds or updates resources in the registry. If there is no resource at the given path,
     * resource is added. If a resource already exist at the given path, it will be replaced with
     * the new resource.
     *
     * @param suggestedPath the path which we'd like to use for the new resource.
     * @param resource      Resource instance for the new resource
     *
     * @return @return the actual path that the server chose to use for our Resource, When an
     * handler is used for the put operation the actual path needs to be set properly within the handler
     * @throws RegistryException is thrown depending on the implementation.
     */
    String put(String suggestedPath, Resource resource) throws RegistryException;

    /**
     * Deletes the resource at the given path. If the path refers to a directory, all child
     * resources of the directory will also be deleted.
     *
     * @param path Path of the resource to be deleted.
     *
     * @throws RegistryException is thrown depending on the implementation.
     */
    void delete(String path) throws RegistryException;
}
