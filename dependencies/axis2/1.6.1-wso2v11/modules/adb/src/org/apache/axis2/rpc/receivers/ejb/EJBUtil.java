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

package org.apache.axis2.rpc.receivers.ejb;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.util.threadpool.DefaultThreadFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EJBUtil {
    public static final java.lang.String EJB_JNDI_NAME = "beanJndiName";
    public static final java.lang.String EJB_HOME_INTERFACE_NAME = "homeInterfaceName";
    public static final java.lang.String EJB_REMOTE_INTERFACE_NAME = "remoteInterfaceName";
    public static final java.lang.String EJB_LOCAL_HOME_INTERFACE_NAME = "localHomeInterfaceName";
    public static final java.lang.String EJB_LOCAL_INTERFACE_NAME = "localInterfaceName";
    public static final java.lang.String EJB_INITIAL_CONTEXT_FACTORY = "jndiContextClass";
    public static final java.lang.String EJB_PROVIDER_URL = "providerUrl";
    public static final java.lang.String EJB_JNDI_USERNAME = "jndiUser";
    public static final java.lang.String EJB_JNDI_PASSWORD = "jndiPassword";

    private static ExecutorService workerPool = null;

    static {
        workerPool =
                new ThreadPoolExecutor(1, 50, 150L, TimeUnit.SECONDS, new LinkedBlockingQueue(),
                                       new DefaultThreadFactory(
                                               new ThreadGroup("EJB provider thread group"),
                                               "EJBProvider"));
    }

    /**
     * Return a object which implements the service.
     *
     * @param msgContext the message context
     * @return an object that implements the service
     * @throws AxisFault if fails
     */
    protected static Object makeNewServiceObject(MessageContext msgContext) throws AxisFault {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(1);
        EJBClientWorker worker = new EJBClientWorker(msgContext, startLatch, stopLatch);
        workerPool.execute(worker);
        startLatch.countDown();
        try {
            stopLatch.await();
        } catch (InterruptedException e) {
            throw AxisFault.makeFault(e);
        }

        if (worker.getException() != null) {
            throw AxisFault.makeFault(worker.getException());
        }

        return worker.getReturnedValue();
    }

    private static class EJBClientWorker implements Runnable {

        private MessageContext msgContext = null;
        private CountDownLatch startLatch = null;
        private CountDownLatch stopLatch = null;
        protected static final Class[] empty_class_array = new Class[0];
        protected static final Object[] empty_object_array = new Object[0];
        private static InitialContext cached_context = null;
        private Exception exception = null;
        private Object returnedValue = null;

        public EJBClientWorker(MessageContext msgContext, CountDownLatch startLatch,
                               CountDownLatch stopLatch) {
            this.msgContext = msgContext;
            this.startLatch = startLatch;
            this.stopLatch = stopLatch;
        }

        public void run() {
            try {
                startLatch.await();
                final AxisService service = msgContext.getAxisService();
                AccessController.doPrivileged(
                        new PrivilegedAction() {
                            public Object run() {
                                Thread.currentThread()
                                        .setContextClassLoader(service.getClassLoader());
                                return null;
                            }
                        }
                );
                Parameter remoteHomeName = service.getParameter(EJB_HOME_INTERFACE_NAME);
                Parameter localHomeName = service.getParameter(EJB_LOCAL_HOME_INTERFACE_NAME);
                Parameter jndiName = service.getParameter(EJB_JNDI_NAME);
                Parameter homeName = (remoteHomeName != null ? remoteHomeName : localHomeName);

                if (jndiName == null || jndiName.getValue() == null) {
                    throw new AxisFault("jndi name is not specified");
                } else if (homeName == null || homeName.getValue() == null) {
                    // cannot find both remote home and local home
                    throw new AxisFault("ejb remote/local home class name is not specified");
                }

                // we create either the ejb using either the RemoteHome or LocalHome object
                if (remoteHomeName != null)
                    returnedValue = createRemoteEJB(msgContext,
                                                    ((String)jndiName.getValue()).trim(),
                                                    ((String)homeName.getValue()).trim());
                else
                    returnedValue = createLocalEJB(msgContext, ((String)jndiName.getValue()).trim(),
                                                   ((String)homeName.getValue()).trim());
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            } finally {
                stopLatch.countDown();
            }
        }

        /**
         * Create an EJB using a remote home object
         *
         * @param msgContext   the message context
         * @param beanJndiName The JNDI name of the EJB remote home class
         * @param homeName     the name of the home interface class
         * @return an EJB
         * @throws Exception If fails
         */
        private Object createRemoteEJB(MessageContext msgContext, String beanJndiName,
                                       String homeName) throws Exception {
            // Get the EJB Home object from JNDI
            Object ejbHome = getEJBHome(msgContext.getAxisService(), beanJndiName);
            Class cls = getContextClassLoader().loadClass(homeName);
            Object ehome = javax.rmi.PortableRemoteObject.narrow(ejbHome, cls);

            // Invoke the create method of the ejbHome class without actually
            // touching any EJB classes (i.e. no cast to EJBHome)
            Method createMethod = cls.getMethod("create", empty_class_array);

            return createMethod.invoke(ehome, empty_object_array);
        }

        /**
         * Create an EJB using a local home object
         *
         * @param msgContext   the message context
         * @param beanJndiName The JNDI name of the EJB local home class
         * @param homeName     the name of the home interface class
         * @return an EJB
         * @throws Exception if fails
         */
        private Object createLocalEJB(MessageContext msgContext, String beanJndiName,
                                      String homeName)
                throws Exception {
            // Get the EJB Home object from JNDI
            Object ejbHome = getEJBHome(msgContext.getAxisService(), beanJndiName);

            // the home object is a local home object
            Object ehome;

            Class cls = getContextClassLoader().loadClass(homeName);

            if (cls.isInstance(ejbHome))
                ehome = ejbHome;
            else {
                throw new ClassCastException("bad ejb home type");
            }

            // Invoke the create method of the ejbHome class without actually
            // touching any EJB classes (i.e. no cast to EJBLocalHome)
            Method createMethod = cls.getMethod("create", empty_class_array);

            return createMethod.invoke(ehome, empty_object_array);
        }

        /**
         * Common routine to do the JNDI lookup on the Home interface object username and password
         * for jndi lookup are got from the configuration or from the messageContext if not found in
         * the configuration
         *
         * @param service      AxisService object
         * @param beanJndiName JNDI name of the EJB home object
         * @return EJB home object
         * @throws AxisFault If fals
         */
        private Object getEJBHome(AxisService service, String beanJndiName) throws AxisFault {
            Object ejbHome = null;

            // Set up an InitialContext and use it get the beanJndiName from JNDI
            try {
                Properties properties = null;

                // collect all the properties we need to access JNDI:
                // username, password, factoryclass, contextUrl

                // username
                Parameter username = service.getParameter(EJB_JNDI_USERNAME);
                if (username != null) {
                    if (properties == null)
                        properties = new Properties();
                    properties.setProperty(Context.SECURITY_PRINCIPAL,
                                           ((String)username.getValue()).trim());
                }

                // password
                Parameter password = service.getParameter(EJB_JNDI_PASSWORD);
                if (password != null) {
                    if (properties == null)
                        properties = new Properties();
                    properties.setProperty(Context.SECURITY_CREDENTIALS,
                                           ((String)password.getValue()).trim());
                }

                // factory class
                Parameter factoryClass = service.getParameter(EJB_INITIAL_CONTEXT_FACTORY);
                if (factoryClass != null) {
                    if (properties == null)
                        properties = new Properties();
                    properties.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                                           ((String)factoryClass.getValue()).trim());
                }

                // contextUrl
                Parameter contextUrl = service.getParameter(EJB_PROVIDER_URL);
                if (contextUrl != null) {
                    if (properties == null)
                        properties = new Properties();
                    properties.setProperty(Context.PROVIDER_URL,
                                           ((String)contextUrl.getValue()).trim());
                }

                // get context using these properties
                InitialContext context = getContext(properties);

                // if we didn't get a context, fail
                if (context == null)
                    throw new AxisFault("cannot create initial context");

                try {
                    ejbHome = getEJBHome(context, beanJndiName);
                } catch (Exception e) {
                    ejbHome = getEJBHome(context, beanJndiName); // Retry for the 2nd time to overcome issues related to cahing
                } 

                if (ejbHome == null)
                    throw new AxisFault("cannot find jndi home");
            }
            catch (Exception exception) {

                throw AxisFault.makeFault(exception);
            }

            return ejbHome;
        }

        private InitialContext getCachedContext()
                throws javax.naming.NamingException {
            if (cached_context == null)
                cached_context = new InitialContext();
            return cached_context;
        }


        private InitialContext getContext(Properties properties)
                throws AxisFault, javax.naming.NamingException {
            return ((properties == null)
                    ? getCachedContext()
                    : new InitialContext(properties));
        }

        private Object getEJBHome(InitialContext context, String beanJndiName)
                throws AxisFault, javax.naming.NamingException {
            return context.lookup(beanJndiName);
        }

        private ClassLoader getContextClassLoader() {
            return (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        }

        public Exception getException() {
            return exception;
        }

        public Object getReturnedValue() {
            return returnedValue;
        }
    }
}
