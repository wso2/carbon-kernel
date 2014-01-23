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

package org.wso2.carbon.registry.core.utils;

import org.wso2.carbon.registry.core.RegistryConstants;

/**
 * This class is used to represent a path with a version (a versioned path).
 */
public class VersionedPath {

    private String path;

    private long version;

    /**
     * Method to obtain the path.
     *
     * @return the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Method to set the path.
     *
     * @param path the path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Method to obtain the version.
     *
     * @return the version.
     */
    public long getVersion() {
        return version;
    }

    /**
     * Method to set the version.
     *
     * @param version the version.
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * This method overrides the default toString method in manner that it returns the versioned
     * path when called.
     *
     * @return the versioned path.
     */
    public String toString() {
        return path + RegistryConstants.URL_SEPARATOR + "version:" + version;
    }
}
