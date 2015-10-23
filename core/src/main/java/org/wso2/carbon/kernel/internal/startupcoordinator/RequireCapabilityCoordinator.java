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
package org.wso2.carbon.kernel.internal.startupcoordinator;

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
import org.wso2.carbon.kernel.startupcoordinator.RequireCapabilityListener;

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
 */
@Component(
        name = "org.wso2.carbon.internal.startupcoordinator.RequireCapabilityCoordinator",
        immediate = true
)
public class RequireCapabilityCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(RequireCapabilityCoordinator.class);

    private static final String PROVIDE_CAPABILITY = "Provide-Capability";
    private static final String REQUIRED_SERVICE_INTERFACE = "required-service-interface";

    private AtomicInteger expectedRCListenerCount = new AtomicInteger(0);
    private Map<String, RequireCapabilityListener> listenerMap = new ConcurrentHashMap<>();
    private MultiCounter<String> capabilityCounter = new MultiCounter<>();
    private BundleContext bundleContext;

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
        this.bundleContext = bundleContext;
        try {
            // 1) Process all Provide-Capability headers and get the provided OSGi services and the count.
            Arrays.asList(bundleContext.getBundles())
                    .parallelStream()
                    .filter(new ProvideCapabilityHeaderFilter<>())
                    .forEach(new ProvideCapabilityHeaderConsumer<>());

            // 2) Check whether there at least one expect RequireCapabilityLister. IF there is none, then simply return.
            if (expectedRCListenerCount.get() == 0) {
                // Clear all the populated maps.
                capabilityCounter = null;
                listenerMap = null;
                return;
            }

            // 3) Schedule a timer to tack service registrations which have happend before populating the counter as
            //      well as to clear all the listeners with zero available services in the runtime.
            // TODO find a way to stop this timer. make this timer task configurable from the carbon.xml
            checkServiceAvailabilityTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    listenerMap.keySet()
                            .stream()
                            .filter(key -> capabilityCounter.get(key) == 0 && listenerMap.get(key) != null)
                            .forEach(key -> {
                                synchronized (key.intern()) {
                                    listenerMap.remove(key).onAllRequiredCapabilitiesAvailable();
                                }
                            });
                }
            }, 200, 200);

            // TODO 4) Start a timer to track pending service registrations.
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
            service = RequireCapabilityListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "deregisterRequireCapabilityListener"
    )
    public void registerRequireCapabilityListener(RequireCapabilityListener listener,
                                                  Map<String, String> propertyMap) {

        String requiredServiceKey = propertyMap.get(REQUIRED_SERVICE_INTERFACE);
        if (requiredServiceKey == null || requiredServiceKey.equals("")) {
            logger.warn("RequireCapabilityListener service ({}) does not contain the required-service-interface proper",
                    listener.getClass().getName());
        } else {
            requiredServiceKey = requiredServiceKey.trim();
        }

        listenerMap.put(requiredServiceKey, listener);

        //TODO close service trackers once all the service are available.
        final String serviceClazz = requiredServiceKey;
        ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<Object, Object>(
                bundleContext,
                requiredServiceKey,
                new ServiceTrackerCustomizer<Object, Object>() {
                    @Override
                    public Object addingService(ServiceReference<Object> reference) {
                        assert serviceClazz != null;
                        synchronized (serviceClazz.intern()) {
                            if (capabilityCounter.decrementAndGet(serviceClazz) == 0) {
                                listener.onAllRequiredCapabilitiesAvailable();
                            }
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

    }

    public void deregisterRequireCapabilityListener(RequireCapabilityListener listener,
                                                    Map<String, String> propertyMap) {
    }

    /**
     * Implementation of the {@link Predicate} interface which filters OSGi manifest header with key Provide-Capability.
     *
     * @param <T> OSGi bundle
     */
    private static class ProvideCapabilityHeaderFilter<T extends Bundle> implements Predicate<T> {
        @Override
        public boolean test(T bundle) {
            return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    return bundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY) != null;
                }
            });
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
            String headerValue = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return bundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY);
                }
            });
            try {
                ManifestElement[] elements = ManifestElement.parseHeader(PROVIDE_CAPABILITY, headerValue);
                Arrays.asList(elements)
                        .stream()
                        .filter(element -> "osgi.service".equals(element.getValue()))
                        .forEach(element -> {
                            if (RequireCapabilityListener.class.getName().equals(element.getAttribute("objectClass"))) {
                                expectedRCListenerCount.incrementAndGet();
                            } else {
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
