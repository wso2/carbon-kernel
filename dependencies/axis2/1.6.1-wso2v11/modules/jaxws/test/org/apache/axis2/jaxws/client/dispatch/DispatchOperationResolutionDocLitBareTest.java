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
package org.apache.axis2.jaxws.client.dispatch;

import org.apache.axis2.jaxws.client.InterceptableClientTestCase;
import org.apache.axis2.jaxws.client.TestClientInvocationController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;

import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import java.net.URL;

/**
 * Validate that operation resolution occurs correctly for a Doc/Lit/Bare message.
 */
public class DispatchOperationResolutionDocLitBareTest extends InterceptableClientTestCase {
    URL wsdlDocumentLocation = getClass().getResource("/wsdl/DispatchOperationResolutionDocLitBare.wsdl");
    QName serviceQName = new QName("http://org/apache/axis2/jaxws/samples/echo/", "EchoService");
    QName portQName = new QName("http://org/apache/axis2/jaxws/samples/echo/", "EchoServicePort");

    private static final String echoBodyContent_PAYLOAD =
        "<ns1:echoStringInput xmlns:ns1=\"http://org/apache/axis2/jaxws/samples/echo/\">" + 
        "The Bare Necessities" + 
        "</ns1:echoStringInput>";
    
    /**
     * A Doc/Lit/Bare message could have an empty body if the operation has no arguments.
     */
    private static final String echoBodyContent_EmptyBody_MESSAGE = 
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>"
        + "</soapenv:Body></soapenv:Envelope>";
    
    
    /**
     * A Doc/Lit/Bare message may not have an element in the Body.
     */
    private static final String echoBodyContent_NoLocalPart_MESSAGE = 
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>"
        + "NoLocalPart"
        + "</soapenv:Body></soapenv:Envelope>";


    public void testOperationResolution() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should not be null", opDesc);
        // Make sure we get the correct Operation Description
        OperationDescription expectedOperationDescription = expectedOperationDescription(requestMC);
        assertSame("Wrong operation description returned", expectedOperationDescription, opDesc);
        
    }

    public void testOperationResolution_EmptySoapBody() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.MESSAGE);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent_EmptyBody_MESSAGE);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        // Because there is no soap body to process, the runtime won't be able to determine the operation
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null", opDesc);
    }

    public void testOperationResolution_NoSoapBodyElement() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.MESSAGE);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent_NoLocalPart_MESSAGE);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        // Because there is no soap body to process, the runtime won't be able to determine the operation
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null", opDesc);
    }

    //*********************************************************************************************
    // Utility methods
    //*********************************************************************************************

    private OperationDescription expectedOperationDescription(MessageContext requestMC) {
        return expectedOperationDescription(requestMC, "echoOperation");
    }
    private OperationDescription expectedOperationDescription(MessageContext requestMC, String operationName) {
        EndpointDescription endpointDescription = requestMC.getEndpointDescription();
        EndpointInterfaceDescription endpointInterfaceDescription = endpointDescription.getEndpointInterfaceDescription();
        QName operationQName = new QName("http://org/apache/axis2/jaxws/samples/echo", operationName);
        OperationDescription expectedOperationDescription = endpointInterfaceDescription.getOperation(operationQName)[0];
        return expectedOperationDescription;
    }

}
