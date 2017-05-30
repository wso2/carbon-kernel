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
package org.wso2.carbon.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.server.util.Utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Responsible for launching the Carbon server by launching Equinox OSGi framework.
 */
public class CarbonLauncher {

    private static Log log = LogFactory.getLog(CarbonLauncher.class);

    protected static final String FILE_SCHEME = "file:";
    protected static final String FRAMEWORK_BUNDLE_NAME = "org.eclipse.osgi";
    protected static final String STARTER =
            "org.eclipse.core.runtime.adaptor.EclipseStarter";
    protected static final String FRAMEWORKPROPERTIES =
            "org.eclipse.osgi.framework.internal.core.FrameworkProperties";
    protected static final String NULL_IDENTIFIER = "@null";
    protected static final String OSGI_FRAMEWORK = "osgi.framework";
    protected static final String OSGI_INSTANCE_AREA = "osgi.instance.area";
    protected static final String OSGI_CONFIGURATION_AREA = "osgi.configuration.area";
    protected static final String OSGI_INSTALL_AREA = "osgi.install.area";
    protected static final String P2_DATA_AREA = "eclipse.p2.data.area";

    private File platformDirectory;
    private URLClassLoader frameworkClassLoader;

    /**
     * Launches Equinox OSGi framework by  invoking EclipseStarter.startup() method using reflection.
     * Creates a ChildFirstClassLoader out of the OSGi framework jar and set the classloader as the framework
     * classloader.
     */
    public void launch() {
        platformDirectory = Utils.getCarbonComponentRepo();
        if (platformDirectory == null) {
            throw new IllegalStateException(
                    "Could not start the Framework - (not deployed)");
        }

        if (frameworkClassLoader != null) {
            return;
        }

        final Map<String, String> initialPropsMap = buildInitialPropertyMap();
        String[] args2 = Utils.getArgs();

        ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            System.setProperty("osgi.framework.useSystemProperties", "false");

            frameworkClassLoader = java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<URLClassLoader>() {
                        public URLClassLoader run() {
                            URLClassLoader cl = null;
                            try {
                                cl = new ChildFirstURLClassLoader(
                                        new URL[]{new URL(initialPropsMap.get(OSGI_FRAMEWORK))}, null);
                            } catch (MalformedURLException e) {
                                log.error(e.getMessage(), e);
                            }
                            return cl;
                        }
                    }
            );

//            frameworkClassLoader =

            //Loads EclipseStarter class.
            Class clazz = frameworkClassLoader.loadClass(STARTER);

            //Set the propertyMap by invoking setInitialProperties method.
            Method setInitialProperties =
                    clazz.getMethod("setInitialProperties", Map.class);
            setInitialProperties.invoke(null, initialPropsMap);

            //Invokes the startup method with some arguments.
            Method runMethod = clazz.getMethod("startup", String[].class, Runnable.class);
            runMethod.invoke(null, args2, null);

        } catch (InvocationTargetException ite) {
            Throwable t = ite.getTargetException();
            if (t == null) {
                t = ite;
            }
            throw new RuntimeException(t.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            Thread.currentThread().setContextClassLoader(original);
        }
    }

    /**
     * buildInitialPropertyMap create the initial set of properties from the contents of launch.ini
     * and for a few other properties necessary to launch defaults are supplied if not provided.
     * The value '@null' will set the map value to null.
     *
     * @return a map containing the initial properties
     */
    private Map<String, String> buildInitialPropertyMap() {
        Map<String, String> initialPropertyMap = new HashMap<String, String>();
        String carbonConfigHome = System.getProperty(LauncherConstants.CARBON_CONFIG_DIR_PATH);
        Properties launchProperties;
        if (carbonConfigHome == null) {
            String carbonHome = System.getProperty(LauncherConstants.CARBON_HOME);
            launchProperties = Utils.loadProperties(Paths.get(carbonHome, "repository", "conf", "etc", LauncherConstants.LAUNCH_INI).toString());
        } else {
            launchProperties = Utils.loadProperties(Paths.get(carbonConfigHome, "etc", LauncherConstants.LAUNCH_INI).toString());
        }
        for (Object o : launchProperties.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.endsWith("*")) { //$NON-NLS-1$
                if (value.equals(NULL_IDENTIFIER)) {
                    Utils.clearPrefixedSystemProperties(key.substring(0, key.length() - 1),
                            initialPropertyMap);
                }
            } else if (value.equals(NULL_IDENTIFIER)) {
                initialPropertyMap.put(key, null);
            } else {
                initialPropertyMap.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
        try {

             /**
            *  in order to support multiple profiling, the new install, configuration and workspace area got to move
            *  from ../components/ to ../components/ PROFILE_ID/
            */
            // install.area if not specified
            if (initialPropertyMap.get(OSGI_INSTALL_AREA) == null) {
                //specifying the install.area according to the running Profile
                File installDir = new File(platformDirectory, System.getProperty(LauncherConstants.PROFILE_ID));

                 initialPropertyMap
                        .put(OSGI_INSTALL_AREA, installDir.toURL().toExternalForm());
            }

            // configuration.area if not specified
            if (initialPropertyMap.get(OSGI_CONFIGURATION_AREA) == null) {
                File configurationDirectory = new File (platformDirectory,
                        System.getProperty(LauncherConstants.PROFILE_ID) +
                                File.separator + "configuration");
                initialPropertyMap.put(OSGI_CONFIGURATION_AREA,
                        configurationDirectory.toURL().toExternalForm());
            }

            // instance.area if not specified
            if (initialPropertyMap.get(OSGI_INSTANCE_AREA) == null) {
                File workspaceDirectory = new File(platformDirectory,  System.getProperty(LauncherConstants.PROFILE_ID) +
                        File.separator + "workspace");
                initialPropertyMap
                        .put(OSGI_INSTANCE_AREA, workspaceDirectory.toURL().toExternalForm());
            }

            // osgi.framework if not specified
            if (initialPropertyMap.get(OSGI_FRAMEWORK) == null) {
                // search for osgi.framework in osgi.install.area
                /*String installArea = initialPropertyMap.get(OSGI_INSTALL_AREA);

                // only support file type URLs for install area
                if (installArea.startsWith(FILE_SCHEME)) {
                    installArea = installArea.substring(FILE_SCHEME.length());
                }

                String path = new File(installArea, "plugins").toString();*/
                String path = new File(platformDirectory, "plugins").toString();
                path = Utils.searchFor(FRAMEWORK_BUNDLE_NAME, path);
                if (path == null) {
                    throw new RuntimeException("Could not find framework");
                }

                initialPropertyMap.put(OSGI_FRAMEWORK,
                        new File(path).toURL().toExternalForm());
            }
            if (initialPropertyMap.get(P2_DATA_AREA) == null) {
                /*initialPropertyMap.put(P2_DATA_AREA, new File(platformDirectory, System.getProperty(LauncherConstants.PROFILE_ID) +
                                                                   File.separator + "p2").toString());*/

                initialPropertyMap.put(P2_DATA_AREA, new File(platformDirectory, "p2").toString());
                //System.out.println("the data area: " + initialPropertyMap.get(P2_DATA_AREA));
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error establishing location");
        }
        return initialPropertyMap;
    }
}
