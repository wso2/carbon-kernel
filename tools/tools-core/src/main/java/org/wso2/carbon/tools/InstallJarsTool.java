/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.tools.converter.utils.BundleGeneratorUtils;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class defines a tool which can automatically installs jars as OSGI bundles
 * in WSO2 Carbon Server.
 *
 * @since 5.1.0
 */
public class InstallJarsTool implements CarbonTool {

    private static final Logger logger = Logger.getLogger(InstallJarsTool.class.getName());

    /**
     * Executes the WSO2 Carbon OSGi-lib deployer tool based on the specified arguments.
     *
     * @param toolArgs the {@link String} argument specifying the Carbon Runtime and CARBON_HOME
     */
    @Override
    public void execute(String... toolArgs) {
        String carbonHome = toolArgs[0];
        Path sourcePath = Paths.get(carbonHome.concat("/jars"));
        Path outputPath = Paths.get(carbonHome.concat("/lib"));
        File jarsDir = sourcePath.toFile();

        if ((Files.isReadable(sourcePath)) && (Files.isWritable(outputPath))) {
            try {
                File[] files = jarsDir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".jar");
                    }
                });
                if (files != null && files.length > 0) {
                    for (File jar : files) {
                        BundleGeneratorUtils.convertFromJarToBundle(
                                jar.toPath(), outputPath, new Manifest(), "");
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
    }
}
