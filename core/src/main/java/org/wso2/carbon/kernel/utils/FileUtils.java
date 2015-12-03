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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * File Utilities.
 *
 * @since 5.0.0
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     *
     * @param dir The directory to be deleted
     * @return true if the directory and its descendents were deleted
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        return false;
                    }
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }


    /**
     * Copies source file to destination file.
     * If the dst file does not exist, it is created
     *
     * @param source The source file
     * @param destination The destination file
     * @throws java.io.IOException If an Exception occurs while copying
     */
    public static void copyFile(File source, File destination) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source file does not exist: " + source);
        }
        try {
            Files.createDirectories(destination.toPath());
            Files.copy(source.toPath(), destination.toPath().resolve(source.getName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.warn("Unable to copy file " + e.getMessage(), e);
            throw new IOException("Unable to copy file ", e);
        }
    }

    /**
     * Copies src file to dst directory.
     * If the dst directory does not exist, it is created
     *
     * @param source The file to be copied
     * @param destination The destination directory to which the file has to be copied
     * @throws java.io.IOException If an error occurs while copying
     */
    public static void copyFileToDir(File source, File destination) throws IOException {

        if (!source.exists()) {
            throw new IOException("Source file does not exist: " + source);
        }
        try {
            Files.createDirectories(destination.toPath());
            Files.copy(source.toPath(), destination.toPath().resolve(source.getName()),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.warn("Unable to copy file " + e.getMessage(), e);
            throw new IOException("Unable to copy file ", e);
        }
    }
}
