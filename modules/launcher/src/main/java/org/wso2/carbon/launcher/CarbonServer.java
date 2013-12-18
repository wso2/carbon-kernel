package org.wso2.carbon.launcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.wso2.carbon.launcher.config.CarbonInitialBundle;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;

import static org.wso2.carbon.launcher.utils.Constants.*;

/**
 * Launches a Carbon instance
 */
public class CarbonServer {

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

        // Sets the server start time.
        System.setProperty(CARBON_START_TIME, Long.toString(System.currentTimeMillis()));

        try {
            // Creates an OSGi framework instance
            ClassLoader fwkClassLoader = createOSGiFwkClassLoader();
            FrameworkFactory fwkFactory = loadOSGiFwkFactory(fwkClassLoader);
            framework = fwkFactory.newFramework(config);

            // Notify Carbon server start
            dispatchEvent(CarbonServerEvent.STARTING);

            // Initializes the framework. Framework will try to resolve all the bundles if their requirements
            //  can be satisfied.
            framework.init();

            // Starts the framework.
            framework.start();

            // Loads initial bundles listed in the launch.properties file.
            loadInitialBundles(framework.getBundleContext());

            // This thread waits until the OSGi framework comes to complete shutdown.
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

        // Framework.stop() method returns before the framework shutdown. But this.stop() method should only return
        //  after framework stops completely. Therefore we invokes the framework.stop() method in a new
        //  thread and this thread waits till the framework stops completely using the framework.waitForStop() method.
        new Thread() {
            public void run() {
                try {
//                    System.out.println("$$$$ Stopping OSGi runtime.");
                    framework.stop();
//                    System.out.println("$$$$ Stopping OSGi runtime - 1");
                } catch (BundleException e) {
//                    System.err.println("Error stopping the framework: " + e.getMessage());
                }
            }
        }.start();


        try {
            FrameworkEvent event = framework.waitForStop(1000 * 60 * 3);
            if (event.getType() == FrameworkEvent.WAIT_TIMEDOUT) {
//                System.out.println("$$$$ Framework did not stop before the wait timeout expired. " +
//                        "Forcefully shutting down the server " + event.getType());
                return;
            }
//            System.out.println("$$$$ Stopped the OSGi runtime " + event.getType());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitForServerStop() throws Exception {
        if (!isFrameworkActive()) {
            return;
        }

        while (true) {
            FrameworkEvent event = framework.waitForStop(0);

            // We should not stop the framework if the user has updated the system bundle via the OSGi console or
            //  programmatically. In this case, framework will shutdown and start itself.
            if (event.getType() != FrameworkEvent.STOPPED_UPDATE) {
//                System.out.println("$$$$ Framework Stopped....." + event.getType());
                break;
            }
        }
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

    private boolean isFrameworkActive() {
        return framework != null && (framework.getState() == Bundle.ACTIVE || framework.getState() == Bundle.STARTING);
    }

    private void dispatchEvent(int event) {
        CarbonServerEvent carbonServerEvent = new CarbonServerEvent(event, config);
        for (CarbonServerListener listener : config.getCarbonServerListeners()) {
            listener.carbonServerEvent(carbonServerEvent);
        }
    }
}
