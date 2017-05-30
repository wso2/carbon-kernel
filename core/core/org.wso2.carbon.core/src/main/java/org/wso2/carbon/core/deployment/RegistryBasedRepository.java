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
package org.wso2.carbon.core.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.synchronization.RegistrySynchronizer;
import org.wso2.carbon.registry.synchronization.SynchronizationException;

import java.io.InputStream;


/**
 * This class is responsible for fetching a repository from the registry, and uploading/deleting
 * artifacts from Registry.
 */
public class RegistryBasedRepository {

    private static Log log = LogFactory.getLog(RegistryBasedRepository.class);

    private UserRegistry configSystemRegistry;

    /**
     * path of the repository in registry
     */
    private String registryPath;

    /**
     * starting path in the filesystem
     */
    private String fileSystemRepo;

    public RegistryBasedRepository(UserRegistry configSystemRegistry,
                                   String registryPath,
                                   String fileSystemRepo) {
        this.configSystemRegistry = configSystemRegistry;
        this.registryPath = registryPath;
        this.fileSystemRepo = fileSystemRepo;
        try {
            if (!configSystemRegistry.resourceExists(registryPath)) {
                configSystemRegistry.put(registryPath, configSystemRegistry.newCollection());    
            }
        } catch (RegistryException e) {
            log.error("Could not create registry path " + registryPath, e);
        }
    }

    /**
     * Copies the repository in the given path to the local file system and returns the path.
     */
    public void updateFileSystemFromRegistry() {
        try {
            if (RegistrySynchronizer.isCheckedOut(fileSystemRepo)) {
                // Get changes from Registry to local file system
                RegistrySynchronizer.update(configSystemRegistry, fileSystemRepo, true);
                // Upload changes in local file system to Registry
                try {
                    RegistrySynchronizer.checkIn(configSystemRegistry, fileSystemRepo, false);
                } catch (SynchronizationException e) {   // TODO: Handle exception
                    log.error("Error on Registry repo synchronization", e);
                }
            } else {
                RegistrySynchronizer.checkOut(configSystemRegistry, fileSystemRepo, registryPath);
            }
        } catch (Exception e) {
            log.error("Error dumping repository from registry.", e);
        }
    }

    /**
     * Stores an artifact in the repository which is in Registry
     *
     * @param artifactStream - input stream of the artifact
     * @param resourcePath   - path to store
     * @throws Exception - error in accessing registry
     */
    public void storeArtifactToRegistry(InputStream artifactStream,
                                        String resourcePath) throws Exception {
        Resource artifactResource = configSystemRegistry.newResource();
        artifactResource.setContentStream(artifactStream);
        configSystemRegistry.put(resourcePath, artifactResource);
    }

    /**
     * When a path to an artifact which is in the file system repository is given, deletes that
     * artifact from the registry based repository.
     *
     * @param fileSystemPath - absolute path of the artifact in the file system
     */
    public void deleteArtifactFromRegistry(String fileSystemPath) {
        // TODO: Fix me

       /* String registryRepoPath = CarbonUtils.getRegistryRepoPath();
        if (registryRepoPath != null && registryRepoPath.lastIndexOf('/') != -1) {
            //get the repo dir from registry repo path
            String repoDir = registryRepoPath.substring(registryRepoPath.lastIndexOf('/') + 1);

            //compute the resource path
            String[] temp = fileSystemPath.split(repoDir);
            String resourcePath = registryRepoPath + temp[temp.length - 1];

            //delete the artifact from registry
            try {
                Registry registry = CarbonCoreServiceComponent
                        .getRegistryService().getConfigSystemRegistry();
                if (registry.resourceExists(resourcePath)) {
                    registry.delete(resourcePath);
                }
            } catch (Exception e) {
                log.error("Can't delete artifact from registry.", e);
            }
        }*/
    }

}
