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


package org.apache.axis2.deployment.repository.util;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.i18n.Messages;

import java.io.File;
import java.net.URL;

/**
 * DeploymentFileData represents a "thing to deploy" in Axis2.  It consists of a file,
 * a deployment ClassLoader, and a Deployer.
 */
public class DeploymentFileData {
    private File file;
    private ClassLoader classLoader;
    private Deployer deployer;

    public DeploymentFileData(File file) {
        this.file = file;
    }

    public DeploymentFileData(File file, Deployer deployer) {
        this(file);
        this.deployer = deployer;
    }

    public String getAbsolutePath() {
        return file.getAbsolutePath();
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public File getFile() {
        return file;
    }

    /**
     * Get the name of the file.
     *
     * @return the name of the referenced file
     */
    public String getName() {
        return file.getName(); // No need to check for null due to constructor check
    }

    /**
     * Get the name of the file.
     *
     * @return the name of the referenced file
     * @deprecated please use getName() instead - this will disappear after 1.3.
     */
    public String getServiceName() {
        return getName();
    }

    public static boolean isModuleArchiveFile(String filename) {
        return (filename.endsWith(".mar"));
    }

    /**
     * Checks whether a given file is a jar or an aar file.
     *
     * @param filename file to check
     * @return Returns boolean.
     */
    public static boolean isServiceArchiveFile(String filename) {
        return ((filename.endsWith(".jar")) | (filename.endsWith(".aar")));
    }

    public static String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return fileName.substring(index + 1);
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setClassLoader(boolean isDirectory, ClassLoader parent, File file, boolean isChildFirstClassLoading) throws AxisFault {
        if (!isDirectory) {
            if (this.file != null) {
                URL[] urlsToLoadFrom;
                try {
                    if (!this.file.exists()) {
                        throw new AxisFault(Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND,
                                                                this.file.getAbsolutePath()));
                    }
                    urlsToLoadFrom = new URL[]{this.file.toURL()};
                    classLoader = Utils.createClassLoader(urlsToLoadFrom, parent, true, file, isChildFirstClassLoading);
                } catch (Exception e) {
                    throw AxisFault.makeFault(e);
                }
            }
        } else {
            if (this.file != null) {
                classLoader = Utils.getClassLoader(parent, this.file, isChildFirstClassLoading);
            }
        }
    }

    public Deployer getDeployer() {
        return deployer;
    }

    public void setDeployer(Deployer deployer) {
        this.deployer = deployer;
    }

    public void deploy() throws DeploymentException {
        deployer.deploy(this);
    }
}
