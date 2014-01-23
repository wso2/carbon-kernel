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

import junit.framework.TestCase;

import org.apache.axis2.jaxws.description.sample.addnumbers.AddNumbersPortType;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class ServiceTests extends TestCase {
    private static String VALID_SERVICE_NAMESPACE = "http://org/test/addnumbers";
    private static String VALID_SERVICE_LOCALPART_1 = "AddNumbersService1";
    private static String VALID_SERVICE_LOCALPART_2 = "AddNumbersService2";
    private static String VALID_PORT_S1P1 = "AddNumbersPortS1P1";
    private static String VALID_PORT_S1P2 = "AddNumbersPortS1P2";
    private static String VALID_PORT_S1P3 = "AddNumbersPortS1P3";
    private static String VALID_PORT_S2P1 = "AddNumbersPortS2P1";
    private static String VALID_PORT_S2P2 = "AddNumbersPortS2P2";
    private static String VALID_PORT_S2P3 = "AddNumbersPortS2P3";
    private static String VALID_PORT_S2P4 = "AddNumbersPortS2P4";

    public void testInvalidServiceNamespace() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");
        QName invalidServiceQN = new QName("invalidServiceNamespace", VALID_SERVICE_LOCALPART_1);
        try {
            Service invalidService = Service.create(wsdlURL, invalidServiceQN);
            fail("Created service with invalid namespace");
        }
        catch (WebServiceException ex) {
            // Expected code path
        }
        catch (Exception ex) {
            fail("Caught unexpected exception " + ex.toString());
        }
    }
    public void testInvalidServiceLocalPart() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");
        QName invalidServiceQN = new QName(VALID_SERVICE_NAMESPACE, "invalidServiceName");
        try {
            Service invalidService = Service.create(wsdlURL, invalidServiceQN);
            fail("Created service with invalid namespace");
        }
        catch (WebServiceException ex) {
            // Expected code path
        }
        catch (Exception ex) {
            fail("Caught unexpected exception " + ex.toString());
        }
    }
    
    public void testValidSameService() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");
        
        QName validService1QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_1);
        Service validService1 = Service.create(wsdlURL, validService1QN);
        assertNotNull(validService1);

        QName validService2QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_1);
        Service validService2 = Service.create(wsdlURL, validService2QN);
        assertNotNull(validService2);
        
    }

    public void testValidMultiServices() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");
        
        QName validService1QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_1);
        Service validService1 = Service.create(wsdlURL, validService1QN);
        assertNotNull(validService1);

        QName validService2QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_2);
        Service validService2 = Service.create(wsdlURL, validService2QN);
        assertNotNull(validService2);
        assertNotSame(validService1, validService2);
        
    }
    
    public void testGetServiceDeclaredPorts() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");

        QName service1QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_1);
        Service service1 = Service.create(wsdlURL, service1QN);
        assertNotNull(service1);
        ServiceDelegate service1Delegate = DescriptionTestUtils2.getServiceDelegate(service1);
        assertNotNull (service1Delegate);
        ServiceDescription service1Desc = service1Delegate.getServiceDescription();
        assertNotNull(service1Desc);
        List<QName> service1PortsList = service1Desc.getPorts(service1Delegate);
        assertNotNull(service1PortsList);
        assertEquals(3, service1PortsList.size());
        Iterator<QName> service1PortIterator = service1.getPorts();
        assertQNameIteratorSameAsList(service1PortIterator, service1PortsList);

        QName service2QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_2);
        Service service2 = Service.create(wsdlURL, service2QN);
        assertNotNull(service2);
        ServiceDelegate service2Delegate = DescriptionTestUtils2.getServiceDelegate(service2);
        assertNotNull (service2Delegate);
        ServiceDescription service2Desc = service2Delegate.getServiceDescription();
        assertNotNull(service2Desc);
        List<QName> service2PortsList = service2Desc.getPorts(service2Delegate);
        assertNotNull(service2PortsList);
        assertEquals(4, service2PortsList.size());
        Iterator<QName> service2PortIterator = service2.getPorts();
        assertQNameIteratorSameAsList(service2PortIterator, service2PortsList);
    }

    public void testGetServiceAddedPorts() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");

        QName service1QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_1);
        Service service1 = Service.create(wsdlURL, service1QN);
        assertNotNull(service1);
        ServiceDelegate service1Delegate = DescriptionTestUtils2.getServiceDelegate(service1);
        assertNotNull (service1Delegate);
        ServiceDescription service1Desc = service1Delegate.getServiceDescription();
        assertNotNull(service1Desc);
        List<QName> service1PortsList = service1Desc.getPorts(service1Delegate);
        assertNotNull(service1PortsList);
        assertEquals(3, service1PortsList.size());
        service1.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS1P1"), null, null);
        service1.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS1P2"), null, null);
        service1PortsList = service1Desc.getPorts(service1Delegate);
        assertEquals(5, service1PortsList.size());
        Iterator<QName> service1PortIterator = service1.getPorts();
        assertQNameIteratorSameAsList(service1PortIterator, service1PortsList);

        QName service2QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_2);
        Service service2 = Service.create(wsdlURL, service2QN);
        assertNotNull(service2);
        ServiceDelegate service2Delegate = DescriptionTestUtils2.getServiceDelegate(service2);
        assertNotNull (service2Delegate);
        ServiceDescription service2Desc = service2Delegate.getServiceDescription();
        assertNotNull(service2Desc);
        List<QName> service2PortsList = service2Desc.getPorts(service2Delegate);
        assertNotNull(service2PortsList);
        assertEquals(4, service2PortsList.size());
        service2.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS2P1"), null, null);
        service2.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS2P2"), null, null);
        service2PortsList = service2Desc.getPorts(service2Delegate);
        assertEquals(6, service2PortsList.size());
        Iterator<QName> service2PortIterator = service2.getPorts();
        assertQNameIteratorSameAsList(service2PortIterator, service2PortsList);
    }
    
    public void testGetServiceDeclaredPortsAfterGetPort() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");

        QName service1QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_1);
        Service service1 = Service.create(wsdlURL, service1QN);
        assertNotNull(service1);
        ServiceDelegate service1Delegate = DescriptionTestUtils2.getServiceDelegate(service1);
        assertNotNull (service1Delegate);
        ServiceDescription service1Desc = service1Delegate.getServiceDescription();
        assertNotNull(service1Desc);
        List<QName> service1PortsList = service1Desc.getPorts(service1Delegate);
        assertNotNull(service1PortsList);
        assertEquals(3, service1PortsList.size());
        AddNumbersPortType addNumbersPortS1P1 = service1.getPort(new QName(VALID_SERVICE_NAMESPACE, VALID_PORT_S1P1), AddNumbersPortType.class); 
        service1PortsList = service1Desc.getPorts(service1Delegate);
        assertEquals(3, service1PortsList.size());
        AddNumbersPortType addNumbersPortS1P3 = service1.getPort(new QName(VALID_SERVICE_NAMESPACE, VALID_PORT_S1P3), AddNumbersPortType.class); 
        assertEquals(3, service1PortsList.size());
        service1.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS1P1"), null, null);
        service1.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS1P2"), null, null);
        service1PortsList = service1Desc.getPorts(service1Delegate);
        assertEquals(5, service1PortsList.size());
        Iterator<QName> service1PortIterator = service1.getPorts();
        assertQNameIteratorSameAsList(service1PortIterator, service1PortsList);
        
        QName service2QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_2);
        Service service2 = Service.create(wsdlURL, service2QN);
        assertNotNull(service2);
        ServiceDelegate service2Delegate = DescriptionTestUtils2.getServiceDelegate(service2);
        assertNotNull (service2Delegate);
        ServiceDescription service2Desc = service2Delegate.getServiceDescription();
        assertNotNull(service2Desc);
        List<QName> service2PortsList = service2Desc.getPorts(service2Delegate);
        assertNotNull(service2PortsList);
        assertEquals(4, service2PortsList.size());
        AddNumbersPortType addNumbersPortS2P1 = service2.getPort(new QName(VALID_SERVICE_NAMESPACE, VALID_PORT_S2P1), AddNumbersPortType.class); 
        service2PortsList = service2Desc.getPorts(service2Delegate);
        assertEquals(4, service2PortsList.size());
        AddNumbersPortType addNumbersPortS2P3 = service2.getPort(new QName(VALID_SERVICE_NAMESPACE, VALID_PORT_S2P3), AddNumbersPortType.class); 
        assertEquals(4, service2PortsList.size());
        AddNumbersPortType addNumbersPortS2P4 = service2.getPort(new QName(VALID_SERVICE_NAMESPACE, VALID_PORT_S2P4), AddNumbersPortType.class); 
        assertEquals(4, service2PortsList.size());
        service2.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS2P1"), null, null);
        service2.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS2P2"), null, null);
        service2PortsList = service2Desc.getPorts(service2Delegate);
        assertEquals(6, service2PortsList.size());
        Iterator<QName> service2PortIterator = service2.getPorts();
        assertQNameIteratorSameAsList(service2PortIterator, service2PortsList);
    }
    
    public void testDynamicService() {
        QName service1QN = new QName(VALID_SERVICE_NAMESPACE, "DynamicService1");
        Service service1 = Service.create(null, service1QN);
        assertNotNull(service1);
        ServiceDelegate service1Delegate = DescriptionTestUtils2.getServiceDelegate(service1);
        assertNotNull (service1Delegate);
        ServiceDescription service1Desc = service1Delegate.getServiceDescription();
        assertNotNull(service1Desc);
        List<QName> service1PortsList = service1Desc.getPorts(service1Delegate);
        assertNotNull(service1PortsList);
        assertTrue(service1PortsList.isEmpty());
        assertEquals(0, service1PortsList.size());
        AddNumbersPortType addNumbersPortS1P1 = service1.getPort(new QName(VALID_SERVICE_NAMESPACE, "dynamicPortS1P1"), AddNumbersPortType.class); 
        service1PortsList = service1Desc.getPorts(service1Delegate);
        assertEquals(1, service1PortsList.size());
        AddNumbersPortType addNumbersPortS1P3 = service1.getPort(new QName(VALID_SERVICE_NAMESPACE, "dynamicPortS1P2"), AddNumbersPortType.class); 
        service1PortsList = service1Desc.getPorts(service1Delegate);
        assertEquals(2, service1PortsList.size());
        service1.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS1P1"), null, null);
        service1.addPort(new QName(VALID_SERVICE_NAMESPACE, "addedPortS1P2"), null, null);
        service1PortsList = service1Desc.getPorts(service1Delegate);
        assertEquals(4, service1PortsList.size());
        Iterator<QName> service1PortIterator = service1.getPorts();
        assertQNameIteratorSameAsList(service1PortIterator, service1PortsList);
    }
    
    public void testCreateDispatchWSDL() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");

        QName service1QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_1);
        Service service1 = Service.create(wsdlURL, service1QN);
        assertNotNull(service1);
        // Create Dispatch should work on a WSDL declared port prior to a getPort
        // and again after the call to getPort
        QName validPortQName = new QName(VALID_SERVICE_NAMESPACE, VALID_PORT_S1P1);
        Dispatch<String> dispatch = service1.createDispatch(validPortQName, String.class, null);
        assertNotNull(dispatch);
        AddNumbersPortType addNumbersPortS1P1 = service1.getPort(validPortQName, AddNumbersPortType.class);
        assertNotNull(addNumbersPortS1P1);
        
        // Create Dispatch should NOT work on a dynamic port that has not been added yet
        // but should work after it has been added
        QName addedPort = new QName(VALID_SERVICE_NAMESPACE, "addedPortS1P1");
        try {
            Dispatch<String> dispatch2 = service1.createDispatch(addedPort, String.class, null);
            fail("Create Dispatch on non-existant port should have thrown an exception");
        }
        catch (WebServiceException ex) {
            // Expected path
        }
        catch (Exception ex) {
            fail("Unexpected exception thrown " + ex.toString());
        }
        service1.addPort(addedPort, null, null);
        Dispatch<String> dispatch2 = service1.createDispatch(addedPort, String.class, null);
        assertNotNull(dispatch2);

    }
    
    public void testCreateDispatchNoWSDL() {
        
        // Note that this test is intentionally using the same names as the WSDL test, even though no WSDL is
        // provided.  This is to verify that using the same names in the abscense of WSDL doesn't cause any
        // issues.
        
        QName service1QN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_1);
        Service service1 = Service.create(service1QN);
        assertNotNull(service1);
        // Create Dispatch should NOT work on a dynamic port prior to a getPort
        // but should work after the getPort causes the port to be added.
        QName validPortQName = new QName(VALID_SERVICE_NAMESPACE, VALID_PORT_S1P1);
        try {
            Dispatch<String> dispatch = service1.createDispatch(validPortQName, String.class, null);
            fail("Create Dispatch on non-existant port should have thrown and exception");
        }
        catch (WebServiceException ex) {
            // Expected path
        }
        catch (Exception ex) {
            fail("Unexpected exception thrown " + ex.toString());
        }
        AddNumbersPortType addNumbersPortS1P1 = service1.getPort(validPortQName, AddNumbersPortType.class);
        Dispatch<String> dispatch = service1.createDispatch(validPortQName, String.class, null);
        assertNotNull(dispatch);

        // Create Dispatch should NOT work on a dynamic port that has not been added yet
        // but should work after it has been added
        QName addedPort = new QName(VALID_SERVICE_NAMESPACE, "addedPortS1P1");
        try {
            Dispatch<String> dispatch2 = service1.createDispatch(addedPort, String.class, null);
            fail("Create Dispatch on non-existant port should have thrown an exception");
        }
        catch (WebServiceException ex) {
            // Expected path
        }
        catch (Exception ex) {
            fail("Unexpected exception thrown " + ex.toString());
        }
        service1.addPort(addedPort, null, null);
        Dispatch<String> dispatch2 = service1.createDispatch(addedPort, String.class, null);
        assertNotNull(dispatch2);
    }
    
    private void assertQNameIteratorSameAsList(Iterator<QName> iterator, List<QName> list) {
        int iteratorSize = 0;
        for (QName iteratorElement = null; iterator.hasNext(); ) { 
            iteratorElement = iterator.next();
            iteratorSize++;
            assertTrue(list.contains(iteratorElement));
        }
        assertEquals(list.size(), iteratorSize);
    }
}
