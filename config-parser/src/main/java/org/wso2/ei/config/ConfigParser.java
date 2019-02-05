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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Configuration parser class. Entry point to the config parsing logic.
 */
public class ConfigParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigParser.class);

    private static final String UX_FILE_PATH = "deployment.toml";
    private static final String TEMPLATE_FILE_DIR = "templates";
    private static final String INFER_CONFIG_FILE_PATH = "infer.json";
    private static final String VALIDATOR_FILE_PATH = "validator.json";
    private static final String MAPPING_FILE_PATH = "key-mappings.toml";
    private String deploymentConfigurationPath;
    private String templateFileDir;
    private String inferConfigurationFilePath;
    private String validatorFilePath;
    private String mappingFilePath;
    private String defaultValueFilePath;

    public void parse(String outputFilePath) {

        File outputDir = new File(outputFilePath);
        if (outputDir.exists() && outputDir.isDirectory()) {
            try {
                Map<String, String> outputs = parse();
                for (Map.Entry<String, String> entry : outputs.entrySet()) {
                    File outputFile = new File(outputDir, entry.getKey());
                    try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                            new FileOutputStream(outputFile), Charset.forName("UTF-8"))) {
                        outputStreamWriter.write(entry.getValue());
                    }
                }
            } catch (ConfigParserException | IOException e) {
                LOGGER.error("Error validating file.", e);
            }
        }
    }

    public Map<String, String> parse() throws IOException, ConfigParserException {

        File templateDir = new File(templateFileDir);
        Set<File> fileNames = new HashSet<>();
        if (templateDir.exists() && templateDir.isDirectory()) {
            for (File file : templateDir.listFiles()) {
                if (file.isFile()) {
                    fileNames.add(file);
                }
            }

        } else {
            throw new ConfigParserException(String.format("Template directory (%s) does not exist or is not a " +
                    "directory", templateDir.getAbsolutePath()));
        }

        Map<String, Object> context = TomlParser.parse(deploymentConfigurationPath);
        Map<String, Object> enrichedContext = ValueInferrer.infer(context, inferConfigurationFilePath);
        Map<String, Object> defaultContext = DefaultParser.addDefaultValues(enrichedContext, defaultValueFilePath);
        try {
            Map<String, Object> mappedConfigs = KeyMapper.mapWithTomlConfig(defaultContext, mappingFilePath);
            ReferenceResolver.resolve(mappedConfigs);
            Validator.validate(mappedConfigs, validatorFilePath);
            return JinjaParser.parse(mappedConfigs, fileNames);
        } catch (ConfigParserException | IOException e) {
            LOGGER.error("Error validating file.", e);
            throw e;
        }
    }

    /**
     * Builder Class for ConfigParser.
     */
    public static final class ConfigParserBuilder {

        private String deploymentConfigurationPath;
        private String templateFileDir;
        private String inferConfigurationFilePath;
        private String validatorFilePath;
        private String mappingFilePath;
        private String defaultValueFilePath;

        public ConfigParserBuilder() {

            deploymentConfigurationPath = UX_FILE_PATH;
            templateFileDir = TEMPLATE_FILE_DIR;
            inferConfigurationFilePath = INFER_CONFIG_FILE_PATH;
            validatorFilePath = VALIDATOR_FILE_PATH;
            mappingFilePath = MAPPING_FILE_PATH;

        }

        public ConfigParserBuilder withDeploymentConfigurationPath(String deploymentConfigurationPath) {

            this.deploymentConfigurationPath = deploymentConfigurationPath;
            return this;
        }

        public ConfigParserBuilder withTemplateFilePath(String templateFilePath) {

            this.templateFileDir = templateFilePath;
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

        public ConfigParserBuilder withDefaultValueFilePath(String defaultValueFilePath) {

            this.defaultValueFilePath = defaultValueFilePath;
            return this;
        }

        public ConfigParser build() {

            ConfigParser configParser = new ConfigParser();
            configParser.templateFileDir = this.templateFileDir;
            configParser.inferConfigurationFilePath = this.inferConfigurationFilePath;
            configParser.validatorFilePath = this.validatorFilePath;
            configParser.deploymentConfigurationPath = this.deploymentConfigurationPath;
            configParser.mappingFilePath = this.mappingFilePath;
            configParser.defaultValueFilePath = this.defaultValueFilePath;
            return configParser;
        }
    }
}
