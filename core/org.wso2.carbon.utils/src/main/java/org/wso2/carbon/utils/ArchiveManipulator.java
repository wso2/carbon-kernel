/*                                                                             
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *                                                                             
 * Licensed under the Apache License, Version 2.0 (the "License");             
 * you may not use this file except in compliance with the License.            
 * You may obtain a copy of the License at                                     
 *                                                                             
 *      http://www.apache.org/licenses/LICENSE-2.0                             
 *                                                                             
 * Unless required by applicable law or agreed to in writing, software         
 * distributed under the License is distributed on an "AS IS" BASIS,           
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.    
 * See the License for the specific language governing permissions and         
 * limitations under the License.                                              
 */
package org.wso2.carbon.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 */
public class ArchiveManipulator {

    private static final int BUFFER_SIZE = 40960;

	private static Log log = LogFactory.getLog(ArchiveManipulator.class);

    private String archiveSourceDir;

    /**
     * Archive a directory
     *
     * @param destArchive
     * @param sourceDir
     * @throws IOException
     */
    public void archiveDir(String destArchive, String sourceDir) throws IOException {
        File zipDir = new File(sourceDir);
        if (!zipDir.isDirectory()) {
            throw new RuntimeException(sourceDir + " is not a directory");
        }

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destArchive));
        this.archiveSourceDir = sourceDir;
        
        try {
            zipDir(zipDir, zos);
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                log.warn("Unable to close the ZipOutputStream " + e.getMessage(), e);
            }
        }
    }

    /**
     * Archive a file
     *
     * @param from
     * @param to
     * @throws IOException
     */
    public void archiveFile(String from, String to) throws IOException {
        ZipOutputStream out = null;
        FileInputStream in = new FileInputStream(from);
        try {
            out = new ZipOutputStream(new FileOutputStream(to));
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
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
     * List the contents of an archive
     *
     * @param archive
     * @return List of Zip Entries
     * @throws IOException
     */
    public String[] check(String archive) throws IOException {
        ZipInputStream zin = null;
        InputStream in = null;
        Collection entries = new ArrayList();
        try {
            in = new FileInputStream(archive);
            zin = new ZipInputStream(in);
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                entries.add(entry.getName());
            }
            return (String[]) entries.toArray(new String[entries.size()]);
        } finally {
            try {
                if (zin != null) {
                    zin.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                log.error("Could not close InputStream ", e);
            }
        }
    }

    public void extract(String archive, String extractDir) throws IOException {
        FileInputStream inputStream = new FileInputStream(archive);
        try {
            extractFromStream(inputStream, extractDir);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
            	log.error("Could not close InputStream ", e);
            }
        }
    }

    public void extractFromStream(InputStream inputStream, String extractDir) throws IOException {
        ZipInputStream zin = null;
        OutputStream out = null;

        try {
            File unzipped = new File(extractDir);
            if(!unzipped.exists() && !unzipped.mkdirs()){
                throw new IOException("Fail to create the directory: " + unzipped.getAbsolutePath());
            }

            // Open the ZIP file
            zin = new ZipInputStream(inputStream);
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String entryName = entry.getName();
                File f = new File(extractDir + File.separator + entryName);

                if (entryName.endsWith("/") && !f.exists()) { // this is a
                    // directory
                    if (!f.mkdirs()){
                        throw new IOException("Fail to create the directory: " + f.getAbsolutePath());    
                    }
                    continue;
                }

                // This is a file. Carry out File processing
                int lastIndexOfSlash = entryName.lastIndexOf('/');
                String dirPath = "";
                if (lastIndexOfSlash != -1) {
                    dirPath = entryName.substring(0, lastIndexOfSlash);
                    File dir = new File(extractDir + File.separator + dirPath);
                    if (!dir.exists() && !dir.mkdirs()){
                            throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());      
                    }
                }

                if (!f.isDirectory()) {
                    out = new FileOutputStream(f);
                    byte[] buf = new byte[BUFFER_SIZE];

                    // Transfer bytes from the ZIP file to the output file
                    int len;
                    while ((len = zin.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                }
            }
        } catch (IOException e) {
            String msg = "Cannot unzip archive. It is probably corrupted";
            log.error(msg, e);
            throw e;

        } finally {
            try {
                if (zin != null) {
                    zin.close();
                }
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

    protected void zipDir(File zipDir, ZipOutputStream zos) throws IOException {
        //get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[BUFFER_SIZE];
        int bytesIn = 0;
        //loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; i++) {
            File f = new File(zipDir, dirList[i]);
            //place the zip entry in the ZipOutputStream object
            zos.putNextEntry(new ZipEntry(getZipEntryPath(f)));
            if (f.isDirectory()) {
                //if the File object is a directory, call this
                //function again to add its content recursively
                zipDir(f, zos);
                //loop again
                continue;
            }
            //if we reached here, the File object f was not a directory
            //create a FileInputStream on top of f
            FileInputStream fis = new FileInputStream(f);

            try {
                //now write the content of the file to the ZipOutputStream
                while ((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
            } finally {
                try {
                    //close the Stream
                    fis.close();
                } catch (IOException e) {
                    log.warn("Unable to close the FileInputStream " + e.getMessage(), e);
                }
            }
        }
    }

    protected String getZipEntryPath(File f) {
        String entryPath = f.getPath();
        entryPath = entryPath.substring(archiveSourceDir.length() + 1);
        if (File.separatorChar == '\\') {
            entryPath = entryPath.replace(File.separatorChar, '/');
        }
        if (f.isDirectory()) {
            entryPath += "/";
        }
        return entryPath;
    }
}
