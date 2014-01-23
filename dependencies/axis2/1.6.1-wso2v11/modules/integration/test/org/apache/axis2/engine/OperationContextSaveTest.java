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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class OperationContextSaveTest extends TestCase {
    protected static final Log log = LogFactory.getLog(OperationContextSaveTest.class);

    private QName serviceName = new QName("TestService");
    private QName operationName = new QName("Operation_1");

    private ConfigurationContext configurationContext = null;
    private ServiceGroupContext serviceGroupContext = null;
    private ServiceContext serviceContext = null;
    private OperationContext operationContext = null;

    private AxisConfiguration axisConfiguration = null;
    private AxisServiceGroup axisServiceGroup = null;
    private AxisService axisService = null;
    private AxisOperation axisOperation = null;

    private TransportOutDescription transportOut = null;
    private TransportInDescription transportIn = null;

    private MessageContext mc = null;

    private ArrayList executedHandlers = null;

    private String testArg = null;


    public OperationContextSaveTest(String arg0) {
        super(arg0);
        testArg = new String(arg0);

        try {
            prepare();
        }
        catch (Exception e) {
            log.debug("OperationContextSaveTest:constructor:  error in setting up object graph [" +
                    e.getClass().getName() + " : " + e.getMessage() + "]");
        }
    }


    //
    // prepare the object hierarchy for testing
    //
    private void prepare() throws Exception {
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

        serviceGroupContext =
                configurationContext.createServiceGroupContext(axisService.getAxisServiceGroup());
        serviceGroupContext.setId("ServiceGroupContextTest");

        serviceContext = serviceGroupContext.getServiceContext(axisService);

        operationContext = serviceContext.createOperationContext(operationName);

        //-----------------------------------------------------------------

        transportOut = new TransportOutDescription("null");
        transportOut.setSender(new CommonsHTTPTransportSender());

        transportIn = new TransportInDescription("null");

        //-----------------------------------------------------------------

        mc = configurationContext.createMessageContext();
        mc.setTransportIn(transportIn);
        mc.setTransportOut(transportOut);
        mc.setTransportOut(transportOut);

        mc.setServerSide(true);
        mc.setProperty(MessageContext.TRANSPORT_OUT, System.out);
        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(omFac.getDefaultEnvelope());

        Phase phase1 = new Phase("beginPhase1");
        phase1.addHandler(new TempHandler(1));
        phase1.addHandler(new TempHandler(2));
        phase1.addHandler(new TempHandler(3));

        Phase phase2 = new Phase("middlePhase2");
        phase2.addHandler(new TempHandler(4));
        phase2.addHandler(new TempHandler(5));
        phase2.addHandler(new TempHandler(6));
        phase2.addHandler(new TempHandler(7));
        phase2.addHandler(new TempHandler(8));

        Phase phase3 = new Phase("lastPhase3");
        phase3.addHandler(new TempHandler(9));
        phase3.addHandler(new TempHandler(10));

        axisOperation.getRemainingPhasesInFlow().add(phase1);
        axisOperation.getRemainingPhasesInFlow().add(phase2);
        axisOperation.getRemainingPhasesInFlow().add(phase3);

        mc.setWSAAction(operationName.getLocalPart());
        mc.setSoapAction(operationName.getLocalPart());
        System.out.flush();

        mc.setMessageID(UUIDGenerator.getUUID());

        //operationContext.addMessageContext(mc);  gets done via the register
        axisOperation.registerOperationContext(mc, operationContext);
        mc.setOperationContext(operationContext);
        mc.setServiceContext(serviceContext);

        mc.setTo(new EndpointReference("axis2/services/NullService"));
        mc.setWSAAction("DummyOp");

        //-----------------------------------------------------------------

        executedHandlers = new ArrayList();

    }


    protected void setUp() throws Exception {
        //org.apache.log4j.BasicConfigurator.configure();
    }


    public void testSaveAndRestore() throws Exception {
        File theFile = null;
        String theFilename = null;
        boolean saved = false;
        boolean restored = false;
        boolean done = false;
        boolean comparesOk = false;

        log.debug("OperationContextSaveTest:testSaveAndRestore():  BEGIN ---------------");

        // ---------------------------------------------------------
        // setup a temporary file to use
        // ---------------------------------------------------------
        try {
            theFile = File.createTempFile("OpCtxSave", null);
            theFilename = theFile.getName();
            log.debug("OperationContextSaveTest:testSaveAndRestore(): temp file = [" +
                    theFilename + "]");
        }
        catch (Exception ex) {
            log.debug(
                    "OperationContextSaveTest:testSaveAndRestore(): error creating temp file = [" +
                            ex.getMessage() + "]");
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
                log.debug("OperationContextSaveTest:testSaveAndRestore(): saving .....");
                saved = false;
                outObjStream.writeObject(operationContext);

                // close out the streams
                outObjStream.flush();
                outObjStream.close();
                outStream.flush();
                outStream.close();

                saved = true;
                log.debug(
                        "OperationContextSaveTest:testSaveAndRestore(): ....save operation completed.....");

                long filesize = theFile.length();
                log.debug("OperationContextSaveTest:testSaveAndRestore(): file size after save [" +
                        filesize + "]   temp file = [" + theFilename + "]");

            }
            catch (Exception ex2) {
                log.debug("OperationContextSaveTest:testSaveAndRestore(): error during save [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(saved);

            // ---------------------------------------------------------
            // restore from the temporary file
            // ---------------------------------------------------------
            try {
                // setup an input stream to the file
                FileInputStream inStream = new FileInputStream(theFile);

                // attach a stream capable of reading objects from the 
                // stream connected to the file
                ObjectInputStream inObjStream = new ObjectInputStream(inStream);

                // try to restore the context
                log.debug("OperationContextSaveTest:testSaveAndRestore(): restoring .....");
                restored = false;
                OperationContext opctx_restored = (OperationContext) inObjStream.readObject();
                inObjStream.close();
                inStream.close();

                opctx_restored.activate(configurationContext);

                restored = true;
                log.debug(
                        "OperationContextSaveTest:testSaveAndRestore(): ....restored operation completed.....");

                // compare to original
                comparesOk = opctx_restored.isEquivalent(operationContext);
                log.debug(
                        "OperationContextSaveTest:testSaveAndRestore():  OperationContext equivalency [" +
                                comparesOk + "]");
                assertTrue(comparesOk);

                ServiceContext restored_srvCtx = opctx_restored.getServiceContext();
                
                ServiceGroupContext restored_sgCtx = restored_srvCtx.getServiceGroupContext();
                comparesOk = restored_sgCtx.isEquivalent(serviceGroupContext);
                log.debug(
                        "OperationContextSaveTest:testSaveAndRestore():  ServiceGroupContext equivalency [" +
                                comparesOk + "]");
                assertTrue(comparesOk);

            }
            catch (Exception ex2) {
                log.debug("OperationContextSaveTest:testSaveAndRestore(): error during restore [" +
                        ex2.getClass().getName() + " : " + ex2.getMessage() + "]");
                ex2.printStackTrace();
            }

            assertTrue(restored);

            // if the save/restore of the operation context succeeded,
            // then don't keep the temporary file around
            boolean removeTmpFile = saved && restored && comparesOk;
            if (removeTmpFile) {
                try {
                    theFile.delete();
                }
                catch (Exception e) {
                    // just absorb it
                }
            }

            // indicate that the temp file was created ok
            done = true;
        }

        // this is false when there are problems with the temporary file
        assertTrue(done);

        log.debug("OperationContextSaveTest:testSaveAndRestore():  END ---------------");
    }


    public class TempHandler extends AbstractHandler {
        private Integer index;

        //-----------------------------------------------------------------
        // constructors
        //-----------------------------------------------------------------

        public TempHandler(int index) {
            this.index = new Integer(index);
            init(new HandlerDescription(new String("handler" + index)));
        }

        //-----------------------------------------------------------------
        // methods
        //-----------------------------------------------------------------

        public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
            log.debug("TempHandler:invoke(): index = [" + index + "]");
            executedHandlers.add(index);
            return InvocationResponse.CONTINUE;
        }

    }

}
