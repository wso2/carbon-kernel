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
package org.wso2.carbon.registry.core.jdbc;

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceIDImpl;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.dataobjects.ResourceDO;

import java.io.Reader;
import java.io.Writer;

/**
 * This is used to handle recursive repository operations that might need calling registry methods
 * for non-recursive resources.
 */
public class RecursionRepository {

    private Repository repository;
    private Registry registry;

    /**
     * Constructor accepting both registry and repository.
     *
     * @param repository the repository
     * @param registry   the registry
     */
    @SuppressWarnings("unused")
    public RecursionRepository(Repository repository, Registry registry) {
        this.repository = repository;
        this.registry = registry;
    }

    /**
     * Constructor accepting registry.
     *
     * @param registry the registry
     */
    public RecursionRepository(Registry registry) {
        this.registry = registry;
    }

    /**
     * Constructor accepting repository.
     *
     * @param repository the repository
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    // Method to determine whether the given path has a resource which can't be subjected to a
    // recursive operation.
    private boolean isNonRecursive(String path) throws RegistryException {
        Resource source = repository.get(path);
        if (source != null) {
            String isNonRecursive = source.getProperty(RegistryConstants.REGISTRY_NON_RECURSIVE);
                return isNonRecursive != null && Boolean.toString(true).equals(isNonRecursive);
        } else {
            return false;
        }

    }

    /**
     * Method to do a copy from source to target.
     *
     * @param sourceResourcePath the source path.
     * @param targetResourcePath the target path.
     *
     * @return the target path.
     * @throws RegistryException if the operation failed.
     */
    public String copy(ResourcePath sourceResourcePath,
                       ResourcePath targetResourcePath)
            throws RegistryException {
        if (isNonRecursive(sourceResourcePath.getPath())) {
            return registry.copy(sourceResourcePath.getPath(), targetResourcePath.getPath());
        } else {
            return repository.copy(sourceResourcePath, targetResourcePath);
        }
    }

    /**
     * Method to do a recursive move operation.
     *
     * @param sourceID               the source resource's identifier.
     * @param targetPath             the target resource path.
     * @param targetParentResourceID the target resource's parent's identifier.
     *
     * @return the target path.
     * @throws RegistryException if the operation failed.
     */
    public String moveRecursively(ResourceIDImpl sourceID,
                                  String targetPath,
                                  ResourceIDImpl targetParentResourceID) throws RegistryException {
        if (isNonRecursive(sourceID.getPath())) {
            return registry.move(sourceID.getPath(), targetPath);
        } else if (sourceID.isCollection()) {
            return repository.moveRecursively(sourceID, targetPath, targetParentResourceID);
        }
        return null;
    }

    /**
     * Method to delete a sub tree of the collection hierarchy.
     *
     * @param resourceID            the resource identifier.
     * @param resourceDO            the resource data object.
     * @param keepAuthorization whether to keep authorizations.
     * 
     * @throws RegistryException if the operation failed.
     */
    public void deleteSubTree(ResourceIDImpl resourceID,
                              ResourceDO resourceDO,
                              boolean keepAuthorization) throws RegistryException {
        if (isNonRecursive(resourceID.getPath())) {
            registry.delete(resourceID.getPath());
        } else {
            repository.deleteSubTree(resourceID, resourceDO, keepAuthorization);
        }
    }

    /**
     * Method to dump a tree of resources recursively.
     *
     * @param path   the path to dump
     * @param writer the xml writer the dump should be written to.
     *
     * @throws RegistryException if the operation failed.
     */
    public void dumpRecursively(String path,
                                Writer writer) throws RegistryException {
        if (isNonRecursive(path)) {
            registry.dump(path, writer);
        } else {
            repository.dump(path, writer);
        }
    }

    /**
     * Method to restore a tree of resource recursively.
     *
     * @param path   the path to dump
     * @param reader the xml writer the dump should be read from.
     *
     * @throws RegistryException if the operation failed.
     */
    public void restoreRecursively(String path,
                                   Reader reader) throws RegistryException {
        if (isNonRecursive(path)) {
            registry.restore(path, reader);
        } else {
            repository.restore(path, reader);
        }
    }
}
