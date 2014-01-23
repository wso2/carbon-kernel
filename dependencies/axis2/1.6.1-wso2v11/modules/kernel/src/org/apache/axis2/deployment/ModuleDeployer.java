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

package org.apache.axis2.deployment;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class ModuleDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(ModuleDeployer.class);
    private AxisConfiguration axisConfig;


    public ModuleDeployer() {
    }

    public ModuleDeployer(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
    }

    //To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.axisConfig = configCtx.getAxisConfiguration();
    }
    //Will process the file and add that to axisConfig

    public void deploy(DeploymentFileData deploymentFileData) {
        File deploymentFile = deploymentFileData.getFile();
        boolean isDirectory = deploymentFile.isDirectory();
        if (isDirectory && deploymentFileData.getName().startsWith(".")) {  // Ignore special meta directories starting with .
            return;
        }

        ArchiveReader archiveReader = new ArchiveReader();
        String moduleStatus = "";
        StringWriter errorWriter = new StringWriter();
        try {

            deploymentFileData.setClassLoader(isDirectory,
                                              axisConfig.getModuleClassLoader(),
                    (File)axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                    this.axisConfig.isChildFirstClassLoading());
            AxisModule metaData = new AxisModule();
            metaData.setModuleClassLoader(deploymentFileData.getClassLoader());
            metaData.setParent(axisConfig);
            archiveReader.readModuleArchive(deploymentFileData, metaData, isDirectory, axisConfig);
            URL url = deploymentFile.toURL();
            metaData.setFileName(url);
            DeploymentEngine.addNewModule(metaData, axisConfig);
            super.deploy(deploymentFileData);
            log.info(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_MODULE,
                                         metaData.getArchiveName(),
                                         url.toString()));
        } catch (DeploymentException e) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                          deploymentFileData.getName(),
                                          e.getMessage()),
                      e);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            e.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } catch (AxisFault axisFault) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                          deploymentFileData.getName(),
                                          axisFault.getMessage()),
                      axisFault);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            axisFault.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } catch (MalformedURLException e) {
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                                          deploymentFileData.getName(),
                                          e.getMessage()),
                      e);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            e.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } catch (Throwable t) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            log.error(Messages.getMessage(DeploymentErrorMsgs.INVALID_MODULE,
                    deploymentFileData.getName(),
                    t.getMessage()),
                    t);
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            t.printStackTrace(error_ptintWriter);
            moduleStatus = "Error:\n" + errorWriter.toString();
        } finally {
            if (moduleStatus.startsWith("Error:")) {
                axisConfig.getFaultyModules().put(DeploymentEngine.getAxisServiceName(
                        deploymentFileData.getName()), moduleStatus);
            }
        }
    }

    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        super.undeploy(fileName);
    }
}
