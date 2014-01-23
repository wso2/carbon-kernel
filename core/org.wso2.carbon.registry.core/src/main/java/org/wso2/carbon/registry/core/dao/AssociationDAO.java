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

package org.wso2.carbon.registry.core.dao;

import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Data Access Object for Associations
 */
public interface AssociationDAO {

    /**
     * Method to persist an association.
     *
     * @param sourcePath      the source path
     * @param targetPath      the target path
     * @param associationType the type of the association
     *
     * @throws RegistryException if some error occurs while adding associations
     */
    void addAssociation(String sourcePath,
                               String targetPath,
                               String associationType) throws RegistryException;

    /**
     * Method to remove an association.
     *
     * @param sourcePath      the source path
     * @param targetPath      the target path
     * @param associationType the type of the association
     *
     * @throws RegistryException if some error occurs while removing associations
     */
    void removeAssociation(String sourcePath,
                                  String targetPath,
                                  String associationType) throws RegistryException;

    /**
     * Method to get all association.
     *
     * @param resourcePath the source path
     *
     * @return the array of all associations
     * @throws RegistryException if some error occurs while getting all associations
     */
    Association[] getAllAssociations(String resourcePath) throws RegistryException;

    /**
     * Method to get all association of a given type.
     *
     * @param resourcePath    the source path
     * @param associationType the type of the association
     *
     * @return the array of associations of a given type
     * @throws RegistryException if some error occurs while getting associations of a given type
     */
    Association[] getAllAssociationsForType(String resourcePath, String associationType)
            throws RegistryException;

    /**
     * Method to replace all associations, when moving or renaming a resource. All the associations
     * towards a given path can be replaced by the new path by calling this method.
     *
     * @param oldPath the old resource path
     * @param newPath the new resource path
     *
     * @throws RegistryException if an error occurs while
     */
    void replaceAssociations(String oldPath, String newPath) throws RegistryException;

    /**
     * Method to remove all associations for a given path.
     *
     * @param resourcePath the source path
     *
     * @throws RegistryException if an error occurs while
     */
    void removeAllAssociations(String resourcePath) throws RegistryException;

    /**
     * Method to copy associations for a given path to a new path.
     *
     * @param fromPath the source path
     * @param toPath   the destination path
     *
     * @throws RegistryException if an error occurs while
     */
    void copyAssociations(String fromPath, String toPath) throws RegistryException;
}
