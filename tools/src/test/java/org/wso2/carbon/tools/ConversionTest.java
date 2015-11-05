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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.tools.exceptions.JarToBundleConverterException;
import org.wso2.carbon.tools.utils.BundleGeneratorUtils;

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

    @Test(expectedExceptions = { IOException.class, JarToBundleConverterException.class })
    public void destinationIsAFileUtilsTest() throws IOException, JarToBundleConverterException {
        //  OSGi bundle destination path refers to a file - must refer to a directory
        BundleGeneratorUtils
                .convertFromJarToBundle(TestConstants.SAMPLE_JAR_FILE, SAMPLE_TEXT_FILE, new Manifest(), "");
    }

    @Test
    public void destinationIsAFileExecutorTest() throws IOException {
        //  OSGi bundle destination path refers to a file - must refer to a directory
        TestUtils.createFile(SAMPLE_TEXT_FILE);
        executeConversion(TestConstants.SAMPLE_JAR_FILE, SAMPLE_TEXT_FILE);
        if (Files.exists(SAMPLE_TEXT_FILE)) {
            BundleGeneratorUtils.delete(SAMPLE_TEXT_FILE);
        }
        Path jarFilePath = TestConstants.SAMPLE_JAR_FILE.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(SAMPLE_TEXT_FILE.toString(), bundleName);
            Assert.assertFalse(Files.exists(bundlePath));
        } else {
            assert false;
        }
    }

    @Test
    public void invalidSourceArgumentExecutorTest() {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(null, destination);
        Path jarFilePath = TestConstants.SAMPLE_JAR_FILE.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            Assert.assertFalse(Files.exists(bundlePath));
        } else {
            assert false;
        }
    }

    @Test
    public void invalidDestinationArgumentExecutorTest() {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(TestConstants.SAMPLE_JAR_FILE, null);
        Path jarFilePath = TestConstants.SAMPLE_JAR_FILE.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            Assert.assertFalse(Files.exists(bundlePath));
        } else {
            assert false;
        }
    }

    @Test(expectedExceptions = { IOException.class, JarToBundleConverterException.class })
    public void createBundleWithNoManifestTest() throws IOException, JarToBundleConverterException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        //  no manifest file for the OSGi bundle to be created - invalid argument
        BundleGeneratorUtils.createBundle(TestConstants.SAMPLE_JAR_FILE, destination, null);
    }

    @Test(expectedExceptions = JarToBundleConverterException.class)
    public void createBundleWithRootElement() throws IOException, JarToBundleConverterException {
        Path source = Paths.get(File.separator);
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"), "temp.jar");
        BundleGeneratorUtils.createBundle(source, destination, new Manifest());
    }

    @Test
    public void nonExistentDestinationTest() {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"), TestConstants.CHILD_TEST_DIRECTORY_ONE);
        executeConversion(TestConstants.SAMPLE_JAR_FILE, destination);
        Path jarFilePath = TestConstants.SAMPLE_JAR_FILE.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            Assert.assertFalse(Files.exists(bundlePath));
        } else {
            assert false;
        }
    }

    @Test(expectedExceptions = { IOException.class, JarToBundleConverterException.class })
    public void convertTextFileTest() throws IOException, JarToBundleConverterException {
        //  the source file path refers to a text file
        Path textFilePath = Files.createTempFile(Paths.get(System.getProperty("java.io.tmpdir")), "sample", ".txt");
        textFilePath.toFile().deleteOnExit();
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        BundleGeneratorUtils.convertFromJarToBundle(textFilePath, destination, new Manifest(), "");
    }

    @Test
    public void convertFromJarToBundleWithNoManifestTest() throws IOException, JarToBundleConverterException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
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

    @Test(expectedExceptions = JarToBundleConverterException.class)
    public void convertJarPathWithNoElementsTest() throws IOException, JarToBundleConverterException {
        Path jarFile = Paths.get(File.separator);
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        BundleGeneratorUtils.convertFromJarToBundle(jarFile, destination, null, "");
    }

    @Test
    public void convertJarFileToBundleTest() throws IOException, JarToBundleConverterException {
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
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

    @Test
    public void convertDirectoryContentToBundlesTest() throws IOException, JarToBundleConverterException {
        Path source = Paths.get("target", "test-resources");
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

    @Test
    public void convertToExistingBundleTest() throws IOException, JarToBundleConverterException {
        Path source = Paths.get("target", "test-resources");
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

    private void executeConversion(Path source, Path destination) {
        String[] arguments;
        if ((source != null) && (destination != null)) {
            arguments = new String[2];
            arguments[0] = source.toString();
            arguments[1] = destination.toString();
        } else if ((source == null) && (destination != null)) {
            arguments = new String[2];
            arguments[0] = "";
            arguments[1] = destination.toString();
        } else if ((source != null) && (destination == null)) {
            arguments = new String[2];
            arguments[0] = source.toString();
            arguments[1] = "";
        } else {
            arguments = new String[2];
            arguments[0] = "";
            arguments[1] = "";
        }
        BundleGenerator.execute(arguments);
    }

    private boolean isOSGiBundle(Path bundlePath, String bundleSymbolicName)
            throws IOException, JarToBundleConverterException {
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
