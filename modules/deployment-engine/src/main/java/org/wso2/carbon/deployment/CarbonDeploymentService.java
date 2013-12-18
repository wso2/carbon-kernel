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
import org.wso2.carbon.deployment.api.DeploymentService;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.spi.Artifact;
import org.wso2.carbon.deployment.spi.Deployer;

import java.io.File;
import java.io.IOException;


public class CarbonDeploymentService implements DeploymentService {

    private CarbonDeploymentEngine carbonDeploymentEngine;

    public CarbonDeploymentService(CarbonDeploymentEngine carbonDeploymentEngine) {
        this.carbonDeploymentEngine = carbonDeploymentEngine;
    }

    @Override
    public void deploy(String deploymentDirectory, String artifactPath)
            throws CarbonDeploymentException {
        Deployer deployer = carbonDeploymentEngine.getDeployer(deploymentDirectory);
        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown deployer for : " + deploymentDirectory);
        }
        String destinationDirectory = carbonDeploymentEngine.getRepositoryDirectory() +
                                      File.separator + deployer.getDirectory();
        try {
            CarbonDeploymentUtils.copyFileToDir(new File(artifactPath),
                                                new File(destinationDirectory));
        } catch (IOException e) {
            throw new CarbonDeploymentException("Error wile copying artifact", e);
        }
    }

    @Override
    public void undeploy(String deploymentDirectory, Object key) throws CarbonDeploymentException {
        Deployer deployer = carbonDeploymentEngine.getDeployer(deploymentDirectory);
        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown deployer for : " + deploymentDirectory);
        }

        Artifact deployedArtifact = carbonDeploymentEngine.getDeployedArtifact(deploymentDirectory,
                                                                               key);
        if (deployedArtifact != null) {
            CarbonDeploymentUtils.deleteDir(new File(deployedArtifact.getPath()));
        } else {
            throw new CarbonDeploymentException("Cannot find artifact with key : " + key +
                                                " to undeploy");
        }

    }

    @Override
    public void redeploy(String deploymentDirectory, Object key) throws CarbonDeploymentException {
        // TODO implement this
    }
}
