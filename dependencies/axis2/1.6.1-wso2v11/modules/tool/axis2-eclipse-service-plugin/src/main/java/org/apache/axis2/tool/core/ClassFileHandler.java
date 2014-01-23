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

package org.apache.axis2.tool.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.ArrayList;
import java.security.PrivilegedAction;

public class ClassFileHandler {


   
/**
 * 
 * @param classFileName
 * @param location
 * @return
 * @throws IOException
 * @throws ClassNotFoundException
 */
    public ArrayList getMethodNamesFromClass(String classFileName,String location) throws IOException, ClassNotFoundException{
        ArrayList returnList = new ArrayList();
        File fileEndpoint = new File(location);
        if (!fileEndpoint.exists()){
            throw new IOException("the location is invalid");
        }
        final URL[] urlList = {fileEndpoint.toURL()};
        URLClassLoader clazzLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
			public URLClassLoader run() {
				return new URLClassLoader(urlList);
			}
		});
        Class clazz = clazzLoader.loadClass(classFileName);
        Method[] methods = clazz.getDeclaredMethods();

        for (int i = 0; i < methods.length; i++) {
            returnList.add(methods[i].getName());

        }
        return returnList;
    }

}
