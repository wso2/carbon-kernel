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
import org.wso2.carbon.tools.converter.utils.BundleGeneratorUtils;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.wso2.carbon.tools.Constants.JAR_MANIFEST_FOLDER;
import static org.wso2.carbon.tools.Constants.MANIFEST_FILE_NAME;
import static org.wso2.carbon.tools.Constants.SERVICES;
import static org.wso2.carbon.tools.Constants.SPI_PROVIDER;

/**
 * This will add the given SPI to META-INF/services of given jar file, which are need to be exposed to OSGi env.
 *
 * @since 5.2.1
 */
public class SPICreator implements CarbonTool {

    private static final Logger logger = Logger.getLogger(SPICreator.class.getName());

    @Override
    public void execute(String... toolArgs) {
        if (toolArgs.length != 4) {
            String message = "Improper usage detected. " +
                             "Usage: spicreator.sh|bat [SPI] [SPI Implementation] [jarfile] [destination] " +
                             "All 4 arguments are compulsory.";
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

                Path metaInf = tmpDir.resolve(Paths.get(JAR_MANIFEST_FOLDER, SERVICES));
                final Path parent = metaInf.getParent();
                if (parent != null) {
                    Files.createDirectory(parent);
                }
                Files.createDirectory(metaInf);

                StringBuilder existingSPIs = new StringBuilder(spiImpl).append("\n");
                // Get existing SPI impl if exist
                try (JarFile jar = new JarFile(finalJarPath.toString())) {
                    if (jar.getJarEntry("META-INF/services/" + spi) != null) {
                        Map<String, String> env = new HashMap<>();
                        env.put("create", "true");
                        // locate file system by using the syntax
                        // defined in java.net.JarURLConnection
                        URI uri = URI.create("jar:file:" + finalJarPath.toString());
                        try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                            Path existingSPIPath = zipfs.getPath(JAR_MANIFEST_FOLDER, SERVICES, spi);
                            Files.readAllLines(existingSPIPath).forEach(s -> existingSPIs.append(s).append("\n"));
                        }
                    }
                }

                try (PrintWriter printWriter = new PrintWriter(
                        new OutputStreamWriter(new FileOutputStream(metaInf.resolve(spi).toFile()), "UTF-8"))) {
                    printWriter.println(existingSPIs.toString());
                    printWriter.flush();
                } catch (FileNotFoundException e) {
                    logger.log(Level.SEVERE, "Couldn't find required SPI file.", e);
                    throw e;
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
                //Add 'SPI-Provider' header to MANIFEST.MF
                addSPIProviderHeader(finalJarPath, tmpDir);

                logger.log(Level.INFO, "Created jar file: '" + finalJarPath.toString());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while running SPI Creator", e);
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE, "Error while adding SPI", e);
            } catch (CarbonToolException e) {
                logger.log(Level.SEVERE, "Error while converting to bundle", e);
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

    /**
     * Add SPI-Provider: * header to MANIFEST.MF file.
     *
     * @param finalJarPath Path of the jar file
     * @param destination  Destination path where jar get created.
     * @throws IOException         if an error occur while reading/writing jar file
     * @throws CarbonToolException if an error occur when converting to bundle
     */
    private void addSPIProviderHeader(Path finalJarPath, Path destination) throws IOException, CarbonToolException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().putValue(SPI_PROVIDER, "*");
        if (BundleGeneratorUtils.isOSGiBundle(finalJarPath)) {
            logger.log(Level.INFO, "Adding '" + SPI_PROVIDER + ": *' to " + MANIFEST_FILE_NAME);
            Path manifestmfFile = destination.resolve(MANIFEST_FILE_NAME);
            try (JarFile jar = new JarFile(finalJarPath.toString()); PrintWriter printWriter = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(manifestmfFile.toFile()), "UTF-8"))) {
                jar.getManifest().getMainAttributes().forEach((key, val) -> manifest.getMainAttributes().put(key, val));
                manifest.getMainAttributes().forEach((o, o2) -> printWriter.println(o + ": " + o2));
                printWriter.flush();
            }

            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            URI uri = URI.create("jar:file:" + finalJarPath.toString());
            try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
                Path pathInZipfile = zipfs.getPath(JAR_MANIFEST_FOLDER, MANIFEST_FILE_NAME);
                Files.copy(manifestmfFile, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
            }
        } else {
            logger.log(Level.INFO, "Running jar to bundle conversion");
            BundleGeneratorUtils.convertFromJarToBundle(finalJarPath, destination, manifest, "");
        }
    }
}
