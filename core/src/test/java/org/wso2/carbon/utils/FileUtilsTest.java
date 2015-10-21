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
package org.wso2.carbon.utils;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class FileUtilsTest {

    private static File testDir = new File("target" + File.separator + "FileUtilTest");
    private static File testSampleDirStructure = new File("target" + File.separator + "FileUtilTest" + File.separator +
            "testSampleDirStructure");
    private static File sampleTextFile = new File("src" + File.separator + "test" + File.separator + "resources" +
        File.separator + "sample.txt");

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
        File destination = new File("target" + File.separator + "FileUtilTest" + File.separator +
                "sample.txt");
        try {
            FileUtils.copyFile(sampleTextFile, destination);
            Assert.assertTrue(destination.exists());
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testCopyFileToDir() {
        File destination = new File("target" + File.separator + "FileUtilTest" + File.separator +
                "sample.txt");
        try {
            FileUtils.copyFileToDir(sampleTextFile, testSampleDirStructure);
            Assert.assertTrue(destination.exists());
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testArchiveDir() {
        File destination = new File("target" + File.separator + "FileUtilTest" + File.separator +
                "sample.txt");
        try {
            File zipFile = new File(testDir.toString() + File.separator + "archive.zip");
            FileUtils.archiveDir(zipFile.getAbsolutePath(), testSampleDirStructure.getAbsolutePath());
            Assert.assertTrue(zipFile.exists());
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    @Test(dependsOnMethods = {"testCopyFile"})
    public void testUDeleteDir() {
        Assert.assertTrue(FileUtils.deleteDir(testDir));
    }
}
