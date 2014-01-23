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

package org.apache.axis2.jaxws.server.endpoint.injection.impl;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.context.WebServiceContextImpl;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.injection.ResourceInjectionException;
import org.apache.axis2.jaxws.server.endpoint.injection.WebServiceContextInjector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class WebServiceContextInjectorImpl implements WebServiceContextInjector {
    private static final Log log = LogFactory.getLog(WebServiceContextInjectorImpl.class);
    private static String METHOD_NAME = "set";
    private static int NUMBER_OF_PARAMETERS = 1;
    private static String RETURN_TYPE = "void";

    public WebServiceContextInjectorImpl() {
        super();

    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.server.endpoint.injection.WebServiceContextInjection#addMessageContext(javax.xml.ws.WebServiceContext, javax.xml.ws.handler.MessageContext)
      */
    public void addMessageContext(WebServiceContext wc, MessageContext mc) {
        WebServiceContextImpl wsContext = (WebServiceContextImpl)wc;
        wsContext.setSoapMessageContext(mc);

    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjection#inject(java.lang.Object, java.lang.Object)
      */
    public void inject(Object resource, Object instance) throws ResourceInjectionException {
        if (instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot inject Resource on a null Service Instance.");
            }
            throw new ResourceInjectionException(
                    Messages.getMessage("WebServiceContextInjectionImplErr1"));
        }

        Class serviceClazz = instance.getClass();

        /*Look for @Resource annotation on Field. If found then look for type on the annotation. If found, then
           * if type is java.lang.Object then make sure type on declared field in javax.xml.WebServiceContext and assign the resource value
           * if type is javax.xml.WebServiceContext then assign the resource value
          */
        Field resourceField = searchFieldsForResourceAnnotation(serviceClazz);
        if (resourceField != null) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to inject Resource on Field");
            }
            //Found field that has a @Resource for WebServiceContext
            //Inject Resource.
            injectOnField(resource, instance, resourceField);
            if (log.isDebugEnabled()) {
                log.debug("Resource Injected on Field");
            }
            return;
        }

        /* If @Resource annotation not found on declared Fileds, then look for it on Methods. If found then,
           * if @Resource type is java.lang.Object then,
           * look for PropertyDescriptor who's write Method or setter is the Method on which you found the annotation and
           * make sure that the declared type of that property is javax.xml.WebServiceContext and invoke the Method with Resource.
           * if @Resource type is javax.xml.ws.WebServiceContext, Invoke the Method with Resource.
           */
        Method method = searchMethodsResourceAnnotation(serviceClazz);
        if (method != null) {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to inject resource on Method");
            }
            if (!isValidMethod(method)) {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "injection can happen using method if method name starts with \"set\" returns a void and only has one parameter and the type of this parameter must be compatible with resource.");
                }
                throw new ResourceInjectionException(
                        Messages.getMessage("WebServiceContextInjectionImplErr6"));
            }
            injectOnMethod(resource, instance, method);
            if (log.isDebugEnabled()) {
                log.debug("Resource Injected");
            }
            return;
        }

        //Nothing to inject
    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjection#injectOnClass(java.lang.Object, java.lang.Object, java.lang.Class)
      */
    public void injectOnClass(Object resource, Object instance, Class clazz)
            throws ResourceInjectionException {
        throw new UnsupportedOperationException(Messages.getMessage("injectOnClsErr"));

    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjection#injectOnField(java.lang.Object, java.lang.Object, java.lang.reflect.Field)
      */
    public void injectOnField(Object resource, Object instance, Field field)
            throws ResourceInjectionException {
        if (instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot inject Resource on a null Service Instance.");
            }
            throw new ResourceInjectionException(
                    Messages.getMessage("WebServiceContextInjectionImplErr1"));
        }
        if (field == null) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Cannot inject WebServiceContext on ServiceInstance Field, field cannot be NULL");
            }
            throw new ResourceInjectionException(
                    Messages.getMessage("WebServiceContextInjectionImplErr3"));
        }
        try {
            if (!Modifier.isPublic(field.getModifiers())) {
                setAccessible(field, true);
            }
            //Inject Resource.
            field.set(instance, resource);
        } catch (IllegalAccessException e) {
            throw new ResourceInjectionException(e);
        }

    }

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.server.endpoint.injection.ResourceInjection#injectOnMethod(java.lang.Object, java.lang.Object, java.lang.reflect.Method)
      */
    public void injectOnMethod(Object resource, Object instance, Method method)
            throws ResourceInjectionException {
        if (instance == null) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot inject Resource on a null Service Instance.");
            }
            throw new ResourceInjectionException(
                    Messages.getMessage("WebServiceContextInjectionImplErr1"));
        }
        if (method == null) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Cannot inject WebServiceContext on ServiceInstance Method, method cannot be NULL");
            }
            throw new ResourceInjectionException(
                    Messages.getMessage("WebServiceContextInjectionImplErr3"));
        }
        try {
            if (!Modifier.isPublic(method.getModifiers())) {
                setAccessible(method, true);
            }
            method.invoke(instance, resource);
            return;
        } catch (IllegalAccessException e) {
            throw new ResourceInjectionException(e);
        } catch (InvocationTargetException e) {
            throw new ResourceInjectionException(e);
        }


    }

    /**
     * Set accessible.  This method must remain private
     *
     * @param obj   AccessibleObject
     * @param value true or false
     */
    private static void setAccessible(final AccessibleObject obj, final boolean value) {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                obj.setAccessible(value);
                return null;
            }
        });

    }

    /*
      * Search for Field with @Resource Annotation.
      */
    private Field searchFieldsForResourceAnnotation(Class bean) {
        if (bean == null) {
            return null;
        }
        List<Field> fields = getFields(bean);
        for (Field field : fields) {
            Annotation[] annotations = field.getAnnotations();
            for (Annotation an : annotations) {
                if (Resource.class.isAssignableFrom(an.getClass())) {
                    //check to make sure it is a @Resource for WebServiceContext.
                    Resource atResource = (Resource)an;
                    Class type = atResource.type();
                    if (isWebServiceContextResource(atResource, field)) {
                        return field;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets all of the fields in this class and the super classes
     *
     * @param beanClass
     * @return
     */
    static private List<Field> getFields(final Class beanClass) {
        // This class must remain private due to Java 2 Security concerns
        List<Field> fields;
        fields = (List<Field>)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        List<Field> fields = new ArrayList<Field>();
                        Class cls = beanClass;
                        while (cls != null) {
                            Field[] fieldArray = cls.getDeclaredFields();
                            for (Field field : fieldArray) {
                                fields.add(field);
                            }
                            cls = cls.getSuperclass();
                        }
                        return fields;
                    }
                }
        );

        return fields;
    }

    /**
     * Gets all of the fields in this class and the super classes
     *
     * @param beanClass
     * @return
     */
    static private List<Method> getMethods(final Class beanClass) {
        // This class must remain private due to Java 2 Security concerns
        List<Method> methods;
        methods = (List<Method>)AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        List<Method> methods = new ArrayList<Method>();
                        Class cls = beanClass;
                        while (cls != null) {
                            Method[] methodArray = cls.getDeclaredMethods();
                            for (Method method : methodArray) {
                                methods.add(method);
                            }
                            cls = cls.getSuperclass();
                        }
                        return methods;
                    }
                }
        );

        return methods;
    }

    /*
      * Search for Method with @Resource Annotation
      */
    private Method searchMethodsResourceAnnotation(Class bean) {
        if (bean == null) {
            return null;
        }
        List<Method> methods = getMethods(bean);
        for (Method method : methods) {
            Annotation[] annotations = method.getAnnotations();
            for (Annotation an : annotations) {
                if (Resource.class.isAssignableFrom(an.getClass())) {
                    //check to make sure it is a @Resource for WebServiceContext.
                    Resource atResource = (Resource)an;
                    if (isWebServiceContextResource(atResource, method)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private boolean isWebServiceContextResource(Resource atResource, Field field) {
        Class type = atResource.type();
        if (type == java.lang.Object.class) {
            if (field != null && field.getType() == WebServiceContext.class) {
                return true;
            }
        } else if (type == WebServiceContext.class) {
            //TODO: Should I check if the field declared type is assignable from WebServiceContext. Spec is not clear about this.
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug(
                    "Invalid Field type or Resource Type found, cannot inject WebServiceContext on this field");
        }
        return false;
    }

    private boolean isWebServiceContextResource(Resource atResource, Method method) {
        //As per JSR-250 the method injection is nothing but setter based injection,
        //Such a injection can happen if method name starts with "set" returns a void and
        //only has one parameter and the type of this parameter must be compatible with
        //resource.

        Class type = atResource.type();
        Class[] paramTypes = method.getParameterTypes();
        for (Class paramType : paramTypes)
            if (type == java.lang.Object.class) {
                if (paramType == WebServiceContext.class ||
                        paramType.isAssignableFrom(WebServiceContext.class)) {
                    return true;
                }
            } else if (type == WebServiceContext.class) {
                //TODO: Should I check if the field declared type is assignable from WebServiceContext. Spec is not clear about this.
                return true;
            }
        if (log.isDebugEnabled()) {
            log.debug(
                    "Invalid Field type or Resource Type found, cannot inject WebServiceContext on this method");
        }
        return false;
    }

    private boolean isValidMethod(Method method) {
        //As per JSR-250 the method injection is nothing but setter based injection,
        //Such a injection can happen if method name starts with "set" returns a void and
        //only has one parameter and the type of this parameter must be compatible with
        //resource.
        String name = method.getName();
        Class returnType = method.getReturnType();
        Class[] types = method.getParameterTypes();
        int noOfDeclaredParameter = 0;
        if (types != null) {
            noOfDeclaredParameter = types.length;
        }

        if (name.startsWith(METHOD_NAME) && noOfDeclaredParameter == NUMBER_OF_PARAMETERS &&
                returnType.getName().equals(RETURN_TYPE)) {
            return true;
        }
        if (log.isDebugEnabled()) {
            log.debug(
                    "Method found with @Resource annotaion and input param to set WebServiceContext Object, However method did not meet the criteria for injection as per JSR-250");
        }
        return false;

    }


}
