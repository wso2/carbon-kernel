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
import org.wso2.carbon.nextgen.config.model.Context;
import org.wso2.carbon.nextgen.config.util.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Configuration parser class. Entry point to the config parsing logic.
 */
public class ConfigParser {

    private static final Log log = LogFactory.getLog(ConfigParser.class);

    public static final String UX_FILE_PATH = "deployment.toml";
    private static final String TEMPLATE_FILE_DIR = "templates";
    private static final String INFER_CONFIG_FILE_PATH = "infer.json";
    private static final String VALIDATOR_FILE_PATH = "validator.json";
    private static final String MAPPING_FILE_PATH = "key-mappings.json";
    private static final String DEFAULT_VALUE_FILE_PATH = "default.json";
    private static final String UNIT_RESOLVER_FILE_PATH = "unit-resolve.json";
    private static final String META_DATA_CONFIG_FILE = "metadata_config.properties";
    private static final String META_DATA_TEMPLATE_FILE = "metadata_template.properties";
    private static final String CONFIG_PROPERTIES_FILE = "references.properties";

    private static final String META_DATA_DIRECTORY = ".metadata";
    private static final String JINJA_TEMPLATE_EXTENSION = ".j2";

    /**
     * Parse Toml file and write configurations into relevant places.
     *
     * @param configFilePath    deployment.toml file path
     * @param resourcesDir      templates and rules directory
     * @param outputDir         configuration destination directory
     * @throws ConfigParserException
     */
    public static void parse(String configFilePath, String resourcesDir, String outputDir)
            throws ConfigParserException {
        ConfigPaths.setPaths(configFilePath, resourcesDir, outputDir);
        try {
            File deploymentConfigurationFile = new File(configFilePath);
            if (deploymentConfigurationFile.exists()) {
                if (Boolean.getBoolean(ConfigConstants.OVERRIDE_CONFIGURATION_ALWAYS)) {
                    log.debug("Forceful override configuration");
                    deployAndStoreMetadata();
                } else {
                    List<String> configurationPaths = Arrays.asList(ConfigPaths.getTemplateFileDir(),
                        ConfigPaths.getInferConfigurationFilePath(),
                        ConfigPaths.getDefaultValueFilePath(), ConfigPaths.getValidatorFilePath(),
                        ConfigPaths.getMappingFilePath(), ConfigPaths.getUnitResolverFilePath());
                    ChangedFileSet templateChanged = MetaDataParser.getChangedFiles(outputDir, configurationPaths,
                                   ConfigPaths.getMetadataTemplateFilePath());
                    ChangedFileSet configurationChanged = MetaDataParser.getChangedFiles(outputDir,
                                   ConfigPaths.getMetadataFilePath());
                    boolean referencesChanged = MetaDataParser.isReferencesChanged(
                            ConfigPaths.getMetadataPropertyPath());
                    if (templateChanged.isChanged() || configurationChanged.isChanged() || referencesChanged) {

                        if (templateChanged.isChanged()) {
                            // template Metadata changed then deploy and write
                            templateChanged.getChangedFiles().forEach(path ->
                                log.warn("Configurations templates Changed in :" + path));
                            templateChanged.getNewFiles().forEach(path ->
                                log.warn("New Configurations found in :" + path));

                            log.info("Applying Configurations upon new Templates");
                        }
                        if (configurationChanged.isChanged()) {
                            configurationChanged.getChangedFiles().forEach(path ->
                                log.warn("Configurations Changed in :" + path));
                            log.warn("Overriding files in configuration directory " + outputDir);
                        }
                        if (referencesChanged) {
                            log.warn("Configuration value changed in references, Overriding files in the " +
                                     "configuration directory" + outputDir);
                        }
                        deployAndStoreMetadata();
                    }
                }
            } else {
                log.warn("deployment.toml not found at " + configFilePath);
            }
        } catch (IOException e) {
            throw new ConfigParserException("Error while store new configurations", e);
        }

    }

    private static void backupConfigurations(String configFilePath, String backupPath) throws ConfigParserException {

        File backupFile = new File(backupPath);
        FileUtils.deleteDirectory(backupFile);
        File templateDir = checkTemplateDirExistence(ConfigPaths.getTemplateFileDir());
        FileUtils.writeDirectory(configFilePath, backupPath, getTemplatedFilesMap(templateDir).keySet());
    }

    private static void deployAndStoreMetadata() throws IOException, ConfigParserException {

        log.debug("Backed up the configurations into " + ConfigPaths.getOutputDir() + File.separator +
                 "backup");
        backupConfigurations(ConfigPaths.getOutputDir(), Paths.get(ConfigPaths.getOutputDir(), "backup").toString());
        Context context = new Context();
        Set<String> deployedFileSet = deploy(context, ConfigPaths.getOutputDir());

        log.info("Writing Metadata Entries...");
        Set<String> entries = new HashSet<>(Arrays.asList(ConfigPaths.getTemplateFileDir(),
            ConfigPaths.getInferConfigurationFilePath(), ConfigPaths.getDefaultValueFilePath(),
            ConfigPaths.getUnitResolverFilePath(), ConfigPaths.getValidatorFilePath(),
                                                          ConfigPaths.getMappingFilePath()));

        try {
            MetaDataParser.storeMetaDataEntries(ConfigPaths.getOutputDir(),
                                                ConfigPaths.getMetadataTemplateFilePath(), entries);
            deployedFileSet.add(ConfigPaths.getConfigFilePath());
            MetaDataParser.storeMetaDataEntries(ConfigPaths.getOutputDir(),
                                                ConfigPaths.getMetadataFilePath(), deployedFileSet);
            MetaDataParser.storeReferences(ConfigPaths.getMetadataPropertyPath(), context);
        } catch (ConfigParserException e) {
            log.warn("Error while Storing Metadata Entries", e);
        }
    }

    private static Set<String> deploy(Context context, String outputFilePath) throws IOException,
                                                                                     ConfigParserException {
        File outputDir = new File(outputFilePath);
        Set<String> changedFileSet = new HashSet<>();
        if (outputDir.exists() && outputDir.isDirectory()) {
            Map<String, String> outputs = parse(context);
            for (Map.Entry<String, String> entry : outputs.entrySet()) {
                File outputFile = new File(outputDir, entry.getKey());
                if (!outputFile.getParentFile().exists()) {
                    if (!outputFile.getParentFile().mkdirs()) {
                        throw new ConfigParserException("Error while creating new directory " + outputFilePath);
                    }
                }
                if (!outputFile.createNewFile()) {
                    log.debug(outputFile.getAbsolutePath() + "File already exist");
                }
                try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                        new FileOutputStream(outputFile), Charset.forName("UTF-8"))) {
                    outputStreamWriter.write(entry.getValue());
                    changedFileSet.add(outputFile.getAbsolutePath());
                }
            }
        }
        return changedFileSet;
    }

    static Map<String, String> parse(Context context) throws ConfigParserException {

        File templateDir = checkTemplateDirExistence(ConfigPaths.getTemplateFileDir());
        context = TomlParser.parse(context);
        context = KeyMapper.mapWithConfig(context, ConfigPaths.getMappingFilePath());
        context = ValueInferrer.infer(context, ConfigPaths.getInferConfigurationFilePath());
        context = DefaultParser.addDefaultValues(context, ConfigPaths.getDefaultValueFilePath());
        ReferenceResolver.resolve(context);
        UnitResolver.updateUnits(context, ConfigPaths.getUnitResolverFilePath());
        Validator.validate(context, ConfigPaths.getValidatorFilePath());

        Map<String, File> fileNames = getTemplatedFilesMap(templateDir);
        return JinjaParser.parse(context, fileNames);
    }

    private static File checkTemplateDirExistence(String templateFileDir) throws ConfigParserException {

        File templateDir = new File(templateFileDir);
        if (!templateDir.exists() || !templateDir.isDirectory()) {
            throw new ConfigParserException(String.format("Template directory (%s) does not exist or is not a " +
                    "directory", templateDir.getAbsolutePath()));
        }
        return templateDir;
    }

    private static Map<String, File> getTemplatedFilesMap(File templateDir) {

        Map<String, File> fileNames = new LinkedHashMap<>();
        File[] files = templateDir.listFiles();
        if (Objects.nonNull(files)) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.put(getRelativeFilePath(file, file), file);
                } else {
                    handleDirectories(file, fileNames, file);
                }
            }
        }
        return fileNames;
    }

    private static String getRelativeFilePath(File basePath, File file) {

        String fileName = basePath.getParentFile().toPath()
                .relativize(file.toPath()).toString();
        if (file.getName().endsWith(JINJA_TEMPLATE_EXTENSION)) {
            fileName = fileName.substring(0, (fileName.length() - JINJA_TEMPLATE_EXTENSION.length()));
        }
        return fileName;
    }

    /**
     * Class to keep required file names.
     */
    static class ConfigPaths {

        private static String configFilePath;
        private static String resourcesDir;
        private static String outputDir;
        private static String templateFileDir;
        private static String inferConfigurationFilePath;
        private static String defaultValueFilePath;
        private static String unitResolverFilePath;
        private static String validatorFilePath;
        private static String mappingFilePath;
        private static String metadataFilePath;
        private static String metadataTemplateFilePath;
        private static String metadataPropertyPath;

        static void setPaths(String configFilePath, String resourcesDir, String outputDir) {
            ConfigPaths.configFilePath = configFilePath;
            ConfigPaths.resourcesDir = resourcesDir;
            ConfigPaths.outputDir =    outputDir;
            ConfigPaths.templateFileDir = resourcesDir + File.separator + TEMPLATE_FILE_DIR;
            ConfigPaths.inferConfigurationFilePath = resourcesDir + File.separator + INFER_CONFIG_FILE_PATH;
            ConfigPaths.defaultValueFilePath = resourcesDir + File.separator + DEFAULT_VALUE_FILE_PATH;
            ConfigPaths.unitResolverFilePath = resourcesDir + File.separator + UNIT_RESOLVER_FILE_PATH;
            ConfigPaths.validatorFilePath = resourcesDir + File.separator + VALIDATOR_FILE_PATH;
            ConfigPaths.mappingFilePath = resourcesDir + File.separator + MAPPING_FILE_PATH;
            ConfigPaths.metadataFilePath = resourcesDir + File.separator + META_DATA_DIRECTORY + File.separator +
                                           META_DATA_CONFIG_FILE;
            ConfigPaths.metadataTemplateFilePath = resourcesDir + File.separator + META_DATA_DIRECTORY +
                                                   File.separator + META_DATA_TEMPLATE_FILE;
            ConfigPaths.metadataPropertyPath = resourcesDir + File.separator + META_DATA_DIRECTORY +
                                               File.separator + CONFIG_PROPERTIES_FILE;

        }

        /**
         * Get deployment.toml file path.
         *
         * @return deployment.toml file path
         */
        static String getConfigFilePath() {
            return configFilePath;
        }

        /**
         * Set deployment toml file path.
         *
         * @param configFilePath    deployment toml file path
         */
        static void setConfigFilePath(String configFilePath) {
            ConfigPaths.configFilePath = configFilePath;
        }

        /**
         * Get templates and rules directory path.
         *
         * @return  resource file path
         */
        static String getResourcesDir() {
            return resourcesDir;
        }

        /**
         * Get file configuration destination directory path.
         *
         * @return  output directory path
         */
        static String getOutputDir() {
            return outputDir;
        }

        /**
         * Get template directory file path.
         *
         * @return  template files path
         */
        static String getTemplateFileDir() {
            return templateFileDir;
        }

        /**
         * Get Infer rules definition file path.
         *
         * @return  infer config path
         */
        static String getInferConfigurationFilePath() {
            return inferConfigurationFilePath;
        }

        /**
         * Get Default rules definition file path.
         *
         * @return  default value path
         */
        static String getDefaultValueFilePath() {
            return defaultValueFilePath;
        }

        /**
         * Get unite resolver rules definition file path.
         *
         * @return unit resolver path
         */
        static String getUnitResolverFilePath() {
            return unitResolverFilePath;
        }

        /**
         * Get validator rules definition file path.
         *
         * @return  validator file path
         */
        static String getValidatorFilePath() {
            return validatorFilePath;
        }

        /**
         * Get mapping rules definition file path.
         * @return  mapping file path
         */
        static String getMappingFilePath() {
            return mappingFilePath;
        }

        /**
         * Get metadata file path.
         *
         * @return  meta data file path
         */
        static String getMetadataFilePath() {
            return metadataFilePath;
        }

        /**
         * Get metadata property path.
         *
         * @return  meta data property file path
         */
        static String getMetadataPropertyPath() {
            return metadataPropertyPath;
        }

        /**
         * Get metadata template file path.
         *
         * @return  meta data template file path
         */
        static String getMetadataTemplateFilePath() {
            return metadataTemplateFilePath;
        }

        static void setMetadataFilePaths(String metadataFilePath) {
            ConfigPaths.metadataFilePath = metadataFilePath + File.separator + META_DATA_DIRECTORY +
                                           File.separator + META_DATA_CONFIG_FILE;
            ConfigPaths.metadataTemplateFilePath = metadataFilePath + File.separator + META_DATA_DIRECTORY +
                                                   File.separator + META_DATA_TEMPLATE_FILE;
            ConfigPaths.metadataPropertyPath = metadataFilePath + File.separator + META_DATA_DIRECTORY +
                                               File.separator + CONFIG_PROPERTIES_FILE;
        }

    }


    private static void handleDirectories(File basePath, Map<String, File> files, File file) {

        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File currentFile : fileList) {
                if (currentFile.isDirectory()) {
                    handleDirectories(basePath, files, currentFile);
                } else {
                    files.put(getRelativeFilePath(basePath, currentFile), currentFile);
                }
            }
        }
    }
}
