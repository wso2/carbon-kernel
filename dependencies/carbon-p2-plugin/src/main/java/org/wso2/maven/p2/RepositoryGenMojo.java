/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.maven.p2;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.eclipse.tycho.p2.facade.internal.P2ApplicationLauncher;
import org.wso2.maven.p2.generate.utils.FileManagementUtil;
import org.wso2.maven.p2.generate.utils.MavenUtils;
import org.wso2.maven.p2.generate.utils.P2Utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Write environment information for the current build to file.
 *
 * Maven goal: p2-repo-gen
 * Maven phase: package
 */
public class RepositoryGenMojo extends AbstractMojo {

//    /**
//     * URL of the Metadata Repository
//     *
//     * @parameter
//     */
//    private URL repository;

    /**
     * Name of the repository
     *
     * @parameter
     */
    private String name;

    /**
     * URL of the Metadata Repository
     *
     * @parameter
     */
    private URL metadataRepository;

    /**
     * URL of the Artifact Repository
     *
     * @parameter
     */
    private URL artifactRepository;

    /**
     * Source folder
     *
     * @parameter
     * @required
     */
    private ArrayList featureArtifacts;

    /**
     * Source folder
     *
     * @parameter
     */
    private ArrayList bundleArtifacts;

    /**
     * Source folder
     *
     * @parameter
     */
    private ArrayList categories;

    /**
     * flag indicating whether the artifacts should be published to the repository. When this flag is not set,
     * the actual bytes underlying the artifact will not be copied, but the repository index will be created.
     * When this option is not specified, it is recommended to set the artifactRepository to be in the same location
     * as the source (-source)
     *
     * @parameter
     */
    private boolean publishArtifacts;

    /**
     * Type of Artifact (War,Jar,etc)
     *
     * @parameter
     */
    private boolean publishArtifactRepository;

    /**
     * Equinox Launcher
     *
     * @parameter
     */
    private EquinoxLauncher equinoxLauncher;


    /**
     * Equinox p2 configuration path
     *
     * @parameter
     */
    private P2Profile p2Profile;

    /**
     * @parameter default-value="${project}"
     */
    private MavenProject project;

    /**
     * @parameter default-value="false"
     */
    private boolean archive;

    /**
     * @component
     */
    private org.apache.maven.artifact.factory.ArtifactFactory artifactFactory;

    /**
     * @component
     */
    private org.apache.maven.artifact.resolver.ArtifactResolver resolver;

    /**
     * @parameter default-value="${localRepository}"
     */
    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

    /**
     * @parameter default-value="${project.remoteArtifactRepositories}"
     */
    private List remoteRepositories;


    /**
     * @component
     */
    private P2ApplicationLauncher launcher;

    /**
     * Kill the forked test process after a certain number of seconds. If set to 0, wait forever for
     * the process, never timing out.
     *
     * @parameter expression="${p2.timeout}"
     */
    private int forkedProcessTimeoutInSeconds;

    private ArrayList processedFeatureArtifacts;
    private ArrayList processedP2LauncherFiles;
    private File targetDir;
    private File tempDir;
    private File sourceDir;
    private File p2AgentDir;

    private ArrayList processedBundleArtifacts;

    private File repoGenLocation;

    private File categoryDeinitionFile;

    private File archiveFile;

    public void execute() throws MojoExecutionException, MojoFailureException {
        createRepo();
        performMopUp();
    }

    public void createRepo() throws MojoExecutionException, MojoFailureException {
        try {
            getProcessedFeatureArtifacts();
            getProcessedBundleArtifacts();
            createAndSetupPaths();
            extractFeatures();
            copyBundleArtifacts();
            copyResources();
            this.getLog().info("Running Equinox P2 Publisher Application for Repository Generation");
            generateRepository();
            this.getLog().info("Running Equinox P2 Category Publisher Application for the Generated Repository");
            updateRepositoryWithCategories();
            archiveRepo();
        } catch (Exception e) {
            this.getLog().error(e.getMessage(), e);
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }


    private void copyResources() throws MojoExecutionException {
        List resources = project.getResources();
        if (resources != null) {
            getLog().info("Copying resources");
            for (Object obj : resources) {
                if (obj instanceof Resource) {
                    Resource resource = (Resource) obj;
                    try {
                        File resourceFolder = new File(resource.getDirectory());
                        if (resourceFolder.exists()) {
                            getLog().info("   " + resource.getDirectory());
                            FileManagementUtil.copyDirectory(resourceFolder, repoGenLocation);
                        }
                    } catch (IOException e) {
                        throw new MojoExecutionException("Unable copy resources: " + resource.getDirectory(), e);
                    }
                }
            }
        }
    }


    private String getPublisherApplication() {
        return "org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher";
    }

    private void generateRepository() throws Exception {

        P2ApplicationLauncher launcher = this.launcher;

        launcher.setWorkingDirectory(project.getBasedir());
        launcher.setApplicationName(getPublisherApplication());

        addArguments(launcher);


        int result = launcher.execute(forkedProcessTimeoutInSeconds);
        if (result != 0) {
            throw new MojoFailureException("P2 publisher return code was " + result);
        }
    }

    private void addArguments(P2ApplicationLauncher launcher) throws IOException, MalformedURLException {
        launcher.addArguments("-source", sourceDir.getAbsolutePath(), //
                "-metadataRepository", metadataRepository.toString(), //
                "-metadataRepositoryName", getRepositoryName(), //
                "-artifactRepository", metadataRepository.toString(), //
                "-artifactRepositoryName", getRepositoryName(), //
                "-publishArtifacts",
                "-publishArtifactRepository",
                "-compress",
                "-append");
    }

    private void extractFeatures() throws MojoExecutionException {
        ArrayList processedFeatureArtifacts = getProcessedFeatureArtifacts();
        if (processedFeatureArtifacts == null) {
            return;
        }
        for (Iterator iterator = processedFeatureArtifacts.iterator(); iterator
                .hasNext(); ) {
            FeatureArtifact featureArtifact = (FeatureArtifact) iterator.next();
            try {
                getLog().info("Extracting feature " +
                        featureArtifact.getGroupId() + ":" + featureArtifact.getArtifactId());
                FileManagementUtil.unzip(featureArtifact.getArtifact().getFile(), sourceDir);
            } catch (Exception e) {
                throw new MojoExecutionException("Error occured when extracting the Feature Artifact: " +
                        featureArtifact.toString(), e);
            }
        }
    }

    private void copyBundleArtifacts() throws MojoExecutionException {
        ArrayList processedBundleArtifacts = getProcessedBundleArtifacts();
        if (processedBundleArtifacts == null) {
            return;
        }
        File pluginsDir = new File(sourceDir, "plugins");
        for (Iterator iterator = processedBundleArtifacts.iterator(); iterator.hasNext(); ) {
            BundleArtifact bundleArtifact = (BundleArtifact) iterator.next();
            try {
                File file = bundleArtifact.getArtifact().getFile();
                FileManagementUtil.copy(file, new File(pluginsDir, file.getName()));
            } catch (Exception e) {
                throw new MojoExecutionException("Error occured when extracting the Feature Artifact: " +
                        bundleArtifact.toString(), e);
            }
        }
    }

    private ArrayList getProcessedFeatureArtifacts() throws MojoExecutionException {
        if (processedFeatureArtifacts != null) {
            return processedFeatureArtifacts;
        }
        if (featureArtifacts == null || featureArtifacts.size() == 0) {
            return null;
        }
        processedFeatureArtifacts = new ArrayList();
        Iterator iter = featureArtifacts.iterator();
        while (iter.hasNext()) {
            FeatureArtifact f = null;
            Object obj = iter.next();
            try {
                if (obj instanceof FeatureArtifact) {
                    f = (FeatureArtifact) obj;
                } else if (obj instanceof String) {
                    f = FeatureArtifact.getFeatureArtifact(obj.toString());
                } else {
                    f = (FeatureArtifact) obj;
                }
                f.resolveVersion(getProject());
                f.setArtifact(MavenUtils.getResolvedArtifact(f, getArtifactFactory(),
                        remoteRepositories, getLocalRepository(), getResolver()));
                processedFeatureArtifacts.add(f);
            } catch (Exception e) {
                throw new MojoExecutionException("Error occured when processing the Feature Artifact: " +
                        obj.toString(), e);
            }
        }
        return processedFeatureArtifacts;
    }

    private void archiveRepo() throws MojoExecutionException {
        if (isArchive()) {
            getLog().info("Generating repository archive...");
            FileManagementUtil.zipFolder(repoGenLocation.toString(), archiveFile.toString());
            getLog().info("Repository Archive: " + archiveFile.toString());
            FileManagementUtil.deleteDirectories(repoGenLocation);
        }
    }

    private ArrayList getProcessedBundleArtifacts() throws MojoExecutionException {
        if (processedBundleArtifacts != null) {
            return processedBundleArtifacts;
        }
        if (bundleArtifacts == null || bundleArtifacts.size() == 0) {
            return null;
        }
        processedBundleArtifacts = new ArrayList();
        Iterator iter = bundleArtifacts.iterator();
        while (iter.hasNext()) {
            Object obj = iter.next();
            BundleArtifact f;
            if (obj instanceof BundleArtifact) {
                f = (BundleArtifact) obj;
            } else if (obj instanceof String) {
                f = BundleArtifact.getBundleArtifact(obj.toString());
            } else {
                f = (BundleArtifact) obj;
            }
            f.resolveVersion(getProject());
            f.setArtifact(MavenUtils.getResolvedArtifact(f, getArtifactFactory(), remoteRepositories,
                    getLocalRepository(), getResolver()));
            processedBundleArtifacts.add(f);
        }
        return processedBundleArtifacts;
    }


    private void createAndSetupPaths() throws Exception {
        targetDir = new File(getProject().getBasedir(), "target");
        String timestampVal = String.valueOf((new Date()).getTime());
        tempDir = new File(targetDir, "tmp." + timestampVal);
        sourceDir = new File(tempDir, "featureExtract");
        sourceDir.mkdirs();

        metadataRepository = (artifactRepository == null ? metadataRepository : artifactRepository);
        artifactRepository = (metadataRepository == null ? artifactRepository : metadataRepository);
        if (metadataRepository == null) {
            File repo = new File(targetDir, getProject().getArtifactId() + "_" + getProject().getVersion());
            metadataRepository = repo.toURL();
            artifactRepository = metadataRepository;
        }
        repoGenLocation = new File(metadataRepository.getFile().replace("/", File.separator));
        archiveFile = new File(targetDir, getProject().getArtifactId() + "_" + getProject().getVersion() + ".zip");
        categoryDeinitionFile = File.createTempFile("equinox-p2", "category");
    }

    private void updateRepositoryWithCategories() throws Exception {
        if (!isCategoriesAvailable()) {
            return;
        } else {
            P2Utils.createCategoryFile(getProject(), categories, categoryDeinitionFile,
                    getArtifactFactory(), getRemoteRepositories(),
                    getLocalRepository(), getResolver());
            P2ApplicationLauncher launcher = this.launcher;
            launcher.setWorkingDirectory(project.getBasedir());
            launcher.setApplicationName("org.eclipse.equinox.p2.publisher.CategoryPublisher");
            launcher.addArguments("-metadataRepository", metadataRepository.toString(),
                    "-categoryDefinition", categoryDeinitionFile.toURI().toString(),
                    "-categoryQualifier",
                    "-compress",
                    "-append");

            int result = launcher.execute(forkedProcessTimeoutInSeconds);
            if (result != 0) {
                throw new MojoFailureException("P2 publisher return code was " + result);
            }
        }
    }

    private boolean isCategoriesAvailable() {
        if (categories == null || categories.size() == 0) {
            return false;
        }
        return true;
    }

    private void performMopUp() {
        try {
            // we want this temp file, in order to debug some errors. since this is in target, it will
            // get removed in the next build cycle.
            // FileUtils.deleteDirectory(tempDir);
        } catch (Exception e) {
            getLog().warn(new MojoExecutionException("Unable complete mop up operation", e));
        }
    }

    public P2Profile getP2Profile() {
        return p2Profile;
    }

    public void setP2Profile(P2Profile p2Profile) {
        this.p2Profile = p2Profile;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }

    public ArtifactFactory getArtifactFactory() {
        return artifactFactory;
    }

    public void setArtifactFactory(org.apache.maven.artifact.factory.ArtifactFactory artifactFactory) {
        this.artifactFactory = artifactFactory;
    }

    public ArtifactResolver getResolver() {
        return resolver;
    }

    public void setResolver(org.apache.maven.artifact.resolver.ArtifactResolver resolver) {
        this.resolver = resolver;
    }

    public ArtifactRepository getLocalRepository() {
        return localRepository;
    }

    public void setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }

    public List getRemoteRepositories() {
        return remoteRepositories;
    }

    public void setRemoteRepositories(List remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }

    public String getRepositoryName() {
        if (name == null) {
            return getProject().getArtifactId();
        } else {
            return name;
        }
    }

    public boolean isArchive() {
        return archive;
    }
}
