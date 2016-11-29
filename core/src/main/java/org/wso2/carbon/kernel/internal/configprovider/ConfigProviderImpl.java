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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigFileReader;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
import org.wso2.carbon.kernel.securevault.exception.SecureVaultException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This impl class provide the ability to override configurations in various components using a single file which has
 * the name {@link ConfigProviderImpl}.
 *
 * @since 5.2.0
 */
public class ConfigProviderImpl implements ConfigProvider {
    private static final Logger logger = LoggerFactory.getLogger(ConfigProviderImpl.class.getName());

    private static volatile Map<String, String>  deploymentConfigs = null;
    //This regex is used to identify placeholders
    private static final String PLACEHOLDER_REGEX;
    //This is used to match placeholders
    private static final Pattern PLACEHOLDER_PATTERN;

    private ConfigFileReader configFileReader;

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

    public ConfigProviderImpl(ConfigFileReader configFileReader) {
        this.configFileReader = configFileReader;
    }

    @Override
    public <T> T getConfigurationObject(Class<T> configClass) throws CarbonConfigurationException {
        //get configuration namespace from the class annotation
        String namespace = null;
        if (configClass.isAnnotationPresent(Configuration.class)) {
            Configuration configuration = configClass.getAnnotation(Configuration.class);
            if (!Configuration.NULL.equals(configuration.namespace())) {
                namespace = configuration.namespace();
            }
        }
        // lazy loading deployment.yaml configuration.
        loadDeploymentConfiguration(configFileReader);

        if (namespace != null && deploymentConfigs.containsKey(namespace)) {
            String yamlConfigString = deploymentConfigs.get(namespace);
            if (logger.isDebugEnabled()) {
                logger.debug("class name: " + configClass.getSimpleName() + " | new configurations: \n" +
                        yamlConfigString);
            }
            String yamlProcessedString = processPlaceholder(yamlConfigString);
            yamlProcessedString = org.wso2.carbon.kernel.utils.Utils.substituteVariables(yamlProcessedString);
            Yaml yaml = new Yaml(new CustomClassLoaderConstructor(configClass, configClass.getClassLoader()));
            yaml.setBeanAccess(BeanAccess.FIELD);
            return yaml.loadAs(yamlProcessedString, configClass);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Deployment configuration mapping doesn't exist: " +
                        "creating configuration instance with default values");
            }
            try {
                return configClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new CarbonConfigurationException("Error while creating configuration instance: "
                        + configClass.getSimpleName(), e);
            }
        }
    }

    @Override
    public Map getConfigurationMap(String namespace) throws CarbonConfigurationException {
        // lazy loading deployment.yaml configuration, if it is not exists
        loadDeploymentConfiguration(configFileReader);
        // check for json configuration from deployment configs of namespace.
        if (deploymentConfigs.containsKey(namespace)) {
            String configString = deploymentConfigs.get(namespace);
            String processedString = processPlaceholder(configString);
            processedString = org.wso2.carbon.kernel.utils.Utils.substituteVariables(processedString);
            Yaml yaml = new Yaml();
            return yaml.loadAs(processedString, Map.class);
        }
        logger.error("configuration doesn't exist for the namespace: " + namespace + " in deployment yaml. Hence " +
                "return null object");
        return null;
    }


    /**
     * This method loads deployment configs in deployment.yaml.
     * loads only if deployment configuration not exists
     */
    private void loadDeploymentConfiguration(ConfigFileReader configFileReader) throws CarbonConfigurationException {
        if (deploymentConfigs == null) {
            synchronized (this) {
                if (deploymentConfigs == null) {
                    deploymentConfigs = configFileReader.getDeploymentConfiguration();
                }
            }
        }
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
                        if (ConfigProviderDataHolder.getInstance().getSecureVault() != null) {
                            String newValue = new String(ConfigProviderDataHolder.getInstance().getSecureVault()
                                    .resolve(value));
                            inputString = inputString.replaceFirst(PLACEHOLDER_REGEX, "$1" + newValue + "$8");
                        } else {
                            throw new RuntimeException("Secure Vault service is not available");
                        }
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
