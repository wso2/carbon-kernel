
/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.jndi.osgi;

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.jndi.JNDIContextManager;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.jndi.osgi.builders.ABCContextFactoryBuilder;
import org.wso2.carbon.jndi.osgi.builders.NullContextFactoryBuilder;
import org.wso2.carbon.jndi.osgi.builders.XYZContextFactoryBuilder;
import org.wso2.carbon.jndi.osgi.factories.BarInitialContextFactory;
import org.wso2.carbon.jndi.osgi.factories.BundleContextICFServiceFactory;
import org.wso2.carbon.jndi.osgi.factories.ExceptionInitialContextFactory;
import org.wso2.carbon.jndi.osgi.factories.FooInitialContextFactory;
import org.wso2.carbon.jndi.osgi.factories.NullInitialContextFactory;
import org.wso2.carbon.jndi.osgi.utils.DummyBundleClassLoader;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import java.util.Arrays;
import java.util.Dictionary;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.testng.Assert.assertEquals;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JNDITest {
    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private JNDIContextManager jndiContextManager;

    //TODO Clean up these tests

    @Test
    public void testJNDITraditionalClient() throws NamingException {
        InitialContext initialContext = new InitialContext();
        initialContext.createSubcontext("java:comp");
        initialContext.bind("java:comp/name", "sameera");

        InitialContext context = new InitialContext();
        String name = (String) context.lookup("java:comp/name");
        assertEquals(name, "sameera", "Value not found in JNDI");

        NamingEnumeration namingEnumeration = context.list("java:comp");
        namingEnumeration.hasMore();
        namingEnumeration.next();

        namingEnumeration = context.listBindings("java:comp");
        namingEnumeration.hasMore();
        namingEnumeration.next();

        context.rebind("java:comp/name", "jayasoma");
        name = (String) context.lookup("java:comp/name");
        assertEquals(name, "jayasoma", "Value not found in JNDI");

        context.rename("java:comp", "java:comp1");
        name = (String) context.lookup("java:comp1/name");
        assertEquals(name, "jayasoma", "Value not found in JNDI");

        context.rename("java:comp1", "java:comp");
    }

    @Test(dependsOnMethods = "testJNDITraditionalClient")
    public void testJNDIContextManagerService() throws NamingException {
        String name = null;

        Context initialContext = jndiContextManager.newInitialContext();

        initialContext.createSubcontext("java:comp/env2");
        initialContext.bind("java:comp/env2/name", "jayasoma");

        Context context = jndiContextManager.newInitialContext();

        name = (String) context.lookup("java:comp/env2/name");

        assertEquals(name, "jayasoma", "Value not found in JNDI");
    }

    /**
     * This method test the code which retrieve the caller's BundleContext instance from the
     * osgi.service.jndi.bundleContext environment variable defined in the OSGi JNDI Specification.
     */
    @Test(dependsOnMethods = "testJNDIContextManagerService")
    public void testJNDITraditionalClientWithEnvironmentBC() throws NamingException {

        // Getting the BundleContext of the org.wso2.carbon.jndi bundle.
        BundleContext carbonJNDIBundleContext = Arrays.asList(bundleContext.getBundles())
                .stream()
                .filter(bundle -> "org.wso2.carbon.jndi".equals(bundle.getSymbolicName()))
                .map(Bundle::getBundleContext)
                .findAny()
                .get();

        // This is used to get the caller's bundle context object.
        BundleContextICFServiceFactory bundleContextICFServiceFactory = new BundleContextICFServiceFactory();

        Dictionary<String, Object> propertyMap = new Hashtable<>();
        propertyMap.put("service.ranking", 10);
        ServiceRegistration serviceRegistration = bundleContext.registerService(InitialContextFactory.class.getName(),
                bundleContextICFServiceFactory, propertyMap);

        //Setting carbonJNDIBundleContext as the value of osgi.service.jndi.bundleContext property.
        Hashtable<String, Object> environment = new Hashtable<>(1);
        environment.put("osgi.service.jndi.bundleContext", carbonJNDIBundleContext);

        InitialContext initialContext = new InitialContext(environment);
        initialContext.createSubcontext("java:comp/bundleContext");

        assertEquals(bundleContextICFServiceFactory.getFirstConsumersBundleContext().getBundle().getSymbolicName(),
                carbonJNDIBundleContext.getBundle().getSymbolicName(), "Value of the osgi.service.jndi.bundleContext " +
                        "environment variable has not been picked up");

        serviceRegistration.unregister();

        //TODO get BundleContext from TCCL and callers class context
    }

    /**
     * This method test the code which retrieve the caller's BundleContext instance from the
     * Thread Context ClassLoader.
     */
    @Test(dependsOnMethods = "testJNDITraditionalClientWithEnvironmentBC")
    public void testJNDITraditionalClientWithTCCL() throws NamingException {
        DummyBundleClassLoader dummyBundleClassLoader = new DummyBundleClassLoader(this.getClass().getClassLoader(),
                bundleContext.getBundle());

        Dictionary<String, Object> propertyMap = new Hashtable<>();
        propertyMap.put("service.ranking", 10);
        ServiceRegistration serviceRegistration = bundleContext.registerService(InitialContextFactory.class.getName(),
                new FooInitialContextFactory(), propertyMap);

        ClassLoader currentTCCL = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(dummyBundleClassLoader);

            InitialContext initialContext = new InitialContext();
            initialContext.createSubcontext("java:comp/tccl");
        } finally {
            Thread.currentThread().setContextClassLoader(currentTCCL);
        }

        assertEquals(true, dummyBundleClassLoader.isGetBundleMethodInvoked(), "TCCL has not used to get the " +
                "caller's Bundle");

        serviceRegistration.unregister();
    }

    /**
     * In this method we are trying to create an InitialContext from a non-existent InitialContextFactory called
     * FooInitialContextFactory. This FooInitialContextFactory is specified as an environment variable.
     */
    @Test(dependsOnMethods = "testJNDITraditionalClientWithTCCL", expectedExceptions = {NamingException.class},
            expectedExceptionsMessageRegExp = "Cannot find the InitialContextFactory " +
                    "org.wso2.carbon.jndi.osgi.factories.FooInitialContextFactory.")
    public void testJNDIContextManagerWithEnvironmentContextFactoryException() throws NamingException {
        Map<String, String> environment = new HashMap<String, String>(1);
        environment.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                FooInitialContextFactory.class.getName());

        jndiContextManager.newInitialContext(environment);
    }

    /**
     * In this method we are trying to create an InitialContext from the FooInitialContextFactory by setting the
     * java.naming.factory.initial environment variable.
     */
    @Test(dependsOnMethods = "testJNDIContextManagerWithEnvironmentContextFactoryException")
    public void testJNDIContextManagerWithEnvironmentContextFactory() throws NamingException {

        ServiceRegistration serviceRegistration = bundleContext.registerService(
                new String[]{InitialContextFactory.class.getName(), FooInitialContextFactory.class.getName()},
                new FooInitialContextFactory(), null);

        Map<String, String> environment = new HashMap<String, String>(1);
        environment.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                FooInitialContextFactory.class.getName());

        Context initialContext = jndiContextManager.newInitialContext(environment);

        initialContext.bind("contextFactoryClass", "org.wso2.carbon.jndi.internal.InMemoryInitialContextFactory");
        String contextFactoryClass = (String) initialContext.lookup("contextFactoryClass");
        assertEquals(contextFactoryClass, FooInitialContextFactory.class.getName(), "Specified InitialContextFactory " +
                "has not been picked up to create the requested initial context.");

        // Unregistering the FooInitialContextFactory service.
        serviceRegistration.unregister();

    }

    /**
     * In this method we are testing the functionality of the JNDIContextManager when there are multiple
     * InitialContextFactory services with different service.ranking property value.
     * <p>
     * We also test the situation with an InitialContextFactory which returns a null context.
     */
    @Test(dependsOnMethods = "testJNDIContextManagerWithEnvironmentContextFactory")
    public void testCustomInitialContextFactories() throws NamingException {
        Dictionary<String, Object> propertyMap = new Hashtable<>();
        propertyMap.put("service.ranking", 10);
        ServiceRegistration<InitialContextFactory> fooICFServiceRef = bundleContext.registerService(
                InitialContextFactory.class, new FooInitialContextFactory(), propertyMap);
        Context initialContext = jndiContextManager.newInitialContext();

        // Here we expect returned Context to be an instance of the TestContext. Following bind operation is ignored
        // in the TestContext class. It is hard coded to return the created InitialContextFactory if you invoke the
        // TestContext.lookup("contextFactoryClass"). In this case it should be the FooInitialContextFactory
        initialContext.bind("contextFactoryClass", "org.wso2.carbon.jndi.internal.InMemoryInitialContextFactory");
        String contextFactoryClass = (String) initialContext.lookup("contextFactoryClass");

        assertEquals(contextFactoryClass, FooInitialContextFactory.class.getName(), "Specified InitialContextFactory " +
                "has not been picked up to create the requested initial context.");

        // Now we are registering another InitialContextFactory with a higher service.ranking value.
        propertyMap = new Hashtable<>();
        propertyMap.put("service.ranking", 20);
        ServiceRegistration<InitialContextFactory> barICFServiceRef = bundleContext.registerService(
                InitialContextFactory.class, new BarInitialContextFactory(), propertyMap);

        initialContext = jndiContextManager.newInitialContext();
        initialContext.bind("contextFactoryClass", "org.wso2.carbon.jndi.internal.InMemoryInitialContextFactory");
        contextFactoryClass = (String) initialContext.lookup("contextFactoryClass");

        assertEquals(contextFactoryClass, BarInitialContextFactory.class.getName(), "Specified InitialContextFactory " +
                "has not been picked up to create the requested initial context.");


        // To test null from getInitialContext methods.
        propertyMap = new Hashtable<>();
        propertyMap.put("service.ranking", 30);
        ServiceRegistration<InitialContextFactory> nullICFServiceRef = bundleContext.registerService(
                InitialContextFactory.class, new NullInitialContextFactory(), propertyMap);

        initialContext = jndiContextManager.newInitialContext();
        initialContext.bind("contextFactoryClass", "org.wso2.carbon.jndi.internal.InMemoryInitialContextFactory");
        contextFactoryClass = (String) initialContext.lookup("contextFactoryClass");

        assertEquals(contextFactoryClass, BarInitialContextFactory.class.getName(), "Specified InitialContextFactory " +
                "has not been picked up to create the requested initial context.");

        // Unregistering all the registered ICF services.
        fooICFServiceRef.unregister();
        barICFServiceRef.unregister();
        nullICFServiceRef.unregister();
    }

    /**
     * In this method we are testing the functionality of the JNDIContextManager when there exists
     * an InitialContextFactory service which throws a NamingException.
     */
    @Test(dependsOnMethods = "testCustomInitialContextFactories", expectedExceptions = {NamingException.class},
            expectedExceptionsMessageRegExp = "InitialContext cannot be created due to a network failure.")
    public void testCustomInitialContextFactoryWithException() throws NamingException {
        // To test null from getInitialContext methods.
        Dictionary<String, Object> propertyMap = new Hashtable<>();
        propertyMap.put("service.ranking", 40);
        ServiceRegistration<InitialContextFactory> exceptionFactorySR = bundleContext.registerService(
                InitialContextFactory.class, new ExceptionInitialContextFactory(), propertyMap);

        try {
            Context initialContext = jndiContextManager.newInitialContext();
        } finally {
            // Unregistering the InitialContextFactory which throws an exception.
            exceptionFactorySR.unregister();
        }
    }

    /**
     * In this method we are trying to create an InitialContext from a non-existent InitialContextFactory called
     * FooInitialContextFactory. But we are registering an InitialContextFactoryBuilder service before that.
     * Therefore this InitialContextFactoryBuilder service should create an InitialContext according the OSGi JNDI
     * specification.
     */
    @Test(dependsOnMethods = "testCustomInitialContextFactoryWithException")
    public void testJNDIContextManagerWithEnvironmentContextFactoryBuilder() throws NamingException {
        Dictionary<String, Object> propertyMap = new Hashtable<>();
        propertyMap.put("service.ranking", 10);
        ServiceRegistration<InitialContextFactoryBuilder> abcICFBServiceRef = bundleContext.registerService(
                InitialContextFactoryBuilder.class, new ABCContextFactoryBuilder(), propertyMap);

        Map<String, String> environment = new HashMap<String, String>(1);
        environment.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                FooInitialContextFactory.class.getName());

        Context initialContext = jndiContextManager.newInitialContext(environment);

        initialContext.bind("contextFactoryBuilderClass", "EMPTY");
        String contextFactoryBuilderClass = (String) initialContext.lookup("contextFactoryBuilderClass");

        assertEquals(contextFactoryBuilderClass, ABCContextFactoryBuilder.class.getName(),
                "Specified InitialContextFactory has not been picked up to create the requested initial context.");

        abcICFBServiceRef.unregister();
    }

    /**
     * In this method we are testing the functionality of the JNDIContextManager when there are multiple
     * InitialContextFactoryBuilder services with different service.ranking property value.
     * <p>
     * We also test the situation with an InitialContextFactoryBuilder which returns a null context.
     */
    @Test(dependsOnMethods = "testJNDIContextManagerWithEnvironmentContextFactoryBuilder")
    public void testCustomInitialContextFactoryBuilders() throws NamingException {
        Dictionary<String, Object> propertyMap = new Hashtable<>();
        propertyMap.put("service.ranking", 10);
        ServiceRegistration<InitialContextFactoryBuilder> abcICFBServiceRef = bundleContext.registerService(
                InitialContextFactoryBuilder.class, new ABCContextFactoryBuilder(), propertyMap);
        Context initialContext = jndiContextManager.newInitialContext();

        initialContext.bind("contextFactoryBuilderClass", "EMPTY");
        String contextFactoryBuilderClass = (String) initialContext.lookup("contextFactoryBuilderClass");

        assertEquals(contextFactoryBuilderClass, ABCContextFactoryBuilder.class.getName(),
                "Specified InitialContextFactory has not been picked up to create the requested initial context.");

        propertyMap.put("service.ranking", 30);
        ServiceRegistration<InitialContextFactoryBuilder> nullICFBServiceRef = bundleContext.registerService(
                InitialContextFactoryBuilder.class, new NullContextFactoryBuilder(), propertyMap);
        initialContext = jndiContextManager.newInitialContext();

        initialContext.bind("contextFactoryBuilderClass", "EMPTY");
        contextFactoryBuilderClass = (String) initialContext.lookup("contextFactoryBuilderClass");

        assertEquals(contextFactoryBuilderClass, ABCContextFactoryBuilder.class.getName(),
                "Specified InitialContextFactory has not been picked up to create the requested initial context.");

        propertyMap.put("service.ranking", 20);
        ServiceRegistration<InitialContextFactoryBuilder> xyzICFBServiceRef = bundleContext.registerService(
                InitialContextFactoryBuilder.class, new XYZContextFactoryBuilder(), propertyMap);
        initialContext = jndiContextManager.newInitialContext();

        initialContext.bind("contextFactoryBuilderClass", "EMPTY");
        contextFactoryBuilderClass = (String) initialContext.lookup("contextFactoryBuilderClass");

        assertEquals(contextFactoryBuilderClass, XYZContextFactoryBuilder.class.getName(),
                "Specified InitialContextFactory has not been picked up to create the requested initial context.");

        abcICFBServiceRef.unregister();
        nullICFBServiceRef.unregister();
        xyzICFBServiceRef.unregister();
    }
}