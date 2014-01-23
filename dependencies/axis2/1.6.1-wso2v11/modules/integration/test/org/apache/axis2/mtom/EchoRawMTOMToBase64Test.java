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

package org.apache.axis2.mtom;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;

public class EchoRawMTOMToBase64Test extends UtilServerBasedTestCase {
    private EndpointReference targetEPR = new EndpointReference("http://127.0.0.1:"
            + (UtilServer.TESTING_PORT)
            + "/axis2/services/EchoXMLService/echoMTOMtoBase64");

    private static final Log log = LogFactory.getLog(EchoRawMTOMToBase64Test.class);

    private QName serviceName = new QName("EchoXMLService");

    private QName operationName = new QName("echoMTOMtoBase64");

    OMText expectedTextData;

    private boolean finish = false;

    public EchoRawMTOMToBase64Test() {
        super(EchoRawMTOMToBase64Test.class.getName());
    }

    public EchoRawMTOMToBase64Test(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(EchoRawMTOMToBase64Test.class));
    }

    protected void setUp() throws Exception {
        AxisService service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    private OMElement createPayload() {

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement rpcWrapEle = fac.createOMElement("echoMTOMtoBase64", omNs);
        OMElement data = fac.createOMElement("data", omNs);
        byte[] byteArray = new byte[] { 13, 56, 65, 32, 12, 12, 7, 98 };
        DataHandler dataHandler = new DataHandler(new ByteArrayDataSource(byteArray));
        expectedTextData = new OMTextImpl(dataHandler, true, fac);
        data.addChild(expectedTextData);
        rpcWrapEle.addChild(data);
        return rpcWrapEle;
    }

    public void testEchoXMLASync() throws Exception {
        OMElement payload = createPayload();
        Options clientOptions = new Options();
        clientOptions.setTo(targetEPR);
        clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);


        AxisCallback callback = new AxisCallback() {

            public void onMessage(MessageContext msgContext) {
                SOAPEnvelope envelope = msgContext.getEnvelope();

                OMElement data = (OMElement)envelope.getBody().getFirstElement().getFirstOMChild();
                compareWithCreatedOMText(data.getText());
                finish = true;
            }

            public void onFault(MessageContext msgContext) {
                SOAPEnvelope envelope = msgContext.getEnvelope();

                OMElement data = (OMElement)envelope.getBody().getFirstElement().getFirstOMChild();
                compareWithCreatedOMText(data.getText());
                finish = true;

            }

            public void onError(Exception e) {
                log.info(e.getMessage());
                finish = true;
            }

            public void onComplete() {
            }
        };

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo"), null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(clientOptions);

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

    public void testEchoXMLSync() throws Exception {
        for (int i = 0; i < 10; i++) {
            OMElement payload = createPayload();

            Options clientOptions = new Options();
            clientOptions.setTo(targetEPR);
            clientOptions.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            clientOptions.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

            ConfigurationContext configContext =
                    ConfigurationContextFactory
                            .createConfigurationContextFromFileSystem(null, null);
            ServiceClient sender = new ServiceClient(configContext, null);
            sender.setOptions(clientOptions);

            OMElement result = sender.sendReceive(payload);

            OMElement data = (OMElement)result.getFirstOMChild();
            compareWithCreatedOMText(data.getText());
            log.info("" + i);
            UtilServer.unDeployClientService();
        }
    }

    private void compareWithCreatedOMText(String actualText) {
        String originalTextValue = expectedTextData.getText();
        TestCase.assertEquals(actualText, originalTextValue);
    }

}