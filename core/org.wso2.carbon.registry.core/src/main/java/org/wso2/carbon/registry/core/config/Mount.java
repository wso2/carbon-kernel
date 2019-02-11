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
package org.wso2.carbon.registry.core.config;

/**
 * This class is represents a mount defined in the registry.xml file. <br /> &lt;mount
 * path="/localPath"&gt; &lt;instanceId&gt;instanceId&lt;/instanceId&gt;
 * &lt;targetPath&gt;/remotePath&lt;/targetPath&gt; &lt;/mount&gt;
 */
public class Mount {

    private String path;
    private String instanceId;
    private String targetPath;
    private boolean overwrite;
    private boolean virtual;
    private boolean isExecuteQueryAllowed;

    /**
     * Method to obtain the local path of the mount.
     *
     * @return the local path of the mount.
     */
    public String getPath() {
        return path;
    }

    /**
     * Method to set the local path of the mount.
     *
     * @param path the local path of the mount.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Method to obtain the database configuration instance identifier used by the mount.
     *
     * @return the database configuration instance identifier used by the mount.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Method to set the database configuration instance identifier used by the mount.
     *
     * @param instanceId the database configuration instance identifier used by the mount.
     */
    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    /**
     * Method to obtain the remote path of the mount.
     *
     * @return the remote path of the mount.
     */
    public String getTargetPath() {
        return targetPath;
    }

    /**
     * Method to set the remote path of the mount.
     *
     * @param targetPath the remote path of the mount.
     */
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    /**
     * Method to obtain whether an existing resource/collection is overwritten when creating a new
     * mount.
     *
     * @return whether an existing resource/collection is overwritten when creating a new mount.
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * Method to set whether an existing resource/collection is overwritten when creating a new
     * mount.
     *
     * @param overwrite whether an existing resource/collection is overwritten when creating a new
     *                  mount.
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * Method to obtain whether the created mount is a virtual replacement to an existing physical
     * path.
     *
     * @return whether the created mount is a virtual replacement to an existing physical path.
     */
    public boolean isVirtual() {
        return virtual;
    }

    /**
     * Method to set whether the created mount is a virtual replacement to an existing physical
     * path.
     *
     * @param virtual whether the created mount is a virtual replacement to an existing physical
     *                path.
     */
    public void setVirtual(boolean virtual) {
        this.virtual = virtual;
    }

    /**
     * Method to get whether an execute queries are allowed to run on this mount
     *
     * @return whether an execute queries are allowed or not.
     */
    public boolean isExecuteQueryAllowed() {
        return isExecuteQueryAllowed;
    }

    /**
     * Method to set whether an execute queries are allowed to run on this mount
     *
     * @param isExecuteQueryAllowed whether an execute queries are allowed or not.
     */
    public void setExecuteQueryAllowed(boolean isExecuteQueryAllowed) {
        this.isExecuteQueryAllowed = isExecuteQueryAllowed;
    }
}
