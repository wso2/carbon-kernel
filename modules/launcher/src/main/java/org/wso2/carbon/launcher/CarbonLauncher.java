package org.wso2.carbon.launcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.wso2.carbon.launcher.config.CarbonInitialBundle;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;
import org.wso2.carbon.launcher.utils.Utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import static org.wso2.carbon.launcher.utils.Constants.*;

/**
 * Launches a Carbon instance
 */
public class CarbonLauncher {

    private CarbonLaunchConfig<String, String> config;
    private Framework framework;

    public CarbonLauncher(CarbonLaunchConfig<String, String> config) {
        this.config = config;
    }

    public void launch() throws Exception {
        ClassLoader fwkClassLoader = createOSGiFwkClassLoader();
        FrameworkFactory fwkFactory = loadOSGiFwkFactory(fwkClassLoader);
        framework = fwkFactory.newFramework(config);
        try {
            framework.init();
            // TODO add framework listeners, if any.
            framework.start();
            loadInitialBundles(framework.getBundleContext());
        } catch (BundleException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static void initAndVerifySysProps() {
        String carbonHome = System.getProperty(CARBON_HOME);
        if (carbonHome == null || carbonHome.length() == 0) {
            throw new RuntimeException("carbon.home system property must be set before starting the server");
        }

        String profileName = System.getProperty(PROFILE);
        if (profileName == null || profileName.length() == 0) {
            System.setProperty(PROFILE, DEFAULT_PROFILE);
        }

        System.setProperty(LOGGING_DEFAULT_SERVICE_NAME, PAX_LOGGING_LEVEL);
        System.setProperty(BUNDLE_CONFIG_LOCATION, carbonHome + File.separator +
                "repository" + File.separator + "conf" + File.separator + "logging-config");
    }

    private ClassLoader createOSGiFwkClassLoader() {
        URL fwkBundleURL = config.getCarbonOSGiFramework();
        return new URLClassLoader(new URL[]{fwkBundleURL});
    }

    private FrameworkFactory loadOSGiFwkFactory(ClassLoader classLoader) {
        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class, classLoader);
        if (!loader.iterator().hasNext()) {
            throw new RuntimeException("An implementation of the " + FrameworkFactory.class.getName() +
                    " must be available in the classpath");
        }
        return loader.iterator().next();
    }

    private void loadInitialBundles(BundleContext bundleContext) throws BundleException {
        for (CarbonInitialBundle initialBundleInfo : config.getInitialBundles()) {
            Bundle bundle = bundleContext.installBundle(initialBundleInfo.getLocation().toString());
            bundle.adapt(BundleStartLevel.class).setStartLevel(initialBundleInfo.getLevel());
            if (initialBundleInfo.isStart()) {
                bundle.start();
            }
        }
    }

    public static void main(String[] args) {
        CarbonLaunchConfig<String, String> config;

        initAndVerifySysProps();

        String launchPropFilePath = Utils.getRepositoryConfDir() + File.separator + "osgi" +
                File.separator + LAUNCH_PROPERTIES_FILE;
        File launchPropFile = new File(launchPropFilePath);

        if (launchPropFile.exists()) {
            config = new CarbonLaunchConfig<String, String>(launchPropFile);
        } else {
            config = new CarbonLaunchConfig<String, String>();
        }

        CarbonLauncher carbonLauncher = new CarbonLauncher(config);
        try {
            carbonLauncher.launch();
        } catch (Exception e) {
            // TODO fix this.
            e.printStackTrace();
        }
    }
}
