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

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class ServiceClientTest extends UtilServerBasedTestCase implements TestConstants {

    private static final Log log = LogFactory.getLog(ServiceClientTest.class);

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;
    public static final QName operationName = new QName("echoOMElementNoResponse");
    protected boolean finish = false;


    public ServiceClientTest() {
        super(ServiceClientTest.class.getName());
    }

    public ServiceClientTest(String testName) {
        super(testName);
    }


    protected void setUp() throws Exception {
        UtilServer.start();
        service = AxisService.createService(Echo.class.getName(), UtilServer.getConfigurationContext().getAxisConfiguration());
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(new QName("Echo"));
        UtilServer.unDeployClientService();
    }

    public static OMElement createDummyOMElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://engine.axis2.apache.org", "ns1");
        OMElement method = fac.createOMElement("echoOM", omNs);
        OMElement value = fac.createOMElement("omEle", omNs);
        value.addChild(
                fac.createOMText(value, "Isaac Asimov, The Foundation Trilogy"));
        method.addChild(value);
        return method;
    }

    public void testSendRobust() throws Exception {

        EndpointReference targetEPR = new EndpointReference(
                "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//            "http://127.0.0.1:" + 5556
                        + "/axis2/services/Echo/echoOMElementNoResponse");
        OMElement payload = createDummyOMElement();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setAction("urn:echoOMElementNoResponse");
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);

        sender.sendRobust(payload);
        String value = System.getProperty("echoOMElementNoResponse");
        System.setProperty("echoOMElementNoResponse", "");
        assertEquals(value, "echoOMElementNoResponse");
    }

    public void testFireAndForget() throws Exception {

        EndpointReference targetEPR = new EndpointReference(
                "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//            "http://127.0.0.1:" + 5556
                        + "/axis2/services/Echo/echoOMElementNoResponse");
        OMElement payload = createDummyOMElement();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setAction("urn:echoOMElementNoResponse");
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);

        sender.fireAndForget(payload);
        Thread.sleep(100);
        String value = System.getProperty("echoOMElementNoResponse");
        System.setProperty("echoOMElementNoResponse", "");
        assertEquals(value, "echoOMElementNoResponse");
    }


    public void testSendRobustException() throws Exception {

        EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//                "http://127.0.0.1:" + 5556
                        + "/axis2/services/Echo/echoWithExeption");
        OMElement payload = createDummyOMElement();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setAction("urn:echoWithExeption");
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);

        try {
            sender.sendRobust(payload);
            TestCase.fail("Shoud get an exception");
        } catch (AxisFault axisFault) {
            assertEquals("Invoked the service", axisFault.getMessage());
        }

    }


}
