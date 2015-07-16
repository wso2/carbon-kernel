package org.wso2.carbon.launcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.wso2.carbon.launcher.bootstrapLogging.BootstrapLogger;
import org.wso2.carbon.launcher.config.CarbonInitialBundle;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.wso2.carbon.launcher.utils.Constants.*;

/**
 * Launches a Carbon instance
 */
public class CarbonServer {

    private static final Logger logger = BootstrapLogger.getBootstrapLogger();

    private CarbonLaunchConfig<String, String> config;
    private Framework framework;

    public CarbonServer(CarbonLaunchConfig<String, String> config) {
        this.config = config;
    }

    /**
     * Starts a Carbon server instance. This method returns only after the server instance stops completely.
     *
     * @throws Exception
     */
    public void start() throws Exception {

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Starting Carbon server instance.");
        }

        // Sets the server start time.
        System.setProperty(CARBON_START_TIME, Long.toString(System.currentTimeMillis()));

        try {
            // Creates an OSGi framework instance
            ClassLoader fwkClassLoader = createOSGiFwkClassLoader();
            FrameworkFactory fwkFactory = loadOSGiFwkFactory(fwkClassLoader);
            framework = fwkFactory.newFramework(config);

            // Notify Carbon server start
            dispatchEvent(CarbonServerEvent.STARTING);

            // Initialize and start OSGi framework.
            initAndStartOSGiFramework();

            // Loads initial bundles listed in the launch.properties file.
            loadInitialBundles(framework.getBundleContext());

            // This thread waits until the OSGi framework comes to a complete shutdown.
            waitForServerStop();

            // Notify Carbon server shutdown.
            dispatchEvent(CarbonServerEvent.STOPPING);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Stop this Carbon server instance.
     */
    public void stop() {
        if (!isFrameworkActive()) {
            return;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Stopping the OSGi framework.");
        }

        // Framework.stop() method returns before the framework shutdown. But this.stop() method should only return
        //  after framework stops completely. Therefore we invokes the framework.stop() method in a new
        //  thread and this thread waits till the framework stops completely using the framework.waitForStop() method.
        new Thread() {
            public void run() {
                try {
                    framework.stop();
                } catch (BundleException e) {
                    logger.log(Level.SEVERE, e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }.start();


        try {
            FrameworkEvent event = framework.waitForStop(1000 * 60 * 3);
            if (event.getType() == FrameworkEvent.WAIT_TIMEDOUT) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "OSGi framework did not stop during the given time.");
                }
//                return;
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private void initAndStartOSGiFramework() throws BundleException {
        // Initializes the framework. Framework will try to resolve all the bundles if their requirements
        //  can be satisfied.
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Initializing the OSGi framework.");
        }

        framework.init();

        // Starts the framework.
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Starting the OSGi framework.");
        }

        framework.start();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Started the OSGi framework.");
        }
    }

    private void waitForServerStop() throws Exception {
        if (!isFrameworkActive()) {
            return;
        }

        // TODO do while loop.. Improvement
        // TODO add notify the carbon server listeners about this event.
        while (true) {
            FrameworkEvent event = framework.waitForStop(0);

            // We should not stop the framework if the user has updated the system bundle via the OSGi console or
            //  programmatically. In this case, framework will shutdown and start itself.
            if (event.getType() != FrameworkEvent.STOPPED_UPDATE) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "OSGi framework is stopped for update.");
                }
                break;
            }
        }
    }

    private ClassLoader createOSGiFwkClassLoader() {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Creating OSGi framework class loader.");
        }

        URL fwkBundleURL = config.getCarbonOSGiFramework();
        return new URLClassLoader(new URL[]{fwkBundleURL});
    }

    private FrameworkFactory loadOSGiFwkFactory(ClassLoader classLoader) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Loading OSGi FrameworkFactory implementation class from the classpath.");
        }

        ServiceLoader<FrameworkFactory> loader = ServiceLoader.load(FrameworkFactory.class, classLoader);
        if (!loader.iterator().hasNext()) {
            throw new RuntimeException("An implementation of the " + FrameworkFactory.class.getName() +
                    " must be available in the classpath");
        }
        return loader.iterator().next();
    }

    private void loadInitialBundles(BundleContext bundleContext) throws BundleException {
        for (CarbonInitialBundle initialBundleInfo : config.getInitialBundles()) {

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Loading initial bundle: " + initialBundleInfo.getLocation().toExternalForm() + " with startlevel " + initialBundleInfo.getLevel());
            }

            Bundle bundle = bundleContext.installBundle(initialBundleInfo.getLocation().toString());
            bundle.adapt(BundleStartLevel.class).setStartLevel(initialBundleInfo.getLevel());
            if (initialBundleInfo.shouldStart()) {
                bundle.start();
            }
        }
    }

    private boolean isFrameworkActive() {
        return framework != null && (framework.getState() == Bundle.ACTIVE || framework.getState() == Bundle.STARTING);
    }

    private void dispatchEvent(int event) {
        CarbonServerEvent carbonServerEvent = new CarbonServerEvent(event, config);
        for (CarbonServerListener listener : config.getCarbonServerListeners()) {
            if (logger.isLoggable(Level.FINE)) {
                String eventName = (event == CarbonServerEvent.STARTING) ? "STARTING" : "STOPPING";
                logger.log(Level.FINE, "Dispatching " + eventName + " event to " + listener.getClass().getName());
            }
            listener.notify(carbonServerEvent);
        }
    }
}
