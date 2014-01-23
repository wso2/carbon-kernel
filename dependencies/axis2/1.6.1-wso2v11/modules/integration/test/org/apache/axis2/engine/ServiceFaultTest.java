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

package org.apache.axis2.engine;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.FaultThrowingService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

import javax.xml.namespace.QName;

/** This test will make sure, faults thrown by services are handled properly. */
public class ServiceFaultTest extends UtilServerBasedTestCase implements TestConstants {

    protected String testResourceDir = "test-resources";
    private AxisService service;

    private QName serviceName = new QName("FaultThrowingService");
    private QName operationName = new QName("echoWithFault");

    private EndpointReference targetEPR;

    public static Test suite() {
        return getTestSetup(new TestSuite(ServiceFaultTest.class));
    }

    protected void setUp() throws Exception {
        service =
                Utils.createSimpleService(serviceName,
                                          FaultThrowingService.class.getName(),
                                          operationName);
        UtilServer.deployService(service);
        targetEPR = new EndpointReference(
                "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//                "http://127.0.0.1:5556"
                        + "/axis2/services/" + serviceName.getLocalPart() + "/" +
                        operationName.getLocalPart());
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    /**
     * Service throws a fault from the service impl, by just creating an AxisFault from all the
     * fault information.
     */
    public void testFaultThrownByServiceUsingAxisFaultOnly() {
        try {
            OMElement payload =
                    getOMElementWithText(FaultThrowingService.THROW_FAULT_AS_AXIS_FAULT);
            Options options = new Options();
            options.setTo(targetEPR);
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setExceptionToBeThrownOnSOAPFault(false);

            ConfigurationContext configContext =
                    ConfigurationContextFactory
                            .createConfigurationContextFromFileSystem(null, null);
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(options);

            String result = sender.sendReceive(payload).toString();

            assertTrue(result.indexOf("test:TestFault") > -1);
            assertTrue(result.indexOf("FaultReason</soapenv:Text>") > -1);
            assertTrue(result.indexOf("This is a test Exception") > -1);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            fail();
        }
    }

    /** Service sends out a fault, filling all the information to the message context */
    public void testFaultThrownByServiceUsingMessageContext() {
        try {
            OMElement payload =
                    getOMElementWithText(FaultThrowingService.THROW_FAULT_WITH_MSG_CTXT);
            Options options = new Options();
            options.setTo(targetEPR);
            options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setExceptionToBeThrownOnSOAPFault(false);

            ConfigurationContext configContext =
                    ConfigurationContextFactory
                            .createConfigurationContextFromFileSystem(null, null);
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(options);

            String result = sender.sendReceive(payload).toString();

            assertTrue(result.indexOf("test:TestFault") > -1);
            assertTrue(result.indexOf("FaultReason</soapenv:Text>") > -1);
            assertTrue(result.indexOf("This is a test Exception") > -1);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            fail();
        }
    }

    private OMElement getOMElementWithText(String text) {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement omElement = omFactory.createOMElement("EchoOMElement", null);
        omElement.setText(text);
        return omElement;
    }
}
