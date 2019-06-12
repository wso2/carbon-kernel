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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.nextgen.config.model.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * ConfigurationFileMetadata Persisting parser.
 */
class MetaDataParser {

    private static Log log = LogFactory.getLog(MetaDataParser.class);

    private MetaDataParser() {}

    static Map<String, String> readLastModifiedValues(String basePath, String path)
            throws ConfigParserException {

        Map<String, String> md5sumValues = new HashMap<>();

        File file = new File(path);
        try {
            if (file.isDirectory()) {
                handleDirectories(basePath, md5sumValues, file);
            } else if (file.isFile()) {
                md5sumValues.put(Paths.get(basePath).relativize(file.toPath()).toString(), getMetadata(file));
            }
        } catch (IOException e) {
            throw new ConfigParserException("Error while reading metadata", e);
        }

        return md5sumValues;
    }

    private static String readLastModifiedValue(String path)
            throws ConfigParserException {

        try {
            File file = new File(path);
            return getMetadata(file);
        } catch (IOException e) {
            throw new ConfigParserException("Error while reading metadata", e);
        }
    }

    private static void handleDirectories(String basePath, Map<String, String> md5sumValues, File directory)
            throws IOException {

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file1 : files) {
                if (file1.isFile()) {
                    md5sumValues.put(Paths.get(basePath).relativize(file1.toPath()).toString(), getMetadata(file1));
                } else if (file1.isDirectory()) {
                    handleDirectories(basePath, md5sumValues, file1);
                }
            }
        }
    }

    private static String getMetadata(File file) throws IOException {

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fileInputStream);
        }
    }

    static ChangedFileSet getChangedFiles(String basePath, List<String> deploymentConfigurationPaths,
                                          String metadataFilePath)
            throws ConfigParserException {

        File metaDataFile = new File(metadataFilePath);
        if (!metaDataFile.exists()) {
            return new ChangedFileSet(true, Collections.emptyList(), Collections.emptyList());
        }
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(metaDataFile)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            throw new ConfigParserException("Metadata File couldn't Read", e);
        }
        ChangedFileSet changedFileSet = new ChangedFileSet();
        for (String deploymentConfigurationPath : deploymentConfigurationPaths) {
            Map<String, String> actualLastModifiedValues = readLastModifiedValues(basePath,
                    deploymentConfigurationPath);
            actualLastModifiedValues.forEach((path, lastModifiedValue) -> {
                String lastModifiedValueFromFile = (properties.containsKey(path) ?
                        (String) properties.get(path) : null);
                if (StringUtils.isNotEmpty(lastModifiedValueFromFile)) {
                    if (!lastModifiedValue.equals(lastModifiedValueFromFile)) {
                        changedFileSet.addChangedFile(path);
                    }
                } else {
                    changedFileSet.addNewFile(path);
                }
            });
        }
        return changedFileSet;
    }

    static ChangedFileSet getChangedFiles(String basePath,
                                          String metadataFilePath)
            throws ConfigParserException {

        File metaDataFile = new File(metadataFilePath);
        if (!metaDataFile.exists()) {
            return new ChangedFileSet(true, Collections.emptyList(), Collections.emptyList());
        }
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(metaDataFile)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            log.error("Metadata File couldn't Read", e);
            throw new ConfigParserException("Metadata File couldn't Read", e);
        }
        ChangedFileSet changedFileSet = new ChangedFileSet();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            String path = (String) entry.getKey();
            String lastModifiedValue = (String) entry.getValue();
            String actualLastModifiedValue = readLastModifiedValue(Paths.get(basePath, path).toString());
            if (StringUtils.isNotEmpty(actualLastModifiedValue)) {
                if (!lastModifiedValue.equals(actualLastModifiedValue)) {
                    changedFileSet.addChangedFile(path);
                }
            }
        }
        return changedFileSet;
    }

    static void storeMetaDataEntries(String basePath, String outputFilePath, Set<String> entries)
            throws ConfigParserException {

        File outputFile = new File(outputFilePath);
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new ConfigParserException("Error while creating new directory " + outputFile.getAbsolutePath());
        }
        Properties properties = new Properties();
        for (String entry : entries) {
            properties.putAll(readLastModifiedValues(basePath, entry));
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath)) {
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            throw new ConfigParserException("Error while storing metadata", e);
        }
    }

    static void storeReferences(String metadataPropertyPath, Context context)
            throws ConfigParserException {

        File outputFile = new File(metadataPropertyPath);
        if (!outputFile.getParentFile().exists() && !outputFile.getParentFile().mkdirs()) {
            throw new ConfigParserException("Error while creating new directory " + outputFile.getAbsolutePath());
        }
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(metadataPropertyPath),
                StandardCharsets.UTF_8)) {
            Properties properties = new Properties();
            context.getResolvedEnvironmentVariables().forEach((key, value) -> {
                properties.put(ConfigConstants.ENVIRONMENT_VARIABLE_PREFIX.concat(key), value);
            });
            context.getResolvedSystemProperties().forEach((key, value) -> {
                properties.put(ConfigConstants.SYSTEM_PROPERTY_PREFIX.concat(key), value);
            });
            properties.store(outputStreamWriter, null);
        } catch (IOException e) {
            throw new ConfigParserException("Error While storing References", e);
        }
    }

    static boolean isReferencesChanged(String metadataPropertyPath) {

        boolean status = false;
        if (new File(metadataPropertyPath).exists()) {
            Properties references = new Properties();
            try (FileInputStream fileInputStream = new FileInputStream(metadataPropertyPath)) {
                references.load(fileInputStream);
            } catch (IOException e) {
                log.error("Error while reading References", e);
            }
            for (Map.Entry<Object, Object> entry : references.entrySet()) {
                String key = (String) entry.getKey();
                if (key.contains(ConfigConstants.SYSTEM_PROPERTY_PREFIX)) {
                    String value = System.getProperty(key.replace(ConfigConstants.SYSTEM_PROPERTY_PREFIX, ""));
                    if (!entry.getValue().equals(value)) {
                        status = true;
                        break;
                    }
                } else if (key.contains(ConfigConstants.ENVIRONMENT_VARIABLE_PREFIX)) {
                    String value = System.getenv(key.replace(ConfigConstants.ENVIRONMENT_VARIABLE_PREFIX, ""));
                    if (!entry.getValue().equals(value)) {
                        status = true;
                        break;
                    }
                }
            }
        }
        return status;
    }
}
