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

public class WebServiceAnnot implements javax.jws.WebService {

    private String name = "";
    private String targetNamespace = "";
    private String serviceName = "";
    private String wsdlLocation = "";
    private String endpointInterface = "";
    private String portName = "";

    /** A WebServiceAnnot cannot be instantiated. */
    private WebServiceAnnot() {

    }

    private WebServiceAnnot(
            String name,
            String targetNamespace,
            String serviceName,
            String wsdlLocation,
            String endpointInterface,
            String portName) {
        this.name = name;
        this.targetNamespace = targetNamespace;
        this.serviceName = serviceName;
        this.wsdlLocation = wsdlLocation;
        this.endpointInterface = endpointInterface;
        this.portName = portName;
    }

    public static WebServiceAnnot createWebServiceAnnotImpl() {
        return new WebServiceAnnot();
    }

    public static WebServiceAnnot createWebServiceAnnotImpl(
            String name,
            String targetNamespace,
            String serviceName,
            String wsdlLocation,
            String endpointInterface,
            String portName
    ) {
        return new WebServiceAnnot(name,
                                   targetNamespace,
                                   serviceName,
                                   wsdlLocation,
                                   endpointInterface,
                                   portName);
    }
    
    public static WebServiceAnnot createFromAnnotation(Annotation annotation) {
        WebServiceAnnot returnAnnot = null;
        if (annotation != null && annotation instanceof javax.jws.WebService) {
            javax.jws.WebService ws = (javax.jws.WebService) annotation;
            return new WebServiceAnnot(ws.name(),
                                       ws.targetNamespace(),
                                       ws.serviceName(),
                                       ws.wsdlLocation(),
                                       ws.endpointInterface(),
                                       ws.portName());
                                      
        }
        return returnAnnot;
    }

    public String name() {
        return this.name;
    }

    public String targetNamespace() {
        return this.targetNamespace;
    }

    public String serviceName() {
        return this.serviceName;
    }

    public String wsdlLocation() {
        return this.wsdlLocation;
    }

    public String endpointInterface() {
        return this.endpointInterface;
    }

    public String portName() {
        return this.portName;
    }


    public Class<Annotation> annotationType() {
        return Annotation.class;
    }

    //Setters

    /** @param endpointInterface The endpointInterface to set. */
    public void setEndpointInterface(String endpointInterface) {
        this.endpointInterface = endpointInterface;
    }

    /** @param name The name to set. */
    public void setName(String name) {
        this.name = name;
    }

    /** @param portName The portName to set. */
    public void setPortName(String portName) {
        this.portName = portName;
    }

    /** @param serviceName The serviceName to set. */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /** @param targetNamespace The targetNamespace to set. */
    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    /** @param wsdlLocation The wsdlLocation to set. */
    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }


    /**
     * Convenience method for unit testing. We will print all of the
     * data members here.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        sb.append(newLine);
        sb.append("@WebService.name= " + name);
        sb.append(newLine);
        sb.append("@WebService.serviceName= " + serviceName);
        sb.append(newLine);
        sb.append("@WebService.endpointInterface= " + endpointInterface);
        sb.append(newLine);
        sb.append("@WebService.targetNamespace= " + targetNamespace);
        sb.append(newLine);
        sb.append("@WebService.wsdlLocation= " + wsdlLocation);
        sb.append(newLine);
        sb.append("@WebService.portName= " + portName);
        sb.append(newLine);
        return sb.toString();
	}
}
