/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.base.logging;

import org.osgi.service.cm.ConfigurationException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.base.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit testing class for org.wso2.carbon.kernel.internal.logging.LoggingConfiguration.
 *
 * @since 5.0.0
 */
public class LoggingConfigurationTest {
    LoggingConfiguration loggingConfiguration = null;

    protected Path testDir = Paths.get(new File(".").getAbsolutePath(), "src", "test");


    @Test
    public void testGetInstance() {
        loggingConfiguration = LoggingConfiguration.getInstance();
        Assert.assertNotNull(loggingConfiguration);
    }

    @Test(dependsOnMethods = "testGetInstance", expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "Configuration admin managed service is not available.")
    public void testRegisterNullManagedService() throws FileNotFoundException, ConfigurationException {
        loggingConfiguration.register(null);
        Assert.assertTrue(false, "Logger register method did not throw an exception when passing null as the " +
                "MangedService");
    }

    @Test(dependsOnMethods = "testRegisterNullManagedService")
    public void testRegisterReadingLog4J2Config() throws FileNotFoundException, ConfigurationException {
        System.setProperty(Constants.CARBON_HOME, getTestResourceFile("carbon-home").getAbsolutePath());
        loggingConfiguration.register(new CustomManagedService());
        System.clearProperty(Constants.CARBON_HOME);
    }

    @Test(dependsOnMethods = "testRegisterNullManagedService", expectedExceptions = IllegalStateException.class,
            expectedExceptionsMessageRegExp = "CARBON_HOME system property is not set")
    public void testRegisterReadingNonExistingConfigDirectory() throws FileNotFoundException, ConfigurationException {
        System.clearProperty(Constants.CARBON_HOME);
        loggingConfiguration.register(new CustomManagedService());
    }

    @Test(dependsOnMethods = "testRegisterReadingNonExistingConfigDirectory",
            expectedExceptions = FileNotFoundException.class,
            expectedExceptionsMessageRegExp = "Logging properties file is not found.*")
    public void testRegisterReadingNonExistingFile() throws ConfigurationException, FileNotFoundException {
        System.setProperty(Constants.CARBON_HOME, getTestResourceFile("fake-carbon-home").getAbsolutePath());
        try {
            loggingConfiguration.register(new CustomManagedService());
        } finally {
            System.clearProperty(Constants.CARBON_HOME);
        }
    }

    private File getTestResourceFile(String relativePath) {
        return Paths.get(testDir.toString(), "resources", relativePath).toFile();
    }

}
