/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.kernel.deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.deployment.config.DeploymentConfiguration;
import org.wso2.carbon.kernel.utils.Utils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Utility class to get the Carbon deployment framework configuration.
 *
 */
public class DeploymentConfigurationProvider {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentConfigurationProvider.class);

    private static DeploymentConfiguration deploymentConfiguration;

    private static boolean isInitialized;

    public static DeploymentConfiguration getDeploymentConfiguration() {
        init();
        return deploymentConfiguration;
    }

    /**
     * todo javadocs
     */
    private static synchronized void init() {
        if (isInitialized) {
            return;
        }

        String configFileLocation = getDeploymentConfigurationLocation();
        try (InputStream inputStream = new FileInputStream(configFileLocation)) {

            String yamlFileString;
            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                yamlFileString = scanner.useDelimiter("\\A").next();
                yamlFileString = Utils.substituteVariables(yamlFileString);
            }

            Yaml yaml = new Yaml();
            yaml.setBeanAccess(BeanAccess.FIELD);
            deploymentConfiguration = yaml.loadAs(yamlFileString, DeploymentConfiguration.class);
            isInitialized = true;
        } catch (IOException e) {
            String errorMessage = "Failed populate DeploymentConfiguration from " + configFileLocation;
            logger.error(errorMessage, e);
            throw new RuntimeException(errorMessage);
        }

    }

    private static String getDeploymentConfigurationLocation() {
        return Utils.getCarbonConfigHome().resolve(Constants.DEPLOYMENT_YAML_CONFIGURATION_FILE)
                .toString();
    }
}
