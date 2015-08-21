/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.application.deployer.handler;

import org.wso2.carbon.application.deployer.config.ApplicationConfiguration;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.config.RegistryConfig;
import org.wso2.carbon.application.deployer.persistence.CarbonAppPersistenceManager;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.internal.ApplicationManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class RegistryResourceDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(RegistryResourceDeployer.class);

    public static final String REGISTRY_RESOURCE_TYPE = "registry/resource";
    public static final String REGISTRY_FILTER_TYPE = "lib/registry/filter";
    public static final String REGISTRY_HANDLER_TYPE = "lib/registry/handler";

    /**
     * Deploy the artifacts which can be deployed through this deployer.
     * @param carbonApp - store info in this object after deploying
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {
        ApplicationConfiguration appConfig = carbonApp.getAppConfig();
        List<Artifact.Dependency> deps = appConfig.getApplicationArtifact().getDependencies();

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact.Dependency dep : deps) {
            if (dep.getArtifact() != null) {
                artifacts.add(dep.getArtifact());
            }
        }
        CarbonAppPersistenceManager capm = ApplicationManager.getInstance()
                .getPersistenceManager(axisConfig);
        //Create the resource path mapping file
        capm.createResourcePathMappingFile(carbonApp.getAppNameWithVersion());
        // deploying registry resources in all dependent artifacts
        deployRegistryArtifacts(capm, artifacts, carbonApp.getAppNameWithVersion());
    }

    /**
     * Undeploys Registry resources of the given cApp
     *
     * @param carbonApp - all information about the existing artifacts are in this instance
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {
        ApplicationConfiguration appConfig = carbonApp.getAppConfig();
        List<Artifact.Dependency> deps = appConfig.getApplicationArtifact().getDependencies();

        List<Artifact> artifacts = new ArrayList<Artifact>();
        for (Artifact.Dependency dep : deps) {
            if (dep.getArtifact() != null) {
                artifacts.add(dep.getArtifact());
            }
        }
        CarbonAppPersistenceManager capm = ApplicationManager.getInstance()
                .getPersistenceManager(axisConfig);
        // undeploying registry resources in all dependent artifacts
        undeployRegistryArtifacts(capm, artifacts, carbonApp.getAppNameWithVersion());
        //deleting the registry resource mapping file
        capm.deleteResourcePathMappingFile(carbonApp.getAppNameWithVersion());
    }

    /**
     * Deploys registry artifacts recursively. A Registry artifact can exist as a sub artifact in
     * any type of artifact. Therefore, have to search recursively
     *
     * @param capm - CarbonAppPersistenceManager for this tenant
     * @param artifacts - list of artifacts to be deployed
     * @param parentAppName - name of the parent cApp
     */
    private void deployRegistryArtifacts(CarbonAppPersistenceManager capm,
                                         List<Artifact> artifacts, String parentAppName) {
        for (Artifact artifact : artifacts) {
            if (REGISTRY_RESOURCE_TYPE.equals(artifact.getType())) {
                try {
                    RegistryConfig regConfig = buildRegistryConfig(artifact, parentAppName);
                    if (regConfig != null) {
                        capm.writeArtifactResources(regConfig);
                    }
                    artifact.setRegConfig(regConfig);
                } catch (Exception e) {
                    log.error("Error while deploying registry artifact " + artifact.getName(), e);
                }
            }

            // set the parent extracted path and name for all sub artifacts..
            List<Artifact> subArtifacts = artifact.getSubArtifacts();
            if (subArtifacts.size() != 0) {
                for (Artifact sub : subArtifacts) {
                    sub.setExtractedPath(artifact.getExtractedPath());
                    sub.setName(artifact.getName());
                }
            }
            deployRegistryArtifacts(capm, subArtifacts, parentAppName);
        }
    }

    /**
     * Registry config file comes bundled inside the Registry/Resource artifact. So we have to
     * find the file from the extractedPath of the artifact and build the RegistryConfig instance
     * using the contents of that file.
     *
     * @param artifact - Registry/Resource artifact
     * @return - RegistryConfig instance
     */
    private RegistryConfig buildRegistryConfig(Artifact artifact, String appName) {
        RegistryConfig regConfig = null;
        // get the file path of the registry config file
        List<CappFile> files = artifact.getFiles();
        if (files.size() == 1) {
            String fileName = artifact.getFiles().get(0).getName();
            String regConfigPath = artifact.getExtractedPath() +
                    File.separator + fileName;

            File f = new File(regConfigPath);
            if (f.exists()) {
                // read the reg config file and build the configuration
                InputStream xmlInputStream = null;
                try {
                    xmlInputStream = new FileInputStream(f);
                    regConfig = AppDeployerUtils.populateRegistryConfig(
                            new StAXOMBuilder(xmlInputStream).getDocumentElement());
                } catch (Exception e) {
                    log.error("Error while reading file : " + fileName, e);
                } finally {
                    if (xmlInputStream != null) {
                        try {
                            xmlInputStream.close();
                        } catch (IOException e) {
                            log.error("Error while closing input stream.", e);
                        }
                    }
                }

                if (regConfig != null) {
                    regConfig.setAppName(appName);
                    regConfig.setExtractedPath(artifact.getExtractedPath());
                    regConfig.setParentArtifactName(artifact.getName());
                    regConfig.setConfigFileName(fileName);
                }
            } else {
                log.error("Registry config file not found at : " + regConfigPath);
            }
        } else {
            log.error("Registry/Resource type must have a single file which declares " +
                    "registry configs. But " + files.size() + " files found.");
        }
        return regConfig;
    }

    /**
     * Uneploys registry artifacts recursively. A Registry artifact can exist as a sub artifact in
     * any type of artifact. Therefore, have to search recursively
     *
     * @param capm - CarbonAppPersistenceManager instance for current tenant
     * @param artifacts - list of artifacts to be undeployed
     * @param parentAppName - name of the parent app name
     */
    private void undeployRegistryArtifacts(CarbonAppPersistenceManager capm,
                                           List<Artifact> artifacts, String parentAppName) {
        for (Artifact artifact : artifacts) {
            if (RegistryResourceDeployer.REGISTRY_RESOURCE_TYPE.equals(artifact.getType())) {
                try {
                    RegistryConfig regConfig = artifact.getRegConfig();
                    if (regConfig == null) {
                        regConfig = capm.loadRegistryConfig(AppDeployerConstants.APPLICATIONS +
                                parentAppName + AppDeployerConstants.APP_DEPENDENCIES +
                                artifact.getName(), parentAppName);
                    }
                    if (regConfig != null) {
                        capm.removeArtifactResources(regConfig);
                    }
                } catch (Exception e) {
                    log.error("Error while loading registry config for artifact " +
                            artifact.getName(), e);
                }
            }
            // set the parent name for all sub artifacts..
            List<Artifact> subArtifacts = artifact.getSubArtifacts();
            if (subArtifacts.size() != 0) {
                for (Artifact sub : subArtifacts) {
                    sub.setName(artifact.getName());
                }
            }
            undeployRegistryArtifacts(capm, subArtifacts, parentAppName);
        }
    }

}
