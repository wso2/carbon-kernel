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

package org.apache.axis2.jaxws.client.dispatch;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.addressing.EndpointReferenceHelper;
import org.apache.axis2.jaxws.addressing.SubmissionAddressingFeature;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReference;
import org.apache.axis2.jaxws.addressing.SubmissionEndpointReferenceBuilder;
import org.apache.axis2.jaxws.client.InterceptableClientTestCase;
import org.apache.axis2.jaxws.client.TestClientInvocationController;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.MessageContext;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

/**
 * This suite of tests is for the SubmissionAddressingFeature configuration that can
 * be used on Dispatch clients.
 */
public class DispatchSubmissionAddressingFeatureTest extends InterceptableClientTestCase {
    private static final OMFactory OMF = OMAbstractFactory.getOMFactory();
    private static final QName ELEMENT200408 =
        new QName(Submission.WSA_NAMESPACE, "EndpointReference", "wsa");
    
    private W3CEndpointReference w3cEPR;
    private SubmissionEndpointReference subEPR;
    
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        W3CEndpointReferenceBuilder w3cBuilder = new W3CEndpointReferenceBuilder();
        w3cBuilder = w3cBuilder.address("http://somewhere.com/somehow");
        w3cBuilder = w3cBuilder.serviceName(new QName("http://test", "TestService"));
        w3cBuilder = w3cBuilder.endpointName(new QName("http://test", "TestPort"));
        w3cEPR = w3cBuilder.build();
        
        SubmissionEndpointReferenceBuilder subBuilder = new SubmissionEndpointReferenceBuilder();
        subBuilder = subBuilder.address("http://somewhere.com/somehow");
        subBuilder = subBuilder.serviceName(new QName("http://test", "TestService"));
        subBuilder = subBuilder.endpointName(new QName("http://test", "TestPort"));
        subEPR = subBuilder.build();
    }

    /*
     * Make sure SubmissionAddressing is not enabled by default.
     */
    public void testNoSubmissionAddressingFeature() {
        Service svc = Service.create(new QName("http://test", "TestService"));
        svc.addPort(new QName("http://test", "TestPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch<Source> d = svc.createDispatch(subEPR, Source.class, Service.Mode.PAYLOAD);
        
        d.invoke(null);
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        String version = (String) request.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        Boolean disabled = (Boolean) request.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        String required = (String) request.getProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
        
        assertNull(version);
        assertTrue(disabled);
        assertNull(required);
        
        org.apache.axis2.context.MessageContext axis2Request =
            request.getAxisMessageContext();
        org.apache.axis2.addressing.EndpointReference epr =
            axis2Request.getTo();
        
        assertNull(epr);
    }
    
    /*
     * Test the default configuration of the SubmissionAddressingFeature.
     */
    public void testDefaultSubmissionAddressingFeature() throws Exception {
        // Use the default feature config
        SubmissionAddressingFeature feature = new SubmissionAddressingFeature();
        
        Service svc = Service.create(new QName("http://test", "TestService"));
        svc.addPort(new QName("http://test", "TestPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch<Source> d = svc.createDispatch(subEPR, Source.class, Service.Mode.PAYLOAD, feature);
        
        d.invoke(null);
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        String version = (String) request.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        Boolean disabled = (Boolean) request.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        String required = (String) request.getProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
        
        assertEquals(Submission.WSA_NAMESPACE, version);
        assertFalse(disabled);
        assertEquals("Wrong required attribute", AddressingConstants.ADDRESSING_UNSPECIFIED, required);
        
        org.apache.axis2.context.MessageContext axis2Request =
            request.getAxisMessageContext();
        org.apache.axis2.addressing.EndpointReference epr =
            axis2Request.getTo();
        
        OMElement omElement =
            EndpointReferenceHelper.toOM(OMF, epr, ELEMENT200408, Submission.WSA_NAMESPACE);
        assertXMLEqual(subEPR.toString(), omElement.toString());
    }
    
    /*
     * Test disabling the SubmissionAddressing feature.
     */
    public void testDisabledSubmissionAddressingFeature() {
        // Set the feature to be disabled.
        SubmissionAddressingFeature feature = new SubmissionAddressingFeature(false);
                
        Service svc = Service.create(new QName("http://test", "TestService"));
        svc.addPort(new QName("http://test", "TestPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");        
        Dispatch<Source> d = svc.createDispatch(subEPR, Source.class, Service.Mode.PAYLOAD, feature);
        
        d.invoke(null);
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        String version = (String) request.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        Boolean disabled = (Boolean) request.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        String required = (String) request.getProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
        
        assertEquals(Submission.WSA_NAMESPACE, version);
        assertTrue(disabled);
        assertEquals("Wrong required attribute", AddressingConstants.ADDRESSING_UNSPECIFIED, required);
        
        org.apache.axis2.context.MessageContext axis2Request =
            request.getAxisMessageContext();
        org.apache.axis2.addressing.EndpointReference epr =
            axis2Request.getTo();
        
        assertNull(epr);
    }
    
    /*
     * Test requiring the SubmissionAddressing feature.
     */
    public void testRequiredSubmissionAddressingFeature() throws Exception {
        // Set the feature to be required
        SubmissionAddressingFeature feature = new SubmissionAddressingFeature(true, true);
                
        Service svc = Service.create(new QName("http://test", "TestService"));
        svc.addPort(new QName("http://test", "TestPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch<Source> d = svc.createDispatch(subEPR, Source.class, Service.Mode.PAYLOAD, feature);
        
        d.invoke(null);
        
        TestClientInvocationController testController = getInvocationController();
        InvocationContext ic = testController.getInvocationContext();
        MessageContext request = ic.getRequestMessageContext();
        
        String version = (String) request.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        Boolean disabled = (Boolean) request.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        String required = (String) request.getProperty(AddressingConstants.ADDRESSING_REQUIREMENT_PARAMETER);
        
        assertEquals(Submission.WSA_NAMESPACE, version);
        assertFalse(disabled);
        assertEquals("Wrong required attribute", AddressingConstants.ADDRESSING_REQUIRED, required);
        
        org.apache.axis2.context.MessageContext axis2Request =
            request.getAxisMessageContext();
        org.apache.axis2.addressing.EndpointReference epr =
            axis2Request.getTo();
        
        OMElement omElement =
            EndpointReferenceHelper.toOM(OMF, epr, ELEMENT200408, Submission.WSA_NAMESPACE);
        assertXMLEqual(subEPR.toString(), omElement.toString());
    }
    
    // Test configurations that are not allowed with the SubmissionAddressingFeature
    public void testInvalidSubmissionAddressingFeature() {
        // Use the default feature config
        SubmissionAddressingFeature feature = new SubmissionAddressingFeature();
        
        Service svc = Service.create(new QName("http://test", "TestService"));
        svc.addPort(new QName("http://test", "TestPort"), SOAPBinding.SOAP11HTTP_BINDING, "http://localhost");
        Dispatch<Source> d = svc.createDispatch(w3cEPR, Source.class, Service.Mode.PAYLOAD, feature);
        
        try {
            d.invoke(null);
            fail("An exception should have been thrown");
        }
        catch (WebServiceException wse) {
            //pass
        }
        catch (Exception e) {
            fail("The wrong exception type was thrown.");
        }
    }
}
