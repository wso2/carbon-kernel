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

/**
 * this class is used to keep the information about
 * the class filed
 */
public class FieldInfo {

    private String javaName;
    private String xmlName;
    // set the default element value to true
    private boolean isElement = true;

    public FieldInfo() {
    }

    public FieldInfo(String javaName) {
        this.javaName = javaName;
    }

    public FieldInfo(String javaName, String xmlName, boolean element) {
        this.javaName = javaName;
        this.xmlName = xmlName;
        isElement = element;
    }

    public String getJavaName() {
        return javaName;
    }

    public void setJavaName(String javaName) {
        this.javaName = javaName;
    }

    public String getXmlName() {
        return xmlName;
    }

    public void setXmlName(String xmlName) {
        this.xmlName = xmlName;
    }

    public boolean isElement() {
        return isElement;
    }

    public void setElement(boolean element) {
        isElement = element;
    }
}
