/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.configuration.component.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.configuration.component.exceptions.ConfigurationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Interface for reading a configuration file.
 *
 * @param <T> configuration bean type
 */
public abstract class ConfigFileReader<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ConfigFileReader.class);
    protected String fileName;

    public ConfigFileReader(String fileName) {
        this.fileName = fileName;
    }

    /**
     * This method reads configuration file and will return and instance of the specified configuration bean class
     * values of the configuration bean classes.
     *
     * @return configuration object
     * @throws ConfigurationException on error on reading configuration file
     */
    public abstract T getConfiguration() throws ConfigurationException;

    /**
     * Get configuration file location.
     *
     * @return configuration file path
     * @throws ConfigurationException error on getting file path
     */
    public abstract Path getConfigurationFileLocation() throws ConfigurationException;

    /**
     * Get contents of the file as a string.
     *
     * @throws ConfigurationException if file name is null or on error on reading file
     */
    public final String getFileContent() throws ConfigurationException {
        if (fileName == null) {
            String message = "Error while reading the configuration file, file name is null";
            LOGGER.error(message);
            throw new ConfigurationException(message);
        }
        try {
            byte[] contentBytes = Files.readAllBytes(getConfigurationFileLocation());
            return new String(contentBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            String message = "Error while reading configuration file";
            LOGGER.error(message);
            throw new ConfigurationException(message, e);
        }
    }
}
