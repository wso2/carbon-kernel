/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.osgi.deployment;

import org.osgi.framework.Bundle;

import java.net.URL;
import java.util.Enumeration;
import java.io.IOException;

/**
 * This classloader will be used with AxisService, AxisModule etc
 */
public class BundleClassLoader extends ClassLoader {

    private final Bundle bundle;

    public BundleClassLoader(Bundle bundle, ClassLoader parent) {
        super(parent);
        this.bundle = bundle;
    }

    protected Class findClass(String name) throws ClassNotFoundException {
        try {
            return bundle.loadClass(name);
        } catch (ClassNotFoundException e) {
            //TODO: add the proper logging
            e.printStackTrace();
            throw e;
        }
    }

    public URL findResource(String name) {
        return bundle.getResource(name);
    }

    public Enumeration findResources(String name) throws IOException {
        return bundle.getResources(name);
    }

    public URL getResource(String name) {
        return findResource(name);
    }

    protected Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz;
        try {
            clazz = findClass(name);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
        if (resolve) {
            resolveClass(clazz);
        }
        return clazz;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        Class clazz;
        try {
            clazz = findClass(name);
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
        return clazz;
    }
}

