/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.ei.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Configuration parser class. Entry point to the config parsing logic.
 */
public class ConfigParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigParser.class);

    private static final String UX_FILE_PATH = "deployment.toml";
    private static final String TEMPLATE_FILE_PATH = "user-mgt.xml";
    private static final String INFER_CONFIG_FILE_PATH = "infer.json";
    private static final String VALIDATOR_FILE_PATH = "validator.json";
    private static final String MAPPING_FILE_PATH = "key-mappings.toml";
    private String deploymentConfigurationPath;
    private String templateFilePath;
    private String inferConfigurationFilePath;
    private String validatorFilePath;
    private String mappingFilePath;


    public void parse(String outputFilePath) {

        try {
            try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(outputFilePath),
                    Charset.forName("UTF-8"))) {
                outputStreamWriter.write(parse());
            }
        } catch (ValidationException | IOException e) {
            LOGGER.error("Error validating file.", e);
        }

    }

    public String parse() throws IOException, ValidationException {

        Map<String, Object> context = TomlParser.parse(deploymentConfigurationPath);
        Map<String, Object> enrichedContext = ValueInferrer.infer(context, inferConfigurationFilePath);
        try {
            Map<String, Object> mappedConfigs = KeyMapper.mapWithTomlConfig(enrichedContext, mappingFilePath);
            Validator.validate(mappedConfigs, validatorFilePath);
            return JinjaParser.parse(mappedConfigs, templateFilePath);
        } catch (ValidationException | IOException e) {
            LOGGER.error("Error validating file.", e);
            throw e;
        }
    }

    /**
     * Builder Class for ConfigParser.
     */
    public static final class ConfigParserBuilder {

        private String deploymentConfigurationPath;
        private String templateFilePath;
        private String inferConfigurationFilePath;
        private String validatorFilePath;
        private String mappingFilePath;

        public ConfigParserBuilder() {

            deploymentConfigurationPath = UX_FILE_PATH;
            templateFilePath = TEMPLATE_FILE_PATH;
            inferConfigurationFilePath = INFER_CONFIG_FILE_PATH;
            validatorFilePath = VALIDATOR_FILE_PATH;
            mappingFilePath = MAPPING_FILE_PATH;

        }

        public ConfigParserBuilder withDeploymentConfigurationPath(String deploymentConfigurationPath) {

            this.deploymentConfigurationPath = deploymentConfigurationPath;
            return this;
        }

        public ConfigParserBuilder withTemplateFilePath(String templateFilePath) {

            this.templateFilePath = templateFilePath;
            return this;
        }

        public ConfigParserBuilder withInferConfigurationFilePath(String inferConfigurationFilePath) {

            this.inferConfigurationFilePath = inferConfigurationFilePath;
            return this;
        }

        public ConfigParserBuilder withValidatorFilePath(String validatorFilePath) {

            this.validatorFilePath = validatorFilePath;
            return this;
        }

        public ConfigParserBuilder withMappingFilePath(String mappingFilePath) {

            this.mappingFilePath = mappingFilePath;
            return this;
        }

        public ConfigParser build() {

            ConfigParser configParser = new ConfigParser();
            configParser.templateFilePath = this.templateFilePath;
            configParser.inferConfigurationFilePath = this.inferConfigurationFilePath;
            configParser.validatorFilePath = this.validatorFilePath;
            configParser.deploymentConfigurationPath = this.deploymentConfigurationPath;
            configParser.mappingFilePath = this.mappingFilePath;
            return configParser;
        }
    }
}
