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
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.http.CommonsHTTPTransportSender;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

public class EngineWithoutPhaseResolvingTest extends TestCase {
    private MessageContext mc;
    private QName serviceName = new QName("axis2/services/NullService");
    private QName operationName = new QName("NullOperation");
    private ConfigurationContext configContext;

    public EngineWithoutPhaseResolvingTest() {
    }

    public EngineWithoutPhaseResolvingTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {

        AxisConfiguration engineRegistry = new AxisConfiguration();
        configContext = new ConfigurationContext(engineRegistry);

        TransportOutDescription transport = new TransportOutDescription("null");
        transport.setSender(new CommonsHTTPTransportSender());

        TransportInDescription transportIn = new TransportInDescription("null");
        AxisOperation axisOp = new InOutAxisOperation(operationName);

        AxisService service = new AxisService(serviceName.getLocalPart());
        axisOp.setMessageReceiver(new MessageReceiver() {
            public void receive(MessageContext messageCtx) throws AxisFault {
                // TODO Auto-generated method stub

            }
        });
        engineRegistry.addService(service);
        service.addOperation(axisOp);

        mc = configContext.createMessageContext();
        mc.setTransportIn(transportIn);
        mc.setTransportOut(transport);

        ServiceGroupContext sgc = configContext.createServiceGroupContext(
                service.getAxisServiceGroup());
        ServiceContext sc = sgc.getServiceContext(service);

        OperationContext opContext = sc.createOperationContext(axisOp);

        mc.setOperationContext(opContext);
        mc.setTransportOut(transport);
        mc.setProperty(MessageContext.TRANSPORT_OUT, System.out);
        mc.setServerSide(true);
        SOAPFactory omFac = OMAbstractFactory.getSOAP11Factory();
        mc.setEnvelope(omFac.getDefaultEnvelope());


        mc.setWSAAction(operationName.getLocalPart());
        mc.setSoapAction(operationName.getLocalPart());
        System.out.flush();
    }

    public void testServerReceive() throws Exception {
        mc.setTo(
                new EndpointReference("axis2/services/NullService"));
        mc.setServerSide(true);
        AxisEngine.receive(mc);
    }

    public class NullMessageReceiver extends AbstractInOutSyncMessageReceiver {

        public void invokeBusinessLogic(MessageContext inMessage,
                                        MessageContext outMessage)
                throws AxisFault {

        }
    }
}
