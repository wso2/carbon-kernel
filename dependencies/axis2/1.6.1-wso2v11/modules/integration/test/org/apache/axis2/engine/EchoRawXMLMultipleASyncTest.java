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
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;


public class EchoRawXMLMultipleASyncTest extends UtilServerBasedTestCase implements TestConstants {

    private static final Log log = LogFactory.getLog(EchoRawXMLMultipleASyncTest.class);
    protected QName transportName = new QName("http://localhost/my",
            "NullTransport");

    protected AxisConfiguration engineRegistry;
    protected MessageContext mc;
    protected ServiceContext serviceContext;
    protected AxisService service;

    protected boolean finish = false;

    public EchoRawXMLMultipleASyncTest() {
        super(EchoRawXMLTest.class.getName());
    }

    public EchoRawXMLMultipleASyncTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(EchoRawXMLMultipleASyncTest.class));
    }

    protected void setUp() throws Exception {
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }


    public void testEchoXMLMultipleASync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();
        Options options = new Options();
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo"),
                        TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo/conf/axis2.xml"));
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        options.setAction("urn:echoOMElement");
        options.setTo(targetEPR);
        for (int i = 0; i < 5; i++) {

            AxisCallback callback = new AxisCallback() {

                public void onMessage(MessageContext msgContext) {
                    TestingUtils.compareWithCreatedOMElement(
                            msgContext.getEnvelope()
                                    .getBody().getFirstElement());
                    finish = true;
                }

                public void onFault(MessageContext msgContext) {
                    TestingUtils.compareWithCreatedOMElement(
                            msgContext.getEnvelope()
                                    .getBody().getFirstElement());
                    finish = true;
                }

                public void onError(Exception e) {
                    log.info(e.getMessage());
                    finish = true;
                }

                public void onComplete() {
                }
            };
            sender.sendReceiveNonBlocking(payload, callback);
            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
                if (index > 10) {
                    throw new AxisFault(
                            "Server was shutdown as the async response take too long to complete");
                }
            }
        }
        sender.cleanup();
        configContext.terminate();
        log.info("send the request");

    }

    public void testEchoXMLMultipleDuelASync() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        "target/test-resources/integrationRepo",
                        "target/test-resources/integrationRepo/conf/axis2.xml");
        ServiceClient sender = new ServiceClient(configContext, null);
        for (int i = 0; i < 5; i++) {
            Options options = new Options();
            options.setAction("urn:echoOMElement");
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setUseSeparateListener(true);
            options.setAction(Constants.AXIS2_NAMESPACE_URI + "/" + operationName.getLocalPart());

            AxisCallback callback = new AxisCallback() {

                public void onMessage(MessageContext msgContext) {
                    TestingUtils.compareWithCreatedOMElement(
                            msgContext.getEnvelope()
                                    .getBody().getFirstElement());
                    finish = true;
                }

                public void onFault(MessageContext msgContext) {
                    TestingUtils.compareWithCreatedOMElement(
                            msgContext.getEnvelope()
                                    .getBody().getFirstElement());
                    finish = true;
                }

                public void onError(Exception e) {
                    log.info(e.getMessage());
                    finish = true;
                }

                public void onComplete() {
                }
            };

            sender.setOptions(options);
            options.setTo(targetEPR);

            sender.sendReceiveNonBlocking(payload, callback);

            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
                if (index > 10) {
                    throw new AxisFault(
                            "Server is shutdown as the Async response take too longs time");
                }
            }
        }
        sender.cleanup();
        configContext.terminate();
        log.info("send the request");
    }
}
