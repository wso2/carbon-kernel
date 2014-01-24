/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.integration.core;

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.server.Main;
import org.wso2.carbon.utils.ArchiveManipulator;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A set of utility methods such as starting & stopping a Carbon server.
 */
public class ServerUtils {

    private static Process process;
    private static String carbonHome;
    private static Thread consoleLogPrinter;
    private static String originalUserDir = null;

    private final static String SERVER_STARTUP_MESSAGE = "WSO2 Carbon started in";
    private final static String SERVER_SHUTDOWN_MESSAGE = "Halting JVM";
    private final static long DEFAULT_START_STOP_WAIT_MS = 1000 * 60 * 4;

    public synchronized static void startServerUsingCarbonHome(String carbonHome)
            throws ServerConfigurationException {
        if (process != null) { // An instance of the server is running
            return;
        }
        Process tempProcess;
        try {
            instrumentJarsForEmma(carbonHome);
            System.setProperty(ServerConstants.CARBON_HOME, carbonHome);
            originalUserDir = System.getProperty("user.dir");
            System.setProperty("user.dir", carbonHome);
            System.out.println("Importing Code Coverage Details...");
            ServerUtils.importEmmaCoverage();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Imported Code Coverage Details.");
            String temp;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                tempProcess = Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "bin" + File.separator + "wso2server.bat"},
                        null, new File(carbonHome));
            } else {
                tempProcess = Runtime.getRuntime().exec(new String[]{"sh", "bin/wso2server.sh", "test"},
                        null, new File(carbonHome));
            }
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        System.out.println("Shutting down server...");
                        ServerUtils.shutdown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(tempProcess.getInputStream()));
            long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
            while ((temp = reader.readLine()) != null && System.currentTimeMillis() < time) {
                System.out.println(temp);
                if (temp.contains(SERVER_STARTUP_MESSAGE)) {
                    consoleLogPrinter = new Thread() {
                        public void run() {
                            try {
                                String temp;
                                while ((temp = reader.readLine()) != null) {
                                    System.out.println(temp);
                                }
                            } catch (Exception ignore) {

                            }
                        }
                    };
                    consoleLogPrinter.start();
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to start server", e);
        }
        process = tempProcess;
        // We need the following sleep because sometimes requests from test reach the server
        // before it completely starts.
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ignored) {
        }
        System.out.println("Successfully started Carbon server. Returning ...");
    }

    public synchronized static void startServerUsingCarbonZip(String carbonServerZipFile)
            throws ServerConfigurationException, IOException {
        if (process != null) { // An instance of the server is running
            return;
        }
        String carbonHome = setUpCarbonHome(carbonServerZipFile);
        instrumentJarsForEmma(carbonHome);
        startServerUsingCarbonHome(carbonHome);
    }

    public synchronized static String setUpCarbonHome(String carbonServerZipFile)
            throws IOException {
        if (process != null) { // An instance of the server is running
            return carbonHome;
        }
        int indexOfZip = carbonServerZipFile.lastIndexOf(".zip");
        if (indexOfZip == -1) {
            throw new IllegalArgumentException(carbonServerZipFile + " is not a zip file");
        }
        String fileSeparator = (File.separator.equals("\\")) ? "\\" : "/";
        if(fileSeparator.equals("\\")){
            carbonServerZipFile = carbonServerZipFile.replace("/", "\\");
        }
        String extractedCarbonDir =
                carbonServerZipFile.substring(carbonServerZipFile.lastIndexOf(fileSeparator) + 1,
                        indexOfZip);
        FileManipulator.deleteDir(extractedCarbonDir);
        new ArchiveManipulator().extract(carbonServerZipFile, "carbontmp");
        return carbonHome = new File(".").getAbsolutePath() + File.separator + "carbontmp" +
                File.separator + extractedCarbonDir;
    }

    public synchronized static void exportEmmaCoverage() {
        if (System.getProperty("emma.home") == null) {
            return;
        }
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String emmaOutput = System.getProperty("emma.output");
        File current = new File(carbonHome + File.separator + "coverage.ec");
        File saved = new File(emmaOutput + File.separator + "coverage.ec");
        if (current.exists() && new File(emmaOutput).exists()) {
            try {
                FileUtils.copyFile(current, saved);
            } catch (IOException ignored) {
            }
        }
    }

    public synchronized static void importEmmaCoverage() {
        if (System.getProperty("emma.home") == null) {
            return;
        }
        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String emmaOutput = System.getProperty("emma.output");
        File current = new File(carbonHome + File.separator + "coverage.ec");
        File saved = new File(emmaOutput + File.separator + "coverage.ec");
        if (saved.exists() && new File(carbonHome).exists()) {
            try {
                FileUtils.copyFile(saved, current);
            } catch (IOException ignored) {
            }
        }
    }

    public synchronized static void instrumentJarsForEmma(String carbonHome) throws IOException {
        String workingDir = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", carbonHome);
            String emmaHome = System.getProperty("emma.home");
            if (emmaHome == null) {
                return;
            } else if (!emmaHome.endsWith(File.separator)) {
                emmaHome += File.separator;
            }
            File emmaOutput = new File(System.getProperty("emma.output"));
            if (!emmaOutput.exists()) {
                FileUtils.forceMkdir(emmaOutput);
            }
            String emmaJarName = null;
            for (File file : new File(emmaHome).listFiles()) {
                if (file.getName().startsWith("org.wso2.carbon.integration.core")) {
                    ArchiveManipulator archiveManipulator = new ArchiveManipulator();
                    archiveManipulator.extract(file.getAbsolutePath(), emmaHome);
                } else if (file.getName().startsWith("emma")) {
                    emmaJarName = file.getName();
                }
            }

            if (emmaJarName == null) {
                return;
            }

            String jarList = System.getProperty("jar.list");


            FileUtils.copyFile(new File(emmaHome + emmaJarName),
                    new File(carbonHome + File.separator + "repository" +
                            File.separator + "components" + File.separator + "plugins" +
                            File.separator + "emma.jar"));
            FileUtils.copyFile(new File(emmaHome + emmaJarName),
                    new File(carbonHome + File.separator + "repository" +
                            File.separator + "components" + File.separator + "lib" +
                            File.separator + "emma.jar"));
            FileUtils.copyFile(new File(emmaHome + emmaJarName),
                    new File(carbonHome + File.separator + "lib" +
                            File.separator + "emma.jar"));
            for (File file : new File[]{new File(carbonHome), emmaOutput}) {
                FileUtils.copyFileToDirectory(new File(carbonHome + File.separator + "lib" +
                        File.separator + "emma.jar"), file);
                FileUtils.copyFileToDirectory(new File(emmaHome + "gen_emma_coverage.rb"), file);
                FileUtils.copyFileToDirectory(new File(jarList), file);
            }

            String temp;
            Process process = Runtime.getRuntime().exec(new String[]{"ruby",
                    "gen_emma_coverage.rb", "instrument", System.getenv("JAVA_HOME")}, null,
                    new File(carbonHome));
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            try {
                while ((temp = reader.readLine()) != null) {
                    System.out.println(temp);
                }
            } catch (IOException ignored) {
            }
            FileUtils.copyFileToDirectory(new File(carbonHome + File.separator + "coverage.em"),
                    emmaOutput);
        } finally {
            System.setProperty("user.dir", workingDir);
        }
    }

    public synchronized static void shutdown() throws Exception {
        if (process != null) {
            process.destroy();
            try {
                String temp;
                process.destroy();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                long time = System.currentTimeMillis() + DEFAULT_START_STOP_WAIT_MS;
                while ((temp = reader.readLine()) != null && System.currentTimeMillis() < time) {
                    if (temp.contains(SERVER_SHUTDOWN_MESSAGE)) {
                        break;
                    }
                }

            } catch (IOException ignored) {
            }
            try {
                consoleLogPrinter.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
            consoleLogPrinter = null;
            process = null;
            System.out.println("Saving Code Coverage Details...");
            ServerUtils.exportEmmaCoverage();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            System.out.println("Completed Saving Code Coverage Details.");
            System.clearProperty(ServerConstants.CARBON_HOME);
            System.setProperty("user.dir", originalUserDir);
        }
    }

}
