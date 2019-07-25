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
import org.wso2.carbon.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines a tool which can automatically install jars as OSGI bundles
 * in WSO2 Carbon Server.
 * <p>
 * Here is the jar installing algorithm.
 * 1) Creates a backup at CARBON_HOME/_lib (if it does not exist). Backup all the bundles in the
 * CARBON_HOME/lib directory.
 * 2) Converts the Jars inside CARBON_HOME/jars directory as OSGI bundles and copy them to CARBON_HOME/lib directory and
 * Writes the last directory updated timestamp to a meta file at CARBON_HOME/.meta/jarsMeta
 * 3) Copies the bundles in the CARBON_HOME/bundles directory to CARBON_HOME/lib directory and writes
 * the last directory updated timestamp to a meta file at CARBON_HOME/.meta/bundleMeta
 * 4) When the CARBON_HOME/jars or/and CARBON_HOME/bundles directory is updated, restores the default
 * CARBON_HOME/lib and performs the installation process again.
 */
public class InstallJarsTool implements CarbonTool {

    private static final Logger logger = Logger.getLogger(InstallJarsTool.class.getName());
    private static final String BUNDLE_BACKUP_DIR_NAME = "_lib";
    private static final String META_DATA_DIR_NAME = ".meta";
    private static final String LIB_DIR_NAME = "lib";
    private static final String JARS_DIR_NAME = "jars";
    private static final String BUNDLE_DIR_NAME = "bundles";
    private static final String JARS_META_FILE = "jarsMeta";
    private static final String BUNDLE_META_FILE = "bundleMeta";
    private static final Pattern EXTRACT_JAR_NAME_PATTERN = Pattern.compile("(.*)-\\d+\\.\\d+\\.\\d+(\\.jar)$");
    private String carbonHome;

    /**
     * Executes the WSO2 Carbon Install Jars tool.
     *
     * @param toolArgs the {@link String} argument specifying the CARBON_HOME
     */
    @Override
    public void execute(String... toolArgs) {
        carbonHome = toolArgs[0];
        Path jarSourcePath = Paths.get(carbonHome , JARS_DIR_NAME);
        Path bundleSourcePath = Paths.get(carbonHome, BUNDLE_DIR_NAME);
        Path outputPath = Paths.get(carbonHome, LIB_DIR_NAME);

        File jarsDir = jarSourcePath.toFile();
        File bundlesDir = bundleSourcePath.toFile();
        File outputDir = outputPath.toFile();

        if ((Files.isReadable(jarSourcePath)) && (Files.isReadable(bundleSourcePath)) &&
                (Files.isWritable(outputPath))) {
            try {
                File metaDir = new File(carbonHome, META_DATA_DIR_NAME);
                File[] jars = listJarsInDirectory(jarsDir);
                File[] bundles = listJarsInDirectory(bundlesDir);
                File jarMetaFile = new File(metaDir, JARS_META_FILE);
                File bundleMetaFile = new File(metaDir, BUNDLE_META_FILE);

                boolean isJarDirectoryUpdated = isDirectoryUpdated(jarMetaFile, jarsDir);
                boolean isBundleDirectoryUpdated = isDirectoryUpdated(bundleMetaFile, bundlesDir);
                boolean isReverted = false;

                if (isBundleDirectoryUpdated || isJarDirectoryUpdated) {
                    revertLibDirToDefault();
                    isReverted = true;
                }

                if (jars != null && (isJarDirectoryUpdated || !jarMetaFile.exists() || isReverted)) {
                    backupLibDirectory(outputDir);
                    for (File jar : jars) {
                        BundleGeneratorUtils.convertFromJarToBundle(
                                jar.toPath(), outputPath, new Manifest(), "");
                    }
                    updateMetaFile(jarsDir, jarMetaFile);
                }
                if (bundles != null && (isJarDirectoryUpdated || !bundleMetaFile.exists()
                        || isReverted)) {
                    for (File bundle : bundles) {
                        String bundleName = bundle.getName();
                        Matcher matcher = EXTRACT_JAR_NAME_PATTERN.matcher(bundleName);
                        if (matcher.matches()) {
                            String bundleArtifactName = matcher.group(1);
                            if (bundleArtifactName != null) {
                                cleanExistingBundle(outputDir, bundleArtifactName);
                            }
                        }
                        FileUtils.copyFileToDir(bundle, outputDir);
                    }
                    updateMetaFile(bundlesDir, bundleMetaFile);
                }
            } catch (IOException | CarbonToolException e) {
                logger.log(Level.SEVERE,
                        "An error occurred while installing Jars and Bundles to the distribution.", e);
            }
        } else {
            String message = "The jars location:" + jarSourcePath + " and/or bundle location:" + bundleSourcePath +
                    " and/or lib location:" + outputPath + "  does not have appropriate " + "read/write permissions.";
            logger.log(Level.WARNING, message);
        }
    }

    private boolean isDirectoryUpdated(File metaDataFile, File directory) throws IOException {
        if (metaDataFile.exists()) {
            try (BufferedReader bufReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(metaDataFile), Charset.defaultCharset()))) {
                String metaReadInfo = bufReader.readLine();
                if (metaReadInfo != null) {
                    return !metaReadInfo.equalsIgnoreCase(Long.toString(directory.lastModified()));
                }
            }
        }
        return false;
    }

    private void revertLibDirToDefault() throws IOException {
        File libDir = new File(carbonHome, LIB_DIR_NAME);
        File backupLibDir = new File(carbonHome, BUNDLE_BACKUP_DIR_NAME);
        if (libDir.exists() && backupLibDir.exists()) {
            logger.info("Reverting " + libDir.toPath() + " to default version.");
            FileUtils.deleteDir(libDir);
            File[] bundles = listJarsInDirectory(backupLibDir);
            for (File bundle : bundles) {
                FileUtils.copyFileToDir(bundle, libDir);
            }
        }
    }

    private void backupLibDirectory(File bundleDir) throws IOException {
        File bundleBackupDir = new File(carbonHome, BUNDLE_BACKUP_DIR_NAME);
        boolean alreadyBackedUp = bundleBackupDir.exists();
        if (!alreadyBackedUp) {
            File[] jarsInDir = listJarsInDirectory(bundleDir);
            for (File jar : jarsInDir) {
                FileUtils.copyFileToDir(jar, bundleBackupDir);
            }
            logger.info("Backed up lib to " + bundleBackupDir.toPath());
        }
    }

    private void updateMetaFile(File directory, File metaFile) {
        File metaDir = new File(carbonHome, META_DATA_DIR_NAME);
        if (!metaDir.exists()) {
            if (metaDir.mkdir()) {
                logger.info(META_DATA_DIR_NAME + " successfully created on " + metaDir.toPath());
            }
        }
        writeToFile(metaFile, Long.toString(directory.lastModified()));
    }

    private void writeToFile(File file, String content) {
        BufferedWriter bufWriter = null;
        try {
            bufWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(file), Charset.defaultCharset()));
            bufWriter.write(content);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred while writing to file " + file.toPath(), e);
        } finally {
            if (bufWriter != null) {
                try {
                    bufWriter.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private void cleanExistingBundle(File bundleDir, String bundleName) {
        File[] files = bundleDir.listFiles((dir, name) -> name.matches(bundleName + ".*\\.jar"));
        if (files != null) {
            for (final File file : files) {
                if (!file.delete()) {
                    logger.log(Level.SEVERE, "Failed to clean existing bundle, " + file.getAbsolutePath());
                }
            }
        }
    }

    private File[] listJarsInDirectory(File jarsDir) {
        File[] files = {};
        if (jarsDir.exists()) {
            files = jarsDir.listFiles((dir, name) -> name.endsWith(".jar"));
            return files;
        } else {
            logger.warning("Directory " + jarsDir.getPath() + " does not exist.");
        }
        return files;
    }
}
