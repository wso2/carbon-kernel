/*
 * Copyright 2015 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.osgi.deployment;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.api.DeploymentService;
import org.wso2.carbon.deployment.exception.CarbonDeploymentException;
import org.wso2.carbon.deployment.spi.Deployer;

import java.io.File;
import javax.inject.Inject;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CarbonDeploymentEngineOSGiTest {

    private static final String DEPLOYER_REPO = "carbon-repo" + File.separator + "text-files";

    @Inject
    private BundleContext bundleContext;

    @Inject
    private DeploymentService deploymentService;

    private static CustomDeployer customDeployer;

    private static String artifactPath;


    static {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
        String testResourceDir = basedir + File.separator + "src" + File.separator + "test" +
                File.separator + "resources";
        customDeployer = new CustomDeployer();
        artifactPath = testResourceDir + File.separator + DEPLOYER_REPO + File.separator + "sample1.txt";
    }

    @Test
    public void testRegisterDeployer() {
        ServiceRegistration serviceRegistration = bundleContext.registerService(Deployer.class.getName(),
                customDeployer, null);
        ServiceReference reference = bundleContext.getServiceReference(Deployer.class.getName());
        Assert.assertNotNull(reference, "Custom Deployer Service Reference is null");
        CustomDeployer deployer = (CustomDeployer) bundleContext.getService(reference);
        Assert.assertNotNull(deployer, "Custom Deployer Service is null");
        serviceRegistration.unregister();
        reference = bundleContext.getServiceReference(Deployer.class.getName());
        Assert.assertNull(reference, "Custom Deployer Service Reference should be unregistered and null");
    }

    @Test(dependsOnMethods = {"testRegisterDeployer"})
    public void testDeploymentService() throws CarbonDeploymentException {
        Assert.assertNotNull(deploymentService);
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
}
