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

public class AsyncServiceWithTransportFailureTest extends
        UtilServerBasedTestCase implements TestConstants {
    private static final Log log = LogFactory.getLog(AsyncServiceWithTransportFailureTest.class);
    EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:0"
            + "/axis2/services/EchoXMLService/echoOMElement");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;
    private boolean finish = false;
    private boolean wasError = false;

    public static Test suite() {
        return getTestSetup(new TestSuite(
                AsyncServiceWithTransportFailureTest.class));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName,
                new AsyncMessageReceiver(),
                Echo.class.getName(), operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testEchoXMLCompleteASyncWithTransportFailure() throws Exception {
        System.out.println("Starting testEchoXMLCompleteASyncWithTransportFailure");
        AxisService service = Utils.createSimpleServiceforClient(
                serviceName,
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

                public void onMessage(MessageContext msgContext) {
                    TestingUtils.compareWithCreatedOMElement(
                            msgContext.getEnvelope().getBody().getFirstElement());
                    System.out.println("result = "
                            +  msgContext.getEnvelope().getBody().getFirstElement());
                    finish = true;
                }

                public void onFault(MessageContext msgContext) {
                    TestingUtils.compareWithCreatedOMElement(
                            msgContext.getEnvelope().getBody().getFirstElement());
                    System.out.println("result = "
                            +  msgContext.getEnvelope().getBody().getFirstElement());
                    finish = true;
                }

                public void onError(Exception e) {
                    log.info(e.getMessage());
                    wasError = true;
                    finish = true;

                }

                public void onComplete() {

                }
            };

            sender = new ServiceClient(configcontext, service);
            sender.setOptions(options);

            sender.sendReceiveNonBlocking(operationName, method, callback);
            System.out.println("send the request");
            log.info("send the request");
            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
                if (index > 45) {
                    throw new AxisFault(
                            "Server was shutdown, as the async response took too long to complete");
                }
                if (finish && !wasError) {
                    fail("An error occurred during the transmission of the async request but the callback was not notified");
                }
            }
        }
        finally {
            if (sender != null)
                sender.cleanup();
        }
    }
}
