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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.nextgen.config.util.FileUtils;
import org.wso2.ciphertool.utils.KeyStoreUtil;
import org.wso2.ciphertool.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.crypto.Cipher;

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
    private static final String UNIT_RESOLVER_FILE_PATH = "unit-resolve.json";
    private static final String META_DATA_CONFIG_FILE = "metadata_config.properties";
    private static final String META_DATA_TEMPLATE_FILE = "metadata_template.properties";
    private static final String CONFIG_PROPERTIES_FILE = "references.properties";

    private static final String META_DATA_DIRECTORY = ".metadata";
    private static final String JINJA_TEMPLATE_EXTENSION = ".j2";
    private String deploymentConfigurationPath;
    private String templateFileDir;
    private String inferConfigurationFilePath;
    private String unitResolverFilePath;
    private String validatorFilePath;
    private String mappingFilePath;
    private String defaultValueFilePath;
    private String metadataFilePath;
    private String metadataTemplateFilePath;
    private String basePath;
    private String backupPath;
    private String metadataPropertyPath;

    public void parse(String outputFilePath) throws ConfigParserException {

        try {

            File deploymentConfigurationFile = new File(deploymentConfigurationPath);
            // Check deployment.toml existence
            if (deploymentConfigurationFile.exists()) {
                if (Boolean.getBoolean(ConfigConstants.OVERRIDE_CONFIGURATION_ALWAYS)) {
                    LOGGER.info("Configurations Override Always was enable");
                    deployAndStoreMetadata(outputFilePath);
                } else {
                    List<String> configurationPaths = Arrays.asList(templateFileDir, inferConfigurationFilePath,
                            defaultValueFilePath, validatorFilePath, mappingFilePath, unitResolverFilePath);
                    ChangedFileSet templateChanged = MetaDataParser.getChangedFiles(basePath, configurationPaths,
                            metadataTemplateFilePath);
                    ChangedFileSet configurationChanged = MetaDataParser.getChangedFiles(basePath,
                            metadataFilePath);
                    boolean referencesChanged = MetaDataParser.isReferencesChanged(metadataPropertyPath);
                    if (templateChanged.isChanged() || configurationChanged.isChanged() | referencesChanged) {

                        if (templateChanged.isChanged()) {
                            // template Metadata changed then deploy and write
                            templateChanged.getChangedFiles().forEach(path ->
                                    LOGGER.warn("Configurations templates Changed in :" + path));
                            templateChanged.getNewFiles().forEach(path ->
                                    LOGGER.warn("New Configurations found in :" + path));

                            LOGGER.warn("Applying Configurations upon new Templates");
                        }
                        if (configurationChanged.isChanged()) {
                            configurationChanged.getChangedFiles().forEach(path ->
                                    LOGGER.warn("Configurations Changed in :" + path));
                            LOGGER.warn("Overriding files in configuration directory " + outputFilePath);
                        }
                        if (referencesChanged) {
                            LOGGER.warn("Configuration value changed in references, Overriding files in the " +
                                    "configuration directory" + outputFilePath);
                        }
                    }
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

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
            justification = "return not need in mkdirs()")
    private void backupConfigurations(String configFilePath, String backupPath) throws ConfigParserException {

        File backupFile = new File(backupPath);
        FileUtils.deleteDirectory(backupFile);
        File templateDir = checkTemplateDirExistence(templateFileDir);
        FileUtils.writeDirectory(configFilePath, backupPath, getTemplatedFilesMap(templateDir).keySet());
    }

    private void deployAndStoreMetadata(String outputFilePath) throws IOException, ConfigParserException {

        LOGGER.info("Backed up the configurations into " + basePath + File.separator +
                "backup");
        backupConfigurations(outputFilePath, backupPath);

        Set<String> deployedFileSet = deploy(outputFilePath);

        LOGGER.info("Writing Metadata Entries...");
        Set<String> entries = new HashSet<>(Arrays.asList(templateFileDir,
                inferConfigurationFilePath, defaultValueFilePath, unitResolverFilePath, validatorFilePath,
                mappingFilePath));
        MetaDataParser.storeMetaDataEntries(basePath, metadataTemplateFilePath, entries);
        deployedFileSet.add(deploymentConfigurationPath);
        MetaDataParser.storeMetaDataEntries(basePath, metadataFilePath, deployedFileSet);

    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
            justification = "return not need in mkdirs()")
    private Set<String> deploy(String outputFilePath) throws IOException, ConfigParserException {

        File outputDir = new File(outputFilePath);
        Set<String> changedFileSet = new HashSet<>();
        if (outputDir.exists() && outputDir.isDirectory()) {
            Map<String, String> outputs = parse();
            for (Map.Entry<String, String> entry : outputs.entrySet()) {
                File outputFile = new File(outputDir, entry.getKey());
                if (!outputFile.getParentFile().exists()) {
                    outputFile.getParentFile().mkdirs();
                }
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
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

    protected Map<String, String> parse() throws ConfigParserException {

        File templateDir = checkTemplateDirExistence(templateFileDir);
        TomlParser tomlParser = new TomlParser(deploymentConfigurationPath);
        Map<String, Object> context = tomlParser.parse();
        context = KeyMapper.mapWithConfig(context, mappingFilePath);
        context = ValueInferrer.infer(context, inferConfigurationFilePath);
        context = DefaultParser.addDefaultValues(context, defaultValueFilePath);
        Map secrets = tomlParser.getSecrets();
        Properties references = new Properties();
        ReferenceResolver.resolve(context, secrets, references);
        MetaDataParser.storeReferences(metadataPropertyPath, references);
        UnitResolver.updateUnits(context, unitResolverFilePath);
        Validator.validate(context, validatorFilePath);

        Map<String, File> fileNames = getTemplatedFilesMap(templateDir);
        return JinjaParser.parse(context, fileNames);
    }

    protected void handleSecVaultProperties() {

        org.wso2.ciphertool.utils.Utils.setSystemProperties();
        org.wso2.ciphertool.utils.Utils.writeToSecureConfPropertyFile();
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
        if (Objects.nonNull(files)) {
            for (File file : files) {
                if (file.isFile()) {
                    fileNames.put(getFileNameWithRelativePath(file, file), file);
                } else {
                    handleDirectories(file, fileNames, file);
                }
            }
        }
        return fileNames;
    }

    private String getFileNameWithRelativePath(File basePath, File file) {

        String fileName = basePath.getParentFile().toPath()
                .relativize(file.toPath()).toString();
        if (file.getName().endsWith(JINJA_TEMPLATE_EXTENSION)) {
            fileName = fileName.substring(0, (fileName.length() - JINJA_TEMPLATE_EXTENSION.length()));
        }
        return fileName;
    }

    /**
     * Builder Class for ConfigParser.
     */
    public static final class ConfigParserBuilder {

        private String deploymentConfigurationPath;
        private String templateFileDir;
        private String inferConfigurationFilePath;
        private String unitResolverFilePath;
        private String validatorFilePath;
        private String mappingFilePath;
        private String defaultValueFilePath;
        private String metadataFilePath;
        private String metadataTemplateFilePath;
        private String basePath;
        private String backupPath;
        private String metadataPropertyPath;

        public ConfigParserBuilder() {

            deploymentConfigurationPath = UX_FILE_PATH;
            templateFileDir = TEMPLATE_FILE_DIR;
            inferConfigurationFilePath = INFER_CONFIG_FILE_PATH;
            validatorFilePath = VALIDATOR_FILE_PATH;
            mappingFilePath = MAPPING_FILE_PATH;
            unitResolverFilePath = UNIT_RESOLVER_FILE_PATH;
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

        public ConfigParserBuilder withUnitResolverFilePath(String defaultUnitResolverPath) {

            this.unitResolverFilePath = defaultUnitResolverPath + File.separator + UNIT_RESOLVER_FILE_PATH;
            return this;
        }


        public ConfigParser build() {

            ConfigParser configParser = new ConfigParser();
            configParser.templateFileDir = this.templateFileDir;
            configParser.inferConfigurationFilePath = this.inferConfigurationFilePath;
            configParser.unitResolverFilePath = this.unitResolverFilePath;
            configParser.validatorFilePath = this.validatorFilePath;
            configParser.deploymentConfigurationPath = this.deploymentConfigurationPath;
            configParser.mappingFilePath = this.mappingFilePath;
            configParser.defaultValueFilePath = this.defaultValueFilePath;
            configParser.metadataFilePath = this.metadataFilePath;
            configParser.metadataTemplateFilePath = this.metadataTemplateFilePath;
            configParser.basePath = this.basePath;
            configParser.backupPath = this.backupPath;
            configParser.metadataPropertyPath = this.metadataPropertyPath;
            return configParser;
        }

        public ConfigParserBuilder withMetaDataFilePath(String metadataFilePath) {

            this.metadataFilePath =
                    metadataFilePath + File.separator + META_DATA_DIRECTORY + File.separator + META_DATA_CONFIG_FILE;

            this.metadataTemplateFilePath =
                    metadataFilePath + File.separator + META_DATA_DIRECTORY + File.separator + META_DATA_TEMPLATE_FILE;
            this.metadataPropertyPath =
                    metadataFilePath + File.separator + META_DATA_DIRECTORY + File.separator + CONFIG_PROPERTIES_FILE;
            return this;
        }

        public ConfigParserBuilder withBasePath(String basePath) {

            this.basePath = basePath;
            this.backupPath = Paths.get(basePath, "backup").toString();
            return this;
        }
    }

    private void handleDirectories(File basePath, Map<String, File> files, File file) {

        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (File currentFile : fileList) {
                if (currentFile.isDirectory()) {
                    handleDirectories(basePath, files, currentFile);
                } else {
                    files.put(getFileNameWithRelativePath(basePath, currentFile), currentFile);
                }
            }
        }
    }

    public void handleEncryption() throws ConfigParserException {
        handleSecVaultProperties();

        Map<String, String> secretMap = new TomlParser(deploymentConfigurationPath).getSecrets();
        Cipher cipher = null;
        if (!secretMap.isEmpty()) {
            cipher = getCipher();
        }
        for (Map.Entry<String, String> entry : secretMap.entrySet()) {
            String key = entry.getKey();
            String value = getUnEncryptedValue(entry.getValue());
            if (StringUtils.isNotEmpty(value)) {
                String encryptedValue = getEncryptedValue(cipher, value);
                secretMap.replace(key, encryptedValue);
            }
        }
        updateDeploymentConfigurationWithEncryptedKeys(secretMap);
    }

    protected String getEncryptedValue(Cipher cipher, String value) {

        return Utils.doEncryption(cipher, value);
    }

    protected Cipher getCipher() {
        Utils.setSystemProperties();
        return KeyStoreUtil.initializeCipher();
    }

    private String getUnEncryptedValue(String value) {

        String[] unEncryptedRefs = StringUtils.substringsBetween(value, ConfigConstants.SECTION_PREFIX,
                ConfigConstants.SECTION_SUFFIX);
        if (unEncryptedRefs != null && unEncryptedRefs.length == 1) {
            return unEncryptedRefs[0];
        } else {
            return null;
        }

    }

    private void updateDeploymentConfigurationWithEncryptedKeys(Map<String, String> encryptedKeyMap)
            throws ConfigParserException {

        try {
            List<String> lines = Files.readAllLines(Paths.get(deploymentConfigurationPath));
            try (BufferedWriter bufferedWriter =
                         new BufferedWriter(new OutputStreamWriter(new FileOutputStream(deploymentConfigurationPath),
                                 StandardCharsets.UTF_8))) {
                boolean found = false;
                for (String line : lines) {
                    if (found) {
                        if (line.matches("[.+]")) {
                            found = false;
                        } else {
                            StringTokenizer stringTokenizer = new StringTokenizer(line,
                                    ConfigConstants.KEY_VALUE_SEPERATOR);
                            if (stringTokenizer.hasMoreTokens()) {
                                String key = stringTokenizer.nextToken();
                                String value = encryptedKeyMap.get(key.trim());
                                line = key.concat(" = \"").concat(value).concat("\"");
                            }
                        }
                    } else {
                        if (ConfigConstants.SECRETS_SECTION.equals(line.trim())) {
                            found = true;
                        }
                    }
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            throw new ConfigParserException("Error while writing encrypted values into deployment file", e);
        }

    }

}
