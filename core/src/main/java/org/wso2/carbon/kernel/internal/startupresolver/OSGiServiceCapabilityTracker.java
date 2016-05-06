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
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.internal.DataHolder;
import org.wso2.carbon.kernel.internal.startupresolver.beans.Capability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.CapabilityProviderCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.OSGiServiceCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.StartupComponent;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OBJECT_CLASS;

/**
 * Tracks OSGi Services by creating a ServiceTracker which tracks only services required by startup components.
 *
 * @since 5.1.0
 */
class OSGiServiceCapabilityTracker {
    private static final Logger logger = LoggerFactory.getLogger(OSGiServiceCapabilityTracker.class);

    private StartupComponentManager startupComponentManager;
    private ServiceTracker<Object, Object> capabilityServiceTracker;

    OSGiServiceCapabilityTracker(StartupComponentManager startupComponentManager) {
        this.startupComponentManager = startupComponentManager;
    }

    /**
     * Starts the ServiceTracker.
     */
    void startTracker() {
        Filter orFilter = getORFilter(getRequiredServiceList(startupComponentManager));
        capabilityServiceTracker = new ServiceTracker<>(DataHolder.getInstance().getBundleContext(), orFilter,
                new CapabilityServiceTrackerCustomizer());

        capabilityServiceTracker.open();
    }

    /**
     * Closes the ServiceTracker.
     */
    void closeTracker() {
        capabilityServiceTracker.close();
        capabilityServiceTracker = null;
        startupComponentManager = null;
    }

    /**
     * Returns a {@link List} of OSGi service keys required by startup components.
     *
     * @param startupComponentManager instance of the StartupComponentManager
     * @return a {@link List} of OSGi service keys
     */
    private List<String> getRequiredServiceList(StartupComponentManager startupComponentManager) {
        List<StartupComponent> startupComponentList = startupComponentManager.getPendingComponents();
        List<String> requiredServiceList = startupComponentList
                .stream()
                .flatMap(startupComponentBean -> startupComponentBean.getRequiredServiceList().stream())
                .distinct()
                .collect(Collectors.toList());

        // We need to track RequiredCapabilityListener services as well as CapabilityProvider service.
        requiredServiceList.add(RequiredCapabilityListener.class.getName());
        requiredServiceList.add(CapabilityProvider.class.getName());
        return requiredServiceList;
    }

    /**
     * Returns and instance of {@link Filter}.
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

        orFilterBuilder.append(")");

        BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
        try {
            return bundleContext.createFilter(orFilterBuilder.toString());
        } catch (InvalidSyntaxException e) {
            throw new StartOrderResolverException("Error occurred while creating the service filter", e);
        }
    }

    /**
     * Custom implementation of the {@link ServiceTrackerCustomizer} which handles registered
     * {@code RequiredCapabilityListener} services, {@code CapabilityProvider} services and
     * all the other required services.
     */
    private class CapabilityServiceTrackerCustomizer implements ServiceTrackerCustomizer<Object, Object> {

        @Override
        public Object addingService(ServiceReference<Object> reference) {
            Object serviceObject = DataHolder.getInstance().getBundleContext().getService(reference);
            String serviceInterfaceClassName = ((String[]) reference.getProperty(OBJECT_CLASS))[0];
            String serviceImplClassName = serviceObject.getClass().getName();
            Bundle bundle = reference.getBundle();

            if (RequiredCapabilityListener.class.getName().equals(serviceInterfaceClassName)) {

                String componentKey = (String) reference.getProperty(COMPONENT_NAME);
                if (componentKey == null || componentKey.equals("")) {
                    throw new StartOrderResolverException(COMPONENT_NAME + " value is missing in the services " +
                            "registered with the key " + serviceInterfaceClassName + ", implementation class name is "
                            + serviceImplClassName);
                }

                startupComponentManager.addRequiredCapabilityListener(
                        (RequiredCapabilityListener) serviceObject, componentKey, reference.getBundle());

            } else if (CapabilityProvider.class.getName().equals(serviceInterfaceClassName)) {
                CapabilityProvider provider = (CapabilityProvider) serviceObject;

                String capabilityName = (String) reference.getProperty(CAPABILITY_NAME);
                if (capabilityName == null || capabilityName.equals("")) {
                    throw new StartOrderResolverException(CAPABILITY_NAME + " value is missing in the services " +
                            "registered with the key " + serviceInterfaceClassName + ", implementation class name is "
                            + serviceImplClassName);
                }

                CapabilityProviderCapability capabilityProvider = new CapabilityProviderCapability(
                        CapabilityProvider.class.getName(), Capability.CapabilityType.OSGi_SERVICE,
                        capabilityName.trim(), bundle);
                startupComponentManager.addAvailableCapabilityProvider(capabilityProvider);

                IntStream.range(0, provider.getCount())
                        .forEach(count -> startupComponentManager.addExpectedRequiredCapability(
                                new OSGiServiceCapability(capabilityName.trim(),
                                        Capability.CapabilityType.OSGi_SERVICE, bundle)));

            } else {
                // this has to be a capability service
                logger.debug("Adding OSGi Service Capability. Service id: {}. Service implementation class: {}. ",
                        serviceInterfaceClassName,
                        serviceImplClassName);

                OSGiServiceCapability osgiServiceCapability = new OSGiServiceCapability(serviceInterfaceClassName,
                        Capability.CapabilityType.OSGi_SERVICE, bundle);
                startupComponentManager.addAvailableRequiredCapability(osgiServiceCapability);
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
