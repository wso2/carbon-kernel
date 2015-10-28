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
package org.wso2.carbon.kernel.internal.context;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.deployment.exception.DeployerRegistrationException;
import org.wso2.carbon.kernel.deployment.exception.DeploymentEngineException;

/**
 * Unit testing class for org.wso2.carbon.kernel.internal.context.DefaultCarbonRuntime.
 */
public class DefaultCarbonRuntimeTest {
    private DefaultCarbonRuntime carbonRuntime;
    private CarbonConfiguration carbonConfiguration;

    @BeforeTest
    public void setup() throws DeploymentEngineException, DeployerRegistrationException {
        carbonRuntime = new DefaultCarbonRuntime();
        carbonConfiguration = new CarbonConfiguration();
    }

    @Test
    public void testDefaultCarbonRuntime() {
       carbonRuntime.setCarbonConfiguration(carbonConfiguration);
        Assert.assertEquals(this.carbonConfiguration, carbonRuntime.getConfiguration());
    }
}
