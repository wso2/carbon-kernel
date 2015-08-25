package org.wso2.maven.p2;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class FeatureArtifact {
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
    private String version;

    private Artifact artifact;

    private String featureId;
    private String featureVersion;

    protected static FeatureArtifact getFeatureArtifact(String featureArtifactDefinition, FeatureArtifact featureArtifact) throws MojoExecutionException {
        String[] split = featureArtifactDefinition.split(":");
        if (split.length > 1) {
            featureArtifact.setGroupId(split[0]);
            featureArtifact.setArtifactId(split[1]);
            if (split.length == 3) featureArtifact.setVersion(split[2]);
            return featureArtifact;
        }
        throw new MojoExecutionException("Insufficient artifact information provided to determine the feature: " + featureArtifactDefinition);
    }

    public static FeatureArtifact getFeatureArtifact(String featureArtifactDefinition) throws MojoExecutionException {
        return getFeatureArtifact(featureArtifactDefinition, new FeatureArtifact());
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

    public void resolveVersion(MavenProject project) throws MojoExecutionException {
        if (version == null) {
            List dependencies = project.getDependencies();
            for (Iterator iterator = dependencies.iterator(); iterator.hasNext(); ) {
                Dependency dependancy = (Dependency) iterator.next();
                if (dependancy.getGroupId().equalsIgnoreCase(getGroupId()) && dependancy.getArtifactId().equalsIgnoreCase(getArtifactId())) {
                    setVersion(dependancy.getVersion());
                }

            }
        }
        if (version == null) {
            List dependencies = project.getDependencyManagement().getDependencies();
            for (Iterator iterator = dependencies.iterator(); iterator.hasNext(); ) {
                Dependency dependancy = (Dependency) iterator.next();
                if (dependancy.getGroupId().equalsIgnoreCase(getGroupId()) && dependancy.getArtifactId().equalsIgnoreCase(getArtifactId())) {
                    setVersion(dependancy.getVersion());
                }

            }
        }
        if (version == null) {
            throw new MojoExecutionException("Could not find the version for " + getGroupId() + ":" + getArtifactId());
        }
        Properties properties = project.getProperties();
        for (Object key : properties.keySet()) {
            version = version.replaceAll(Pattern.quote("${" + key + "}"), properties.get(key).toString());
        }
    }

    public String getFeatureId() {
        return featureId;
    }

    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }

    public String getFeatureVersion() {
        return featureVersion;
    }

    public void setFeatureVersion(String featureVersion) {
        this.featureVersion = featureVersion;
    }
}
