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
import org.apache.axis2.description.InOnlyAxisOperation;
import org.apache.axis2.description.OutOnlyAxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

public class AddressingSubmissionServiceTest extends UtilServerBasedTestCase
        implements TestConstants {

    protected QName transportName = new QName("http://localhost/my",
                                              "NullTransport");
    EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT) +
                    "/axis2/services/EchoXMLService/echoOMElement");

    EndpointReference replyTo = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT) +
                    "/axis2/services/RedirectReceiverService/echoOMElementResponse");

    EndpointReference faultTo = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT) +
                    "/axis2/services/RedirectReceiverService/fault");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService echoService;
    protected AxisService rrService;

    public static Test suite() {
        return getTestSetup(new TestSuite(AddressingSubmissionServiceTest.class));
    }

    protected void setUp() throws Exception {
        echoService = Utils.createSimpleService(serviceName,
                                                new RawXMLINOutMessageReceiver(),
                                                Echo.class.getName(),
                                                operationName);
        echoService.getOperation(operationName).setOutputAction("echoOMElementResponse");
        UtilServer.deployService(echoService);

        rrService = createRedirectReceiverService();
        UtilServer.deployService(rrService);
    }

    AxisService createRedirectReceiverService() throws AxisFault {
        AxisService service = new AxisService("RedirectReceiverService");

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(
                new Parameter(Constants.SERVICE_CLASS, RedirectReceiver.class.getName()));

        AxisOperation axisOp = new InOnlyAxisOperation(new QName("echoOMElementResponse"));

        axisOp.setMessageReceiver(new RawXMLINOnlyMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + "echoOMElementResponse",
                                     axisOp);

        AxisOperation axisOp2 = new InOnlyAxisOperation(new QName("fault"));

        axisOp2.setMessageReceiver(new RawXMLINOnlyMessageReceiver());
        axisOp2.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp2);
        service.mapActionToOperation(Constants.AXIS2_NAMESPACE_URI + "/" + "fault", axisOp2);

        return service;
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployService(new QName("RedirectReceiverService"));
        UtilServer.unDeployClientService();
    }

    public static AxisService createSimpleOneWayServiceforClient(QName serviceName,
                                                                 String className,
                                                                 QName opName)
            throws AxisFault {
        AxisService service = new AxisService(serviceName.getLocalPart());

        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        service.addParameter(new Parameter(Constants.SERVICE_CLASS, className));

        AxisOperation axisOp = new OutOnlyAxisOperation(opName);

        axisOp.setMessageReceiver(new RawXMLINOnlyMessageReceiver());
        axisOp.setStyle(WSDLConstants.STYLE_RPC);
        service.addOperation(axisOp);

        return service;
    }

    public void testEchoToReplyTo() throws Exception {
        OMElement method = createEchoOMElement("this message should not cause a fault.");
        ServiceClient sender = null;

        try {
            sender = createServiceClient();
            sender.fireAndForget(operationName, method);
            System.out.println("send the reqest");
            int index = 0;
            while (!RedirectReceiver.hasReceivedResponse()) {
                Thread.sleep(100);
                index++;
                if (index > 45) {
                    throw new AxisFault(
                            "Tests was failed as redirected response not received in time");
                }
            }
        } finally {
            if (sender != null)
                sender.cleanup();
        }
    }

    public void testFaultToFaultTo() throws Exception {
        OMElement method = createEchoOMElement("fault");
        ServiceClient sender = null;

        try {
            sender = createServiceClient();
            sender.fireAndForget(operationName, method);
            System.out.println("send the reqest");
            int index = 0;
            while (!RedirectReceiver.hasReceivedFault()) {
                Thread.sleep(100);
                index++;
                if (index > 45) {
                    throw new AxisFault(
                            "Tests was failed as redirected fault not received in time");
                }
            }
        } finally {
            if (sender != null)
                sender.cleanup();
        }
    }

    private OMElement createEchoOMElement(String text) {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText(text);
        method.addChild(value);

        return method;
    }

    private ServiceClient createServiceClient() throws AxisFault {
        AxisService service =
                createSimpleOneWayServiceforClient(serviceName,
                                                   Echo.class.getName(),
                                                   operationName);

        ConfigurationContext configcontext = UtilServer.createClientConfigurationContext();

        ServiceClient sender;

        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setAction(operationName.getLocalPart());
        options.setReplyTo(replyTo);
        options.setFaultTo(faultTo);
        options.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                            AddressingConstants.Submission.WSA_NAMESPACE);

        sender = new ServiceClient(configcontext, service);
        sender.setOptions(options);
        sender.engageModule("addressing");

        return sender;
    }

    private ServiceClient createSyncResponseServiceClient() throws AxisFault {
        AxisService service =
                Utils.createSimpleServiceforClient(serviceName,
                                                   Echo.class.getName(),
                                                   operationName);

        ConfigurationContext configcontext = UtilServer.createClientConfigurationContext();
        ServiceClient sender;

        Options options = new Options();
        options.setTo(targetEPR);
        options.setAction(operationName.getLocalPart());
        options.setProperty(AddressingConstants.WS_ADDRESSING_VERSION,
                            AddressingConstants.Submission.WSA_NAMESPACE);

        sender = new ServiceClient(configcontext, service);
        sender.setOptions(options);
        sender.engageModule("addressing");

        return sender;
    }

    public void testSyncResponseAddressing() throws Exception {
        String test = "hello.";
        OMElement method = createEchoOMElement(test);
        ServiceClient sender = null;

        try {
            sender = createSyncResponseServiceClient();
            OMElement result = sender.sendReceive(operationName, method);
            System.out.println("echoOMElementResponse: " + result);
            QName name = new QName("http://localhost/my", "myValue");
            String value = result.getFirstChildWithName(name).getText();

            assertEquals(test, value);
        } finally {
            if (sender != null)
                sender.cleanup();
        }
    }
}
