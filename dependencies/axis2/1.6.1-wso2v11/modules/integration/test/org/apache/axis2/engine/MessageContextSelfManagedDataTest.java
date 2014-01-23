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
import org.apache.axis2.context.SelfManagedDataManager;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MessageContextSelfManagedDataTest extends TestCase {
    protected static final Log log = LogFactory.getLog(MessageContextSelfManagedDataTest.class);
    
    private File theFile = null;
    boolean savedMessageContext = false;
    boolean restoredMessageContext = false;

    private String serviceGroupName = new String("NullServiceGroup");
    private QName serviceName = new QName("NullService");
    private QName operationName = new QName("DummyOp");

    private ConfigurationContext cfgContext = null;
    private ServiceGroupContext serviceGroupContext = null;
    private ServiceContext serviceContext = null;
    private OperationContext operationContext = null;

    private AxisConfiguration axisConfiguration = null;
    private AxisServiceGroup axisServiceGroup = null;
    private AxisService axisService = null;
    private AxisOperation axisOperation = null;

    private TransportOutDescription transportOut = null;
    private TransportOutDescription transportOut2 = null;
    private TransportOutDescription transportOut3 = null;
    private TransportInDescription transportIn = null;
    private TransportInDescription transportIn2 = null;
    private TransportInDescription transportIn3 = null;

    private MessageContext mc = null;

    private TempHandler01 handler01;
    private TempHandler02 handler02;
    private TempHandler02 subhandler;  // this handler is intended to be a few levels down in executionChain
    private TempHandler03 handler03;
    private TempHandler04 handler04;
    private Phase phase1;
    private Phase phase2;
    private Phase phase3;
    private Phase subPhase;

    // use this to count how many times methods get called for a particular test
    private int invokecallcount = 0;

    // key-value pairs to be used for self managed data

    private String key01 = "key01";
    private String testData01 = "TempHandler01_01";

    private String key02 = "key02";
    private String testData02 = "TempHandler01_02";

    private String key03 = "key03";
    private byte [] testData03 = {0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, 0x01,
            0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F};

    private String key04 = "key04";
    private long [] testData04 = {0L, 1L, -6601664200673063531L, -7753637088257391858L};

    private String key05 = "key05";
    private int testData05 = 123456;


    public MessageContextSelfManagedDataTest(String arg0) {
        super(arg0);
        initAll();
    }


    protected void initAll() {
        try {
            prepare();
        }
        catch (Exception e) {
            log.debug(
                    "MessageContextSelfManagedDataTest:initAll:  error in setting up object graph [" +
                            e.getClass().getName() + " : " + e.getMessage() + "]");
        }

        if (handler01 == null) {
            handler01 = new TempHandler01(101);
        }

        if (handler02 == null) {
            handler02 = new TempHandler02(102);
        }

        if (handler03 == null) {
            handler03 = new TempHandler03(103);
        }

        if (handler04 == null) {
            handler04 = new TempHandler04(104);
        }

        if (subhandler == null) {
            subhandler = new TempHandler02(1000);
        }

    }


    //
    // prepare the object hierarchy for testing
    //
    private void prepare() throws Exception {
        //-----------------------------------------------------------------

        axisConfiguration = new AxisConfiguration();

        cfgContext = new ConfigurationContext(axisConfiguration);

        cfgContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-only", new RawXMLINOnlyMessageReceiver());
        cfgContext.getAxisConfiguration().addMessageReceiver(
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

        cfgContext.getAxisConfiguration().getInFlowPhases().add(dispatchPhase);

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


        cfgContext.getAxisConfiguration().addService(axisService);

        //-----------------------------------------------------------------

        serviceGroupContext =
                cfgContext.createServiceGroupContext(axisService.getAxisServiceGroup());
        serviceGroupContext.setId("ServiceGroupContextTest");

        serviceContext = serviceGroupContext.getServiceContext(axisService);

        operationContext = serviceContext.createOperationContext(operationName);

        //-----------------------------------------------------------------

        transportOut = new TransportOutDescription("null");
        transportOut2 = new TransportOutDescription("happy");
        transportOut3 = new TransportOutDescription("golucky");
        transportOut.setSender(new CommonsHTTPTransportSender());
        transportOut2.setSender(new CommonsHTTPTransportSender());
        transportOut3.setSender(new CommonsHTTPTransportSender());
        axisConfiguration.addTransportOut(transportOut3);
        axisConfiguration.addTransportOut(transportOut2);
        axisConfiguration.addTransportOut(transportOut);

        transportIn = new TransportInDescription("null");
        transportIn2 = new TransportInDescription("always");
        transportIn3 = new TransportInDescription("thebest");
        transportIn.setReceiver(new SimpleHTTPServer());
        transportIn2.setReceiver(new SimpleHTTPServer());
        transportIn3.setReceiver(new SimpleHTTPServer());
        axisConfiguration.addTransportIn(transportIn2);
        axisConfiguration.addTransportIn(transportIn);
        axisConfiguration.addTransportIn(transportIn3);

    }


    /*
     * (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     * 
     * setUp gets called before each test* method.  In this class, a new
     * MessageContext object is created for each test* method.  The test*
     * methods are responsible for adding whichever handler they may need
     * in order to run the desired test.  See the handler class comments
     * for their respective functions.
     */
    protected void setUp() throws Exception {
        //org.apache.log4j.BasicConfigurator.configure();

        invokecallcount = 0;

        mc = cfgContext.createMessageContext();
        mc.setTransportIn(transportIn);
        mc.setTransportOut(transportOut);

        mc.setServerSide(true);
        mc.setProperty(MessageContext.TRANSPORT_OUT, System.out);

        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(omFac.getDefaultEnvelope());

        phase1 = new Phase("1");
        phase1.addHandler(new TempHandler02(0));
        phase1.addHandler(new TempHandler02(1));

        phase2 = new Phase("2");
        phase2.addHandler(new TempHandler02(2));
        phase2.addHandler(handler02);
        phase2.addHandler(new TempHandler02(3));

        phase3 = new Phase("3");
        phase3.addHandler(new TempHandler02(4));
        phase3.addHandler(subhandler);
        phase3.addHandler(handler02);  // same instance, second insertion
        phase3.addHandler(new TempHandler02(5));

        /*
         * TODO:  WARNING WARNING WARNING
         * Ideally inserting subPhase here would make the axis2 engine call
         * the invoke of nested subhandler.  It does not do this.  Please see the
         * warning at bottom of testPause06 method.
         */
        subPhase = new Phase("sub");
        subPhase.addHandler(subhandler);
        phase3.addHandler(subPhase);
        phase3.addHandler(new TempHandler02(6));
        phase3.addHandler(new TempHandler02(7));

        axisOperation.getRemainingPhasesInFlow().add(phase1);
        axisOperation.getRemainingPhasesInFlow().add(phase2);
        axisOperation.getRemainingPhasesInFlow().add(phase3);


        mc.setMessageID(UUIDGenerator.getUUID());

        //operationContext.addMessageContext(mc);  gets done via the register
        axisOperation.registerOperationContext(mc, operationContext);
        mc.setOperationContext(operationContext);
        mc.setServiceContext(serviceContext);

        mc.setTo(new EndpointReference("axis2/services/NullService"));

        mc.setWSAAction(operationName.getLocalPart());
        mc.setSoapAction(operationName.getLocalPart());

    }

    //-------------------------------------------------------------------------
    // test cases
    //-------------------------------------------------------------------------


    /**
     * Test case for setting and removing data from a message context
     */
    public void testSelfManagedData01() {
        log.debug(
                "MessageContextSelfManagedDataTest::testSelfManagedData01()=======================================");
        try {
            ArrayList handlers = new ArrayList();
            handlers.add(handler01);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(testData01, handler01.getTestData01FromMessageContext(mc));
        assertEquals(testData02, handler01.getTestData02FromMessageContext(mc));

        boolean isOk3 = isEquals(testData03, handler01.getTestData03FromMessageContext(mc));
        assertTrue(isOk3);

        boolean isOk4 = isEquals(testData04, handler01.getTestData04FromMessageContext(mc));
        assertTrue(isOk4);

        assertEquals(1, invokecallcount);
    }


    /**
     * Test for setting, saving, restoring self managed data with no exceptions
     */
    public void testPause01_noExceptions() {
        log.debug(
                "MessageContextSelfManagedDataTest::testPause01_noExceptions()=======================================");

        try {
            ArrayList handlers = new ArrayList();
            handlers.add(handler02);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // if we didn't get any exceptions during save/restore, these will be true
        assertTrue(savedMessageContext);
        assertTrue(restoredMessageContext);
        assertEquals(1, invokecallcount);
    }


    /**
     * Test for verifying the self managed data used during the save and restore
     */
    public void testPause02_saveRestoreSelfManagedData() {
        log.debug(
                "MessageContextSelfManagedDataTest::testPause02_saveRestoreSelfManagedData()=======================================");

        try {
            ArrayList handlers = new ArrayList();
            handlers.add(handler02);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // make sure the data in our handler got restored
        assertEquals(handler02.testData, handler02.getTestDataFromMessageContext(mc));
        assertEquals(1, invokecallcount);
    }

    /**
     * Test for save and restore of self managed data and the AxisOperation
     */
    public void testPause03_saveRestoreOperation() {
        log.debug(
                "MessageContextSelfManagedDataTest::testPause03_saveRestoreOperation()=======================================");

        try {
            ArrayList handlers = new ArrayList();
            handlers.add(handler02);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // make sure the operation got restored in the MessageContext object
        assertEquals(operationName.toString(), mc.getAxisOperation().getName().toString());
        assertEquals(1, invokecallcount);

    }


    /**
     * Test for save and restore of self managed data and the AxisService
     */
    public void testPause04_saveRestoreAxisService() {
        log.debug(
                "MessageContextSelfManagedDataTest::testPause04_saveRestoreAxisService()=======================================");

        try {
            ArrayList handlers = new ArrayList();
            handlers.add(handler02);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // make sure the service got restored in the MessageContext object
        assertEquals(serviceName.toString(), mc.getAxisService().getName().toString());
        assertEquals(1, invokecallcount);
    }


    /**
     * Test for save and restore of self managed data and the AxisServiceGroup
     */
    public void testPause05_saveRestoreAxisServiceGroup() {
        log.debug(
                "MessageContextSelfManagedDataTest::testPause05_saveRestoreAxisServiceGroup()=======================================");

        try {
            ArrayList handlers = new ArrayList();
            handlers.add(handler02);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // make sure the serviceGroup got restored in the MessageContext object
        //assertEquals(serviceGroupName.toString(), mc.getAxisServiceGroup().getServiceGroupName());
        assertEquals(1, invokecallcount);
    }


    /**
     * Test for phases
     */
    public void testPause06_saveRestorePhases() {
        log.debug(
                "MessageContextSelfManagedDataTest::testPause06_saveRestorePhases()=======================================");

        TempHandler02 handlerA = new TempHandler02(666);

        Phase phase601 = new Phase("01");
        phase601.addHandler(new TempHandler02(61)); // slot 1
        phase601.addHandler(new TempHandler03(62)); // slot 2

        Phase phase602 = new Phase("02");
        phase602.addHandler(new TempHandler02(63)); // slot 3
        phase602.addHandler(handlerA);              // slot 4
        phase602.addHandler(new TempHandler03(64)); // slot 5

        Phase phase603 = new Phase("03");
        phase603.addHandler(new TempHandler02(65)); // slot 6
        phase603.addHandler(subhandler);            // slot 7
        phase603.addHandler(handlerA);              // slot 8 - same instance, second insertion
        phase603.addHandler(new TempHandler03(66)); // slot 9

        /*
         * TODO:  WARNING WARNING WARNING
         * Ideally inserting subPhase here would make the axis2 engine call
         * the invoke of nested subhandler.  It does not do this.  
         * Please see the warning later in this method.
         */
        Phase subPhase601 = new Phase("sub6");
        subPhase601.addHandler(subhandler);
        phase603.addHandler(subPhase601);           // slot 10
        phase603.addHandler(new TempHandler02(67)); // slot 11
        phase603.addHandler(new TempHandler03(68)); // slot 12

        try {
            ArrayList phases = new ArrayList();
            phases.add(phase601);
            phases.add(phase602);
            phases.add(phase603);

            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(phases);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // get the phase lists and see if they match up
        ArrayList restoredPhases = mc.getExecutionChain();
        int it_count = 0;

        Iterator it = restoredPhases.iterator();
        while (it.hasNext()) {
            // we know everything at this level is a Phase.  
            // if you change it, you might get a ClassCastException 
            Phase restored_phase = (Phase) it.next();

            Phase original_phase = null;

            it_count++;

            if (it_count == 1) {
                original_phase = phase601;
            } else if (it_count == 2) {
                original_phase = phase602;
            } else if (it_count == 3) {
                original_phase = phase603;
            }

            //comparePhases(restored_phase.getHandlers().iterator(), original_phase.getHandlers());

            boolean isOk = comparePhases(restored_phase, original_phase);
            assertTrue(isOk);
        }

        // TODO WARNING WARNING WARNING
        // The axis2 engine is not calling the invoke on nested handlers!  
        // The way this testcase works is that the handler's invoke() method is 
        // what sets the data.  So, any handlers that are in subPhase will 
        // not get called, and thus will not set or restore any data.  
        // Notice we do currently have a subhandler in the subPhase object.
        // When axis2 decides to support nested handlers, the
        // first three assertEquals below will fail.
        log.debug(
                "MessageContextSelfManagedDataTest::testPause06_saveRestorePhases()::  invokecallcount [" +
                        invokecallcount + "]");
        assertEquals(11, invokecallcount);

        // even though there are two occurrances of the same instance of
        // handlerA in the executionChain, its serialize and deserialize 
        // should only be called once per unique instance in the list
        int count_s = handlerA.getSerializecallcount();
        log.debug(
                "MessageContextSelfManagedDataTest::testPause06_saveRestorePhases()::  handlerA serialize call count [" +
                        count_s + "]");
        assertEquals(11, count_s);

        // here comes some fun math...
        // Since a handler (TempHandler02) in this case 
        // doesn't add any data until its invoke method gets called, 
        // and the invoke is what causes the save/restore (and thus 
        // the serialize/deserialize) there is no data for handlerA 
        // to deserialize until the first occurance of handlerA 
        // invoke is called in the executionChain.  Observing our phases, 
        // we see it is in slot #3.  11 - 3 = 8
        int count_d = handlerA.getDeserializecallcount();
        log.debug(
                "MessageContextSelfManagedDataTest::testPause06_saveRestorePhases()::  handlerA deserialize call count [" +
                        count_d + "]");
        assertEquals(8, count_d);
        assertEquals(subhandler.testData, subhandler.getTestDataFromMessageContext(mc));

    }


    /**
     * Test for save and restore of binary self managed data
     */
    public void testSelfManagedData07() {
        log.debug(
                "MessageContextSelfManagedDataTest::testSelfManagedData07()=======================================");
        try {
            ArrayList handlers = new ArrayList();
            handlers.add(handler03);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        boolean isOk3 = isEquals(testData03, (byte []) handler03.getTestDataFromMessageContext(mc));
        assertTrue(isOk3);

        assertEquals(1, invokecallcount);
    }


    /**
     * Test for handler04
     */
    public void testSelfManagedData08() {
        log.debug(
                "MessageContextSelfManagedDataTest::testSelfManagedData08()=======================================");
        try {

            ArrayList handlers = new ArrayList();
            handlers.add(handler04);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        boolean isOk4 = isEquals(testData04, (long []) handler04.getTestDataFromMessageContext(mc));
        assertTrue(isOk4);

        assertEquals(1, invokecallcount);
    }


    /**
     * Test for handler03 and handler04
     */
    public void testSelfManagedData09() {
        log.debug(
                "MessageContextSelfManagedDataTest::testSelfManagedData09()=======================================");
        try {

            ArrayList handlers = new ArrayList();
            handlers.add(handler03);
            handlers.add(handler04);
            cfgContext.getAxisConfiguration().setInPhasesUptoAndIncludingPostDispatch(handlers);

            mc.setTo(new EndpointReference("axis2/services/NullService"));
            mc.setWSAAction("DummyOp");

            AxisEngine.receive(mc);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        boolean isOk3 = isEquals(testData03, (byte []) handler03.getTestDataFromMessageContext(mc));
        assertTrue(isOk3);

        boolean isOk4 = isEquals(testData04, (long []) handler04.getTestDataFromMessageContext(mc));
        assertTrue(isOk4);

        assertEquals(2, invokecallcount);
    }

    //-------------------------------------------------------------------------
    // internal helper methods
    //-------------------------------------------------------------------------

    /**
     * Saves the specified message context to a temporary
     * file, then restores it.
     *
     * @param mc1      The message context object to save
     * @param fnprefix A prefix for the filename of the temporary file
     * @param desc     Text that describes the caller's situation
     * @return The restored message context object or NULL
     */
    private MessageContext saveAndRestore(MessageContext mc1, String fnprefix, String desc) {
        MessageContext msgContext2 = null;
        String title = "MessageContextSelfManagedDataTest::saveAndRestore::[" + desc + "]   ";
        log.debug(title);

        try {
            theFile = File.createTempFile(fnprefix, null);
            log.debug(title + "temp file = [" + theFile.getName() + "]");
        }
        catch (Exception ex) {
            log.debug(title + "error creating temp file = [" + ex.getMessage() + "]");
            theFile = null;
        }

        if (theFile != null) {
            try {
                // setup an output stream to a physical file
                FileOutputStream outStream = new FileOutputStream(theFile);

                // attach a stream capable of writing objects to the 
                // stream connected to the file
                ObjectOutputStream outObjStream = new ObjectOutputStream(outStream);

                // try to save the message context
                log.debug(title + "saving message context ....");
                savedMessageContext = false;

                outObjStream.writeObject(mc1);
                outObjStream.close();
                outStream.close();

                // no exceptions, set savedMessageContext to true
                savedMessageContext = true;

                log.debug(title + "....saved message context .....");

                // setup an input stream to the file
                FileInputStream inStream = new FileInputStream(theFile);

                // attach a stream capable of reading objects from the 
                // stream connected to the file
                ObjectInputStream inObjStream = new ObjectInputStream(inStream);

                // try to restore the message context
                log.debug(title + "restoring message context .....");
                restoredMessageContext = false;
                msgContext2 = (MessageContext) inObjStream.readObject();
                inObjStream.close();
                inStream.close();
                msgContext2.activate(mc1.getConfigurationContext());

                // no exceptions, set restoredMessageContext to true
                restoredMessageContext = true;

                // VERY IMPORTANT: replace testcase's messagecontext object with the new restored one
                mc = msgContext2;

                log.debug(title + "....restored message context .....");

            }
            catch (Exception ex2) {
                if (savedMessageContext != true) {
                    log.debug(title + "Error with saving message context = [" +
                            ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                    ex2.printStackTrace();
                } else {
                    log.debug(title + "Error with restoring message context = [" +
                            ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                    ex2.printStackTrace();
                }
            }

            // if the save/restore of the message context succeeded,
            // then don't keep the temporary file around
            boolean removeTmpFile = savedMessageContext && restoredMessageContext;
            if (removeTmpFile) {
                try {
                    theFile.delete();
                }
                catch (Exception e) {
                    // just absorb it
                }
            }
        }

        return msgContext2;
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

        if (o instanceof TempHandler01) {
            id = ((TempHandler01) o).getID();
        } else if (o instanceof TempHandler02) {
            id = ((TempHandler02) o).getID();
        } else if (o instanceof TempHandler03) {
            id = ((TempHandler03) o).getID();
        } else if (o instanceof TempHandler04) {
            id = ((TempHandler04) o).getID();
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
                log.debug("MessagecontextSelfManagedDataTest::compareHandlers:  class [" +
                        c1 + "] match ");

                int id1 = getHandlerID(o1);
                int id2 = getHandlerID(o2);

                if (id1 == id2) {
                    log.debug("MessagecontextSelfManagedDataTest::compareHandlers:  id [" +
                            id1 + "] match");
                    return true;
                } else {
                    log.debug("MessagecontextSelfManagedDataTest::compareHandlers:  id1 [" + id1 +
                            "] != id2 [" + id2 + "] ");
                    return false;
                }
            } else {
                log.debug("MessagecontextSelfManagedDataTest::compareHandlers:  class1 [" +
                        c1 + "] != class2 [" + c2 + "]   ");
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
            return true;
        }

        try {
            if (((o1 != null) && (o2 != null))
                    && ((o1 instanceof Phase) && (o2 instanceof Phase))
                    ) {
                Phase p1 = (Phase) o1;
                Phase p2 = (Phase) o2;

                List list1 = p1.getHandlers();
                List list2 = p2.getHandlers();

                if ((list1 == null) && (list2 == null)) {
                    return true;
                }

                if ((list1 != null) && (list2 != null)) {
                    int size1 = list1.size();
                    int size2 = list2.size();

                    if (size1 != size2) {
                        return false;
                    }

                    for (int j = 0; j < size1; j++) {
                        Object obj1 = list1.get(j);
                        Object obj2 = list2.get(j);

                        if ((obj1 == null) && (obj2 == null)) {
                            // ok
                        } else if ((obj1 != null) && (obj2 != null)) {
                            boolean check = false;

                            if (obj1 instanceof Phase) {
                                check = comparePhases(obj1, obj2);
                            } else {
                                // must be a handler
                                check = compareHandlers(obj1, obj2);
                            }

                            if (check == false) {
                                return false;
                            }
                        } else {
                            // mismatch
                            return false;
                        }
                    }

                    // if we got here, the comparison completed ok
                    // with a match

                    return true;
                }

            }
        }
        catch (Exception e) {
            // some error
            e.printStackTrace();
        }

        return false;
    }

/*
    private void comparePhases(Iterator it, ArrayList al)
    {
        int it_count = -1;

        while (it.hasNext())
        {
            it_count++;
            Handler handler = (Handler)it.next();
            if (handler instanceof Phase)
            {
                comparePhases(((Phase)handler).getHandlers().iterator(), ((Phase)al.get(it_count)).getHandlers());
            }
            else
            {
                assertEquals(handler, (TempHandler02)al.get(it_count));
            }
        }
    }

*/

    //-------------------------------------------------------------------------
    // internal handlers
    //-------------------------------------------------------------------------


    /*
     * TempHandler01 simply sets and/or removes data from the MessageContext
     * object.
     */
    public class TempHandler01 extends AbstractHandler {
        private int id = -2;

        public TempHandler01() {
            id = -1;
        }

        public TempHandler01(int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public String getName() {
            return new String(Integer.toString(id));
        }

        public String getTestData01FromMessageContext(MessageContext _mc) {
            return (String) _mc.getSelfManagedData(this.getClass(), key01);
        }

        public String getTestData02FromMessageContext(MessageContext _mc) {
            return (String) _mc.getSelfManagedData(this.getClass(), key02);
        }

        public byte [] getTestData03FromMessageContext(MessageContext _mc) {
            return (byte []) _mc.getSelfManagedData(this.getClass(), key03);
        }

        public long [] getTestData04FromMessageContext(MessageContext _mc) {
            return (long []) _mc.getSelfManagedData(this.getClass(), key04);
        }

        /* we're just using the invoke to set/change/remove data
         * Tests:
         * for key01:  set, remove, set, get
         * for key02:  set, get, set
         * for key03:  set, remove, set, get
         * for key04:  set, get, set, get
         */
        public InvocationResponse invoke(MessageContext _mc) throws AxisFault {

            String desc = "TempHandler01[id=" + id + "].invoke()";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            String tmp = "whaaa?";
            _mc.setSelfManagedData(this.getClass(), key02, tmp);
            String data2 = (String) _mc.getSelfManagedData(this.getClass(), key02);
            assertEquals(tmp, data2);

            _mc.setSelfManagedData(this.getClass(), key01, testData01);
            _mc.removeSelfManagedData(this.getClass(), key01);
            _mc.setSelfManagedData(this.getClass(), key01, testData01);
            String data1 = (String) _mc.getSelfManagedData(this.getClass(), key01);
            assertEquals(data1, testData01);

            _mc.setSelfManagedData(this.getClass(), key02, testData02);

            _mc.setSelfManagedData(this.getClass(), key03, testData03);
            byte [] data3 = (byte []) _mc.getSelfManagedData(this.getClass(), key03);
            boolean isOk3 = isEquals(data3, testData03);
            assertTrue(isOk3);

            _mc.removeSelfManagedData(this.getClass(), key03);
            _mc.setSelfManagedData(this.getClass(), key03, testData03);

            _mc.setSelfManagedData(this.getClass(), key04, testData04);
            long [] data4 = (long []) _mc.getSelfManagedData(this.getClass(), key04);
            boolean isOk4 = isEquals(data4, testData04);
            assertTrue(isOk4);

            invokecallcount++;

            log.debug(title + ": Completed");
            return InvocationResponse.CONTINUE;
        }

    }


    /*
     * TempHandler02 sets data, saves it (via the serializeSelfManagedData method
     * called by the MessageContext object when saved in this handler's invoke()),
     * 
     */
    public class TempHandler02 extends AbstractHandler implements SelfManagedDataManager {
        private int id = -2;
        private int serializecallcount = 0;
        private int deserializecallcount = 0;
        public String testData = new String("this is some test data");

        /*
         * constructor
         */
        public TempHandler02() {
            this.id = -1;
            this.serializecallcount = 0;
            this.deserializecallcount = 0;
        }

        /*
         * constructor
         */
        public TempHandler02(int id) {
            this.id = id;
            this.serializecallcount = 0;
            this.deserializecallcount = 0;
        }

        public int getID() {
            return this.id;
        }

        public String getName() {
            return new String(Integer.toString(id));
        }

        public String getTestDataFromMessageContext(MessageContext _mc) {
            return (String) _mc.getSelfManagedData(this.getClass(), Integer.toString(id));
        }

        /*
         *  (non-Javadoc)
         * @see org.apache.axis2.engine.Handler#invoke(org.apache.axis2.context.MessageContext)
         * 
         * This invoke() method will test several things:
         * 1.  verify that serializeSelfManagedData is actually called when saving the MessageContext object
         * 2.  verify that deserializeSelfManagedData is actually called when restoring the MessageContext object
         * 3.  verify that any SelfManagedData stored in the MessageContext is properly saved/restored (via serializeSelfManagedData/deserializeSelfManagedData)
         */
        public InvocationResponse invoke(MessageContext _mc) throws AxisFault {
            String desc = "TempHandler02[id=" + id + "].invoke()";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            MessageContext msgContext2 = null;  // this will be the restored one

            _mc.setSelfManagedData(this.getClass(), Integer.toString(id),
                                   testData + Integer.toString(id));

            log.debug(title + ":   Setting self managed data using key [" +
                    Integer.toString(id) + "]");

            msgContext2 = saveAndRestore(_mc, "TempHandler02_", desc);

            boolean result = (msgContext2 != null);
            assertTrue(result);

            invokecallcount++;

            log.debug(title + ": Completed");
            return InvocationResponse.CONTINUE;
        }


        public void deserializeSelfManagedData(ByteArrayInputStream data, MessageContext _mc)
                throws IOException {
            deserializecallcount++;

            String desc = "TempHandler02[id=" + id + "].deserializeSelfManagedData()   count [" +
                    deserializecallcount + "]";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            ObjectInputStream ois = new ObjectInputStream(data);
            testData = ois.readUTF().concat(" with extra text " + Integer.toString(id));
            _mc.setSelfManagedData(this.getClass(), Integer.toString(id), testData);
        }


        public void restoreTransientData(MessageContext _mc) {
            // not necessary to test
        }


        public ByteArrayOutputStream serializeSelfManagedData(MessageContext _mc)
                throws IOException {
            serializecallcount++;

            String desc = "TempHandler02[id=" + id + "].serializeSelfManagedData()   count [" +
                    serializecallcount + "]";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            String storedTestData =
                    (String) _mc.getSelfManagedData(this.getClass(), Integer.toString(id));
            if (storedTestData == null) {
                log.debug(title + ":  No self managed data to serialize");
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeUTF(storedTestData);
            oos.close();
            int baos_size = baos.size();
            baos.close();

            log.debug(title + ": saved self managed data string length [" +
                    storedTestData.length() + "]");
            log.debug(title +
                    ": saved self managed data byte array output stream (UTF) length [" +
                    baos_size + "]");
            return baos;
        }

        public int getDeserializecallcount() {
            return deserializecallcount;
        }

        public int getSerializecallcount() {
            return serializecallcount;
        }
    }


    /*
     * TempHandler03 sets binary data, saves it (via the serializeSelfManagedData method
     * called by the MessageContext object when saved in this handler's invoke()),
     * 
     */
    public class TempHandler03 extends AbstractHandler implements SelfManagedDataManager {
        private int id = -2;
        private int serializecallcount = 0;
        private int deserializecallcount = 0;

        public TempHandler03() {
            id = -1;
        }

        public TempHandler03(int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public String getName() {
            return new String(Integer.toString(id));
        }

        public Object getTestDataFromMessageContext(MessageContext _mc) {
            return _mc.getSelfManagedData(this.getClass(), Integer.toString(id));
        }

        /*
         *  (non-Javadoc)
         * @see org.apache.axis2.engine.Handler#invoke(org.apache.axis2.context.MessageContext)
         * 
         * This invoke() method will test several things:
         * 1.  verify that serializeSelfManagedData is actually called when saving the MessageContext object
         * 2.  verify that deserializeSelfManagedData is actually called when restoring the MessageContext object
         * 3.  verify that any SelfManagedData stored in the MessageContext is properly saved/restored (via serializeSelfManagedData/deserializeSelfManagedData)
         */
        public InvocationResponse invoke(MessageContext _mc) throws AxisFault {

            String desc = "TempHandler03[id=" + id + "].invoke()";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            MessageContext msgContext2 = null;  // this will be the restored one

            // add the byte [] of data
            _mc.setSelfManagedData(this.getClass(), Integer.toString(id), testData03);

            log.debug(title + ":   Setting self managed data using key [" +
                    Integer.toString(id) + "]");

            msgContext2 = saveAndRestore(_mc, "TempHandler03_", desc);

            boolean result = (msgContext2 != null);
            assertTrue(result);

            invokecallcount++;

            log.debug(title + ": Completed");
            return InvocationResponse.CONTINUE;
        }

        public void deserializeSelfManagedData(ByteArrayInputStream data, MessageContext _mc)
                throws IOException {
            boolean isOK = true;

            deserializecallcount++;

            String desc = "TempHandler03[id=" + id + "].deserializeSelfManagedData()   count [" +
                    deserializecallcount + "]";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            int expected = testData03.length;

            byte [] tmp = new byte [expected];

            boolean keepGoing = true;
            int index = 0;

            while (keepGoing) {
                int tmpdata = data.read();

                if (tmpdata != -1) {
                    Integer value = new Integer(tmpdata);
                    tmp[index] = value.byteValue();
                    index++;

                    if (index > expected) {
                        isOK = false;
                        assertTrue(isOK);
                    }
                } else {
                    keepGoing = false;
                }
            }

            isOK = isEquals(tmp, testData03);
            assertTrue(isOK);

            _mc.setSelfManagedData(this.getClass(), Integer.toString(id), tmp);
        }

        public void restoreTransientData(MessageContext _mc) {
            // not necessary to test
        }

        public ByteArrayOutputStream serializeSelfManagedData(MessageContext _mc)
                throws IOException {

            serializecallcount++;

            String desc = "TempHandler03[id=" + id + "].serializeSelfManagedData()   count [" +
                    serializecallcount + "]";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            byte [] tmp = (byte []) _mc.getSelfManagedData(this.getClass(), Integer.toString(id));

            if (tmp == null) {
                log.debug(title + ":  No self managed data to serialize");
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(tmp, 0, tmp.length);
            int baos_size = baos.size();
            baos.close();

            log.debug(title + ": saved self managed data byte [] length [" + tmp.length + "]");
            log.debug(title +
                    ": saved self managed data byte array output stream length [" + baos_size +
                    "]");
            return baos;
        }


        public int getDeserializecallcout() {
            return deserializecallcount;
        }

        public int getSerializecallcount() {
            return serializecallcount;
        }
    }


    /*
     * TempHandler04 sets binary data, saves it (via the serializeSelfManagedData method
     * called by the MessageContext object when saved in this handler's invoke()),
     * 
     */
    public class TempHandler04 extends AbstractHandler implements SelfManagedDataManager {
        private int id = -2;
        private int serializecallcount = 0;
        private int deserializecallcount = 0;


        public TempHandler04() {
            id = -1;
        }

        public TempHandler04(int id) {
            this.id = id;
        }

        public int getID() {
            return this.id;
        }

        public String getName() {
            return new String(Integer.toString(id));
        }

        public Object getTestDataFromMessageContext(MessageContext _mc) {
            return _mc.getSelfManagedData(this.getClass(), Integer.toString(id));
        }

        /*
         *  (non-Javadoc)
         * @see org.apache.axis2.engine.Handler#invoke(org.apache.axis2.context.MessageContext)
         * 
         * This invoke() method will test several things:
         * 1.  verify that serializeSelfManagedData is actually called when saving the MessageContext object
         * 2.  verify that deserializeSelfManagedData is actually called when restoring the MessageContext object
         * 3.  verify that any SelfManagedData stored in the MessageContext is properly saved/restored (via serializeSelfManagedData/deserializeSelfManagedData)
         */
        public InvocationResponse invoke(MessageContext _mc) throws AxisFault {

            String desc = "TempHandler04[id=" + id + "].invoke()";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            MessageContext msgContext2 = null;  // this will be the restored one

            // add the byte [] of data
            _mc.setSelfManagedData(this.getClass(), Integer.toString(id), testData04);

            log.debug(title + ":   Setting self managed data using key [" +
                    Integer.toString(id) + "]");

            msgContext2 = saveAndRestore(_mc, "TempHandler04_", desc);

            boolean result = (msgContext2 != null);
            assertTrue(result);

            invokecallcount++;

            log.debug(title + ": Completed");
            return InvocationResponse.CONTINUE;
        }

        public void deserializeSelfManagedData(ByteArrayInputStream data, MessageContext _mc)
                throws IOException {
            boolean isOK = true;

            deserializecallcount++;

            String desc = "TempHandler04[id=" + id + "].deserializeSelfManagedData()   count [" +
                    deserializecallcount + "]";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            int expected = testData04.length;

            long [] tmp = new long [expected];

            ObjectInputStream ois = new ObjectInputStream(data);

            boolean keepGoing = true;
            int index = 0;

            int sizeWritten = ois.readInt();

            if (sizeWritten > 0) {
                while (keepGoing) {
                    try {
                        long tmpdata = ois.readLong();

                        tmp[index] = tmpdata;
                        index++;

                        if (index > expected) {
                            isOK = false;
                            assertTrue(isOK);
                        }
                    }
                    catch (Exception exc) {
                        keepGoing = false;
                    }
                }

                isOK = isEquals(tmp, testData04);
                assertTrue(isOK);

                _mc.setSelfManagedData(this.getClass(), Integer.toString(id), tmp);
            }

        }

        public void restoreTransientData(MessageContext _mc) {
            // not necessary to test
        }

        public ByteArrayOutputStream serializeSelfManagedData(MessageContext _mc)
                throws IOException {

            serializecallcount++;

            String desc = "TempHandler04[id=" + id + "].serializeSelfManagedData()   count [" +
                    serializecallcount + "]";
            String title = "MessageContextSelfManagedDataTest::" + desc;
            log.debug(title);

            long [] tmp = (long []) _mc.getSelfManagedData(this.getClass(), Integer.toString(id));

            if (tmp == null) {
                log.debug(title + ":  No self managed data to serialize");
                return null;
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            int size = tmp.length;

            oos.writeInt(size);

            for (int i = 0; i < size; i++) {
                oos.writeLong(tmp[i]);
            }

            oos.close();
            int baos_size = baos.size();
            baos.close();

            log.debug(title + ": saved self managed data byte [] length [" + tmp.length + "]");
            log.debug(title +
                    ": saved self managed data byte array output stream length [" + baos_size +
                    "]");
            return baos;
        }


        public int getDeserializecallcount() {
            return deserializecallcount;
        }

        public int getSerializecallcount() {
            return serializecallcount;
        }
    }


    /*
     * compares the two byte arrays to see if they are equal
     */
    private boolean isEquals(byte [] b1, byte [] b2) {
        int size1 = b1.length;
        int size2 = b2.length;

        if (size1 != size2) {
            return false;
        }

        for (int i = 0; i < size1; i++) {
            if (b1[i] != b2[i]) {
                return false;
            }
        }

        return true;
    }

    /*
     * compares the two long arrays to see if they are equal
     */
    private boolean isEquals(long [] L1, long [] L2) {
        int size1 = L1.length;
        int size2 = L2.length;

        if (size1 != size2) {
            return false;
        }

        for (int i = 0; i < size1; i++) {
            if (L1[i] != L2[i]) {
                return false;
            }
        }

        return true;
    }


}
