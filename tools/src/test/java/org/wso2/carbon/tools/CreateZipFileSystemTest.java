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

/**
 * A Java class which tests the createZipFileSystem method of BundleGeneratorUtils.java class.
 *
 * @since 5.0.0
 */
public class CreateZipFileSystemTest {

    @Test public void createZipFileSystemFromExistingTest() throws IOException, JarToBundleConverterException {
        Path bundlePath = TestUtils.loadResourceFile(CreateZipFileSystemTest.class,
                Paths.get("test-artifacts", "osgi", "org.wso2.carbon.sample.deployer.mgt-5.0.0-SNAPSHOT.jar")
                        .toString());
        FileSystem fileSystem = BundleGeneratorUtils.createZipFileSystem(bundlePath, false);
        Assert.assertNotNull(fileSystem);
        fileSystem.close();
    }

    @Test public void createZipFileSystemFromNonExistingTest() throws IOException, JarToBundleConverterException {
        Path zipFilePath = Paths.get(System.getProperty("java.io.tmpdir"), "temp.zip");
        BundleGeneratorUtils.createZipFileSystem(zipFilePath, true);
        assert Files.exists(zipFilePath);
        Files.deleteIfExists(zipFilePath);
    }

    @Test(expectedExceptions = { IOException.class,
            JarToBundleConverterException.class }) public void createZipFileSystemFromTextTest()
            throws JarToBundleConverterException, IOException {
        Path textFilePath = TestUtils.loadResourceFile(CreateZipFileSystemTest.class,
                Paths.get("test-artifacts", "source", "sample.txt").toString());
        BundleGeneratorUtils.createZipFileSystem(textFilePath, false);
    }

    @Test(expectedExceptions = JarToBundleConverterException.class) public void createZipFileSystemFromRoot()
            throws IOException, JarToBundleConverterException {
        Path root = Paths.get(File.separator);
        BundleGeneratorUtils.createZipFileSystem(root, false);
    }

}
