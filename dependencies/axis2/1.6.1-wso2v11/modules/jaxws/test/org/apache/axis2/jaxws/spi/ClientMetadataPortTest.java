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

import junit.framework.TestCase;

import org.apache.axis2.jaxws.description.DescriptionTestUtils2;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;

import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.soap.SOAPBinding;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class ClientMetadataPortTest extends TestCase {
    static final String namespaceURI = "http://description.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";

    static final String originalWsdl = "ClientMetadata.wsdl";
    static final String overridenWsdl = "ClientMetadataOverriden.wsdl";
    static final String otherWsdl = "ClientMetadataOther.wsdl";
    static final String multiPortWsdl = "ClientMetadataMultiPort.wsdl";

    static final String originalWsdl_portLocalPart = "portLocalPart";
    static final String overridenWsdl_portLocalPart = "portLocalPartOverriden";
    static final String otherWsdl_portLocalPart = "portLocalPartOther";
    static final String multiPortWsdl_portLocalPart1 = "portLocalPartMulti1";
    static final String multiPortWsdl_portLocalPart2 = "portLocalPartMulti2";
    static final String multiPortWsdl_portLocalPart3 = "portLocalPartMulti3";
    
    /**
     * Test the getPort functionality without any composite specified.
     */
    public void testOriginalGetPort() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(otherWsdl);
        Service service = Service.create(wsdlUrl, serviceQName);
        assertNotNull(service);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        DescriptionBuilderComposite dbcInServiceDesc = DescriptionTestUtils2.getServiceDescriptionComposite(serviceDesc);
        assertNotNull(dbcInServiceDesc);
        assertEquals(Service.class, dbcInServiceDesc.getCorrespondingClass());
        // Since this is a generic Service with no overrides, there will be no WebServiceClient annotation
        WebServiceClient wsClient = dbcInServiceDesc.getWebServiceClientAnnot();
        assertNull(wsClient);

        // WSDL was specified on the create, so make sure the right one was used by checking the ports
        assertTrue("Wrong WSDL used", ClientMetadataTest.validatePort(service, otherWsdl_portLocalPart));
        
        QName portQN = new QName(namespaceURI, otherWsdl_portLocalPart);
        ClientMetadataPortSEI port = service.getPort(portQN, ClientMetadataPortSEI.class);
        assertNotNull(port);
    }
    
    /**
     * Specify a sparse composite on a getPort call
     */
    public void testGetPortWithComposite() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(otherWsdl);
        Service service = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();

        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        assertNull(ServiceDelegate.getPortMetadata());
        ServiceDelegate.setPortMetadata(sparseComposite);
        assertNull(ServiceDelegate.getServiceMetadata());
        assertSame(sparseComposite, ServiceDelegate.getPortMetadata());
        QName portQN = new QName(namespaceURI, otherWsdl_portLocalPart);
        ClientMetadataPortSEI port = service.getPort(portQN, ClientMetadataPortSEI.class);
        assertNotNull(port);
        assertNull(ServiceDelegate.getPortMetadata());
        
        EndpointDescription epDescArray[] = serviceDesc.getEndpointDescriptions();
        assertEquals(1, epDescArray.length);
        DescriptionBuilderComposite epDBC = epDescArray[0].getDescriptionBuilderComposite();
        assertNotNull(epDBC);
        assertNotSame(sparseComposite, epDBC);
        assertSame(sparseComposite, epDBC.getSparseComposite(serviceDelegate));
    }
    
    /**
     * Do multiple getPorts on the same service specifiying different sparse composite.  Verify that
     * the sparse composite overwrites the previous one.
     */
    public void testMulitpleGetPortSameService() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(otherWsdl);
        Service service = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();

        // Do the first getPort on the first Service
        DescriptionBuilderComposite sparseComposite1 = new DescriptionBuilderComposite();
        assertNull(ServiceDelegate.getPortMetadata());
        ServiceDelegate.setPortMetadata(sparseComposite1);
        assertNull(ServiceDelegate.getServiceMetadata());
        assertSame(sparseComposite1, ServiceDelegate.getPortMetadata());
        QName portQN = new QName(namespaceURI, otherWsdl_portLocalPart);
        ClientMetadataPortSEI port1 = service.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray1[] = serviceDesc.getEndpointDescriptions();
        assertEquals(1, epDescArray1.length);
        DescriptionBuilderComposite epDBC1 = epDescArray1[0].getDescriptionBuilderComposite();
        assertNotNull(epDBC1);
        assertNotSame(sparseComposite1, epDBC1);
        assertSame(sparseComposite1, epDBC1.getSparseComposite(serviceDelegate));
        
        // Do a second getPort for the same port on the same service using a different composite
        DescriptionBuilderComposite sparseComposite2 = new DescriptionBuilderComposite();
        assertNull(ServiceDelegate.getPortMetadata());
        ServiceDelegate.setPortMetadata(sparseComposite2);
        assertNull(ServiceDelegate.getServiceMetadata());
        assertSame(sparseComposite2, ServiceDelegate.getPortMetadata());

        ClientMetadataPortSEI port2 = service.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray2[] = serviceDesc.getEndpointDescriptions();
        assertEquals(1, epDescArray2.length);
        DescriptionBuilderComposite epDBC2 = epDescArray2[0].getDescriptionBuilderComposite();
        assertNotNull(epDBC2);
        assertNotSame(sparseComposite2, epDBC2);
        assertSame(sparseComposite2, epDBC1.getSparseComposite(serviceDelegate));
        // Verify the previous sparse composite was overwritten for this delegate
        assertNotSame(sparseComposite1, epDBC1.getSparseComposite(serviceDelegate));
    }
    
    /**
     * Test multiple getPorts using different composites on different services.  Validate the composite
     * is for each service delegate is different.  Note that we have to install a configuration
     * factory that will cause the ServiceDescriptions to be cached; the default factory will
     * not.
     */
    public void testGetPortDifferentServices() {
        
        try {
            ClientMetadataTest.installCachingFactory();
            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            QName portQN = new QName(namespaceURI, otherWsdl_portLocalPart);
            URL wsdlUrl = ClientMetadataTest.getWsdlURL(otherWsdl);

            // Create the first service 
            Service service1 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
            assertNull(ServiceDelegate.getServiceMetadata());
            ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();
            
            // Do the first getPort on the first Service
            DescriptionBuilderComposite sparseComposite1 = new DescriptionBuilderComposite();
            assertNull(ServiceDelegate.getPortMetadata());
            ServiceDelegate.setPortMetadata(sparseComposite1);
            assertNull(ServiceDelegate.getServiceMetadata());
            assertSame(sparseComposite1, ServiceDelegate.getPortMetadata());
            ClientMetadataPortSEI port1 = service1.getPort(portQN, ClientMetadataPortSEI.class);
            EndpointDescription epDescArray1[] = serviceDesc1.getEndpointDescriptions();
            assertEquals(1, epDescArray1.length);
            DescriptionBuilderComposite epDBC1 = epDescArray1[0].getDescriptionBuilderComposite();
            assertNotNull(epDBC1);
            assertNotSame(sparseComposite1, epDBC1);
            assertSame(sparseComposite1, epDBC1.getSparseComposite(serviceDelegate1));
            
            // Create the second service 
            Service service2 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
            assertNull(ServiceDelegate.getServiceMetadata());
            ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();

            // Do the getPort on the second Service
            DescriptionBuilderComposite sparseComposite2 = new DescriptionBuilderComposite();
            assertNull(ServiceDelegate.getPortMetadata());
            ServiceDelegate.setPortMetadata(sparseComposite2);
            assertNull(ServiceDelegate.getServiceMetadata());
            assertSame(sparseComposite2, ServiceDelegate.getPortMetadata());
            ClientMetadataPortSEI port2 = service2.getPort(portQN, ClientMetadataPortSEI.class);
            EndpointDescription epDescArray2[] = serviceDesc2.getEndpointDescriptions();
            assertEquals(1, epDescArray2.length);
            DescriptionBuilderComposite epDBC2 = epDescArray2[0].getDescriptionBuilderComposite();
            assertNotNull(epDBC2);
            assertNotSame(sparseComposite2, epDBC2);
            
            // Since we installed a caching configuration factory above, the ServiceDescriptions
            // should match for the two service delegates.  The EndpointDesc and the composite
            // in the EndpointDesc should be the same.  The sparse composite should be unique to
            // each service delegate.
            assertNotSame(serviceDelegate1, serviceDelegate2);
            assertSame(serviceDesc1, serviceDesc2);
            assertSame(epDBC1, epDBC2);
            assertSame(epDescArray1[0], epDescArray2[0]);
            assertNotSame(sparseComposite1, sparseComposite2);
            assertSame(sparseComposite1, epDBC1.getSparseComposite(serviceDelegate1));
            assertSame(sparseComposite2, epDBC2.getSparseComposite(serviceDelegate2));
        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    
    /**
     * Test doing GET_PORT on seperate ports under the same service.  They should have unique
     * EndpointDesriptions and the sparse composites should be unique to the service delegate and
     * endpoint.
     */
    public void testMultiplePortsSameService() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        QName portQN2 = new QName(namespaceURI, multiPortWsdl_portLocalPart2);

        Service service = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNull(ServiceDelegate.getServiceMetadata());
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();

        // Do the first getPort on the first Service
        DescriptionBuilderComposite sparseComposite1 = new DescriptionBuilderComposite();
        ServiceDelegate.setPortMetadata(sparseComposite1);
        assertNull(ServiceDelegate.getServiceMetadata());
        assertSame(sparseComposite1, ServiceDelegate.getPortMetadata());
        ClientMetadataPortSEI port1 = service.getPort(portQN1, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray1[] = serviceDesc.getEndpointDescriptions();
        assertEquals(1, epDescArray1.length);
        DescriptionBuilderComposite epDBC1 = epDescArray1[0].getDescriptionBuilderComposite();
        assertNotNull(epDBC1);
        assertNotSame(sparseComposite1, epDBC1);
        assertSame(sparseComposite1, epDBC1.getSparseComposite(serviceDelegate));
        
        // Do a second getPort for a different port on the same service using a different composite
        DescriptionBuilderComposite sparseComposite2 = new DescriptionBuilderComposite();
        assertNull(ServiceDelegate.getPortMetadata());
        ServiceDelegate.setPortMetadata(sparseComposite2);
        assertNull(ServiceDelegate.getServiceMetadata());
        assertSame(sparseComposite2, ServiceDelegate.getPortMetadata());
        ClientMetadataPortSEI port2 = service.getPort(portQN2, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray2[] = serviceDesc.getEndpointDescriptions();
        assertEquals(2, epDescArray2.length);
        EndpointDescription epdPort1 = serviceDesc.getEndpointDescription(portQN1);
        EndpointDescription epdPort2 = serviceDesc.getEndpointDescription(portQN2);
        assertNotNull(epdPort1);
        assertNotNull(epdPort2);
        assertNotSame(epdPort1, epdPort2);
        
        DescriptionBuilderComposite epDBC2 = epdPort2.getDescriptionBuilderComposite();
        assertNotNull(epDBC2);
        
        assertSame(epDescArray1[0], epdPort1);
        assertNotSame(epDBC1, epDBC2);
        
        assertSame(sparseComposite2, epDBC2.getSparseComposite(serviceDelegate));
        assertNotSame(sparseComposite2, epDBC1.getSparseComposite(serviceDelegate));
        
        assertSame(sparseComposite1, epDBC1.getSparseComposite(serviceDelegate));
        assertNotSame(sparseComposite1, epDBC2.getSparseComposite(serviceDelegate));
    }

    /**
     * Validate setting a prefered port when creating the service results in a particular
     * port being returned on the getPort(Class) call.
     */
    public void testPreferredPort() {
        // Without setting a prefered port, the first port in the WSDL should
        // be returned.
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        
        Service service1 = Service.create(wsdlUrl, serviceQName);
        ClientMetadataPortSEI port1 = service1.getPort(ClientMetadataPortSEI.class);
        assertNotNull(port1);
        // Get the endpoint address to verify which port we got.  Note that the WSDL is setup
        // so that the endpoint address ends with the name of the port for testing.
        BindingProvider bindingProvider1 = (BindingProvider) port1;
        Map<String, Object> requestContext1 = bindingProvider1.getRequestContext();
        String endpointAddress1 = (String) requestContext1.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        assertNotNull(endpointAddress1);
        // FIXME: We should get the first port in the WSDL, but that isn't working
        // Depending on the JDK in use, the 2nd or 3rd port is returned
//        assertTrue(endpointAddress.endsWith(multiPortWsdl_portLocalPart1));
        
        // Set a prefered port and create the service
        QName portQN2 = new QName(namespaceURI, multiPortWsdl_portLocalPart2);
        DescriptionBuilderComposite sparseComposite2 = new DescriptionBuilderComposite();
        sparseComposite2.setPreferredPort(portQN2);
        ServiceDelegate.setServiceMetadata(sparseComposite2);
        Service service2 = Service.create(wsdlUrl, serviceQName);
        ClientMetadataPortSEI port2 = service2.getPort(ClientMetadataPortSEI.class);
        BindingProvider bindingProvider2 = (BindingProvider) port2;
        Map<String, Object> requestContext2 = bindingProvider2.getRequestContext();
        String endpointAddress2 = (String) requestContext2.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        assertNotNull(endpointAddress2);
        assertTrue(endpointAddress2.endsWith(multiPortWsdl_portLocalPart2));
        
    }
    /**
     * Validate setting a prefered port when creating the service results in a particular
     * port being returned on the getPort(Class) call.  The ServiceDesc in this case 
     * are cached.
     */
    public void testPreferredPortCachedService() {
        try {
            ClientMetadataTest.installCachingFactory();

            // Without setting a prefered port, the first port in the WSDL should
            // be returned.
            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
            QName portQN1 = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
            
            Service service1 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
            ServiceDescription svcDesc1 = serviceDelegate1.getServiceDescription();
            ClientMetadataPortSEI port1 = service1.getPort(ClientMetadataPortSEI.class);
            assertNotNull(port1);
            // Get the endpoint address to verify which port we got.  Note that the WSDL is setup
            // so that the endpoint address ends with the name of the port for testing.
            BindingProvider bindingProvider1 = (BindingProvider) port1;
            Map<String, Object> requestContext1 = bindingProvider1.getRequestContext();
            String endpointAddress1 = (String) requestContext1.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            assertNotNull(endpointAddress1);
            // FIXME: We should get the first port in the WSDL, but that isn't working
            // Depending on the JDK in use, the 2nd or 3rd port is returned
//            assertTrue(endpointAddress.endsWith(multiPortWsdl_portLocalPart1));
            
            // Set a prefered port and create the service
            QName portQN2 = new QName(namespaceURI, multiPortWsdl_portLocalPart2);
            DescriptionBuilderComposite sparseComposite2 = new DescriptionBuilderComposite();
            sparseComposite2.setPreferredPort(portQN2);
            ServiceDelegate.setServiceMetadata(sparseComposite2);
            Service service2 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
            ServiceDescription svcDesc2 = serviceDelegate2.getServiceDescription();
            assertNotSame(service1, service2);
            assertNotSame(serviceDelegate1, serviceDelegate2);
            assertSame(svcDesc1, svcDesc2);
            
            ClientMetadataPortSEI port2 = service2.getPort(ClientMetadataPortSEI.class);
            BindingProvider bindingProvider2 = (BindingProvider) port2;
            Map<String, Object> requestContext2 = bindingProvider2.getRequestContext();
            String endpointAddress2 = (String) requestContext2.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            assertNotNull(endpointAddress2);
            assertTrue(endpointAddress2.endsWith(multiPortWsdl_portLocalPart2));
            
            // Create a third service without a composite and make sure the previous composite
            // setting of preferred port doesn't affect this one.
            Service service3 = Service.create(wsdlUrl, serviceQName);
            ServiceDelegate serviceDelegate3 = DescriptionTestUtils2.getServiceDelegate(service3);
            ServiceDescription svcDesc3 = serviceDelegate3.getServiceDescription();
            assertNotSame(service2, service3);
            assertNotSame(serviceDelegate1, serviceDelegate3);
            assertNotSame(serviceDelegate2, serviceDelegate3);
            assertSame(svcDesc1, svcDesc3);
            
            ClientMetadataPortSEI port3 = service3.getPort(ClientMetadataPortSEI.class);
            assertNotNull(port3);
            BindingProvider bindingProvider3 = (BindingProvider) port3;
            Map<String, Object> requestContext3 = bindingProvider3.getRequestContext();
            String endpointAddress3 = (String) requestContext3.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            assertNotNull(endpointAddress1);
            // FIXME: We should get the first port in the WSDL, but that isn't working
            // Depending on the JDK in use, the 2nd or 3rd port is returned
//            assertTrue(endpointAddress.endsWith(multiPortWsdl_portLocalPart1));

        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
    
    /**
     * Validate enabling MTOM when creating the service results ports created under that service
     * have MTOM enabled.
     */
    public void testEnableMTOM() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        sparseComposite.setIsMTOMEnabled(true);
        ServiceDelegate.setServiceMetadata(sparseComposite);
        Service service = Service.create(wsdlUrl, serviceQName);
        ClientMetadataPortSEI port = service.getPort(ClientMetadataPortSEI.class);
        assertNotNull(port);
        // Verify that MTOM is enabled on this port.
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding binding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue(binding.isMTOMEnabled());
        
        // Verify that specific ports under this service also have MTOM enabled
        QName port1QN = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        ClientMetadataPortSEI port1 = service.getPort(port1QN, ClientMetadataPortSEI.class);
        SOAPBinding binding1 = ((SOAPBinding) ((BindingProvider) port1).getBinding());
        assertTrue(binding1.isMTOMEnabled());
        
        QName port2QN = new QName(namespaceURI, multiPortWsdl_portLocalPart2);
        ClientMetadataPortSEI port2 = service.getPort(port2QN, ClientMetadataPortSEI.class);
        SOAPBinding binding2 = ((SOAPBinding) ((BindingProvider) port2).getBinding());
        assertTrue(binding2.isMTOMEnabled());
    }
    
    /**
     * Validate enabling MTOM when creating the service results ports created under that service
     * have MTOM enabled.
     */
    public void testEnableMTOMFromServiceDBC() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        Map<String, Boolean> seiToMTOM = new HashMap<String, Boolean>();
        seiToMTOM.put(ClientMetadataPortSEI.class.getName(), true);
        sparseComposite.getProperties().put(MDQConstants.SEI_MTOM_ENABLEMENT_MAP, seiToMTOM);
        ServiceDelegate.setServiceMetadata(sparseComposite);
        Service service = Service.create(wsdlUrl, serviceQName);
        ClientMetadataPortSEI port = service.getPort(ClientMetadataPortSEI.class);
        assertNotNull(port);
        // Verify that MTOM is enabled on this port.
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding binding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue(binding.isMTOMEnabled());
        
        // Verify that specific ports under this service also have MTOM enabled
        QName port1QN = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        ClientMetadataPortSEI port1 = service.getPort(port1QN, ClientMetadataPortSEI.class);
        SOAPBinding binding1 = ((SOAPBinding) ((BindingProvider) port1).getBinding());
        assertTrue(binding1.isMTOMEnabled());
        
        QName port2QN = new QName(namespaceURI, multiPortWsdl_portLocalPart2);
        ClientMetadataPortSEI port2 = service.getPort(port2QN, ClientMetadataPortSEI.class);
        SOAPBinding binding2 = ((SOAPBinding) ((BindingProvider) port2).getBinding());
        assertTrue(binding2.isMTOMEnabled());
    }
    
    /**
     * Validate enabling MTOM when creating the service results ports created under that service
     * have MTOM enabled.
     */
    public void testDisableMTOMFromServiceDBC() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
        DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
        Map<String, Boolean> seiToMTOM = new HashMap<String, Boolean>();
        seiToMTOM.put(ClientMetadataPortSEI.class.getName(), false);
        sparseComposite.getProperties().put(MDQConstants.SEI_MTOM_ENABLEMENT_MAP, seiToMTOM);
        ServiceDelegate.setServiceMetadata(sparseComposite);
        Service service = Service.create(wsdlUrl, serviceQName);
        ClientMetadataPortSEI port = service.getPort(ClientMetadataPortSEI.class);
        assertNotNull(port);
        // Verify that MTOM is enabled on this port.
        BindingProvider bindingProvider = (BindingProvider) port;
        SOAPBinding binding = (SOAPBinding) bindingProvider.getBinding();
        assertTrue(!binding.isMTOMEnabled());
        
        // Verify that specific ports under this service also have MTOM enabled
        QName port1QN = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
        ClientMetadataPortSEI port1 = service.getPort(port1QN, ClientMetadataPortSEI.class);
        SOAPBinding binding1 = ((SOAPBinding) ((BindingProvider) port1).getBinding());
        assertTrue(!binding1.isMTOMEnabled());
        
        QName port2QN = new QName(namespaceURI, multiPortWsdl_portLocalPart2);
        ClientMetadataPortSEI port2 = service.getPort(port2QN, ClientMetadataPortSEI.class);
        SOAPBinding binding2 = ((SOAPBinding) ((BindingProvider) port2).getBinding());
        assertTrue(!binding2.isMTOMEnabled());
    }
    
    
    /**
     * Validate enabling MTOM when creating the service results in enablement only
     * for that service delegate, and not a different service delegate referencing
     * the same service.
     */
    public void testEnableMTOMCachedService() {
        try {
            ClientMetadataTest.installCachingFactory();

            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);

            DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
            sparseComposite.setIsMTOMEnabled(true);
            ServiceDelegate.setServiceMetadata(sparseComposite);
            Service service1 = Service.create(wsdlUrl, serviceQName);
            
            Service service2 = Service.create(wsdlUrl, serviceQName);
            
            QName portQN = new QName(namespaceURI, multiPortWsdl_portLocalPart1);
            ClientMetadataPortSEI port1 = service1.getPort(portQN, ClientMetadataPortSEI.class);
            ClientMetadataPortSEI port2 = service2.getPort(portQN, ClientMetadataPortSEI.class);

            SOAPBinding binding1 = ((SOAPBinding) ((BindingProvider) port1).getBinding());
            assertTrue(binding1.isMTOMEnabled());
            
            SOAPBinding binding2 = ((SOAPBinding) ((BindingProvider) port2).getBinding());
            assertFalse(binding2.isMTOMEnabled());

        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
        
    /**
     * Validate enabling the setting of properties on the BindingProvider based
     * on a map of properties supplied to the sparse composite.
     */
    public void testSetBindingProperties() {
        try {
            ClientMetadataTest.installCachingFactory();

            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
            QName portQN = new QName(namespaceURI, multiPortWsdl_portLocalPart1);

            DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
            sparseComposite.setIsMTOMEnabled(true);
            Map<String, Map<String, Object>> allBindingProps = new HashMap<String, Map<String,Object>>();
            String key = ClientMetadataPortSEI.class.getName() + ":" + portQN.toString();
            Map<String, Object> bindingProps = new HashMap<String, Object>();
            bindingProps.put("customProperty", "someValue");
            allBindingProps.put(key, bindingProps);
            sparseComposite.getProperties().put(MDQConstants.BINDING_PROPS_MAP, allBindingProps);

            ServiceDelegate.setServiceMetadata(sparseComposite);
            Service service1 = Service.create(wsdlUrl, serviceQName);
            ClientMetadataPortSEI port1 = service1.getPort(portQN, ClientMetadataPortSEI.class);
            BindingProvider bp = (BindingProvider) port1;
            assertNotNull(bp.getRequestContext().get("customProperty"));
            assertEquals(bp.getRequestContext().get("customProperty"), "someValue");

        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }

    /**
     * This will validate that the properties to be set on the BindingProvider, via
     * the sparse composite, can be correctly scoped at the port level.
     */
    public void testNoSetBindingProperties() {
        try {
            ClientMetadataTest.installCachingFactory();

            QName serviceQName = new QName(namespaceURI, svcLocalPart);
            URL wsdlUrl = ClientMetadataTest.getWsdlURL(multiPortWsdl);
            QName portQN = new QName(namespaceURI, multiPortWsdl_portLocalPart2);

            DescriptionBuilderComposite sparseComposite = new DescriptionBuilderComposite();
            sparseComposite.setIsMTOMEnabled(true);
            Map<String, Map<String, Object>> allBindingProps = new HashMap<String, Map<String,Object>>();
            String key = ClientMetadataPortSEI.class.getName() + ":" + portQN.toString();
            Map<String, Object> bindingProps = new HashMap<String, Object>();
            bindingProps.put("customProperty", "someValue");
            allBindingProps.put(key, bindingProps);
            sparseComposite.getProperties().put(MDQConstants.BINDING_PROPS_MAP, allBindingProps);

            ServiceDelegate.setServiceMetadata(sparseComposite);
            Service service1 = Service.create(wsdlUrl, serviceQName);
            ClientMetadataPortSEI port1 = service1.getPort(new QName(namespaceURI, multiPortWsdl_portLocalPart1), 
                                                           ClientMetadataPortSEI.class);
            BindingProvider bp = (BindingProvider) port1;
            assertNull(bp.getRequestContext().get("customProperty"));

        } finally {
            ClientMetadataTest.restoreOriginalFactory();
        }
    }
}

@WebService(name="EchoMessagePortType", targetNamespace="http://description.jaxws.axis2.apache.org")
interface ClientMetadataPortSEI {
    public String echoMessage(String string);
}
