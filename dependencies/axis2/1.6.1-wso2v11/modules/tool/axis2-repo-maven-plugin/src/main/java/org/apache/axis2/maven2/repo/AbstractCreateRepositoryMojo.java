/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.maven2.repo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;
import org.apache.maven.shared.artifact.filter.collection.FilterArtifacts;
import org.apache.maven.shared.artifact.filter.collection.ScopeFilter;
import org.apache.maven.shared.artifact.filter.collection.TypeFilter;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

public abstract class AbstractCreateRepositoryMojo extends AbstractMojo {
    /**
     * @component
     */
    private ArtifactFactory factory;
    
    /**
     * @component
     */
    private ArtifactResolver resolver;
    
    /**
     * @parameter expression="${project.artifacts}"
     * @readonly
     * @required
     */
    private Set<Artifact> projectArtifacts;
    
    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @readonly
     * @required
     */
    private List remoteRepositories;
    
    /**
     * @parameter expression="${localRepository}"
     * @readonly
     * @required
     */
    private ArtifactRepository localRepository;
    
    /**
     * @parameter expression="${project.collectedProjects}"
     * @required
     * @readonly
     */
    private List<MavenProject> collectedProjects;
    
    /**
     * The directory (relative to the repository root) where AAR files are copied. This should be
     * set to the same value as the <tt>ServicesDirectory</tt> property in <tt>axis2.xml</tt>.
     * 
     * @parameter default-value="services"
     */
    private String servicesDirectory;
    
    /**
     * The directory (relative to the repository root) where MAR files are copied. This should be
     * set to the same value as the <tt>ModulesDirectory</tt> property in <tt>axis2.xml</tt>.
     * 
     * @parameter default-value="modules"
     */
    private String modulesDirectory;
    
    /**
     * The <tt>axis2.xml</tt> file to be copied into the repository.
     * 
     * @parameter
     */
    private File axis2xml;
    
    /**
     * The directory (relative to the repository root) where the <tt>axis2.xml</tt> file will be
     * copied. If this parameter is not set, then the file will be copied into the repository
     * root.
     * 
     * @parameter
     */
    private String configurationDirectory;
    
    /**
     * Specifies whether the plugin should scan the project dependencies for AAR and MAR artifacts.
     * 
     * @parameter default-value="true"
     */
    private boolean useDependencies;
    
    /**
     * Specifies whether the plugin should scan Maven modules for AAR and MAR artifacts. This
     * parameter only has an effect for multimodule projects.
     * 
     * @parameter default-value="true"
     */
    private boolean useModules;
    
    /**
     * Specifies whether the plugin should generate <tt>services.list</tt> and <tt>modules.list</tt>
     * files.
     * 
     * @parameter default-value="false"
     */
    private boolean generateFileLists;
    
    /**
     * Specifies whether the plugin strips version numbers from AAR files.
     * 
     * @parameter default-value="true"
     */
    private boolean stripServiceVersion;
    
    /**
     * Specifies whether the plugin strips version numbers from MAR files.
     * 
     * @parameter default-value="false"
     */
    private boolean stripModuleVersion;
    
    /**
     * Specifies whether modules should be deployed to the repository.
     * 
     * @parameter default-value="true"
     */
    private boolean includeModules;
    
    /**
     * Comma separated list of modules (by artifactId) to include in the repository.
     * 
     * @parameter
     */
    private String modules;
    
    /**
     * Specifies whether services should be deployed to the repository.
     * 
     * @parameter default-value="true"
     */
    private boolean includeServices;
    
    /**
     * Comma separated list of services (by artifactId) to include in the repository.
     * 
     * @parameter
     */
    private String services;
    
    protected abstract String getScope();
    
    protected abstract File getOutputDirectory();

    public void execute() throws MojoExecutionException, MojoFailureException {
        Set<Artifact> artifacts = new HashSet<Artifact>();
        if (useDependencies) {
            artifacts.addAll(projectArtifacts);
        }
        if (useModules) {
            for (MavenProject project : collectedProjects) {
                artifacts.add(project.getArtifact());
                artifacts.addAll(project.getAttachedArtifacts());
            }
        }
        File outputDirectory = getOutputDirectory();
        if (includeModules || includeServices) {
            FilterArtifacts filter = new FilterArtifacts();
            filter.addFilter(new ScopeFilter(getScope(), null));
            if (includeModules && includeServices) {
                filter.addFilter(new TypeFilter("aar,mar", null));
            } else if (includeModules) {
                filter.addFilter(new TypeFilter("mar", null));
            }
            try {
                artifacts = filter.filter(artifacts);
            } catch (ArtifactFilterException ex) {
                throw new MojoExecutionException(ex.getMessage(), ex);
            }
            selectArtifacts(artifacts, modules, "mar");
            selectArtifacts(artifacts, services, "aar");
            artifacts = replaceIncompleteArtifacts(artifacts);
            Map<String,ArchiveDeployer> deployers = new HashMap<String,ArchiveDeployer>();
            deployers.put("aar", new ArchiveDeployer(outputDirectory, servicesDirectory, "services.list", generateFileLists, stripServiceVersion));
            deployers.put("mar", new ArchiveDeployer(outputDirectory, modulesDirectory, "modules.list", generateFileLists, stripModuleVersion));
            for (Artifact artifact : artifacts) {
                String type = artifact.getType();
                ArchiveDeployer deployer = deployers.get(type);
                if (deployer == null) {
                    throw new MojoExecutionException("No deployer found for artifact type " + type);
                }
                deployer.deploy(getLog(), artifact);
            }
            for (ArchiveDeployer deployer : deployers.values()) {
                deployer.finish(getLog());
            }
        }
        if (axis2xml != null) {
            getLog().info("Copying axis2.xml");
            File targetDirectory = configurationDirectory == null
                    ? outputDirectory : new File(outputDirectory, configurationDirectory);
            try {
                FileUtils.copyFile(axis2xml, new File(targetDirectory, "axis2.xml"));
            } catch (IOException ex) {
                throw new MojoExecutionException("Error copying axis2.xml file: " + ex.getMessage(), ex);
            }
        }
    }

    private void selectArtifacts(Set<Artifact> artifacts, String list, String type) throws MojoFailureException {
        if (list != null) {
            Set<String> set = new HashSet<String>(Arrays.asList(StringUtils.split(list, ",")));
            for (Iterator<Artifact> it = artifacts.iterator(); it.hasNext(); ) {
                Artifact artifact = it.next();
                if (artifact.getType().equals(type) && !set.remove(artifact.getArtifactId())) {
                    it.remove();
                }
            }
            if (!set.isEmpty()) {
                throw new MojoFailureException("The following " + type + " artifacts have not been found: " + set);
            }
        }
    }

    /**
     * Replace artifacts that have not been packaged yet. This occurs if the artifact is
     * part of the reactor build and the compile phase has been executed, but not the
     * the package phase. These artifacts will be replaced by new artifact objects
     * resolved from the repository.
     * 
     * @param artifacts the original sets of {@link Artifact} objects
     * @return a set of {@link Artifact} objects built as described above
     * @throws MojoExecutionException
     */
    private Set<Artifact> replaceIncompleteArtifacts(Set<Artifact> artifacts) throws MojoExecutionException {
        Set<Artifact> result = new HashSet<Artifact>();
        for (Artifact artifact : artifacts) {
            File file = artifact.getFile();
            if (file != null && file.isDirectory()) {
                artifact = factory.createDependencyArtifact(artifact.getGroupId(), artifact.getArtifactId(),
                        artifact.getVersionRange(), artifact.getType(), artifact.getClassifier(), artifact.getScope());
                try {
                    resolver.resolve(artifact, remoteRepositories, localRepository);
                } catch (AbstractArtifactResolutionException ex) {
                    throw new MojoExecutionException(ex.getMessage(), ex);
                }
            }
            result.add(artifact);
        }
        return result;
    }
}
