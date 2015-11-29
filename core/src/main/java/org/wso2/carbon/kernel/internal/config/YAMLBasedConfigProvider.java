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
package org.wso2.carbon.kernel.internal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.config.CarbonConfigProvider;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.internal.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class takes care of parsing the carbon.yml file and creating the CarbonConfiguration object model.
 *
 * @since 5.0.0
 */
public class YAMLBasedConfigProvider implements CarbonConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(YAMLBasedConfigProvider.class);

    /**
     * Parse the carbon.yml and returns the CarbonConfiguration object.
     *
     * @return CarbonConfiguration
     */
    public CarbonConfiguration getCarbonConfiguration() {
        String configFileLocation = Utils.getCarbonYAMLLocation();
        try (InputStream in = new FileInputStream(configFileLocation)) {
            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml.loadAs(in, CarbonConfiguration.class);
        } catch (IOException e) {
            logger.error("Could not load " + configFileLocation, e);
        }
        return null;
    }
}
