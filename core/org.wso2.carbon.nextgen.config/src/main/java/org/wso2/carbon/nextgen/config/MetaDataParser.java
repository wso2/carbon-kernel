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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ConfigurationFileMetadata Persisting parser.
 */
public class MetaDataParser {

    private static Log logger = LogFactory.getLog(MetaDataParser.class);

    public static Map<String, String> readLastModifiedValues(String basePath, String path)
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

    public static ChangedFileSet getChangedFiles(String basePath, String[] deploymentConfigurationPaths,
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
            logger.error("Metadata File couldn't Read", e);
            throw new ConfigParserException("Metadata File couldn't Read", e);
        }
        List<String> changedFiles = new ArrayList<>();
        List<String> newFiles = new ArrayList<>();
        for (String deploymentConfigurationPath : deploymentConfigurationPaths) {
            Map<String, String> actualLastModifiedValues = readLastModifiedValues(basePath,
                    deploymentConfigurationPath);
            for (Map.Entry<String, String> entry : actualLastModifiedValues.entrySet()) {
                String path = entry.getKey();
                String lastModifiedTimeStamp = entry.getValue();
                String lastModified = properties.getProperty(path);
                if (StringUtils.isNotEmpty(lastModified)) {
                    if (!lastModifiedTimeStamp.equals(lastModified)) {
                        changedFiles.add(path);
                    }
                } else {
                    newFiles.add(path);
                }
            }
        }
        if (!changedFiles.isEmpty() || !newFiles.isEmpty()) {
            return new ChangedFileSet(true, changedFiles, newFiles);
        }
        return new ChangedFileSet(false, changedFiles, newFiles);
    }

    public static boolean metaDataFileExist(String metadataFilePath) {

        File metaDataFile = new File(metadataFilePath);
        return metaDataFile.exists();
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
            justification = "return not need in mkdirs()")
    public static void storeMetaDataEntries(String basePath, String outputFilePath, String[] entries)
            throws ConfigParserException {

        File outputFile = new File(outputFilePath);
        outputFile.getParentFile().mkdirs();
        Properties properties = new Properties();
        for (String entry : entries) {
            properties.putAll(readLastModifiedValues(basePath, entry));
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath)) {
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            logger.error("error while storing metadata", e);
            throw new RuntimeException("error while storing metadata");
        }
    }
}
