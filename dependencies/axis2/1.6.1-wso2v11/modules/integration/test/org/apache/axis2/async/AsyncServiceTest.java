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

package org.apache.axis2.async;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

public class AsyncServiceTest extends UtilServerBasedTestCase implements TestConstants {

    private static final Log log = LogFactory.getLog(AsyncServiceTest.class);
    protected QName transportName = new QName("http://localhost/my",
                                              "NullTransport");
    EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//            "http://127.0.0.1:" + 5556
                    + "/axis2/services/EchoXMLService/echoOMElement");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;
    private boolean finish = false;

    public static Test suite() {
        return getTestSetup(new TestSuite(AsyncServiceTest.class));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName,
                                            new AsyncMessageReceiver(),
                                            Echo.class.getName(),
                                            operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLCompleteASync() throws Exception {
        AxisService service =
                Utils.createSimpleServiceforClient(serviceName,
                                                   Echo.class.getName(),
                                                   operationName);

        ConfigurationContext configcontext = UtilServer.createClientConfigurationContext();

        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText("Isaac Asimov, The Foundation Trilogy");
        method.addChild(value);
        ServiceClient sender = null;

        try {
            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setUseSeparateListener(true);
            options.setAction(operationName.getLocalPart());

            AxisCallback callback = new AxisCallback() {

                /**
                 * This is called when we receive a message.
                 *
                 * @param msgContext the (response) MessageContext
                 */
                public void onMessage(MessageContext msgContext) {
                    OMElement result = msgContext.getEnvelope().getBody().getFirstElement();
                    TestingUtils.compareWithCreatedOMElement(result);
                    log.debug("result = " + result);
                }

                /**
                 * This gets called when a fault message is received.
                 *
                 * @param msgContext the MessageContext containing the fault.
                 */
                public void onFault(MessageContext msgContext) {
                    fail("Fault received");
                }


                /**
                 * This gets called ONLY when an internal processing exception occurs.
                 *
                 * @param e the Exception which caused the problem
                 */
                public void onError(Exception e) {
                }

                /** This is called at the end of the MEP no matter what happens, quite like a finally block. */
                public void onComplete() {
                    finish = true;
                    notify();
                }
            };

            sender = new ServiceClient(configcontext, service);
            sender.setOptions(options);

            sender.sendReceiveNonBlocking(operationName, method, callback);
            log.info("send the request");
            synchronized (callback) {
                if (!finish) {
                    callback.wait(45000);
                    if (!finish) {
                        throw new AxisFault(
                                "Server was shutdown as the async response take too long to complete");
                    }
                }
            }
        } finally {
            if (sender != null)
                sender.cleanup();
        }

    }

}
