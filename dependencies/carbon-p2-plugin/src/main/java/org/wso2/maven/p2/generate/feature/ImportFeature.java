package org.wso2.maven.p2.generate.feature;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.wso2.maven.p2.generate.utils.P2Utils;

/**
 * TODO: class level comment
 */
public class ImportFeature {

    /**
     * Feature Id of the feature
     *
     * @parameter
     */

    private String featureId;

    /**
     * Version of the feature
     *
     * @parameter default-value=""
     */
    private String featureVersion;

    /**
     * Version Compatibility of the Feature
     *
     * @parameter
     */
    private String compatibility;

    private Artifact artifact;

    private boolean isOptional;

    protected static ImportFeature getFeature(String featureDefinition) throws MojoExecutionException {
        String[] split = featureDefinition.split(":");
        ImportFeature feature = new ImportFeature();
        if (split.length > 0) {
            feature.setFeatureId(split[0]);
            String match = "equivalent";
            if (split.length > 1) {
                if (P2Utils.isMatchString(split[1])) {
                    match = split[1].toUpperCase();
                    if (match.equalsIgnoreCase("optional")) {
                        feature.setOptional(true);
                    }
                    if (split.length > 2) {
                        feature.setFeatureVersion(split[2]);
                    }
                } else {
                    feature.setFeatureVersion(split[1]);
                    if (split.length > 2) {
                        if (P2Utils.isMatchString(split[2])) {
                            match = split[2].toUpperCase();
                            if (match.equalsIgnoreCase("optional")) {
                                feature.setOptional(true);
                            }
                        }
                    }
                }
            }
            feature.setCompatibility(match);
            return feature;
        }
        throw new MojoExecutionException("Insufficient feature artifact information " +
                "provided to determine the feature: " + featureDefinition);
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getCompatibility() {
        return compatibility;
    }

    public void setCompatibility(String compatibility) {
        this.compatibility = compatibility;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public String getFeatureVersion() {
        return featureVersion;
    }

    public void setFeatureVersion(String version) {
        if (featureVersion == null || featureVersion.equals("")) {
            featureVersion = Bundle.getOSGIVersion(version);
        }
    }
}
