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
import org.apache.axis2.jaxws.spi.BindingProvider;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.unitTest.echo.EchoPort;

import javax.wsdl.Port;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class PortSelectionTests extends TestCase {
    private static String VALID_SERVICE_NAMESPACE = "http://org/test/addnumbers";
    private static String VALID_SERVICE_LOCALPART_3 = "AddNumbersService3";
    
    public void testServiceDescPortSelectionMethods() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");

        QName serviceQN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_3);
        Service service = Service.create(wsdlURL, serviceQN);
        assertNotNull(service);
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull(serviceDelegate);
        ServiceDescription serviceDesc = serviceDelegate.getServiceDescription();
        assertNotNull(serviceDesc);
        
        ServiceDescriptionWSDL serviceDescWSDL = (ServiceDescriptionWSDL) serviceDesc;
        
        Map allPorts = serviceDescWSDL.getWSDLPorts();
        assertEquals(4, allPorts.size());
        
        QName portTypeQName = new QName(VALID_SERVICE_NAMESPACE, "AddNumbersPortType"); 
        List<Port> portsUsingPortType = serviceDescWSDL.getWSDLPortsUsingPortType(portTypeQName);
        assertEquals(3, portsUsingPortType.size());

        QName otherPortTypeQName = new QName(VALID_SERVICE_NAMESPACE, "AddNumbersPortTypeOtherPT"); 
        List<Port> portsUsingOtherPortType = serviceDescWSDL.getWSDLPortsUsingPortType(otherPortTypeQName);
        assertEquals(1, portsUsingOtherPortType.size());

        List<Port> portsUsingSOAPAddress = serviceDescWSDL.getWSDLPortsUsingSOAPAddress(portsUsingPortType);
        assertEquals(2, portsUsingSOAPAddress.size());
    }
    
    public void testPortSelection() {
        // Test the Service.getPort call
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");

        QName serviceQN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_3);
        Service service = Service.create(wsdlURL, serviceQN);
        assertNotNull(service);
        AddNumbersPortType selectPort = service.getPort(AddNumbersPortType.class);
        BindingProvider bindingProvider = (BindingProvider)Proxy.getInvocationHandler(selectPort);
        EndpointDescription endpointDesc = bindingProvider.getEndpointDescription();
        QName selectedPortQName = endpointDesc.getPortQName();
        assertNotNull(selectedPortQName);
        // We expect the first port in the WSDL which uses the PortType for the SEI AddNumbersPortType to be selected
        // UNFORTUNATELY!  WSDL4J Service.getPorts(), which returns a Map of ports under the service DOES NOT return
        // them in the order defined in the WSDL.  Therefore, we can't necessarily predict which one we'll get back.
        // So, the following two lines may cause the test to fail.
//        QName expectedQName = new QName(VALID_SERVICE_NAMESPACE, "AddNumbersPortS3P2");
//        assertEquals(expectedQName, selectedPortQName);
        // So, the best we can do is just test that ONE of the valid ports is the one that was selected.
        boolean testSuccessful = false;
        QName[] validPorts = new QName[3];
        validPorts[0] = new QName(VALID_SERVICE_NAMESPACE, "AddNumbersPortS3P2");
        validPorts[1] = new QName(VALID_SERVICE_NAMESPACE, "AddNumbersPortS3P3");
        validPorts[2] = new QName(VALID_SERVICE_NAMESPACE, "AddNumbersPortS3P4");
        for (QName checkPort : validPorts) {
            if (selectedPortQName.equals(checkPort)) {
                testSuccessful = true;
                break;
            }
        }
        assertTrue(testSuccessful);
    }
    
    public void testInvalidPortSelection() {
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL("WSDLMultiTests.wsdl");

        QName serviceQN = new QName(VALID_SERVICE_NAMESPACE, VALID_SERVICE_LOCALPART_3);
        Service service = Service.create(wsdlURL, serviceQN);
        assertNotNull(service);
        // There should be no ports in the service that use this SEI!
        try {
            EchoPort selectPort = service.getPort(EchoPort.class);
            fail("Shouldn't have found a port for the given SEI!");
        }
        catch (WebServiceException ex) {
            // Expected code path
        }
        catch (Exception ex) {
            fail("Unexpected exception " + ex.toString());
        }

        
    }

}
