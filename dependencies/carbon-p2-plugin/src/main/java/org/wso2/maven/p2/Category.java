package org.wso2.maven.p2;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Category {

    /**
     * Category Id
     *
     * @parameter
     * @required
     */
    private String id;

    /**
     * Category Label
     *
     * @parameter
     */
    private String label;

    /**
     * Category description
     *
     * @parameter
     */
    private String description;

    /**
     * List of features contained in the category
     *
     * @parameter
     * @required
     */
    private ArrayList<CatFeature> features;

    private ArrayList<CatFeature> processedFeatures;

    public ArrayList<CatFeature> getFeatures() {
        return features;
    }

    public ArrayList<CatFeature> getProcessedFeatures(MavenProject project,
                                                      ArtifactFactory artifactFactory,
                                                      List remoteRepositories,
                                                      ArtifactRepository localRepository,
                                                      ArtifactResolver resolver) throws MojoExecutionException {
        if (processedFeatures != null) {
            return processedFeatures;
        }
        if (features == null || features.size() == 0) {
            return null;
        }
        processedFeatures = new ArrayList<CatFeature>();
        Iterator<CatFeature> iter = features.iterator();
        while (iter.hasNext()) {
            CatFeature f = (CatFeature) iter.next();
            processedFeatures.add(f);
            f.replaceProjectKeysInVersion(project);
        }
        return processedFeatures;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        if (label == null) {
            return getId();
        } else {
            return label;
        }
    }

    public String getDescription() {
        if (description == null) {
            return getLabel();
        } else {
            return description;
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
