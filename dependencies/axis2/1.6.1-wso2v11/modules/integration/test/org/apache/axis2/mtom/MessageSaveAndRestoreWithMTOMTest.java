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
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.Phase;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.util.Utils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * This tests the saving and restoring of the Axis2 contexts from within a handler when MTOM is
 * used.  This is a more thorough test of the serialization mechanisms than the other unit tests, as
 * the contexts are populated from the beginning of the Axis2 codepath.
 */
public class MessageSaveAndRestoreWithMTOMTest extends UtilServerBasedTestCase
        implements TestConstants {
    private OMTextImpl expectedTextData = null;

    public MessageSaveAndRestoreWithMTOMTest() {
        super(MessageSaveAndRestoreWithMTOMTest.class.getName());

        org.apache.log4j.BasicConfigurator.configure();
    }

    public MessageSaveAndRestoreWithMTOMTest(String testName) {
        super(testName);

        org.apache.log4j.BasicConfigurator.configure();
    }

    public static Test suite() {
        return getTestSetup2(new TestSuite(MessageSaveAndRestoreWithMTOMTest.class),
                             TestingUtils.prefixBaseDirectory(Constants.TESTING_PATH + "MTOM-enabledRepository"));
    }

    protected void setUp() throws Exception {

        AxisService service = Utils.createSimpleService(TestConstants.serviceName,
                                                        Echo.class.getName(),
                                                        TestConstants.operationName);
        UtilServer.deployService(service);

        AxisOperation operation = service.getOperation(TestConstants.operationName);

        ArrayList phases = new ArrayList();
        phases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(phases);
        ArrayList phase = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phase.size(); i++) {
            Phase phase1 = (Phase)phase.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION.equals(phase1.getPhaseName())) {
                phase1.addHandler(inboundHandler);
            }
        }

        phases = new ArrayList();
        phases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setPhasesOutFlow(phases);
        phase = operation.getPhasesOutFlow();
        for (int i = 0; i < phase.size(); i++) {
            Phase phase1 = (Phase)phase.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION.equals(phase1.getPhaseName())) {
                phase1.addHandler(outboundHandler);
            }
        }
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(TestConstants.serviceName);
        UtilServer.unDeployClientService();
    }

    public void testSaveAndRestoreOfMessage() throws Exception {
        OMElement payload = createEnvelope();

        Options options = new Options();
        options.setTo(TestConstants.targetEPR);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setAction(
                Constants.AXIS2_NAMESPACE_URI + "/" + TestConstants.operationName.getLocalPart());
        options.setUseSeparateListener(true);
        options.setTimeOutInMilliSeconds(50000);

        ConfigurationContext configurationContext = ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(TestingUtils.prefixBaseDirectory("target/test-resources/integrationRepo"),
                                                          null);

        ServiceClient sender = new ServiceClient(configurationContext, null);
        sender.setOptions(options);
        sender.engageModule("addressing");

        OMElement result = sender.sendReceive(payload);

        OMElement element = (OMElement)result.getFirstOMChild();
        OMText binaryNode = (OMText)element.getFirstOMChild();

        compareWithCreatedOMText(binaryNode);

        DataHandler actualDH = (DataHandler)binaryNode.getDataHandler();
        BufferedImage bi = ImageIO.read(actualDH.getDataSource().getInputStream());
    }

    protected OMElement createEnvelope() throws Exception {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace omNamespace = omFactory.createOMNamespace("http://localhost/my", "my");
        OMElement rpcWrapperElement = omFactory.createOMElement("echoOMElement", omNamespace);
        OMElement data = omFactory.createOMElement("data", omNamespace);

        FileDataSource fileDataSource = new FileDataSource(TestingUtils.prefixBaseDirectory("test-resources/mtom/test.jpg"));
        DataHandler expectedDataHandler = new DataHandler(fileDataSource);
        expectedTextData = new OMTextImpl(expectedDataHandler, true, omFactory);
        data.addChild(expectedTextData);
        rpcWrapperElement.addChild(data);
        return rpcWrapperElement;

    }

    protected InputStream getResourceAsStream(String path) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
    }

    protected void compareWithCreatedOMText(OMText actualTextData) {
        String originalTextValue = expectedTextData.getText();
        String returnedTextValue = actualTextData.getText();
        TestCase.assertEquals(returnedTextValue, originalTextValue);
    }

    private Handler inboundHandler = new AbstractHandler() {
        private static final long serialVersionUID = 1L;
        private String stateProperty = "InboundHandlerState";

        public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
            System.out.println("MessageSaveAndRestoreWithMTOMTest:Inbound handler invoked");
            if (messageContext.getProperty(stateProperty) == null) {
                System.out.println("MessageSaveAndRestoreWithMTOMTest:Suspending processing");
                messageContext.setProperty(stateProperty, new Object());
                messageContext.pause();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try {
                    ObjectOutputStream objectOutputStream =
                            new ObjectOutputStream(byteArrayOutputStream);
                    objectOutputStream.writeObject(messageContext);
                    objectOutputStream.flush();
                    objectOutputStream.close();
                    byteArrayOutputStream.flush();
                    byteArrayOutputStream.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    fail("An error occurred when serializing the MessageContext");
                }
                new Worker(byteArrayOutputStream.toByteArray(),
                           messageContext.getConfigurationContext()).start();
            } else {
                System.out.println(
                        "MessageSaveAndRestoreWithMTOMTest:Skipping previously invoked Inbound handler");
            }
            return InvocationResponse.CONTINUE;
        }
    };

    private Handler outboundHandler = new AbstractHandler() {
        private static final long serialVersionUID = 1L;

        public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
            System.out.println("MessageSaveAndRestoreWithMTOMTest:Outbound handler invoked");
            return InvocationResponse.CONTINUE;
        }
    };

    private class Worker extends Thread {
        private byte[] serializedMessageContext;
        private ConfigurationContext configurationContext;

        public Worker(byte[] serializedMessageContext, ConfigurationContext configurationContext) {
            this.serializedMessageContext = serializedMessageContext;
            this.configurationContext = configurationContext;
        }

        public void run() {
            try {
                System.out.println("MessageSaveAndRestoreWithMTOMTest:Worker thread started");
                Thread.sleep(5000);
                System.out.println("MessageSaveAndRestoreWithMTOMTest:Resuming processing");
                ObjectInputStream objectInputStream =
                        new ObjectInputStream(new ByteArrayInputStream(serializedMessageContext));
                MessageContext reconstitutedMessageContext =
                        (MessageContext)objectInputStream.readObject();
                reconstitutedMessageContext.activate(configurationContext);
                AxisEngine.resume(reconstitutedMessageContext);
            }
            catch (Exception e) {
                e.printStackTrace();
                fail("An error occurred in the worker thread");
            }
        }
    }
}
