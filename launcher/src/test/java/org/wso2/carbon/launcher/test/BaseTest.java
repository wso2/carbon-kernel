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
package org.wso2.carbon.launcher.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

/**
 * BaseTest class for launcher tests.
 */
public class BaseTest {
    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);
    /**
     * Basedir for all file I/O. Important when running tests from the reactor.
     * Note that anyone can use this statically.
     */
    public static String basedir;

    static {
        basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = new File(".").getAbsolutePath();
        }
    }

    protected String testDir = "src" + File.separator + "test" + File.separator;
    protected String testResourceDir = testDir + "resources";

    public BaseTest() {
        testDir = new File(basedir, testDir).getAbsolutePath();
        testResourceDir = new File(basedir, testResourceDir).getAbsolutePath();
    }

    public File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }

    public InputStream getTestResource(String relativePath) {
        File testResource = getTestResourceFile(relativePath);
        try {
            return new FileInputStream(testResource);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("The '" + testResource.getAbsolutePath() +
                    "' file does not exist. Verify that the 'basedir' System property " +
                    "is pointing to the root of the project", e);
        }
    }

    public ArrayList<String> getLogsFromTestResource(FileInputStream testResource) {
        ArrayList<String> logRecords = new ArrayList<String>();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(testResource))) {
            String strLine;
            while ((strLine = bufferedReader.readLine()) != null) {
                logRecords.add(strLine);
            }
        } catch (IOException e) {
            logger.error("Could not get logs", e);
        }
        return logRecords;
    }

    protected boolean containsLogRecord(ArrayList<String> logRecords, String record) {
        for (String log : logRecords) {
            if (log.contains(record)) {
                return true;
            }
            continue;
        }
        return false;
    }

    /**
     * Set the carbon home for execute tests.
     * Carbon home is set to target/carbon-home
     */
    public void setupCarbonHome() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");
        System.setProperty("carbon.home", carbonHome.toString());
        try {
            Path launchPropertyFileLocation = Paths.get(testResourceDir, "launch.properties");
            Path osgiConfLocation = Paths.get(carbonHome.toString(), "repository", "conf", "osgi");
            if (!osgiConfLocation.toFile().exists()) {
                Files.createDirectories(osgiConfLocation);
                Files.copy(launchPropertyFileLocation,
                        osgiConfLocation.resolve(launchPropertyFileLocation.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            Path pidFileLocation = Paths.get(testResourceDir, "wso2carbon.pid");
            if (!Paths.get(carbonHome.toString(), "wso2carbon.pid").toFile().exists()) {
                Files.copy(pidFileLocation, carbonHome.resolve(pidFileLocation.getFileName()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            logger.error("Could not setup carbon home", e.getMessage(), e);
        }
    }
}
