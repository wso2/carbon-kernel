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

package org.apache.axis2.jaxws.utility;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;
import javax.xml.ws.WebFault;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceProvider;

import org.apache.axis2.java.security.AccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** Contains static Class utility methods related to method parameter/argument marshalling. */
public class ClassUtils {

    private static Log log = LogFactory.getLog(ClassUtils.class);


    /**
     * Gets the RootCause for an throwable. The root cause is defined as the first
     * non-InvocationTargetException.
     *
     * @param e Throwable
     * @return Throwable root cause
     */
    public static Throwable getRootCause(Throwable e) {
        Throwable t = null;

        if (e != null) {
            if (e instanceof InvocationTargetException) {
                t = ((InvocationTargetException)e).getTargetException();
            } else {
                t = null;
            }

            if (t != null) {
                e = getRootCause(t);
            }
        }
        return e;
    }

    private static HashMap loadClassMap = new HashMap();

    static {
        loadClassMap.put("byte", byte.class);
        loadClassMap.put("int", int.class);
        loadClassMap.put("short", short.class);
        loadClassMap.put("long", long.class);
        loadClassMap.put("float", float.class);
        loadClassMap.put("double", double.class);
        loadClassMap.put("boolean", boolean.class);
        loadClassMap.put("char", char.class);
        loadClassMap.put("void", void.class);
    }

    /** Converts text of the form Foo[] to the proper class name for loading [LFoo */
    private static HashMap loadableMap = new HashMap();

    static {
        loadableMap.put("byte", "B");
        loadableMap.put("char", "C");
        loadableMap.put("double", "D");
        loadableMap.put("float", "F");
        loadableMap.put("int", "I");
        loadableMap.put("long", "J");
        loadableMap.put("short", "S");
        loadableMap.put("boolean", "Z");
    }

    /**
     * @param text String
     * @return String that can be used for Class.forName
     */
    public static String getLoadableClassName(String text) {
        int bracket = text.indexOf("[");
        String className = text;

        // Get the className without any array brackets
        if (bracket > 0) {
            className = className.substring(0, bracket);
        }

        // Now get the loadable name from the map or 
        // its L<className>;
        String loadClass = (String)loadableMap.get(className);
        if (loadClass == null) {
            loadClass = "L" + className + ";";
        }

        // Now prepend [ for each array dimension
        if (bracket > 0) {
            int i = text.indexOf("]");
            while (i > 0) {
                loadClass = "[" + loadClass;
                i = text.indexOf("]", i + 1);
            }
        }
        return loadClass;
    }

    /** Converts text of the form [LFoo to the Foo[] */
    public static String getTextClassName(String text) {
        if (text == null ||
                text.indexOf("[") != 0)
            return text;
        String className = "";
        int index = 0;
        while (index < text.length() &&
                text.charAt(index) == '[') {
            index ++;
            className += "[]";
        }
        if (index < text.length()) {
            if (text.charAt(index) == 'B')
                className = "byte" + className;
            else if (text.charAt(index) == 'C')
                className = "char" + className;
            else if (text.charAt(index) == 'D')
                className = "double" + className;
            else if (text.charAt(index) == 'F')
                className = "float" + className;
            else if (text.charAt(index) == 'I')
                className = "int" + className;
            else if (text.charAt(index) == 'J')
                className = "long" + className;
            else if (text.charAt(index) == 'S')
                className = "short" + className;
            else if (text.charAt(index) == 'Z')
                className = "boolean" + className;
            else if (text.equals("void"))
                className = "void";
            else {
                className = text.substring(index + 1, text.indexOf(";")) + className;
            }
        }
        return className;
    }

    /**
     * @param primitive
     * @return java wrapper class or null
     */
    public static Class getWrapperClass(Class primitive) {
        if (primitive == int.class)
            return java.lang.Integer.class;
        else if (primitive == short.class)
            return java.lang.Short.class;
        else if (primitive == boolean.class)
            return java.lang.Boolean.class;
        else if (primitive == byte.class)
            return java.lang.Byte.class;
        else if (primitive == long.class)
            return java.lang.Long.class;
        else if (primitive == double.class)
            return java.lang.Double.class;
        else if (primitive == float.class)
            return java.lang.Float.class;
        else if (primitive == char.class)
            return java.lang.Character.class;

        return null;
    }


    /**
     * @param wrapper
     * @return primitive clas or null
     */
    public static Class getPrimitiveClass(Class wrapper) {
        if (wrapper == java.lang.Integer.class)
            return int.class;
        else if (wrapper == java.lang.Short.class)
            return short.class;
        else if (wrapper == java.lang.Boolean.class)
            return boolean.class;
        else if (wrapper == java.lang.Byte.class)
            return byte.class;
        else if (wrapper == java.lang.Long.class)
            return long.class;
        else if (wrapper == java.lang.Double.class)
            return double.class;
        else if (wrapper == java.lang.Float.class)
            return float.class;
        else if (wrapper == java.lang.Character.class)
            return char.class;

        return null;
    }

    private static final Class[] noClass = new Class[] { };

    /**
     * Get the default public constructor
     *
     * @param clazz
     * @return Constructor or null
     */
    public static Constructor getDefaultPublicConstructor(Class clazz) {
        try {
            return clazz.getConstructor(noClass);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param name of primitive type
     * @return primitive Class or null
     */
    public static Class getPrimitiveClass(String text) {
        return (Class)loadClassMap.get(text);
    }

    /**
     * @param cls
     * @return true if this is a JAX-WS or JAX-WS generated class
     */
    public static final boolean isJAXWSClass(Class cls) {
        // TODO Processing all of these annotations is very expensive.  We need to cache the 
        // result in a static WeakHashMap<Class, Boolean>

        // Kinds of generated classes: Service, Provider, Impl, Exception, Holder
        // Or the class is in the jaxws.xml.ws package

        // Check for Impl
        WebService wsAnn = (WebService)getAnnotation(cls,WebService.class);
        if (wsAnn != null) {
            return true;
        }

        // Check for service
        WebServiceClient wscAnn = (WebServiceClient)getAnnotation(cls,WebServiceClient.class);
        if (wscAnn != null) {
            return true;
        }

        // Check for provider
        WebServiceProvider wspAnn = (WebServiceProvider)
            getAnnotation(cls,WebServiceProvider.class);
        if (wspAnn != null) {
            return true;
        }

        // Check for Exception
        WebFault wfAnn = (WebFault)getAnnotation(cls,WebFault.class);
        if (wfAnn != null) {
            return true;
        }

        // Check for Holder
        if (Holder.class.isAssignableFrom(cls)) {
            return true;
        }

        // Check for a javax.xml.ws.Service class instance
        if (Service.class.isAssignableFrom(cls)) {
            return true;
        }

        String className = cls.getPackage() == null ? null : cls.getPackage().getName();
        if (className != null && className.startsWith("javax.xml.ws") && !className.startsWith("javax.xml.ws.wsaddressing")) {
            return true;
        }
        
        return false;
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
    
    /**
     * 
     */
    public static Set<Class> getClasses(Type type, Set<Class> list) {
    	if (list == null) {
    		list = new HashSet<Class>();
    	}
    	try {
    		if (type instanceof Class) {
    			list.add( (Class)type);
    		}
    		if (type instanceof ParameterizedType) {
    			ParameterizedType pt = (ParameterizedType) type;
    			getClasses(pt.getRawType(), list);
    			Type types[] = pt.getActualTypeArguments();
    			if (types != null) {
    				for (int i=0; i<types.length; i++) {
    					getClasses(types[i], list);
    				}
    			}
    		} 
    		if (type instanceof GenericArrayType) {
    			GenericArrayType gat = (GenericArrayType) type;
    			getClasses(gat.getGenericComponentType(), list);
    		}
    	} catch (Throwable t) {
    		if (log.isDebugEnabled()) {
    			log.debug("Problem occurred in getClasses. Processing continues " + t);
    		}
    	}
    	return list;
    }
    
}

