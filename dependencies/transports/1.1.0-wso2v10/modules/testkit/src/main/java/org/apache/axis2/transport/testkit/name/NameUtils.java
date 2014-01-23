/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.axis2.transport.testkit.name;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.axis2.transport.testkit.Adapter;

public class NameUtils {
    public static Map<String,String> getNameComponents(Object object) {
        Map<String,String> result = new LinkedHashMap<String,String>();
        getNameComponents(result, object);
        return result;
    }
    
    private static <A extends Annotation> A getAnnotationInherited(Class<?> type, Class<A> annotationClass) {
        A ann = type.getAnnotation(annotationClass);
        if (ann != null) {
            return ann;
        }
        Class<?> superClass = type.getSuperclass();
        if (superClass != null) {
            ann = getAnnotationInherited(superClass, annotationClass);
            if (ann != null) {
                return ann;
            }
        }
        for (Class<?> iface : type.getInterfaces()) {
            ann = getAnnotationInherited(iface, annotationClass);
            if (ann != null) {
                return ann;
            }
        }
        return null;
    }
    
    public static void getNameComponents(Map<String,String> map, Object object) {
        while (object instanceof Adapter) {
            object = ((Adapter)object).getTarget();
        }
        Class<?> clazz = object.getClass();
        {
            Key key = getAnnotationInherited(clazz, Key.class);
            if (key != null) {
                Name name = clazz.getAnnotation(Name.class);
                if (name == null) {
                    String className = clazz.getName();
                    map.put(key.value(), className.substring(className.lastIndexOf('.') + 1));
                } else {
                    map.put(key.value(), name.value());
                }
            }
        }
        for (Method method : clazz.getMethods()) {
            Key key = method.getAnnotation(Key.class);
            Named named = method.getAnnotation(Named.class);
            if (key != null || named != null) {
                Object relatedObject;
                try {
                    method.setAccessible(true);
                    relatedObject = method.invoke(object);
                } catch (Throwable ex) {
                    throw new Error("Error invoking " + method, ex);
                }
                if (relatedObject != null) {
                    if (key != null) {
                        map.put(key.value(), relatedObject.toString());
                    } else {
                        getNameComponents(map, relatedObject);
                    }
                }
            }
        }
    }
}
