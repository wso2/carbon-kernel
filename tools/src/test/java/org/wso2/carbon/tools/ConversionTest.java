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

    @Test(expectedExceptions = { IOException.class,
            JarToBundleConverterException.class }) public void destinationIsAFileUtilsTest()
            throws IOException, JarToBundleConverterException {
        Path jarFile = getTestJarFilePath();
        //  OSGi bundle destination path refers to a file - must refer to a directory
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"), "sample.txt");
        BundleGeneratorUtils.convertFromJarToBundle(jarFile, destination, new Manifest(), "");
    }

    @Test public void destinationIsAFileExecutionTest() throws IOException {
        Path jarFile = getTestJarFilePath();
        //  OSGi bundle destination path refers to a file - must refer to a directory
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"), "sample.txt");
        TestUtils.createFile(destination);
        executeConversion(jarFile, destination);
        if (Files.exists(destination)) {
            Files.deleteIfExists(destination);
        }
        Path jarFilePath = jarFile.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            Assert.assertFalse(Files.exists(bundlePath));
        } else {
            assert false;
        }
    }

    @Test(expectedExceptions = { IOException.class,
            JarToBundleConverterException.class }) public void equalSourceAndDestinationTest()
            throws IOException, JarToBundleConverterException {
        //  the source file path and destination file path are the same - cannot be the same
        Path source = Paths.get(System.getProperty("java.io.tmpdir"));
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        BundleGeneratorUtils.convertFromJarToBundle(source, destination, new Manifest(), "");
    }

    @Test(expectedExceptions = { IOException.class,
            JarToBundleConverterException.class }) public void convertTextFileTest()
            throws IOException, JarToBundleConverterException {
        //  the source file path refers to a text file
        Path textFilePath = TestUtils.loadResourceFile(ListZipFileContentTest.class,
                Paths.get("test-artifacts", "source", "sample.txt").toString());
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        BundleGeneratorUtils.convertFromJarToBundle(textFilePath, destination, new Manifest(), "");
    }

    @Test(expectedExceptions = { IOException.class,
            JarToBundleConverterException.class }) public void createBundleWithNoManifestTest()
            throws IOException, JarToBundleConverterException {
        Path jarFile = getTestJarFilePath();
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        //  no manifest file for the OSGi bundle to be created - invalid argument
        BundleGeneratorUtils.createBundle(jarFile, destination, null);
    }

    @Test(expectedExceptions = JarToBundleConverterException.class) public void createBundleWithRootElement()
            throws IOException, JarToBundleConverterException {
        Path source = Paths.get(File.separator);
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"), "temp.jar");
        BundleGeneratorUtils.createBundle(source, destination, new Manifest());
    }

    @Test public void nonExistentDestinationTest() {
        Path jarFile = getTestJarFilePath();
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"), TestConstants.CHILD_TEST_DIRECTORY_ONE);
        executeConversion(jarFile, destination);
        Path jarFilePath = jarFile.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            Assert.assertFalse(Files.exists(bundlePath));
        } else {
            assert false;
        }
    }

    @Test public void improperExecutorArgumentsTest() {
        Path jarFile = getTestJarFilePath();
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(null, destination);
        Path jarFilePath = jarFile.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            Assert.assertFalse(Files.exists(bundlePath));
        } else {
            assert false;
        }
    }

    @Test public void convertFromJarFileToOSGiBundleTest() throws IOException, JarToBundleConverterException {
        Path jarFile = getTestJarFilePath();
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(jarFile, destination);

        Path jarFilePath = jarFile.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            assert isOSGiBundle(bundlePath, jarFileName);
            Files.deleteIfExists(bundlePath);
        } else {
            assert false;
        }
    }

    @Test public void convertDirectoryContentToOSGiBundlesTest() throws IOException, JarToBundleConverterException {
        Path source = TestUtils
                .loadResourceFile(ConversionTest.class, Paths.get("test-artifacts", "source").toString());
        Path destination = Paths.get(System.getProperty("java.io.tmpdir"));
        executeConversion(source, destination);

        Path jarFile = getTestJarFilePath();
        Path jarFilePath = jarFile.getFileName();
        if (jarFilePath != null) {
            String jarFileName = getBundleSymbolicName(jarFilePath);
            String bundleName = jarFileName + "_1.0.0.jar";
            Path bundlePath = Paths.get(destination.toString(), bundleName);
            assert isOSGiBundle(bundlePath, jarFileName);
            Files.deleteIfExists(bundlePath);
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
        } else {
            arguments = new String[1];
            arguments[0] = "";
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
                validSymbolicName = ((attributes.getValue(TestConstants.BUNDLE_SYMBOLIC_NAME) != null) && (
                        (bundleSymbolicName != null) && bundleSymbolicName
                                .equals(attributes.getValue(TestConstants.BUNDLE_SYMBOLIC_NAME))));
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

    private Path getTestJarFilePath() {
        Path jarPath = Paths.get("test-artifacts", "source", "tool-test-artifact-5.0.0-SNAPSHOT.jar");
        return TestUtils.loadResourceFile(ListPackagesTest.class, jarPath.toString());
    }

}
