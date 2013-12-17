/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.spi.Artifact;
import org.wso2.carbon.deployment.spi.Deployer;

import java.io.File;
import java.util.Map;

public class CarbonRepositoryListener {
    private static final Log log = LogFactory.getLog(CarbonRepositoryListener.class);

    private final CarbonDeploymentEngine carbonDeploymentEngine;

    public CarbonRepositoryListener(CarbonDeploymentEngine carbonDeploymentEngine) {
        this.carbonDeploymentEngine = carbonDeploymentEngine;
    }

    /**
     * Start the repository listener on the given deployment engine.
     */
    public void start() {
        checkArtifacts();
        update();
    }

    /**
     * Update the repository with new changes. This is called by the Deployment Scheduler Task
     */
    public void update() {
        synchronized (carbonDeploymentEngine) {
            carbonDeploymentEngine.checkUndeployedArtifacts();
            carbonDeploymentEngine.undeployArtifacts();
            carbonDeploymentEngine.deployArtifacts();
        }
    }

    /**
     * Find and add the artifacts in all deployment directories in the repository
     * and add them to the deployment engine to carry out the deployment process
     */
    public void checkArtifacts() {
        for (Map.Entry<String, Deployer> entry : carbonDeploymentEngine.getDeployers().entrySet()) {
            String deployerDirectory = entry.getValue().getDirectory();
            File dirToSearch = new File(deployerDirectory);
            if (!dirToSearch.isAbsolute()) {
                dirToSearch = new File(carbonDeploymentEngine.getRepositoryDirectory(),
                                       deployerDirectory);
            }
            String artifactType = entry.getValue().getType();
            findArtifactsToDeploy(dirToSearch, artifactType);
        }
    }

    /**
     * Finds and add the artifacts in the given deployment directory to the
     * deploy artifacts list
     *
     * @param directoryToScan the directory to scan
     * @param artifactType artifactType
     */
    private void findArtifactsToDeploy(File directoryToScan, String artifactType) {
        try {
            if (directoryToScan.exists()) {
                File[] files = directoryToScan.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        Artifact artifact = new Artifact(file);
                        artifact.setType(artifactType);
                        carbonDeploymentEngine.addArtifactToDeploy(artifact);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }
}
