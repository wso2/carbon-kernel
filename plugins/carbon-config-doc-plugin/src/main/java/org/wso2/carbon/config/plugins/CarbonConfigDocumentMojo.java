/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.config.plugins;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.classworlds.realm.ClassRealm;

import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

/**
 * This maven plugin will create the config document.
 */
@Mojo(name = "doc")
public class CarbonConfigDocumentMojo extends AbstractMojo {

    /**
     * The plugin descriptor
     *
     * @parameter default-value="${descriptor}"
     */
    @Component
    private PluginDescriptor descriptor;

    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    @Component
    private MavenProject project;

//    /**
//     * Project's source directory as specified in the POM.
//     *
//     * @parameter expression="${project.build.sourceDirectory}"
//     * @readonly
//     * @required
//     */
//    @Component
//    private File sourceDirectory = new File("");

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Configuration Document Maven Plugin Started");
//        getLog().info(sourceDirectory.getAbsolutePath());

        try {
            if (project == null || descriptor == null) {
                getLog().error("project and plugin descriptor is null");
                return;
            }
            List<String> runtimeClasspathElements = project.getRuntimeClasspathElements();
            ClassRealm realm = descriptor.getClassRealm();

            for (String element : runtimeClasspathElements) {
                File elementFile = new File(element);
                realm.addURL(elementFile.toURI().toURL());
            }
        } catch (DependencyResolutionRequiredException | MalformedURLException e) {
            getLog().error("error while executing.", e);
        }
    }
}
