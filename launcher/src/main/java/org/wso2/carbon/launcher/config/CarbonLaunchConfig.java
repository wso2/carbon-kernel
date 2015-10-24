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

import static org.wso2.carbon.launcher.Constants.CARBON_HOME;
import static org.wso2.carbon.launcher.Constants.CARBON_INITIAL_OSGI_BUNDLES;
import static org.wso2.carbon.launcher.Constants.CARBON_OSGI_FRAMEWORK;
import static org.wso2.carbon.launcher.Constants.CARBON_OSGI_REPOSITORY;
import static org.wso2.carbon.launcher.Constants.CARBON_SERVER_LISTENERS;
import static org.wso2.carbon.launcher.Constants.ECLIPSE_P2_DATA_AREA;
import static org.wso2.carbon.launcher.Constants.OSGI_CONFIG_AREA;
import static org.wso2.carbon.launcher.Constants.OSGI_INSTALL_AREA;
import static org.wso2.carbon.launcher.Constants.OSGI_INSTANCE_AREA;


/**
 * Loading properties from launch configuration (launch.properties) file
 * and initialize carbon server listeners.
 *
 * @since 5.0.0
 */
public class CarbonLaunchConfig {

    private static final Logger logger = BootstrapLogger.getCarbonLogger(CarbonLaunchConfig.class.toString());

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
            // First load all the default properties.
            loadFromClasspath();

            // Then load all the other properties defined in the file.
            loadLaunchConfiguration(fileInputStream);

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
            // First load all the default properties.
            loadFromClasspath();

            // Then load all the other properties defined in the file.
            loadLaunchConfiguration(launchPropURL.openStream());

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

    /**
     * Get the value of carbonOSGiRepository.
     *
     * @return {@link #carbonOSGiRepository}
     */
    public URL getCarbonOSGiRepository() {
        return carbonOSGiRepository;
    }

    /**
     * Get the value of carbonOSGiFramework.
     *
     * @return {@link #carbonOSGiFramework}
     */
    public URL getCarbonOSGiFramework() {
        return carbonOSGiFramework;
    }

    /**
     * Get the value of osgiInstallArea.
     *
     * @return {@link #osgiInstallArea}
     */
    public URL getOSGiInstallArea() {
        return osgiInstallArea;
    }

    /**
     * Get the value of osgiConfigurationArea.
     *
     * @return {@link #osgiConfigurationArea}
     */
    public URL getOSGiConfigurationArea() {
        return osgiConfigurationArea;
    }

    /**
     * Get the value of osgiInstanceArea.
     *
     * @return {@link #osgiInstanceArea}
     */
    public URL getOSGiInstanceArea() {
        return osgiInstanceArea;
    }

    /**
     * Get the value of eclipseP2DataArea.
     *
     * @return {@link #eclipseP2DataArea}
     */
    public URL getEclipseP2DataArea() {
        return eclipseP2DataArea;
    }

    /**
     * @return initial bundle list
     */
    public List<CarbonInitialBundle> getInitialBundles() {
        return Collections.unmodifiableList(initialBundles);
    }

    /**
     * Get the value of carbon home.
     *
     * @return {@link #carbonHome}
     */
    public String getCarbonHome() {
        return carbonHome;
    }

    /**
     * @return carbon server listeners
     */
    public List<CarbonServerListener> getCarbonServerListeners() {
        return Collections.unmodifiableList(carbonServerListeners);
    }

    /**
     * Load launch configuration from file.
     */
    private void loadFromClasspath() {
        InputStream stream = CarbonLaunchConfig.class.getClassLoader().getResourceAsStream("launch.properties");
        loadLaunchConfiguration(stream);
    }

    /**
     * Loading launch properties from launch configuration..
     *
     * @param is launch configuration input stream
     */
    private void loadLaunchConfiguration(InputStream is) {
        try {
            Properties launchProps = new Properties();
            launchProps.load(is);

            // Load the Map from the properties object.
            // Replace variables with proper value. eg. ${carbon.home}.
            for (Map.Entry entry : launchProps.entrySet()) {
                properties.put((String) entry.getKey(), Utils.initializeSystemProperties((String) entry.getValue()));
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

    /**
     * Resolving and initializing properties.
     */
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
     * @param path       file path to resolve
     * @param parentPath parent path for the file
     * @param key        property key
     * @return URL for file
     */
    private URL resolvePath(String path, String parentPath, String key) {
        URL url;

        if (Utils.isNullOrEmpty(path)) {
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

    /**
     * Populating bundles read from the initialBundleList.
     *
     * @param initialBundleList comma separated bundle list
     */
    private void populateInitialBundlesList(String initialBundleList) {
        if (Utils.isNullOrEmpty(initialBundleList)) {
            return;
        }

        String[] strArray = Utils.tokenize(initialBundleList, ",");

        // Pattern to extract information from a initial bundle entry.
        // e.g. file:plugins/org.eclipse.equinox.console_1.0.100.v20130429-0953.jar@2:true.
        Pattern bundleEntryPattern = Pattern.compile("(file):(.*)@(.*):(.*)");
        for (String bundleEntry : strArray) {

            if (Utils.isNullOrEmpty(bundleEntry)) {
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

    /**
     * Adding carbon server listeners.
     *
     * @param serverListenersList comma separated server listeners
     */
    private void loadCarbonServerListeners(String serverListenersList) {
        if (Utils.isNullOrEmpty(serverListenersList)) {
            return;
        }

        String[] classNameArray = Utils.tokenize(serverListenersList, ",");
        for (String className : classNameArray) {
            if (Utils.isNullOrEmpty(className)) {
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

    /**
     * @return an unmodifiable view of the specified map
     */
    public Map<String, String> getProperties() {
        return Collections.unmodifiableMap(properties);
    }
}
