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
import java.util.Map;

public class MessageContextSaveBTest extends TestCase {
    protected static final Log log = LogFactory.getLog(MessageContextSaveBTest.class);

    //-------------------------------------------------------------------------
    // test key-value pairs
    //-------------------------------------------------------------------------
    private String [] serviceKeys = {
            "serviceKey1",
            "serviceKey2",
            "serviceKey3"
    };

    private String [] serviceValues = {
            "serviceValue1",
            "serviceValue2",
            "serviceValue3"
    };

    //-------------------------------------------------------------------------
    // variables for the object graph 
    //-------------------------------------------------------------------------
    // used on a save/restore of the message context
    // has a full object graph with 3 sets of service groups 
    // and associated objects
    private ConfigurationContext configurationContext = null;
    private AxisConfiguration axisConfiguration = null;

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

    private ServiceGroupContext srvGrpCtx_ABC = null;
    private AxisServiceGroup axisSrvGrp_ABC = null;

    private ServiceContext srvCtx_A = null;
    private ServiceContext srvCtx_B = null;
    private ServiceContext srvCtx_C = null;
    private AxisService axisSrv_A = null;
    private AxisService axisSrv_B = null;
    private AxisService axisSrv_C = null;

    private OperationContext opCtx_A1 = null;
    private OperationContext opCtx_A2 = null;
    private AxisOperation axisOp_A1 = null;
    private AxisOperation axisOp_A2 = null;

    private MessageContext msgCtx_A1 = null;
    private MessageContext msgCtx_A2 = null;
    //private AxisMessage          axisMsg_A1    = null;
    //private AxisMessage          axisMsg_A2    = null;

    //------------------------------
    // service group 123
    //------------------------------
    private String serviceGroupName_123 = "123ServiceGroup";

    private String serviceName_1 = "Service1";
    private String serviceName_2 = "Service2";
    private String serviceName_3 = "Service3";
    private String serviceName_4 = "Service4";
    private QName service_QName_1 = new QName(serviceName_1);
    private QName service_QName_2 = new QName(serviceName_2);
    private QName service_QName_3 = new QName(serviceName_3);
    private QName service_QName_4 = new QName(serviceName_4);

    private String operationName_1_1 = "TestOperation1_1";
    private String operationName_1_2 = "TestOperation1_2";
    private QName operation_QName_1_1 = new QName(operationName_1_1);
    private QName operation_QName_1_2 = new QName(operationName_1_2);

    private ServiceGroupContext srvGrpCtx_123 = null;
    private AxisServiceGroup axisSrvGrp_123 = null;

    private ServiceContext srvCtx_1 = null;
    private ServiceContext srvCtx_2 = null;
    private ServiceContext srvCtx_3 = null;
    private ServiceContext srvCtx_4 = null;
    private AxisService axisSrv_1 = null;
    private AxisService axisSrv_2 = null;
    private AxisService axisSrv_3 = null;
    private AxisService axisSrv_4 = null;

    private OperationContext opCtx_1_1 = null;
    private OperationContext opCtx_1_2 = null;
    private AxisOperation axisOp_1_1 = null;
    private AxisOperation axisOp_1_2 = null;

    private MessageContext msgCtx_1_1 = null;
    private MessageContext msgCtx_1_2 = null;
    //private AxisMessage          axisMsg_1_1   = null;
    //private AxisMessage          axisMsg_1_2   = null;


    //------------------------------
    // service group DAY
    //------------------------------
    private String serviceGroupName_DAY = "DayServiceGroup";

    private String serviceName_Mon = "MondayService";
    private String serviceName_Tue = "TuesdayService";
    private String serviceName_Wed = "WednesdayService";
    private String serviceName_Thu = "ThursdayService";
    private String serviceName_Fri = "FridayService";
    private QName service_QName_Mon = new QName(serviceName_Mon);
    private QName service_QName_Tue = new QName(serviceName_Tue);
    private QName service_QName_Wed = new QName(serviceName_Wed);
    private QName service_QName_Thu = new QName(serviceName_Thu);
    private QName service_QName_Fri = new QName(serviceName_Fri);

    private String operationName_Mon_1 = "TestOperation_Mon_1";
    private String operationName_Mon_2 = "TestOperation_Mon_2";
    private QName operation_QName_Mon_1 = new QName(operationName_Mon_1);
    private QName operation_QName_Mon_2 = new QName(operationName_Mon_2);

    private ServiceGroupContext srvGrpCtx_DAY = null;
    private AxisServiceGroup axisSrvGrp_DAY = null;

    private ServiceContext srvCtx_Mon = null;
    private ServiceContext srvCtx_Tue = null;
    private ServiceContext srvCtx_Wed = null;
    private ServiceContext srvCtx_Thu = null;
    private ServiceContext srvCtx_Fri = null;
    private AxisService axisSrv_Mon = null;
    private AxisService axisSrv_Tue = null;
    private AxisService axisSrv_Wed = null;
    private AxisService axisSrv_Thu = null;
    private AxisService axisSrv_Fri = null;

    private OperationContext opCtx_Mon_1 = null;
    private OperationContext opCtx_Mon_2 = null;
    private AxisOperation axisOp_Mon_1 = null;
    private AxisOperation axisOp_Mon_2 = null;

    private MessageContext msgCtx_Mon_1 = null;
    private MessageContext msgCtx_Mon_2 = null;
    //private AxisMessage          axisMsg_Mon_1 = null;
    //private AxisMessage          axisMsg_Mon_2 = null;

    //-------------------------------------
    //  objects needed for message context
    //------------------------------------

    private TransportOutDescription transportOut = null;
    private TransportOutDescription transportOut2 = null;
    private TransportOutDescription transportOut3 = null;
    private TransportInDescription transportIn = null;
    private TransportInDescription transportIn2 = null;
    private TransportInDescription transportIn3 = null;

    private Phase phase1 = null;

    private ArrayList executedHandlers = null;

    private MessageContext restoredMessageContext = null;

    //-------------------------------------------------------------------------
    // methods
    //-------------------------------------------------------------------------


    public MessageContextSaveBTest(String arg0) {
        super(arg0);

        try {
            prepare();
        }
        catch (Exception e) {
            log.debug("MessageContextSaveBTest:constructor:  error in setting up object graph [" +
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

        axisConfiguration = new AxisConfiguration();

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

        //----------------------------
        // transport-related objects
        //----------------------------
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

        //----------------------------
        // phase-related objects
        //----------------------------
        phase1 = new Phase("Phase1");
        phase1.addHandler(new TempHandler(1, 2));
        phase1.addHandler(new HandlerMCS(2, true));
        phase1.addHandler(new TempHandler(3, 2));

        ArrayList phases = new ArrayList();
        phases.add(phase1);

        axisConfiguration.setInPhasesUptoAndIncludingPostDispatch(phases);

        //-----------------------------------------------------------------
        // setup the axis side of the hierachy
        //-----------------------------------------------------------------
        // ABC group
        //----------------------------
        axisSrvGrp_ABC = new AxisServiceGroup(axisConfiguration);
        axisSrvGrp_ABC.setServiceGroupName(serviceGroupName_ABC);

        axisSrv_A = new AxisService(service_QName_A.getLocalPart());
        axisSrv_B = new AxisService(service_QName_B.getLocalPart());
        axisSrv_C = new AxisService(service_QName_C.getLocalPart());

        axisSrvGrp_ABC.addService(axisSrv_A);
        axisSrvGrp_ABC.addService(axisSrv_B);
        axisSrvGrp_ABC.addService(axisSrv_C);

        axisOp_A1 = new InOutAxisOperation(operation_QName_A1);
        axisOp_A2 = new InOutAxisOperation(operation_QName_A2);

        axisOp_A1.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisOp_A2.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisSrv_A.addOperation(axisOp_A1);
        axisSrv_A.mapActionToOperation(operation_QName_A1.getLocalPart(), axisOp_A1);

        axisSrv_A.addOperation(axisOp_A2);
        axisSrv_A.mapActionToOperation(operation_QName_A2.getLocalPart(), axisOp_A2);

        axisConfiguration.addService(axisSrv_A);
        axisConfiguration.addService(axisSrv_B);
        axisConfiguration.addService(axisSrv_C);


        axisOp_A1.getRemainingPhasesInFlow().add(phase1);
        axisOp_A2.getRemainingPhasesInFlow().add(phase1);

        //----------------------------
        // 123 group
        //----------------------------
        axisSrvGrp_123 = new AxisServiceGroup(axisConfiguration);
        axisSrvGrp_123.setServiceGroupName(serviceGroupName_123);

        axisSrv_1 = new AxisService(service_QName_1.getLocalPart());
        axisSrv_2 = new AxisService(service_QName_2.getLocalPart());
        axisSrv_3 = new AxisService(service_QName_3.getLocalPart());
        axisSrv_4 = new AxisService(service_QName_4.getLocalPart());

        axisSrvGrp_123.addService(axisSrv_1);
        axisSrvGrp_123.addService(axisSrv_2);
        axisSrvGrp_123.addService(axisSrv_3);
        axisSrvGrp_123.addService(axisSrv_4);

        axisOp_1_1 = new InOutAxisOperation(operation_QName_1_1);
        axisOp_1_2 = new InOutAxisOperation(operation_QName_1_2);

        axisOp_1_1.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisOp_1_2.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisSrv_1.addOperation(axisOp_1_1);
        axisSrv_1.mapActionToOperation(operation_QName_1_1.getLocalPart(), axisOp_1_1);

        axisSrv_1.addOperation(axisOp_1_2);
        axisSrv_1.mapActionToOperation(operation_QName_1_2.getLocalPart(), axisOp_1_2);

        axisConfiguration.addService(axisSrv_1);
        axisConfiguration.addService(axisSrv_2);
        axisConfiguration.addService(axisSrv_3);
        axisConfiguration.addService(axisSrv_4);

        axisOp_1_1.getRemainingPhasesInFlow().add(phase1);
        axisOp_1_2.getRemainingPhasesInFlow().add(phase1);

        //----------------------------
        // DAY group
        //----------------------------
        axisSrvGrp_DAY = new AxisServiceGroup(axisConfiguration);
        axisSrvGrp_DAY.setServiceGroupName(serviceGroupName_DAY);

        axisSrv_Mon = new AxisService(service_QName_Mon.getLocalPart());
        axisSrv_Tue = new AxisService(service_QName_Tue.getLocalPart());
        axisSrv_Wed = new AxisService(service_QName_Wed.getLocalPart());
        axisSrv_Thu = new AxisService(service_QName_Thu.getLocalPart());
        axisSrv_Fri = new AxisService(service_QName_Fri.getLocalPart());

        axisSrvGrp_DAY.addService(axisSrv_Mon);
        axisSrvGrp_DAY.addService(axisSrv_Tue);
        axisSrvGrp_DAY.addService(axisSrv_Wed);
        axisSrvGrp_DAY.addService(axisSrv_Thu);
        axisSrvGrp_DAY.addService(axisSrv_Fri);

        axisOp_Mon_1 = new InOutAxisOperation(operation_QName_Mon_1);
        axisOp_Mon_2 = new InOutAxisOperation(operation_QName_Mon_2);

        axisOp_Mon_1.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisOp_Mon_2.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) {
            }
        });

        axisSrv_Mon.addOperation(axisOp_Mon_1);
        axisSrv_Mon.mapActionToOperation(operation_QName_Mon_1.getLocalPart(), axisOp_Mon_1);

        axisSrv_Mon.addOperation(axisOp_Mon_2);
        axisSrv_Mon.mapActionToOperation(operation_QName_Mon_2.getLocalPart(), axisOp_Mon_2);

        axisConfiguration.addService(axisSrv_Mon);
        axisConfiguration.addService(axisSrv_Tue);
        axisConfiguration.addService(axisSrv_Wed);
        axisConfiguration.addService(axisSrv_Thu);
        axisConfiguration.addService(axisSrv_Fri);

        axisOp_Mon_1.getRemainingPhasesInFlow().add(phase1);
        axisOp_Mon_2.getRemainingPhasesInFlow().add(phase1);

        //-----------------------------------------------------------------
        // setup the context objects
        //-----------------------------------------------------------------
        srvGrpCtx_ABC = configurationContext.createServiceGroupContext(axisSrvGrp_ABC);
        srvGrpCtx_ABC.setId(serviceGroupName_ABC);

        srvGrpCtx_123 = configurationContext.createServiceGroupContext(axisSrvGrp_123);
        srvGrpCtx_123.setId(serviceGroupName_ABC);

        srvGrpCtx_DAY = configurationContext.createServiceGroupContext(axisSrvGrp_DAY);
        srvGrpCtx_DAY.setId(serviceGroupName_DAY);

        srvCtx_A = srvGrpCtx_ABC.getServiceContext(axisSrv_A);
        srvCtx_B = srvGrpCtx_ABC.getServiceContext(axisSrv_B);
        srvCtx_C = srvGrpCtx_ABC.getServiceContext(axisSrv_C);

        srvCtx_1 = srvGrpCtx_123.getServiceContext(axisSrv_1);
        srvCtx_2 = srvGrpCtx_123.getServiceContext(axisSrv_2);
        srvCtx_3 = srvGrpCtx_123.getServiceContext(axisSrv_3);
        srvCtx_4 = srvGrpCtx_123.getServiceContext(axisSrv_4);

        srvCtx_Mon = srvGrpCtx_DAY.getServiceContext(axisSrv_Mon);
        srvCtx_Tue = srvGrpCtx_DAY.getServiceContext(axisSrv_Tue);
        srvCtx_Wed = srvGrpCtx_DAY.getServiceContext(axisSrv_Wed);
        srvCtx_Thu = srvGrpCtx_DAY.getServiceContext(axisSrv_Thu);
        srvCtx_Fri = srvGrpCtx_DAY.getServiceContext(axisSrv_Fri);

        opCtx_A1 = srvCtx_A.createOperationContext(operation_QName_A1);
        opCtx_A2 = srvCtx_A.createOperationContext(operation_QName_A2);

        opCtx_1_1 = srvCtx_1.createOperationContext(operation_QName_1_1);
        opCtx_1_2 = srvCtx_1.createOperationContext(operation_QName_1_2);

        opCtx_Mon_1 = srvCtx_Mon.createOperationContext(operation_QName_Mon_1);
        opCtx_Mon_2 = srvCtx_Mon.createOperationContext(operation_QName_Mon_2);

        //----------------------------------------
        // message context objects
        //----------------------------------------
        msgCtx_A1 = createMessageContext(opCtx_A1);
        msgCtx_A2 = createMessageContext(opCtx_A2);

        msgCtx_1_1 = createMessageContext(opCtx_1_1);
        msgCtx_1_2 = createMessageContext(opCtx_1_2);

        msgCtx_Mon_1 = createMessageContext(opCtx_Mon_1);
        msgCtx_Mon_2 = createMessageContext(opCtx_Mon_2);

        //-----------------------------------------------------------------
        // other objects
        //-----------------------------------------------------------------
        executedHandlers = new ArrayList();
    }


    private MessageContext createMessageContext(OperationContext oc) throws Exception {
        MessageContext mc = configurationContext.createMessageContext();
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

    public void testServiceProperties() throws Exception {
        String title = "MessageContextSaveBTest: testServiceProperties(): ";


        MessageContext mc = msgCtx_1_1;

        // add some service-level properties
        String suffix = "_before";
        addServiceProperties(mc, suffix);

        // get the service level properties into a separate table
        Map properties_original = new HashMap(getServiceProperties(mc));
        showProperties(properties_original, "original properties");

        log.debug(title + "start - - engine.receive(mc) - - - - - - - - - - - - - - - -");
        AxisEngine.receive(mc);

        // get the service level properties into a separate table
        Map properties2 = new HashMap(getServiceProperties(restoredMessageContext));
        showProperties(properties2, "restored properties");

        // add some more properties
        suffix = "_postReceive";
        addServiceProperties(restoredMessageContext, suffix);

        // resume the paused message context
        log.debug(title + "resume - - engine.resume(mc) - - - - - - - - - - - - - - - -");
        AxisEngine.resume(restoredMessageContext);

        // get the service level properties into a separate table
        Map properties3 = new HashMap(getServiceProperties(restoredMessageContext));
        showProperties(properties3, "restored service properties post-resume");

        // get the service level properties from the other message context
        // in the same service 
        Map properties4 = getServiceProperties(msgCtx_1_2);
        showProperties(properties4, "service properties from other active MsgCtx");

        // the service level properties should be the same
        boolean isOk = ActivateUtils.isEquivalent(properties3, properties4, true);
        assertTrue(isOk);

    }


    private Map getServiceProperties(MessageContext mc) {
        Map properties = null;

        // get the service context from the message context
        ServiceContext serviceContext = mc.getServiceContext();

        if (serviceContext == null) {
            // get the service context from the operation context
            OperationContext operationContext = mc.getOperationContext();
            serviceContext = operationContext.getServiceContext();
        }

        if (serviceContext != null) {
            properties = serviceContext.getProperties();
        }

        return properties;
    }

    private void addServiceProperties(MessageContext mc, String suffix) {
        // get the service context from the message context
        ServiceContext serviceContext = mc.getServiceContext();

        if (serviceContext == null) {
            // get the service context from the operation context
            OperationContext operationContext = mc.getOperationContext();
            serviceContext = operationContext.getServiceContext();
        }

        if (serviceContext != null) {
            for (int k = 0; k < serviceKeys.length; k++) {
                String key = serviceKeys[k];
                String value = serviceValues[k] + suffix;

                serviceContext.setProperty(key, value);
            }
        }
    }


    private void showProperties(Map map, String description) {
        log.debug(description + " ======================================");
        if ((map == null) || (map.isEmpty())) {
            log.debug(description + ": No properties");
            log.debug(description + " ======================================");
            return;
        }

        Iterator it = map.keySet().iterator();

        while (it.hasNext()) {
            String key = (String) it.next();

            String value = (String) map.get(key);

            log.debug(description + ": key-value pair [" + key + "][" + value + "]");
        }
        log.debug(description + " ======================================");
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
     * Performs a save and restore on the message context
     */
    public class HandlerMCS extends AbstractHandler {
        private Integer handlerID = null;

        private File theFile = null;
        private String theFilename = null;

        private boolean pause = false;
        private boolean savedOk = false;
        private boolean restoredOk = false;
        private boolean comparesOk = false;

        //-----------------------------------------------------------------
        // constructors
        //-----------------------------------------------------------------

        public HandlerMCS() {
            this.handlerID = new Integer(-5);
        }

        public HandlerMCS(int index, boolean pause) {
            this.handlerID = new Integer(index);
            this.pause = pause;
            init(new HandlerDescription(new String("handler" + index)));
        }

        public HandlerMCS(int index) {
            this.handlerID = new Integer(index);
            init(new HandlerDescription(new String("handler" + index)));
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
            String title = "HandlerMCS[" + getHandlerID() + "]:invoke(): ";
            log.debug(title + "pause = [" + pause + "]");
            savedOk = false;
            restoredOk = false;

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

                        msgContext2.activate(configurationContext);

                        restoredOk = true;
                        log.debug(title + "....restored message context.....");

                        // now put the restored message context in the global
                        // variable for the test 
                        restoredMessageContext = msgContext2;
                    }
                    catch (Exception ex2) {
                        log.debug(title + "error with restoring message context = [" +
                                ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                        ex2.printStackTrace();
                        restoredMessageContext = null;
                    }

                    assertTrue(restoredOk);

                    // if the save/restore of the message context succeeded,
                    // then don't keep the temporary file around
                    boolean removeTmpFile = savedOk && restoredOk;
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
