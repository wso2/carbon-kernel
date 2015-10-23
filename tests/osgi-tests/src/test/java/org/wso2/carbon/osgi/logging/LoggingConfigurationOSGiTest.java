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
package org.wso2.carbon.osgi.logging;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.ops4j.pax.exam.util.Filter;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.inject.Inject;

/**
 * Logging Configuration OSGi test case.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LoggingConfigurationOSGiTest {
    private static final String LOGGING_CONFIG_PID = "org.ops4j.pax.logging";
    private static final String LOG4J2_CONFIG_FILE_KEY = "org.ops4j.pax.logging.log4j2.config.file";
    private static final String LOG4J2_CONFIG_FILE = "log4j2.xml";

    @Inject
    private ConfigurationAdmin configAdmin;

    @Inject
    @Filter("(service.pid=org.ops4j.pax.logging)")
    private ManagedService managedService;

    private static String loggingConfigDirectory;

    static {
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        loggingConfigDirectory = Paths.get(basedir, "src", "test", "resources", "logging").toString();
    }

    @Test
    public void testConfigAdminService() throws IOException {
        Assert.assertNotNull(configAdmin, "Configuration Service is null");
        Configuration config = configAdmin.getConfiguration(LOGGING_CONFIG_PID);
        Assert.assertNotNull(config, "PAX Logging Configuration is null");
        config.update();
        Dictionary properties = config.getProperties();
        Assert.assertNotNull(properties, "PAX Logging Configuration Admin Service properties is null");
        Assert.assertEquals(properties.get("service.pid"), LOGGING_CONFIG_PID);
    }

    @Test
    public void testLog4j2ConfigUpdate() throws IOException, ConfigurationException {
        Logger logger = LoggerFactory.getLogger(LoggingConfigurationOSGiTest.class);
        Assert.assertNotNull(managedService, "Managed Service is null");

        //default log level is "ERROR"
        Assert.assertEquals(logger.isErrorEnabled(), true);
        Assert.assertEquals(logger.isDebugEnabled(), false);

        Dictionary<String, Object> properties = new Hashtable<>();
        String log4jConfigFilePath = Paths.get(loggingConfigDirectory, LOG4J2_CONFIG_FILE).toString();
        properties.put(LOG4J2_CONFIG_FILE_KEY, log4jConfigFilePath);
        managedService.updated(properties);

        //updated log level is "DEBUG"
        Assert.assertEquals(logger.isDebugEnabled(), true);
    }
}
