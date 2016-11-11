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
package org.wso2.carbon.kernel.internal.configprovider;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.configuration.annotations.Configuration;
import org.wso2.carbon.configuration.annotations.Ignore;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.kernel.configprovider.DeploymentConfigProvider;
import org.wso2.carbon.kernel.configprovider.configs.YAMLBasedConfigProvider;
import org.wso2.carbon.kernel.configprovider.utils.ConfigurationUtils;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This util class provide the ability to override configurations in various components using a single file which has
 * the name {@link ConfigProviderImpl}.
 *
 * @since 5.2.0
 */
public class ConfigProviderImpl implements ConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(ConfigProviderImpl.class.getName());

    private static Hashtable<String, String> deploymentConfigs = new Hashtable<>();
    //This regex is used to identify placeholders
    private static final String PLACEHOLDER_REGEX;
    //This is used to match placeholders
    private static final Pattern PLACEHOLDER_PATTERN;

    static {
        PLACEHOLDER_REGEX = "(.*?)(\\$\\{(" + getPlaceholderString() + "):([^,]+?)((,)(.+?))?\\})(.*?)";
        PLACEHOLDER_PATTERN = Pattern.compile(PLACEHOLDER_REGEX);
    }

    /**
     * Enum to hold the supported placeholder types.
     */
    private enum Placeholder {
        SYS("sys"), ENV("env"), SEC("sec");
        private String value;
        Placeholder(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    @Override
    public Object getConfigurationInstance(String configClassName) throws CarbonConfigurationException {
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
            String yamlConfigString;
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
                yamlConfigString = ConfigurationUtils.convertJSONToYAML(modifiedObject.toString());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Deployment configuration mapping doesn't exist: " +
                            "creating configuration instance with default values");
                }

                if (defaultElementMap.isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Default configuration doesn't exist: " +
                                "creating configuration instance without values");
                    }
                    return configClass.newInstance();
                }
                yamlConfigString = yaml.dumpAsMap(defaultElementMap);
            }
            String yamlProcessedString = processPlaceholder(yamlConfigString);
            yamlProcessedString = org.wso2.carbon.kernel.utils.Utils.substituteVariables(yamlProcessedString);
            return yaml.loadAs(yamlProcessedString, configClass);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CarbonConfigurationException("Error while creating configuration Instance", e);
        }
    }

    @Override
    public Map getConfigurationMap(String namespace) throws CarbonConfigurationException {
        //lazy loading deployment.yaml configuration, if it is not exists
        loadDeploymentConfiguration();
        //check for json configuration from deployment configs of namespace.
        if (deploymentConfigs.containsKey(namespace)) {
            String jsonConfigString = deploymentConfigs.get(namespace);
            String jsonProcessedString = processPlaceholder(jsonConfigString);
            jsonProcessedString = org.wso2.carbon.kernel.utils.Utils.substituteVariables(jsonProcessedString);
            Yaml yaml = new Yaml();
            return yaml.loadAs(jsonProcessedString, Map.class);
        }
        logger.error("configuration doesn't exist for the namespace: " + namespace + " in deployment yaml. hence " +
                "return null object");
        return null;
    }


    private void loadDeploymentConfiguration() {
        if (deploymentConfigs.isEmpty()) {
            synchronized (this) {
                if (deploymentConfigs.isEmpty()) {
                    DeploymentConfigProvider configProvider = new YAMLBasedConfigProvider();
                    deploymentConfigs = configProvider.getDeploymentConfiguration();
                }
            }
        }
    }

    private Map<String, Object> readConfigurationElements(Class configClass) {
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
                    if (!fieldElem.defaultValue().equals(org.wso2.carbon.configuration.annotations.Element.NULL)) {
                        elementMap.put(field.getName(), fieldElem.defaultValue());
                    }
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("class name: " + configClass.getSimpleName() + " | default configurations :: " + elementMap
                    .toString());
        }
        return elementMap;
    }

    private JSONObject overrideDefaultConfigurations(JSONObject defaultConfig, JSONObject newConfig) {
        for (Object key : newConfig.keySet()) {
            //based on you key types
            String keyStr = (String) key;
            Object keyvalue = newConfig.get(keyStr);
            //for nested objects iteration if required
            if (keyvalue instanceof JSONObject) {
                if (defaultConfig.has(keyStr)) {
                    overrideDefaultConfigurations((JSONObject) defaultConfig.get(keyStr), (JSONObject) keyvalue);
                } else {
                    defaultConfig.put(keyStr, keyvalue);
                }
            } else {
                defaultConfig.put(keyStr, keyvalue);
            }
        }
        return defaultConfig;
    }


    /**
     * This method will concatenate and return the placeholder types.. Placeholder types will be separated
     * by | token. This method will be used to create the {@link ConfigProviderImpl#PLACEHOLDER_REGEX}.
     *
     * @return String that contains placeholder types which are separated by | token.
     */
    private static String getPlaceholderString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Placeholder placeholder : Placeholder.values()) {
            stringBuilder.append(placeholder.getValue()).append("|");
        }
        String value = stringBuilder.substring(0, stringBuilder.length() - 1);
        logger.debug("PlaceHolders String: {}", value);
        return value;
    }

    /**
     * This method returns the new value after processing the placeholders. This method can process multiple
     * placeholders within the same String as well.
     *
     * @param inputString Placeholder that needs to be replaced
     * @return New getContent which corresponds to inputString
     */
    private String processPlaceholder(String inputString) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(inputString);
        //Match all placeholders in the inputString
        while (matcher.find()) {
            //Group 3 corresponds to the key in the inputString
            String key = matcher.group(3);
            //Group 4 corresponds to the value of the inputString
            String value = matcher.group(4);
            //Group 7 corresponds to the default value in the inputString. If default value is not available, this
            // will be null
            String defaultValue = matcher.group(7);
            switch (key) {
                case "env":
                    inputString = processValue(System::getenv, value, inputString, defaultValue, Placeholder.ENV);
                    break;
                case "sys":
                    inputString = processValue(System::getProperty, value, inputString, defaultValue, Placeholder.SYS);
                    break;
                case "sec":
                    try {
                        inputString = new String(ConfigProviderDataHolder.getInstance().getOptSecureVault()
                                .orElseThrow(() -> new RuntimeException("Secure Vault service is not available"))
                                .resolve(value));
                    } catch (SecureVaultException e) {
                        throw new RuntimeException("Unable to resolve the given alias", e);
                    }
                    break;
                default:
                    String msg = String.format("Unsupported placeholder: %s", key);
                    logger.error(msg);
                    throw new RuntimeException(msg);
            }
        }
        return inputString;
    }

    /**
     * This method process a given placeholder string and returns the string with replaced new value.
     *
     * @param func         Function to apply.
     * @param key          Environment Variable/System Property key.
     * @param inputString  String which needs to process.
     * @param defaultValue Default value of the placeholder. If default value is not available, this is null.
     * @param type         Type of the placeholder (env/sys/sec) This is used to print the error message.
     * @return String which has the new value instead of the placeholder.
     */
    private static String processValue(Function<String, String> func, String key, String inputString, String
            defaultValue, Placeholder type) {
        String newValue = func.apply(key);
        //If the new value is not null, replace the placeholder with the new value and return the string.
        if (newValue != null) {
            return inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + newValue + "$8");
        }
        //If the new value is empty and the default value is not empty, replace the placeholder with the default
        // value and return the string
        if (defaultValue != null) {
            return inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + defaultValue + "$8");
        }
        //Otherwise print an error message and throw na exception
        String msg;
        if (Placeholder.ENV.getValue().equals(type.getValue())) {
            msg = String.format("Environment variable %s not found. Placeholder: %s", key,
                    inputString);
        } else if (Placeholder.SYS.getValue().equals(type.getValue())) {
            msg = String.format("System property %s not found. Placeholder: %s", key,
                    inputString);
        } else {
            msg = String.format("Unsupported placeholder type: %s", type.getValue());
        }
        logger.error(msg);
        throw new RuntimeException(msg);
    }
}
