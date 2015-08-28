/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.launcher.config;

import org.wso2.carbon.launcher.CarbonServerListener;
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;
import org.wso2.carbon.launcher.utils.FileResolver;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.launcher.utils.Constants.CARBON_HOME;
import static org.wso2.carbon.launcher.utils.Constants.CARBON_INITIAL_OSGI_BUNDLES;
import static org.wso2.carbon.launcher.utils.Constants.CARBON_OSGI_FRAMEWORK;
import static org.wso2.carbon.launcher.utils.Constants.CARBON_OSGI_REPOSITORY;
import static org.wso2.carbon.launcher.utils.Constants.CARBON_SERVER_LISTENERS;
import static org.wso2.carbon.launcher.utils.Constants.ECLIPSE_P2_DATA_AREA;
import static org.wso2.carbon.launcher.utils.Constants.OSGI_CONFIG_AREA;
import static org.wso2.carbon.launcher.utils.Constants.OSGI_INSTALL_AREA;
import static org.wso2.carbon.launcher.utils.Constants.OSGI_INSTANCE_AREA;

/**
 * TODO: class level comment
 *
 * @param <K> TODO
 * @param <V> TODO
 */
public class CarbonLaunchConfig<K, V> {

    private static final Logger logger = BootstrapLogger.getBootstrapLogger();

    private URL carbonOSGiRepository;
    private URL carbonOSGiFramework;
    private URL osgiInstallArea;
    private URL osgiConfigurationArea;
    private URL osgiInstanceArea;
    private URL eclipseP2DataArea;

    private String carbonHome;

    private String carbonOSGiRepositoryPath;

    private List<CarbonInitialBundle> initialBundles = new ArrayList<>();

    private List<CarbonServerListener> carbonServerListeners = new ArrayList<>();

    private Map<String, String> properties = new HashMap<>();

    /**
     * Load the launch configuration from the classpath.
     */
    public CarbonLaunchConfig() {
        loadFromClasspath();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Loaded properties from the launch.properties file.");
            for (Map.Entry entry : properties.entrySet()) {
                logger.log(Level.FINE, "Key: " + entry.getKey() + " Value: " + entry.getValue());
            }
        }

        initializeProperties();
    }

    /**
     * Load the configuration from the given properties file.
     *
     * @param launchPropFile launch.properties file
     */
    public CarbonLaunchConfig(File launchPropFile) {
        try (FileInputStream fileInputStream = new FileInputStream(launchPropFile)) {
            // First load all the default properties
            loadFromClasspath();

            // Then load all the other properties defined in the file.
            loadInternal(fileInputStream);

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Loaded properties from the launch.properties file.");
                for (Map.Entry entry : properties.entrySet()) {
                    logger.log(Level.FINE, "Key: " + entry.getKey() + " Value: " + entry.getValue());
                }
            }

            initializeProperties();
        } catch (FileNotFoundException e) {
            String errorMsg = "File " + launchPropFile + "does not exists";
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Exception while loading file " + launchPropFile;
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Load the configuration from the given properties file.
     *
     * @param launchPropURL launch.properties URL
     */
    public CarbonLaunchConfig(URL launchPropURL) {
        try {
            // First load all the default properties
            loadFromClasspath();

            // Then load all the other properties defined in the file.
            loadInternal(launchPropURL.openStream());

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Loaded properties from the launch.properties file.");
                for (Map.Entry entry : properties.entrySet()) {
                    logger.log(Level.FINE, "Key: " + entry.getKey() + " Value: " + entry.getValue());
                }
            }

            initializeProperties();
        } catch (IOException e) {
            String errorMsg = "Error loading the launch.properties";
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    public URL getCarbonOSGiRepository() {
        return carbonOSGiRepository;
    }

    public URL getCarbonOSGiFramework() {
        return carbonOSGiFramework;
    }

    public URL getOSGiInstallArea() {
        return osgiInstallArea;
    }

    public URL getOSGiConfigurationArea() {
        return osgiConfigurationArea;
    }

    public URL getOSGiInstanceArea() {
        return osgiInstanceArea;
    }

    public URL getEclipseP2DataArea() {
        return eclipseP2DataArea;
    }

    public List<CarbonInitialBundle> getInitialBundles() {
        return Collections.unmodifiableList(initialBundles);
    }

    public String getCarbonHome() {
        return carbonHome;
    }

    public List<CarbonServerListener> getCarbonServerListeners() {
        return Collections.unmodifiableList(carbonServerListeners);
    }

    private void loadFromClasspath() {
        InputStream stream = CarbonLaunchConfig.class.getClassLoader().getResourceAsStream("launch.properties");
        loadInternal(stream);
    }

    private void loadInternal(InputStream is) {
        try {
            Properties launchProps = new Properties();
            launchProps.load(is);

            // Load the Map from the properties object.
            // Replace variables with proper value. eg. ${carbon.home}
            for (Map.Entry entry : launchProps.entrySet()) {
                properties.put((String) entry.getKey(), Utils.substituteVars((String) entry.getValue()));
            }

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);

        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                //Ignored
            }
        }
    }

    private void initializeProperties() {
        carbonHome = System.getProperty(CARBON_HOME);

        carbonOSGiRepository = resolvePath(properties.get(CARBON_OSGI_REPOSITORY), carbonHome, CARBON_OSGI_REPOSITORY);
        carbonOSGiRepositoryPath = carbonOSGiRepository.toExternalForm().substring(5);

        carbonOSGiFramework =
                resolvePath(properties.get(CARBON_OSGI_FRAMEWORK), carbonOSGiRepositoryPath, CARBON_OSGI_FRAMEWORK);
        osgiInstallArea =
                resolvePath(properties.get(OSGI_INSTALL_AREA), carbonOSGiRepositoryPath, OSGI_INSTALL_AREA);
        osgiConfigurationArea =
                resolvePath(properties.get(OSGI_CONFIG_AREA), carbonOSGiRepositoryPath, OSGI_CONFIG_AREA);
        osgiInstanceArea =
                resolvePath(properties.get(OSGI_INSTANCE_AREA), carbonOSGiRepositoryPath, OSGI_INSTANCE_AREA);
        eclipseP2DataArea =
                resolvePath(properties.get(ECLIPSE_P2_DATA_AREA), carbonOSGiRepositoryPath, ECLIPSE_P2_DATA_AREA);

        populateInitialBundlesList(properties.get(CARBON_INITIAL_OSGI_BUNDLES));
        loadCarbonServerListeners(properties.get(CARBON_SERVER_LISTENERS));
    }

    /**
     * Resolve a file path against a parent path.
     *
     * @param path
     * @param parentPath
     * @param key
     * @return
     */
    private URL resolvePath(String path, String parentPath, String key) {
        URL url;

        if (Utils.checkForNullOrEmpty(path)) {
            String errorMsg = "The property " + key + " must not be null or empty.";
            logger.log(Level.SEVERE, errorMsg);
            throw new RuntimeException("The property " + key + " must not be null or empty.");

        } else {
            url = FileResolver.resolve(path, parentPath);
        }

        properties.put(key, url.toExternalForm());

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Path: " + path);
            logger.log(Level.FINE, "Parent path: " + parentPath);
            logger.log(Level.FINE, "Resolved path: " + url.toExternalForm());
        }

        return url;
    }

    private void populateInitialBundlesList(String initialBundleList) {
        if (Utils.checkForNullOrEmpty(initialBundleList)) {
            return;
        }

        String[] strArray = Utils.tokenize(initialBundleList, ",");

        // Pattern to extract information from a initial bundle entry.
        // e.g. file:plugins/org.eclipse.equinox.console_1.0.100.v20130429-0953.jar@2:true
        Pattern bundleEntryPattern = Pattern.compile("(file):(.*)@(.*):(.*)");
        for (String bundleEntry : strArray) {

            if (Utils.checkForNullOrEmpty(bundleEntry)) {
                continue;
            }

            Matcher matcher = bundleEntryPattern.matcher(bundleEntry.trim());
            if (!matcher.matches()) {
                throw new RuntimeException("Invalid initial bundle entry: " + bundleEntry);
            }

            if (!"file".equals(matcher.group(1))) {
                throw new RuntimeException("URLs other than file URLs are not supported.");
            }

            String path = matcher.group(2);
            int bundleStartLevel = Integer.parseInt(matcher.group(3));
            boolean start = Boolean.parseBoolean(matcher.group(4));

            URL bundleURL = FileResolver.resolve("file:" + path, carbonOSGiRepositoryPath);
            initialBundles.add(new CarbonInitialBundle(bundleURL, bundleStartLevel, start));

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Initial bundle entry: " + bundleEntry);
                logger.log(Level.FINE, "Bundle URL: " + bundleURL.toExternalForm());
                logger.log(Level.FINE, "Bundle start level: " + bundleStartLevel);
                logger.log(Level.FINE, "Start flag: " + start);
            }
        }
    }

    private void loadCarbonServerListeners(String serverListenersList) {
        if (Utils.checkForNullOrEmpty(serverListenersList)) {
            return;
        }

        String[] classNameArray = Utils.tokenize(serverListenersList, ",");
        for (String className : classNameArray) {
            if (Utils.checkForNullOrEmpty(className)) {
                continue;
            }

            try {
                Class clazz = Class.forName(className.trim());
                carbonServerListeners.add((CarbonServerListener) clazz.newInstance());

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Loaded CarbonServerListener: " + className);
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }

    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
