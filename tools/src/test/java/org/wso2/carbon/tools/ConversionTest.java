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

import org.testng.annotations.Test;
import org.wso2.carbon.tools.converter.BundleGenerator;
import org.wso2.carbon.tools.converter.utils.BundleGeneratorUtils;
import org.wso2.carbon.tools.exception.CarbonToolException;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * A Java class which tests the convertFromJarToBundle and createBundle methods of BundleGeneratorUtils.java class
 * and BundleGenerator.java.
 *
 * @since 5.0.0
 */
public class ConversionTest {
    private static final Path SAMPLE_TEXT_FILE = Paths.get(System.getProperty("java.io.tmpdir"), "sample.txt");

    @Test(description = "Tests the conversion process with a text file as the destination", expectedExceptions = {
            CarbonToolException.class })
    public void testConversionWhenDestinationIsAFile() throws IOException, CarbonToolException {
        //  OSGi bundle destination path refers to a file - must refer to a directory
        BundleGeneratorUtils.
                convertFromJarToBundle(TestConstants.SAMPLE_JAR_FILE, SAMPLE_TEXT_FILE, new Manifest(), "");
    }

    @Test(description = "Tests the conversion process with a text file as the destination by calling the CarbonTool "
            + "execute() method", expectedExceptions = { CarbonToolException.class })
    public void testConversionWhenDestinationIsAFileWithExecutor() throws CarbonToolException, IOException {
        //  OSGi bundle destination path refers to a file - must refer to a directory
        TestUtils.createFile(SAMPLE_TEXT_FILE);
        executeConversion(TestConstants.SAMPLE_JAR_FILE, SAMPLE_TEXT_FILE);
    }

    @Test(description = "Tests the conversion with invalid source argument", expectedExceptions = {
            CarbonToolException.class })
    public void testInvalidSourceArgumentWithExecutor() throws CarbonToolException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(null, destination);
    }

    @Test(description = "Tests the conversion with invalid destination argument", expectedExceptions = {
            CarbonToolException.class })
    public void testInvalidDestinationArgumentWithExecutor() throws CarbonToolException {
        executeConversion(TestConstants.SAMPLE_JAR_FILE, null);
    }

    @Test(description = "Attempts to create an OSGi bundle with no manifest", expectedExceptions = {
            CarbonToolException.class })
    public void testCreatingBundleWithNoManifest() throws IOException, CarbonToolException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        //  no manifest file for the OSGi bundle to be created - invalid argument
        BundleGeneratorUtils.createBundle(TestConstants.SAMPLE_JAR_FILE, destination, null);
    }

    @Test(description = "Attempts to create an OSGi bundle from a directory",
            expectedExceptions = CarbonToolException.class)
    public void testCreatingBundleWithRootElement() throws IOException, CarbonToolException {
        Path source = Paths.get(File.separator);
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"), "temp.jar");
        BundleGeneratorUtils.createBundle(source, destination, new Manifest());
    }

    @Test(description = "Attempts to create an OSGi bundle at a non-existent destination directory",
            expectedExceptions = { CarbonToolException.class })
    public void testCreatingBundleAtNonExistentDestination() throws CarbonToolException {
        Path destination = Paths.get(TestConstants.TARGET_FOLDER, TestConstants.CHILD_TEST_DIRECTORY_ONE);
        executeConversion(TestConstants.SAMPLE_JAR_FILE, destination);
    }

    @Test(description = "Attempts to convert a text file to an OSGi bundle", expectedExceptions = {
            CarbonToolException.class })
    public void testConvertingTextFile() throws IOException, CarbonToolException {
        //  the source file path refers to a text file
        Path textFilePath = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "sample", ".txt");
        textFilePath.toFile().deleteOnExit();
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        BundleGeneratorUtils.convertFromJarToBundle(textFilePath, destination, new Manifest(), "");
    }

    @Test(description = "Attempts to convert a JAR to an OSGi bundle with no manifest provided")
    public void testConversionFromJarToBundleWithNoManifest() throws IOException, CarbonToolException {
        Path destination = Paths.get(TestConstants.TARGET_FOLDER);
        BundleGeneratorUtils.convertFromJarToBundle(TestConstants.SAMPLE_JAR_FILE, destination, null, "");

        Path jarFilePath = TestConstants.SAMPLE_JAR_FILE.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            assert isOSGiBundle(bundlePath, jarFileName);
            BundleGeneratorUtils.delete(bundlePath);
        } else {
            assert false;
        }
    }

    @Test(description = "Attempts to convert a JAR file path with no elements",
            expectedExceptions = CarbonToolException.class)
    public void testConvertingJarPathWithNoElements() throws IOException, CarbonToolException {
        Path jarFile = Paths.get(File.separator);
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        BundleGeneratorUtils.convertFromJarToBundle(jarFile, destination, null, "");
    }

    @Test(description = "Attempts to convert a JAR file to an OSGi bundle by calling the CarbonTool execute() method")
    public void testConvertingJarFileToBundle() throws IOException, CarbonToolException {
        Path destination = Paths.get(TestConstants.TARGET_FOLDER);
        executeConversion(TestConstants.SAMPLE_JAR_FILE, destination);

        Path jarFilePath = TestConstants.SAMPLE_JAR_FILE.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            assert isOSGiBundle(bundlePath, jarFileName);
            BundleGeneratorUtils.delete(bundlePath);
        } else {
            assert false;
        }
    }

    @Test(description = "Attempts to convert the contents of a source directory containing an OSGi bundle",
            expectedExceptions = { CarbonToolException.class }, priority = 1)
    public void testConvertingDirectoryContainingOSGiBundle() throws IOException, CarbonToolException {
        Path source = Paths.get(TestConstants.TARGET_FOLDER, "test-resources");
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(source, destination);
    }

    @Test(description = "Attempts to convert the contents of a source directory that does not contain any OSGi bundles",
            priority = 2)
    public void testConvertingDirectoryNotContainingOSGiBundle() throws IOException, CarbonToolException {
        Path source = Paths.get(TestConstants.TARGET_FOLDER, "test-resources");
        Files.deleteIfExists(Paths.get(source.toString(), "carbon-context-test-artifact.jar"));
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(source, destination);

        Path jarFilePath = TestConstants.SAMPLE_JAR_FILE.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            assert isOSGiBundle(bundlePath, jarFileName);
            BundleGeneratorUtils.delete(bundlePath);
        } else {
            assert false;
        }
    }

    @Test(description = "Attempts to convert a JAR to an OSGi bundle to a destination where it has already " +
            "been converted to", priority = 3)
    public void testConvertingExistingBundle() throws IOException, CarbonToolException {
        Path source = Paths.get(TestConstants.TARGET_FOLDER, "test-resources");
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(source, destination);
        executeConversion(source, destination);

        Path jarFilePath = TestConstants.SAMPLE_JAR_FILE.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            assert isOSGiBundle(bundlePath, jarFileName);
            BundleGeneratorUtils.delete(bundlePath);
        } else {
            assert false;
        }
    }

    private void executeConversion(Path source, Path destination) throws CarbonToolException {
        String[] arguments;
        if ((source != null) && (destination != null)) {
            arguments = new String[2];
            arguments[0] = source.toString();
            arguments[1] = destination.toString();
        } else if ((source == null) && (destination != null)) {
            arguments = new String[2];
            arguments[0] = "";
            arguments[1] = destination.toString();
        } else if (source != null) {
            arguments = new String[2];
            arguments[0] = source.toString();
            arguments[1] = "";
        } else {
            arguments = new String[2];
            arguments[0] = "";
            arguments[1] = "";
        }
        CarbonTool converter = new BundleGenerator();
        converter.execute(arguments);
    }

    private boolean isOSGiBundle(Path bundlePath, String bundleSymbolicName) throws IOException, CarbonToolException {
        if (Files.exists(bundlePath)) {
            boolean validSymbolicName, exportPackageAttributeCheck, importPackageAttributeCheck;
            try (FileSystem zipFileSystem = BundleGeneratorUtils.createZipFileSystem(bundlePath, false)) {
                Path manifestPath = zipFileSystem.getPath("META-INF", "MANIFEST.MF");
                Manifest manifest = new Manifest(Files.newInputStream(manifestPath));

                Attributes attributes = manifest.getMainAttributes();
                String actualBundleSymbolicName = attributes.getValue(TestConstants.BUNDLE_SYMBOLIC_NAME);
                validSymbolicName = ((actualBundleSymbolicName != null) && ((bundleSymbolicName != null)
                        && bundleSymbolicName.equals(actualBundleSymbolicName)));
                exportPackageAttributeCheck = attributes.getValue(TestConstants.EXPORT_PACKAGE) != null;
                importPackageAttributeCheck = attributes.getValue(TestConstants.IMPORT_PACKAGE) != null;
            }
            return (validSymbolicName && exportPackageAttributeCheck && importPackageAttributeCheck);
        } else {
            return false;
        }
    }

    private String getBundleSymbolicName(Path jarFilePath) {
        if (jarFilePath != null) {
            String jarFileName = jarFilePath.toString();
            jarFileName = jarFileName.substring(0, jarFileName.length() - 4);
            jarFileName = jarFileName.replace("-", "_");
            return jarFileName;
        } else {
            return null;
        }
    }
}
