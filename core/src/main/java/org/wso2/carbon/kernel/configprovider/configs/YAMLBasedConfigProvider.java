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
package org.wso2.carbon.kernel.configprovider.configs;


import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.configprovider.DeploymentConfigProvider;
import org.wso2.carbon.kernel.configprovider.utils.ConfigurationUtils;
import org.wso2.carbon.kernel.configresolver.ConfigResolverUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.stream.Collectors;

/**
 *
 */
public class YAMLBasedConfigProvider implements DeploymentConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(YAMLBasedConfigProvider.class);

    @Override
    public Hashtable<String, String> getDeploymentConfiguration() {
        org.wso2.carbon.kernel.utils.Utils.checkSecurity();
        File configFile = ConfigurationUtils.getDeploymentYAMLLocation().toFile();
        try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
            String yamlFileString = getDeploymentContent(fileInputStream);
            String jsonString = ConfigResolverUtils.convertYAMLToJSON(yamlFileString);
            return getDeploymentConfigTable(jsonString);
        } catch (IOException e) {
            String errorMessage = "Failed populate Deployment Configuration from " + configFile.getName();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
    }

    private Hashtable<String, String> getDeploymentConfigTable(String jsonString) {
        Hashtable<String, String> deploymentConfigs = new Hashtable<>();
        JSONObject jsonObject = new JSONObject(jsonString);
        for (Object key : jsonObject.keySet()) {
            String keyContent = jsonObject.get((String) key).toString();
            deploymentConfigs.put((String) key, keyContent);
        }
        return deploymentConfigs;
    }

    private String getDeploymentContent(FileInputStream fileInputStream) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream,
                StandardCharsets.UTF_8))) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }
}
