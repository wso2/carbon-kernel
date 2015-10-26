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
 */
public class XMLBasedConfigProviderTest extends BaseTest {

    private XMLBasedConfigProvider xmlBasedConfigProvider;

    public XMLBasedConfigProviderTest(String testName) {
        super(testName);
    }

    @BeforeClass
    public void init() {
        xmlBasedConfigProvider = new XMLBasedConfigProvider();
    }

    @Test
    public void testGetCarbonConfiguration() throws Exception {
        String backupRepoLocation = System.getProperty(Constants.CARBON_REPOSITORY);
        System.setProperty(Constants.CARBON_REPOSITORY, getTestResourceFile("xsd").getAbsolutePath());

        CarbonConfiguration carbonConfiguration = xmlBasedConfigProvider.getCarbonConfiguration();

        Assert.assertEquals(carbonConfiguration.getId(), "carbon-kernel");
        Assert.assertEquals(carbonConfiguration.getName(), "WSO2 Carbon Kernel");
        Assert.assertEquals(carbonConfiguration.getVersion(), "1.2.3");
        Assert.assertEquals(carbonConfiguration.getPortsConfig().getOffset(), 0);

        DeploymentConfig deploymentConfig = carbonConfiguration.getDeploymentConfig();

        Assert.assertEquals(deploymentConfig.getUpdateInterval(), 15);
        Assert.assertEquals(deploymentConfig.getRepositoryLocation(), "test-repo-location");
        Assert.assertEquals(deploymentConfig.getMode(), DeploymentModeEnum.scheduled);

        if (backupRepoLocation != null) {
            System.setProperty(Constants.CARBON_REPOSITORY, backupRepoLocation);
        } else {
            System.clearProperty(Constants.CARBON_REPOSITORY);
        }
    }
}
