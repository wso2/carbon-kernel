/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.deployment.spi;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provide an abstraction level for the concept "artifact" in carbon.
 * <p/>
 * An artifact can be considered as a thing to deploy in carbon.
 * Eg: webapp, service, jaggery-app, proxy-service, sequence, api, endpoint
 * <p/>
 * Each artifact is associated with a deployer, which can process and deploy it to the relevant
 * runtime configuration.
 * <p/>
 * An artifact will have a unique identifier (key), which can be used for artifact identification
 * within a runtime.
 * <p/>
 * An artifact can have custom properties that are needed for relevant runtime environment.
 */
public class Artifact {

    /**
     * The file associated with the artifact instance
     */
    private File file;

    /**
     * Keeps track of the last modified time of this artifact
     */
    private long lastModifiedTime;

    /**
     * Version of the artifact
     */
    private String version;

    /**
     * A key to uniquely identify an Artifact within a runtime
     */
    private Object key;

    /**
     * Deployment directory that the artifact is associated with  Eg : webapps, sequences, apis, etc
     */
    private String directory;

    /**
     * To keep set of custom properties related to this artifact
     */
    private Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Default constructor which takes the associated file with this artifact instance
     *
     * @param file the associated file
     */
    public Artifact(File file) {
        this.file = file;
    }

    /**
     * Returns a meaningful name for this artifact.
     *
     * @return file name
     */
    public String getName() {
        return file.getName();
    }

    /**
     * A key is used to uniquely identify an Artifact within a runtime
     *
     * @return key
     */
    public Object getKey() {
        return key;
    }

    /**
     * Sets a given key to this artifact instance
     *
     * @param key key
     */
    public void setKey(Object key) {
        this.key = key;
    }
    /**
     * Path of the file associated with this artifact
     *
     * @return path
     */
    public String getPath() {
        return file.getPath();
    }

    /**
     * The file associated with the artifact
     *
     * @return file
     */
    public File getFile() {
        return file;
    }

    /**
     * Version of the artifact
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets a given version to this artifact instance
     *
     * @param version version to be set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Type of the artifact
     * Eg : war, aar, dbs
     *
     * @return artifact directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Sets the given directory for this artifact instance
     *
     * @param type directory of the artifact to be set
     */
    public void setDirectory(String type) {
        this.directory = type;
    }

    /**
     * This will return the last modified time of this artifact
     *
     * @return last modified time
     */
    public long getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Sets the last modified time of this artifact
     *
     * @param lastModifiedTime lastModifiedTime
     */
    public void setLastModifiedTime(long lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    /**
     * This will return the Map of custom properties for this artifact
     *
     * @return custom properties map
     */
    public Map<String, Object> getProperties() {
        return this.properties;
    }

    /**
     * This method allows to set custom properties which are needed for this Artifact
     *
     * @param properties map of custom properties
     */
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
