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
package org.wso2.carbon.osgi.runtime;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.config.model.DeploymentModeEnum;
import org.wso2.carbon.osgi.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.inject.Inject;

/**
 * CarbonRuntimeOSGiTest class is to test the availability and the functionality of the Carbon Runtime Service.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class CarbonRuntimeOSGiTest {

    private static final Logger logger = LoggerFactory.getLogger(CarbonRuntimeOSGiTest.class);
    private static final String CARBON_RUNTIME_SERVICE = CarbonRuntime.class.getName();

    @Inject
    private BundleContext bundleContext;

    @Configuration
    public Option[] createConfiguration() {
        Utils.setCarbonHome();
        Utils.setupMavenLocalRepo();
        copyCarbonYAML();
        return Utils.getDefaultPaxOptions();
    }

    @Test
    public void testCarbonRuntimeService() {

        ServiceReference reference = bundleContext.getServiceReference(CARBON_RUNTIME_SERVICE);
        Assert.assertNotNull(reference, "Carbon Runtime Service Reference is null");

        CarbonRuntime carbonRuntime = (CarbonRuntime) bundleContext.getService(reference);
        Assert.assertNotNull(carbonRuntime, "Carbon Runtime Service is null");

        CarbonConfiguration carbonConfiguration = carbonRuntime.getConfiguration();
        Assert.assertNotNull(carbonConfiguration, "Carbon Configuration is null");
    }

    @Test(dependsOnMethods = { "testCarbonRuntimeService" })
    public void testCarbonConfiguration() {

        CarbonConfiguration carbonConfiguration = getCarbonConfiguration();
        Assert.assertEquals(carbonConfiguration.getId(), "carbon-kernel");
        Assert.assertEquals(carbonConfiguration.getName(), "WSO2 Carbon Kernel");
        Assert.assertEquals(carbonConfiguration.getVersion(), "5.0.0");

        Assert.assertEquals(carbonConfiguration.getPortsConfig().getOffset(), 0);

        Assert.assertEquals(carbonConfiguration.getDeploymentConfig().getMode(),
                DeploymentModeEnum.fromValue("scheduled"));
        Assert.assertEquals(carbonConfiguration.getDeploymentConfig().getUpdateInterval(), 15);
        String deploymentPath = Paths.get(System.getProperty("carbon.home"), "repository", "deployment",
                "server").toString();
        Assert.assertEquals(Paths.get(carbonConfiguration.getDeploymentConfig().getRepositoryLocation()).toString(),
                deploymentPath);

    }

    /**
     * @return Carbon Configuration reference
     */
    private CarbonConfiguration getCarbonConfiguration() {
        ServiceReference reference = bundleContext.getServiceReference(CARBON_RUNTIME_SERVICE);
        CarbonRuntime carbonRuntime = (CarbonRuntime) bundleContext.getService(reference);
        return carbonRuntime.getConfiguration();
    }

    /**
     * Replace the existing carbon.yml file with populated carbon.yml file.
     */
    private static void copyCarbonYAML() {
        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        try {
            carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "runtime", "carbon.yml");
            Files.copy(carbonYmlFilePath, Paths.get(System.getProperty("carbon.home"), "repository", "conf",
                    "carbon.yml"), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to copy the carbon.yml file", e);
        }
    }
}
