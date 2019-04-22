/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.utils;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.base.ServerConfigurationException;

import java.nio.file.Paths;

public class SecretTest extends BaseTest {

    private static final String CARBON_WITH_PASSOWORD_TRIM_DISABLED_XML = "carbon-with-password-trim-disabled.xml";

    @BeforeTest(alwaysRun = true)
    public void setup() throws Exception {
        super.setup();
    }

    @Test(groups = {"org.wso2.carbon.utils"})
    public void testPasswordTrimWithoutConfiguration() throws Exception {
        String passwordWithSpace = "  testPassword ";
        char[] passwordWithSpaceChars = passwordWithSpace.trim().toCharArray();
        Assert.assertEquals(Secret.getSecret(passwordWithSpace).getChars(), passwordWithSpaceChars, "Password has " +
                "not been trimmed");
    }

    @Test(groups = {"org.wso2.carbon.utils"}, dependsOnMethods = "testPasswordTrimWithoutConfiguration")
    public void testPasswordTrimOverConfiguration() throws Exception {
        initCustomTestServerConfiguration();
        String passwordWithSpace = "  testPassword ";
        char[] passwordWithSpaceChars = passwordWithSpace.toCharArray();
        Assert.assertEquals(Secret.getSecret(passwordWithSpace).getChars(), passwordWithSpaceChars, "Password has " +
                "been trimmed");
    }

    private void initCustomTestServerConfiguration() throws ServerConfigurationException {
        String serverConfigPath = Paths.get(testDir, CARBON_WITH_PASSOWORD_TRIM_DISABLED_XML).toString();
        ServerConfiguration.getInstance().forceInit(serverConfigPath);
    }

    @AfterTest(alwaysRun = true)
    public void cleanup() {
        System.clearProperty(ServerConstants.CARBON_HOME);
    }
}
