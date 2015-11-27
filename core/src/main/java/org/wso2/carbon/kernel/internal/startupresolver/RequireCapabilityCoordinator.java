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

import org.eclipse.osgi.util.ManifestElement;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.internal.DataHolder;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * RequireCapabilityCoordinator handles carbon component startup complexities. Here are two such cases
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
public class RequireCapabilityCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(RequireCapabilityCoordinator.class);

    private static final String PROVIDE_CAPABILITY = "Provide-Capability";
    private static final String CAPABILITY_NAME = "capability-name";

    private AtomicInteger requiredCapabilityListenerCount = new AtomicInteger(0);
    private Map<String, RequiredCapabilityListener> listenerMap = new ConcurrentHashMap<>();
    private Map<String, ServiceTracker> capabilityTrackerMap = new ConcurrentHashMap<>();
    private MultiCounter<String> capabilityCounter = new MultiCounter<>();
    private MultiCounter<String> capabilityProviderCounter = new MultiCounter<>();

    private Timer checkServiceAvailabilityTimer = new Timer();
    private Timer pendingServiceTimer = new Timer();


    /**
     * Process Provide-Capability headers and populate a counter which keep all the expected service counts. Register
     * timers to track the service availability as well as pending service registrations.
     *
     * If there are no RequireCapabilityListener instances then this method returns.
     *
     * @param bundleContext OSGi bundle context of the Carbon.core bundle
     * @throws Exception if the service component activation fails
     */
    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        try {
            // 1) Process all Provide-Capability headers and get the provided OSGi services and the count.
            Arrays.asList(bundleContext.getBundles())
                    .parallelStream()
                    .filter(new ProvideCapabilityHeaderFilter<>())
                    .forEach(new ProvideCapabilityHeaderConsumer<>());

            // 2) Check whether there at least one expect RequireCapabilityLister. IF there is none, then simply return.
            if (requiredCapabilityListenerCount.get() == 0) {
                // Clear all the populated maps.
                capabilityCounter = null;
                listenerMap = null;
                return;
            }

            // 3) Schedule a timer to tack service registrations which have happened before populating the counter as
            //      well as to clear all the listeners with zero available services in the runtime.
            // TODO find a way to stop these timers and make this timer task configurable from the carbon.yaml
            checkServiceAvailabilityTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    listenerMap.keySet()
                            .stream()
                            .filter(key -> capabilityCounter.get(key) == 0 && listenerMap.get(key) != null &&
                                    capabilityProviderCounter.get(key) == 0)
                            .forEach(key -> {
                                synchronized (key.intern()) {
                                    logger.debug("Invoking listener ({}) as all its required capabilities are " +
                                            "available for ({})", listenerMap.get(key).getClass().getName(), key);
                                    listenerMap.remove(key).onAllRequiredCapabilitiesAvailable();
                                    closeCapabilityTracker(key);
                                }
                            });
                }
            }, 200, 200);

            // 4) Start a timer to track pending service registrations.
            pendingServiceTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    listenerMap.keySet()
                            .stream()
                            .forEach(key -> {
                                synchronized (key.intern()) {
                                    if (capabilityProviderCounter.get(key) == 0 && capabilityCounter.get(key) > 0) {
                                        logger.warn("Waiting on pending capability registration for ({})", key);
                                    } else if (capabilityProviderCounter.get(key) > 0) {
                                        logger.warn("Waiting on pending capability provider registration for ({})",
                                                key);
                                    }
                                }
                            });
                }
            }, 60000, 30000);
        } catch (Throwable e) {
            logger.error("Error occurred while processing Provide-Capability manifest headers", e);
            throw e;
        }
    }

    @Deactivate
    public void stop(BundleContext bundleContext) throws Exception {
    }

    /**
     * Register RequireCapabilityListener instance as and when they are available.
     *
     * First extract the required OSGi service interface from the service properties and then register a
     * {@link ServiceTracker} to track required OSGi services.
     *
     * @param listener an instance of the RequireCapabilityListener interface.
     * @param propertyMap OSGi service properties registered with the listener.
     */
    @Reference(
            name = "require.capability.listener.service",
            service = RequiredCapabilityListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "deregisterRequireCapabilityListener"
    )
    public void registerRequireCapabilityListener(RequiredCapabilityListener listener,
                                                  Map<String, String> propertyMap) {

        String requiredServiceKey = propertyMap.get(CAPABILITY_NAME);
        if (requiredServiceKey == null || requiredServiceKey.equals("")) {
            logger.warn("RequireCapabilityListener service ({}) does not contain the proper " +
                            "capability-name name",
                    listener.getClass().getName());
            return;
        } else {
            requiredServiceKey = requiredServiceKey.trim();
        }

        logger.debug("Updating listenerMap for ({}), from ({})", requiredServiceKey, listener.getClass().getName());

        listenerMap.put(requiredServiceKey, listener);

        BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            final String serviceClazz = requiredServiceKey;
            ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<>(
                    bundleContext,
                    requiredServiceKey,
                    new ServiceTrackerCustomizer<Object, Object>() {
                        @Override
                        public Object addingService(ServiceReference<Object> reference) {
                            synchronized (serviceClazz.intern()) {
                                int count = capabilityCounter.decrementAndGet(serviceClazz);
                                logger.debug("Decrementing count for ({}) from serviceTracker on required " +
                                                "capability registration from ({}). Current count is {}",
                                        serviceClazz, listener.getClass().getName(), count);
                            }
                            return bundleContext.getService(reference);
                        }

                        @Override
                        public void modifiedService(ServiceReference<Object> reference, Object service) {

                        }

                        @Override
                        public void removedService(ServiceReference<Object> reference, Object service) {

                        }
                    });
            serviceTracker.open();
            capabilityTrackerMap.put(requiredServiceKey, serviceTracker);
        }
    }

    public void deregisterRequireCapabilityListener(RequiredCapabilityListener listener,
                                                    Map<String, String> propertyMap) {
    }

    /**
     * Closes the service tracker instance opened for the given capability name.
     *
     * @param capabilityName the key in which capability service trackers are mapped against.
     */
    private void closeCapabilityTracker(String capabilityName) {
        if (capabilityTrackerMap.containsKey(capabilityName) && capabilityTrackerMap.get(capabilityName) != null) {
            logger.debug("Closing service tracker instance for ({}) capability", capabilityName);
            capabilityTrackerMap.remove(capabilityName).close();
        } else {
            logger.warn("A service tracker instance is not found for ({}) capability", capabilityName);
        }
    }

    /**
     * Registers and updates the CapabilityCounter with the CapabilityName and the CapabilityCount value.
     * The CapabilityName is used as the key and the integer value of the capability count that startup
     * coordinator should wait before calling the onAllRequiredCapabilitiesAvailable callback method
     * of an interested listener.
     *
     * @param provider an instance of the CapabilityProvider when it is registered as an OSGi service.
     */
    @Reference(
            name = "capability.provider.service",
            service = CapabilityProvider.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterCapabilityProvider"
    )
    public void registerCapabilityProvider(CapabilityProvider provider, Map<String, String> propertyMap) {

        String dynamicCapabilityName = propertyMap.get(CAPABILITY_NAME);
        if (dynamicCapabilityName == null || dynamicCapabilityName.equals("")) {
            logger.warn("CapabilityProvider service ({}) does not contain the capability name",
                    provider.getClass().getName());
        } else {
            final String capabilityName = dynamicCapabilityName.trim();
            logger.debug("Updating CapabilityCounter with Capability-Name : ({}) , Capability-Count : {}",
                    capabilityName, provider.getCount());
            int providerCount = capabilityProviderCounter.decrementAndGet(capabilityName);
            logger.debug("Current provider count for ({}) capability is {}", capabilityName, providerCount);
            IntStream.range(0, provider.getCount()).forEach(
                    count -> {
                        int currentCount = capabilityCounter.incrementAndGet(capabilityName);
                        logger.debug("Current count for ({}) capability is {}", capabilityName, currentCount);
                    }
            );
        }
    }

    public void unregisterCapabilityProvider(CapabilityProvider provider) {

    }

    /**
     * Implementation of the {@link Predicate} interface which filters OSGi manifest header with key Provide-Capability.
     *
     * @param <T> OSGi bundle
     */
    private static class ProvideCapabilityHeaderFilter<T extends Bundle> implements Predicate<T> {
        @Override
        public boolean test(T bundle) {
            return AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                    bundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY) != null);
        }
    }

    /**
     * Implementation of the {@link Consumer} interface which populates capability counter.
     *
     * @param <T> OSGi bundle
     */
    private class ProvideCapabilityHeaderConsumer<T extends Bundle> implements Consumer<T> {
        @Override
        public void accept(T bundle) {
            String headerValue = AccessController.doPrivileged((PrivilegedAction<String>) () ->
                    bundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY));
            try {
                ManifestElement[] elements = ManifestElement.parseHeader(PROVIDE_CAPABILITY, headerValue);
                Arrays.asList(elements)
                        .stream()
                        .filter(element -> "osgi.service".equals(element.getValue()))
                        .forEach(element -> {
                            if (RequiredCapabilityListener.class.getName().
                                    equals(element.getAttribute("objectClass"))) {
                                logger.debug("Adding Capability-Listener ({}) to watch list from bundle ({})",
                                        element.getAttribute("objectClass"), bundle.getSymbolicName());
                                requiredCapabilityListenerCount.incrementAndGet();
                            } else if (CapabilityProvider.class.getName().equals(element.getAttribute("objectClass"))) {
                                String capability = element.getAttribute(CAPABILITY_NAME);
                                if (capability != null && !capability.isEmpty()) {
                                    logger.debug("Adding Capability-Provider for ({}) to watch list from bundle ({})",
                                            capability, bundle.getSymbolicName());
                                    capabilityProviderCounter.incrementAndGet(capability.trim());
                                }
                            } else {
                                logger.debug("Updating Capability-Counter for ({}) from bundle ({})",
                                        element.getAttribute("objectClass"), bundle.getSymbolicName());
                                capabilityCounter.incrementAndGet(element.getAttribute("objectClass"));
                            }
                        });
            } catch (BundleException e) {
                logger.error("Error occurred while parsing the {} header in bundle {}",
                        PROVIDE_CAPABILITY, bundle.getSymbolicName(), e);
            }
        }
    }
}
