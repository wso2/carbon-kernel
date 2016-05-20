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
package org.wso2.carbon.kernel.internal.startupresolver.beans;

import org.osgi.framework.Bundle;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@code StartupComponent} Represents an entity which needs to hold its initialization until all the required
 * capabilities are available.
 * <p>
 * A {@code StartupComponent} should register an OSGi service with the interface {@code RequiredCapabilityListener}.
 * This service will be invoked when all the required capabilities are available.
 * <p>
 * e.g.
 * Carbon-Component: startup.listener;componentName="transport-mgt";
 * requiredService="org.wso2.carbon.kernel.transports.CarbonTransport"
 *
 * @since 5.1.0
 */
public class StartupComponent {

    /**
     * Name of the startup listener component extracted from the componentName manifest attribute
     */
    private String name;

    /**
     * List of required service class names extracted from the requiredService manifest attribute.
     */
    private List<String> requiredServiceList = new ArrayList<>();

    /**
     * List of pending expected or available capabilities.
     */
    private List<Capability> pendingCapabilityList = Collections.synchronizedList(new ArrayList<>());

    /**
     * RequiredCapabilityListener service instance.
     */
    private RequiredCapabilityListener listener;

    /**
     * List of pending expected or available CapabilityProvider OSGi services.
     */
    private List<CapabilityProviderCapability> pendingCapabilityProviderList = new ArrayList<>();

    /**
     * OSGi bundle to which this component resides.
     */
    private Bundle bundle;

    /**
     * Indicates whether this startup listener component is already satisfied or not.
     */
    private boolean satisfied = false;

    /**
     * Constructor to create a {@code StartupComponent} instance.
     *
     * @param componentName name of the startup listener component.
     * @param bundle        bundle in which this startup listener component resides.
     */
    public StartupComponent(String componentName, Bundle bundle) {
        this.name = componentName;
        this.bundle = bundle;
    }

    /**
     * Name of the startup listener component.
     *
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the required OSGi services lists of this component.
     *
     * @return a list which contains the all the required OSGi services of this component.
     */
    public List<String> getRequiredServices() {
        return requiredServiceList;
    }

    public void addRequiredServices(List<String> requiredServiceList) {
        this.requiredServiceList.addAll(requiredServiceList);
    }

    public void addRequiredService(String requiredService) {
        requiredServiceList.add(requiredService);
    }

    public boolean isServiceRequired(String service) {
        return requiredServiceList.contains(service);
    }

    /**
     * Register {@code Capability} instances with this startup listener component.
     * <p>
     * This {@code Capability} may be in the AVAILABLE or EXPECTED state.
     * <p>
     * 1) If the {@code Capability} is in the EXPECTED
     * state and if there is matching {@code Capability} already registered with this startup listener component, then
     * it is removed from the list of {@code Capability} of this component.
     * <p>
     * 2) If the {@code Capability} is in the AVAILABLE
     * state and if there is matching {@code Capability} already registered with this startup listener component, then
     * it is removed from the list of {@code Capability} of this component.
     *
     * @param capability {@code Capability} object to be registered with this startup listener component.
     */
    public synchronized void addExpectedOrAvailableCapability(Capability capability) {
        if (pendingCapabilityList.contains(capability)) {
            pendingCapabilityList.remove(capability);
        } else {
            pendingCapabilityList.add(capability);
        }
    }

    /**
     * Returns all the pending capabilities of this startup listener component. There could capabilities
     * in both AVAILABLE and EXPECTED state.
     * <p>
     * If the {@code Capability} is in the EXPECTED state which means that the corresponding AVAILABLE
     * {@code Capability} is not yet registered with this component.
     * <p>
     * IF the {@code Capability} is in the AVAILABLE state which means that the corresponding EXPECTED
     * {@code Capability} is not yet registered with this component.
     *
     * @return the list of pending capabilities.
     */
    public List<Capability> getPendingCapabilities() {
        return Collections.unmodifiableList(pendingCapabilityList);
    }

    public RequiredCapabilityListener getListener() {
        return listener;
    }

    public void setListener(RequiredCapabilityListener listener) {
        this.listener = listener;
    }

    public void addExpectedOrAvailableCapabilityProvider(CapabilityProviderCapability capabilityProvider) {
        if (pendingCapabilityProviderList.contains(capabilityProvider)) {
            pendingCapabilityProviderList.remove(capabilityProvider);
        } else {
            pendingCapabilityProviderList.add(capabilityProvider);
        }
    }

    public List<CapabilityProviderCapability> getPendingCapabilityProviders() {
        return Collections.unmodifiableList(pendingCapabilityProviderList);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public boolean isSatisfied() {
        return satisfied;
    }

    public void setSatisfied(boolean satisfied) {
        this.satisfied = satisfied;
    }

    /**
     * Returns 'true' if this component can be satisfied.
     * <p>
     * A {@code StartupComponent} can be satisfied if all the following conditions are valid.
     * 1) If there no pending capability registrations.
     * 2) If the {@code RequiredCapabilityListener} OSGi service is available.
     * 3) If there are no pending {@code CapabilityProvider} OSGi service registrations.
     *
     * @return 'true' if this component can be satisfied, or else 'false'.
     */
    public boolean isSatisfiable() {
        return !satisfied &&
                pendingCapabilityList.size() == 0 &&
                listener != null &&
                pendingCapabilityProviderList.size() == 0;
    }

    /**
     * Returns 'true' if this component in in the pending state.
     * <p>
     * A {@code StartupComponents} becomes pending if either of the following conditions met.
     * 1) If there are pending capability registrations,
     * 2) If there are pending {@code CapabilityProvider} service registrations,
     * 3) If the {@code RequiredCapabilityListener} is not yet registered.
     *
     * @return 'true' if the component is in the pending state.
     */
    public boolean isPending() {
        return !satisfied;
    }

    /**
     * Checks whether the given components is equal to this component.
     * <p>
     * Two components are equal if their names and the bundles are equal.
     *
     * @param obj Startup component to checked.
     * @return 'true' if the given component is equal to this component.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof StartupComponent)) {
            return false;
        }

        StartupComponent other = (StartupComponent) obj;
        return this.getName().equals(other.getName()) && (this.bundle.equals(other.bundle));
    }

    public int hashCode() {
        assert false;
        return 10;
    }
}
