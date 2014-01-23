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

public class WebServiceProviderAnnot implements javax.xml.ws.WebServiceProvider {

    private String wsdlLocation = "";
    private String serviceName = "";
    private String portName = "";
    private String targetNamespace = "";

    /** A WebServiceProviderAnnot cannot be instantiated. */
    private WebServiceProviderAnnot() {

    }

    private WebServiceProviderAnnot(
            String wsdlLocation,
            String serviceName,
            String portName,
            String targetNamespace) {
        this.targetNamespace = targetNamespace;
        this.serviceName = serviceName;
        this.wsdlLocation = wsdlLocation;
        this.portName = portName;
    }

    public static WebServiceProviderAnnot createWebServiceAnnotImpl() {
        return new WebServiceProviderAnnot();
    }

    public static WebServiceProviderAnnot createWebServiceAnnotImpl(
            String name,
            String targetNamespace,
            String serviceName,
            String wsdlLocation,
            String endpointInterface,
            String portName
    ) {
        return new WebServiceProviderAnnot(wsdlLocation,
                                           serviceName,
                                           portName,
                                           targetNamespace);
    }
    
    public static WebServiceProviderAnnot createFromAnnotation(Annotation annotation) {
        WebServiceProviderAnnot returnAnnot = null;
        if (annotation != null && annotation instanceof javax.xml.ws.WebServiceProvider) {
            javax.xml.ws.WebServiceProvider wsp = (javax.xml.ws.WebServiceProvider) annotation;
            returnAnnot = new WebServiceProviderAnnot(wsp.wsdlLocation(),
                                                      wsp.serviceName(),
                                                      wsp.portName(),
                                                      wsp.targetNamespace());
                                                     
        }
        
        return returnAnnot;
    }

    /** @return Returns the portName. */
    public String portName() {
        return portName;
    }

    /** @return Returns the serviceName. */
    public String serviceName() {
        return serviceName;
    }

    /** @return Returns the targetNamespace. */
    public String targetNamespace() {
        return targetNamespace;
    }

    /** @return Returns the wsdlLocation. */
    public String wsdlLocation() {
        return wsdlLocation;
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

    public Class<Annotation> annotationType() {
        return Annotation.class;
    }

    /**
     * Convenience method for unit testing. We will print all of the
     * data members here.
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        String newLine = "\n";
        sb.append(newLine);
        sb.append("@WebServiceProvider.serviceName= " + serviceName);
        sb.append(newLine);
        sb.append("@WebServiceProvider.targetNamespace= " + targetNamespace);
        sb.append(newLine);
        sb.append("@WebServiceProvider.wsdlLocation= " + wsdlLocation);
        sb.append(newLine);
        sb.append("@WebServiceProvider.portName= " + portName);
        sb.append(newLine);
        return sb.toString();
	}
}
