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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Code generation data binding extension for JiBX support. JiBX currently requires a predefined
 * binding definition to be supplied in order to be used with Axis2.
 */
public class JiBXExtension extends AbstractDBProcessingExtension {

    /** Name of "extra" option used to supply binding definition path. */
    public static final String BINDING_PATH_OPTION = "bindingfile";
    private static final String JIBX_MODEL_CLASS =
            "org.jibx.binding.model.BindingElement";
    private static final String JIBX_UTILITY_CLASS =
            "org.apache.axis2.jibx.CodeGenerationUtility";
    private static final String JIBX_UTILITY_METHOD = "engage";

    public void engage(CodeGenConfiguration configuration) {

        // just return if JiBX binding not active
        if (testFallThrough(configuration.getDatabindingType())) {
            return;
        }

        // check the JiBX binding definition file specified
        String path = (String)configuration.getProperties().get(BINDING_PATH_OPTION);
        try {

            // try dummy load of framework class first to check missing jars
            try {
                getClass().getClassLoader().loadClass(JIBX_MODEL_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("JiBX framework jars not in classpath");
            }

            // load the actual utility class
            Class clazz;
            try {
                clazz = JiBXExtension.class.getClassLoader().loadClass(JIBX_UTILITY_CLASS);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("JiBX binding extension not in classpath");
            }

            // create an instance of the class
            Object inst = null;
            Constructor constructor =
                    clazz.getConstructor(new Class[] { CodeGenConfiguration.class });
            inst = constructor.newInstance(new Object[] { configuration });

            // invoke utility class method for actual processing
            Method method = clazz.getMethod(JIBX_UTILITY_METHOD,
                                            new Class[] { String.class });
            method.invoke(inst, new Object[] { path });

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else if (e instanceof InvocationTargetException) {
                if (e.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)e.getCause();
                } else {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException(e);
            }
        }

    }
}