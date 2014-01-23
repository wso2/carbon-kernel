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
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AxisCallback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import java.io.InputStream;

public class EchoRawMTOMTest extends UtilServerBasedTestCase implements TestConstants {


    private static final Log log = LogFactory.getLog(EchoRawMTOMTest.class);

    private AxisService service;

    private OMTextImpl expectedTextData;

    private boolean finish = false;

    public EchoRawMTOMTest() {
        super(EchoRawMTOMTest.class.getName());
    }

    public EchoRawMTOMTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawMTOMTest.class),
                TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "MTOM-enabledRepository"));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName, Echo.class.getName(),
                operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    protected OMElement createEnvelope() throws Exception {

        DataHandler expectedDH;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement rpcWrapEle = fac.createOMElement("echoOMElement", omNs);
        OMElement data = fac.createOMElement("data", omNs);
        FileDataSource fileDataSource = new FileDataSource(TestingUtils.prefixBaseDirectory("test-resources/mtom/test.jpg"));
        expectedDH = new DataHandler(fileDataSource);
        expectedTextData = new OMTextImpl(expectedDH, true, fac);
        data.addChild(expectedTextData);
        rpcWrapEle.addChild(data);
        return rpcWrapEle;

    }

    public void testEchoXMLASync() throws Exception {
        OMElement payload = createEnvelope();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, MessageContext.UTF_16);

        AxisCallback callback = new AxisCallback() {

            public void onMessage(MessageContext msgContext) {
                SOAPEnvelope envelope = msgContext.getEnvelope();

                OMElement ele = (OMElement)envelope.getBody().getFirstElement().getFirstOMChild();
                OMText binaryNode = (OMText)ele.getFirstOMChild();

                // to the assert equal
                compareWithCreatedOMText(binaryNode);
                finish = true;
            }

            public void onFault(MessageContext msgContext) {
                SOAPEnvelope envelope = msgContext.getEnvelope();

                OMElement ele = (OMElement)envelope.getBody().getFirstElement().getFirstOMChild();
                OMText binaryNode = (OMText)ele.getFirstOMChild();

                // to the assert equal
                compareWithCreatedOMText(binaryNode);
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
        options.setAction(Constants.AXIS2_NAMESPACE_URI + "/" + operationName.getLocalPart());
        sender.setOptions(options);

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
        OMElement payload = createEnvelope();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo"), null);

        ServiceClient sender = new ServiceClient(configContext, null);
        options.setAction(Constants.AXIS2_NAMESPACE_URI + "/" + operationName.getLocalPart());
        sender.setOptions(options);
        options.setTo(targetEPR);
        OMElement result = sender.sendReceive(payload);

        OMElement ele = (OMElement)result.getFirstOMChild();
        OMText binaryNode = (OMText)ele.getFirstOMChild();

        // to the assert equal
        compareWithCreatedOMText(binaryNode);

        // Save the image
        DataHandler actualDH;
        actualDH = (DataHandler)binaryNode.getDataHandler();
        ImageIO.read(actualDH.getDataSource()
                .getInputStream());
    }

    public void testEchoXMLSyncSeperateListener() throws Exception {
        OMElement payload = createEnvelope();
        Options options = new Options();
        options.setTo(targetEPR);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo"), null);

        ServiceClient sender = new ServiceClient(configContext, null);
        sender.engageModule(new QName("addressing"));
        options.setAction(Constants.AXIS2_NAMESPACE_URI + "/" + operationName.getLocalPart());
        sender.setOptions(options);
        options.setUseSeparateListener(true);
        options.setTo(targetEPR);
        OMElement result = sender.sendReceive(payload);

        OMElement ele = (OMElement)result.getFirstOMChild();
        OMText binaryNode = (OMText)ele.getFirstOMChild();
        // to the assert equal
        compareWithCreatedOMText(binaryNode);
    }

    protected InputStream getResourceAsStream(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(path);
    }

    protected void compareWithCreatedOMText(OMText actualTextData) {
        String originalTextValue = expectedTextData.getText();
        String returnedTextValue = actualTextData.getText();
        TestCase.assertEquals(returnedTextValue, originalTextValue);
    }
}