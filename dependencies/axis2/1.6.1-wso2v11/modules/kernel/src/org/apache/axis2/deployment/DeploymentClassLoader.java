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

package org.apache.axis2.deployment;

import org.apache.axiom.attachments.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DeploymentClassLoader extends URLClassLoader {
    // List of URL's
    private URL[] urls = null;

    // List of jar files inside the jars in the original url
    private List embedded_jars;

    private boolean isChildFirstClassLoading;

    /**
     * DeploymentClassLoader is extended from URLClassLoader. The constructor
     * does not override the super constructor, but takes in an addition list of
     * jar files inside /lib directory.
     *
     * @param urls   <code>URL</code>s
     * @param parent parent classloader <code>ClassLoader</code>
     */
    public DeploymentClassLoader(URL[] urls,
                                 List embedded_jars,
                                 ClassLoader parent,
                                 boolean isChildFirstClassLoading) {
        super(urls, parent);
        this.urls = urls;
        this.embedded_jars = embedded_jars;
        this.isChildFirstClassLoading = isChildFirstClassLoading;
    }

    /**
     * Finds and loads the class with the specified name from the URL search
     * path. Any URLs referring to JAR files are loaded and opened as needed
     * until the class is found.
     *
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        Class clazz;
        try {
            clazz = super.findClass(name);
        } catch (ClassNotFoundException e) {
            byte raw[] = null;
            try {
                String completeFileName = name;
                /**
                 * Replacing org.apache. -> org/apache/...
                 */
                completeFileName = completeFileName.replace('.', '/').concat(".class");
                raw = getBytes(completeFileName);
            } catch (Exception ex) {
                // Fall through
            }
            if (raw == null) {
                throw new ClassNotFoundException("Class Not found : " + name);
            }
            clazz = defineClass(name, raw, 0, raw.length);
        }
        return clazz;
    }


    /**
     * Finds the resource with the specified name on the URL search path.
     *
     * @param resource the name of the resource
     * @return a <code>URL</code> for the resource, or <code>null</code>
     * if the resource could not be found.
     */
    public URL findResource(String resource) {
        URL url = super.findResource(resource);
        if (url == null) {
            for (int i = 0; embedded_jars != null && i < embedded_jars.size(); i++) {
                String libjar_name = (String) embedded_jars.get(i);
                try {
                    InputStream in = getJarAsStream(libjar_name);
                    ZipInputStream zin = new ZipInputStream(in);
                    ZipEntry entry;
                    String entryName;
                    while ((entry = zin.getNextEntry()) != null) {
                        entryName = entry.getName();
                        if (entryName != null &&
                                entryName.endsWith(resource)) {
                            byte[] raw = IOUtils.getStreamAsByteArray(zin);
                            return new URL("jar", "", -1, urls[0] + "!/" + libjar_name + "!/" + entryName,
                                    new ByteUrlStreamHandler(raw));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return url;
    }

    /**
     * Returns an Enumeration of URLs representing all of the resources
     * on the URL search path having the specified name.
     *
     * @param resource the resource name
     * @exception IOException if an I/O exception occurs
     * @return an <code>Enumeration</code> of <code>URL</code>s
     */
    public Enumeration findResources(String resource) throws IOException {
        ArrayList resources = new ArrayList();
        Enumeration e = super.findResources(resource);
        while (e.hasMoreElements()) {
            resources.add(e.nextElement());
        }
        for (int i = 0; embedded_jars != null && i < embedded_jars.size(); i++) {
            String libjar_name = (String) embedded_jars.get(i);
            try {
            InputStream in = getJarAsStream(libjar_name);
            ZipInputStream zin = new ZipInputStream(in);
            ZipEntry entry;
            String entryName;
                while ((entry = zin.getNextEntry()) != null) {
                    entryName = entry.getName();
                    if (entryName != null &&
                            entryName.endsWith(resource)) {
                        byte[] raw = IOUtils.getStreamAsByteArray(zin);
                        resources.add(new URL("jar", "", -1, urls[0] + "!/" + libjar_name + "!/" + entryName,
                                new ByteUrlStreamHandler(raw)));
                    }
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return Collections.enumeration(resources);
    }

    /**
     * Access the jars file (/lib) one by one , then for each one create a <code>ZipInputStream</code>
     * and check to see whether there is any entry eith given name. if it is found then
     * return the byte array for that
     *
     * @param resource <code>String</code>  Name of the file to be found
     * @return byte[]
     * @throws java.io.IOException <code>Exception</code>
     */
    private byte[] getBytes(String resource) throws Exception {
        for (int i = 0; embedded_jars != null && i < embedded_jars.size(); i++) {
            String libjar_name = (String) embedded_jars.get(i);
            InputStream in = getJarAsStream(libjar_name);
            byte[] bytes = getBytes(in, resource);
            if(bytes != null) {
                return bytes;
            }
        }
        return null;
    }

    /**
     * Get a specific entry's content as a byte array
     * 
     * @param in
     * @param resource
     * @return
     * @throws Exception
     */
    private byte[] getBytes(InputStream in, String resource) throws Exception {
        ZipInputStream zin = new ZipInputStream(in);
        ZipEntry entry;
        String entryName;
        while ((entry = zin.getNextEntry()) != null) {
            entryName = entry.getName();
            if (entryName != null &&
                    entryName.endsWith(resource)) {
                byte[] raw = IOUtils.getStreamAsByteArray(zin);
                zin.close();
                return raw;
            }
        }
        return null;
    }

    /**
     * Get the specified embedded jar from the main jar 
     *
     * @param libjar_name
     * @return
     * @throws Exception
     */
    private InputStream getJarAsStream(String libjar_name) throws Exception {
        return new ByteArrayInputStream(getBytes(urls[0].openStream(), libjar_name));
    }

    public static class ByteUrlStreamHandler extends URLStreamHandler {
        private byte[] bytes;

        public ByteUrlStreamHandler(byte[] bytes) {
            this.bytes = bytes;
        }

        protected URLConnection openConnection(URL u) throws IOException {
            return new ByteURLConnection(u, bytes);
        }
    }

    public static class ByteURLConnection extends URLConnection {
        protected byte[] bytes;

        public ByteURLConnection(URL url, byte[] bytes) {
            super(url);
            this.bytes = bytes;
        }

        public void connect() {
        }

        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }
    }

    public InputStream getResourceAsStream(String name) {
        URL url = findResource(name);
        if(url == null) {
            url = getResource(name);
        }
        if(url!=null){
            try {
                return url.openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class c = null;
        if (!isChildFirstClassLoading) {
            c = super.loadClass(name, resolve);
        } else {
            c = findLoadedClass(name);
            if (c == null) {
                try {
                    c = findClass(name);
                } catch (Exception e) {
                    c = super.loadClass(name, resolve);
                }
            }
        }
        return c;
    }

    public boolean isChildFirstClassLoading() {
        return isChildFirstClassLoading;
    }

    public void setChildFirstClassLoading(boolean childFirstClassLoading) {
        isChildFirstClassLoading = childFirstClassLoading;
    }
}
