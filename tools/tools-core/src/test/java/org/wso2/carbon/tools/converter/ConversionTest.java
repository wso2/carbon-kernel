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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.tools.CarbonTool;
import org.wso2.carbon.tools.CarbonToolExecutor;
import org.wso2.carbon.tools.Constants;
import org.wso2.carbon.tools.TestConstants;
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
 * and BundleGeneratorTool.java.
 *
 * @since 5.0.0
 */
public class ConversionTest {
    private static final Path converterTestResources = Paths.get(TestConstants.TARGET_FOLDER,
            TestConstants.TEST_RESOURCES, TestConstants.CONVERTER_TEST_RESOURCES);
    private static final Path sampleJARFile = Paths.get(converterTestResources.toString(), TestConstants.ARTIFACT_FIVE);
    private static final Path sampleTextFile = Paths.get(TestConstants.TEMP_DIRECTORY, "sample.txt");

    @Test(description = "Tests the conversion process with a text file as the destination", expectedExceptions = {
            CarbonToolException.class})
    public void testConversionWhenDestinationIsAFile() throws IOException, CarbonToolException {
        //  OSGi bundle destination path refers to a file - must refer to a directory
        BundleGeneratorUtils.convertFromJarToBundle(sampleJARFile, sampleTextFile, new Manifest(), "");
    }

    @Test(description = "Tests the conversion process with a text file as the destination by calling the CarbonTool "
            + "execute() method")
    public void testConversionWhenDestinationIsAFileWithExecutor() throws CarbonToolException, IOException {
        //  OSGi bundle destination path refers to a file - must refer to a directory
        TestUtils.createFile(sampleTextFile);
        executeConversion(sampleJARFile, sampleTextFile);

        Path jarFilePath = sampleJARFile.getFileName();
        String jarFileName = getBundleSymbolicName(jarFilePath);
        String bundleName = jarFileName + "_1.0.0.jar";
        Path bundlePath = Paths.get(sampleTextFile.getParent().toString(), bundleName);
        Assert.assertFalse(Files.exists(bundlePath));
    }

    @Test(description = "Tests the conversion with invalid source argument")
    public void testInvalidSourceArgumentWithExecutor() throws CarbonToolException {
        Path destination = Paths.get(TestConstants.TEMP_DIRECTORY);
        executeConversion(null, destination);

        String bundleName = null + "_1.0.0.jar";
        Path bundlePath = Paths.get(destination.toString(), bundleName);
        Assert.assertFalse(Files.exists(bundlePath));
    }

    @Test(description = "Attempts to create an OSGi bundle with no manifest", expectedExceptions = {
            CarbonToolException.class})
    public void testCreatingBundleWithNoManifest() throws IOException, CarbonToolException {
        Path destination = Paths.get(TestConstants.TEMP_DIRECTORY);
        //  no manifest file for the OSGi bundle to be created - invalid argument
        BundleGeneratorUtils.createBundle(sampleJARFile, destination, null);
    }

    @Test(description = "Attempts to create an OSGi bundle from a directory",
            expectedExceptions = CarbonToolException.class)
    public void testCreatingBundleWithRootElement() throws IOException, CarbonToolException {
        Path source = Paths.get(File.separator);
        Path destination = Paths.get(TestConstants.TEMP_DIRECTORY, "temp.jar");
        BundleGeneratorUtils.createBundle(source, destination, new Manifest());
    }

    @Test(description = "Attempts to create an OSGi bundle at a non-existent destination directory")
    public void testCreatingBundleAtNonExistentDestination() throws CarbonToolException {
        Path destination = Paths.get(TestConstants.TARGET_FOLDER, TestConstants.CHILD_TEST_DIRECTORY_ONE);
        executeConversion(sampleJARFile, destination);

        Path jarFilePath = sampleJARFile.getFileName();
        String jarFileName = getBundleSymbolicName(jarFilePath);
        String bundleName = jarFileName + "_1.0.0.jar";
        Path bundlePath = Paths.get(destination.toString(), bundleName);
        Assert.assertFalse(Files.exists(bundlePath));
    }

    @Test(description = "Attempts to convert a text file to an OSGi bundle", expectedExceptions = {
            CarbonToolException.class})
    public void testConvertingTextFile() throws IOException, CarbonToolException {
        //  the source file path refers to a text file
        Path textFilePath = Files.createTempFile(Paths.get(TestConstants.TEMP_DIRECTORY), "sample", ".txt");
        textFilePath.toFile().deleteOnExit();
        Path destination = Paths.get(TestConstants.TEMP_DIRECTORY);
        BundleGeneratorUtils.convertFromJarToBundle(textFilePath, destination, new Manifest(), "");
    }

    @Test(description = "Attempts to convert a JAR to an OSGi bundle with no manifest provided")
    public void testConversionFromJarToBundleWithNoManifest() throws IOException, CarbonToolException {
        Path destination = Paths.get(TestConstants.TARGET_FOLDER);
        BundleGeneratorUtils.convertFromJarToBundle(sampleJARFile, destination, null, "");

        Path jarFilePath = sampleJARFile.getFileName();
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
        Path destination = Paths.get(TestConstants.TEMP_DIRECTORY);
        BundleGeneratorUtils.convertFromJarToBundle(jarFile, destination, null, "");
    }

    @Test(description = "Attempts to convert a JAR file to an OSGi bundle by calling the CarbonTool execute() method")
    public void testConvertingJarFileToBundle() throws IOException, CarbonToolException {
        Path destination = Paths.get(TestConstants.TARGET_FOLDER);
        executeConversion(sampleJARFile, destination);

        Path jarFilePath = sampleJARFile.getFileName();
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

    @Test(description = "Attempts to convert the contents of a source directory that contains an OSGi bundle",
            priority = 2)
    public void testConvertingDirectoryContainingOSGiBundle() throws IOException, CarbonToolException {
        Path destination = Paths.get(TestConstants.TEMP_DIRECTORY);
        System.setProperty(Constants.CARBON_TOOL_SYSTEM_PROPERTY, "jar-to-bundle-converter");
        CarbonToolExecutor.main(new String[] { converterTestResources.toString(), destination.toString() });

        Path jarFilePathOne = sampleJARFile.getFileName();
        Path osgiBundle = Paths.get(converterTestResources.toString(), TestConstants.ARTIFACT_ONE);
        Path jarFilePathTwo = osgiBundle.getFileName();
        if ((jarFilePathOne != null) && (jarFilePathTwo != null)) {
            String jarFileOneName = getBundleSymbolicName(jarFilePathOne);
            String jarFileTwoName = getBundleSymbolicName(jarFilePathTwo);
            String bundleOneName = jarFileOneName + "_1.0.0.jar";
            String bundleTwoName = jarFileTwoName + "_1.0.0.jar";
            Path bundleOnePath = Paths.get(destination.toString(), bundleOneName);
            Path bundleTwoPath = Paths.get(destination.toString(), bundleTwoName);
            assert ((isOSGiBundle(bundleOnePath, jarFileOneName) && (isOSGiBundle(bundleTwoPath, jarFileTwoName))));
            BundleGeneratorUtils.delete(bundleOnePath);
            BundleGeneratorUtils.delete(bundleTwoPath);
        } else {
            assert false;
        }
    }

    @Test(description = "Attempts to convert a JAR to an OSGi bundle to a destination where it has already "
            + "been converted to", priority = 3)
    public void testConvertingExistingBundle() throws IOException, CarbonToolException {
        Files.deleteIfExists(Paths.get(converterTestResources.toString(), TestConstants.ARTIFACT_ONE));
        Path destination = Paths.get(TestConstants.TEMP_DIRECTORY);
        //  executes twice
        CarbonToolExecutor.main(new String[]{
                converterTestResources.toString(), destination.toString()
        });
        CarbonToolExecutor.main(new String[]{
                converterTestResources.toString(), destination.toString()
        });

        Path jarFilePath = sampleJARFile.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            assert isOSGiBundle(bundlePath, jarFileName);
            BundleGeneratorUtils.delete(bundlePath);
            System.clearProperty(Constants.CARBON_TOOL_SYSTEM_PROPERTY);
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
        CarbonTool converter = new BundleGeneratorTool();
        converter.execute(arguments);
    }

    private boolean isOSGiBundle(Path bundlePath, String bundleSymbolicName) throws IOException, CarbonToolException {
        if (Files.exists(bundlePath)) {
            boolean validSymbolicName, exportPackageAttributeCheck, importPackageAttributeCheck;
            try (FileSystem zipFileSystem = BundleGeneratorUtils.createZipFileSystem(bundlePath, false)) {
                Path manifestPath = zipFileSystem.getPath(Constants.JAR_MANIFEST_FOLDER, Constants.MANIFEST_FILE_NAME);
                Manifest manifest = new Manifest(Files.newInputStream(manifestPath));

                Attributes attributes = manifest.getMainAttributes();
                String actualBundleSymbolicName = attributes.getValue(Constants.BUNDLE_SYMBOLIC_NAME);
                validSymbolicName = ((actualBundleSymbolicName != null) && ((bundleSymbolicName != null)
                        && bundleSymbolicName.equals(actualBundleSymbolicName)));
                exportPackageAttributeCheck = attributes.getValue(Constants.EXPORT_PACKAGE) != null;
                importPackageAttributeCheck = attributes.getValue(Constants.DYNAMIC_IMPORT_PACKAGE) != null;
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
