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
package org.wso2.carbon.jndi.internal.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.NoInitialContextException;
import javax.naming.directory.DirContext;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.wso2.carbon.jndi.internal.util.LambdaExceptionUtil.*;

/**
 * Implements JNDIContextManager interface.
 */
public class JNDIContextManagerImpl implements JNDIContextManager {

    private static final Logger logger = LoggerFactory.getLogger(JNDIContextManagerImpl.class);

    private static final String OBJECT_CLASS = "objectClass";

    private BundleContext bundleContext;
    private ServiceRegistration serviceRegistration;

    public JNDIContextManagerImpl(BundleContext bundleContext, ServiceRegistration serviceRegistration) {
        this.bundleContext = bundleContext;
        this.serviceRegistration = serviceRegistration;
    }

    @Override
    public Context newInitialContext() throws NamingException {
        Hashtable<?, ?> environment = new Hashtable<>();
        return new WrapperContext(bundleContext, getInitialContext(environment), environment);
    }

//    @Override
//    public Context newInitialContext(Map map) throws NamingException {
//        return null;
//    }

    @Override
    public Context newInitialContext(Map environment) throws NamingException {
        Hashtable envMap = new Hashtable(environment);
        return new WrapperContext(bundleContext, getInitialContext(envMap), envMap);
    }

    @Override
    public DirContext newInitialDirContext() throws NamingException {
        return null;
    }

    @Override
    public DirContext newInitialDirContext(Map map) throws NamingException {
        return null;
    }

//    @Override
//    public DirContext newInitialDirContext(Map<?, ?> environment) throws NamingException {
//        return null;
//    }

    /**
     *
     * @param environment
     * @return
     * @throws NamingException
     */
    private Optional<Context> getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        Optional<Context> initialContextOptional;

        //1) Check whether java.naming.factory.initial property is present
        String userDefinedICFClassName = (String) environment.get(javax.naming.Context.INITIAL_CONTEXT_FACTORY);
        if (userDefinedICFClassName != null && !"".equals(userDefinedICFClassName)) {

            //2) Check for OSGi service with key matches with given context factory class name as well as the
            //   InitialContextFactory class name in the ranking order.
            Collection<ServiceReference<InitialContextFactory>> factorySRefCollection =
                    getServiceReferences(InitialContextFactory.class, getServiceFilter(userDefinedICFClassName));

            initialContextOptional = getInitialContextFromFactory(factorySRefCollection, environment);
            if (initialContextOptional.isPresent()) {
                return initialContextOptional;
            }

            //3) If not, try to get an InitialContext from InitialContextFactoryBuilder OSGi services.
            Collection<ServiceReference<InitialContextFactoryBuilder>> serviceRefCollection =
                    getServiceReferences(InitialContextFactoryBuilder.class, null);

            initialContextOptional = getInitialContextFromBuilder(serviceRefCollection, environment);

            //4) If not, throw an error.
            initialContextOptional.orElseThrow(() -> new NoInitialContextException(
                    "Cannot find the given InitialContextFactory " + userDefinedICFClassName));

            //5) Returning the initialContext which is not null.
            return initialContextOptional;

        } else {
            //2) Try to get an InitialContext from InitialContextFactoryBuilder OSGi services.
            Collection<ServiceReference<InitialContextFactoryBuilder>> builderSRefCollection =
                    getServiceReferences(InitialContextFactoryBuilder.class, null);

            initialContextOptional = getInitialContextFromBuilder(builderSRefCollection, environment);

            if (initialContextOptional.isPresent()) {
                return initialContextOptional;
            }

            //3) If not, try to get an InitialContext from InitialContextFactory OSGi services.
            Collection<ServiceReference<InitialContextFactory>> factorySRefCollection =
                    getServiceReferences(InitialContextFactory.class, null);

            initialContextOptional = getInitialContextFromFactory(factorySRefCollection, environment);

            //4) If no Context has been found, an initial Context is returned without any backing. This returned initial
            //  Context can then only be used to perform URL based lookups.
            return initialContextOptional;
        }
    }

    /**
     * Create a service filter which matches OSGi services registered with two values for the objectClass property.
     *
     * User defined initial context factory class name and the InitialContextFactory class name are those two values of
     * the objectClass property.
     *
     * @param userDefinedICFClassName value of java.naming.factory.initial property
     * @return filter string
     */
    private String getServiceFilter(String userDefinedICFClassName) {
        // Here I've initially user StringBuilder, but IntelliJ IDEA suggested to replace StringBuilder usage with
        // Strings becuause for this specific case, String concatenation is at least as efficient or more efficent
        // than the original StringBuilder or StringBuffer user.
        return "(&" +
                "(" + OBJECT_CLASS + "=" + userDefinedICFClassName + ")" +
                "(" + OBJECT_CLASS + "=" + InitialContextFactory.class.getName() + ")" +
                ")";
    }

    /**
     *
     * @param builderOptional
     * @param environment
     * @return
     */
    private Optional<InitialContextFactory> getContextFactory(Optional<InitialContextFactoryBuilder> builderOptional,
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
     *
     * @param serviceRefCollection
     * @param environment
     * @return
     * @throws NamingException
     */
    private Optional<Context> getInitialContextFromBuilder(
            Collection<ServiceReference<InitialContextFactoryBuilder>> serviceRefCollection,
            Hashtable<?, ?> environment) throws NamingException {

        return serviceRefCollection
                .stream()
                .sorted(new ServiceRankComparator())
                .map(this::getService)
                .map(builderOptional -> getContextFactory(builderOptional, environment))
                .flatMap(factoryOptional -> factoryOptional.map(Stream::of).orElseGet(Stream::empty))
                .map(rethrowFunction(factory -> factory.getInitialContext(environment)))
                .findFirst();
    }

    /**
     *
     * @param serviceRefCollection
     * @param environment
     * @return
     * @throws NamingException
     */
    private Optional<Context> getInitialContextFromFactory(
            Collection<ServiceReference<InitialContextFactory>> serviceRefCollection,
            Hashtable<?, ?> environment) throws NamingException {

        return serviceRefCollection
                .stream()
                .sorted(new ServiceRankComparator())
                .map(this::getService)
                .flatMap(factoryOptional -> factoryOptional.map(Stream::of).orElseGet(Stream::empty))
                .map(rethrowFunction(contextFactory -> contextFactory.getInitialContext(environment)))
                .findFirst();
    }

    /**
     *
     * @param clazz
     * @param filter
     * @param <S>
     * @return
     */
    private <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter) {
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
     *
     * @param serviceReference
     * @param <S>
     * @return
     */
    private <S> Optional<S> getService(ServiceReference<S> serviceReference) {
        return Optional.ofNullable(bundleContext.getService(serviceReference));
    }

    /**
     *
     */
    private static class ServiceRankComparator implements Comparator<ServiceReference<?>> {

        @Override
        public int compare(ServiceReference<?> ref1, ServiceReference<?> ref2) {
            int rank1 = (Integer) ref1.getProperty("service.ranking");
            int rank2 = (Integer) ref2.getProperty("service.ranking");
            int diff = rank1 - rank2;
            if (diff == 0) {
                int serviceId1 = (Integer) ref1.getProperty("service.id");
                int serviceId2 = (Integer) ref2.getProperty("service.id");
                return -(serviceId1 - serviceId2);
            } else {
                return diff;
            }
        }
    }
}
