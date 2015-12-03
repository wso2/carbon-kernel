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
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.internal.DataHolder;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.utils.manifest.ManifestElement;
import org.wso2.carbon.kernel.utils.manifest.ManifestElementParserException;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * {@code RequireCapabilityCoordinator} handles carbon component startup complexities. Here are two such cases
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
    private static final String COMPONENT_KEY = "component-key";
    private static final String OSGI_SERVICE_HEADER_ELEMENT = "osgi.service";
    private static final String DEPENDENT_COMPONENT_KEY = "dependent-component-key";
    private static final String OBJECT_CLASS = "objectClass";

    private AtomicInteger requiredCapabilityListenerCount = new AtomicInteger(0);
    private MultiCounter<String> capabilityProviderCounter = new MultiCounter<>();

    // Holds capability - component-key (CapabilityListener) dependencies. List of component-keys(CapabilityListeners)
    // depends on the capability
    private Map<String, List<String>> capabilityComponentKeyMap = new HashMap<>();

    // Holds the component-key to CapabilityListener mapping. This is required to check satisfiable CapabilityListeners
    private Map<String, RequiredCapabilityListener> componentKeyCapabilityListenerMap = new ConcurrentHashMap<>();

    // Maintain the capability count of component-keys(CapabilityListeners).
    // Increment the counter to increase the expected capabilities (service registrations)
    // Decrement the counter as and when expected capabilities are available.
    // If the count is zero for a given component-key means two things
    //      1) There are zero expected capabilities or
    //      2) All the expected capabilities are now availalb.
    private MultiCounter<String> componentKeyCapabilityCounter = new MultiCounter<>();

    private Timer satisfiableCapabilityListenerTimer = new Timer();
//    private Timer pendingCapabilityTimer = new Timer();

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
            // 1.
            processManifestHeaders(Arrays.asList(bundleContext.getBundles()));

            //TODO Syncronize cases
            //TODO Test dependent-component-key scenarios
            //TODO Pending timer

            // 2.
            if (requiredCapabilityListenerCount.get() == 0) {
                // There are no registered RequiredCapabilityListener
                // Clear all the populated maps.
                capabilityComponentKeyMap = null;
                return;
            }

            // 3.
            ServiceTracker<Object, Object> capabilityServiceTracker = getCapabilityServiceTracker();
            capabilityServiceTracker.open();

            // 4.
            satisfiableCapabilityListenerTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    if (requiredCapabilityListenerCount.get() == 0 && componentKeyCapabilityListenerMap.size() == 0) {
                        logger.debug("All the RequiredCapabilityListeners are notified, " +
                                "therefore cancelling the satisfiableCapabilityListenerTimer");
                        satisfiableCapabilityListenerTimer.cancel();
                        return;
                    }

                    componentKeyCapabilityListenerMap.keySet()
                            .stream()
                            .forEach(componentKey -> {
                                synchronized (componentKey.intern()) {
                                    if (capabilityProviderCounter.get(componentKey) == 0 &&
                                            componentKeyCapabilityCounter.get(componentKey) == 0) {
                                        RequiredCapabilityListener capabilityListener =
                                                componentKeyCapabilityListenerMap.remove(componentKey);
                                        logger.debug("Notifying RequiredCapabilityListener: {} since all the " +
                                                        "required capabilities are available",
                                                capabilityListener.getClass().getName());
                                        capabilityListener.onAllRequiredCapabilitiesAvailable();
                                    }
                                }
                            });
                }
            }, 200, 200);


            // 5) Start a timer to track pending service registrations.
//            pendingCapabilityTimer.scheduleAtFixedRate(new TimerTask() {
//
//                @Override
//                public void run() {
//
//                    if (capabilityListenerMap.size() == 0) {
//                        logger.debug("Cancelling the timer which checks the satisfiable CapabilityListeners");
//                        satisfiableCapabilityListenerTimer.cancel();
//
//                        logger.debug("Cancelling the time which checks pending capabilities");
//                        pendingCapabilityTimer.cancel();
//                    }
//
//                    capabilityListenerMap.keySet()
//                            .stream()
//                            .forEach(capabilityName -> {
//                                synchronized (capabilityName.intern()) {
//                                    if (capabilityProviderCounter.get(capabilityName) == 0 &&
//                                            capabilityCounter.get(capabilityName) > 0) {
//                                        logger.warn("Waiting on pending capability registration for ({})",
//                                                capabilityName);
//                                    } else if (capabilityProviderCounter.get(capabilityName) > 0) {
//                                        logger.warn("Waiting on pending capability provider registration for ({})",
//                                                capabilityName);
//                                    }
//                                }
//                            });
//                }
//            }, 60000, 30000);
        } catch (Throwable e) {
            logger.error("Failed to initialize startup resolver. ", e);
        }
    }

    @Deactivate
    public void stop(BundleContext bundleContext) throws Exception {
        logger.debug("Deactivating startup resolver component available in bundle {}",
                bundleContext.getBundle().getSymbolicName());
    }

    /**
     * Process manifest headers in all the bundles and calculate startup order requirements.
     *
     * @param bundleList list of OSGi bundles to scanned for Provide-Capability headers.
     */
    private void processManifestHeaders(List<Bundle> bundleList) {
        // Extract all the osgi.service Provide-Capability manifest header elements
        List<ManifestElement> manifestElementList = getOSGiServiceProvideCapabilityHeaders(bundleList);

        // process RequiredCapabilityListeners, CapabilityProviders and Capabilities with
        // dependency-component-key attribute
        manifestElementList
                .stream()
                .forEach(manifestElement -> {
                    String objectClassName = manifestElement.getAttribute(OBJECT_CLASS);

                    if (RequiredCapabilityListener.class.getName().equals(objectClassName)) {
                        String capabilityName = getManifestElementAttribute(CAPABILITY_NAME, manifestElement, true);
                        String componentKey = getManifestElementAttribute(COMPONENT_KEY, manifestElement, true);
                        addCapabilityComponetKeyMapping(capabilityName, componentKey);
                        requiredCapabilityListenerCount.incrementAndGet();

                    } else if (CapabilityProvider.class.getName().equals(objectClassName)) {
                        String capabilityName = getManifestElementAttribute(CAPABILITY_NAME, manifestElement, true);
                        String dependentComponentKey = getManifestElementAttribute(
                                DEPENDENT_COMPONENT_KEY, manifestElement, false);
                        if (dependentComponentKey != null) {
                            addCapabilityComponetKeyMapping(capabilityName, dependentComponentKey);
                        }
                        capabilityProviderCounter.incrementAndGet(capabilityName);

                    } else if (manifestElement.getAttribute(DEPENDENT_COMPONENT_KEY) != null) {
                        // objectClass is the capabilityName here.
                        String dependentComponentKey = getManifestElementAttribute(
                                DEPENDENT_COMPONENT_KEY, manifestElement, false);
                        addCapabilityComponetKeyMapping(objectClassName, dependentComponentKey);
                    }
                });

        // Update  componentKeyCapabilityCounter with expected number of capabilities
        manifestElementList
                .stream()
                .filter(manifestElement -> {
                    String objectClass = manifestElement.getAttribute(OBJECT_CLASS);
                    return !RequiredCapabilityListener.class.getName().equals(objectClass) &&
                            !CapabilityProvider.class.getName().equals(objectClass);
                })
                .forEach(manifestElement -> {
                    String objectClass = manifestElement.getAttribute(OBJECT_CLASS);
                    if (capabilityComponentKeyMap.containsKey(objectClass)) {
                        capabilityComponentKeyMap.get(objectClass)
                                .forEach(componentKeyCapabilityCounter::incrementAndGet);
                    }
                });
    }

    /**
     * Iterates through all the manifest header in all OSGi bundles and filter out manifest headers with the
     * "Provide-Capability" as the header key
     * <p>
     * Extract all the osgi.service Provide-Capability manifest header elements
     *
     * @param bundleList list of bundles to be scanned for Provide-Capability headers
     * @return a list of ManifestElement.
     */
    private List<ManifestElement> getOSGiServiceProvideCapabilityHeaders(List<Bundle> bundleList) {

        return bundleList
                .parallelStream()
                .filter(bundle -> AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                        bundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY) != null))

                .map(bundle -> {
                    String headerValue = AccessController.doPrivileged((PrivilegedAction<String>) () ->
                            bundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY));
                    try {
                        return ManifestElement.parseHeader(PROVIDE_CAPABILITY, headerValue);
                    } catch (ManifestElementParserException e) {
                        String message = "Error occurred while parsing the " + PROVIDE_CAPABILITY
                                + " header in bundle " + bundle.getSymbolicName();
                        throw new RuntimeException(message);
                    }
                })
                .flatMap(manifestElementArray -> Arrays.asList(manifestElementArray).stream())
                .filter(manifestElement -> OSGI_SERVICE_HEADER_ELEMENT.equals(manifestElement.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Returns the attribute value of the specified key from the {@code ManifestElement}
     *
     * @param attributeKey    key of the manifest element attribute
     * @param manifestElement {@code ManifestElement} object from which we lookup for attributes
     * @param mandatory       states whether this is a required attribute or not. If a required attribute value is null,
     *                        then this method throws a {@code RuntimeException}
     * @return requested attribute value
     */
    private String getManifestElementAttribute(String attributeKey,
                                               ManifestElement manifestElement, boolean mandatory) {
        String value = manifestElement.getAttribute(attributeKey);
        if ((value == null || value.equals("")) && mandatory) {
            throw new RuntimeException(attributeKey + " value is missing in Provide-Capability header");
        }

        return value != null ? value.trim() : null;

    }

    /**
     * Add a mapping from capability-name to component-key
     *
     * @param capability   service capability. Full qualified class name of the service
     * @param componentKey mapped component-key value.
     */
    private void addCapabilityComponetKeyMapping(String capability, String componentKey) {
        List<String> componentKeyList = capabilityComponentKeyMap.get(capability);
        if (componentKeyList == null) {
            componentKeyList = new ArrayList<>();
            capabilityComponentKeyMap.put(capability, componentKeyList);
        }
        // TODO Do we need to check for duplicates here.
        componentKeyList.add(componentKey);
    }

    /**
     * Returns an instance of OSGi {@link ServiceTracker} which tracks all the required capability services, all the
     * RequiredCapabilityListener services and all the CapabilityProvider services
     *
     * @return {@code ServiceTracker} instance
     */
    private ServiceTracker<Object, Object> getCapabilityServiceTracker() {
        BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
        Filter orFilter = getORFilter(new ArrayList<>(capabilityComponentKeyMap.keySet()));
        return new ServiceTracker<>(bundleContext, orFilter, new CapabilityServiceTrackerCustomizer());
    }

    /**
     * Returns and instance of {@link Filter}
     *
     * @param capabilityNameList all the required capability services.
     * @return LDAP like filter
     */
    private Filter getORFilter(List<String> capabilityNameList) {
        StringBuilder orFilterBuilder = new StringBuilder();
        orFilterBuilder.append("(|");

        for (String service : capabilityNameList) {
            orFilterBuilder.append("(").append(OBJECT_CLASS).append("=").append(service).append(")");
        }

        orFilterBuilder.append("(").append(OBJECT_CLASS).append("=").append(
                RequiredCapabilityListener.class.getName()).append(")");
        orFilterBuilder.append("(").append(OBJECT_CLASS).append("=").append(
                CapabilityProvider.class.getName()).append(")");
        orFilterBuilder.append(")");

        BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
        try {
            return bundleContext.createFilter(orFilterBuilder.toString());
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Custom implementation of the {@link ServiceTrackerCustomizer} which handles registered
     * {@code RequiredCapabilityListener} services, {@code CapabilityProvider} services and
     * all the other required services
     */
    private class CapabilityServiceTrackerCustomizer implements ServiceTrackerCustomizer<Object, Object> {

        @Override
        public Object addingService(ServiceReference<Object> reference) {
            Object serviceObject = DataHolder.getInstance().getBundleContext().getService(reference);
            String serviceInterfaceClassName = ((String[]) reference.getProperty(OBJECT_CLASS))[0];
            String serviceImplClassName = serviceObject.getClass().getName();

            if (RequiredCapabilityListener.class.getName().equals(serviceInterfaceClassName)) {
                String capabilityName = (String) reference.getProperty(CAPABILITY_NAME);
                if (capabilityName == null || capabilityName.equals("")) {
                    throw new RuntimeException(CAPABILITY_NAME + " value is missing in the services registered " +
                            "with the key " + serviceInterfaceClassName + ", implementation class name is "
                            + serviceImplClassName);
                }

                String componentKey = (String) reference.getProperty(COMPONENT_KEY);
                if (componentKey == null || componentKey.equals("")) {
                    throw new RuntimeException(COMPONENT_KEY + " value is missing in the services registered " +
                            "with the key " + serviceInterfaceClassName + ", implementation class name is "
                            + serviceImplClassName);
                }

                logger.debug("Adding {}. Service implementation class name: {}. " +
                                "capability-name: {}. component-key: {}", RequiredCapabilityListener.class.getName(),
                        serviceImplClassName, capabilityName, componentKey);

                requiredCapabilityListenerCount.decrementAndGet();
                componentKeyCapabilityListenerMap.put(componentKey, (RequiredCapabilityListener) serviceObject);

            } else if (CapabilityProvider.class.getName().equals(serviceInterfaceClassName)) {
                CapabilityProvider provider = (CapabilityProvider) serviceObject;

                String capabilityName = (String) reference.getProperty(CAPABILITY_NAME);
                if (capabilityName == null || capabilityName.equals("")) {
                    throw new RuntimeException(CAPABILITY_NAME + " value is missing in the services registered " +
                            "with the key " + serviceInterfaceClassName + ", implementation class name is "
                            + serviceImplClassName);
                }

                logger.debug("Adding {}. Service implementation class name: {}. " + "capability-name: {}",
                        CapabilityProvider.class.getName(), serviceImplClassName, capabilityName);

                IntStream.range(0, provider.getCount()).forEach(count -> capabilityComponentKeyMap.get(capabilityName)
                        .forEach(componentKeyCapabilityCounter::incrementAndGet));
                capabilityProviderCounter.decrementAndGet(capabilityName);

            } else {
                // this has to be a capability service
                logger.debug("Adding Capability. Service id: {}. Service implementation class: {}. " +
                                "dependent-component-key: {}", serviceInterfaceClassName, serviceImplClassName,
                        reference.getProperty(DEPENDENT_COMPONENT_KEY));

                capabilityComponentKeyMap.get(serviceInterfaceClassName)
                        .forEach(componentKeyCapabilityCounter::decrementAndGet);
            }

            return serviceObject;
        }

        @Override
        public void modifiedService(ServiceReference<Object> reference, Object service) {
        }

        @Override
        public void removedService(ServiceReference<Object> reference, Object service) {
        }
    }
}
