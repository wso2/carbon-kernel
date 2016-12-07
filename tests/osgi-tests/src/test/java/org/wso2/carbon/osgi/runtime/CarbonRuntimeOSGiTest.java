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
import org.ops4j.pax.exam.ExamFactory;
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
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.CarbonRuntime;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.configprovider.utils.ConfigurationUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import javax.inject.Inject;

import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

/**
 * CarbonRuntimeOSGiTest class is to test the availability and the functionality of the Carbon Runtime Service.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class CarbonRuntimeOSGiTest {

    private static final Logger logger = LoggerFactory.getLogger(CarbonRuntimeOSGiTest.class);
    private static final String CARBON_RUNTIME_SERVICE = CarbonRuntime.class.getName();

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonRuntime carbonRuntime;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[] { copyCarbonYAMLOption() };
    }

    @Test
    public void testCarbonRuntimeService() {
        Assert.assertNotNull(carbonRuntime, "Carbon Runtime Service is null");

        CarbonConfiguration carbonConfiguration = carbonRuntime.getConfiguration();
        Assert.assertNotNull(carbonConfiguration, "Carbon Configuration is null");
    }

    @Test(dependsOnMethods = { "testCarbonRuntimeService" })
    public void testCarbonConfiguration() {

        CarbonConfiguration carbonConfiguration = getCarbonConfiguration();
        Assert.assertEquals(carbonConfiguration.getId(), "carbon-kernel");
        Assert.assertEquals(carbonConfiguration.getName(), "WSO2 Carbon Kernel");
        Properties properties = ConfigurationUtils.loadProjectProperties();
        Assert.assertEquals(carbonConfiguration.getVersion(), properties.getProperty(Constants.MAVEN_PROJECT_VERSION));

        Assert.assertEquals(carbonConfiguration.getPortsConfig().getOffset(), 0);

        Assert.assertEquals(carbonConfiguration.getStartupResolverConfig().getCapabilityListenerTimer().getDelay(),
                200);
        Assert.assertEquals(carbonConfiguration.getStartupResolverConfig().getCapabilityListenerTimer().getPeriod(),
                200);

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
     * Replace the existing deployment.yaml file with populated deployment.yaml file.
     */
    private Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "runtime", "deployment.yaml");
        return copyFile(carbonYmlFilePath, Paths.get("conf", "deployment.yaml"));
    }
}
