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

import java.util.Map;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.directory.Attributes;

/**
 * This interface defines the OSGi service interface for the JNDIProviderAdmin
 * service.
 * <p>
 * This service provides the ability to resolve JNDI References in a dynamic
 * fashion that does not require calls to
 * <code>NamingManager.getObjectInstance()</code>. The methods of this service
 * provide similar reference resolution, but rely on the OSGi Service Registry
 * in order to find <code>ObjectFactory</code> instances that can convert a
 * Reference to an Object.
 * <p>
 * This service will typically be used by OSGi-aware JNDI Service Providers.
 */
public interface JNDIProviderAdmin {

    /**
     * Resolve the object from the given reference.
     *
     * @param refInfo     Reference info
     * @param name        the JNDI name associated with this reference
     * @param context     the JNDI context associated with this reference
     * @param environment the JNDI environment associated with this JNDI context
     * @return an Object based on the reference passed in, or the original
     * reference object if the reference could not be resolved.
     * @throws Exception in the event that an error occurs while attempting to
     *                   resolve the JNDI reference.
     */
    public Object getObjectInstance(Object refInfo, Name name, Context context,
                                    Map<?, ?> environment) throws Exception;

    /**
     * Resolve the object from the given reference.
     *
     * @param refInfo     Reference info
     * @param name        the JNDI name associated with this reference
     * @param context     the JNDI context associated with this reference
     * @param environment the JNDI environment associated with this JNDI context
     * @param attributes  the jndi attributes to use when resolving this object
     * @return an Object based on the reference passed in, or the original
     * reference object if the reference could not be resolved.
     * @throws Exception in the event that an error occurs while attempting to
     *                   resolve the JNDI reference.
     */
    public Object getObjectInstance(Object refInfo, Name name, Context context,
                                    Map<?, ?> environment, Attributes attributes) throws Exception;
}