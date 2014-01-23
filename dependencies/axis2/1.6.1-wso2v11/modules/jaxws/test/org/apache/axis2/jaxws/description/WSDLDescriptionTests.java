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
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.unitTest.echo.EchoPort;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebParam.Mode;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Holder;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.net.URL;

/**
 * Directly test the Description classes built with a WSDL file.
 */
public class WSDLDescriptionTests extends TestCase {
    
    private Service service;
    private ServiceDelegate serviceDelegate;
    private ServiceDescription serviceDescription;

    private static final String VALID_PORT = "EchoPort";
    private static final String VALID_NAMESPACE = "http://ws.apache.org/axis2/tests";
    private QName validPortQName = new QName(VALID_NAMESPACE, VALID_PORT);

    
    protected void setUp() {
        // Create a new service for each test to test various valid and invalid
        // flows
        String namespaceURI = VALID_NAMESPACE;
        String localPart = "EchoService";
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL();
        assertNotNull(wsdlURL);
        service = Service.create(wsdlURL, new QName(namespaceURI, localPart));
        serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        serviceDescription = serviceDelegate.getServiceDescription();
    }
    
    /*
     * ========================================================================
     * ServiceDescription Tests
     * ========================================================================
     */
    public void testInvalidLocalpartServiceGetEndpoint() {
        QName invalidPortQname = new QName(VALID_NAMESPACE, "InvalidEchoPort");
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(invalidPortQname);
        assertNull("EndpointDescription should not be found", endpointDescription);
    }

    public void testInvalidNamespaceServiceGetEndpoint() {
        QName invalidPortQname = new QName("http://ws.apache.org/axis2/tests/INVALID", VALID_PORT);
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(invalidPortQname);
        assertNull("EndpointDescription should not be found", endpointDescription);
    }

    // ========================================================================
    // EndpointDescription Tests
    // ========================================================================
    
    public void testValidGetPortWithClass() {
        try {
            EchoPort echoPort = service.getPort(EchoPort.class);
        }
        catch (Exception e) {
            fail("Caught unexpected exception");
        }
    }
    
    public void testValidGetPortWithClassAndQName() {
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);

        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(validPortQName);
        assertNotNull(endpointDesc);
        EndpointDescription endpointDescViaSEI = serviceDescription.getEndpointDescription(EchoPort.class)[0];
        assertNotNull(endpointDescViaSEI);
        assertEquals(endpointDesc, endpointDescViaSEI);
        
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(endpointInterfaceDesc);
        Class sei = endpointInterfaceDesc.getSEIClass();
        assertEquals(EchoPort.class, sei);
    }
    
    public void testValidMultipleGetPort() {
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);

        EchoPort echoPort2 = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort2);
    }
    
    public void testInvalidMultipleGetPort() {
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);

        try {
            EchoPort2 echoPort2 = service.getPort(validPortQName, EchoPort2.class);
            fail("Should have caught exception");
        }
        catch (WebServiceException e) {
            // Expected flow
        }
        catch (Exception e) {
            fail("Caught unexpected exception" + e);
        }
        
    }
    
    public void testValidAddPort() {
        QName dispatchPortQN = new QName(VALID_NAMESPACE, "dispatchPort");
        service.addPort(dispatchPortQN, null, null);
        
        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(dispatchPortQN, serviceDelegate);
        assertNotNull(endpointDesc);
       
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNull(endpointInterfaceDesc);
    }
    
    public void testInvalidAddPortExists() {
        try {
            service.addPort(validPortQName, null, null);
            fail("Shouldn't be able to add a port that exists in the WSDL");
        }
        catch (WebServiceException e) {
            // Expected path
        }
    }
    
    public void testInvalidAddPort() {
        // Null portQname
        try {
            service.addPort(null, null, null);
            fail("Shouldn't be able to add a port with a null QName");
        }
        catch (WebServiceException e) {
            // Expected path
        }
        catch (Exception e) {
            fail("Unexpected exception caught " + e);
        }
        
        // Empty Port QName
        try {
            service.addPort(new QName("", ""), null, null);
            fail("Shouldn't be able to add an empty port QName");
        }
        catch (WebServiceException e) {
            // Expected path
        }
        catch (Exception e) {
            fail("Unexpected exception caught " + e);
        }

        // Empty binding ID
        try {
            service.addPort(new QName(VALID_NAMESPACE, "dispatchPort2"), "", null);
            fail("Shouldn't be able to add a port with an empty binding type");
        }
        catch (WebServiceException e) {
            // Expected path
        }
        catch (Exception e) {
            fail("Unexpected exception caught " + e);
        }

        // Invalid binding ID
        try {
            service.addPort(new QName(VALID_NAMESPACE, "dispatchPort3"), "InvalidBindingType", null);
            fail("Shouldn't be able to add a port with an invalid binding type");
        }
        catch (WebServiceException e) {
            // Expected path
        }
        catch (Exception e) {
            fail("Unexpected exception caught " + e);
        }

    }
    
    public void testValidAddAndGetPort() {
        QName dispatchPortQN = new QName(VALID_NAMESPACE, "dispatchPort");
        service.addPort(dispatchPortQN, null, null);
        
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);
        
        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(validPortQName);
        assertNotNull(endpointDesc);
        EndpointDescription endpointDescViaSEI = serviceDescription.getEndpointDescription(EchoPort.class)[0];
        assertNotNull(endpointDescViaSEI);
        assertEquals(endpointDesc, endpointDescViaSEI);
        
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(endpointInterfaceDesc);
        Class sei = endpointInterfaceDesc.getSEIClass();
        assertEquals(EchoPort.class, sei);

        EndpointDescription endpointDescDispatch = serviceDescription.getEndpointDescription(dispatchPortQN, serviceDelegate);
        assertNotNull(endpointDescDispatch);
       
        EndpointInterfaceDescription endpointInterfaceDescDispatch = endpointDescDispatch.getEndpointInterfaceDescription();
        assertNull(endpointInterfaceDescDispatch);
    }
    
    public void testValidCreateDispatch() {
        Dispatch<Source> dispatch = service.createDispatch(validPortQName, Source.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);

        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(validPortQName);
        assertNotNull(endpointDesc);
        // Since ther is no SEI, can not get the endpointDescription based on the sei class
        EndpointDescription[] endpointDescViaSEI = serviceDescription.getEndpointDescription(EchoPort.class);
        assertNull(endpointDescViaSEI);
        
        // There will be an EndpointInterfaceDescription because the service was created with 
        // WSDL, however there will be no SEI created because a getPort has not been done
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(endpointInterfaceDesc);
        assertNull(endpointInterfaceDesc.getSEIClass());
    }
    
    public void testValidCreateAndGet() {
        Dispatch<Source> dispatch = service.createDispatch(validPortQName, Source.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(validPortQName);
        assertNotNull(endpointDesc);
        // Since ther is no SEI, can not get the endpointDescription based on the sei class
        EndpointDescription[] endpointDescViaSEI = serviceDescription.getEndpointDescription(EchoPort.class);
        assertNull(endpointDescViaSEI);
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(endpointInterfaceDesc);
        assertNull(endpointInterfaceDesc.getSEIClass());

        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);
        // Since a getPort has been done, should now be able to get things based on the SEI
        endpointDesc = serviceDescription.getEndpointDescription(validPortQName);
        assertNotNull(endpointDesc);
        // Since ther is no SEI, can not get the endpointDescription based on the sei class
        endpointDescViaSEI = serviceDescription.getEndpointDescription(EchoPort.class);
        assertNotNull(endpointDescViaSEI);
        assertEquals(endpointDesc, endpointDescViaSEI[0]);
        endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(endpointInterfaceDesc);
        assertEquals(EchoPort.class, endpointInterfaceDesc.getSEIClass());
    }
    
    public void testValidGetAndCreate() {
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);
        Dispatch<Source> dispatch = service.createDispatch(validPortQName, Source.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);

        // Since a getPort has been done, should now be able to get things based on the SEI
        EndpointDescription endpointDesc = serviceDescription.getEndpointDescription(validPortQName);
        assertNotNull(endpointDesc);
        // Since ther is no SEI, can not get the endpointDescription based on the sei class
        EndpointDescription[] endpointDescViaSEI = serviceDescription.getEndpointDescription(EchoPort.class);
        assertNotNull(endpointDescViaSEI);
        assertEquals(endpointDesc, endpointDescViaSEI[0]);
        EndpointInterfaceDescription endpointInterfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(endpointInterfaceDesc);
        assertEquals(EchoPort.class, endpointInterfaceDesc.getSEIClass());
    }
    // TODO: Need to add a similar test with no WSDL present; note that it currently would not pass
    public void testInvalidAddAndGetPort() {
        // Should not be able to do a getPort on one that was added with addPort
        QName dispatchPortQN = new QName(VALID_NAMESPACE, "dispatchPort");
        service.addPort(dispatchPortQN, null, null);
        try {
            EchoPort echoPort = service.getPort(dispatchPortQN, EchoPort.class);
            fail("Should have thrown a WebServiceException");
        }
        catch (WebServiceException e) {
            // Expected path
        }
    }
}

// EchoPort2 is identical to EchoPort, but it should still cause an exception
// if it is used on a subsequent getPort after getPort(EchoPort.class) is done.
@WebService(name = "EchoPort", targetNamespace = "http://ws.apache.org/axis2/tests", wsdlLocation = "\\work\\apps\\eclipse\\workspace\\axis2-live\\modules\\jaxws\\test-resources\\wsdl\\WSDLTests.wsdl")
interface EchoPort2 {


    /**
     * 
     * @param text
     */
    @WebMethod(operationName = "Echo", action = "http://ws.apache.org/axis2/tests/echo")
    @RequestWrapper(localName = "Echo", targetNamespace = "http://ws.apache.org/axis2/tests", className = "org.apache.ws.axis2.tests.Echo")
    @ResponseWrapper(localName = "EchoResponse", targetNamespace = "http://ws.apache.org/axis2/tests", className = "org.apache.ws.axis2.tests.EchoResponse")
    public void echo(
        @WebParam(name = "text", targetNamespace = "", mode = Mode.INOUT)
        Holder<String> text);

}
