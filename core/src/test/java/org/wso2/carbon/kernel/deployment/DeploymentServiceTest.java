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
package org.wso2.carbon.kernel.deployment;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.deployment.deployers.CustomDeployer;
import org.wso2.carbon.kernel.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.kernel.deployment.exception.DeployerRegistrationException;
import org.wso2.carbon.kernel.deployment.exception.DeploymentEngineException;
import org.wso2.carbon.kernel.deployment.service.CustomDeploymentService;
import org.wso2.carbon.kernel.internal.deployment.DeploymentEngine;
import org.wso2.carbon.kernel.utils.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Deployment Service Test class.
 *
 * @since 5.0.0
 */
public class DeploymentServiceTest extends BaseTest {

    private static final String CARBON_REPO = "carbon-repo";
    private static final String DEPLOYER_REPO = "carbon-repo" + File.separator + "text-files";
    private CustomDeploymentService deploymentService;
    private DeploymentEngine deploymentEngine;
    private CustomDeployer customDeployer;
    private String artifactPath;

    /**
     * @param testName
     */
    public DeploymentServiceTest(String testName) {
        super(testName);
    }

    @BeforeTest
    public void setup() throws DeploymentEngineException, DeployerRegistrationException {
        customDeployer = new CustomDeployer();
        artifactPath = getTestResourceFile(DEPLOYER_REPO).getAbsolutePath()
                + File.separator + "sample1.txt";
        deploymentEngine = new DeploymentEngine(getTestResourceFile(CARBON_REPO).getAbsolutePath());
        deploymentEngine.start();
        deploymentEngine.registerDeployer(customDeployer);
    }

    @Test
    public void testDeploymentService() {
        deploymentService = new CustomDeploymentService(deploymentEngine);
    }

    @Test(dependsOnMethods = {"testDeploymentService"})
    public void testDeploy() throws CarbonDeploymentException {
        deploymentService.deploy(artifactPath, customDeployer.getArtifactType());
        Assert.assertTrue(CustomDeployer.sample1Deployed);
    }

    @Test(dependsOnMethods = {"testDeploy"})
    public void testUpdate() throws CarbonDeploymentException {
        deploymentService.redeploy(new File(artifactPath).getName(),
                customDeployer.getArtifactType());
        Assert.assertTrue(CustomDeployer.sample1Updated);
    }

    @Test(dependsOnMethods = {"testUpdate"})
    public void testUndeploy() throws CarbonDeploymentException {
        deploymentService.undeploy(new File(artifactPath).getName(),
                customDeployer.getArtifactType());
        Assert.assertFalse(CustomDeployer.sample1Deployed);
    }

    @AfterTest
    public void cleanupTempfile() throws IOException {
        FileUtils.deleteDir(new File(getTestResourceFile(CARBON_REPO).getAbsolutePath() +
                File.separator + "file:text-files"));
    }
}
