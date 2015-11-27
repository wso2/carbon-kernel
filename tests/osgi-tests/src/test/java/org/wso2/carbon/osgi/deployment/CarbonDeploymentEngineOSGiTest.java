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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.deployment.Deployer;
import org.wso2.carbon.kernel.deployment.DeploymentService;
import org.wso2.carbon.kernel.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.kernel.deployment.exception.DeployerRegistrationException;
import org.wso2.carbon.kernel.deployment.exception.DeploymentEngineException;
import org.wso2.carbon.kernel.internal.deployment.DeploymentEngine;
import org.wso2.carbon.osgi.util.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Dictionary;
import java.util.Hashtable;
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
        Utils.setCarbonHome();
        Utils.setupMavenLocalRepo();
        copyCarbonYAML();
        return Utils.getDefaultPaxOptions();
    }


    @Inject
    private BundleContext bundleContext;

    @Inject
    private DeploymentService deploymentService;

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
            deploymentService.undeploy(artifactPath, customDeployer.getArtifactType());
        } catch (CarbonDeploymentException e) {
            Assert.assertEquals(e.getMessage(), "Cannot find artifact with key : " + artifactPath + " to undeploy");
        }
        //deploy
        deploymentService.deploy(artifactPath, customDeployer.getArtifactType());

        //redeploy - this does not do anything for the moment.
        deploymentService.redeploy(artifactPath, customDeployer.getArtifactType());
    }


    @Test(dependsOnMethods = {"testDeploymentService"})
    public void testDeploymentEngine() throws DeploymentEngineException, InvalidSyntaxException,
            CarbonDeploymentException, DeployerRegistrationException {
        DeploymentEngine deploymentEngine = new DeploymentEngine(carbonRepo);
        CustomDeploymentService customDeploymentService = new CustomDeploymentService(deploymentEngine);
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("ServiceType", "Custom");

        ServiceRegistration serviceRegistration = bundleContext.registerService(DeploymentService.class,
                customDeploymentService, properties);

        String filter = "(ServiceType=Custom)";
        ServiceReference<?>[] references = bundleContext.getServiceReferences(DeploymentService.class.getName(),
                filter);
        Assert.assertNotNull(references, "Custom Deployment Service Reference is null");

        CustomDeployer customDeployer = new CustomDeployer();
        deploymentEngine.registerDeployer(customDeployer);
        //deploy
        customDeploymentService.deploy(artifactPath, customDeployer.getArtifactType());
        //un-register
        deploymentEngine.unregisterDeployer(customDeployer);
        serviceRegistration.unregister();
    }

    /**
     * Replace the existing carbon.yml file with populated carbon.xml file.
     */
    private static void copyCarbonYAML() {
        Path carbonYAMLFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        try {
            carbonYAMLFilePath = Paths.get(basedir, "src", "test", "resources", "runtime", "carbon.yml");
            Files.copy(carbonYAMLFilePath, Paths.get(System.getProperty("carbon.home"), "repository", "conf",
                    "carbon.yml"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to copy the carbon.yml file", e);
        }
    }
}
