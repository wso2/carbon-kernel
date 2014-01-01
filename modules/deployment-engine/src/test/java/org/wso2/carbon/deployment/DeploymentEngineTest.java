package org.wso2.carbon.deployment;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.deployers.CustomDeployer;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;

import java.io.File;

public class DeploymentEngineTest extends BaseTest {
    private final static String CARBON_REPO = "carbon-repo";
    private final static String DEPLOYER_REPO = "carbon-repo" + File.separator + "text-files";
    private DeploymentEngine deploymentEngine;
    private CustomDeployer customDeployer;

    /**
     * @param testName
     */
    public DeploymentEngineTest(String testName) {
        super(testName);
    }

    @Test
    public void testCarbonDeploymentEngine() throws CarbonDeploymentException {
        deploymentEngine =
                new DeploymentEngine(getTestResourceFile(CARBON_REPO).getAbsolutePath());
        deploymentEngine.start();
    }

    @Test(dependsOnMethods = {"testCarbonDeploymentEngine"})
    public void testAddDeployer() throws CarbonDeploymentException {
        customDeployer = new CustomDeployer();
        deploymentEngine.registerDeployer(customDeployer);
        Assert.assertNotNull(deploymentEngine.getDeployer(customDeployer.getArtifactType()));
    }

    @Test(dependsOnMethods = {"testCarbonDeploymentEngine"})
    public void testRemoveDeployer() {
        deploymentEngine.unRegisterDeployer(customDeployer);
        Assert.assertNull(deploymentEngine.getDeployer(customDeployer.getArtifactType()));
    }

}
