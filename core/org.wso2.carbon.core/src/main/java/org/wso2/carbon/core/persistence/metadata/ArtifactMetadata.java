/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.core.persistence.metadata;

import java.io.File;
import java.util.Properties;

public class ArtifactMetadata {

    private File file;
    private Properties properties = new Properties();

    private String artifactName;
    private String artifactType;
    private String metaDirName;

    public ArtifactMetadata(String artifactName, ArtifactType artifactType, File metadataFile) {
        this.file = metadataFile;
        this.artifactName = artifactName;
        this.artifactType = artifactType.getArtifactType();
        this.metaDirName = artifactType.getMetadataDirName();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public String getArtifactType() {
        return artifactType;
    }

    public String getMetaDirName() {
        return metaDirName;
    }

    public void setMetaDirName(String metaDirName) {
        this.metaDirName = metaDirName;
    }
}
