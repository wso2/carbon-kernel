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

package org.wso2.carbon.nextgen.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration parser class. Entry point to the config parsing logic.
 */
public class ConfigParser {

    private static final Log LOGGER = LogFactory.getLog(ConfigParser.class);

    private static final String UX_FILE_PATH = "deployment.toml";
    private static final String TEMPLATE_FILE_DIR = "templates";
    private static final String INFER_CONFIG_FILE_PATH = "infer.json";
    private static final String VALIDATOR_FILE_PATH = "validator.json";
    private static final String MAPPING_FILE_PATH = "key-mappings.json";
    private static final String DEFAULT_VALUE_FILE_PATH = "default.json";
    private static final String META_DATA_CONFIG_FILE = "metadata_config.properties";
    private static final String META_DATA_TEMPLATE_FILE = "metadata_template.properties";
    private static final String META_DATA_DIRECTORY = ".metadata";
    private String deploymentConfigurationPath;
    private String templateFileDir;
    private String inferConfigurationFilePath;
    private String validatorFilePath;
    private String mappingFilePath;
    private String defaultValueFilePath;
    private String metadataFilePath;
    private String metadataTemplateFilePath;
    private String basePath;

    public void parse(String outputFilePath) throws ConfigParserException {

        try {

            File deploymentConfigurationFile = new File(deploymentConfigurationPath);
            // Check deployment.toml existence
            if (deploymentConfigurationFile.exists()) {
                // deployment.toml exist
                boolean metaDataTemplateExist = MetaDataParser.metaDataFileExist(metadataTemplateFilePath);
                if (metaDataTemplateExist) {
                    // template metadata exist
                    ChangedFileSet templateChanged = MetaDataParser.getChangedFiles(basePath,
                            new String[]{templateFileDir,
                                    inferConfigurationFilePath, defaultValueFilePath, validatorFilePath,
                                    mappingFilePath}, metadataTemplateFilePath);
                    if (templateChanged.isChanged()) {
                        // template Metadata changed then deploy and write
                        LOGGER.warn("Template files changed under " + templateFileDir);
                        LOGGER.warn("Applying Configurations upon new Templates");
                        deployAndStoreMetadata(outputFilePath);
                    } else {
                        // check configurations metadata exist
                        boolean metaDataExist = MetaDataParser.metaDataFileExist(metadataFilePath);
                        if (metaDataExist) {
                            // if exist check if its changed
                            ChangedFileSet configurationChanged = MetaDataParser.getChangedFiles(basePath,
                                    new String[]{outputFilePath}, metadataFilePath);
                            if (configurationChanged.isChanged()) {
                                // if changed override configs
                                configurationChanged.getChangedFiles().forEach(path -> {
                                    LOGGER.warn("Configurations Changed in :" + path);
                                });
                                configurationChanged.getNewFiles().forEach(path -> {
                                    LOGGER.warn("New Configurations Added in :" + path);
                                });
                                LOGGER.warn("Overriding files in configuration directory " + outputFilePath);
                                deployAndStoreMetadata(outputFilePath);
                            } else {
                                // if configuration is not changed check deployment.toml is changed
                                ChangedFileSet deploymentConfigurationChanged =
                                        MetaDataParser.getChangedFiles(basePath,
                                                                       new String[]{deploymentConfigurationPath},
                                                                       metadataFilePath);
                                if (deploymentConfigurationChanged.isChanged()) {
                                    // if deployment.toml is changed then deploy
                                    deployAndStoreMetadata(outputFilePath);
                                } else {
                                    // if there's noting touched then start without applying configurations
                                    LOGGER.info("No new configuration to apply");
                                }
                            }
                        } else {
                            deployAndStoreMetadata(outputFilePath);
                        }

                    }
                } else {
                    LOGGER.warn("Metadata File doesn't exist at " + new File(metadataFilePath).getParent() +
                                " Consider as first startup");
                    // template Metadata not exist then deploy and write
                    deployAndStoreMetadata(outputFilePath);
                }

            } else {
                LOGGER.warn("deployment.toml didn't exist in " + deploymentConfigurationPath + " configurations not " +
                            "overridden");
            }
        } catch (IOException e) {
            throw new ConfigParserException("Error while store new configurations", e);
        }

    }

    private void deployAndStoreMetadata(String outputFilePath) throws IOException, ConfigParserException {

        deploy(outputFilePath);
        LOGGER.info("Writing Metadata Entries...");
        MetaDataParser.storeMetaDataEntries(basePath, metadataTemplateFilePath,
                                            new String[]{templateFileDir, inferConfigurationFilePath,
                                                         defaultValueFilePath,
                                                         validatorFilePath, mappingFilePath});
        MetaDataParser.storeMetaDataEntries(basePath, metadataFilePath, new String[]{outputFilePath,
                                                                                     deploymentConfigurationPath});
    }

    private void deploy(String outputFilePath) throws IOException, ConfigParserException {

        File outputDir = new File(outputFilePath);
        if (outputDir.exists() && outputDir.isDirectory()) {
            Map<String, String> outputs = parse();
            for (Map.Entry<String, String> entry : outputs.entrySet()) {
                File outputFile = new File(outputDir, entry.getKey());
                try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                        new FileOutputStream(outputFile), Charset.forName("UTF-8"))) {
                    outputStreamWriter.write(entry.getValue());
                }
            }
        }
    }

    protected Map<String, String> parse() throws ConfigParserException {

        File templateDir = checkTemplateDirExistence(templateFileDir);

        Map<String, Object> context = TomlParser.parse(deploymentConfigurationPath);
        Map<String, Object> mappedConfigs = KeyMapper.mapWithConfig(context, mappingFilePath);
        Map<String, Object> defaultContext = DefaultParser.addDefaultValues(mappedConfigs, defaultValueFilePath);
        Map<String, Object> enrichedContext = ValueInferrer.infer(defaultContext, inferConfigurationFilePath);
        ReferenceResolver.resolve(enrichedContext);
        Validator.validate(enrichedContext, validatorFilePath);

        Map<String, File> fileNames = getTemplatedFilesMap(templateDir);
        return JinjaParser.parse(enrichedContext, fileNames);
    }

    private File checkTemplateDirExistence(String templateFileDir) throws ConfigParserException {
        File templateDir = new File(templateFileDir);
        if (!templateDir.exists() || !templateDir.isDirectory()) {
            throw new ConfigParserException(String.format("Template directory (%s) does not exist or is not a " +
                                                          "directory", templateDir.getAbsolutePath()));
        }
        return templateDir;
    }

    private Map<String, File> getTemplatedFilesMap(File templateDir) {

        Map<String, File> fileNames = new LinkedHashMap<>();
        File[] files = templateDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.put(file.getParentFile().toPath().relativize(file.toPath()).toString(), file);
                } else {
                    handleDirectories(file, fileNames, file);
                }
            }
        }
        return fileNames;
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
        private String metadataFilePath;
        private String metadataTemplateFilePath;
        private String basePath;

        public ConfigParserBuilder() {

            deploymentConfigurationPath = UX_FILE_PATH;
            templateFileDir = TEMPLATE_FILE_DIR;
            inferConfigurationFilePath = INFER_CONFIG_FILE_PATH;
            validatorFilePath = VALIDATOR_FILE_PATH;
            mappingFilePath = MAPPING_FILE_PATH;
        }

        public ConfigParserBuilder withDeploymentConfigurationPath(String deploymentConfigurationPath) {

            this.deploymentConfigurationPath = deploymentConfigurationPath + File.separator + UX_FILE_PATH;
            return this;
        }

        public ConfigParserBuilder withTemplateFilePath(String templateFilePath) {

            this.templateFileDir = templateFilePath + File.separator + TEMPLATE_FILE_DIR;
            return this;
        }

        public ConfigParserBuilder withInferConfigurationFilePath(String inferConfigurationFilePath) {

            this.inferConfigurationFilePath = inferConfigurationFilePath + File.separator + INFER_CONFIG_FILE_PATH;
            return this;
        }

        public ConfigParserBuilder withValidatorFilePath(String validatorFilePath) {

            this.validatorFilePath = validatorFilePath + File.separator + VALIDATOR_FILE_PATH;
            return this;
        }

        public ConfigParserBuilder withMappingFilePath(String mappingFilePath) {

            this.mappingFilePath = mappingFilePath + File.separator + MAPPING_FILE_PATH;
            return this;
        }

        public ConfigParserBuilder withDefaultValueFilePath(String defaultValueFilePath) {

            this.defaultValueFilePath = defaultValueFilePath + File.separator + DEFAULT_VALUE_FILE_PATH;
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
            configParser.metadataFilePath = this.metadataFilePath;
            configParser.metadataTemplateFilePath = this.metadataTemplateFilePath;
            configParser.basePath = this.basePath;
            return configParser;
        }

        public ConfigParserBuilder withMetaDataFilePath(String metadataFilePath) {

            this.metadataFilePath =
                    metadataFilePath + File.separator + META_DATA_DIRECTORY + File.separator + META_DATA_CONFIG_FILE;

            this.metadataTemplateFilePath =
                    metadataFilePath + File.separator + META_DATA_DIRECTORY + File.separator + META_DATA_TEMPLATE_FILE;
            return this;
        }

        public ConfigParserBuilder withBasePath(String basePath) {

            this.basePath = basePath;
            return this;
        }
    }

    private void handleDirectories(File basePath, Map<String, File> files, File file) {

        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File file1 : fileList) {
                if (file1.isDirectory()) {
                    handleDirectories(basePath, files, file1);
                } else {
                    files.put(file.getParentFile().toPath().relativize(file1.toPath()).toString(), file1);
                }
            }
        }
    }
}
