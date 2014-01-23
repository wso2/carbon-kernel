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

package org.apache.axis2.jaxws.description.builder;

import java.lang.annotation.Annotation;

public class WebFaultAnnot implements javax.xml.ws.WebFault {

    private String name = "return";
    private String targetNamespace = "";
    private String faultBean = "";
    private String messageName = "";

    /** A WebFaultAnnot cannot be instantiated. */
    private WebFaultAnnot() {

    }

    public static WebFaultAnnot createWebFaultAnnotImpl() {
        return new WebFaultAnnot();
    }

    /**
     * Get the 'name'
     *
     * @return String
     */
    public String name() {
        return this.name;
    }

    public String targetNamespace() {
        return this.targetNamespace;
    }

    public String faultBean() {
        return this.faultBean;
    }

    public String messageName() {
        return this.messageName;
    }

    /** @param faultBean The faultBean to set. */
    public void setFaultBean(String faultBean) {
        this.faultBean = faultBean;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @param targetNamespace The targetNamespace to set. */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    /** @param messageName The name of the wsdl:message. */
    public void setMessageName(String messageName) {
        this.messageName = messageName;
    }

    public Class<Annotation> annotationType() {
        return Annotation.class;
    }

    /** Convenience method for unit testing. We will print all of the data members here. */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        sb.append(newLine);
        sb.append("@WebFault.name= " + name);
        sb.append(newLine);
        sb.append("@WebFault.faultBean= " + faultBean);
        sb.append(newLine);
        sb.append("@WebFault.targetNamespace= " + targetNamespace);
        sb.append(newLine);
        sb.append("@WebFault.messageName= " + messageName);
        sb.append(newLine);
        return sb.toString();
    }

}
