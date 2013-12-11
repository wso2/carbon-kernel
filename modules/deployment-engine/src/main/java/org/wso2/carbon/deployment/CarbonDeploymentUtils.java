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

package org.wso2.carbon.deployment;

import java.io.File;
import org.wso2.carbon.deployment.spi.Artifact;

public class CarbonDeploymentUtils {

    /**
     * Checks if a file has been modified by comparing the last update date of
     * both files and Artifact. If they are different, the file is assumed to have
     * been modified.
     *
     * @param artifact
     */
    public static boolean isArtifactModified(Artifact artifact) {
        long currentTimeStamp = artifact.getLastModifiedTime();

        setArtifactLastModifiedTime(artifact);

        return (currentTimeStamp != artifact.getLastModifiedTime());
    }

    /**
     * Sets the last modified time to the given artifact
     *
     * @param artifact
     */
    public static void setArtifactLastModifiedTime(Artifact artifact) {
        File file = artifact.getFile();
        if (file != null && (artifact.getLastModifiedTime() < file.lastModified())) {
            artifact.setLastModifiedTime(file.lastModified());
        }
    }
}
