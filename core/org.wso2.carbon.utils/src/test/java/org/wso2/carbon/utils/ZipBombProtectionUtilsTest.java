/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.BaseTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Test cases to verify ZipBombProtectionUtils functionality.
 */
public class ZipBombProtectionUtilsTest extends BaseTest {

    private Path tempDirectory;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws IOException {
        tempDirectory = Files.createTempDirectory("zipbomb-test");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() throws IOException {
        if (tempDirectory != null && Files.exists(tempDirectory)) {
            Files.walk(tempDirectory)
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation of a normal zip archive")
    public void testValidateNormalZipArchive() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Add a normal file
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            String content = "This is a normal test file content";
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // Add another file
            ZipEntry entry2 = new ZipEntry("test2.txt");
            zos.putNextEntry(entry2);
            zos.write("Another test file".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        // Should not throw exception for normal archive
        ZipBombProtectionUtils.validateZipArchive(bais);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation detects too many entries",
            expectedExceptions = ZipBombProtectionUtils.ZipBombException.class)
    public void testValidateTooManyEntries() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Create more entries than allowed
            for (int i = 0; i < 15000; i++) {
                ZipEntry entry = new ZipEntry("file" + i + ".txt");
                zos.putNextEntry(entry);
                zos.write("content".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ZipBombProtectionUtils.validateZipArchive(bais);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation detects oversized uncompressed content",
            expectedExceptions = ZipBombProtectionUtils.ZipBombException.class)
    public void testValidateOversizedContent() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("huge.txt");
            zos.putNextEntry(entry);

            // Write a large amount of data (exceeds default limit)
            byte[] chunk = new byte[1024 * 1024]; // 1 MB chunks
            for (int i = 0; i < 1100; i++) { // Write 1.1 GB
                zos.write(chunk);
            }
            zos.closeEntry();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ZipBombProtectionUtils.validateZipArchive(bais);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test custom configuration with strict limits",
            expectedExceptions = ZipBombProtectionUtils.ZipBombException.class)
    public void testStrictConfiguration() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("file.txt");
            zos.putNextEntry(entry);

            // Write 60 MB (exceeds strict config limit of 50 MB per entry)
            byte[] chunk = new byte[1024 * 1024]; // 1 MB
            for (int i = 0; i < 60; i++) {
                zos.write(chunk);
            }
            zos.closeEntry();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ZipBombProtectionUtils.ZipBombConfig strictConfig =
                ZipBombProtectionUtils.createStrictConfig();
        ZipBombProtectionUtils.validateZipArchive(bais, strictConfig);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test lenient configuration allows larger files")
    public void testLenientConfiguration() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("file.txt");
            zos.putNextEntry(entry);

            // Write 200 MB (within lenient config limits)
            byte[] chunk = new byte[1024 * 1024]; // 1 MB
            for (int i = 0; i < 200; i++) {
                zos.write(chunk);
            }
            zos.closeEntry();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ZipBombProtectionUtils.ZipBombConfig lenientConfig =
                ZipBombProtectionUtils.createLenientConfig();

        // Should not throw exception with lenient config
        ZipBombProtectionUtils.validateZipArchive(bais, lenientConfig);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation of zip entry with path traversal",
            expectedExceptions = ZipBombProtectionUtils.ZipBombException.class)
    public void testValidateZipEntryWithPathTraversal() throws Exception {
        ZipEntry entry = new ZipEntry("../../etc/passwd");
        ZipBombProtectionUtils.validateZipEntry(entry, "/tmp/extract", entry.getName());
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation of zip entry with null bytes",
            expectedExceptions = ZipBombProtectionUtils.ZipBombException.class)
    public void testValidateZipEntryWithNullBytes() throws Exception {
        ZipEntry entry = new ZipEntry("file\0.txt");
        ZipBombProtectionUtils.validateZipEntry(entry, "/tmp/extract", entry.getName());
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation of zip entry with excessively long filename",
            expectedExceptions = ZipBombProtectionUtils.ZipBombException.class)
    public void testValidateZipEntryWithLongFilename() throws Exception {
        // Create a filename longer than 255 characters
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 260; i++) {
            longName.append("a");
        }
        longName.append(".txt");

        ZipEntry entry = new ZipEntry(longName.toString());
        ZipBombProtectionUtils.validateZipEntry(entry, "/tmp/extract", entry.getName());
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation of valid zip entry")
    public void testValidateValidZipEntry() throws Exception {
        ZipEntry entry = new ZipEntry("folder/file.txt");

        // Should not throw exception for valid entry
        ZipBombProtectionUtils.validateZipEntry(entry, "/tmp/extract", entry.getName());
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test creation of default configuration")
    public void testCreateDefaultConfig() {
        ZipBombProtectionUtils.ZipBombConfig config =
                ZipBombProtectionUtils.createDefaultConfig();

        Assert.assertNotNull(config);
        Assert.assertTrue(config.getMaxUncompressedSize() > 0);
        Assert.assertTrue(config.getMaxEntries() > 0);
        Assert.assertTrue(config.getMaxEntrySize() > 0);
        Assert.assertTrue(config.getMaxCompressionRatio() > 0);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test creation of custom configuration with max size")
    public void testCreateConfigWithMaxSize() {
        long maxSizeInMB = 500;
        ZipBombProtectionUtils.ZipBombConfig config =
                ZipBombProtectionUtils.createConfigWithMaxSize(maxSizeInMB);

        Assert.assertNotNull(config);
        Assert.assertEquals(config.getMaxUncompressedSize(), maxSizeInMB * 1024L * 1024L);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test configuration setters and getters")
    public void testConfigurationSettersGetters() {
        ZipBombProtectionUtils.ZipBombConfig config = new ZipBombProtectionUtils.ZipBombConfig();

        long maxSize = 2048L * 1024L * 1024L;
        config.setMaxUncompressedSize(maxSize);
        Assert.assertEquals(config.getMaxUncompressedSize(), maxSize);

        int maxEntries = 5000;
        config.setMaxEntries(maxEntries);
        Assert.assertEquals(config.getMaxEntries(), maxEntries);

        long maxEntrySize = 256L * 1024L * 1024L;
        config.setMaxEntrySize(maxEntrySize);
        Assert.assertEquals(config.getMaxEntrySize(), maxEntrySize);

        int maxRatio = 150;
        config.setMaxCompressionRatio(maxRatio);
        Assert.assertEquals(config.getMaxCompressionRatio(), maxRatio);

        int maxDepth = 5;
        config.setMaxDepth(maxDepth);
        Assert.assertEquals(config.getMaxDepth(), maxDepth);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation with null input stream",
            expectedExceptions = IllegalArgumentException.class)
    public void testValidateNullInputStream() throws Exception {
        ZipBombProtectionUtils.validateZipArchive(null);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation with null configuration",
            expectedExceptions = IllegalArgumentException.class)
    public void testValidateNullConfig() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("test.txt");
            zos.putNextEntry(entry);
            zos.write("test".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ZipBombProtectionUtils.validateZipArchive(bais, null);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation with directory entries")
    public void testValidateWithDirectoryEntries() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Add directory entry
            ZipEntry dirEntry = new ZipEntry("folder/");
            zos.putNextEntry(dirEntry);
            zos.closeEntry();

            // Add file entry
            ZipEntry fileEntry = new ZipEntry("folder/file.txt");
            zos.putNextEntry(fileEntry);
            zos.write("content".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        // Should not throw exception - directories are skipped
        ZipBombProtectionUtils.validateZipArchive(bais);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validation detects high compression ratio",
            expectedExceptions = ZipBombProtectionUtils.ZipBombException.class)
    public void testValidateHighCompressionRatio() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("highly-compressed.txt");
            zos.putNextEntry(entry);

            // Write highly compressible data (repeated zeros)
            byte[] zeros = new byte[10 * 1024 * 1024]; // 10 MB of zeros
            for (int i = 0; i < 60; i++) { // Write 600 MB of highly compressible data
                zos.write(zeros);
            }
            zos.closeEntry();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

        // Use strict config to detect high compression ratio
        ZipBombProtectionUtils.ZipBombConfig config =
                ZipBombProtectionUtils.createStrictConfig();
        ZipBombProtectionUtils.validateZipArchive(bais, config);
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validateZipEntry with null entry",
            expectedExceptions = IllegalArgumentException.class)
    public void testValidateNullZipEntry() throws Exception {
        ZipBombProtectionUtils.validateZipEntry(null, "/tmp/extract", "test.txt");
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validateZipEntry with null target directory",
            expectedExceptions = IllegalArgumentException.class)
    public void testValidateZipEntryNullTargetDir() throws Exception {
        ZipEntry entry = new ZipEntry("test.txt");
        ZipBombProtectionUtils.validateZipEntry(entry, null, "test.txt");
    }

    @Test(groups = "org.wso2.carbon.utils.zipbomb.protection",
            description = "Test validateZipEntry with empty target directory",
            expectedExceptions = IllegalArgumentException.class)
    public void testValidateZipEntryEmptyTargetDir() throws Exception {
        ZipEntry entry = new ZipEntry("test.txt");
        ZipBombProtectionUtils.validateZipEntry(entry, "", "test.txt");
    }
}