/*
 * Copyright (c) OSGi Alliance (2009, 2010). All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osgi.service.jndi;

/**
 * Constants for the JNDI implementation.
 */
public class JNDIConstants {
    /**
     * This service property is set by JNDI Providers that publish URL Context
     * Factories as OSGi Services. The value of this property should be the URL
     * scheme that is supported by the published service.
     */
    public static final String JNDI_URLSCHEME = "osgi.jndi.url.scheme";

    /**
     * This service property is set on an OSGi service to provide a name that
     * can be used to locate the service other than the service interface name.
     */
    public static final String JNDI_SERVICENAME = "osgi.jndi.service.name";

    /**
     * This JNDI environment property can be used by a JNDI client to indicate
     * the caller's BundleContext. This property can be set and passed to an
     * InitialContext constructor. This property is only useful in the
     * "traditional" mode of JNDI.
     */
    public static final String BUNDLE_CONTEXT = "osgi.service.jndi.bundleContext";

    private JNDIConstants() {
        // non-instantiable
    }
}
