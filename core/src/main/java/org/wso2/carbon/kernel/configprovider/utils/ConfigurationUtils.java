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

import org.wso2.carbon.kernel.Constants;
import org.yaml.snakeyaml.Yaml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Configuration internal utils.
 *
 * @since 5.2.0
 */
public class ConfigurationUtils {

    private ConfigurationUtils() {
    }

    /**
     * Returns the deployment.yaml location.
     *
     * @return Path deployment.yaml location
     */
    public static Path getDeploymentYAMLLocation() {
        return Paths.get(org.wso2.carbon.kernel.utils.Utils.getCarbonConfigHome().toString(),
                Constants.DEPLOYMENT_CONFIG_YAML);
    }

    /**
     * This method converts a given JSON String to YAML format.
     *
     * @param jsonString JSON String that needs to be converted to YAML format
     * @return String in YAML format
     */
    public static String convertJSONToYAML(String jsonString) {
        Yaml yaml = new Yaml();
        Map map = yaml.loadAs(jsonString, Map.class);
        return yaml.dumpAsMap(map);
    }
}
