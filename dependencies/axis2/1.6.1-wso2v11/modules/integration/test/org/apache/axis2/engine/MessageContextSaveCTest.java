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
import org.apache.axis2.util.MetaDataEntry;
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
import java.util.LinkedHashMap;

/**
 * Provides tests that focus on the message context object graph
 */
public class MessageContextSaveCTest extends TestCase {
    protected static final Log log = LogFactory.getLog(MessageContextSaveCTest.class);

    private File persistentStore = null;

    //-------------------------------------------------------------------------
    // variables for the object graph 
    //-------------------------------------------------------------------------
    // used on a save of a message context, uses the same setup 
    // as the regular top-level objects. the difference is that
    // the phase contains handlers that split the action across
    // two handlers
    private ConfigurationContext saveConfigurationContext = null;
    private AxisConfiguration saveAxisConfiguration = null;

    // used on a restore of a message context, uses the same setup 
    // as the regular top-level objects with the exception that the
    // context objects don't exist. also, the phase contains handlers
    // that split the action across two handlers
    private ConfigurationContext restoreConfigurationContext = null;
    private AxisConfiguration restoreAxisConfiguration = null;

    // used on a restore of a message context, uses the same setup 
    // as the regular top-level objects with service-level context objects
    // with the name service name. also, the phase contains handlers
    // that split the action across two handlers
    private ConfigurationContext equivConfigurationContext = null;
    private AxisConfiguration equivAxisConfiguration = null;


    //------------------------------
    // service group ABC
    //------------------------------
    private String serviceGroupName_ABC = "ABCServiceGroup";

    private String serviceName_A = "ServiceA";
    private String serviceName_B = "ServiceB";
    private String serviceName_C = "ServiceC";
    private QName service_QName_A = new QName(serviceName_A);
    private QName service_QName_B = new QName(serviceName_B);
    private QName service_QName_C = new QName(serviceName_C);

    private String operationName_A1 = "TestOperationA1";
    private String operationName_A2 = "TestOperationA2";
    private QName operation_QName_A1 = new QName(operationName_A1);
    private QName operation_QName_A2 = new QName(operationName_A2);

    private ServiceGroupContext srvGrpCtx_ABC_save = null;
    private AxisServiceGroup axisSrvGrp_ABC_save = null;

    private AxisServiceGroup axisSrvGrp_ABC_restore = null;

    private ServiceGroupContext srvGrpCtx_ABC_equiv = null;
    private AxisServiceGroup axisSrvGrp_ABC_equiv = null;

    private ServiceContext srvCtx_A_save = null;
    private ServiceContext srvCtx_B_save = null;
    private ServiceContext srvCtx_C_save = null;
    private AxisService axisSrv_A_save = null;
    private AxisService axisSrv_B_save = null;
    private AxisService axisSrv_C_save = null;

    private ServiceContext srvCtx_A_restore = null;
    private ServiceContext srvCtx_B_restore = null;
    private ServiceContext srvCtx_C_restore = null;
    private AxisService axisSrv_A_restore = null;
    private AxisService axisSrv_B_restore = null;
    private AxisService axisSrv_C_restore = null;

    private ServiceContext srvCtx_A_equiv = null;
    private ServiceContext srvCtx_B_equiv = null;
    private ServiceContext srvCtx_C_equiv = null;
    private AxisService axisSrv_A_equiv = null;
    private AxisService axisSrv_B_equiv = null;
    private AxisService axisSrv_C_equiv = null;

    private OperationContext opCtx_A1_save = null;
    private OperationContext opCtx_A2_save = null;
    private AxisOperation axisOp_A1_save = null;
    private AxisOperation axisOp_A2_save = null;

    private OperationContext opCtx_A1_restore = null;
    private OperationContext opCtx_A2_restore = null;
    private AxisOperation axisOp_A1_restore = null;
    private AxisOperation axisOp_A2_restore = null;

    private OperationContext opCtx_A1_equiv = null;
    private OperationContext opCtx_A2_equiv = null;
    private AxisOperation axisOp_A1_equiv = null;
    private AxisOperation axisOp_A2_equiv = null;

    private MessageContext msgCtx_A1_1_save = null;
    private MessageContext msgCtx_A1_2_save = null;
    private MessageContext msgCtx_A2_save = null;

    private MessageContext msgCtx_A1_1_equiv = null;
    private MessageContext msgCtx_A1_2_equiv = null;
    private MessageContext msgCtx_A2_equiv = null;

    //-------------------------------------
    //  objects needed for message context
    //------------------------------------

    private TransportOutDescription transportOut = null;
    private TransportOutDescription transportOut2 = null;
    private TransportOutDescription transportOut3 = null;
    private TransportInDescription transportIn = null;
    private TransportInDescription transportIn2 = null;
    private TransportInDescription transportIn3 = null;

    private Phase phaseSave = null;
    private Phase phaseRestore = null;
    private Phase phaseEquiv = null;

    private ArrayList executedHandlers = null;

    private MessageContext restoredMessageContext = null;

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------


    public MessageContextSaveCTest(String arg0) {
        super(arg0);

        try {
            prepare();
        }
        catch (Exception e) {
            log.debug("MessageContextSaveCTest:constructor:  error in setting up object graph [" +
                    e.getClass().getName() + " : " + e.getMessage() + "]");
        }
    }


    //
    // prepare the object hierarchy for testing
    //
    private void prepare() throws Exception {
        //-----------------------------------------------------------------
        // setup the top-level objects
        //-----------------------------------------------------------------

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

        saveAxisConfiguration = new AxisConfiguration();
        saveConfigurationContext = new ConfigurationContext(saveAxisConfiguration);
        saveConfigurationContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-only", new RawXMLINOnlyMessageReceiver());
        saveConfigurationContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-out", new RawXMLINOutMessageReceiver());
        saveConfigurationContext.getAxisConfiguration().getInFlowPhases().add(dispatchPhase);

        restoreAxisConfiguration = new AxisConfiguration();
        restoreConfigurationContext = new ConfigurationContext(restoreAxisConfiguration);
        restoreConfigurationContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-only", new RawXMLINOnlyMessageReceiver());
        restoreConfigurationContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-out", new RawXMLINOutMessageReceiver());
        restoreConfigurationContext.getAxisConfiguration().getInFlowPhases().add(dispatchPhase);

        equivAxisConfiguration = new AxisConfiguration();
        equivConfigurationContext = new ConfigurationContext(equivAxisConfiguration);
        equivConfigurationContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-only", new RawXMLINOnlyMessageReceiver());
        equivConfigurationContext.getAxisConfiguration().addMessageReceiver(
                "http://www.w3.org/2004/08/wsdl/in-out", new RawXMLINOutMessageReceiver());
        equivConfigurationContext.getAxisConfiguration().getInFlowPhases().add(dispatchPhase);

        //----------------------------
        // transport-related objects
        //----------------------------
        transportOut = new TransportOutDescription("null");
        transportOut2 = new TransportOutDescription("happy");
        transportOut3 = new TransportOutDescription("golucky");
        transportOut.setSender(new CommonsHTTPTransportSender());
        transportOut2.setSender(new CommonsHTTPTransportSender());
        transportOut3.setSender(new CommonsHTTPTransportSender());

        saveAxisConfiguration.addTransportOut(transportOut3);
        saveAxisConfiguration.addTransportOut(transportOut2);
        saveAxisConfiguration.addTransportOut(transportOut);

        restoreAxisConfiguration.addTransportOut(transportOut3);
        restoreAxisConfiguration.addTransportOut(transportOut2);
        restoreAxisConfiguration.addTransportOut(transportOut);

        equivAxisConfiguration.addTransportOut(transportOut3);
        equivAxisConfiguration.addTransportOut(transportOut2);
        equivAxisConfiguration.addTransportOut(transportOut);


        transportIn = new TransportInDescription("null");
        transportIn2 = new TransportInDescription("always");
        transportIn3 = new TransportInDescription("thebest");
        transportIn.setReceiver(new SimpleHTTPServer());
        transportIn2.setReceiver(new SimpleHTTPServer());
        transportIn3.setReceiver(new SimpleHTTPServer());

        saveAxisConfiguration.addTransportIn(transportIn2);
        saveAxisConfiguration.addTransportIn(transportIn);
        saveAxisConfiguration.addTransportIn(transportIn3);

        restoreAxisConfiguration.addTransportIn(transportIn2);
        restoreAxisConfiguration.addTransportIn(transportIn);
        restoreAxisConfiguration.addTransportIn(transportIn3);

        equivAxisConfiguration.addTransportIn(transportIn2);
        equivAxisConfiguration.addTransportIn(transportIn);
        equivAxisConfiguration.addTransportIn(transportIn3);

        //----------------------------
        // phase-related objects
        //----------------------------
        persistentStore = File.createTempFile("mcObjTest", null);

        phaseSave = new Phase("PhaseTest");
        phaseSave.addHandler(new TempHandler(1, 1));
        phaseSave.addHandler(new SaveHandler(2, persistentStore, true));
        phaseSave.addHandler(new TempHandler(3, 1));

        ArrayList phases_Save = new ArrayList();
        phases_Save.add(phaseSave);

        saveAxisConfiguration.setInPhasesUptoAndIncludingPostDispatch(phases_Save);


        phaseRestore = new Phase("PhaseTest");
        phaseRestore.addHandler(new TempHandler(1, 1));
        phaseRestore.addHandler(new SaveHandler(2, persistentStore, false));
        phaseRestore.addHandler(new TempHandler(3, 1));

        ArrayList phases_Restore = new ArrayList();
        phases_Restore.add(phaseRestore);

        restoreAxisConfiguration.setInPhasesUptoAndIncludingPostDispatch(phases_Restore);

        phaseEquiv = new Phase("PhaseTest");
        phaseEquiv.addHandler(new TempHandler(1, 1));
        phaseEquiv.addHandler(new SaveHandler(2, persistentStore, true));
        phaseEquiv.addHandler(new TempHandler(3, 1));

        ArrayList phases_Equiv = new ArrayList();
        phases_Equiv.add(phaseEquiv);

        equivAxisConfiguration.setInPhasesUptoAndIncludingPostDispatch(phases_Equiv);

        //-----------------------------------------------------------------
        // setup the axis side of the hierachy
        //-----------------------------------------------------------------
        // ABC group
        //----------------------------

        axisSrvGrp_ABC_save = new AxisServiceGroup(saveAxisConfiguration);
        axisSrvGrp_ABC_save.setServiceGroupName(serviceGroupName_ABC);

        axisSrv_A_save = new AxisService(service_QName_A.getLocalPart());
        axisSrv_B_save = new AxisService(service_QName_B.getLocalPart());
        axisSrv_C_save = new AxisService(service_QName_C.getLocalPart());

        axisSrvGrp_ABC_save.addService(axisSrv_A_save);
        axisSrvGrp_ABC_save.addService(axisSrv_B_save);
        axisSrvGrp_ABC_save.addService(axisSrv_C_save);

        axisOp_A1_save = new InOutAxisOperation(operation_QName_A1);
        axisOp_A2_save = new InOutAxisOperation(operation_QName_A2);

        axisOp_A1_save.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisOp_A2_save.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisSrv_A_save.addOperation(axisOp_A1_save);
        axisSrv_A_save.mapActionToOperation(operation_QName_A1.getLocalPart(), axisOp_A1_save);

        axisSrv_A_save.addOperation(axisOp_A2_save);
        axisSrv_A_save.mapActionToOperation(operation_QName_A2.getLocalPart(), axisOp_A2_save);

        saveAxisConfiguration.addService(axisSrv_A_save);
        saveAxisConfiguration.addService(axisSrv_B_save);
        saveAxisConfiguration.addService(axisSrv_C_save);

        //---------------------

        axisSrvGrp_ABC_restore = new AxisServiceGroup(restoreAxisConfiguration);
        axisSrvGrp_ABC_restore.setServiceGroupName(serviceGroupName_ABC);

        axisSrv_A_restore = new AxisService(service_QName_A.getLocalPart());
        axisSrv_B_restore = new AxisService(service_QName_B.getLocalPart());
        axisSrv_C_restore = new AxisService(service_QName_C.getLocalPart());

        axisSrvGrp_ABC_restore.addService(axisSrv_A_restore);
        axisSrvGrp_ABC_restore.addService(axisSrv_B_restore);
        axisSrvGrp_ABC_restore.addService(axisSrv_C_restore);

        axisOp_A1_restore = new InOutAxisOperation(operation_QName_A1);
        axisOp_A2_restore = new InOutAxisOperation(operation_QName_A2);

        axisOp_A1_restore.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisOp_A2_restore.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisSrv_A_restore.addOperation(axisOp_A1_restore);
        axisSrv_A_restore
                .mapActionToOperation(operation_QName_A1.getLocalPart(), axisOp_A1_restore);

        axisSrv_A_restore.addOperation(axisOp_A2_restore);
        axisSrv_A_restore
                .mapActionToOperation(operation_QName_A2.getLocalPart(), axisOp_A2_restore);


        restoreAxisConfiguration.addService(axisSrv_A_restore);
        restoreAxisConfiguration.addService(axisSrv_B_restore);
        restoreAxisConfiguration.addService(axisSrv_C_restore);


        axisOp_A1_save.getRemainingPhasesInFlow().add(phaseSave);
        axisOp_A2_save.getRemainingPhasesInFlow().add(phaseSave);

        axisOp_A1_restore.getRemainingPhasesInFlow().add(phaseRestore);
        axisOp_A2_restore.getRemainingPhasesInFlow().add(phaseRestore);

        //-------------------------------

        axisSrvGrp_ABC_equiv = new AxisServiceGroup(equivAxisConfiguration);
        axisSrvGrp_ABC_equiv.setServiceGroupName(serviceGroupName_ABC);

        axisSrv_A_equiv = new AxisService(service_QName_A.getLocalPart());
        axisSrv_B_equiv = new AxisService(service_QName_B.getLocalPart());
        axisSrv_C_equiv = new AxisService(service_QName_C.getLocalPart());

        axisSrvGrp_ABC_equiv.addService(axisSrv_A_equiv);
        axisSrvGrp_ABC_equiv.addService(axisSrv_B_equiv);
        axisSrvGrp_ABC_equiv.addService(axisSrv_C_equiv);

        axisOp_A1_equiv = new InOutAxisOperation(operation_QName_A1);
        axisOp_A2_equiv = new InOutAxisOperation(operation_QName_A2);

        axisOp_A1_equiv.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisOp_A2_equiv.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisSrv_A_equiv.addOperation(axisOp_A1_equiv);
        axisSrv_A_equiv.mapActionToOperation(operation_QName_A1.getLocalPart(), axisOp_A1_equiv);

        axisSrv_A_equiv.addOperation(axisOp_A2_equiv);
        axisSrv_A_equiv.mapActionToOperation(operation_QName_A2.getLocalPart(), axisOp_A2_equiv);

        equivAxisConfiguration.addService(axisSrv_A_equiv);
        equivAxisConfiguration.addService(axisSrv_B_equiv);
        equivAxisConfiguration.addService(axisSrv_C_equiv);

        //-----------------------------------------------------------------
        // setup the context objects
        //-----------------------------------------------------------------
        srvGrpCtx_ABC_save = saveConfigurationContext.createServiceGroupContext(axisSrvGrp_ABC_save);
        srvGrpCtx_ABC_save.setId(serviceGroupName_ABC);

        srvCtx_A_save = srvGrpCtx_ABC_save.getServiceContext(axisSrv_A_save);
        srvCtx_B_save = srvGrpCtx_ABC_save.getServiceContext(axisSrv_B_save);
        srvCtx_C_save = srvGrpCtx_ABC_save.getServiceContext(axisSrv_C_save);

        opCtx_A1_save = srvCtx_A_save.createOperationContext(operation_QName_A1);
        opCtx_A2_save = srvCtx_A_save.createOperationContext(operation_QName_A2);

        //----------------------------------------

        srvGrpCtx_ABC_equiv =
                equivConfigurationContext.createServiceGroupContext(axisSrvGrp_ABC_equiv);
        srvGrpCtx_ABC_equiv.setId(serviceGroupName_ABC);

        srvCtx_A_equiv = srvGrpCtx_ABC_equiv.getServiceContext(axisSrv_A_equiv);
        srvCtx_B_equiv = srvGrpCtx_ABC_equiv.getServiceContext(axisSrv_B_equiv);
        srvCtx_C_equiv = srvGrpCtx_ABC_equiv.getServiceContext(axisSrv_C_equiv);

        opCtx_A1_equiv = srvCtx_A_equiv.createOperationContext(operation_QName_A1);
        opCtx_A2_equiv = srvCtx_A_equiv.createOperationContext(operation_QName_A2);

        //----------------------------------------
        // message context objects
        //----------------------------------------
        msgCtx_A1_1_save = createMessageContext(opCtx_A1_save, saveConfigurationContext,
                                                MessageContext.IN_FLOW);
        msgCtx_A1_2_save = createMessageContext(opCtx_A1_save, saveConfigurationContext,
                                                MessageContext.OUT_FLOW);
        msgCtx_A2_save = createMessageContext(opCtx_A2_save, saveConfigurationContext,
                                              MessageContext.IN_FLOW);

        msgCtx_A1_1_equiv = createMessageContext(opCtx_A1_equiv, equivConfigurationContext,
                                                 MessageContext.IN_FLOW);
        msgCtx_A1_2_equiv = createMessageContext(opCtx_A1_equiv, equivConfigurationContext,
                                                 MessageContext.OUT_FLOW);
        msgCtx_A2_equiv = createMessageContext(opCtx_A2_equiv, equivConfigurationContext,
                                               MessageContext.IN_FLOW);

        //-----------------------------------------------------------------
        // other objects
        //-----------------------------------------------------------------
        executedHandlers = new ArrayList();
    }


    private MessageContext createMessageContext(OperationContext oc, ConfigurationContext cc,
                                                int flowType) throws Exception {
        MessageContext mc = cc.createMessageContext();

        mc.setFLOW(flowType);
        mc.setTransportIn(transportIn);
        mc.setTransportOut(transportOut);

        mc.setServerSide(true);
//        mc.setProperty(MessageContext.TRANSPORT_OUT, System.out);

        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(omFac.getDefaultEnvelope());

        AxisOperation axisOperation = oc.getAxisOperation();
        String action = axisOperation.getName().getLocalPart();
        mc.setSoapAction(action);
//        System.out.flush();

        mc.setMessageID(UUIDGenerator.getUUID());

        axisOperation.registerOperationContext(mc, oc);
        mc.setOperationContext(oc);

        ServiceContext sc = oc.getServiceContext();
        mc.setServiceContext(sc);

        mc.setTo(new EndpointReference("axis2/services/NullService"));
        mc.setWSAAction("DummyOp");

        AxisMessage axisMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        mc.setAxisMessage(axisMessage);

        return mc;
    }

    protected void setUp() throws Exception {
        //org.apache.log4j.BasicConfigurator.configure();
    }

    public void testHierarchyNewContext() throws Exception {
        String title = "MessageContextSaveCTest:testHierarchyNewContext(): ";


        MessageContext mc = msgCtx_A1_2_save;
        OperationContext oc = mc.getOperationContext();
        log.debug(title + "*** Original OperationContext message context table ****");
        showMCTable(oc);

        // run the message through the message processing
        // this causes the message context to get saved 
        log.debug(title + "- - - Save the message context from the engine - - - -");
        AxisEngine.receive(mc);

        LinkedHashMap original_object_graph = getObjectGraphInfo(mc);
        log.debug(title + "*** Originial object graph ****");
        showObjectGraphInfo(original_object_graph);

        log.debug(title +
                "- - - Restore the message context on a separate engine  - - - - - - - - - - - - - - - -");
        MessageContext mc2 = restoreMessageContext(persistentStore, restoreConfigurationContext);

        LinkedHashMap restored_object_graph = getObjectGraphInfo(mc2);
        log.debug(title + "*** Restored object graph ****");
        showObjectGraphInfo(restored_object_graph);

        OperationContext oc2 = mc2.getOperationContext();
        log.debug(title + "*** Restored OperationContext message context table ****");
        showMCTable(oc2);

        boolean mcTableMatch = compareMCTable(oc, oc2);
        assertTrue(mcTableMatch);

        // resume the restored paused message context on an engine that has the 
        // same setup as the engine where the save occurred
        // but doesn't have the Context objects
        log.debug(title +
                "- - - Resume the restored message context - - - - - - - - - - - - - - - -");
        AxisEngine.resume(mc2);

        LinkedHashMap resumed_object_graph = getObjectGraphInfo(mc2);
        log.debug(title + "*** Post Resumed object graph ****");
        showObjectGraphInfo(resumed_object_graph);

        // compare object hashcodes - expect differences
        boolean expectFalse =
                compareObjectGraphInfo(original_object_graph, restored_object_graph, true);
        assertFalse(expectFalse);

        boolean expectSameObjectIds =
                compareObjectGraphInfo(original_object_graph, restored_object_graph, false);
        assertTrue(expectSameObjectIds);

        boolean isSame = compareObjectGraphInfo(restored_object_graph, resumed_object_graph, false);
        assertTrue(isSame);

    }

    public void testHierarchyReuse() throws Exception {
        String title = "MessageContextSaveCTest:testHierarchyReuse(): ";


        MessageContext mc = msgCtx_A1_1_save;

        // run the message through the message processing
        // this causes the message context to get saved 
        log.debug(title + "- - - Save the message context from the engine - - - -");
        AxisEngine.receive(mc);

        LinkedHashMap original_object_graph = getObjectGraphInfo(mc);
        log.debug(title + "*** Originial object graph ****");
        showObjectGraphInfo(original_object_graph);

        log.debug(title +
                "- - - Restore the message context on a separate engine  - - - - - - - - - - - - - - - -");
        MessageContext mc2 = restoreMessageContext(persistentStore, equivConfigurationContext);

        LinkedHashMap restored_object_graph = getObjectGraphInfo(mc2);
        log.debug(title + "*** Restored object graph ****");
        showObjectGraphInfo(restored_object_graph);

        // we don't use strict checking here since the engine where the 
        // restoration takes place is a "copy", ie, there are new objects
        // for the same names/identifications
        boolean hasEquivalence =
                compareObjectGraphInfo(original_object_graph, restored_object_graph, false);
        assertTrue(hasEquivalence);

        // get an object graph from an equivalent message context on the separate engine
        // we would expect a strict match of the object graphs between the 
        // restored message context and the message context on the separate engine
        LinkedHashMap separate_object_graph = getObjectGraphInfo(msgCtx_A1_1_equiv);

        // compare the restored object graph with the existing object graph
        boolean expectStrict =
                compareObjectGraphInfo(restored_object_graph, separate_object_graph, true);
        assertTrue(expectStrict);

        // resume the restored paused message context on an engine that has the 
        // same setup as the engine where the save occurred
        // and save has the Service-level Context objects
        log.debug(title +
                "- - - Resume the restored message context - - - - - - - - - - - - - - - -");
        AxisEngine.resume(mc2);

        LinkedHashMap resumed_object_graph = getObjectGraphInfo(mc2);
        log.debug(title + "*** Post Resumed object graph ****");
        showObjectGraphInfo(resumed_object_graph);

        // there should be no changes in the object graph in our case after the resume
        hasEquivalence = compareObjectGraphInfo(restored_object_graph, resumed_object_graph, true);
        assertTrue(hasEquivalence);


    }

    /**
     * Restores a previously saved message context
     */
    public MessageContext restoreMessageContext(File restoreFile, ConfigurationContext cc) {
        String title = "restoreMessageContext(): ";

        MessageContext restoredMC = null;

        File theFile = restoreFile;
        String theFilename = null;

        // the configuration context to use for message context activation
        ConfigurationContext cfgCtx = cc;

        boolean restoredOk = false;

        if ((theFile != null) && (theFile.exists())) {
            theFilename = theFile.getName();
            log.debug(title + "temp file = [" + theFilename + "]");

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
                restoredOk = false;

                MessageContext msgContext2 = (MessageContext) inObjStream.readObject();
                inObjStream.close();
                inStream.close();

                msgContext2.activate(cfgCtx);

                restoredOk = true;
                log.debug(title + "....restored message context.....");

                // now put the restored message context in the global
                // variable for the test 
                restoredMC = msgContext2;
            }
            catch (Exception ex2) {
                log.debug(title + "error with restoring message context = [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
                restoredMessageContext = null;
            }

            assertTrue(restoredOk);

            // if the restore of the message context succeeded,
            // then don't keep the temporary file around
            boolean removeTmpFile = restoredOk;
            if (removeTmpFile) {
                try {
                    theFile.delete();
                }
                catch (Exception e) {
                    // just absorb it
                }
            }
        }

        return restoredMC;
    }


    private LinkedHashMap getObjectGraphInfo(MessageContext msgCtx) {
        if (msgCtx == null) {
            return null;
        }

        MetaDataEntry metaMC = null;
        MetaDataEntry metaOC = null;
        MetaDataEntry metaSC = null;
        MetaDataEntry metaSGC = null;
        MetaDataEntry metaCC = null;
        MetaDataEntry metaAO = null;
        MetaDataEntry metaAS = null;
        MetaDataEntry metaASG = null;
        MetaDataEntry metaAC = null;

        String keyMC = null;
        String keyOC = null;
        String keySC = null;
        String keySGC = null;
        String keyCC = null;
        String keyAO = null;
        String keyAS = null;
        String keyASG = null;
        String keyAC = null;

        LinkedHashMap objInfo = new LinkedHashMap();

        // get the identification info about the primary objects in the object graph
        //     class name
        //     name string
        //     hashcode string

        // message context
        keyMC = msgCtx.getClass().getName();
        metaMC = new MetaDataEntry(keyMC, msgCtx.getMessageID(), "[" + msgCtx.hashCode() + "]");
        objInfo.put(keyMC, metaMC);

        // operation context
        OperationContext oc = msgCtx.getOperationContext();
        keyOC = oc.getClass().getName();
        metaOC = new MetaDataEntry(keyOC, oc.getOperationName(), "[" + oc.hashCode() + "]");
        objInfo.put(keyOC, metaOC);

        // service context
        ServiceContext sc = msgCtx.getServiceContext();
        keySC = sc.getClass().getName();
        metaSC = new MetaDataEntry(keySC, sc.getName(), "[" + sc.hashCode() + "]");
        objInfo.put(keySC, metaSC);

        // service group context
        ServiceGroupContext sgc = msgCtx.getServiceGroupContext();
        keySGC = sgc.getClass().getName();
        metaSGC = new MetaDataEntry(keySGC, sgc.getId(), "[" + sgc.hashCode() + "]");
        objInfo.put(keySGC, metaSGC);

        // configuration context
        ConfigurationContext cc = msgCtx.getConfigurationContext();
        keyCC = cc.getClass().getName();
        metaCC = new MetaDataEntry(keyCC, null, "[" + cc.hashCode() + "]");
        objInfo.put(keyCC, metaCC);

        // axis operation
        AxisOperation ao = msgCtx.getAxisOperation();
        keyAO = ao.getClass().getName();
        metaAO = new MetaDataEntry(keyAO, ao.getName().toString(), "[" + ao.hashCode() + "]");
        objInfo.put(keyAO, metaAO);

        // axis service
        AxisService as = msgCtx.getAxisService();
        keyAS = as.getClass().getName();
        metaAS = new MetaDataEntry(keyAS, as.getName(), "[" + as.hashCode() + "]");
        objInfo.put(keyAS, metaAS);

        // axis service group
        AxisServiceGroup asg = msgCtx.getAxisServiceGroup();
        keyASG = asg.getClass().getName();
        metaASG = new MetaDataEntry(keyASG, asg.getServiceGroupName(), "[" + asg.hashCode() + "]");
        objInfo.put(keyASG, metaASG);

        // axis configuration
        AxisConfiguration ac = cc.getAxisConfiguration();
        keyAC = ac.getClass().getName();
        metaAC = new MetaDataEntry(keyAC, null, "[" + ac.hashCode() + "]");
        objInfo.put(keyAC, metaAC);

        return objInfo;
    }

    /**
     * Compare two mappings containing object graph info.
     * This uses the class name and object ID.
     * <p/>
     * Strict comparison includes the object hash codes. If
     * you expect the same object to be represented in
     * both maps, you may want to use Strict checking.
     * <p/>
     *
     * @param map1   The first object graph info map
     * @param map2   The second object graph info map
     * @param strict TRUE if strict comparison
     * @return Outcome of the comparison: TRUE if equivalent, FALSE otherwise
     */
    private boolean compareObjectGraphInfo(LinkedHashMap map1, LinkedHashMap map2, boolean strict) {
        String title = "MessageContextSaveCTest: compareObjectGraphInfo(): ";

        if ((map1 != null) && (map2 != null)) {
            if (map1.size() != map2.size()) {
                log.debug(title + "Object graph info mappings are different sizes.");
                return false;
            }

            Iterator it = map1.keySet().iterator();

            while (it.hasNext()) {
                // the key is the class name associated with the object
                String key = (String) it.next();

                // skip certain objects, those will always be unique
                if ((key.indexOf("MessageContext") == -1) &&
                        (key.indexOf("OperationContext") == -1) &&
                        (key.indexOf("ConfigurationContext") == -1) &&
                        (key.indexOf("AxisConfiguration") == -1)
                        ) {
                    // the class names listed above were not found
                    // so we're dealing with the other objects
                    MetaDataEntry value1 = (MetaDataEntry) map1.get(key);
                    MetaDataEntry value2 = (MetaDataEntry) map2.get(key);

                    if ((value1 != null) && (value2 != null)) {
                        // check the object identification
                        String name1 = value1.getName();
                        String name2 = value2.getName();

                        if ((name1 != null) && (name2 != null)) {
                            if (name1.equals(name2) == false) {
                                log.debug(title + "name1 [" + name1 + "]  !=   name2 [" +
                                        name2 + "]");
                                return false;
                            }
                        } else if ((name1 == null) && (name2 == null)) {
                            // ok
                        } else {
                            // mismatch
                            log.debug(title + "name1 [" + name1 + "]  !=   name2 [" + name2 + "]");
                            return false;
                        }

                        // Strict testing means checking the object hashcodes.
                        // Use this option when you expect the same
                        // objects in the map.
                        if (strict) {
                            String code1 = value1.getExtraName();
                            String code2 = value2.getExtraName();

                            if ((code1 != null) && (code2 != null)) {
                                if (code1.equals(code2) == false) {
                                    log.debug(title + "name [" + name1 + "]  code1 [" +
                                            code1 + "]  !=   code2 [" + code2 + "]");
                                    return false;
                                }
                            } else if ((code1 == null) && (code2 == null)) {
                                // ok
                            } else {
                                // mismatch
                                log.debug(title + "name [" + name1 + "]code1 [" + code1 +
                                        "]  !=   code2 [" + code2 + "]");
                                return false;
                            }
                        }
                    } else if ((value1 == null) && (value2 == null)) {
                        // ok
                    } else {
                        // mismatch
                        log.debug(title + "value1 [" + value1 + "]  !=   value2 [" + value2 + "]");
                        return false;
                    }
                }
            }

            return true;

        } else if ((map1 == null) && (map2 == null)) {
            return true;
        } else {
            log.debug(title + "mismatch: one or more of the maps are null.  ");
            return false;
        }

    }


    private void showObjectGraphInfo(LinkedHashMap map) {
        if (map == null) {
            return;
        }

        Iterator it = map.keySet().iterator();

        while (it.hasNext()) {
            String metaClassName = (String) it.next();
            MetaDataEntry meta = (MetaDataEntry) map.get(metaClassName);

            if (meta != null) {
                String classname = meta.getClassName();
                String name = meta.getName();
                String hashcode = meta.getExtraName();

                log.debug("class[" + classname + "]  id[" + name + "]  hashcode" + hashcode + " ");
            }

        }

    }


    private boolean compareMCTable(OperationContext oc1, OperationContext oc2) {
        String title = "compareMCTable: ";

        if ((oc1 != null) && (oc2 != null)) {
            HashMap mcTable1 = oc1.getMessageContexts();
            HashMap mcTable2 = oc2.getMessageContexts();

            if ((mcTable1 != null) && (mcTable2 != null)) {
                if ((!mcTable1.isEmpty()) && (!mcTable2.isEmpty())) {
                    int size1 = mcTable1.size();
                    int size2 = mcTable2.size();

                    if (size1 != size2) {
                        log.debug(title +
                                " Return FALSE:  table sizes don't match   size1[" + size1 +
                                "] != size2 [" + size2 + "] ");
                        return false;
                    }

                    Iterator it1 = mcTable1.keySet().iterator();

                    while (it1.hasNext()) {
                        String key1 = (String) it1.next();
                        MessageContext mc1 = (MessageContext) mcTable1.get(key1);
                        MessageContext mc2 = (MessageContext) mcTable2.get(key1);

                        if ((mc1 != null) && (mc2 != null)) {
                            // check the IDs
                            String id1 = mc1.getMessageID();
                            String id2 = mc2.getMessageID();

                            if ((id1 != null) && (id2 != null)) {
                                if (!id1.equals(id2)) {
                                    log.debug(title +
                                            " Return FALSE:  message IDs don't match   id1[" + id1 +
                                            "] != id2 [" + id2 + "] ");
                                    return false;
                                }
                            } else if ((id1 == null) && (id2 == null)) {
                                // can't tell, keep going
                            } else {
                                // mismatch
                                log.debug(title +
                                        " Return FALSE:  message IDs don't match   id1[" + id1 +
                                        "] != id2 [" + id2 + "] ");
                                return false;
                            }

                        } else if ((mc1 == null) && (mc2 == null)) {
                            // entries match
                        } else {
                            // mismatch
                            log.debug(
                                    title + " Return FALSE:  message context objects don't match ");
                            return false;
                        }
                    }

                    log.debug(title + " Return TRUE:  message context tables match");
                    return true;

                } else if (mcTable1.isEmpty() && mcTable2.isEmpty()) {
                    log.debug(title + " Return TRUE:  message context tables are both empty ");
                    return true;
                } else {
                    log.debug(title + " Return FALSE:  message context tables mismatch");
                    return false;
                }
            } else if ((mcTable1 == null) && (mcTable2 == null)) {
                log.debug(title + " Return TRUE:  message context tables are null");
                return true;
            } else {
                log.debug(title + " Return FALSE:  message context tables don't match");
                return false;
            }
        } else if ((oc1 == null) && (oc2 == null)) {
            log.debug(title + " Return TRUE:  operation context objects are null ");
            return true;
        } else {
            log.debug(title + " Return FALSE:  operation context objects don't match ");
            return false;
        }


    }


    private void showMCTable(OperationContext oc) {
        if (oc == null) {
            return;
        }

        HashMap mcTable = oc.getMessageContexts();

        if ((mcTable == null) || (mcTable.isEmpty())) {
            return;
        }

        Iterator it = mcTable.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();
            MessageContext mc = (MessageContext) mcTable.get(key);

            if (mc != null) {
                String id = mc.getMessageID();
                log.debug("message context table entry:   label [" + key +
                        "]    message ID [" + id + "]    ");
            }
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

    //=========================================================================
    // Handler classes 
    //=========================================================================

    /**
     * Pauses and saves the message context the message context
     */
    public class SaveHandler extends AbstractHandler {
        private Integer handlerID = null;

        private File theFile = null;
        private String theFilename = null;

        private boolean performSave = true;

        private boolean savedOk = false;

        //-----------------------------------------------------------------
        // constructors
        //-----------------------------------------------------------------

        public SaveHandler() {
            this.handlerID = new Integer(-5);
        }

        public SaveHandler(int index, File saveFile, boolean doIt) {
            this.handlerID = new Integer(index);
            init(new HandlerDescription(new String("handler" + index)));
            theFile = saveFile;
            performSave = doIt;
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
            String title = "SaveHandler[" + getHandlerID() + "]:invoke(): ";
            savedOk = false;

            if (performSave == false) {
                log.debug(title + "Configured for no action to be performed.");
                return InvocationResponse.CONTINUE;
            }


            log.debug(title + "msgContext.pause()");
            msgContext.pause();

            if (theFile != null) {
                try {
                    log.debug(title + "Resetting the file to use.");
                    theFile.delete();
                    theFile.createNewFile();
                    theFilename = theFile.getName();
                    log.debug(title + "temp file = [" + theFilename + "]");
                }
                catch (Exception ex) {
                    log.debug(title + "error creating new file = [" + ex.getMessage() + "]");
                }

                if (theFile.exists() == true) {
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
                        savedOk = false;
                        outObjStream.writeObject(msgContext);

                        // close out the streams
                        outObjStream.flush();
                        outObjStream.close();
                        outStream.flush();
                        outStream.close();

                        savedOk = true;
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

                    assertTrue(savedOk);

                }
            }

            log.debug(title + "executedHandlers.add(" + handlerID + ")");
            executedHandlers.add(handlerID);

            return InvocationResponse.SUSPEND;

        }

    }


    public class TempHandler extends AbstractHandler {
        private Integer handlerID = null;
        private int count = 0;
        private int numberProperties = 3;
        private String propertyKey = "Property";
        private String propertyValue = "ServiceLevelSetting";

        //-----------------------------------------------------------------
        // constructors
        //-----------------------------------------------------------------

        public TempHandler() {
            this.handlerID = new Integer(-5);
        }

        public TempHandler(int index) {
            this.handlerID = new Integer(index);
            init(new HandlerDescription(new String("handler" + index)));
        }

        public TempHandler(int index, int number) {
            this.handlerID = new Integer(index);
            init(new HandlerDescription(new String("handler" + index)));
            numberProperties = number;
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

            // get the service context from the message context
            ServiceContext serviceContext = msgContext.getServiceContext();

            if (serviceContext == null) {
                // get the service context from the operation context
                OperationContext operationContext = msgContext.getOperationContext();
                serviceContext = operationContext.getServiceContext();
            }

            if (serviceContext != null) {
                for (int j = 0; j < numberProperties; j++) {
                    count++;
                    String key = new String(propertyKey + ".ID[" + getHandlerID() + "]." + count);
                    String value = new String(propertyValue + "[" + count + "]");
                    serviceContext.setProperty(key, value);
                }
            }

            log.debug(title + "executedHandlers.add(" + handlerID + ")");
            executedHandlers.add(handlerID);

            return InvocationResponse.CONTINUE;
        }

    }

}
