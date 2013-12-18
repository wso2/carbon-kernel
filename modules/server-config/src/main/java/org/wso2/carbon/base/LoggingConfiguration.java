/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.base;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class LoggingConfiguration {

    private static final Log log = LogFactory.getLog(LoggingConfiguration.class);
    private ConfigurationAdmin configurationAdmin;
    private static LoggingConfiguration instance = new LoggingConfiguration();

    /**
     * Method to retrieve an instance of the logging configuration.
     *
     * @return instance of the logging configuration
     */
    public static LoggingConfiguration getInstance() {
        return instance;
    }

    public void setConfigurationAdminService(ConfigurationAdmin configurationAdminService) {
        configurationAdmin = configurationAdminService;
    }

    public void registerConfigurations(String configuration)
            throws IOException, InvalidSyntaxException, IllegalStateException {
        if (configurationAdmin == null) {
            throw new IllegalStateException(
                    "Configuration admin service is not available."
            );
        }

        File configDir = new File(System.getProperty(Constants.LOGGING_CONFIGURATION_LOCATION));
        if (!configDir.exists()) {
            return;
        }
        File configFileName = new File(configDir, Constants.CONFIG_FILE_NAME);
        createConfigurationForFile(configuration, configFileName);
    }

    private void createConfigurationForFile(String configuration, File configFileName)
            throws IOException {

        if (!configFileName.isDirectory()) {
            // check if the service is the one that should be configured
            if ((configuration != null) && !Constants.LOGGING_CONFIG_PID.equals(configuration)) {
                return;
            }
            Properties prop = readProperties(configFileName);

            synchronized (this) {
                Configuration conf = configurationAdmin.getConfiguration(Constants.LOGGING_CONFIG_PID, null);
                conf.update(prop);
            }
            log.debug("Logging registration configuration completed");
        }
    }

    public final Properties readProperties(File file)
            throws IllegalArgumentException {
        try {
            Properties prop = new Properties();
            FileInputStream fis = new FileInputStream(file);
            prop.load(fis);
            return prop;
        } catch (IOException e) {
            log.error("Fail to read Properties from file [" + file.getAbsolutePath() + "] configuration property.", e);
        }
        return null;
    }

}
