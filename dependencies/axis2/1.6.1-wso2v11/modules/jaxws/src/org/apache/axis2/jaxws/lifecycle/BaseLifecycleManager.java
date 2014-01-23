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

package org.apache.axis2.jaxws.lifecycle;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.java.security.AccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

public abstract class BaseLifecycleManager {
    
    private static final Log log = LogFactory.getLog(BaseLifecycleManager.class);
    
    protected Object instance;
    
    public void invokePostConstruct() throws LifecycleException {
        if (instance == null) {
            throw new LifecycleException(Messages.getMessage("EndpointLifecycleManagerImplErr1"));
        }
        Method method = getPostConstructMethod();
        if (method != null) {
            invokePostConstruct(method);
        }
    }

    protected void invokePostConstruct(final Method method) throws LifecycleException {
        if (log.isDebugEnabled()) {
            log.debug("Invoking Method with @PostConstruct annotation");
        }
        /*
         * As per JSR-250 pre destroy and post construct can be
         * public, protected, private or default encapsulation.
         * I will check and make sure the methods are accessible
         * before we invoke them.
         * 
         */
        
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws InvocationTargetException, IllegalAccessException {
                            if(!method.isAccessible()){
                                method.setAccessible(true);
                            }
                            return null;
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            throw new LifecycleException(e.getException());
        }
        invokeMethod(method, null);
        if (log.isDebugEnabled()) {
            log.debug("Completed invoke on Method with @PostConstruct annotation");
        }
    }

    public void invokePreDestroy() throws LifecycleException {
        if (instance == null) {
            throw new LifecycleException(Messages.getMessage("EndpointLifecycleManagerImplErr1"));
        }
        Method method = getPreDestroyMethod();
        if (method != null) {
            invokePreDestroy(method);
        }
    }

    protected void invokePreDestroy(Method method) throws LifecycleException {
        if (log.isDebugEnabled()) {
            log.debug("Invoking Method with @PreDestroy annotation");
        }
        invokeMethod(method, null);
        if (log.isDebugEnabled()) {
            log.debug("Completed invoke on Method with @PreDestroy annotation");
        }
    }

    protected void invokeMethod(final Method m, final Object[] params) throws LifecycleException {
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws InvocationTargetException, IllegalAccessException {
                            return m.invoke(instance, params);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            throw new LifecycleException(e.getException());
        }
    }

    protected Method getPostConstructMethod() {
        // REVIEW: This method should not be called in performant situations.
        // Plus the super class methods are not being considered 

        //return Method with @PostConstruct Annotation.
        if (instance != null) {
            final Class endpointClazz = instance.getClass();
            Method[] methods = (Method[]) AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            return endpointClazz.getMethods();
                        }
                    }
            );

            for (Method method : methods) {
                if (isPostConstruct(method)) {
                    return method;
                }
            }
        }
        return null;
    }

    protected Method getPreDestroyMethod() {
        // REVIEW: This method should not be called in performant situations.
        // Plus the super class methods are not being considered 
        //return Method with @PreDestroy Annotation
        if (instance != null) {
            final Class endpointClazz = instance.getClass();
            Method[] methods = (Method[]) AccessController.doPrivileged(
                    new PrivilegedAction() {
                        public Object run() {
                            return endpointClazz.getMethods();
                        }
                    }
            );

            for (Method method : methods) {
                if (isPreDestroy(method)) {
                    return method;
                }
            }
        }
        return null;
    }

    protected boolean isPostConstruct(final Method method) {
        Annotation[] annotations = (Annotation[]) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return method.getDeclaredAnnotations();
                    }
                }
        );
        for (Annotation annotation : annotations) {
            return PostConstruct.class.isAssignableFrom(annotation.annotationType());
        }
        return false;
    }

    protected boolean isPreDestroy(final Method method) {
        Annotation[] annotations = (Annotation[]) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return method.getDeclaredAnnotations();
                    }
                }
        );
        for (Annotation annotation : annotations) {
            return PreDestroy.class.isAssignableFrom(annotation.annotationType());
        }
        return false;
    }
   
}
