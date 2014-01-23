/**
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

package org.apache.ws.security.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Load resources (or images) from various sources.
 * <p/>
 *
 * @author Davanum Srinivas (dims@yahoo.com).
 */
public class Loader {
    private static Log log = LogFactory.getLog(Loader.class.getName());

    /**
     * This method will search for <code>resource</code> in different
     * places. The search order is as follows:
     * <ol>
     * <p><li>Search for <code>resource</code> using the thread context
     * class loader under Java2.
     * <p><li>Try one last time with
     * <code>ClassLoader.getSystemResource(resource)</code>, that is is
     * using the system class loader in JDK 1.2 and virtual machine's
     * built-in class loader in JDK 1.1.
     * </ol>
     * <p/>
     *
     * @param resource
     * @return TODO
     */
    public static URL getResource(String resource) {
        ClassLoader classLoader = null;
        URL url = null;
        try {
            classLoader = getTCL();
            if (classLoader != null) {
                log.debug("Trying to find [" + resource + "] using " + classLoader + " class loader.");
                url = classLoader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }
        } catch (Throwable t) {
            log.warn("Caught Exception while in Loader.getResource. This may be innocuous.", t);
        }
    
        // Last ditch attempt: get the resource from the class path. It
        // may be the case that clazz was loaded by the Extension class
        // loader which the parent of the system class loader. Hence the
        // code below.
        log.debug("Trying to find [" + resource + "] using ClassLoader.getSystemResource().");
        return ClassLoader.getSystemResource(resource);
    }
    

    /**
     * This method will search for <code>resource</code> in different
     * places. The search order is as follows:
     * <ol>
     * <p><li>Search for <code>resource</code> using the supplied class loader. 
     * If that fails, search for <code>resource</code> using the thread context
     * class loader.
     * <p><li>Try one last time with
     * <code>ClassLoader.getSystemResource(resource)</code>, that is is
     * using the system class loader in JDK 1.2 and virtual machine's
     * built-in class loader in JDK 1.1.
     * </ol>
     * <p/>
     *
     * @param resource
     * @return TODO
     */
    public static URL getResource(ClassLoader loader, String resource) {
        URL url = null;
        try {
            if (loader != null) {
                log.debug("Trying to find [" + resource + "] using " + loader + " class loader.");
                url = loader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }
        } catch (Throwable t) {
            log.warn("Caught Exception while in Loader.getResource. This may be innocuous.", t);
        }
        return getResource(resource);
    }

    /**
     * Get the Thread context class loader.
     * <p/>
     *
     * @return the Thread context class loader
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static ClassLoader getTCL() throws IllegalAccessException, InvocationTargetException {
         return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return Thread.currentThread().getContextClassLoader();
            }
         });
    }
    
    /**
     * Get the class loader of the class argument
     * <p/>
     *
     * @return the class loader of the argument
     */
    public static ClassLoader getClassLoader(final Class clazz) {
        return (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return clazz.getClassLoader();
            }
         });
    }

    /**
     * Try the specified classloader and then fall back to the loadClass
     * <p/>
     *
     * @param loader
     * @param clazz
     * @return Class
     * @throws ClassNotFoundException
     */
    public static Class loadClass(ClassLoader loader, String clazz) throws ClassNotFoundException {
        try {
            if (loader != null) {
                Class c = loader.loadClass(clazz);
                if (c != null) {
                    return c;
                }
            }
        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        return loadClass(clazz, true);
    }

    /**
     * If running under JDK 1.2 load the specified class using the
     * <code>Thread</code> <code>contextClassLoader</code> if that
     * fails try Class.forname.
     * <p/>
     *
     * @param clazz
     * @return TODO
     * @throws ClassNotFoundException
     */
    public static Class loadClass(String clazz) throws ClassNotFoundException {
        return loadClass(clazz, true);
    }
    
    public static Class loadClass(String clazz, boolean warn) throws ClassNotFoundException {
        try {
            ClassLoader tcl = getTCL(); 
            
            if (tcl != null) {
                Class c = tcl.loadClass(clazz);
                if (c != null) {
                    return c;
                }
            }
        } catch (Throwable e) {
            if (warn) {
                log.warn(e.getMessage(), e);
            } else {
                log.debug(e.getMessage(), e);
            }
        }
        // we reached here because tcl was null or because of a
        // security exception, or because clazz could not be loaded...
        // In any case we now try one more time
        return Class.forName(clazz);
    }
}
