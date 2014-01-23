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

package org.apache.axis2.jaxws.server.endpoint.injection;

import org.apache.axis2.jaxws.injection.ResourceInjectionException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/*
 * Resource Injection is responsible to Injecting Resource Object to endpoint Instance at runtime.
 */

public interface ResourceInjector {

    /**
     * Inject resource on Filed, Method or Class depending on how the @Resource annotation is defined.
     *
     * @param resource
     * @param instance
     */
    abstract void inject(Object resource, Object instance) throws ResourceInjectionException;

    /**
     * Resource will be injected on the field.
     *
     * @param resource
     * @param instance
     * @param field
     * @return
     */
    abstract void injectOnField(Object resource, Object instance, Field field)
            throws ResourceInjectionException;

    /**
     * Resource will be injected on the Method.
     *
     * @param resource
     * @param instance
     * @param method
     * @return
     */
    abstract void injectOnMethod(Object resource, Object instance, Method method)
            throws ResourceInjectionException;

    /**
     * Resource will be injection on the class.
     * @param resource
     * @param instance
     * @param clazz
     * @return
     */
    abstract void injectOnClass(Object resource, Object instance, Class clazz)
            throws ResourceInjectionException;
}
