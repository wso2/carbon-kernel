/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.axis2.deployment;

import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.repository.util.WSInfo;
import org.apache.axis2.deployment.repository.util.WSInfoList;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.util.Loader;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

public class RepositoryListener implements DeploymentConstants {
    protected static final Log log = LogFactory.getLog(RepositoryListener.class);

    protected DeploymentEngine deploymentEngine;

    /** Reference to a WSInfoList */
    protected WSInfoList wsInfoList;

    /**
     * This constructor takes two arguments, a folder name and a reference to Deployment Engine
     * First, it initializes the system, by loading all the modules in the /modules directory and
     * then creates a WSInfoList to store information about available modules and services.
     *
     * @param deploymentEngine reference to engine registry for updates
     * @param isClasspath      true if this RepositoryListener should scan the classpath for
     *                         Modules
     */
    public RepositoryListener(DeploymentEngine deploymentEngine, boolean isClasspath) {
        this.deploymentEngine = deploymentEngine;
        wsInfoList = new WSInfoList(deploymentEngine);
        init2(isClasspath);
    }

    public void init2(boolean isClasspath) {
        if (!isClasspath) {
            init();
        }
        loadClassPathModules();
    }

    /** Finds a list of modules in the folder and adds to wsInfoList. */
    public void checkModules() {
        File root = deploymentEngine.getModulesDir();
        File[] files = root.listFiles();

        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (isSourceControlDir(file)) {
                    continue;
                }
                if (!file.isDirectory()) {
                    if (DeploymentFileData.isModuleArchiveFile(file.getName())) {
                        addFileToDeploy(file, deploymentEngine.getModuleDeployer(),
                                        WSInfo.TYPE_MODULE);
                    }
                } else {
                    if (!"lib".equalsIgnoreCase(file.getName())) {
                        addFileToDeploy(file, deploymentEngine.getModuleDeployer(),
                                        WSInfo.TYPE_MODULE);
                    }
                }
            }
        }
    }


    protected boolean isSourceControlDir(File file) {
        if (file.isDirectory()) {
            String name = file.getName();
            if (name.equalsIgnoreCase("CVS") || name.equalsIgnoreCase(".svn")) {
                return true;
            }
        }
        return false;
    }

    protected void loadClassPathModules() {
        ModuleDeployer deployer = deploymentEngine.getModuleDeployer();

        // Find Modules on the class path (i.e. if classpath includes "addressing.mar" then
        // addressing will be available for engaging)

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration moduleURLs = loader.getResources("META-INF/module.xml");
            while (moduleURLs.hasMoreElements()) {
                try {
                    URL url = (URL)moduleURLs.nextElement();
                    URI moduleURI;
                    if (url.getProtocol().equals("file")) {
                        String urlString = url.toString();
                        moduleURI = new URI(urlString.substring(0,
                                urlString.lastIndexOf("/META-INF/module.xml")));
                    } else {
                        // Check if the URL refers to an archive (such as
                        // jar:file:/dir/some.jar!/META-INF/module.xml) and extract the
                        // URL of the archive. In general the protocol will be "jar", but
                        // some containers may use other protocols, e.g. WebSphere uses
                        // "wsjar" (AXIS2-4258).
                        String path = url.getPath();
                        int idx = path.lastIndexOf("!/");
                        if (idx != -1 && path.substring(idx+2).equals("META-INF/module.xml")) {
                            moduleURI = new URI(path.substring(0, idx).replaceAll(" ", "%20"));
                            if (!moduleURI.getScheme().equals("file")) {
                                continue;
                            }
                        } else {
                            continue;
                        }
                    }
    
                    log.debug("Deploying module from classpath at '" + moduleURI + "'");
                    File f = new File(moduleURI);
                    addFileToDeploy(f, deployer, WSInfo.TYPE_MODULE);

                } catch (URISyntaxException e) {
                    log.info(e);
                }
            }
        } catch (Exception e) {
            // Oh well, log the problem
            log.error("Error occurred while loading modules from classpath", e);
        }

        String classPath = getLocation();

        if (classPath == null) return;

        int lstindex = classPath.lastIndexOf(File.separatorChar);
        if (lstindex > 0) {
            classPath = classPath.substring(0, lstindex);
        } else {
            classPath = ".";
        }
        File root = new File(classPath);
        File[] files = root.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (!file.isDirectory()) {
                    if (DeploymentFileData.isModuleArchiveFile(file.getName())) {
                        //adding modules in the class path
                        addFileToDeploy(file, deployer, WSInfo.TYPE_MODULE);
                    }
                }
            }
        }

        ClassLoader cl = deploymentEngine.getAxisConfig().getModuleClassLoader();
        while (cl != null) {
            if (cl instanceof URLClassLoader) {
                URL[] urls = ((URLClassLoader)cl).getURLs();
                for (int i = 0; (urls != null) && i < urls.length; i++) {
                    String path = urls[i].getPath();
                    //If it is a drive letter, adjust accordingly.
                    if (path.length() >= 3 && path.charAt(0) == '/' && path.charAt(2) == ':') {
                        path = path.substring(1);
                    }
                    try {
                        path = URLDecoder.decode(path, Utils.defaultEncoding);
                    } catch (UnsupportedEncodingException e) {
                        // Log this?
                    }
                    File file = new File(path.replace('/', File.separatorChar).replace('|', ':'));
                    // If there is a security manager, then it is highly probable that it will deny
                    // read access to some files in the class loader hierarchy. Therefore we first
                    // check if the name matches that of a module archive and only then check if we
                    // can access it. If the security manager denies access, we log a warning.
                    if (DeploymentFileData.isModuleArchiveFile(file.getName())) {
                        boolean isFile;
                        try {
                            isFile = file.isFile();
                        } catch (SecurityException ex) {
                            log.warn("Not deploying " + file.getName() +
                                    " because security manager denies access", ex);
                            isFile = false;
                        }
                        if (isFile) {
                            //adding modules in the class path
                            addFileToDeploy(file, deployer, WSInfo.TYPE_MODULE);
                        }
                    }
                }
            }
            cl = cl.getParent();
        }

        deploymentEngine.doDeploy();
    }

    /**
     * To get the location of the Axis2.jar from that I can drive the location of class path
     *
     * @return String (location of the axis2 jar)
     */
    protected String getLocation() {
        try {
            Class clazz = Loader.loadClass("org.apache.axis2.engine.AxisEngine");
            java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
            String location = url.toString();
            if (location.startsWith("jar")) {
                url = ((java.net.JarURLConnection)url.openConnection()).getJarFileURL();
                location = url.toString();
            }
            if (location.startsWith("file")) {
                File file = Utils.toFile(url);
                return file.getAbsolutePath();
            } else {
                return url.toString();
            }
        } catch (Throwable t) {
            return null;
        }
    }

    /** Finds a list of services in the folder and adds to wsInfoList. */
    public void checkServices() {
        findServicesInDirectory(deploymentEngine.getServicesDir());
        loadOtherDirectories();
        update();
    }

    /**
     * First initializes the WSInfoList, then calls checkModule to load all the modules and calls
     * update() to update the Deployment engine and engine registry.
     */
    public void init() {
        wsInfoList.init();
        checkModules();
        deploymentEngine.doDeploy();
    }

    //This will load the files from the directories
    // specified by axis2.xml (As <deployer>)
    private void loadOtherDirectories() {
        for (Map.Entry<String, Map<String, Deployer>> entry : deploymentEngine.getDeployers().entrySet()) {
            String directory = entry.getKey();
            Map<String, Deployer> extensionMap = entry.getValue();
            for (String extension : extensionMap.keySet()) {
                File dirToSearch = new File(directory);
                if (!dirToSearch.isAbsolute()) {
                    dirToSearch = new File(deploymentEngine.getRepositoryDir(), directory);
                }
                findFileForGivenDirectory(dirToSearch, extension, directory);
            }
        }
    }

    /**
     * Recursively finds files with the provided extension and adds them to be deployed
     * @param directory - directory to search
     * @param extension - extension to look for
     * @param dir - dir given in the axis2.xml this is used to find the correct deployer
     */
    private void findFileForGivenDirectory(File directory, String extension, String dir) {
        try {
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null && files.length > 0) {
                    for (int i = 0; i < files.length; i++) {
                        File file = files[i];
                        if (isSourceControlDir(file)) {
                            continue;
                        }
                        if (extension == null && file.isDirectory()) {
                            Deployer deployer = deploymentEngine.getDeployer(dir, extension);
                            deployer.setDirectory(dir);
                            addFileToDeploy(file, deployer, WSInfo.TYPE_CUSTOM);
                        } else if (extension != null) {
                            if (!file.isDirectory() && extension
                                    .equals(DeploymentFileData.getFileExtension(file.getName()))) {
                                Deployer deployer = deploymentEngine.getDeployer(dir, extension);
                                deployer.setDirectory(dir);
                                addFileToDeploy(file, deployer, WSInfo.TYPE_CUSTOM);
                            } else if (file.isDirectory() && !file.getName().startsWith(".") &&
                                    !(dir.equals(directory.getName()) && "lib".equalsIgnoreCase(file.getName()))) {
                                //look in the child directory also
                                findFileForGivenDirectory(file, extension, dir);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
    }

    /**
     * Searches a given folder for aar files and adds them to a list in the WSInfolist class.
     * If sub folders found, those are also searched for services.
     * Ex : repository/services/foo/1.0.0/echo.aar
     *      repository/services/foo/1.0.1/echo.aar
     *      repository/services/echo.aar 
     * @param root - directory from which we start searching for services
     */
    protected void findServicesInDirectory(File root) {
        // flag to identify whether this is the services folder
        boolean servicesDir = false;
        if (deploymentEngine.getServicesDir().getAbsolutePath().equals(root.getAbsolutePath())) {
            servicesDir = true;
        }
        File[] files = root.listFiles();

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (isSourceControlDir(file)) {
                    continue;
                }
                if (file.isDirectory()) {
                    if (!(servicesDir && "lib".equalsIgnoreCase(file.getName())) &&
                        !file.getName().startsWith(".")) {
                        File servicesXML = new File(file, DeploymentConstants.SERVICES_XML);
                        if (!servicesXML.exists()) {
                            servicesXML =
                                    new File(file, DeploymentConstants.SERVICES_XML.toLowerCase());
                        }
                        if (servicesXML.exists()) {
                            addFileToDeploy(file, deploymentEngine.getServiceDeployer(),
                                            WSInfo.TYPE_SERVICE);
                        } else {
                            findServicesInDirectory(file);
                        }
                    }
                } else {
                    if (DeploymentFileData.isServiceArchiveFile(file.getName())) {
                        addFileToDeploy(file, deploymentEngine.getServiceDeployer(),
                                        WSInfo.TYPE_SERVICE);
                    }
                }
            }
        }
    }

    /** Method invoked from the scheduler to start the listener. */
    public void startListener() {
        checkServices();
//        update();
    }

    /** Updates WSInfoList object. */
    public void update() {
        wsInfoList.update();
    }

    public void updateRemote() throws Exception {
        findServicesInDirectory(deploymentEngine.getServicesDir());
        update();
    }

    public void addFileToDeploy(File file, Deployer deployer, int type) {
        if(!file.getName().startsWith(".")) {
            wsInfoList.addWSInfoItem(file, deployer, type);
        }
    }
}
