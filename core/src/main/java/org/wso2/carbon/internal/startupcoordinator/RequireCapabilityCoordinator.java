/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.carbon.internal.startupcoordinator;

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
import org.wso2.carbon.startupcoordinator.RequireCapabilityListener;

import java.util.Arrays;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * TODO sameera
 */
@Component(
        name = "org.wso2.carbon.internal.startupcoordinator.RequireCapabilityCoordinator",
        immediate = true
)
public class RequireCapabilityCoordinator {
    private static final Logger logger = LoggerFactory.getLogger(RequireCapabilityCoordinator.class);

    private static final String PROVIDE_CAPABILITY = "Provide-Capability";
    private AtomicInteger expectedRCListenerCount = new AtomicInteger(0);
    private Map<String, RequireCapabilityListener> listenerMap = new ConcurrentHashMap<>();
    private MultiCounter<String> capabilityCounter = new MultiCounter<>();
    private BundleContext bundleContext;

    private Timer checkServiceAvailabilityTimer = new Timer();

    @Activate
    public void start(BundleContext bundleContext) throws Exception {
        this.bundleContext = bundleContext;
        try {
            // 1) Process all Provide-Capability headers and get the provided OSGi services and the count.
            Arrays.asList(bundleContext.getBundles())
                    .parallelStream()
                    .filter(new ProvideCapabilityHeaderFilter<>())
                    .forEach(new ProvideCapabilityHeaderConsumer<>());

            // 2) Register listeners to get service registrations events of the interested OSGi services.
            capabilityCounter.getAllKeys().forEach(capability -> logger.info("#### {} = {}", capability,
                    capabilityCounter.get(capability)));

            // 3) Check whether there at least one expect RequireCapabilityLister. IF there is none, then simply return.
            if (expectedRCListenerCount.get() == 0) {
                // There is nothing to do here.
                // TODO Clear all the populated maps.
                return;
            }

            // 4)
            checkServiceAvailabilityTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    listenerMap.keySet()
                            .stream()
                            .filter(key -> capabilityCounter.get(key) == 0 && listenerMap.get(key) != null)
                            .forEach(key -> {
                                listenerMap.remove(key).onAllRequiredCapabilitiesAvailable();
                            });
                }
            }, 200, 200);

            // TODO Start a timer to track pending service registrations.
        } catch (Throwable e) {
            logger.error("Error occurred while processing Provide-Capability manifest headers", e);
        }
    }

    @Deactivate
    public void stop(BundleContext bundleContext) throws Exception {
    }

    @Reference(
            name = "require.capability.listener.service",
            service = RequireCapabilityListener.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "deregisterRequireCapabilityListener"
    )
    public void registerRequireCapabilityListener(RequireCapabilityListener listener,
                                                  Map<String, String> propertyMap) {
        String requiredServiceKey = propertyMap.get("required-service-interface");
        if (requiredServiceKey == null || requiredServiceKey.equals("")) {
            logger.warn("RequireCapabilityListener service ({}) does not contain the required-service-interface proper",
                    listener.getClass().getName());
        } else {
            requiredServiceKey = requiredServiceKey.trim();
        }

        final String serviceClazz = requiredServiceKey;
        listenerMap.put(requiredServiceKey, listener);
        // Now register a service listener to listen to required service
        // When a service is available incrementAndGet the requiredCapability Counter
        // capabilityCounter.decrementAndGet(requiredServiceInterface);

        //TODO close service trackers
        ServiceTracker<Object, Object> serviceTracker = new ServiceTracker<Object, Object>(
                bundleContext,
                requiredServiceKey,
                new ServiceTrackerCustomizer<Object, Object>() {
                    @Override
                    public Object addingService(ServiceReference<Object> reference) {
                        Object obj = bundleContext.getService(reference);

                        //TODO Syncronize this with the schedular task.
                        if (capabilityCounter.decrementAndGet(serviceClazz) == 0) {
                            listener.onAllRequiredCapabilitiesAvailable();
                        }
                        return obj;
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
     * @param <T>
     */
    private static class ProvideCapabilityHeaderFilter<T extends Bundle> implements Predicate<T> {
        @Override
        public boolean test(T bundle) {
            return bundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY) != null;
        }
    }

    private class ProvideCapabilityHeaderConsumer<T extends Bundle> implements Consumer<T> {
        @Override
        public void accept(T bundle) {
            String headerValue = bundle.getHeaders(PROVIDE_CAPABILITY).get(PROVIDE_CAPABILITY);
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
