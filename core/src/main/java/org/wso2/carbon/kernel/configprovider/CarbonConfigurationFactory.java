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
import org.wso2.carbon.configuration.annotations.Configuration;
import org.wso2.carbon.configuration.annotations.Ignore;
import org.wso2.carbon.kernel.configprovider.configs.YAMLBasedConfigProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class CarbonConfigurationFactory {

    private static final Logger logger = LoggerFactory.getLogger(CarbonConfigurationFactory.class);
    private static Hashtable<String, String> deploymentConfigs = new Hashtable<>();

    private CarbonConfigurationFactory() {
    }

    public static Object getConfigurationInstance(String configClassName) throws CarbonConfigurationException {
        //load the class using reflection and yaml instance from the class loader
        Class configClass;
        try {
            configClass = Class.forName(configClassName);
        } catch (ClassNotFoundException e) {
            throw new CarbonConfigurationException("Config Class : " + configClassName + "does not exists.", e);
        }
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(configClass,
                configClass.getClassLoader()));
        yaml.setBeanAccess(BeanAccess.FIELD);

        //read default configuration values from annotations
        Map<String, Object> defaultElementMap = readConfigurationElements(configClass);

        //get configuration namespace from the class annotation
        Configuration configuration = null;
        if (configClass.isAnnotationPresent(Configuration.class)) {
            configuration = (Configuration) configClass.getAnnotation(Configuration.class);
        }

        //lazy loading deployment.yaml configuration, if it is not exists
        loadDeploymentConfiguration();

        try {
            if (configuration != null && deploymentConfigs.containsKey(configuration.namespace())) {
                String jsonConfigString = deploymentConfigs.get(configuration.namespace());
                if (logger.isDebugEnabled()) {
                    logger.info("class name: " + configClass.getSimpleName() + " | new configurations : \n" +
                            jsonConfigString);
                }
                JSONObject modifiedObject = overrideDefaultConfigurations(new JSONObject(defaultElementMap),
                        new JSONObject(jsonConfigString));
                if (logger.isDebugEnabled()) {
                    logger.info("class name: " + configClass.getSimpleName() + " | modified configurations:: \n" +
                            modifiedObject.toString());
                }
                Map map = yaml.loadAs(modifiedObject.toString(), Map.class);
                return yaml.loadAs(yaml.dumpAsMap(map), configClass);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Deployment configuration mapping doesn't exist: " +
                            "creating configuration with default values");
                }
                if (!defaultElementMap.isEmpty()) {
                    String defaultContent = yaml.dumpAsMap(defaultElementMap);;
                    return yaml.loadAs(defaultContent, configClass);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Default configuration doesn't exist: " +
                            "creating configuration without values");
                }
                return configClass.newInstance();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CarbonConfigurationException("Error while creating configuration Instance", e);
        }
    }

    private static void loadDeploymentConfiguration() {
        if (deploymentConfigs.isEmpty()) {
            synchronized (deploymentConfigs) {
                if (deploymentConfigs.isEmpty()) {
                    DeploymentConfigProvider configProvider = new YAMLBasedConfigProvider();
                    deploymentConfigs = configProvider.getDeploymentConfiguration();
                }
            }
        }
    }

    private static Map<String, Object> readConfigurationElements(Class configClass) {
        Map<String, Object> elementMap = new LinkedHashMap<>();
        Field[] fields = configClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.getAnnotation(Ignore.class) != null) {
                continue;
            }

            Class fieldTypeClass = null;
            if (!field.getType().isPrimitive()) {
                fieldTypeClass = field.getType();
            }

            if (fieldTypeClass != null && fieldTypeClass.isAnnotationPresent(Configuration.class)) {
                Configuration configuration = (Configuration) fieldTypeClass.getAnnotation(Configuration.class);
                elementMap.put(configuration.namespace(), readConfigurationElements(fieldTypeClass));
            } else {
                org.wso2.carbon.configuration.annotations.Element fieldElem = field.getAnnotation(org.wso2.carbon
                        .configuration.annotations.Element.class);
                if (fieldElem != null) {
                    elementMap.put(field.getName(), fieldElem.defaultValue());
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("class name: " + configClass.getSimpleName() + " | default configurations :: " + elementMap
                    .toString());
        }
        return elementMap;
    }

    private static JSONObject overrideDefaultConfigurations(JSONObject defaultConfig, JSONObject newConfig) {
        for (Object key : newConfig.keySet()) {
            //based on you key types
            String keyStr = (String) key;
            Object keyvalue = newConfig.get(keyStr);
            if (defaultConfig.has(keyStr)) {
                //for nested objects iteration if required
                if (keyvalue instanceof JSONObject) {
                    overrideDefaultConfigurations((JSONObject) defaultConfig.get(keyStr), (JSONObject) keyvalue);
                } else {
                    defaultConfig.put(keyStr, keyvalue);
                }
            }
        }
        return defaultConfig;
    }
}
