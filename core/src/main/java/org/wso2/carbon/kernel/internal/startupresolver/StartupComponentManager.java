/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.kernel.internal.startupresolver;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.internal.startupresolver.beans.Capability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.CapabilityProviderCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.StartupComponent;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Manages StartupComponents.
 *
 * @since 5.1.0
 */
class StartupComponentManager {
    private static final Logger logger = LoggerFactory.getLogger(StartupComponentManager.class);

    // Key of this map is the component name
    private Map<String, StartupComponent> startupComponentMap = new HashMap<>();

    /**
     * Adds the given {@code StartupComponent}.
     * <p>
     * Add the StartupComponent to the startupComponentMap. Key is the componentName.
     *
     * @param startupComponent to be added.
     */
    void addStartupComponent(StartupComponent startupComponent) {
        String componentName = startupComponent.getName();

        if (startupComponentMap.get(componentName) != null) {
            StartupComponent existingComponent = startupComponentMap.get(componentName);
            logger.warn("Duplicate Startup-Component detected. Existing Startup-Component {} " +
                            "from bundle({}:{}). New Startup-Component {} from bundle({}:{}).",
                    existingComponent.getName(),
                    existingComponent.getBundle().getSymbolicName(),
                    existingComponent.getBundle().getVersion(),
                    startupComponent.getName(),
                    startupComponent.getBundle().getSymbolicName(),
                    startupComponent.getBundle().getVersion());
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding startup component {} from bundle({}:{})",
                    componentName,
                    startupComponent.getBundle().getSymbolicName(),
                    startupComponent.getBundle().getVersion());
        }

        startupComponentMap.put(componentName, startupComponent);
    }

    /**
     * Add a new required service capability to the specified component.
     *
     * @param componentName  the component name to which this new service capability to be added.
     * @param capabilityName the capability name to added to the component.
     */
    void addRequiredOSGiServiceCapabilityToComponent(String componentName, String capabilityName) {
        StartupComponent startupComponent = startupComponentMap.get(componentName);
        if (startupComponent == null) {
            logger.warn("Adding a required OSGi service capability to component, but specified startup component is " +
                    "not available, component-name: {} and capability-name: {}.", componentName, capabilityName);
            return;
        }

        logger.debug("Updating the required OSGi Service list of startup component {}. capabilityName: {} ",
                componentName, capabilityName);

        startupComponent.addRequiredService(capabilityName);
    }

    /**
     * Adds the available {@code RequiredCapabilityListener} OSGi service to the specified startup component.
     * <p>
     * This service is required to notify the component when all of its required capabilities are available.
     *
     * @param listener      {@code RequiredCapabilityListener} service object.
     * @param componentName the component name to which this listener is added.
     * @param bundle        the bundle from which this listener service is registered.
     */
    void addRequiredCapabilityListener(RequiredCapabilityListener listener, String componentName, Bundle bundle) {
        StartupComponent startupComponent = startupComponentMap.get(componentName);
        if (startupComponent == null) {
            logger.warn("Adding a RequiredCapabilityListener from bundle({}:{}), but specified startup component is " +
                    "not available, component-name: {}", bundle.getSymbolicName(), bundle.getVersion(), componentName);
            return;
        }

        if (startupComponent.getListener() != null) {
            logger.warn("Duplicate RequiredCapabilityListener detected. Existing RequiredCapabilityListener for " +
                            "startup component {}. New RequiredCapabilityListener from bundle({}:{}).",
                    componentName,
                    bundle.getSymbolicName(),
                    bundle.getVersion());
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Adding available RequiredCapabilityListener with the componentName {} from bundle({}:{})",
                    componentName, bundle.getSymbolicName(), bundle.getVersion());
        }
        startupComponent.setListener(listener);
    }

    /**
     * Adds expected {@code CapabilityProvider} capability.
     * <p>
     * This method is invoked during the manifest header processing stage.
     *
     * @param capabilityProvider {@code CapabilityProvider} capability instance
     */
    void addExpectedOrAvailableCapabilityProvider(CapabilityProviderCapability capabilityProvider) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding expected CapabilityProvider with the CapabilityName {} from bundle({}:{})",
                    capabilityProvider.getProvidedCapabilityName(),
                    capabilityProvider.getBundle().getSymbolicName(),
                    capabilityProvider.getBundle().getVersion());
        }


        startupComponentMap.values().stream()
                .filter(startupComponent ->
                        startupComponent.isServiceRequired(capabilityProvider.getProvidedCapabilityName()))
                .forEach(startupComponent ->
                        startupComponent.addExpectedOrAvailableCapabilityProvider(capabilityProvider));
    }

    /**
     * Adds expect required capability. Capability could be an OSGi service, manifest header etc.
     * <p>
     * This method is invoked during the manifest header processing time or when {@code CapabilityProvider}
     * OSGi service is registered.
     *
     * @param capability {@code Capability} instance
     */
    void addRequiredCapability(Capability capability) {
        startupComponentMap.values()
                .stream()
                .filter(startupComponent -> startupComponent.isServiceRequired(capability.getName()))
                .forEach(startupComponent -> {

                    if (startupComponent.isSatisfied()) {
                        logger.warn("You are trying to add an {} capability {} from bundle({}:{}) to an already " +
                                        "activated startup listener component {} in bundle({}:{}). Refer the Startup " +
                                        "Order Resolver documentation and validated your configuration",
                                (capability.getState() == Capability.CapabilityState.AVAILABLE) ?
                                        "available" : "expected",
                                capability.getName(),
                                capability.getBundle().getSymbolicName(),
                                capability.getBundle().getVersion(),
                                startupComponent.getName(),
                                startupComponent.getBundle().getSymbolicName(),
                                startupComponent.getBundle().getVersion());
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("Adding {} required capability {} from bundle({}:{}) to " +
                                        "startup listener component {}.",
                                (capability.getState() == Capability.CapabilityState.AVAILABLE) ?
                                        "available" : "expected",
                                capability.getName(),
                                capability.getBundle().getSymbolicName(),
                                capability.getBundle().getVersion(),
                                startupComponent.getName());
                    }
                    startupComponent.addExpectedOrAvailableCapability(capability);
                });

    }

    /**
     * Returns a list of {@code StartupComponent}s based on the given {@code Predicate}.
     * <p>
     *
     * @param componentFilter which specifies the criteria to retrieve components.
     * @return a list of {@code StartupComponent}s with pending capabilities
     */
    List<StartupComponent> getComponents(Predicate<StartupComponent> componentFilter) {
        return startupComponentMap.values().stream()
                .filter(componentFilter)
                .collect(Collectors.toList());
    }

    /**
     * Returns the pending OSGi Service of type {@code CapabilityProvider}.
     *
     * @return a list of pending {@code CapabilityProvider}s
     */
    List<CapabilityProviderCapability> getPendingCapabilityProviders() {
        return startupComponentMap.values().stream()
                .filter(StartupComponent::isSatisfied)
                .flatMap((startupComponent) -> startupComponent.getPendingCapabilityProviders().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    void notifySatisfiableComponents() {
        getComponents(StartupComponent::isSatisfiable)
                .forEach(startupComponent -> {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Notifying RequiredCapabilityListener of component {} from bundle({}:{}) " +
                                        "since all the required capabilities are available",
                                startupComponent.getName(),
                                startupComponent.getBundle().getSymbolicName(),
                                startupComponent.getBundle().getVersion());
                    }

                    startupComponent.setSatisfied(true);
                    RequiredCapabilityListener capabilityListener = startupComponent.getListener();
                    capabilityListener.onAllRequiredCapabilitiesAvailable();
                });
    }
}
