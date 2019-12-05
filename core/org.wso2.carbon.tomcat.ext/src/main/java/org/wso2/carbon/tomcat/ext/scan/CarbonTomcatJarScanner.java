/*
 *  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.tomcat.ext.scan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.JarScanType;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.osgi.internal.loader.ModuleClassLoader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;
import javax.servlet.ServletContext;

/*
 * Problem description : According to the servlet 3.0 spec, tldScanner classes are picked up during web-app load phase from
 * the classPath using SPI mechanism. Normal sequence is to scan;
 *  - WEB-INF/lib
 *  - parent URL classPath
 *  However with the ModuleClassLoader being the parent classLoader of Tomcat web-app classLoader,
 *  the StandardJarScanner fails to pick up the TLD scanner references reside in plugins directory.
 *  Because in the StandardJarScanner scan the classes if the classloader is instance of the URLClassLoader.
 *  But the ModuleClassLoader is not an instance of it.
 *
 *  Contribution of this class : Tomcat allows a standard extension called JarScanner in its context.xml.
 *  This implementation extends the StandardJarScanner and forcefully-process plugins dir during the web-app load phase.
 *
 *  Configuration : add this implementation class to root Context.xml of the tomcat servlet container.
 * */
public class CarbonTomcatJarScanner extends StandardJarScanner {

    private static final Log log = LogFactory.getLog(CarbonTomcatJarScanner.class);
    private static final StringManager sm = StringManager.getManager(Constants.Package);
    private static final String CARBON_PLUGINS_DIR_PATH;

    static {
        // Normally we have set this default
        String pluginsPath = System.getProperty("components.repo");
        if (pluginsPath == null) {
            CARBON_PLUGINS_DIR_PATH = Paths.get(System.getProperty("carbon.home"), "repository", "components",
                    "plugins").toString();
        } else {
            CARBON_PLUGINS_DIR_PATH = pluginsPath;
        }
    }

    @Override
    protected void doScanClassPath(JarScanType scanType, ServletContext context,
                                   JarScannerCallback callback, Set<URL> processedURLs) {

        super.doScanClassPath(scanType, context, callback, processedURLs);

        // WSO2 Carbon specific code snippet
        // Setting the plugins directory only if the parent classLoader is a ModuleClassLoader.

        if (log.isTraceEnabled()) {
            log.trace(sm.getString("wso2.jarScan.classloaderStart"));
        }

        ClassLoader stopLoader = null;
        if (!isScanBootstrapClassPath()) {
            // Stop when we reach the bootstrap class loader
            stopLoader = ClassLoader.getSystemClassLoader().getParent();
        }

        ClassLoader classLoader = context.getClassLoader();
        ModuleClassLoader moduleClassloader = null;
        while (classLoader != null && classLoader != stopLoader) {
            // UPDATE: BundleClassLoader has been refactored into ModuleClassLoader in Luna
            if (classLoader instanceof ModuleClassLoader) {
                moduleClassloader = (ModuleClassLoader)classLoader;
                break;
            }
            classLoader = classLoader.getParent();
        }

        if (moduleClassloader == null) {
            return;
        }

        // Use a Deque so URLs can be removed as they are processed
        // and new URLs can be added as they are discovered during
        // processing.
        Deque<URL> classPathUrlsToProcess = new LinkedList<>();
        File pluginsDir = new File(CARBON_PLUGINS_DIR_PATH);
        File[] files = pluginsDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.getName().endsWith(Constants.JAR_EXT)) {
                try {
                    classPathUrlsToProcess.add(files[0].toURI().toURL());
                } catch (MalformedURLException e) {
                    // ignore
                }
            }
        }
        processURLs(scanType, callback, processedURLs, false, classPathUrlsToProcess);
    }
}
