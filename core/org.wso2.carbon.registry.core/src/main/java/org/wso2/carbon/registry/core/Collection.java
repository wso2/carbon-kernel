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
 * The Collection Interface. Collection is specific type of resource that can contain other
 * resources (including other collections). We call the resource contained in a collection as the
 * children of the collection and the collection is called the parent of its children. The path of
 * the child = The path of the parent + RegistryConstant.SEPERATOR + The resource name of the child.
 * The ROOT collection is a specific instance of the Collection interface which doesn't have a
 * parent.
 */
public interface Collection extends Resource, org.wso2.carbon.registry.api.Collection {
//public interface Collection extends org.wso2.carbon.registry.api.Collection {

    @Deprecated
    @SuppressWarnings("unused")
    static final String ALLOW_ALL = "ALLOW_ALL";
    @Deprecated
    @SuppressWarnings("unused")
    static final String ALLOW_SELECTED = "ALLOW_SELECTED";
    @Deprecated
    @SuppressWarnings("unused")
    static final String DENY_SELECTED = "DENY_SELECTED";

    /**
     * Method to return the absolute paths of the children of the collection
     *
     * @return the array of absolute paths of the children
     * @throws RegistryException if the operation fails.
     */
    String[] getChildren() throws RegistryException;

    /**
     * Method to return the paths of the selected range of children.
     *
     * @param start   the starting number of children.
     * @param pageLen the number of entries to retrieve.
     *
     * @return an array of paths of the selected range of children.
     * @throws RegistryException if the operation fails.
     */
    String[] getChildren(int start, int pageLen) throws RegistryException;

    /**
     * Method to return the the number of children.
     *
     * @return the number of children.
     * @throws RegistryException if the operation fails.
     */
    int getChildCount() throws RegistryException;

    /**
     * Method to set the absolute paths of the children belonging to this collection. Absolute paths
     * begin from the ROOT collection.
     *
     * @param paths the array of absolute paths of the children
     *
     * @throws RegistryException if the operation fails.
     */
    void setChildren(String[] paths) throws RegistryException;
}
