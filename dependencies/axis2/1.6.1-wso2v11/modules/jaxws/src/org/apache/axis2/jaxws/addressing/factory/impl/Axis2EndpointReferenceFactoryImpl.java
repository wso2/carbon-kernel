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

package org.apache.axis2.jaxws.addressing.factory.impl;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.factory.Axis2EndpointReferenceFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMap;
import org.apache.axis2.jaxws.addressing.util.EndpointContextMapManager;
import org.apache.axis2.jaxws.addressing.util.EndpointKey;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceUtils;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;

import javax.xml.namespace.QName;
import java.net.URL;

/**
 * This class produces instances of {@link EndpointReference}.
 *
 */
public class Axis2EndpointReferenceFactoryImpl implements Axis2EndpointReferenceFactory {
    public Axis2EndpointReferenceFactoryImpl() {
    	super();
    }
    
    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.addressing.factory.Axis2EndpointReferenceFactory#createEndpointReference(java.lang.String)
     */
    public EndpointReference createEndpointReference(String address) {
        if (address == null)
            throw new IllegalStateException("The endpoint address URI is null.");

        return new EndpointReference(address);
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.addressing.factory.Axis2EndpointReferenceFactory#createEndpointReference(javax.xml.namespace.QName, javax.xml.namespace.QName)
     */
    public EndpointReference createEndpointReference(QName serviceName, QName endpoint) {
        EndpointKey key = new EndpointKey(serviceName, endpoint);
        EndpointContextMap map = EndpointContextMapManager.getEndpointContextMap();
        
        if (!map.containsKey(key))
            throw new IllegalStateException("Unable to locate a deployed service that maps to the requested endpoint, " + key);
        
        AxisService axisService = (AxisService) map.get(key);
        String address = null;
        
        try {
            address = axisService.getEPRs()[0];
        }
        catch (Exception e) {
            //do nothing
        }
        
        return createEndpointReference(address);
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.addressing.factory.Axis2EndpointReferenceFactory#createEndpointReference(java.lang.String, javax.xml.namespace.QName, javax.xml.namespace.QName, java.lang.String, java.lang.String)
     */
    public EndpointReference createEndpointReference(String address, QName serviceName, QName portName, String wsdlDocumentLocation, String addressingNamespace) {
        EndpointReference axis2EPR = null;
        
        if (address != null) {
            if (serviceName == null && portName != null) {
                throw new IllegalStateException(
                    Messages.getMessage("axisEndpointReferenceFactoryErr", 
                                        portName.toString()));
            }
            axis2EPR = createEndpointReference(address);
        }
        else if (serviceName != null && portName != null) {
            axis2EPR = createEndpointReference(serviceName, portName);
        }
        else {
            throw new IllegalStateException(
                 Messages.getMessage("axisEndpointReferenceFactoryErr2"));
        }
        
        //TODO If no service name and port name are specified, but the wsdl location is
        //specified, and the WSDL only contains one service and one port then maybe we
        //should simply use those.        
        try {
            //This code is locate here instead of in the createEndpointReference(QName, QName)
            //method so that if the address is also specified the EPR metadata will still be
            //filled in correctly.
            EndpointReferenceUtils.addService(axis2EPR, serviceName, portName, addressingNamespace);

            if (wsdlDocumentLocation != null) {
            	URL wsdlURL = new URL(wsdlDocumentLocation);
            	// This is a temporary usage, so use a memory sensitive wrapper
                WSDLWrapper wrapper = new WSDL4JWrapper(wsdlURL, true, 2);
            	
                if (serviceName != null) {
                    if (wrapper.getService(serviceName) == null) {
                        throw new IllegalStateException(
                            Messages.getMessage("MissingServiceName", 
                                                serviceName.toString(), 
                                                wsdlDocumentLocation));
                    }
                    if (portName != null) {
                        String[] ports = wrapper.getPorts(serviceName);
                        String portLocalName = portName.getLocalPart();
                        boolean found = false;

                        if (ports != null) {
                            for (String port : ports) {
                                if (port.equals(portLocalName)) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                        if (!found) {
                            throw new IllegalStateException(
                                         Messages.getMessage("MissingPortName", 
                                                             portName.toString(), 
                                                             wsdlDocumentLocation)); 
                        }
                        EndpointReferenceUtils.addLocation(axis2EPR, portName.getNamespaceURI(), wsdlDocumentLocation, addressingNamespace);
                    }
                }
            }
        }
        catch (IllegalStateException ise) {
        	throw ise;
        }
        catch (Exception e) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("endpointRefCreationError"), e);
        }
        return axis2EPR;
    }
}
