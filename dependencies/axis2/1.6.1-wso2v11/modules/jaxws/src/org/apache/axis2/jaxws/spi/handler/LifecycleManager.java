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

package org.apache.axis2.jaxws.spi.handler;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.lifecycle.BaseLifecycleManager;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

public class LifecycleManager extends BaseLifecycleManager {

    public LifecycleManager(Object instance) {
        this.instance = instance;
    }

    protected Method getPostConstructMethod() {
        if (instance != null) {
            List<Method> methods = getMethods(instance.getClass());
            for (Method method : methods) {
                if (getAnnotation(method,PostConstruct.class) != null) {
                    return method;
                }
            }
        }
        return null;
    }

    protected Method getPreDestroyMethod() {
        if (instance != null) {
            List<Method> methods = getMethods(instance.getClass());
            for (Method method : methods) {
                if (getAnnotation(method,PreDestroy.class) != null) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Gets all of the methods in this class and the super classes
     *
     * @param beanClass
     * @return
     */
    private List<Method> getMethods(final Class beanClass) {
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
    
    /**
     * Get an annotation.  This is wrappered to avoid a Java2Security violation.
     * @param cls Class that contains annotation 
     * @param annotation Class of requrested Annotation
     * @return annotation or null
     */
    private static Annotation getAnnotation(final AnnotatedElement element, final Class annotation) {
        return (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return element.getAnnotation(annotation);
            }
        });
    }
}
