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

import org.wso2.carbon.deployment.Artifact;
import org.wso2.carbon.deployment.ArtifactType;
import org.wso2.carbon.deployment.api.DeploymentService;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.spi.Deployer;
import org.wso2.carbon.utils.FileUtils;

import java.io.File;
import java.io.IOException;


public class CarbonDeploymentService implements DeploymentService {

    private DeploymentEngine carbonDeploymentEngine;

    public CarbonDeploymentService(DeploymentEngine carbonDeploymentEngine) {
        this.carbonDeploymentEngine = carbonDeploymentEngine;
    }

    public void deploy(String artifactPath, ArtifactType artifactType)
            throws CarbonDeploymentException {
        Deployer deployer = carbonDeploymentEngine.getDeployer(artifactType);
        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown artifactType : " + artifactType);
        }
        String destinationDirectory = carbonDeploymentEngine.getRepositoryDirectory() +
                                      File.separator + deployer.getLocation();
        try {
            FileUtils.copyFileToDir(new File(artifactPath),
                                    new File(destinationDirectory));
        } catch (IOException e) {
            throw new CarbonDeploymentException("Error wile copying artifact", e);
        }
    }

    public void undeploy(Object key, ArtifactType artifactType) throws CarbonDeploymentException {
        Deployer deployer = carbonDeploymentEngine.getDeployer(artifactType);
        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown artifactType : " + artifactType);
        }

        Artifact deployedArtifact = carbonDeploymentEngine.getDeployedArtifact(artifactType,
                                                                               key);
        if (deployedArtifact != null) {
            FileUtils.deleteDir(new File(deployedArtifact.getPath()));
        } else {
            throw new CarbonDeploymentException("Cannot find artifact with key : " + key +
                                                " to undeploy");
        }
    }

    public void redeploy(Object key, ArtifactType artifactType) throws CarbonDeploymentException {
        // TODO implement this
    }
}
