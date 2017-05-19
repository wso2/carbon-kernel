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
package org.wso2.carbon.kernel.internal.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Kernel internal utils.
 *
 * @since 5.0.0
 */
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    /**
     * Maven project properties.
     */
    private static final String PROJECT_DEFAULTS_PROPERTY_FILE = "project.defaults.properties";

    private Utils() {
    }

    /**
     * Returns the carbon.yaml location.
     *
     * @return Path carbon.yaml location
     */
    public static Path getCarbonYAMLLocation() {
        return Paths.get(org.wso2.carbon.utils.Utils.getCarbonConfigHome().toString(),
                Constants.CARBON_CONFIG_YAML);
    }

    /**
     * This method reads project properties in resource file.
     *
     * @return project properties
     */
    public static Properties loadProjectProperties() {
        Properties properties = new Properties();
        try (InputStream in = Utils.class.getClassLoader().getResourceAsStream(PROJECT_DEFAULTS_PROPERTY_FILE)) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            logger.error("Error while reading the project default properties, hence apply default values.", e);
        }
        return properties;
    }
}
