/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.spi;

import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.description.builder.AddressingAnnot;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.AddressingFeature.Responses;

import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Validate the setting up of the Addressing WebServiceFeature on the client side via meta-data 
 * (such as a deployment descriptor)
 */
public class ClientMetadataAddressingFeatureTests extends TestCase {
    static final String namespaceURI = "http://description.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    static final String multiPortWsdl = "ClientMetadataMultiPort.wsdl";
    
    public void testAddressingEnabled() {
        Service service = createService();
        ClientMetadataAddressingPortSEI port = service.getPort(ClientMetadataAddressingPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("Addressing is not configured", soapBinding.isAddressingConfigured());
        assertTrue("Addressing is not enabled", soapBinding.isAddressingEnabled());
    }
    
    public void testAddressingDisabled() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        AddressingAnnot addressingFeature = new AddressingAnnot();
        addressingFeature.setEnabled(false);
        wsFeatures.add(addressingFeature);
        map.put(ClientMetadataAddressingPortSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);
        ClientMetadataAddressingPortSEI port = service.getPort(ClientMetadataAddressingPortSEI.class);

        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("Addressing is not configured", soapBinding.isAddressingConfigured());
        assertFalse("Addressing is enabled", soapBinding.isAddressingEnabled());
    }
    
    public void testAddressingRequired() {
        Service service = createService();
        ClientMetadataAddressingPortSEI port = service.getPort(ClientMetadataAddressingPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("Addressing is not configured", soapBinding.isAddressingConfigured());
        assertTrue("Addressing is not required", soapBinding.isAddressingRequired());
    }
    
    public void testAddressingNotRequired() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        AddressingAnnot addressingFeature = new AddressingAnnot();
        addressingFeature.setEnabled(true);
        addressingFeature.setRequired(false);
        wsFeatures.add(addressingFeature);
        map.put(ClientMetadataAddressingPortSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);
        ClientMetadataAddressingPortSEI port = service.getPort(ClientMetadataAddressingPortSEI.class);

        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("Addressing is not configured", soapBinding.isAddressingConfigured());
        assertFalse("Addressing is required", soapBinding.isAddressingRequired());
    }
    
    public void testAddressingResponses() {
        Service service = createService();
        ClientMetadataAddressingPortSEI port = service.getPort(ClientMetadataAddressingPortSEI.class);
        
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue("Addressing is not configured", soapBinding.isAddressingConfigured());
        assertEquals("Addressing Responses incorrect", Responses.NON_ANONYMOUS, soapBinding.getAddressingResponses());
    }
    
    public void testDefaultAddressingValues() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Service service = Service.create(wsdlUrl, serviceQName);
        ClientMetadataAddressingPortSEI port = service.getPort(ClientMetadataAddressingPortSEI.class);

        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding soapBinding = (SOAPBinding) bindingProvider.getBinding();
        assertFalse("Addressing is configured", soapBinding.isAddressingConfigured());
        assertFalse("Addressing is enabled", soapBinding.isAddressingEnabled());
        assertFalse("Addressing is required", soapBinding.isAddressingRequired());
        assertEquals("Addressing responses incorrect", Responses.ALL, soapBinding.getAddressingResponses());
    }
    
    /**
     * Create a service as would be done via injection or lookup, including a sparse composite that 
     * contains features (as might be set by a deployment descriptor).
     * 
     * @return a Service created as done via injection or lookup.
     */
    private Service createService() {
        // Even for a port injection or lookup, the service will also be treated as an injection or lookup
        // So we need to setup the sparse DBC to create the service
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite serviceDBC = new DescriptionBuilderComposite();
        
        Map<String, List<Annotation>> map = new HashMap();
        ArrayList<Annotation> wsFeatures = new ArrayList<Annotation>();
        AddressingAnnot addressingFeature = new AddressingAnnot();
        addressingFeature.setEnabled(true);
        addressingFeature.setRequired(true);
        addressingFeature.setResponses(Responses.NON_ANONYMOUS);
        wsFeatures.add(addressingFeature);
        map.put(ClientMetadataAddressingPortSEI.class.getName(), wsFeatures);
        serviceDBC.getProperties().put(MDQConstants.SEI_FEATURES_MAP, map);
        ServiceDelegate.setServiceMetadata(serviceDBC);
        Service service = Service.create(wsdlUrl, serviceQName);
        return service;
    }
    
    @WebService(name="EchoMessagePortType", targetNamespace="http://description.jaxws.axis2.apache.org")
    interface ClientMetadataAddressingPortSEI {
        public String echoMessage(String string);
    }
}