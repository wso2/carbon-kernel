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

package org.apache.axis2.jaxws.addressing.factory;

import org.apache.axis2.addressing.EndpointReference;

import javax.xml.namespace.QName;

/**
 * This class represents factories that can be use to create instances of
 * {@link EndpointReference} that can ultimately be converted into instances
 * of {@link javax.xml.ws.EndpointReference} that are suitable to be returned
 * via the appropriate JAX-WS 2.1 API methods.
 * 
 */
public interface Axis2EndpointReferenceFactory {
    /**
     * Create an instance of <code>EndpointReference</code> with the specified address.
     * 
     * @param address the address URI to use. It cannot be null.
     * @return an instance of <code>EndpointReference</code>.
     */
    public EndpointReference createEndpointReference(String address);
    
    /**
     * Create an instance of <code>EndpointReference</code> that targets the endpoint
     * identified by the specified WSDL service name and endpoint name.
     * 
     * @param serviceName the WSDL service name
     * @param endpoint the WSDL port name
     * @return an instance of <code>EndpointReference</code> that targets the specified
     * endpoint
     */
    public EndpointReference createEndpointReference(QName serviceName, QName endpoint);
    
    /**
     * Create an instance of <code>EndpointReference</code>. If the address is specified
     * then it will be used. If the address is null, but the WSDL service name and port
     * name are specified then they will be used to target the specified endpoint. Either
     * the address URI, or the WSDL service name and port name must be specified.
     * 
     * @param address the address URI to use, if specified
     * @param serviceName the WSDL service name, if specified
     * @param portName the WSDL port name, if specified
     * @param wsdlDocumentLocation the URI from where the WSDL for the endpoint can be
     * retrieved, if specified.
     * @param addressingNamespace the intended WS-Addressing namespace that the <code>
     * EndpointRefence</code> should comply with.
     * @return an instance of <code>EndpointReference</code>.
     */
    public EndpointReference createEndpointReference(String address, QName serviceName, QName portName, String wsdlDocumentLocation, String addressingNamespace);
}