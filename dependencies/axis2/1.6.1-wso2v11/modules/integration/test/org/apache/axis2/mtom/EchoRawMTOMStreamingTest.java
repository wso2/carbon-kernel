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
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.io.InputStream;

public class EchoRawMTOMStreamingTest extends UtilServerBasedTestCase implements TestConstants {

    public static final EndpointReference targetEPR = new EndpointReference(
            "http://127.0.0.1:" + (UtilServer.TESTING_PORT)
//            "http://127.0.0.1:" + 5556
                    + "/axis2/services/EchoService2/mtomSample");

    private QName serviceName = new QName("EchoService2");

    private QName operationName = new QName("mtomSample");

    private OMElement data;

    public EchoRawMTOMStreamingTest() {
        super(EchoRawMTOMStreamingTest.class.getName());
    }

    public EchoRawMTOMStreamingTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawMTOMStreamingTest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "MTOM-enabledRepository"));
    }

    protected void setUp() throws Exception {
        AxisService service = Utils.createSimpleService(serviceName, EchoService2.class.getName(),
                                                        operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
    }

    private OMElement createEnvelope() throws Exception {

        DataHandler expectedDH;
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement rpcWrapEle = fac.createOMElement("mtomSample", omNs);
        data = fac.createOMElement("data", omNs);
        expectedDH = new DataHandler(
                new ByteArrayDataSource(new byte[] { 13, 56, 65, 32, 12, 12, 7, -3, -2, -1,
                        98 }));
        OMElement subData = fac.createOMElement("subData", omNs);
        OMText textData = new OMTextImpl(expectedDH, fac);
        subData.addChild(textData);
        data.addChild(subData);
        rpcWrapEle.addChild(data);
        return rpcWrapEle;

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
                        Constants.TESTING_PATH + "commons-http-enabledRepository", null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        options.setTo(targetEPR);

        OMElement result = sender.sendReceive(payload);
        this.campareWithCreatedOMElement(result);

    }

    private InputStream getResourceAsStream(String path) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return cl.getResourceAsStream(path);
    }

    private void campareWithCreatedOMElement(OMElement element) {
        OMElement firstChild = element.getFirstElement();
        TestCase.assertNotNull(firstChild);
        String originalTextValue = data.getFirstElement().getText();
        String returnedTextValue = firstChild.getText();
        TestCase.assertEquals(returnedTextValue, originalTextValue);
    }

}
