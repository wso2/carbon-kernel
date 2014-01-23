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


package org.apache.axis2.engine;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.classloader.MultiParentClassLoader;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.receivers.AbstractMessageReceiver.ThreadContextDescriptor;
import org.apache.axis2.service.Lifecycle;
import org.apache.axis2.util.Loader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;

/**
 * If the service implementation has an init method with 1 or 2 message context as its parameters, then
 * the DependencyManager calls the init method with appropriate parameters.
 */
public class DependencyManager {
    private static final Log log = LogFactory.getLog(DependencyManager.class);
    public final static String SERVICE_INIT_METHOD = "init";
    public final static String SERVICE_DESTROY_METHOD = "destroy";

    /**
     * Initialize a new service object.  Essentially, check to see if the object wants to receive
     * an init() call - if so, call it.
     *
     * @param obj the service object
     * @param serviceContext the active ServiceContext
     * @throws AxisFault if there's a problem initializing
     * 
     * @deprecated please use initServiceObject()
     */
    public static void initServiceClass(Object obj, ServiceContext serviceContext)
            throws AxisFault {
        initServiceObject(obj, serviceContext);
    }

    /**
     * Initialize a new service object.  Essentially, check to see if the object wants to receive
     * an init() call - if so, call it.
     *
     * @param obj the service object
     * @param serviceContext the active ServiceContext
     * @throws AxisFault if there's a problem initializing
     */
    public static void initServiceObject(Object obj, ServiceContext serviceContext)
            throws AxisFault {
        // This is the way to do things into the future.
        if (obj instanceof Lifecycle) {
            ((Lifecycle)obj).init(serviceContext);
            return;
        }

        // ...however, we also still support the old way for now.  Note that introspecting for
        // a method like this is something like 10 times slower than the above instanceof check.

        Class classToLoad = obj.getClass();
        // We can not call classToLoad.getDeclaredMethed() , since there
        //  can be insatnce where mutiple services extends using one class
        // just for init and other reflection methods
        Method method =  null;
        try {
            method = classToLoad.getMethod(SERVICE_INIT_METHOD, new Class[]{ServiceContext.class});
        } catch (Exception e) {
            //We do not need to inform this to user , since this something
            // Axis2 is checking to support Session. So if the method is
            // not there we should ignore that
        }
        if (method != null) {
            try {
                method.invoke(obj, new Object[]{serviceContext});
            } catch (IllegalAccessException e) {
                log.error("Exception trying to call " + SERVICE_INIT_METHOD, e);
                throw new AxisFault("Can not access the method ", e);
            } catch (IllegalArgumentException e) {
                log.error("Exception trying to call " + SERVICE_INIT_METHOD, e);
                throw new AxisFault(" Incorrect arguments ", e);
            } catch (InvocationTargetException e) {
                log.error("Exception trying to call " + SERVICE_INIT_METHOD, e);
                throw new AxisFault(" problem in invocation the method ", e);
            }
        }
    }

    /**
     * To init all the services in application scope
     *
     * @param serviceGroupContext the ServiceGroupContext from which to extract all the services
     * @throws AxisFault if there's a problem initializing
     */
    public static void initService(ServiceGroupContext serviceGroupContext) throws AxisFault {
        AxisServiceGroup serviceGroup = serviceGroupContext.getDescription();
        Iterator<AxisService> serviceItr = serviceGroup.getServices();
        while (serviceItr.hasNext()) {
            AxisService axisService = (AxisService) serviceItr.next();
            ServiceContext serviceContext = serviceGroupContext.getServiceContext(axisService);
            AxisService service = serviceContext.getAxisService();
            ClassLoader classLoader = service.getClassLoader();
            Parameter implInfoParam = service.getParameter(Constants.SERVICE_CLASS);
            if (implInfoParam != null) {
                try {
                	ThreadContextDescriptor tc = setThreadContext(axisService);
                    Class implClass = Loader.loadClass(
                            classLoader,
                            ((String) implInfoParam.getValue()).trim());
                    Object serviceImpl = implClass.newInstance();
                    serviceContext.setProperty(ServiceContext.SERVICE_OBJECT, serviceImpl);
                    initServiceObject(serviceImpl, serviceContext);
                    restoreThreadContext(tc);
                } catch (Exception e) {
                    throw AxisFault.makeFault(e);
                }
            }
        }
    }

    /**
     * Notify a service object that it's on death row.
     * @param serviceContext the active ServiceContext
     */
    public static void destroyServiceObject(ServiceContext serviceContext) {
        Object obj = serviceContext.getProperty(ServiceContext.SERVICE_OBJECT);
        if (obj != null) {
            // If this is a Lifecycle object, just call it.
            if (obj instanceof Lifecycle) {
                ((Lifecycle)obj).destroy(serviceContext);
                return;
            }

            // For now, we also use "raw" introspection to try and find the destroy method.

            Class classToLoad = obj.getClass();
            Method method =
                    null;
            try {
                method = classToLoad.getMethod(SERVICE_DESTROY_METHOD, new Class[]{ServiceContext.class});
            } catch (NoSuchMethodException e) {
                //We do not need to inform this to user , since this something
                // Axis2 is checking to support Session. So if the method is
                // not there we should ignore that
            }

            if(method!=null){
                try {
                    method.invoke(obj, new Object[]{serviceContext});
                } catch (IllegalAccessException e) {
                    log.info("Exception trying to call " + SERVICE_DESTROY_METHOD, e);
                } catch (InvocationTargetException e) {
                    log.info("Exception trying to call " + SERVICE_DESTROY_METHOD, e);
                }
            }

        }
    }
    
    protected static ThreadContextDescriptor setThreadContext(final AxisService service) {
        ThreadContextDescriptor tc = new ThreadContextDescriptor();
        tc.oldMessageContext = (MessageContext) MessageContext.currentMessageContext.get();
        final ClassLoader contextClassLoader = getContextClassLoader_doPriv();
        tc.oldClassLoader = contextClassLoader;

        
        String serviceTCCL = (String) service.getParameterValue(Constants.SERVICE_TCCL);
        if (serviceTCCL != null) {
            serviceTCCL = serviceTCCL.trim().toLowerCase();
            //TODO
            serviceTCCL = Constants.TCCL_COMPOSITE; 
            //TODO

            if (serviceTCCL.equals(Constants.TCCL_COMPOSITE)) {
                final ClassLoader loader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        return new MultiParentClassLoader(new URL[]{},
                                new ClassLoader[]{
                        		service.getClassLoader(),
                                        contextClassLoader
                                });
                    }
                });
                org.apache.axis2.java.security.AccessController.doPrivileged(
                        new PrivilegedAction() {
                            public Object run() {
                                Thread.currentThread().setContextClassLoader(
                                        loader);
                                return null;
                            }
                        }
                );
            } else if (serviceTCCL.equals(Constants.TCCL_SERVICE)) {
                org.apache.axis2.java.security.AccessController.doPrivileged(
                        new PrivilegedAction() {
                            public Object run() {
                                Thread.currentThread().setContextClassLoader(
                                		service.getClassLoader()
                                );
                                return null;
                            }
                        }
                );
            }
        }
        return tc;
    }
    
    private static ClassLoader getContextClassLoader_doPriv() {
        return (ClassLoader) org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                }
        );
    }
    
    protected static void restoreThreadContext(final ThreadContextDescriptor tc) {
        org.apache.axis2.java.security.AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        Thread.currentThread().setContextClassLoader(tc.oldClassLoader);
                        return null;
                    }
                }
        );
        MessageContext.currentMessageContext.set(tc.oldMessageContext);
    }
}
