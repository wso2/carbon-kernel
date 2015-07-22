/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.internal.deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.Artifact;
import org.wso2.carbon.deployment.ArtifactType;
import org.wso2.carbon.deployment.spi.Deployer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Repository Scanner which does the scanning of repository in carbon.
 * This will scan each registered deployer's deployment directory and sweep
 * the relevant artifact lists (deploy, undeploy, sweep)
 */
public class RepositoryScanner {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryScanner.class);
    /**
     * A list which holds the artifacts to be deployed
     */
    private ArrayList<Artifact> artifactsToDeploy = new ArrayList<>();

    /**
     * A list which holds the artifact to be undeployed
     */
    private ArrayList<Artifact> artifactsToUndeploy = new ArrayList<>();

    /**
     * A list which holds the artifact to be updated
     */
    private ArrayList<Artifact> artifactsToUpdate = new ArrayList<>();

    /**
     * A list to hold the path of the artifacts to be deployed
     */
    private List<String> artifactFilePathList = new ArrayList<>();


    private final DeploymentEngine carbonDeploymentEngine;

    public RepositoryScanner(DeploymentEngine carbonDeploymentEngine) {
        this.carbonDeploymentEngine = carbonDeploymentEngine;
    }

    /**
     * Scans the repository on the given deployment engine.
     */
    public void scan() {
        logger.debug("Starting scanning of deployer directories");
        mark();
        sweep();
    }



    /**
     * Search and add the artifacts in all deployment directories in the repository
     * and populate the relevant lists (deploy, undeploy, sweep) to carry out the
     * deployment process
     */
    private void mark() {
        File carbonRepo = carbonDeploymentEngine.getRepositoryDirectory();
        for (Deployer deployer : carbonDeploymentEngine.getDeployers().values()) {
            File deploymentLocation = Utils.resolveFileURL(deployer.getLocation().getPath(),
                                                           carbonRepo.getPath());
            findArtifactsToDeploy(deploymentLocation, deployer.getArtifactType());
        }
        checkUndeployedArtifacts();
    }

    /**
     * Based of populated artifact list, each list will be given to deployment engine to carry on
     * the relevant deployment process.
     */
    private void sweep() {
        if (artifactsToUpdate.size() > 0) {
            try {
                carbonDeploymentEngine.updateArtifacts(artifactsToUpdate);
            } finally {
                artifactsToUpdate.clear();
            }
        }
        if (artifactsToUndeploy.size() > 0) {
            try {
                carbonDeploymentEngine.undeployArtifacts(artifactsToUndeploy);
            } finally {
                artifactsToUndeploy.clear();
            }
        }
        if (artifactsToDeploy.size() > 0) {
            try {
                carbonDeploymentEngine.deployArtifacts(artifactsToDeploy);
            } finally {
                artifactsToDeploy.clear();
            }
        }
    }

    /**
     * Finds and add the artifacts in the given deployment directory to the
     * deploy artifacts list
     *
     * @param directoryToSearch the directory to scan
     * @param type ArtifactType
     */
    private void findArtifactsToDeploy(File directoryToSearch, ArtifactType type) {
        File[] files = directoryToSearch.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                Artifact artifact = new Artifact(file);
                artifact.setType(type);
                addArtifactToDeploy(artifact);
            }
        }
    }

    /**
     * Add given artifact to the list artifacts to deploy
     *
     * @param artifact artifact to deploy
     */
    private void addArtifactToDeploy(Artifact artifact) {
        Artifact deployedArtifact = findDeployedArtifact(artifact.getType(),
                                                         artifact.getPath());
        if (deployedArtifact != null) { // Artifact is getting updated
            if (Utils.isArtifactModified(deployedArtifact)) {
                artifactsToUpdate.add(deployedArtifact);
            }
        } else { // New artifact deployment
            artifactsToDeploy.add(artifact);
            Utils.setArtifactLastModifiedTime(artifact);
        }
        artifactFilePathList.add(artifact.getPath());
    }

    private Artifact findDeployedArtifact(ArtifactType type, String path) {
        Artifact artifact = null;
        Map<ArtifactType, ConcurrentHashMap<Object, Artifact>> deployedArtifacts =
                carbonDeploymentEngine.getDeployedArtifacts();
        if (deployedArtifacts.get(type) != null) {
            for (Artifact anArtifact : deployedArtifacts.get(type).values()) {
                if (path.equals(anArtifact.getPath())) {
                    artifact = anArtifact;
                }
            }
        }
        return artifact;
    }

    private void checkUndeployedArtifacts() {
        Map<ArtifactType, ConcurrentHashMap<Object, Artifact>> deployedArtifacts =
                carbonDeploymentEngine.getDeployedArtifacts();
        for (Map<Object, Artifact> deployedArtifactList : deployedArtifacts.values()) {
            for (Artifact artifact : deployedArtifactList.values()) {
                String filePath = artifact.getPath();
                boolean found = false;
                for (Object anArtifactFilePath : artifactFilePathList) {
                    String artifactFilePath = (String) anArtifactFilePath;
                    if (filePath.equals(artifactFilePath)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    artifactsToUndeploy.add(artifact);
                }
            }
        }
        artifactFilePathList.clear();
    }
}
