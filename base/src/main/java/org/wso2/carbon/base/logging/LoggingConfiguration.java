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
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.base.Constants;
import org.wso2.carbon.base.utils.BaseUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * An IllegalStateException will be thrown in the absence of managedService instance.
     *
     * @param managedService managed service instance to configure pax logging
     * @throws FileNotFoundException  this is thrown if the logging config file is not found at the carbon default
     *                                conf/etc location
     * @throws ConfigurationException this is thrown if any error occurred while update of config admin service
     *                                properties for pax-logging is being invoked.
     */
    public void register(ManagedService managedService) throws FileNotFoundException, ConfigurationException {
        if (managedService == null) {
            throw new IllegalStateException("Configuration admin managed service is not available.");
        }
        Hashtable loggingProperties;
        Path carbonConfigEtcHome = Paths.get(BaseUtils.getCarbonConfigHome().toString(), "etc");
        if (carbonConfigEtcHome.toFile().exists()) {
            File loggingPropertiesFile = Paths.get(carbonConfigEtcHome.toString(),
                    Constants.PAX_LOGGING_PROPERTIES_FILE).toFile();
            if (loggingPropertiesFile.exists()) {
                loggingProperties = BaseUtils.readProperties(loggingPropertiesFile);
            } else {
                throw new FileNotFoundException("Logging properties file is not found at : " + carbonConfigEtcHome);
            }
            managedService.updated(loggingProperties);
            logger.debug("Logging configuration registration completed using {} ",
                    loggingPropertiesFile.getAbsolutePath());
        } else {
            throw new FileNotFoundException("Carbon configuration etc directory is not found");
        }
    }
}
