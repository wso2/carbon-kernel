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
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME_SPLIT_CHAR;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CARBON_COMPONENT_HEADER;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.DEPENDENT_COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OBJECT_CLASS;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.REQUIRED_BY_COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.REQUIRED_SERVICE;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.SERVICE_COUNT;
import static org.wso2.carbon.kernel.utils.StringUtils.getNonEmptyStringAfterTrim;

/**
 * This class contains utility methods required for the StartupOrderResolver.
 *
 * @since 5.1.0
 */
class StartupOrderResolverUtils {

    private StartupOrderResolverUtils() {
        throw new AssertionError("Instantiating utility class...");

    }

    static Predicate<ManifestElement> capabilityProviderElementPredicate =
            manifestElement -> CapabilityProvider.class.getName().equals(
                    getObjectClassName(manifestElement));

    static Predicate<ManifestElement> requiredCapabilityListenerElementPredicate =
            manifestElement -> RequiredCapabilityListener.class.getName().equals(
                    getObjectClassName(manifestElement));

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
    static List<ManifestElement> getManifestElements(Bundle bundle) {
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
     * Create a {@code StartupComponent} from he manifest element.
     *
     * @param manifestElement {@code ManifestElement} from which the {@code StartupComponent} is created.
     * @return the created {@code StartupComponent}.
     */
    static StartupComponent getStartupComponent(ManifestElement manifestElement) {
        String componentName = getNonEmptyStringAfterTrim(manifestElement.getAttribute(COMPONENT_NAME))
                .orElseThrow(
                        () -> new StartOrderResolverException(COMPONENT_NAME + " attribute value is missing in " +
                                manifestElement.getManifestHeaderName() + " header of bundle(" +
                                manifestElement.getBundle().getSymbolicName() + ":" +
                                manifestElement.getBundle().getVersion() + ")"));

        String requiredServices = getNonEmptyStringAfterTrim(manifestElement.getAttribute(REQUIRED_SERVICE))
                .orElseThrow(
                        () -> new StartOrderResolverException(REQUIRED_SERVICE + " attribute value is missing in " +
                                manifestElement.getManifestHeaderName() + " header of bundle(" +
                                manifestElement.getBundle().getSymbolicName() + ":" +
                                manifestElement.getBundle().getVersion() + ")"));

        String[] requiredServiceArray = requiredServices.split(CAPABILITY_NAME_SPLIT_CHAR);
        List<String> requiredServicesList = Arrays.asList(requiredServiceArray)
                .stream()
                .map(String::trim)
                .collect(Collectors.toList());

        StartupComponent startupComponent = new StartupComponent(componentName, manifestElement.getBundle());
        startupComponent.addRequiredServices(requiredServicesList);
        return startupComponent;
    }

    static CapabilityProviderCapability getCapabilityProviderCapability(ManifestElement manifestElement) {
        // Processing CapabilityProvider OSGi service
        String providedCapabilityName = getNonEmptyStringAfterTrim(manifestElement.getAttribute(CAPABILITY_NAME))
                .orElseThrow(
                        () -> new StartOrderResolverException(CAPABILITY_NAME + " attribute value is missing in " +
                                manifestElement.getManifestHeaderName() + " header of bundle(" +
                                manifestElement.getBundle().getSymbolicName() + ":" +
                                manifestElement.getBundle().getVersion() + ")"));

        return new CapabilityProviderCapability(
                getObjectClassName(manifestElement),
                Capability.CapabilityType.OSGi_SERVICE,
                Capability.CapabilityState.EXPECTED,
                providedCapabilityName,
                manifestElement.getBundle());
    }

    static List<OSGiServiceCapability> getOSGiServiceCapabilities(ManifestElement manifestElement) {
        // Get the value of the serviceCount manifest attribute, if any. Default value is 1.
        int serviceCount = getNonEmptyStringAfterTrim(manifestElement.getAttribute(SERVICE_COUNT))
                .map(serviceCountStr -> {
                    try {
                        return Integer.parseInt(serviceCountStr.trim());
                    } catch (NumberFormatException e) {
                        throw new StartOrderResolverException("Invalid value for serviceCount manifest " +
                                "attribute in bundle(" + manifestElement.getBundle().getSymbolicName() +
                                ":" + manifestElement.getBundle().getVersion() + ")", e);
                    }
                })
                .orElse(1);

        // Create specified number of OSGi service components and adding them to a list.
        List<OSGiServiceCapability> osgiServiceCapabilityList = new ArrayList<>(serviceCount);
        IntStream.range(0, serviceCount)
                .forEach(count -> {
                    OSGiServiceCapability osgiServiceCapability = new OSGiServiceCapability(
                            getObjectClassName(manifestElement),
                            Capability.CapabilityType.OSGi_SERVICE,
                            Capability.CapabilityState.EXPECTED,
                            manifestElement.getBundle());

                    // Check whether requiredByComponent property is specified.
                    getNonEmptyStringAfterTrim(manifestElement.getAttribute(REQUIRED_BY_COMPONENT_NAME))
                            .ifPresent(requiredByComponentNameStr ->
                                    addRequiredByComponentNames(osgiServiceCapability, requiredByComponentNameStr)
                            );

                    // Check whether dependentComponentName property is specified. Backward compatibility.
                    getNonEmptyStringAfterTrim(manifestElement.getAttribute(DEPENDENT_COMPONENT_NAME))
                            .ifPresent(requiredByComponentNameStr ->
                                    addRequiredByComponentNames(osgiServiceCapability, requiredByComponentNameStr)
                            );

                    osgiServiceCapabilityList.add(osgiServiceCapability);
                });

        return osgiServiceCapabilityList;
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
                        logger.warn("Waiting for a RequiredCapabilityListener " +
                                        "OSGi Service from bundle({}:{}) with componentName: {}",
                                startupComponent.getBundle().getSymbolicName(),
                                startupComponent.getBundle().getVersion(),
                                startupComponent.getName()));
    }

    static void logPendingCapabilityProviderServiceDetails(
            Logger logger,
            List<CapabilityProviderCapability> pendingCapabilityProviderList) {

        pendingCapabilityProviderList
                .forEach(capabilityProvider -> {
                    if (capabilityProvider.getState() == Capability.CapabilityState.EXPECTED) {
                        logger.warn("Waiting for a CapabilityProvider OSGi service to be registered " +
                                        "from bundle({}:{}) with providedCapabilityName: {}. " +
                                        "Refer the Startup Order Resolver documentation for more information.",
                                capabilityProvider.getBundle().getSymbolicName(),
                                capabilityProvider.getBundle().getVersion(),
                                capabilityProvider.getProvidedCapabilityName());
                    } else {
                        logger.warn("Looks like you've registered a CapabilityProvider OSGi service from " +
                                        "bundle({}:{}) with the providedCapabilityName: {}, but you haven't " +
                                        "declared it in the pom.xml using Carbon-Component manifest header. " +
                                        "Refer the Startup Order Resolver documentation for more information.",
                                capabilityProvider.getBundle().getSymbolicName(),
                                capabilityProvider.getBundle().getVersion(),
                                capabilityProvider.getProvidedCapabilityName());
                    }
                });

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
     * Extracts the "objectClass" manifest element attribute from the give {@code ManifestElement}.
     *
     * @param manifestElement {@code ManifestElement} from which the "objectClass" is to be extracted.
     * @return the value of the "objectClass" attribute.
     */
    private static String getObjectClassName(ManifestElement manifestElement) {
        return getNonEmptyStringAfterTrim(manifestElement.getAttribute(OBJECT_CLASS))
                .orElseThrow(() -> new StartOrderResolverException("objectClass cannot be empty. " +
                        "Bundle-SymbolicName: " + manifestElement.getBundle().getSymbolicName()));
    }

    private static void addRequiredByComponentNames(OSGiServiceCapability osgiServiceCapability,
                                                    String requiredByComponentNameStr) {
        Arrays.asList(requiredByComponentNameStr.split(CAPABILITY_NAME_SPLIT_CHAR))
                .stream()
                .map(String::trim)
                .filter(componentName -> componentName != null && componentName.length() > 0)
                .forEach(osgiServiceCapability::setRequiredByComponentName);
    }
}
