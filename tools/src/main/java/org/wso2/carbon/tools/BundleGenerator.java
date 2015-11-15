/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.tools;

import org.wso2.carbon.tools.exceptions.JarToBundleConverterException;
import org.wso2.carbon.tools.utils.BundleGeneratorUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Java executor file for the Jar-to-OSGi-Bundle converter.
 *
 * @since 5.0.0
 */
public class BundleGenerator {

    private static final Logger logger = Logger.getLogger(BundleGenerator.class.getName());

    /**
     * Application executor of the JAR to OSGi bundle conversion tool.
     *
     * @param args a {@link String} array providing the source and destination {@link String} path values
     */
    public static void main(String[] args) {
        execute(args);
    }

    /**
     * Executes the JAR to OSGi bundle conversion process.
     *
     * @param args a {@link String} array providing the source and destination {@link String} path values
     */
    public static void execute(String[] args) {
        int sourceIndex = 0;
        int destinationIndex = 1;

        if (args.length == 2 && args[0].length() > 0 && args[1].length() > 0) {
            Path source = getPath(args[sourceIndex]);
            Path destination = getPath(args[destinationIndex]);

            if ((source != null) && (destination != null)) {
                if ((Files.isReadable(source)) && (Files.isWritable(destination))) {
                    try {
                        if (!Files.isDirectory(source)) {
                            BundleGeneratorUtils.convertFromJarToBundle(source, destination, new Manifest(), "");
                        } else {
                            List<Path> directoryContent = BundleGeneratorUtils.listFiles(source);
                            directoryContent.forEach(aDirectoryItem -> {
                                if (aDirectoryItem.toString().endsWith(".jar")) {
                                    try {
                                        BundleGeneratorUtils
                                                .convertFromJarToBundle(aDirectoryItem, destination, new Manifest(),
                                                        "");
                                    } catch (IOException | JarToBundleConverterException e) {
                                        logger.log(Level.SEVERE, e.getMessage(), e);
                                    }
                                }
                            });
                        }
                    } catch (IOException | JarToBundleConverterException e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                } else {
                    String message =
                            "The source location and/or bundle destination may not have appropriate read/write " +
                                    "permissions.";
                    logger.log(Level.WARNING, message);
                }
            } else {
                String message = "Invalid file path(s). Please try again.";
                logger.log(Level.WARNING, message);
            }
        } else {
            String message = "Usage: wso2jartobundle.sh [source] [destination].\nBoth arguments source " +
                    "and destination are compulsory.";
            logger.log(Level.SEVERE, message);
        }
    }

    /**
     * Returns a {@code Path} instance if the {@code String pathValue} is valid.
     *
     * @param pathValue a {@link String} value of the file path
     * @return a {@link Path} instance, if the {@code String pathValue} is valid, else {@code null}
     */
    private static Path getPath(String pathValue) {
        Path path;
        if (pathValue != null) {
            path = Paths.get(pathValue);
            if (Files.exists(path)) {
                return path;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}

