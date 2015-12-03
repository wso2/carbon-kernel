/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.kernel.internal.deployment;

import org.wso2.carbon.kernel.deployment.Artifact;
import org.wso2.carbon.kernel.deployment.ArtifactType;
import org.wso2.carbon.kernel.deployment.Deployer;
import org.wso2.carbon.kernel.deployment.DeploymentService;
import org.wso2.carbon.kernel.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.kernel.utils.FileUtils;
import org.wso2.carbon.kernel.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class represent the implementation of deployment, undeployment and redeployment of Carbon Artifacts.
 *
 * @since 5.0.0
 */
public class CarbonDeploymentService implements DeploymentService {

    private DeploymentEngine carbonDeploymentEngine;

    /**
     * This will construct the CarbonDeploymentService using the given DeploymentEngine instance.
     *
     * @param carbonDeploymentEngine the DeploymentEngine instance used with constructing the CarbonDeploymentService
     */
    public CarbonDeploymentService(DeploymentEngine carbonDeploymentEngine) {
        this.carbonDeploymentEngine = carbonDeploymentEngine;
    }

    /**
     * This method will be called externally to deploy an artifact by giving the artifact deployment
     * directory and the path. The consumers of the DeploymentService will be calling this method.
     *
     * @param artifactPath path where the artifact resides. This has to be the full qualified path
     *                     of the artifact
     * @param artifactType the type of the artifact going to be dpeloyed
     *                     Eg : webapp, dataservice, sequence
     * @throws CarbonDeploymentException this will be thrown on error situation while trying deploy the given artifact
     */
    public void deploy(String artifactPath, ArtifactType artifactType)
            throws CarbonDeploymentException {
        Utils.checkSecurity();
        Deployer deployer = carbonDeploymentEngine.getDeployer(artifactType);
        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown artifactType : " + artifactType);
        }
        try {
            Path destination = Paths.get(carbonDeploymentEngine.getRepositoryDirectory().getAbsolutePath(),
                    deployer.getLocation().getFile());
            FileUtils.copyFileToDir(new File(artifactPath), destination.toFile());
        } catch (IOException e) {
            throw new CarbonDeploymentException("Error wile copying artifact", e);
        }
    }

    /**
     * This method will be called externally to undeploy an artifact by giving the artifact deployment
     * directory and the path. The consumers of the DeploymentService will be calling this method.
     *
     * @param key          artifact key to uniquely identify an artifact in a runtime
     *                     Eg: for webapps this can be webapp context such as /foo , /bar, etc
     *                     for service this can be service name such as EchoService, VersionService, etc
     * @param artifactType the type of the artifact going to be deployed
     *                     Eg : webapp, dataservice, sequence
     * @throws CarbonDeploymentException this will be thrown on error situation while trying undeploy the given artifact
     */
    public void undeploy(Object key, ArtifactType artifactType) throws CarbonDeploymentException {
        Utils.checkSecurity();
        Deployer deployer = carbonDeploymentEngine.getDeployer(artifactType);
        if (deployer == null) {
            throw new CarbonDeploymentException("Unknown artifactType : " + artifactType);
        }

        Artifact deployedArtifact = carbonDeploymentEngine.getDeployedArtifact(artifactType, key);
        if (deployedArtifact != null) {
            FileUtils.deleteDir(new File(deployedArtifact.getPath()));
        } else {
            throw new CarbonDeploymentException("Cannot find artifact with key : " + key +
                    " to undeploy");
        }
    }

    /**
     * This method is same as deploy and undeploy, will be called externally to undeploy an artifact by giving
     * the artifact deployment directory and the path. The consumers of the DeploymentService will be calling
     * this method.
     *
     * @param key          artifact key to uniquely identify an artifact in a runtime
     *                     Eg: for webapps this can be webapp context such as /foo , /bar, etc
     *                     for service this can be service name such as EchoService, VersionService, etc
     * @param artifactType the type of the artifact going to be deployed
     *                     Eg : webapp, dataservice, sequence
     * @throws CarbonDeploymentException throws redeployment exceptions
     */
    public void redeploy(Object key, ArtifactType artifactType) throws CarbonDeploymentException {
        // TODO implement this
    }
}
