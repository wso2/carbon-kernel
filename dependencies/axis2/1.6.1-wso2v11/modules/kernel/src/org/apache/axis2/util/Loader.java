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

package org.apache.axis2.util;

import org.apache.axis2.java.security.AccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.PrivilegedAction;

/**
 * Loads resources (or images) from various sources.
 */
public class Loader {
    private static final Log log = LogFactory.getLog(Loader.class);

    /**
     * Searches for <code>resource</code> in different
     * places. The search order is as follows:
     * <ol>
     * <p><li>Search for <code>resource</code> using the thread context
     * class loader under Java2. If that fails, search for
     * <code>resource</code> using the class loader that loaded this
     * class (<code>Loader</code>).
     * <p><li>Try one last time with
     * <code>ClassLoader.getSystemResource(resource)</code>, that is is
     * using the system class loader in JDK 1.2 and virtual machine's
     * built-in class loader in JDK 1.1.
     * </ol>
     * <p/>
     *
     * @param resource
     * @return Returns URL
     */
    static public URL getResource(String resource) {
        ClassLoader classLoader = null;
        URL url = null;
        try {
            // We could not find resource. Ler us now try with the
            // classloader that loaded this class.
            classLoader = getTCL();
            if (classLoader != null) {
                log.debug("Trying to find [" + resource + "] using " + classLoader +
                        " class loader.");
                url = classLoader.getResource(resource);
                if (url != null) {
                    return url;
                }
            }
        } catch (Throwable t) {
            log.warn("Caught Exception while in Loader.getResource. This may be innocuous.", t);
        }

        // Last ditch attempt: get the resource from the class path. It
        // may be the case that clazz was loaded by the Extentsion class
        // loader which the parent of the system class loader. Hence the
        // code below.
        log.debug("Trying to find [" + resource + "] using ClassLoader.getSystemResource().");
        return ClassLoader.getSystemResource(resource);
    }


    /**
     * Gets the resource with the specified class loader.
     *
     * @param loader
     * @param resource
     * @return Returns URL.
     * @throws ClassNotFoundException
     */
    static public URL getResource(ClassLoader loader, String resource)
            throws ClassNotFoundException {
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
     * Searches for <code>resource</code> in different
     * places. The search order is as follows:
     * <ol>
     * <p><li>Search for <code>resource</code> using the thread context
     * class loader under Java2. If that fails, search for
     * <code>resource</code> using the class loader that loaded this
     * class (<code>Loader</code>).
     * <p><li>Try one last time with
     * <code>ClassLoader.getSystemResourceAsStream(resource)</code>, that is is
     * using the system class loader in JDK 1.2 and virtual machine's
     * built-in class loader in JDK 1.1.
     * </ol>
     * <p/>
     *
     * @param resource
     * @return Returns URL
     */
    static public InputStream getResourceAsStream(String resource) {
        ClassLoader classLoader = null;
        try {
            // Let's try the Thread Context Class Loader
            classLoader = getTCL();
            if (classLoader != null) {
                log.debug("Trying to find [" + resource + "] using " + classLoader +
                        " class loader.");
                InputStream is = classLoader.getResourceAsStream(resource);
                if (is != null) {
                    return is;
                }
            }
        } catch (Throwable t) {
            log.warn("Caught Exception while in Loader.getResourceAsStream. This may be innocuous.", t);
        }

        try {
            // We could not find resource. Ler us now try with the
            // classloader that loaded this class.
            classLoader = Loader.class.getClassLoader();
            if (classLoader != null) {
                log.debug("Trying to find [" + resource + "] using " + classLoader +
                        " class loader.");
                InputStream is = classLoader.getResourceAsStream(resource);
                if (is != null) {
                    return is;
                }
            }
        } catch (Throwable t) {
            log.warn("Caught Exception while in Loader.getResourceAsStream. This may be innocuous.", t);
        }

        // Last ditch attempt: get the resource from the class path. It
        // may be the case that clazz was loaded by the Extentsion class
        // loader which the parent of the system class loader. Hence the
        // code below.
        log.debug("Trying to find [" + resource + "] using ClassLoader.getSystemResourceAsStream().");
        return ClassLoader.getSystemResourceAsStream(resource);
    }


    /**
     * Gets the resource with the specified class loader.
     *
     * @param loader
     * @param resource
     * @return Returns URL.
     * @throws ClassNotFoundException
     */
    static public InputStream getResourceAsStream(ClassLoader loader, String resource)
            throws ClassNotFoundException {
        try {
            if (loader != null) {
                log.debug("Trying to find [" + resource + "] using " + loader + " class loader.");
                InputStream is = loader.getResourceAsStream(resource);
                if (is != null) {
                    return is;
                }
            }
        } catch (Throwable t) {
            log.warn("Caught Exception while in Loader.getResource. This may be innocuous.", t);
        }
        return getResourceAsStream(resource);
    }

    /**
     * Gets the thread context class loader.
     *
     * @return Returns ClassLoader.
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    static private ClassLoader getTCL() throws IllegalAccessException, InvocationTargetException {
        return (ClassLoader) AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
    }

    /**
     * Loads the specified classloader and then falls back to the loadClass.
     *
     * @param loader
     * @param clazz
     * @return Returns Class.
     * @throws ClassNotFoundException
     */
    static public Class loadClass(ClassLoader loader, String clazz) throws ClassNotFoundException {
        try {
            if (loader != null) {
                Class c = loader.loadClass(clazz);
                if (c != null) {
                    return c;
                }
            }
        } catch (UnsupportedClassVersionError e) {
            log.debug(e);
            throw e;
        } catch (Throwable e) {
            log.debug(e);
        }
        return loadClass(clazz);
    }

    /**
     * If running under JDK 1.2, loads the specified class using the
     * <code>Thread</code> <code>contextClassLoader</code> . If that
     * fails, try Class.forname.
     * <p/>
     *
     * @param clazz
     * @return Returns Class.
     * @throws ClassNotFoundException
     */
    static public Class loadClass(String clazz) throws ClassNotFoundException {
        try {
            ClassLoader tcl = getTCL();

            if (tcl != null) {
                Class c = tcl.loadClass(clazz);
                if (c != null) {
                    return c;
                }
            }
        } catch (UnsupportedClassVersionError e) {
            log.debug(e);
            throw e;
        } catch (Throwable e) {
            log.debug(e);
        }
        // we reached here because tcl was null or because of a
        // security exception, or because clazz could not be loaded...
        // In any case we now try one more time
        return Class.forName(clazz);
    }
}
