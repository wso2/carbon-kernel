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
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

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

    @Override
    public Context newInitialContext(Map<?, ?> environment) throws NamingException {
        Hashtable<?, ?> envMap = new Hashtable<>(environment);
        return new WrapperContext(bundleContext, getInitialContext(envMap), envMap);
    }

    @Override
    public DirContext newInitialDirContext() throws NamingException {
        return null;
    }

    @Override
    public DirContext newInitialDirContext(Map<?, ?> environment) throws NamingException {
        return null;
    }

    private Optional<Context> getInitialContext(Hashtable<?, ?> environment) throws NamingException {
//        Context initialContext = null;
        Optional<Context> initialContext = Optional.empty();

        // 1 Check whether java.naming.factory.initial property is present
        String initialContextFactoryClassName = (String) environment.get(javax.naming.Context.INITIAL_CONTEXT_FACTORY);
        if (initialContextFactoryClassName != null && !"".equals(initialContextFactoryClassName)) {

            //2 Check for OSGi service with key matches with given context factory class name as well as the
            // InitialContextFactory class name in the ranking order.


            // Create the filter.
            StringBuilder serviceFilter = new StringBuilder();
            serviceFilter.append("(&");
            serviceFilter.append("(").append(OBJECT_CLASS).append("=").append(initialContextFactoryClassName).append(")");
            serviceFilter.append("(").append(OBJECT_CLASS).append("=").append(InitialContextFactory.class.getName()).append(")");
            serviceFilter.append(")");

            try {
                Collection<ServiceReference<InitialContextFactory>> serviceRefCollection =
                        bundleContext.getServiceReferences(InitialContextFactory.class, serviceFilter.toString());

                initialContext = serviceRefCollection
                        .stream()
                        .sorted(new ServiceRankComparator())
                        .findFirst()
                        .map(factoryServiceRef -> getContextFromFactory(factoryServiceRef, environment))
                        .get();
            } catch (InvalidSyntaxException e) {
                logger.info(e.getMessage(), e);
            }

            //4 Loop through all the InitialContextFactoryBuilder services in the ranking order.

            if (!initialContext.isPresent()) {

                try {
                    Collection<ServiceReference<InitialContextFactoryBuilder>> ref = bundleContext.getServiceReferences(InitialContextFactoryBuilder.class, null);
                } catch (InvalidSyntaxException e) {
                    e.printStackTrace();
                }

                try {
                    initialContext = bundleContext.getServiceReferences(InitialContextFactoryBuilder.class, null)
                            .stream()
                            .sorted(new ServiceRankComparator())
                            .map(builderServiceRef -> getInitialContextFactoryFromBuilder(builderServiceRef, environment))
                            .map(initialContextFactory -> getContextFromFactory(initialContextFactory, environment))
                            .findFirst()
                            .orElse(Optional.empty());

                } catch (InvalidSyntaxException ignored) {
                }
            }

            //5 If not throw an error.
            //TODO Proper error messages
            throw new NoInitialContextException();

        } else {
            //2 Loop through all the InitialContextFactoryBuilder services in the ranking order.

            try {
                initialContext = bundleContext.getServiceReferences(InitialContextFactoryBuilder.class, null)
                        .stream()
                        .sorted(new ServiceRankComparator())
                        .map(builderServiceRef -> getInitialContextFactoryFromBuilder(builderServiceRef, environment))
                        .map(initialContextFactory -> getContextFromFactory(initialContextFactory, environment))
                        .findFirst()
                        .orElse(Optional.empty());

            } catch (InvalidSyntaxException ignored) {
            }

            //3 Loop through all the InitialContextFactory services in the ranking order.
            // TODO check for an alternative for using .isPresent()
            if (!initialContext.isPresent()) {

                try {
                    initialContext = bundleContext.getServiceReferences(InitialContextFactory.class, null)
                            .stream()
                            .sorted(new ServiceRankComparator())
                            .map(factoryServiceRef -> getContextFromFactory(factoryServiceRef, environment))
                            .findFirst()
                            .orElse(Optional.empty());

                } catch (InvalidSyntaxException ignored) {
                }
            }

            //4 If no Context has been found, an initial Context is returned without any backing. This returned initial
            //  Context can then only be used to perform URL based lookups.
            return initialContext;
        }
    }

    private Optional<InitialContextFactory> getInitialContextFactoryFromBuilder(
            ServiceReference<InitialContextFactoryBuilder> serviceReference,
            Hashtable<?, ?> environment) {

        Optional<InitialContextFactoryBuilder> contextFactoryBuilder =
                Optional.ofNullable(bundleContext.getService(serviceReference));

        return contextFactoryBuilder.map(builder -> {
            try {
                return builder.createInitialContextFactory(environment);
            } catch (NamingException e) {
                logger.debug(e.getMessage(), e);
                return null;
            }
        });
    }

    private Optional<Context> getContextFromFactory(
            Optional<InitialContextFactory> contextFactory,
            Hashtable<?, ?> environment) {

        return contextFactory.map(factory -> {
            try {
                return factory.getInitialContext(environment);
            } catch (NamingException e) {
                logger.debug(e.getMessage(), e);
                // TODO Fix this runtime exception.
                throw new RuntimeException(e.getMessage(), e);
            }
        });
    }

    private Optional<Context> getContextFromFactory(
            ServiceReference<InitialContextFactory> serviceReference,
            Hashtable<?, ?> environment) {

        Optional<InitialContextFactory> contextFactory =
                Optional.ofNullable(bundleContext.getService(serviceReference));

        return getContextFromFactory(contextFactory, environment);
    }

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
