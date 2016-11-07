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
import org.wso2.carbon.kernel.configresolver.configfiles.YAML;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

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
            YAML deploymentYaml = new YAML(fileInputStream, configFile.getName());
            String yamlFileString = deploymentYaml.getContent();
            yamlFileString = org.wso2.carbon.kernel.utils.Utils.substituteVariables(yamlFileString);
            String jsonString = ConfigResolverUtils.convertYAMLToJSON(yamlFileString);
            return getDeploymentConfigTable(jsonString);
        } catch (IOException e) {
            String errorMessage = "Failed populate CarbonConfiguration from " + configFile.getName();
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }
    }

    private Hashtable<String, String> getDeploymentConfigTable(String jsonString) {
        Yaml yaml = new Yaml();
        Hashtable<String, String> deploymentConfigs = new Hashtable<>();
        JSONObject jsonObject = new JSONObject(jsonString);
        for (Object key : jsonObject.keySet()) {
            String keyContent = jsonObject.get((String) key).toString();
            Map map = yaml.loadAs(keyContent, Map.class);
            deploymentConfigs.put((String) key, yaml.dumpAsMap(map));
        }
        return deploymentConfigs;
    }
}
