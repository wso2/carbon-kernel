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
package org.wso2.carbon.kernel.configprovider.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.Constants;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration internal utils.
 *
 * @since 5.2.0
 */
public class ConfigurationUtils {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationUtils.class);
    private ConfigurationUtils() {}

    /**
     * Returns the configuration file location.
     *
     * @param filename configuration filename (e.g: deployment.yaml)
     * @return Path configuration file location
     */
    public static Path getConfigurationFileLocation(String filename) {
        return org.wso2.carbon.kernel.utils.Utils.getCarbonConfigHome().resolve(filename);
    }

    /**
     * This method converts a given XML String to YAML format.
     *
     * @param xmlString XML String that needs to be converted to YAML format
     * @return String in YAML format
     */
    public static String convertXMLToYAML(String xmlString) {
        String jsonString;
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
            jsonString = xmlJSONObj.toString();
            Yaml yaml = new Yaml();
            Map map = yaml.loadAs(jsonString, Map.class);
            return yaml.dumpAsMap(map);
        } catch (JSONException e) {
            throw new RuntimeException("Exception occurred while converting XML to JSON: ", e);
        }
    }

    /**
     * This method converts the yaml string to configuration map.
     * Map contains, key : yaml (root)key
     * values  : yaml string of the key
     * @param yamlString yaml string
     * @return configuration map
     */
    public static Map<String, String> getDeploymentConfigMap(String yamlString) {
        Map<String, String> deploymentConfigs = new HashMap<>();
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.loadAs(yamlString, Map.class);

        map.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .forEach(entry -> deploymentConfigs.put(entry.getKey(), yaml.dumpAsMap(entry.getValue())));
        return deploymentConfigs;
    }

    /**
     * This method reads project properties in resource file,
     * {@value org.wso2.carbon.kernel.Constants#PROJECT_DEFAULTS_PROPERTY_FILE}
     *
     * @return project properties
     */
    public static Properties loadProjectProperties() {
        Properties properties = new Properties();
        try (InputStream in = ConfigurationUtils.class.getClassLoader().getResourceAsStream(Constants
                .PROJECT_DEFAULTS_PROPERTY_FILE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            logger.error("Error while reading the project default properties, hence apply default values.", e);
        }
        return properties;
    }
}
