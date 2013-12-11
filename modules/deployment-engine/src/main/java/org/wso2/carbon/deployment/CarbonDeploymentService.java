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

import org.wso2.carbon.deployment.spi.Artifact;
import org.wso2.carbon.deployment.spi.Deployer;
import org.wso2.carbon.deployment.api.DeploymentService;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.internal.CarbonDeploymentDataHolder;

import java.io.File;


public class CarbonDeploymentService implements DeploymentService {
    private CarbonDeploymentEngine carbonDeploymentEngine;

    public CarbonDeploymentService(CarbonDeploymentEngine carbonDeploymentEngine) {
        this.carbonDeploymentEngine = carbonDeploymentEngine;
    }

    @Override
    public void deploy(String artifactPath, String artifactType) throws CarbonDeploymentException {
        String deployerDirectory = carbonDeploymentEngine.getDeployerDirectories().get(artifactType);
        if (deployerDirectory == null) {
            throw new CarbonDeploymentException("Unknown artifact type : " + artifactType);
        }
        Deployer deployer = carbonDeploymentEngine.getDeployer(deployerDirectory);
        // TODO : Copy the artifact to the deployment folder
        Artifact artifact = new Artifact(new File(artifactPath));
        deployer.deploy(artifact);
    }

    @Override
    public void undeploy(Object key, String artifactType) throws CarbonDeploymentException {
        // TODO implement this
    }

    @Override
    public void redeploy(Object key, String artifactType) throws CarbonDeploymentException {
        // TODO implement this
    }
}
