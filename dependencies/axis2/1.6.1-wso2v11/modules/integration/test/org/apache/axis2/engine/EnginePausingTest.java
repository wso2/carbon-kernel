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

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBinding;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.AxisEndpoint;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.dispatchers.AddressingBasedDispatcher;
import org.apache.axis2.dispatchers.RequestURIBasedDispatcher;
import org.apache.axis2.dispatchers.SOAPActionBasedDispatcher;
import org.apache.axis2.dispatchers.SOAPMessageBodyBasedDispatcher;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.receivers.RawXMLINOnlyMessageReceiver;
import org.apache.axis2.receivers.RawXMLINOutMessageReceiver;
import org.apache.axis2.transport.http.CommonsHTTPTransportSender;

import javax.xml.namespace.QName;
import java.util.ArrayList;

public class EnginePausingTest extends TestCase {

    public static final String FAULT_IDX = "FaultIndex";
    public static final String FAULT_REASON = "I faulted because I was told to!";

    private QName serviceName = new QName("NullService");
    private QName operationName = new QName("DummyOp");
    private ConfigurationContext configContext;

    private TransportOutDescription transportOut;
    private TransportInDescription transportIn;
    private MessageContext mc;
    private ArrayList executedHandlers;

    public EnginePausingTest(String arg0) {
        super(arg0);
        executedHandlers = new ArrayList();
        AxisConfiguration engineRegistry = new AxisConfiguration();
        configContext = new ConfigurationContext(engineRegistry);
        configContext.setServicePath(Constants.DEFAULT_SERVICES_PATH);
        configContext.setContextRoot("axis2");
        transportOut = new TransportOutDescription("null");
        transportOut.setSender(new CommonsHTTPTransportSender());
        transportIn = new TransportInDescription("null");

    }

    protected void setUp() throws Exception {

        AxisService service = new AxisService(serviceName.getLocalPart());
        configContext.getAxisConfiguration().addService(service);
        configContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-only", new RawXMLINOnlyMessageReceiver());
        configContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-out", new RawXMLINOutMessageReceiver());

        DispatchPhase dispatchPhase = new DispatchPhase();

        dispatchPhase.setName("Dispatch");

        AddressingBasedDispatcher abd = new AddressingBasedDispatcher();

        abd.initDispatcher();

        RequestURIBasedDispatcher rud = new RequestURIBasedDispatcher();

        rud.initDispatcher();

        SOAPActionBasedDispatcher sabd = new SOAPActionBasedDispatcher();

        sabd.initDispatcher();

        SOAPMessageBodyBasedDispatcher smbd = new SOAPMessageBodyBasedDispatcher();

        smbd.initDispatcher();

        dispatchPhase.addHandler(abd);
        dispatchPhase.addHandler(rud);
        dispatchPhase.addHandler(sabd);
        dispatchPhase.addHandler(smbd);
        configContext.getAxisConfiguration().getInFlowPhases().add(dispatchPhase);
        AxisOperation axisOp = new InOutAxisOperation(operationName);
        axisOp.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {

            }
        });
        service.addOperation(axisOp);

        AxisEndpoint endpoint = new AxisEndpoint();
        endpoint.setName("NullService");

        AxisBinding binding = new AxisBinding();
        AxisBindingOperation bindingOp = new AxisBindingOperation();

        bindingOp.setName(axisOp.getName());
        bindingOp.setAxisOperation(axisOp);
        binding.addChild(bindingOp);
        endpoint.setBinding(binding);
        service.addEndpoint(endpoint.getName(), endpoint);
        service.setEndpointName(endpoint.getName());

        service.mapActionToOperation(operationName.getLocalPart(), axisOp);

        mc = configContext.createMessageContext();
        mc.setTransportIn(transportIn);
        mc.setTransportOut(transportOut);

        mc.setTransportOut(transportOut);
        mc.setServerSide(true);
//        mc.setProperty(MessageContext.TRANSPORT_OUT, System.out);
        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(omFac.getDefaultEnvelope());

        Phase phase1 = new Phase("1");
        phase1.addHandler(new TempHandler(1));
        phase1.addHandler(new TempHandler(2));
        phase1.addHandler(new TempHandler(3));
        phase1.addHandler(new TempHandler(4));
        phase1.addHandler(new TempHandler(5));
        phase1.addHandler(new TempHandler(6));
        phase1.addHandler(new TempHandler(7));
        phase1.addHandler(new TempHandler(8));
        phase1.addHandler(new TempHandler(9));

        Phase phase2 = new Phase("2");
        phase2.addHandler(new TempHandler(10));
        phase2.addHandler(new TempHandler(11));
        phase2.addHandler(new TempHandler(12));
        phase2.addHandler(new TempHandler(13));
        phase2.addHandler(new TempHandler(14));
        phase2.addHandler(new TempHandler(15, true));
        phase2.addHandler(new TempHandler(16));
        phase2.addHandler(new TempHandler(17));
        phase2.addHandler(new TempHandler(18));

        Phase phase3 = new Phase("3");
        phase3.addHandler(new TempHandler(19));
        phase3.addHandler(new TempHandler(20));
        phase3.addHandler(new TempHandler(21));
        phase3.addHandler(new TempHandler(22));
        phase3.addHandler(new TempHandler(23));
        phase3.addHandler(new TempHandler(24));
        phase3.addHandler(new TempHandler(25));
        phase3.addHandler(new TempHandler(26));
        phase3.addHandler(new TempHandler(27));

        //TODO
        axisOp.getRemainingPhasesInFlow().add(phase1);
        axisOp.getRemainingPhasesInFlow().add(phase2);
        axisOp.getRemainingPhasesInFlow().add(phase3);

        mc.setWSAAction(operationName.getLocalPart());
        mc.setSoapAction(operationName.getLocalPart());
//        System.out.flush();

    }

    public void testReceive() throws Exception {
        mc.setTo(new EndpointReference("/axis2/services/NullService"));
        mc.setWSAAction("DummyOp");
        AxisEngine.receive(mc);
        assertEquals(14, executedHandlers.size());
        for (int i = 0; i < 14; i++) {
            assertEquals(((Integer) executedHandlers.get(i)).intValue(),
                         i + 1);
        }
        AxisEngine.resume(mc);

        assertEquals(27, executedHandlers.size());
        for (int i = 15; i < 27; i++) {
            assertEquals(((Integer) executedHandlers.get(i)).intValue(),
                         i + 1);
        }
    }

    public void testFlowComplete() throws Exception {
        mc.setTo(new EndpointReference("/axis2/services/NullService"));
        mc.setWSAAction("DummyOp");

        // Fault on Handler #5
        mc.setProperty(FAULT_IDX, new Integer(5));

        try {
            AxisEngine.receive(mc);
        } catch (AxisFault axisFault) {
            // Expected this fault.
            assertEquals(4, executedHandlers.size());
            return;
        }
        fail("Expected fault did not occur");
    }

    public class TempHandler extends AbstractHandler {
        private Integer index;
        private boolean pause = false;

        public TempHandler(int index, boolean pause) {
            this.index = new Integer(index);
            this.pause = pause;
            init(new HandlerDescription("handler" + index));
        }

        public TempHandler(int index) {
            this.index = new Integer(index);
            init(new HandlerDescription("handler" + index));
        }

        public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
            if (pause) {
                msgContext.pause();
                pause = false;
                return InvocationResponse.SUSPEND;
            }

            Integer faultIndex = (Integer)msgContext.getProperty(FAULT_IDX);
            if (faultIndex != null && faultIndex.equals(index)) {
                throw new AxisFault(FAULT_REASON);
            }

            executedHandlers.add(index);
            return InvocationResponse.CONTINUE;
        }


        public void flowComplete(MessageContext msgContext) {
            if (msgContext.getProperty(FAULT_IDX) != null) {
                // If we're here on an invocation where someone was supposed to fault...
                AxisFault fault = (AxisFault)msgContext.getFailureReason();
                assertNotNull("No fault!", fault);

                // Make sure it's the right fault.
                assertEquals(FAULT_REASON, fault.getReason());
            }
        }
    }
}
