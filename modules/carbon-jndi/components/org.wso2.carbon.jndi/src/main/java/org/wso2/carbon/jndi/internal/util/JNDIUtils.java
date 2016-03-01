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
package org.wso2.carbon.jndi.internal.util;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Optional;
import java.util.stream.Stream;

import static org.wso2.carbon.jndi.internal.util.LambdaExceptionUtil.rethrowFunction;

/**
 * This class contains utility methods used in this carbon-jndi implementation.
 */
public class JNDIUtils {

    private static final Logger logger = LoggerFactory.getLogger(JNDIUtils.class);

    /**
     * @param builderOptional an {@code Optional} describing InitialContextFactoryBuilder instance.
     * @param environment     The possibly null environment
     *                        specifying information to be used in the creation
     *                        of the initial context.
     * @return an {@code Optional} describing the created InitialContextFactory instance.
     */
    public static Optional<InitialContextFactory> getContextFactory(
            Optional<InitialContextFactoryBuilder> builderOptional,
            Hashtable<?, ?> environment) {

        return builderOptional.map(builder -> {
            try {
                return builder.createInitialContextFactory(environment);
            } catch (NamingException ignored) {
                // According to the OSGi JNDI service specification this exception should not thrown to the caller.
                logger.debug(ignored.getMessage(), ignored);
                return null;
            }
        });
    }

    /**
     * @param serviceRefCollection Collection of {@code ServiceReference} objects of InitialContextFactoryBuilders.
     * @param environment          The possibly null environment
     *                             specifying information to be used in the creation
     *                             of the initial context.
     * @return an {@code Optional} describing created initial context from the builder.
     * @throws NamingException upon any error that occurs during context creation
     */
    public static Optional<Context> getInitialContextFromBuilder(
            BundleContext bundleContext,
            Collection<ServiceReference<InitialContextFactoryBuilder>> serviceRefCollection,
            Hashtable<?, ?> environment) throws NamingException {

        return serviceRefCollection
                .stream()
                .sorted(new ServiceRankComparator())
                .map(serviceReference -> getService(bundleContext, serviceReference))
                .map(builderOptional -> getContextFactory(builderOptional, environment))
                .flatMap(factoryOptional -> factoryOptional.map(Stream::of).orElseGet(Stream::empty))
                .map(rethrowFunction(factory -> factory.getInitialContext(environment)))
                .findFirst();
    }

    /**
     * @param serviceRefCollection collection of {@code ServiceReference} objects of InitialContextFactory.
     * @param environment          The possibly null environment
     *                             specifying information to be used in the creation
     *                             of the initial context.
     * @return an {@code Optional} describing created initial context from the factory.
     * @throws NamingException upon any error that occurs during context creation
     */
    public static Optional<Context> getInitialContextFromFactory(
            BundleContext bundleContext,
            Collection<ServiceReference<InitialContextFactory>> serviceRefCollection,
            Hashtable<?, ?> environment) throws NamingException {

        return serviceRefCollection
                .stream()
                .sorted(new ServiceRankComparator())
                .map(serviceReference -> getService(bundleContext, serviceReference))
                .flatMap(factoryOptional -> factoryOptional.map(Stream::of).orElseGet(Stream::empty))
                .map(rethrowFunction(contextFactory -> contextFactory.getInitialContext(environment)))
                .findFirst();
    }

    /**
     * @param clazz  The class under whose name the service was registered. Must not be null.
     * @param filter The filter expression or null for all services.
     * @param <S>    Type of Service
     * @return a collection of {@code ServiceReference} objects of the given type of the Service S.
     */
    public static <S> Collection<ServiceReference<S>> getServiceReferences(BundleContext bundleContext,
                                                                           Class<S> clazz,
                                                                           String filter) {
        try {
            return bundleContext.getServiceReferences(clazz, filter);
        } catch (InvalidSyntaxException ignored) {
            // This branch cannot be invoked. Since the filter is always correct.
            // However I am logging the exception in case
            logger.error("Filter syntax is invalid: " + filter, ignored);
            return Collections.<ServiceReference<S>>emptyList();
        }
    }

    /**
     * @param serviceReference A reference to the service of type S.
     * @param <S> Type of Service.
     * @return an {@code Optional} describing the service of type S.
     */
    public static <S> Optional<S> getService(BundleContext bundleContext,
                                             ServiceReference<S> serviceReference) {
        return Optional.ofNullable(bundleContext.getService(serviceReference));
    }
}
