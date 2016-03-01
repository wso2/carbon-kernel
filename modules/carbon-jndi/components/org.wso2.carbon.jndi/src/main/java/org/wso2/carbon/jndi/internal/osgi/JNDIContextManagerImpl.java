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
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import static org.wso2.carbon.jndi.internal.util.JNDIUtils.*;


/**
 * This class provides an implementation of JNDIContextManager interface.
 */
public class JNDIContextManagerImpl implements JNDIContextManager {

    private static final String OBJECT_CLASS = "objectClass";

    private BundleContext bundleContext;
    private ServiceRegistration<JNDIContextManager> serviceRegistration;

    public JNDIContextManagerImpl(BundleContext bundleContext, ServiceRegistration<JNDIContextManager> serviceRegistration) {
        this.bundleContext = bundleContext;
        this.serviceRegistration = serviceRegistration;
    }

    @Override
    public Context newInitialContext() throws NamingException {
        Hashtable<?, ?> environment = new Hashtable<>();
        Optional<Context> initialContextInternal = getInitialContextInternal(environment);
        return new WrapperContext(bundleContext, initialContextInternal, environment);
    }

    @Override
    public Context newInitialContext(Map environment) throws NamingException {
        Hashtable<?, ?> envMap = new Hashtable(environment);
        Optional<Context> initialContextInternal = getInitialContextInternal(envMap);
        return new WrapperContext(bundleContext, initialContextInternal, envMap);
    }

    @Override
    public DirContext newInitialDirContext() throws NamingException {
        return null;
    }

    @Override
    public DirContext newInitialDirContext(Map map) throws NamingException {
        return null;
    }

    /**
     * Creates a new JNDI initial context with the specified JNDI environment properties.
     *
     * @param environment The possibly null environment
     *                    specifying information to be used in the creation
     *                    of the initial context.
     * @return an {@code Optional} describing created initial context.
     * @throws NamingException If cannot create an initial context.
     */
    private Optional<Context> getInitialContextInternal(Hashtable<?, ?> environment) throws NamingException {
        Optional<Context> initialContextOptional;

        //1) Check whether java.naming.factory.initial property is present
        String userDefinedICFClassName = (String) environment.get(javax.naming.Context.INITIAL_CONTEXT_FACTORY);
        if (userDefinedICFClassName != null && !"".equals(userDefinedICFClassName)) {

            //2) Check for OSGi service with key matches with given context factory class name as well as the
            //   InitialContextFactory class name in the ranking order.
            Collection<ServiceReference<InitialContextFactory>> factorySRefCollection =
                    getServiceReferences(bundleContext, InitialContextFactory.class,
                            getServiceFilter(userDefinedICFClassName));

            initialContextOptional = getInitialContextFromFactory(bundleContext,
                    factorySRefCollection, environment);
            if (initialContextOptional.isPresent()) {
                return initialContextOptional;
            }

            //3) If not, try to get an InitialContext from InitialContextFactoryBuilder OSGi services.
            Collection<ServiceReference<InitialContextFactoryBuilder>> serviceRefCollection =
                    getServiceReferences(bundleContext, InitialContextFactoryBuilder.class, null);

            initialContextOptional = getInitialContextFromBuilder(bundleContext,
                    serviceRefCollection, environment);

            //4) If not, throw an error.
            initialContextOptional.orElseThrow(() -> new NoInitialContextException(
                    "Cannot find the InitialContextFactory " + userDefinedICFClassName));

            //5) Returning the initialContext which is not null.
            return initialContextOptional;

        } else {
            //2) Try to get an InitialContext from InitialContextFactoryBuilder OSGi services.
            Collection<ServiceReference<InitialContextFactoryBuilder>> builderSRefCollection =
                    getServiceReferences(bundleContext, InitialContextFactoryBuilder.class, null);

            initialContextOptional = getInitialContextFromBuilder(bundleContext,
                    builderSRefCollection, environment);

            if (initialContextOptional.isPresent()) {
                return initialContextOptional;
            }

            //3) If not, try to get an InitialContext from InitialContextFactory OSGi services.
            Collection<ServiceReference<InitialContextFactory>> factorySRefCollection =
                    getServiceReferences(bundleContext, InitialContextFactory.class, null);

            initialContextOptional = getInitialContextFromFactory(bundleContext,
                    factorySRefCollection, environment);

            //4) If no Context has been found, an initial Context is returned without any backing. This returned initial
            //  Context can then only be used to perform URL based lookups.
            return initialContextOptional;
        }
    }

    /**
     * Create a service filter which matches OSGi services registered with two values for the objectClass property.
     * <p>
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
}
