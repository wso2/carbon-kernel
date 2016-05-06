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
package org.wso2.carbon.kernel.internal.startupresolver;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.config.model.CarbonConfiguration;
import org.wso2.carbon.kernel.internal.CarbonStartupHandler;
import org.wso2.carbon.kernel.internal.DataHolder;
import org.wso2.carbon.kernel.internal.startupresolver.beans.Capability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.CapabilityProviderCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.OSGiServiceCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.StartupComponent;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.utils.manifest.ManifestElement;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.extractManifestElements;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.getManifestElementAttribute;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.getObjectClassName;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.isSupportedManifestHeaderExists;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CARBON_COMPONENT_HEADER;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.DEPENDENT_COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OSGI_SERVICE_COMPONENT;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.SERVICE_COUNT;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.STARTUP_LISTENER_COMPONENT;


/**
 * {@code StartupOrderResolver} handles carbon component startup complexities. Here are two such cases.
 * <p>
 * 1) In a Carbon base product, certain components must be initialized first before certain other components.
 * <p>
 * e.g. Deployment Engine must be initialized and all the deployers must be initiated before starting transports
 * in a Carbon based product.
 * <p>
 * 2) A Carbon components needs to know whether all required services are registered as OSGi services. This is not
 * possible in a standard OSGi containers.
 * <p>
 * e.g. A Transport Manager starts transports all at once
 *
 * @since 5.0.0
 */
@Component(
        name = "org.wso2.carbon.kernel.internal.startupresolver.RequireCapabilityCoordinator",
        immediate = true
)
public class StartupOrderResolver {
    private static final Logger logger = LoggerFactory.getLogger(StartupOrderResolver.class);

    private static final List<String> supportedManifestHeaders = new ArrayList<>();

    static {
        supportedManifestHeaders.add(CARBON_COMPONENT_HEADER);
    }

    private StartupComponentManager startupComponentManager = new StartupComponentManager();

    private OSGiServiceCapabilityTracker osgiServiceTracker = new OSGiServiceCapabilityTracker(startupComponentManager);

    private Timer capabilityListenerTimer = new Timer();

    private Timer pendingCapabilityTimer = new Timer();

    /**
     * Process Provide-Capability headers and populate a counter which keep all the expected service counts. Register
     * timers to track the service availability as well as pending service registrations.
     * <p>
     * If there are no RequireCapabilityListener instances then this method returns.
     *
     * @param bundleContext OSGi bundle context of the Carbon.core bundle
     * @throws Exception if the service component activation fails
     */
    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        try {

            logger.debug("Initialize - Startup Order Resolver.");

            // 1) Process OSGi manifest headers to calculate the expected list required capabilities.
            processManifestHeaders(Arrays.asList(bundleContext.getBundles()), supportedManifestHeaders);

            // 2) Check for any startup components with pending required capabilities.
            if (startupComponentManager.getPendingComponents().size() == 0) {
                // There are no registered RequiredCapabilityListener
                // Clear all the populated maps.
                startupComponentManager = null;
                osgiServiceTracker = null;
                return;
            }

            // 3) Register capability trackers to get notified when required capabilities are available.
            startCapabilityTrackers();

            // 4) Schedule a time task to check for startup components with zero pending required capabilities.
            scheduleCapabilityListenerTimer();

            // 5) Start a timer to track pending capabilities, pending CapabilityProvider services,
            // pending RequiredCapabilityLister services.
            schedulePendingCapabilityTimerTask();

        } catch (Throwable e) {
            logger.error("Error occurred in Startup Order Resolver.", e);
        }
    }

    @Deactivate
    public void stop(BundleContext bundleContext) throws Exception {
        logger.debug("Deactivating startup resolver component available in bundle {}",
                bundleContext.getBundle().getSymbolicName());
    }

    /**
     * Process supported manifest headers (Startup-Component and Provide-Capability).
     * <p>
     * Process Startup-Component headers and create StartupComponent instances.
     * <p>
     * Process Provide-Capability headers to calculate the expected number of required capabilities.
     * <p>
     * Process Provide-Capability headers to get a list of CapabilityProviders and RequiredCapabilityListeners.
     *
     * @param bundleList               list of bundles to be scanned for Provide-Capability headers.
     * @param supportedManifestHeaders list of manifest headers processed by this method.
     */
    private void processManifestHeaders(List<Bundle> bundleList, List<String> supportedManifestHeaders) {
        Map<String, List<ManifestElement>> groupedManifestElements = bundleList
                .stream()
                // Filter out all the supported manifest headers.
                .filter(bundle -> AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                        isSupportedManifestHeaderExists(bundle, supportedManifestHeaders)))
                // Process filtered manifest headers and get a list of ManifestElements.
                .flatMap(bundle -> extractManifestElements(bundle, supportedManifestHeaders).stream())
                // Partition all the ManifestElements with the manifest header name.
                .collect(Collectors.groupingBy(ManifestElement::getValue));

        if (groupedManifestElements.get(STARTUP_LISTENER_COMPONENT) != null) {
            processStartupComponents(groupedManifestElements.get(STARTUP_LISTENER_COMPONENT));
        }

        if (groupedManifestElements.get(OSGI_SERVICE_COMPONENT) != null) {
            processOSGiServiceCapabilities(groupedManifestElements.get(OSGI_SERVICE_COMPONENT));
        }

        // You can add logic to handle other types of provide capabilities here.
        // e.g. custom manifest headers, config files etc.
    }

    /**
     * Starts all the capability trackers.
     */
    private void startCapabilityTrackers() {
        // Start the OSGi service capability tracker.
        osgiServiceTracker.startTracker();

        // Likewise you can register trackers for other types of capabilities.
    }

    /**
     * Schedule a timer task to monitor satisfiable CapabilityListeners.
     */
    private void scheduleCapabilityListenerTimer() {
        CarbonConfiguration carbonConfiguration = DataHolder.getInstance().getCarbonRuntime().getConfiguration();
        long capabilityListenerTimerDelay = carbonConfiguration.getStartupResolverConfig().
                getCapabilityListenerTimer().getDelay();
        long capabilityListenerTimerPeriod = carbonConfiguration.getStartupResolverConfig().
                getCapabilityListenerTimer().getPeriod();

        capabilityListenerTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (startupComponentManager.getPendingComponents().size() == 0) {
                    notifySatisfiableComponents();

                    logger.debug("All the StartupComponents are satisfied. Cancelling the capabilityListenerTimer");

                    CarbonStartupHandler.logServerStartupTime();
                    CarbonStartupHandler.registerCarbonServerInfoService();

                    capabilityListenerTimer.cancel();
                    osgiServiceTracker.closeTracker();

                    logger.debug("Complete - Startup Order Resolver.");
                    return;
                }

                notifySatisfiableComponents();

            }
        }, capabilityListenerTimerDelay, capabilityListenerTimerPeriod);
    }

    private void schedulePendingCapabilityTimerTask() {
        CarbonConfiguration carbonConfiguration = DataHolder.getInstance().getCarbonRuntime().getConfiguration();
        long pendingCapabilityTimerDelay = carbonConfiguration.getStartupResolverConfig().
                getPendingCapabilityTimer().getDelay();
        long pendingCapabilityTimerPeriod = carbonConfiguration.getStartupResolverConfig().
                getPendingCapabilityTimer().getPeriod();

        pendingCapabilityTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                List<StartupComponent> pendingComponents =
                        startupComponentManager.getPendingComponents();

                if (pendingComponents.size() == 0) {
                    logger.debug("All the RequiredCapabilityListeners are notified, " +
                            "therefore cancelling the pendingCapabilityTimer");
                    pendingCapabilityTimer.cancel();
                    return;
                } else {
                    pendingComponents
                            .forEach(startupComponent -> {
                                List<Capability> pendingCapabilityList =
                                        startupComponentManager.getPendingProvideCapabilityList(
                                                startupComponent.getName());
                                pendingCapabilityList
                                        .forEach(provideCapability ->
                                                logger.warn("Startup component {} from bundle({}:{}) will be in the " +
                                                                "pending state until Capability {} from " +
                                                                "bundle({}:{}) is available",
                                                        startupComponent.getName(),
                                                        startupComponent.getBundle().getSymbolicName(),
                                                        startupComponent.getBundle().getVersion(),
                                                        provideCapability.getName(),
                                                        provideCapability.getBundle().getSymbolicName(),
                                                        provideCapability.getBundle().getVersion()));

                            });
                }

                // Check for pending RequiredCapabilityListeners
                List<StartupComponent> pendingComponentList =
                        startupComponentManager.getPendingRequiredCapabilityListeners();

                pendingComponentList
                        .forEach(startupComponent -> logger.warn("Waiting for RequiredCapabilityListener " +
                                        "OSGi Service from bundle({}:{}). component-key: {}",
                                startupComponent.getBundle().getSymbolicName(),
                                startupComponent.getBundle().getVersion(),
                                startupComponent.getName()));

                // Check for pending CapabilityProviders
                List<CapabilityProviderCapability> pendingCapabilityProviderList =
                        startupComponentManager.getPendingCapabilityProviderList();

                pendingCapabilityProviderList
                        .forEach(capabilityProvider -> logger.warn("Waiting for CapabilityProvider OSGi service " +
                                        "from bundle({}:{}). Provided capability name: {} ",
                                capabilityProvider.getBundle().getSymbolicName(),
                                capabilityProvider.getBundle().getVersion(),
                                capabilityProvider.getProvidedCapabilityName()));
            }
        }, pendingCapabilityTimerDelay, pendingCapabilityTimerPeriod);
    }

    /**
     * Process all the Startup-Component manifest header elements. Creates {@code StartupComponent} instances
     * for each and every ManifestElement.
     *
     * @param manifestElementList a list of {@code ManifestElement} whose header name is Startup-Component.
     */
    private void processStartupComponents(List<ManifestElement> manifestElementList) {
        // Create StartupComponents from the manifest elements.
        List<StartupComponent> startupComponentList = manifestElementList
                .stream()
                .map(StartupOrderResolverUtils::getStartupComponentBean)
                .collect(Collectors.toList());
        startupComponentManager.addComponents(startupComponentList);
    }

    /**
     * Process all the Provide-Capability Manifest header elements to get a list of required Capabilities.
     * <p>
     * At the moment this methods process manifest elements with the namespace osgi.service.
     *
     * @param manifestElementList partitioned manifest elements by the header name.
     */
    private void processOSGiServiceCapabilities(List<ManifestElement> manifestElementList) {
        // Handle CapabilityProvider OSGi services
        processCapabilityProviders(manifestElementList);

        // Handle other OSGi service capabilities
        manifestElementList
                .stream()
                .filter(manifestElement -> !CapabilityProvider.class.getName().equals(
                        getObjectClassName(manifestElement)) &&
                        !RequiredCapabilityListener.class.getName().equals(getObjectClassName(manifestElement)))
                // Creating a Capability from the manifestElement
                .flatMap(manifestElement -> {
                    String capabilityName = getObjectClassName(manifestElement);

                    String serviceCountStr = getManifestElementAttribute(SERVICE_COUNT, manifestElement, false);

                    int serviceCount = 1;
                    if (serviceCountStr != null) {
                        try {
                            serviceCount = Integer.parseInt(serviceCountStr.trim());
                        } catch (NumberFormatException e) {
                            throw new StartOrderResolverException("Invalid value for serviceCount manifest attribute " +
                                    "in bundle(" + manifestElement.getBundle().getSymbolicName() + ":" +
                                    manifestElement.getBundle().getVersion() + ")", e);
                        }
                    }

                    // Create specifed  number of OSGi service components and adding them to a list.
                    List<OSGiServiceCapability> osgiServiceCapabilityList = new ArrayList<>(serviceCount);
                    IntStream.range(0, serviceCount)
                            .forEach(count -> {
                                OSGiServiceCapability osgiServiceCapability = new OSGiServiceCapability(capabilityName,
                                        Capability.CapabilityType.OSGi_SERVICE, manifestElement.getBundle());

                                // Check whether a dependent-component-key or dependent-component-name
                                // property is specified.
                                String dependentComponentName = getManifestElementAttribute(
                                        DEPENDENT_COMPONENT_NAME, manifestElement, false);

                                if (dependentComponentName != null && !dependentComponentName.equals("")) {
                                    osgiServiceCapability.setDependentComponentName(dependentComponentName.trim());
                                }
                                osgiServiceCapabilityList.add(osgiServiceCapability);
                            });

                    return osgiServiceCapabilityList.stream();
                })
                .forEach(capability -> {
                    if (capability.getDependentComponentName() != null) {
                        startupComponentManager.addRequiredOSGiServiceCapabilityToComponent(
                                capability.getDependentComponentName(), capability.getName());
                    }
                    startupComponentManager.addExpectedRequiredCapability(capability);
                });


    }

    private void processCapabilityProviders(List<ManifestElement> manifestElementList) {

        manifestElementList
                .stream()
                .filter(manifestElement -> CapabilityProvider.class.getName().equals(
                        getObjectClassName(manifestElement)))
                .map(manifestElement -> {
                    // Processing CapabilityProvider OSGi service
                    String providedCapabilityName = getManifestElementAttribute(
                            CAPABILITY_NAME, manifestElement, false);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating a CapabilityProviderCapability from manifest element in " +
                                        "bundle({}:{}), with CapabilityName - {}. ",
                                manifestElement.getBundle().getSymbolicName(),
                                manifestElement.getBundle().getVersion(),
                                providedCapabilityName);
                    }

                    return new CapabilityProviderCapability(getObjectClassName(manifestElement),
                            Capability.CapabilityType.OSGi_SERVICE, providedCapabilityName,
                            manifestElement.getBundle());
                })
                .forEach(capability -> startupComponentManager.addExpectedCapabilityProvider(capability));

    }

    private void notifySatisfiableComponents() {
        startupComponentManager.getSatisfiableComponents()
                .forEach(startupComponent -> {
                    String componentName = startupComponent.getName();

                    synchronized (componentName.intern()) {
                        startupComponentManager.removeSatisfiedComponent(startupComponent);
                        RequiredCapabilityListener capabilityListener = startupComponent.getListener();

                        if (logger.isDebugEnabled()) {
                            logger.debug("Notifying RequiredCapabilityListener of component {} from bundle({}:{}) " +
                                            "since all the required capabilities are available",
                                    componentName,
                                    startupComponent.getBundle().getSymbolicName(),
                                    startupComponent.getBundle().getVersion());
                        }

                        capabilityListener.onAllRequiredCapabilitiesAvailable();
                    }

                });
    }
}
