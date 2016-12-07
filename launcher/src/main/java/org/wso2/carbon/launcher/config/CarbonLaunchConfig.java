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
import org.wso2.carbon.launcher.utils.FileResolver;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.wso2.carbon.launcher.Constants.CARBON_HOME;
import static org.wso2.carbon.launcher.Constants.CARBON_INITIAL_OSGI_BUNDLES;
import static org.wso2.carbon.launcher.Constants.CARBON_OSGI_FRAMEWORK;
import static org.wso2.carbon.launcher.Constants.CARBON_OSGI_REPOSITORY;
import static org.wso2.carbon.launcher.Constants.CARBON_PROFILE_REPOSITORY;
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

    private static final Logger logger = Logger.getLogger(CarbonLaunchConfig.class.getName());

    private URL carbonOSGiRepository;
    private URL carbonProfileRepository;
    private URL carbonOSGiFramework;
    private URL osgiInstallArea;
    private URL osgiConfigurationArea;
    private URL osgiInstanceArea;
    private URL eclipseP2DataArea;

    private String carbonHome;

    private String carbonOSGiRepositoryPath;
    private String carbonProfileRepositoryPath;

    private List<CarbonInitialBundle> initialBundles = new ArrayList<>();

    private List<CarbonServerListener> carbonServerListeners = new ArrayList<>();

    private Map<String, String> properties = new HashMap<>();

    /**
     * Load the launch configuration from the classpath.
     */
    public CarbonLaunchConfig() {
        loadCarbonConfiguration(null);
    }

    /**
     * Load the configuration from the given properties file.
     *
     * @param launchPropFile launch.properties file
     */
    public CarbonLaunchConfig(File launchPropFile) {
        loadCarbonConfiguration(launchPropFile);
    }

    /**
     * Load the configuration from a given url.
     *
     * @param launchPropURL launch.properties URL
     */
    public CarbonLaunchConfig(URL launchPropURL) {
        loadCarbonConfiguration(launchPropURL);
    }

    /**
     * Load configuration based on the given source. If the source is null, configuration is loaded only from
     * the launch.properties file located in the class path.
     *
     * @param source source to load configuration.
     * @param <T>    java.io.File or java.net.URL
     */
    private <T> void loadCarbonConfiguration(T source) {
        loadFromClasspath();
        if (source != null) {
            Properties customProperties = new Properties();
            if (source instanceof File) {
                customProperties = loadConfigurationFromFile((File) source);
            } else if (source instanceof URL) {
                customProperties = loadConfigurationFromUrl((URL) source);
            }
            mergeCustomProperties(customProperties);
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Loaded properties from the launch.properties file.");
            properties.forEach((key, value) -> logger.log(Level.FINE, "Key: " + key + " Value: " + value));
        }
        initializeProperties();
    }

    private void mergeCustomProperties(Properties customProperties) {
        customProperties.forEach((key, value) -> {
            if (CARBON_INITIAL_OSGI_BUNDLES.equals(key)) {
                properties.put((String) key, generateInitialBundlesList(properties.get(key), (String) value));
            } else {
                properties.put((String) key, (String) value);
            }
        });
    }

    private String generateInitialBundlesList(String defaultBundles, String customBundles) {
        String[] defaultBundlesArray = Utils.tokenize(defaultBundles, ",");
        String[] customBundlesArray = Utils.tokenize(customBundles, ",");
        List list = Arrays.stream(defaultBundlesArray).collect(Collectors.toList());
        list.addAll(list.size() - 1, Arrays.stream(customBundlesArray).collect(Collectors.toList()));
        return list.toString().substring(1, list.toString().length() - 1);
    }

    /**
     * Load carbon configuration from a java.io.File object.
     *
     * @param launchPropFile File
     */
    private Properties loadConfigurationFromFile(File launchPropFile) {
        try (FileInputStream fileInputStream = new FileInputStream(launchPropFile)) {
            return loadLaunchConfigurationFromStream(fileInputStream);
        } catch (FileNotFoundException e) {
            String errorMsg = "File " + launchPropFile + " does not exists";
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (IOException e) {
            String errorMsg = "Exception while loading file " + launchPropFile;
            logger.log(Level.SEVERE, errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Load carbon configuration given as a URL.
     *
     * @param launchPropURL URL
     */
    private Properties loadConfigurationFromUrl(URL launchPropURL) {
        try (InputStream stream = launchPropURL.openStream()) {
            return loadLaunchConfigurationFromStream(stream);
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
     * Load launch configuration from launch.properties file which resides in the classpath.
     */
    private void loadFromClasspath() {
        try (InputStream stream = CarbonLaunchConfig.class.getClassLoader().getResourceAsStream("launch.properties")) {
            Properties defaultProperties = loadLaunchConfigurationFromStream(stream);
            defaultProperties.forEach((key, value) -> properties.put((String) key, (String) value));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Loading launch properties from launch configuration..
     *
     * @param is launch configuration input stream
     */
    private Properties loadLaunchConfigurationFromStream(InputStream is) throws IOException {
        Properties launchProps = new Properties();
        launchProps.load(is);

        // Load the Map from the properties object.
        // Replace variables with proper value. eg. ${carbon.home}.
        launchProps.forEach(
                (key, value) -> launchProps.put((String) key, Utils.initializeSystemProperties((String) value)));
        return launchProps;
    }

    /**
     * Resolving and initializing properties.
     */
    private void initializeProperties() {
        carbonHome = System.getProperty(CARBON_HOME);

        carbonOSGiRepository = resolvePath(properties.get(CARBON_OSGI_REPOSITORY), carbonHome, CARBON_OSGI_REPOSITORY);
        carbonProfileRepository = resolvePath(properties.get(CARBON_PROFILE_REPOSITORY), carbonHome,
                CARBON_PROFILE_REPOSITORY);
        carbonOSGiRepositoryPath = carbonOSGiRepository.toExternalForm().substring(5);
        carbonProfileRepositoryPath = carbonProfileRepository.toExternalForm().substring(5);

        carbonOSGiFramework = resolvePath(properties.get(CARBON_OSGI_FRAMEWORK), carbonOSGiRepositoryPath,
                CARBON_OSGI_FRAMEWORK);
        osgiInstallArea = resolvePath(properties.get(OSGI_INSTALL_AREA), carbonProfileRepositoryPath,
                OSGI_INSTALL_AREA);
        osgiConfigurationArea = resolvePath(properties.get(OSGI_CONFIG_AREA), carbonProfileRepositoryPath,
                OSGI_CONFIG_AREA);
        osgiInstanceArea = resolvePath(properties.get(OSGI_INSTANCE_AREA), carbonProfileRepositoryPath,
                OSGI_INSTANCE_AREA);
        eclipseP2DataArea = resolvePath(properties.get(ECLIPSE_P2_DATA_AREA), carbonOSGiRepositoryPath,
                ECLIPSE_P2_DATA_AREA);

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
        if (Utils.isNullOrEmpty(path)) {
            String errorMsg = "The property " + key + " must not be null or empty.";
            logger.log(Level.SEVERE, errorMsg);
            throw new RuntimeException("The property " + key + " must not be null or empty.");

        }
        URL url = FileResolver.resolve(path, parentPath);
        if (url == null) {
            throw new RuntimeException("URL must not be null.");
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
