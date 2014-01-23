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

import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import java.net.URL;

import test.EchoString;
import test.ObjectFactory;

/**
 * Validate that Dispatch operation resolution occurs correctly for JAXB elements.
 * 
 * Note that this test uses the JAXB-generated artifacts from test-resources/xsd/echo.xsd and that the WSDL
 * used in this test, test-resources/wsdl/DispatchOperationResolutionJAXB.wsdl, was crafted to match the
 * generated JAXB artifacts.  That is why the WSDL and operations are different between this test and the other
 * DispatchOperationResolution*.java tests
 */
public class DispatchOperationResolutionJAXBTest extends InterceptableClientTestCase {
    URL wsdlDocumentLocation = getClass().getResource("/wsdl/DispatchOperationResolutionJAXB.wsdl");
    QName serviceQName = new QName("http://test/", "EchoService");
    QName portQName = new QName("http://test/", "EchoServicePort");

    public void testJAXBResolution() {
        try {
            ObjectFactory factory = new ObjectFactory();
            EchoString request = factory.createEchoString();         
            request.setInput("Operation resolution JAXB test");
            JAXBContext jbc = JAXBContext.newInstance(EchoString.class);
    
            Service service = Service.create(wsdlDocumentLocation, serviceQName);
            Dispatch<Object> dispatch = service.createDispatch(portQName, jbc, Service.Mode.PAYLOAD);
    
            // The InterceptableClientTestCase invoke will return the "request" as the response.
            EchoString response = (EchoString) dispatch.invoke(request);
        } catch (Exception e) {
            fail("Caught exception: " + e);
        }
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should not be null", opDesc);
        // Make sure we get the correct Operation Description
        OperationDescription expectedOperationDescription = expectedOperationDescription(requestMC, "echoString");
        assertSame("Wrong operation description returned", expectedOperationDescription, opDesc);
    }
    
    private OperationDescription expectedOperationDescription(MessageContext requestMC, String operationName) {
        EndpointDescription endpointDescription = requestMC.getEndpointDescription();
        EndpointInterfaceDescription endpointInterfaceDescription = endpointDescription.getEndpointInterfaceDescription();
        QName operationQName = new QName("http://test/", operationName);
        OperationDescription expectedOperationDescription = endpointInterfaceDescription.getOperation(operationQName)[0];
        return expectedOperationDescription;
    }

}
