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
import org.wso2.carbon.kernel.internal.startupresolver.beans.StartupComponent;
import org.wso2.carbon.kernel.utils.manifest.ManifestElement;
import org.wso2.carbon.kernel.utils.manifest.ManifestElementParserException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.CAPABILITY_NAME_SPLIT_CHAR;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.COMPONENT_NAME;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.OBJECT_CLASS;
import static org.wso2.carbon.kernel.internal.startupresolver.StartupResolverConstants.REQUIRED_SERVICE;

/**
 * @since 5.1.0
 */
class StartupOrderResolverUtils {

    private StartupOrderResolverUtils() {
        throw new AssertionError("Instantiating utility class...");

    }


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
        String componentName = getManifestElementAttribute(COMPONENT_NAME, manifestElement, true);
        String requiredServices = getManifestElementAttribute(REQUIRED_SERVICE, manifestElement, true);
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
     *                        then this method throws a {@code StartOrderResolverException}
     * @return requested attribute value
     */
    public static String getManifestElementAttribute(String attributeKey,
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

    /**
     * Extracts the "objectClass" manifest element attribute from the give {@code ManifestElement}.
     *
     * @param manifestElement {@code ManifestElement} from which the "objectClass" is to be extracted.
     * @return the value of the "objectClass" attribut.
     */
    public static String getObjectClassName(ManifestElement manifestElement) {
        String className = manifestElement.getAttribute(OBJECT_CLASS);

        if (className == null || className.equals("")) {
            throw new StartOrderResolverException("objectClass cannot be empty. Bundle-SymbolicName: " +
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
            throw new StartOrderResolverException(message, e);
        }
    }
}
