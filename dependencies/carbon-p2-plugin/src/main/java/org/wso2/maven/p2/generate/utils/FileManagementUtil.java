package org.wso2.maven.p2.generate.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * TODO: class level comment
 */
public class FileManagementUtil {
    private static final int BUFFER = 2048;


    /**
     * @param destination
     * @param profile
     * @return the config.ini file for the Profile
     */
    public static File getProfileConfigIniFile(String destination, String profile) {
        return new File(destination + File.separator + profile + File.separator + "configuration" +
                File.separator + "config.ini");
    }

    public static void changeConfigIniProperty(File configIniFile, String propKey, String value) {
        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream(configIniFile));
            prop.setProperty(propKey, value);
            prop.store(new FileOutputStream(configIniFile), null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void copyDirectory(File srcPath, File dstPath, List filesToBeCopied) throws IOException {
        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdir();
            }
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                copyDirectory(new File(srcPath, files[i]),
                        new File(dstPath, files[i]), filesToBeCopied);
            }
        } else {
            if (!filesToBeCopied.contains(srcPath.getAbsolutePath())) {
                return;
            }
            if (!srcPath.exists()) {
                return;
            } else {
                FileManagementUtil.copy(srcPath, dstPath);
            }
        }
    }

    public static List getAllFilesPresentInFolder(File srcPath) {
        List fileList = new ArrayList();
        if (srcPath.isDirectory()) {
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                fileList.addAll(getAllFilesPresentInFolder(new File(srcPath, files[i])));
            }
        } else {
            fileList.add(srcPath.getAbsolutePath());
        }
        return fileList;
    }

    public static void removeEmptyDirectories(File srcPath) {
        if (srcPath.isDirectory()) {
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                removeEmptyDirectories(new File(srcPath, files[i]));
            }
            if (srcPath.list().length == 0) {
                srcPath.delete();
            }
        }
    }

    public static void zipFolder(String srcFolder, String destZipFile) {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;
        try {
            fileWriter = new FileOutputStream(destZipFile);
            zip = new ZipOutputStream(fileWriter);
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        addFolderContentsToZip(srcFolder, zip);
        try {
            zip.flush();
            zip.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void addToZip(String path, String srcFile, ZipOutputStream zip) {
        File folder = new File(srcFile);
        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            try {
                FileInputStream in = new FileInputStream(srcFile);
                if (path.trim().equals("")) {
                    zip.putNextEntry(new ZipEntry(folder.getName()));
                } else {
                    zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                }
                while ((len = in.read(buf)) > 0) {
                    zip.write(buf, 0, len);
                }
                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static void addFolderContentsToZip(String srcFolder, ZipOutputStream zip) {
        File folder = new File(srcFolder);
        String fileListe[] = folder.list();
        try {
            int i = 0;
            while (true) {
                if (fileListe.length == i) {
                    break;
                }
                if (new File(folder, fileListe[i]).isDirectory()) {
                    zip.putNextEntry(new ZipEntry(fileListe[i] + "/"));
                    zip.closeEntry();
                }
                addToZip("", srcFolder + "/" + fileListe[i], zip);
                i++;
            }
        } catch (Exception ex) {
        }
    }

    private static void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) {
        File folder = new File(srcFolder);
        String fileListe[] = folder.list();
        try {
            int i = 0;
            while (true) {
                if (fileListe.length == i) {
                    break;
                }
                String newPath = folder.getName();
                if (!path.equalsIgnoreCase("")) {
                    newPath = path + "/" + newPath;
                }
                if (new File(folder, fileListe[i]).isDirectory()) {
                    zip.putNextEntry(new ZipEntry(newPath + "/" + fileListe[i] + "/"));
//					zip.closeEntry();
                }
                addToZip(newPath, srcFolder + "/" + fileListe[i], zip);
                i++;
            }
        } catch (Exception ex) {
        }
    }

    public static void copyFile(String src, String dest) {
        try (InputStream is = new FileInputStream(src); FileOutputStream fos = new FileOutputStream(dest)) {
            int c = 0;
            byte[] array = new byte[1024];
            while ((c = is.read(array)) >= 0) {
                fos.write(array, 0, c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File createFileAndParentDirectories(String fileName) throws Exception {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        file.createNewFile();
        return file;
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static void deleteDirectories(File dir) {
        File[] children = dir.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].list() != null && children[i].list().length > 0) {
                deleteDirectories(children[i]);
            } else {
                children[i].delete();
            }
        }
        dir.delete();
    }

    public static void deleteDirectories(String dir) {
        File directory = new File(dir);
        deleteDirectories(directory);
    }

    public static void createTargetFile(String sourceFileName, String targetFileName)
            throws Exception {
        createTargetFile(sourceFileName, targetFileName, false);
    }

    public static void createTargetFile(String sourceFileName, String targetFileName,
                                        boolean overwrite) throws Exception {
        File idealResultFile = new File(targetFileName);
        if (overwrite || !idealResultFile.exists()) {
            FileManagementUtil.createFileAndParentDirectories(targetFileName);
            FileManagementUtil.copyFile(sourceFileName, targetFileName);
        }
    }

    public static boolean createDirectory(String directory) {
        // Create a directory; all ancestor directories must exist
        boolean success = (new File(directory)).mkdir();
        if (!success) {
            // Directory creation failed
        }
        return success;
    }

    public static boolean createDirectorys(String directory) {
        // Create a directory; all ancestor directories must exist
        boolean success = (new File(directory)).mkdirs();
        if (!success) {
            // Directory creation failed
        }
        return success;
    }

    //Copies all files under srcDir to dstDir.
    // If dstDir does not exist, it will be created.
    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }

            String[] children = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(srcDir, children[i]),
                        new File(dstDir, children[i]));
            }
        } else {
            copy(srcDir, dstDir);
        }
    }

    //Copies src file to dst file.
    // If the dst file does not exist, it is created
    public static void copy(File src, File dst) throws IOException {
        if (dst.getParentFile() != null && !dst.getParentFile().exists()) {
            dst.getParentFile().mkdirs();
        }
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String addAnotherNodeToPath(String currentPath, String newNode) {
        return currentPath + File.separator + newNode;
    }

    public static String addNodesToPath(String currentPath, String[] newNode) {
        String returnPath = currentPath;
        for (int i = 0; i < newNode.length; i++) {
            returnPath = returnPath + File.separator + newNode[i];
        }
        return returnPath;
    }

    public static String addNodesToPath(StringBuffer currentPath, String[] pathNodes) {
        for (int i = 0; i < pathNodes.length; i++) {
            currentPath.append(File.separator);
            currentPath.append(pathNodes[i]);
        }
        return currentPath.toString();
    }

    public static String addNodesToURL(String currentPath, String[] newNode) {
        String returnPath = currentPath;
        for (int i = 0; i < newNode.length; i++) {
            returnPath = returnPath + "/" + newNode[i];
        }
        return returnPath;
    }

    /**
     * Get the list of file with a prefix of <code>fileNamePrefix</code> &amp; an extension of
     * <code>extension</code>
     *
     * @param sourceDir      The directory in which to search the files
     * @param fileNamePrefix The prefix to look for
     * @param extension      The extension to look for
     * @return The list of file with a prefix of <code>fileNamePrefix</code> &amp; an extension of
     * <code>extension</code>
     */
    public static File[] getMatchingFiles(String sourceDir, String fileNamePrefix, String extension) {
        List fileList = new ArrayList();
        File libDir = new File(sourceDir);
        String libDirPath = libDir.getAbsolutePath();
        String[] items = libDir.list();
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                if (fileNamePrefix != null && extension != null) {
                    if (item.startsWith(fileNamePrefix) && item.endsWith(extension)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else if (fileNamePrefix == null && extension != null) {
                    if (item.endsWith(extension)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else if (fileNamePrefix != null && extension == null) {
                    if (item.startsWith(fileNamePrefix)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else {
                    fileList.add(new File(libDirPath + File.separator + item));
                }
            }
            return (File[]) fileList.toArray(new File[fileList.size()]);
        }
        return new File[0];
    }

    /**
     * Filter out files inside a <code>sourceDir</code> with matching <codefileNamePrefix></code>
     * and <code>extension</code>
     *
     * @param sourceDir      The directory to filter the files
     * @param fileNamePrefix The filtering filename prefix
     * @param extension      The filtering file extension
     */
    public static void filterOutRestrictedFiles(String sourceDir, String fileNamePrefix, String extension) {
        File[] resultedMatchingFiles = getMatchingFiles(sourceDir, fileNamePrefix, extension);
        for (int i = 0; i < resultedMatchingFiles.length; i++) {
            File matchingFilePath = new File(resultedMatchingFiles[i].getAbsolutePath());
            matchingFilePath.delete();
        }
    }

    public static void unzip(File archiveFile, File destination) throws Exception {
        try {
            BufferedOutputStream dest = null;
            FileInputStream fis = new FileInputStream(archiveFile);
            ZipInputStream zis = new
                    ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            File base = destination;

            while ((entry = zis.getNextEntry()) != null) {
                int count;
                byte data[] = new byte[BUFFER];
                File file = new File(base, entry.getName());
                if (entry.getName().endsWith("/")) {
                    file.mkdirs();
                    continue;
                }
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                dest = new BufferedOutputStream(fos, BUFFER);
                while ((count = zis.read(data, 0, BUFFER))
                        != -1) {
                    dest.write(data, 0, count);
                }
                dest.flush();
                dest.close();
            }
            zis.close();
            fis.close();
        } catch (Exception e) {
            throw e;
        }
    }
}
