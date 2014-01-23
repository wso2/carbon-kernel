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

package org.apache.axis2.addressing;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;

import javax.xml.namespace.QName;

/**
 * This tests that it is possible to use the more advanced function allowed by the WS-Addressing
 * specification to redirect responses. This test sets up 2 services, MultiHopRedirectService1 and
 * MultiHopRedirectService2. MultiHopRedirectService1 echos the XML it receives to
 * MultiHopRedirectService2, adding the original ReplyTo. MultiHopRedirectService2 then echos the
 * XML to the orignal ReplyTo, setting the RelatesTo to the messageID of the original request
 * message to allow the client to correlate the message.
 * <p/>
 * The clearly only works if the client is using a separate listener (which the client in the test
 * does)
 */
public class MultiHopRedirectServiceTest extends UtilServerBasedTestCase implements TestConstants {

    EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT) +
                    "/axis2/services/MultiHopRedirectService1/echoRedirect");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService mhs1;
    protected AxisService mhs2;

    public static Test suite() {
        return getTestSetup(new TestSuite(MultiHopRedirectServiceTest.class));
    }

    protected void setUp() throws Exception {
        mhs1 = createMultiHopRedirectService1();
        mhs2 = createMultiHopRedirectService2();
        UtilServer.deployService(mhs1);
        UtilServer.deployService(mhs2);
    }

    AxisService createMultiHopRedirectService1() throws AxisFault {
        AxisService service = new AxisService("MultiHopRedirectService1");

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(
                new Parameter(Constants.SERVICE_CLASS, MultiHopRedirectService1.class.getName()));

        AxisOperation axisOp = new InOutAxisOperation(new QName("echoRedirect"));

        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + "echoRedirect", axisOp);

        return service;
    }

    AxisService createMultiHopRedirectService2() throws AxisFault {
        AxisService service = new AxisService("MultiHopRedirectService2");

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(
                new Parameter(Constants.SERVICE_CLASS, MultiHopRedirectService2.class.getName()));

        AxisOperation axisOp = new InOutAxisOperation(new QName("echoRedirect"));

        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + "echoRedirect", axisOp);

        return service;
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(new QName("MultiHopRedirectService1"));
        UtilServer.unDeployService(new QName("MultiHopRedirectService2"));
        UtilServer.unDeployClientService();
    }

    public void testMultiHop() throws Exception {
        OMElement method = createElement("this message should not cause a fault.");
        ServiceClient sender = null;

        try {
            sender = createServiceClient();
            OMElement response = sender.sendReceive(new QName("echoRedirect"), method);
            assertEquals("this message should not cause a fault.", response.getText());
        } finally {
            if (sender != null)
                sender.cleanup();
        }
    }

    private OMElement createElement(String text) {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace(Constants.AXIS2_NAMESPACE_URI + "/addressing",
                                                 "axis2addressing");
        OMElement value = fac.createOMElement("element", omNs);
        value.setText(text);

        return value;
    }

    private ServiceClient createServiceClient() throws AxisFault {
        AxisService service = new AxisService("MultiHopRedirectService1");

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(
                new Parameter(Constants.SERVICE_CLASS, MultiHopRedirectService1.class.getName()));

        AxisOperation axisOp = new OutInAxisOperation(new QName("echoRedirect"));

        axisOp.setMessageReceiver(new RawXMLINOutMessageReceiver());
        axisOp.setSoapAction(Constants.AXIS2_NAMESPACE_URI + "/" + "echoRedirect");
        axisOp.setOutputAction(Constants.AXIS2_NAMESPACE_URI + "/" + "echoRedirect");
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + "echoRedirect", axisOp);

        ConfigurationContext configcontext = UtilServer.createClientConfigurationContext();

        ServiceClient sender;
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setUseSeparateListener(true);
        options.setTimeOutInMilliSeconds(5000);
        sender = new ServiceClient(configcontext, service);
        sender.setOptions(options);
        sender.engageModule("addressing");
        return sender;
    }
}
