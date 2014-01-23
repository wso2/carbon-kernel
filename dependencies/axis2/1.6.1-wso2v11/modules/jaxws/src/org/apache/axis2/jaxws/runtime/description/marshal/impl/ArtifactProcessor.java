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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.message.databinding.ClassFinder;
import org.apache.axis2.jaxws.message.factory.ClassFinderFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Examines a ServiceDesc and locates and/or builds the JAX-WS artifacts. The JAX-WS artifacts are:
 * - request wrapper classes - response wrapper classes - fault beans for non-JAX-WS compliant
 * exceptions
 */
class ArtifactProcessor {

    private static final Log log = LogFactory.getLog(ArtifactProcessor.class);

    private ServiceDescription serviceDesc;
    private Map<OperationDescription, String> requestWrapperMap =
        new HashMap<OperationDescription, String>();
    private Map<OperationDescription, String> responseWrapperMap =
        new HashMap<OperationDescription, String>();
    private Map<OperationDescription, Method> methodMap =
        new HashMap<OperationDescription, Method>();
    private Map<FaultDescription, FaultBeanDesc> faultBeanDescMap =
        new HashMap<FaultDescription, FaultBeanDesc>();

    static final String JAXWS_SUBPACKAGE = "jaxws";

    /**
     * Artifact Processor
     *
     * @param serviceDesc
     */
    ArtifactProcessor(ServiceDescription serviceDesc) {
        this.serviceDesc = serviceDesc;
    }

    Map<OperationDescription, String> getRequestWrapperMap() {
        return requestWrapperMap;
    }

    Map<OperationDescription, String> getResponseWrapperMap() {
        return responseWrapperMap;
    }

    Map<FaultDescription, FaultBeanDesc> getFaultBeanDescMap() {
        return faultBeanDescMap;
    }

    Map<OperationDescription, Method> getMethodMap() {
        return methodMap;
    }

    void build() {
        for (EndpointDescription ed : serviceDesc.getEndpointDescriptions()) {
            if (ed.getEndpointInterfaceDescription() != null) {
                for (OperationDescription opDesc : ed.getEndpointInterfaceDescription()
                    .getOperations()) {

                    String declaringClassName = opDesc.getJavaDeclaringClassName();
                    String packageName = getPackageName(declaringClassName);
                    String simpleName = getSimpleClassName(declaringClassName);
                    String methodName = opDesc.getJavaMethodName();


                    // There is no default for @RequestWrapper/@ResponseWrapper classname  None is listed in Sec. 7.3 on p. 80 of
                    // the JAX-WS spec, BUT Conformance(Using javax.xml.ws.RequestWrapper) in Sec 2.3.1.2 on p. 13
                    // says the entire annotation "...MAY be omitted if all its properties would have default values."
                    // We will assume that this statement gives us the liberty to find a wrapper class/build a wrapper class or 
                    // implement an engine w/o the wrapper class.

                    // @RequestWrapper className processing
                    String requestWrapperName = opDesc.getRequestWrapperClassName();
                    String foundRequestWrapperName = getWrapperClass("@RequestWrapper",
                        requestWrapperName, 
                        packageName, 
                        javaMethodToClassName(methodName),
                        ed.getAxisService().getClassLoader(),
                        serviceDesc);

                    if (foundRequestWrapperName != null) {
                        requestWrapperMap.put(opDesc, foundRequestWrapperName);
                    }

                    // @ResponseWrapper className processing
                    String responseWrapperName = opDesc.getResponseWrapperClassName();
                    String foundResponseWrapperName = getWrapperClass("@ResponseWrapper",
                        responseWrapperName, 
                        packageName, 
                        javaMethodToClassName(methodName) + "Response",
                        ed.getAxisService().getClassLoader(),
                        serviceDesc);

                    if (foundResponseWrapperName != null) {
                        responseWrapperMap.put(opDesc, foundResponseWrapperName);
                    }

                    for (FaultDescription faultDesc : opDesc.getFaultDescriptions()) {
                        FaultBeanDesc faultBeanDesc = create(ed, faultDesc, opDesc);
                        faultBeanDescMap.put(faultDesc, faultBeanDesc);
                    }

                    // Get the Method
                    Class cls = null;
                    try {
                        cls = loadClass(declaringClassName, getContextClassLoader());
                    } catch(Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Class " + declaringClassName + " was not found by the Context ClassLoader.  " +
                                "Will use the ClassLoader associated with the service.  The exception is: " +e);
                        }
                    }

                    if (cls == null) {
                        try {
                            cls = loadClass(declaringClassName, ed.getAxisService().getClassLoader());
                        } catch(Exception e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Class " + declaringClassName + " was not found by the AxisService ClassLoader.  " +
                                    "Processing continues.  The exception is:" +e);
                            }

                        }
                    }
                    if (cls != null) {
                        Method method = getMethod(opDesc.getJavaMethodName(), cls);
                        if (method != null) {
                            methodMap.put(opDesc, method);
                        }
                    }

                }
            }
        }
    }

    /**
     * @param type "@RequestWrapper", "@ResponseWrapper", and "@WebFault"
     * @param providedValue String name of the Wrapper or Fault Bean from annotations
     * @param defaultPkg String name of the package to use for defaulting
     * @param defaultClassName name of the class to use if defaulting
     * @param altClassLoader name of the alternative classloader
     * @return
     */
    static private String getWrapperClass(String type,
        String providedValue, 
        String defaultPkg, 
        String defaultClassName, 
        ClassLoader altClassLoader,
        ServiceDescription serviceDesc) {

        if (log.isDebugEnabled()) {
            log.debug("getWrapperClass for " + type + " with value (" + providedValue + ")");
        }

        String wrapperClass = null;
        try {
           
            ClassLoader cl = getContextClassLoader();
            if (providedValue != null  && providedValue.length() > 0) {
                Class cls = null;
                // If a className is provided try to load it with the context classloader
                // and then the alternate classloader.
                // If the class still cannot be loaded, then try inserting the
                // jaxws sub-package.

                if (log.isDebugEnabled()) {
                    log.debug("Try finding the class with the name provided = " + providedValue);
                }
                cls = loadClassOrNull(providedValue, cl);
                if (cls != null) {
                    wrapperClass = providedValue;
                }
                else {
                    cls = loadClassOrNull(providedValue, altClassLoader);
                    if (cls != null) {
                        wrapperClass = providedValue;
                    }
                }
                // Legacy
                if (cls == null) {
                    String origPackage = getPackageName(providedValue);
                    if (origPackage.length() > 0) {
                        String newPackage = origPackage + "." + JAXWS_SUBPACKAGE;
                        String clsName = getSimpleClassName(providedValue);
                        String newValue = newPackage + "." + clsName;
                        if (log.isDebugEnabled()) {
                            log.debug("Did not find the name provided.  Now trying " + newValue);
                        }
                        cls = loadClassOrNull(newValue, cl);
                        if (cls != null) {
                            wrapperClass = newValue;
                        } else {
                            cls = loadClassOrNull(newValue, altClassLoader);
                            if (cls != null) {
                                wrapperClass = newValue;
                            }
                        }
                        
                        if(cls==null && 
                            (type.equals("@RequestWrapper")|| type.equals("@ResponseWrapper")||type.equals("@WebFault")|| type.equals("faultInfo"))){
                            
                            //Support for Fault Bean Generation
                            //As per JAX-WS 2.2 Specification section 3.7 an application programmer can choose not to
                            //package the faultBeans, if we have reached this point in the code then user has choosen
                            //not to package the fault bean. If there is a cache of generated artifacts available then 
                            //lets look for the missing faultBean there.
                            
                            //Support for Wrapper Bean Generation
                            //As per JAX-WS 2.2 Specificaiton section 3.6.2.1 pg 41 an application programmer does not use
                            //the wrapper bean classes, so the application need not package these classes. If we have reached
                            //this point in the code then user has choosen not to package these beans.
                            
                            //NOTE:If we find Generated artifacts from cache this guarantees that we will not use
                            //DocLitWrappedMinimum marshaller code. The advantage of normal DocLitWrappedMarshaller is
                            //that it is very robust and has support of lot more datatypes than in DocLitWrappedMinimum.
                            if(log.isDebugEnabled()){
                                log.debug("Adding cache to classpath");
                            }
                            ClassFinderFactory cff =
                                (ClassFinderFactory)FactoryRegistry.getFactory(ClassFinderFactory.class);
                            ClassFinder cf = cff.getClassFinder();
                            String cachePath = (String)serviceDesc.getAxisConfigContext().getProperty(Constants.WS_CACHE);
                            if(cachePath!=null){
                                //lets add the cache to classpath and retry loading missing artifacts.
                                if(log.isDebugEnabled()){
                                    log.debug("updating classpath with cache location");
                                }
                                cf.updateClassPath(cachePath, cl);
                                if(log.isDebugEnabled()){
                                    log.debug("trying to load class "+newValue+" from cache.");
                                }
                                cls=loadClassOrNull(newValue, cl);
                                if(cls!=null){
                                    wrapperClass=newValue;
                                }
                            }
                        }
                    }
                }
            } else {
                // If no value is provided by the annotation, then the we try default values.
                // The wsgen tool generates classes in the jaxws subpackage.
                // The wsimport tool generates classes in the same package as the SEI.
                // Note that from reading the JAX-WS spec, it seems that WSGen is doing that
                // correctly; See the conformance requirement in JAX-WS 2.0 Spec Section 3.6.2.1 Document
                // Wrapped on page 36: Conformance (Default wrapper bean package): In the absence of
                // customizations, the wrapper beans package MUST be a generated jaxws subpackage of the SEI
                // package.
                // However, if the class is in both places the runtime should prefer the one
                // in the non-jaxws package.  Why ?
                // The other classes in the non-jaxws package will cause the non-jaxws 
                // wrapper to get pulled in first....thus the jaxws wrapper will cause a collision.
                // 
                // Thus the following algorithm with check the non-jaxws package first
                
                Class cls1 = null;  // Class from the non-JAXWS package
                Class cls2 = null;  // Class from the JAX-WS package
                boolean cls1IsJAXB = false;
                boolean cls2IsJAXB = false;
                
                
                // Look for the class in the non-jaxws package first
                String defaultValue = null;
                if (defaultPkg.length() > 0) {
                    defaultValue = defaultPkg + "." + defaultClassName;
                } else {
                    defaultValue = defaultClassName;
                }
                if (log.isDebugEnabled()) {
                    log.debug("No provided value.  Try the default class name =  " + defaultValue);
                }
                cls1 = loadClassOrNull(defaultValue, cl);

                if (cls1 == null) {
                    cls1 = loadClassOrNull(defaultValue, altClassLoader);
                }
                if (cls1 != null) {
                    cls1IsJAXB = isJAXB(cls1);
                }

                // Now try the one in the jaxws subpackage (if cls1 is missing or perhaps not a JAXB class)
                if (cls1 == null || !cls1IsJAXB) {
                    if (defaultPkg.length() > 0) {
                        defaultValue = defaultPkg + "." + JAXWS_SUBPACKAGE + "." + defaultClassName;
                        if (log.isDebugEnabled()) {
                            log.debug("Did not find the default name.  Try a different default class name =  " + defaultValue);
                        }
                        cls2 = loadClassOrNull(defaultValue, cl);
                        if (cls2 == null) {
                            cls2 = loadClassOrNull(defaultValue, altClassLoader);
                        }
                        if(cls2==null && 
                        		(type.equals("@RequestWrapper")|| type.equals("@ResponseWrapper")||type.equals("@WebFault")|| type.equals("faultInfo"))){

                        	//Support for Fault Bean Generation
                        	//As per JAX-WS 2.2 Specification section 3.7 an application programmer can choose not to
                        	//package the faultBeans, if we have reached this point in the code then user has choosen
                        	//not to package the fault bean. If there is a cache of generated artifacts available then 
                        	//lets look for the missing faultBean there.

                        	//Support for Wrapper Bean Generation
                        	//As per JAX-WS 2.2 Specificaiton section 3.6.2.1 pg 41 an application programmer does not use
                        	//the wrapper bean classes, so the application need not package these classes. If we have reached
                        	//this point in the code then user has choosen not to package these beans.

                        	//NOTE:If we find Generated artifacts from cache this guarantees that we will not use
                        	//DocLitWrappedMinimum marshaller code. The advantage of normal DocLitWrappedMarshaller is
                        	//that it is very robust and has support of lot more datatypes than in DocLitWrappedMinimum.
                        	if(log.isDebugEnabled()){
                        		log.debug("Adding cache to classpath");
                        	}
                            if(log.isDebugEnabled()){
                                log.debug("Adding cache to classpath");
                            }
                            ClassFinderFactory cff =
                                (ClassFinderFactory)FactoryRegistry.getFactory(ClassFinderFactory.class);
                            ClassFinder cf = cff.getClassFinder();
                            String cachePath = (String)serviceDesc.getAxisConfigContext().getProperty(Constants.WS_CACHE);
                            if(log.isDebugEnabled()){
                                log.debug("cachePath = "+cachePath);
                            }
                            if(cachePath!=null){
                                //lets add the cache to classpath and retry loading missing artifacts.
                                if(log.isDebugEnabled()){
                                    log.debug("updating classpath with cache location");
                                }
                                cf.updateClassPath(cachePath, cl);
                                if(log.isDebugEnabled()){
                                    log.debug("trying to load class "+defaultValue+" from cache.");
                                }
                                cls2=loadClassOrNull(defaultValue, cl);
                            }
                        }
                    }  
                }
                
                if (cls2 !=null) {
                    cls2IsJAXB = isJAXB(cls2);
                }
                
                // Choose the wrapper class
                if (cls1 == null && cls2 == null) {
                    if(log.isDebugEnabled()){
                        log.debug("Could not find a wrapper class");
                    }
                    wrapperClass = null;
                } else if (cls1 == null) {
                    wrapperClass = cls2.getCanonicalName();
                    if(log.isDebugEnabled()){
                        log.debug("Choosing " + wrapperClass);
                    }
                } else if (cls2 == null) {
                    wrapperClass = cls1.getCanonicalName();
                    if(log.isDebugEnabled()){
                        log.debug("Choosing " + wrapperClass);
                    }
                } else {
                    if(log.isDebugEnabled()){
                        log.debug("There are two classes that are present " + cls1 + " and " + cls2);
                    }
                    // Choose the one that is JAXB enabled
                    if (!cls1IsJAXB && !cls2IsJAXB) {
                        // If neither is JAXB enabled.  Choose the one in the jaxws package.
                        // This is the one most likely provided by tooling.
                        if(log.isDebugEnabled()){
                            log.debug("Neither are JAXB enabled. Choosing " + cls2);
                        }
                        wrapperClass = cls2.getCanonicalName();
                    } else if (cls1IsJAXB && cls2IsJAXB) {
                        // If both are JAXB enabled, choose the one in the non-JAXWS package.
                        // This generally means that multiple tools generated the packages.
                        // Choosing the one in the non-JAXWS package will avoid a JAXBContext collision.
                        if(log.isDebugEnabled()){
                            log.debug("Both are JAXB enabled. Choosing " + cls1);
                        }
                        wrapperClass = cls1.getCanonicalName();
                    } else if (cls1IsJAXB) {
                        if(log.isDebugEnabled()){
                            log.debug("Choosing " + cls1 + " because it is JAXB enabled");
                        }
                        wrapperClass = cls1.getCanonicalName();
                    } else {
                        if(log.isDebugEnabled()){
                            log.debug("Choosing " + cls2 + " because it is JAXB enabled");
                        }
                        wrapperClass = cls2.getCanonicalName();
                    }
                }
                
            } 
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("Unexpected error.  Processing continues. ", t);
            }
        } 
        if (log.isDebugEnabled()) {
            log.debug("exit getWrapperClass with " + wrapperClass);
        }  
        return wrapperClass;

    }

    private FaultBeanDesc create(EndpointDescription ed, FaultDescription faultDesc, OperationDescription opDesc) {
        /* FaultBeanClass algorithm
         *   1) The class defined on @WebFault of the exception
         *   2) If not present or invalid, the class defined by getFaultInfo.
         *   3) If not present, the class is found using the default name and location
         */
        String declaringClassName = opDesc.getJavaDeclaringClassName();

        String type = "@WebFault";
        String faultBeanClassName = faultDesc.getFaultBean();
        if (faultBeanClassName == null || faultBeanClassName.length() == 0) {
            type = "faultInfo";
            faultBeanClassName = faultDesc.getFaultInfo();
        }
        String foundClassName = getWrapperClass(type,
            faultBeanClassName,
            getPackageName(declaringClassName), 
            getSimpleClassName(faultDesc.getExceptionClassName()) + "Bean",
            ed.getAxisService().getClassLoader(),
            serviceDesc);
        if (foundClassName == null) {
            faultBeanClassName = missingArtifact(faultBeanClassName);
        }
        if (foundClassName != null) {
            faultBeanClassName = foundClassName;
        }

        /* Local NameAlgorithm:
         *   1) The name defined on the @WebFault of the exception.
         *   2) If not present, the name defined via the @XmlRootElement of the fault bean class.
         *   3) If not present, the <exceptionName>Bean
         */
        String faultBeanLocalName = faultDesc.getName();
        if (faultBeanLocalName == null || faultBeanLocalName.length() == 0) {
            if (faultBeanClassName != null && faultBeanClassName.length() > 0) {
                try {
                    Class faultBean;
                    try {
                        faultBean = loadClass(faultBeanClassName, getContextClassLoader());
                    } catch (ClassNotFoundException e){
                        faultBean = loadClass(faultBeanClassName, ed.getAxisService().getClassLoader());
                    }
                    AnnotationDesc aDesc = AnnotationDescImpl.create(faultBean);
                    if (aDesc.hasXmlRootElement()) {
                        faultBeanLocalName = aDesc.getXmlRootElementName();
                    }
                } catch (Throwable t) {
                    throw ExceptionFactory.makeWebServiceException(t);
                }
            }
        }
        if (faultBeanLocalName == null || faultBeanLocalName.length() == 0) {
            faultBeanLocalName = getSimpleClassName(faultDesc.getExceptionClassName()) + "Bean";
        }

        /* Algorithm for fault bean namespace
         *   1) The namespace defined on the @WebFault of the exception.
         *   2) If not present, the namespace defined via the @XmlRootElement of the class name.
         *   3) If not present, the namespace of the method's declared class + "/jaxws"
         */
        String faultBeanNamespace = faultDesc.getTargetNamespace();
        if (faultBeanNamespace == null || faultBeanNamespace.length() == 0) {
            if (faultBeanClassName != null && faultBeanClassName.length() > 0) {
                try {
                    Class faultBean;
                    try {
                        faultBean = loadClass(faultBeanClassName, getContextClassLoader());
                    } catch (ClassNotFoundException e){
                        faultBean = loadClass(faultBeanClassName, ed.getAxisService().getClassLoader());
                    }
                    AnnotationDesc aDesc = AnnotationDescImpl.create(faultBean);
                    if (aDesc.hasXmlRootElement()) {
                        faultBeanNamespace = aDesc.getXmlRootElementNamespace();
                    }
                } catch (Throwable t) {
                    throw ExceptionFactory.makeWebServiceException(t);
                }
            }
        }
        if (faultBeanNamespace == null || faultBeanNamespace.length() == 0) {
            faultBeanNamespace = opDesc.getEndpointInterfaceDescription().getTargetNamespace();
        }

        return new FaultBeanDescImpl(
            faultBeanClassName,
            faultBeanLocalName,
            faultBeanNamespace);
    }

    /**
     * @param className
     * @return package name
     */
    private static String getPackageName(String className) {
        int index = className.lastIndexOf(".");
        if (index <= 0) {
            return "";
        } else {
            return className.substring(0, index);
        }
    }

    /**
     * @param className
     * @return simple class name
     */
    private static String getSimpleClassName(String className) {
        int index = className.lastIndexOf(".");
        if (index <= 0) {
            return className;
        } else {
            return className.substring(index + 1);
        }
    }

    /**
     * @param methodName
     * @return method name converted into a class name
     */
    private static String javaMethodToClassName(String methodName) {
        String className = null;
        if (methodName != null) {
            StringBuffer buildClassName = new StringBuffer(methodName);
            buildClassName.replace(0, 1, methodName.substring(0, 1).toUpperCase());
            className = buildClassName.toString();
        }
        return className;
    }

    /**
     * This method is invoked if the artifact is missing
     *
     * @param artifactName
     * @return newly constructed name or null
     */
    private String missingArtifact(String artifactName) {

        if (log.isDebugEnabled()) {
            log.debug("The following class was not found: " + artifactName + " Processing continues without this class.");
        }
        return null;
    }

    /**
     * @param className
     * @param classLoader
     * @return Class or Null
     */
    private static Class loadClassOrNull(String className, ClassLoader classLoader) {
        try {
            return loadClass(className, classLoader);
        } catch (Throwable t) {
            return null;
        }  
    }

    private static Class loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        // Don't make this public, its a security exposure
        return forName(className, true, classLoader);
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
        final ClassLoader classloader) throws ClassNotFoundException {
        // NOTE: This method must remain protected because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run() throws ClassNotFoundException {
                        // Class.forName does not support primitives
                        Class cls = ClassUtils.getPrimitiveClass(className);
                        try{
                            if (cls == null) {
                                cls = Class.forName(className, initialize, classloader);
                            }
                            return cls;
                            //Lets catch NoClassDefFoundError as its part of Throwable
                            //Any Exception that extends Exception will be handled by doPriv method.    
                        } catch (NoClassDefFoundError e) {
                            /**
                             * In different jaxws scenarios, some classes may be missing.  So it is normal behavior
                             * to get to this point.  The exception is swallowed and a null is returned.  
                             * The exception is not logged...as this would give servicability folks the idea that a problem occurred.
                             */
                        } 
                        return cls;
                    }
                }
            );
        } catch (PrivilegedActionException e) {
            /**
             * In different jaxws scenarios, some classes may be missing.  So it is normal behavior
             * to get to this point. 
             * The exception is not logged...as this would give servicability folks the idea that a problem occurred.
             */
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }

    /**
     * Return the Method matching the method name or null
     * @param methodName String containing method name
     * @param cls Class of the class that declares the method
     *
     * @return Method or null
     */
    private static Method getMethod(final String methodName, final Class cls) {
        // NOTE: This method must remain protected because it uses AccessController
        Method method = null;
        try {
            method = (Method)AccessController.doPrivileged(
                new PrivilegedExceptionAction() {
                    public Object run()  {
                        Method[] methods = cls.getMethods();
                        if (methods != null) {
                            for (int i=0; i<methods.length; i++) {
                                if (methods[i].getName().equals(methodName)) {
                                    return methods[i];
                                }
                            }
                        }
                        return null;
                    }
                }
            );
        } catch (PrivilegedActionException e) {

        }

        return method;
    }

    /** @return ClassLoader */
    private static ClassLoader getContextClassLoader() {
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
     * @param cls
     * @return true if cls appears to be a JAXB enabled class
     */
    private static boolean isJAXB(Class cls) {
        // See if the object represents a root element
        XmlRootElement root = (XmlRootElement)
            getAnnotation(cls,XmlRootElement.class);
        if (root != null) {
            if (log.isDebugEnabled()) {
                log.debug("isJAXB returns true due to presence of @XmlRootElement on " + cls);
            }
            return true;
        }
        
        // See if the object represents an type
        XmlType type = (XmlType)
            getAnnotation(cls,XmlType.class);
        if (type != null) {
            if (log.isDebugEnabled()) {
                log.debug("isJAXB returns true due to presence of @XmlType on " + cls);
            }
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug("isJAXB returns false for" + cls);
        }
        return false;
    }
    /**
     * Get an annotation.  This is wrappered to avoid a Java2Security violation.
     * @param cls Class that contains annotation 
     * @param annotation Class of requested Annotation
     * @return annotation or null
     */
    private static Annotation getAnnotation(final AnnotatedElement element, final Class annotation) {
        Annotation anno = null;
        try {
        anno = (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return element.getAnnotation(annotation);
            }
        });
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("Problem occurred.  Continuing.  The problem is " + t);
            }
        }
        return anno;
    }
}
