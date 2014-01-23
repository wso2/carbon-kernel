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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.TestConstants;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.apache.axis2.phaseresolver.PhaseMetadata;
import org.apache.axis2.util.Utils;

public class PausingHandlerExecutionTest extends UtilServerBasedTestCase implements TestConstants {
    private static boolean initDone = false;
    private static ArrayList testResults;
    private AxisService testService;
    private static TestHandler middleGlobalInHandler;
    private TestHandler firstOperationInHandler;
    private TestHandler middleOperationInHandler;
    private TestHandler middleOperationOutHandler;

    public PausingHandlerExecutionTest() {
        super(PausingHandlerExecutionTest.class.getName());
    }

    public PausingHandlerExecutionTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(PausingHandlerExecutionTest.class));
    }

    protected void setUp() throws Exception {
        //org.apache.log4j.BasicConfigurator.configure();

        testResults = new ArrayList();

        if (!initDone) {
            initDone = true;
            List globalInPhases =
                    UtilServer.getConfigurationContext().getAxisConfiguration().getInFlowPhases();
            for (int i = 0; i < globalInPhases.size(); i++) {
                Phase globalInPhase = (Phase)globalInPhases.get(i);
                if (PhaseMetadata.PHASE_PRE_DISPATCH.equals(globalInPhase.getPhaseName())) {
                    System.out.println("Adding handlers to  globalInPhase   name [" +
                            globalInPhase.getPhaseName() + "]  ...");
                    globalInPhase.addHandler(new TestHandler("In1"));
                    middleGlobalInHandler = new TestHandler("In2");
                    globalInPhase.addHandler(middleGlobalInHandler);
                    globalInPhase.addHandler(new TestHandler("In3"));
                    System.out.println("...done adding handlers to  globalInPhase   name [" +
                            globalInPhase.getPhaseName() + "]  ...");
                }
            }
        }

        testService = Utils.createSimpleService(serviceName, Echo.class.getName(),
                                                operationName);
        UtilServer.deployService(testService);
        AxisOperation operation = testService.getOperation(operationName);

        ArrayList operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(
                PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(operationSpecificPhases);
        ArrayList phaseList = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                firstOperationInHandler = new TestHandler("In4");
                operationSpecificPhase.addHandler(firstOperationInHandler);
                middleOperationInHandler = new TestHandler("In5");
                operationSpecificPhase.addHandler(middleOperationInHandler);
                operationSpecificPhase.addHandler(new TestHandler("In6"));
            }
        }

        operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(
                PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setPhasesOutFlow(operationSpecificPhases);
        phaseList = operation.getPhasesOutFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(new TestHandler("Out1"));
                middleOperationOutHandler = new TestHandler("Out2");
                operationSpecificPhase.addHandler(middleOperationOutHandler);
                operationSpecificPhase.addHandler(new TestHandler("Out3"));
            }
        }
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    private ServiceClient createClient() throws Exception {
        Options options = new Options();
        options.setTo(targetEPR);
        options.setSoapVersionURI(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
        options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        options.setAction(operationName.getLocalPart());
        options.setUseSeparateListener(true);
        options.setTimeOutInMilliSeconds(50000);

        ConfigurationContext configContext = UtilServer.createClientConfigurationContext();

        ServiceClient sender = new ServiceClient(configContext, null);
        sender.setOptions(options);
        sender.engageModule("addressing");
        return sender;
    }

    private void executeClient() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();
        OMElement result = createClient().sendReceive(payload);

        TestingUtils.compareWithCreatedOMElement(result);
    }

    public void testSuccessfulInvocation() throws Exception {
        System.out.println("Starting testSuccessfulInvocation");
        middleGlobalInHandler.shouldPause(true);
        executeClient();

        /*Since the response is going back separately, we need to give the server
 *time to unwind the rest of the invocation.*/
        //Thread.sleep(5000);
        Thread.sleep(7000);

        Iterator it = testResults.iterator();
        int count = 0;
        while (it.hasNext()) {
            count ++;
            String tmp = (String)it.next();
            System.out.println("Test Results [" + count + "] = [" + tmp + "]");
        }

        //-----------------------------------------------------------------------
        // uncomment/comment out the desired results based on whether pausing
        // the message context

        // expected results when not pausing
        //List expectedExecutionState = Arrays.asList(new String[] {"In1", "In2", "In3", "In4", "In5", "In6", "Out1", "Out2", "Out3", "FCOut3", "FCOut2", "FCOut1", "FCIn6", "FCIn5", "FCIn4", "FCIn3", "FCIn2", "FCIn1"});

        // expected results when pausing
        List expectedExecutionState = Arrays.asList(new String[] { "In1", "In2", "In2", "In3",
                "In4", "In5", "In6", "Out1", "Out2", "Out3", "FCOut3", "FCOut2", "FCOut1", "FCIn6",
                "FCIn5", "FCIn4", "FCIn3", "FCIn2", "FCIn1" });
        //-----------------------------------------------------------------------
        assertEquals(expectedExecutionState, testResults);

    }

    private class TestHandler extends AbstractHandler {
        private String handlerName;
        private boolean shouldFail = false;
        private boolean shouldPause = false;
        private boolean shouldSave = true;

        public TestHandler(String handlerName) {
            this.handlerName = handlerName;
        }

        public void shouldFail(boolean fail) {
            System.out.println("Setting shouldFail to " + fail + " for " + handlerName);
            this.shouldFail = fail;
        }

        public void shouldPause(boolean pause) {
            System.out.println("Setting shouldPause to " + pause + " for " + handlerName);
            this.shouldPause = pause;
        }

        public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
            String title = "TestHandler[" + handlerName + "] ";
            System.out.println(title + " invoked:");

            // check if the handler should fail
            if (shouldFail) {
                String failmsg = title + " kaboom";
                System.out.println(title + " adding [" + failmsg + "] to testResults list");
                testResults.add(failmsg);
                System.out.println(failmsg);
                throw new AxisFault(failmsg + "   Handler failed");
            }

            // add this handler to the list of invoked handlers
            System.out.println(title + " adding this handler to testResults list");
            testResults.add(handlerName);

            boolean isPaused = msgContext.isPaused();
            if (isPaused) {
                System.out.println(title + "   message context is paused   *****");
            } else {
                System.out.println(title + "   message context is not paused");
            }

            checkLists(msgContext);

            // check if the handler should pause
            if (shouldPause) {
                String pausemsg = title + " - pausing the message context";
                System.out.println(pausemsg);
                msgContext.pause();
                shouldPause = false;

                File theFile = null;
                String theFilename = null;

                try {
                    theFile = File.createTempFile("pHexec", null);
                    theFilename = theFile.getName();
                    System.out.println(title + "temp file = [" + theFilename + "]");
                }
                catch (Exception ex) {
                    System.out.println(
                            title + "error creating temp file = [" + ex.getMessage() + "]");
                    theFile = null;
                }

                if (theFile != null) {
                    // save to the temporary file
                    try {
                        // setup an output stream to a physical file
                        FileOutputStream outStream = new FileOutputStream(theFile);

                        // attach a stream capable of writing objects to the
                        // stream connected to the file
                        ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

                        // try to save the message context
                        System.out.println(title + "saving message context.....");
                        outObjStream.writeObject(msgContext);

                        // close out the streams
                        outObjStream.flush();
                        outObjStream.close();
                        outStream.flush();
                        outStream.close();

                        System.out.println(title + "....saved message context.....");

                        long filesize = theFile.length();
                        System.out.println(title + "file size after save [" + filesize +
                                "]   temp file = [" + theFilename + "]");

                        new Worker(theFile, msgContext.getConfigurationContext()).start();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        fail("An error occurred when serializing the MessageContext");
                    }
                } else {
                    // couldn't get a temporary file
                    new Worker(msgContext, msgContext.getConfigurationContext()).start();
                }
                return InvocationResponse.SUSPEND;
            } // end if should pause

            return InvocationResponse.CONTINUE;
        }

        public void flowComplete(MessageContext msgContext) {
            String title = "TestHandler[" + handlerName + "] ";
            String label = "FC" + handlerName;
            System.out
                    .println(title + " flowComplete(): adding [" + label + "] to testResults list");
            testResults.add("FC" + handlerName);
        }

        private void checkLists(MessageContext mc) {
            if (mc == null) {
                return;
            }

            String title = "TestHandler[" + handlerName + "] ";

            System.out.println(title + "Checking execution chain.....");
            ArrayList execList = mc.getExecutionChain();
            Iterator execIt = null;
            if (execList != null) {
                execIt = execList.iterator();
            }
            checkHandler(execIt);

            System.out.println(title + "Checking inbound executed phases list.....");
            Iterator inboundIt = mc.getExecutedPhases();
            checkHandler(inboundIt);
        }

        private void checkHandler(Iterator it) {
            if (it == null) {
                return;
            }

            while (it.hasNext()) {
                Handler handler = (Handler)it.next();

                if (handler instanceof Handler) {
                    System.out.println("Handler name [" + handler.getName() + "]");
                } else if (handler instanceof Phase) {
                    Phase phase = (Phase)handler;
                    System.out.println("Phase name [" + phase.getName() + "]");

                    List list2 = phase.getHandlers();
                    Iterator it2 = list2.iterator();
                    checkHandler(it2);
                }
            }

        }


    }

    private class Worker extends Thread {
        private byte[] serializedMessageContext = null;
        private ConfigurationContext configurationContext = null;
        private File theFile = null;
        private String theFilename = null;
        private MessageContext msgContext = null;

        public Worker(MessageContext msgContext) {
            this.msgContext = msgContext;
            this.configurationContext = msgContext.getConfigurationContext();
        }

        public Worker(byte[] serializedMessageContext, ConfigurationContext configurationContext) {
            this.serializedMessageContext = serializedMessageContext;
            this.configurationContext = configurationContext;
        }

        public Worker(File theFile, ConfigurationContext configurationContext) {
            this.theFile = theFile;
            this.configurationContext = configurationContext;
        }

        public Worker(MessageContext mc, ConfigurationContext configurationContext) {
            this.msgContext = mc;
            this.configurationContext = configurationContext;
        }


        public void run() {
            try {
                System.out.println("Worker thread started");
                Thread.sleep(5000);

                FileInputStream inStream = null;
                ObjectInputStream objectInputStream = null;
                MessageContext reconstitutedMessageContext = null;

                if (theFile != null) {
                    // setup an input stream to the file
                    inStream = new FileInputStream(theFile);

                    // attach a stream capable of reading objects from the
                    // stream connected to the file
                    objectInputStream = new ObjectInputStream(inStream);

                    System.out.println("Worker thread restoring message context from file");
                    reconstitutedMessageContext = (MessageContext)objectInputStream.readObject();
                    reconstitutedMessageContext.activate(configurationContext);
                } else if (serializedMessageContext != null) {
                    // use the byte array
                    objectInputStream = new ObjectInputStream(
                            new ByteArrayInputStream(serializedMessageContext));

                    System.out.println("Worker thread restoring message context from byte array");
                    reconstitutedMessageContext = (MessageContext)objectInputStream.readObject();
                    reconstitutedMessageContext.activate(configurationContext);
                } else if (msgContext != null) {
                    System.out.println("Worker thread using message context");
                    reconstitutedMessageContext = msgContext;
                }

                if (inStream != null) {
                    inStream.close();
                }

                if (objectInputStream != null) {
                    objectInputStream.close();
                }

                if (theFile != null) {
                    // remove the temporary file
                    try {
                        theFile.delete();
                    }
                    catch (Exception e) {
                        // just absorb it
                    }
                }

                if (reconstitutedMessageContext != null) {
                    System.out.println("Worker thread resuming message context");
                    AxisEngine.resume(reconstitutedMessageContext);
                } else {
                    fail("An error occurred in the worker thread - no message context");
                }

            }
            catch (Exception e) {
                e.printStackTrace();
                fail("An error occurred in the worker thread");
            }
        }
    }
}
