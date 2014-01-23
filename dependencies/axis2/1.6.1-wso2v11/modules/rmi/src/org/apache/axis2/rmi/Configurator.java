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

package org.apache.axis2.rmi;

import org.apache.axis2.rmi.config.ClassInfo;
import org.apache.axis2.rmi.databind.SimpleTypeHandler;
import org.apache.axis2.rmi.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * this class is used to keep all the
 * user defined configuration parameters
 */
public class Configurator {

    private boolean isBare;
    private List extensionClasses;
    private Map packageToNamespaceMap;
    private SimpleTypeHandler simpleTypeHandler;
    /**
     * this map contains custom details of the classes
     */
    private Map classInfoMap;

    public Configurator() {
        this.extensionClasses = new ArrayList();
        this.packageToNamespaceMap = new HashMap();
        this.classInfoMap = new HashMap();

        populateDefualtValues();
    }

    private void populateDefualtValues(){
        // we want to keep key and value attributes in null names pace.
        this.packageToNamespaceMap.put("org.apache.axis2.rmi.types", Constants.RMI_TYPE_NAMSPACE);
        this.simpleTypeHandler = new SimpleTypeHandler();
    }

    public String getNamespace(String packageName){
       if (this.packageToNamespaceMap.containsKey(packageName)){
           return (String) this.packageToNamespaceMap.get(packageName);
       } else {
           return "urn:" + packageName;
       }
    }

    public void addExtension(Class extensionClass){
        this.extensionClasses.add(extensionClass);
    }

    public void addPackageToNamespaceMaping(String packageName,
                                            String namespace){
        this.packageToNamespaceMap.put(packageName,namespace);
    }

    public void addClassInfo(ClassInfo classInfo){
        this.classInfoMap.put(classInfo.getJavaClass(),classInfo);
    }

    public ClassInfo getClassInfo(Class javaClass){
        return (ClassInfo) this.classInfoMap.get(javaClass);
    }

    public boolean isBare() {
        return isBare;
    }

    public void setBare(boolean bare) {
        isBare = bare;
    }

    public List getExtensionClasses() {
        return extensionClasses;
    }

    public void setExtensionClasses(List extensionClasses) {
        this.extensionClasses = extensionClasses;
    }

    public SimpleTypeHandler getSimpleTypeHandler() {
        return simpleTypeHandler;
    }

    public void setSimpleTypeHandler(SimpleTypeHandler simpleTypeHandler) {
        this.simpleTypeHandler = simpleTypeHandler;
    }
}
