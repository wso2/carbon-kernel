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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.apache.axis2.util.XMLUtils;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ProviderTests  extends XMLTestCase {
    Provider provider = new Provider();
    W3CEndpointReference w3cEpr;
    String address = "http://localhost:8080/jaxws-samples/services/EchoService";
    QName interfaceName = new QName("http://org/apache/axis2/jaxws/samples/echo/", "EchoServicePortType", "tns");
    QName serviceName = new QName("http://org/apache/axis2/jaxws/samples/echo/", "EchoService", "tns");
    QName portName = new QName("http://org/apache/axis2/jaxws/samples/echo/", "EchoServicePort", "tns");
    String wsdlDocumentLocation = getClass().getResource("/wsdl/Echo.wsdl").toExternalForm();
    
    List<Element> metadata = new ArrayList<Element>();
    List<Element> referenceParameters = new ArrayList<Element>();
    List<Element> elements = new ArrayList<Element>();
    Map<QName,String> attributes = new HashMap<QName,String>();
    
    /* Test the new createW3CEndpointReference method added for JAX-WS 2.2 */
    
    public void test22CreateW3CEndpointReferenceAllNull() {
        try {
            w3cEpr = provider.createW3CEndpointReference(null, null, null, null, null, null, null, null, null);
        } catch (IllegalStateException e) {
            // Expected Exception - address, serviceName and portName are all null
            return;
        }
        fail("Did not catch expected IllegalStateException");
    }
       
    public void test22CreateW3CEndpointReferenceServiceAndPortName() {
        try {
            w3cEpr = provider.createW3CEndpointReference(null, null, serviceName, portName, null, null, null, null, null);
        } catch (IllegalStateException e) {
            // Expected Exception - address property is null and the serviceName and portName do not specify a valid endpoint published by the same Java EE application. 
            return;
        }
        fail("Did not catch expected IllegalStateException");
    }
    
    public void test22CreateW3CEndpointReferenceAddressPortName() {
        try {
            w3cEpr = provider.createW3CEndpointReference(address, null, null, portName, null, null, null, null, null);
        } catch (IllegalStateException e) {
            // Expected Exception - serviceName service is null and the portName is NOT null
            return;
        }
        fail("Did not catch expected IllegalStateException");
    }
    
    public void test22CreateW3CEndpointReferenceServiceNotInWSDL() {
        try {
            w3cEpr = provider.createW3CEndpointReference(address, interfaceName, new QName("UnknownService"), portName, null, wsdlDocumentLocation, null, null, null);
        } catch (IllegalStateException e) {
            // Expected Exception - serviceName is NOT null and is not present in the specified WSDL
            return;
        }
        fail("Did not catch expected IllegalStateException");
    }
    
    public void test22CreateW3CEndpointReferencePortNameNotInWSDL() {
        try {
            w3cEpr = provider.createW3CEndpointReference(address, interfaceName, serviceName, new QName("UnknownPort"), null, wsdlDocumentLocation, null, null, null);
        } catch (IllegalStateException e) {
            // Expected Exception - portName port is not null and it is not present in serviceName service in the WSDL
            return;
        }
        fail("Did not catch expected IllegalStateException");
    }
    
    public void test22CreateW3CEndpointReferenceInvalidWSDL() {
        try {
            w3cEpr = provider.createW3CEndpointReference(address, interfaceName, serviceName, portName, null, getClass().getResource("/wsdl/Invalid.wsdl").toExternalForm(), null, null, null);
        } catch (IllegalStateException e) {
            // Expected Exception - wsdlDocumentLocation is NOT null and does not represent a valid WSDL
            return;
        }
        fail("Did not catch expected IllegalStateException");
    }
    
    public void test22CreateW3CEndpointReferenceMissingWSDLLocationNamespace() {
        try {
            w3cEpr = provider.createW3CEndpointReference(address, null, new QName("EchoService"), new QName("EchoServicePort"), null, wsdlDocumentLocation, null, null, null);
            System.out.println(w3cEpr);
        } catch (IllegalStateException e) {
            // Expected Exception - wsdlDocumentLocation is NOT null but wsdli:wsdlLocation's namespace name cannot be got from the available metadata
            return;
        }
        fail("Did not catch expected IllegalStateException");
    }
    
    public void test22CreateW3CEndpointReferenceOnlyAddress() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        try {
            w3cEpr = provider.createW3CEndpointReference(address, null, null, null, null, null, null, null, null);
            
            String expectedEPR = "<EndpointReference xmlns=\"http://www.w3.org/2005/08/addressing\">" +
                                     "<Address>http://localhost:8080/jaxws-samples/services/EchoService</Address>"+
                                 "</EndpointReference>";
            
            assertXMLEqual(expectedEPR, w3cEpr.toString());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }
    
    public void test22CreateW3CEndpointReference() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        try {
            Document doc = XMLUtils.newDocument();
            
            // Create metadata
            Element metadata1 = doc.createElementNS("http://test.com", "test:testMetadata1");
            metadata.add(metadata1);
            Element metadata2 = doc.createElementNS("http://test.com", "test:testMetadata2");
            metadata.add(metadata2);
            
            // Create reference parameters
            Element key = doc.createElementNS("http://example.com/fabrikam", "fabrikam:CustomerKey");
            key.appendChild(doc.createTextNode("123456789"));
            referenceParameters.add(key);
            Element cart = doc.createElementNS("http://example.com/fabrikam", "fabrikam:ShoppingCart");
            cart.appendChild(doc.createTextNode("ABCDEFG"));
            referenceParameters.add(cart);
            
            // Create elements
            Element element1 = doc.createElementNS("http://test.com", "test:testElement1");
            elements.add(element1);
            Element element2 = doc.createElementNS("http://test.com", "test:testElement2");
            elements.add(element2);
            
            // Create attributes
            attributes.put(new QName("http://test.com", "attribute1", "test"), "value1");
            attributes.put(new QName("http://test.com", "attribute2", "test"), "value2");
            
            w3cEpr = provider.createW3CEndpointReference(address, interfaceName, serviceName, portName, metadata, wsdlDocumentLocation, referenceParameters, elements, attributes);
            
            // Cannot put EPR in an external file since absolute WSDL location cannot be hard coded
            String expectedEPR = "<EndpointReference test:attribute1=\"value1\" test:attribute2=\"value2\" xmlns=\"http://www.w3.org/2005/08/addressing\" xmlns:test=\"http://test.com\">" +
                                     "<Address>http://localhost:8080/jaxws-samples/services/EchoService</Address>" +
                                     "<ReferenceParameters xmlns:fabrikam=\"http://example.com/fabrikam\">" +
                                         "<fabrikam:CustomerKey>123456789</fabrikam:CustomerKey>" +
                                         "<fabrikam:ShoppingCart>ABCDEFG</fabrikam:ShoppingCart>" +
                                     "</ReferenceParameters>" +
                                     "<Metadata xmlns:wsam=\"http://www.w3.org/2007/05/addressing/metadata\" xmlns:tns=\"http://org/apache/axis2/jaxws/samples/echo/\" xmlns:wsdli=\"http://www.w3.org/ns/wsdl-instance\" wsdli:wsdlLocation=\"http://org/apache/axis2/jaxws/samples/echo/ " + wsdlDocumentLocation + "\">" +
                                         "<wsam:ServiceName EndpointName=\"EchoServicePort\">tns:EchoService</wsam:ServiceName>" +
                                         "<wsam:InterfaceName>tns:EchoServicePortType</wsam:InterfaceName>" +
                                         "<test:testMetadata1/>" +
                                         "<test:testMetadata2/>" +
                                     "</Metadata>" +
                                     "<test:testElement1/>" +
                                     "<test:testElement2/>" +
                                 "</EndpointReference>";
            
            assertXMLEqual(expectedEPR, w3cEpr.toString());
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }
}
