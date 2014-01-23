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
import java.lang.reflect.Method;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.factory.MethodMarshallerFactory;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitWrappedMethodMarshaller;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

/**
 * Directly test the Description classes built via annotations without a WSDL file.
 * These tests focus on combinations of the following:
 * - A generic service (no annotations)
 * - A generated service (annotations)
 * - An SEI
 */
public class AnnotationDescriptionTests extends TestCase {
    
    /* 
     * ========================================================================
     * ServiceDescription Tests
     * ========================================================================
     */
    public void testCreateService() {
        String namespaceURI= "http://ws.apache.org/axis2/tests";
        String localPart = "EchoServiceAnnotated";
        Service service = Service.create(null,  new QName(namespaceURI, localPart));
        ServiceDelegate serviceDelegate = DescriptionTestUtils2.getServiceDelegate(service);
        ServiceDescription serviceDescription = serviceDelegate.getServiceDescription();
        String portLocalPart = "EchoServiceAnnotatedPort";
        QName portQName = new QName(namespaceURI, portLocalPart);
        DocumentLiteralWrappedProxy dlwp = service.getPort(portQName, DocumentLiteralWrappedProxy.class);
        
        // Validate that the Endpoint and EndpointInterface Descriptions were created correctly
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(portQName);
        assertNotNull("Endpoint not created ", endpointDescription);
        EndpointInterfaceDescription endpointInterfaceDescription = endpointDescription.getEndpointInterfaceDescription();
        assertNotNull("EndpointInterface not created", endpointInterfaceDescription);
        // Verify we can get the same endpoint description based on the SEI class
        EndpointDescription[] fromSEIClass = serviceDescription.getEndpointDescription(DocumentLiteralWrappedProxy.class);
        assertEquals(1,fromSEIClass.length);
        assertEquals(endpointDescription, fromSEIClass[0]);
        
        // Test getOperation methods parameter validation
        OperationDescription[] operationResultArray = endpointInterfaceDescription.getOperation((QName) null);
        assertNull(operationResultArray);
        operationResultArray = endpointInterfaceDescription.getOperation(new QName("",""));
        assertNull(operationResultArray);
        OperationDescription operationResult = endpointInterfaceDescription.getOperation((Method) null);
        assertNull(operationResult);
        
        // Test getOperations(): Number of methods on SEI should match number of operationDescriptions
        Method[] seiMethods = DocumentLiteralWrappedProxy.class.getMethods();
        operationResultArray = endpointInterfaceDescription.getOperations();
        assertEquals("Number of SEI methods and operations did not match", seiMethods.length, operationResultArray.length);
        
        // Test getOperation(QName)
        // Verify @WebMethod.name is used if present.  See the SEI class annotations for more information
        // The SEI has @WebMethod annotations that override the name of "invokeAsync", so none should be found.
        QName javaMethodQName = new QName("", "invokeAsync");
        operationResultArray = endpointInterfaceDescription.getOperation(javaMethodQName);
        assertNull(operationResultArray);
        // The SEI has @WebMethod annotations that name three operations "invoke"
        javaMethodQName = new QName("", "invoke");
        operationResultArray = endpointInterfaceDescription.getOperation(javaMethodQName);
        assertNotNull(operationResultArray);
        assertEquals(3, operationResultArray.length);
        
        // Test getOperation(Method)
        // Verify an SEI method lookup works
        operationResult = endpointInterfaceDescription.getOperation(seiMethods[0]);
        assertNotNull(operationResult);
        
        //Verify OneWay Method is not minimal when there is a request wrapper        
        OperationDescription oneWayOperation = endpointInterfaceDescription.getOperation("oneWayVoid");
        
        MethodMarshaller methodMarshaller = MethodMarshallerFactory.getMarshaller(oneWayOperation, false);
       
        assertEquals("Method Marshaller class is incorrect for oneWay Doc/Lit/Wrapped operation", DocLitWrappedMethodMarshaller.class, methodMarshaller.getClass());
        
        // Verify a non-SEI method returns a null
        operationResult = endpointInterfaceDescription.getOperation(this.getClass().getMethods()[0]);
        assertNull(operationResult);
    }
    
       
    /*
     * TO TEST
     * - Invalid namespace.  TNS in annotation doesn't match one from getPort
     * - Multiple service.getPort() calls with same SEI and different QName, and that serviceDesc.getEndpointDesc(Class) returns multielement array
     * - Test service.getPort(..) with same QName; should return same descrpption
     */
/*    
    public void testValidServiceGetEndpoint() {
        QName validPortQname = new QName("http://ws.apache.org/axis2/tests", "EchoPort");
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(validPortQname);
        assertNotNull("EndpointDescription should be found", endpointDescription);
    }
    
    public void testInvalidLocalpartServiceGetEndpoint() {
        QName validPortQname = new QName("http://ws.apache.org/axis2/tests", "InvalidEchoPort");
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(validPortQname);
        assertNull("EndpointDescription should not be found", endpointDescription);
    }

    public void testInvalidNamespaceServiceGetEndpoint() {
        QName validPortQname = new QName("http://ws.apache.org/axis2/tests/INVALID", "EchoPort");
        EndpointDescription endpointDescription = serviceDescription.getEndpointDescription(validPortQname);
        assertNull("EndpointDescription should not be found", endpointDescription);
    }
*/    
}
