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
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.context.externalize.ActivateUtils;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
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
import org.apache.axis2.transport.http.SimpleHTTPServer;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MessageContextSaveATest extends TestCase {
    protected static final Log log = LogFactory.getLog(MessageContextSaveATest.class);

    private QName serviceName = new QName("TestService");
    private QName operationName = new QName("Operation_1");

    private ConfigurationContext configurationContext = null;
    private ServiceGroupContext serviceGroupContext = null;
    private OperationContext operationContext = null;

    private AxisServiceGroup axisServiceGroup = null;
    private AxisService axisService = null;
    private AxisOperation axisOperation = null;
    private AxisMessage axisMessage = null;

    private Phase phase1 = null;
    private Phase phase2 = null;
    private Phase phase3 = null;
    private Phase phase4 = null;
    private Phase phase5 = null;
    private Phase phase6 = null;
    private Phase phase7 = null;

    private MessageContext mc = null;
    private MessageContext mc2 = null;

    private ArrayList executedHandlers = null;

    public MessageContextSaveATest(String arg0) {
        super(arg0);

        try {
            prepare();
        }
        catch (Exception e) {
            log.debug("MessageContextSaveATest:constructor:  error in setting up object graph [" +
                    e.getClass().getName() + " : " + e.getMessage() + "]");
        }
    }


    //
    // prepare the object hierarchy for testing
    //
    private void prepare() throws Exception {
        //-----------------------------------------------------------------

        AxisConfiguration axisConfiguration = new AxisConfiguration();

        configurationContext = new ConfigurationContext(axisConfiguration);

        configurationContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-only", new RawXMLINOnlyMessageReceiver());
        configurationContext.getAxisConfiguration().addMessageReceiver(
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

        configurationContext.getAxisConfiguration().getInFlowPhases().add(dispatchPhase);

        //-----------------------------------------------------------------

        axisServiceGroup = new AxisServiceGroup(axisConfiguration);
        axisServiceGroup.setServiceGroupName("ServiceGroupTest");


        axisService = new AxisService(serviceName.getLocalPart());
        axisServiceGroup.addService(axisService);


        axisOperation = new InOutAxisOperation(operationName);
        axisOperation.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {

            }
        });

        axisService.addOperation(axisOperation);
        axisService.mapActionToOperation(operationName.getLocalPart(), axisOperation);


        configurationContext.getAxisConfiguration().addService(axisService);

        //-----------------------------------------------------------------

        serviceGroupContext = configurationContext.
                createServiceGroupContext(axisService.getAxisServiceGroup());
        serviceGroupContext.setId("ServiceGroupContextTest");

        ServiceContext serviceContext = serviceGroupContext.getServiceContext(axisService);

        operationContext = serviceContext.createOperationContext(operationName);

        //-----------------------------------------------------------------

        TransportOutDescription transportOut = new TransportOutDescription("null");
        TransportOutDescription transportOut2 = new TransportOutDescription("happy");
        TransportOutDescription transportOut3 = new TransportOutDescription("golucky");
        transportOut.setSender(new CommonsHTTPTransportSender());
        transportOut2.setSender(new CommonsHTTPTransportSender());
        transportOut3.setSender(new CommonsHTTPTransportSender());
        axisConfiguration.addTransportOut(transportOut3);
        axisConfiguration.addTransportOut(transportOut2);
        axisConfiguration.addTransportOut(transportOut);

        TransportInDescription transportIn = new TransportInDescription("null");
        TransportInDescription transportIn2 = new TransportInDescription("always");
        TransportInDescription transportIn3 = new TransportInDescription("thebest");
        transportIn.setReceiver(new SimpleHTTPServer());
        transportIn2.setReceiver(new SimpleHTTPServer());
        transportIn3.setReceiver(new SimpleHTTPServer());
        axisConfiguration.addTransportIn(transportIn2);
        axisConfiguration.addTransportIn(transportIn);
        axisConfiguration.addTransportIn(transportIn3);

        //-----------------------------------------------------------------

        mc = configurationContext.createMessageContext();
        mc.setTransportIn(transportIn);
        mc.setTransportOut(transportOut);

        mc.setServerSide(true);
//        mc.setProperty(MessageContext.TRANSPORT_OUT, System.out);

        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(omFac.getDefaultEnvelope());

        phase1 = new Phase("beginPhase1");
        phase1.addHandler(new TempHandler(1));
        phase1.addHandler(new TempHandler(2));
        phase1.addHandler(new TempHandler(3));
        phase1.addHandler(new TempHandler(4));
        phase1.addHandler(new TempHandler(5));
        phase1.addHandler(new TempHandler(6));
        phase1.addHandler(new TempHandler(7));
        phase1.addHandler(new TempHandler(8));
        phase1.addHandler(new TempHandler(9));

        phase2 = new Phase("middlePhase2");
        phase2.addHandler(new TempHandler(10));
        phase2.addHandler(new TempHandler(11));
        phase2.addHandler(new TempHandler(12));
        phase2.addHandler(new TempHandler(13));
        phase2.addHandler(new TempHandler(14));
        phase2.addHandler(new TempHandler(15, true));
        phase2.addHandler(new TempHandler(16));
        phase2.addHandler(new TempHandler(17));
        phase2.addHandler(new TempHandler(18));

        phase3 = new Phase("lastPhase3");
        phase3.addHandler(new TempHandler(19));
        phase3.addHandler(new TempHandler(20));
        phase3.addHandler(new TempHandler(21));
        phase3.addHandler(new TempHandler(22));
        phase3.addHandler(new TempHandler(23));
        phase3.addHandler(new TempHandler(24));
        phase3.addHandler(new TempHandler(25));
        phase3.addHandler(new TempHandler(26));
        phase3.addHandler(new TempHandler(27));

        phase4 = new Phase("extraPhase1");
        phase4.addHandler(new TempHandler(28));
        phase4.addHandler(new TempHandler(29));

        phase5 = new Phase("extraPhase2");
        phase5.addHandler(new TempHandler(30));

        phase6 = new Phase("extraPhase3");
        phase6.addHandler(new TempHandler(31, true));
        phase6.addHandler(new TempHandler(32));

        phase7 = new Phase("extraPhase4");
        phase7.addHandler(new TempHandler(33));
        phase7.addHandler(new TempHandler(34));
        phase7.addHandler(new TempHandler(35));

        axisOperation.getRemainingPhasesInFlow().add(phase1);
        axisOperation.getRemainingPhasesInFlow().add(phase2);
        axisOperation.getRemainingPhasesInFlow().add(phase3);
        axisOperation.getRemainingPhasesInFlow().add(phase4);
        axisOperation.getRemainingPhasesInFlow().add(phase5);
        axisOperation.getRemainingPhasesInFlow().add(phase6);
        axisOperation.getRemainingPhasesInFlow().add(phase7);

        ArrayList phases = new ArrayList();
        phases.add(phase1);
        phases.add(phase2);
        phases.add(phase3);
        phases.add(phase4);
        phases.add(phase5);
        phases.add(phase6);
        phases.add(phase7);
        axisConfiguration.setInPhasesUptoAndIncludingPostDispatch(phases);

        mc.setWSAAction(operationName.getLocalPart());
        mc.setSoapAction(operationName.getLocalPart());
//        System.out.flush();

        mc.setMessageID(UUIDGenerator.getUUID());

        //operationContext.addMessageContext(mc);  gets done via the register
        axisOperation.registerOperationContext(mc, operationContext);
        mc.setOperationContext(operationContext);
        mc.setServiceContext(serviceContext);

        mc.setTo(new EndpointReference("axis2/services/NullService"));
        mc.setWSAAction("DummyOp");

        axisMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        mc.setAxisMessage(axisMessage);

        //-----------------------------------------------------------------

        executedHandlers = new ArrayList();
    }


    protected void setUp() throws Exception {
        //org.apache.log4j.BasicConfigurator.configure();
    }


    public void testReceive() throws Exception {

        log.debug(
                "MessageContextSaveATest:testReceive(): start - - engine.receive(mc) - - - - - - - - - - - - - - - -");
        AxisEngine.receive(mc);

        log.debug(
                "MessageContextSaveATest:testReceive(): resume - - engine.resume(mc) - - - - - - - - - - - - - - - -");
        AxisEngine.resume(mc);

        assertEquals(30, executedHandlers.size());
        for (int i = 15; i < 30; i++) {
            assertEquals(((Integer) executedHandlers.get(i)).intValue(), i + 1);
        }

        // get the phase lists and see if they match up
        ArrayList restoredPhases = mc2.getExecutionChain();
        int it_count = 0;

        Iterator it = restoredPhases.iterator();
        while (it.hasNext()) {
            // we know everything at this level is a Phase.  
            // if you change it, you might get a ClassCastException 
            Phase restored_phase = (Phase) it.next();

            Phase original_phase = null;

            it_count++;

            if (it_count == 1) {
                original_phase = phase1;
            } else if (it_count == 2) {
                original_phase = phase2;
            } else if (it_count == 3) {
                original_phase = phase3;
            } else if (it_count == 4) {
                original_phase = phase4;
            } else if (it_count == 5) {
                original_phase = phase5;
            } else if (it_count == 6) {
                original_phase = phase6;
            } else if (it_count == 7) {
                original_phase = phase7;
            } else {
                // unexpected
                assertTrue(false);
            }

            boolean isOk = comparePhases(restored_phase, original_phase);
            assertTrue(isOk);
        }

        // -------------------------------------------------------------------
        // second resume to start the second pause
        // -------------------------------------------------------------------
        log.debug(
                "MessageContextSaveATest:testReceive(): resume - - engine.resume(mc) - - - - - - - - - - - - - - - -");
        AxisEngine.resume(mc);

        assertEquals(35, executedHandlers.size());
        for (int i = 31; i < 35; i++) {
            assertEquals(((Integer) executedHandlers.get(i)).intValue(), i + 1);
        }

        // get the phase lists and see if they match up
        restoredPhases = mc2.getExecutionChain();
        it_count = 0;

        it = restoredPhases.iterator();
        while (it.hasNext()) {
            // we know everything at this level is a Phase.  
            // if you change it, you might get a ClassCastException 
            Phase restored_phase = (Phase) it.next();

            Phase original_phase = null;

            it_count++;

            if (it_count == 1) {
                original_phase = phase1;
            } else if (it_count == 2) {
                original_phase = phase2;
            } else if (it_count == 3) {
                original_phase = phase3;
            } else if (it_count == 4) {
                original_phase = phase4;
            } else if (it_count == 5) {
                original_phase = phase5;
            } else if (it_count == 6) {
                original_phase = phase6;
            } else if (it_count == 7) {
                original_phase = phase7;
            } else {
                // unexpected
                assertTrue(false);
            }

            boolean isOk = comparePhases(restored_phase, original_phase);
            assertTrue(isOk);
        }
    }


    /**
     * Gets the ID associated with the handler object.
     *
     * @param o The handler object
     * @return The ID associated with the handler,
     *         -1 otherwise
     */
    private int getHandlerID(Object o) {
        int id = -1;

        if (o instanceof TempHandler) {
            id = ((TempHandler) o).getHandlerID();
        }

        return id;
    }


    /**
     * Check the handler objects to see if they are equivalent.
     *
     * @param o1 The first handler
     * @param o2 The second handler
     * @return TRUE if the handler objects are equivalent,
     *         FALSE otherwise
     */
    private boolean compareHandlers(Object o1, Object o2) {
        if ((o1 == null) && (o2 == null)) {
            return true;
        }

        if ((o1 != null) && (o2 != null)) {
            String c1 = o1.getClass().getName();
            String c2 = o2.getClass().getName();

            if (c1.equals(c2)) {
                log.debug("MessageContextSaveATest::compareHandlers:  class [" + c1 + "] match ");

                int id1 = getHandlerID(o1);
                int id2 = getHandlerID(o2);

                if (id1 == id2) {
                    log.debug("MessageContextSaveATest::compareHandlers:  id [" + id1 + "] match");
                    return true;
                } else {
                    log.debug("MessageContextSaveATest::compareHandlers:  id1 [" + id1 +
                            "] != id2 [" + id2 + "] ");
                    return false;
                }
            } else {
                log.debug("MessageContextSaveATest::compareHandlers:  class1 [" + c1 +
                        "] != class2 [" + c2 + "]   ");
                return false;
            }
        }

        return false;
    }


    /**
     * Compare two phases.
     *
     * @param o1 The first phase object
     * @param o2 The second phase object
     * @return TRUE if the phases are equivalent,
     *         FALSE otherwise
     */
    private boolean comparePhases(Object o1, Object o2) {
        if ((o1 == null) && (o2 == null)) {
            log.debug(
                    "MessageContextSaveATest: comparePhases:  Phase1[] == Phase2[] - both null objects");
            return true;
        }

        if (((o1 != null) && (o2 != null))
                && ((o1 instanceof Phase) && (o2 instanceof Phase))
                ) {

            try {
                Phase p1 = (Phase) o1;
                Phase p2 = (Phase) o2;

                String name1 = p1.getName();
                String name2 = p2.getName();

                List list1 = p1.getHandlers();
                List list2 = p2.getHandlers();

                if ((list1 == null) && (list2 == null)) {
                    log.debug("MessageContextSaveATest: comparePhases:  Phase1[" + name1 +
                            "] == Phase2[" + name2 + "]");
                    return true;
                }

                if ((list1 != null) && (list2 != null)) {
                    int size1 = list1.size();
                    int size2 = list2.size();

                    if (size1 != size2) {
                        log.debug("MessageContextSaveATest: comparePhases:  Phase1[" +
                                name1 + "] != Phase2[" + name2 +
                                "] - mismatched size of handler lists");
                        return false;
                    }

                    for (int j = 0; j < size1; j++) {
                        Object obj1 = list1.get(j);
                        Object obj2 = list2.get(j);

                        if ((obj1 == null) && (obj2 == null)) {
                            // ok
                        } else if ((obj1 != null) && (obj2 != null)) {
                            boolean check;

                            if (obj1 instanceof Phase) {
                                check = comparePhases(obj1, obj2);
                            } else {
                                // must be a handler
                                check = compareHandlers(obj1, obj2);
                            }

                            if (!check) {
                                log.debug(
                                        "MessageContextSaveATest: comparePhases:  Phase1[" + name1 +
                                                "] != Phase2[" + name2 +
                                                "] - mismatched handler lists");
                                return false;
                            }
                        } else {
                            // mismatch
                            log.debug("MessageContextSaveATest: comparePhases:  Phase1[" +
                                    name1 + "] != Phase2[" + name2 +
                                    "] - mismatched handler lists");
                            return false;
                        }
                    }

                    // if we got here, the comparison completed ok
                    // with a match

                    log.debug("MessageContextSaveATest: comparePhases:  Phase1[" + name1 +
                            "] == Phase2[" + name2 + "] - matched handler lists");
                    return true;
                }

            }
            catch (Exception e) {
                // some error
                e.printStackTrace();
            }
        }

        log.debug("MessageContextSaveATest: comparePhases:  Phase1[] != Phase2[]");
        return false;
    }

    private void showMcMap(HashMap map) {
        if ((map != null) && (!map.isEmpty())) {
            Iterator itList = map.keySet().iterator();

            while (itList.hasNext()) {
                String key = (String) itList.next();

                MessageContext value = (MessageContext) map.get(key);
                String valueID = null;

                if (value != null) {
                    valueID = value.getMessageID();

                    log.debug(
                            "MessageContextSaveATest: showMcMap:  Message context   ID[" + valueID +
                                    "]   Key Label [" + key + "]");

                }
            }
        } else {
            log.debug(
                    "MessageContextSaveATest: showMcMap:  No entries to display for message contexts table.");
        }
    }


    // this checks the save/restore of a message context that hasn't been
    // through the engine to simulate what some WS-RM implementations 
    // need to do - make a simple message context for a RM ack or other
    // simple message
    public void testSimpleMC() throws Exception {
        String title = "MessageContextSaveATest:testSimpleMC(): ";
        log.debug(title + "start - - - - - - - - - - - - - - - -");

        MessageContext simpleMsg = new MessageContext();
        MessageContext restoredSimpleMsg = null;

        File theFile = null;
        String theFilename = null;

        boolean savedMessageContext = false;
        boolean restoredMessageContext = false;
        boolean comparesOk = false;

        try {
            theFile = File.createTempFile("Simple", null);
            theFilename = theFile.getName();
            log.debug(title + "temp file = [" + theFilename + "]");
        }
        catch (Exception ex) {
            log.debug(title + "error creating temp file = [" + ex.getMessage() + "]");
            theFile = null;
        }

        if (theFile != null) {
            // ---------------------------------------------------------
            // save to the temporary file
            // ---------------------------------------------------------
            try {
                // setup an output stream to a physical file
                FileOutputStream outStream = new FileOutputStream(theFile);

                // attach a stream capable of writing objects to the 
                // stream connected to the file
                ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

                // try to save the message context
                log.debug(title + "saving message context.....");
                savedMessageContext = false;
                outObjStream.writeObject(simpleMsg);

                // close out the streams
                outObjStream.flush();
                outObjStream.close();
                outStream.flush();
                outStream.close();

                savedMessageContext = true;
                log.debug(title + "....saved message context.....");

                long filesize = theFile.length();
                log.debug(title + "file size after save [" + filesize +
                        "]   temp file = [" + theFilename + "]");

            }
            catch (Exception ex2) {
                log.debug(title + "error with saving message context = [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(savedMessageContext);

            // ---------------------------------------------------------
            // restore from the temporary file
            // ---------------------------------------------------------
            try {
                // setup an input stream to the file
                FileInputStream inStream = new FileInputStream(theFile);

                // attach a stream capable of reading objects from the 
                // stream connected to the file
                ObjectInputStream inObjStream = new ObjectInputStream(inStream);

                // try to restore the message context
                log.debug(title + "restoring a message context.....");
                restoredMessageContext = false;

                restoredSimpleMsg = (MessageContext) inObjStream.readObject();
                inObjStream.close();
                inStream.close();

                restoredSimpleMsg.activate(configurationContext);

                restoredMessageContext = true;
                log.debug(title + "....restored message context.....");

                // compare to original execution chain
                ArrayList restored_execChain = restoredSimpleMsg.getExecutionChain();
                ArrayList orig_execChain = simpleMsg.getExecutionChain();

                comparesOk =
                        ActivateUtils.isEquivalent(restored_execChain, orig_execChain, false);
                log.debug(title + "execution chain equivalency [" + comparesOk + "]");
                assertTrue(comparesOk);

                // check executed list
                Iterator restored_executed_it = restoredSimpleMsg.getExecutedPhases();
                Iterator orig_executed_it = simpleMsg.getExecutedPhases();
                if ((restored_executed_it != null) && (orig_executed_it != null)) {
                    while (restored_executed_it.hasNext() && orig_executed_it.hasNext()) {
                        Object p1 = restored_executed_it.next();
                        Object p2 = orig_executed_it.next();

                        comparesOk = comparePhases(p1, p2);
                        log.debug(title + "executed phase list:  compare phases [" +
                                comparesOk + "]");
                        assertTrue(comparesOk);
                    }
                } else {
                    // problem with the executed lists
                    assertTrue(false);
                }

            }
            catch (Exception ex2) {
                log.debug(title + "error with saving message context = [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(restoredMessageContext);

            // if the save/restore of the message context succeeded,
            // then don't keep the temporary file around
            boolean removeTmpFile = savedMessageContext && restoredMessageContext && comparesOk;
            if (removeTmpFile) {
                try {
                    theFile.delete();
                }
                catch (Exception e) {
                    // just absorb it
                }
            }
        }

        log.debug(title + "end - - - - - - - - - - - - - - - -");
    }


    // this checks the save/restore of a message context that has
    // some properties set
    public void testMcProperties() throws Exception {
        String title = "MessageContextSaveATest:testMcProperties(): ";
        log.debug(title + "start - - - - - - - - - - - - - - - -");

        MessageContext simpleMsg = new MessageContext();
        MessageContext restoredSimpleMsg = null;

        simpleMsg.setProperty("key1", "value1");
        simpleMsg.setProperty("key2", null);
        simpleMsg.setProperty("key3", new Integer(3));
        simpleMsg.setProperty("key4", new Long(4L));


        File theFile = null;
        String theFilename = null;

        boolean pause = false;
        boolean savedMessageContext = false;
        boolean restoredMessageContext = false;
        boolean comparesOk = false;

        try {
            theFile = File.createTempFile("McProps", null);
            theFilename = theFile.getName();
            log.debug(title + "temp file = [" + theFilename + "]");
        }
        catch (Exception ex) {
            log.debug(title + "error creating temp file = [" + ex.getMessage() + "]");
            theFile = null;
        }

        if (theFile != null) {
            // ---------------------------------------------------------
            // save to the temporary file
            // ---------------------------------------------------------
            try {
                // setup an output stream to a physical file
                FileOutputStream outStream = new FileOutputStream(theFile);

                // attach a stream capable of writing objects to the 
                // stream connected to the file
                ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

                // try to save the message context
                log.debug(title + "saving message context.....");
                savedMessageContext = false;
                outObjStream.writeObject(simpleMsg);

                // close out the streams
                outObjStream.flush();
                outObjStream.close();
                outStream.flush();
                outStream.close();

                savedMessageContext = true;
                log.debug(title + "....saved message context.....");

                long filesize = theFile.length();
                log.debug(title + "file size after save [" + filesize +
                        "]   temp file = [" + theFilename + "]");

            }
            catch (Exception ex2) {
                log.debug(title + "error with saving message context = [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(savedMessageContext);

            // ---------------------------------------------------------
            // restore from the temporary file
            // ---------------------------------------------------------
            try {
                // setup an input stream to the file
                FileInputStream inStream = new FileInputStream(theFile);

                // attach a stream capable of reading objects from the 
                // stream connected to the file
                ObjectInputStream inObjStream = new ObjectInputStream(inStream);

                // try to restore the message context
                log.debug(title + "restoring a message context.....");
                restoredMessageContext = false;

                restoredSimpleMsg = (MessageContext) inObjStream.readObject();
                inObjStream.close();
                inStream.close();

                restoredSimpleMsg.activate(configurationContext);

                restoredMessageContext = true;
                log.debug(title + "....restored message context.....");

                // compare to original execution chain
                ArrayList restored_execChain = restoredSimpleMsg.getExecutionChain();
                ArrayList orig_execChain = simpleMsg.getExecutionChain();

                comparesOk =
                    ActivateUtils.isEquivalent(restored_execChain, orig_execChain, false);
                log.debug(title + "execution chain equivalency [" + comparesOk + "]");
                assertTrue(comparesOk);

                // check executed list
                Iterator restored_executed_it = restoredSimpleMsg.getExecutedPhases();
                Iterator orig_executed_it = simpleMsg.getExecutedPhases();
                if ((restored_executed_it != null) && (orig_executed_it != null)) {
                    while (restored_executed_it.hasNext() && orig_executed_it.hasNext()) {
                        Object p1 = restored_executed_it.next();
                        Object p2 = orig_executed_it.next();

                        comparesOk = comparePhases(p1, p2);
                        log.debug(title + "executed phase list:  compare phases [" +
                                comparesOk + "]");
                        assertTrue(comparesOk);
                    }
                } else {
                    // problem with the executed lists
                    assertTrue(false);
                }

                // check the properties
                String value1 = (String) restoredSimpleMsg.getProperty("key1");
                Object value2 = restoredSimpleMsg.getProperty("key2");
                Integer value3 = (Integer) restoredSimpleMsg.getProperty("key3");
                Long value4 = (Long) restoredSimpleMsg.getProperty("key4");

                assertEquals("value1", value1);
                assertNull(value2);

                boolean isOk = false;
                if ((value3 != null) && value3.equals(new Integer(3))) {
                    isOk = true;
                }
                assertTrue(isOk);

                if ((value4 != null) && value4.equals(new Long(4L))) {
                    isOk = true;
                }
                assertTrue(isOk);

            }
            catch (Exception ex2) {
                log.debug(title + "error with restoring message context = [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(restoredMessageContext);

            // if the save/restore of the message context succeeded,
            // then don't keep the temporary file around
            boolean removeTmpFile = savedMessageContext && restoredMessageContext && comparesOk;
            if (removeTmpFile) {
                try {
                    theFile.delete();
                }
                catch (Exception e) {
                    // just absorb it
                }
            }
        }

        log.debug(title + "end - - - - - - - - - - - - - - - -");
    }


    public void testMapping() throws Exception {

        String title = "MessageContextSaveATest:testMapping(): ";
        log.debug(title + "start - - - - - - - - - - - - - - - -");

        MessageContext restoredMC = null;

        //---------------------------------------------------------------------
        // make sure that the operation context messageContexts table 
        // has an entry for the message context that we working with
        //---------------------------------------------------------------------
        // look at the OperationContext messageContexts table
        HashMap mcMap1 = mc.getOperationContext().getMessageContexts();

        if ((mcMap1 == null) || (mcMap1.isEmpty())) {
            mc.getAxisOperation().addMessageContext(mc, mc.getOperationContext());
        }
        // update the table
        mcMap1 = mc.getOperationContext().getMessageContexts();

        log.debug(title + "- - - - - original message contexts table- - - - - - - - - - -");
        showMcMap(mcMap1);

        //---------------------------------------------------------------------
        // save and restore the message context
        //---------------------------------------------------------------------

        File theFile;
        String theFilename = null;

        boolean pause = false;
        boolean savedMessageContext = false;
        boolean restoredMessageContext = false;
        boolean comparesOk = false;

        try {
            theFile = File.createTempFile("McMappings", null);
            theFilename = theFile.getName();
            log.debug(title + "temp file = [" + theFilename + "]");
        }
        catch (Exception ex) {
            log.debug(title + "error creating temp file = [" + ex.getMessage() + "]");
            theFile = null;
        }

        if (theFile != null) {
            // ---------------------------------------------------------
            // save to the temporary file
            // ---------------------------------------------------------
            try {
                // setup an output stream to a physical file
                FileOutputStream outStream = new FileOutputStream(theFile);

                // attach a stream capable of writing objects to the 
                // stream connected to the file
                ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

                // try to save the message context
                log.debug(title + "saving message context.....");
                savedMessageContext = false;
                outObjStream.writeObject(mc);

                // close out the streams
                outObjStream.flush();
                outObjStream.close();
                outStream.flush();
                outStream.close();

                savedMessageContext = true;
                log.debug(title + "....saved message context.....");

                long filesize = theFile.length();
                log.debug(title + "file size after save [" + filesize +
                        "]   temp file = [" + theFilename + "]");

            }
            catch (Exception ex2) {
                log.debug(title + "error with saving message context = [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(savedMessageContext);

            // ---------------------------------------------------------
            // restore from the temporary file
            // ---------------------------------------------------------
            try {
                // setup an input stream to the file
                FileInputStream inStream = new FileInputStream(theFile);

                // attach a stream capable of reading objects from the 
                // stream connected to the file
                ObjectInputStream inObjStream = new ObjectInputStream(inStream);

                // try to restore the message context
                log.debug(title + "restoring a message context.....");
                restoredMessageContext = false;

                restoredMC = (MessageContext) inObjStream.readObject();
                inObjStream.close();
                inStream.close();

                restoredMC.activate(configurationContext);

                restoredMessageContext = true;
                log.debug(title + "....restored message context.....");

                // get the table after the restore
                HashMap mcMap2 = restoredMC.getOperationContext().getMessageContexts();

                log.debug(
                        "MessageContextSaveATest:testMapping(): - - - - - restored message contexts table- - - - - - - - - - -");
                showMcMap(mcMap2);

                boolean okMap = compareMCMaps(mcMap1, mcMap2);
                assertTrue(okMap);

            }
            catch (Exception ex2) {
                log.debug(title + "error with restoring message context = [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(restoredMessageContext);

            // if the save/restore of the message context succeeded,
            // then don't keep the temporary file around
            boolean removeTmpFile = savedMessageContext && restoredMessageContext && comparesOk;
            if (removeTmpFile) {
                try {
                    theFile.delete();
                }
                catch (Exception e) {
                    // just absorb it
                }
            }
        }

        log.debug(title + "end - - - - - - - - - - - - - - - -");


    }

    private boolean compareMCMaps(HashMap m1, HashMap m2) {
        String title = "MessageContextSaveATest:compareMCMaps(): ";

        if ((m1 != null) && (m2 != null)) {
            int size1 = m1.size();
            int size2 = m2.size();

            if (size1 != size2) {
                log.debug(title + "MISMATCH:  map1 size [" + size1 +
                        "]  !=   map2 size [" + size2 + "]");
                return false;
            }

            String id1 = null;
            String id2 = null;

            // check the keys, ordering is not important between the two maps
            Iterator it1 = m1.keySet().iterator();

            while (it1.hasNext()) {
                String key1 = (String) it1.next();
                MessageContext value1 = (MessageContext) m1.get(key1);

                if (value1 != null) {
                    id1 = value1.getMessageID();

                    MessageContext value2 = (MessageContext) m2.get(key1);

                    if (value2 != null) {
                        id2 = value2.getMessageID();
                    } else {
                        // mismatch
                        log.debug(title +
                                "MISMATCH:  no message context in one of the tables for key [" +
                                key1 + "]");
                        return false;
                    }

                    if ((id1 != null) && (id2 != null)) {
                        if (!id1.equals(id2)) {
                            // mismatch
                            log.debug(title + "MISMATCH:  messageID_1 [" + id1 +
                                    "]   !=    messageID_2 [" + id2 + "]");
                            return false;
                        }
                    } else {
                        // null values, can't tell
                        log.debug(title + "MISMATCH:  one or more null message IDs");
                        return false;
                    }
                }
            }
            return true;
        } else if ((m1 == null) && (m2 == null)) {
            return true;
        } else {
            // mismatch
            log.debug(title + "MISMATCH:  one of the tables is null");
            return false;
        }
    }


    public class TempHandler extends AbstractHandler {
        private Integer handlerID = null;

        private File theFile = null;
        private String theFilename = null;

        private boolean pause = false;
        private boolean savedMessageContext = false;
        private boolean restoredMessageContext = false;
        private boolean comparesOk = false;

        //-----------------------------------------------------------------
        // constructors
        //-----------------------------------------------------------------

        public TempHandler() {
            this.handlerID = new Integer(-5);
        }

        public TempHandler(int index, boolean pause) {
            this.handlerID = new Integer(index);
            this.pause = pause;
            init(new HandlerDescription("handler" + index));
        }

        public TempHandler(int index) {
            this.handlerID = new Integer(index);
            init(new HandlerDescription("handler" + index));
        }

        //-----------------------------------------------------------------
        // methods
        //-----------------------------------------------------------------

        public int getHandlerID() {
            if (handlerID != null) {
                return handlerID.intValue();
            }

            return -5;
        }


        public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
            String title = "TempHandler[" + getHandlerID() + "]:invoke(): ";
            log.debug(title + "pause = [" + pause + "]");
            savedMessageContext = false;
            restoredMessageContext = false;

            if (pause) {
                log.debug(title + "msgContext.pause()");
                msgContext.pause();
                pause = false;

                try {
                    theFile = File.createTempFile("mcSave", null);
                    theFilename = theFile.getName();
                    log.debug(title + "temp file = [" + theFilename + "]");
                }
                catch (Exception ex) {
                    log.debug(title + "error creating temp file = [" + ex.getMessage() + "]");
                    theFile = null;
                }

                if (theFile != null) {
                    // ---------------------------------------------------------
                    // save to the temporary file
                    // ---------------------------------------------------------
                    try {
                        // setup an output stream to a physical file
                        FileOutputStream outStream = new FileOutputStream(theFile);

                        // attach a stream capable of writing objects to the 
                        // stream connected to the file
                        ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

                        // try to save the message context
                        log.debug(title + "saving message context.....");
                        savedMessageContext = false;
                        outObjStream.writeObject(msgContext);

                        // close out the streams
                        outObjStream.flush();
                        outObjStream.close();
                        outStream.flush();
                        outStream.close();

                        savedMessageContext = true;
                        log.debug(title + "....saved message context.....");

                        long filesize = theFile.length();
                        log.debug(title + "file size after save [" + filesize +
                                "]   temp file = [" + theFilename + "]");

                    }
                    catch (Exception ex2) {
                        log.debug(title + "error with saving message context = [" +
                                ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                        ex2.printStackTrace();
                    }

                    assertTrue(savedMessageContext);

                    // ---------------------------------------------------------
                    // restore from the temporary file
                    // ---------------------------------------------------------
                    try {
                        // setup an input stream to the file
                        FileInputStream inStream = new FileInputStream(theFile);

                        // attach a stream capable of reading objects from the 
                        // stream connected to the file
                        ObjectInputStream inObjStream = new ObjectInputStream(inStream);

                        // try to restore the message context
                        log.debug(title + "restoring a message context.....");
                        restoredMessageContext = false;

                        MessageContext msgContext2 = (MessageContext) inObjStream.readObject();
                        inObjStream.close();
                        inStream.close();

                        msgContext2.activate(configurationContext);

                        restoredMessageContext = true;
                        log.debug(title + "....restored message context.....");

                        // compare to original execution chain
                        ArrayList restored_execChain = msgContext2.getExecutionChain();
                        ArrayList orig_execChain = msgContext.getExecutionChain();

                        comparesOk = ActivateUtils
                                .isEquivalent(restored_execChain, orig_execChain, false);
                        log.debug(title + "execution chain equivalency [" + comparesOk + "]");
                        assertTrue(comparesOk);

                        // check executed list
                        Iterator restored_executed_it = msgContext2.getExecutedPhases();
                        Iterator orig_executed_it = msgContext.getExecutedPhases();
                        if ((restored_executed_it != null) && (orig_executed_it != null)) {
                            while (restored_executed_it.hasNext() && orig_executed_it.hasNext()) {
                                Object p1 = restored_executed_it.next();
                                Object p2 = orig_executed_it.next();

                                comparesOk = comparePhases(p1, p2);
                                log.debug(title +
                                        "executed phase list:  compare phases [" + comparesOk +
                                        "]");
                                assertTrue(comparesOk);
                            }
                        } else {
                            // problem with the executed lists
                            assertTrue(false);
                        }

                        // now put the restored message context in the global
                        // variable for the test 
                        mc2 = msgContext2;
                    }
                    catch (Exception ex2) {
                        log.debug(title + "error with saving message context = [" +
                                ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                        ex2.printStackTrace();
                    }

                    assertTrue(restoredMessageContext);

                    // if the save/restore of the message context succeeded,
                    // then don't keep the temporary file around
                    boolean removeTmpFile =
                            savedMessageContext && restoredMessageContext && comparesOk;
                    if (removeTmpFile) {
                        try {
                            theFile.delete();
                        }
                        catch (Exception e) {
                            // just absorb it
                        }
                    }
                }

                return InvocationResponse.SUSPEND;

            } else {
                log.debug(title + "executedHandlers.add(" + handlerID + ")");
                executedHandlers.add(handlerID);
            }

            return InvocationResponse.CONTINUE;
        }

    }
}
