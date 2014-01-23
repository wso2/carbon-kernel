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

package org.apache.axis2.jaxws.message.databinding;

import java.util.ArrayList;
/*
 * ClassFinder will be used to find classes within a given package. The given 
 * package can be found in a Directory or in a jar file. ClassFinder will use the 
 * ClassLoader to get the classes from the jar files. 
 */

public interface ClassFinder {


    /**
     * This method reads all the classes from given package in a jar file. It uses ClassLoader to find
     * the given package in a jar file that is located in classpath.
     *
     * @param pkg
     * @param cl
     * @return
     */
    ArrayList<Class> getClassesFromJarFile(String pkg, ClassLoader cl)
            throws ClassNotFoundException;
    
    /**
     * This method will be used to add addition paths to existing classpath.
     * We may need to add classpath to search for jax-ws wrapper classes that
     * applicaiton developer did not package. 
     * @param filePath: path of the location where wrapper classes may be stored.
     * example a cache folder.
     * @param cl
     */
    public void updateClassPath(String filePath, ClassLoader cl) throws Exception;
}
