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

import org.apache.maven.artifact.Artifact;

/**
 * Creates an Axis2 repository from the project's runtime dependencies. This goal is typically
 * used to build an Axis2 repository that will be packaged into some kind of distribution.
 * 
 * @goal create-repository
 * @phase package
 * @requiresDependencyResolution runtime
 */
public class CreateRepositoryMojo extends AbstractCreateRepositoryMojo {
    /**
     * The output directory where the repository will be created.
     * 
     * @parameter default-value="${project.build.directory}/repository"
     */
    private File outputDirectory;
    
    @Override
    protected String getScope() {
        return Artifact.SCOPE_RUNTIME;
    }

    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }
}
