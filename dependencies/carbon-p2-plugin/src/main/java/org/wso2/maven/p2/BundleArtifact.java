package org.wso2.maven.p2;

import org.apache.maven.plugin.MojoExecutionException;
import org.wso2.maven.p2.generate.feature.Bundle;

/**
 * TODO: class level comment
 */
public class BundleArtifact extends Bundle {
    protected static BundleArtifact getBundleArtifact(String bundleArtifactDefinition,
                                                      BundleArtifact bundleArtifact) throws MojoExecutionException {
        String[] split = bundleArtifactDefinition.split(":");
        if (split.length > 1) {
            bundleArtifact.setGroupId(split[0]);
            bundleArtifact.setArtifactId(split[1]);
            if (split.length == 3) {
                bundleArtifact.setVersion(split[2]);
            }
            return bundleArtifact;
        }
        throw new MojoExecutionException("Insufficient artifact information provided to determine the feature: " +
                bundleArtifactDefinition);
    }

    public static BundleArtifact getBundleArtifact(String bundleArtifactDefinition) throws MojoExecutionException {
        return getBundleArtifact(bundleArtifactDefinition, new BundleArtifact());
    }
}
