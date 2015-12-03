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
package org.wso2.carbon.kernel.utils;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Test class for FileUtils class.
 *
 * @since 5.0.0
 */
public class FileUtilsTest {

    private static File testDir = Paths.get("target", "FileUtilTest").toFile();
    private static File testSampleDirStructure = Paths.get("target", "FileUtilTest", "testSampleDirStructure").toFile();
    private static File sampleTextFile = Paths.get("src", "test", "resources", "sample.txt").toFile();

    @BeforeTest
    public void setup() {
        createDummyFolderStructure();
    }

    private static void createDummyFolderStructure() {
        int subFolderCount = 5;
        for (int i = 0; i < subFolderCount; i++) {
            File tempFile = new File(testSampleDirStructure, "tempFolder" + i);
            tempFile.mkdirs();
        }

    }

    @Test
    public void testCopyFile() {
        File destination = Paths.get("target", "FileUtilTest", "tempFolder", "sample.txt").toFile();
        try {
            FileUtils.copyFile(sampleTextFile, destination);
            Assert.assertTrue(destination.exists());
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    @Test(expectedExceptions = IOException.class)
    public void testCopyFileWithNonExistingSource() throws IOException {
        File destination = Paths.get("target", "FileUtilTest", "tempFolder", "sample.txt").toFile();
        FileUtils.copyFile(new File("non-existing-file"), destination);
    }

    @Test(dependsOnMethods = {"testCopyFile"})
    public void testCopyFileToDir() {
        File destination = Paths.get("target", "FileUtilTest", "testSampleDirStructure", "sample.txt").toFile();
        try {
            FileUtils.copyFileToDir(sampleTextFile, testSampleDirStructure);
            Assert.assertTrue(destination.exists());
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

//    @Test(dependsOnMethods = {"testCopyFileToDir"})
//    public void testArchiveDir() {
//        try {
//            File zipFile = Paths.get(testDir.toString(), "archive.zip").toFile();
//            FileUtils.archiveDir(zipFile.getAbsolutePath(), testSampleDirStructure.getAbsolutePath());
//            Assert.assertTrue(zipFile.exists());
//        } catch (IOException e) {
//            Assert.assertTrue(false);
//        }
//    }
//
//    @Test(dependsOnMethods = {"testArchiveDir"})
//    public void testFailArchiveDir() throws IOException {
//        File destination = Paths.get("target", "FileUtilTest", "testSampleDirStructure", "sample.txt").toFile();
//        try {
//            File zipFile = Paths.get(testDir.toString(), "archive.zip").toFile();
//            FileUtils.archiveDir(zipFile.getAbsolutePath(), destination.getAbsolutePath());
//            Assert.assertTrue(false);
//        } catch (RuntimeException e) {
//            Assert.assertEquals(e.getMessage(), (destination.getAbsolutePath() + " is not a directory"));
//        }
//    }

    @Test(dependsOnMethods = {"testCopyFileToDir"})
    public void testUDeleteDir() {
        Assert.assertTrue(FileUtils.deleteDir(testDir));
    }
}
