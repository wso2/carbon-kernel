/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.internal.deployment;

import org.wso2.carbon.deployment.Artifact;

import java.io.File;

/**
 * A utility class for handling deployment engine related tasks such as check for artifact modification, etc.
 *
 */
public class Utils {

    /**
     * Checks if a file has been modified by comparing the last update date of
     * both files and Artifact. If they are different, the file is assumed to have
     * been modified.
     *
     * @param artifact artifact to check for modification
     * @return boolean value of artifact modified or not
     */
    public static boolean isArtifactModified(Artifact artifact) {
        long currentTimeStamp = artifact.getLastModifiedTime();

        setArtifactLastModifiedTime(artifact);

        return (currentTimeStamp != artifact.getLastModifiedTime());
    }

    /**
     * Sets the last modified time to the given artifact
     *
     * @param artifact artifact for update modified time
     */
    public static void setArtifactLastModifiedTime(Artifact artifact) {
        File file = artifact.getFile();
        if (file != null && (artifact.getLastModifiedTime() < file.lastModified())) {
            artifact.setLastModifiedTime(file.lastModified());
        }
    }

    /**
     * Request: file:sample.war
     * Response: file:/user/wso2carbon-kernel-5.0.0/repository/deployment/server/webapps/sample.war
     *
     * @param path       file path to resolve
     * @param parentPath parent file path of the file
     * @return file with resolved path
     */
    public static File resolveFileURL(String path, String parentPath) {
        File file;
        if (path.contains(":") && !path.startsWith("file:")) {
            throw new RuntimeException("URLs other than file URLs are not supported.");
        }
        String relativeFilePath = path;
        if (path.startsWith("file:")) {
            relativeFilePath = path.substring(5);
        }

        file = new File(relativeFilePath);
        if (!file.isAbsolute()) {
            file = new File(parentPath, relativeFilePath);
            if (!file.isAbsolute()) {
                throw new RuntimeException("Malformed URL : " + path);
            }
        }
        return file;
    }
}
