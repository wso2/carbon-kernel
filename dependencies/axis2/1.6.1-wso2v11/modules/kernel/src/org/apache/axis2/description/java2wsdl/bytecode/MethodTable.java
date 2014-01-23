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

import java.lang.reflect.Method;
import java.util.HashMap;

public class MethodTable {

    private HashMap nameToMethodMap;
    private ChainedParamReader cpr;

    public MethodTable(Class cls) throws Exception {
        cpr = new ChainedParamReader(cls);
        nameToMethodMap = new HashMap();
        loadMethods(cls);
    }

    /**
     * To load all the methods in the given class by Java reflection
     *
     * @param cls
     * @throws Exception
     */
    private void loadMethods(Class cls) throws Exception {
        Method [] methods = cls.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.isBridge()) {
                continue;
            }
            nameToMethodMap.put(method.getName(), method);
        }
    }

    public String [] getParameterNames(String methodName) {
        Method method = (Method) nameToMethodMap.get(methodName);
        if (method == null) {
            return null;
        }
        return cpr.getParameterNames(method);
    }


}
