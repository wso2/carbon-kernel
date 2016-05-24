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
import org.wso2.carbon.kernel.internal.startupresolver.beans.StartupComponent;
import org.wso2.carbon.kernel.utils.manifest.ManifestElement;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.
        capabilityProviderElementPredicate;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.
        logPendingCapabilityProviderServiceDetails;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.logPendingComponentDetails;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.
        logPendingRequiredCapabilityListenerServiceDetails;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolverUtils.
        requiredCapabilityListenerElementPredicate;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OSGI_SERVICE_COMPONENT;
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
        name = "org.wso2.carbon.kernel.internal.startupresolver.StartupOrderResolver",
        immediate = true
)
public class StartupOrderResolver {
    private static final Logger logger = LoggerFactory.getLogger(StartupOrderResolver.class);

    private StartupComponentManager startupComponentManager = new StartupComponentManager();

    private OSGiServiceCapabilityTracker osgiServiceTracker;

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
            processManifestHeaders(Arrays.asList(bundleContext.getBundles()));

            // 2) Register capability trackers to get notified when required capabilities are available.
            startCapabilityTrackers();

            // 3) Schedule a time task to check for startup components with zero pending required capabilities.
            scheduleCapabilityListenerTimer();

            // 4) Start a timer task to track pending capabilities, pending CapabilityProvider services,
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
     * @param bundleList list of bundles to be scanned for Provide-Capability headers.
     */
    private void processManifestHeaders(List<Bundle> bundleList) {
        Map<String, List<ManifestElement>> groupedManifestElements =
                bundleList.stream()
                        // Filter out all the bundles with the Carbon-Component manifest header.
                        .filter(StartupOrderResolverUtils::isCarbonComponentHeaderPresent)
                        // Process filtered manifest headers and get a list of ManifestElements.
                        .map(StartupOrderResolverUtils::getManifestElements)
                        // Merge all the manifest elements lists into a single list.
                        .flatMap(Collection::stream)
                        // Partition all the ManifestElements with the manifest header name.
                        .collect(Collectors.groupingBy(ManifestElement::getValue));

        if (groupedManifestElements.get(STARTUP_LISTENER_COMPONENT) != null) {
            processServiceComponents(groupedManifestElements);
        }

        if (groupedManifestElements.get(OSGI_SERVICE_COMPONENT) != null) {
            processCapabilityProviders(groupedManifestElements.get(OSGI_SERVICE_COMPONENT));
            processOSGiServices(groupedManifestElements.get(OSGI_SERVICE_COMPONENT));
        }

        // You can add logic to handle other types of provide capabilities here.
        // e.g. custom manifest headers, config files etc.
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

                if (startupComponentManager.getComponents(StartupComponent::isPending).size() == 0) {
                    startupComponentManager.notifySatisfiableComponents();

                    logger.debug("All the StartupComponents are satisfied. Cancelling the capabilityListenerTimer");

                    CarbonStartupHandler.logServerStartupTime();
                    CarbonStartupHandler.registerCarbonServerInfoService();

                    capabilityListenerTimer.cancel();
                    capabilityListenerTimer = null;
                    stopCapabilityTrackers();

                    logger.debug("Complete - Startup Order Resolver.");
                    return;
                }

                startupComponentManager.notifySatisfiableComponents();
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
                        startupComponentManager.getComponents(StartupComponent::isPending);

                if (pendingComponents.size() == 0) {
                    logger.debug("All the RequiredCapabilityListeners are notified, " +
                            "therefore cancelling the pendingCapabilityTimer");
                    pendingCapabilityTimer.cancel();
                    startupComponentManager = null;
                    pendingCapabilityTimer = null;
                    return;
                }

                // Report pending startup component details.
                logPendingComponentDetails(logger, pendingComponents);


                // Report pending RequiredCapabilityListener details.
                logPendingRequiredCapabilityListenerServiceDetails(logger,
                        startupComponentManager.getComponents(
                                startupComponent -> startupComponent.getListener() == null));

                // Report pending CapabilityProvider details.
                logPendingCapabilityProviderServiceDetails(logger,
                        startupComponentManager.getPendingCapabilityProviders());
            }

        }, pendingCapabilityTimerDelay, pendingCapabilityTimerPeriod);
    }

    private void processServiceComponents(Map<String, List<ManifestElement>> groupedManifestElements) {
        groupedManifestElements.get(STARTUP_LISTENER_COMPONENT)
                .stream()
                .map(StartupOrderResolverUtils::getStartupComponent)
                .forEach(startupComponentManager::addStartupComponent);
    }

    /**
     * Process all the Provide-Capability Manifest header elements to get a list of required Capabilities.
     * <p>
     * At the moment this methods process manifest elements with the namespace osgi.service.
     *
     * @param manifestElementList manifest elements by the header name.
     */
    private void processOSGiServices(List<ManifestElement> manifestElementList) {
        manifestElementList
                .stream()
                .filter(capabilityProviderElementPredicate.negate().and(
                        requiredCapabilityListenerElementPredicate.negate()))
                // Creating a Capability from the manifestElement
                .map(StartupOrderResolverUtils::getOSGiServiceCapabilities)
                .flatMap(Collection::stream)
                .forEach(serviceCapability -> {
                    if (!serviceCapability.getRequiredByComponentNames().isEmpty()) {
                        serviceCapability.getRequiredByComponentNames()
                                .forEach(componentName ->
                                        startupComponentManager.addRequiredOSGiServiceToComponent(
                                                componentName,
                                                serviceCapability.getName()));
                    }

                    startupComponentManager.addExpectedOrAvailableCapability(serviceCapability);
                });
    }

    /**
     * @param manifestElementList A list of {@code ManifestElement}
     */
    private void processCapabilityProviders(List<ManifestElement> manifestElementList) {
        manifestElementList.stream()
                .filter(capabilityProviderElementPredicate)
                .map(StartupOrderResolverUtils::getCapabilityProviderCapability)
                .forEach(startupComponentManager::addExpectedOrAvailableCapabilityProvider);
    }

    /**
     * Starts all the capability trackers.
     */
    private void startCapabilityTrackers() {
        // Start the OSGi service capability tracker.
        osgiServiceTracker = new OSGiServiceCapabilityTracker(startupComponentManager);
        osgiServiceTracker.startTracker();

        // Likewise you can register trackers for other types of capabilities.
    }

    /**
     * Stops all the capability trackers.
     */
    private void stopCapabilityTrackers() {
        // Stop the OSGi service capability tracker.
        if (osgiServiceTracker != null) {
            osgiServiceTracker.closeTracker();
            osgiServiceTracker = null;
        }
    }
}
