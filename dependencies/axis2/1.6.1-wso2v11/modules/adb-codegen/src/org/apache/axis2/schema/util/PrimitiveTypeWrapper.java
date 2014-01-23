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

package org.apache.axis2.schema.util;

import java.util.HashMap;
import java.util.Map;


public class PrimitiveTypeWrapper {

    private static Map primitiveTypeWrappersMap;

    static{
        primitiveTypeWrappersMap =  new HashMap();
        //add the java primitive class names
        primitiveTypeWrappersMap.put(int.class.getName(),Integer.class.getName());
        primitiveTypeWrappersMap.put(long.class.getName(),Long.class.getName());
        primitiveTypeWrappersMap.put(byte.class.getName(),Byte.class.getName());
        primitiveTypeWrappersMap.put(double.class.getName(),Double.class.getName());
        primitiveTypeWrappersMap.put(boolean.class.getName(),Boolean.class.getName());
        primitiveTypeWrappersMap.put(float.class.getName(),Float.class.getName());
        primitiveTypeWrappersMap.put(short.class.getName(),Short.class.getName());
        primitiveTypeWrappersMap.put(char.class.getName(),Character.class.getName());

    }

    /**
     *
     * @param primitiveclassName
     */
    public static String getWrapper(String primitiveclassName){
        String returnClassName = null;
        if (primitiveclassName.trim().endsWith("[]")) {
            String className = primitiveclassName.substring(0, primitiveclassName.length() - 2);
            returnClassName = primitiveTypeWrappersMap.get(className) + "[]";
        } else {
            returnClassName = (String) primitiveTypeWrappersMap.get(primitiveclassName);
        }
        return returnClassName;
    }
}
