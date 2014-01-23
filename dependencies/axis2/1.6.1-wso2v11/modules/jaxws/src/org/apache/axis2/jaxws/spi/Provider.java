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

package org.apache.axis2.jaxws.spi;

import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.metadata.ServiceName;
import org.apache.axis2.addressing.metadata.WSDLLocation;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.util.EndpointReferenceUtils;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.server.endpoint.EndpointImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Provider extends javax.xml.ws.spi.Provider {
    private static final Log log = LogFactory.getLog(Provider.class);
    
	private static final Element[] ZERO_LENGTH_ARRAY = new Element[0];

	@Override
    public Endpoint createAndPublishEndpoint(String s, Object obj) {
	  return createAndPublishEndpoint(s, obj, (WebServiceFeature[]) null);
    }

    public Endpoint createAndPublishEndpoint(String s, Object obj, WebServiceFeature... features) {
        Endpoint ep = new EndpointImpl(obj);
        ep.publish(s);
        return ep;
    }

    @Override
    public Endpoint createEndpoint(String binding, Object obj) {
        return createEndpoint(binding, obj, (WebServiceFeature[])null);
    }

    //TODO: Fix the Endpoint to support WebServiceFeatures (for non-JEE users)
    public Endpoint createEndpoint(String binding, Object obj, WebServiceFeature... features) {
        return new EndpointImpl(obj);
    }

    //TODO: Fix the Endpoint to support WebServiceFeatures (for non-JEE users)
//    @Override
//    public Endpoint createEndpoint(String binding, Class clazz, Invoker invoker, WebServiceFeature... features) {
//      
//    }
    
    @Override
    public ServiceDelegate createServiceDelegate(URL url, QName qname, Class clazz) {
        return createServiceDelegate(url, qname, clazz, (WebServiceFeature[])null);
    }

    public ServiceDelegate createServiceDelegate(URL url, QName qname, Class clazz, WebServiceFeature... features) {
        return new org.apache.axis2.jaxws.spi.ServiceDelegate(url, qname, clazz, features);
    }

    @Override
    public W3CEndpointReference createW3CEndpointReference(String address,
            QName serviceName,
            QName portName,
            List<Element> metadata,
            String wsdlDocumentLocation,
            List<Element> referenceParameters) {
        String addressingNamespace =
        	EndpointReferenceUtils.getAddressingNamespace(W3CEndpointReference.class);    	
        org.apache.axis2.addressing.EndpointReference axis2EPR =
        	EndpointReferenceUtils.createAxis2EndpointReference(address, serviceName, portName, wsdlDocumentLocation, addressingNamespace);
        
        W3CEndpointReference w3cEPR = null;
        
        try {
            if (metadata != null)
                EndpointReferenceUtils.addMetadata(axis2EPR, metadata.toArray(ZERO_LENGTH_ARRAY));
        	
            if (referenceParameters != null)
                EndpointReferenceUtils.addReferenceParameters(axis2EPR, referenceParameters.toArray(ZERO_LENGTH_ARRAY));
        	
            w3cEPR =
                (W3CEndpointReference) EndpointReferenceUtils.convertFromAxis2(axis2EPR, addressingNamespace);
        }
        catch (Exception e) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("referenceParameterConstructionErr"),
                                    e);
        }
        
        return w3cEPR;
    }
    
    @Override
    public W3CEndpointReference createW3CEndpointReference(String address,
            QName interfaceName, 
            QName serviceName,
            QName portName,
            List<Element> metadata,
            String wsdlDocumentLocation,
            List<Element> referenceParameters,
            List<Element> elements,
            Map<QName, String> attributes) {
        String addressingNamespace =
            EndpointReferenceUtils.getAddressingNamespace(W3CEndpointReference.class);      
        org.apache.axis2.addressing.EndpointReference axis2EPR =
            EndpointReferenceUtils.createAxis2EndpointReference(address, serviceName, portName, wsdlDocumentLocation, addressingNamespace);
        
        W3CEndpointReference w3cEPR = null;
        
        try {
            if (interfaceName != null)
                EndpointReferenceUtils.addInterface(axis2EPR, interfaceName, addressingNamespace);
            
            if (metadata != null)
                EndpointReferenceUtils.addMetadata(axis2EPR, metadata.toArray(ZERO_LENGTH_ARRAY));
            
            if (referenceParameters != null)
                EndpointReferenceUtils.addReferenceParameters(axis2EPR, referenceParameters.toArray(ZERO_LENGTH_ARRAY));
            
            if (elements != null)
                EndpointReferenceUtils.addExtensibleElements(axis2EPR, elements.toArray(ZERO_LENGTH_ARRAY));
            
            if (attributes != null)
                EndpointReferenceUtils.addExtensibleAttributes(axis2EPR, attributes);
            
            w3cEPR =
                (W3CEndpointReference) EndpointReferenceUtils.convertFromAxis2(axis2EPR, addressingNamespace);
        }
        catch (Exception e) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("referenceParameterConstructionErr"),
                                    e);
        }
        
        return w3cEPR;
    }

    @Override
    public <T> T getPort(EndpointReference jaxwsEPR, Class<T> sei, WebServiceFeature... features) {
        if (jaxwsEPR == null) {
            throw ExceptionFactory.
                makeWebServiceException(Messages.getMessage("dispatchNoEndpointReference2"));
        }
        
        if (sei == null) {
            throw ExceptionFactory.makeWebServiceException(
                    Messages.getMessage("getPortInvalidSEI", jaxwsEPR.toString(), "null"));
        }
        
        org.apache.axis2.addressing.EndpointReference axis2EPR =
            EndpointReferenceUtils.createAxis2EndpointReference("");
        String addressingNamespace = null;
        
        try {
            addressingNamespace = EndpointReferenceUtils.convertToAxis2(axis2EPR, jaxwsEPR);
        }
        catch (Exception e) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("invalidEndpointReference", 
                                                          e.toString()));
        }
        
        org.apache.axis2.jaxws.spi.ServiceDelegate serviceDelegate = null;
        
        try {
            ServiceName serviceName =
            	EndpointReferenceHelper.getServiceNameMetadata(axis2EPR, addressingNamespace);
            WSDLLocation wsdlLocation =
            	EndpointReferenceHelper.getWSDLLocationMetadata(axis2EPR, addressingNamespace);
            URL wsdlLocationURL = null;
            
            if (wsdlLocation.getLocation() != null) {
            	wsdlLocationURL = new URL(wsdlLocation.getLocation());
                if (log.isDebugEnabled()) {
                    log.debug("getPort: Using EPR wsdlLocationURL = " + wsdlLocationURL);
                }
            } else {
            	wsdlLocationURL = new URL(axis2EPR.getAddress() + "?wsdl");
                if (log.isDebugEnabled()) {
                    log.debug("getPort: Using default wsdlLocationURL = " + wsdlLocationURL);
                }
            }
            
            serviceDelegate =
            	new org.apache.axis2.jaxws.spi.ServiceDelegate(wsdlLocationURL, serviceName.getName(), Service.class);
        }
        catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(Messages.getMessage("endpointUpdateError"), e);
        }

        return serviceDelegate.getPort(axis2EPR, addressingNamespace, sei, features);
    }

    @Override
    public EndpointReference readEndpointReference(Source eprInfoset) {
        EndpointReference jaxwsEPR = null;

        try {
            jaxwsEPR = EndpointReferenceUtils.convertFromSource(eprInfoset);
        }
        catch (Exception e) {
            throw ExceptionFactory.
              makeWebServiceException(Messages.getMessage("endpointRefCreationError"),
                                      e);
        }
        
        return jaxwsEPR;
    }
}
