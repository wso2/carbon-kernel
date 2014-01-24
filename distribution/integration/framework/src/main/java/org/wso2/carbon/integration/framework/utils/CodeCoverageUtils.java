/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.integration.framework.utils;

import com.vladium.emma.Command;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * This util class provides functionality for computing code coverage.
 */
public final class CodeCoverageUtils {

    private static final Log log = LogFactory.getLog(CodeCoverageUtils.class);

    private CodeCoverageUtils() {
    }

    public static void init() {
        String emmaHome = System.getProperty("emma.home");
        if (emmaHome == null) {
            return;
        } else if (!emmaHome.endsWith(File.separator)) {
            emmaHome += File.separator;
        }
        try {
            if (System.getProperty("emma.properties") == null) {
                for (File file : new File(emmaHome).listFiles()) {
                    if (file.getName().startsWith("org.wso2.carbon.integration.framework")) {
                        new ArchiveManipulatorUtil().extractFile(file.getAbsolutePath(), emmaHome);
                    }
                }
                System.setProperty("emma.properties",
                                   new File(emmaHome).getAbsolutePath() + File.separator + "emma.properties");
            }
            if (System.getProperty("emma.report.html.out.file") == null) {
                System.setProperty("emma.report.html.out.file",
                                   new File(emmaHome).getAbsolutePath() + File.separator + "coverage" +
                                   File.separator + "index.html");
            }
            if (System.getProperty("emma.rt.control.port") == null) {
                System.setProperty("emma.rt.control.port", "44444");
            }
        } catch (IOException e) {
            log.error("Cannot initialize Emma", e);
        }
    }

    public static void instrument(String carbonHome) {
        String workingDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", carbonHome);
            String emmaHome = System.getProperty("emma.home");
            if (emmaHome == null) {
                return;
            } else if (!emmaHome.endsWith(File.separator)) {
                emmaHome += File.separator;
            }
            String emmaJarName = null;
            for (File file : new File(emmaHome).listFiles()) {
                String fileName = file.getName();
                if (fileName.startsWith("emma") && fileName.endsWith(".jar")) {
                    emmaJarName = fileName;
                }
            }
            if (emmaJarName == null) {
                return;
            }

            FileUtils.copyFileToDirectory(new File(emmaHome + emmaJarName),
                                          new File(carbonHome + File.separator + "repository" +
                                                   File.separator + "components" + File.separator +
                                                   "lib" + File.separator));

            // Load the file patterns of the bundles to be instrumented
            instrumentSelectedFiles(carbonHome);

        } catch (Exception e) {
            log.error("Cannot instrument jars", e);
        } finally {
            System.setProperty("user.dir", workingDir);
        }
    }

    private static void instrumentSelectedFiles(String carbonHome) throws Exception {
        File instrumentationTxt = System.getProperty("instr.file") != null ?
                                  new File(System.getProperty("instr.file")) :
                                  new File(System.getProperty("basedir") + File.separator + "src" +
                                           File.separator + "test" + File.separator +
                                           "resources" + File.separator + "instrumentation.txt");
        List<String> filePatterns = new ArrayList<String>();
        if (instrumentationTxt.exists()) {
            RandomAccessFile rf = new RandomAccessFile(instrumentationTxt, "r");
            try {
                String line;
                while ((line = rf.readLine()) != null) {
                    filePatterns.add(line);
                }
            } finally {
                rf.close();
            }
        }

        // Instrument the bundles which match the speficied patterns in <code>filePatterns</code>
        File plugins = new File(carbonHome + File.separator + "repository" + File.separator +
                                "components" + File.separator + "plugins");
        int instrumentedFileCount = 0;
        for (File file : plugins.listFiles()) {
            if (file.isFile()) {
                if (filePatterns.isEmpty()) { // If file patterns are not specified, instrument all files
                    instrument(file);
                    instrumentedFileCount++;
                } else {
                    for (String filePattern : filePatterns) {
                        if (file.getName().startsWith(filePattern)) {
                            instrument(file);
                            instrumentedFileCount++;
                        }
                    }
                }
            }
        }
        log.info("Instrumented " + instrumentedFileCount + " files.");
    }

    private static void instrument(File file) throws Exception {
        addEmmaDynamicImportPackage(file.getAbsolutePath());
        doEmmaInstrumentation(file);
        if (log.isDebugEnabled()) {
            log.debug("Instrumented " + file.getAbsolutePath());
        }
    }

    private synchronized static void addEmmaDynamicImportPackage(String jarFilePath)
            throws IOException {
        if (!jarFilePath.endsWith(".jar")) {
            throw new IllegalArgumentException("Jar file should have the extension .jar. " +
                                               jarFilePath + " is invalid");
        }
        JarFile jarFile = new JarFile(jarFilePath);
        Manifest manifest = jarFile.getManifest();
        if (manifest == null) {
            throw new IllegalArgumentException(jarFilePath + " does not contain a MANIFEST.MF file");
        }
        String fileSeparator = (File.separatorChar == '\\') ? "\\" : File.separator;
        String jarFileName = jarFilePath;
        if (jarFilePath.lastIndexOf(fileSeparator) != -1) {
            jarFileName = jarFilePath.substring(jarFilePath.lastIndexOf(fileSeparator) + 1);
        }
        ArchiveManipulator archiveManipulator;
        String tempExtractedDir;
        try {
            archiveManipulator = new ArchiveManipulator();
            tempExtractedDir = System.getProperty("basedir") + File.separator + "target" +
                               File.separator + jarFileName.substring(0, jarFileName.lastIndexOf('.'));
            new ArchiveManipulatorUtil().extractFile(jarFilePath, tempExtractedDir);
        } finally {
            jarFile.close();
        }

        String dynamicImports = manifest.getMainAttributes().getValue("DynamicImport-Package");
        if (dynamicImports != null) {
            manifest.getMainAttributes().putValue("DynamicImport-Package",
                                                  dynamicImports + ",com.vladium.*");
        } else {
            manifest.getMainAttributes().putValue("DynamicImport-Package", "com.vladium.*");
        }
        File newManifest = new File(tempExtractedDir + File.separator + "META-INF" +
                                    File.separator + "MANIFEST.MF");
        FileOutputStream manifestOut = null;
        try {
            manifestOut = new FileOutputStream(newManifest);
            manifest.write(manifestOut);
        } catch (IOException e) {
            log.error("Could not write content to new MANIFEST.MF file", e);
        } finally {
            if (manifestOut != null) {
                manifestOut.close();
            }
        }
        archiveManipulator.archiveDir(jarFilePath, tempExtractedDir);
        FileManipulator.deleteDir(tempExtractedDir);
    }

    private static void doEmmaInstrumentation(File file) {
        String emmaFilters = System.getProperty("filters.file");
        if (emmaFilters == null) {
            emmaFilters = System.getProperty("basedir") + File.separator + "src" +
                          File.separator + "test" + File.separator +
                          "resources" + File.separator + "filters.txt";
        } else {
            if (!new File(emmaFilters).exists()) {
                log.warn("Emma filters file " + emmaFilters + " does not exist");
            }
        }
        File emmaFiltersFile = new File(emmaFilters);

        Command cmd = emmaFiltersFile.exists() ?
                      Command.create("instr", "emmarun",
                                     new String[]{"-m", "overwrite",
                                                  "-ip", file.getAbsolutePath(),
                                                  "-ix", "@" + emmaFiltersFile.getAbsolutePath()}) :
                      Command.create("instr", "emmarun",
                                     new String[]{"-m", "overwrite",
                                                  "-ip", file.getAbsolutePath()});
        cmd.run();
    }

    public static void generateReports() {       //-r html -in coverage.em,coverage.ec
        String emmaHome = System.getProperty("emma.home");
        if (emmaHome == null) {
            return;
        }
        String basedir = System.getProperty("basedir");
        String coverageEm = new File(basedir + File.separator + "" +
                                     "target" + File.separator + "coverage.em").getAbsolutePath();

        // Recursively find all coverage.ec files, and generate the report
        Collection<File> ecFiles = FileUtils.listFiles(new File(basedir), new String[]{"ec"}, true);
        StringBuilder ecFilesString = new StringBuilder();
        for (File ecFile : ecFiles) {
            ecFilesString.append(ecFile.getAbsolutePath()).append(",");
        }
        Command cmd = Command.create("report", "emmarun",
                                     new String[]{"-r", "html", "-in",
                                                  coverageEm + "," + ecFilesString});
        cmd.run();
        log.info("Generated Emma reports");
    }
}
