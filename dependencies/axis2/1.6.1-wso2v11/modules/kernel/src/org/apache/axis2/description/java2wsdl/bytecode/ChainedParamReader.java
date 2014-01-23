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

package org.apache.axis2.description.java2wsdl.bytecode;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description: In ParamReader class, user cannot get inherited method parameter
 * from the class they passed in for performance reasons This class
 * is walks up the inheritance chain. If the method is not found in
 * the derived class, search in super class. If not found in the immedidate super
 * class, search super class's super class, until the root, which is java.lang.Object,
 * is reached. This is not an eager load since it only start searching the super class
 * when it is asked to.
 */
public class ChainedParamReader {
    private List chain = new ArrayList();
    private List clsChain = new ArrayList();
    private Map methodToParamMap = new HashMap();

    /**
     * Processes a given class's parameter names.
     *
     * @param cls the class which user wants to get parameter info from
     * @throws IOException
     */
    public ChainedParamReader(Class cls) throws IOException {
        ParamReader reader = new ParamReader(cls);
        chain.add(reader);
        clsChain.add(cls);
    }

    //now I need to create deligate methods

    /**
     * Returns the names of the declared parameters for the given constructor.
     * If we cannot determine the names, return null.  The returned array will
     * have one name per parameter.  The length of the array will be the same
     * as the length of the Class[] array returned by Constructor.getParameterTypes().
     *
     * @param ctor
     * @return Returns array of names, one per parameter, or null
     */
    public String[] getParameterNames(Constructor ctor) {
        //there is no need for the constructor chaining.
        return ((ParamReader) chain.get(0)).getParameterNames(ctor);
    }

    /**
     * Returns the names of the declared parameters for the given method.
     * If cannot determine the names in the current class, search its parent 
     * class until we reach java.lang.Object. If still can not find the method,
     * returns null. The returned array has one name per parameter. The length 
     * of the array will be the same as the length of the Class[] array 
     * returned by Method.getParameterTypes().
     *
     * @param method
     * @return String[] Returns array of names, one per parameter, or null
     */
    public String[] getParameterNames(Method method) {
        //go find the one from the cache first
        if (methodToParamMap.containsKey(method)) {
            return (String[]) methodToParamMap.get(method);
        }

        String[] ret = null;
        for (Iterator it = chain.iterator(); it.hasNext();) {
            ParamReader reader = (ParamReader) it.next();
            ret = reader.getParameterNames(method);
            if (ret != null) {
                methodToParamMap.put(method, ret);
                return ret;
            }
        }
        //if we here, it means we need to create new chain.
        Class cls = (Class) clsChain.get(chain.size() - 1);
        while (cls != null && cls != java.lang.Object.class && cls.getSuperclass() != null) {
            Class superClass = cls.getSuperclass();
            try {
                ParamReader _reader = new ParamReader(superClass);
                chain.add(_reader);
                clsChain.add(cls);
                ret = _reader.getParameterNames(method);
                if (ret != null) { //we found it so just return it.
                    methodToParamMap.put(method, ret);
                    return ret;
                }
            } catch (IOException e) {
                //can not find the super class in the class path, abort here
                return null;
            }
            cls = superClass;
        }
        methodToParamMap.put(method, ret);
        return null;
    }
}
