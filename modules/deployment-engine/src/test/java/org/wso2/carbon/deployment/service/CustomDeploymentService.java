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

package org.wso2.carbon.deployment.service;

import org.wso2.carbon.deployment.Artifact;
import org.wso2.carbon.deployment.ArtifactType;
import org.wso2.carbon.deployment.DeploymentEngine;
import org.wso2.carbon.deployment.api.DeploymentService;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.spi.Deployer;

import java.io.File;
import java.util.ArrayList;


public class CustomDeploymentService implements DeploymentService {

    private DeploymentEngine deploymentEngine;

    public CustomDeploymentService(DeploymentEngine deploymentEngine) {
        this.deploymentEngine = deploymentEngine;
    }
    @Override
    public void deploy(String artifactPath, ArtifactType artifactType)
            throws CarbonDeploymentException {
        Artifact artifact = new Artifact(new File(artifactPath));
        artifact.setType(artifactType);
        ArrayList artifactList = new ArrayList();
        artifactList.add(artifact);
        Deployer deployer = deploymentEngine.getDeployer(artifactType);

        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown artifactType : " + artifactType);
        }
        deploymentEngine.deployArtifacts(artifactList);
    }

    @Override
    public void undeploy(Object key, ArtifactType artifactType) throws CarbonDeploymentException {
        Deployer deployer = deploymentEngine.getDeployer(artifactType);

        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown artifactType : " + artifactType);
        }
        deployer.undeploy(key);
    }

    @Override
    public void redeploy(Object key, ArtifactType artifactType) throws CarbonDeploymentException {
        Deployer deployer = deploymentEngine.getDeployer(artifactType);
        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown artifactType : " + artifactType);
        }

        Artifact deployedArtifact = deploymentEngine.getDeployedArtifact(artifactType, key);

        if (deployedArtifact == null) {
            throw new CarbonDeploymentException("Cannot find artifact with the key : " + key);
        }

        deployer.update(deployedArtifact);
    }
}
