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
package org.wso2.carbon.logging.internal;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Optional;

/**
 * This class creates and initializes the logging configurations based on log4j2 for pax logging framework.
 * It sets the log4j2 config file (log4j2.xml) to the ManagedService instance as a configuration property and
 * update it.
 *
 * @since 5.0.0
 */
public class LoggingConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfiguration.class);
    private static LoggingConfiguration instance = new LoggingConfiguration();

    private static final String CARBON_HOME = "carbon.home";
    private static final String CARBON_HOME_ENV = "CARBON_HOME";
    private static final String LOG4J2_CONFIG_FILE_KEY = "org.ops4j.pax.logging.log4j2.config.file";
    private static final String LOG4J2_CONFIG_FILE_NAME = "log4j2.xml";

    /**
     * Singleton LoggingConfiguration class.
     */
    private LoggingConfiguration() {

    }

    /**
     * Method to retrieve an instance of the logging configuration.
     *
     * @return instance of the logging configuration
     */
    public static LoggingConfiguration getInstance() {
        return instance;
    }

    /**
     * This method will configure the logging framework with the log4j2 configuration. It uses the ManagedService
     * to update the loggingConfigFile location that is looked up by the pax logging framework.
     * An IllegalStateException will be thrown in the absence of managedService instance.
     *
     * @param managedService managed service instance to configure pax logging
     * @throws java.io.FileNotFoundException this is thrown if the log4j2 config file is not found at the carbon default
     *                                       conf location
     * @throws ConfigurationException        this is thrown if any error occurred while update of config admin service
     *                                       properties for pax-logging is being invoked.
     */
    public void register(ManagedService managedService) throws FileNotFoundException, ConfigurationException {
        if (managedService == null) {
            throw new IllegalStateException("Configuration admin managed service is not available.");
        }
        File configDir = getCarbonConfigHome().toFile();
        if (!configDir.exists()) {
            throw new IllegalStateException("Carbon configuration directory is not found.");
        }
        File loggingConfigFile = new File(configDir, LOG4J2_CONFIG_FILE_NAME);
        if (loggingConfigFile.exists() && loggingConfigFile.isFile()) {
            Hashtable<String, String> prop = new Hashtable<>();
            prop.put(LOG4J2_CONFIG_FILE_KEY, loggingConfigFile.getAbsolutePath());
            managedService.updated(prop);
            logger.debug("Logging configuration registration completed using config file : {}",
                    loggingConfigFile.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Log4J2 configuration file is not found at : " +
                    configDir.getAbsolutePath());
        }
    }

    /**
     * This method will return the carbon configuration directory path.
     * i.e ${carbon.home}/conf. If {@code carbon.home} system property is not found, gets the
     * {@code CARBON_HOME_ENV} system property value and sets to the carbon home.
     *
     * @return returns the Carbon Configuration directory path
     */
    private Path getCarbonConfigHome() {
        String carbonHome = Optional.ofNullable(System.getProperty(CARBON_HOME))
                .orElseGet(() -> {
                    String carbonHomeEnvVar = System.getenv(CARBON_HOME_ENV);
                    System.setProperty(CARBON_HOME, carbonHomeEnvVar);
                    return carbonHomeEnvVar;
                });
        return Paths.get(carbonHome, "conf");
    }
}
