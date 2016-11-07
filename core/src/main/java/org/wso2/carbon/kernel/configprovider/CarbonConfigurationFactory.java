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
import org.wso2.carbon.configuration.annotations.Configuration;
import org.wso2.carbon.kernel.configprovider.configs.YAMLBasedConfigProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.util.Hashtable;

/**
 *
 */
public class CarbonConfigurationFactory {

    private static final Logger logger = LoggerFactory.getLogger(CarbonConfigurationFactory.class);
    private static Hashtable<String, String> deploymentConfigs = new Hashtable<>();

    private CarbonConfigurationFactory() {
    }

    public static Object getConfigurationInstance(String configClassName) throws CarbonConfigurationException,
            ClassNotFoundException {
        Configuration configuration = null;
        Class configClass = Class.forName(configClassName);
        if (configClass.isAnnotationPresent(Configuration.class)) {
            configuration = (Configuration) configClass.getAnnotation(Configuration.class);
        }

        if (deploymentConfigs.isEmpty()) {
            DeploymentConfigProvider configProvider = new YAMLBasedConfigProvider();
            deploymentConfigs = configProvider.getDeploymentConfiguration();
        }

        try {
            if (deploymentConfigs.isEmpty() || configuration == null) {
                logger.error("Deployment configuration mapping doesn't exist: " +
                        "creating configuration with default values");
                return configClass.newInstance();
            }
            if (deploymentConfigs.containsKey(configuration.key())) {
                String xmlConfigString = deploymentConfigs.get(configuration.key());
                logger.info("mapping configuration string:: \n" + xmlConfigString);

                Yaml yaml = new Yaml(new CustomClassLoaderConstructor(configClass,
                        configClass.getClassLoader()));
                yaml.setBeanAccess(BeanAccess.FIELD);
                return yaml.loadAs(xmlConfigString, configClass);
            }
            return configClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CarbonConfigurationException("Error while creating configuration Instance", e);
        }
    }
}
