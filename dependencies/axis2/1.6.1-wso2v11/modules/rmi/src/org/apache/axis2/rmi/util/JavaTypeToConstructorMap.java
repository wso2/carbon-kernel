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

package org.apache.axis2.rmi.util;

import java.lang.reflect.Constructor;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JavaTypeToConstructorMap {

    private static Map javaTypeToConstructorMap;

    public static Constructor getConstructor(Class key) {
        return (Constructor) javaTypeToConstructorMap.get(key);
    }

    public static boolean containsKey(Class key) {
        return javaTypeToConstructorMap.containsKey(key);
    }

    static {
        javaTypeToConstructorMap = new HashMap();
        // pupualte the map
        try {
            javaTypeToConstructorMap.put(String.class, String.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(boolean.class, Boolean.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(Boolean.class, Boolean.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(double.class, Double.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(Double.class, Double.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(float.class, Float.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(Float.class, Float.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(int.class, Integer.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(Integer.class, Integer.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(long.class, Long.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(Long.class, Long.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(short.class, Short.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(Short.class, Short.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(byte.class, Byte.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(Byte.class, Byte.class.getConstructor(new Class[]{String.class}));
            javaTypeToConstructorMap.put(Calendar.class, Constants.XSD_DATETIME);
            javaTypeToConstructorMap.put(Date.class, Constants.XSD_DATE);
        } catch (NoSuchMethodException e) {
            // this exception should not occur since we dealing with know class
            // print the stacktrace for debuging purposes
            e.printStackTrace();
        }

    }

}
