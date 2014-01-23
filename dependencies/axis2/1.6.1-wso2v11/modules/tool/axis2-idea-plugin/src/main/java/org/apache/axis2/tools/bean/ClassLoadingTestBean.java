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

package org.apache.axis2.tools.bean;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class ClassLoadingTestBean {

    public static boolean tryLoadingClass(String className,
                                          String[] classPathEntries, List errorListener) {

        //make a URL class loader from the entries
        ClassLoader classLoader;

        if (classPathEntries.length > 0) {
            URL[] urls = new URL[classPathEntries.length];

            try {
                for (int i = 0; i < classPathEntries.length; i++) {
                    String classPathEntry = classPathEntries[i];
                    //this should be a file(or a URL)
                    if (classPathEntry.startsWith("http://")) {
                        urls[i] = new URL(classPathEntry);
                    } else {
                        urls[i] = new File(classPathEntry).toURL();
                    }
                }
            } catch (MalformedURLException e) {
                if (errorListener!=null){
                    errorListener.add(e);
                }
                return false;
            }

            classLoader = new URLClassLoader(urls);

        } else {
            classLoader = Thread.currentThread().getContextClassLoader();
        }

        //try to load the class with the given name

        try {
            Class clazz=classLoader.loadClass(className);
            clazz.getMethods();


        } catch (Throwable t) {
            if (errorListener!=null){
                errorListener.add(t);
            }
            return false;
        }

        return true;

    }
}