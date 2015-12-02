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

package org.wso2.carbon.kernel.internal.config;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.config.model.DeploymentConfig;
import org.wso2.carbon.kernel.config.model.DeploymentModeEnum;
import org.wso2.carbon.kernel.deployment.BaseTest;

/**
 * This class tests the functionality of org.wso2.carbon.kernel.internal.kernel.config.XMLBasedConfigProvider class.
 *
 * @since 5.0.0
 */
public class YAMLBasedConfigProviderTest extends BaseTest {

    private YAMLBasedConfigProvider yamlBasedConfigProvider;

    public YAMLBasedConfigProviderTest(String testName) {
        super(testName);
    }

    @BeforeClass
    public void init() {
        System.setProperty("carbon.home", "/home/siripala/wso2carbon-5.0.0");
        System.setProperty("carbon.version", "1.0.0");
        System.setProperty("carbon.offset", "10");
        yamlBasedConfigProvider = new YAMLBasedConfigProvider();
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Failed populate CarbonConfiguration from.*")
    public void testGetCarbonConfigurationFailScenario() throws Exception {
        System.setProperty(Constants.CARBON_REPOSITORY, getTestResourceFile("wrongPath").getAbsolutePath());
        CarbonConfiguration carbonConfiguration = yamlBasedConfigProvider.getCarbonConfiguration();
    }

    @Test(dependsOnMethods = "testGetCarbonConfigurationFailScenario")
    public void testGetCarbonConfiguration() throws Exception {
        System.setProperty(Constants.CARBON_REPOSITORY, getTestResourceFile("yaml").getAbsolutePath());

        CarbonConfiguration carbonConfiguration = yamlBasedConfigProvider.getCarbonConfiguration();

        Assert.assertEquals(carbonConfiguration.getId(), "carbon-kernel");
        Assert.assertEquals(carbonConfiguration.getName(), "WSO2 Carbon Kernel");

        // Test for system property substitution
        Assert.assertEquals(carbonConfiguration.getVersion(), "1.0.0");

        // Test for system property substitution
        Assert.assertEquals(carbonConfiguration.getPortsConfig().getOffset(), 10);

        DeploymentConfig deploymentConfig = carbonConfiguration.getDeploymentConfig();

        // Test for default values
        Assert.assertEquals(deploymentConfig.getUpdateInterval(), 15);

        // Test for system property substitution
        Assert.assertEquals(deploymentConfig.getRepositoryLocation(),
                "/home/siripala/wso2carbon-5.0.0/repository/deployment/server/");
        Assert.assertEquals(deploymentConfig.getMode(), DeploymentModeEnum.scheduled);
    }

    public void testForDefaultValuesInCarbonConfiguration() throws Exception {
        String backupRepoLocation = System.getProperty(Constants.CARBON_REPOSITORY);
        System.setProperty(Constants.CARBON_REPOSITORY, getTestResourceFile("yaml").getAbsolutePath());
        CarbonConfiguration carbonConfiguration = yamlBasedConfigProvider.getCarbonConfiguration();

        Assert.assertEquals(carbonConfiguration.getPortsConfig().getOffset(), 0, "Port offset value should be 0");

        if (backupRepoLocation != null) {
            System.setProperty(Constants.CARBON_REPOSITORY, backupRepoLocation);
        } else {
            System.clearProperty(Constants.CARBON_REPOSITORY);
        }
    }
}
