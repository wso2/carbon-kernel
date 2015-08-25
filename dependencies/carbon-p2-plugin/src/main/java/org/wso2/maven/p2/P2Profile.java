package org.wso2.maven.p2;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

public class P2Profile {
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
     * @parameter
     */
    private String version;

    private Artifact artifact;

    protected static P2Profile getP2Profile(String p2ProfileDefinition,
                                            P2Profile p2Profile) throws MojoExecutionException {
        String[] split = p2ProfileDefinition.split(":");
        if (split.length > 1) {
            p2Profile.setGroupId(split[0]);
            p2Profile.setArtifactId(split[1]);
            if (split.length == 3) p2Profile.setVersion(split[2]);
            return p2Profile;
        }
        throw new MojoExecutionException("Insufficient artifact information provided to determine the profile: " +
                p2ProfileDefinition);
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

    public void resolveVersion(MavenProject project) throws MojoExecutionException {
        if (version == null) {
            List dependencies = project.getDependencies();
            for (Iterator iterator = dependencies.iterator(); iterator.hasNext(); ) {
                Dependency dependancy = (Dependency) iterator.next();
                if (dependancy.getGroupId().equalsIgnoreCase(getGroupId()) &&
                        dependancy.getArtifactId().equalsIgnoreCase(getArtifactId())) {
                    setVersion(dependancy.getVersion());
                }

            }
        }
        if (version == null) {
            List dependencies = project.getDependencyManagement().getDependencies();
            for (Iterator iterator = dependencies.iterator(); iterator.hasNext(); ) {
                Dependency dependancy = (Dependency) iterator.next();
                if (dependancy.getGroupId().equalsIgnoreCase(getGroupId()) &&
                        dependancy.getArtifactId().equalsIgnoreCase(getArtifactId())) {
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

    public Artifact getArtifact() {
        return artifact;
    }

    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }
}
