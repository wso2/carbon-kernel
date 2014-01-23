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

package org.apache.axis2.jaxws.providerapi;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.dispatchers.GenericProviderDispatcher;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.Provider;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;

/**
 * 
 */
public class GenericOperationProviderTest extends TestCase {
    
    public void testGenericHTTPBindingOperation() {
        // The HTTP binding supports a generic operation for WSDL-less endpoints.
        ServiceDescription serviceDesc = DescriptionFactory.createServiceDescription(HTTPBindingProviderImpl.class);
        assertNotNull(serviceDesc);
        EndpointDescription endpointDesc = serviceDesc.getEndpointDescriptions_AsCollection().iterator().next();
        assertNotNull(endpointDesc);
        AxisService axisSvc = endpointDesc.getAxisService();
        assertNotNull(axisSvc);
        
        EndpointInterfaceDescription interfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(interfaceDesc);

        // There should be a single OpDesc with a single AxisOperation with a specific name
        OperationDescription opDescs[] = interfaceDesc.getOperations();
        assertNotNull(opDescs);
        assertEquals(1, opDescs.length);
        AxisOperation axisOperation = opDescs[0].getAxisOperation();
        assertNotNull(axisOperation);
        assertEquals(EndpointInterfaceDescription.JAXWS_NOWSDL_PROVIDER_OPERATION_NAME, axisOperation.getName().getLocalPart());
        
        // Now verify that the special dispather can find this operation
        GenericProviderDispatcher dispatcher = new GenericProviderDispatcher();
        MessageContext messageContext = new MessageContext();
        messageContext.setAxisService(axisSvc);
        
        try {
            // The dispatcher will not try to resolve an AxisService
            assertNull(dispatcher.findService(messageContext));
            
            // The dispatcher should find the special AxisOperation
            assertEquals(axisOperation, dispatcher.findOperation(axisSvc, messageContext));
        } catch (AxisFault e) {
            fail("Unexpected exception" + e);
        }
    }
    
    public void _testGenericSOAPBindingOperation() {
        // REVIEW: Currently generic operations are not supported for SOAP Bindings
        
        ServiceDescription serviceDesc = DescriptionFactory.createServiceDescription(SOAPBindingProviderImpl.class);
        assertNotNull(serviceDesc);
        EndpointDescription endpointDesc = serviceDesc.getEndpointDescriptions_AsCollection().iterator().next();
        assertNotNull(endpointDesc);
        AxisService axisSvc = endpointDesc.getAxisService();
        assertNotNull(axisSvc);
        
        // Since there's no WSDL, there will be no operations and no EndpointInterfaceDescription
        // because this is a SOAPBinding.
        EndpointInterfaceDescription interfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNull(interfaceDesc);
    }
    
    public void testSEIBasedEndpoint() {
        ServiceDescription serviceDesc = DescriptionFactory.createServiceDescription(SEIBasedEndpoint.class);
        assertNotNull(serviceDesc);
        EndpointDescription endpointDesc = serviceDesc.getEndpointDescriptions_AsCollection().iterator().next();
        assertNotNull(endpointDesc);
        AxisService axisSvc = endpointDesc.getAxisService();
        assertNotNull(axisSvc);
        
        EndpointInterfaceDescription interfaceDesc = endpointDesc.getEndpointInterfaceDescription();
        assertNotNull(interfaceDesc);

        // There should be a single OpDesc with a single AxisOperation based on the SEI below
        // But it should not be the special named operation and the special dispatcher should not
        // return null for operation dispatch
        OperationDescription opDescs[] = interfaceDesc.getOperations();
        assertNotNull(opDescs);
        assertEquals(1, opDescs.length);
        AxisOperation axisOperation = opDescs[0].getAxisOperation();
        assertNotNull(axisOperation);
        if (EndpointInterfaceDescription.JAXWS_NOWSDL_PROVIDER_OPERATION_NAME.equals(axisOperation.getName().getLocalPart())) {
            fail("Operation has the generic provider name");
        }
        
        // Now verify that the special dispather doesn't find the special operation
        GenericProviderDispatcher dispatcher = new GenericProviderDispatcher();
        MessageContext messageContext = new MessageContext();
        messageContext.setAxisService(axisSvc);
        
        try {
            // The dispatcher will not try to resolve an AxisService
            assertNull(dispatcher.findService(messageContext));
            
            // The dispatcher should find the special AxisOperation
            assertNull(dispatcher.findOperation(axisSvc, messageContext));
        } catch (AxisFault e) {
            fail("Unexpected exception" + e);
        }
        
    }
}


// Notice no WSDL is specified
@WebServiceProvider()
@BindingType(value=HTTPBinding.HTTP_BINDING)
@ServiceMode(value=javax.xml.ws.Service.Mode.MESSAGE)
class HTTPBindingProviderImpl implements Provider<String> {

    public String invoke(String obj) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
//Notice no WSDL is specified
@WebServiceProvider()
class SOAPBindingProviderImpl implements Provider<String> {

    public String invoke(String obj) {
        // TODO Auto-generated method stub
        return null;
    }
}

// SEI based endpoint to make sure it doesn't get get the special generic provider operation added
@WebService()
class SEIBasedEndpoint {
    public String echo() {
        return null;
    }
}
