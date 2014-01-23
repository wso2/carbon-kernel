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
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

public class EchoRawMTOMCommonsChunkingTest extends UtilServerBasedTestCase
        implements TestConstants {

    private OMElement data;
    private String fileName = TestingUtils.prefixBaseDirectory("test-resources/mtom/test.jpg");

    public EchoRawMTOMCommonsChunkingTest() {
        super(EchoRawMTOMCommonsChunkingTest.class.getName());
    }

    public EchoRawMTOMCommonsChunkingTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawMTOMCommonsChunkingTest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "MTOM-enabledRepository"));
    }

    protected void setUp() throws Exception {
        AxisService service = Utils.createSimpleService(serviceName, Echo.class.getName(),
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
        OMElement rpcWrapEle = fac.createOMElement("echoOMElement", omNs);
        data = fac.createOMElement("data", omNs);
        FileDataSource dataSource = new FileDataSource(fileName);
        expectedDH = new DataHandler(dataSource);
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
                        TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "commons-http-enabledRepository"), null);
        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        options.setTo(targetEPR);

        OMElement ret = sender.sendReceive(payload);
        ret.build();
        this.compareWithCreatedOMElement(ret);

    }

    private void compareWithCreatedOMElement(OMElement element) {
        OMElement firstChild = element.getFirstElement();
        TestCase.assertNotNull(firstChild);
        String originalTextValue = data.getFirstElement().getText();
        String returnedTextValue = firstChild.getFirstElement().getText();
        TestCase.assertEquals(returnedTextValue, originalTextValue);
    }

}