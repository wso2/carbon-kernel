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

package org.wso2.carbon.registry.core;

/**
 * This represent a resource id. This keep the path which used to identify that resource in the
 * present state in a unique way. In addition to that it keep the more drilled down details of the
 * path, the path id and resource name. If the resource is a collection pathID = REG_PATH_ID(path)
 * name = null If the resource is not a collection pathId = REG_PATH_ID(parentPath(path)) name = the
 * name component of the path id so path = parentPath(path) + "/" + name component of the path.
 */
public class ResourceIDImpl {

    private String path;
    private int pathID;
    private String name;
    private boolean isCollection;

    /**
     * Returning the full path, i.e. parentPath + resourceName
     *
     * @return the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Setting the full path i.e. parentPath + resourceName
     *
     * @param path the path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Method to check whether the resource is a collection.
     *
     * @return true, if it is a collection, false otherwise.
     */
    public boolean isCollection() {
        return isCollection;
    }

    /**
     * Method to set whether the resource is a collection.
     *
     * @param collection whether this is a collection or not.
     */
    public void setCollection(boolean collection) {
        isCollection = collection;
    }

    /**
     * Method to get the path id, If the resource is a collection, pathID = REG_PATH_ID(path) name =
     * null If the resource is not a collection, pathId = REG_PATH_ID(parentPath(path))
     *
     * @return the path id.
     */
    public int getPathID() {
        return pathID;
    }

    /**
     * Method to set the path id.
     *
     * @param pathID the path id.
     */
    public void setPathID(int pathID) {
        this.pathID = pathID;
    }

    /**
     * Method to get the name.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Method to set the name.
     *
     * @param name the name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The string value of the resource ID = getPath();
     *
     * @return the string value of the resource id.
     */
    public String toString() {
        // logic to convert the id to string for the authorization purpose of UM
        return path;
    }
}
