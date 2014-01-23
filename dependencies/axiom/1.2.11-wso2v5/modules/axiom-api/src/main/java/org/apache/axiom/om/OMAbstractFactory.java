/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axiom.om;

import org.apache.axiom.soap.SOAPFactory;

/**
 * Provides default instances for object model and meta factories.
 * <p>
 * The {@link #getMetaFactory()} method returns the default {@link OMMetaFactory} instance.
 * See the Javadoc of the {@link #getMetaFactory()} method for details about how this
 * instance is determined.
 * <p>
 * The {@link #getOMFactory()}, {@link #getSOAP11Factory()} and {@link #getSOAP12Factory()}
 * methods return default instances for plain XML, SOAP 1.1 and SOAP 1.2 object model factories.
 * They are convenience methods calling {@link #getMetaFactory()} and then delegating to the
 * returned {@link OMMetaFactory}.
 * <p>
 * Note that while {@link #getMetaFactory()} always returns the same instance, the other methods
 * may return new instances on every invocation, depending on the {@link OMMetaFactory}
 * implementation.
 * <p>
 */
public class OMAbstractFactory {
    public static final String META_FACTORY_NAME_PROPERTY = "org.apache.axiom.om.OMMetaFactory";

    private static final String DEFAULT_META_FACTORY_CLASS_NAME =
            "org.apache.axiom.om.impl.llom.factory.OMLinkedListMetaFactory";

    /**
     * The default {@link OMMetaFactory} instance determined by the system
     * property {@link #META_FACTORY_NAME_PROPERTY}, or if no such system
     * property is set, by the value of the
     * {@link #DEFAULT_META_FACTORY_CLASS_NAME} constant.
     */
    private static OMMetaFactory defaultMetaFactory;
    
    /**
     * The {@link OMMetaFactory} set through
     * {@link #setMetaFactory(OMMetaFactory)}. If this is <code>null</code>,
     * then {@link #defaultMetaFactory} will be returned by
     * {@link #getMetaFactory()}.
     */
    private static OMMetaFactory metaFactory;

    private OMAbstractFactory() {}

    /**
     * Explicitly set a meta factory instance. The new instance will be returned
     * by all subsequent calls to {@link #getMetaFactory()}. Note that this is
     * an application wide setting. More precisely, the configured meta factory
     * will be used by all classes loaded from the class loader where Axiom is
     * deployed and all its child class loaders. Therefore this method should be
     * used with care and only be invoked during the initialization of the
     * application.
     * <p>
     * When Axiom is deployed as a bundle in an OSGi environment, this method
     * will be used to inject the meta factory instance from the implementation
     * bundle.
     * 
     * @param newMetaFactory
     *            the new meta factory instance, or <code>null</code> to revert
     *            to the default meta factory instance determined by the
     *            <code>org.apache.axiom.om.OMMetaFactory</code> system property
     */
    public static void setMetaFactory(OMMetaFactory newMetaFactory) {
        metaFactory = newMetaFactory;
    }
    
    /**
     * Get the default meta factory instance. The default instance is determined
     * using the following algorithm:
     * <ol>
     * <li>If an instance has been set using
     * {@link #setMetaFactory(OMMetaFactory)}, then that instance is returned.
     * Note that this will be the case in an OSGi runtime, where
     * {@link #setMetaFactory(OMMetaFactory)} is invoked by a helper component
     * that is part of Axiom.
     * <li>If no instance has been set using
     * {@link #setMetaFactory(OMMetaFactory)}, then the implementation class is
     * determined by the <code>org.apache.axiom.om.OMMetaFactory</code> system
     * property.
     * <li>If the <code>org.apache.axiom.om.OMMetaFactory</code> system property
     * is not set, the meta factory for the LLOM implementation is returned.
     * </ol>
     * This method uses {@link System#getProperty(String)} to determine the
     * value of the <code>org.apache.axiom.om.OMMetaFactory</code> system
     * property. A {@link SecurityException} thrown by this method is simply
     * ignored and the default factory implementation is used.
     * 
     * @return the default OM factory instance
     * @throws OMException
     *             if the factory's implementation class can't be found or if
     *             the class can't be instantiated
     */
    public static OMMetaFactory getMetaFactory() {
        if (metaFactory != null) {
            return metaFactory;
        }
        
        if (defaultMetaFactory != null) {
            return defaultMetaFactory;
        }
        
        String omFactory;
        try {
            omFactory = System.getProperty(META_FACTORY_NAME_PROPERTY);
            if (omFactory == null || omFactory.isEmpty()) {
                omFactory = DEFAULT_META_FACTORY_CLASS_NAME;
            }
        } catch (SecurityException e) {
            omFactory = DEFAULT_META_FACTORY_CLASS_NAME;
        }

        try {
            defaultMetaFactory = (OMMetaFactory) Class.forName(omFactory).newInstance();
        } catch (InstantiationException e) {
            throw new OMException(e);
        } catch (IllegalAccessException e) {
            throw new OMException(e);
        } catch (ClassNotFoundException e) {
            throw new OMException(e);
        }
        return defaultMetaFactory;
    }
    
    /**
     * Get the default OM factory instance.
     *
     * @return the default OM factory instance
     * @throws OMException if the factory's implementation class can't be found
     *                     or if the class can't be instantiated
     */
    public static OMFactory getOMFactory() {
        return getMetaFactory().getOMFactory();
    }


    /**
     * Get the default SOAP 1.1 OM factory instance.
     *
     * @return the default SOAP 1.1 OM factory instance
     * @throws OMException if the factory's implementation class can't be found
     *                     or if the class can't be instantiated
     */
    public static SOAPFactory getSOAP11Factory() {
        return getMetaFactory().getSOAP11Factory();
    }


    /**
     * Get the default SOAP 1.2 OM factory instance.
     *
     * @return the default SOAP 1.2 OM factory instance
     * @throws OMException if the factory's implementation class can't be found
     *                     or if the class can't be instantiated
     */
    public static SOAPFactory getSOAP12Factory() {
        return getMetaFactory().getSOAP12Factory();
    }
}
