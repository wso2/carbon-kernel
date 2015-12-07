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
package org.wso2.carbon.tools.utils;

import org.wso2.carbon.tools.exceptions.JarToBundleConverterException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * A Java class which contains utility methods utilized during the process of
 * converting a JAR file to an OSGi bundle.
 *
 * @since 5.0.0
 */
public class BundleGeneratorUtils {

    private static final Logger logger = Logger.getLogger(BundleGeneratorUtils.class.getName());

    /**
     * Converts a specified non-OSGi JAR file to an OSGi bundle at the specified destination.
     *
     * @param jarFile         the JAR file to be bundled
     * @param targetDirectory the directory into which the created OSGi bundle needs to be placed
     * @param manifest        the OSGi bundle manifest file
     * @param extensionPrefix prefix, if any, for the bundle
     * @throws IOException                   if an I/O error occurs while reading the JAR or generating the bundle
     * @throws JarToBundleConverterException if the {@link Path} representing the JAR file has no elements or if an
     *                                       error occurs when generating the bundle
     */
    public static void convertFromJarToBundle(Path jarFile, Path targetDirectory, Manifest manifest,
            String extensionPrefix) throws IOException, JarToBundleConverterException {
        //  checks for validity of the arguments
        if (!Files.isDirectory(targetDirectory)) {
            String message = "Path target directory does not point to a directory.";
            throw new JarToBundleConverterException(message);
        }

        Path tempJarFilePathHolder = jarFile.getFileName();
        if (tempJarFilePathHolder != null) {
            String fileName = tempJarFilePathHolder.toString();
            if (fileName.endsWith(Constants.JAR_FILE_EXTENSION)) {
                if (BundleGeneratorUtils.isOSGiBundle(jarFile)) {
                    String message = "Path jarFile refers to an OSGi bundle.";
                    throw new JarToBundleConverterException(message);
                }

                if (manifest == null) {
                    manifest = new Manifest();
                }
                String exportedPackages = BundleGeneratorUtils
                        .generateExportPackageList(BundleGeneratorUtils.listPackages(jarFile));
                fileName = fileName.replaceAll("-", "_");
                fileName = fileName.substring(0, fileName.length() - 4);
                String symbolicName = extensionPrefix + fileName;
                String pluginName = extensionPrefix + fileName + "_1.0.0" + Constants.JAR_FILE_EXTENSION;
                Path extensionBundle = Paths.get(targetDirectory.toString(), pluginName);

                logger.log(Level.FINEST, "Setting Manifest attributes.");
                Attributes attributes = manifest.getMainAttributes();
                attributes.putValue(Constants.MANIFEST_VERSION, "1.0");
                attributes.putValue(Constants.BUNDLE_MANIFEST_VERSION, "2");
                attributes.putValue(Constants.BUNDLE_NAME, fileName);
                attributes.putValue(Constants.BUNDLE_SYMBOLIC_NAME, symbolicName);
                attributes.putValue(Constants.BUNDLE_VERSION, "1.0.0");
                attributes.putValue(Constants.EXPORT_PACKAGE, exportedPackages);
                attributes.putValue(Constants.BUNDLE_CLASSPATH, ".," + tempJarFilePathHolder.toString());
                attributes.putValue(Constants.DYNAMIC_IMPORT_PACKAGE, "*");

                if (!(Files.exists(extensionBundle))) {
                    logger.log(Level.FINE, "Creating the OSGi bundle for JAR file " + jarFile.toString());
                    logger.log(Level.FINE, "Creating an OSGi bundle for JAR file " + tempJarFilePathHolder.toString() +
                            ", at target directory " + extensionBundle.toString() + ".");
                    BundleGeneratorUtils.createBundle(jarFile, extensionBundle, manifest);
                    logger.log(Level.FINE, "Created an OSGi bundle for JAR file " + tempJarFilePathHolder.toString() +
                            ", at target directory " + extensionBundle.toString() + ".");
                    logger.log(Level.INFO, "Created the OSGi bundle " + pluginName + " for JAR file " +
                            jarFile.toString());
                } else {
                    logger.log(Level.INFO, "OSGi bundle " + pluginName + " already exists in the target directory.");
                }
            } else {
                String message = "Path jarFile does not point to a JAR file.";
                throw new JarToBundleConverterException(message);
            }
        } else {
            String message = "Path representing the JAR file name has zero elements.";
            throw new JarToBundleConverterException(message);
        }
    }

    /**
     * Returns a comma separated {@code String} value of the concatenated package names from the {@code List<String>}.
     *
     * @param packageNames a {@link List<String>} whose {@link String} package name values are to be concatenated
     * @return a comma separated {@link String} value of the concatenated package names from the {@code List<String>}
     */
    private static String generateExportPackageList(List<String> packageNames) {
        StringBuilder exportedPackages = new StringBuilder();
        if (packageNames != null) {
            IntStream.range(0, packageNames.size()).forEach(packageCount -> {
                exportedPackages.append(packageNames.get(packageCount));
                if (packageCount != (packageNames.size() - 1)) {
                    exportedPackages.append(",");
                }
            });
        }
        return exportedPackages.toString();
    }

    /**
     * Returns true if the {@code jarFilePath} refers to an OSGi bundle, else false.
     *
     * @param jaFilePath the {@link Path} instance to be checked if it is an OSGi bundle
     * @return true if the {@code jarFilePath} refers to an OSGi bundle, else false.
     * @throws IOException                   if an I/O error occurs
     * @throws JarToBundleConverterException if {@link Path} {@code zipFilePath} does not refer to a .jar file or if
     *                                       {@link Path} {@code zipFilePath} has zero elements
     */
    private static boolean isOSGiBundle(Path jaFilePath) throws IOException, JarToBundleConverterException {
        boolean hasSymbolicName, hasVersion;
        try (FileSystem zipFileSystem = BundleGeneratorUtils.createZipFileSystem(jaFilePath, false)) {
            Path manifestPath = zipFileSystem.getPath(Constants.JAR_MANIFEST_FOLDER, Constants.MANIFEST_FILE_NAME);
            Manifest manifest = new Manifest(Files.newInputStream(manifestPath));
            Attributes attributes = manifest.getMainAttributes();
            hasSymbolicName = attributes.getValue(Constants.BUNDLE_SYMBOLIC_NAME) != null;
            hasVersion = attributes.getValue(Constants.BUNDLE_VERSION) != null;
        }
        return (hasSymbolicName && hasVersion);
    }

    /**
     * Creates an OSGi bundle out of a JAR file.
     *
     * @param jarFile    the JAR file to be bundled
     * @param bundlePath the directory into which the created OSGi bundlePath needs to be placed into
     * @param manifest   the OSGi bundlePath manifest file
     * @throws IOException                   if an I/O error occurs while reading the JAR or generating the bundlePath
     * @throws JarToBundleConverterException if JAR file cannot be copied to the temporary directory or if an error
     *                                       occurs when archiving the final bundlePath directory
     */
    public static void createBundle(Path jarFile, Path bundlePath, Manifest manifest)
            throws IOException, JarToBundleConverterException {
        Path tempJarFilePathHolder = jarFile.getFileName();
        if (tempJarFilePathHolder != null) {
            if (manifest != null) {
                Path tempBundleHolder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")),
                        Constants.JAR_TO_BUNDLE_TEMP_DIRECTORY_NAME);
                tempBundleHolder.toFile().deleteOnExit();

                Path manifestFile = Paths.get(tempBundleHolder.toString(), Constants.MANIFEST_FILE_NAME);
                Path p2InfFile = Files.
                        createTempFile(tempBundleHolder, Constants.P2_INF_FILE_NAME, Constants.P2_INF_FILE_EXTENSION);
                p2InfFile.toFile().deleteOnExit();

                try (OutputStream manifestOutputStream = Files.newOutputStream(manifestFile);
                        OutputStream p2InfOutputStream = Files.newOutputStream(p2InfFile);
                        FileSystem zipFileSystem = createZipFileSystem(bundlePath, true)) {
                    manifest.write(manifestOutputStream);
                    logger.log(Level.FINE,
                            "Generated the OSGi bundlePath MANIFEST.MF for the JAR file " + jarFile.toString());
                    p2InfOutputStream.write("instructions.configure=markStarted(started:true);".
                            getBytes(Charset.forName("UTF-8")));
                    p2InfOutputStream.flush();
                    logger.log(Level.FINE,
                            "Generated the OSGi bundlePath p2.inf for the JAR file " + jarFile.toString());

                    Path manifestFolderPath = zipFileSystem.getPath(Constants.JAR_MANIFEST_FOLDER);
                    if (!Files.exists(manifestFolderPath)) {
                        Files.createDirectories(manifestFolderPath);
                    }
                    Path manifestPathInBundle = zipFileSystem.
                            getPath(Constants.JAR_MANIFEST_FOLDER, Constants.MANIFEST_FILE_NAME);
                    Path p2InfPathInBundle = zipFileSystem.getPath(Constants.JAR_MANIFEST_FOLDER,
                            Constants.P2_INF_FILE_NAME + Constants.P2_INF_FILE_EXTENSION);
                    Files.copy(jarFile, zipFileSystem.getPath(tempJarFilePathHolder.toString()));
                    Files.copy(manifestFile, manifestPathInBundle);
                    Files.copy(p2InfFile, p2InfPathInBundle);

                    //  delete temporary manifest file
                    delete(manifestFile);
                }
            } else {
                String message = "Manifest cannot refer to null.";
                throw new JarToBundleConverterException(message);
            }
        } else {
            String message = "Path representing the JAR file name has zero elements.";
            throw new JarToBundleConverterException(message);
        }
    }

    /**
     * Deletes the specified file or the directory and its child content.
     *
     * @param path the {@link Path} to the file or the directory and its child content to be deleted
     * @return true if successfully deleted, else false
     * @throws IOException if an I/O error occurs during the deletion
     */
    public static boolean delete(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            List<Path> children = listFiles(path);
            if (children.size() > 0) {
                children.forEach(aChild -> {
                    try {
                        delete(aChild);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, e.getMessage(), e);
                    }
                });
            }
        }
        logger.log(Level.FINE, "Deleting " + path + ".");
        return Files.deleteIfExists(path);
    }

    /**
     * Returns a {@code List} of file paths of the child elements of the specified directory.
     *
     * @param directory the directory whose child elements are to be returned
     * @return a {@link List} of {@link Path} instances of the child elements of the specified directory
     * @throws IOException if an I/O error occurs
     */
    public static List<Path> listFiles(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(directory)) {
            directoryStream.forEach(files::add);
        }
        return files;
    }

    /**
     * Returns a {@code List} of {@code String} Java package names within the JAR file.
     *
     * @param jarFile the JAR file of which the package name list is to be returned
     * @return a {@link List} of {@link String} Java package names within the JAR file
     * @throws IOException                   if an I/O error occurs
     * @throws JarToBundleConverterException if an error occurs when retrieving child content from the {@code jarFile}
     */
    public static List<String> listPackages(Path jarFile) throws IOException, JarToBundleConverterException {
        List<String> exportedPackagesList = new ArrayList<>();
        List<Path> content = BundleGeneratorUtils.listZipFileContent(jarFile);
        content.forEach(zipChild -> {
            String path = zipChild.toString();
            if (!path.endsWith("/") && path.endsWith(".class")) {
                //  This is package that contains classes. Thus, exportedPackagesList
                int index = path.lastIndexOf('/');
                if (index != -1) {
                    path = path.substring(1, index);
                    path = path.replaceAll("/", ".");
                    if (!exportedPackagesList.contains(path)) {
                        exportedPackagesList.add(path);
                    }
                }
            }
        });

        logger.log(Level.FINE, "Returning a List<String> of packages from the JAR file " + jarFile.toString());
        return exportedPackagesList;
    }

    /**
     * Returns a list of content in the .zip or .jar file corresponding to the {@code zipFilePath Path} instance, in the
     * form of {@code Path} instances.
     *
     * @param zipFilePath the {@link Path} to the .zip or .jar file
     * @return a list of content in the .zip or .jar file in the form of a {@link List} of {@link Path} instances
     * @throws IOException                   if an I/O error occurs
     * @throws JarToBundleConverterException if a non-existent {@link Path} instance or a {@link Path} instance of
     *                                       invalid file format is given or if the {@link Path} representing the zip
     *                                       file name has zero elements
     */
    public static List<Path> listZipFileContent(Path zipFilePath) throws IOException, JarToBundleConverterException {
        List<Path> zipFileContent = new ArrayList<>();
        if (Files.exists(zipFilePath)) {
            Path zipFileName = zipFilePath.getFileName();
            if (zipFileName != null) {
                if ((!Files.isDirectory(zipFilePath)) && (zipFileName.toString().endsWith(Constants.ZIP_FILE_EXTENSION)
                        || zipFileName.toString().endsWith(Constants.JAR_FILE_EXTENSION))) {
                    try (FileSystem zipFileSystem = createZipFileSystem(zipFilePath, false)) {
                        Path root = zipFileSystem.getPath("/");

                        // walk the file tree and add the directories and files to the list
                        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                zipFileContent.add(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs)
                                    throws IOException {
                                zipFileContent.add(directory);
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    }
                } else {
                    String message = "Path zipFilePath may not exist or may not refer to a .zip or .jar file.";
                    throw new JarToBundleConverterException(message);
                }
            }
        } else {
            String message = "Path represented by the zipFilePath does not exist.";
            throw new JarToBundleConverterException(message);
        }
        return zipFileContent;
    }

    /**
     * Creates a new {@code FileSystem} instance for the .zip or .jar file specified.
     *
     * @param zipFilePath the {@link Path} representation of the .zip or .jar file, from which a {@link FileSystem}
     *                    instance is to be created
     * @param create      true if a .zip or .jar file is to be created at the specified {@link Path}, else false
     * @return the new {@code FileSystem} instance for the .zip or .jar file specified
     * @throws IOException                   if an I/O error occurs, when creating the file system
     * @throws JarToBundleConverterException if the specified {@link Path} file format is not .zip or .jar or
     *                                       if the {@link Path} representing the zip file name has zero elements
     */
    public static FileSystem createZipFileSystem(Path zipFilePath, boolean create)
            throws IOException, JarToBundleConverterException {
        Path zipFileName = zipFilePath.getFileName();
        if (zipFileName != null) {
            if ((zipFileName.toString().endsWith(Constants.ZIP_FILE_EXTENSION)) || (zipFileName.toString().
                    endsWith(Constants.JAR_FILE_EXTENSION))) {
                Map<String, String> bundleJarProperties = new HashMap<>();
                if (create) {
                    bundleJarProperties.put(Constants.CREATE_NEW_ZIP_FILE_PROPERTY, "true");
                } else {
                    bundleJarProperties.put(Constants.CREATE_NEW_ZIP_FILE_PROPERTY, "false");
                }
                bundleJarProperties.put(Constants.ENCODING_TYPE_PROPERTY, "UTF-8");
                // converts the filename to a URI
                URI zipFileIURI = URI.create("jar:file:" + zipFilePath.toUri().getPath());
                return FileSystems.newFileSystem(zipFileIURI, bundleJarProperties);
            } else {
                String message = "Path zipFilePath does not refer to a .zip or .jar file.";
                throw new JarToBundleConverterException(message);
            }
        } else {
            String message = "Path representing the zip file name has zero elements.";
            throw new JarToBundleConverterException(message);
        }
    }

}
