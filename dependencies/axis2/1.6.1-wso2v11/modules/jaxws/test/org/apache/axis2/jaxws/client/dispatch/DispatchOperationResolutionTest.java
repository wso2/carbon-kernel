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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.client.InterceptableClientTestCase;
import org.apache.axis2.jaxws.client.TestClientInvocationController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.DescriptionTestUtils2;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.spi.BindingProvider;
import org.apache.axis2.jaxws.spi.ServiceDelegate;

import javax.xml.namespace.QName;
import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Response;
import javax.xml.ws.Service;
import javax.xml.ws.soap.MTOMFeature;
import javax.xml.ws.soap.SOAPBinding;

import java.net.URL;
import java.util.concurrent.Future;

/**
 * Validate the operation resolution of a Dispatch client.  For a Dispatch client, operation resolution is done
 * by parsing the outbound message in order to determine the operation so that the Action can be set correctly.
 * Prior to this functionality being added, Dispatch clients always used a generic Anonymous outbound operation, resulting
 * in an Action that was not correct for the operation being invoked.  That meant that the client had to set the Action on 
 * the outound Request Context to get the correct Action set.
 * 
 * With the new functionality, an outbound Dispatch message will be parsed to determine the operation and thus the correct
 * Action UNLESS
 * - A property has been set indicating that Resolution should not be done
 * - A non-null Action has been set on the Request Context
 * 
 * Note that these tests are all Doc/Lit/Wrapped based.  There are additional tests to validate other styles such
 * as Doc/Lit/Bare.  Those tests simply test the operation resolution using that style; they do not validate
 * the flag settings and other aspects since that doesn't change based on the style in use.
 * @see DispatchOperationResolutionDocLitBareTest
 * @see DispatchOperationResolutionJAXBTest
 */
public class DispatchOperationResolutionTest extends InterceptableClientTestCase {
    URL wsdlDocumentLocation = getClass().getResource("/wsdl/DispatchOperationResolution.wsdl");
    QName serviceQName = new QName("http://org/apache/axis2/jaxws/samples/echo/", "EchoService");
    QName portQName = new QName("http://org/apache/axis2/jaxws/samples/echo/", "EchoServicePort");

    private static final String echoBodyContent_PAYLOAD =
        "<ns1:echoOperation xmlns:ns1=\"http://org/apache/axis2/jaxws/samples/echo\">" + 
        "<ns1:echoStringInput xmlns=\"http://org/apache/axis2/jaxws/samples/echo\">HELLO THERE!!!</ns1:echoStringInput>" + 
        "</ns1:echoOperation>";
    
    private static final String echoBodyContent_NoResolution_PAYLOAD =
        "<ns1:echoNoOperation xmlns:ns1=\"http://org/apache/axis2/jaxws/samples/echo\">" + 
        "<ns1:echoStringInput xmlns=\"http://org/apache/axis2/jaxws/samples/echo\">HELLO THERE!!!</ns1:echoStringInput>" + 
        "</ns1:echoNoOperation>";

    private static final String echoBodyContent_MESSAGE = 
        "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>"
        + echoBodyContent_PAYLOAD
        + "</soapenv:Body></soapenv:Envelope>";

    private static final String echoBodyContent2_PAYLOAD =
        "<ns1:echoOperation2 xmlns:ns1=\"http://org/apache/axis2/jaxws/samples/echo\">" + 
        "<ns1:echoStringInput xmlns=\"http://org/apache/axis2/jaxws/samples/echo\">HELLO THERE!!!</ns1:echoStringInput>" + 
        "</ns1:echoOperation2>";
    
    /**
     * Validate that operation resolution does not happen for dynamic ports
     */
    public void testDynamicPort_NoResolution() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        QName dynamicPortQN = new QName("http://org/apache/axis2/jaxws/samples/echo/", "DynamicPort");
        service.addPort(dynamicPortQN, SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch<String> dispatch = service.createDispatch(dynamicPortQN, String.class, Service.Mode.PAYLOAD);

        String result = dispatch.invoke(echoBodyContent_PAYLOAD);

        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null for dynamic ports", opDesc);
    }
    
    /**
     * Validate Dispatch<String>, Synchronous request/response, Mode.PAYLOAD determines the operation correctly.
     */
    public void testOperationResolution_Sync_String_PAYLOAD() {
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

    /**
     * Validate Dispatch<String>, Synchronous request/response, Mode.PAYLOAD determines the second operation correctly.
     * 
     * Most other tests validate the "echoOperation" operation; this test simply validates that the second WSDL operation
     * "echoOperation2" is also resolved correctly.
     */
    public void testOperation2Resolution_Sync_String_PAYLOAD() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent2_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should not be null", opDesc);
        // Make sure we get the correct Operation Description
        OperationDescription expectedOperationDescription = expectedOperationDescription(requestMC, "echoOperation2");
        assertSame("Wrong operation description returned", expectedOperationDescription, opDesc);
    }

    /**
     * Validate Dispatch<String>, Synchronous request/response,Mode.MESSAGE determines the operation correctly.
     */
    public void testOperationResolution_Sync_String_MESSAGE() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.MESSAGE);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent_MESSAGE);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should not be null", opDesc);
        // Make sure we get the correct Operation Description
        OperationDescription expectedOperationDescription = expectedOperationDescription(requestMC);
        assertSame("Wrong operation description returned", expectedOperationDescription, opDesc);
    }
    
    /**
     * Validate that if the operation in the outbound dispatch message can not be resolved to a WSDL operation, the 
     * operation is null.
     * 
     * Note that this seems like it should be an error.  However, it was not an error in previous releases since we did no
     * operation resolution on the outbound dispatch message.  For backwards compatability, it is still not considered an error.
     */
    public void testNoOperationResolution() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent_NoResolution_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null because operation in message could not be resolved to WSDL operations", opDesc);
    }
    
    /**
     * Validate Dispatch<String>, Oneway request, Mode.PAYLOAD determines the operation correctly.
     */
    public void testOperationResolution_Oneway_String_PAYLOAD() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        dispatch.invokeOneWay(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should not be null", opDesc);
        // Make sure we get the correct Operation Description
        OperationDescription expectedOperationDescription = expectedOperationDescription(requestMC);
        assertSame("Wrong operation description returned", expectedOperationDescription, opDesc);
    }

    /**
     * Validate Dispatch<String>, Async request/response, Mode.PAYLOAD determines the operation correctly.
     */
    public void testOperationResolution_Async_String_PAYLOAD() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        Response<String> response = dispatch.invokeAsync(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should not be null", opDesc);
        // Make sure we get the correct Operation Description
        OperationDescription expectedOperationDescription = expectedOperationDescription(requestMC);
        assertSame("Wrong operation description returned", expectedOperationDescription, opDesc);
    }

    /**
     * Validate Dispatch<String>, Async-Callback request/response, Mode.PAYLOAD determines the operation correctly.
     */
    public void testOperationResolution_AsyncCallback_String_PAYLOAD() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        AsyncHandler asyncHandler = new TestAsyncHandler();
        Future<?> future = dispatch.invokeAsync(echoBodyContent_PAYLOAD, asyncHandler);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should not be null", opDesc);
        // Make sure we get the correct Operation Description
        OperationDescription expectedOperationDescription = expectedOperationDescription(requestMC);
        assertSame("Wrong operation description returned", expectedOperationDescription, opDesc);
    }
    
    /**
     * Validate that operation resolution is not performed if the AxisConfiguration property is set to indicate it should not be.
     */
    public void testOperationResolutionDisabled_AxisConfiguration_false() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        
        setAxisConfigParameter(service, Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION, "false");
        
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null", opDesc);
    }

    /**
     * Validate that operation resolution is not performed if the AxisConfiguration property is set to indicate it should not be.
     * Note the value used in the property value is mixed case.
     */
    public void testOperationResolutionDisabled_AxisConfiguration_False() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        setAxisConfigParameter(service, Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION, "False");

        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null", opDesc);
    }
    
    /**
     * Validate that setting the property on System has no effect on disabling operation resolution.  The property should only be set on 
     * the AxisConfiguration (to affect all Dispatch clients) or on the Request Context (to affect that Dispatch client).
     */
    public void testOperationResolutionDisabled_SystemProperty_NoEffect() {
        try {
            System.setProperty(Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION, "false");
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

        } finally {
            System.clearProperty(Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION);
        }
    }
    
    /**
     * Validate that doing operation resolution does not impact using a WebServiceFeature such as MTOM on the
     * createDispatch.
     */
    public void testOperationResolutionAndMTOMFeature() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        MTOMFeature feature = new MTOMFeature(true);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.MESSAGE, feature);
        assertNotNull(dispatch);
        
        String result = dispatch.invoke(echoBodyContent_MESSAGE);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should not be null", opDesc);
        // Make sure we get the correct Operation Description
        OperationDescription expectedOperationDescription = expectedOperationDescription(requestMC);
        assertSame("Wrong operation description returned", expectedOperationDescription, opDesc);
        assertTrue("MTOM should be enabled via the MTOMFeature.", requestMC.getMessage().isMTOMEnabled());
    }

    /**
     * Validate that if a Action URI is set, the operation resolution does not occur.
     */
    public void testOperationResolutionDisabled_RequestContext_Action_Set() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,"http://should.not.resolve.operation");

        String result = dispatch.invoke(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null", opDesc);
    }

    /**
     * Validate that even if the AxisConfiguration property is explicitly set to "true" to enable operation resolution,
     * if a Action URI is also set, then resolution does not occur.
     */
    public void testOperationResolutionDisabled_AxisConfiguration_True_Request_Context_Action_Set() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        setAxisConfigParameter(service, Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION, "true");

        
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY,"http://should.not.resolve.operation");

        String result = dispatch.invoke(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null", opDesc);
    }
    
    /**
     * Validate that if the property to use a SOAP action is set, but the value for the SOAP action is null
     * then operation resolution occurs.  That is necessary so the correct value for the soap action can be set.
     */
    public void testOperationResolutionDisabled_RequestContext_Action_null() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, null);

        String result = dispatch.invoke(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNotNull("OpDesc from request MC should be null", opDesc);
    }
    /**
     * Validate that if the property to use a SOAP action is set, and the value for the SOAP action is the 
     * empty string, then operation resolution does NOT occur.  The empty string value is acceptable as a action value.
     */
    public void testOperationResolutionDisabled_RequestContext_Action_EmptyString() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
        dispatch.getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, "");

        String result = dispatch.invoke(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
        OperationDescription opDesc = requestMC.getOperationDescription();
        assertNull("OpDesc from request MC should be null", opDesc);
    }

    /**
     * Validate that operation resolution can be disabled by setting a property on the request context.
     */
    public void testOperationResolutionDisabled_RequestContext_Property() {
        Service service = Service.create(wsdlDocumentLocation, serviceQName);
        Dispatch<String> dispatch = service.createDispatch(portQName, String.class, Service.Mode.PAYLOAD);
        assertNotNull(dispatch);
        
        dispatch.getRequestContext().put(Constants.DISPATCH_CLIENT_OUTBOUND_RESOLUTION, "false");

        String result = dispatch.invoke(echoBodyContent_PAYLOAD);
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext requestMC = ic.getRequestMessageContext();
        
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
    
    private void setAxisConfigParameter(Service service, String key, String value) {
        ServiceDelegate delegate = DescriptionTestUtils2.getServiceDelegate(service);
        ServiceDescription svcDesc = delegate.getServiceDescription();
        AxisConfiguration axisConfig = svcDesc.getAxisConfigContext().getAxisConfiguration();
        Parameter parameter = new Parameter(key, value);
        try {
            axisConfig.addParameter(parameter);
        } catch (AxisFault e) {
            fail("Unable to set Parameter on AxisConfig due to exception " + e);
        }
    }

    class TestAsyncHandler implements AsyncHandler {
        public void handleResponse(Response response) {
            // The Test Invocation Controller will not call the async handler, so this method
            // does not need to do anything.
        }
        
    }
}
