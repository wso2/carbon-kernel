/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.tomcat.ext.scan;

import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.scan.Constants;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.servlet.ServletContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * CarbonTomcatJarScannerTest includes test scenarios for CarbonTomcatJarScanner.
 * @since 4.4.19
 */
public class CarbonTomcatJarScannerTest {

    private static final Logger log = Logger.getLogger("CarbonTomcatJarScannerTest");

    /**
     * Checks getters and setters of scanClassPath.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.scan"})
    public void testScanClassPath () {
        CarbonTomcatJarScanner carbonTomcatJarScanner = new CarbonTomcatJarScanner();
        carbonTomcatJarScanner.setScanClassPath(true);
        Assert.assertEquals(carbonTomcatJarScanner.isScanClassPath(), true, "Retrieved value did not " +
                "match with set value for scanClassPath");
        carbonTomcatJarScanner.setScanClassPath(false);
        log.info("Testing getters and setters of scanClassPath");
        Assert.assertEquals(carbonTomcatJarScanner.isScanClassPath(), false, "Retrieved value did not " +
                "match with set value for scanClassPath");
    }

    /**
     * Checks getters and setters of scanAllFiles.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.scan"})
    public void testScanAllFiles () {
        CarbonTomcatJarScanner carbonTomcatJarScanner = new CarbonTomcatJarScanner();
        carbonTomcatJarScanner.setScanAllFiles(true);
        Assert.assertEquals(carbonTomcatJarScanner.isScanAllFiles(), true, "Retrieved value did not " +
                "match with set value for scanAllFiles");
        carbonTomcatJarScanner.setScanAllFiles(false);
        log.info("Testing getters and setters of scanAllFiles");
        Assert.assertEquals(carbonTomcatJarScanner.isScanAllFiles(), false, "Retrieved value did not " +
                "match with set value for scanAllFiles");
    }

    /**
     * Checks getters and setters of scanAllDirectories.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.scan"})
    public void testScanAllDirectories () {
        CarbonTomcatJarScanner carbonTomcatJarScanner = new CarbonTomcatJarScanner();
        carbonTomcatJarScanner.setScanAllDirectories(true);
        Assert.assertEquals(carbonTomcatJarScanner.isScanAllDirectories(), true, "Retrieved value did not " +
                "match with set value for scanAllDirectories");
        carbonTomcatJarScanner.setScanAllDirectories(false);
        log.info("Testing getters and setters of scanAllDirectories");
        Assert.assertEquals(carbonTomcatJarScanner.isScanAllDirectories(), false, "Retrieved value did not " +
                "match with set value for scanAllDirectories");
    }

    /**
     * Checks scan () for its default behavior with Case1.
     * Case 1: Verifying number of successful scans when valid jars to skip in place.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.scan"})
    public void testScanWithCase1 () throws IOException {
        // setting up method inputs
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourcePaths(Constants.WEB_INF_LIB)).thenReturn(null);
        ClassLoader classLoader = mock(ClassLoader.class);
        JarScannerCallback jarScannerCallback = mock(JarScannerCallback.class);
        Set<String> jarsToSkip = new HashSet<>();
        // replace default jars to skip with new jars
        jarsToSkip.add("surefirebooter*.jar");
        jarsToSkip.add("org.jacoco.agent-*-runtime.jar");

        // calling method scan ()
        CarbonTomcatJarScanner carbonTomcatJarScanner = new CarbonTomcatJarScanner();
        carbonTomcatJarScanner.scan(servletContext, classLoader, jarScannerCallback, jarsToSkip);
        log.info("Testing scan () with case 1");
        verify(jarScannerCallback, times(6)).scan((JarURLConnection) any());
    }

    /**
     * Checks scan () for its default behavior with Case2.
     * Case 2: Verifying number of successful scans when no valid jars to skip in place.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.scan"})
    public void testScanWithCase2 () throws IOException {
        // setting up method inputs
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourcePaths(Constants.WEB_INF_LIB)).thenReturn(null);
        ClassLoader classLoader = mock(ClassLoader.class);
        JarScannerCallback jarScannerCallback = mock(JarScannerCallback.class);

        Mockito.doThrow(new IOException()).when(jarScannerCallback).scan(any(JarURLConnection.class));

        // calling method scan ()
        CarbonTomcatJarScanner carbonTomcatJarScanner = new CarbonTomcatJarScanner();
        carbonTomcatJarScanner.scan(servletContext, classLoader, jarScannerCallback, null);
        log.info("Testing scan () with case 2");
        verify(jarScannerCallback, times(8)).scan((JarURLConnection) any());
    }

    /**
     * Checks scan () for its default behavior with Case3.
     * Case 3: Verifying if method logs appropriate warning message when a scan fails with an exception.
     */
    @Test(groups = {"org.wso2.carbon.tomcat.ext.scan"})
    public void testScanWithCase3 () throws IOException {
        // setting up logger handler
        Logger logger = Logger.getLogger(CarbonTomcatJarScanner.class.getName());
        Formatter formatter = new SimpleFormatter();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Handler handler = new StreamHandler(out, formatter);
        logger.addHandler(handler);

        // setting up method inputs
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getResourcePaths(Constants.WEB_INF_LIB)).thenReturn(null);
        ClassLoader classLoader = mock(ClassLoader.class);
        JarScannerCallback jarScannerCallback = mock(JarScannerCallback.class);
        Mockito.doThrow(new IOException()).when(jarScannerCallback).scan(any(JarURLConnection.class));

        try {
            // calling method scan ()
            CarbonTomcatJarScanner carbonTomcatJarScanner = new CarbonTomcatJarScanner();
            carbonTomcatJarScanner.scan(servletContext, classLoader, jarScannerCallback, null);
            // extracting log message if any
            handler.flush();
            String logMsg = out.toString();
            log.info("Testing scan () with case 3");
            Assert.assertNotNull(logMsg);
            Assert.assertTrue(logMsg.contains("WARNING: Failed to scan"), "Actual log message is different " +
                    "from the expected");
        } finally {
            logger.removeHandler(handler);
        }
    }
}
