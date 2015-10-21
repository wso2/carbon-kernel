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
package org.wso2.carbon.deployment;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.deployers.CustomDeployer;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;

import java.io.File;
import java.util.HashMap;

public class DeployerTest extends BaseTest {
    private static final String DEPLOYER_REPO = "carbon-repo" + File.separator + "text-files";
    private CustomDeployer customDeployer;
    private Artifact artifact;
    private String key;

    /**
     * @param testName
     */
    public DeployerTest(String testName) {
        super(testName);
    }

    @BeforeTest
    public void setup() throws CarbonDeploymentException {
        customDeployer = new CustomDeployer();
        customDeployer.init();
        artifact = new Artifact(new File(getTestResourceFile(DEPLOYER_REPO).getAbsolutePath()
                + File.separator + "sample1.txt"));
        artifact.setVersion("1.0.0");
        artifact.setProperties(new HashMap<String, Object>());
    }

    @Test
    public void testDeployerInitialization() {
        Assert.assertTrue(CustomDeployer.initCalled);
    }

    @Test(dependsOnMethods = {"testDeployerInitialization"})
    public void testDeploy() throws CarbonDeploymentException {
        key = customDeployer.deploy(artifact);
        Assert.assertTrue(CustomDeployer.sample1Deployed);
    }

    @Test(dependsOnMethods = {"testDeploy"})
    public void testUpdate() throws CarbonDeploymentException {
        key = customDeployer.update(artifact);
        Assert.assertTrue(CustomDeployer.sample1Updated);
    }

    @Test(dependsOnMethods = {"testUpdate"})
    public void testUnDeploy() throws CarbonDeploymentException {
        customDeployer.undeploy(key);
        Assert.assertFalse(CustomDeployer.sample1Deployed);
    }
}
