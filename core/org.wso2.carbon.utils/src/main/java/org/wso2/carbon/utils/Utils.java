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
package org.wso2.carbon.utils;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.component.xml.Component;
import org.wso2.carbon.utils.component.xml.ComponentConfigFactory;
import org.wso2.carbon.utils.component.xml.ComponentConstants;
import org.wso2.carbon.utils.component.xml.config.DeployerConfig;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 *
 */
public class Utils {
	
	private Utils() {
	    //disable external instantiation
	}

    private static final int BYTE_ARRAY_SIZE = 1024;
	private static Log log = LogFactory.getLog(Utils.class);

    public static void transform(InputStream xmlStream, InputStream xslStream,
                                 OutputStream outputStream) throws TransformerException {
        Source xmlStreamSource = new StreamSource(xmlStream);
        Source xslStreamSource = new StreamSource(xslStream);
        Result result = new StreamResult(outputStream);
        Transformer transformer = TransformerFactory.newInstance().newTransformer(xslStreamSource);
        transformer.transform(xmlStreamSource, result);
    }

    public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException {
        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdir()) {
                throw new IOException("Fail to create the directory: " + targetLocation.getAbsolutePath());            
            }

            String[] children = sourceLocation.list();
            for (String aChildren : children) {
                copyDirectory(new File(sourceLocation, aChildren),
                        new File(targetLocation, aChildren));
            }

        } else {
            int len;
            OutputStream out = null;
            InputStream in = new FileInputStream(sourceLocation);

            try {
                out = new FileOutputStream(targetLocation);
                // Copy the bits from instream to outstream
                byte[] buf = new byte[BYTE_ARRAY_SIZE];
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
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
    }

    /**
     * For a given Zip file, process each entry.
     *
     * @param zipFileLocation zipFileLocation
     * @param targetLocation  targetLocation
     * @throws CarbonException CarbonException
     */
    public static void deployZipFile(File zipFileLocation, File targetLocation)
            throws CarbonException {
        try {
            SortedSet<String> dirsMade = new TreeSet<String>();
            JarFile jarFile = new JarFile(zipFileLocation);
            Enumeration all = jarFile.entries();
            while (all.hasMoreElements()) {
                getFile((ZipEntry) all.nextElement(), jarFile, targetLocation, dirsMade);
            }
        } catch (IOException e) {
            log.error("Error while copying component", e);
            throw new CarbonException(e);
        }
    }

    /**
     * Process one file from the zip, given its name.
     * Either print the name, or create the file on disk.
     *
     * @param zipEntry              zip entry
     * @param zippy          jarfile
     * @param targetLocation target
     * @param dirsMade       dir
     * @throws java.io.IOException will be thrown
     */
    private static void getFile(ZipEntry zipEntry, JarFile zippy, File targetLocation,
                                SortedSet<String> dirsMade) throws IOException {
        byte[] b = new byte[BYTE_ARRAY_SIZE];
        String zipName = zipEntry.getName();

        if (zipName.startsWith("/")) {
            zipName = zipName.substring(1);
        }
        //Process only fliles that start with "ui"
        if (!zipName.startsWith("ui")) {
            return;
        }
        // Strip off the ui bit
        zipName = zipName.substring(2);
        // if a directory, just return. We mkdir for every file,
        // since some widely-used Zip creators don't put out
        // any directory entries, or put them in the wrong place.
        if (zipName.endsWith("/")) {
            return;
        }
        // Else must be a file; open the file for output
        // Get the directory part.
        int ix = zipName.lastIndexOf('/');
        if (ix > 0) {
            String dirName = zipName.substring(0, ix);
            if (!dirsMade.contains(dirName)) {
                File d = new File(targetLocation, dirName);
                // If it already exists as a dir, don't do anything
                if (!(d.exists() && d.isDirectory())) {
                    // Try to create the directory, warn if it fails
                    if (log.isDebugEnabled()) {
                        log.debug("Deploying Directory: " + dirName);
                    }
                    if (!d.mkdirs()) {
                        log.warn("Warning: unable to mkdir " + dirName);
                    }
                    dirsMade.add(dirName);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Deploying " + zipName);
        }

        int n;
        InputStream is = zippy.getInputStream(zipEntry);
        File file = new File(targetLocation, zipName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            while ((n = is.read(b)) > 0) {
                os.write(b, 0, n);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                log.warn("Unable to close the InputStream " + e.getMessage(), e);
            }

            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                log.warn("Unable to close the OutputStream " + e.getMessage(), e);
            }
        }
    }

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     *
     * @param dir directory to delete
     * @return status
     */
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
        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Read context name from carbon.xml
     * "carbon" will be the default value
     *
     * @return webcontext name
     */
    public static String getWebContextName(BundleContext bundleContext) {
        String webContext = "carbon";

        ServerConfiguration sc = ServerConfiguration.getInstance();
        if (sc != null) {
            String value = sc.getFirstProperty("WebContext");
            if (value != null) {
                webContext = value;
            }
        }
        return webContext;
    }

    /**
     * Registers the Deployer services by using values defined in the corresonding bundles component.xml file..
     *
     * @param bundleContext
     */
    public static void registerDeployerServices(BundleContext bundleContext) throws Exception {
        URL url = bundleContext.getBundle().getEntry("META-INF/component.xml");
        if (url == null) {
            return;
        }

        InputStream inputStream = url.openStream();
        Component component = ComponentConfigFactory.build(inputStream);
        DeployerConfig[] deployerConfigs = null;
        if (component != null) {
            deployerConfigs = (DeployerConfig[]) component.getComponentConfig(ComponentConstants.DEPLOYER_CONFIG);
        }

        if (deployerConfigs != null) {
            for (DeployerConfig deployerConfig : deployerConfigs) {
                Class deployerClass = null;
                try {
                    deployerClass = bundleContext.getBundle().loadClass(deployerConfig.getClassStr());
                } catch (ClassNotFoundException e) {
                    deployerClass = Class.forName(deployerConfig.getClassStr());
                }
                Deployer deployer = (Deployer) deployerClass.newInstance();
                String directory = deployerConfig.getDirectory();
                String extension = deployerConfig.getExtension();
                deployer.setDirectory(directory);
                deployer.setExtension(extension);

                Dictionary propsMap = new Hashtable(2);
                propsMap.put(DeploymentConstants.DIRECTORY, directory);
                propsMap.put(DeploymentConstants.EXTENSION, extension);
                propsMap.put(CarbonConstants.AXIS2_CONFIG_SERVICE, Deployer.class.getName());

                //Registering the Axis1 deployer in the OSGi registry
                bundleContext.registerService(Deployer.class.getName(), deployer, propsMap);
            }
        }
    }
}
