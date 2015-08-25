/*
 * Copyright 2009-2010 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.maven.p2.generate.feature;

import org.apache.maven.artifact.Artifact;

public class IncludedFeature {

    /**
     * Group Id of the Bundle
     *
     * @parameter
     * @required
     */
    private String groupId;

    /**
     * Artifact Id of the Bundle
     *
     * @parameter
     * @required
     */
    private String artifactId;

    /**
     * Version of the Bundle
     *
     * @parameter default-value=""
     */
    private String artifactVersion;

    /**
     * Optionality of the included feature
     *
     * @parameter default-value=""
     */
    private String optionality;

    private boolean optional = false;

    private String featureID;

    private String featureVersion;

    private Artifact artifact;

    public static IncludedFeature getIncludedFeature(String definition) {
        IncludedFeature feature;
        String segment;
        String[] segments = definition.split(":");

        if (segments.length >= 2) {
            feature = new IncludedFeature();
            feature.groupId = segments[0];
            feature.artifactId = segments[1];
            if (segments[1].endsWith(".feature")) {
                feature.featureID = segments[1].substring(0, segments[1].lastIndexOf(".feature"));
            }
        } else {
            return null;
        }

        if (segments.length >= 3) {
            segment = segments[2];
            if ("optional".equals(segment)) {
                feature.optional = true;
            } else {
                feature.artifactVersion = segment;
                feature.featureVersion = Bundle.getOSGIVersion(segment);
            }
        }

        if (segments.length == 4) {
            segment = segments[3];
            if ("optional".equals(segment))
                feature.optional = true;
        }

        return feature;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getArtifactVersion() {
        return artifactVersion;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public String getFeatureID() {
        return featureID;
    }

    public String getFeatureVersion() {
        return featureVersion;
    }

    public void setFeatureVersion(String version) {
        if (artifactVersion == null || artifactVersion.equals("")) {
            artifactVersion = version;
            featureVersion = Bundle.getOSGIVersion(version);
        }
    }
}
