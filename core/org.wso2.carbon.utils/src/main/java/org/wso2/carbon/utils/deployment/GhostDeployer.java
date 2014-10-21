/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.utils.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.*;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.*;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is an extension of Axis2 Deployment Engine which will handle all Deployers registered
 * here. Even though this extends the AbstractDeployer, functionality of this deployer goes
 * beyond a normal Axis2 deployer.
 *
 * If this deployer is used, this should be registered in the parent Axis2 engine for all types of
 * sub deployers registered here. So that when a service deployment event comes into Axis2
 * deployment engine, it will call this deployer.
 *
 * When the GhostDeployer gets a service deployment event for the first time, it will create a
 * Ghost metadata file and store it in the file system and let the actual service to be available
 * in the ConfigurationContext. But when reloading an unloaded service, it will inject a Ghost
 * service into the ConfigurationContext and won't deploy the actual service.
 */
public class GhostDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(GhostDeployer.class);

    /**
     * There can be many deployers for the same directory with different extensions. Therefore, we
     * use an extension map inside the deployer map. In the top level map, the deployment directory
     * name is used as the key. In the inner map, each deployer should be registered with the file
     * extension as the key.
     */
    private Map<String, Map<String, Deployer>> deployerMap =
            new HashMap<String, Map<String, Deployer>>();
    private List<DeploymentFileData> deploymentData = new ArrayList<DeploymentFileData>();

    private Lock deployerLock = new ReentrantLock();
    private Lock fileDataLock = new ReentrantLock();
    private boolean initialized = false;
    private boolean cleanedUp = false;

    private static final String DIRECTORY_DEPLOYER_KEY = ".";

    private AxisConfiguration axisConfig = null;
    private ConfigurationContext configCtx = null;

    public void init(ConfigurationContext configCtx) {
        if (initialized) {
            return;
        }
        this.configCtx = configCtx;
        this.axisConfig = configCtx.getAxisConfiguration();
        /**
         * Axis2 DeploymentEngine has made the AAR deployer a special case by hardcoding it.
         * ServiceDeployer is not registered as a normal deployer. But when extending the
         * deployment process by introducing the Ghost Deployer, we have to add the Service
         * Deployer as follows.
         */
        addDeployer(new ServiceDeployer(), CarbonUtils.getAxis2ServicesDir(axisConfig), ".aar");

        // initialize all deployers
        for (Map<String, Deployer> extensionMap : deployerMap.values()) {
            for (Deployer deployer : extensionMap.values()) {
                deployer.init(configCtx);
            }
        }
        initialized = true;
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String absoluteFilePath = deploymentFileData.getAbsolutePath();
        String directoryName = calculateDirectoryName(absoluteFilePath);

        if(log.isDebugEnabled()){
            log.debug("Ghost Deployer Deploying Artifact : " + absoluteFilePath);
        }
        /*if(!"servicemetafiles".equals(directoryName) && !"modulemetafiles".equals(directoryName)){
            log.info("Ghost Deployer Deploying Artifact : " + absoluteFilePath);
        } else {
        }*/

        // First extract out the file extension and the deployment folder
        String fileExtension = getFileExtension(deploymentFileData.getFile());

        // Now we can decide what is the correct deployer
        Deployer deployer = getDeployer(directoryName, fileExtension);
        if (deployer == null) {
            log.error("Matching deployer can't be found for the deployment file : " +
                    absoluteFilePath);
            return;
        }
        deployer.setDirectory(directoryName);
        deploymentFileData.setDeployer(deployer);

        // Check the ghost file
        File ghostFile = GhostDeployerUtils.getGhostFile(absoluteFilePath, axisConfig);
        if (ghostFile == null || !ghostFile.exists()) {
            // ghost file is not found. so this is a new service and we have to deploy it
            deploymentFileData.deploy();
            // iterate all deployed services and find the deployed service
            Set<Map.Entry<String, AxisService>> services = axisConfig.getServices().entrySet();
            for (Map.Entry<String, AxisService> entry : services) {
                String tempAbsolutePath = null;
                AxisService service = entry.getValue();
                // we ignore Admin Services
                if (CarbonUtils.isFilteredOutService(service)) {
                    continue;
                }
                if (service.getFileName() != null) {
                    tempAbsolutePath = GhostDeployer.separatorsToUnix(absoluteFilePath);
                    File serviceFilePathUrlToFile = new File(service.getFileName().getPath());
                    String serviceFilePathUrlToFileAbsolutePath = serviceFilePathUrlToFile.getAbsolutePath();
                    String serviceFileAbsolutePathToUnix = GhostDeployer.separatorsToUnix(serviceFilePathUrlToFileAbsolutePath);

                    if (serviceFileAbsolutePathToUnix
                            .equals(tempAbsolutePath)) {
                    GhostDeployerUtils.updateLastUsedTime(service);
                    try {
                        //skip ghost metafile generation for worker nodes.
                        if (!CarbonUtils.isWorkerNode()) {
                            GhostDeployerUtils.serializeServiceGroup((AxisServiceGroup)
                                    service.getParent(), axisConfig, deploymentFileData);
                        }
                    } catch (Exception ex) {
                        log.error("Error while adding parameter into service : " +
                                service.getName(), ex);
                    }
                    break;
                }
            }
            }
        } else {
            // load the ghost service group
            try {
                AxisServiceGroup ghostServiceGroup = GhostDeployerUtils.createGhostServiceGroup(
                        axisConfig, ghostFile, deploymentFileData.getFile().toURI().toURL());
                axisConfig.addServiceGroup(ghostServiceGroup);
            } catch (Exception e) {
                String msg = "Error while loading the Ghost Service : " +
                        ghostFile.getAbsolutePath();
                log.error(msg, e);
                throw new DeploymentException(msg, e);
            }
        }

        // we have to store the fileData object to deploy the service later
        fileDataLock.lock();
        try {
            deploymentData.add(deploymentFileData);
        } finally {
            fileDataLock.unlock();
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        if (fileName == null) {
            return;
        }
        // Remove the corresponding ghost file
        File ghostFile = GhostDeployerUtils.getGhostFile(fileName, axisConfig);
        if (ghostFile != null && ghostFile.exists() && !ghostFile.delete()) {
            log.error("Error while deleting ghost service file : " + ghostFile.getAbsolutePath());
        }
        if (log.isDebugEnabled()) {
            log.debug("Going to undeploy file: " + fileName);
        }
        removeFileData(fileName);
        // Find the correct deployer and call the undeploy method

        File deployementFile = new File(fileName);
        String extension = getFileExtension(deployementFile);
        Deployer deployer = getDeployer(calculateDirectoryName(fileName), extension);
        if (deployer != null) {
            log.info("Undeploying file : " + fileName);
            /*if((fileName.contains("servicemetafiles") || fileName.contains("modulemetafiles") &&
                log.isDebugEnabled())){
                log.debug("Undeploying file : " + fileName);
            } else {
            }*/
            deployer.undeploy(fileName);
        }
    }

    public void cleanup () throws DeploymentException {
        if (cleanedUp) {
            return;
        }
        for (Map<String, Deployer> extensionMap : this.deployerMap.values()) {
            for (Deployer deployer : extensionMap.values()) {
                try {
                    deployer.cleanup();
                } catch (DeploymentException e) {
                    log.error("Error occurred while cleaning up deployer", e);
                }
            }
        }
        cleanedUp = true;
    }

    public void setDirectory(String directory) {
        // ghost deployer is common for all real deployers. so we don't have one directory
    }

    public void setExtension(String extension) {
        // ghost deployer is common for all real deployers. so we don't have one directory
    }

    public DeploymentFileData getFileData(String fileName) {
        for (DeploymentFileData data : deploymentData) {
            if (fileName.equals(data.getFile().getPath())) {
                return data;
            }
        }
        return null;
    }

    public void removeFileData(DeploymentFileData fileData) {
        fileDataLock.lock();
        try {
            deploymentData.remove(fileData);
        } finally {
            fileDataLock.unlock();
        }
    }

    public void addFileData(DeploymentFileData deploymentFileData){
        // we have to store the fileData object to deploy the service later
        fileDataLock.lock();
        try {
            deploymentData.add(deploymentFileData);
        } finally {
            fileDataLock.unlock();
        }
    }

    public void removeFileData(String filePath) {
        if (filePath == null) {
            return;
        }
        fileDataLock.lock();
        try {
            DeploymentFileData dataToRemove = null;
            for (DeploymentFileData d : deploymentData) {
                if (filePath.equals(d.getAbsolutePath())) {
                    dataToRemove = d;
                    break;
                }
            }
            if (dataToRemove != null) {
                deploymentData.remove(dataToRemove);
            }
        } finally {
            fileDataLock.unlock();
        }
    }

    public Deployer getDeployer(String directory, String extension) {
        Map<String, Deployer> extensionMap = deployerMap.get(directory);
        if(extension == null) {
            extension = DIRECTORY_DEPLOYER_KEY;
        }
        return (extensionMap != null) ? extensionMap.get(extension) : null;
    }

    /**
     * Add a new Deployer.
     *
     * @param deployer  Deployer object to be registered
     * @param directory the directory which will be scanned for deployable artifacts
     * @param extension the extension of the deployable artifacts for this Deployer
     */
    public void addDeployer(Deployer deployer, String directory, String extension) {
        if (deployer == null || directory == null) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to add Deployer : Couldn't find needed information..");
            }
            return;
        }
        String deployerKey;
        //we use extension as "" when deployer is registered on a directory
        if(extension == null) {
            deployerKey = DIRECTORY_DEPLOYER_KEY;
        } else {
            // A leading dot is redundant, so strip it.  So we allow either ".foo" or "foo", either
            // of which will result in extension="foo"
            deployerKey = extension;
            if (extension.charAt(0) == '.') {
                deployerKey = extension.substring(1);
            }
        }
        // if the ghost deployer is already initialized, we have to init the new deployer
        if (initialized) {
            deployer.init(configCtx);
        }

        deployerLock.lock();
        try {
            Map<String, Deployer> extensionMap = deployerMap.get(directory);
            if (extensionMap == null) {
                extensionMap = new HashMap<String, Deployer>();
                deployerMap.put(directory, extensionMap);
            }
            extensionMap.put(deployerKey, deployer);
        } finally {
            deployerLock.unlock();
        }
    }

    /**
     * Remove any Deployer mapped for the given directory and extension
     *
     * @param directory the directory of deployables
     * @param extension the extension of deployables
     */
    public void removeDeployer(String directory, String extension) {
        if (directory == null) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to remove Deployer : Couldn't find needed information..");
            }
            return;
        }

        if(extension == null) {
            extension = DIRECTORY_DEPLOYER_KEY;
        }

        Map<String, Deployer> extensionMap = deployerMap.get(directory);
        if (extensionMap == null) {
            return;
        }

        deployerLock.lock();
        try {
            if (extensionMap.containsKey(extension)) {
                Deployer deployer = extensionMap.remove(extension);
                if (extensionMap.isEmpty()) {
                    deployerMap.remove(directory);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Deployer " + deployer.getClass().getName() + " is removed");
                }
            }
        } finally {
            deployerLock.unlock();
        }
    }

    private String calculateDirectoryName(String servicePathStr) {
        String pathPrefix = "";
        if (servicePathStr != null && servicePathStr.contains("\\")) {
            pathPrefix = "/";
        }
        String servicePath = separatorsToUnix(servicePathStr);
        if (servicePath == null) {
            return null;
        }
        String repoPath = axisConfig.getRepository().getPath();
        String dirName = null;
        servicePath = pathPrefix + servicePath;
        if (servicePath.startsWith(repoPath)) {
            dirName = servicePath.substring(repoPath.length());
            if (dirName.startsWith("/")) {
                dirName = dirName.substring(1);
            }
            if (dirName.contains("/")) {
                dirName = dirName.substring(0, dirName.indexOf("/"));
            }
        } else {
            // These are the deployers which are not in the Axis2 repository
            if (servicePath.lastIndexOf(File.separator) != -1) {
                dirName = servicePath.substring(0, servicePath.lastIndexOf("/"));
            }
        }
        return dirName;
    }

    private String getFileExtension(File deploymentFile) {
        if (deploymentFile.isDirectory()) {
            return null;
        }
        String fileName = deploymentFile.getName();
        int index = fileName.lastIndexOf('.');
        String fileExtension = null;
        if (index != -1) {
            fileExtension = fileName.substring(index + 1);
        }
        return fileExtension;
    }

    public static String  separatorsToUnix(String path) {
        if (path == null || !path.contains("\\")) {
            return path;
        }
        return path.replace("\\", "/");

    }

}
