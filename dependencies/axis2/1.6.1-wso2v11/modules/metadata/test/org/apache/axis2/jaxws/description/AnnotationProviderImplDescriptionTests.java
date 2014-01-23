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
import org.apache.axis2.jaxws.description.builder.MDQConstants;
import org.apache.log4j.BasicConfigurator;

import javax.jws.WebService;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;

public class AnnotationProviderImplDescriptionTests extends TestCase {
    static {
        // Note you will probably need to increase the java heap size, for example
        // -Xmx512m.  This can be done by setting maven.junit.jvmargs in project.properties.
        // To change the settings, edit the log4j.property file
        // in the test-resources directory.
        BasicConfigurator.configure();
    }


    public void testBasicProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(BasicProviderTestImpl.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescriptionJava testEndpointDesc = (EndpointDescriptionJava)endpointDesc[0];
        assertNotNull(testEndpointDesc);
        assertEquals(Service.Mode.MESSAGE, testEndpointDesc.getAnnoServiceModeValue());
        assertEquals("http://www.w3.org/2003/05/soap/bindings/HTTP/",
                     testEndpointDesc.getAnnoBindingTypeValue());
        // The WebServiceProvider annotation specified no values on it.
        // TODO: When the Description package changes to provide default values when no annotation present, this may need to change.
        assertEquals("", testEndpointDesc.getAnnoWebServiceWSDLLocation());
        assertEquals("BasicProviderTestImplService",
                     testEndpointDesc.getAnnoWebServiceServiceName());
        assertEquals("BasicProviderTestImplPort", testEndpointDesc.getAnnoWebServicePortName());
        assertEquals("http://description.jaxws.axis2.apache.org/",
                     testEndpointDesc.getAnnoWebServiceTargetNamespace());
 
    }

    public void testBasicProviderWithJMS() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(BasicProviderJMSTestImpl.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescriptionJava testEndpointDesc = (EndpointDescriptionJava)endpointDesc[0];
        assertNotNull(testEndpointDesc);
        assertEquals(Service.Mode.MESSAGE, testEndpointDesc.getAnnoServiceModeValue());
        assertEquals(MDQConstants.SOAP12JMS_BINDING,
                     testEndpointDesc.getAnnoBindingTypeValue());
        
        // The WebServiceProvider annotation specified no values on it.
        // TODO: When the Description package changes to provide default values when no annotation present, this may need to change.
        assertEquals("", testEndpointDesc.getAnnoWebServiceWSDLLocation());
        assertEquals("BasicProviderJMSTestImplService",
                     testEndpointDesc.getAnnoWebServiceServiceName());
        assertEquals("BasicProviderJMSTestImplPort", testEndpointDesc.getAnnoWebServicePortName());
        assertEquals("http://description.jaxws.axis2.apache.org/",
                     testEndpointDesc.getAnnoWebServiceTargetNamespace());
    }

    public void testWebServiceProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(WebServiceProviderTestImpl.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescriptionJava testEndpointDesc = (EndpointDescriptionJava)endpointDesc[0];
        assertNotNull(testEndpointDesc);
        assertEquals(Service.Mode.PAYLOAD, testEndpointDesc.getAnnoServiceModeValue());
        assertEquals("http://www.w3.org/2003/05/soap/bindings/HTTP/",
                     testEndpointDesc.getAnnoBindingTypeValue());

        assertEquals("http://wsdl.test", testEndpointDesc.getAnnoWebServiceWSDLLocation());
        assertEquals("ProviderService", testEndpointDesc.getAnnoWebServiceServiceName());
        assertEquals("ProviderServicePort", testEndpointDesc.getAnnoWebServicePortName());
        assertEquals("http://namespace.test", testEndpointDesc.getAnnoWebServiceTargetNamespace());
    }
    
    public void testWebServiceProviderWithJMS() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(WebServiceProviderJMSTestImpl.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescriptionJava testEndpointDesc = (EndpointDescriptionJava)endpointDesc[0];
        assertNotNull(testEndpointDesc);
        assertEquals(Service.Mode.PAYLOAD, testEndpointDesc.getAnnoServiceModeValue());
        assertEquals(MDQConstants.SOAP12JMS_BINDING,
                     testEndpointDesc.getAnnoBindingTypeValue());

        assertEquals("http://wsdl.test", testEndpointDesc.getAnnoWebServiceWSDLLocation());
        assertEquals("ProviderService", testEndpointDesc.getAnnoWebServiceServiceName());
        assertEquals("ProviderServicePort", testEndpointDesc.getAnnoWebServicePortName());
        assertEquals("http://namespace.test", testEndpointDesc.getAnnoWebServiceTargetNamespace());
    }
    

    public void testDefaultServiceModeProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory
                        .createServiceDescription(DefaultServiceModeProviderTestImpl.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescriptionJava testEndpointDesc = (EndpointDescriptionJava)endpointDesc[0];
        // Default ServiceMode is PAYLOAD per JAXWS p. 80
        assertEquals(Service.Mode.PAYLOAD, testEndpointDesc.getAnnoServiceModeValue());
        assertEquals("http://schemas.xmlsoap.org/wsdl/soap/http",
                     testEndpointDesc.getAnnoBindingTypeValue());
    }

    public void testNoServiceModeProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(NoServiceModeProviderTestImpl.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescriptionJava testEndpointDesc = (EndpointDescriptionJava)endpointDesc[0];
        assertEquals(javax.xml.ws.Service.Mode.PAYLOAD, testEndpointDesc.getAnnoServiceModeValue());
        assertEquals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING,
                     testEndpointDesc.getAnnoBindingTypeValue());
    }

    public void testNoWebServiceProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        try {
            DescriptionFactory.createServiceDescription(NoWebServiceProviderTestImpl.class);
            fail("Expected WebServiceException not caught");
        }
        catch (WebServiceException e) {
            // This is the expected successful test path
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("Wrong exception caught.  Expected WebServiceException but caught " + e);
        }
    }

    public void testBothWebServiceAnnotations() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        try {
            DescriptionFactory.createServiceDescription(BothWebServiceAnnotationTestImpl.class);
            fail("Expected WebServiceException not caught");
        }
        catch (WebServiceException e) {
            // This is the expected successful test path
        }
        catch (Exception e) {
            fail("Wrong exception caught.  Expected WebServiceException but caught " + e);
        }
    }

    public void testServiceModeOnNonProvider() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(WebServiceSEITestImpl.class);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescriptionJava testEndpointDesc = (EndpointDescriptionJava)endpointDesc[0];
        assertNull(testEndpointDesc.getAnnoServiceModeValue());
        assertEquals(javax.xml.ws.soap.SOAPBinding.SOAP11HTTP_BINDING,
                     testEndpointDesc.getAnnoBindingTypeValue());
    }
}

//===============================================
//Basic Provider service implementation class
//===============================================

@ServiceMode(value = Service.Mode.MESSAGE)
@WebServiceProvider()
@BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
class BasicProviderTestImpl implements Provider<SOAPMessage> {
 public BasicProviderTestImpl() {
 }

 public SOAPMessage invoke(SOAPMessage obj) {
     return null;
 }
}

//===============================================
//Basic Provider service implementation class using SOAP/JMS
//===============================================

@ServiceMode(value = Service.Mode.MESSAGE)
@WebServiceProvider()
@BindingType(MDQConstants.SOAP12JMS_BINDING)
class BasicProviderJMSTestImpl implements Provider<SOAPMessage> {
 public BasicProviderJMSTestImpl() {
 }

 public SOAPMessage invoke(SOAPMessage obj) {
     return null;
 }
}

//===============================================
//WebServiceProvider service implementation class
//===============================================

@ServiceMode(value = Service.Mode.PAYLOAD)
@WebServiceProvider(serviceName = "ProviderService", portName = "ProviderServicePort",
                 targetNamespace = "http://namespace.test", wsdlLocation = "http://wsdl.test")
@BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
class WebServiceProviderTestImpl implements Provider<String> {
 public WebServiceProviderTestImpl() {
 }

 public String invoke(String obj) {
     return null;
 }
}

//===============================================
//WebServiceProvider service implementation class
//===============================================

@ServiceMode(value = Service.Mode.PAYLOAD)
@WebServiceProvider(serviceName = "ProviderService", portName = "ProviderServicePort",
                 targetNamespace = "http://namespace.test", wsdlLocation = "http://wsdl.test")
@BindingType(MDQConstants.SOAP12JMS_BINDING)
class WebServiceProviderJMSTestImpl implements Provider<String> {
 public WebServiceProviderJMSTestImpl() {
 }

 public String invoke(String obj) {
     return null;
 }
}

// ===============================================
// Default ServiceMode and BindingType Provider service implementation class
// Default is PAYLOAD per JAXWS p. 80
// ===============================================

@ServiceMode()
@WebServiceProvider()
@BindingType()
class DefaultServiceModeProviderTestImpl implements Provider<String> {
    public DefaultServiceModeProviderTestImpl() {
    }

    public String invoke(String obj) {
        return null;
    }
}

// ===============================================
// No ServiceMode and no BindingType Provider service implementation class
// ===============================================

@WebServiceProvider()
class NoServiceModeProviderTestImpl implements Provider<Source> {
    public NoServiceModeProviderTestImpl() {
    }

    public Source invoke(Source obj) {
        return null;
    }
}

// ===============================================
// NO WebServiceProvider Provider service implementation class
// This is an INVALID service implementation
// ===============================================

@ServiceMode(value = Service.Mode.MESSAGE)
@BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
class NoWebServiceProviderTestImpl implements Provider<SOAPMessage> {
    public NoWebServiceProviderTestImpl() {
    }

    public SOAPMessage invoke(SOAPMessage obj) {
        return null;
    }
}

// ===============================================
// BOTH WebService and WebServiceProvider Provider service implementation class
// This is an INVALID service implementation
//===============================================

@ServiceMode(value = Service.Mode.MESSAGE)
@WebService()
@WebServiceProvider()
@BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
class BothWebServiceAnnotationTestImpl implements Provider<SOAPMessage> {
    public BothWebServiceAnnotationTestImpl() {
    }

    public SOAPMessage invoke(SOAPMessage obj) {
        return null;
    }
}

// ===============================================
// WebService service implementation class; not 
// Provider-based
// ===============================================

@WebService()
class WebServiceSEITestImpl {
    public String echo(String s) {
        return "From WebServiceSEITestImpl " + "s";
    }
}

