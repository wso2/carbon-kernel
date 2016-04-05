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
import org.wso2.carbon.kernel.internal.startupresolver.beans.OSGiServiceCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.RequiredCapabilityListenerCapability;
import org.wso2.carbon.kernel.internal.startupresolver.beans.StartupComponent;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.utils.manifest.ManifestElement;
import org.wso2.carbon.kernel.utils.manifest.ManifestElementParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME_SPLIT_CHAR;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.DEPENDENT_COMPONENT_KEY;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.DEPENDENT_COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OBJECT_CLASS;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OBJECT_CLASS_LIST_STRING;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OSGI_SERVICE;

/**
 * @since 5.1.0
 */
class StartupOrderResolverUtils {
    private static final Logger logger = LoggerFactory.getLogger(StartupOrderResolverUtils.class);

    /**
     * Checks whether the given bundle contains at least one of the support manifest headers.
     *
     * @param bundle                   a bundle from which the headers are extracted.
     * @param supportedManifestHeaders list of supported manifest header names.
     * @return true if the bundle contains at least one support manifest header.
     */
    static boolean isSupportedManifestHeaderExists(Bundle bundle, List<String> supportedManifestHeaders) {
        Dictionary<String, String> headerDictionary = bundle.getHeaders();
        for (String manifestHeader : supportedManifestHeaders) {
            if (headerDictionary.get(manifestHeader) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a {@code StartupComponent} from he manifest element.
     *
     * @param manifestElement {@code ManifestElement} from which the {@code StartupComponent} is created.
     * @return the created {@code StartupComponent}.
     */
    static StartupComponent getStartupComponentBean(ManifestElement manifestElement) {
        String componentName = manifestElement.getValue();
        String requiredServices = getManifestElementAttribute("required-service", manifestElement, true);
        String[] requiredServiceArray = requiredServices != null ?
                requiredServices.split(CAPABILITY_NAME_SPLIT_CHAR) : new String[0];
        List<String> requiredServicesList = Arrays.asList(requiredServiceArray)
                .stream()
                .map(String::trim)
                .collect(Collectors.toList());

        StartupComponent startupComponent = new StartupComponent(componentName, manifestElement.getBundle());
        startupComponent.setRequiredServiceList(requiredServicesList);
        return startupComponent;
    }

    /**
     * Extract manifest elements from the supported manifest headers.
     *
     * @param bundle                   a bundle instance from which the headers are retrieved.
     * @param supportedManifestHeaders list of supported manifest header names.
     * @return a list of created {@code ManifestElement} instances.
     */
    static List<ManifestElement> extractManifestElements(Bundle bundle, List<String> supportedManifestHeaders) {
        Dictionary<String, String> headerDictionary = bundle.getHeaders();
        List<ManifestElement> manifestElementList = new ArrayList<>();

        supportedManifestHeaders
                .stream()
                .forEach(manifestHeader -> {
                    if (headerDictionary.get(manifestHeader) != null) {
                        processManifestHeader(manifestHeader, headerDictionary.get(manifestHeader), bundle)
                                .stream()
                                .forEach(manifestElementList::add);
                    }
                });

        return manifestElementList;
    }

    /**
     * Returns the attribute value of the specified key from the {@code ManifestElement}.
     *
     * @param attributeKey    key of the manifest element attribute
     * @param manifestElement {@code ManifestElement} object from which we lookup for attributes
     * @param mandatory       states whether this is a required attribute or not. If a required attribute value is null,
     *                        then this method throws a {@code RuntimeException}
     * @return requested attribute value
     */
    private static String getManifestElementAttribute(String attributeKey,
                                                      ManifestElement manifestElement, boolean mandatory) {
        String value = manifestElement.getAttribute(attributeKey);
        if ((value == null || value.equals("")) && mandatory) {
            throw new RuntimeException(attributeKey + " attribute value is missing in " +
                    manifestElement.getManifestHeaderName() + " header of bundle(" +
                    manifestElement.getBundle().getSymbolicName() + ":" +
                    manifestElement.getBundle().getVersion() + ")");
        }

        return value != null ? value.trim() : null;

    }

    /**
     * Extracts the "objectClass" manifest element attribute from the give {@code ManifestElement}
     *
     * @param manifestElement {@code ManifestElement} from which the "objectClass" is to be extracted.
     * @return the value of the "objectClass" attribut.
     */
    private static String getObjectClassName(ManifestElement manifestElement) {
        String className = manifestElement.getAttribute(OBJECT_CLASS);
        if (className == null) {
            className = manifestElement.getAttribute(OBJECT_CLASS_LIST_STRING);
        }

        if (className == null || className.equals("")) {
            throw new RuntimeException("objectClass cannot be empty. Bundle-SymbolicName: " +
                    manifestElement.getBundle().getSymbolicName());
        }

        return className;
    }

    private static List<ManifestElement> processManifestHeader(String headerName, String headerValue, Bundle bundle) {
        try {
            return ManifestElement.parseHeader(headerName, headerValue, bundle);
        } catch (ManifestElementParserException e) {
            String message = "Error occurred while parsing the " + headerName + " header in bundle(" +
                    bundle.getSymbolicName() + ":" + bundle.getVersion() + "). " + "Header value: " + headerValue;
            throw new RuntimeException(message);
        }
    }

    static class RequireCapabilityListenerProcessor implements
            Function<ManifestElement, Optional<Capability>> {

        @Override
        public Optional<Capability> apply(ManifestElement manifestElement) {
            String capabilityType = manifestElement.getValue();

            if (OSGI_SERVICE.equals(capabilityType) &&
                    RequiredCapabilityListener.class.getName().equals(getObjectClassName(manifestElement))) {

                // Processing RequiredCapabilityListener capability for backward compatibility
                String capabilityNames = getManifestElementAttribute(CAPABILITY_NAME, manifestElement, false);

                // This check is required due to https://github.com/bndtools/bnd/issues/1364. Once this issue is
                // we can get-rid of the following check and make the capability-name compulsory.
                if (capabilityNames == null) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Discarding manifest element with RequiredCapabilityListener from bundle({}:{}) " +
                                        "due to missing capability-name property.",
                                manifestElement.getBundle().getSymbolicName(),
                                manifestElement.getBundle().getVersion());
                    }

                    return Optional.empty();

                } else {
                    String componentName = getManifestElementAttribute(COMPONENT_NAME, manifestElement, true);
                    String[] requiredServiceArray = capabilityNames.split(CAPABILITY_NAME_SPLIT_CHAR);
                    List<String> requiredServicesList = Arrays.asList(requiredServiceArray)
                            .stream()
                            .map(String::trim)
                            .collect(Collectors.toList());

                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating a RequiredCapabilityListenerCapability from manifest element in " +
                                        "bundle({}:{}), with the componentName - {} and CapabilityNames - {}. ",
                                manifestElement.getBundle().getSymbolicName(),
                                manifestElement.getBundle().getVersion(), componentName, capabilityNames);
                    }

                    return Optional.of(new RequiredCapabilityListenerCapability(getObjectClassName(manifestElement),
                            Capability.CapabilityType.OSGi_SERVICE, componentName, requiredServicesList,
                            manifestElement.getBundle()));
                }

            } else {
                return Optional.empty();
            }
        }
    }

    static class CapabilityProviderProcessor implements Function<ManifestElement, Optional<Capability>> {

        @Override
        public Optional<Capability> apply(ManifestElement manifestElement) {
            String capabilityType = manifestElement.getValue();
            if (OSGI_SERVICE.equals(capabilityType) &&
                    CapabilityProvider.class.getName().equals(getObjectClassName(manifestElement))) {

                // Processing CapabilityProvider capability
                String providedCapabilityName = getManifestElementAttribute(CAPABILITY_NAME, manifestElement, false);
                // This check is required due to https://github.com/bndtools/bnd/issues/1364. Once this issue is
                // we can get-rid of the following check and make the capability-name compulsory.
                if (providedCapabilityName == null) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Discarding manifest element with CapabilityProvider from bundle({}:{}) " +
                                        "due to missing capability-name property.",
                                manifestElement.getBundle().getSymbolicName(),
                                manifestElement.getBundle().getVersion());
                    }

                    return Optional.empty();

                } else {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Creating a CapabilityProviderCapability from manifest element in " +
                                        "bundle({}:{}), with CapabilityName - {}. ",
                                manifestElement.getBundle().getSymbolicName(),
                                manifestElement.getBundle().getVersion(),
                                providedCapabilityName);
                    }

                    return Optional.of(new CapabilityProviderCapability(getObjectClassName(manifestElement),
                            Capability.CapabilityType.OSGi_SERVICE, providedCapabilityName,
                            manifestElement.getBundle()));
                }
            } else {
                return Optional.empty();
            }
        }
    }

    static class OSGiServiceCapabilityProcessor implements Function<ManifestElement, Optional<Capability>> {

        @Override
        public Optional<Capability> apply(ManifestElement manifestElement) {
            String capabilityType = manifestElement.getValue();
            if (OSGI_SERVICE.equals(capabilityType)) {
                // Process rest of the OSGi service capabilities.
                String capabilityName = getObjectClassName(manifestElement);

                OSGiServiceCapability osgiServiceCapability = new OSGiServiceCapability(capabilityName,
                        Capability.CapabilityType.OSGi_SERVICE, manifestElement.getBundle());

                // Check whether a dependent-component-key or dependent-component-name property is specified.
                String dependentComponentName = getManifestElementAttribute(
                        DEPENDENT_COMPONENT_KEY, manifestElement, false);

                if (dependentComponentName == null || dependentComponentName.equals("")) {
                    dependentComponentName = getManifestElementAttribute(
                            DEPENDENT_COMPONENT_NAME, manifestElement, false);
                }

                if (dependentComponentName != null && !dependentComponentName.equals("")) {
                    osgiServiceCapability.setDependentComponentName(dependentComponentName.trim());
                }

                return Optional.of(osgiServiceCapability);
            } else {
                return Optional.empty();
            }
        }
    }
}
