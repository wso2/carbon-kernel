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
package org.wso2.carbon.launcher;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.wso2.carbon.launcher.bootstrap.logging.BootstrapLogger;
import org.wso2.carbon.launcher.config.CarbonInitialBundle;
import org.wso2.carbon.launcher.config.CarbonLaunchConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.wso2.carbon.launcher.Constants.CARBON_START_TIME;

/**
 * Launches a Carbon instance.
 *
 * @since 5.0.0
 */
public class CarbonServer {

    private static final Logger logger = BootstrapLogger.getCarbonLogger(CarbonServer.class.getName());

    private CarbonLaunchConfig config;
    private Framework framework;
    private ServerStatus serverStatus;

    /**
     * Constructor.
     *
     * @param config Carbon launcher configuration
     */
    public CarbonServer(CarbonLaunchConfig config) {
        this.config = config;
    }

    /**
     * Starts a Carbon server instance. This method returns only after the server instance stops completely.
     *
     * @throws Exception if error occurred
     */
    public void start() throws Exception {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Starting Carbon server instance.");
        }

        // Sets the server start time.
        System.setProperty(CARBON_START_TIME, Long.toString(System.currentTimeMillis()));

        try {
            // Creates an OSGi framework instance.
            ClassLoader fwkClassLoader = createOSGiFwkClassLoader();
            FrameworkFactory fwkFactory = loadOSGiFwkFactory(fwkClassLoader);
            framework = fwkFactory.newFramework(config.getProperties());

            setServerCurrentStatus(ServerStatus.STARTING);
            // Notify Carbon server start.
            dispatchEvent(CarbonServerEvent.STARTING);

            // Initialize and start OSGi framework.
            initAndStartOSGiFramework(framework);

            // Loads initial bundles listed in the launch.properties file.
            loadInitialBundles(framework.getBundleContext());

            setServerCurrentStatus(ServerStatus.STARTED);
            // This thread waits until the OSGi framework comes to a complete shutdown.
            waitForServerStop(framework);

            setServerCurrentStatus(ServerStatus.STOPPING);
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
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes and start framework. Framework will try to resolve all the bundles if their requirements
     * can be satisfied.
     *
     * @param framework osgiFramework
     * @throws BundleException
     */
    private void initAndStartOSGiFramework(Framework framework) throws BundleException {
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

        ServiceLoader<FrameworkStartupHook> frameworkStartupHookLoader
                = ServiceLoader.load(FrameworkStartupHook.class);

        frameworkStartupHookLoader.forEach((frameworkStartupHook)
                -> frameworkStartupHook.systemBundleStarted(framework.getBundleContext()));

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "SPI class processor service registered successfully.");
        }
    }

    /**
     * Wait until this Framework has completely stopped.
     *
     * @param framework OSGi framework
     * @throws java.lang.Exception
     */
    private void waitForServerStop(Framework framework) throws Exception {
        if (!isFrameworkActive()) {
            return;
        }

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

    /**
     * Create OSGi framework class loader.
     *
     * @return new OSGi class loader
     */
    private ClassLoader createOSGiFwkClassLoader() {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Creating OSGi framework class loader.");
        }

        URL exten = null;
        try {
            exten = new URL("file:///home/mirage/WSO2/carbon-kernel/carbon-spi/carbon-spi-hook/target/carbon-spi-hook-5.1.0-SNAPSHOT.jar");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        URL fwkBundleURL = config.getCarbonOSGiFramework();
        return new URLClassLoader(new URL[]{fwkBundleURL, exten});
    }

    /**
     * Creates a new service loader for the given service type and class loader.
     * Load OSGi framework factory for the given class loader.
     *
     * @param classLoader The class loader to be used to load provider-configurations
     * @return framework factory for creating framework instances
     */
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

    /**
     * Installs a bundle from the specified locations.
     *
     * @param bundleContext bundle's execution context within the Framework
     * @throws BundleException
     */
    private void loadInitialBundles(BundleContext bundleContext) throws BundleException {
        //Setting this property due to an issue with equinox simple configurator where it tries to uninstall bundles
        //which are loaded from initial bundle list.
        System.setProperty(Constants.EQUINOX_SIMPLE_CONFIGURATOR_EXCLUSIVE_INSTALLATION, "false");

        for (CarbonInitialBundle initialBundleInfo : config.getInitialBundles()) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Loading initial bundle: " + initialBundleInfo.getLocation().toExternalForm() +
                        " with startlevel " + initialBundleInfo.getLevel());
            }

            Bundle bundle = bundleContext.installBundle(initialBundleInfo.getLocation().toString());
            if (initialBundleInfo.shouldStart()) {
                bundle.start();
            }
        }
    }

    /**
     * Check if framework is active.
     *
     * @return true if framework is in active or starting state, false otherwise
     */
    private boolean isFrameworkActive() {
        return framework != null
                && (framework.getState() == Framework.ACTIVE || framework.getState() == Framework.STARTING);
    }

    /**
     * set status of Carbon server.
     */
    private void setServerCurrentStatus(ServerStatus status) {
        serverStatus = status;
    }

    /**
     * Check status of Carbon server.
     *
     * @return Server status
     */
    public ServerStatus getServerCurrentStatus() {
        return serverStatus;
    }

    /**
     * Notify Carbon server listeners about the given event.
     *
     * @param event number to notify
     */
    private void dispatchEvent(int event) {
        CarbonServerEvent carbonServerEvent = new CarbonServerEvent(event, config);
        config.getCarbonServerListeners().forEach(listener -> {
            if (logger.isLoggable(Level.FINE)) {
                String eventName = (event == CarbonServerEvent.STARTING) ? "STARTING" : "STOPPING";
                logger.log(Level.FINE, "Dispatching " + eventName + " event to " + listener.getClass().getName());
            }
            listener.notify(carbonServerEvent);
        });
    }
}
