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


package org.apache.axis2.jaxws.description.impl;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.ServiceDescription;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceException;
import java.net.URL;

/**
 * This test validates the error checking and internal functioning of the ServiceDescription class.
 * These tests are the construction of a a ServiceDescription.  Direct tests of the functionality of
 * a ServiceDescription and other Description classes is done in WSDLDescriptionTests.
 */
public class ServiceDescriptionImplTests extends TestCase {
    private static final String namespaceURI =
            "http://org.apache.axis2.jaxws.description.ServiceDescriptionTests";
    private static final String localPart = "EchoService";
    private static final QName serviceQName = new QName(namespaceURI, localPart);

    public void testNullWSDL() {

        QName uniqueQName = new QName(namespaceURI, localPart + "_testNullWSDL");
        ServiceDescription serviceDescription =
                new ServiceDescriptionImpl(null, uniqueQName, javax.xml.ws.Service.class);
        assertNotNull("Service description not created with null WSDL", serviceDescription);
    }

    public void testNullServiceName() {

        try {
            ServiceDescription serviceDescription =
                    new ServiceDescriptionImpl(null, null, javax.xml.ws.Service.class);
            fail("Exception for null Service Name not thrown.");
        }
        catch (WebServiceException e) {
            // Expected path
            // TODO Message text changed
            //assertEquals("Did not receive correct exception", "Invalid Service class.  The service QName cannot be null.", e.getMessage());
        }
    }

    public void testInvalidServiceClass() {
        try {
            ServiceDescription serviceDescription =
                    new ServiceDescriptionImpl(null, serviceQName, Object.class);
            fail("Exception for invalid Service class not thrown.");
        }
        catch (WebServiceException e) {
            // Expected path
            // TODO Message text changed
            //assertEquals("Did not receive correct exception", "Invalid Service Class; must be assignable to javax.xml.ws.Service", e.getMessage());
        }
    }

    public void testNullServiceClass() {
        try {
            ServiceDescription serviceDescription =
                    new ServiceDescriptionImpl(null, serviceQName, null);
            fail("Exception for invalid Service class not thrown.");
        }
        catch (WebServiceException e) {
            // Expected path
            // TODO Message text changed
            //assertEquals("Did not receive correct exception", "Invalid Service Class; cannot be null", e.getMessage());
        }

    }

    public void testValidServiceSubclass() {
        QName uniqueQName = new QName(namespaceURI, localPart + "_testValidServiceSubclass");
        ServiceDescription serviceDescription =
                new ServiceDescriptionImpl(null, uniqueQName, ServiceSubclass.class);
        assertNotNull("Service description not created with valid Service subclass",
                      serviceDescription);
    }
}

class ServiceSubclass extends javax.xml.ws.Service {

    protected ServiceSubclass(URL wsdlDocumentLocation, QName serviceName) {
        super(wsdlDocumentLocation, serviceName);
    }
}
