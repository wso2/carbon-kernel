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

package org.apache.axis2.jsr181;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Helper class to work with JSR 181 annotations. This class should be used to retrieve JSR 181
 * annotations. It avoids the direct dependency on the API, i.e. it avoids
 * {@link NoClassDefFoundError} exceptions if the JAR is not in the classpath. This is useful
 * because these annotations are completely optional in Axis2.
 */
public abstract class JSR181Helper {
    /**
     * The instance of this helper class. If the JSR 181 API is not available in the classpath, this
     * will be an implementation that always returns <code>null</code>.
     */
    public static final JSR181Helper INSTANCE;
    
    static {
        boolean jsr181present;
        try {
            Class.forName("javax.jws.WebService");
            jsr181present = true;
        } catch (ClassNotFoundException ex) {
            jsr181present = false;
        }
        INSTANCE = jsr181present ? new JSR181HelperImpl() : new NullJSR181Helper();
    }

    /**
     * Get the WebService annotation for a given class.
     * 
     * @param clazz
     *            the class
     * @return the WebService annotation, or <code>null</code> if there is no such annotation
     */
    public abstract WebServiceAnnotation getWebServiceAnnotation(Class<?> clazz);

    /**
     * Get the WebMethod annotation for a given method.
     * 
     * @param method
     *            the method
     * @return the WebMethod annotation, or <code>null</code> if there is no such annotation
     */
    public abstract WebMethodAnnotation getWebMethodAnnotation(Method method);
    
    /**
     * Get the WebParam annotation from a set of annotations. This method is typically used in
     * conjunction with {@link Method#getParameterAnnotations()}.
     * 
     * @param annotations
     *            an array of annotations
     * @return the WebParam annotation, or <code>null</code> if the array didn't contain a WebParam
     *         annotation
     */
    public abstract WebParamAnnotation getWebParamAnnotation(Annotation[] annotations);
    
    /**
     * Get the WebResult annotation for a given method.
     * 
     * @param method
     *            the method
     * @return the WebResult annotation, or <code>null</code> if there is no such annotation
     */
    public abstract WebResultAnnotation getWebResultAnnotation(Method method);
}
