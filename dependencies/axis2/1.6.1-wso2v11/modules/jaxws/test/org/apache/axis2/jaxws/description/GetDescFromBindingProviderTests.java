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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.spi.BindingProvider;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.jaxws.unitTest.echo.EchoPort;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import java.lang.reflect.Proxy;
import java.net.URL;

/**
 * Test that the EndpointDescription can be gotten from
 * the Binding Provider impl class and that the AxisService can be 
 * gotten from the EndpointDesc.  Note that the BindingProvider class is NOT
 * the jaxws API one; it is the internal implementation BindingProvider class.
 */
public class GetDescFromBindingProviderTests extends TestCase {
    
    private static final String wsdlSOAPAddress = "http://localhost:6060/axis2/services/EchoService";
    
    public void testForProxy() {
        String namespaceURI = "http://ws.apache.org/axis2/tests";
        String localPart = "EchoService";

        URL wsdlURL = DescriptionTestUtils2.getWSDLURL();
        assertNotNull(wsdlURL);
        
        Service service = Service.create(wsdlURL, new QName(namespaceURI, localPart));
        assertNotNull(service);
        
        QName validPortQName = new QName(namespaceURI, "EchoPort");
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);
        
        BindingProvider bindingProvider = (BindingProvider)Proxy.getInvocationHandler(echoPort);
        ServiceDelegate serviceDelegate = bindingProvider.getServiceDelegate();
        assertNotNull(serviceDelegate);
        EndpointDescription endpointDesc = bindingProvider.getEndpointDescription();
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);
        
        // The endpoint address should match what is in the WSDL
        String endpointAddress = endpointDesc.getEndpointAddress();
        assertEquals(wsdlSOAPAddress, endpointAddress);
    }
    
    public void testForDispatch() {
        String namespaceURI = "http://ws.apache.org/axis2/tests";
        String localPart = "EchoService";

        URL wsdlURL = DescriptionTestUtils2.getWSDLURL();
        assertNotNull(wsdlURL);
        
        Service service = Service.create(wsdlURL, new QName(namespaceURI, localPart));
        assertNotNull(service);
        
        QName validPortQName = new QName(namespaceURI, "EchoPort");
        Dispatch<String> dispatch = service.createDispatch(validPortQName, String.class, null);
        assertNotNull(dispatch);
        
        BindingProvider bindingProvider = (BindingProvider) dispatch;
        ServiceDelegate serviceDelegate = bindingProvider.getServiceDelegate();
        assertNotNull(serviceDelegate);
        EndpointDescription endpointDesc = bindingProvider.getEndpointDescription();
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);
        
        // The endpoint address should match what is in the WSDL
        String endpointAddress = endpointDesc.getEndpointAddress();
        assertEquals(wsdlSOAPAddress, endpointAddress);
    }
    
    public void testForAddPort() {
        String namespaceURI = "http://ws.apache.org/axis2/tests";
        String localPart = "EchoService";

        URL wsdlURL = DescriptionTestUtils2.getWSDLURL();
        assertNotNull(wsdlURL);
        
        Service service = Service.create(wsdlURL, new QName(namespaceURI, localPart));
        assertNotNull(service);
        
        QName validPortQName = new QName(namespaceURI, "EchoPortAdded");
        service.addPort(validPortQName, null, null);
        Dispatch<String> dispatch = service.createDispatch(validPortQName, String.class, null);
        assertNotNull(dispatch);
        
        BindingProvider bindingProvider = (BindingProvider) dispatch;
        ServiceDelegate serviceDelegate = bindingProvider.getServiceDelegate();
        assertNotNull(serviceDelegate);
        EndpointDescription endpointDesc = bindingProvider.getEndpointDescription();
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);
        // The endpoint address should be null since it wasn't specified on the addPort
        String endpointAddress = endpointDesc.getEndpointAddress();
        assertNull(endpointAddress);
        
        QName validPortQName2 = new QName(namespaceURI, "EchoPortAdded2");
        final String port2EndpointAddress = "http://testAddress:6060/my/test/address"; 
        service.addPort(validPortQName2, null, port2EndpointAddress);
        dispatch = service.createDispatch(validPortQName2, String.class, null);
        assertNotNull(dispatch);
        bindingProvider = (BindingProvider) dispatch;
        endpointDesc = bindingProvider.getEndpointDescription();
        assertNotNull(endpointDesc);
        // The endpoint address should be as set on the addPort above.
        endpointAddress = endpointDesc.getEndpointAddress();
        assertEquals(port2EndpointAddress, endpointAddress);
    }
    
    public void testForProxyNoWSDL() {
        String namespaceURI = "http://ws.apache.org/axis2/tests";
        String localPart = "EchoService";

        Service service = Service.create(null, new QName(namespaceURI, localPart));
        assertNotNull(service);
        
        QName validPortQName = new QName(namespaceURI, "EchoPort");
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        assertNotNull(echoPort);
        
        BindingProvider bindingProvider = (BindingProvider)Proxy.getInvocationHandler(echoPort);
        ServiceDelegate serviceDelegate = bindingProvider.getServiceDelegate();
        assertNotNull(serviceDelegate);
        EndpointDescription endpointDesc = bindingProvider.getEndpointDescription();
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);
        // The endpoint address should be null since there was no WSDL and it hasn't been set yet
        String endpointAddress = endpointDesc.getEndpointAddress();
        assertNull(endpointAddress);
    }
    public void testForDispatchNoWSDL() {
        String namespaceURI = "http://ws.apache.org/axis2/tests";
        String localPart = "EchoService";

        Service service = Service.create(null, new QName(namespaceURI, localPart));
        assertNotNull(service);
        
        QName validPortQName = new QName(namespaceURI, "EchoPort");
        EchoPort echoPort = service.getPort(validPortQName, EchoPort.class);
        Dispatch<String> dispatch = service.createDispatch(validPortQName, String.class, null);
        assertNotNull(dispatch);
        
        BindingProvider bindingProvider = (BindingProvider) dispatch;
        ServiceDelegate serviceDelegate = bindingProvider.getServiceDelegate();
        assertNotNull(serviceDelegate);
        EndpointDescription endpointDesc = bindingProvider.getEndpointDescription();
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);
    }
    public void testForAddPortNoWSDL() {
        String namespaceURI = "http://ws.apache.org/axis2/tests";
        String localPart = "EchoService";

        Service service = Service.create(null, new QName(namespaceURI, localPart));
        assertNotNull(service);
        
        QName validPortQName = new QName(namespaceURI, "EchoPortAdded");
        service.addPort(validPortQName, null, null);
        Dispatch<String> dispatch = service.createDispatch(validPortQName, String.class, null);
        assertNotNull(dispatch);
        
        BindingProvider bindingProvider = (BindingProvider) dispatch;
        ServiceDelegate serviceDelegate = bindingProvider.getServiceDelegate();
        assertNotNull(serviceDelegate);
        EndpointDescription endpointDesc = bindingProvider.getEndpointDescription();
        assertNotNull(endpointDesc);
        AxisService axisService = endpointDesc.getAxisService();
        assertNotNull(axisService);
    }
}
