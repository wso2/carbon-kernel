package org.wso2.carbon.deployment;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.deployers.CustomDeployer;
import org.wso2.carbon.deployment.exception.DeployerRegistrationException;
import org.wso2.carbon.deployment.exception.DeploymentEngineException;

import java.io.File;

public class DeploymentEngineTest extends BaseTest {
    private final static String CARBON_REPO = "carbon-repo";
    private DeploymentEngine deploymentEngine;
    private CustomDeployer customDeployer;

    /**
     * @param testName
     */
    public DeploymentEngineTest(String testName) {
        super(testName);
    }

    @Test
    public void testCarbonDeploymentEngine() throws DeploymentEngineException {
        deploymentEngine =
                new DeploymentEngine(getTestResourceFile(CARBON_REPO).getAbsolutePath());
        deploymentEngine.start();
    }

    @Test(dependsOnMethods = {"testCarbonDeploymentEngine"})
    public void testAddDeployer() throws DeployerRegistrationException {
        customDeployer = new CustomDeployer();
        deploymentEngine.registerDeployer(customDeployer);
        Assert.assertNotNull(deploymentEngine.getDeployer(customDeployer.getArtifactType()));
    }

    @Test(dependsOnMethods = {"testCarbonDeploymentEngine"})
    public void testRemoveDeployer() throws DeployerRegistrationException {
        deploymentEngine.unRegisterDeployer(customDeployer);
        Assert.assertNull(deploymentEngine.getDeployer(customDeployer.getArtifactType()));
    }

}
