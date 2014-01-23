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

package org.apache.axis2.jaxws.message.databinding.impl;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.databinding.ClassFinder;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class ClassFinderImpl implements ClassFinder {
    private static final Log log = LogFactory.getLog(ClassFinderImpl.class);

    /* (non-Javadoc)
      * @see org.apache.axis2.jaxws.message.databinding.ClassFinder#getClassesFromJarFile(java.lang.String, java.lang.ClassLoader)
      */
    public ArrayList<Class> getClassesFromJarFile(String pkg, ClassLoader cl)
            throws ClassNotFoundException {
        try {
            ArrayList<Class> classes = new ArrayList<Class>();
            URLClassLoader ucl = (URLClassLoader)cl;
            URL[] srcURL = ucl.getURLs();
            String path = pkg.replace('.', '/');
            //Read resources as URL from class loader.
            for (URL url : srcURL) {
                if ("file".equals(url.getProtocol())) {
                    File f = new File(url.toURI().getPath()); 
                    //If file is not of type directory then its a jar file
                    if (f.exists() && !f.isDirectory()) {
                        try {
                            JarFile jf = new JarFile(f);
                            Enumeration<JarEntry> entries = jf.entries();
                            //read all entries in jar file
                            while (entries.hasMoreElements()) {
                                JarEntry je = entries.nextElement();
                                String clazzName = je.getName();
                                if (clazzName != null && clazzName.endsWith(".class")) {
                                    //Add to class list here.
                                    clazzName = clazzName.substring(0, clazzName.length() - 6);
                                    clazzName = clazzName.replace('/', '.').replace('\\', '.')
                                            .replace(':', '.');
                                    //We are only going to add the class that belong to the provided package.
                                    if (clazzName.startsWith(pkg + ".")) {
                                        try {
                                            Class clazz = forName(clazzName, false, cl);
                                            // Don't add any interfaces or JAXWS specific classes.
                                            // Only classes that represent data and can be marshalled
                                            // by JAXB should be added.
                                            if (!clazz.isInterface() &&
                                                    clazz.getPackage().getName().equals(pkg)
                                                    && ClassUtils
                                                    .getDefaultPublicConstructor(clazz) != null
                                                    && !ClassUtils.isJAXWSClass(clazz)) {
                                                if (log.isDebugEnabled()) {
                                                    log.debug("Adding class: " + clazzName);
                                                }
                                                classes.add(clazz);

                                            }
                                            //catch Throwable as ClassLoader can throw an NoClassDefFoundError that
                                            //does not extend Exception, so lets catch everything that extends Throwable
                                            //rather than just Exception.
                                        } catch (Throwable e) {
                                            if (log.isDebugEnabled()) {
                                                log.debug("Tried to load class " + clazzName +
                                                        " while constructing a JAXBContext.  This class will be skipped.  Processing Continues.");
                                                log.debug(
                                                        "  The reason that class could not be loaded:" +
                                                                e.toString());
                                                log.trace(JavaUtils.stackToString(e));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            throw new ClassNotFoundException(Messages.getMessage("ClassUtilsErr4"));
                        }
                    }
                }
            }
            return classes;
        } catch (Exception e) {
            throw new ClassNotFoundException(e.getMessage());
        }

    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize,
                                 final ClassLoader classloader) {
        // NOTE: This method must remain private because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className, initialize, classloader);
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }

    /** @return ClassLoader */
    private static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw ExceptionFactory.makeWebServiceException(e.getException());
        }

        return cl;
    }

    /* (non-Javadoc)
     * @see org.apache.axis2.jaxws.message.databinding.ClassFinder#updateClassPath(java.lang.String, java.lang.ClassLoader)
     */
    public void updateClassPath(final String filePath, final ClassLoader cl) throws Exception{
        if(filePath == null){
            return;
        }
        if(filePath.length()==0){
            return;
        }
        if(cl instanceof URLClassLoader){
            //lets add the path to the classloader.
            try{
                AccessController.doPrivileged(
                    new PrivilegedExceptionAction()  {
                        public Object run() throws Exception{
                            URLClassLoader ucl = (URLClassLoader)cl;
                            //convert file path to URL.
                            File file = new File(filePath);
                            URL url = file.toURI().toURL();
                            Class uclClass = URLClassLoader.class;
                            Method method = uclClass.getDeclaredMethod("addURL", new Class[]{URL.class});
                            method.setAccessible(true);
                            method.invoke(ucl, new Object[]{url});
                            return ucl;
                        }
                    }
                );
            } catch (PrivilegedActionException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception thrown from AccessController: " + e);
                }
                throw ExceptionFactory.makeWebServiceException(e.getException());
            }

        }
    }
}
