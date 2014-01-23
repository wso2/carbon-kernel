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


import javax.xml.namespace.QName;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class JavaTypeToQNameMap {

    private static Map javaTypeToQNameMap;

    public static QName getTypeQName(Class key) {
        return (QName) javaTypeToQNameMap.get(key);
    }

    public static boolean containsKey(Class key) {
        return javaTypeToQNameMap.containsKey(key);
    }

    public static Set getKeys(){
        return javaTypeToQNameMap.keySet();
    }

    static {
        javaTypeToQNameMap = new HashMap();
        // pupualte the map
        javaTypeToQNameMap.put(String.class, Constants.XSD_STRING);
        javaTypeToQNameMap.put(boolean.class, Constants.XSD_BOOLEAN);
        javaTypeToQNameMap.put(Boolean.class, Constants.XSD_BOOLEAN);
        javaTypeToQNameMap.put(double.class, Constants.XSD_DOUBLE);
        javaTypeToQNameMap.put(Double.class, Constants.XSD_DOUBLE);
        javaTypeToQNameMap.put(float.class, Constants.XSD_FLOAT);
        javaTypeToQNameMap.put(Float.class, Constants.XSD_FLOAT);
        javaTypeToQNameMap.put(int.class, Constants.XSD_INT);
        javaTypeToQNameMap.put(Integer.class, Constants.XSD_INT);
        javaTypeToQNameMap.put(long.class, Constants.XSD_LONG);
        javaTypeToQNameMap.put(Long.class, Constants.XSD_LONG);
        javaTypeToQNameMap.put(short.class, Constants.XSD_SHORT);
        javaTypeToQNameMap.put(Short.class, Constants.XSD_SHORT);
        javaTypeToQNameMap.put(byte.class, Constants.XSD_BYTE);
        javaTypeToQNameMap.put(Byte.class, Constants.XSD_BYTE);
        javaTypeToQNameMap.put(Calendar.class, Constants.XSD_DATETIME);
        javaTypeToQNameMap.put(Date.class, Constants.XSD_DATE);
        javaTypeToQNameMap.put(Object.class, Constants.XSD_ANYTYPE);


    }


}
