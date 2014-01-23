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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.util.FaultThrowingService;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.integration.LocalTestCase;
import org.apache.axis2.integration.TestingUtils;
import org.apache.axis2.phaseresolver.PhaseMetadata;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandlerExecutionTest extends LocalTestCase {
    private static ArrayList testResults;
    private AxisService testService;
    private AxisService testFailingService;
    private QName failingServiceName = new QName("FaultThrowingService");
    private QName failingOperationName = new QName("echoWithFault");
    private static TestHandler middleGlobalInHandler;
    private TestHandler firstOperationInHandler;
    private TestHandler middleOperationInHandler;
    private TestHandler middleOperationOutHandler;

    private void registerOperationLevelHandlers(AxisOperation operation) {
        ArrayList operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(operationSpecificPhases);
        ArrayList phaseList = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(firstOperationInHandler);
                operationSpecificPhase.addHandler(middleOperationInHandler);
                operationSpecificPhase.addHandler(new TestHandler("In6"));
            }
        }

        operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setPhasesOutFlow(operationSpecificPhases);
        phaseList = operation.getPhasesOutFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(new TestHandler("Out1"));
                operationSpecificPhase.addHandler(middleOperationOutHandler);
                operationSpecificPhase.addHandler(new TestHandler("Out3"));
            }
        }

    }

    protected void setUp() throws Exception {
    	super.setUp();
        testResults = new ArrayList();

            List globalInPhases =
                    serverConfig.getAxisConfiguration().getInFlowPhases();
            for (int i = 0; i < globalInPhases.size(); i++) {
                Phase globalInPhase = (Phase)globalInPhases.get(i);
                if (PhaseMetadata.PHASE_PRE_DISPATCH.equals(globalInPhase.getPhaseName())) {
                    globalInPhase.addHandler(new TestHandler("In1"));
                    middleGlobalInHandler = new TestHandler("In2");
                    globalInPhase.addHandler(middleGlobalInHandler);
                    globalInPhase.addHandler(new TestHandler("In3"));
                }
            }
        firstOperationInHandler = new TestHandler("In4");
        middleOperationInHandler = new TestHandler("In5");
        middleOperationOutHandler = new TestHandler("Out2");

        testService = deployClassAsService(Echo.SERVICE_NAME, Echo.class);
        registerOperationLevelHandlers(testService.getOperation(new QName(Echo.ECHO_OM_ELEMENT_OP_NAME)));

        testFailingService = deployClassAsService(failingServiceName.getLocalPart(), FaultThrowingService.class);
        registerOperationLevelHandlers(testFailingService.getOperation(failingOperationName));

    }

    private ServiceClient createClient() throws Exception {
    	return getClient(Echo.SERVICE_NAME, Echo.ECHO_OM_ELEMENT_OP_NAME);
    }

    private void executeClient() throws Exception {
        OMElement payload = TestingUtils.createDummyOMElement();
        OMElement result = createClient().sendReceive(payload);

        TestingUtils.compareWithCreatedOMElement(result);
    }

    public void testSuccessfulInvocation() throws Exception {
        System.out.println("Starting testSuccessfulInvocation");

        OMElement payload = TestingUtils.createDummyOMElement();
        ServiceClient sender = createClient();

        AxisOperation operation =
                sender.getAxisService().getOperation(ServiceClient.ANON_OUT_IN_OP);
        ArrayList operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(operationSpecificPhases);
        ArrayList phaseList = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(new TestHandler("CIn1"));
                operationSpecificPhase.addHandler(new TestHandler("CIn2"));
                operationSpecificPhase.addHandler(new TestHandler("CIn3"));
            }
        }

        operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setPhasesOutFlow(operationSpecificPhases);
        phaseList = operation.getPhasesOutFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(new TestHandler("COut1"));
                operationSpecificPhase.addHandler(new TestHandler("COut2"));
                operationSpecificPhase.addHandler(new TestHandler("COut3"));
            }
        }

        OMElement result = sender.sendReceive(payload);

        TestingUtils.compareWithCreatedOMElement(result);

        List expectedExecutionState = Arrays.asList(new String[] { "COut1", "COut2", "COut3", "In1",
                "In2", "In3", "In4", "In5", "In6", "Out1", "Out2", "Out3", "FCOut3", "FCOut2",
                "FCOut1", "FCIn6", "FCIn5", "FCIn4", "FCIn3", "FCIn2", "FCIn1", "FCCOut3",
                "FCCOut2", "FCCOut1", "CIn1", "CIn2", "CIn3", "FCCIn3", "FCCIn2", "FCCIn1" });
        assertEquals(expectedExecutionState, testResults);
    }

    public void testServersideFailureInService() throws Exception {
        System.out.println("Starting testServersideFailureInService");

        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement payload = omFactory.createOMElement("EchoOMElement", null);
        payload.setText(FaultThrowingService.THROW_FAULT_AS_AXIS_FAULT);

        ServiceClient sender = getClient(failingServiceName.getLocalPart(), failingOperationName.getLocalPart());
        sender.getOptions().setExceptionToBeThrownOnSOAPFault(true);
        
        AxisOperation operation =
                sender.getAxisService().getOperation(ServiceClient.ANON_OUT_IN_OP);
        ArrayList operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(operationSpecificPhases);
        ArrayList phaseList = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(new TestHandler("CIn1"));
                operationSpecificPhase.addHandler(new TestHandler("CIn2"));
                operationSpecificPhase.addHandler(new TestHandler("CIn3"));
            }
        }

        operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setPhasesOutFlow(operationSpecificPhases);
        phaseList = operation.getPhasesOutFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(new TestHandler("COut1"));
                operationSpecificPhase.addHandler(new TestHandler("COut2"));
                operationSpecificPhase.addHandler(new TestHandler("COut3"));
            }
        }
        try{
        	sender.sendReceive(payload);
        	fail("Expecting exception to be thrown.");
        }catch(AxisFault af){
        	assertEquals("TestFault", af.getFaultCode().getLocalPart());
        	assertTrue(af.getReason().indexOf("FaultReason") > -1);
        }
        //This odd pattern of CIn FCCIn CIn FCCIn is caused by the InOutAxisOperation always executing the inflow phases, even if there was a fault (and then executing the infaulflow)
        List expectedExecutionState = Arrays.asList(new String[] { "COut1", "COut2", "COut3", "In1",
                "In2", "In3", "In4", "In5", "In6", "FCIn6", "FCIn5", "FCIn4", "FCIn3", "FCIn2",
                "FCIn1", "FCCOut3", "FCCOut2", "FCCOut1", "CIn1", "CIn2", "CIn3", "FCCIn3",
                "FCCIn2", "FCCIn1", "CIn1", "CIn2", "CIn3", "FCCIn3", "FCCIn2", "FCCIn1" });
        //TODO : Need to fix the this test case
//    assertEquals(expectedExecutionState, testResults);
    }

    public void testServersideHandlerFailureInInboundOperationSpecificPhase() throws Exception {
        System.out.println("Starting testServersideHandlerFailureInInboundOperationSpecificPhase");

        middleOperationInHandler.shouldFail(true);
        try {
            executeClient();
            fail("An expected handler failure did not occur");
        }
        catch (AxisFault e) {
        }
        List expectedExecutionState = Arrays.asList(new String[] { "In1", "In2", "In3", "In4",
                "kaboom", "FCIn4", "FCIn3", "FCIn2", "FCIn1" });
        assertEquals(expectedExecutionState, testResults);
        middleOperationInHandler.shouldFail(false);
    }

    public void testServersideHandlerFailureInInboundOperationSpecificPhaseWithFirstHandler()
            throws Exception {
        System.out.println("Starting testServersideHandlerFailureInOutboundPhaseWithFirstHandler");
        firstOperationInHandler.shouldFail(true);
        try {
            executeClient();
            fail("An expected handler failure did not occur");
        }
        catch (AxisFault e) {
        }
        List expectedExecutionState = Arrays.asList(
                new String[] { "In1", "In2", "In3", "kaboom", "FCIn3", "FCIn2", "FCIn1" });
        assertEquals(expectedExecutionState, testResults);
        firstOperationInHandler.shouldFail(false);
    }

    public void testServersideHandlerFailureInInboundGlobalPhase() throws Exception {
        System.out.println("Starting testServersideHandlerFailureInGlobalPhase");

        middleGlobalInHandler.shouldFail(true);
        try {
            executeClient();
            fail("An expected handler failure did not occur");
        }
        catch (AxisFault e) {
        }
        List expectedExecutionState = Arrays.asList(new String[] {"In1", "kaboom","FCIn1"});
        assertEquals(expectedExecutionState, testResults);
        middleGlobalInHandler.shouldFail(false);
    }

    public void testServersideHandlerFailureInOutboundPhase() throws Exception {
        System.out.println("Starting testServersideHandlerFailureInOutboundPhase");

        middleOperationOutHandler.shouldFail(true);
        try {
            executeClient();
            fail("An expected handler failure did not occur");
        }
        catch (AxisFault e) {
        }
        List expectedExecutionState = Arrays.asList(new String[] { "In1", "In2", "In3", "In4",
                "In5", "In6", "Out1", "kaboom", "FCOut1", "FCIn6", "FCIn5", "FCIn4", "FCIn3",
                "FCIn2", "FCIn1" });
        assertEquals(expectedExecutionState, testResults);
        middleOperationOutHandler.shouldFail(false);
    }

    public void testClientsideHandlerFailureInOutboundPhaseWithFirstHandler() throws Exception {
        System.out.println("Starting testClientsideHandlerFailureInOutboundPhaseWithFirstHandler");

        OMElement payload = TestingUtils.createDummyOMElement();
        ServiceClient sender = createClient();

        AxisOperation operation =
                sender.getAxisService().getOperation(ServiceClient.ANON_OUT_IN_OP);
        ArrayList operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setPhasesOutFlow(operationSpecificPhases);
        ArrayList phaseList = operation.getPhasesOutFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                //phase1.addHandler(new TestHandler("COut1"));
                TestHandler clientOutboundHandler = new TestHandler("COut1");
                clientOutboundHandler.shouldFail(true);
                operationSpecificPhase.addHandler(clientOutboundHandler);
                //phase1.addHandler(clientOutboundHandler);
            }
        }

        try {
            OMElement result = sender.sendReceive(payload);
            fail("An expected handler failure did not occur");
        }
        catch (AxisFault e) {
        }
        List expectedExecutionState = Arrays.asList(new String[] { "kaboom" });
        assertEquals(expectedExecutionState, testResults);
    }

    public void testClientsideHandlerFailureInOutboundPhase() throws Exception {
        System.out.println("Starting testClientsideHandlerFailureInOutboundPhase");

        OMElement payload = TestingUtils.createDummyOMElement();
        ServiceClient sender = createClient();

        AxisOperation operation =
                sender.getAxisService().getOperation(ServiceClient.ANON_OUT_IN_OP);
        ArrayList operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setPhasesOutFlow(operationSpecificPhases);
        ArrayList phaseList = operation.getPhasesOutFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(new TestHandler("COut1"));
                TestHandler clientOutboundHandler = new TestHandler("COut2");
                clientOutboundHandler.shouldFail(true);
                operationSpecificPhase.addHandler(clientOutboundHandler);
                operationSpecificPhase.addHandler(new TestHandler("COut3"));
            }
        }

        try {
            OMElement result = sender.sendReceive(payload);
            fail("An expected handler failure did not occur");
        }
        catch (AxisFault e) {
        }
        List expectedExecutionState = Arrays.asList(new String[] { "COut1", "kaboom", "FCCOut1" });
        assertEquals(expectedExecutionState, testResults);

    }

    public void testClientsideHandlerFailureInInboundOperationSpecificPhase() throws Exception {
        System.out.println("Starting testClientsideHandlerFailureInInboundOperationSpecific");

        OMElement payload = TestingUtils.createDummyOMElement();
        ServiceClient sender = createClient();

        AxisOperation operation =
                sender.getAxisService().getOperation(ServiceClient.ANON_OUT_IN_OP);
        ArrayList operationSpecificPhases = new ArrayList();
        operationSpecificPhases.add(new Phase(PhaseMetadata.PHASE_POLICY_DETERMINATION));
        operation.setRemainingPhasesInFlow(operationSpecificPhases);
        ArrayList phaseList = operation.getRemainingPhasesInFlow();
        for (int i = 0; i < phaseList.size(); i++) {
            Phase operationSpecificPhase = (Phase)phaseList.get(i);
            if (PhaseMetadata.PHASE_POLICY_DETERMINATION
                    .equals(operationSpecificPhase.getPhaseName())) {
                operationSpecificPhase.addHandler(new TestHandler("CIn1"));
                TestHandler clientOutboundHandler = new TestHandler("CIn2");
                clientOutboundHandler.shouldFail(true);
                operationSpecificPhase.addHandler(clientOutboundHandler);
                operationSpecificPhase.addHandler(new TestHandler("CIn3"));
            }
        }

        try {
            OMElement result = sender.sendReceive(payload);
            fail("An expected handler failure did not occur");
        }
        catch (AxisFault e) {
        }

        List expectedExecutionState = Arrays.asList(new String[] { "In1", "In2", "In3", "In4",
                "In5", "In6", "Out1", "Out2", "Out3", "FCOut3", "FCOut2", "FCOut1", "FCIn6",
                "FCIn5", "FCIn4", "FCIn3", "FCIn2", "FCIn1", "CIn1", "kaboom", "FCCIn1" });
        assertEquals(expectedExecutionState, testResults);
    }

    private class TestHandler extends AbstractHandler {
        private String handlerName;
        private boolean shouldFail;
        private boolean shouldPause;

        public TestHandler(String handlerName) {
            this.handlerName = handlerName;
        }

        public void shouldFail(boolean fail) {
            this.shouldFail = fail;
        }

        public void shouldPause(boolean pause) {
            this.shouldPause = pause;
        }

        public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
            System.out.println("TestHandler " + handlerName + " invoked");
            if (shouldFail) {
                testResults.add("kaboom");
                System.out.println("Handler went kaboom");
                throw new AxisFault("Handler failed");
            }
            testResults.add(handlerName);
            return InvocationResponse.CONTINUE;
        }

        public void flowComplete(MessageContext msgContext) {
            System.out.println("TestHandler " + handlerName + " called for flowComplete()");
            testResults.add("FC" + handlerName);
        }
    }
}
