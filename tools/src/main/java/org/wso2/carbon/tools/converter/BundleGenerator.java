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

/**
 * The Java class which defines the tool for Jar-to-OSGi-Bundle conversion.
 *
 * @since 5.0.0
 */
public class BundleGenerator implements CarbonTool {
    /**
     * Executes the JAR to OSGi bundle conversion process.
     *
     * @param toolArgs a {@link String} array providing the source and destination {@link String} path values
     * @throws CarbonToolException if an error occurs when executing the tool
     */
    @Override
    public void execute(String[] toolArgs) throws CarbonToolException {
        int sourceIndex = 0;
        int destinationIndex = 1;

        if (toolArgs.length == 2 && toolArgs[0].length() > 0 && toolArgs[1].length() > 0) {
            Optional<Path> source = getPath(toolArgs[sourceIndex]);
            Optional<Path> destination = getPath(toolArgs[destinationIndex]);

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
                                    BundleGeneratorUtils.convertFromJarToBundle(aDirectoryItem, destination.get(),
                                            new Manifest(), "");
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new CarbonToolException(
                                "An error occurred when making the JAR (Java Archive) to OSGi bundle conversion", e);
                    }
                } else {
                    String message = "The source location and/or bundle destination does not have appropriate " +
                            "read/write permissions.";
                    throw new CarbonToolException(message);
                }
            } else {
                throw new CarbonToolException("Invalid file path(s)");
            }
        } else {
            String message = "Improper usage detected. Usage: jartobundle.sh/.bat [source] [destination], both " +
                    "arguments source and destination are compulsory";
            throw new CarbonToolException(message);
        }
    }

    /**
     * Returns a {@code Path} instance if the {@code String pathValue} is valid.
     *
     * @param pathValue a {@link String} value of the file path
     * @return a {@link Path} instance, if the {@code String pathValue} is valid, else {@code null}
     */
    private static Optional<Path> getPath(String pathValue) {
        Path path;
        if (pathValue != null) {
            path = Paths.get(pathValue);
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

