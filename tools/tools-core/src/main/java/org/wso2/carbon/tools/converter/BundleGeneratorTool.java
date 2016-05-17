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
package org.wso2.carbon.tools.converter;

import org.wso2.carbon.tools.CarbonTool;
import org.wso2.carbon.tools.converter.utils.BundleGeneratorUtils;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Java class which defines the tool for Jar-to-OSGi-Bundle conversion.
 *
 * @since 5.0.0
 */
public class BundleGeneratorTool implements CarbonTool {
    private static final Logger logger = Logger.getLogger(BundleGeneratorTool.class.getName());

    /**
     * Executes the JAR to OSGi bundle conversion process.
     *
     * @param toolArgs a {@link String} array providing the source and destination {@link String} path values
     */
    @Override
    public void execute(String[] toolArgs) {
        int sourceIndex = 0;
        int destinationIndex = 1;
        int executingDirectoryIndex = 2;

        if ((toolArgs.length == 3) && (!toolArgs[sourceIndex].isEmpty()) && (!toolArgs[destinationIndex].isEmpty())
                && (!toolArgs[executingDirectoryIndex].isEmpty())) {
            Optional<Path> source = getPath(toolArgs[sourceIndex], toolArgs[executingDirectoryIndex]);
            Optional<Path> destination = getPath(toolArgs[destinationIndex], toolArgs[executingDirectoryIndex]);

            if ((source.isPresent()) && (destination.isPresent())) {
                if ((Files.isReadable(source.get())) && (Files.isWritable(destination.get()))) {
                    try {
                        if (!Files.isDirectory(source.get())) {
                            BundleGeneratorUtils.
                                    convertFromJarToBundle(source.get(), destination.get(), new Manifest(), "");
                        } else {
                            List<Path> directoryContent = BundleGeneratorUtils.listFiles(source.get());
                            for (Path aDirectoryItem : directoryContent) {
                                if (aDirectoryItem.toString().endsWith(".jar")) {
                                    BundleGeneratorUtils.convertFromJarToBundle(
                                            aDirectoryItem, destination.get(), new Manifest(), "");
                                }
                            }
                        }
                    } catch (IOException | CarbonToolException e) {
                        logger.log(Level.SEVERE,
                                "An error occurred when making the JAR (Java Archive) to OSGi bundle conversion", e);
                    }
                } else {
                    String message = "The source location and/or bundle destination does not have appropriate " +
                            "read/write permissions.";
                    logger.log(Level.WARNING, message);
                }
            } else {
                logger.log(Level.WARNING, "Invalid file path(s)");
            }
        } else {
            String message = "Improper usage detected. " +
                    "Usage: jartobundle.sh/.bat [source file/directory] [destination folder], both " +
                    "arguments source and destination are compulsory";
            logger.log(Level.INFO, message);
        }
    }

    /**
     * Returns a {@code Path} instance if the {@code String userPathInput} is valid.
     *
     * @param userPathInput      a {@link String} value of the file path input by the user
     * @param executingDirectory the directory from which the script for the tool was executed
     * @return a {@link Path} instance, if the {@code String userPathInput} is valid, else {@code null}
     */
    private static Optional<Path> getPath(String userPathInput, String executingDirectory) {
        Path path;
        if (userPathInput != null) {
            path = Paths.get(userPathInput);

            if (!path.isAbsolute()) {
                path = Paths.get(executingDirectory, userPathInput);
            }

            if (Files.exists(path)) {
                return Optional.of(path);
            } else {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}

