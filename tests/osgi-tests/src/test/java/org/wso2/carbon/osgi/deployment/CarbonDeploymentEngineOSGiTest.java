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
package org.wso2.carbon.osgi.deployment;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.deployment.ArtifactType;
import org.wso2.carbon.kernel.deployment.Deployer;
import org.wso2.carbon.kernel.deployment.DeploymentService;
import org.wso2.carbon.kernel.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import javax.inject.Inject;

/**
 * Carbon Deployment Engine OSGi Test case.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CarbonDeploymentEngineOSGiTest {

    private static final Logger logger = LoggerFactory.getLogger(CarbonDeploymentEngineOSGiTest.class);

    @Configuration
    public Option[] createConfiguration() {
        List<Option> optionList = OSGiTestConfigurationUtils.getConfiguration();
        copyCarbonYAML();
        return optionList.toArray(new Option[optionList.size()]);
    }


    @Inject
    private BundleContext bundleContext;

    @Inject
    private DeploymentService deploymentService;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    private static String carbonRepo;
    private static String artifactPath;


    static {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        Path testResourceDir = Paths.get(basedir, "src", "test", "resources");
        carbonRepo = Paths.get(testResourceDir.toString(), "carbon-repo").toString();
        artifactPath = Paths.get(testResourceDir.toString(), "carbon-repo", "text-files", "sample1.txt").toString();
    }

    @Test
    public void testRegisterDeployer() {
        ServiceRegistration serviceRegistration = bundleContext.registerService(Deployer.class.getName(),
                new CustomDeployer(), null);
        ServiceReference reference = bundleContext.getServiceReference(Deployer.class.getName());
        Assert.assertNotNull(reference, "Custom Deployer Service Reference is null");
        CustomDeployer deployer = (CustomDeployer) bundleContext.getService(reference);
        Assert.assertNotNull(deployer, "Custom Deployer Service is null");
        serviceRegistration.unregister();
        reference = bundleContext.getServiceReference(Deployer.class.getName());
        Assert.assertNull(reference, "Custom Deployer Service Reference should be unregistered and null");

        //register faulty deployers
        CustomDeployer deployer1 = new CustomDeployer();
        deployer1.setArtifactType(null);
        bundleContext.registerService(Deployer.class.getName(), deployer1, null);

        CustomDeployer deployer2 = new CustomDeployer();
        deployer2.setLocation(null);
        bundleContext.registerService(Deployer.class.getName(), deployer2, null);
    }

    @Test(dependsOnMethods = {"testRegisterDeployer"})
    public void testDeploymentService() throws CarbonDeploymentException {
        Assert.assertNotNull(deploymentService);
        CustomDeployer customDeployer = new CustomDeployer();
        bundleContext.registerService(Deployer.class.getName(), customDeployer, null);
        //undeploy
        try {
            deploymentService.undeploy(artifactPath, new ArtifactType<>("unknown"));
        } catch (CarbonDeploymentException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown artifactType"));
        }
        try {
            deploymentService.undeploy("fake.path", customDeployer.getArtifactType());
        } catch (CarbonDeploymentException e) {
            Assert.assertEquals(e.getMessage(), "Cannot find artifact with key : fake.path to undeploy");
        }
        try {
            deploymentService.undeploy(artifactPath, customDeployer.getArtifactType());
        } catch (CarbonDeploymentException e) {
            Assert.assertEquals(e.getMessage(), "Cannot find artifact with key : " + artifactPath + " to undeploy");
        }
        //deploy
        try {
            deploymentService.deploy("fake.path", customDeployer.getArtifactType());
        } catch (CarbonDeploymentException e) {
            Assert.assertTrue(e.getMessage().contains("Error wile copying artifact"));
        }
        try {
            deploymentService.deploy(artifactPath, new ArtifactType<>("unknown"));
        } catch (CarbonDeploymentException e) {
            Assert.assertTrue(e.getMessage().contains("Unknown artifactType"));
        }
        deploymentService.deploy(artifactPath, customDeployer.getArtifactType());

        //redeploy - this does not do anything for the moment.
        deploymentService.redeploy(artifactPath, customDeployer.getArtifactType());
    }


    /**
     * Replace the existing carbon.yml file with populated carbon.yml file.
     */
    private static void copyCarbonYAML() {
        Path carbonYAMLFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        try {
            carbonYAMLFilePath = Paths.get(basedir, "src", "test", "resources", "runtime", "carbon.yml");
            Files.copy(carbonYAMLFilePath, Paths.get(System.getProperty("carbon.home"), "conf",
                    "carbon.yml"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to copy the carbon.yml file", e);
        }
    }
}
