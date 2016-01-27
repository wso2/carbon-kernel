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
package org.wso2.carbon.kernel.internal.logging;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.Constants;
import org.wso2.carbon.kernel.deployment.BaseTest;

import java.io.FileNotFoundException;

/**
 * Unit testing class for org.wso2.carbon.kernel.internal.logging.LoggingConfiguration.
 *
 * @since 5.0.0
 */
public class LoggingConfigurationTest extends BaseTest {
    LoggingConfiguration loggingConfiguration = null;

    /**
     * @param testName
     */
    public LoggingConfigurationTest(String testName) {
        super(testName);
    }

    @Test
    public void testGetInstance() {
        loggingConfiguration = LoggingConfiguration.getInstance();
        Assert.assertNotNull(loggingConfiguration);
    }

    @Test(dependsOnMethods = "testGetInstance", expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Configuration admin service is not available.")
    public void testRegisterNullManagedService() throws FileNotFoundException {
        loggingConfiguration.register(null);
    }

    @Test(dependsOnMethods = "testRegisterNullManagedService")
    public void testRegisterReadingLog4J2Config() throws FileNotFoundException {
        System.setProperty(Constants.CARBON_HOME, getTestResourceFile("xsd").getAbsolutePath());
        loggingConfiguration.register(new CustomManagedService());
        System.clearProperty(Constants.CARBON_HOME);
    }

    @Test(dependsOnMethods = "testRegisterReadingLog4J2Config")
    public void testRegisterReadingNonExistingfile() {
        System.setProperty(Constants.CARBON_HOME, getTestResourceFile("carbon-repo").getAbsolutePath());
        try {
            loggingConfiguration.register(new CustomManagedService());
        } catch (IllegalStateException e) {
            Assert.assertTrue(false, "IllegalStateException thrown when expected is FileNotFoundException");
        } catch (FileNotFoundException e) {
            Assert.assertTrue(true);
        }
        System.clearProperty(Constants.CARBON_HOME);
    }

}
