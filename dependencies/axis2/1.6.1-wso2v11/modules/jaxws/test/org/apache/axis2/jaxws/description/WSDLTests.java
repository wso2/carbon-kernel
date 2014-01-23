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

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import java.net.URL;

/**
 * Tests building a ServiceDescription using WSDL and the JAXWS Service API.  
 * Note that a ServiceDescription is built when a javax.xml.ws.Service is created.  Since that is the actual API 
 * that should be used, this test will create Service objects and then use introspection
 * to check the underlying ServiceDelegate which contains the ServiceDescription.
 */
public class WSDLTests extends TestCase {
    
    public void testValidWSDLService() {
        Service service = null;
        ServiceDelegate serviceDelegate = null;
 
        String namespaceURI= "http://ws.apache.org/axis2/tests";
        String localPart = "EchoService";
        URL wsdlURL = DescriptionTestUtils2.getWSDLURL();
        assertNotNull(wsdlURL);
        service = Service.create(wsdlURL, new QName(namespaceURI, localPart));
        assertNotNull("Service not created", service);

        serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        assertNotNull("ServiceDelegate not created", serviceDelegate);
        
        ServiceDescription serviceDescription = serviceDelegate.getServiceDescription();
        assertNotNull("ServiceDescription not created", serviceDescription);
        
//        AxisService axisService = serviceDescription.getAxisService();
//        assertNotNull("AxisService not created", axisService);
    }
    
    public void testInvalidServiceLocalName() {
        Service service = null;
 
        String namespaceURI= "http://ws.apache.org/axis2/tests";
        String localPart = "BADEchoService";
        try {
            URL wsdlURL = DescriptionTestUtils2.getWSDLURL();
            assertNotNull(wsdlURL);
            service = Service.create(wsdlURL, new QName(namespaceURI, localPart));
            fail("Exception should have been thrown for invalid Service name");
        }
        catch (WebServiceException e) {
            // This is the expected flow; it is really more a test of ServiceDelegate that ServiceDescription
        }
    }

    public void testNullWSDLLocation() {
        Service service = null;

        String namespaceURI= "http://ws.apache.org/axis2/tests";
        String localPart = "EchoService_nullWSDL";
        service = Service.create(new QName(namespaceURI, localPart));
        assertNotNull("Service not created", service);
        
    }
    
    public void testNullServiceName() {
        Service service = null;
        
        try {
            service = Service.create(null);
            fail("Exception should have been thrown for null Service name");
        }
        catch (WebServiceException e) {
            // This is the expected flow; it is really more a test of ServiceDelegate that ServiceDescription
            // but we are verifying expected behavior.
        }
        
    }
}
