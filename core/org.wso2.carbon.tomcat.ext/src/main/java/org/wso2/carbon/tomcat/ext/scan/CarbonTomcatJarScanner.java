package org.wso2.carbon.tomcat.ext.scan;/*
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

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.JarScannerCallback;
import org.apache.tomcat.util.file.Matcher;
import org.apache.tomcat.util.res.StringManager;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.eclipse.osgi.framework.adaptor.BundleClassLoader;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;


/*
* Problem description : According the servlet 3.0 spec, tldScanner classes are picked up during web-app load phase from
* the classPath using SPI mechanism. Normal sequence is to scan;
*  - WEB-INF/lib
*  - parent URL classPath
*  However with the BundleClassLoader being the parent classLoader of Tomcat web-app classLoder, it fails to pick up
*  TLD scanner references reside in plugins directory.
*
*  Contribution of this class : Tomcat allows a standard extension called JarScanner in its context.xml. This
*  implementation extends the StandardJarScanner and add/forcefully-process plugins dir during the
*
*  Configuration : add this implementation class to root Context.xml of the tomcat servlet container.
* */
public class CarbonTomcatJarScanner extends StandardJarScanner{

    private static final Log log = LogFactory.getLog(CarbonTomcatJarScanner.class);
    private static final Set<String> defaultJarsToSkip = new HashSet<String>();
    private static final StringManager sm =
            StringManager.getManager(Constants.Package);
    private static final String CARBON_PLUGINS_DIR_PATH = System.getProperty("carbon.home") +
            "/repository/components/plugins";

    static {
        String jarList = System.getProperty(Constants.SKIP_JARS_PROPERTY);
        if (jarList != null) {
            StringTokenizer tokenizer = new StringTokenizer(jarList, ",");
            while (tokenizer.hasMoreElements()) {
                defaultJarsToSkip.add(tokenizer.nextToken());
            }
        }
    }



    /**
     * Controls the classpath scanning extension.
     */
    private boolean scanClassPath = true;
    public boolean isScanClassPath() {
        return scanClassPath;
    }
    public void setScanClassPath(boolean scanClassPath) {
        this.scanClassPath = scanClassPath;
    }
    /**
     * Controls the testing all files to see of they are JAR files extension.
     */
    private boolean scanAllFiles = false;
    public boolean isScanAllFiles() {
        return scanAllFiles;
    }
    public void setScanAllFiles(boolean scanAllFiles) {
        this.scanAllFiles = scanAllFiles;
    }

    /**
     * Controls the testing all directories to see of they are exploded JAR
     * files extension.
     */
    private boolean scanAllDirectories = false;
    public boolean isScanAllDirectories() {
        return scanAllDirectories;
    }
    public void setScanAllDirectories(boolean scanAllDirectories) {
        this.scanAllDirectories = scanAllDirectories;
    }

    /*
    * Extract the JAR name, if present, from a URL
    */
    private String getJarName(URL url) {

        String name = null;

        String path = url.getPath();
        int end = path.indexOf(Constants.JAR_EXT);
        if (end != -1) {
            int start = path.lastIndexOf('/', end);
            name = path.substring(start + 1, end + 4);
        } else if (isScanAllDirectories()){
            int start = path.lastIndexOf('/');
            name = path.substring(start + 1);
        }

        return name;
    }

   public void scan(ServletContext context, ClassLoader classloader,
                    JarScannerCallback callback, Set<String> jarsToSkip){


       Set<String> ignoredJars;
       if (jarsToSkip == null) {
           ignoredJars = defaultJarsToSkip;
       } else {
           ignoredJars = jarsToSkip;
       }
       Set<String> ignoredJarsTokens = new HashSet<String>();
       for (String pattern: ignoredJars) {
           if (pattern.length() > 0) {
               ignoredJarsTokens.add(pattern);
           }
       }

       // Scan WEB-INF/lib
       Set<String> dirList = context.getResourcePaths(Constants.WEB_INF_LIB);
       if (dirList != null) {
           Iterator<String> it = dirList.iterator();
           while (it.hasNext()) {
               String path = it.next();
               if (path.endsWith(Constants.JAR_EXT) &&
                       !Matcher.matchName(ignoredJarsTokens,
                               path.substring(path.lastIndexOf('/')+1))) {
                   // Need to scan this JAR
                   if (log.isDebugEnabled()) {
                       log.debug(sm.getString("jarScan.webinflibJarScan", path));
                   }
                   URL url = null;
                   try {
                       // File URLs are always faster to work with so use them
                       // if available.
                       String realPath = context.getRealPath(path);
                       if (realPath == null) {
                           url = context.getResource(path);
                       } else {
                           url = (new File(realPath)).toURI().toURL();
                       }
                       process(callback, url);
                   } catch (IOException e) {
                       log.warn(sm.getString("jarScan.webinflibFail", url), e);
                   }
               } else {
                   if (log.isTraceEnabled()) {
                       log.trace(sm.getString("jarScan.webinflibJarNoScan", path));
                   }
               }
           }
       }

       // Scan the classpath
       if (scanClassPath) {
           if (log.isTraceEnabled()) {
               log.trace(sm.getString("jarScan.classloaderStart"));
           }

           ClassLoader loader =
                   Thread.currentThread().getContextClassLoader();

           while (loader != null) {
               if (loader instanceof URLClassLoader) {
                   URL[] urls = ((URLClassLoader) loader).getURLs();
                   for (int i=0; i<urls.length; i++) {
                       // Extract the jarName if there is one to be found
                       String jarName = getJarName(urls[i]);

                       // Skip JARs known not to be interesting and JARs
                       // in WEB-INF/lib we have already scanned
                       if (jarName != null &&
                               !(Matcher.matchName(ignoredJarsTokens, jarName) ||
                                       urls[i].toString().contains(
                                               Constants.WEB_INF_LIB + jarName))) {
                           if (log.isDebugEnabled()) {
                               log.debug(sm.getString("jarScan.classloaderJarScan", urls[i]));
                           }
                           try {
                               process(callback, urls[i]);
                           } catch (IOException ioe) {
                               log.warn(sm.getString(
                                       "jarScan.classloaderFail",urls[i]), ioe);
                           }
                       } else {
                           if (log.isTraceEnabled()) {
                               log.trace(sm.getString("jarScan.classloaderJarNoScan", urls[i]));
                           }
                       }
                   }
               }
               // WSO2 Carbon specific code snippet
               // Setting the plugins directory only if the parent classLoader is a bundleClassLoader.
               if (loader instanceof BundleClassLoader) {
                   File  pluginsDir = new File(CARBON_PLUGINS_DIR_PATH);
                   File[] jarFiles = pluginsDir.listFiles(new FileFilter(){
                       public boolean accept(File file) {
                           if(file.getName().endsWith(Constants.JAR_EXT)) {
                               return true;
                           }
                           return false;
                       }
                   });
                   // processing collected jar files for tldListeners
                   for (File jarFile : jarFiles) {
                       try {
                           process(callback, jarFile.toURI().toURL());
                       } catch (IOException e) {
                           log.warn(sm.getString("jarScan.classloaderFail"),e);
                       }
                   }
               }

               loader = loader.getParent();
           }

       }

   }

    /*
    * Scan a URL for JARs with the optional extensions to look at all files
    * and all directories.
    */
    private void process(JarScannerCallback callback, URL url)
            throws IOException {

        if (log.isTraceEnabled()) {
            log.trace(sm.getString("jarScan.jarUrlStart", url));
        }

        URLConnection conn = url.openConnection();
        if (conn instanceof JarURLConnection) {
            callback.scan((JarURLConnection) conn);
        } else {
            String urlStr = url.toString();
            if (urlStr.startsWith("file:") || urlStr.startsWith("jndi:")) {
                if (urlStr.endsWith(Constants.JAR_EXT)) {
                    URL jarURL = new URL("jar:" + urlStr + "!/");
                    callback.scan((JarURLConnection) jarURL.openConnection());
                } else {
                    File f;
                    try {
                        f = new File(url.toURI());
                        if (f.isFile() && scanAllFiles) {
                            // Treat this file as a JAR
                            URL jarURL = new URL("jar:" + urlStr + "!/");
                            callback.scan((JarURLConnection) jarURL.openConnection());
                        } else if (f.isDirectory() && scanAllDirectories) {
                            File metainf = new File(f.getAbsoluteFile() +
                                    File.separator + "META-INF");
                            if (metainf.isDirectory()) {
                                callback.scan(f);
                            }
                        }
                    } catch (URISyntaxException e) {
                        // Wrap the exception and re-throw
                        IOException ioe = new IOException();
                        ioe.initCause(e);
                        throw ioe;
                    }
                }
            }
        }

    }


}
