/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.bridge;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.wso2.carbon.bridge.FrameworkLauncherConstants.BUNDLE_CLASSPATH;
import static org.wso2.carbon.bridge.FrameworkLauncherConstants.BUNDLE_MANIFEST_VERSION;
import static org.wso2.carbon.bridge.FrameworkLauncherConstants.BUNDLE_NAME;
import static org.wso2.carbon.bridge.FrameworkLauncherConstants.BUNDLE_SYMBOLIC_NAME;
import static org.wso2.carbon.bridge.FrameworkLauncherConstants.BUNDLE_VERSION;
import static org.wso2.carbon.bridge.FrameworkLauncherConstants.EXPORT_PACKAGE;
import static org.wso2.carbon.bridge.FrameworkLauncherConstants.MANIFEST_VERSION;

/**
 * Utility methods used by the Carbon Bridge
 */
public class BridgeUtils {    


    private static String JAR_TO_BUNDLE_DIR =
            System.getProperty("java.io.tmpdir").endsWith(File.separator) ?
            System.getProperty("java.io.tmpdir") + "jarsToBundles" :
            System.getProperty("java.io.tmpdir") + File.separator + "jarsToBundles" ;

    static {
        File jarsToBundlesDir = new File(JAR_TO_BUNDLE_DIR);
        if (jarsToBundlesDir.exists()) {
            deleteDir(jarsToBundlesDir);
        }
    }

    /**
     * Create & return the bundle directory
     * @param bundleDir The relative path of directory which contains the jars to be made into bundles
     * @return The bundle directory
     */
    public static File getBundleDirectory(String bundleDir) {
        String carbonHome = System.getProperty("carbon.home");

        if (carbonHome == null) {
            carbonHome = System.getenv("CARBON_HOME");
        }

        if (carbonHome == null || carbonHome.length() == 0) {
            throw new RuntimeException("CARBON_HOME not found");
        }
        File dir = new File(carbonHome, bundleDir);
        if (!dir.exists() && !dir.mkdirs()) {
        }
        return dir;
    }

    /**
     * Create an OSGi bundle out of a JAR file
     *
     * @param jarFile         The jarfile to be bundled
     * @param targetDir       The directory into which the created OSGi bundle needs to be placed into.
     * @param mf              The bundle manifest file
     * @param extensionPrefix Prefix, if any, for the bundle
     * @throws IOException If an error occurs while reading the jar or creating the bundle
     */
    public static void createBundle(File jarFile,
                                    File targetDir,
                                    Manifest mf,
                                    String extensionPrefix) throws IOException {
        if (mf == null) {
            mf = new Manifest();
        }
        String exportedPackages = BridgeUtils.parseJar(jarFile);

        String fileName = jarFile.getName();
        fileName = fileName.replaceAll("-", "_");
        if(fileName.endsWith(".jar")){
            fileName = fileName.substring(0, fileName.length()-4);
        }
        String symbolicName = extensionPrefix + fileName;
        String pluginName = extensionPrefix + fileName + "_1.0.0.jar";
        File extensionBundle = new File(targetDir, pluginName);

        Attributes attribs = mf.getMainAttributes();
        attribs.putValue(MANIFEST_VERSION, "1.0");
        attribs.putValue(BUNDLE_MANIFEST_VERSION, "2");
        attribs.putValue(BUNDLE_NAME, fileName);
        attribs.putValue(BUNDLE_SYMBOLIC_NAME, symbolicName);
        attribs.putValue(BUNDLE_VERSION, "1.0.0");
        attribs.putValue(EXPORT_PACKAGE, exportedPackages);
        attribs.putValue(BUNDLE_CLASSPATH, ".,"+jarFile.getName());
        
        BridgeUtils.createBundle(jarFile, extensionBundle, mf);
    }

    /**
     * Create an OSGi bundle out of a JAR file
     *
     * @param jarFile  The jarfile to be bundled
     * @param bundle   The bundle to be created
     * @param manifest The manifest file
     * @throws IOException If an error occurs while reading the jar or creating the bundle
     */
    public static void createBundle(File jarFile,
                                    File bundle,
                                    Manifest manifest) throws IOException {
        String extractedDirPath = JAR_TO_BUNDLE_DIR + File.separator +
                                  System.currentTimeMillis() + Math.random();
        File extractedDir = new File(extractedDirPath);
        if(!extractedDir.mkdirs()){
            throw new IOException("Fail to create the directory: " + extractedDir.getAbsolutePath());
        }
        FileOutputStream mfos = null;
        FileOutputStream p2InfOs = null;
        try {
            BridgeUtils.copyFileToDir(jarFile, extractedDir);
            String metaInfPath = extractedDirPath + File.separator + "META-INF";
            if (!new File(metaInfPath).mkdirs()) {
                throw new IOException("Failed to create the directory: " + metaInfPath);
            }
            mfos = new FileOutputStream(metaInfPath + File.separator + "MANIFEST.MF");
            manifest.write(mfos);
            
            File p2InfFile = new File(metaInfPath + File.separator + "p2.inf");
            if(!p2InfFile.createNewFile()){
                throw new IOException("Fail to create the file: " + p2InfFile.getAbsolutePath());
            }
            p2InfOs = new FileOutputStream(p2InfFile);
            p2InfOs.write("instructions.configure=markStarted(started:true);".getBytes());
            p2InfOs.flush();
            
            BridgeUtils.archiveDir(bundle.getAbsolutePath(), extractedDirPath);
            BridgeUtils.deleteDir(extractedDir);
            
        } finally {
            try {
                if (mfos != null) {
                    mfos.close();
                }
            } catch (IOException e) {
            }
            
            try {
                if (p2InfOs != null) {
                    p2InfOs.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String aChildren : children) {
                    boolean success = deleteDir(new File(dir, aChildren));
                    if (!success) {
                        return false;
                    }
                }
            }
        }

        // The directory is now empty so delete it
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
        return true;
    }

    /**
     * Archive a directory
     *
     * @param destArchive
     * @param sourceDir
     * @throws java.io.IOException
     */
    public static void archiveDir(String destArchive, String sourceDir) throws IOException {
        File zipDir = new File(sourceDir);
        if (!zipDir.isDirectory()) {
            throw new RuntimeException(sourceDir + " is not a directory");
        }

        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destArchive));
        zipDir(zipDir, zos, sourceDir);
        zos.close();
    }

    protected static void zipDir(File zipDir, ZipOutputStream zos, String archiveSourceDir)
            throws IOException {
        //get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[40960];
        int bytesIn = 0;
        //loop through dirList, and zip the files
        for (String aDirList : dirList) {
            File f = new File(zipDir, aDirList);
            //place the zip entry in the ZipOutputStream object
            zos.putNextEntry(new ZipEntry(getZipEntryPath(f, archiveSourceDir)));
            if (f.isDirectory()) {
                //if the File object is a directory, call this
                //function again to add its content recursively
                zipDir(f, zos, archiveSourceDir);
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
                }
            }
        }
    }

    protected static String getZipEntryPath(File f, String archiveSourceDir) {
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

    /**
     * Copies src file to dst directory.
     * If the dst directory does not exist, it is created
     */
    public static void copyFileToDir(File src, File dst) throws IOException {
        String dstAbsPath = dst.getAbsolutePath();
        String dstDir = dstAbsPath.substring(0, dstAbsPath.lastIndexOf(File.separator));
        File dir = new File(dstDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());
        }

        File file = new File(dstAbsPath + File.separator + src.getName());
        copyFile(src, file);
    }


    /**
     * Copies src file to dst file.
     * If the dst file does not exist, it is created
     */
    public static void copyFile(File src, File dst) throws IOException {
        OutputStream out = null;
        InputStream in = new FileInputStream(src);

        try{
        String dstAbsPath = dst.getAbsolutePath();
        String dstDir = dstAbsPath.substring(0, dstAbsPath.lastIndexOf(File.separator));
        File dir = new File(dstDir);
        if (!dir.exists() && ! dir.mkdirs()) {
            throw new IOException("Fail to create the directory: " + dir.getAbsolutePath());
        }

        out = new FileOutputStream(dst);
        // Transfer bytes from in to out
        byte[] buf = new byte[10240];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        }finally {
            try {
                in.close();
            } catch (IOException e) {
            }

            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static void extract(String archive, String extractDir) throws IOException {
        FileInputStream inputStream = new FileInputStream(archive);
        extractFromStream(inputStream, extractDir);
    }

    public static void extractFromStream(InputStream inputStream, String extractDir)
            throws IOException {
        ZipInputStream zin = null;
        try {
            File unzipped = new File(extractDir);
            // Open the ZIP file
            zin = new ZipInputStream(inputStream);
            if(!unzipped.mkdirs()){
                throw new IOException("Fail to create the directory: " + unzipped.getAbsolutePath());
            }
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                String entryName = entry.getName();
                File f = new File(extractDir + File.separator + entryName);

                if (entryName.endsWith("/") && !f.exists()) { // this is a
                    // directory
                    if(!f.mkdirs()){
                        throw new IOException("Fail to create the directory: " + f.getAbsolutePath());                        
                    }else {
                        continue;
                    }
                }

                // This is a file. Carry out File processing
                int lastIndexOfSlash = entryName.lastIndexOf("/");
                String dirPath = "";
                if (lastIndexOfSlash != -1) {
                    dirPath = entryName.substring(0, lastIndexOfSlash);
                    File dir = new File(extractDir + File.separator + dirPath);
                    if (!dir.exists() && !dir.mkdirs()) {
                        throw new IOException("Failed to create the directory: " + dir.getAbsoluteFile());
                    }
                }

                if (!f.isDirectory()) {
                    OutputStream out = new FileOutputStream(f);
                    byte[] buf = new byte[40960];

                    // Transfer bytes from the ZIP file to the output file
                    int len;
                    while ((len = zin.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                }
            }
        } catch (IOException e) {
            String msg = "Cannot unzip archive. It is probably corrupt";
            throw e;
        } finally {
            try {
                if (zin != null) {
                    zin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param jarFile jar file location
     * @return package name list separated by ","
     * @throws IOException IOException
     */
    public static String parseJar(File jarFile) throws IOException {
        List<String> exportedPackagesList = new ArrayList<String>();
        ZipInputStream zipInputStream = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(jarFile)));
        List<ZipEntry> entries = populateList(zipInputStream);
        zipInputStream.close();

        for (ZipEntry entry : entries) {
            String path = entry.getName();
            if (!path.endsWith("/") && path.endsWith(".class")) {
                //This is package that contains classes. Thus, exportedPackagesList
                int index = path.lastIndexOf("/");
                if (index != -1) {
                    path = path.substring(0, index);
                    path = path.replaceAll("/", ".");
                    if (!exportedPackagesList.contains(path)) {
                        exportedPackagesList.add(path);
                    }
                }
            }
        }

        String[] packageArray =
                exportedPackagesList.toArray(new String[exportedPackagesList.size()]);
        StringBuffer exportedPackages = new StringBuffer();
        for (int i = 0; i < packageArray.length; i++) {
            exportedPackages.append(packageArray[i]);
            if (i != (packageArray.length - 1)) {
                exportedPackages.append(",");
            }
        }
        return exportedPackages.toString();
    }


    /**
     * @param zipInputStream zipInputStream
     * @return return zipetry map
     * @throws IOException IOException
     */
    private static List<ZipEntry> populateList(ZipInputStream zipInputStream) throws IOException {
        List<ZipEntry> listEntry = new ArrayList<ZipEntry>();
        while (zipInputStream.available() == 1) {
            ZipEntry entry = zipInputStream.getNextEntry();
            if (entry == null) {
                break;
            }
            listEntry.add(entry);
        }
        return listEntry;
    }

    public static class JarFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".jar");
        }
    }
}
