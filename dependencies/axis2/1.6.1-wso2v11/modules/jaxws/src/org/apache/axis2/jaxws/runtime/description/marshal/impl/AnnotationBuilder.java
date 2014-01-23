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

package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBElement;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

/**
 * Walks the ServiceDescription and its child *Description classes to find all of the types.  An
 * AnnotationDesc is built for each of the types
 */
public class AnnotationBuilder {

    private static Log log = LogFactory.getLog(AnnotationBuilder.class);


    /** This is a static utility class.  The constructor is intentionally private */
    private AnnotationBuilder() {
    }


    /**
     * @param serviceDescription ServiceDescription
     * @param ap                 ArtifactProcessor which found/produced artifact classes
     * @return AnnotationDesc Map
     */
    public static Map<String, AnnotationDesc> getAnnotationDescs(ServiceDescription serviceDesc,
                                                                 ArtifactProcessor ap) {
        Map<String, AnnotationDesc> map = new HashMap<String, AnnotationDesc>();
        EndpointDescription[] endpointDescs = serviceDesc.getEndpointDescriptions();

        // Build a set of packages from all of the endpoints
        if (endpointDescs != null) {
            for (int i = 0; i < endpointDescs.length; i++) {
                getAnnotationDescs(endpointDescs[i], ap, map);
            }
        }
        return map;
    }


    /**
     * @param endpointDesc
     * @param ap           ArtifactProcessor which found/produced artifact classes
     * @param map
     */
    private static void getAnnotationDescs(EndpointDescription endpointDesc,
                                           ArtifactProcessor ap, 
                                           Map<String, AnnotationDesc> map) {
        String implClassName = getServiceImplClassName(endpointDesc);
        if (implClassName != null) {
            Class clz;
            clz = loadClass(implClassName);
            if(clz == null){
                clz = loadClass(implClassName, endpointDesc.getAxisService().getClassLoader());
            }
            if (clz != null) {
                addAnnotation(endpointDesc, clz, map);
            }
        }
        EndpointInterfaceDescription endpointInterfaceDesc =
                endpointDesc.getEndpointInterfaceDescription();
        if (endpointInterfaceDesc != null) {
            getAnnotationDescs(endpointDesc, endpointInterfaceDesc, ap, map);
        }
    }


    /**
     * @param endpointInterfaceDesc
     * @param ap                    ArtifactProcessor which found/produced artifact classes
     * @param map
     */
    private static void getAnnotationDescs(EndpointDescription endpointDesc, 
                                           EndpointInterfaceDescription endpointInterfaceDesc,
                                           ArtifactProcessor ap,
                                           Map<String, AnnotationDesc> map) {
        Class clz = endpointInterfaceDesc.getSEIClass();
        if (clz != null) {
            addAnnotation(endpointDesc, clz, map);
        }
        
        // Don't dig into the async operations
        OperationDescription[] opDescs = endpointInterfaceDesc.getDispatchableOperations();

        // Build a set of packages from all of the opertions
        if (opDescs != null) {
            for (int i = 0; i < opDescs.length; i++) {
                getAnnotationDescs(endpointDesc, opDescs[i], ap, map);
            }
        }
    }


    /**
     * Get annotations for this operation
     *
     * @param opDesc
     * @param ap     ArtifactProcessor which found/produced artifact classes
     * @param map
     */
    private static void getAnnotationDescs(EndpointDescription endpointDesc,
                                           OperationDescription opDesc,
                                           ArtifactProcessor ap,
                                           Map<String, AnnotationDesc> map) {

        // Walk the parameter information
        ParameterDescription[] parameterDescs = opDesc.getParameterDescriptions();
        if (parameterDescs != null) {
            for (int i = 0; i < parameterDescs.length; i++) {
                getAnnotationDescs(endpointDesc, parameterDescs[i], map);
            }
        }

        // Walk the fault information
        FaultDescription[] faultDescs = opDesc.getFaultDescriptions();
        if (faultDescs != null) {
            for (int i = 0; i < faultDescs.length; i++) {
                getAnnotationDescs(endpointDesc, faultDescs[i], ap, map);
            }
        }

        // Also consider the request and response wrappers
        String wrapperName = ap.getRequestWrapperMap().get(opDesc);
        if (wrapperName != null) {
            addAnnotation(endpointDesc, wrapperName, map);
        }
        wrapperName = ap.getResponseWrapperMap().get(opDesc);
        if (wrapperName != null) {
            addAnnotation(endpointDesc, wrapperName, map);
        }

        // Finally consider the result type
        Class cls = opDesc.getResultActualType();
        if (cls != null && cls != void.class && cls != Void.class) {
            addAnnotation(endpointDesc, cls, map);
        }
    }


    private static void getAnnotationDescs(EndpointDescription endpointDesc,
                                           ParameterDescription paramDesc,
                                           Map<String, AnnotationDesc> map) {

        // Get the type that defines the actual data.  (this is never a holder )
        Class paramClass = paramDesc.getParameterActualType();

        if (paramClass != null) {
            getTypeAnnotationDescs(endpointDesc, paramClass, map);
        }

    }

    /**
     * Update the package set with the packages referenced by this FaultDescription
     *
     * @param faultDesc FaultDescription
     * @param set       Set<Package> that is updated
     */
    private static void getAnnotationDescs(EndpointDescription endpointDesc, 
                                           FaultDescription faultDesc,
                                           ArtifactProcessor ap,
                                           Map<String, AnnotationDesc> map) {
        FaultBeanDesc faultBeanDesc = ap.getFaultBeanDescMap().get(faultDesc);
        Class faultBean = loadClass(faultBeanDesc.getFaultBeanClassName());
        if(faultBean == null) {
            faultBean = loadClass(faultBeanDesc.getFaultBeanClassName(), endpointDesc.getAxisService().getClassLoader());    
        }
        if (faultBean != null) {
            getTypeAnnotationDescs(endpointDesc, faultBean, map);
        }
    }

    private final static Class[] noClass = new Class[] { };

    /**
     * Get the annotations for this type
     *
     * @param cls
     */
    private static void getTypeAnnotationDescs(EndpointDescription endpointDesc, Class cls, Map<String, AnnotationDesc> map) {

        if (JAXBElement.class.isAssignableFrom(cls)) {
            try {
                Method m = cls.getMethod("getValue", noClass);
                Class cls2 = m.getReturnType();
                addAnnotation(endpointDesc, cls2, map);

            } catch (Exception e) {
                // We should never get here
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find JAXBElement.getValue method.");
                }
            }
        } else {
            addAnnotation(endpointDesc, cls, map);
        }
    }

    private static void addAnnotation(EndpointDescription endpointDesc, String className, Map<String, AnnotationDesc> map) {

        if (map.get(className) == null) {
            Class clz = loadClass(className);
            if (clz == null) {
                clz = loadClass(className, endpointDesc.getAxisService().getClassLoader());    
            }
            if (clz != null) {
                addAnnotation(endpointDesc, clz, map);
            }
        }
    }

    private static void addAnnotation(EndpointDescription endpointDesc, Class cls, Map<String, AnnotationDesc> map) {

        String className = cls.getCanonicalName();
        if (map.get(className) == null) {
            AnnotationDesc desc = AnnotationDescImpl.create(cls);
            map.put(className, desc);
            if (cls.isPrimitive()) {
                Class class2 = ClassUtils.getWrapperClass(cls);
                AnnotationDesc desc2 = AnnotationDescImpl.create(class2);
                map.put(class2.getCanonicalName(), desc2);
            } else {
                Class class2 = ClassUtils.getPrimitiveClass(cls);
                if (class2 != null) {
                    AnnotationDesc desc2 = AnnotationDescImpl.create(class2);
                    map.put(class2.getCanonicalName(), desc2);
                }
            }
            
            // Inspect the interfaces.  This is done to pick up other
            // @XmlSeeAlso usages.
            Class[] interfaces = cls.getInterfaces();
            if (interfaces != null) {
                for (int i=0; i<interfaces.length; i++) {
                    addAnnotation(endpointDesc, interfaces[i], map);
                }
            }
            
        }
    }

    /**
     * Loads the class
     *
     * @param className
     * @return Class (or null if the class cannot be loaded)
     */
    private static Class loadClass(String className, ClassLoader loader) {
        // Don't make this public, its a security exposure
        if (className == null || className.length() == 0) {
            return null;
        }
        try {

            return forName(className, true,
                           loader);
            //Catch Throwable as ClassLoader can throw an NoClassDefFoundError that
            //does not extend Exception, so lets catch everything that extends Throwable
            //rather than just Exception.
        } catch (Throwable e) {
            // Depending on the jaxws scenario, it is normal that some classes are not available.
            if (log.isDebugEnabled()) {
                log.debug("AnnotationBuilder did not find the following class:" + className);
            }
        }
        return null;
    }

    /**
     * Loads the class
     *
     * @param className
     * @return Class (or null if the class cannot be loaded)
     */
    private static Class loadClass(String className) {
        // Don't make this public, its a security exposure
        if (className == null || className.length() == 0) {
            return null;
        }
        try {

            return forName(className, true,
                           getContextClassLoader());
            //Catch Throwable as ClassLoader can throw an NoClassDefFoundError that
            //does not extend Exception, so lets catch everything that extends Throwable
            //rather than just Exception.
        } catch (Throwable e) {
            // Depending on the jaxws scenario, it is normal that some classes are not available.
            if (log.isDebugEnabled()) {
                log.debug("AnnotationBuilder did not find the following class:" + className);
            }
        }
        return null;
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    static Class forName(final String className, final boolean initialize,
                         final ClassLoader classloader) throws ClassNotFoundException {
        // NOTE: This method must remain protected because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            // Class.forName does not support primitives
                            Class cls = ClassUtils.getPrimitiveClass(className);
                            if (cls == null) {
                                cls = Class.forName(className, initialize, classloader);
                            }
                            return cls;
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            /* An exception should NOT be logged.  Depending on the JAXWS scenario, certain classes 
             * may or may not exist.  Putting an exception in the log will confuse programmers who
             * are servicing the product
             * 
             
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            */
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }

    /** @return ClassLoader */
    static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (RuntimeException)e.getException();
        }

        return cl;
    }
    
    /**
     * Get the Serivce Impl Class by looking at the AxisService
     * @param endpointDescription
     * @return class name or null
     */
    static private String getServiceImplClassName(EndpointDescription endpointDescription) {
        String result = null;
        if (endpointDescription != null) {
            AxisService as = endpointDescription.getAxisService();
            if (as != null) {
                Parameter param = as.getParameter(org.apache.axis2.Constants.SERVICE_CLASS);

                // If there was no implementation class, we should not go any further
                if (param != null) {
                    result = ((String)param.getValue()).trim();
                }
            }
        }
        return result;
    }
}
