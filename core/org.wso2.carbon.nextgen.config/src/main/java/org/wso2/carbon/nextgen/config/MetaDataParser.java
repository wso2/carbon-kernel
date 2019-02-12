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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ConfigurationFileMetadata Persisting parser.
 */
public class MetaDataParser {

    private static Log logger = LogFactory.getLog(MetaDataParser.class);

    public static Map<String, String> readLastModifiedValues(String path) {

        Map<String, String> md5sumValues = new HashMap<>();

        File file = new File(path);

        if (file.isDirectory()) {
            handleDirectories(md5sumValues, file);
        } else if (file.isFile()) {
            md5sumValues.put(file.getAbsolutePath(), getLastModified(file));
        }

        return md5sumValues;
    }

    private static void handleDirectories(Map<String, String> md5sumValues, File directory) {

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file1 : files) {
                if (file1.isFile()) {
                    md5sumValues.put(file1.getAbsolutePath(), getLastModified(file1));
                } else if (file1.isDirectory()) {
                    handleDirectories(md5sumValues, file1);
                }
            }
        }
    }

    public static String getLastModified(File file) {

        return String.valueOf(file.lastModified());
    }

    public static boolean isFilesChanged(String deploymentConfigurationPath, String metadataFilePath) {

        File metaDataFile = new File(metadataFilePath);
        if (!metaDataFile.exists()) {
            return true;
        }
        Properties properties = new Properties();

        try (FileInputStream fileInputStream = new FileInputStream(metaDataFile)) {
            properties.load(fileInputStream);
        } catch (IOException e) {
            logger.error("Metadata File couldn't Read", e);
            return true;

        }
        Map<String, String> actualLastModifiedValues = readLastModifiedValues(deploymentConfigurationPath);
        for (Map.Entry<String, String> entry : actualLastModifiedValues.entrySet()) {
            String path = entry.getKey();
            String lastModifiedTimeStamp = entry.getValue();
            String lastModified = properties.getProperty(path);
            if (StringUtils.isNotEmpty(lastModified)) {
                if (!lastModifiedTimeStamp.equals(lastModified)) {
                    return true;
                }
            } else {
                logger.warn("New configuration File Detected :" + path);
            }
        }
        return false;
    }

    public static boolean metaDataFileExist(String metadataFilePath) {

        File metaDataFile = new File(metadataFilePath);
        return metaDataFile.exists();
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTICE",
            justification = "return not need in mkdirs()")
    public static void storeMetaDataEntries(String outputFilePath, String[] entries) {

        File outputFile = new File(outputFilePath);
        outputFile.mkdirs();
        Properties properties = new Properties();
        for (String entry : entries) {
            properties.putAll(readLastModifiedValues(entry));
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputFilePath)) {
            properties.store(fileOutputStream, null);
        } catch (IOException e) {
            logger.error("error while storing metadata", e);
            throw new RuntimeException("error while storing metadata");
        }
    }
}
