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

package org.apache.axis2.jaxws.description;

import org.apache.axis2.jaxws.util.Constants;

import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.xml.namespace.QName;

/**
 * 
 */
public interface EndpointDescriptionWSDL {
    /**
     * Strings representing the SOAP Binding.  These correspond to the namespace of the binding
     * extensibility element under the WSDL binding.  This could be SOAP or HTTP
     */
    public static final String SOAP11_WSDL_BINDING = "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String SOAP12_WSDL_BINDING = "http://schemas.xmlsoap.org/wsdl/soap12/";
    public static final String HTTP_WSDL_BINDING = "http://schemas.xmlsoap.org/wsdl/http/";
    /** QNames for the SOAP address extensiblity element under the WSDL Port element */
    public static final QName SOAP_11_ADDRESS_ELEMENT =
            new QName(Constants.URI_WSDL_SOAP11, "address");
    public static final QName SOAP_12_ADDRESS_ELEMENT =
            new QName(Constants.URI_WSDL_SOAP12, "address");

    public Definition getWSDLDefinition();
    public Service getWSDLService();

    public Port getWSDLPort();

    public Binding getWSDLBinding();

    /**
     * Returns the namespace for the specific wsdl:binding extensibility element. Typically, this is
     * the <soap:binding> element that defines either a SOAP 1.1 or a SOAP 1.2 binding.
     * 
     * IMPORTANT NOTE: The value returned is converted from the WSDL Binding type (which is the
     * namespace on the assocaited binding extensibility element) to the corresponding value
     * for the SOAPBinding annotation.  For example, the following SOAP12 WSDL
     *     ...
     *     xmlns:soap12="http://schemas.xmlsoap.org/wsdl/soap12/"
     *     ...  
     *     <wsdl:binding ...>
     *       <soap12:binding ...>
     * 
     * Would return the value javax.xml.ws.soap.SOAPBinding.SOAP12HTTP_BINDING which is
     *     "http://www.w3.org/2003/05/soap/bindings/HTTP/"
     *
     * @return String constants defined in javax.xml.ws.soap.SOAPBinding
     */
    public String getWSDLBindingType();

    public String getWSDLSOAPAddress();

    /**
     * Is the WSDL definition fully specified for the endpoint (WSDL 1.1 port) represented by this
     * EndpointDescription.  If the WSDL is Partial, that means the Endpoint could not be created
     * with the infomation contained in the WSDL file, and annotations were used.
     *
     * @return true if the WSDL was fully specified; false if it was partial WSDL
     */
    public boolean isWSDLFullySpecified();

}
