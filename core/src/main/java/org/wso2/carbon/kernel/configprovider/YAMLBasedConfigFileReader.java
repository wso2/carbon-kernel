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


import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.configprovider.utils.ConfigurationUtils;
import org.wso2.carbon.kernel.configresolver.ConfigResolverUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class takes care of parsing the deployment.yaml file and creating the deployment configuration table.
 *
 * @since 5.2.0
 */
public class YAMLBasedConfigFileReader implements ConfigFileReader {
    private static final Logger logger = LoggerFactory.getLogger(YAMLBasedConfigFileReader.class);
    private String filename;

    public YAMLBasedConfigFileReader(String filename) {
        this.filename = filename;
    }

    /**
     * this method reads deployment.yaml file and return configuration map which is used for overriding default
     * values of the configuration bean classes.
     * @return configuration map
     */
    @Override
    public Map<String, String> getDeploymentConfiguration() {
        org.wso2.carbon.kernel.utils.Utils.checkSecurity();
        File configFile = ConfigurationUtils.getConfigurationFileLocation(filename).toFile();
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            String yamlFileString = getDeploymentContent(fileInputStream);
            String jsonString = ConfigResolverUtils.convertYAMLToJSON(yamlFileString);
            return getDeploymentConfigMap(jsonString);
        } catch (IOException e) {
            String errorMessage = "Failed populate Deployment Configuration from " + configFile.getName();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    /**
     * this method converts the json string to configuration map as,
     * key : json (root)key
     * values  : json string of the key
     * @param jsonString json string
     * @return configuration map
     */
    private Map<String, String> getDeploymentConfigMap(String jsonString) {
        Map<String, String> deploymentConfigs = new HashMap<>();
        JSONObject jsonObject = new JSONObject(jsonString);
        for (Object key : jsonObject.keySet()) {
            String keyContent = jsonObject.get((String) key).toString();
            deploymentConfigs.put((String) key, keyContent);
        }
        return deploymentConfigs;
    }

    /**
     * this methods returns file content as String from the input stream
     * @param fileInputStream file inputstream
     * @return file content
     * @throws IOException
     */
    private String getDeploymentContent(FileInputStream fileInputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream,
                StandardCharsets.UTF_8))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }
}
