package org.wso2.carbon.deployment;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.deployers.CustomDeployer;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;

import java.io.File;

public class CarbonDeploymentEngineTest extends BaseTest {
    private final static String CARBON_REPO = "carbon-repo";
    private final static String DEPLOYER_REPO = "carbon-repo" + File.separator + "text-files";
    private CarbonDeploymentEngine deploymentEngine;
    private CustomDeployer customDeployer;

    /**
     * @param testName
     */
    public CarbonDeploymentEngineTest(String testName) {
        super(testName);
    }

    @Test
    public void testCarbonDeploymentEngine() throws CarbonDeploymentException {
        deploymentEngine =
                new CarbonDeploymentEngine(getTestResourceFile(CARBON_REPO).getAbsolutePath());
        deploymentEngine.start();
    }

    @Test(dependsOnMethods = {"testCarbonDeploymentEngine"})
    public void testAddDeployer() {
        customDeployer = new CustomDeployer();
        String deployerDirectory = getTestResourceFile(CARBON_REPO).getAbsolutePath() +
                                   File.separator + customDeployer.getDirectory();
        deploymentEngine.registerDeployer(customDeployer, deployerDirectory);
    }

    @Test(dependsOnMethods = {"testCarbonDeploymentEngine"})
    public void testCarbonDeploymentService() throws CarbonDeploymentException {
        CarbonDeploymentService service = new CarbonDeploymentService(deploymentEngine);
        String artifactPath = getTestResourceFile(DEPLOYER_REPO).getAbsolutePath()
                              + File.separator + "sample1.txt";
        service.deploy(artifactPath, "txt");
        Assert.assertTrue(CustomDeployer.sample1Deployed);
    }

    @Test(dependsOnMethods = {"testCarbonDeploymentService"})
    public void testRemoveDeployer() {
        String deployerDirectory = CARBON_REPO + File.separator + customDeployer.getDirectory();
        deploymentEngine.unRegisterDeployer(deployerDirectory);
    }

}
