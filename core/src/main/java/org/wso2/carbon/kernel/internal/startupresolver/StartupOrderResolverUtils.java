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
import org.wso2.carbon.kernel.internal.startupresolver.beans.Capability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.CapabilityProviderCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.OSGiServiceCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.StartupComponent;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.utils.manifest.ManifestElement;
import org.wso2.carbon.kernel.utils.manifest.ManifestElementParserException;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME_SPLIT_CHAR;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CARBON_COMPONENT_HEADER;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.DEPENDENT_COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OBJECT_CLASS;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.REQUIRED_SERVICE;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.SERVICE_COUNT;

/**
 * This class contains utility methods required for the StartupOrderResolver.
 *
 * @since 5.1.0
 */
class StartupOrderResolverUtils {

    private StartupOrderResolverUtils() {
        throw new AssertionError("Instantiating utility class...");

    }

    static Boolean isCarbonComponentHeaderPresent(Bundle bundle) {
        return AccessController.doPrivileged((PrivilegedAction<Boolean>) () ->
                bundle.getHeaders().get(CARBON_COMPONENT_HEADER) != null);
    }

    /**
     * Creates {@code ManifestElement} instances from CARBON_COMPONENT_HEADER in the given bundle.
     *
     * @param bundle from the which the header value should retrieved.
     * @return the created list of {@code ManifestElement} instances
     */
    static List<ManifestElement> createManifestElements(Bundle bundle) {
        String headerValue = AccessController.doPrivileged((PrivilegedAction<String>) () ->
                bundle.getHeaders().get(CARBON_COMPONENT_HEADER));

        try {
            return ManifestElement.parseHeader(CARBON_COMPONENT_HEADER, headerValue, bundle);
        } catch (ManifestElementParserException e) {
            String message = "Error occurred while parsing the " + CARBON_COMPONENT_HEADER + " header in bundle(" +
                    bundle.getSymbolicName() + ":" + bundle.getVersion() + "). " + "Header value: " + headerValue;
            throw new StartOrderResolverException(message, e);
        }
    }

    /**
     * Process all the Startup-Component manifest header elements and creates {@code StartupComponent} instances
     * for each and every ManifestElement.
     *
     * @param manifestElementList a list of {@code ManifestElement} whose header name is Startup-Component.
     */
    static List<StartupComponent> createStartupComponents(List<ManifestElement> manifestElementList) {
        // Create StartupComponents from the manifest elements.
        return manifestElementList.stream()
                .map(StartupOrderResolverUtils::getStartupComponent)
                .collect(Collectors.toList());
    }

    static List<CapabilityProviderCapability> createCapabilityProviders(List<ManifestElement> manifestElementList) {
        return manifestElementList.stream()
                .filter(manifestElement -> CapabilityProvider.class.getName().equals(
                        getObjectClassName(manifestElement)))
                .map(manifestElement -> {
                    // Processing CapabilityProvider OSGi service
                    String providedCapabilityName = getMandatoryManifestElementAttribute(
                            CAPABILITY_NAME, manifestElement, true);

                    return new CapabilityProviderCapability(
                            getObjectClassName(manifestElement),
                            Capability.CapabilityType.OSGi_SERVICE,
                            Capability.CapabilityState.EXPECTED,
                            providedCapabilityName,
                            manifestElement.getBundle());
                })
                .collect(Collectors.toList());
    }

    static List<OSGiServiceCapability> createOSGiServiceCapabilities(List<ManifestElement> manifestElementList) {
        // Handle other OSGi service capabilities
        return manifestElementList
                .stream()
                .filter(manifestElement -> !CapabilityProvider.class.getName().equals(
                        getObjectClassName(manifestElement)) &&
                        !RequiredCapabilityListener.class.getName().equals(getObjectClassName(manifestElement)))
                // Creating a Capability from the manifestElement
                .flatMap(manifestElement -> {
                    String capabilityName = getObjectClassName(manifestElement);

                    String serviceCountStr = getOptionalManifestElementAttribute(SERVICE_COUNT, manifestElement);

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

                    // Create specified  number of OSGi service components and adding them to a list.
                    List<OSGiServiceCapability> osgiServiceCapabilityList = new ArrayList<>(serviceCount);
                    IntStream.range(0, serviceCount)
                            .forEach(count -> {
                                OSGiServiceCapability osgiServiceCapability = new OSGiServiceCapability(
                                        capabilityName,
                                        Capability.CapabilityType.OSGi_SERVICE,
                                        Capability.CapabilityState.EXPECTED,
                                        manifestElement.getBundle());

                                // Check whether a dependent-component-key or dependent-component-name
                                // property is specified.
                                String dependentComponentName = getOptionalManifestElementAttribute(
                                        DEPENDENT_COMPONENT_NAME, manifestElement);

                                if (dependentComponentName != null && !dependentComponentName.equals("")) {
                                    osgiServiceCapability.setDependentComponentName(dependentComponentName.trim());
                                }
                                osgiServiceCapabilityList.add(osgiServiceCapability);
                            });

                    return osgiServiceCapabilityList.stream();
                })
                .collect(Collectors.toList());
    }

    static void logPendingComponentDetails(Logger logger, List<StartupComponent> pendingComponents) {
        pendingComponents
                .forEach(startupComponent -> {
                    List<Capability> pendingCapabilities = startupComponent.getPendingCapabilities();

                    pendingCapabilities
                            .forEach(provideCapability ->
                                    logPendingCapabilityDetails(logger, startupComponent, provideCapability)
                            );

                });
    }

    static void logPendingRequiredCapabilityListenerServiceDetails(
            Logger logger,
            List<StartupComponent> componentsWithPendingListeners) {

        componentsWithPendingListeners
                .forEach(startupComponent ->
                        logger.warn("Waiting for RequiredCapabilityListener " +
                                        "OSGi Service from bundle({}:{}). component-key: {}",
                                startupComponent.getBundle().getSymbolicName(),
                                startupComponent.getBundle().getVersion(),
                                startupComponent.getName()));
    }

    static void logPendingCapabilityProviderServiceDetails(
            Logger logger,
            List<CapabilityProviderCapability> pendingCapabilityProviderList) {

        pendingCapabilityProviderList
                .forEach(capabilityProvider ->
                        logger.warn("Waiting for CapabilityProvider OSGi service " +
                                        "from bundle({}:{}). Provided capability name: {} ",
                                capabilityProvider.getBundle().getSymbolicName(),
                                capabilityProvider.getBundle().getVersion(),
                                capabilityProvider.getProvidedCapabilityName()));

    }

    private static void logPendingCapabilityDetails(Logger logger,
                                                    StartupComponent startupComponent,
                                                    Capability provideCapability) {
        if (provideCapability.getState() == Capability.CapabilityState.EXPECTED) {
            logger.warn("Startup component {} from bundle({}:{}) is in the " +
                            "pending state until Capability {} from " +
                            "bundle({}:{}) is available as an OSGi service. Refer the Startup Order " +
                            "Resolver documentation for information.",
                    startupComponent.getName(),
                    startupComponent.getBundle().getSymbolicName(),
                    startupComponent.getBundle().getVersion(),
                    provideCapability.getName(),
                    provideCapability.getBundle().getSymbolicName(),
                    provideCapability.getBundle().getVersion());
        } else {
            logger.warn("Startup component {} from bundle({}:{}) is in the " +
                            "pending state, because of the Capability {} from " +
                            "bundle({}:{}). If you've registered this capability as an OSGi service, you need to " +
                            "declare it using the Carbon-Component manifest header. Refer the Startup Order " +
                            "Resolver documentation for information.",
                    startupComponent.getName(),
                    startupComponent.getBundle().getSymbolicName(),
                    startupComponent.getBundle().getVersion(),
                    provideCapability.getName(),
                    provideCapability.getBundle().getSymbolicName(),
                    provideCapability.getBundle().getVersion());
        }
    }

    /**
     * Create a {@code StartupComponent} from he manifest element.
     *
     * @param manifestElement {@code ManifestElement} from which the {@code StartupComponent} is created.
     * @return the created {@code StartupComponent}.
     */
    private static StartupComponent getStartupComponent(ManifestElement manifestElement) {
        String componentName = getMandatoryManifestElementAttribute(COMPONENT_NAME, manifestElement, true);
        String requiredServices = getMandatoryManifestElementAttribute(REQUIRED_SERVICE, manifestElement, true);
        String[] requiredServiceArray = requiredServices != null ?
                requiredServices.split(CAPABILITY_NAME_SPLIT_CHAR) : new String[0];
        List<String> requiredServicesList = Arrays.asList(requiredServiceArray)
                .stream()
                .map(String::trim)
                .collect(Collectors.toList());

        StartupComponent startupComponent = new StartupComponent(componentName, manifestElement.getBundle());
        startupComponent.addRequiredServices(requiredServicesList);
        return startupComponent;
    }

    /**
     * Returns the attribute value of the specified key from the {@code ManifestElement}.
     *
     * @param attributeKey    key of the manifest element attribute
     * @param manifestElement {@code ManifestElement} object from which we lookup for attributes
     * @param mandatory       states whether this is a required attribute or not. If a required attribute value is null,
     *                        then this method throws a {@code StartOrderResolverException}
     * @return requested attribute value
     */
    private static String getMandatoryManifestElementAttribute(String attributeKey,
                                                               ManifestElement manifestElement, boolean mandatory) {
        String value = manifestElement.getAttribute(attributeKey);
        if ((value == null || value.equals("")) && mandatory) {
            throw new StartOrderResolverException(attributeKey + " attribute value is missing in " +
                    manifestElement.getManifestHeaderName() + " header of bundle(" +
                    manifestElement.getBundle().getSymbolicName() + ":" +
                    manifestElement.getBundle().getVersion() + ")");
        }

        return value != null ? value.trim() : null;

    }

    private static String getOptionalManifestElementAttribute(String attributeKey, ManifestElement manifestElement) {
        String value = manifestElement.getAttribute(attributeKey);
        return value != null ? value.trim() : null;

    }

    /**
     * Extracts the "objectClass" manifest element attribute from the give {@code ManifestElement}.
     *
     * @param manifestElement {@code ManifestElement} from which the "objectClass" is to be extracted.
     * @return the value of the "objectClass" attribut.
     */
    private static String getObjectClassName(ManifestElement manifestElement) {
        String className = manifestElement.getAttribute(OBJECT_CLASS);

        if (className == null || className.equals("")) {
            throw new StartOrderResolverException("objectClass cannot be empty. Bundle-SymbolicName: " +
                    manifestElement.getBundle().getSymbolicName());
        }

        return className;
    }
}
