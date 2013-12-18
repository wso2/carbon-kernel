/*
 *  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.deployment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.deployment.spi.Artifact;

public class CarbonDeploymentUtils {
    private static final Log log = LogFactory.getLog(CarbonDeploymentUtils.class);

    /**
     * Checks if a file has been modified by comparing the last update date of
     * both files and Artifact. If they are different, the file is assumed to have
     * been modified.
     *
     * @param artifact artifact to check for modification
     */
    public static boolean isArtifactModified(Artifact artifact) {
        long currentTimeStamp = artifact.getLastModifiedTime();

        setArtifactLastModifiedTime(artifact);

        return (currentTimeStamp != artifact.getLastModifiedTime());
    }

    /**
     * Sets the last modified time to the given artifact
     *
     * @param artifact artifact for update modified time
     */
    public static void setArtifactLastModifiedTime(Artifact artifact) {
        File file = artifact.getFile();
        if (file != null && (artifact.getLastModifiedTime() < file.lastModified())) {
            artifact.setLastModifiedTime(file.lastModified());
        }
    }
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
     * Copies src file to dst file.
     * If the dst file does not exist, it is created
     *
     * @param src The source file
     * @param dst The destiination file
     * @throws java.io.IOException If an Exception occurs while copying
     */
    public static void copyFile(File src, File dst) throws IOException {
        String dstAbsPath = dst.getAbsolutePath();
        String dstDir = dstAbsPath.substring(0, dstAbsPath.lastIndexOf(File.separator));
        File dir = new File(dstDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());
        }

        InputStream in = new FileInputStream(src);
        OutputStream out = null;
        try {
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[10240];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.warn("Unable to close the InputStream " + e.getMessage(), e);
            }

            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                log.warn("Unable to close the OutputStream " + e.getMessage(), e);
            }
        }
    }

    /**
     * Copies src file to dst directory.
     * If the dst directory does not exist, it is created
     * @param src  The file to be copied
     * @param dst  The destination directory to which the file has to be copied
     * @throws java.io.IOException If an error occurs while copying
     */
    public static void copyFileToDir(File src, File dst) throws IOException {
        String dstAbsPath = dst.getAbsolutePath();
        String dstDir = dstAbsPath.substring(0, dstAbsPath.lastIndexOf(File.separator));
        File dir = new File(dstDir);
        if(!dir.exists() && !dir.mkdirs()){
            throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());
        }

        File file = new File(dstAbsPath + File.separator + src.getName());
        copyFile(src, file);
    }
}
