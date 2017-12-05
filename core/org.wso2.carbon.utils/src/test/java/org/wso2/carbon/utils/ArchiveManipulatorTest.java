/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.utils;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

/**
 * Test cases to verify ArchiveManipulator utility functionality.
 */
public class ArchiveManipulatorTest extends BaseTest {
    private Path archiveDirectory = Paths.get(testSampleDirectory.getAbsolutePath(), "testArchiveDirectory");
    private Path extractDirectory = Paths.get(testSampleDirectory.getAbsolutePath(), "testExtractDirectory");

    @BeforeTest(alwaysRun = true)
    public void createDirectories() {
        archiveDirectory.toFile().mkdirs();
        extractDirectory.toFile().mkdirs();
    }

    @Test(groups = "org.wso2.carbon.utils.archive.manipulation",
            description = "Test and verify archive directory functionality")
    public void testArchiveDir() throws Exception {
        String archiveName = "testArchive.zip";
        File targetArchive = Paths.get(archiveDirectory.toString(), archiveName).toFile();
        ArchiveManipulator archiveManipulator = new ArchiveManipulator();
        archiveManipulator.archiveDir(targetArchive.getAbsolutePath(), testDir);
        Assert.assertTrue(targetArchive.exists());
    }

    @Test(groups = "org.wso2.carbon.utils.archive.manipulation", dependsOnMethods = "testArchiveDir",
            description = "Test and verify archive file functionality")
    public void testArchiveFile() throws Exception {
        String originalFile = Paths.get(testDir, "axis2.xml").toString();
        Path sourceFile = Paths.get(testSampleDirectory.toString(), "example-axis2.xml");
        Files.copy(Paths.get(originalFile), sourceFile, StandardCopyOption.REPLACE_EXISTING);
        String archiveFileName = "testAnotherArchive.zip";
        File targetArchive = Paths.get(archiveDirectory.toString(), archiveFileName).toFile();
        ArchiveManipulator archiveManipulator = new ArchiveManipulator();
        archiveManipulator.archiveFile(sourceFile.toString(), targetArchive.getAbsolutePath());
        Assert.assertTrue(targetArchive.exists());
        String[] fileNames = archiveManipulator.check(targetArchive.getAbsolutePath());
        Assert.assertTrue(Arrays.asList(fileNames).contains(sourceFile.toString()));
    }

    @Test(groups = "org.wso2.carbon.utils.archive.manipulation", dependsOnMethods = "testArchiveFile",
            description = "Test and verify extract archive functionality")
    public void testExtractArchive() throws Exception {
        String archiveFileName = "testArchive.zip";
        String archiveFile = Paths.get(archiveDirectory.toString(), archiveFileName).toString();
        ArchiveManipulator archiveManipulator = new ArchiveManipulator();
        archiveManipulator.extract(archiveFile, extractDirectory.toString());
        String[] sourceFileList = new File(testDir).list();
        String[] extractedFileList = extractDirectory.toFile().list();
        assert sourceFileList != null;
        assert extractedFileList != null;
        for (String sourceFile : sourceFileList) {
            Assert.assertTrue(Arrays.asList(extractedFileList).contains(sourceFile));
        }
    }
}
