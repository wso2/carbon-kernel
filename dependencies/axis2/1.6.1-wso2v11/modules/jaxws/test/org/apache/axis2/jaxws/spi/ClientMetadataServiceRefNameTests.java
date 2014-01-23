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

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.jaxws.description.DescriptionTestUtils2;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MDQConstants;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import java.net.URL;

import junit.framework.TestCase;

/**
 * Validate the behavior related to a ServiceRefName being specified (or not) in the client metadata.  Different 
 * ServiceRefName values should result in different instances of an AxisService.  This is so that unique policy sets
 * can be attached to AxisSerivces based on a ServiceRefName.  If the ServiceRefName is not unique then the AxisService
 * should be shared.  Also if no ServiceRefName is specified, the AxisService should be shared.
 * 
 * Note that all these tests require that a test client configuration factory which caches ServiceDescriptions be
 * installed and restored at the end.
 */
public class ClientMetadataServiceRefNameTests extends TestCase {
    static final String namespaceURI = "http://description.jaxws.axis2.apache.org";
    static final String svcLocalPart = "svcLocalPart";
    static final String originalWsdl_portLocalPart = "portLocalPart";

    
    static final String originalWsdl = "ClientMetadata.wsdl";
    
    // All tests require a test client configuration factory which caches ServiceDescriptions
    protected void setUp() throws Exception {
        ClientMetadataTest.installCachingFactory();
    }
    protected void tearDown() throws Exception {
        ClientMetadataTest.restoreOriginalFactory();
    }
    
    /**
     * Validate that multiple ports created under the same service without additional service metadata
     * specified via a setServiceMetatadata(composite) and therefore without a service ref name being specified
     * share the same description objects and Axis service.
     */
    public void testNoServiceRefNameSameService() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(originalWsdl);
        QName portQN = new QName(namespaceURI, originalWsdl_portLocalPart);


        Service service = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();

        // Get the first port under the service
        ClientMetadataPortSEI port1 = service.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray1[] = serviceDesc.getEndpointDescriptions();
        assertEquals(1, epDescArray1.length);
        EndpointDescription epDesc1 = epDescArray1[0];
        AxisService axisService1 = epDesc1.getAxisService();
        
        // Get a second port using the same service and port names
        ClientMetadataPortSEI port2 = service.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray2[] = serviceDesc.getEndpointDescriptions();
        assertEquals(1, epDescArray2.length);
        EndpointDescription epDesc2 = epDescArray2[0];
        AxisService axisService2 = epDesc2.getAxisService();
        
        // Validate that the description and AxisService objects are shared
        assertNotSame("Ports should be different", port1, port2);
        assertSame("Endpoint Descriptions should be same", epDesc1, epDesc2);
        assertSame("Axis Services should be same", axisService1, axisService2);
        
        // Validate that the service ref name property does not exist on the axis service
        assertNull("Service Ref Name should not exist", axisService1.getParameter(MDQConstants.SERVICE_REF_NAME));
    }
    
    /**
     * Validate that multiple ports created under different service instances of the same Service QName and 
     * without additional metadata and therefore without a service ref name being specified share the same 
     * description objects and Axis service.
     */
    public void testNoServiceRefNameMultipleService() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(originalWsdl);
        QName portQN = new QName(namespaceURI, originalWsdl_portLocalPart);

        // Create the first service and port
        Service service1 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
        ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();

        ClientMetadataPortSEI port1 = service1.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray1[] = serviceDesc1.getEndpointDescriptions();
        assertEquals(1, epDescArray1.length);
        EndpointDescription epDesc1 = epDescArray1[0];
        AxisService axisService1 = epDesc1.getAxisService();
        
        // Create the second service and port using the same QNames & WSDL
        Service service2 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
        ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();

        ClientMetadataPortSEI port2 = service2.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray2[] = serviceDesc2.getEndpointDescriptions();
        assertEquals(1, epDescArray2.length);
        EndpointDescription epDesc2 = epDescArray2[0];
        AxisService axisService2 = epDesc2.getAxisService();
        
        // Validate that the description and AxisService objects are shared
        assertNotSame("Service Delegate instances should be different", serviceDelegate1, serviceDelegate2);
        assertSame("Service Descriptions should be same", serviceDesc1, serviceDesc2);
        assertNotSame("Ports should be different", port1, port2);
        assertSame("Endpoint Descriptions should be same", epDesc1, epDesc2);
        assertSame("Axis Services should be same", axisService1, axisService2);
        
        // Validate that the service ref name property does not exist on the axis service
        assertNull("Service Ref Name should not exist", axisService1.getParameter(MDQConstants.SERVICE_REF_NAME));
    }
    
    /**
     * Validate that setting the service ref name property on the sparse composite when creating a service
     * causes the related AxisService to have that property and the corresponding value. 
     */
    public void testServiceRefNameParameter() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(originalWsdl);
        QName portQN = new QName(namespaceURI, originalWsdl_portLocalPart);
        String myServiceRefName = "MyServiceRefName";

        // Set the ServiceRefName as a parameter on the sparse composite that will be used to create the service
        DescriptionBuilderComposite composite = new DescriptionBuilderComposite();
        composite.getProperties().put(MDQConstants.SERVICE_REF_NAME, myServiceRefName);
        ServiceDelegate.setServiceMetadata(composite);
        Service service = Service.create(wsdlUrl, serviceQName);
        assertNotNull(service);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();

        ClientMetadataPortSEI port = service.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray[] = serviceDesc.getEndpointDescriptions();
        assertEquals(1, epDescArray.length);
        EndpointDescription epDesc = epDescArray[0];
        AxisService axisService = epDesc.getAxisService();
        
        // Validate that the service ref name property is set on the Axis Service
        Parameter serviceRefParam = axisService.getParameter(MDQConstants.SERVICE_REF_NAME);
        assertNotNull("Service Ref Paramater does not exist", serviceRefParam);
        assertEquals("Service Ref Parameter has wrong value", myServiceRefName, (String) serviceRefParam.getValue());

    }
    
    /**
     * Validate that setting different service ref names on the same WSDL Service (same service QName) results in 
     * different instances of the description hierarchy and different AxisServices each with the appropriate service
     * ref name set on the AxisService.
     */
    public void  testServiceRefNameParameterDifferentNames() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(originalWsdl);
        QName portQN = new QName(namespaceURI, originalWsdl_portLocalPart);
        String myServiceRefName1 = "MyServiceRefName1";
        String myServiceRefName2 = "MyServiceRefName2";

        // Create the first service and port
        // Set the ServiceRefName as a parameter on the sparse composite that will be used to create the service
        DescriptionBuilderComposite composite1 = new DescriptionBuilderComposite();
        composite1.getProperties().put(MDQConstants.SERVICE_REF_NAME, myServiceRefName1);
        ServiceDelegate.setServiceMetadata(composite1);
        Service service1 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
        ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();

        ClientMetadataPortSEI port1 = service1.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray1[] = serviceDesc1.getEndpointDescriptions();
        assertEquals(1, epDescArray1.length);
        EndpointDescription epDesc1 = epDescArray1[0];
        AxisService axisService1 = epDesc1.getAxisService();
        
        // Create the second service and port using the same QNames & WSDL
        DescriptionBuilderComposite composite2 = new DescriptionBuilderComposite();
        composite2.getProperties().put(MDQConstants.SERVICE_REF_NAME, myServiceRefName2);
        ServiceDelegate.setServiceMetadata(composite2);
        Service service2 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
        ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();

        ClientMetadataPortSEI port2 = service2.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray2[] = serviceDesc2.getEndpointDescriptions();
        assertEquals(1, epDescArray2.length);
        EndpointDescription epDesc2 = epDescArray2[0];
        AxisService axisService2 = epDesc2.getAxisService();
        
        // Validate that the description and AxisService objects are not shared
        assertNotSame("Service Delegate instances should be different", serviceDelegate1, serviceDelegate2);
        assertNotSame("Service Descriptions should be different", serviceDesc1, serviceDesc2);
        assertNotSame("Ports should be different", port1, port2);
        assertNotSame("Endpoint Descriptions should be different", epDesc1, epDesc2);
        assertNotSame("Axis Services should be different", axisService1, axisService2);
        
        // Validate that the correct service ref name property exists on each axis service
        assertNotNull("Service Ref Name should exist", axisService1.getParameter(MDQConstants.SERVICE_REF_NAME));
        assertEquals("Wrong Service Ref Name value", myServiceRefName1, (String) axisService1.getParameter(MDQConstants.SERVICE_REF_NAME).getValue());
        
        assertNotNull("Service Ref Name should exist", axisService2.getParameter(MDQConstants.SERVICE_REF_NAME));
        assertEquals("Wrong Service Ref Name value", myServiceRefName2, (String) axisService2.getParameter(MDQConstants.SERVICE_REF_NAME).getValue());

    }
    
    /**
     * Validate that if the same service ref name is specified on two different service creates, those services share
     * the same description objects and AxisService.
     */
    public void testMultipleServicesSameName() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(originalWsdl);
        QName portQN = new QName(namespaceURI, originalWsdl_portLocalPart);
        String myServiceRefName = "MyServiceRefName";

        // Create the first service and port
        // Set the ServiceRefName as a parameter on the sparse composite that will be used to create the service
        DescriptionBuilderComposite composite1 = new DescriptionBuilderComposite();
        composite1.getProperties().put(MDQConstants.SERVICE_REF_NAME, myServiceRefName);
        ServiceDelegate.setServiceMetadata(composite1);
        Service service1 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
        ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();

        ClientMetadataPortSEI port1 = service1.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray1[] = serviceDesc1.getEndpointDescriptions();
        assertEquals(1, epDescArray1.length);
        EndpointDescription epDesc1 = epDescArray1[0];
        AxisService axisService1 = epDesc1.getAxisService();
        
        // Create the second service and port using the same service ref name, QNames & WSDL
        DescriptionBuilderComposite composite2 = new DescriptionBuilderComposite();
        composite2.getProperties().put(MDQConstants.SERVICE_REF_NAME, myServiceRefName);
        ServiceDelegate.setServiceMetadata(composite2);
        Service service2 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
        ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();

        ClientMetadataPortSEI port2 = service2.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray2[] = serviceDesc2.getEndpointDescriptions();
        assertEquals(1, epDescArray2.length);
        EndpointDescription epDesc2 = epDescArray2[0];
        AxisService axisService2 = epDesc2.getAxisService();
        
        // Validate that the description and AxisService objects are shared
        assertNotSame("Service Delegate instances should be different", serviceDelegate1, serviceDelegate2);
        assertSame("Service Descriptions should be same", serviceDesc1, serviceDesc2);
        assertNotSame("Ports should be different", port1, port2);
        assertSame("Endpoint Descriptions should be same", epDesc1, epDesc2);
        assertSame("Axis Services should be same", axisService1, axisService2);
        
        // Validate that the service ref name property does not exist on the axis service
        assertNotNull("Service Ref Name should exist", axisService1.getParameter(MDQConstants.SERVICE_REF_NAME));
        assertEquals("Wrong Service Ref Name value", myServiceRefName, (String) axisService1.getParameter(MDQConstants.SERVICE_REF_NAME).getValue());
        
        assertNotNull("Service Ref Name should exist", axisService2.getParameter(MDQConstants.SERVICE_REF_NAME));
        assertEquals("Wrong Service Ref Name value", myServiceRefName, (String) axisService2.getParameter(MDQConstants.SERVICE_REF_NAME).getValue());

    }
    /**
     * Validate that if a service is created with a sparse composite that does not specific a service ref name, and 
     * another service is created without a sparse composite, they share metadata objects including an AxisService.
     */
    public void testMultipleServicesPropAndNoProp() {
        QName serviceQName = new QName(namespaceURI, svcLocalPart);
        URL wsdlUrl = ClientMetadataTest.getWsdlURL(originalWsdl);
        QName portQN = new QName(namespaceURI, originalWsdl_portLocalPart);

        // Create the first service and port
        // Set a sparse composite, but do not specify the service ref name property
        DescriptionBuilderComposite composite1 = new DescriptionBuilderComposite();
        ServiceDelegate.setServiceMetadata(composite1);
        Service service1 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate1 = DescriptionTestUtils2.getServiceDelegate(service1);
        ServiceDescription serviceDesc1 = serviceDelegate1.getServiceDescription();

        ClientMetadataPortSEI port1 = service1.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray1[] = serviceDesc1.getEndpointDescriptions();
        assertEquals(1, epDescArray1.length);
        EndpointDescription epDesc1 = epDescArray1[0];
        AxisService axisService1 = epDesc1.getAxisService();
        
        // Create the second service and port using the same QNames & WSDL, but no sparse composite.
        Service service2 = Service.create(wsdlUrl, serviceQName);
        ServiceDelegate serviceDelegate2 = DescriptionTestUtils2.getServiceDelegate(service2);
        ServiceDescription serviceDesc2 = serviceDelegate2.getServiceDescription();

        ClientMetadataPortSEI port2 = service2.getPort(portQN, ClientMetadataPortSEI.class);
        EndpointDescription epDescArray2[] = serviceDesc2.getEndpointDescriptions();
        assertEquals(1, epDescArray2.length);
        EndpointDescription epDesc2 = epDescArray2[0];
        AxisService axisService2 = epDesc2.getAxisService();
        
        // Validate that the description and AxisService objects are shared
        assertNotSame("Service Delegate instances should be different", serviceDelegate1, serviceDelegate2);
        assertSame("Service Descriptions should be same", serviceDesc1, serviceDesc2);
        assertNotSame("Ports should be different", port1, port2);
        assertSame("Endpoint Descriptions should be same", epDesc1, epDesc2);
        assertSame("Axis Services should be same", axisService1, axisService2);
        
        // Validate that the service ref name property does not exist on the axis service
        assertNull("Service Ref Name should not exist", axisService1.getParameter(MDQConstants.SERVICE_REF_NAME));
        assertNull("Service Ref Name should not exist", axisService2.getParameter(MDQConstants.SERVICE_REF_NAME));
    }
    
}
