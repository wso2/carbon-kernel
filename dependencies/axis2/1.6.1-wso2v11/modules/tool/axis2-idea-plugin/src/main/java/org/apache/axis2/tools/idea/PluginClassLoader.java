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

package org.apache.axis2.tools.idea;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PluginClassLoader extends URLClassLoader {

    //urls which gives to create the classLoader
    private URL[] urls;

    //To keep jar files inside /lib directory in the main jar
    private ArrayList lib_jars_list;


    /**
     * PluginClassLoader is exetend form URLClassLoader , and the constructor
     * has not overide the super constroctor , but has done some stuff to find out
     * jar fils inside /lib director
     *
     * @param urls   <code>URL</code>
     * @param parent parent classloader <code>ClassLoader</code>
     */
    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.urls = urls;
        lib_jars_list = new ArrayList();
        findLibJars();
    }

    /**
     * This just search for jar files inside /lib dirctory and if there are any then those
     * will be added to the arraylit (only the name of the jar file)
     */
    private void findLibJars() {
        /**
         * though the URL array can contains one or more urls , I have only consider the
         * first one , that is this classLoader is only for Axis2 stuff and the classloader
         * is created by Deployment , so there wont be any chance to have more the one urls for
         * the URL array list
         */
        File file = new File(urls[0].getFile());
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;
            String entryName = "";
            while ((entry = zin.getNextEntry()) != null) {
                entryName = entry.getName();
                /**
                 * id the entry name start with /lib and end with .jar
                 * then those entry name will be added to the arraylist
                 */
                if (entryName != null && (entryName.startsWith("lib/") ||
                        entryName.startsWith("Lib/")) &&
                        entryName.endsWith(".jar")) {
                    lib_jars_list.add(entryName);
                }
            }
            zin.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param name <code>String</code>  Name of the file to be loaded
     * @return <code>Class</code> return a class object if it found else
     *         will return null or a ClassNotFoundException
     *         <p/>
     *         This method has been overridden in the following way
     *         1. It calls the super class and checks to see whether the class is there
     *         If the class is found then return it, else if super return ClassNotfoundException
     *         2. Check whether the entry corresponding to the class name exists in one of jar files
     *         in /lib director
     *         3. If it is there get the byte array out of that and create a Class object out of that
     *         by calling "defineClass()" , if it succeeds then return that else
     *         4. Throw a ClassNotFoundException
     * @throws ClassNotFoundException
     */
    protected Class findClass(final String name)
            throws ClassNotFoundException {
        Class cla = null;
        try {
            boolean foundClass = false;
            try {
                cla = super.findClass(name);
                foundClass = true;
                return cla;
            } catch (ClassNotFoundException e) {
                foundClass = false;
            }
            if (!foundClass) {
                byte raw[] = getBytes(name);
                cla = defineClass(name, raw, 0, raw.length);
                foundClass = true;
                return cla;
            }
            if (!foundClass) {
                throw new ClassNotFoundException("Class Not found : " + name);
            }

        } catch (Exception e) {

        }
        return null;
    }

    /**
     * Read jar file (/lib) one by one , then for each file craete <code>ZipInputStream</code>
     * that and check to see wether there is any entry eith given name if it found then
     * Creat ByteArrayOutPutStream and get the class bytes to that .
     * after goning throgh each and evry jar file if there is no entry with given name
     * will throug a ClassNotFound execption
     *
     * @param filename <code>String</code>  Name of the file to be loaded (Class Name)
     * @return bytt[]
     * @throws java.io.IOException <code>Exception</code>
     */
    private byte[] getBytes(String filename) throws Exception {
        String completeFileName = filename;
        /**
         * Replacing org.apache. -> org/apache/...
         */
        completeFileName = completeFileName.replace('.', '/').concat(".class");
        byte raw[] = null;
        for (int i = 0; i < lib_jars_list.size(); i++) {
            String libjar_name = (String) lib_jars_list.get(i);
            InputStream in = this.getResourceAsStream(libjar_name);
            try {
                ZipInputStream zin = new ZipInputStream(in);
                ZipEntry entry;
                String entryName = "";
                while ((entry = zin.getNextEntry()) != null) {
                    entryName = entry.getName();
                    if (entryName != null &&
                            entryName.endsWith(completeFileName)) {
                        byte data[] = new byte[2048];
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        int count;
                        while ((count = zin.read(data, 0, 2048)) != -1) {
                            out.write(data, 0, count);
                        }
                        raw = out.toByteArray();
                        out.close();
                        zin.close();
                        return raw;
                    }
                }
            } catch (IOException e) {
                throw e;
            }

        }
        throw new ClassNotFoundException("Class Not found : " + filename);
    }
}

