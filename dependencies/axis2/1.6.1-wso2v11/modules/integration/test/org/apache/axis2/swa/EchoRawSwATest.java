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

package org.apache.axis2.swa;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.Constants;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.util.Utils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;

public class EchoRawSwATest extends UtilServerBasedTestCase implements TestConstants {


    private static final Log log = LogFactory.getLog(EchoRawSwATest.class);

    private AxisService service;

    private boolean finish = false;

    public EchoRawSwATest() {
        super(EchoRawSwATest.class.getName());
    }

    public EchoRawSwATest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(EchoRawSwATest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "SwA-enabledRepository"));
    }

    protected void setUp() throws Exception {
        service = Utils.createSimpleService(serviceName, EchoSwA.class.getName(),
                                            operationName);
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    protected SOAPEnvelope createEnvelope() throws Exception {
        SOAPFactory fac = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope env = fac.getDefaultEnvelope();

        OMNamespace omNs = fac.createOMNamespace("htp://localhost/my", "my");
        OMElement rpcWrapEle = fac.createOMElement("echoOMElement", omNs);
        OMElement data = fac.createOMElement("data", omNs);
        OMText textData = fac.createOMText("Apache Axis2 Rocks !!!");
        data.addChild(textData);
        rpcWrapEle.addChild(data);

        env.getBody().addChild(rpcWrapEle);
        return env;
    }


    public void testEchoXMLSync() throws Exception {

        Options options = new Options();
        options.setTo(targetEPR);
        options.setProperty(Constants.Configuration.ENABLE_SWA, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapVersionURI(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTimeOutInMilliSeconds(100000);
        options.setAction(Constants.AXIS2_NAMESPACE_URI + "/" + operationName.getLocalPart());
        options.setTo(targetEPR);

        ConfigurationContext configContext =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo"), null);

        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        OperationClient mepClient = sender.createClient(ServiceClient.ANON_OUT_IN_OP);

        MessageContext mc = new MessageContext();
        mc.setEnvelope(createEnvelope());
        FileDataSource fileDataSource = new FileDataSource(TestingUtils.prefixBaseDirectory("test-resources/mtom/test.jpg"));
        DataHandler dataHandler = new DataHandler(fileDataSource);
        mc.addAttachment("FirstAttachment", dataHandler);

        mepClient.addMessageContext(mc);
        mepClient.execute(true);
        MessageContext response = mepClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        DataHandler dataHandler2 = response.getAttachment("FirstAttachment");
        assertNotNull(dataHandler);
        compareDataHandlers(dataHandler, dataHandler2);
    }

    protected void compareDataHandlers(DataHandler dataHandler, DataHandler dataHandler2) {
        String originalTextValue = new OMTextImpl(dataHandler, true, null).getText();
        String returnedTextValue = new OMTextImpl(dataHandler2, true, null).getText();
        assertEquals(returnedTextValue, originalTextValue);
    }
}