/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.tools.spi;

import org.wso2.carbon.tools.CarbonTool;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This will add the given SPI to META-INF/services of given jar file, which are need to be exposed to OSGi env.
 */
public class SPICreator implements CarbonTool {

    private static final Logger logger = Logger.getLogger(SPICreator.class.getName());

    @Override
    public void execute(String... toolArgs) {
        if (toolArgs.length != 4) {
            String message = "Improper usage detected. " +
                             "Usage: spicreator.sh/.bat [SPI] [SPI Implementation] [jarfile] [destination]" +
                             "All arguments are compulsory";
            logger.log(Level.INFO, message);
            return;
        }

        String spi = toolArgs[0];
        String spiImpl = toolArgs[1];
        Path jarFile = Paths.get(toolArgs[2]);
        Path destination = Paths.get(toolArgs[3]);

        Path fileName = jarFile.getFileName();
        if (fileName == null) {
            return;
        }
        String jarFileName = fileName.toString();
        Path tmpDir = destination.resolve(jarFileName.substring(0, jarFileName.lastIndexOf(".")));

        Process process = null;
        if (Files.exists(jarFile) && Files.exists(destination) && Files.isWritable(destination) &&
            !Files.exists(tmpDir)) {
            try {
                Files.createDirectory(tmpDir);
                //Copy the original jar file to tmp dir
                Path finalJarPath = tmpDir.resolve(jarFileName);
                Files.copy(jarFile, finalJarPath);

                Path metaInf = tmpDir.resolve(Paths.get("META-INF", "services"));
                final Path parent = metaInf.getParent();
                if (parent != null) {
                    Files.createDirectory(parent);
                }
                Files.createDirectory(metaInf);
                try (PrintWriter printWriter = new PrintWriter(
                        new OutputStreamWriter(new FileOutputStream(metaInf.resolve(spi).toFile()), "UTF-8"))) {
                    printWriter.println(spiImpl);
                    printWriter.flush();
                } catch (FileNotFoundException e) {
                    logger.log(Level.SEVERE, "Couldn't find required SPI file.", e);
                }

                StringBuilder command = new StringBuilder();
                command.append("jar uf ")
                       .append(finalJarPath.toString())
                       .append(" -C ")
                       .append(tmpDir.toString())
                       .append(" ")
                       .append(metaInf.resolve(spi).toString().replace(tmpDir.toString(), ""));
                logger.log(Level.INFO, "Executing '" + command.toString() + "'");
                process = Runtime.getRuntime().exec(command.toString());
                process.waitFor(5, TimeUnit.SECONDS);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while running SPI Creator", e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error while adding SPI", e);
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        } else {
            String message = "The destination location '" + tmpDir.toString() +
                             "' already exist/does not have write permissions or jar file doesn't exist";
            logger.log(Level.WARNING, message);
        }
    }
}
