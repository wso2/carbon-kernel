package org.wso2.carbon.launcher.config;

import org.wso2.carbon.launcher.utils.FileResolver;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.wso2.carbon.launcher.utils.Constants.*;

public class CarbonLaunchConfig<K, V> extends HashMap<String, String> {

    private URL carbonOSGiRepository;
    private URL carbonOSGiFramework;
    private URL osgiInstallArea;
    private URL osgiConfigurationArea;
    private URL osgiInstanceArea;
    private URL eclipseP2DataArea;

    private String carbonHome;

    private String carbonOSGiRepositoryPath;

    private CarbonInitialBundle[] initialBundles;

    /**
     * Load the launch configuration from the classpath.
     */
    public CarbonLaunchConfig() {
        loadFromClasspath();
        initializeProperties();
    }

    /**
     * Load the configuration from the given properties file.
     *
     * @param launchPropFile launch.properties file
     */
    public CarbonLaunchConfig(File launchPropFile) {
        try {
            // First load all the default properties
            loadFromClasspath();

            // Then load all the other properties defined in the file.
            loadInternal(new FileInputStream(launchPropFile));

            initializeProperties();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File " + launchPropFile + "does not exists", e);
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

            initializeProperties();
        } catch (IOException e) {
            throw new RuntimeException("Error loading the launch.properties", e);
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

    public CarbonInitialBundle[] getInitialBundles() {
        return initialBundles;
    }

    public String getCarbonHome() {
        return carbonHome;
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
                this.put((String) entry.getKey(), Utils.substituteVars((String) entry.getValue()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading the launch.properties", e);
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

        carbonOSGiRepository = resolvePath(get(CARBON_OSGI_REPOSITORY), carbonHome, CARBON_OSGI_REPOSITORY);
        carbonOSGiRepositoryPath = carbonOSGiRepository.toExternalForm().substring(5);

        carbonOSGiFramework = resolvePath(get(CARBON_OSGI_FRAMEWORK), carbonOSGiRepositoryPath, CARBON_OSGI_FRAMEWORK);
        osgiInstallArea = resolvePath(get(OSGI_INSTALL_AREA), carbonOSGiRepositoryPath, OSGI_INSTALL_AREA);
        osgiConfigurationArea = resolvePath(get(OSGI_CONFIG_AREA), carbonOSGiRepositoryPath, OSGI_CONFIG_AREA);
        osgiInstanceArea = resolvePath(get(OSGI_INSTANCE_AREA), carbonOSGiRepositoryPath, OSGI_INSTANCE_AREA);
        eclipseP2DataArea = resolvePath(get(ECLIPSE_P2_DATA_AREA), carbonOSGiRepositoryPath, ECLIPSE_P2_DATA_AREA);

        populateInitialBundlesList(get(CARBON_INITIAL_OSGI_BUNDLES));
    }

    //TODO default values should be available in a launch.properties(classpath)
    private URL resolvePath(String path, String parentPath, String key) {
        // TODO temp...
        URL url = null;
        if (Utils.checkForNullOrEmpty(path)) {
            // Set the default value
            // TODO
        } else {
            url = FileResolver.resolve(path, parentPath);
        }
        //TODO NPE possible. Fix this - Sameera.
        put(key, url.toExternalForm());
        return url;
    }

    private void populateInitialBundlesList(String initialBundleList) {
        if (Utils.checkForNullOrEmpty(initialBundleList)) {
            initialBundles = new CarbonInitialBundle[0];
        }

        String[] strArray = Utils.tokenize(initialBundleList, ",");
        ArrayList<CarbonInitialBundle> bundleArrayList = new ArrayList<CarbonInitialBundle>(strArray.length);

        // Pattern to extract information from a initial bundle entry.
        // e.g. file:plugins/org.eclipse.equinox.console_1.0.100.v20130429-0953.jar@2:true
        Pattern bundleEntryPattern = Pattern.compile("(file):(.*)@(.*):(.*)");
        for (String bundleEntry : strArray) {

            Matcher matcher = bundleEntryPattern.matcher(bundleEntry);
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
            bundleArrayList.add(new CarbonInitialBundle(bundleURL, bundleStartLevel, start));
        }

        initialBundles = bundleArrayList.toArray(new CarbonInitialBundle[bundleArrayList.size()]);
    }
}
