/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class to prevent zip bomb attacks by validating zip archives before extraction.
 * A zip bomb (also known as a decompression bomb) is a malicious archive file designed to crash
 * or render useless the program or system reading it.
 */
public class ZipBombProtectionUtils {

    private static final Log log = LogFactory.getLog(ZipBombProtectionUtils.class);

    private ZipBombProtectionUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Configuration class for zip bomb protection parameters.
     */
    public static class ZipBombConfig {
        private long maxUncompressedSize = CarbonConstants.DEFAULT_MAX_UNCOMPRESSED_SIZE;
        private int maxEntries = CarbonConstants.DEFAULT_MAX_ENTRIES;
        private long maxEntrySize = CarbonConstants.DEFAULT_MAX_ENTRY_SIZE;
        private int maxCompressionRatio = CarbonConstants.DEFAULT_MAX_COMPRESSION_RATIO;
        private int maxDepth = CarbonConstants.DEFAULT_MAX_DEPTH;
        public long getMaxUncompressedSize() {
            return maxUncompressedSize;
        }

        public void setMaxUncompressedSize(long maxUncompressedSize) {
            this.maxUncompressedSize = maxUncompressedSize;
        }

        public int getMaxEntries() {
            return maxEntries;
        }

        public void setMaxEntries(int maxEntries) {
            this.maxEntries = maxEntries;
        }

        public long getMaxEntrySize() {
            return maxEntrySize;
        }

        public void setMaxEntrySize(long maxEntrySize) {
            this.maxEntrySize = maxEntrySize;
        }

        public int getMaxCompressionRatio() {
            return maxCompressionRatio;
        }

        public void setMaxCompressionRatio(int maxCompressionRatio) {
            this.maxCompressionRatio = maxCompressionRatio;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        public void setMaxDepth(int maxDepth) {
            this.maxDepth = maxDepth;
        }
    }

    /**
     * Exception thrown when a potential zip bomb is detected.
     */
    public static class ZipBombException extends IOException {
        public ZipBombException(String message) {
            super(message);
        }
    }

    /**
     * Validates a zip archive against zip bomb attacks using default configuration.
     *
     * @param inputStream The input stream of the zip archive to validate
     * @throws ZipBombException if a potential zip bomb is detected
     * @throws IOException if an I/O error occurs
     */
    public static void validateZipArchive(InputStream inputStream) throws IOException {
        validateZipArchive(inputStream, new ZipBombConfig());
    }

    /**
     * Validates a zip archive against zip bomb attacks using custom configuration.
     *
     * @param inputStream The input stream of the zip archive to validate
     * @param config Custom configuration for zip bomb protection
     * @throws ZipBombException if a potential zip bomb is detected
     * @throws IOException if an I/O error occurs
     */
    public static void validateZipArchive(InputStream inputStream, ZipBombConfig config) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null");
        }
        if (config == null) {
            throw new IllegalArgumentException("Configuration cannot be null");
        }

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            validateZipInputStream(zipInputStream, config);
        }
    }

    /**
     * Validates a ZipInputStream against zip bomb attacks.
     *
     * @param zipInputStream The ZipInputStream to validate
     * @param config Configuration for zip bomb protection
     * @throws ZipBombException if a potential zip bomb is detected
     * @throws IOException if an I/O error occurs
     */
    private static void validateZipInputStream(ZipInputStream zipInputStream, ZipBombConfig config)
            throws IOException {
        int entryCount = 0;
        long totalUncompressedSize = 0;
        ZipEntry entry;
        byte[] buffer = new byte[8192];

        while ((entry = zipInputStream.getNextEntry()) != null) {
            entryCount++;

            // Check maximum number of entries
            if (entryCount > config.getMaxEntries()) {
                String message = String.format("Zip archive contains too many entries (%d). Maximum allowed: %d",
                        entryCount, config.getMaxEntries());
                log.warn(message);
                throw new ZipBombException(message);
            }

            String entryName = entry.getName();
            long compressedSize = entry.getCompressedSize();
            long uncompressedSize = entry.getSize();

            // Skip directory entries
            if (entry.isDirectory()) {
                continue;
            }

            // Validate individual entry size if known
            if (uncompressedSize > 0 && uncompressedSize > config.getMaxEntrySize()) {
                String message = String.format(
                        "Entry '%s' uncompressed size (%d bytes) exceeds maximum allowed size (%d bytes)",
                        entryName, uncompressedSize, config.getMaxEntrySize());
                log.warn(message);
                throw new ZipBombException(message);
            }

            // Check compression ratio if both sizes are known
            if (compressedSize > 0 && uncompressedSize > 0) {
                long compressionRatio = uncompressedSize / compressedSize;
                if (compressionRatio > config.getMaxCompressionRatio()) {
                    String message = String.format(
                            "Entry '%s' has suspicious compression ratio (%d:1). Maximum allowed: %d:1",
                            entryName, compressionRatio, config.getMaxCompressionRatio());
                    log.warn(message);
                    throw new ZipBombException(message);
                }
            }

            // Read and count actual uncompressed bytes
            long actualUncompressedSize = 0;
            int bytesRead;
            while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                actualUncompressedSize += bytesRead;
                totalUncompressedSize += bytesRead;

                // Check individual entry size during reading
                if (actualUncompressedSize > config.getMaxEntrySize()) {
                    String message = String.format(
                            "Entry '%s' actual uncompressed size exceeds maximum allowed size (%d bytes)",
                            entryName, config.getMaxEntrySize());
                    log.warn(message);
                    throw new ZipBombException(message);
                }

                // Check total uncompressed size
                if (totalUncompressedSize > config.getMaxUncompressedSize()) {
                    String message = String.format(
                            "Total uncompressed size (%d bytes) exceeds maximum allowed size (%d bytes)",
                            totalUncompressedSize, config.getMaxUncompressedSize());
                    log.warn(message);
                    throw new ZipBombException(message);
                }
            }

            // Verify actual size against declared size
            if (uncompressedSize >= 0 && actualUncompressedSize != uncompressedSize) {
                log.debug(String.format("Entry '%s' declared size (%d) differs from actual size (%d)",
                        entryName, uncompressedSize, actualUncompressedSize));
            }

            zipInputStream.closeEntry();
        }

        if (log.isDebugEnabled()) {
            log.debug(String.format("Zip validation completed: %d entries, %d total uncompressed bytes",
                    entryCount, totalUncompressedSize));
        }
    }

    /**
     * Validates a single zip entry before extraction.
     *
     * @param entry The zip entry to validate
     * @param targetDir The target directory for extraction
     * @param entryName The name of the entry
     * @throws ZipBombException if the entry is suspicious
     * @throws IOException if an I/O error occurs
     */
    public static void validateZipEntry(ZipEntry entry, String targetDir, String entryName) throws IOException {
        if (entry == null) {
            throw new IllegalArgumentException("Zip entry cannot be null");
        }
        if (targetDir == null || targetDir.trim().isEmpty()) {
            throw new IllegalArgumentException("Target directory cannot be null or empty");
        }

        // Check for path traversal attacks
        if (entryName.contains("..")) {
            String message = String.format("Entry '%s' contains path traversal characters", entryName);
            log.warn(message);
            throw new ZipBombException(message);
        }

        // Check for absolute paths
        if (entryName.startsWith("/") || entryName.startsWith("\\")) {
            log.debug(String.format("Entry '%s' has absolute path", entryName));
        }

        // Check for null bytes in filename (potential security issue)
        if (entryName.indexOf(0) != -1) {
            String message = String.format("Entry '%s' contains null bytes in filename", entryName);
            log.warn(message);
            throw new ZipBombException(message);
        }

        // Check for excessively long filenames
        if (entryName.length() > 255) {
            String message = String.format("Entry '%s' has excessively long filename (%d characters)",
                    entryName, entryName.length());
            log.warn(message);
            throw new ZipBombException(message);
        }
    }

    /**
     * Creates a default configuration with standard protection values.
     *
     * @return A new ZipBombConfig with default values
     */
    public static ZipBombConfig createDefaultConfig() {
        return new ZipBombConfig();
    }

    /**
     * Creates a configuration from the ZipBombProtection element of carbon.xml, which is rendered
     * from the {@code [server.zip_bomb_protection]} section of deployment.toml. Any value that is
     * absent or unparsable falls back to the corresponding default in {@link CarbonConstants}.
     *
     * @return A new ZipBombConfig reflecting the server configuration
     */
    public static ZipBombConfig createConfigFromServerConfiguration() {
        ZipBombConfig config = new ZipBombConfig();
        ServerConfiguration serverConfiguration = ServerConfiguration.getInstance();

        Long maxUncompressedSizeInMB =
                readLong(serverConfiguration, CarbonConstants.ZIP_BOMB_MAX_UNCOMPRESSED_SIZE_IN_MB);
        if (maxUncompressedSizeInMB != null) {
            config.setMaxUncompressedSize(maxUncompressedSizeInMB * 1024L * 1024L);
        }

        Long maxEntrySizeInMB = readLong(serverConfiguration, CarbonConstants.ZIP_BOMB_MAX_ENTRY_SIZE_IN_MB);
        if (maxEntrySizeInMB != null) {
            config.setMaxEntrySize(maxEntrySizeInMB * 1024L * 1024L);
        }

        Long maxEntries = readLong(serverConfiguration, CarbonConstants.ZIP_BOMB_MAX_ENTRIES);
        if (maxEntries != null) {
            config.setMaxEntries(maxEntries.intValue());
        }

        Long maxCompressionRatio = readLong(serverConfiguration, CarbonConstants.ZIP_BOMB_MAX_COMPRESSION_RATIO);
        if (maxCompressionRatio != null) {
            config.setMaxCompressionRatio(maxCompressionRatio.intValue());
        }

        Long maxDepth = readLong(serverConfiguration, CarbonConstants.ZIP_BOMB_MAX_DEPTH);
        if (maxDepth != null) {
            config.setMaxDepth(maxDepth.intValue());
        }

        return config;
    }

    /**
     * Reads a positive long from the server configuration, returning null when the property is not
     * set or does not hold a usable value. A bad value is logged and ignored rather than failing
     * server startup, so that protection stays enabled at its default limit.
     */
    private static Long readLong(ServerConfiguration serverConfiguration, String property) {
        String value = serverConfiguration.getFirstProperty(property);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            long parsedValue = Long.parseLong(value.trim());
            if (parsedValue <= 0) {
                log.warn("Ignoring non-positive value '" + value + "' for " + property
                        + ". Falling back to the default limit.");
                return null;
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            log.warn("Ignoring malformed value '" + value + "' for " + property
                    + ". Falling back to the default limit.", e);
            return null;
        }
    }

    /**
     * Creates a configuration with custom maximum uncompressed size.
     *
     * @param maxUncompressedSizeInMB Maximum uncompressed size in megabytes
     * @return A new ZipBombConfig with specified max size
     */
    public static ZipBombConfig createConfigWithMaxSize(long maxUncompressedSizeInMB) {
        ZipBombConfig config = new ZipBombConfig();
        config.setMaxUncompressedSize(maxUncompressedSizeInMB * 1024L * 1024L);
        return config;
    }

    /**
     * Creates a lenient configuration suitable for trusted archives.
     *
     * @return A new ZipBombConfig with relaxed limits
     */
    public static ZipBombConfig createLenientConfig() {
        ZipBombConfig config = new ZipBombConfig();
        config.setMaxUncompressedSize(500L * 1024L * 1024L); // 500 MB
        config.setMaxEntries(50000);
        config.setMaxEntrySize(500L * 1024L * 1024L); // 500 MB
        config.setMaxCompressionRatio(200);
        return config;
    }

    /**
     * Creates a strict configuration suitable for untrusted sources.
     *
     * @return A new ZipBombConfig with strict limits
     */
    public static ZipBombConfig createStrictConfig() {
        ZipBombConfig config = new ZipBombConfig();
        config.setMaxUncompressedSize(50L * 1024L * 1024L); // 50 MB
        config.setMaxEntries(1000);
        config.setMaxEntrySize(50L * 1024L * 1024L); // 50 MB
        config.setMaxCompressionRatio(50);
        return config;
    }
}