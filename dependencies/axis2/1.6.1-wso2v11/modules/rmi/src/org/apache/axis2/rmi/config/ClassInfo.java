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

package org.apache.axis2.rmi.config;

import java.util.HashMap;
import java.util.Map;

/**
 * this is used to keep the custome information
 * about the class
 */
public class ClassInfo {

    private Map fieldInfoMap;
    private Class javaClass;

    public ClassInfo(Class javaClass) {
        this();
        this.javaClass = javaClass;
    }

    public ClassInfo() {
        fieldInfoMap = new HashMap();
    }

    public void addFieldInfo(FieldInfo fieldInfo){
        fieldInfoMap.put(fieldInfo.getJavaName(),fieldInfo);
    }

    public FieldInfo getFieldInfo(String javaName){
        return (FieldInfo) fieldInfoMap.get(javaName);
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public void setJavaClass(Class javaClass) {
        this.javaClass = javaClass;
    }
}
