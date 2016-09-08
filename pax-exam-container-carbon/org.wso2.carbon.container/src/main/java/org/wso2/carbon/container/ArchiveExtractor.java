/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.container;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.ops4j.pax.exam.TestContainerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * Extract zip or tar.gz archives to a target Directory.
 */
public class ArchiveExtractor {

    private static final Logger logger = LoggerFactory.getLogger(ArchiveExtractor.class);

    private ArchiveExtractor() {
    }

    /**
     * Extract zip or tar.gz specified by maven url to a target Directory.
     *
     * @param sourceURL       url of the archive to extract
     * @param targetDirectory where to extract to
     * @throws IOException on I/O error
     */
    public static void extract(URL sourceURL, File targetDirectory) throws IOException {
        if (sourceURL.getProtocol().equals("file")) {
            String file = sourceURL.getFile();
            if (file.endsWith(".zip")) {
                extractZipDistribution(sourceURL, targetDirectory);
            } else if (file.endsWith(".tar.gz")) {
                extractTarGzDistribution(sourceURL, targetDirectory);
            } else {
                throw new TestContainerException(
                        "Unknown packaging of distribution; only zip or tar.gz could be handled.");
            }
            return;
        }
        if (sourceURL.toExternalForm().endsWith("/zip")) {
            extractZipDistribution(sourceURL, targetDirectory);
        } else if (sourceURL.toExternalForm().endsWith("/tar.gz")) {
            extractTarGzDistribution(sourceURL, targetDirectory);
        } else {
            throw new TestContainerException(
                    "Unknown packaging; only zip or tar.gz could be handled. URL was " + sourceURL);
        }
    }

    /**
     * Extract zip file in the system to a target Directory.
     *
     * @param path            path of the archive to be extract
     * @param targetDirectory where to extract to
     * @throws IOException on I/O error
     */
    public static void extract(Path path, File targetDirectory) throws IOException {
        if (path.toString().endsWith(".zip")) {
            extract(new ZipArchiveInputStream(new FileInputStream(path.toFile())), targetDirectory);
        } else {
            throw new TestContainerException("Unknown packaging of distribution; only zip can be handled.");
        }
    }

    private static void extractTarGzDistribution(URL sourceDistribution, File targetDirectory) throws IOException {
        File uncompressedFile = File.createTempFile("uncompressedTarGz-", ".tar");
        extractGzArchive(sourceDistribution, uncompressedFile);
        extract(new TarArchiveInputStream(new FileInputStream(uncompressedFile)), targetDirectory);
        FileUtils.forceDelete(uncompressedFile);
    }

    private static void extractZipDistribution(URL sourceDistribution, File targetDirectory) throws IOException {
        extract(new ZipArchiveInputStream(sourceDistribution.openStream()), targetDirectory);
    }

    private static void extractGzArchive(URL sourceDistribution, File tar) throws IOException {
        try (BufferedInputStream in = new BufferedInputStream(sourceDistribution.openStream());
                FileOutputStream out = new FileOutputStream(tar);
                GzipCompressorInputStream gzIn = new GzipCompressorInputStream(in)) {
            final byte[] buffer = new byte[1000];
            int n;
            while (-1 != (n = gzIn.read(buffer))) {
                out.write(buffer, 0, n);
            }
        }
    }

    /**
     * Extract the file and create directories accordingly.
     *
     * @param is        pass the stream, because the stream is different for various file types
     * @param targetDir target directory
     * @throws IOException
     */
    private static void extract(ArchiveInputStream is, File targetDir) throws IOException {
        try {
            if (targetDir.exists()) {
                FileUtils.forceDelete(targetDir);
            }
            createDirectory(targetDir);
            ArchiveEntry entry = is.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                name = name.substring(name.indexOf("/") + 1);
                File file = new File(targetDir, name);
                if (entry.isDirectory()) {
                    createDirectory(file);
                } else {
                    createDirectory(file.getParentFile());
                    OutputStream os = new FileOutputStream(file);
                    try {
                        IOUtils.copy(is, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }
                }
                entry = is.getNextEntry();
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                logger.warn("Error occurred while closing the stream", e);
            }
        }
    }

    private static void createDirectory(File directory) {
        boolean isCreated = true;
        if (!directory.exists()) {
            isCreated = directory.mkdirs();
        }

        if (!isCreated) {
            throw new TestContainerException("Couldn't create the directory: " + directory.toString());
        }
    }
}
