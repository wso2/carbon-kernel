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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

/**
 * Deploys artifacts of a given type into an Axis2 repository.
 */
public class ArchiveDeployer {
    private final File directory;
    private final String fileListName;
    private final boolean generateFileList;
    private final boolean stripVersion;
    private final List<String> files = new ArrayList<String>();
    
    public ArchiveDeployer(File repositoryDirectory, String directory, String fileListName, boolean generateFileList, boolean stripVersion) {
        this.directory = new File(repositoryDirectory, directory);
        this.fileListName = fileListName;
        this.generateFileList = generateFileList;
        this.stripVersion = stripVersion;
    }
    
    public void deploy(Log log, Artifact artifact) throws MojoExecutionException {
        StringBuilder buffer = new StringBuilder(artifact.getArtifactId());
        if (!stripVersion) {
            buffer.append("-");
            buffer.append(artifact.getVersion());
        }
        buffer.append(".");
        buffer.append(artifact.getType());
        String destFileName = buffer.toString();
        log.info("Adding " + destFileName);
        try {
            FileUtils.copyFile(artifact.getFile(), new File(directory, destFileName));
        } catch (IOException ex) {
            throw new MojoExecutionException("Error copying " + destFileName + ": " + ex.getMessage(), ex);
        }
        files.add(destFileName);
    }
    
    public void finish(Log log) throws MojoExecutionException {
        if (generateFileList && !files.isEmpty()) {
            log.info("Writing " + fileListName);
            try {
                OutputStream out = new FileOutputStream(new File(directory, fileListName));
                try {
                    Writer writer = new OutputStreamWriter(out, "UTF-8");
                    for (String file : files) {
                        writer.write(file);
                        writer.write('\n');
                    }
                    writer.flush();
                } finally {
                    out.close();
                }
            } catch (IOException ex) {
                throw new MojoExecutionException("Error writing " + fileListName + ": " + ex.getMessage(), ex);
            }
        }
    }
}
