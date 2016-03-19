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
package org.wso2.carbon.logging;

import org.osgi.service.cm.ConfigurationException;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.logging.internal.LoggingConfiguration;

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

    private static final String CARBON_HOME = "carbon.home";

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
        System.setProperty(CARBON_HOME, getTestResourceFile("xsd").getAbsolutePath());
        loggingConfiguration.register(new CustomManagedService());
        System.clearProperty(CARBON_HOME);
    }

    @Test(dependsOnMethods = "testRegisterReadingLog4J2Config")
    public void testRegisterReadingNonExistingfile() throws ConfigurationException {
        System.setProperty(CARBON_HOME, getTestResourceFile("carbon-repo").getAbsolutePath());
        try {
            loggingConfiguration.register(new CustomManagedService());
        } catch (IllegalStateException e) {
            Assert.assertTrue(false, "IllegalStateException thrown when expected is FileNotFoundException");
        } catch (FileNotFoundException e) {
            Assert.assertTrue(true);
        }
        System.clearProperty(CARBON_HOME);
    }

    private File getTestResourceFile(String relativePath) {
        return Paths.get(testDir.toString(), "resources", relativePath).toFile();
    }

}
