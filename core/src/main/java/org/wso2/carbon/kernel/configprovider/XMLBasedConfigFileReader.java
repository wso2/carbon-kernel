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
package org.wso2.carbon.kernel.configprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.configprovider.utils.ConfigurationUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * This class takes care of parsing the deployment.yaml file and creating the deployment configuration table.
 *
 * @since 5.2.0
 */
public class XMLBasedConfigFileReader implements ConfigFileReader {
    private static final Logger logger = LoggerFactory.getLogger(XMLBasedConfigFileReader.class);
    private String filename;

    public XMLBasedConfigFileReader(String filename) {
        this.filename = filename;
    }
    /**
     * This method reads configuration file and return configuration map which is used for overriding default
     * values of the configuration bean classes.
     * @return configuration map
     */
    @Override
    public Map<String, String> getDeploymentConfiguration() throws CarbonConfigurationException {
        org.wso2.carbon.kernel.utils.Utils.checkSecurity();
        if (filename == null) {
            throw new CarbonConfigurationException("Error while reading the configuration file, filename is null");
        }
        try {
            byte[] contentBytes = Files.readAllBytes(ConfigurationUtils.getConfigurationFileLocation(filename));
            String xmlFileString = new String(contentBytes, StandardCharsets.UTF_8);
            String yamlString = ConfigurationUtils.convertXMLToYAML(xmlFileString);
            return ConfigurationUtils.getDeploymentConfigMap(yamlString);
        } catch (IOException e) {
            String errorMessage = "Failed populate deployment configuration from " + filename;
            logger.error(errorMessage, e);
            throw new CarbonConfigurationException(errorMessage, e);
        }
    }
}
