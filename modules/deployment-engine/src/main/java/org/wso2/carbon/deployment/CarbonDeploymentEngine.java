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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.deployment.spi.Deployer;
import org.wso2.carbon.deployment.spi.Artifact;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.scheduler.CarbonDeploymentIterator;
import org.wso2.carbon.deployment.scheduler.CarbonScheduler;
import org.wso2.carbon.deployment.scheduler.CarbonSchedulerTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Deployment Engine of Carbon which manages the deployment/undeployment of artifacts in carbon.
 */

public class CarbonDeploymentEngine {

    private static final Log log = LogFactory.getLog(CarbonDeploymentEngine.class);

    /**
     * The repository listener associated with this engine
     */
    private CarbonRepositoryListener repositoryListener;

    /**
     * Repository directory for this deployment engine
     */
    private File repositoryDirectory = null;

    /**
     * The map which holds the set of registered deployers with this engine
     */
    private ConcurrentHashMap<String, Deployer> deployerMap =
            new ConcurrentHashMap<String, Deployer>();

    /**
     * A map to hold all currently deployed artifacts
     */
    private Map<String, Artifact> deployedArtifacts = new HashMap<String, Artifact>();

    /**
     * A list which holds the artifacts to be deployed
     */
    private ArrayList artifactsToDeploy = new ArrayList();

    /**
     * A list which holds the artifact to be undeployed
     */
    private ArrayList artifactsToUndeploy = new ArrayList();

    /**
     * A map to hold the keys of the currently deployed artifacts
     */
    private Map<String, Object> artifactKeys = new HashMap<String, Object>();

    /**
     * A map to hold set of deployer directories against the deployer type
     */
    private ConcurrentHashMap<String, String> deployerDirectories =
            new ConcurrentHashMap<String, String>();

    /**
     * A list to hold the path of the artifacts to be deployed
     */
    private List artifactFilePathList = new ArrayList();


    public CarbonDeploymentEngine(String repositoryDir) throws CarbonDeploymentException {
        init(repositoryDir);
    }

    /**
     * Configure and prepare the repository associated with this engine.
     *
     * @throws CarbonDeploymentException on error
     */
    private void init(String repositoryDir) throws CarbonDeploymentException {
        repositoryDirectory = new File(repositoryDir);
        if (!repositoryDirectory.exists()) {
            throw new CarbonDeploymentException("Cannot find repository : " + repositoryDirectory);
        }
        repositoryListener = new CarbonRepositoryListener(this);
    }

    /**
     * Start the repository listener and scheduler task and load artifacts to the deployment engine
     * to be deployed
     */
    public void start() {
        repositoryListener.start();
        // We need to check and start the task based on the deployment engine mode of operation
        // Currently there can be two modes
        // 1. Scheduled Mode - where the task runs periodically and trigger deployment
        // 2. Triggered Mode - the deployment has to be triggered externally,
        //    eg : in a worker node we don't need the task to run, but rather when we receive a
        //         cluster msg,  the deployment has to be triggered manually
        //TODO : Need to check whether we need to start the task when triggered mode is enabled
        startSchedulerTask();
    }


    /**
     * Starts the Deployment engine to perform Hot deployment and so on.
     */
    private void startSchedulerTask() {
        CarbonScheduler carbonScheduler = new CarbonScheduler();
        CarbonSchedulerTask carbonSchedulerTask = new CarbonSchedulerTask(repositoryListener);
        carbonScheduler.schedule(carbonSchedulerTask, new CarbonDeploymentIterator());
    }

    /**
     * Add and initialize a new Deployer to deployment engine.
     *
     * @param deployer  the deployer
     * @param directory the directory which will be scanned for deployable artifacts
     */
    public void registerDeployer(Deployer deployer, String directory) {

        if (deployer == null) {
            log.error("Failed to add Deployer : Deployer Class Name is null");
            return;
        }

        if (directory == null) {
            log.error("Failed to add Deployer " + deployer.getClass().getName() +
                      ": missing 'directory' attribute");
            return;
        }

        Deployer existingDeployer = deployerMap.get(directory);
        if (existingDeployer == null) {
            deployerMap.put(directory, deployer);
            deployerDirectories.put(deployer.getType(), directory);
        }
    }

    /**
     * Removes a deployer from the deployment engine configuration
     *
     * @param directory the directory that the deployer is associated with
     */
    public void unRegisterDeployer(String directory) {
        if (directory == null) {
            log.error("Failed to remove Deployer : missing 'directory' attribute");
            return;
        }

        Deployer existingDeployer = deployerMap.get(directory);
        if (existingDeployer != null) {
            deployerMap.remove(directory);
            deployerDirectories.remove(existingDeployer.getType());
        }
    }

    /**
     * Retrieve the deployer from the current deployers map, by giving the associated directory
     *
     * @param directory associated directory of the deployer
     * @return Deployer instance
     */
    public Deployer getDeployer(String directory) {
        Deployer existingDeployer = deployerMap.get(directory);
        return (existingDeployer != null) ? existingDeployer : null;
    }

    /**
     * Return the registered deployers as a Map
     *
     * @return registered deployers
     */
    public Map<String, Deployer> getDeployers() {
        return this.deployerMap;
    }


    /**
     * Returns the map of deployer directories associated with the registered deployers
     *
     * @return deployerDirectories
     */
    public Map<String, String> getDeployerDirectories() {
        return this.deployerDirectories;
    }

    /**
     * Returns the repository directory that the deployment engine is registered with
     *      Eg: CARBON_HOME/repository/deployment/server
     * @return repository directory
     */
    public File getRepositoryDirectory() {
        return repositoryDirectory;
    }

    /**
     * This will return the keys of the currently deployed artifacts.
     * A key of an artifact is used to uniquely identify it self within a runtime
     *
     * @return the map containing the deployed artifact keys
     */
    public Map<String, Object> getDeployedArtifactKeys() {
        return artifactKeys;
    }


    /**
     * Add given artifact to the list artifacts to deploy
     *
     * @param artifact artifact to deploy
     */
    public synchronized void addArtifactToDeploy(Artifact artifact) {
        Artifact deployedArtifact = deployedArtifacts.get(artifact.getPath());
        if (deployedArtifact != null) {
            if (CarbonDeploymentUtils.isArtifactModified(deployedArtifact)) {
                artifactsToUndeploy.add(deployedArtifact);
                artifactsToDeploy.add(deployedArtifact);
            }
        } else {
            artifactsToDeploy.add(artifact);
            CarbonDeploymentUtils.setArtifactLastModifiedTime(artifact);
        }
        artifactFilePathList.add(artifact.getPath());
    }

    /**
     * Add given artifact to the list fo artifact to be undeployed
     *
     * @param artifact artifact to undeploy
     */
    private synchronized void addArtifactToUndeploy(Artifact artifact) {
        artifactsToUndeploy.add(artifact);
    }

    /**
     * Deploy the artifacts found in the artifacts to be deployed list
     */
    public void deployArtifacts() {
        try {
            if (artifactsToDeploy.size() > 0) {
                for (Object artifact : artifactsToDeploy) {
                    Artifact artifactToDeploy = (Artifact) artifact;
                    try {
                        String directory = deployerDirectories.get(artifactToDeploy.getType());
                        Deployer deployer = getDeployer(directory);
                        Object artifactKey = deployer.deploy(artifactToDeploy);
                        artifactToDeploy.setKey(artifactKey);
                        deployedArtifacts.put(artifactToDeploy.getPath(), artifactToDeploy);
                        artifactKeys.put(artifactToDeploy.getPath(), artifactKey);
                    } catch (CarbonDeploymentException e) {
                        log.error(e);
                    }
                }
            }
        } finally {
            artifactsToDeploy.clear();
        }
    }

    /**
     * UnDeploy the artifacts found in the artifact to be unDeployed list
     */
    public void undeployArtifacts() {
        try {
            if (artifactsToUndeploy.size() > 0) {
                for (Object artifact : artifactsToUndeploy) {
                    Artifact artifactToUnDeploy = (Artifact) artifact;
                    try {
                        String directory = deployerDirectories.get(artifactToUnDeploy.getType());
                        Deployer deployer = getDeployer(directory);
                        deployer.undeploy(artifactToUnDeploy.getKey());
                        deployedArtifacts.remove(artifactToUnDeploy.getPath());
                        artifactKeys.remove(artifactToUnDeploy.getPath());
                    } catch (CarbonDeploymentException e) {
                        log.error(e);
                    }
                }
            }
        } finally {
            artifactsToUndeploy.clear();
        }
    }

    /**
     * Check and add artifacts which are removed form the repository to the artifacts to be
     * unDeployed list
     */
    public void checkUndeployedArtifacts() {
        Iterator artifactPaths = deployedArtifacts.keySet().iterator();
        List toBeRemoved = new ArrayList();
        while (artifactPaths.hasNext()) {
            String filePath = (String) artifactPaths.next();
            Artifact artifact = deployedArtifacts.get(filePath);
            boolean found = false;
            for (Object anArtifactFilePath : artifactFilePathList) {
                String artifactFilePath = (String) anArtifactFilePath;
                if (filePath.equals(artifactFilePath)) {
                    found = true;
                }
            }
            if (!found) {
                toBeRemoved.add(filePath);
                this.addArtifactToUndeploy(artifact);
            }
        }

        for (Object fileNameToBeRemoved : toBeRemoved) {
            String fileName = (String) fileNameToBeRemoved;
            deployedArtifacts.remove(fileName);
        }
        toBeRemoved.clear();
        artifactFilePathList.clear();
    }
}
