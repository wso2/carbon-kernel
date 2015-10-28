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

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.internal.Constants;
import org.wso2.carbon.kernel.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Hashtable;

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
     *
     * @param managedService managed service
     * @throws IllegalStateException this is thrown if the managedService instance is not set
     * @throws FileNotFoundException this is thrown if the log4j2 config file is not found at the carbon default
     *                               conf location
     */
    public void register(ManagedService managedService)
            throws IllegalStateException, FileNotFoundException {
        if (managedService == null) {
            throw new IllegalStateException(
                    "Configuration admin service is not available."
            );
        }
        File configDir = Utils.getCarbonConfigHome().toFile();
        if (!configDir.exists()) {
            return;
        }
        File loggingConfigFile = new File(configDir, Constants.LOG4J2_CONFIG_FILE_NAME);
        if (loggingConfigFile.exists() && loggingConfigFile.isFile()) {
            Hashtable<String, String> prop = new Hashtable<>();
            synchronized (this) {
                prop.put(Constants.LOG4J2_CONFIG_FILE_KEY, loggingConfigFile.getAbsolutePath());
                try {
                    managedService.updated(prop);
                } catch (ConfigurationException e) {
                    logger.error("Fail to read Log4J2 configurations from file [" + loggingConfigFile.getAbsolutePath()
                            + "]", e);
                }
            }
            logger.debug("Logging registration configuration completed");
        } else {
            throw new FileNotFoundException("Log4J2 configuration file is not found at : " +
                    configDir.getAbsolutePath());
        }
    }

    /**
     * This is the method remove the ManagedService instance to LoggingConfiguration to be used for configuring the
     * logging framework  with log4j2.xml config file.
     *
     * @param managedService managed service
     */
    public void unregister(ManagedService managedService) {
        //TODO properly remove logging config from config admin
    }
}
