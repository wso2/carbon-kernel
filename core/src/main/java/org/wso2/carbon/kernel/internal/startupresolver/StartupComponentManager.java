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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages StartupComponents.
 *
 * @since 5.1.0
 */
class StartupComponentManager {
    private static final Logger logger = LoggerFactory.getLogger(StartupComponentManager.class);

    // Key of this map is the component name
    private Map<String, StartupComponent> startupComponentMap = new ConcurrentHashMap<>();

    // This map contains the pending list of Capabilities against the component name.
    // Key of the this map is the component name.
    // Returns the list of pending required Capabilities for a specified component key.
    private Map<String, List<Capability>> pendingCapabilityMap = new ConcurrentHashMap<>();

    // This map contains the list of StartupComponent against the capability name.
    // Key of the this map is the capability name.
    // Returns the list of StartupComponents which depends on a given capability.
    private Map<String, List<StartupComponent>> capabilityToComponentMap = new HashMap<>();

    // Key of this map is the capability name;
    private Map<String, List<CapabilityProviderCapability>> pendingCapabilityProviderMap = new HashMap<>();

    /**
     * Iterates though the list of StartupComponents and update internal data structures.
     * <p>
     * Add each StartupComponent to the startupComponentMap. Key is the componentName.
     * <p>
     * Update capabilityToComponentMap. This map maintains the list of StartupComponents against
     * each and every capability.
     *
     * @param startupComponentList list of StartupComponents to process.
     */
    void addComponents(List<StartupComponent> startupComponentList) {
        startupComponentList
                .stream()
                .forEach(this::addComponentInternal);
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

        startupComponent.getRequiredServiceList().add(capabilityName);
        updateCapabilityToComponentMap(startupComponent, capabilityName);
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
    void addExpectedCapabilityProvider(CapabilityProviderCapability capabilityProvider) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding expected CapabilityProvider with the CapabilityName {} from bundle({}:{})",
                    capabilityProvider.getProvidedCapabilityName(),
                    capabilityProvider.getBundle().getSymbolicName(),
                    capabilityProvider.getBundle().getVersion());
        }

        String providedCapabilityName = capabilityProvider.getProvidedCapabilityName();
        synchronized (providedCapabilityName.intern()) {
            if (pendingCapabilityProviderMap.containsKey(providedCapabilityName)) {
                pendingCapabilityProviderMap.get(providedCapabilityName).add(capabilityProvider);
            } else {
                List<CapabilityProviderCapability> providerCapabilityList = new ArrayList<>();
                providerCapabilityList.add(capabilityProvider);
                pendingCapabilityProviderMap.put(providedCapabilityName, providerCapabilityList);
            }
        }
    }

    /**
     * Adds available {@code CapabilityProvider} capability.
     * <p>
     * This method is invoked when an OSGi serice of type {@code CapabilityProvider} is registered.
     *
     * @param capabilityProvider {@code CapabilityProvider} capability instance
     */
    void addAvailableCapabilityProvider(CapabilityProviderCapability capabilityProvider) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding available CapabilityProvider with the CapabilityName {} from bundle({}:{})",
                    capabilityProvider.getProvidedCapabilityName(),
                    capabilityProvider.getBundle().getSymbolicName(),
                    capabilityProvider.getBundle().getVersion());
        }

        String providedCapabilityName = capabilityProvider.getProvidedCapabilityName();
        synchronized (providedCapabilityName.intern()) {
            List<CapabilityProviderCapability> capabilityProviderList =
                    pendingCapabilityProviderMap.get(providedCapabilityName);
            if (capabilityProviderList != null) {
                capabilityProviderList.remove(capabilityProvider);
                if (capabilityProviderList.size() == 0) {
                    pendingCapabilityProviderMap.remove(providedCapabilityName);
                }
            } else {
                logger.debug("Unknown CapabilityProvider from bundle({}:{})",
                        capabilityProvider.getBundle().getSymbolicName(),
                        capabilityProvider.getBundle().getVersion());
            }
        }
    }

    /**
     * Adds expect required capability. Capability could be an OSGi service, manifest header etc.
     * <p>
     * This method is invoked during the manifest header processing time or when {@code CapabilityProvider}
     * OSGi service is registered.
     *
     * @param capability {@code Capability} instance
     */
    void addExpectedRequiredCapability(Capability capability) {
        String capabilityName = capability.getName();

        synchronized (capabilityName.intern()) {
            startupComponentMap.values()
                    .stream()
                    .filter(startupComponent ->
                            startupComponent.getRequiredServiceList().contains(capabilityName))
                    .forEach(startupComponent -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Adding expected required capability {} from bundle({}:{}) to " +
                                            "startup component {}.", capability.getName(),
                                    capability.getBundle().getSymbolicName(),
                                    capability.getBundle().getVersion(),
                                    startupComponent.getName());
                        }

                        String componentName = startupComponent.getName();
                        addCapabilityToComponent(componentName, capability);
                    });
        }
    }

    /**
     * Adds available requried capability.
     * <p>
     * This method is invoked when a required capability is availalbe.
     *
     * @param capability {@code Capability} instance
     */
    void addAvailableRequiredCapability(Capability capability) {
        String capabilityName = capability.getName();

        synchronized (capabilityName.intern()) {
            capabilityToComponentMap.get(capabilityName)
                    .stream()
                    .forEach(startupComponent -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Adding available required capability {} from bundle({}:{}) to " +
                                            "startup component {}.", capability.getName(),
                                    capability.getBundle().getSymbolicName(),
                                    capability.getBundle().getVersion(),
                                    startupComponent.getName());
                        }
                        String componentName = startupComponent.getName();
                        addCapabilityToComponent(componentName, capability);
                    });
        }
    }

    /**
     * Returns a list of pending {@code StartupComponent}s.
     * <p>
     * A {@code StartupComponents} becomes pending if either of the following conditions met.
     * 1) If there are pending capability registrations,
     * 2) If there are pending {@code CapabilityProvider} service registrations,
     * 3) If the {@code RequiredCapabilityListener} is not yet registered.
     *
     * @return a list of {@code StartupComponent}s with pending capabilities
     */
    List<StartupComponent> getPendingComponents() {

        return pendingCapabilityMap.keySet()
                .stream()
                .filter(componentName -> getPendingProvideCapabilityList(componentName).size() != 0 ||
                        startupComponentMap.get(componentName).getListener() == null ||
                        getPendingCapabilityProviderList(componentName).size() != 0)
                .map(componentName -> startupComponentMap.get(componentName))
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of {@code StartupComponent}s whose required capabilities are available.
     * <p>
     * A {@code StartupComponent} can be satisfied if all the following conditions are valid.
     * 1) If there no pending capability registrations.
     * 2) If the {@code RequiredCapabilityListener} OSGi service is available.
     * 3) If there are no pending {@code CapabilityProvider} OSGi service registrations.
     *
     * @return a list of {@code StartupComponent}s whose required capabilities are available.
     */
    List<StartupComponent> getSatisfiableComponents() {

        return pendingCapabilityMap.keySet()
                .stream()
                // Filter out all the startup components whose required capabilities are all available;
                .filter(componentName -> getPendingProvideCapabilityList(componentName).size() == 0)
                // Filter out all the startup components whose RequiredCapabilityListener is available.
                .filter(componentName -> startupComponentMap.get(componentName).getListener() != null)
                // Check whether there are pending CapabilityProviders which provide capabilities
                // required by these startup components.
                .filter(componentName -> getPendingCapabilityProviderList(componentName).size() == 0)
                .map(componentName -> startupComponentMap.get(componentName))
                .collect(Collectors.toList());
    }

    /**
     * Deletes the satisfied components from the internal data structures.
     *
     * @param startupComponent {@code StartupComponent} to be removed.
     */
    void removeSatisfiedComponent(StartupComponent startupComponent) {
        startupComponentMap.remove(startupComponent.getName());
        pendingCapabilityMap.remove(startupComponent.getName());
    }

    /**
     * Returns the pending OSGi Service of type {@code RequiredCapabilityListener}.
     *
     * @return a list of pending {@code RequiredCapabilityListener}s
     */
    List<StartupComponent> getPendingRequiredCapabilityListeners() {
        return startupComponentMap.values()
                .stream()
                .filter(startupComponent -> startupComponent.getListener() == null)
                .collect(Collectors.toList());
    }

    /**
     * Returns the pending OSGi Service of type {@code CapabilityProvider}.
     *
     * @return a list of pending {@code CapabilityProvider}s
     */
    List<CapabilityProviderCapability> getPendingCapabilityProviderList() {
        return pendingCapabilityProviderMap.values()
                .stream()
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Returns all the pending capabilities of a given startup component.
     *
     * @param componentName name of the startup component.
     * @return a list of pending {@code Capability} instances of the give statup component.
     */
    List<Capability> getPendingProvideCapabilityList(String componentName) {
        return pendingCapabilityMap.get(componentName);

    }

    private void addComponentInternal(StartupComponent startupComponent) {
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
        pendingCapabilityMap.put(componentName, new ArrayList<>());

        // Iterate through the list of required OSGi service capabilities in a StartupComponent and update
        // capabilityToComponentMap.
        startupComponent.getRequiredServiceList()
                .forEach(requiredCapabilityName -> {
                    logger.debug("Startup component {} depends on OSGi Service capability {}",
                            startupComponent.getName(), requiredCapabilityName);
                    updateCapabilityToComponentMap(startupComponent, requiredCapabilityName);
                });
    }

    private List<CapabilityProviderCapability> getPendingCapabilityProviderList(String componentName) {
        return startupComponentMap.get(componentName).getRequiredServiceList()
                .stream()
                .filter(requiredCapability -> pendingCapabilityProviderMap.get(requiredCapability) != null)
                .flatMap(requiredCapability -> pendingCapabilityProviderMap.get(requiredCapability).stream())
                .collect(Collectors.toList());
    }

    private void addCapabilityToComponent(String componentName, Capability capability) {
        List<Capability> pendingCapabilityList = pendingCapabilityMap.get(componentName);

        if (pendingCapabilityList != null) {
            if (pendingCapabilityList.contains(capability)) {
                // If the capability is already added, then we remove it.
                // This is added as an available capability
                pendingCapabilityList.remove(capability);
            } else {
                pendingCapabilityList.add(capability);
            }

        } else {
            pendingCapabilityList = new ArrayList<>();
            pendingCapabilityList.add(capability);
            pendingCapabilityMap.put(componentName, pendingCapabilityList);
        }

        logger.debug("Required Capability count of component {}: {}", componentName, pendingCapabilityList.size());
    }

    private void updateCapabilityToComponentMap(StartupComponent startupComponent, String requiredCapabilityName) {
        if (capabilityToComponentMap.containsKey(requiredCapabilityName)) {
            capabilityToComponentMap.get(requiredCapabilityName).add(startupComponent);
        } else {
            List<StartupComponent> componentList = new ArrayList<>();
            componentList.add(startupComponent);
            capabilityToComponentMap.put(requiredCapabilityName, componentList);
        }
    }

}
