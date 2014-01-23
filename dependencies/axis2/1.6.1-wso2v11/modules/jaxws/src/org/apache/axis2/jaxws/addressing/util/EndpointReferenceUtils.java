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

package org.apache.axis2.jaxws.addressing.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.addressing.metadata.InterfaceName;
import org.apache.axis2.addressing.metadata.ServiceName;
import org.apache.axis2.addressing.metadata.WSDLLocation;
import org.apache.axis2.jaxws.addressing.factory.Axis2EndpointReferenceFactory;
import org.apache.axis2.jaxws.addressing.factory.JAXWSEndpointReferenceFactory;
import org.apache.axis2.jaxws.registry.FactoryRegistry;
import org.apache.axis2.util.XMLUtils;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public final class EndpointReferenceUtils {
    
    private static OMFactory omFactory = OMAbstractFactory.getOMFactory();

    private EndpointReferenceUtils() {
    }

    /**
     * Convert from a {@link EndpointReference} to a
     * subclass of {@link javax.xml.ws.EndpointReference}.
     * 
     * @param axis2EPR
     * @param addressingNamespace
     * @return
     * @throws Exception
     */
    public static javax.xml.ws.EndpointReference convertFromAxis2(EndpointReference axis2EPR, String addressingNamespace)
    throws Exception {
        QName qname = new QName(addressingNamespace, "EndpointReference", "wsa");
        OMElement omElement =
            EndpointReferenceHelper.toOM(omFactory, axis2EPR, qname, addressingNamespace);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        omElement.serialize(baos);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());        
        Source eprInfoset = new StreamSource(bais);
        
        return convertFromSource(eprInfoset);
    }

    /**
     * Convert from a {@link Source} to a
     * subclass of {@link javax.xml.ws.EndpointReference}.
     * 
     * @param eprInfoset
     * @return
     * @throws Exception
     */
    public static javax.xml.ws.EndpointReference convertFromSource(Source eprInfoset)
    throws Exception {
        JAXWSEndpointReferenceFactory factory = (JAXWSEndpointReferenceFactory)
            FactoryRegistry.getFactory(JAXWSEndpointReferenceFactory.class);
        
        return factory.createEndpointReference(eprInfoset);
    }
    
    /**
     * Convert from a {@link javax.xml.ws.EndpointReference} to a an instance of
     * {@link EndpointReference}.
     * 
     * @param axis2EPR
     * @param jaxwsEPR
     * @return the WS-Addressing namespace of the <code>javax.xml.ws.EndpointReference</code>.
     * @throws Exception
     */
    public static String convertToAxis2(EndpointReference axis2EPR, javax.xml.ws.EndpointReference jaxwsEPR)
    throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxwsEPR.writeTo(new StreamResult(baos));
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        OMElement eprElement = (OMElement) XMLUtils.toOM(bais);
        
        return EndpointReferenceHelper.fromOM(axis2EPR, eprElement);
    }

    /**
     * 
     * @param <T>
     * @param clazz
     * @return
     */
    public static <T extends javax.xml.ws.EndpointReference> String getAddressingNamespace(Class<T> clazz) {
        JAXWSEndpointReferenceFactory factory = (JAXWSEndpointReferenceFactory)
            FactoryRegistry.getFactory(JAXWSEndpointReferenceFactory.class);
        
        return factory.getAddressingNamespace(clazz);
    }
    
    /**
     * 
     * @param address
     * @param serviceName
     * @param portName
     * @param wsdlDocumentLocation
     * @param addressingNamespace
     * @return
     */
    public static EndpointReference createAxis2EndpointReference(String address, QName serviceName, QName portName, String wsdlDocumentLocation, String addressingNamespace) {
        Axis2EndpointReferenceFactory factory = (Axis2EndpointReferenceFactory)
            FactoryRegistry.getFactory(Axis2EndpointReferenceFactory.class);
        
        return factory.createEndpointReference(address, serviceName, portName, wsdlDocumentLocation, addressingNamespace);
    }
    
    /**
     * 
     * @param address
     * @return
     */
    public static EndpointReference createAxis2EndpointReference(String address) {
        Axis2EndpointReferenceFactory factory = (Axis2EndpointReferenceFactory)
            FactoryRegistry.getFactory(Axis2EndpointReferenceFactory.class);
        
    	return factory.createEndpointReference(address);
    }
    
    /**
     * 
     * @param axis2EPR
     * @param referenceParameters
     * @throws Exception
     */
    public static void addReferenceParameters(EndpointReference axis2EPR, Element...referenceParameters)
    throws Exception {
        if (referenceParameters != null) {
            for (Element element : referenceParameters) {
                OMElement omElement = XMLUtils.toOM(element);
                axis2EPR.addReferenceParameter(omElement);
            }            
        }    	
    }
    
    /**
     * 
     * @param axis2EPR
     * @param elements
     * @throws Exception
     */
    public static void addExtensibleElements(EndpointReference axis2EPR, Element... elements)
    throws Exception {
        if (elements != null) {
            for (Element element : elements) {
                OMElement omElement = XMLUtils.toOM(element);
                axis2EPR.addExtensibleElement(omElement);
            }
        }
    }
    
    public static void addExtensibleAttributes(EndpointReference axis2EPR, Map<QName, String> attributes)
    throws Exception {
        if (attributes != null) {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            for (Map.Entry<QName, String> attribute : attributes.entrySet()) {
                QName qName = attribute.getKey();
                OMNamespace omNamespace = fac.createOMNamespace(qName.getNamespaceURI(), qName.getPrefix());
                axis2EPR.addAttribute(qName.getLocalPart(), omNamespace, attribute.getValue());
            }
        }
    }
    
    /**
     * 
     * @param axis2EPR
     * @param metadata
     * @throws Exception
     */
    public static void addMetadata(EndpointReference axis2EPR, Element...metadata)
    throws Exception {
        if (metadata != null) {
            for (Element element : metadata) {
                OMElement omElement = XMLUtils.toOM(element);
                axis2EPR.addMetaData(omElement);
            }
        }
    }
    
    /**
     * 
     * @param axis2EPR
     * @param portType
     * @param addressingNamespace
     * @throws Exception
     */
    public static void addInterface(EndpointReference axis2EPR, QName portType, String addressingNamespace)
    throws Exception {
    	if (portType != null) {
    		InterfaceName interfaceName = new InterfaceName(portType);
    		EndpointReferenceHelper.setInterfaceNameMetadata(omFactory, axis2EPR, addressingNamespace, interfaceName);
    	}
    }
    
    /**
     * 
     * @param axis2EPR
     * @param service
     * @param port
     * @param addressingNamespace
     * @throws Exception
     */
    public static void addService(EndpointReference axis2EPR, QName service, QName port, String addressingNamespace)
    throws Exception {
        if (service != null && port != null) {
            ServiceName serviceName = new ServiceName(service, port.getLocalPart());
            EndpointReferenceHelper.setServiceNameMetadata(omFactory, axis2EPR, addressingNamespace, serviceName);
        }
    }
    
    /**
     * 
     * @param axis2EPR
     * @param targetNamespace
     * @param wsdlDocumentLocation
     * @param addressingNamespace
     * @throws Exception
     */
    public static void addLocation(EndpointReference axis2EPR, String targetNamespace, String wsdlDocumentLocation, String addressingNamespace)
    throws Exception {
        if (targetNamespace != null && wsdlDocumentLocation != null) {
            WSDLLocation wsdlLocation = new WSDLLocation(targetNamespace, wsdlDocumentLocation);
            EndpointReferenceHelper.setWSDLLocationMetadata(omFactory, axis2EPR, addressingNamespace, wsdlLocation);
        }
    }
}
